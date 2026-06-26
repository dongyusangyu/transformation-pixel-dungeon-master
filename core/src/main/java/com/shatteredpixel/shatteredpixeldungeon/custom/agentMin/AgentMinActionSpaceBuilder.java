package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class AgentMinActionSpaceBuilder {

	private static final int[][] DIRECTIONS = new int[][]{
			{-1, -1}, {0, -1}, {1, -1},
			{-1, 0},           {1, 0},
			{-1, 1},  {0, 1},  {1, 1}
	};

	private static final String[] DIRECTION_NAMES = new String[]{
			"move_nw", "move_n", "move_ne",
			"move_w",            "move_e",
			"move_sw", "move_s", "move_se"
	};
	private static boolean loggedMetamorphHeadReady;

	public static AgentMinActionSpace build(AgentMinState state) {
		AgentMinActionSpace space = new AgentMinActionSpace();
		if (state == null || state.level.width <= 0 || state.level.height <= 0) {
			return space;
		}

		if (addMonitorActions(space, state)) {
			encode(space, state);
			return space;
		}

		addZeroRandomMove(space, state);
		addMovement(space, state);
		addWait(space, state);
		addAttacks(space, state);
		addActionIndicators(space, state);
		addPickUp(space, state);
		addUnlocks(space, state);
		addStairs(space, state);
		addHealing(space, state);
		addFood(space, state);
		addIndexedItemActions(space, state);
		addAlchemy(space, state);
		addThrowing(space, state);
		addTargetedItemThrows(space, state);
		addWands(space, state);
		encode(space, state);
		return space;
	}

	public static AgentMinActionSpace buildTalentUpgrade(AgentMinState state) {
		AgentMinActionSpace space = new AgentMinActionSpace();
		space.forcedSkill = AgentMinAction.Skill.TALENT.ordinal();
		if (state == null || state.level.width <= 0 || state.level.height <= 0) {
			return space;
		}
		addTalentUpgrades(space, state);
		encode(space, state);
		return space;
	}

	private static boolean addMonitorActions(AgentMinActionSpace space, AgentMinState state) {
		fillMonitorCellMask(space, state);
		boolean added = false;
		if (GameScene.agentMinCellSelectorActive()) {
			space.forcedSkill = AgentMinAction.Skill.ITEM.ordinal();
			space.monitorType = "cell";
			added |= addMonitorCellActions(space, state);
		}
		if (!added && GameScene.agentMinItemSelectorActive()) {
			space.forcedSkill = AgentMinAction.Skill.ITEM.ordinal();
			space.monitorType = "item";
			added |= addMonitorItemActions(space);
		}
		if (!added && GameScene.agentMinOptionsActive()) {
			space.forcedSkill = AgentMinAction.Skill.ITEM.ordinal();
			space.monitorType = "option";
			added |= addMonitorOptionActions(space, state);
		}
		return added;
	}

	private static boolean addMonitorCellActions(AgentMinActionSpace space, AgentMinState state) {
		if (Dungeon.level == null || Dungeon.level.heroFOV == null) {
			return false;
		}
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.MONITOR_CELL, "monitor_cell_select_position");
		action.fromCell = state.level.heroPos;
		action.targetCell = preferredMonitorCell(state);
		action.priority = 0.88f;
		add(space, action);
		return hasMonitorCellMask(space);
	}

	private static void addMonitorCell(AgentMinActionSpace space, AgentMinState state, ArrayList<Integer> cells,
									   int cell, float priority, int mobRow) {
		if (cell < 0 || cells.contains(cell) || space.actions.size() >= AgentMinActionSpace.MAX_ACTIONS) {
			return;
		}
		cells.add(cell);
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.MONITOR_CELL, "monitor_cell");
		action.fromCell = state.level.heroPos;
		action.targetCell = cell;
		action.targetMobRow = mobRow;
		action.priority = clamp(priority);
		add(space, action);
	}

	private static void fillMonitorCellMask(AgentMinActionSpace space, AgentMinState state) {
		if (space == null || state == null || Dungeon.level == null || Dungeon.level.heroFOV == null) {
			return;
		}
		int size = AgentMinEncodedState.LOCAL_MAP_SIZE;
		int radius = size / 2;
		int hero = state.level.heroPos;
		if (hero < 0 || state.level.width <= 0 || state.level.height <= 0) {
			return;
		}
		int heroX = hero % state.level.width;
		int heroY = hero / state.level.width;
		for (int ly = 0; ly < size; ly++) {
			for (int lx = 0; lx < size; lx++) {
				int x = heroX + lx - radius;
				int y = heroY + ly - radius;
				int index = ly * size + lx;
				if (x < 0 || y < 0 || x >= state.level.width || y >= state.level.height) {
					continue;
				}
				int cell = y * state.level.width + x;
				if (cell < 0 || cell >= Dungeon.level.length()) {
					continue;
				}
				if (cell != hero && (cell >= Dungeon.level.heroFOV.length || !Dungeon.level.heroFOV[cell])) {
					continue;
				}
				if (cell != hero && Dungeon.level.solid != null && cell < Dungeon.level.solid.length && Dungeon.level.solid[cell]) {
					continue;
				}
				space.monitorCellMask[index] = 1f;
			}
		}
		int center = radius * size + radius;
		if (center >= 0 && center < space.monitorCellMask.length) {
			space.monitorCellMask[center] = 1f;
		}
	}

	private static boolean hasMonitorCellMask(AgentMinActionSpace space) {
		if (space == null || space.monitorCellMask == null) {
			return false;
		}
		for (float value : space.monitorCellMask) {
			if (value > 0f) {
				return true;
			}
		}
		return false;
	}

	private static int preferredMonitorCell(AgentMinState state) {
		if (state == null) {
			return -1;
		}
		int bestCell = state.level.heroPos;
		float bestPriority = 0.18f;
		for (int i = 0; i < state.combat.visibleEnemies.size(); i++) {
			AgentMinState.MobCombatState mob = state.combat.visibleEnemies.get(i);
			float priority = mob.distanceToHero <= 1 ? 0.65f : 0.95f;
			if (priority > bestPriority) {
				bestPriority = priority;
				bestCell = mob.pos;
			}
		}
		for (AgentMinState.CellEntityState item : state.level.visibleItems) {
			if (item.pos >= 0 && 0.48f > bestPriority) {
				bestPriority = 0.48f;
				bestCell = item.pos;
			}
		}
		return bestCell;
	}

	private static float monitorCellPriority(AgentMinState state, int cell) {
		float priority = 0.18f;
		int distance = cellDistance(state, state.level.heroPos, cell);
		if (distance <= 1) {
			priority += 0.05f;
		} else if (distance <= 5) {
			priority += 0.14f;
		}
		int nearestItem = nearestVisibleItemDistance(state, state.level.heroPos);
		int targetItem = nearestVisibleItemDistance(state, cell);
		if (nearestItem >= 0 && targetItem >= 0 && targetItem < nearestItem) {
			priority += 0.12f;
		}
		if (AgentMinExplorationTracker.isDoorCell(state, cell)) {
			priority += 0.16f;
		}
		return clamp(priority);
	}

	private static boolean addMonitorItemActions(AgentMinActionSpace space) {
		WndBag.ItemSelector selector = GameScene.agentMinActiveItemSelector();
		if (selector == null) {
			return false;
		}
		fillMonitorItemMask(space, selector);
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.MONITOR_ITEM, "monitor_item_select_row");
		action.fromCell = Dungeon.hero == null ? -1 : Dungeon.hero.pos;
		action.targetCell = action.fromCell;
		action.itemRow = -1;
		action.priority = monitorItemSelectorPriority(selector);
		add(space, action);
		return true;
	}

	private static void fillMonitorItemMask(AgentMinActionSpace space, WndBag.ItemSelector selector) {
		if (space == null || selector == null) {
			return;
		}
		ArrayList<Item> items = liveItems();
		for (int i = 0; i < items.size() && i < space.monitorItemMask.length; i++) {
			Item item = items.get(i);
			space.monitorItemMask[i] = item != null && selector.itemSelectable(item) ? 1f : 0f;
		}
	}

	private static float monitorItemSelectorPriority(WndBag.ItemSelector selector) {
		if (selector == null || Dungeon.hero == null) {
			return 0.10f;
		}
		float best = 0.12f;
		for (Item item : liveItems()) {
			if (item == null || !selector.itemSelectable(item)) {
				continue;
			}
			float priority = 0.32f;
			if (!item.isIdentified()) {
				priority += 0.38f;
			}
			if (item.isEquipped(Dungeon.hero)) {
				priority += 0.16f;
			}
			if (item.isUpgradable()) {
				priority += 0.20f;
			}
			if (item.cursedKnown && item.cursed) {
				priority -= 0.20f;
			}
			best = Math.max(best, priority);
		}
		return clamp(best);
	}

	private static boolean addMonitorOptionActions(AgentMinActionSpace space, AgentMinState state) {
		int count = GameScene.agentMinOptionCount();
		if (count <= 0) {
			return false;
		}
		for (int i = 0; i < count && i < space.monitorOptionMask.length; i++) {
			space.monitorOptionMask[i] = 1f;
		}
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.MONITOR_OPTION, "monitor_option_select_index");
		action.fromCell = state.level.heroPos;
		action.targetCell = state.level.heroPos;
		action.optionIndex = 0;
		action.priority = 0.72f;
		add(space, action);
		return true;
	}

	private static void addZeroRandomMove(AgentMinActionSpace space, AgentMinState state) {
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ZERO_RANDOM_MOVE, "zero_random_visible_move");
		action.fromCell = state.level.heroPos;
		action.targetCell = -1;
		action.priority = state.combat.visibleEnemies.isEmpty() ? 0.18f : 0.08f;
		add(space, action);
	}

	private static void addMovement(AgentMinActionSpace space, AgentMinState state) {
		for (int i = 0; i < DIRECTIONS.length; i++) {
			int dx = DIRECTIONS[i][0];
			int dy = DIRECTIONS[i][1];
			int target = offsetCell(state.level.heroPos, dx, dy, state.level.width, state.level.height);
			if (target < 0 || !canMoveTo(state, target)) {
				continue;
			}
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.MOVE, DIRECTION_NAMES[i]);
			action.directionIndex = i;
			action.dx = dx;
			action.dy = dy;
			action.fromCell = state.level.heroPos;
			action.targetCell = target;
			action.priority = movementPriority(state, target);
			add(space, action);
		}
	}

	private static void addWait(AgentMinActionSpace space, AgentMinState state) {
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.WAIT, "wait");
		action.fromCell = state.level.heroPos;
		action.targetCell = state.level.heroPos;
		int frontier = AgentMinExplorationTracker.frontierDistance(state, state.level.heroPos);
		action.priority = state.combat.visibleEnemies.isEmpty()
				? (frontier >= 0 ? 0.02f : 0.08f)
				: 0.02f;
		add(space, action);
	}

	private static void addAttacks(AgentMinActionSpace space, AgentMinState state) {
		for (int i = 0; i < state.combat.visibleEnemies.size(); i++) {
			AgentMinState.MobCombatState mob = state.combat.visibleEnemies.get(i);
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ATTACK, "attack_visible_enemy");
			action.fromCell = state.level.heroPos;
			action.targetCell = mob.pos;
			action.targetMobRow = i;
			action.priority = mob.distanceToHero <= 1
					? 1f
					: 0.72f + Math.max(0f, 1f - mob.hpRatio) * 0.22f;
			add(space, action);
		}
	}

	private static void addActionIndicators(AgentMinActionSpace space, AgentMinState state) {
		if (ActionIndicator.action != null && ActionIndicator.action.usable()) {
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ACTION_INDICATOR, "action_indicator");
			action.fromCell = state.level.heroPos;
			action.targetCell = state.level.heroPos;
			action.itemAction = "ACTION_INDICATOR";
			action.priority = state.combat.visibleEnemies.isEmpty() ? 0.34f : 0.68f;
			add(space, action);
		}
		if (ActionIndicator1.action != null) {
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ACTION_INDICATOR, "action_indicator_1");
			action.fromCell = state.level.heroPos;
			action.targetCell = state.level.heroPos;
			action.itemAction = "ACTION_INDICATOR1";
			action.priority = state.combat.visibleEnemies.isEmpty() ? 0.30f : 0.62f;
			add(space, action);
		}
	}

	private static void addPickUp(AgentMinActionSpace space, AgentMinState state) {
		for (AgentMinState.CellEntityState item : state.level.visibleItems) {
			if (item.pos == state.level.heroPos
					&& !AgentMinRewardTracker.isRecentPickupFailure(item.className, item.image, item.pos)) {
				boolean lockedChest = (codeAt(state, state.level.heroPos) & AgentMinState.FLAG_LOCKED_CHEST) != 0;
				if (lockedChest && (!canUnlockChest(state) || AgentMinRewardTracker.isRecentLockedChestFailure(state.level.heroPos))) {
					return;
				}
				AgentMinAction action = new AgentMinAction(lockedChest ? AgentMinAction.Kind.UNLOCK : AgentMinAction.Kind.PICK_UP,
						lockedChest ? "unlock_chest_here" : "pick_up_here");
				action.fromCell = state.level.heroPos;
				action.targetCell = state.level.heroPos;
				action.priority = lockedChest ? 0.75f : 0.9f;
				add(space, action);
				return;
			}
		}
	}

	private static void addUnlocks(AgentMinActionSpace space, AgentMinState state) {
		for (int i = 0; i < DIRECTIONS.length; i++) {
			int target = offsetCell(state.level.heroPos, DIRECTIONS[i][0], DIRECTIONS[i][1], state.level.width, state.level.height);
			int code = codeAt(state, target);
			if (code == AgentMinState.CELL_UNKNOWN) {
				continue;
			}
			int terrain = terrain(code);
			if (terrain != Terrain.LOCKED_DOOR && terrain != Terrain.HERO_LKD_DR && terrain != Terrain.CRYSTAL_DOOR) {
				continue;
			}
			if (!canUnlockDoor(state, terrain) || AgentMinRewardTracker.isRecentLockedDoorFailure(target)) {
				continue;
			}
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.UNLOCK, "unlock_locked_door");
			action.directionIndex = i;
			action.dx = DIRECTIONS[i][0];
			action.dy = DIRECTIONS[i][1];
			action.fromCell = state.level.heroPos;
			action.targetCell = target;
			action.priority = 0.85f;
			add(space, action);
		}
	}

	private static void addStairs(AgentMinActionSpace space, AgentMinState state) {
		if (state.level.heroPos == state.level.exit) {
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.USE_STAIRS, "descend");
			action.fromCell = state.level.heroPos;
			action.targetCell = state.level.exit;
			action.stairMode = AgentMinAction.StairMode.DESCEND;
			action.priority = state.combat.visibleEnemies.isEmpty() ? 0.8f : 0.2f;
			add(space, action);
		}
		if (state.level.heroPos == state.level.entrance && !isBlockedFloorOneEntranceAscend(state)) {
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.USE_STAIRS, "ascend");
			action.fromCell = state.level.heroPos;
			action.targetCell = state.level.entrance;
			action.stairMode = AgentMinAction.StairMode.ASCEND;
			action.priority = 0.1f;
			add(space, action);
		}
	}

	private static boolean isBlockedFloorOneEntranceAscend(AgentMinState state) {
		return state != null
				&& state.level.heroPos == state.level.entrance
				&& Dungeon.depth == 1
				&& Dungeon.branch == 0;
	}

	private static void addHealing(AgentMinActionSpace space, AgentMinState state) {
		ArrayList<AgentMinState.ItemState> items = allItems(state);
		for (int i = 0; i < items.size(); i++) {
			AgentMinState.ItemState item = items.get(i);
			if (isHealingPotion(item) && hasAction(item, "DRINK")) {
				AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.DRINK_HEALING, "drink_healing_potion");
				action.fromCell = state.level.heroPos;
				action.targetCell = state.level.heroPos;
				action.itemRow = i;
				action.itemClassName = item.className;
				action.itemName = item.name;
				fillItemAction(action, item, "DRINK");
				action.priority = state.hero.hpRatio < 0.35f ? 0.95f : state.hero.hpRatio < 0.65f ? 0.45f : 0.05f;
				add(space, action);
			}
		}
	}

	private static void addFood(AgentMinActionSpace space, AgentMinState state) {
		for (int i = 0; i < state.inventory.backpack.size(); i++) {
			AgentMinState.ItemState item = state.inventory.backpack.get(i);
			if (!hasAction(item, "EAT")) {
				continue;
			}
			if (!isFood(item) && !isEatLike(item)) {
				continue;
			}
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.EAT_FOOD, "eat_first_food");
			action.fromCell = state.level.heroPos;
			action.targetCell = state.level.heroPos;
			action.itemRow = state.inventory.equipped.size() + i;
			action.itemClassName = item.className;
			action.itemName = item.name;
			fillItemAction(action, item, "EAT");
			action.priority = foodPriority(state);
			add(space, action);
			return;
		}
	}

	private static void addIndexedItemActions(AgentMinActionSpace space, AgentMinState state) {
		ArrayList<AgentMinState.ItemState> items = allItems(state);
		for (int i = 0; i < items.size(); i++) {
			AgentMinState.ItemState item = items.get(i);
			if (item.actions == null) {
				continue;
			}
			for (int actionIndex = 0; actionIndex < item.actions.size(); actionIndex++) {
				String actionName = item.actions.get(actionIndex);
				if (!shouldAddIndexedItemAction(state, item, actionName)) {
					continue;
				}
				AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ITEM_ACTION, indexedActionLabel(item, actionName));
				action.fromCell = state.level.heroPos;
				action.targetCell = state.level.heroPos;
				action.itemRow = i;
				action.itemClassName = item.className;
				action.itemName = item.name;
				action.itemAction = actionName;
				action.itemActionIndex = actionIndex;
				action.itemActionCount = item.actions.size();
				action.priority = indexedActionPriority(state, item, actionName);
				add(space, action);
				logMetamorphHeadReadyOnce(state, item, actionName);
			}
		}
	}

	private static void addTalentUpgrades(AgentMinActionSpace space, AgentMinState state) {
		Hero hero = Dungeon.hero;
		if (hero == null || hero.talents == null) {
			return;
		}
		for (int tierIndex = 0; tierIndex < hero.talents.size(); tierIndex++) {
			int tier = tierIndex + 1;
			if (!AgentMinMetamorphPlanner.tierAllowed(hero, tier)) {
				continue;
			}
			if (hero.talentPointsAvailable(tier) <= 0) {
				continue;
			}
			LinkedHashMap<Talent, Integer> talents = hero.talents.get(tierIndex);
			if (talents == null) {
				continue;
			}
			int slot = 0;
			for (Map.Entry<Talent, Integer> entry : talents.entrySet()) {
				Talent talent = entry.getKey();
				int points = entry.getValue() == null ? 0 : entry.getValue();
				if (talent == null || !hero.canUpgradeTalent(talent)) {
					slot++;
					continue;
				}
				AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.TALENT_UPGRADE, "upgrade_talent_" + talent.name().toLowerCase());
				action.fromCell = state.level.heroPos;
				action.targetCell = state.level.heroPos;
				action.talentName = talent.name();
				action.talentTier = tier;
				action.talentSlot = slot;
				action.talentPoints = points;
				action.talentMaxPoints = talent.maxPoints();
				action.priority = talentUpgradePriority(tier, points, talent.maxPoints());
				add(space, action);
				slot++;
			}
		}
	}

	private static void addAlchemy(AgentMinActionSpace space, AgentMinState state) {
		if (!hasAlchemyMaterial(state)) {
			return;
		}
		int target = nearestAlchemyCell(state);
		if (target < 0) {
			return;
		}
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ALCHEMY, "open_alchemy");
		action.fromCell = state.level.heroPos;
		action.targetCell = target;
		action.priority = state.level.heroPos == target ? 0.72f : 0.38f;
		add(space, action);
	}

	private static void addThrowing(AgentMinActionSpace space, AgentMinState state) {
		if (state.combat.visibleEnemies.isEmpty()) {
			return;
		}
		ArrayList<AgentMinState.ItemState> items = allItems(state);
		for (int i = 0; i < items.size(); i++) {
			AgentMinState.ItemState item = items.get(i);
			if (!isThrowableWeapon(item) || !hasAction(item, "THROW")) {
				continue;
			}
			for (int m = 0; m < state.combat.visibleEnemies.size(); m++) {
				AgentMinState.MobCombatState mob = state.combat.visibleEnemies.get(m);
				if (mob.distanceToHero <= 1) {
					continue;
				}
				AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.THROW_WEAPON, "throw_weapon_at_enemy");
				action.fromCell = state.level.heroPos;
				action.targetCell = mob.pos;
				action.targetMobRow = m;
				action.itemRow = i;
				action.itemClassName = item.className;
				action.itemName = item.name;
				fillItemAction(action, item, "THROW");
				float line = lineQuality(state, mob.pos, Ballistica.PROJECTILE);
				action.priority = clamp((mob.distanceToHero >= 3 ? 0.48f : 0.16f)
						+ mob.rangedValue * 0.22f
						+ mob.killChance * 0.18f
						+ line * 0.12f);
				add(space, action);
			}
		}
	}

	private static void addTargetedItemThrows(AgentMinActionSpace space, AgentMinState state) {
		if (state.combat.visibleEnemies.isEmpty()) {
			return;
		}
		ArrayList<AgentMinState.ItemState> items = allItems(state);
		for (int i = 0; i < items.size(); i++) {
			AgentMinState.ItemState item = items.get(i);
			if (isThrowableWeapon(item) || item.actions == null) {
				continue;
			}
			for (String throwAction : new String[]{"THROW", "LIGHTTHROW"}) {
				if (!hasAction(item, throwAction)) {
					continue;
				}
				for (int m = 0; m < state.combat.visibleEnemies.size(); m++) {
					AgentMinState.MobCombatState mob = state.combat.visibleEnemies.get(m);
					if (mob.distanceToHero <= 1 && !"LIGHTTHROW".equals(throwAction)) {
						continue;
					}
					AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ITEM_ACTION,
							"item_action_" + throwAction.toLowerCase() + "_at_enemy");
					action.fromCell = state.level.heroPos;
					action.targetCell = mob.pos;
					action.targetMobRow = m;
					action.itemRow = i;
					action.itemClassName = item.className;
					action.itemName = item.name;
					fillItemAction(action, item, throwAction);
					float line = lineQuality(state, mob.pos, Ballistica.PROJECTILE);
					action.priority = clamp((isCombatConsumable(item) ? 0.34f : 0.14f)
							+ (mob.distanceToHero > 1 ? 0.16f : 0f)
							+ mob.rangedValue * 0.18f
							+ mob.killChance * 0.12f
							+ line * 0.14f);
					add(space, action);
				}
			}
		}
	}

	private static void addWands(AgentMinActionSpace space, AgentMinState state) {
		if (state.combat.visibleEnemies.isEmpty()) {
			return;
		}
		ArrayList<AgentMinState.ItemState> items = allItems(state);
		for (int i = 0; i < items.size(); i++) {
			AgentMinState.ItemState item = items.get(i);
			if (!isWandLike(item) || !hasAction(item, "ZAP")) {
				continue;
			}
			for (int m = 0; m < state.combat.visibleEnemies.size(); m++) {
				AgentMinState.MobCombatState mob = state.combat.visibleEnemies.get(m);
				AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ZAP_WAND, "zap_wand_at_enemy");
				action.fromCell = state.level.heroPos;
				action.targetCell = mob.pos;
				action.targetMobRow = m;
				action.itemRow = i;
				action.itemClassName = item.className;
				action.itemName = item.name;
				fillItemAction(action, item, "ZAP");
				float line = lineQuality(state, mob.pos, Ballistica.MAGIC_BOLT);
				action.priority = clamp((mob.distanceToHero <= 1 ? 0.16f : 0.50f)
						+ mob.rangedValue * 0.25f
						+ mob.killChance * 0.14f
						+ line * 0.14f);
				add(space, action);
			}
		}
	}

	private static void encode(AgentMinActionSpace space, AgentMinState state) {
		float[] optionVector = optionVector(state);
		space.actionCount = Math.min(space.actions.size(), AgentMinActionSpace.MAX_ACTIONS);
		for (int i = 0; i < space.actionCount; i++) {
			AgentMinAction action = space.actions.get(i);
			space.actionMask[i] = action.valid ? 1f : 0f;
			float[] row = space.actionMatrix[i];
			row[0] = action.valid ? 1f : 0f;
			row[1] = kind(action.kind);
			row[2] = action.directionIndex < 0 ? 0f : (action.directionIndex + 1) / 8f;
			row[3] = action.dx / 1f;
			row[4] = action.dy / 1f;
			row[5] = norm(action.fromCell, Math.max(1, state.level.length));
			row[6] = norm(action.targetCell, Math.max(1, state.level.length));
			row[7] = norm(action.targetMobRow + 1, AgentMinActionSpace.MAX_ACTIONS);
			row[8] = norm(action.itemRow + 1, AgentMinActionSpace.MAX_ACTIONS);
			row[9] = action.quickSlot >= 0 ? 1f : 0f;
			row[10] = action.quickSlot >= 0 ? norm(action.quickSlot + 1, 6f) : 0f;
			row[11] = action.stairMode == AgentMinAction.StairMode.ASCEND ? 1f : 0f;
			row[12] = action.stairMode == AgentMinAction.StairMode.DESCEND ? 1f : 0f;
			row[13] = clamp(action.priority);
			row[14] = itemCategory(action.itemClassName);
			row[15] = action.itemAction == null ? 0f : actionKind(action.itemAction);
			row[16] = action.kind == AgentMinAction.Kind.MOVE || action.kind == AgentMinAction.Kind.ZERO_RANDOM_MOVE ? 1f : 0f;
			row[17] = action.kind == AgentMinAction.Kind.ATTACK ? 1f : 0f;
			row[18] = action.kind == AgentMinAction.Kind.DRINK_HEALING ? 1f : 0f;
			row[19] = action.kind == AgentMinAction.Kind.EAT_FOOD ? 1f : 0f;
			row[20] = action.kind == AgentMinAction.Kind.THROW_WEAPON ? 1f : 0f;
			row[21] = action.kind == AgentMinAction.Kind.ZAP_WAND ? 1f : 0f;
			row[22] = frontierImprovement(state, action);
			row[23] = explorationAffinity(state, action);
			row[24] = optionVector[0] * actionOptionAffinity(state, action, "EXPLORE");
			row[25] = optionVector[1] * actionOptionAffinity(state, action, "ENGAGE");
			row[26] = optionVector[2] * actionOptionAffinity(state, action, "RETREAT");
			row[27] = optionVector[3] * actionOptionAffinity(state, action, "USE_RESOURCE");
			row[28] = optionVector[4] * actionOptionAffinity(state, action, "CONSUME");
			row[29] = optionVector[5] * actionOptionAffinity(state, action, "PICKUP");
			row[30] = optionVector[6] * actionOptionAffinity(state, action, "UNLOCK");
			row[31] = optionVector[7] * actionOptionAffinity(state, action, "DROP");
			fillCombatFeatures(row, state, action);
			row[40] = action.kind == AgentMinAction.Kind.TALENT_UPGRADE ? norm(action.talentTier, Talent.MAX_TALENT_TIERS)
					: norm(action.itemActionIndex + 1, 16f);
			row[41] = action.kind == AgentMinAction.Kind.TALENT_UPGRADE ? norm(action.talentPoints + 1, Math.max(1, action.talentMaxPoints + 1))
					: norm(action.itemActionCount, 16f);
			row[42] = action.kind == AgentMinAction.Kind.ITEM_ACTION ? 1f : 0f;
			row[43] = action.kind == AgentMinAction.Kind.ALCHEMY ? 1f : 0f;
			row[44] = action.skill == AgentMinAction.Skill.TALENT ? 1f : 0f;
			row[45] = action.skill == AgentMinAction.Skill.DROP ? 1f : 0f;
			row[46] = action.targetCell >= 0 && action.targetCell != action.fromCell ? 1f : 0f;
			row[47] = action.itemAction != null && action.itemActionIndex == 0 ? 1f : 0f;
			row[48] = action.kind == AgentMinAction.Kind.TALENT_UPGRADE ? norm(action.talentSlot + 1, AgentMinEncodedState.TALENT_ICON_SLOTS)
					: norm(action.optionIndex + 1, 16f);
			row[49] = action.kind == AgentMinAction.Kind.MONITOR_OPTION ? 1f : 0f;
			row[50] = action.kind == AgentMinAction.Kind.TALENT_UPGRADE || isTalentResource(action.itemClassName) ? 1f : 0f;
			row[51] = action.kind == AgentMinAction.Kind.TALENT_UPGRADE ? itemActionHeadHint("TALENT_UPGRADE")
					: action.itemAction != null ? itemActionHeadHint(action.itemAction) : 0f;
			int skill = skillIndex(action.skill);
			if (skill >= 0 && skill < AgentMinActionSpace.MAX_SKILLS) {
				space.actionSkillMask[skill][i] = action.valid ? 1f : 0f;
				if (action.valid) {
					space.skillMask[skill] = 1f;
				}
			}
		}
	}

	private static void add(AgentMinActionSpace space, AgentMinAction action) {
		if (space.actions.size() >= AgentMinActionSpace.MAX_ACTIONS) {
			return;
		}
		action.skill = inferSkill(action);
		action.actionId = space.actions.size();
		space.actions.add(action);
	}

	private static AgentMinAction.Skill inferSkill(AgentMinAction action) {
		if (action == null || action.kind == null) {
			return AgentMinAction.Skill.EXPLORE;
		}
		switch (action.kind) {
			case ATTACK:
			case THROW_WEAPON:
			case ZAP_WAND:
			case ACTION_INDICATOR:
				return AgentMinAction.Skill.COMBAT;
			case DRINK_HEALING:
			case EAT_FOOD:
				return AgentMinAction.Skill.EAT;
			case ITEM_ACTION:
				if ("DROP".equals(action.itemAction)) {
					return AgentMinAction.Skill.DROP;
				}
				return AgentMinAction.Skill.ITEM;
			case MONITOR_CELL:
			case MONITOR_ITEM:
			case MONITOR_OPTION:
				return AgentMinAction.Skill.ITEM;
			case ALCHEMY:
				return AgentMinAction.Skill.ALCHEMY;
			case TALENT_UPGRADE:
				return AgentMinAction.Skill.TALENT;
			case UNLOCK:
				return AgentMinAction.Skill.UNLOCK;
			case PICK_UP:
				return AgentMinAction.Skill.PICKUP;
			case USE_STAIRS:
				return action.stairMode == AgentMinAction.StairMode.DESCEND
						? AgentMinAction.Skill.DESCEND
						: AgentMinAction.Skill.EXPLORE;
			case ZERO_RANDOM_MOVE:
			case MOVE:
			case WAIT:
			default:
				return AgentMinAction.Skill.EXPLORE;
		}
	}

	private static void fillItemAction(AgentMinAction action, AgentMinState.ItemState item, String actionName) {
		action.itemAction = actionName;
		action.itemActionIndex = itemActionIndex(item, actionName);
		action.itemActionCount = item == null || item.actions == null ? 0 : item.actions.size();
	}

	private static int itemActionIndex(AgentMinState.ItemState item, String actionName) {
		if (item == null || item.actions == null || actionName == null) {
			return -1;
		}
		for (int i = 0; i < item.actions.size(); i++) {
			if (actionName.equals(item.actions.get(i))) {
				return i;
			}
		}
		return -1;
	}

	private static float talentUpgradePriority(int tier, int points, int maxPoints) {
		float fill = maxPoints <= 0 ? 0f : points / (float)maxPoints;
		return clamp(0.70f + tier * 0.04f - fill * 0.10f);
	}

	private static boolean shouldAddIndexedItemAction(AgentMinState state, AgentMinState.ItemState item, String actionName) {
		if (item == null || actionName == null) {
			return false;
		}
		if (isTalentResource(item.className) && ("READ".equals(actionName) || "CAST".equals(actionName))) {
			return canUseTalentResource(state, item);
		}
		if (isAgentMinNoWindowItemAction(item, actionName)) {
			return true;
		}
		if (isBlockedRealtimeItemAction(item, actionName) || isTargetedThrowAction(actionName)) {
			return false;
		}
		if ("EQUIP".equals(actionName)) {
			return shouldExposeEquipAction(state, item);
		}
		if ("UNEQUIP".equals(actionName)) {
			return AgentMinRewardConfig.ALLOW_UNEQUIP_ACTIONS && shouldExposeUnequipAction(item);
		}
		if ("INSPECT".equals(actionName)) {
			return true;
		}
		if ("DROP".equals(actionName)) {
			return !item.unique && inventoryUnderPressure(state);
		}
		if (isSafeNoTargetItemAction(actionName)) {
			return true;
		}
		if (!item.usesTargeting && actionName.equals(item.defaultAction)) {
			return true;
		}
		return false;
	}

	private static String indexedActionLabel(AgentMinState.ItemState item, String actionName) {
		if (isTalentResource(item == null ? null : item.className)) {
			return "talent_resource_" + actionName.toLowerCase();
		}
		if (isUpgradeResource(item == null ? null : item.className) && "READ".equals(actionName)) {
			return "upgrade_best_item";
		}
		return "item_action_" + actionName.toLowerCase();
	}

	private static float indexedActionPriority(AgentMinState state, AgentMinState.ItemState item, String actionName) {
		if (isTalentResource(item.className)) {
			return 0.98f;
		}
		if (isUpgradeResource(item.className) && "READ".equals(actionName)) {
			return 0.70f;
		}
		if ("EQUIP".equals(actionName)) {
			if (item.cursedKnown && item.cursed) {
				return 0.02f;
			}
			AgentMinState.ItemState oldItem = currentEquippedFor(state, item);
			float oldScore = oldItem == null ? 0f : oldItem.equipmentScore;
			float priority = 0.08f + Math.min(0.30f, Math.max(0f, item.equipmentScore - oldScore) / 40f);
			if (item.strengthRequirement > 0) {
				priority += item.strengthMargin >= 0 ? 0.22f + Math.min(0.12f, item.strengthMargin * 0.02f)
						: Math.max(-0.22f, item.strengthMargin * 0.06f);
			}
			return clamp(priority);
		}
		if ("UNEQUIP".equals(actionName)) {
			if (item.cursedKnown && item.cursed) {
				return 0.02f;
			}
			return item.strengthRequirement > 0 && item.strengthMargin < 0 ? 0.10f : 0.04f;
		}
		if ("INSPECT".equals(actionName)) {
			return 0.16f;
		}
		if ("DROP".equals(actionName)) {
			return inventoryUnderPressure(state) ? 0.08f : 0.01f;
		}
		if (isConsumableAction(actionName)) {
			return "EAT".equals(actionName) || "eat".equals(actionName) ? foodPriority(state) : 0.22f;
		}
		if ("PLANT".equals(actionName) || "PLANT_IN_BODY".equals(actionName)) {
			return state.combat.visibleEnemies.isEmpty() ? 0.18f : 0.32f;
		}
		return 0.05f;
	}

	private static boolean shouldExposeEquipAction(AgentMinState state, AgentMinState.ItemState item) {
		if (state == null || item == null || item.equipped || item.equipmentScore <= 0f) {
			return false;
		}
		if (item.cursedKnown && item.cursed) {
			return false;
		}
		if (item.strengthRequirement > 0 && item.strengthMargin < -1) {
			return false;
		}
		if (AgentMinRewardTracker.isEquipmentActionCoolingDown(item)) {
			return false;
		}
		AgentMinState.ItemState oldItem = currentEquippedFor(state, item);
		if (oldItem == null) {
			return item.equipmentScore >= 1f;
		}
		float scoreDiff = item.equipmentScore - oldItem.equipmentScore;
		if (scoreDiff >= AgentMinRewardConfig.EQUIP_MIN_SCORE_IMPROVEMENT) {
			return true;
		}
		return oldItem.strengthRequirement > 0
				&& oldItem.strengthMargin < 0
				&& item.strengthMargin > oldItem.strengthMargin
				&& scoreDiff > -AgentMinRewardConfig.EQUIP_MIN_SCORE_IMPROVEMENT;
	}

	private static boolean shouldExposeUnequipAction(AgentMinState.ItemState item) {
		return item != null
				&& item.equipped
				&& !AgentMinRewardTracker.isEquipmentActionCoolingDown(item)
				&& item.strengthRequirement > 0
				&& item.strengthMargin < -2;
	}

	private static AgentMinState.ItemState currentEquippedFor(AgentMinState state, AgentMinState.ItemState item) {
		if (state == null || item == null || item.equipmentKind == null || item.equipmentKind.length() == 0) {
			return null;
		}
		for (AgentMinState.ItemState equipped : state.inventory.equipped) {
			if (equipped == null || equipped == item || equipped.equipmentKind == null) {
				continue;
			}
			if (sameEquipmentSlot(item.equipmentKind, equipped.equipmentKind)) {
				return equipped;
			}
		}
		return null;
	}

	private static boolean sameEquipmentSlot(String a, String b) {
		if (a == null || b == null) {
			return false;
		}
		if (a.equals(b)) {
			return true;
		}
		boolean aMisc = "ring".equals(a) || "artifact".equals(a) || "misc".equals(a);
		boolean bMisc = "ring".equals(b) || "artifact".equals(b) || "misc".equals(b);
		return aMisc && bMisc;
	}

	private static boolean inventoryUnderPressure(AgentMinState state) {
		return state != null && state.inventory.backpack.size() >= 18;
	}

	private static boolean isBlockedRealtimeItemAction(AgentMinState.ItemState item, String actionName) {
		if (actionName == null) {
			return true;
		}
		if ("READ".equals(actionName)) {
			return !isAgentMinNoWindowItemAction(item, actionName);
		}
		if ("OPEN".equals(actionName)
				|| "CHOOSE".equals(actionName)
				|| "APPLY".equals(actionName)
				|| "AFFIX".equals(actionName)
				|| "IMBUE".equals(actionName)
				|| "TRANS".equals(actionName)
				|| "BREW".equals(actionName)
				|| "ENERGIZE".equals(actionName)
				|| "STORE".equals(actionName)
				|| "DIRECT".equals(actionName)
				|| "OUTFIT".equals(actionName)
				|| "LOAD".equals(actionName)
				|| "LOAD_ENERGY".equals(actionName)
				|| "LOAD_POTION".equals(actionName)
				|| "TRANSFER".equals(actionName)
				|| "DETACH".equals(actionName)
				|| "SET".equals(actionName)
				|| "RETURN".equals(actionName)
				|| "STEAL".equals(actionName)
				|| "ENTER".equals(actionName)
				|| "GUIDE".equals(actionName)
				|| "make".equals(actionName)) {
			return true;
		}
		return item != null && item.className != null
				&& item.className.contains(".items.ScrollOfSublimation")
				&& "READ".equals(actionName);
	}

	private static boolean isAgentMinNoWindowItemAction(AgentMinState.ItemState item, String actionName) {
		return item != null
				&& isUpgradeResource(item.className)
				&& "READ".equals(actionName);
	}

	private static boolean isUpgradeResource(String className) {
		return className != null && className.contains(".items.scrolls.ScrollOfUpgrade");
	}

	private static boolean isTargetedThrowAction(String actionName) {
		return "THROW".equals(actionName) || "LIGHTTHROW".equals(actionName);
	}

	private static boolean isSafeNoTargetItemAction(String actionName) {
		return isConsumableAction(actionName)
				|| "PLANT".equals(actionName)
				|| "PLANT_IN_BODY".equals(actionName)
				|| "LIGHT".equals(actionName)
				|| "BLESS".equals(actionName)
				|| "USE".equals(actionName)
				|| "ALLUSE".equals(actionName)
				|| "SHATTER".equals(actionName)
				|| "SNACK".equals(actionName)
				|| "STEALTH".equals(actionName)
				|| "ROOT".equals(actionName)
				|| "ACTIVATE".equals(actionName);
	}

	private static boolean isConsumableAction(String actionName) {
		return "DRINK".equals(actionName)
				|| "EAT".equals(actionName)
				|| "eat".equals(actionName);
	}

	private static boolean isCombatConsumable(AgentMinState.ItemState item) {
		if (item == null || item.className == null) {
			return false;
		}
		return hasPath(item, ".items.stones.")
				|| hasPath(item, ".plants.")
				|| hasPath(item, ".items.bombs.")
				|| hasPath(item, ".items.Honeypot")
				|| hasPath(item, ".items.potions.brews.");
	}

	private static boolean hasAlchemyMaterial(AgentMinState state) {
		for (AgentMinState.ItemState item : allItems(state)) {
			if (isAlchemyMaterial(item)) {
				return true;
			}
		}
		return false;
	}

	private static int nearestAlchemyCell(AgentMinState state) {
		int best = -1;
		int bestDistance = Integer.MAX_VALUE;
		if (state == null || state.level.length <= 0) {
			return -1;
		}
		for (int cell = 0; cell < state.level.length; cell++) {
			int code = codeAt(state, cell);
			if (code == AgentMinState.CELL_UNKNOWN || terrain(code) != Terrain.ALCHEMY) {
				continue;
			}
			int distance = cellDistance(state, state.level.heroPos, cell);
			if (distance < bestDistance) {
				best = cell;
				bestDistance = distance;
			}
		}
		return best;
	}

	private static boolean canMoveTo(AgentMinState state, int cell) {
		int code = state.level.visibleMap != null && cell < state.level.visibleMap.length ? state.level.visibleMap[cell] : 0;
		if (code == AgentMinState.CELL_UNKNOWN && state.level.exploredMap != null && cell < state.level.exploredMap.length) {
			code = state.level.exploredMap[cell];
		}
		boolean passableDoor = AgentMinStateBuilder.isPassableDoorTerrain(terrain(code));
		return code != AgentMinState.CELL_UNKNOWN
				&& (((code & AgentMinState.FLAG_SOLID) == 0) || passableDoor)
				&& ((code & AgentMinState.FLAG_PASSABLE) != 0 || (code & AgentMinState.FLAG_AVOID) != 0 || passableDoor)
				&& (code & AgentMinState.FLAG_MOB) == 0;
	}

	private static float movementPriority(AgentMinState state, int target) {
		float priority = state.combat.visibleEnemies.isEmpty() ? 0.42f : 0.18f;
		int nearestItem = nearestVisibleItemDistance(state, state.level.heroPos);
		int targetItem = nearestVisibleItemDistance(state, target);
		if (nearestItem >= 0 && targetItem >= 0 && targetItem < nearestItem) {
			priority += 0.28f;
		}
		if (state.level.visibleMap != null && target >= 0 && target < state.level.visibleMap.length) {
			int code = state.level.visibleMap[target];
			if ((code & AgentMinState.FLAG_ITEM) != 0) {
				priority += 0.2f;
			}
		}
		int currentFrontier = AgentMinExplorationTracker.frontierDistance(state, state.level.heroPos);
		int targetFrontier = AgentMinExplorationTracker.frontierDistance(state, target);
		if (currentFrontier >= 0 && targetFrontier >= 0 && targetFrontier < currentFrontier) {
			priority += 0.18f + 0.03f * Math.min(4, currentFrontier - targetFrontier);
		}
		if (AgentMinExplorationTracker.isDoorCell(state, target)) {
			priority += 0.26f;
		}
		if (AgentMinExplorationTracker.isAdjacentToUnpassedDoor(state, target)) {
			priority += 0.16f;
		}
		return clamp(priority);
	}

	private static float frontierImprovement(AgentMinState state, AgentMinAction action) {
		if (action == null || (action.kind != AgentMinAction.Kind.MOVE && action.kind != AgentMinAction.Kind.ZERO_RANDOM_MOVE)) {
			return 0f;
		}
		if (action.kind == AgentMinAction.Kind.ZERO_RANDOM_MOVE || action.targetCell < 0) {
			return 0.15f;
		}
		int current = AgentMinExplorationTracker.frontierDistance(state, state.level.heroPos);
		int target = AgentMinExplorationTracker.frontierDistance(state, action.targetCell);
		if (current < 0 || target < 0) {
			return 0f;
		}
		float delta = (current - target) / 8f;
		return Math.max(-1f, Math.min(1f, delta));
	}

	private static float explorationAffinity(AgentMinState state, AgentMinAction action) {
		if (action == null) {
			return 0f;
		}
		if (action.kind == AgentMinAction.Kind.WAIT) {
			return 0f;
		}
		if (action.kind == AgentMinAction.Kind.ZERO_RANDOM_MOVE) {
			return 0.3f;
		}
		if (action.targetCell >= 0 && AgentMinExplorationTracker.isDoorCell(state, action.targetCell)) {
			return 1f;
		}
		if (action.targetCell >= 0 && AgentMinExplorationTracker.isAdjacentToUnpassedDoor(state, action.targetCell)) {
			return 0.7f;
		}
		return action.kind == AgentMinAction.Kind.MOVE ? 0.2f : 0f;
	}

	private static float targetDistanceFeature(AgentMinState state, AgentMinAction action) {
		if (action == null || action.targetCell < 0) {
			return 0f;
		}
		return norm(cellDistance(state, state.level.heroPos, action.targetCell), 16f);
	}

	private static float resourceUseAffinity(AgentMinAction action) {
		if (action == null) {
			return 0f;
		}
		switch (action.kind) {
			case DRINK_HEALING:
			case EAT_FOOD:
			case THROW_WEAPON:
			case ZAP_WAND:
			case ITEM_ACTION:
			case ALCHEMY:
			case MONITOR_CELL:
			case MONITOR_ITEM:
				return 1f;
			default:
				return 0f;
		}
	}

	private static void fillCombatFeatures(float[] row, AgentMinState state, AgentMinAction action) {
		AgentMinState.MobCombatState mob = targetMob(state, action);
		row[32] = targetDistanceFeature(state, action);
		row[33] = projectileLineFeature(state, action);
		row[34] = mob == null ? 0f : clamp(1f - mob.hpRatio);
		row[35] = mob == null ? 0f : norm(mob.threatScore, 120f);
		row[36] = mob == null ? 0f : clamp(mob.meleeDanger);
		row[37] = resourceUseAffinity(action);
		row[38] = mob == null ? 0f : clamp(state.hero.expectedDamage / Math.max(1f, mob.hp));
		row[39] = tacticalCombatScore(state, action, mob);
	}

	private static AgentMinState.MobCombatState targetMob(AgentMinState state, AgentMinAction action) {
		if (state == null || action == null || action.targetMobRow < 0 || action.targetMobRow >= state.combat.visibleEnemies.size()) {
			return null;
		}
		return state.combat.visibleEnemies.get(action.targetMobRow);
	}

	private static float projectileLineFeature(AgentMinState state, AgentMinAction action) {
		if (action == null || action.targetCell < 0) {
			return 0f;
		}
		if (action.kind == AgentMinAction.Kind.THROW_WEAPON) {
			return lineQuality(state, action.targetCell, Ballistica.PROJECTILE);
		}
		if (action.kind == AgentMinAction.Kind.ZAP_WAND) {
			return lineQuality(state, action.targetCell, Ballistica.MAGIC_BOLT);
		}
		return 0f;
	}

	private static float tacticalCombatScore(AgentMinState state, AgentMinAction action, AgentMinState.MobCombatState mob) {
		if (state == null || action == null || mob == null) {
			return 0f;
		}
		float score = mob.killChance * 0.35f + norm(mob.threatScore, 120f) * 0.25f;
		if (action.kind == AgentMinAction.Kind.ATTACK) {
			score += mob.distanceToHero <= 1 ? 0.35f : -0.15f;
		} else if (action.kind == AgentMinAction.Kind.THROW_WEAPON || action.kind == AgentMinAction.Kind.ZAP_WAND) {
			score += mob.distanceToHero > 1 ? 0.25f : -0.25f;
			score += projectileLineFeature(state, action) * 0.25f;
			if (state.hero.hpRatio < 0.5f) {
				score += 0.1f;
			}
		}
		return Math.max(-1f, Math.min(1f, score));
	}

	private static float lineQuality(AgentMinState state, int targetCell, int ballisticaMode) {
		if (state == null || targetCell < 0 || Dungeon.level == null || state.level.heroPos < 0) {
			return 0f;
		}
		try {
			Ballistica shot = new Ballistica(state.level.heroPos, targetCell, ballisticaMode);
			if (shot.collisionPos == targetCell) {
				return 1f;
			}
			int targetDistance = cellDistance(state, state.level.heroPos, targetCell);
			int collisionDistance = cellDistance(state, state.level.heroPos, shot.collisionPos);
			if (targetDistance <= 0) {
				return 0f;
			}
			return clamp(collisionDistance / (float)targetDistance);
		} catch (Exception e) {
			return 0f;
		}
	}

	private static float[] optionVector(AgentMinState state) {
		float[] out = new float[8];
		boolean hasEnemy = !state.combat.visibleEnemies.isEmpty();
		boolean lowHp = state.hero.hpRatio <= 0.45f;
		boolean hungry = state.hero.starving || state.hero.hungerLevel <= 150f;
		int frontier = AgentMinExplorationTracker.frontierDistance(state, state.level.heroPos);
		boolean onItem = false;
		for (AgentMinState.CellEntityState item : state.level.visibleItems) {
			if (item.pos == state.level.heroPos) {
				onItem = true;
				break;
			}
		}
		out[0] = !hasEnemy && frontier >= 0 ? clamp(0.55f + Math.max(0f, 0.35f - frontier * 0.03f)) : 0.1f;
		out[1] = hasEnemy && state.combat.fightScore >= 0 ? clamp(0.45f + state.hero.hpRatio * 0.45f) : 0.05f;
		out[2] = hasEnemy && (state.combat.fightScore < 0 || lowHp) ? clamp(0.5f + (1f - state.hero.hpRatio) * 0.4f) : 0.05f;
		out[3] = lowHp ? 0.9f : 0.1f;
		out[4] = hungry ? 0.95f : 0.05f;
		out[5] = onItem ? 0.9f : (state.level.visibleItems.isEmpty() ? 0.05f : 0.2f);
		out[6] = (state.hero.hasIronKey || state.hero.hasGoldenKey || state.hero.hasCrystalKey || state.hero.hasWornKey) ? 0.55f : 0.05f;
		out[7] = 0.1f;
		return out;
	}

	private static float actionOptionAffinity(AgentMinState state, AgentMinAction action, String option) {
		if (state == null || action == null || option == null) {
			return 0f;
		}
		switch (option) {
			case "EXPLORE":
				return (action.kind == AgentMinAction.Kind.MOVE || action.kind == AgentMinAction.Kind.ZERO_RANDOM_MOVE) ? 1f : 0.05f;
			case "ENGAGE":
				return (action.kind == AgentMinAction.Kind.ATTACK || action.kind == AgentMinAction.Kind.THROW_WEAPON || action.kind == AgentMinAction.Kind.ZAP_WAND) ? 1f : 0.05f;
			case "RETREAT":
				return (action.kind == AgentMinAction.Kind.WAIT || action.kind == AgentMinAction.Kind.DRINK_HEALING) ? 0.6f : 0.05f;
			case "USE_RESOURCE":
				return (action.kind == AgentMinAction.Kind.DRINK_HEALING || action.kind == AgentMinAction.Kind.EAT_FOOD
						|| action.kind == AgentMinAction.Kind.ZAP_WAND || action.kind == AgentMinAction.Kind.ITEM_ACTION
						|| action.kind == AgentMinAction.Kind.ALCHEMY || action.kind == AgentMinAction.Kind.MONITOR_CELL
						|| action.kind == AgentMinAction.Kind.MONITOR_ITEM) ? 1f : 0.05f;
			case "CONSUME":
				return (action.kind == AgentMinAction.Kind.EAT_FOOD || action.kind == AgentMinAction.Kind.DRINK_HEALING
						|| (action.kind == AgentMinAction.Kind.ITEM_ACTION && ("EAT".equals(action.itemAction) || "DRINK".equals(action.itemAction)))) ? 1f : 0.05f;
			case "PICKUP":
				return action.kind == AgentMinAction.Kind.PICK_UP ? 1f : 0.05f;
			case "UNLOCK":
				return action.kind == AgentMinAction.Kind.UNLOCK
						|| (action.kind == AgentMinAction.Kind.MOVE && AgentMinExplorationTracker.isDoorCell(state, action.targetCell)) ? 1f : 0.05f;
			case "DROP":
				return action.kind == AgentMinAction.Kind.ITEM_ACTION && "DROP".equals(action.itemAction) ? 1f : 0.05f;
			default:
				return 0f;
		}
	}

	private static int nearestVisibleItemDistance(AgentMinState state, int from) {
		int best = -1;
		for (AgentMinState.CellEntityState item : state.level.visibleItems) {
			if (!item.visible || item.pos < 0) {
				continue;
			}
			int distance = cellDistance(state, from, item.pos);
			if (best == -1 || distance < best) {
				best = distance;
			}
		}
		return best;
	}

	private static int cellDistance(AgentMinState state, int a, int b) {
		if (a < 0 || b < 0 || state.level.width <= 0) {
			return 9999;
		}
		int ax = a % state.level.width;
		int ay = a / state.level.width;
		int bx = b % state.level.width;
		int by = b / state.level.width;
		return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
	}

	private static int codeAt(AgentMinState state, int cell) {
		if (state == null || cell < 0) {
			return AgentMinState.CELL_UNKNOWN;
		}
		if (state.level.visibleMap != null && cell < state.level.visibleMap.length
				&& state.level.visibleMap[cell] != AgentMinState.CELL_UNKNOWN) {
			return state.level.visibleMap[cell];
		}
		if (state.level.exploredMap != null && cell < state.level.exploredMap.length) {
			return state.level.exploredMap[cell];
		}
		return AgentMinState.CELL_UNKNOWN;
	}

	private static int terrain(int code) {
		return code & 0xFFFF;
	}

	private static boolean canUnlockDoor(AgentMinState state, int terrain) {
		if (terrain == Terrain.LOCKED_DOOR) {
			return state.hero.hasIronKey;
		}
		if (terrain == Terrain.CRYSTAL_DOOR) {
			return state.hero.hasCrystalKey;
		}
		return terrain == Terrain.HERO_LKD_DR;
	}

	private static boolean canUnlockChest(AgentMinState state) {
		return state != null && (state.hero.hasGoldenKey || state.hero.hasCrystalKey);
	}

	private static int skillIndex(AgentMinAction.Skill skill) {
		return skill == null ? -1 : skill.ordinal();
	}

	private static int offsetCell(int cell, int dx, int dy, int width, int height) {
		int x = cell % width;
		int y = cell / width;
		int nx = x + dx;
		int ny = y + dy;
		if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
			return -1;
		}
		return ny * width + nx;
	}

	private static ArrayList<AgentMinState.ItemState> allItems(AgentMinState state) {
		ArrayList<AgentMinState.ItemState> items = new ArrayList<>();
		items.addAll(state.inventory.equipped);
		items.addAll(state.inventory.backpack);
		return items;
	}

	private static ArrayList<Item> liveItems() {
		ArrayList<Item> items = new ArrayList<>();
		if (Dungeon.hero == null || Dungeon.hero.belongings == null || Dungeon.hero.belongings.backpack == null) {
			return items;
		}
		addLiveItemIfNotNull(items, Dungeon.hero.belongings.weapon());
		addLiveItemIfNotNull(items, Dungeon.hero.belongings.armor());
		addLiveItemIfNotNull(items, Dungeon.hero.belongings.artifact());
		addLiveItemIfNotNull(items, Dungeon.hero.belongings.misc());
		addLiveItemIfNotNull(items, Dungeon.hero.belongings.ring());
		addLiveItemIfNotNull(items, Dungeon.hero.belongings.secondWep());
		for (Item item : Dungeon.hero.belongings.backpack.items) {
			addLiveItemTree(items, item);
		}
		return items;
	}

	private static void addLiveItemTree(ArrayList<Item> items, Item item) {
		addLiveItemIfNotNull(items, item);
		if (item instanceof Bag) {
			for (Item child : ((Bag)item).items) {
				addLiveItemTree(items, child);
			}
		}
	}

	private static void addLiveItemIfNotNull(ArrayList<Item> items, Item item) {
		if (item != null) {
			items.add(item);
		}
	}

	private static boolean isHealingPotion(AgentMinState.ItemState item) {
		return item != null && item.className != null && item.className.endsWith(".PotionOfHealing");
	}

	private static boolean isThrowableWeapon(AgentMinState.ItemState item) {
		return item != null && item.className != null && item.className.contains(".items.weapon.missiles.");
	}

	private static boolean isFood(AgentMinState.ItemState item) {
		return item != null && item.className != null && item.className.contains(".items.food.");
	}

	private static boolean isEatLike(AgentMinState.ItemState item) {
		if (item == null || item.className == null) {
			return false;
		}
		String c = item.className.toLowerCase();
		return c.contains("berry") || c.contains("ration") || c.contains("pasty") || c.contains("stew");
	}

	private static boolean isWandLike(AgentMinState.ItemState item) {
		if (item == null || item.className == null) {
			return false;
		}
		return item.className.contains(".items.wands.") || item.className.endsWith(".MagesStaff");
	}

	private static boolean isAlchemyMaterial(AgentMinState.ItemState item) {
		return hasPath(item, ".items.potions.")
				|| hasPath(item, ".items.scrolls.")
				|| hasPath(item, ".items.stones.")
				|| hasPath(item, ".items.bombs.")
				|| hasPath(item, ".items.EnergyCrystal")
				|| hasPath(item, ".items.ArcaneResin")
				|| hasPath(item, ".items.LiquidMetal");
	}

	private static boolean isTalentResource(String className) {
		return className != null && (className.contains(".items.scrolls.exotic.ScrollOfMetamorphosis")
				|| className.contains(".items.spells.TransformSpell")
				|| className.contains(".items.ScrollOfSublimation"));
	}

	private static boolean canAutoUseTalentResource(AgentMinState state, AgentMinState.ItemState item) {
		return canUseTalentResource(state, item);
	}

	private static boolean canUseTalentResource(AgentMinState state, AgentMinState.ItemState item) {
		if (state == null || item == null || item.className == null) {
			return false;
		}
		if (item.className.contains(".items.ScrollOfSublimation")) {
			return false;
		}
		if (!item.className.contains(".items.scrolls.exotic.ScrollOfMetamorphosis")
				&& !item.className.contains(".items.spells.TransformSpell")) {
			return false;
		}
		if (state.hero == null || state.hero.talentSlots == null) {
			return false;
		}
		for (AgentMinState.TalentSlotState slot : state.hero.talentSlots) {
			if (slot != null && !slot.placeholder && slot.maxPoints > 0) {
				return true;
			}
		}
		return false;
	}

	private static void logMetamorphHeadReadyOnce(AgentMinState state, AgentMinState.ItemState item, String actionName) {
		if (loggedMetamorphHeadReady
				|| state == null
				|| item == null
				|| !isTalentResource(item.className)
				|| item.className.contains(".items.ScrollOfSublimation")
				|| (!"READ".equals(actionName) && !"CAST".equals(actionName))) {
			return;
		}
		AgentMinState.TalentSlotState source = firstMetamorphSourceSlot(state);
		if (source == null) {
			return;
		}
		loggedMetamorphHeadReady = true;
		AgentMinRuntimeLog.log("metamorph item head ready: action=" + actionName
				+ " item=" + item.className
				+ " source=" + source.talentName
				+ " points=" + source.points);
	}

	private static AgentMinState.TalentSlotState firstMetamorphSourceSlot(AgentMinState state) {
		if (state == null || state.hero == null || state.hero.talentSlots == null) {
			return null;
		}
		for (AgentMinState.TalentSlotState slot : state.hero.talentSlots) {
			if (slot != null && !slot.placeholder && slot.maxPoints > 0) {
				return slot;
			}
		}
		return null;
	}

	private static boolean hasPath(AgentMinState.ItemState item, String part) {
		return item != null && item.className != null && item.className.contains(part);
	}

	private static boolean hasAction(AgentMinState.ItemState item, String action) {
		return item != null && item.actions != null && item.actions.contains(action);
	}

	private static float foodPriority(AgentMinState state) {
		if (state.hero.starving || state.hero.hungerLevel <= 150f) {
			return 0.88f;
		}
		if (state.hero.hungerLevel <= 260f) {
			return 0.42f;
		}
		return 0.08f;
	}

	private static float kind(AgentMinAction.Kind kind) {
		if (kind == null) return 0f;
		return (kind.ordinal() + 1) / (float)AgentMinAction.Kind.values().length;
	}

	private static float itemCategory(String className) {
		if (className == null) return 0f;
		String c = className.toLowerCase();
		if (c.contains(".weapon.")) return 0.2f;
		if (c.contains(".wands.")) return 0.4f;
		if (c.contains(".potions.")) return 0.6f;
		if (c.contains(".artifacts.")) return 0.8f;
		if (c.contains(".food.")) return 0.9f;
		return 1f;
	}

	private static float actionKind(String action) {
		if (action == null) return 0f;
		if (action.equals("DROP")) return 1f / 32f;
		if (action.equals("THROW")) return 2f / 32f;
		if (action.equals("EQUIP")) return 3f / 32f;
		if (action.equals("UNEQUIP")) return 4f / 32f;
		if (action.equals("DRINK")) return 5f / 32f;
		if (action.equals("READ")) return 6f / 32f;
		if (action.equals("EAT") || action.equals("eat")) return 7f / 32f;
		if (action.equals("ZAP")) return 8f / 32f;
		if (action.equals("CAST")) return 9f / 32f;
		if (action.equals("OPEN")) return 10f / 32f;
		if (action.equals("INSPECT")) return 11f / 32f;
		if (action.equals("PLANT")) return 12f / 32f;
		if (action.equals("PLANT_IN_BODY")) return 13f / 32f;
		if (action.equals("LIGHTTHROW")) return 14f / 32f;
		if (action.equals("LIGHT")) return 15f / 32f;
		if (action.equals("USE")) return 16f / 32f;
		if (action.equals("BLESS")) return 17f / 32f;
		if (action.equals("SNACK")) return 18f / 32f;
		if (action.equals("STEALTH")) return 19f / 32f;
		if (action.equals("ROOT")) return 20f / 32f;
		if (action.equals("ACTIVATE")) return 21f / 32f;
		if (action.equals("ACTION_INDICATOR")) return 22f / 32f;
		if (action.equals("ACTION_INDICATOR1")) return 23f / 32f;
		if (action.equals("APPLY")) return 24f / 32f;
		if (action.equals("AFFIX")) return 25f / 32f;
		if (action.equals("IMBUE")) return 26f / 32f;
		if (action.equals("TRANS")) return 27f / 32f;
		if (action.equals("BREW")) return 28f / 32f;
		if (action.equals("ENERGIZE")) return 29f / 32f;
		if (action.equals("SET")) return 30f / 32f;
		if (action.equals("RETURN")) return 31f / 32f;
		return 1f;
	}

	private static float itemActionHeadHint(String action) {
		if (action == null) {
			return 0f;
		}
		if ("THROW".equals(action) || "LIGHTTHROW".equals(action)) return 0.10f;
		if ("ZAP".equals(action)) return 0.15f;
		if ("DRINK".equals(action)) return 0.20f;
		if ("EAT".equals(action) || "eat".equals(action)) return 0.25f;
		if ("READ".equals(action) || "CAST".equals(action)) return 0.30f;
		if ("TALENT_UPGRADE".equals(action)) return 0.30f;
		if ("EQUIP".equals(action)) return 0.35f;
		if ("UNEQUIP".equals(action)) return 0.40f;
		if ("DROP".equals(action)) return 0.45f;
		if ("OPEN".equals(action)) return 0.50f;
		if ("INSPECT".equals(action)) return 0.55f;
		if ("PLANT".equals(action) || "PLANT_IN_BODY".equals(action)) return 0.60f;
		if ("BLESS".equals(action)) return 0.65f;
		if ("ACTIVATE".equals(action) || "USE".equals(action) || "ALLUSE".equals(action)) return 0.70f;
		if ("LIGHT".equals(action)) return 0.75f;
		if ("SNACK".equals(action) || "STEALTH".equals(action) || "ROOT".equals(action)) return 0.80f;
		if ("APPLY".equals(action) || "AFFIX".equals(action) || "IMBUE".equals(action) || "TRANS".equals(action)) return 0.85f;
		if ("BREW".equals(action) || "ENERGIZE".equals(action) || "SET".equals(action) || "RETURN".equals(action)) return 0.90f;
		return 1f;
	}

	private static float norm(float value, float max) {
		if (max <= 0) return 0f;
		return clamp(value / max);
	}

	private static float clamp(float value) {
		return Math.max(0f, Math.min(1f, value));
	}
}
