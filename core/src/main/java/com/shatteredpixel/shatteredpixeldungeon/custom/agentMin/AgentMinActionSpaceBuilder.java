package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

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

		addMovement(space, state);
		addWait(space, state);
		addAttacks(space, state);
		addPickUp(space, state);
		addStairs(space, state);
		addHealing(space, state);
		addThrowing(space, state);
		addWands(space, state);
		encode(space, state);
		return space;
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
			action.priority = 0.25f;
			add(space, action);
		}
	}

	private static void addWait(AgentMinActionSpace space, AgentMinState state) {
		AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.WAIT, "wait");
		action.fromCell = state.level.heroPos;
		action.targetCell = state.level.heroPos;
		action.priority = state.combat.visibleEnemies.isEmpty() ? 0.15f : 0.02f;
		add(space, action);
	}

	private static void addAttacks(AgentMinActionSpace space, AgentMinState state) {
		for (int i = 0; i < state.combat.visibleEnemies.size(); i++) {
			AgentMinState.MobCombatState mob = state.combat.visibleEnemies.get(i);
			AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.ATTACK, "attack_visible_enemy");
			action.fromCell = state.level.heroPos;
			action.targetCell = mob.pos;
			action.targetMobRow = i;
			action.priority = 0.55f + Math.max(0f, 1f - mob.hpRatio) * 0.25f;
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
				action.priority = 0.5f;
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
				AgentMinAction action = new AgentMinAction(AgentMinAction.Kind.THROW_WEAPON, "throw_weapon_at_enemy");
				action.fromCell = state.level.heroPos;
				action.targetCell = mob.pos;
				action.targetMobRow = m;
				action.itemRow = i;
				action.itemClassName = item.className;
				action.itemName = item.name;
				action.itemAction = "THROW";
				action.priority = mob.distanceToHero >= 2 ? 0.65f : 0.25f;
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
				action.priority = 0.6f;
				add(space, action);
			}
		}
	}

	private static void encode(AgentMinActionSpace space, AgentMinState state) {
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
			row[16] = action.kind == AgentMinAction.Kind.MOVE ? 1f : 0f;
			row[17] = action.kind == AgentMinAction.Kind.ATTACK ? 1f : 0f;
			row[18] = action.kind == AgentMinAction.Kind.DRINK_HEALING ? 1f : 0f;
			row[19] = action.kind == AgentMinAction.Kind.THROW_WEAPON ? 1f : 0f;
			row[20] = action.kind == AgentMinAction.Kind.ZAP_WAND ? 1f : 0f;
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

	private static boolean isWandLike(AgentMinState.ItemState item) {
		if (item == null || item.className == null) {
			return false;
		}
		return item.className.contains(".items.wands.") || item.className.endsWith(".MagesStaff");
	}

	private static boolean hasAction(AgentMinState.ItemState item, String action) {
		return item != null && item.actions != null && item.actions.contains(action);
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
		return 1f;
	}

	private static float actionKind(String action) {
		if (action.equals("DRINK")) return 0.25f;
		if (action.equals("THROW")) return 0.5f;
		if (action.equals("ZAP")) return 0.75f;
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
