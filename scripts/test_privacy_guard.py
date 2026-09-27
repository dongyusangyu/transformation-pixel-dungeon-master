import os
from pathlib import Path
import subprocess
import tempfile
import unittest

from privacy_guard import forbidden_path, scan


class PrivacyGuardTest(unittest.TestCase):
    def test_private_paths(self):
        paths = [".private/server-admin/console.py", "server/admin_console.py",
                 "core/src/main/assets/admin_console.py", "server/challenge_analysis.json",
                 "server/cumulative_win_rate_analysis.json", "server/deploy_sync_protocol.py",
                 "server/deployment_reports/report.md", "server/cloud_data/live.sqlite3-wal",
                 "server/node_modules/library/index.js", "server/__pycache__/cache.pyc",
                 "android/.env", "docs/design.md", "SERVER/ADMIN_CONSOLE.PY",
                 ".private\\player-data\\record.json"]
        for path in paths:
            with self.subTest(path=path):
                self.assertTrue(forbidden_path(path))

    def test_public_paths(self):
        for path in ["server/cloud_backend.py", "server/test_cloud_http.py",
                     "gradle/wrapper/gradle-wrapper.jar", "SPD-classes/src/main/java/Game.java",
                     "core/src/main/assets/sprites/hero.png", "scripts/privacy_guard.py",
                     ".github/workflows/privacy.yml", ".env.example"]:
            with self.subTest(path=path):
                self.assertFalse(forbidden_path(path))

    def setUp(self):
        self.original = os.getcwd()
        self.temporary = tempfile.TemporaryDirectory()
        os.chdir(self.temporary.name)
        self.command("init", "-q")
        self.command("config", "user.name", "Privacy Test")
        self.command("config", "user.email", "test@example.invalid")

    def tearDown(self):
        os.chdir(self.original)
        self.temporary.cleanup()

    def command(self, *arguments):
        return subprocess.check_output(["git", *arguments], stderr=subprocess.STDOUT)

    def stage(self, name, content):
        file = Path(name)
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(content, encoding="utf-8")
        self.command("add", "-f", "--", name)

    def test_force_added_private_file_is_blocked(self):
        self.stage(".private/player-data/example.json", "{}")
        self.assertEqual(scan(), [".private/player-data/example.json"])

    def test_deletion_is_allowed(self):
        self.stage("server/admin_console.py", "# private")
        self.command("commit", "-qm", "fixture")
        self.command("rm", "server/admin_console.py")
        self.assertEqual(scan(), [])

    def test_scans_index_not_working_copy(self):
        self.stage("safe.py", "value = 1")
        Path("safe.py").write_text("ADMIN_" + "ACCOUNTS = {}", encoding="utf-8")
        self.assertEqual(scan(), [])

    def test_renamed_credential_is_blocked(self):
        self.stage("renamed.txt", "gh" + "p_" + "A" * 36)
        self.assertEqual(scan(), ["renamed.txt"])

    def test_previous_private_revision_is_blocked(self):
        self.stage("server/challenge_analysis.json", "{}")
        self.command("commit", "-qm", "fixture")
        self.command("rm", "server/challenge_analysis.json")
        self.command("commit", "-qm", "delete fixture")
        self.assertEqual(scan("HEAD"), [])
        self.assertIn("server/challenge_analysis.json", scan(history=True))


if __name__ == "__main__":
    unittest.main()
