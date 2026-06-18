#!/usr/bin/env python3
import argparse
import csv
import io
import json
import re
import secrets
import sqlite3
import time
from http import cookies
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse

ADMIN_ACCOUNTS = {
    "2025100778": "Wy123456789",
    "dm216": "tpd0216",
    "Unnamed946": "1357900qwertyuiop",
}
SESSION_COOKIE = "tpd_admin_session"
SESSIONS = {}


def now_ms() -> int:
    return int(time.time() * 1000)


def today_start_ms() -> int:
    now = time.localtime()
    return int(time.mktime((now.tm_year, now.tm_mon, now.tm_mday, 0, 0, 0, now.tm_wday, now.tm_yday, now.tm_isdst)) * 1000)


def day_key(timestamp_ms: int = None) -> str:
    return time.strftime("%Y-%m-%d", time.localtime((timestamp_ms or now_ms()) / 1000))


class AdminStore:
    def __init__(self, db_path: Path, snapshot_dir: Path, keep_snapshots: int, actors_zh_path: Path = None):
        self.db_path = db_path
        self.snapshot_dir = snapshot_dir
        self.keep_snapshots = keep_snapshots
        self.talent_names = self._load_talent_names(actors_zh_path)
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self.snapshot_dir.mkdir(parents=True, exist_ok=True)
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
            cols = [row[1] for row in db.execute("PRAGMA table_info(player_cloud_data)").fetchall()]
            if "created_at" not in cols:
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

    def player_stats(self):
        with self._connect() as db:
            total = db.execute("SELECT COUNT(*) FROM player_cloud_data").fetchone()[0]
            today_new = db.execute("SELECT COUNT(*) FROM player_cloud_data WHERE created_at >= ?", (today_start_ms(),)).fetchone()[0]
        return {"total": total, "today_new": today_new}

    def list_players(self):
        with self._connect() as db:
            rows = db.execute(
                """
                SELECT p.device_ip, p.talent_stats, p.updated_at, p.created_at, b.reason, b.updated_at
                FROM player_cloud_data p
                LEFT JOIN device_blacklist b ON b.device_ip = p.device_ip
                ORDER BY p.updated_at DESC
                """
            ).fetchall()
        players = []
        for device_ip, stats_json, updated_at, created_at, reason, blacklisted_at in rows:
            stats = self._loads(stats_json)
            selected, appeared, targeted = self._totals(stats)
            players.append({
                "device_ip": device_ip,
                "updated_at": updated_at,
                "created_at": created_at,
                "talent_count": len(stats),
                "selected": selected,
                "appeared": appeared,
                "targeted": targeted,
                "blacklisted": reason is not None,
                "blacklist_reason": reason or "",
                "blacklisted_at": blacklisted_at or 0,
            })
        return players

    def get_player(self, device_ip: str):
        with self._connect() as db:
            row = db.execute(
                "SELECT device_ip, global_data, talent_stats, updated_at, created_at FROM player_cloud_data WHERE device_ip = ?",
                (device_ip,),
            ).fetchone()
        if row is None:
            return None
        return {
            "device_ip": row[0],
            "global_data": self._loads(row[1]),
            "talent_stats": self._loads(row[2]),
            "updated_at": row[3],
            "created_at": row[4],
            "blacklisted": self.is_blacklisted(row[0]),
        }

    def update_player(self, device_ip: str, global_data: dict, talent_stats: dict):
        ts = now_ms()
        with self._connect() as db:
            old_row = db.execute("SELECT talent_stats FROM player_cloud_data WHERE device_ip = ?", (device_ip,)).fetchone()
            old_totals = self._totals(self._loads(old_row[0])) if old_row else (0, 0, 0)
            new_totals = self._totals(talent_stats)
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
                    ts,
                    ts,
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

    def delete_player(self, device_ip: str):
        with self._connect() as db:
            db.execute("DELETE FROM player_cloud_data WHERE device_ip = ?", (device_ip,))

    def set_blacklisted(self, device_ip: str, blacklisted: bool, reason: str = ""):
        with self._connect() as db:
            if blacklisted:
                existed = db.execute("SELECT 1 FROM device_blacklist WHERE device_ip = ?", (device_ip,)).fetchone() is not None
                db.execute(
                    """
                    INSERT INTO device_blacklist(device_ip, reason, updated_at)
                    VALUES (?, ?, ?)
                    ON CONFLICT(device_ip) DO UPDATE SET reason = excluded.reason, updated_at = excluded.updated_at
                    """,
                    (device_ip, reason or "", now_ms()),
                )
                if not existed:
                    self._record_daily(db, blacklist_added=1)
            else:
                db.execute("DELETE FROM device_blacklist WHERE device_ip = ?", (device_ip,))

    def is_blacklisted(self, device_ip: str):
        with self._connect() as db:
            return db.execute("SELECT 1 FROM device_blacklist WHERE device_ip = ?", (device_ip,)).fetchone() is not None

    def list_blacklist(self):
        with self._connect() as db:
            rows = db.execute("SELECT device_ip, reason, updated_at FROM device_blacklist ORDER BY updated_at DESC").fetchall()
        return [{"device_ip": row[0], "reason": row[1], "updated_at": row[2]} for row in rows]

    def activity_series(self, days: int = 30):
        days = max(7, min(180, int(days or 30)))
        today = time.localtime()
        end = time.mktime((today.tm_year, today.tm_mon, today.tm_mday, 0, 0, 0, today.tm_wday, today.tm_yday, today.tm_isdst))
        labels = [time.strftime("%Y-%m-%d", time.localtime(end - (days - 1 - idx) * 86400)) for idx in range(days)]
        with self._connect() as db:
            rows = db.execute(
                """
                SELECT day, player_uploads, new_players, blacklist_added, selected_delta, appeared_delta, targeted_delta
                FROM daily_activity
                WHERE day >= ?
                ORDER BY day ASC
                """,
                (labels[0],),
            ).fetchall()
        by_day = {
            row[0]: {
                "player_uploads": row[1],
                "new_players": row[2],
                "blacklist_added": row[3],
                "selected_delta": row[4],
                "appeared_delta": row[5],
                "targeted_delta": row[6],
            }
            for row in rows
        }
        cumulative = {"players": 0, "blacklist": 0, "selected": 0, "appeared": 0, "targeted": 0}
        series = []
        for label in labels:
            item = by_day.get(label, {})
            cumulative["players"] += int(item.get("new_players", 0) or 0)
            cumulative["blacklist"] += int(item.get("blacklist_added", 0) or 0)
            cumulative["selected"] += int(item.get("selected_delta", 0) or 0)
            cumulative["appeared"] += int(item.get("appeared_delta", 0) or 0)
            cumulative["targeted"] += int(item.get("targeted_delta", 0) or 0)
            series.append({
                "day": label,
                "player_uploads": int(item.get("player_uploads", 0) or 0),
                "new_players": int(item.get("new_players", 0) or 0),
                "blacklist_added": int(item.get("blacklist_added", 0) or 0),
                "selected_delta": int(item.get("selected_delta", 0) or 0),
                "appeared_delta": int(item.get("appeared_delta", 0) or 0),
                "targeted_delta": int(item.get("targeted_delta", 0) or 0),
                "cumulative_players": cumulative["players"],
                "cumulative_blacklist": cumulative["blacklist"],
                "cumulative_selected": cumulative["selected"],
                "cumulative_appeared": cumulative["appeared"],
                "cumulative_targeted": cumulative["targeted"],
            })
        return series

    def aggregate(self):
        result = {}
        for player in self.all_player_data(include_blacklisted=False):
            for talent, stats in player["talent_stats"].items():
                target = result.setdefault(talent, {"selected": 0, "appeared": 0, "targeted": 0})
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
        for (stats_json,) in rows:
            totals = self._totals(self._loads(stats_json))
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

    def all_player_data(self, include_blacklisted: bool = True):
        where = "" if include_blacklisted else "WHERE b.device_ip IS NULL"
        with self._connect() as db:
            rows = db.execute(
                f"""
                SELECT p.device_ip, p.global_data, p.talent_stats, p.updated_at, p.created_at, b.reason, b.updated_at
                FROM player_cloud_data p
                LEFT JOIN device_blacklist b ON b.device_ip = p.device_ip
                {where}
                ORDER BY p.updated_at DESC
                """
            ).fetchall()
        return [{
            "device_ip": row[0],
            "global_data": self._loads(row[1]),
            "talent_stats": self._loads(row[2]),
            "updated_at": row[3],
            "created_at": row[4],
            "blacklisted": row[5] is not None,
            "blacklist_reason": row[5] or "",
            "blacklisted_at": row[6] or 0,
        } for row in rows]

    def export_csv(self):
        out = io.StringIO()
        writer = csv.writer(out)
        writer.writerow(["talent", "talent_name", "selected", "appeared", "selection_rate", "targeted"])
        for talent, stats in sorted(self.aggregate().items(), key=lambda item: item[0]):
            selected = int(stats.get("selected", 0))
            appeared = int(stats.get("appeared", 0))
            targeted = int(stats.get("targeted", 0))
            writer.writerow([
                talent,
                self.talent_names.get(talent, talent),
                selected,
                appeared,
                f"{selected / appeared:.6f}" if appeared > 0 else "0.000000",
                targeted,
            ])
        return out.getvalue()

    def export_json(self):
        aggregate = self.aggregate()
        talents = {}
        totals = {"selected": 0, "appeared": 0, "targeted": 0}
        for talent, stats in aggregate.items():
            selected = int(stats.get("selected", 0))
            appeared = int(stats.get("appeared", 0))
            targeted = int(stats.get("targeted", 0))
            totals["selected"] += selected
            totals["appeared"] += appeared
            totals["targeted"] += targeted
            talents[talent] = {
                "talent_name": self.talent_names.get(talent, talent),
                "selected": selected,
                "appeared": appeared,
                "selection_rate": selected / appeared if appeared > 0 else 0,
                "targeted": targeted,
            }
        totals["selection_rate"] = totals["selected"] / totals["appeared"] if totals["appeared"] > 0 else 0
        return {
            "exported_at": now_ms(),
            "scope": "aggregate_only",
            "note": "Blacklisted devices are excluded. No per-player data is included.",
            "talents": talents,
            "totals": totals,
        }

    def list_snapshots(self):
        items = [self._snapshot_info(path) for path in self.snapshot_dir.glob("talent_cloud_*.sqlite3")]
        items.sort(key=lambda item: item["created_at"], reverse=True)
        return items

    def create_snapshot(self, label: str = "manual"):
        label = re.sub(r"[^A-Za-z0-9_-]+", "_", label or "manual").strip("_") or "manual"
        path = self.snapshot_dir / f"talent_cloud_{label}_{time.strftime('%Y%m%d_%H%M%S')}.sqlite3"
        with self._connect() as src, sqlite3.connect(path) as dst:
            src.backup(dst)
        self.prune_snapshots()
        return self._snapshot_info(path)

    def delete_snapshot(self, name: str):
        path = self._snapshot_path(name)
        if not path.exists():
            return False
        path.unlink()
        return True

    def restore_snapshot(self, name: str):
        path = self._snapshot_path(name)
        if not path.exists():
            raise FileNotFoundError(name)
        with sqlite3.connect(path) as src, self._connect() as dst:
            src.backup(dst)
        return self._snapshot_info(path)

    def prune_snapshots(self):
        items = sorted(self.snapshot_dir.glob("talent_cloud_*.sqlite3"), key=lambda p: p.stat().st_mtime, reverse=True)
        for path in items[self.keep_snapshots:]:
            path.unlink(missing_ok=True)

    def _snapshot_path(self, name: str):
        if Path(name).name != name:
            raise ValueError("invalid_snapshot_name")
        path = (self.snapshot_dir / name).resolve()
        if path.parent != self.snapshot_dir.resolve():
            raise ValueError("invalid_snapshot_name")
        return path

    @staticmethod
    def _snapshot_info(path: Path):
        stat = path.stat()
        return {"name": path.name, "size": stat.st_size, "created_at": int(stat.st_mtime * 1000)}

    @staticmethod
    def _load_talent_names(path: Path):
        names = {}
        if path is None or not path.exists():
            return names
        prefix = "actors.hero.talent."
        suffix = ".title"
        for raw in path.read_text(encoding="utf-8").splitlines():
            line = raw.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            if key.startswith(prefix) and key.endswith(suffix):
                names[key[len(prefix):-len(suffix)].upper()] = value.strip()
        return names

    @staticmethod
    def _loads(value: str):
        try:
            loaded = json.loads(value or "{}")
            return loaded if isinstance(loaded, dict) else {}
        except json.JSONDecodeError:
            return {}

    @staticmethod
    def _totals(stats: dict):
        selected = appeared = targeted = 0
        if not isinstance(stats, dict):
            return selected, appeared, targeted
        for item in stats.values():
            if not isinstance(item, dict):
                continue
            selected += int(item.get("selected", 0))
            appeared += int(item.get("appeared", 0))
            targeted += int(item.get("targeted", 0))
        return selected, appeared, targeted


