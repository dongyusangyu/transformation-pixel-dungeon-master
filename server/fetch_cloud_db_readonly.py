import os
import time
from pathlib import Path

import paramiko


HOST = "103.236.98.149"
PORT = 44140
USER = "root"
REMOTE_DIR = "/opt/tpd-cloud"
REMOTE_DB = f"{REMOTE_DIR}/cloud_data/talent_cloud.sqlite3"


def main():
    output = Path(os.environ["TPD_CLOUD_DB_OUTPUT"]).resolve()
    password = os.environ["TPD_SSH_PASSWORD"]
    stamp = time.strftime("%Y%m%d_%H%M%S")
    remote_copy = f"/tmp/tpd_cloud_analysis_{stamp}.sqlite3"

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, port=PORT, username=USER, password=password, timeout=20)

    def run(command):
        _, stdout, stderr = client.exec_command(command, timeout=60)
        result = stdout.read().decode("utf-8", "replace").strip()
        error = stderr.read().decode("utf-8", "replace").strip()
        if stdout.channel.recv_exit_status() != 0:
            raise RuntimeError(f"remote command failed: {command}\n{error}\n{result}")
        return result

    run(
        "python3 -c \"import sqlite3; "
        f"s=sqlite3.connect('{REMOTE_DB}'); "
        f"d=sqlite3.connect('{remote_copy}'); "
        "s.backup(d); d.close(); s.close()\""
    )
    integrity = run(
        "python3 -c \"import sqlite3; "
        f"print(sqlite3.connect('{remote_copy}').execute('PRAGMA integrity_check').fetchone()[0])\""
    )
    if integrity != "ok":
        raise RuntimeError(f"remote analysis copy failed integrity check: {integrity}")

    output.parent.mkdir(parents=True, exist_ok=True)
    sftp = client.open_sftp()
    sftp.get(remote_copy, str(output))
    sftp.close()
    run(f"rm -f {remote_copy}")
    client.close()
    print(f"downloaded={output}")
    print(f"integrity={integrity}")


if __name__ == "__main__":
    main()
