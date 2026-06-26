package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AgentMinRewardTracker {

	private static float episodeReward;
	private static float pendingReward;
	private static float lastDoorRewardTurn = -9999f;
	private static int doorLoiterAnchor = -1;
	private static int doorLoiterStreak;
	private static Object lastHeroRef;
	private static final ArrayList<RewardEvent> recentEvents = new ArrayList<>();
	private static final ArrayList<DroppedItemRecord> recentDrops = new ArrayList<>();
	private static final ArrayList<ItemCellRecord> recentPickupFailures = new ArrayList<>();
	private static final ArrayList<CellFailureRecord> recentLockedDoorFailures = new ArrayList<>();
	private static final ArrayList<CellFailureRecord> recentLockedChestFailures = new ArrayList<>();
	private static final ArrayList<EquipmentActionRecord> recentEquipmentActions = new ArrayList<>();
	private static final int NO_STAIR_DEPTH = Integer.MIN_VALUE;
	private static int lastStairDepth = NO_STAIR_DEPTH;
	private static int lastStairBranch;
	private static boolean lastStairDescend;
	private static float lastStairTime = -9999f;

	public static void resetEpisode() {
		lastHeroRef = Dungeon.hero;
		resetEpisodeState();
	}

	private static void resetEpisodeState() {
		episodeReward = 0f;
		pendingReward = 0f;
		lastDoorRewardTurn = -9999f;
		doorLoiterAnchor = -1;
		doorLoiterStreak = 0;
		recentEvents.clear();
		recentDrops.clear();
		recentPickupFailures.clear();
		recentLockedDoorFailures.clear();
		recentLockedChestFailures.clear();
		recentEquipmentActions.clear();
		lastStairDepth = NO_STAIR_DEPTH;
		lastStairBranch = 0;
		lastStairDescend = false;
		lastStairTime = -9999f;
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
		if (!progress.passedNewDoor && (progress.immediateBacktrack || progress.shortCycle)
				&& nearestVisibleDoor(targetCell, AgentMinRewardConfig.DOOR_NEAR_DISTANCE) >= 0) {
			reward += add("door_cycle", AgentMinRewardConfig.DOOR_NEAR_CYCLE_PENALTY);
		}
		if (progress.roomStall) {
			reward += add("room_stall", AgentMinRewardConfig.ROOM_STALL_PENALTY);
		}
		if (progress.longStall) {
			reward += add("long_stall", AgentMinRewardConfig.LONG_STALL_PENALTY);
		}
		reward += rewardForDoorLoiter(targetCell, progress.passedNewDoor);
		reward += rewardForCombatPosture(fromCell, targetCell);
		AgentMinHistoryTracker.recordMove(fromCell, targetCell, reward);
	}

	public static void onHeroWait() {
		ensureEpisode();
		float reward = add("wait", AgentMinRewardConfig.WAIT_PENALTY);
		if (safeAdjacentVisibleEnemy()) {
			reward += add("wait_adjacent_enemy", AgentMinRewardConfig.IGNORE_ADJACENT_ENEMY_PENALTY);
		}
		int nearbyDoor = nearestVisibleDoor(Dungeon.hero == null ? -1 : Dungeon.hero.pos, AgentMinRewardConfig.DOOR_NEAR_DISTANCE);
		if (nearbyDoor >= 0) {
			doorLoiterAnchor = nearbyDoor;
			doorLoiterStreak++;
			reward += add("door_wait", AgentMinRewardConfig.DOOR_NEAR_WAIT_PENALTY);
		}
		AgentMinHistoryTracker.recordWait(Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onLockedDoorInteraction(boolean success, int cell) {
		ensureEpisode();
		float value = success ? AgentMinRewardConfig.LOCKED_DOOR_OPEN_REWARD : AgentMinRewardConfig.LOCKED_DOOR_FAIL_PENALTY;
		if (success) {
			forgetCellFailure(recentLockedDoorFailures, cell);
		} else {
			if (!hasKeyForDoor(cell)) {
				value += AgentMinRewardConfig.LOCKED_DOOR_NO_KEY_PENALTY;
			}
			if (isRecentCellFailure(recentLockedDoorFailures, cell, AgentMinRewardConfig.LOCKED_DOOR_FAIL_MEMORY_TURNS)) {
				value += AgentMinRewardConfig.LOCKED_DOOR_REPEAT_FAIL_PENALTY;
			}
			rememberCellFailure(recentLockedDoorFailures, cell, AgentMinRewardConfig.LOCKED_DOOR_FAIL_MEMORY_TURNS);
		}
		float reward = add(success ? "locked_door_open" : "locked_door_fail", value);
		AgentMinHistoryTracker.recordTransition(success, cell, reward);
	}

	public static void onLockedChestInteraction(boolean success, int cell) {
		ensureEpisode();
		float value = success ? AgentMinRewardConfig.LOCKED_CHEST_OPEN_REWARD : AgentMinRewardConfig.LOCKED_CHEST_FAIL_PENALTY;
		if (success) {
			forgetCellFailure(recentLockedChestFailures, cell);
		} else {
			if (!hasKeyForChest(cell)) {
				value += AgentMinRewardConfig.LOCKED_CHEST_NO_KEY_PENALTY;
			}
			if (isRecentCellFailure(recentLockedChestFailures, cell, AgentMinRewardConfig.LOCKED_CHEST_FAIL_MEMORY_TURNS)) {
				value += AgentMinRewardConfig.LOCKED_CHEST_REPEAT_FAIL_PENALTY;
			}
			rememberCellFailure(recentLockedChestFailures, cell, AgentMinRewardConfig.LOCKED_CHEST_FAIL_MEMORY_TURNS);
		}
		float reward = add(success ? "locked_chest_open" : "locked_chest_fail", value);
		AgentMinHistoryTracker.recordPickup(cell, success, reward);
	}

	public static boolean isRecentLockedDoorFailure(int cell) {
		ensureEpisode();
		return isRecentCellFailure(recentLockedDoorFailures, cell, AgentMinRewardConfig.LOCKED_DOOR_FAIL_MEMORY_TURNS);
	}

	public static boolean isRecentLockedChestFailure(int cell) {
		ensureEpisode();
		return isRecentCellFailure(recentLockedChestFailures, cell, AgentMinRewardConfig.LOCKED_CHEST_FAIL_MEMORY_TURNS);
	}

	public static void onTalentResourceUsed(Item item) {
		ensureEpisode();
		float reward = add("talent_resource_used", AgentMinRewardConfig.TALENT_RESOURCE_USE_REWARD);
		AgentMinHistoryTracker.recordWait(Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onTalentUpgradeDecision(boolean success) {
		ensureEpisode();
		float reward = add(success ? "talent_upgrade_success" : "talent_upgrade_fail",
				success ? AgentMinRewardConfig.TALENT_UPGRADE_SUCCESS_REWARD : AgentMinRewardConfig.TALENT_UPGRADE_FAIL_PENALTY);
		AgentMinHistoryTracker.recordTransition(success, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onMonitorItemSelection(boolean success) {
		ensureEpisode();
		float reward = add(success ? "monitor_item_valid" : "monitor_item_invalid",
				success ? AgentMinRewardConfig.MONITOR_ITEM_VALID_SELECTION_REWARD : AgentMinRewardConfig.MONITOR_ITEM_INVALID_SELECTION_PENALTY);
		AgentMinHistoryTracker.recordTransition(success, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onMonitorCellSelection(boolean success, int targetCell) {
		ensureEpisode();
		float reward = add(success ? "monitor_cell_valid" : "monitor_cell_invalid",
				success ? AgentMinRewardConfig.MONITOR_CELL_VALID_SELECTION_REWARD : AgentMinRewardConfig.MONITOR_CELL_INVALID_SELECTION_PENALTY);
		AgentMinHistoryTracker.recordTransition(success, targetCell, reward);
	}

	public static void onMonitorOptionSelection(boolean success) {
		ensureEpisode();
		float reward = add(success ? "monitor_option_valid" : "monitor_option_invalid",
				success ? AgentMinRewardConfig.MONITOR_OPTION_VALID_SELECTION_REWARD : AgentMinRewardConfig.MONITOR_OPTION_INVALID_SELECTION_PENALTY);
		AgentMinHistoryTracker.recordTransition(success, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static void onItemUiBlocked() {
		ensureEpisode();
		float reward = add("item_ui_blocked", AgentMinRewardConfig.ITEM_UI_BLOCKED_PENALTY);
		AgentMinHistoryTracker.recordTransition(false, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	public static EquipmentActionSnapshot equipmentActionSnapshot(Item focus) {
		ensureEpisode();
		return new EquipmentActionSnapshot(focus);
	}

	public static boolean isEquipmentActionCoolingDown(AgentMinState.ItemState item) {
		return equipmentActionCooldownRemaining(item) > 0f;
	}

	public static float equipmentActionCooldownRemaining(AgentMinState.ItemState item) {
		ensureEpisode();
		if (item == null) {
			return 0f;
		}
		pruneEquipmentActions();
		float now = Actor.now();
		float remaining = 0f;
		for (EquipmentActionRecord record : recentEquipmentActions) {
			if (record.matches(item)) {
				remaining = Math.max(remaining, record.until - now);
			}
		}
		return Math.max(0f, remaining);
	}

	public static void onEquipmentAction(String action, EquipmentActionSnapshot before, Item focus) {
		ensureEpisode();
		if (!AgentMinRewardConfig.ENABLED || action == null || before == null
				|| (!"EQUIP".equals(action) && !"UNEQUIP".equals(action))) {
			return;
		}
		boolean repeated = isEquipmentActionCoolingDown(focus);
		EquipmentActionSnapshot after = new EquipmentActionSnapshot(focus);
		float reward = 0f;
		String reason = null;
		if ("UNEQUIP".equals(action)) {
			if (before.focusEquipped && before.focus != null && before.focus.cursed) {
				reward += cursedEquippedActionPenalty(before.focus);
				reason = "unequip_cursed_equipment";
			}
			if (before.focusEquipped && !after.focusEquipped && before.focus != null) {
				reward += AgentMinRewardConfig.UNEQUIP_BASE_PENALTY
						+ before.focus.score * AgentMinRewardConfig.UNEQUIP_VALUE_SCALE
						+ before.focus.strengthDeficit * AgentMinRewardConfig.UNEQUIP_OVERSTR_RELIEF_PER_POINT;
				if (reason == null) {
					reason = "unequip_equipment";
				}
			} else if (before.focusEquipped) {
				reward += AgentMinRewardConfig.EQUIP_ACTION_NO_CHANGE_PENALTY;
				if (reason == null) {
					reason = "unequip_no_change";
				}
			}
		} else if ("EQUIP".equals(action)) {
			if (after.focusEquipped && after.focus != null && (before.focus == null || !before.focusEquipped)) {
				EquipmentItemSnapshot oldItem = before.slot(after.focusSlot);
				if (oldItem != null && oldItem.sameItem(after.focus)) {
					oldItem = null;
				}
				float oldScore = oldItem == null ? 0f : oldItem.score;
				float valueDiff = after.focus.score - oldScore;
				reward = (oldItem == null ? AgentMinRewardConfig.EQUIP_NEW_BASE_REWARD : AgentMinRewardConfig.EQUIP_REPLACE_BASE_REWARD)
						+ valueDiff * AgentMinRewardConfig.EQUIP_VALUE_DIFF_SCALE
						+ equipmentStrengthReward(after.focus, oldItem);
				if (oldItem != null && valueDiff < AgentMinRewardConfig.EQUIP_MIN_SCORE_IMPROVEMENT) {
					reward += AgentMinRewardConfig.EQUIP_LOW_VALUE_CHANGE_PENALTY;
					if (valueDiff <= 0f) {
						reward += AgentMinRewardConfig.EQUIP_SIDEGRADE_PENALTY;
					}
				}
				if (oldItem != null && oldItem.cursed) {
					reward += cursedEquippedActionPenalty(oldItem);
				}
				if (after.focus.cursed) {
					reward += AgentMinRewardConfig.EQUIP_CURSED_PENALTY
							+ after.focus.score * AgentMinRewardConfig.EQUIP_CURSED_VALUE_SCALE
							+ after.focus.strengthDeficit * AgentMinRewardConfig.EQUIP_STRENGTH_DEFICIT_PENALTY_PER_POINT;
					reason = oldItem == null ? "equip_cursed_new" : "equip_cursed_replace";
				} else {
					reason = oldItem != null && oldItem.cursed ? "equip_replace_cursed_old"
							: oldItem == null ? "equip_new" : "equip_replace";
				}
			} else if (before.focus == null || !before.focusEquipped) {
				EquipmentItemSnapshot cursedBlocker = cursedReplacementBlocker(before, focus);
				if (cursedBlocker != null) {
					reward = AgentMinRewardConfig.EQUIP_ACTION_NO_CHANGE_PENALTY
							+ cursedEquippedActionPenalty(cursedBlocker);
					reason = "equip_replace_cursed_blocked";
				} else {
					reward = AgentMinRewardConfig.EQUIP_ACTION_NO_CHANGE_PENALTY;
					reason = "equip_no_change";
				}
			}
		}
		if (repeated) {
			reward += AgentMinRewardConfig.EQUIP_REPEAT_COOLDOWN_PENALTY;
			if (reason == null) {
				reason = "equipment_repeat_cooldown";
			} else {
				reason += "_cooldown";
			}
		}
		if (reason != null && reward != 0f) {
			float added = add(reason, reward);
			AgentMinHistoryTracker.recordTransition(reward > 0f, Dungeon.hero == null ? -1 : Dungeon.hero.pos, added);
		}
		rememberEquipmentAction(focus, action);
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
		writeEpisodeEndLog(false, Statistics.totalScore);
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

	public static void writeEpisodeEndLog(boolean win, int rankingScore) {
		if (!AgentMinBridgeConfig.ENABLED) {
			return;
		}
		ensureEpisode();
		String path = System.getenv("AGENTMIN_EPISODE_LOG");
		if (path == null || path.length() == 0) {
			path = System.getProperty("agentmin.episode_log", "");
		}
		if (path == null || path.length() == 0) {
			path = siblingLogPathFromDeathFlag("agent_min_episode_results.log");
		}
		if (path == null || path.length() == 0) {
			return;
		}
		try {
			File file = new File(path);
			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
			int maxDepth = Math.max(Statistics.deepestFloor, Dungeon.depth);
			FileWriter writer = new FileWriter(file, true);
			writer.write(format.format(new Date()) + "=episodeReward=" + episodeReward
					+ ",maxDepth=" + maxDepth
					+ ",rankingScore=" + rankingScore
					+ ",won=" + win + "\n");
			writer.close();
			AgentMinRuntimeLog.log("episode end logged: " + path);
		} catch (IOException e) {
			AgentMinRuntimeLog.log("failed to write episode end log: " + e.getMessage());
		}
	}

	private static String siblingLogPathFromDeathFlag(String fileName) {
		String deathFlag = System.getenv("AGENTMIN_DEATH_FLAG");
		if (deathFlag == null || deathFlag.length() == 0) {
			deathFlag = System.getProperty("agentmin.death_flag", "");
		}
		if (deathFlag == null || deathFlag.length() == 0) {
			return "";
		}
		File parent = new File(deathFlag).getParentFile();
		return parent == null ? "" : new File(parent, fileName).getPath();
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
		int depth = Dungeon.depth;
		int branch = Dungeon.branch;
		boolean reversal = isStairReversal(descend, depth, branch);
		boolean repeat = isStairRepeat(descend, depth, branch);
		float value = descend ? AgentMinRewardConfig.DESCEND_REWARD : AgentMinRewardConfig.ASCEND_PENALTY;
		String reason = descend ? "descend" : "ascend";
		if (descend) {
			float explorationPenalty = descendExplorationPenalty();
			if (explorationPenalty < 0f) {
				value += explorationPenalty;
				reason += "_partial";
			}
		}
		if (reversal) {
			value += AgentMinRewardConfig.STAIR_REVERSAL_PENALTY;
			reason += "_reversal";
		} else if (repeat) {
			value += AgentMinRewardConfig.STAIR_REPEAT_PENALTY;
			reason += "_repeat";
		}
		float reward = add(reason, value);
		rememberStair(descend, depth, branch);
		AgentMinHistoryTracker.recordTransition(descend, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
	}

	private static float descendExplorationPenalty() {
		Level level = Dungeon.level;
		if (level == null || level.map == null || level.visited == null) {
			return 0f;
		}
		int explorable = 0;
		int visited = 0;
		int length = Math.min(level.length(), Math.min(level.map.length, level.visited.length));
		for (int i = 0; i < length; i++) {
			if (!isDescendExplorable(level, i)) {
				continue;
			}
			explorable++;
			if (level.visited[i] || (level.heroFOV != null && i < level.heroFOV.length && level.heroFOV[i])) {
				visited++;
			}
		}
		float penalty = 0f;
		if (explorable > 0) {
			float unexploredRatio = 1f - (visited / (float) explorable);
			float excess = unexploredRatio - AgentMinRewardConfig.DESCEND_UNEXPLORED_GRACE_RATIO;
			if (excess > 0f) {
				penalty += excess * AgentMinRewardConfig.DESCEND_UNEXPLORED_RATIO_PENALTY;
			}
		}
		int knownUnpickedItems = 0;
		if (level.heaps != null) {
			for (Heap heap : level.heaps.valueList()) {
				if (heap == null || heap.pos < 0 || heap.pos >= length) {
					continue;
				}
				boolean known = level.visited[heap.pos] || (level.heroFOV != null && heap.pos < level.heroFOV.length && level.heroFOV[heap.pos]);
				if (known) {
					knownUnpickedItems++;
				}
			}
		}
		if (knownUnpickedItems > 0) {
			penalty += knownUnpickedItems * AgentMinRewardConfig.DESCEND_UNPICKED_KNOWN_ITEM_PENALTY;
		}
		return Math.max(AgentMinRewardConfig.DESCEND_EXPLORATION_MAX_PENALTY, penalty);
	}

	private static boolean isDescendExplorable(Level level, int cell) {
		if (cell < 0 || level == null || level.map == null || cell >= level.map.length) {
			return false;
		}
		if (level.passable != null && cell < level.passable.length && level.passable[cell]) {
			return true;
		}
		int terrain = level.map[cell];
		return terrain == Terrain.DOOR
				|| terrain == Terrain.OPEN_DOOR
				|| terrain == Terrain.ENTRANCE
				|| terrain == Terrain.EXIT
				|| terrain == Terrain.UNLOCKED_EXIT;
	}

	public static void onBlockedFloorOneAscend() {
		ensureEpisode();
		int depth = Dungeon.depth;
		int branch = Dungeon.branch;
		float value = AgentMinRewardConfig.FLOOR_ONE_ASCEND_BLOCKED_PENALTY;
		String reason = "floor_one_ascend_blocked";
		if (isStairRepeat(false, depth, branch)) {
			value += AgentMinRewardConfig.STAIR_REPEAT_PENALTY;
			reason += "_repeat";
		}
		float reward = add(reason, value);
		rememberStair(false, depth, branch);
		AgentMinHistoryTracker.recordTransition(false, Dungeon.hero == null ? -1 : Dungeon.hero.pos, reward);
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

	public static void onThrownResourceResolved(Item item, int targetCell, float rewardBefore) {
		ensureEpisode();
		if (!AgentMinRewardConfig.ENABLED || item == null) {
			return;
		}
		float rewardDelta = pendingReward - rewardBefore;
		if (rewardDelta > AgentMinRewardConfig.THROWN_EFFECT_REWARD_EPSILON) {
			return;
		}
		boolean equipment = isEquipmentLikeThrownItem(item);
		float value = AgentMinRewardConfig.THROWN_NO_EFFECT_PENALTY;
		if (equipment) {
			value += AgentMinRewardConfig.THROWN_EQUIPMENT_NO_EFFECT_EXTRA_PENALTY;
		}
		float reward = add(equipment ? "throw_equipment_no_effect" : "throw_no_effect", value);
		AgentMinHistoryTracker.recordAttack(targetCell, 0, reward);
	}

	private static boolean isEquipmentLikeThrownItem(Item item) {
		if (item == null) {
			return false;
		}
		if (item instanceof Armor || item instanceof Ring || item instanceof Artifact) {
			return true;
		}
		return item instanceof Weapon && !(item instanceof MissileWeapon);
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

	private static boolean isStairReversal(boolean descend, int depth, int branch) {
		if (lastStairDepth == NO_STAIR_DEPTH || lastStairBranch != branch
				|| lastStairDescend == descend
				|| Actor.now() - lastStairTime > AgentMinRewardConfig.STAIR_LOOP_MEMORY_TURNS) {
			return false;
		}
		if (lastStairDescend) {
			return depth == lastStairDepth + 1;
		}
		return depth == lastStairDepth - 1;
	}

	private static boolean isStairRepeat(boolean descend, int depth, int branch) {
		return lastStairDepth != NO_STAIR_DEPTH
				&& lastStairBranch == branch
				&& lastStairDescend == descend
				&& lastStairDepth == depth
				&& Actor.now() - lastStairTime <= AgentMinRewardConfig.STAIR_LOOP_MEMORY_TURNS;
	}

	private static void rememberStair(boolean descend, int depth, int branch) {
		lastStairDescend = descend;
		lastStairDepth = depth;
		lastStairBranch = branch;
		lastStairTime = Actor.now();
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
		resetDoorLoiter();
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

	private static float rewardForDoorLoiter(int targetCell, boolean passedNewDoor) {
		if (!AgentMinRewardConfig.ENABLED || Dungeon.level == null || targetCell < 0 || targetCell >= Dungeon.level.length()) {
			resetDoorLoiter();
			return 0f;
		}
		if (passedNewDoor || isDoorTerrain(Dungeon.level.map[targetCell])) {
			resetDoorLoiter();
			return 0f;
		}
		int nearestDoor = nearestVisibleDoor(targetCell, AgentMinRewardConfig.DOOR_NEAR_DISTANCE);
		if (nearestDoor < 0) {
			resetDoorLoiter();
			return 0f;
		}
		if (nearestDoor == doorLoiterAnchor) {
			doorLoiterStreak++;
		} else {
			doorLoiterAnchor = nearestDoor;
			doorLoiterStreak = 1;
		}
		if (doorLoiterStreak >= AgentMinRewardConfig.DOOR_LOITER_STREAK_THRESHOLD
				&& doorLoiterStreak % AgentMinRewardConfig.DOOR_LOITER_REPEAT_INTERVAL == 0) {
			return add("door_loiter", AgentMinRewardConfig.DOOR_NEAR_LOITER_PENALTY);
		}
		return 0f;
	}

	private static void resetDoorLoiter() {
		doorLoiterAnchor = -1;
		doorLoiterStreak = 0;
	}

	private static int nearestVisibleDoor(int fromCell, int maxDistance) {
		if (Dungeon.level == null || fromCell < 0 || fromCell >= Dungeon.level.length()) {
			return -1;
		}
		int best = -1;
		int bestDistance = Integer.MAX_VALUE;
		int width = Dungeon.level.width();
		int height = Dungeon.level.height();
		int fromX = fromCell % width;
		int fromY = fromCell / width;
		for (int y = Math.max(1, fromY - maxDistance); y <= Math.min(height - 2, fromY + maxDistance); y++) {
			for (int x = Math.max(1, fromX - maxDistance); x <= Math.min(width - 2, fromX + maxDistance); x++) {
				int cell = y * width + x;
				if (!isDoorTerrain(Dungeon.level.map[cell])) {
					continue;
				}
				if (Dungeon.level.heroFOV != null && !Dungeon.level.heroFOV[cell]) {
					continue;
				}
				int distance = Dungeon.level.distance(fromCell, cell);
				if (distance <= maxDistance && distance < bestDistance) {
					best = cell;
					bestDistance = distance;
				}
			}
		}
		return best;
	}

	private static float rewardForCombatPosture(int fromCell, int targetCell) {
		if (!AgentMinRewardConfig.ENABLED || Dungeon.level == null || Dungeon.hero == null
				|| fromCell < 0 || targetCell < 0) {
			return 0f;
		}
		Mob enemy = nearestVisibleEnemy(fromCell);
		if (enemy == null || !safeToFight(enemy)) {
			return 0f;
		}
		int fromDistance = Dungeon.level.distance(fromCell, enemy.pos);
		int targetDistance = Dungeon.level.distance(targetCell, enemy.pos);
		if (fromDistance <= 1 && targetDistance > fromDistance) {
			return add("ignore_adjacent_enemy", AgentMinRewardConfig.IGNORE_ADJACENT_ENEMY_PENALTY);
		}
		if (targetDistance < fromDistance) {
			return add("approach_enemy", AgentMinRewardConfig.APPROACH_VISIBLE_ENEMY_REWARD);
		}
		if (targetDistance > fromDistance && fromDistance <= 4) {
			return add("avoid_safe_combat", AgentMinRewardConfig.SAFE_COMBAT_AVOIDANCE_PENALTY);
		}
		return 0f;
	}

	private static boolean safeAdjacentVisibleEnemy() {
		if (!AgentMinRewardConfig.ENABLED || Dungeon.level == null || Dungeon.hero == null) {
			return false;
		}
		Mob enemy = nearestVisibleEnemy(Dungeon.hero.pos);
		return enemy != null && safeToFight(enemy) && Dungeon.level.distance(Dungeon.hero.pos, enemy.pos) <= 1;
	}

	private static Mob nearestVisibleEnemy(int fromCell) {
		if (Dungeon.level == null || Dungeon.level.heroFOV == null || fromCell < 0) {
			return null;
		}
		Mob best = null;
		int bestDistance = Integer.MAX_VALUE;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob == null || mob.HP <= 0 || mob.alignment != Char.Alignment.ENEMY
					|| mob.pos < 0 || mob.pos >= Dungeon.level.length()
					|| !Dungeon.level.heroFOV[mob.pos]) {
				continue;
			}
			int distance = Dungeon.level.distance(fromCell, mob.pos);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = mob;
			}
		}
		return best;
	}

	private static boolean safeToFight(Mob enemy) {
		if (Dungeon.hero == null || enemy == null) {
			return false;
		}
		return Dungeon.hero.HP * 2 >= Dungeon.hero.HT || enemy.HP <= Math.max(2, Dungeon.hero.HT / 3);
	}

	private static boolean isEquipmentActionCoolingDown(Item item) {
		if (item == null) {
			return false;
		}
		pruneEquipmentActions();
		for (EquipmentActionRecord record : recentEquipmentActions) {
			if (record.matches(item)) {
				return true;
			}
		}
		return false;
	}

	private static void rememberEquipmentAction(Item item, String action) {
		if (item == null || action == null) {
			return;
		}
		String slot = equipmentSlotKey(item);
		if (slot == null || slot.length() == 0) {
			return;
		}
		pruneEquipmentActions();
		recentEquipmentActions.add(new EquipmentActionRecord(slot, item, action));
		while (recentEquipmentActions.size() > 16) {
			recentEquipmentActions.remove(0);
		}
	}

	private static void pruneEquipmentActions() {
		float now = Actor.now();
		for (int i = recentEquipmentActions.size() - 1; i >= 0; i--) {
			if (recentEquipmentActions.get(i).until <= now) {
				recentEquipmentActions.remove(i);
			}
		}
	}

	private static String equipmentSlotKey(Item item) {
		if (item instanceof Armor) {
			return "armor";
		}
		if (item instanceof KindOfWeapon) {
			return "weapon";
		}
		if (item instanceof Ring) {
			return "ring";
		}
		if (item instanceof Artifact) {
			return "artifact";
		}
		return item != null && Dungeon.hero != null && item.isEquipped(Dungeon.hero) ? "misc" : "";
	}

	private static String equipmentSlotKey(AgentMinState.ItemState item) {
		if (item == null || item.equipmentKind == null) {
			return "";
		}
		if (item.equipmentKind.equals("armor")) return "armor";
		if (item.equipmentKind.equals("weapon")) return "weapon";
		if (item.equipmentKind.equals("ring")) return "ring";
		if (item.equipmentKind.equals("artifact")) return "artifact";
		return item.equipmentKind.length() == 0 ? "" : "misc";
	}

	private static float equipmentStrengthReward(EquipmentItemSnapshot item, EquipmentItemSnapshot oldItem) {
		if (item == null || item.strReq <= 0) {
			return 0f;
		}
		float reward;
		if (item.strengthDeficit <= 0) {
			reward = AgentMinRewardConfig.EQUIP_STRENGTH_FIT_REWARD
					+ Math.min(6, item.strengthMargin) * AgentMinRewardConfig.EQUIP_STRENGTH_MARGIN_REWARD_PER_POINT;
		} else {
			reward = item.strengthDeficit * AgentMinRewardConfig.EQUIP_STRENGTH_DEFICIT_PENALTY_PER_POINT;
		}
		int oldDeficit = oldItem == null ? 0 : oldItem.strengthDeficit;
		reward += (oldDeficit - item.strengthDeficit) * AgentMinRewardConfig.EQUIP_STRENGTH_DIFF_SCALE;
		return reward;
	}

	private static float cursedEquippedActionPenalty(EquipmentItemSnapshot item) {
		if (item == null || !item.cursed) {
			return 0f;
		}
		float reward = AgentMinRewardConfig.CURSED_EQUIPPED_ACTION_PENALTY
				+ item.score * AgentMinRewardConfig.CURSED_EQUIPPED_VALUE_SCALE;
		if (item.strengthDeficit > 0) {
			reward += item.strengthDeficit * AgentMinRewardConfig.CURSED_EQUIPPED_OVERSTR_RELIEF_PER_POINT;
		} else {
			reward += Math.min(6, Math.max(0, item.strengthMargin))
					* AgentMinRewardConfig.CURSED_EQUIPPED_STRENGTH_MARGIN_SCALE;
		}
		return Math.min(-0.05f, reward);
	}

	private static EquipmentItemSnapshot cursedReplacementBlocker(EquipmentActionSnapshot before, Item focus) {
		if (before == null || focus == null) {
			return null;
		}
		if (focus instanceof Armor) {
			return cursedSlot(before, "armor");
		}
		if (focus instanceof KindOfWeapon) {
			return cursedSlot(before, "weapon");
		}
		if (focus instanceof Ring) {
			EquipmentItemSnapshot slot = cursedSlot(before, "ring");
			if (slot != null) return slot;
			slot = cursedSlot(before, "misc");
			if (slot != null) return slot;
			return cursedSlot(before, "artifact");
		}
		if (focus instanceof Artifact) {
			EquipmentItemSnapshot slot = cursedSlot(before, "artifact");
			if (slot != null) return slot;
			return cursedSlot(before, "misc");
		}
		return null;
	}

	private static EquipmentItemSnapshot cursedSlot(EquipmentActionSnapshot before, String slot) {
		EquipmentItemSnapshot item = before.slot(slot);
		return item != null && item.cursed ? item : null;
	}

	private static String slotOf(Item item) {
		Hero hero = Dungeon.hero;
		if (hero == null || item == null) {
			return null;
		}
		if (hero.belongings.weapon() == item) return "weapon";
		if (hero.belongings.armor() == item) return "armor";
		if (hero.belongings.artifact() == item) return "artifact";
		if (hero.belongings.misc() == item) return "misc";
		if (hero.belongings.ring() == item) return "ring";
		if (hero.belongings.secondWep() == item) return "second_weapon";
		return null;
	}

	private static EquipmentItemSnapshot slotSnapshot(String slot, Item item) {
		return item == null ? null : new EquipmentItemSnapshot(slot, item);
	}

	private static int strengthRequirement(Item item) {
		if (item instanceof Weapon) {
			return ((Weapon)item).STRReq();
		}
		if (item instanceof Armor) {
			return ((Armor)item).STRReq();
		}
		return 0;
	}

	private static float equipmentScore(Item item) {
		if (item instanceof KindOfWeapon) {
			KindOfWeapon weapon = (KindOfWeapon)item;
			return (weapon.min() + weapon.max()) * 0.5f + Math.max(0, weapon.buffedLvl()) * 1.5f;
		}
		if (item instanceof Armor) {
			Armor armor = (Armor)item;
			return (armor.DRMin() + armor.DRMax()) * 0.65f + armor.tier + Math.max(0, armor.buffedLvl()) * 1.5f;
		}
		if (item instanceof Ring || item instanceof Artifact) {
			return 4f + Math.max(0, item.buffedLvl()) * 2f;
		}
		return item != null && Dungeon.hero != null && item.isEquipped(Dungeon.hero) ? 2f + Math.max(0, item.buffedLvl()) : 0f;
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

	private static boolean hasKeyForDoor(int cell) {
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()) {
			return false;
		}
		int terrain = Dungeon.level.map[cell];
		if (terrain == Terrain.LOCKED_DOOR) {
			return Notes.keyCount(new IronKey(Dungeon.depth)) > 0;
		}
		if (terrain == Terrain.CRYSTAL_DOOR) {
			return Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0;
		}
		if (terrain == Terrain.LOCKED_EXIT) {
			return Notes.keyCount(new WornKey(Dungeon.depth)) > 0;
		}
		return terrain == Terrain.HERO_LKD_DR;
	}

	private static boolean hasKeyForChest(int cell) {
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()) {
			return false;
		}
		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap == null) {
			return false;
		}
		if (heap.type == Heap.Type.LOCKED_CHEST) {
			return Notes.keyCount(new GoldenKey(Dungeon.depth)) > 0;
		}
		if (heap.type == Heap.Type.CRYSTAL_CHEST) {
			return Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0;
		}
		return true;
	}

	private static void rememberCellFailure(ArrayList<CellFailureRecord> records, int cell, int maxAge) {
		forgetCellFailure(records, cell);
		records.add(new CellFailureRecord(cell));
		pruneCellFailures(records, maxAge);
	}

	private static void forgetCellFailure(ArrayList<CellFailureRecord> records, int cell) {
		for (int i = records.size() - 1; i >= 0; i--) {
			if (records.get(i).matches(cell)) {
				records.remove(i);
			}
		}
	}

	private static boolean isRecentCellFailure(ArrayList<CellFailureRecord> records, int cell, int maxAge) {
		pruneCellFailures(records, maxAge);
		for (CellFailureRecord record : records) {
			if (record.matches(cell)) {
				return true;
			}
		}
		return false;
	}

	private static void pruneCellFailures(ArrayList<CellFailureRecord> records, int maxAge) {
		float now = Actor.now();
		for (int i = records.size() - 1; i >= 0; i--) {
			if (!records.get(i).active(now, maxAge)) {
				records.remove(i);
			}
		}
	}

	public static class EquipmentActionSnapshot {
		public final String focusSlot;
		public final boolean focusEquipped;
		public final EquipmentItemSnapshot focus;
		private final EquipmentItemSnapshot weapon;
		private final EquipmentItemSnapshot armor;
		private final EquipmentItemSnapshot artifact;
		private final EquipmentItemSnapshot misc;
		private final EquipmentItemSnapshot ring;
		private final EquipmentItemSnapshot secondWeapon;

		private EquipmentActionSnapshot(Item focusItem) {
			Hero hero = Dungeon.hero;
			focusSlot = slotOf(focusItem);
			focusEquipped = focusSlot != null;
			focus = focusItem == null ? null : new EquipmentItemSnapshot(focusSlot, focusItem);
			weapon = hero == null ? null : slotSnapshot("weapon", hero.belongings.weapon());
			armor = hero == null ? null : slotSnapshot("armor", hero.belongings.armor());
			artifact = hero == null ? null : slotSnapshot("artifact", hero.belongings.artifact());
			misc = hero == null ? null : slotSnapshot("misc", hero.belongings.misc());
			ring = hero == null ? null : slotSnapshot("ring", hero.belongings.ring());
			secondWeapon = hero == null ? null : slotSnapshot("second_weapon", hero.belongings.secondWep());
		}

		private EquipmentItemSnapshot slot(String slot) {
			if (slot == null) return null;
			if (slot.equals("weapon")) return weapon;
			if (slot.equals("armor")) return armor;
			if (slot.equals("artifact")) return artifact;
			if (slot.equals("misc")) return misc;
			if (slot.equals("ring")) return ring;
			if (slot.equals("second_weapon")) return secondWeapon;
			return null;
		}
	}

	public static class EquipmentItemSnapshot {
		public final String slot;
		public final String className;
		public final int image;
		public final int level;
		public final int buffedLevel;
		public final int strReq;
		public final int strengthMargin;
		public final int strengthDeficit;
		public final float score;
		public final boolean cursed;
		public final boolean cursedKnown;

		private EquipmentItemSnapshot(String slot, Item item) {
			this.slot = slot;
			className = item.getClass().getName();
			image = item.image();
			level = item.level();
			buffedLevel = item.buffedLvl();
			strReq = strengthRequirement(item);
			int heroStrength = Dungeon.hero == null ? 0 : Dungeon.hero.STR();
			strengthMargin = strReq <= 0 ? 0 : heroStrength - strReq;
			strengthDeficit = Math.max(0, -strengthMargin);
			score = equipmentScore(item);
			cursed = item.cursed;
			cursedKnown = item.cursedKnown;
		}

		private boolean sameItem(EquipmentItemSnapshot other) {
			return other != null
					&& className.equals(other.className)
					&& image == other.image
					&& level == other.level
					&& buffedLevel == other.buffedLevel;
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

	private static class EquipmentActionRecord {
		private final String slot;
		private final String className;
		private final int image;
		private final String action;
		private final float until;

		private EquipmentActionRecord(String slot, Item item, String action) {
			this.slot = slot;
			this.className = item.getClass().getName();
			this.image = item.image();
			this.action = action;
			this.until = Actor.now() + AgentMinRewardConfig.EQUIP_ACTION_COOLDOWN_TURNS;
		}

		private boolean matches(Item item) {
			if (item == null) {
				return false;
			}
			String currentSlot = equipmentSlotKey(item);
			return slot.equals(currentSlot) || (className.equals(item.getClass().getName()) && image == item.image());
		}

		private boolean matches(AgentMinState.ItemState item) {
			if (item == null) {
				return false;
			}
			String currentSlot = equipmentSlotKey(item);
			return slot.equals(currentSlot)
					|| (item.className != null && className.equals(item.className) && image == item.image);
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

	private static class CellFailureRecord {
		private final int depth;
		private final int branch;
		private final int pos;
		private final float turn;

		private CellFailureRecord(int pos) {
			this.depth = Dungeon.depth;
			this.branch = Dungeon.branch;
			this.pos = pos;
			this.turn = Actor.now();
		}

		private boolean matches(int pos) {
			return depth == Dungeon.depth && branch == Dungeon.branch && this.pos == pos;
		}

		private boolean active(float now, int maxAge) {
			return depth == Dungeon.depth && branch == Dungeon.branch && now - turn <= maxAge;
		}
	}
}
