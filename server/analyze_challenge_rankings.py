import json
import math
import sqlite3
from collections import Counter, defaultdict
from itertools import combinations
from pathlib import Path


DB_PATH = Path(r"D:\STUDY\test\talent_cloud_analysis_latest_retry.sqlite3")
OUTPUT_PATH = Path(r"D:\STUDY\Dungeon\transformation-pixel-dungeon-master\server\challenge_analysis.json")
CHALLENGE_MESSAGES = Path(
    r"D:\STUDY\Dungeon\transformation-pixel-dungeon-master\core\src\main\assets\messages\misc\misc_zh.properties"
)

CHALLENGES = [
    ("harsh_environment", 8192),
    ("extreme_environment", 16384),
    ("champion_enemies", 128),
    ("stronger_bosses", 256),
    ("no_food", 1),
    ("no_armor", 2),
    ("no_healing", 4),
    ("no_herbalism", 8),
    ("swarm_intelligence", 16),
    ("darkness", 32),
    ("no_scrolls", 64),
    ("weakened_talent", 512),
    ("max_wheat", 2048),
    ("negative", 4096),
    ("red_envelope", 1024),
    ("test_mode", 32768),
]
CHALLENGE_MASKS = dict(CHALLENGES)
TEST_MODE_MASK = CHALLENGE_MASKS["test_mode"]


def parse_properties(path):
    labels = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        if key.startswith("challenges.") and not key.endswith("_desc"):
            labels[key.removeprefix("challenges.")] = value.replace("\\n", " ").strip()
    return labels


def as_bool(value):
    return value is True or value == 1 or value == "true"


def as_int(value, default=0):
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def wilson(successes, trials, z=1.959963984540054):
    if trials <= 0:
        return None, None
    p = successes / trials
    denominator = 1 + z * z / trials
    centre = (p + z * z / (2 * trials)) / denominator
    margin = z * math.sqrt((p * (1 - p) + z * z / (4 * trials)) / trials) / denominator
    return max(0.0, centre - margin), min(1.0, centre + margin)


def rate_row(label, mask, scope, records, extra=None):
    wins = sum(1 for record in records if record["win"])
    total = len(records)
    low, high = wilson(wins, total)
    row = {
        "口径": scope,
        "挑战": label,
        "挑战枚举": mask[0],
        "掩码": mask[1],
        "对局数": total,
        "胜局数": wins,
        "负局数": total - wins,
        "胜率": wins / total if total else None,
        "胜率95%下限": low,
        "胜率95%上限": high,
    }
    if extra:
        row.update(extra)
    return row


def challenge_records(records, mask):
    return [record for record in records if record["challenges"] & mask]


