import unittest
import io
import json
import tempfile
from pathlib import Path

from server.cloud_backend import TalentCloudStore, CloudHandler


class CloudRankingsSyncTest(unittest.TestCase):
    def record(self, game_id, score, cycle=False):
        return {"gameID": game_id, "score": score, "new_cycle": cycle, "win": True}

    def merge(self, old, new):
        return TalentCloudStore._merge_rankings(old, new)

    def test_reset_generation_clears_old_records_and_counters(self):
        old = {"records": [self.record("old", 100)], "total": 20, "won": 10}
        new = {"sync_generation": 1, "records": [], "total": 0, "won": 0}
        merged = self.merge(old, new)
        self.assertEqual([], merged["records"])
        self.assertEqual(0, merged["total"])
        self.assertEqual(0, merged["won"])

    def test_delayed_pre_reset_upload_cannot_resurrect_records(self):
        old = {"sync_generation": 2, "records": [], "total": 0, "won": 0}
        merged = self.merge(old, {"records": [self.record("old", 100)], "total": 20})
        self.assertEqual([], merged["records"])
        self.assertEqual(0, merged["total"])

    def test_tombstone_survives_stale_and_legacy_uploads(self):
        old = {"records": [self.record("deleted", 100), self.record("kept", 1)]}
        deleted = self.merge(old, {"deleted_game_ids": ["deleted"], "records": []})
        merged = self.merge(deleted, old)
        self.assertEqual(["kept"], [r["gameID"] for r in merged["records"]])
        self.assertEqual(["deleted"], merged["deleted_game_ids"])

    def test_empty_legacy_upload_is_not_a_delete(self):
        merged = self.merge({"records": [self.record("old", 100)]}, {"records": []})
        self.assertEqual("old", merged["records"][0]["gameID"])

    def test_separate_cycle_limits_and_latest_is_protected(self):
        old = {"records": [self.record(f"normal-{i}", 100 + i) for i in range(12)]
                          + [self.record(f"cycle-{i}", 200 + i, True) for i in range(12)]}
        merged = self.merge(old, {"records": [self.record("latest", 0.5, True)], "latest": 0})
        self.assertEqual(11, sum(not r["new_cycle"] for r in merged["records"]))
        self.assertEqual(11, sum(r["new_cycle"] for r in merged["records"]))
        self.assertEqual("latest", merged["records"][merged["latest"]]["gameID"])
        self.assertEqual("latest", merged["latest_game_id"])

    def test_latest_index_is_resolved_before_sorting(self):
        merged = self.merge({"records": [self.record("old", 100)]},
                            {"records": [self.record("new", 10)], "latest": 0})
        self.assertEqual("new", merged["records"][merged["latest"]]["gameID"])

    def test_preserves_cycle_counters_restart_reservations_and_unknown_fields(self):
        merged = self.merge({"restart_source_game_ids": ["a"], "future_field": "kept"},
                            {"new_cycle_total": 50, "new_cycle_won": 30,
                             "restart_source_game_ids": ["b"]})
        self.assertEqual(50, merged["new_cycle_total"])
        self.assertEqual(30, merged["new_cycle_won"])
        self.assertEqual(["a", "b"], merged["restart_source_game_ids"])
        self.assertEqual("kept", merged["future_field"])

    def test_fractional_scores_and_ties_match_java_comparator(self):
        merged = self.merge({}, {"records": [self.record("a", 1.1), self.record("b", 1.2)]})
        self.assertEqual(["b", "a"], [r["gameID"] for r in merged["records"]])
        merged = self.merge({}, {"records": [self.record("a", 1), self.record("b", 1)]})
        self.assertEqual(["b", "a"], [r["gameID"] for r in merged["records"]])

    def test_older_device_snapshot_does_not_change_latest_or_archive(self):
        old = {"records": [self.record("new", 1)], "latest": 0,
               "hero_hall_records": [], "sync_device_revisions": {"device": 5}}
        merged = self.merge(old, {"sync_device_key": "device", "sync_device_revision": 4,
                                  "records": [self.record("old", 100)], "latest": 0,
                                  "hero_hall_records": [self.record("old", 100)]})
        self.assertEqual(["new"], [r["gameID"] for r in merged["records"]])
        self.assertEqual([], merged["hero_hall_records"])

    def test_hero_hall_tombstone_prevents_resurrection(self):
        old = {"hero_hall_records": [self.record("removed", 100)]}
        deleted = self.merge(old, {"hero_hall_records": [], "deleted_hero_hall_ids": ["removed"]})
        merged = self.merge(deleted, old)
        self.assertEqual([], merged["hero_hall_records"])

    def test_explicit_readding_to_hero_hall_removes_tombstone(self):
        old = {"hero_hall_records": [], "deleted_hero_hall_ids": ["readded"]}
        merged = self.merge(old, {"hero_hall_records": [self.record("readded", 1)],
                                  "restored_hero_hall_ids": ["readded"]})
        self.assertEqual("readded", merged["hero_hall_records"][0]["gameID"])
        self.assertEqual([], merged["deleted_hero_hall_ids"])

    def test_http_upload_only_acknowledges_and_backup_remains_downloadable(self):
        with tempfile.TemporaryDirectory(ignore_cleanup_errors=True) as directory:
            store = TalentCloudStore(Path(directory) / "cloud.sqlite3")
            handler = CloudHandler.__new__(CloudHandler)
            handler.store = store
            handler.path = "/api/upload"
            handler.client_address = ("127.0.0.1", 1)
            data = {"device_key": "test-device", "device_ip": "test-device",
                    "global_data": {"rankings": {"records": [self.record("saved", 100)]},
                                    "badges": {"badges": ["VICTORY"]}}, "talent_stats": {}}
            body = json.dumps(data).encode("utf-8")
            handler.headers = {"Content-Length": str(len(body))}
            handler.rfile = io.BytesIO(body)
            responses = []
            handler._send_json = lambda payload, status=200: responses.append((payload, status))
            handler.do_POST()
            response, status = responses[0]
            self.assertEqual(200, status)
            self.assertTrue(response["ok"])
            self.assertNotIn("global_data", response)
            self.assertNotIn("talent_stats", response)
            downloaded = store.download(player_uuid=response["player_uuid"])
            self.assertEqual("saved", downloaded["global_data"]["rankings"]["records"][0]["gameID"])
            self.assertEqual(["VICTORY"], downloaded["global_data"]["badges"]["badges"])

    def test_database_keeps_reset_when_older_upload_arrives_last(self):
        with tempfile.TemporaryDirectory(ignore_cleanup_errors=True) as directory:
            store = TalentCloudStore(Path(directory) / "cloud.sqlite3")
            first = store.upload("", "device", "", {"rankings": {
                "records": [self.record("old", 100)], "sync_device_revision": 1}}, {})
            uuid = first["player_uuid"]
            store.upload(uuid, "device", "", {"rankings": {
                "sync_generation": 2, "sync_device_revision": 3, "records": [], "total": 0}}, {})
            late = store.upload(uuid, "device", "", {"rankings": {
                "records": [self.record("old", 100)], "sync_device_revision": 2}}, {})
            self.assertFalse(late["rankings_accepted"])
            self.assertEqual([], store.download(player_uuid=uuid)["global_data"]["rankings"]["records"])


if __name__ == "__main__":
    unittest.main()
