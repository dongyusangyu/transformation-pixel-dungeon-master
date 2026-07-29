#!/usr/bin/env python3
import argparse
import json
import re
import secrets
import sqlite3
import time
import uuid
from copy import deepcopy
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


def valid_uuid(value: str) -> bool:
    if not isinstance(value, str):
        return False
    try:
        uuid.UUID(value)
        return True
    except ValueError:
        return False


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
                    created_at INTEGER NOT NULL DEFAULT 0,
                    restore_allowed INTEGER NOT NULL DEFAULT 0
                )
                """
            )
            columns = [row[1] for row in db.execute("PRAGMA table_info(player_cloud_data)").fetchall()]
            if "created_at" not in columns:
                db.execute("ALTER TABLE player_cloud_data ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
            if "player_uuid" not in columns:
                db.execute("ALTER TABLE player_cloud_data ADD COLUMN player_uuid TEXT")
            if "restore_allowed" not in columns:
                db.execute("ALTER TABLE player_cloud_data ADD COLUMN restore_allowed INTEGER NOT NULL DEFAULT 0")
            db.execute("UPDATE player_cloud_data SET created_at = updated_at WHERE created_at = 0")
            db.execute(
                """
                CREATE TABLE IF NOT EXISTS device_uuid_map (
                    device_key TEXT PRIMARY KEY,
                    player_uuid TEXT NOT NULL UNIQUE,
                    updated_at INTEGER NOT NULL
                )
                """
            )
            rows = db.execute("SELECT device_ip FROM player_cloud_data WHERE player_uuid IS NULL OR player_uuid = ''").fetchall()
            for (device_ip,) in rows:
                player_uuid = self._new_uuid(db)
                db.execute("UPDATE player_cloud_data SET player_uuid = ? WHERE device_ip = ?", (player_uuid, device_ip))
            for device_ip, player_uuid in db.execute("SELECT device_ip, player_uuid FROM player_cloud_data").fetchall():
                self._bind_device_key(db, device_ip, player_uuid)
            db.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_player_cloud_uuid ON player_cloud_data(player_uuid)")
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
            db.execute(
                """
                CREATE TABLE IF NOT EXISTS restore_sessions (
                    token TEXT PRIMARY KEY,
                    player_uuid TEXT NOT NULL,
                    device_key TEXT NOT NULL,
                    expires_at INTEGER NOT NULL,
                    committed INTEGER NOT NULL DEFAULT 0
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

    def is_blacklisted_identity(self, player_uuid: str, device_key: str, legacy_device_ip: str):
        keys = {key for key in (device_key, legacy_device_ip) if key}
        with self._connect() as db:
            if valid_uuid(player_uuid):
                row = db.execute("SELECT device_ip FROM player_cloud_data WHERE player_uuid = ?", (player_uuid,)).fetchone()
                if row and row[0]:
                    keys.add(row[0])
            for key in list(keys):
                row = db.execute("SELECT player_uuid FROM device_uuid_map WHERE device_key = ?", (key,)).fetchone()
                if row:
                    device = db.execute("SELECT device_ip FROM player_cloud_data WHERE player_uuid = ?", (row[0],)).fetchone()
                    if device and device[0]:
                        keys.add(device[0])
            if not keys:
                return False
            placeholders = ",".join("?" for _ in keys)
            return db.execute(f"SELECT 1 FROM device_blacklist WHERE device_ip IN ({placeholders})", tuple(keys)).fetchone() is not None

    def upload(self, player_uuid: str, device_key: str, legacy_device_ip: str, global_data: dict, talent_stats: dict):
        timestamp = now_ms()
        with self._connect() as db:
            player_uuid, primary_device = self._resolve_player_uuid(db, player_uuid, device_key, legacy_device_ip)
            old_row = db.execute(
                "SELECT global_data, talent_stats FROM player_cloud_data WHERE device_ip = ?",
                (primary_device,),
            ).fetchone()
            if old_row is None:
                old_row = db.execute(
                    "SELECT global_data, talent_stats FROM player_cloud_data WHERE player_uuid = ?",
                    (player_uuid,),
                ).fetchone()
            old_global = self._loads(old_row[0]) if old_row else {}
            old_talent_stats = self._loads(old_row[1]) if old_row else {}
            merged_talent_stats = self._merge_talent_stats(old_talent_stats, talent_stats)
            merged_global = self._merge_global_data(old_global, global_data, merged_talent_stats)

            old_totals = self._sum_stats(old_talent_stats)
            new_totals = self._sum_stats(merged_talent_stats)
            db.execute(
                """
                INSERT INTO player_cloud_data(device_ip, player_uuid, global_data, talent_stats, updated_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    device_ip = excluded.device_ip,
                    global_data = excluded.global_data,
                    talent_stats = excluded.talent_stats,
                    updated_at = excluded.updated_at,
                    created_at = CASE
                        WHEN player_cloud_data.created_at = 0 THEN excluded.created_at
                        ELSE player_cloud_data.created_at
                    END
                """,
                (
                    primary_device,
                    player_uuid,
                    json.dumps(merged_global, ensure_ascii=False, separators=(",", ":")),
                    json.dumps(merged_talent_stats, ensure_ascii=False, separators=(",", ":")),
                    timestamp,
                    timestamp,
                ),
            )
            self._bind_device_key(db, primary_device, player_uuid)
            self._record_daily(
                db,
                player_uploads=1,
                new_players=0 if old_row else 1,
                selected_delta=max(0, new_totals[0] - old_totals[0]),
                appeared_delta=max(0, new_totals[1] - old_totals[1]),
                targeted_delta=max(0, new_totals[2] - old_totals[2]),
            )
            return {
                "player_uuid": player_uuid,
                "device_key": primary_device,
                "global_data": merged_global,
                "talent_stats": merged_talent_stats,
            }

    def _resolve_player_uuid(self, db, requested_uuid: str, device_key: str, legacy_device_ip: str):
        device_key = (device_key or "").strip()
        legacy_device_ip = (legacy_device_ip or "").strip()
        primary_device = device_key or legacy_device_ip
        requested_uuid = (requested_uuid or "").strip()

        if valid_uuid(requested_uuid):
            row = db.execute("SELECT device_ip FROM player_cloud_data WHERE player_uuid = ?", (requested_uuid,)).fetchone()
            if row is not None:
                primary_device = primary_device or row[0]
                self._bind_device_key(db, primary_device, requested_uuid)
                return requested_uuid, primary_device

        if primary_device:
            row = db.execute("SELECT player_uuid FROM device_uuid_map WHERE device_key = ?", (primary_device,)).fetchone()
            if row is not None:
                return row[0], primary_device
            row = db.execute("SELECT player_uuid FROM player_cloud_data WHERE device_ip = ?", (primary_device,)).fetchone()
            if row is not None and row[0]:
                self._bind_device_key(db, primary_device, row[0])
                return row[0], primary_device

        if legacy_device_ip and legacy_device_ip != primary_device:
            row = db.execute("SELECT player_uuid FROM player_cloud_data WHERE device_ip = ?", (legacy_device_ip,)).fetchone()
            if row is not None and row[0]:
                self._bind_device_key(db, primary_device or legacy_device_ip, row[0])
                return row[0], primary_device or legacy_device_ip

        player_uuid = requested_uuid if valid_uuid(requested_uuid) else self._new_uuid(db)
        if not primary_device:
            primary_device = player_uuid
        self._bind_device_key(db, primary_device, player_uuid)
        return player_uuid, primary_device

    @staticmethod
    def _bind_device_key(db, device_key: str, player_uuid: str):
        if not device_key or not player_uuid:
            return
        db.execute("DELETE FROM device_uuid_map WHERE device_key = ? OR player_uuid = ?", (device_key, player_uuid))
        db.execute(
            """
            INSERT INTO device_uuid_map(device_key, player_uuid, updated_at)
            VALUES (?, ?, ?)
            """,
            (device_key, player_uuid, now_ms()),
        )

    @staticmethod
    def _new_uuid(db):
        while True:
            value = str(uuid.uuid4())
            if db.execute("SELECT 1 FROM player_cloud_data WHERE player_uuid = ?", (value,)).fetchone() is None:
                return value

    def delete_legacy_ip_record(self, device_ip: str, local_ip: str):
        if not local_ip or device_ip == local_ip:
            return
        if looks_like_ipv4(device_ip) or not looks_like_ipv4(local_ip):
            return
        with self._connect() as db:
            db.execute("DELETE FROM player_cloud_data WHERE device_ip = ?", (local_ip,))

    def download(
        self,
        player_uuid: str = "",
        device_key: str = "",
        legacy_device_ip: str = "",
        consume_restore_permission: bool = False,
        prepare_restore: bool = False,
    ):
        with self._connect() as db:
            player_uuid = (player_uuid or "").strip()
            device_key = (device_key or "").strip()
            legacy_device_ip = (legacy_device_ip or "").strip()
            row = None
            if player_uuid:
                if not valid_uuid(player_uuid):
                    return None
                row = db.execute(
                    "SELECT player_uuid, device_ip, global_data, talent_stats, updated_at, restore_allowed FROM player_cloud_data WHERE player_uuid = ?",
                    (player_uuid,),
                ).fetchone()
                if row is None:
                    return None
            if row is None and not player_uuid and device_key:
                mapped = db.execute("SELECT player_uuid FROM device_uuid_map WHERE device_key = ?", (device_key,)).fetchone()
                if mapped is not None:
                    row = db.execute(
                        "SELECT player_uuid, device_ip, global_data, talent_stats, updated_at, restore_allowed FROM player_cloud_data WHERE player_uuid = ?",
                        (mapped[0],),
                    ).fetchone()
            lookup_device = device_key or legacy_device_ip
            if row is None and not player_uuid and lookup_device:
                row = db.execute(
                    "SELECT player_uuid, device_ip, global_data, talent_stats, updated_at, restore_allowed FROM player_cloud_data WHERE device_ip = ?",
                    (lookup_device,),
                ).fetchone()
            if row is None:
                return None
            restore_allowed = bool(row[5])
            restore_requested = consume_restore_permission or prepare_restore
            if restore_requested and not restore_allowed:
                return {
                    "player_uuid": row[0],
                    "device_key": row[1],
                    "updated_at": row[4],
                    "restore_allowed": False,
                    "restore_denied": True,
                }
            restore_token = None
            if prepare_restore:
                restore_token = self._create_restore_session(db, row[0], device_key)
            elif consume_restore_permission:
                if not self._finalize_restore(db, row[0], device_key):
                    return {
                        "player_uuid": row[0],
                        "device_key": row[1],
                        "updated_at": row[4],
                        "restore_allowed": False,
                        "restore_denied": True,
                    }
                restore_allowed = False
            result = {
                "player_uuid": row[0],
                "device_key": row[1],
                "global_data": json.loads(row[2]),
                "talent_stats": json.loads(row[3]),
                "updated_at": row[4],
                "restore_allowed": restore_allowed,
            }
            if restore_token:
                result["restore_token"] = restore_token
            return result

    def commit_restore(self, player_uuid: str, device_key: str, token: str):
        player_uuid = (player_uuid or "").strip()
        device_key = (device_key or "").strip()
        token = (token or "").strip()
        if not valid_uuid(player_uuid) or not device_key or not token:
            return False

        with self._connect() as db:
            db.execute("BEGIN IMMEDIATE")
            session = db.execute(
                """
                SELECT player_uuid, device_key, expires_at, committed
                FROM restore_sessions
                WHERE token = ?
                """,
                (token,),
            ).fetchone()
            if (
                session is None
                or session[0] != player_uuid
                or session[1] != device_key
            ):
                return False
            if session[3]:
                return True
            if session[2] < now_ms():
                db.execute("DELETE FROM restore_sessions WHERE token = ?", (token,))
                return False
            if not self._finalize_restore(db, player_uuid, device_key):
                db.execute("DELETE FROM restore_sessions WHERE token = ?", (token,))
                return False
            db.execute(
                """
                UPDATE restore_sessions
                SET committed = 1
                WHERE token = ?
                """,
                (token,),
            )
            return True

    @staticmethod
    def _create_restore_session(db, player_uuid: str, device_key: str):
        timestamp = now_ms()
        db.execute(
            "DELETE FROM restore_sessions WHERE committed = 0 AND expires_at < ?",
            (timestamp,),
        )
        db.execute("DELETE FROM restore_sessions WHERE player_uuid = ?", (player_uuid,))
        token = secrets.token_urlsafe(32)
        db.execute(
            """
            INSERT INTO restore_sessions(token, player_uuid, device_key, expires_at, committed)
            VALUES (?, ?, ?, ?, 0)
            """,
            (token, player_uuid, device_key, timestamp + 5 * 60 * 1000),
        )
        return token

    def _finalize_restore(self, db, player_uuid: str, device_key: str):
        updated = db.execute(
            """
            UPDATE player_cloud_data
            SET restore_allowed = 0
            WHERE player_uuid = ? AND restore_allowed = 1
            """,
            (player_uuid,),
        )
        if updated.rowcount != 1:
            return False
        self._detach_previous_device_owner(db, device_key, player_uuid)
        self._bind_device_key(db, device_key, player_uuid)
        return True

    @staticmethod
    def _detach_previous_device_owner(db, device_key: str, player_uuid: str):
        if not device_key:
            return
        previous = db.execute(
            """
            SELECT player_uuid
            FROM player_cloud_data
            WHERE device_ip = ? AND player_uuid <> ?
            """,
            (device_key, player_uuid),
        ).fetchone()
        if previous is not None:
            db.execute(
                "UPDATE player_cloud_data SET device_ip = ? WHERE player_uuid = ?",
                ("detached:" + previous[0], previous[0]),
            )

    def download_by_legacy_device(self, device_ip: str):
        with self._connect() as db:
            row = db.execute(
                "SELECT player_uuid, device_ip, global_data, talent_stats, updated_at FROM player_cloud_data WHERE device_ip = ?",
                (device_ip,),
            ).fetchone()
        if row is None:
            return None
        return {
            "player_uuid": row[0],
            "device_key": row[1],
            "global_data": json.loads(row[2]),
            "talent_stats": json.loads(row[3]),
            "updated_at": row[4],
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

    @staticmethod
    def _safe_int(value, default=0):
        try:
            return int(value)
        except (TypeError, ValueError):
            return default

    @classmethod
    def _merge_max_map(cls, existing: dict, incoming: dict):
        merged = {}
        for key in set((existing or {}).keys()) | set((incoming or {}).keys()):
            merged[key] = max(
                cls._safe_int((existing or {}).get(key, 0)),
                cls._safe_int((incoming or {}).get(key, 0)),
            )
        return merged

    @classmethod
    def _merge_talent_stats(cls, existing: dict, incoming: dict):
        merged = {}
        keys = set((existing or {}).keys()) | set((incoming or {}).keys())
        for talent_name in keys:
            old_stats = existing.get(talent_name, {}) if isinstance(existing, dict) else {}
            new_stats = incoming.get(talent_name, {}) if isinstance(incoming, dict) else {}
            if not isinstance(old_stats, dict):
                old_stats = {}
            if not isinstance(new_stats, dict):
                new_stats = {}
            merged[talent_name] = {
                "selected": max(cls._safe_int(old_stats.get("selected", 0)), cls._safe_int(new_stats.get("selected", 0))),
                "appeared": max(cls._safe_int(old_stats.get("appeared", 0)), cls._safe_int(new_stats.get("appeared", 0))),
                "targeted": max(cls._safe_int(old_stats.get("targeted", 0)), cls._safe_int(new_stats.get("targeted", 0))),
            }
        return merged

    @classmethod
    def _merge_global_data(cls, existing: dict, incoming: dict, merged_talent_stats: dict):
        merged = deepcopy(existing) if isinstance(existing, dict) else {}
        incoming = incoming if isinstance(incoming, dict) else {}

        if "badges" in incoming or "badges" in merged:
            merged["badges"] = cls._merge_badges(merged.get("badges"), incoming.get("badges"))
        if "rankings" in incoming or "rankings" in merged:
            merged["rankings"] = cls._merge_rankings(merged.get("rankings"), incoming.get("rankings"))
        if "journal" in incoming or "journal" in merged:
            merged["journal"] = cls._merge_journal(merged.get("journal"), incoming.get("journal"), merged_talent_stats)

        for passthrough_key in ("lastSaved", "version"):
            if passthrough_key in incoming:
                merged[passthrough_key] = incoming[passthrough_key]

        for key, value in incoming.items():
            if key not in ("badges", "rankings", "journal", "lastSaved", "version"):
                merged[key] = deepcopy(value)

        return merged

    @staticmethod
    def _merge_badges(existing, incoming):
        existing = existing if isinstance(existing, dict) else {}
        incoming = incoming if isinstance(incoming, dict) else {}
        merged = deepcopy(existing)
        merged_badges = sorted(set(existing.get("badges", []) or []) | set(incoming.get("badges", []) or []))
        if merged_badges:
            merged["badges"] = merged_badges
        elif "badges" in merged:
            merged.pop("badges", None)
        for key, value in incoming.items():
            if key != "badges":
                merged[key] = deepcopy(value)
        return merged

    @classmethod
    def _merge_rankings(cls, existing, incoming):
        existing = existing if isinstance(existing, dict) else {}
        incoming = incoming if isinstance(incoming, dict) else {}
        merged = {}

        records_map = {}
        for record in cls._as_list(existing.get("records")) + cls._as_list(incoming.get("records")):
            if not isinstance(record, dict):
                continue
            game_id = record.get("gameID")
            if not game_id:
                continue
            previous = records_map.get(game_id)
            if previous is None or cls._record_sort_key(record) < cls._record_sort_key(previous):
                records_map[game_id] = record
        records = sorted(records_map.values(), key=cls._record_sort_key)[:11]
        merged["records"] = records

        merged["latest"] = cls._resolve_latest_index(records, incoming.get("latest"), existing.get("latest"))
        merged["total"] = max(cls._safe_int(existing.get("total", 0)), cls._safe_int(incoming.get("total", 0)), len(records))
        merged["won"] = max(cls._safe_int(existing.get("won", 0)), cls._safe_int(incoming.get("won", 0)))

        latest_daily = cls._pick_best_record(existing.get("latest_daily"), incoming.get("latest_daily"))
        if latest_daily:
            merged["latest_daily"] = latest_daily

        history = cls._merge_daily_history(existing, incoming)
        if history:
            merged["daily_history_dates"] = [day for day, _score in history]
            merged["daily_history_scores"] = [score for _day, score in history]

        return merged

    @classmethod
    def _merge_daily_history(cls, existing: dict, incoming: dict):
        history = {}
        for source in (existing, incoming):
            if not isinstance(source, dict):
                continue
            dates = cls._as_list(source.get("daily_history_dates"))
            scores = cls._as_list(source.get("daily_history_scores"))
            for index, day in enumerate(dates):
                if index >= len(scores):
                    continue
                day_int = cls._safe_int(day, None)
                if day_int is None:
                    continue
                history[day_int] = max(history.get(day_int, 0), cls._safe_int(scores[index], 0))
        return sorted(history.items())

    @classmethod
    def _pick_best_record(cls, left, right):
        candidates = [record for record in (left, right) if isinstance(record, dict)]
        if not candidates:
            return None
        return min(candidates, key=cls._daily_record_sort_key)

    @classmethod
    def _resolve_latest_index(cls, records, preferred_a, preferred_b):
        candidates = [cls._safe_int(preferred_a, -1), cls._safe_int(preferred_b, -1)]
        for candidate in candidates:
            if 0 <= candidate < len(records):
                return candidate
        return 0 if records else -1

    @classmethod
    def _merge_journal(cls, existing, incoming, merged_talent_stats):
        existing = existing if isinstance(existing, dict) else {}
        incoming = incoming if isinstance(incoming, dict) else {}
        merged = deepcopy(existing)

        if any(key in incoming or key in merged for key in ("catalog_classes", "catalog_seen", "catalog_uses")):
            merged.update(cls._merge_sparse_class_stats(
                existing,
                incoming,
                classes_key="catalog_classes",
                bool_key="catalog_seen",
                count_key="catalog_uses",
            ))

        if any(key in incoming or key in merged for key in ("bestiary_classes", "bestiary_seen", "bestiary_encounters")):
            merged.update(cls._merge_sparse_class_stats(
                existing,
                incoming,
                classes_key="bestiary_classes",
                bool_key="bestiary_seen",
                count_key="bestiary_encounters",
            ))

        if "documents" in incoming or "documents" in merged:
            merged["documents"] = cls._merge_documents(existing.get("documents"), incoming.get("documents"))

        merged["talent_counts"] = cls._build_talent_value_map(merged_talent_stats, "selected", existing, incoming, "talent_counts")
        merged["talent_metamorph_appearances"] = cls._build_talent_value_map(merged_talent_stats, "appeared", existing, incoming, "talent_metamorph_appearances")
        merged["transform_spell_talent_counts"] = cls._build_talent_value_map(merged_talent_stats, "targeted", existing, incoming, "transform_spell_talent_counts")

        existing_server = existing.get("server_talent_stats") if isinstance(existing.get("server_talent_stats"), dict) else {}
        incoming_server = incoming.get("server_talent_stats") if isinstance(incoming.get("server_talent_stats"), dict) else {}
        merged["server_talent_stats"] = cls._merge_talent_stats(
            cls._merge_talent_stats(existing_server, incoming_server),
            merged_talent_stats,
        )

        return merged

    @classmethod
    def _build_talent_value_map(cls, merged_talent_stats, stat_key, existing, incoming, journal_key):
        merged = {}
        existing_map = existing.get(journal_key) if isinstance(existing.get(journal_key), dict) else {}
        incoming_map = incoming.get(journal_key) if isinstance(incoming.get(journal_key), dict) else {}
        merged.update(cls._merge_max_map(existing_map, incoming_map))
        for talent_name, stats in merged_talent_stats.items():
            if not isinstance(stats, dict):
                continue
            merged[talent_name] = max(merged.get(talent_name, 0), cls._safe_int(stats.get(stat_key, 0)))
        return {key: value for key, value in merged.items() if value > 0}

    @classmethod
    def _merge_sparse_class_stats(cls, existing, incoming, classes_key, bool_key, count_key):
        merged_map = {}
        for source in (existing, incoming):
            if not isinstance(source, dict):
                continue
            classes = cls._as_list(source.get(classes_key))
            bools = cls._as_list(source.get(bool_key))
            counts = cls._as_list(source.get(count_key))
            for index, class_name in enumerate(classes):
                if not class_name:
                    continue
                entry = merged_map.setdefault(str(class_name), {"seen": False, "count": 0})
                if index < len(bools):
                    entry["seen"] = entry["seen"] or bool(bools[index])
                if index < len(counts):
                    entry["count"] = max(entry["count"], cls._safe_int(counts[index], 0))

        ordered_classes = sorted(merged_map.keys())
        return {
            classes_key: ordered_classes,
            bool_key: [merged_map[class_name]["seen"] for class_name in ordered_classes],
            count_key: [merged_map[class_name]["count"] for class_name in ordered_classes],
        }

    @classmethod
    def _merge_documents(cls, existing, incoming):
        existing = existing if isinstance(existing, dict) else {}
        incoming = incoming if isinstance(incoming, dict) else {}
        merged = {}
        for doc_name in set(existing.keys()) | set(incoming.keys()):
            left_pages = existing.get(doc_name) if isinstance(existing.get(doc_name), dict) else {}
            right_pages = incoming.get(doc_name) if isinstance(incoming.get(doc_name), dict) else {}
            page_names = set(left_pages.keys()) | set(right_pages.keys())
            merged_pages = {}
            for page_name in page_names:
                merged_pages[page_name] = max(
                    cls._safe_int(left_pages.get(page_name, 0)),
                    cls._safe_int(right_pages.get(page_name, 0)),
                )
            if merged_pages:
                merged[doc_name] = merged_pages
        return merged

    @staticmethod
    def _record_sort_key(record):
        return (
            0 if not record.get("custom_seed") else 1,
            -TalentCloudStore._safe_int(record.get("score", 0)),
            str(record.get("gameID", "")),
        )

    @staticmethod
    def _daily_record_sort_key(record):
        date_value = str(record.get("date", ""))
        return (
            -TalentCloudStore._safe_int(date_value.replace("-", ""), 0),
            -TalentCloudStore._safe_int(record.get("score", 0)),
            str(record.get("gameID", "")),
        )

    @staticmethod
    def _as_list(value):
        return value if isinstance(value, list) else []


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
            player_uuid = params.get("player_uuid", [""])[0]
            device_key = params.get("device_key", [""])[0]
            device_ip = params.get("device_ip", [""])[0] or self.client_address[0]
            prepare_restore = params.get("prepare_restore", ["0"])[0] == "1"
            data = self.store.download(
                player_uuid,
                device_key,
                device_ip,
                consume_restore_permission=not prepare_restore,
                prepare_restore=prepare_restore,
            )
            if data is None:
                self._send_json({"ok": False, "error": "not_found"}, status=404)
                return
            if data.get("restore_denied"):
                self._send_json({
                    "ok": False,
                    "error": "restore_not_allowed",
                    "player_uuid": data.get("player_uuid", ""),
                    "device_key": data.get("device_key", ""),
                    "aggregate": self.store.aggregate(),
                }, status=403)
                return
            data["ok"] = True
            data["aggregate"] = self.store.aggregate()
            self._send_json(data)
            return
        self._send_json({"ok": False, "error": "not_found"}, status=404)

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path not in ("/api/upload", "/api/restore/commit"):
            self._send_json({"ok": False, "error": "not_found"}, status=404)
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            if parsed.path == "/api/restore/commit":
                committed = self.store.commit_restore(
                    payload.get("player_uuid") or "",
                    payload.get("device_key") or "",
                    payload.get("restore_token") or "",
                )
                if not committed:
                    self._send_json({"ok": False, "error": "restore_commit_failed"}, status=409)
                    return
                self._send_json({"ok": True, "player_uuid": payload.get("player_uuid") or ""})
                return

            player_uuid = payload.get("player_uuid") or ""
            device_key = payload.get("device_key") or ""
            device_ip = payload.get("device_ip") or self.client_address[0]
            local_ip = payload.get("local_ip") or ""
            global_data = payload.get("global_data") or {}
            talent_stats = payload.get("talent_stats") or {}
            if not isinstance(global_data, dict) or not isinstance(talent_stats, dict):
                raise ValueError("invalid payload")
            if self.store.is_blacklisted_identity(player_uuid, device_key, device_ip):
                self._send_json({"ok": True, "blacklisted": True, "aggregate": self.store.aggregate()})
                return
            self.store.delete_legacy_ip_record(device_ip, local_ip)
            merged = self.store.upload(player_uuid, device_key, device_ip, global_data, talent_stats)
            self._send_json({
                "ok": True,
                "player_uuid": merged["player_uuid"],
                "device_key": merged["device_key"],
                "global_data": merged["global_data"],
                "talent_stats": merged["talent_stats"],
                "aggregate": self.store.aggregate(),
            })
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
