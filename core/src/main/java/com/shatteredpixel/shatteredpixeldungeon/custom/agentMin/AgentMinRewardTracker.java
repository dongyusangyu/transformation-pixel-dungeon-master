package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import java.util.ArrayList;

public class AgentMinRewardTracker {

	private static float episodeReward;
	private static float pendingReward;
	private static final ArrayList<RewardEvent> recentEvents = new ArrayList<>();
	private static final ArrayList<DroppedItemRecord> recentDrops = new ArrayList<>();
	private static final ArrayList<ItemCellRecord> recentPickupFailures = new ArrayList<>();

	public static void resetEpisode() {
		episodeReward = 0f;
		pendingReward = 0f;
		recentEvents.clear();
		recentDrops.clear();
		recentPickupFailures.clear();
		AgentMinHistoryTracker.reset();
	}

	public static float episodeReward() {
		return episodeReward;
	}

	public static float pendingReward() {
		return pendingReward;
	}

	public static float consumePendingReward() {
		float reward = pendingReward;
		pendingReward = 0f;
		return reward;
	}

	public static ArrayList<RewardEvent> recentEvents() {
		return new ArrayList<>(recentEvents);
	}

	public static void onHeroMove(boolean travelling) {
		onHeroMove(-1, Dungeon.hero == null ? -1 : Dungeon.hero.pos, travelling);
	}

	public static void onHeroMove(int fromCell, int targetCell, boolean travelling) {
		if (travelling) {
			float reward = add("移动", AgentMinRewardConfig.MOVE_PENALTY);
			AgentMinHistoryTracker.recordMove(fromCell, targetCell, reward);
		}
	}

	public static void onHeroDamage(int effectiveDamage) {
		if (effectiveDamage > 0) {
			float reward = add("失去生命", effectiveDamage * AgentMinRewardConfig.HP_LOSS_PENALTY_PER_POINT);
			AgentMinHistoryTracker.recordDamageTaken(effectiveDamage, reward);
		}
	}

	public static void onHeroDeath() {
		float reward = add("英雄死亡", AgentMinRewardConfig.HERO_DEATH_PENALTY);
		AgentMinHistoryTracker.recordDeath(reward);
	}

	public static void onHeroAttackEnemy(Char enemy, int damage) {
		if (enemy != null && enemy.alignment == Char.Alignment.ENEMY && damage > 0) {
			float reward = add("攻击敌人", AgentMinRewardConfig.ATTACK_HIT_REWARD + damage * AgentMinRewardConfig.DAMAGE_ENEMY_REWARD_PER_POINT);
			AgentMinHistoryTracker.recordAttack(enemy.pos, damage, reward);
		}
	}

	public static void onEnemyKilled(Mob mob, Object cause) {
		if (mob == null || mob.alignment != Char.Alignment.ENEMY) {
			return;
		}
		float reward = AgentMinRewardConfig.KILL_ENEMY_REWARD;
		boolean boss = Char.hasProp(mob, Char.Property.BOSS);
		if (boss) {
			reward *= AgentMinRewardConfig.KILL_BOSS_MULTIPLIER;
		} else if (Char.hasProp(mob, Char.Property.MINIBOSS)) {
			reward *= AgentMinRewardConfig.KILL_MINIBOSS_MULTIPLIER;
		}
		reward = add("击杀敌人", reward);
		AgentMinHistoryTracker.recordKill(mob.pos, boss, reward);
	}

	public static void onPickup(Item item, int pos) {
		forgetPickupFailure(item, pos);
		if (wasRecentlyDropped(item, pos)) {
			float reward = add("拾取刚丢弃的物品", AgentMinRewardConfig.RECENT_DROP_PICKUP_PENALTY);
			AgentMinHistoryTracker.recordPickup(pos, false, reward);
		} else {
			float reward = add("拾取物品", AgentMinRewardConfig.PICKUP_REWARD);
			AgentMinHistoryTracker.recordPickup(pos, true, reward);
		}
	}

	public static void onPickupFailed(Item item, int pos) {
		if (!AgentMinRewardConfig.ENABLED || item == null) {
			return;
		}
		recentPickupFailures.add(new ItemCellRecord(item, pos));
		while (recentPickupFailures.size() > AgentMinRewardConfig.RECENT_PICKUP_FAILURE_MEMORY_SIZE) {
			recentPickupFailures.remove(0);
		}
		float reward = add("拾取失败", AgentMinRewardConfig.PICKUP_FAILED_PENALTY);
		AgentMinHistoryTracker.recordPickup(pos, false, reward);
	}

