import json
import sqlite3
from collections import Counter
from pathlib import Path


DB_PATH = Path(r"D:\STUDY\test\talent_cloud_analysis.sqlite3")


def main():
    db = sqlite3.connect(DB_PATH)
    print("size=", DB_PATH.stat().st_size)
    print("tables=", db.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name").fetchall())
    print("players=", db.execute("SELECT COUNT(*), MIN(created_at), MAX(updated_at) FROM player_cloud_data").fetchone())
    row = db.execute("SELECT player_uuid, global_data, talent_stats FROM player_cloud_data WHERE global_data LIKE '%rankings%' LIMIT 1").fetchone()
    print("uuid=", row[0])
    global_data = json.loads(row[1])
    print("global_keys=", sorted(global_data.keys()))
    print("global_prefix=", row[1][:1000])
    if isinstance(global_data.get("rankings"), dict):
        print("rankings_keys=", sorted(global_data["rankings"].keys()))
        rankings = global_data["rankings"]
        print("rankings_keys=", sorted(rankings.keys()))
        records = rankings.get("records", [])
        print("record_count=", len(records))
        print("record_fields=", sorted(records[0].keys()) if records else [])
        print("record_summary=", [{key: record.get(key) for key in ("gameID", "win", "custom_seed", "daily", "new_cycle", "date", "version")} for record in records[:5]])
        print("game_data_keys=", sorted(records[0].get("gameData", {}).keys()) if records else [])
        print("challenge_value=", records[0].get("gameData", {}).get("challenges") if records else None)
    key_counts = Counter()
    rankings_rows = []
    lengths = []
    for player_uuid, global_json in db.execute("SELECT player_uuid, global_data FROM player_cloud_data"):
        try:
            data = json.loads(global_json)
        except (TypeError, json.JSONDecodeError):
            continue
        key_counts.update(data.keys())
        lengths.append(len(global_json))
        if "rankings" in data:
            rankings_rows.append((player_uuid, data["rankings"]))
    print("global_key_counts=", key_counts)
    print("global_length_min_max=", (min(lengths), max(lengths)) if lengths else None)
    print("rankings_rows=", len(rankings_rows))
    if rankings_rows:
        print("rankings_example=", json.dumps(rankings_rows[0][1], ensure_ascii=False)[:5000])
    db.close()


if __name__ == "__main__":
    main()