class AdminHandler(BaseHTTPRequestHandler):
    store: AdminStore = None

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/":
            self._send_html(DASHBOARD_HTML if self._authed() else LOGIN_HTML)
            return
        if parsed.path == "/api/session":
            self._send_json({"ok": True, "authenticated": self._authed()})
            return
        if not self._require_auth():
            return
        if parsed.path == "/api/players":
            self._send_json({
                "ok": True,
                "players": self.store.list_players(),
                "aggregate": self.store.aggregate(),
                "talent_names": self.store.talent_names,
                "blacklist": self.store.list_blacklist(),
                "player_stats": self.store.player_stats(),
                "activity": self.store.activity_series(),
            })
            return
        if parsed.path == "/api/snapshots":
            self._send_json({"ok": True, "snapshots": self.store.list_snapshots()})
            return
        if parsed.path == "/api/player":
            device_ip = parse_qs(parsed.query).get("device_ip", [""])[0]
            player = self.store.get_player(device_ip)
            self._send_json({"ok": player is not None, "player": player} if player else {"ok": False, "error": "not_found"}, status=200 if player else 404)
            return
        if parsed.path == "/api/export.json":
            self._send_download(json.dumps(self.store.export_json(), ensure_ascii=False, indent=2), "application/json; charset=utf-8", "tpd-cloud-aggregate-export.json")
            return
        if parsed.path == "/api/export.csv":
            self._send_download(self.store.export_csv(), "text/csv; charset=utf-8", "tpd-cloud-aggregate-export.csv")
            return
        self._send_json({"ok": False, "error": "not_found"}, status=404)

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path == "/api/login":
            payload = self._read_json()
            username = (payload.get("username") or "").strip()
            password = payload.get("password") or ""
            if ADMIN_ACCOUNTS.get(username) == password:
                token = secrets.token_urlsafe(32)
                SESSIONS[token] = {"login_at": now_ms(), "username": username}
                self._send_json({"ok": True}, headers=[("Set-Cookie", f"{SESSION_COOKIE}={token}; Path=/; HttpOnly; SameSite=Lax")])
            else:
                self._send_json({"ok": False, "error": "bad_credentials"}, status=403)
            return
        if parsed.path == "/api/logout":
            SESSIONS.pop(self._session_token(), None)
            self._send_json({"ok": True}, headers=[("Set-Cookie", f"{SESSION_COOKIE}=; Path=/; Max-Age=0")])
            return
        if not self._require_auth():
            return
        if parsed.path == "/api/player/update":
            payload = self._read_json()
            device_ip = payload.get("device_ip", "").strip()
            global_data = payload.get("global_data") or {}
            talent_stats = payload.get("talent_stats") or {}
            if not device_ip or not isinstance(global_data, dict) or not isinstance(talent_stats, dict):
                self._send_json({"ok": False, "error": "invalid_payload"}, status=400)
                return
            self.store.update_player(device_ip, global_data, talent_stats)
            self._send_json({"ok": True})
            return
        if parsed.path == "/api/player/delete":
            self.store.delete_player(self._read_json().get("device_ip", "").strip())
            self._send_json({"ok": True})
            return
        if parsed.path == "/api/blacklist/set":
            payload = self._read_json()
            device_ip = payload.get("device_ip", "").strip()
            if not device_ip:
                self._send_json({"ok": False, "error": "device_ip_required"}, status=400)
                return
            self.store.set_blacklisted(device_ip, bool(payload.get("blacklisted")), payload.get("reason", ""))
            self._send_json({"ok": True})
            return
        if parsed.path == "/api/snapshot/create":
            self._send_json({"ok": True, "snapshot": self.store.create_snapshot("manual")})
            return
        if parsed.path == "/api/snapshot/delete":
            try:
                self._send_json({"ok": True, "deleted": self.store.delete_snapshot(self._read_json().get("name", ""))})
            except ValueError:
                self._send_json({"ok": False, "error": "invalid_snapshot_name"}, status=400)
            return
        if parsed.path == "/api/snapshot/restore":
            try:
                self._send_json({"ok": True, "snapshot": self.store.restore_snapshot(self._read_json().get("name", ""))})
            except ValueError:
                self._send_json({"ok": False, "error": "invalid_snapshot_name"}, status=400)
            except FileNotFoundError:
                self._send_json({"ok": False, "error": "snapshot_not_found"}, status=404)
            return
        self._send_json({"ok": False, "error": "not_found"}, status=404)

    def log_message(self, fmt, *args):
        print("%s - %s" % (self.address_string(), fmt % args))

    def _authed(self):
        token = self._session_token()
        return bool(token and token in SESSIONS)

    def _require_auth(self):
        if self._authed():
            return True
        self._send_json({"ok": False, "error": "unauthorized"}, status=401)
        return False

    def _session_token(self):
        jar = cookies.SimpleCookie(self.headers.get("Cookie", ""))
        morsel = jar.get(SESSION_COOKIE)
        return morsel.value if morsel else None

    def _read_json(self):
        length = int(self.headers.get("Content-Length", "0"))
        if length <= 0:
            return {}
        try:
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            return payload if isinstance(payload, dict) else {}
        except json.JSONDecodeError:
            return {}

    def _send_html(self, html: str):
        body = html.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _send_json(self, payload, status=200, headers=None):
        body = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        if headers:
            for key, value in headers:
                self.send_header(key, value)
        self.end_headers()
        self.wfile.write(body)

    def _send_download(self, text: str, content_type: str, filename: str):
        body = text.encode("utf-8-sig" if filename.endswith(".csv") else "utf-8")
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Disposition", f'attachment; filename="{filename}"')
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