def analyze_scope(records, scope):
    all_wins = sum(record["win"] for record in records)
    all_total = len(records)
    type_rows = []
    for name, mask in CHALLENGES:
        enabled = challenge_records(records, mask)
        disabled = [record for record in records if not record["challenges"] & mask]
        enabled_wins = sum(record["win"] for record in enabled)
        disabled_wins = sum(record["win"] for record in disabled)
        enabled_rate = enabled_wins / len(enabled) if enabled else None
        disabled_rate = disabled_wins / len(disabled) if disabled else None
        type_rows.append(
            rate_row(
                name,
                (name, mask),
                scope,
                enabled,
                {
                    "未开启对局数": len(disabled),
                    "未开启胜局数": disabled_wins,
                    "未开启胜率": disabled_rate,
                    "胜率差百分点": (enabled_rate - disabled_rate) * 100
                    if enabled_rate is not None and disabled_rate is not None
                    else None,
                    "相对提升倍数": enabled_rate / disabled_rate
                    if enabled_rate is not None and disabled_rate not in (None, 0)
                    else None,
                },
            )
        )

    count_groups = defaultdict(list)
    for record in records:
        count_groups[record["challenge_count"]].append(record)
    count_rows = []
    for challenge_count in sorted(count_groups):
        group = count_groups[challenge_count]
        count_rows.append(
            {
                "口径": scope,
                "开启挑战数量": challenge_count,
                "对局数": len(group),
                "胜局数": sum(record["win"] for record in group),
                "负局数": sum(not record["win"] for record in group),
                "胜率": sum(record["win"] for record in group) / len(group),
                "占该口径比例": len(group) / all_total if all_total else None,
                "平均挑战数": challenge_count,
            }
        )

    pair_rows = []
    for (name_a, mask_a), (name_b, mask_b) in combinations(CHALLENGES, 2):
        a = sum(1 for record in records if record["challenges"] & mask_a)
        b = sum(1 for record in records if record["challenges"] & mask_b)
        both_records = [record for record in records if record["challenges"] & mask_a and record["challenges"] & mask_b]
        both = len(both_records)
        union = a + b - both
        p_a = a / all_total if all_total else 0
        p_b = b / all_total if all_total else 0
        p_ab = both / all_total if all_total else 0
        expected = p_a * p_b
        phi_denominator = math.sqrt(p_a * (1 - p_a) * p_b * (1 - p_b))
        pair_rows.append(
            {
                "口径": scope,
                "挑战A": name_a,
                "挑战B": name_b,
                "共同开启次数": both,
                "共同开启胜局数": sum(record["win"] for record in both_records),
                "共同开启胜率": sum(record["win"] for record in both_records) / both if both else None,
                "挑战A开启次数": a,
                "挑战B开启次数": b,
                "Jaccard": both / union if union else None,
                "Lift": p_ab / expected if expected else None,
                "Phi": ((p_ab - expected) / phi_denominator) if phi_denominator else None,
            }
        )
    pair_rows.sort(key=lambda row: ((row["Lift"] is not None, row["Lift"] or 0), row["共同开启次数"]), reverse=True)

    combination_groups = defaultdict(list)
    for record in records:
        combination_groups[record["challenges"]].append(record)
    combination_rows = []
    for mask, group in sorted(combination_groups.items(), key=lambda item: (-len(item[1]), item[0])):
        names = [name for name, challenge_mask in CHALLENGES if mask & challenge_mask]
        wins = sum(record["win"] for record in group)
        combination_rows.append(
            {
                "口径": scope,
                "挑战掩码": mask,
                "开启挑战数量": len(names),
                "挑战组合": ", ".join(names) if names else "无挑战",
                "对局数": len(group),
                "胜局数": wins,
                "负局数": len(group) - wins,
                "胜率": wins / len(group),
            }
        )

    challenge_selection_rows = []
    for name, mask in CHALLENGES:
        group = challenge_records(records, mask)
        wins = sum(record["win"] for record in group)
        challenge_selection_rows.append(
            {
                "口径": scope,
                "挑战": name,
                "开启次数": len(group),
                "胜局数": wins,
                "负局数": len(group) - wins,
                "胜率": wins / len(group) if group else None,
                "占全部对局比例": len(group) / all_total if all_total else None,
            }
        )

    return {
        "summary": {
            "口径": scope,
            "对局数": all_total,
            "胜局数": all_wins,
            "负局数": all_total - all_wins,
            "胜率": all_wins / all_total if all_total else None,
        },
        "type_rows": type_rows,
        "count_rows": count_rows,
        "pair_rows": pair_rows,
        "combination_rows": combination_rows,
        "challenge_selection_rows": challenge_selection_rows,
    }


