package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

import java.util.ArrayList;

public class AgentMinHistoryTracker {

	public static final int HISTORY_ROWS = 16;
	public static final int HISTORY_FEATURES = 16;

	public enum EventType {
		MOVE,
		WAIT,
		ATTACK,
		PICK_UP,
		USE_STAIRS,
		DRINK_HEALING,
		EAT_FOOD,
		THROW_WEAPON,
		ZAP_WAND,
		DROP,
		DAMAGE_TAKEN,
		KILL,
		EXPLORE,
		DEATH,
		OTHER
	}

	private static final ArrayList<HistoryRecord> records = new ArrayList<>();

	private AgentMinHistoryTracker() {
	}

	public static void reset() {
		records.clear();
	}

	// 供未来的动作执行器直接记录模型选中的动作，便于把“选择”与后续奖励对齐。
	public static void recordChosenAction(AgentMinAction action, AgentMinState state) {
		if (action == null) {
			return;
		}
		HistoryRecord record = baseRecord(toEventType(action.kind), 0f, state);
		record.fromCell = action.fromCell;
		record.targetCell = action.targetCell;
		record.directionIndex = action.directionIndex;
		record.success = action.valid;
		record.failed = !action.valid;
		push(record);
	}

	public static void recordMove(int fromCell, int targetCell, float reward) {
		HistoryRecord record = baseRecord(EventType.MOVE, reward, null);
		record.fromCell = fromCell;
		record.targetCell = targetCell;
		record.success = true;
		record.directionIndex = directionIndex(fromCell, targetCell);
		push(record);
	}

	public static void recordWait(int pos, float reward) {
		HistoryRecord record = baseRecord(EventType.WAIT, reward, null);
		record.fromCell = pos;
		record.targetCell = pos;
		record.success = reward >= 0f;
		record.failed = reward < 0f;
		push(record);
	}

	public static void recordAttack(int targetCell, int damage, float reward) {
		HistoryRecord record = baseRecord(EventType.ATTACK, reward, null);
		record.fromCell = heroPos();
		record.targetCell = targetCell;
		record.damage = damage;
		record.success = damage > 0;
		record.failed = damage <= 0;
		push(record);
	}

	public static void recordPickup(int pos, boolean success, float reward) {
		HistoryRecord record = baseRecord(EventType.PICK_UP, reward, null);
		record.fromCell = pos;
		record.targetCell = pos;
		record.success = success;
		record.failed = !success;
		push(record);
	}

	public static void recordDrop(int pos, float reward) {
		HistoryRecord record = baseRecord(EventType.DROP, reward, null);
		record.fromCell = pos;
		record.targetCell = pos;
		record.success = true;
		push(record);
	}

	public static void recordTransition(boolean descend, int pos, float reward) {
		HistoryRecord record = baseRecord(EventType.USE_STAIRS, reward, null);
		record.fromCell = pos;
		record.targetCell = pos;
		record.success = true;
		record.directionIndex = descend ? 1 : 0;
		push(record);
	}

	public static void recordDamageTaken(int damage, float reward) {
		HistoryRecord record = baseRecord(EventType.DAMAGE_TAKEN, reward, null);
		record.fromCell = heroPos();
		record.targetCell = heroPos();
		record.damage = damage;
		record.success = damage > 0;
		push(record);
	}

	public static void recordKill(int pos, boolean boss, float reward) {
		HistoryRecord record = baseRecord(EventType.KILL, reward, null);
		record.fromCell = heroPos();
		record.targetCell = pos;
		record.killed = true;
		record.success = true;
		record.damage = boss ? 2 : 1;
		push(record);
	}

	public static void recordExplore(int newlyVisited, float reward) {
		HistoryRecord record = baseRecord(EventType.EXPLORE, reward, null);
		record.fromCell = heroPos();
		record.targetCell = heroPos();
		record.success = newlyVisited > 0;
		record.damage = newlyVisited;
		push(record);
	}

	public static void recordDeath(float reward) {
		HistoryRecord record = baseRecord(EventType.DEATH, reward, null);
		record.fromCell = heroPos();
		record.targetCell = heroPos();
		record.failed = true;
		push(record);
	}