	public static boolean isRecentPickupFailure(String className, int image, int pos) {
		if (!AgentMinRewardConfig.ENABLED || className == null) {
			return false;
		}
		for (ItemCellRecord record : recentPickupFailures) {
			if (record.matches(className, image, pos)) {
				return true;
			}
		}
		return false;
	}

	public static void onDrop(Item item, int pos) {
		if (!AgentMinRewardConfig.ENABLED || item == null) {
			return;
		}
		recentDrops.add(new DroppedItemRecord(item, pos));
		while (recentDrops.size() > AgentMinRewardConfig.RECENT_DROP_MEMORY_SIZE) {
			recentDrops.remove(0);
		}
		float reward = add("丢弃物品", AgentMinRewardConfig.DROP_ITEM_PENALTY);
		AgentMinHistoryTracker.recordDrop(pos, reward);
	}

	public static void onTransition(boolean descend) {
		float reward = add(descend ? "下楼" : "上楼", descend ? AgentMinRewardConfig.DESCEND_REWARD : AgentMinRewardConfig.ASCEND_PENALTY);
		AgentMinHistoryTracker.recordTransition(descend, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static boolean[] snapshotVisited(Level level) {
		if (!AgentMinRewardConfig.ENABLED || level == null || level.visited == null) {
			return null;
		}
		return level.visited.clone();
	}

	public static void onObserve(Level level, boolean[] beforeVisited) {
		if (!AgentMinRewardConfig.ENABLED || level == null || beforeVisited == null || level.visited == null) {
			return;
		}
		int newlyVisited = 0;
		int length = Math.min(beforeVisited.length, level.visited.length);
		for (int i = 0; i < length; i++) {
			if (!beforeVisited[i] && level.visited[i]) {
				newlyVisited++;
			}
		}
		if (newlyVisited > 0) {
			float reward = add("探索新视野", newlyVisited * AgentMinRewardConfig.NEW_VISITED_CELL_REWARD);
			AgentMinHistoryTracker.recordExplore(newlyVisited, reward);
		}
	}

	private static float add(String reason, float reward) {
		if (!AgentMinRewardConfig.ENABLED || reward == 0f) {
			return 0f;
		}
		reward = Math.max(AgentMinRewardConfig.MIN_EVENT_REWARD, Math.min(AgentMinRewardConfig.MAX_EVENT_REWARD, reward));
		episodeReward += reward;
		pendingReward += reward;
		recentEvents.add(new RewardEvent(reason, reward, episodeReward));
		if (recentEvents.size() > 64) {
			recentEvents.remove(0);
		}
		return reward;
	}

	private static boolean wasRecentlyDropped(Item item, int pos) {
		if (item == null) {
			return false;
		}
		for (int i = recentDrops.size() - 1; i >= 0; i--) {
			if (recentDrops.get(i).matches(item, pos)) {
				recentDrops.remove(i);
				return true;
			}
		}
		return false;
	}

	private static void forgetPickupFailure(Item item, int pos) {
		if (item == null) {
			return;
		}
		for (int i = recentPickupFailures.size() - 1; i >= 0; i--) {
			if (recentPickupFailures.get(i).matches(item, pos)) {
				recentPickupFailures.remove(i);
			}
		}
	}

	public static class RewardEvent {
		public final String reason;
		public final float reward;
		public final float episodeReward;

		public RewardEvent(String reason, float reward, float episodeReward) {
			this.reason = reason;
			this.reward = reward;
			this.episodeReward = episodeReward;
		}
	}

	private static class DroppedItemRecord {
		private final int depth;
		private final int branch;
		private final int pos;
		private final String className;
		private final int image;

		private DroppedItemRecord(Item item, int pos) {
			this.depth = Dungeon.depth;
			this.branch = Dungeon.branch;
			this.pos = pos;
			this.className = item.getClass().getName();
			this.image = item.image();
		}

		private boolean matches(Item item, int pos) {
			return depth == Dungeon.depth
					&& branch == Dungeon.branch
					&& this.pos == pos
					&& className.equals(item.getClass().getName())
					&& image == item.image();
		}
	}

	private static class ItemCellRecord {
		private final int depth;
		private final int branch;
		private final int pos;
		private final String className;
		private final int image;

		private ItemCellRecord(Item item, int pos) {
			this.depth = Dungeon.depth;
			this.branch = Dungeon.branch;
			this.pos = pos;
			this.className = item.getClass().getName();
			this.image = item.image();
		}

		private boolean matches(Item item, int pos) {
			return item != null && matches(item.getClass().getName(), item.image(), pos);
		}

		private boolean matches(String className, int image, int pos) {
			return depth == Dungeon.depth
					&& branch == Dungeon.branch
					&& this.pos == pos
					&& this.className.equals(className)
					&& this.image == image;
		}
	}
}
