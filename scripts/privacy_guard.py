"""Reject known private paths and obvious credentials without printing contents."""

import argparse
from pathlib import PurePosixPath
import subprocess
import sys


PRIVATE_NAMES = {
    "admin_console.py", "challenge_analysis.json",
    "cumulative_win_rate_analysis.json", "talent-cloud-admin.service",
    "talent-cloud-snapshot.service", "talent-cloud-snapshot.timer",
    "fetch_cloud_db_readonly.py", "inspect_cloud_rankings.py",
    "create_challenge_excel.mjs", "verify_challenge_excel.mjs",
}
PRIVATE_DIRECTORIES = {
    ".private", "node_modules", "__pycache__", "cloud_data", "cloud_snapshots",
    "deployment_reports", ".codex-artifacts", ".playwright-cli",
}
SECRET_PATTERN = (
    r"github_pat_[A-Za-z0-9_]{30,}|gh[pousr]_[A-Za-z0-9]{30,}|"
    r"-----BEGIN (RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----|"
    r"^[[:space:]]*ADMIN_ACCOUNTS[[:space:]]*="
)


def forbidden_path(path):
    parts = PurePosixPath(path.replace("\\", "/")).parts
    lowered = tuple(part.lower() for part in parts)
    if not lowered:
        return False
    name = lowered[-1]
    if any(part in PRIVATE_DIRECTORIES for part in lowered):
        return True
    if name in PRIVATE_NAMES or (name.startswith(".env") and name != ".env.example"):
        return True
    if name.endswith((".sqlite", ".sqlite3", ".db", ".pyc", ".pyo", ".jks", ".keystore")):
        return True
    if any(marker in name for marker in (".sqlite3-", ".sqlite-", ".db-")):
        return True
    if lowered[0] in {"docs", "tools", "pixel-art", ".idea", "tmp", "pw_stage"}:
        return True
    if lowered[0] == "server" and name.startswith(("deploy_", "analyze_")):
        return True
    return "agentmin" in lowered and "training" in lowered and "logs" in lowered


def git(*arguments):
    return subprocess.check_output(["git", *arguments])


def scan(revision=None, history=False):
    if history:
        lines = git("rev-list", "--objects", "--all").decode("utf-8", "surrogateescape").splitlines()
        paths = [line.partition(" ")[2] for line in lines if " " in line]
    else:
        arguments = ("ls-tree", "-r", "--name-only", "-z", revision) if revision else ("ls-files", "--cached", "-z")
        paths = git(*arguments).decode("utf-8", "surrogateescape").split("\0")
    violations = sorted({path for path in paths if path and forbidden_path(path)})
    if not history:
        arguments = ["git", "grep", "-l", "-I", "-E"]
        if not revision:
            arguments.append("--cached")
        arguments.extend(["-e", SECRET_PATTERN])
        if revision:
            arguments.append(revision)
        arguments.append("--")
        result = subprocess.run(arguments, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        if result.returncode not in (0, 1):
            raise RuntimeError("Unable to check tracked contents for credentials")
        violations.extend(result.stdout.decode("utf-8", "replace").splitlines())
    return sorted(set(violations))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--index", action="store_true")
    mode.add_argument("--revision")
    mode.add_argument("--history", action="store_true")
    arguments = parser.parse_args()
    try:
        violations = scan(arguments.revision, arguments.history)
    except (subprocess.CalledProcessError, RuntimeError) as error:
        print("Privacy check could not run: " + str(error), file=sys.stderr)
        return 2
    if violations:
        print("Blocked: private paths or credentials are present in Git:", file=sys.stderr)
        for path in violations:
            print("  " + path, file=sys.stderr)
        return 1
    print("Privacy check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