	public static float[][] encode(AgentMinState state) {
		float[][] matrix = new float[HISTORY_ROWS][HISTORY_FEATURES];
		for (int row = 0; row < records.size() && row < HISTORY_ROWS; row++) {
			HistoryRecord record = records.get(records.size() - 1 - row);
			float[] out = matrix[row];
			out[0] = 1f;
			out[1] = (record.type.ordinal() + 1) / (float)EventType.values().length;
			out[2] = normSigned(record.reward, 20f);
			out[3] = normSigned(record.episodeReward, 100f);
			out[4] = norm(record.fromCell, levelLength(state));
			out[5] = norm(record.targetCell, levelLength(state));
			out[6] = record.directionIndex < 0 ? 0f : norm(record.directionIndex + 1, 8f);
			out[7] = record.success ? 1f : 0f;
			out[8] = record.failed ? 1f : 0f;
			out[9] = norm(record.damage, 120f);
			out[10] = record.killed ? 1f : 0f;
			out[11] = clamp(record.hpRatio);
			out[12] = norm(record.visibleEnemies, 16f);
			out[13] = norm(record.inventoryCount, AgentMinEncodedState.INVENTORY_ROWS);
			out[14] = norm(record.depth, 30f);
			out[15] = norm(consecutiveSameTypeFromNewest(row), HISTORY_ROWS);
		}
		return matrix;
	}

	private static HistoryRecord baseRecord(EventType type, float reward, AgentMinState state) {
		HistoryRecord record = new HistoryRecord();
		record.type = type;
		record.reward = reward;
		record.episodeReward = AgentMinRewardTracker.episodeReward();
		record.depth = Dungeon.depth;
		Hero hero = Dungeon.hero;
		if (hero != null) {
			record.hpRatio = hero.HT <= 0 ? 0f : hero.HP / (float)hero.HT;
			record.fromCell = hero.pos;
			record.targetCell = hero.pos;
		}
		if (state != null) {
			record.visibleEnemies = state.combat.visibleEnemies.size();
			record.inventoryCount = state.inventory.equipped.size() + state.inventory.backpack.size();
		} else if (Dungeon.level != null) {
			record.visibleEnemies = Dungeon.level.mobs.size();
		}
		return record;
	}

	private static void push(HistoryRecord record) {
		records.add(record);
		while (records.size() > HISTORY_ROWS) {
			records.remove(0);
		}
	}

	private static EventType toEventType(AgentMinAction.Kind kind) {
		if (kind == null) {
			return EventType.OTHER;
		}
		switch (kind) {
			case ZERO_RANDOM_MOVE: return EventType.MOVE;
			case MOVE: return EventType.MOVE;
			case WAIT: return EventType.WAIT;
			case ATTACK: return EventType.ATTACK;
			case PICK_UP: return EventType.PICK_UP;
			case USE_STAIRS: return EventType.USE_STAIRS;
			case DRINK_HEALING: return EventType.DRINK_HEALING;
			case EAT_FOOD: return EventType.EAT_FOOD;
			case THROW_WEAPON: return EventType.THROW_WEAPON;
			case ZAP_WAND: return EventType.ZAP_WAND;
			default: return EventType.OTHER;
		}
	}

	private static int directionIndex(int fromCell, int targetCell) {
		if (Dungeon.level == null || fromCell < 0 || targetCell < 0) {
			return -1;
		}
		int width = Dungeon.level.width();
		int dx = targetCell % width - fromCell % width;
		int dy = targetCell / width - fromCell / width;
		if (dx == -1 && dy == -1) return 0;
		if (dx == 0 && dy == -1) return 1;
		if (dx == 1 && dy == -1) return 2;
		if (dx == -1 && dy == 0) return 3;
		if (dx == 1 && dy == 0) return 4;
		if (dx == -1 && dy == 1) return 5;
		if (dx == 0 && dy == 1) return 6;
		if (dx == 1 && dy == 1) return 7;
		return -1;
	}

	private static int consecutiveSameTypeFromNewest(int encodedRow) {
		int newestIndex = records.size() - 1 - encodedRow;
		if (newestIndex < 0 || newestIndex >= records.size()) {
			return 0;
		}
		EventType type = records.get(newestIndex).type;
		int count = 0;
		for (int i = newestIndex; i >= 0; i--) {
			if (records.get(i).type == type) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	private static int heroPos() {
		return Dungeon.hero == null ? -1 : Dungeon.hero.pos;
	}

	private static int levelLength(AgentMinState state) {
		if (state != null && state.level.length > 0) {
			return state.level.length;
		}
		return Dungeon.level == null ? 1 : Math.max(1, Dungeon.level.length());
	}

	private static float norm(float value, float max) {
		if (value < 0 || max <= 0) return 0f;
		return clamp(value / max);
	}

	private static float normSigned(float value, float absMax) {
		if (absMax <= 0) return 0f;
		return Math.max(-1f, Math.min(1f, value / absMax));
	}

	private static float clamp(float value) {
		return Math.max(0f, Math.min(1f, value));
	}

	private static class HistoryRecord {
		EventType type = EventType.OTHER;
		float reward;
		float episodeReward;
		int fromCell = -1;
		int targetCell = -1;
		int directionIndex = -1;
		boolean success;
		boolean failed;
		int damage;
		boolean killed;
		float hpRatio;
		int visibleEnemies;
		int inventoryCount;
		int depth;
	}
}
