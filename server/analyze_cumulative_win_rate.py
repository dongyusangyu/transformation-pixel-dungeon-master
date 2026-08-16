import json
import sqlite3
from pathlib import Path


DB_PATH = Path(r"D:\STUDY\test\talent_cloud_analysis_latest_retry.sqlite3")
OUTPUT_PATH = Path(r"D:\STUDY\Dungeon\transformation-pixel-dungeon-master\server\cumulative_win_rate_analysis.json")


def as_int(value, default=0):
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def load_rows(db, include_blacklisted):
    blacklist = {row[0] for row in db.execute("SELECT device_ip FROM device_blacklist")}
    rows = []
    quality = {
        "玩家总数": 0,
        "有排行榜玩家数": 0,
        "无排行榜玩家数": 0,
        "黑名单玩家数": 0,
        "缺少total字段玩家数": 0,
        "缺少won字段玩家数": 0,
        "累计游戏数": 0,
        "累计胜局数": 0,
    }
    for player_uuid, device_ip, global_data_json in db.execute(
        "SELECT player_uuid, device_ip, global_data FROM player_cloud_data"
    ):
        quality["玩家总数"] += 1
        blacklisted = device_ip in blacklist
        if blacklisted:
            quality["黑名单玩家数"] += 1
        if blacklisted and not include_blacklisted:
            continue
        try:
            global_data = json.loads(global_data_json or "{}")
        except json.JSONDecodeError:
            quality["无排行榜玩家数"] += 1
            continue
        rankings = global_data.get("rankings")
        if not isinstance(rankings, dict):
            quality["无排行榜玩家数"] += 1
            continue
        quality["有排行榜玩家数"] += 1
        total_present = "total" in rankings
        won_present = "won" in rankings
        if not total_present:
            quality["缺少total字段玩家数"] += 1
        if not won_present:
            quality["缺少won字段玩家数"] += 1
        total = max(0, as_int(rankings.get("total")))
        won = max(0, as_int(rankings.get("won")))
        if won > total and total > 0:
            won = total
        quality["累计游戏数"] += total
        quality["累计胜局数"] += won
        rows.append(
            {
                "player_uuid": player_uuid,
                "device_ip": device_ip,
                "黑名单": blacklisted,
                "累计游戏次数": total,
                "累计胜利次数": won,
                "累计失败次数": max(0, total - won),
                "个人总胜率": won / total if total else None,
                "排行榜当前records数": len(rankings.get("records") or []),
                "排行榜当前records胜局数": sum(bool(row.get("win")) for row in rankings.get("records") or [] if isinstance(row, dict)),
            }
        )
    return rows, quality


def summarize(rows, scope):
    total_games = sum(row["累计游戏次数"] for row in rows)
    total_wins = sum(row["累计胜利次数"] for row in rows)
    return {
        "口径": scope,
        "玩家数": len(rows),
        "累计游戏次数": total_games,
        "累计胜利次数": total_wins,
        "累计失败次数": max(0, total_games - total_wins),
        "真实总胜率": total_wins / total_games if total_games else None,
        "个人胜率简单平均": sum(row["个人总胜率"] for row in rows if row["个人总胜率"] is not None) / sum(row["个人总胜率"] is not None for row in rows) if any(row["个人总胜率"] is not None for row in rows) else None,
        "当前records累计胜率": sum(row["排行榜当前records胜局数"] for row in rows) / sum(row["排行榜当前records数"] for row in rows) if sum(row["排行榜当前records数"] for row in rows) else None,
    }


def main():
    db = sqlite3.connect(DB_PATH)
    all_rows, all_quality = load_rows(db, True)
    non_blacklisted_rows, non_blacklisted_quality = load_rows(db, False)
    db.close()
    output = {
        "metadata": {
            "source": str(DB_PATH),
            "definition": "真实总胜率 = 所有纳入玩家的 rankings.won 之和 / rankings.total 之和",
            "scope_note": "rankings.total/won 是客户端累计的普通非自定义种子、非轮回模式游戏统计；黑名单口径单独列出。",
        },
        "summaries": {
            "全部玩家": summarize(all_rows, "全部玩家"),
            "剔除黑名单玩家": summarize(non_blacklisted_rows, "剔除黑名单玩家"),
        },
        "quality": {
            "全部玩家": all_quality,
            "剔除黑名单玩家": non_blacklisted_quality,
        },
        "players": non_blacklisted_rows,
    }
    OUTPUT_PATH.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(output["summaries"], ensure_ascii=False, indent=2))
    print(json.dumps(output["quality"], ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
