package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import java.util.ArrayList;

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

	public static AgentMinActionSpace build(AgentMinState state) {
		AgentMinActionSpace space = new AgentMinActionSpace();
		if (state == null || state.level.width <= 0 || state.level.height <= 0) {
			return space;
		}

		addZeroRandomMove(space, state);
		addMovement(space, state);
		addWait(space, state);
		addAttacks(space, state);
		addPickUp(space, state);
		addStairs(space, state);
		addHealing(space, state);
		addFood(space, state);
		addThrowing(space, state);
		addWands(space, state);
		encode(space, state);
		return space;
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

	private static void addPickUp(AgentMinActionSpace space, AgentMinState state) {
		for (AgentMinState.CellEntityState item : state.level.visibleItems) {
			if (item.pos == state.level.heroPos
					&& !AgentMinRewardTracker.isRecentPickupFailure(item.className, item.image, item.pos)) {
				AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.PICK_UP, "pick_up_here");
				action.fromCell = state.level.heroPos;
				action.targetCell = state.level.heroPos;
				action.priority = 0.9f;
				add(space, action);
				return;
			}
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
		if (state.level.heroPos == state.level.entrance) {
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.USE_STAIRS, "ascend");
			action.fromCell = state.level.heroPos;
			action.targetCell = state.level.entrance;
			action.stairMode = AgentMinAction.StairMode.ASCEND;
			action.priority = 0.1f;
			add(space, action);
		}
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
				action.itemAction = "DRINK";
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
			action.itemAction = "EAT";
			action.priority = foodPriority(state);
			add(space, action);
			return;
		}
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
				action.itemAction = "THROW";
				float line = lineQuality(state, mob.pos, Ballistica.PROJECTILE);
				action.priority = clamp((mob.distanceToHero >= 3 ? 0.48f : 0.16f)
						+ mob.rangedValue * 0.22f
						+ mob.killChance * 0.18f
						+ line * 0.12f);
				add(space, action);
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
				action.itemAction = "ZAP";
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
		}
	}

	private static void add(AgentMinActionSpace space, AgentMinAction action) {
		if (space.actions.size() >= AgentMinActionSpace.MAX_ACTIONS) {
			return;
		}
		action.actionId = space.actions.size();
		space.actions.add(action);
	}

	private static boolean canMoveTo(AgentMinState state, int cell) {
		int code = state.level.visibleMap != null && cell < state.level.visibleMap.length ? state.level.visibleMap[cell] : 0;
		if (code == AgentMinState.CELL_UNKNOWN && state.level.exploredMap != null && cell < state.level.exploredMap.length) {
			code = state.level.exploredMap[cell];
		}
		return code != AgentMinState.CELL_UNKNOWN
				&& (code & AgentMinState.FLAG_SOLID) == 0
				&& ((code & AgentMinState.FLAG_PASSABLE) != 0 || (code & AgentMinState.FLAG_AVOID) != 0)
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
				return (action.kind == AgentMinAction.Kind.DRINK_HEALING || action.kind == AgentMinAction.Kind.EAT_FOOD || action.kind == AgentMinAction.Kind.ZAP_WAND) ? 1f : 0.05f;
			case "CONSUME":
				return (action.kind == AgentMinAction.Kind.EAT_FOOD || action.kind == AgentMinAction.Kind.DRINK_HEALING) ? 1f : 0.05f;
			case "PICKUP":
				return action.kind == AgentMinAction.Kind.PICK_UP ? 1f : 0.05f;
			case "UNLOCK":
				return action.kind == AgentMinAction.Kind.MOVE && AgentMinExplorationTracker.isDoorCell(state, action.targetCell) ? 0.7f : 0.05f;
			case "DROP":
				return 0.05f;
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
		if (action.equals("DRINK")) return 0.25f;
		if (action.equals("EAT")) return 0.5f;
		if (action.equals("THROW")) return 0.75f;
		if (action.equals("ZAP")) return 1f;
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
