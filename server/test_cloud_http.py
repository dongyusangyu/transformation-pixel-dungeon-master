"""Exercise the real HTTP protocol against disposable local databases."""

import gc
import json
import sqlite3
import tempfile
import threading
import unittest
from http.server import ThreadingHTTPServer
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

from server.cloud_backend import CloudHandler, TalentCloudStore


class CloudHttpTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.database = Path(self.directory.name) / "cloud.sqlite3"
        self.store = TalentCloudStore(self.database)

        class Handler(CloudHandler):
            store = self.store

            def log_message(self, *args):
                pass

        self.http = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        self.thread = threading.Thread(
            target=lambda: self.http.serve_forever(poll_interval=0.01), daemon=True)
        self.thread.start()
        self.base = "http://127.0.0.1:" + str(self.http.server_port)
        self.player_uuid = self.upload()[1]["player_uuid"]

    def tearDown(self):
        self.http.shutdown()
        self.http.server_close()
        self.thread.join(timeout=5)
        # SQLite context managers commit transactions but leave connections open.
        # Collect their cycles before removing database files on Windows.
        gc.collect()
        self.directory.cleanup()

    def request(self, path, payload=None):
        data = None if payload is None else json.dumps(payload).encode()
        request = Request(self.base + path, data=data,
                          headers={"Content-Type": "application/json"})
        try:
            with urlopen(request, timeout=5) as response:
                return response.status, json.load(response)
        except HTTPError as error:
            with error:
                return error.code, json.load(error)

    def upload(self, selected=2):
        return self.request("/api/upload", {
            "device_key": "android:http-regression-source",
            "global_data": {"rankings": {"total": 5, "won": 2}},
            "talent_stats": {"TEST_TALENT": {
                "selected": selected, "appeared": 5, "targeted": 1}},
        })

    def prepare(self):
        with sqlite3.connect(self.database) as db:
            db.execute("UPDATE player_cloud_data SET restore_allowed=1 WHERE player_uuid=?",
                       (self.player_uuid,))
        return self.request("/api/download?" + urlencode({
            "player_uuid": self.player_uuid,
            "device_key": "android:http-regression-target",
            "prepare_restore": "1",
        }))

    def commit(self, token):
        return self.request("/api/restore/commit", {
            "player_uuid": self.player_uuid,
            "device_key": "android:http-regression-target",
            "restore_token": token,
        })

    def permission(self):
        with sqlite3.connect(self.database) as db:
            return db.execute("SELECT restore_allowed FROM player_cloud_data WHERE player_uuid=?",
                              (self.player_uuid,)).fetchone()[0]

    def test_upload_returns_ack_and_aggregate_is_separate(self):
        status, response = self.upload()
        self.assertEqual(200, status)
        self.assertEqual({"ok", "player_uuid", "device_key", "rankings_accepted"},
                         set(response))
        self.assertTrue(response["rankings_accepted"])
        status, aggregate = self.request("/api/aggregate")
        self.assertEqual(200, status)
        self.assertTrue(aggregate["ok"])
        self.assertEqual(2, aggregate["aggregate"]["TEST_TALENT"]["selected"])

    def test_restore_commit_consumes_permission_and_rebinds_device(self):
        status, prepared = self.prepare()
        self.assertEqual(200, status)
        self.assertIn("global_data", prepared)
        self.assertEqual(1, self.permission())
        self.assertEqual(200, self.commit(prepared["restore_token"])[0])
        self.assertEqual(0, self.permission())
        with sqlite3.connect(self.database) as db:
            mapped = db.execute("SELECT player_uuid FROM device_uuid_map WHERE device_key=?",
                                ("android:http-regression-target",)).fetchone()[0]
        self.assertEqual(self.player_uuid, mapped)

    def test_changed_backup_rejects_commit_without_consuming_permission(self):
        prepared = self.prepare()[1]
        self.assertEqual(200, self.upload(selected=3)[0])
        status, response = self.commit(prepared["restore_token"])
        self.assertEqual(409, status)
        self.assertEqual("restore_commit_failed", response["error"])
        self.assertEqual(1, self.permission())

    def test_restore_without_permission_returns_403(self):
        status, response = self.request("/api/download?" + urlencode({
            "player_uuid": self.player_uuid,
            "device_key": "android:http-regression-target", "prepare_restore": "1"}))
        self.assertEqual(403, status)
        self.assertEqual("restore_not_allowed", response["error"])
        self.assertNotIn("global_data", response)

    def test_legacy_empty_upload_preserves_personal_backup(self):
        status, response = self.request("/api/upload", {
            "device_key": "android:http-regression-source", "global_data": {},
            "talent_stats": {}})
        self.assertEqual(200, status)
        self.assertNotIn("global_data", response)
        prepared = self.prepare()[1]
        self.assertEqual(5, prepared["global_data"]["rankings"]["total"])
        self.assertEqual(2, prepared["talent_stats"]["TEST_TALENT"]["selected"])


if __name__ == "__main__":
    unittest.main()