def main():
    labels = parse_properties(CHALLENGE_MESSAGES)
    db = sqlite3.connect(DB_PATH)
    blacklisted_devices = {row[0] for row in db.execute("SELECT device_ip FROM device_blacklist")}
    player_rows = db.execute("SELECT player_uuid, device_ip, global_data FROM player_cloud_data").fetchall()
    records = []
    quality = Counter()
    seen_game_ids = set()
    duplicate_game_ids = 0
    for player_uuid, device_ip, global_data_json in player_rows:
        quality["玩家总数"] += 1
        if device_ip in blacklisted_devices:
            quality["黑名单玩家数"] += 1
            continue
        try:
            global_data = json.loads(global_data_json or "{}")
        except json.JSONDecodeError:
            quality["global_data解析失败"] += 1
            continue
        rankings = global_data.get("rankings")
        if not isinstance(rankings, dict):
            quality["无排行榜玩家数"] += 1
            continue
        quality["有排行榜玩家数"] += 1
        ranking_records = list(rankings.get("records") or [])
        latest_daily = rankings.get("latest_daily")
        if isinstance(latest_daily, dict):
            ranking_records.append(latest_daily)
            quality["加入latest_daily记录数"] += 1
        for index, raw in enumerate(ranking_records):
            if not isinstance(raw, dict):
                quality["非对象记录数"] += 1
                continue
            game_id = raw.get("gameID") or f"{player_uuid}:record:{index}"
            if game_id in seen_game_ids:
                duplicate_game_ids += 1
                continue
            seen_game_ids.add(game_id)
            game_data = raw.get("gameData") if isinstance(raw.get("gameData"), dict) else {}
            challenge_mask = as_int(game_data.get("challenges", raw.get("challenges", 0)))
            if "win" not in raw:
                quality["缺失胜负字段记录数"] += 1
                continue
            custom_seed = raw.get("custom_seed") or game_data.get("custom_seed") or ""
            daily = as_bool(raw.get("daily", game_data.get("daily", False)))
            test_mode = bool(challenge_mask & TEST_MODE_MASK)
            records.append(
                {
                    "player_uuid": player_uuid,
                    "device_ip": device_ip,
                    "game_id": game_id,
                    "win": as_bool(raw.get("win")),
                    "challenges": challenge_mask,
                    "challenge_count": sum(1 for _name, mask in CHALLENGES if challenge_mask & mask),
                    "custom_seed": custom_seed,
                    "daily": daily,
                    "test_mode": test_mode,
                    "date": raw.get("date", ""),
                    "version": raw.get("version", ""),
                }
            )

    db.close()
    quality["排行榜原始记录数"] = sum(
        1 for _ in records
    ) + duplicate_game_ids + quality["缺失胜负字段记录数"] + quality["非对象记录数"]
    quality["去重后有效记录数"] = len(records)
    quality["重复gameID记录数"] = duplicate_game_ids
    quality["自定义种子记录数"] = sum(bool(record["custom_seed"]) for record in records)
    quality["每日模式记录数"] = sum(record["daily"] for record in records)
    quality["测试模式记录数"] = sum(record["test_mode"] for record in records)
    quality["无挑战记录数"] = sum(record["challenges"] == 0 for record in records)
    quality["标准可比记录数"] = sum(
        not record["custom_seed"] and not record["daily"] and not record["test_mode"]
        for record in records
    )

    standard_records = [
        record
        for record in records
        if not record["custom_seed"] and not record["daily"] and not record["test_mode"]
    ]
    scopes = {
        "全量排行榜记录": analyze_scope(records, "全量排行榜记录"),
        "标准可比对局": analyze_scope(standard_records, "标准可比对局"),
    }

    output = {
        "metadata": {
            "source": str(DB_PATH),
            "player_count": len(player_rows),
            "blacklist_count": len(blacklisted_devices),
            "challenge_labels": labels,
            "challenge_definitions": [{"name": name, "mask": mask, "label": labels.get(name, name)} for name, mask in CHALLENGES],
            "definitions": {
                "win_rate": "胜局数 / 对局数",
                "standard_scope": "排除自定义种子、每日模式和 TEST_MODE；黑名单设备不纳入分析",
                "record_scope": "使用云端排行榜 records，并补充 latest_daily；服务器仅保留排行榜前11条记录，因此不等同于全部历史对局",
                "pair_metrics": "Jaccard 衡量共现集合重叠，Lift > 1 表示共同开启高于独立期望，Phi > 0 表示正相关",
                "confidence_interval": "Wilson 95% 区间",
            },
        },
        "quality": dict(quality),
        "scopes": scopes,
        "records": records,
    }
    OUTPUT_PATH.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"output": str(OUTPUT_PATH), "quality": dict(quality), "scope_summaries": {key: value["summary"] for key, value in scopes.items()}}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
