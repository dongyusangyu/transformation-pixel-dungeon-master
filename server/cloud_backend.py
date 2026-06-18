#!/usr/bin/env python3
import argparse
import json
import re
import sqlite3
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse


def now_ms() -> int:
    return int(time.time() * 1000)


def day_key(timestamp_ms: int = None) -> str:
    return time.strftime("%Y-%m-%d", time.localtime((timestamp_ms or now_ms()) / 1000))


def looks_like_ipv4(value: str) -> bool:
    if not isinstance(value, str) or not re.match(r"^\d{1,3}(\.\d{1,3}){3}$", value):
        return False
    return all(0 <= int(part) <= 255 for part in value.split("."))


class TalentCloudStore:
    def __init__(self, db_path: Path):
        self.db_path = db_path
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self._init_db()

    def _connect(self):
        return sqlite3.connect(self.db_path)

    def _init_db(self):
        with self._connect() as db:
            db.execute(
                """
                CREATE TABLE IF NOT EXISTS player_cloud_data (
                    device_ip TEXT PRIMARY KEY,
                    global_data TEXT NOT NULL,
                    talent_stats TEXT NOT NULL,
                    updated_at INTEGER NOT NULL,
                    created_at INTEGER NOT NULL DEFAULT 0
                )
                """
            )
            columns = [row[1] for row in db.execute("PRAGMA table_info(player_cloud_data)").fetchall()]
            if "created_at" not in columns:
                db.execute("ALTER TABLE player_cloud_data ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
            db.execute("UPDATE player_cloud_data SET created_at = updated_at WHERE created_at = 0")
            db.execute(
                """
                CREATE TABLE IF NOT EXISTS device_blacklist (
                    device_ip TEXT PRIMARY KEY,
                    reason TEXT NOT NULL DEFAULT '',
                    updated_at INTEGER NOT NULL
                )
                """
            )
            db.execute(
                """
                CREATE TABLE IF NOT EXISTS daily_activity (
                    day TEXT PRIMARY KEY,
                    player_uploads INTEGER NOT NULL DEFAULT 0,
                    new_players INTEGER NOT NULL DEFAULT 0,
                    blacklist_added INTEGER NOT NULL DEFAULT 0,
                    selected_delta INTEGER NOT NULL DEFAULT 0,
                    appeared_delta INTEGER NOT NULL DEFAULT 0,
                    targeted_delta INTEGER NOT NULL DEFAULT 0
                )
                """
            )
            self._seed_activity_baseline(db)

    def is_blacklisted(self, device_ip: str):
        with self._connect() as db:
            row = db.execute(
                "SELECT 1 FROM device_blacklist WHERE device_ip = ?",
                (device_ip,),
            ).fetchone()
        return row is not None

    def upload(self, device_ip: str, global_data: dict, talent_stats: dict):
        timestamp = now_ms()
        with self._connect() as db:
            old_row = db.execute(
                "SELECT talent_stats FROM player_cloud_data WHERE device_ip = ?",
                (device_ip,),
            ).fetchone()
            old_totals = self._sum_stats(self._loads(old_row[0])) if old_row else (0, 0, 0)
            new_totals = self._sum_stats(talent_stats)
            db.execute(
                """
                INSERT INTO player_cloud_data(device_ip, global_data, talent_stats, updated_at, created_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(device_ip) DO UPDATE SET
                    global_data = excluded.global_data,
                    talent_stats = excluded.talent_stats,
                    updated_at = excluded.updated_at,
                    created_at = CASE
                        WHEN player_cloud_data.created_at = 0 THEN excluded.created_at
                        ELSE player_cloud_data.created_at
                    END
                """,
                (
                    device_ip,
                    json.dumps(global_data, ensure_ascii=False, separators=(",", ":")),
                    json.dumps(talent_stats, ensure_ascii=False, separators=(",", ":")),
                    timestamp,
                    timestamp,
                ),
            )
            self._record_daily(
                db,
                player_uploads=1,
                new_players=0 if old_row else 1,
                selected_delta=max(0, new_totals[0] - old_totals[0]),
                appeared_delta=max(0, new_totals[1] - old_totals[1]),
                targeted_delta=max(0, new_totals[2] - old_totals[2]),
            )

    def delete_legacy_ip_record(self, device_ip: str, local_ip: str):
        if not local_ip or device_ip == local_ip:
            return
        if looks_like_ipv4(device_ip) or not looks_like_ipv4(local_ip):
            return
        with self._connect() as db:
            db.execute("DELETE FROM player_cloud_data WHERE device_ip = ?", (local_ip,))

    def download(self, device_ip: str):
        with self._connect() as db:
            row = db.execute(
                "SELECT global_data, talent_stats, updated_at FROM player_cloud_data WHERE device_ip = ?",
                (device_ip,),
            ).fetchone()
        if row is None:
            return None
        return {
            "global_data": json.loads(row[0]),
            "talent_stats": json.loads(row[1]),
            "updated_at": row[2],
        }

    def aggregate(self):
        result = {}
        with self._connect() as db:
            rows = db.execute(
                """
                SELECT p.talent_stats
                FROM player_cloud_data p
                LEFT JOIN device_blacklist b ON b.device_ip = p.device_ip
                WHERE b.device_ip IS NULL
                """
            ).fetchall()
        for (talent_stats_json,) in rows:
            try:
                talent_stats = json.loads(talent_stats_json)
            except json.JSONDecodeError:
                continue
            for talent_name, stats in talent_stats.items():
                target = result.setdefault(
                    talent_name,
                    {"selected": 0, "appeared": 0, "targeted": 0},
                )
                target["selected"] += int(stats.get("selected", 0))
                target["appeared"] += int(stats.get("appeared", 0))
                target["targeted"] += int(stats.get("targeted", 0))
        return result

    def _seed_activity_baseline(self, db):
        if db.execute("SELECT 1 FROM daily_activity LIMIT 1").fetchone() is not None:
            return
        players = db.execute("SELECT COUNT(*) FROM player_cloud_data").fetchone()[0]
        blacklist = db.execute("SELECT COUNT(*) FROM device_blacklist").fetchone()[0]
        selected = appeared = targeted = 0
        rows = db.execute(
            """
            SELECT p.talent_stats
            FROM player_cloud_data p
            LEFT JOIN device_blacklist b ON b.device_ip = p.device_ip
            WHERE b.device_ip IS NULL
            """
        ).fetchall()
        for (talent_stats_json,) in rows:
            try:
                stats = json.loads(talent_stats_json)
            except json.JSONDecodeError:
                continue
            totals = self._sum_stats(stats)
            selected += totals[0]
            appeared += totals[1]
            targeted += totals[2]
        db.execute(
            """
            INSERT INTO daily_activity(day, new_players, blacklist_added, selected_delta, appeared_delta, targeted_delta)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            (day_key(), players, blacklist, selected, appeared, targeted),
        )

    @staticmethod
    def _record_daily(db, **increments):
        fields = [
            "player_uploads",
            "new_players",
            "blacklist_added",
            "selected_delta",
            "appeared_delta",
            "targeted_delta",
        ]
        values = {field: int(increments.get(field, 0) or 0) for field in fields}
        db.execute(
            """
            INSERT INTO daily_activity(day, player_uploads, new_players, blacklist_added, selected_delta, appeared_delta, targeted_delta)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(day) DO UPDATE SET
                player_uploads = player_uploads + excluded.player_uploads,
                new_players = new_players + excluded.new_players,
                blacklist_added = blacklist_added + excluded.blacklist_added,
                selected_delta = selected_delta + excluded.selected_delta,
                appeared_delta = appeared_delta + excluded.appeared_delta,
                targeted_delta = targeted_delta + excluded.targeted_delta
            """,
            (
                day_key(),
                values["player_uploads"],
                values["new_players"],
                values["blacklist_added"],
                values["selected_delta"],
                values["appeared_delta"],
                values["targeted_delta"],
            ),
        )

    @staticmethod
    def _sum_stats(talent_stats: dict):
        selected = appeared = targeted = 0
        if not isinstance(talent_stats, dict):
            return selected, appeared, targeted
        for stats in talent_stats.values():
            if not isinstance(stats, dict):
                continue
            selected += int(stats.get("selected", 0) or 0)
            appeared += int(stats.get("appeared", 0) or 0)
            targeted += int(stats.get("targeted", 0) or 0)
        return selected, appeared, targeted

    @staticmethod
    def _loads(value: str):
        try:
            loaded = json.loads(value or "{}")
            return loaded if isinstance(loaded, dict) else {}
        except json.JSONDecodeError:
            return {}


class CloudHandler(BaseHTTPRequestHandler):
    store: TalentCloudStore = None

    def do_OPTIONS(self):
        self._send_json({"ok": True})

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/api/aggregate":
            self._send_json({"ok": True, "aggregate": self.store.aggregate()})
            return
        if parsed.path == "/api/download":
            params = parse_qs(parsed.query)
            device_ip = params.get("device_ip", [""])[0] or self.client_address[0]
            data = self.store.download(device_ip)
            if data is None:
                self._send_json({"ok": False, "error": "not_found"}, status=404)
                return
            data["ok"] = True
            data["aggregate"] = self.store.aggregate()
            self._send_json(data)
            return
        self._send_json({"ok": False, "error": "not_found"}, status=404)

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path != "/api/upload":
            self._send_json({"ok": False, "error": "not_found"}, status=404)
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            device_ip = payload.get("device_ip") or self.client_address[0]
            local_ip = payload.get("local_ip") or ""
            global_data = payload.get("global_data") or {}
            talent_stats = payload.get("talent_stats") or {}
            if not isinstance(global_data, dict) or not isinstance(talent_stats, dict):
                raise ValueError("invalid payload")
            if self.store.is_blacklisted(device_ip):
                self._send_json({"ok": True, "blacklisted": True, "aggregate": self.store.aggregate()})
                return
            self.store.delete_legacy_ip_record(device_ip, local_ip)
            self.store.upload(device_ip, global_data, talent_stats)
            self._send_json({"ok": True, "aggregate": self.store.aggregate()})
        except Exception as exc:
            self._send_json({"ok": False, "error": str(exc)}, status=400)

    def log_message(self, fmt, *args):
        print("%s - %s" % (self.address_string(), fmt % args))

    def _send_json(self, payload, status=200):
        body = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=44140)
    parser.add_argument("--db", default="cloud_data/talent_cloud.sqlite3")
    args = parser.parse_args()

    CloudHandler.store = TalentCloudStore(Path(args.db))
    server = ThreadingHTTPServer((args.host, args.port), CloudHandler)
    print(f"Talent cloud backend listening on {args.host}:{args.port}")
    server.serve_forever()


if __name__ == "__main__":
    main()
