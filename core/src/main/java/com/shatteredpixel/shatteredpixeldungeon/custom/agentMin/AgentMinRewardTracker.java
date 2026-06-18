package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AgentMinRewardTracker {

	private static float episodeReward;
	private static float pendingReward;
	private static float lastDoorRewardTurn = -9999f;
	private static Object lastHeroRef;
	private static final ArrayList<RewardEvent> recentEvents = new ArrayList<>();
	private static final ArrayList<DroppedItemRecord> recentDrops = new ArrayList<>();
	private static final ArrayList<ItemCellRecord> recentPickupFailures = new ArrayList<>();

	public static void resetEpisode() {
		lastHeroRef = Dungeon.hero;
		resetEpisodeState();
	}

	private static void resetEpisodeState() {
		episodeReward = 0f;
		pendingReward = 0f;
		lastDoorRewardTurn = -9999f;
		recentEvents.clear();
		recentDrops.clear();
		recentPickupFailures.clear();
		AgentMinHistoryTracker.reset();
		AgentMinVisitTracker.reset();
		AgentMinExplorationTracker.reset();
	}

	public static float episodeReward() {
		ensureEpisode();
		return episodeReward;
	}

	public static float pendingReward() {
		ensureEpisode();
		return pendingReward;
	}

	public static float consumePendingReward() {
		ensureEpisode();
		float reward = pendingReward;
		pendingReward = 0f;
		return reward;
	}

	public static ArrayList<RewardEvent> recentEvents() {
		ensureEpisode();
		return new ArrayList<>(recentEvents);
	}

	public static void onHeroMove(boolean travelling) {
		onHeroMove(-1, Dungeon.hero == null ? -1 : Dungeon.hero.pos, travelling);
	}

	public static void onHeroMove(int fromCell, int targetCell, boolean travelling) {
		ensureEpisode();
		if (!travelling) {
			return;
		}
		float reward = add("move", AgentMinRewardConfig.MOVE_PENALTY);
		reward += add("visit", AgentMinVisitTracker.onHeroMove(fromCell, targetCell));
		reward += rewardForDoorPass(targetCell);
		AgentMinExplorationTracker.MoveProgress progress = AgentMinExplorationTracker.onMove(Dungeon.level, fromCell, targetCell);
		if (progress.frontierDelta > 0) {
			reward += add("frontier_closer", progress.frontierDelta * AgentMinRewardConfig.FRONTIER_STEP_CLOSER_REWARD);
		} else if (progress.frontierDelta < 0) {
			reward += add("frontier_away", -progress.frontierDelta * AgentMinRewardConfig.FRONTIER_STEP_AWAY_PENALTY);
		}
		if (progress.reachedNewDoor) {
			reward += add("reach_new_door", AgentMinRewardConfig.REACH_NEW_DOOR_REWARD);
		}
		if (progress.passedNewDoor) {
			reward += add("pass_new_door", AgentMinRewardConfig.PASS_NEW_DOOR_REWARD);
		}
		if (!progress.passedNewDoor && progress.immediateBacktrack) {
			reward += add("immediate_backtrack", AgentMinRewardConfig.IMMEDIATE_BACKTRACK_PENALTY);
		}
		if (!progress.passedNewDoor && progress.shortCycle) {
			reward += add("short_move_cycle", AgentMinRewardConfig.SHORT_CYCLE_PENALTY);
		}
		if (!progress.passedNewDoor && progress.smallAreaLoop) {
			reward += add("small_area_loop", AgentMinRewardConfig.SMALL_AREA_LOOP_PENALTY);
		}
		if (!progress.passedNewDoor && progress.frontierLoop) {
			reward += add("frontier_loop", AgentMinRewardConfig.FRONTIER_LOOP_PENALTY);
		}
		if (progress.roomStall) {
			reward += add("room_stall", AgentMinRewardConfig.ROOM_STALL_PENALTY);
		}
		if (progress.longStall) {
			reward += add("long_stall", AgentMinRewardConfig.LONG_STALL_PENALTY);
		}
		AgentMinHistoryTracker.recordMove(fromCell, targetCell, reward);
	}

	public static void onHeroWait() {
		ensureEpisode();
		float reward = add("wait", AgentMinRewardConfig.WAIT_PENALTY);
		AgentMinHistoryTracker.recordWait(Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onLockedDoorInteraction(boolean success, int cell) {
		ensureEpisode();
		float reward = add(success ? "locked_door_open" : "locked_door_fail",
				success ? AgentMinRewardConfig.LOCKED_DOOR_OPEN_REWARD : AgentMinRewardConfig.LOCKED_DOOR_FAIL_PENALTY);
		AgentMinHistoryTracker.recordTransition(success, cell, reward);
	}

	public static void onLockedChestInteraction(boolean success, int cell) {
		ensureEpisode();
		float reward = add(success ? "locked_chest_open" : "locked_chest_fail",
				success ? AgentMinRewardConfig.LOCKED_CHEST_OPEN_REWARD : AgentMinRewardConfig.LOCKED_CHEST_FAIL_PENALTY);
		AgentMinHistoryTracker.recordPickup(cell, success, reward);
	}

	public static void onFoodEaten(Hero hero) {
		ensureEpisode();
		if (!AgentMinRewardConfig.ENABLED || hero == null) {
			return;
		}
		Hunger hunger = hero.buff(Hunger.class);
		boolean hungry = hunger != null && hunger.level >= Hunger.HUNGRY;
		boolean starving = hunger != null && hunger.isStarving();
		boolean lowHp = hero.HP * 2 <= hero.HT;
		boolean fullHp = hero.HP >= hero.HT;

		float reward = 0f;
		if (lowHp) {
			reward += add("food_low_hp", AgentMinRewardConfig.FOOD_EAT_LOW_HP_REWARD);
		}
		if (hungry || starving) {
			reward += add(starving ? "food_starving" : "food_hungry", AgentMinRewardConfig.FOOD_EAT_HUNGRY_REWARD);
		}
		if ((!hungry && !starving) || fullHp) {
			reward += add("food_waste", AgentMinRewardConfig.FOOD_EAT_WASTEFUL_PENALTY);
		}
		if (reward != 0f) {
			AgentMinHistoryTracker.recordWait(hero.pos, reward);
		}
	}

	public static void onHeroDamage(int effectiveDamage) {
		ensureEpisode();
		if (effectiveDamage > 0) {
			float reward = add("hp_loss", effectiveDamage * AgentMinRewardConfig.HP_LOSS_PENALTY_PER_POINT);
			AgentMinHistoryTracker.recordDamageTaken(effectiveDamage, reward);
		}
	}

	public static void onHeroDeath() {
		ensureEpisode();
		float reward = add("hero_death", AgentMinRewardConfig.HERO_DEATH_PENALTY);
		AgentMinHistoryTracker.recordDeath(reward);
		writeDeathFlag();
	}

	private static void writeDeathFlag() {
		if (!AgentMinBridgeConfig.ENABLED) {
			return;
		}
		String path = System.getenv("AGENTMIN_DEATH_FLAG");
		if (path == null || path.length() == 0) {
			path = System.getProperty("agentmin.death_flag", "");
		}
		if (path.length() == 0) {
			return;
		}
		try {
			File file = new File(path);
			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			FileWriter writer = new FileWriter(file, false);
			writer.write("death\n");
			writer.write("episodeReward=" + episodeReward + "\n");
			writer.close();
			AgentMinRuntimeLog.log("hero death flag written: " + path);
		} catch (IOException e) {
			AgentMinRuntimeLog.log("failed to write hero death flag: " + e.getMessage());
		}
	}

	public static void onHeroAttackEnemy(Char enemy, int damage) {
		ensureEpisode();
		if (enemy != null && enemy.alignment == Char.Alignment.ENEMY && damage > 0) {
			float base = AgentMinRewardConfig.ATTACK_HIT_REWARD + damage * AgentMinRewardConfig.DAMAGE_ENEMY_REWARD_PER_POINT;
			int distance = Dungeon.level == null || Dungeon.hero == null ? 99 : Dungeon.level.distance(Dungeon.hero.pos, enemy.pos);
			if (distance <= 1) {
				base += AgentMinRewardConfig.MELEE_ADJACENT_HIT_REWARD;
			} else {
				base += AgentMinRewardConfig.RANGED_HIT_REWARD;
			}
			if (enemy.HP <= 0 || damage >= enemy.HP) {
				base += AgentMinRewardConfig.FINISHING_HIT_REWARD;
			}
			if (Dungeon.hero != null && Dungeon.hero.HP * 2 <= Dungeon.hero.HT) {
				base += AgentMinRewardConfig.LOW_HP_COMBAT_BONUS;
			}
			float reward = add(distance <= 1 ? "melee_hit_enemy" : "ranged_hit_enemy", base);
			AgentMinHistoryTracker.recordAttack(enemy.pos, damage, reward);
		}
	}

	public static void onEnemyKilled(Mob mob, Object cause) {
		ensureEpisode();
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
		reward = add("kill_enemy", reward);
		AgentMinHistoryTracker.recordKill(mob.pos, boss, reward);
	}

	public static void onPickup(Item item, int pos) {
		ensureEpisode();
		forgetPickupFailure(item, pos);
		if (wasRecentlyDropped(item, pos)) {
			float reward = add("pickup_recent_drop", AgentMinRewardConfig.RECENT_DROP_PICKUP_PENALTY);
			AgentMinHistoryTracker.recordPickup(pos, false, reward);
		} else {
			float reward = add("pickup", AgentMinRewardConfig.PICKUP_REWARD);
			AgentMinHistoryTracker.recordPickup(pos, true, reward);
		}
	}

	public static void onPickupFailed(Item item, int pos) {
		ensureEpisode();
		if (!AgentMinRewardConfig.ENABLED || item == null) {
			return;
		}
		recentPickupFailures.add(new ItemCellRecord(item, pos));
		while (recentPickupFailures.size() > AgentMinRewardConfig.RECENT_PICKUP_FAILURE_MEMORY_SIZE) {
			recentPickupFailures.remove(0);
		}
		float reward = add("pickup_failed", AgentMinRewardConfig.PICKUP_FAILED_PENALTY);
		AgentMinHistoryTracker.recordPickup(pos, false, reward);
	}

	public static boolean isRecentPickupFailure(String className, int image, int pos) {
		ensureEpisode();
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
		ensureEpisode();
		if (!AgentMinRewardConfig.ENABLED || item == null) {
			return;
		}
		recentDrops.add(new DroppedItemRecord(item, pos));
		while (recentDrops.size() > AgentMinRewardConfig.RECENT_DROP_MEMORY_SIZE) {
			recentDrops.remove(0);
		}
		float reward = add("drop_item", AgentMinRewardConfig.DROP_ITEM_PENALTY);
		AgentMinHistoryTracker.recordDrop(pos, reward);
	}

	public static void onTransition(boolean descend) {
		ensureEpisode();
		float reward = add(descend ? "descend" : "ascend",
				descend ? AgentMinRewardConfig.DESCEND_REWARD : AgentMinRewardConfig.ASCEND_PENALTY);
		AgentMinHistoryTracker.recordTransition(descend, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onFallDescend() {
		ensureEpisode();
		float reward = add("fall_descend", AgentMinRewardConfig.FALL_DESCEND_PENALTY);
		AgentMinHistoryTracker.recordTransition(true, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onActionFailed() {
		ensureEpisode();
		float reward = add("action_failed", AgentMinRewardConfig.ACTION_FAILED_PENALTY);
		AgentMinHistoryTracker.recordWait(Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onAdjacentRangedResourceUse(int targetCell) {
		ensureEpisode();
		float reward = add("adjacent_ranged_waste", AgentMinRewardConfig.ADJACENT_RANGED_WASTE_PENALTY);
		AgentMinHistoryTracker.recordAttack(targetCell, 0, reward);
	}

	public static boolean[] snapshotVisited(Level level) {
		ensureEpisode();
		if (!AgentMinRewardConfig.ENABLED || level == null || level.visited == null) {
			return null;
		}
		return level.visited.clone();
	}

	public static void onObserve(Level level, boolean[] beforeVisited) {
		ensureEpisode();
		if (!AgentMinRewardConfig.ENABLED || level == null || beforeVisited == null || level.visited == null) {
			return;
		}
		if (!hasVisitedCell(beforeVisited)) {
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
			float reward = add("explore_new_visited", newlyVisited * AgentMinRewardConfig.NEW_VISITED_CELL_REWARD);
			AgentMinHistoryTracker.recordExplore(newlyVisited, reward);
		}
		AgentMinExplorationTracker.ObserveProgress progress = AgentMinExplorationTracker.onObserve(level);
		if (progress.newDoorCount > 0) {
			add("discover_new_door", progress.newDoorCount * AgentMinRewardConfig.DISCOVER_NEW_DOOR_REWARD);
		}
		if (progress.enteredNewRoom) {
			add("enter_new_room", AgentMinRewardConfig.ENTER_NEW_ROOM_REWARD);
		}
		if (progress.postDoorReveal) {
			add("post_door_reveal", AgentMinRewardConfig.POST_DOOR_REVEAL_REWARD);
		}
	}

	private static void ensureEpisode() {
		if (Dungeon.hero != lastHeroRef) {
			lastHeroRef = Dungeon.hero;
			resetEpisodeState();
		}
		AgentMinExplorationTracker.ensureEpisode();
	}

	private static boolean hasVisitedCell(boolean[] visited) {
		for (boolean cell : visited) {
			if (cell) {
				return true;
			}
		}
		return false;
	}

	private static float rewardForDoorPass(int targetCell) {
		if (!AgentMinRewardConfig.ENABLED || Dungeon.level == null || targetCell < 0 || targetCell >= Dungeon.level.length()) {
			return 0f;
		}
		int terrain = Dungeon.level.map[targetCell];
		if (!isDoorTerrain(terrain)) {
			return 0f;
		}
		float now = Actor.now();
		if (now - lastDoorRewardTurn < AgentMinRewardConfig.DOOR_PASS_COOLDOWN_TURNS) {
			return 0f;
		}
		lastDoorRewardTurn = now;
		return add("pass_door", AgentMinRewardConfig.DOOR_PASS_REWARD);
	}

	private static boolean isDoorTerrain(int terrain) {
		return terrain == Terrain.DOOR
				|| terrain == Terrain.OPEN_DOOR
				|| terrain == Terrain.LOCKED_DOOR
				|| terrain == Terrain.HERO_LKD_DR
				|| terrain == Terrain.CRYSTAL_DOOR;
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
