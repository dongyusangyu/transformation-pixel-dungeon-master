import json
import os
import time

import paramiko


HOST = "103.236.98.149"
PORT = 44140
USER = "root"
REMOTE_DIR = "/opt/tpd-cloud"
REMOTE_BACKEND = f"{REMOTE_DIR}/cloud_backend.py"
REMOTE_DB = f"{REMOTE_DIR}/cloud_data/talent_cloud.sqlite3"


def main():
    password = os.environ["TPD_SSH_PASSWORD"]
    stamp = time.strftime("%Y%m%d_%H%M%S")
    backup_dir = f"{REMOTE_DIR}/manual_backups"
    backend_backup = f"{backup_dir}/cloud_backend_pre_restore_tx_{stamp}.py"
    database_backup = f"{backup_dir}/talent_cloud_pre_restore_tx_{stamp}.sqlite3"
    local_backend = os.path.join(os.path.dirname(__file__), "cloud_backend.py")
    remote_temp = f"{REMOTE_BACKEND}.new"

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(
        HOST,
        port=PORT,
        username=USER,
        password=password,
        timeout=20,
    )

    def run(command):
        _, stdout, stderr = client.exec_command(command, timeout=60)
        output = stdout.read().decode("utf-8", "replace").strip()
        error = stderr.read().decode("utf-8", "replace").strip()
        exit_code = stdout.channel.recv_exit_status()
        if exit_code != 0:
            raise RuntimeError(
                f"command failed ({exit_code}): {command}\n{error}\n{output}"
            )
        return output

    run(f"mkdir -p {backup_dir}")
    run(f"cp -a {REMOTE_BACKEND} {backend_backup}")
    run(
        "python3 -c \"import sqlite3; "
        f"s=sqlite3.connect('{REMOTE_DB}'); "
        f"d=sqlite3.connect('{database_backup}'); "
        "s.backup(d); d.close(); s.close()\""
    )
    backup_integrity = run(
        "python3 -c \"import sqlite3; "
        f"print(sqlite3.connect('{database_backup}')"
        ".execute('PRAGMA integrity_check').fetchone()[0])\""
    )
    if backup_integrity != "ok":
        raise RuntimeError(f"backup integrity check failed: {backup_integrity}")

    count_query = (
        "import sqlite3,json; "
        f"d=sqlite3.connect('{REMOTE_DB}'); "
        "print(json.dumps({"
        "'players':d.execute('SELECT COUNT(*) FROM player_cloud_data').fetchone()[0],"
        "'allowed':d.execute('SELECT COUNT(*) FROM player_cloud_data "
        "WHERE restore_allowed=1').fetchone()[0]}))"
    )
    before = json.loads(run(f'python3 -c "{count_query}"'))

    sftp = client.open_sftp()
    sftp.put(local_backend, remote_temp)
    sftp.close()
    run(f"python3 -m py_compile {remote_temp}")
    run(f"mv {remote_temp} {REMOTE_BACKEND}")
    run("systemctl restart talent-cloud.service")
    time.sleep(2)
    active = run("systemctl is-active talent-cloud.service")
    if active != "active":
        raise RuntimeError(f"service is not active: {active}")

    after_query = (
        "import sqlite3,json; "
        f"d=sqlite3.connect('{REMOTE_DB}'); "
        "print(json.dumps({"
        "'integrity':d.execute('PRAGMA integrity_check').fetchone()[0],"
        "'players':d.execute('SELECT COUNT(*) FROM player_cloud_data').fetchone()[0],"
        "'allowed':d.execute('SELECT COUNT(*) FROM player_cloud_data "
        "WHERE restore_allowed=1').fetchone()[0],"
        "'restore_sessions':d.execute(\\\"SELECT COUNT(*) FROM sqlite_master "
        "WHERE type='table' AND name='restore_sessions'\\\").fetchone()[0]}))"
    )
    after = json.loads(run(f'python3 -c "{after_query}"'))
    aggregate = json.loads(
        run("curl -fsS http://127.0.0.1:44140/api/aggregate")
    )
    if not aggregate.get("ok"):
        raise RuntimeError("aggregate endpoint returned invalid response")
    if before != {key: after[key] for key in ("players", "allowed")}:
        raise RuntimeError(
            f"database counts changed during deploy: before={before}, after={after}"
        )

    print(
        json.dumps(
            {
                "backend_backup": backend_backup,
                "database_backup": database_backup,
                "backup_integrity": backup_integrity,
                "service": active,
                "before": before,
                "after": after,
                "aggregate_ok": True,
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    client.close()


if __name__ == "__main__":
    main()