LOGIN_HTML = r"""<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>蜕变地牢控制台</title>
<style>
:root{--bg:#08050f;--ink:#f3eaff;--muted:#a89ab9;--line:#3b2757;--accent:#a855f7;--shadow:0 28px 90px rgba(0,0,0,.55)}
*{box-sizing:border-box}body{margin:0;min-height:100vh;font-family:"Microsoft YaHei","Segoe UI",sans-serif;background:radial-gradient(circle at 12% 8%,#5b21b6 0,#180c2a 30%,#08050f 72%);color:var(--ink);display:grid;place-items:center}
.login{width:min(430px,calc(100vw - 32px));background:linear-gradient(145deg,rgba(33,19,50,.94),rgba(12,8,20,.96));border:1px solid rgba(216,180,254,.32);box-shadow:var(--shadow),0 0 40px rgba(168,85,247,.25);padding:30px;border-radius:22px}
.sigil{width:64px;height:64px;border-radius:18px;margin-bottom:18px;background:radial-gradient(circle,#f5c46b,#a855f7 48%,#1e1230 70%);box-shadow:0 0 35px rgba(168,85,247,.65)}h1{margin:0 0 8px;font-size:28px}.sub{margin:0 0 24px;color:var(--muted);font-size:14px}label{display:block;margin:14px 0 7px;color:#d8b4fe;font-size:13px}
input{width:100%;height:44px;border:1px solid var(--line);border-radius:12px;padding:0 12px;background:#0c0814;color:var(--ink);outline:none}button{width:100%;height:46px;margin-top:22px;border:0;border-radius:14px;background:linear-gradient(135deg,#7e22ce,#c084fc);color:white;font-weight:800;font-size:15px;cursor:pointer}.error{min-height:20px;margin-top:12px;color:#fca5a5;font-size:13px}
</style></head><body>
<form class="login" id="login"><div class="sigil"></div><h1>蜕变地牢控制台</h1><p class="sub">云端玩家数据、黑名单与天赋统计管理</p><label>账号</label><input id="username" autocomplete="username" autofocus><label>密码</label><input id="password" type="password" autocomplete="current-password"><button>登录控制台</button><div class="error" id="error"></div></form>
<script>const el=id=>document.getElementById(id);document.querySelector('#login').addEventListener('submit',async e=>{e.preventDefault();const r=await fetch('/api/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:el('username').value.trim(),password:el('password').value})});if(r.ok&&(await r.json()).ok)location.href='/';else el('error').textContent='账号或密码不正确';});</script>
</body></html>"""


