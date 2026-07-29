import json
import sqlite3
import tempfile
import unittest
from pathlib import Path

from server.cloud_backend import TalentCloudStore


class CloudRestoreTest(unittest.TestCase):

    UUID_A = "11111111-1111-4111-8111-111111111111"
    UUID_B = "22222222-2222-4222-8222-222222222222"
    CURRENT_DEVICE = "android:current-device"
    UNKNOWN_UUID = "33333333-3333-4333-8333-333333333333"

    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory(ignore_cleanup_errors=True)
        self.db_path = Path(self.temp_dir.name) / "cloud.sqlite3"
        self.store = TalentCloudStore(self.db_path)
        with sqlite3.connect(self.db_path) as db:
            self._insert_player(db, self.CURRENT_DEVICE, self.UUID_A, {"owner": "A"}, restore_allowed=0)
            self._insert_player(db, "legacy-device-b", self.UUID_B, {"owner": "B"}, restore_allowed=1)
            self.store._bind_device_key(db, self.CURRENT_DEVICE, self.UUID_A)
            self.store._bind_device_key(db, "legacy-device-b", self.UUID_B)

    def tearDown(self):
        self.temp_dir.cleanup()

    @staticmethod
    def _insert_player(db, device_key, player_uuid, global_data, restore_allowed):
        db.execute(
            """
            INSERT INTO player_cloud_data(
                device_ip, player_uuid, global_data, talent_stats,
                updated_at, created_at, restore_allowed
            ) VALUES (?, ?, ?, ?, 1, 1, ?)
            """,
            (
                device_key,
                player_uuid,
                json.dumps(global_data),
                json.dumps({"TALENT": {"selected": 1, "appeared": 2, "targeted": 3}}),
                restore_allowed,
            ),
        )

    def test_restore_uuid_b_rebinds_current_device_without_deleting_uuid_a(self):
        result = self.store.download(
            player_uuid=self.UUID_B,
            device_key=self.CURRENT_DEVICE,
            legacy_device_ip="",
            consume_restore_permission=True,
        )

        self.assertEqual(self.UUID_B, result["player_uuid"])
        self.assertEqual({"owner": "B"}, result["global_data"])

        with sqlite3.connect(self.db_path) as db:
            mapped_uuid = db.execute(
                "SELECT player_uuid FROM device_uuid_map WHERE device_key = ?",
                (self.CURRENT_DEVICE,),
            ).fetchone()[0]
            restore_allowed = db.execute(
                "SELECT restore_allowed FROM player_cloud_data WHERE player_uuid = ?",
                (self.UUID_B,),
            ).fetchone()[0]
            player_uuids = {
                row[0] for row in db.execute("SELECT player_uuid FROM player_cloud_data")
            }

        self.assertEqual(self.UUID_B, mapped_uuid)
        self.assertEqual(0, restore_allowed)
        self.assertEqual({self.UUID_A, self.UUID_B}, player_uuids)

    def test_restore_permission_is_single_use_and_denial_does_not_rebind(self):
        first = self.store.download(
            player_uuid=self.UUID_B,
            device_key=self.CURRENT_DEVICE,
            consume_restore_permission=True,
        )
        second = self.store.download(
            player_uuid=self.UUID_B,
            device_key="android:second-device",
            consume_restore_permission=True,
        )

        self.assertEqual(self.UUID_B, first["player_uuid"])
        self.assertTrue(second["restore_denied"])
        with sqlite3.connect(self.db_path) as db:
            second_mapping = db.execute(
                "SELECT player_uuid FROM device_uuid_map WHERE device_key = ?",
                ("android:second-device",),
            ).fetchone()
        self.assertIsNone(second_mapping)

    def test_explicit_unknown_uuid_does_not_fall_back_to_current_device(self):
        with sqlite3.connect(self.db_path) as db:
            db.execute(
                "UPDATE player_cloud_data SET restore_allowed = 1 WHERE player_uuid = ?",
                (self.UUID_A,),
            )

        result = self.store.download(
            player_uuid=self.UNKNOWN_UUID,
            device_key=self.CURRENT_DEVICE,
            consume_restore_permission=True,
        )

        self.assertIsNone(result)
        with sqlite3.connect(self.db_path) as db:
            allowed = db.execute(
                "SELECT restore_allowed FROM player_cloud_data WHERE player_uuid = ?",
                (self.UUID_A,),
            ).fetchone()[0]
            mapped_uuid = db.execute(
                "SELECT player_uuid FROM device_uuid_map WHERE device_key = ?",
                (self.CURRENT_DEVICE,),
            ).fetchone()[0]
        self.assertEqual(1, allowed)
        self.assertEqual(self.UUID_A, mapped_uuid)

    def test_prepared_restore_consumes_permission_only_after_commit(self):
        prepared = self.store.download(
            player_uuid=self.UUID_B,
            device_key=self.CURRENT_DEVICE,
            prepare_restore=True,
        )

        with sqlite3.connect(self.db_path) as db:
            allowed_before_commit = db.execute(
                "SELECT restore_allowed FROM player_cloud_data WHERE player_uuid = ?",
                (self.UUID_B,),
            ).fetchone()[0]
            mapped_before_commit = db.execute(
                "SELECT player_uuid FROM device_uuid_map WHERE device_key = ?",
                (self.CURRENT_DEVICE,),
            ).fetchone()[0]

        committed = self.store.commit_restore(
            self.UUID_B,
            self.CURRENT_DEVICE,
            prepared["restore_token"],
        )
        committed_retry = self.store.commit_restore(
            self.UUID_B,
            self.CURRENT_DEVICE,
            prepared["restore_token"],
        )

        self.assertEqual(1, allowed_before_commit)
        self.assertEqual(self.UUID_A, mapped_before_commit)
        self.assertTrue(committed)
        self.assertTrue(committed_retry)
        with sqlite3.connect(self.db_path) as db:
            allowed_after_commit = db.execute(
                "SELECT restore_allowed FROM player_cloud_data WHERE player_uuid = ?",
                (self.UUID_B,),
            ).fetchone()[0]
            mapped_after_commit = db.execute(
                "SELECT player_uuid FROM device_uuid_map WHERE device_key = ?",
                (self.CURRENT_DEVICE,),
            ).fetchone()[0]
        self.assertEqual(0, allowed_after_commit)
        self.assertEqual(self.UUID_B, mapped_after_commit)

    def test_committed_restore_can_be_confirmed_after_original_expiry(self):
        prepared = self.store.download(
            player_uuid=self.UUID_B,
            device_key=self.CURRENT_DEVICE,
            prepare_restore=True,
        )
        token = prepared["restore_token"]
        self.assertTrue(
            self.store.commit_restore(self.UUID_B, self.CURRENT_DEVICE, token)
        )

        with sqlite3.connect(self.db_path) as db:
            db.execute(
                "UPDATE restore_sessions SET expires_at = 0 WHERE token = ?",
                (token,),
            )

        self.assertTrue(
            self.store.commit_restore(self.UUID_B, self.CURRENT_DEVICE, token)
        )

    def test_empty_uuid_uses_current_device_mapping(self):
        with sqlite3.connect(self.db_path) as db:
            db.execute(
                "UPDATE player_cloud_data SET restore_allowed = 1 WHERE player_uuid = ?",
                (self.UUID_A,),
            )

        prepared = self.store.download(
            player_uuid="",
            device_key=self.CURRENT_DEVICE,
            prepare_restore=True,
        )

        self.assertEqual(self.UUID_A, prepared["player_uuid"])
        self.assertTrue(prepared["restore_token"])
        with sqlite3.connect(self.db_path) as db:
            allowed = db.execute(
                "SELECT restore_allowed FROM player_cloud_data WHERE player_uuid = ?",
                (self.UUID_A,),
            ).fetchone()[0]
        self.assertEqual(1, allowed)

    def test_rebound_uuid_can_upload_without_deleting_original_player(self):
        prepared = self.store.download(
            player_uuid=self.UUID_B,
            device_key=self.CURRENT_DEVICE,
            prepare_restore=True,
        )
        self.assertTrue(
            self.store.commit_restore(
                self.UUID_B,
                self.CURRENT_DEVICE,
                prepared["restore_token"],
            )
        )

        uploaded = self.store.upload(
            self.UUID_B,
            self.CURRENT_DEVICE,
            "",
            {"owner": "B", "badges": {"wins": 2}},
            {"TALENT": {"selected": 2, "appeared": 3, "targeted": 4}},
        )

        self.assertEqual(self.UUID_B, uploaded["player_uuid"])
        with sqlite3.connect(self.db_path) as db:
            rows = db.execute(
                "SELECT player_uuid, device_ip FROM player_cloud_data ORDER BY player_uuid"
            ).fetchall()
        self.assertEqual([self.UUID_A, self.UUID_B], [row[0] for row in rows])
        self.assertEqual(self.CURRENT_DEVICE, rows[1][1])
        self.assertNotEqual(self.CURRENT_DEVICE, rows[0][1])


if __name__ == "__main__":
    unittest.main()