DASHBOARD_HTML = r"""<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>蜕变地牢控制台</title>
<style>
:root{--bg:#08050f;--panel:#140d1f;--ink:#f4ecff;--muted:#aa9ab8;--line:#3b2757;--accent:#a855f7;--accent2:#f43f5e;--gold:#f6c76e;--cyan:#7dd3fc;--green:#34d399}
*{box-sizing:border-box}body{margin:0;font-family:"Microsoft YaHei","Segoe UI",sans-serif;background:radial-gradient(circle at 20% 0,#2d1450,#08050f 48%),linear-gradient(135deg,#08050f,#170d24);color:var(--ink)}
header{position:sticky;top:0;z-index:4;height:70px;background:rgba(8,5,15,.86);backdrop-filter:blur(14px);border-bottom:1px solid rgba(168,85,247,.3);display:flex;align-items:center;justify-content:space-between;padding:0 24px}
h1{font-size:22px;margin:0}.brand{display:flex;gap:12px;align-items:center}.orb{width:36px;height:36px;border-radius:12px;background:radial-gradient(circle,#f6c76e,#a855f7 55%,#0d0714);box-shadow:0 0 28px rgba(168,85,247,.7)}
button,a.btn{border:0;border-radius:12px;padding:9px 13px;background:linear-gradient(135deg,#7e22ce,#a855f7);color:white;text-decoration:none;display:inline-flex;align-items:center;gap:6px;cursor:pointer;font-weight:800}button.secondary,a.secondary{background:#2b2038}button.danger{background:linear-gradient(135deg,#be123c,#f43f5e)}
main{padding:22px;display:grid;gap:18px;grid-template-columns:minmax(720px,1.55fr) minmax(430px,.9fr)}.activity-grid{grid-column:1/-1;display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:14px}.chart-card,.panel{background:linear-gradient(145deg,rgba(29,18,48,.95),rgba(13,8,21,.96));border:1px solid rgba(168,85,247,.22);border-radius:18px;box-shadow:0 18px 50px rgba(0,0,0,.28),inset 0 1px 0 rgba(255,255,255,.04)}
.chart-card{padding:14px;min-height:182px;position:relative;overflow:hidden}.chart-title{font-weight:900;font-size:14px}.chart-subtitle{color:var(--muted);font-size:12px;margin-top:4px}.chart-card svg{width:100%;height:118px;margin-top:10px;display:block}.chart-grid{stroke:rgba(170,154,184,.16);stroke-width:1}.chart-line-a{fill:none;stroke:#a855f7;stroke-width:2.8;stroke-linecap:round;stroke-linejoin:round}.chart-line-b{fill:none;stroke:#34d399;stroke-width:2.4;stroke-linecap:round;stroke-linejoin:round}.chart-dot{fill:#f4ecff;stroke:#120a1d;stroke-width:2}.chart-tip{position:absolute;left:12px;right:12px;bottom:10px;border:1px solid rgba(168,85,247,.24);border-radius:12px;background:rgba(8,5,15,.86);padding:7px 9px;font-size:12px;color:#e9d5ff;opacity:0;transform:translateY(6px);transition:.16s}.chart-card:hover .chart-tip{opacity:1;transform:none}.panel{overflow:hidden}.panel h2{font-size:16px;margin:0;padding:15px 16px;border-bottom:1px solid var(--line);background:rgba(255,255,255,.025)}
.toolbar{display:flex;gap:9px;padding:12px 16px;border-bottom:1px solid rgba(59,39,87,.75);flex-wrap:wrap}input,textarea{border:1px solid var(--line);border-radius:12px;padding:10px;background:#0b0712;color:var(--ink);font:13px Consolas,"Microsoft YaHei",monospace;outline:none}input{min-width:0;flex:1}
table{width:100%;border-collapse:collapse;font-size:13px}th,td{text-align:left;padding:10px 12px;border-bottom:1px solid rgba(59,39,87,.62);vertical-align:middle}th{color:#e9d5ff;background:rgba(255,255,255,.035);position:sticky;top:0;z-index:1}th.sortable{cursor:pointer}tr:hover{background:rgba(168,85,247,.08)}.scroll{max-height:530px;overflow:auto}.pill{display:inline-flex;border:1px solid var(--line);border-radius:999px;padding:2px 8px;color:var(--muted);font-size:12px}.bad{color:#fecdd3;border-color:#881337;background:rgba(244,63,94,.12)}.ok{color:#bbf7d0;border-color:#166534;background:rgba(34,197,94,.1)}
.detail{display:grid;grid-template-columns:1fr 1fr;gap:14px;padding:16px}.detail textarea{width:100%;height:250px;resize:vertical}.actions{display:flex;gap:8px;padding:0 16px 16px;flex-wrap:wrap}.hint{color:var(--muted);font-size:13px;padding:14px 16px}.full{grid-column:1/-1}.result{padding:12px 16px;color:#e9d5ff}
.top-layout{display:grid;grid-template-columns:minmax(0,1fr) 190px;gap:10px;padding:12px}.pie-card{border:1px solid var(--line);border-radius:18px;background:rgba(255,255,255,.035);padding:12px;display:grid;place-items:center;text-align:center}.pie{width:132px;height:132px;border-radius:50%;background:conic-gradient(#a855f7 0deg,#a855f7 var(--selectedDeg),#f6c76e var(--selectedDeg),#f6c76e var(--appearedDeg),#7dd3fc var(--appearedDeg),#7dd3fc 360deg);box-shadow:0 0 35px rgba(168,85,247,.28);position:relative}.pie:after{content:"";position:absolute;inset:34px;border-radius:50%;background:#120a1d;border:1px solid var(--line)}.pie-title{margin-top:10px;font-weight:800;font-size:13px}.pie-meta{color:var(--muted);font-size:12px;line-height:1.7}.legend{display:flex;gap:7px;flex-wrap:wrap;justify-content:center;margin-top:8px;font-size:12px}.dot{width:9px;height:9px;border-radius:50%;display:inline-block;margin-right:4px}.top-row{cursor:pointer}.top-row.active{background:rgba(168,85,247,.18)}
@media(max-width:1440px){main{grid-template-columns:minmax(620px,1.45fr) minmax(360px,.95fr)}.activity-grid{grid-template-columns:repeat(3,minmax(0,1fr))}}
@media(max-width:1200px){header{height:auto;min-height:70px;padding:12px 16px;gap:12px;flex-wrap:wrap}.brand{flex-wrap:wrap}main{grid-template-columns:1fr;padding:16px}.activity-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.panel h2{font-size:15px}.scroll{max-height:460px}.chart-card{min-height:168px}.chart-card svg{height:108px}.top-layout{grid-template-columns:1fr 170px}.pie{width:120px;height:120px}}
@media(max-width:900px){.activity-grid{grid-template-columns:1fr 1fr}.detail,.top-layout{grid-template-columns:1fr}.chart-card,.panel{border-radius:16px}.toolbar{padding:10px 12px}.panel h2{padding:12px 14px}.scroll{max-height:400px}.pie-card{padding:10px}.pie{width:108px;height:108px}.pie:after{inset:28px}}
@media(max-width:700px){header{position:static;padding:12px}h1{font-size:18px}.brand{gap:8px}button,a.btn{width:100%;justify-content:center}.activity-grid{grid-template-columns:1fr}.chart-card{min-height:156px;padding:12px}.chart-card svg{height:96px}.chart-tip{font-size:11px;line-height:1.4}.toolbar{gap:8px}.toolbar input,.toolbar button,.toolbar a.btn{width:100%}.scroll{max-height:none;overflow-x:auto;overflow-y:visible}table{min-width:760px}th,td{padding:8px 10px;font-size:12px}.top-layout{padding:10px}.pie-card{min-height:176px}.detail textarea{height:190px}.actions button{width:100%}.result,.hint{padding:12px 14px}}
</style></head><body>
<header><div class="brand"><div class="orb"></div><div><h1>蜕变地牢控制台</h1><div style="color:var(--muted);font-size:12px">黑名单设备不参与全服统计</div></div></div><div><a class="btn secondary" href="/api/export.csv">导出 CSV</a> <a class="btn secondary" href="/api/export.json">导出 JSON</a> <button onclick="logout()">退出</button></div></header>
<main>
<section class="activity-grid" id="activityCharts"></section>
<section class="panel"><h2>玩家数据</h2><div class="toolbar"><input id="filter" placeholder="搜索设备 ID / IP" oninput="renderPlayers()"></div><div class="scroll"><table><thead><tr><th class="sortable" onclick="sortPlayers('device_ip')">设备 <span id="psort-device_ip"></span></th><th class="sortable" onclick="sortPlayers('blacklisted')">状态 <span id="psort-blacklisted"></span></th><th class="sortable" onclick="sortPlayers('updated_at')">最后上传 <span id="psort-updated_at"></span></th><th class="sortable" onclick="sortPlayers('selected')">普通选择 <span id="psort-selected"></span></th><th class="sortable" onclick="sortPlayers('appeared')">候选出现 <span id="psort-appeared"></span></th><th class="sortable" onclick="sortPlayers('targeted')">指定蜕变 <span id="psort-targeted"></span></th><th>操作</th></tr></thead><tbody id="players"></tbody></table></div></section>
<section class="panel"><h2>全体天赋统计 Top 12</h2><div class="top-layout"><div class="scroll" style="max-height:318px"><table><thead><tr><th>天赋</th><th>普通选择</th><th>候选出现</th><th>选中率</th><th>指定</th></tr></thead><tbody id="topRows"></tbody></table></div><div class="pie-card"><div class="pie" id="topPie" style="--selectedDeg:0deg;--appearedDeg:0deg"></div><div class="pie-title" id="pieTitle">暂无数据</div><div class="pie-meta" id="pieMeta">选择左侧天赋查看构成</div><div class="legend"><span><i class="dot" style="background:#a855f7"></i>普通选择</span><span><i class="dot" style="background:#f6c76e"></i>候选未选</span><span><i class="dot" style="background:#7dd3fc"></i>指定蜕变</span></div></div></div></section>
<section class="panel full"><h2>天赋查询</h2><div class="toolbar"><input id="talentSearch" placeholder="输入中文名或枚举名，例如 晶体火药 / CRYSTAL_GUNPOWDER" oninput="renderTalentQuery()"><button class="secondary" onclick="renderTalentQuery()">查询</button></div><div class="result" id="queryResult">输入天赋名后查看全服统计。</div></section>
<section class="panel full"><h2>全服天赋汇总</h2><div class="toolbar"><span class="pill">点击表头可按普通选择、候选出现、选中率、指定蜕变升序/降序排序</span></div><div class="scroll"><table><thead><tr><th>天赋</th><th class="sortable" onclick="sortAgg('selected')">普通选择 <span id="sort-selected"></span></th><th class="sortable" onclick="sortAgg('appeared')">候选出现 <span id="sort-appeared"></span></th><th class="sortable" onclick="sortAgg('rate')">选中率 <span id="sort-rate"></span></th><th class="sortable" onclick="sortAgg('targeted')">指定蜕变 <span id="sort-targeted"></span></th></tr></thead><tbody id="aggregateRows"></tbody></table></div></section>
<section class="panel full"><h2>玩家详情与编辑</h2><div class="hint" id="empty">选择左侧玩家后，可编辑 global_data 与 talent_stats，也可以将该设备拉入或移出黑名单。</div><div class="detail" id="detail" style="display:none"><div><label>设备 ID</label><input id="deviceIp"><label>global_data JSON</label><textarea id="globalData"></textarea></div><div><label>talent_stats JSON</label><textarea id="talentStats"></textarea></div></div><div class="actions" id="actions" style="display:none"><button onclick="savePlayer()">保存</button><button class="danger" onclick="deletePlayer()">删除玩家数据</button><button class="danger" id="blackBtn" onclick="toggleBlacklist()">拉入黑名单</button><button class="secondary" onclick="refresh()">刷新</button></div><div class="scroll" id="playerTalentWrap" style="display:none"><table><thead><tr><th>天赋</th><th>普通选择</th><th>候选出现</th><th>选中率</th><th>指定蜕变</th></tr></thead><tbody id="playerTalentRows"></tbody></table></div></section>
<section class="panel full"><h2>数据库快照</h2><div class="toolbar"><button onclick="createSnapshot()">新增快照</button><button class="secondary" onclick="loadSnapshots()">刷新快照</button><span class="pill">每天 0 点自动保存，最多保留 7 个最新快照</span></div><div class="scroll"><table><thead><tr><th>快照文件</th><th>大小</th><th>创建时间</th><th>操作</th></tr></thead><tbody id="snapshots"></tbody></table></div></section>
</main>
<script>
let state={players:[],aggregate:{},talentNames:{},blacklist:[],playerStats:{total:0,today_new:0},activity:[],selected:null,selectedPlayer:null,sort:{key:'selected',dir:'desc'},playerSort:{key:'updated_at',dir:'desc'},topKey:null};
const el=id=>document.getElementById(id),fmt=n=>Number(n||0).toLocaleString('zh-CN'),rate=r=>r.appeared>0?r.selected/r.appeared:0,pct=r=>(rate(r)*100).toFixed(1)+'%';
function esc(v){return String(v).replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));}
function js(v){return String(v).replace(/\\/g,'\\\\').replace(/'/g,"\\'");}
function tName(k){const n=state.talentNames[k]||k;return n===k?k:`${n} (${k})`;}
async function api(url,opt){const r=await fetch(url,opt);if(r.status===401)location.reload();return r.json();}
async function refresh(){const d=await api('/api/players?t='+Date.now());state.players=d.players||[];state.aggregate=d.aggregate||{};state.talentNames=d.talent_names||{};state.blacklist=d.blacklist||[];state.playerStats=d.player_stats||{total:state.players.length,today_new:0};state.activity=d.activity||[];renderActivityCharts();renderPlayers();renderTop12();renderAggregateRows();renderTalentQuery();loadSnapshots();}
function dateOnly(ms){if(!ms)return '-';const d=new Date(ms);if(Number.isNaN(d.getTime()))return '-';const y=d.getFullYear(),m=String(d.getMonth()+1).padStart(2,'0'),day=String(d.getDate()).padStart(2,'0');return `${y}-${m}-${day}`;}
function sortPlayers(key){if(state.playerSort.key===key)state.playerSort.dir=state.playerSort.dir==='asc'?'desc':'asc';else state.playerSort={key,dir:key==='device_ip'?'asc':'desc'};renderPlayers();}
function sortedPlayers(){const q=el('filter').value.trim().toLowerCase(),m=state.playerSort.dir==='asc'?1:-1,key=state.playerSort.key;return state.players.filter(p=>!q||p.device_ip.toLowerCase().includes(q)).sort((a,b)=>{let av=a[key],bv=b[key];if(key==='device_ip')return String(av).localeCompare(String(bv),'zh-CN')*m;if(key==='blacklisted'){av=av?1:0;bv=bv?1:0}return ((Number(av)||0)-(Number(bv)||0))*m||String(a.device_ip).localeCompare(String(b.device_ip),'zh-CN');});}
function renderPlayers(){['device_ip','blacklisted','updated_at','selected','appeared','targeted'].forEach(k=>el('psort-'+k).textContent=state.playerSort.key===k?(state.playerSort.dir==='asc'?'↑':'↓'):'');el('players').innerHTML=sortedPlayers().map(p=>`<tr><td onclick="loadPlayer('${js(p.device_ip)}')">${esc(p.device_ip)}</td><td>${p.blacklisted?'<span class="pill bad">黑名单</span>':'<span class="pill ok">正常</span>'}</td><td>${dateOnly(p.updated_at)}</td><td>${fmt(p.selected)}</td><td>${fmt(p.appeared)}</td><td>${fmt(p.targeted)}</td><td><button class="secondary" onclick="loadPlayer('${js(p.device_ip)}')">详情</button> <button class="${p.blacklisted?'secondary':'danger'}" onclick="setBlacklist('${js(p.device_ip)}',${!p.blacklisted})">${p.blacklisted?'移出':'拉黑'}</button></td></tr>`).join('')||'<tr><td colspan="7">暂无玩家</td></tr>';}
function linePoints(rows,key,w,h,pad,maxv){if(!rows.length)return '';const denom=Math.max(1,rows.length-1);return rows.map((r,i)=>{const x=pad+i*(w-pad*2)/denom,y=h-pad-((Number(r[key])||0)/maxv)*(h-pad*2);return `${x.toFixed(1)},${y.toFixed(1)}`}).join(' ');}
function chartSvg(rows,aKey,bKey){const w=260,h=118,p=14,maxv=Math.max(1,...rows.flatMap(r=>[Number(r[aKey])||0,Number(r[bKey])||0]));const grid=[.25,.5,.75].map(v=>`<line class="chart-grid" x1="${p}" y1="${(h-p-v*(h-p*2)).toFixed(1)}" x2="${w-p}" y2="${(h-p-v*(h-p*2)).toFixed(1)}"></line>`).join('');const ptsA=linePoints(rows,aKey,w,h,p,maxv),ptsB=linePoints(rows,bKey,w,h,p,maxv);const last=rows[rows.length-1]||{};const lx=p+(rows.length-1)*(w-p*2)/Math.max(1,rows.length-1);const lyA=h-p-((Number(last[aKey])||0)/maxv)*(h-p*2),lyB=h-p-((Number(last[bKey])||0)/maxv)*(h-p*2);return `<svg viewBox="0 0 ${w} ${h}" preserveAspectRatio="none">${grid}<polyline class="chart-line-a" points="${ptsA}"></polyline><polyline class="chart-line-b" points="${ptsB}"></polyline><circle class="chart-dot" cx="${lx.toFixed(1)}" cy="${lyA.toFixed(1)}" r="3"></circle><circle class="chart-dot" cx="${lx.toFixed(1)}" cy="${lyB.toFixed(1)}" r="3"></circle></svg>`;}
function renderActivityCharts(){const rows=state.activity.length?state.activity:[{day:dateOnly(Date.now()),player_uploads:0,new_players:0,blacklist_added:0,selected_delta:0,appeared_delta:0,targeted_delta:0,cumulative_players:0,cumulative_blacklist:0,cumulative_selected:0,cumulative_appeared:0,cumulative_targeted:0}];const last=rows[rows.length-1]||{};const charts=[['上传玩家','累计上传玩家','今日上传玩家','cumulative_players','player_uploads'],['黑名单设备','累计黑名单','今日新增黑名单','cumulative_blacklist','blacklist_added'],['普通选择','累计普通选择','今日新增普通选择','cumulative_selected','selected_delta'],['候选出现','累计候选出现','今日新增候选出现','cumulative_appeared','appeared_delta'],['指定蜕变','累计指定蜕变','今日新增指定蜕变','cumulative_targeted','targeted_delta']];el('activityCharts').innerHTML=charts.map(([title,aLabel,bLabel,aKey,bKey])=>`<article class="chart-card"><div class="chart-title">${title}</div><div class="chart-subtitle"><span style="color:#c084fc">${aLabel}</span> / <span style="color:#34d399">${bLabel}</span></div>${chartSvg(rows,aKey,bKey)}<div class="chart-tip">${esc(last.day||'-')}：${aLabel} ${fmt(last[aKey])}，${bLabel} ${fmt(last[bKey])}</div></article>`).join('');}
async function loadPlayer(id){const d=await api('/api/player?device_ip='+encodeURIComponent(id));if(!d.ok)return;state.selected=d.player.device_ip;state.selectedPlayer=d.player;el('empty').style.display='none';el('detail').style.display='grid';el('actions').style.display='flex';el('playerTalentWrap').style.display='block';el('deviceIp').value=d.player.device_ip;el('globalData').value=JSON.stringify(d.player.global_data||{},null,2);el('talentStats').value=JSON.stringify(d.player.talent_stats||{},null,2);el('blackBtn').textContent=d.player.blacklisted?'移出黑名单':'拉入黑名单';el('blackBtn').className=d.player.blacklisted?'secondary':'danger';renderPlayerTalentRows(d.player.talent_stats||{});}
async function savePlayer(){let global_data,talent_stats;try{global_data=JSON.parse(el('globalData').value||'{}');talent_stats=JSON.parse(el('talentStats').value||'{}')}catch(e){alert('JSON 格式有误');return}const d=await api('/api/player/update',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({device_ip:el('deviceIp').value.trim(),global_data,talent_stats})});if(d.ok){await refresh();alert('已保存')}else alert('保存失败');}
async function deletePlayer(){if(!state.selected||!confirm('确认删除该玩家云端数据？'))return;const d=await api('/api/player/delete',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({device_ip:state.selected})});if(d.ok){state.selected=null;state.selectedPlayer=null;el('detail').style.display='none';el('actions').style.display='none';el('playerTalentWrap').style.display='none';el('empty').style.display='block';await refresh();}}
async function setBlacklist(id,blacklisted){const reason=blacklisted?(prompt('拉黑原因，可留空','')||''):'';const d=await api('/api/blacklist/set',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({device_ip:id,blacklisted,reason})});if(d.ok){await refresh();if(state.selected===id)loadPlayer(id);}else alert('黑名单操作失败');}
async function toggleBlacklist(){if(state.selectedPlayer)setBlacklist(state.selectedPlayer.device_ip,!state.selectedPlayer.blacklisted);}
function rowsFrom(stats){return Object.entries(stats).map(([k,v])=>({key:k,selected:+v.selected||0,appeared:+v.appeared||0,targeted:+v.targeted||0}));}
function sortAgg(key){if(state.sort.key===key)state.sort.dir=state.sort.dir==='asc'?'desc':'asc';else state.sort={key,dir:'desc'};renderAggregateRows();}
function sortedRows(){const m=state.sort.dir==='asc'?1:-1;return rowsFrom(state.aggregate).filter(r=>r.selected||r.appeared||r.targeted).sort((a,b)=>{const av=state.sort.key==='rate'?rate(a):a[state.sort.key],bv=state.sort.key==='rate'?rate(b):b[state.sort.key];return (av===bv?tName(a.key).localeCompare(tName(b.key),'zh-CN'):(av-bv)*m);});}
function renderAggregateRows(){['selected','appeared','rate','targeted'].forEach(k=>el('sort-'+k).textContent=state.sort.key===k?(state.sort.dir==='asc'?'↑':'↓'):'');el('aggregateRows').innerHTML=sortedRows().map(r=>`<tr><td>${esc(tName(r.key))}</td><td>${fmt(r.selected)}</td><td>${fmt(r.appeared)}</td><td>${pct(r)}</td><td>${fmt(r.targeted)}</td></tr>`).join('')||'<tr><td colspan="5">暂无全服天赋统计</td></tr>';}
function renderPlayerTalentRows(stats){const rows=rowsFrom(stats).filter(r=>r.selected||r.appeared||r.targeted).sort((a,b)=>(b.selected+b.appeared+b.targeted)-(a.selected+a.appeared+a.targeted));el('playerTalentRows').innerHTML=rows.map(r=>`<tr><td>${esc(tName(r.key))}</td><td>${fmt(r.selected)}</td><td>${fmt(r.appeared)}</td><td>${pct(r)}</td><td>${fmt(r.targeted)}</td></tr>`).join('')||'<tr><td colspan="5">该玩家暂无非零天赋统计</td></tr>';}
function renderTalentQuery(){const q=(el('talentSearch')?.value||'').trim().toLowerCase();if(!q){el('queryResult').textContent='输入天赋名后查看全服统计。';return}const matches=rowsFrom(state.aggregate).filter(r=>r.key.toLowerCase().includes(q)||(state.talentNames[r.key]||'').toLowerCase().includes(q)).sort((a,b)=>b.selected-a.selected).slice(0,10);el('queryResult').innerHTML=matches.length?matches.map(r=>`<div><b>${esc(tName(r.key))}</b>：普通选择 ${fmt(r.selected)}，候选出现 ${fmt(r.appeared)}，选中率 ${pct(r)}，指定蜕变 ${fmt(r.targeted)}</div>`).join(''):'未找到匹配天赋';}
function topRows(){return rowsFrom(state.aggregate).filter(r=>r.selected||r.appeared||r.targeted).sort((a,b)=>b.selected-a.selected||b.targeted-a.targeted||b.appeared-a.appeared).slice(0,12);}
function renderTop12(){const rows=topRows();if(!state.topKey&&rows.length)state.topKey=rows[0].key;if(state.topKey&&!rows.some(r=>r.key===state.topKey)&&rows.length)state.topKey=rows[0].key;el('topRows').innerHTML=rows.map(r=>`<tr class="top-row ${r.key===state.topKey?'active':''}" onmouseenter="selectTop('${js(r.key)}')" onclick="selectTop('${js(r.key)}')"><td>${esc(tName(r.key))}</td><td>${fmt(r.selected)}</td><td>${fmt(r.appeared)}</td><td>${pct(r)}</td><td>${fmt(r.targeted)}</td></tr>`).join('')||'<tr><td colspan="5">暂无全服天赋统计</td></tr>';renderPie();}
function selectTop(key){state.topKey=key;renderTop12();}
function renderPie(){const rows=topRows();const r=rows.find(x=>x.key===state.topKey)||rows[0];if(!r){el('pieTitle').textContent='暂无数据';el('pieMeta').textContent='选择左侧天赋查看构成';el('topPie').style.setProperty('--selectedDeg','0deg');el('topPie').style.setProperty('--appearedDeg','0deg');return;}const unselected=Math.max(0,r.appeared-r.selected),total=Math.max(1,r.selected+unselected+r.targeted),selectedDeg=360*r.selected/total,appearedDeg=360*(r.selected+unselected)/total;el('topPie').style.setProperty('--selectedDeg',selectedDeg+'deg');el('topPie').style.setProperty('--appearedDeg',appearedDeg+'deg');el('pieTitle').textContent=tName(r.key);el('pieMeta').innerHTML=`普通选择 ${fmt(r.selected)}<br>候选未选 ${fmt(unselected)}<br>指定蜕变 ${fmt(r.targeted)}<br>选中率 ${pct(r)}`;}
async function loadSnapshots(){const d=await api('/api/snapshots');const list=d.snapshots||[];el('snapshots').innerHTML=list.map(s=>`<tr><td>${esc(s.name)}</td><td>${fmtBytes(s.size)}</td><td>${new Date(s.created_at).toLocaleString('zh-CN')}</td><td><button class="secondary" onclick="restoreSnapshot('${js(s.name)}')">覆盖恢复</button> <button class="danger" onclick="deleteSnapshot('${js(s.name)}')">删除</button></td></tr>`).join('')||'<tr><td colspan="4">暂无快照</td></tr>';}
function fmtBytes(n){n=Number(n||0);return n>1048576?(n/1048576).toFixed(2)+' MB':n>1024?(n/1024).toFixed(1)+' KB':n+' B'}
async function createSnapshot(){const d=await api('/api/snapshot/create',{method:'POST'});if(d.ok){await loadSnapshots();alert('已创建快照')}else alert('创建快照失败')}
async function deleteSnapshot(name){if(!confirm('确认删除快照 '+name+'？'))return;const d=await api('/api/snapshot/delete',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name})});if(d.ok)loadSnapshots();else alert('删除快照失败')}
async function restoreSnapshot(name){if(!confirm('确认用该快照覆盖当前数据库？'))return;const d=await api('/api/snapshot/restore',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name})});if(d.ok){await refresh();alert('已恢复快照')}else alert('恢复失败')}
async function logout(){await fetch('/api/logout',{method:'POST'});location.reload()}
refresh();
</script></body></html>"""


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=44142)
    parser.add_argument("--db", default="/opt/tpd-cloud/cloud_data/talent_cloud.sqlite3")
    parser.add_argument("--snapshot-dir", default="/opt/tpd-cloud/cloud_snapshots")
    parser.add_argument("--keep-snapshots", type=int, default=7)
    parser.add_argument("--actors-zh", default="/opt/tpd-cloud/actors_zh.properties")
    parser.add_argument("--snapshot-once", action="store_true")
    args = parser.parse_args()
    store = AdminStore(Path(args.db), Path(args.snapshot_dir), max(1, args.keep_snapshots), Path(args.actors_zh))
    if args.snapshot_once:
        snapshot = store.create_snapshot("auto")
        print(f"Created database snapshot: {snapshot['name']}")
        return
    AdminHandler.store = store
    server = ThreadingHTTPServer((args.host, args.port), AdminHandler)
    print(f"Talent cloud admin console listening on {args.host}:{args.port}")
    server.serve_forever()


if __name__ == "__main__":
    main()
