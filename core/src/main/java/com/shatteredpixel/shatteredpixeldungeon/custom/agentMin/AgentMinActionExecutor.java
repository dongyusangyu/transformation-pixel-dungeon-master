package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.watabou.utils.Random;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class AgentMinActionExecutor {

	public static boolean execute(int actionId) {
		if (Dungeon.hero == null || Dungeon.level == null) {
			return false;
		}
		AgentMinState state = AgentMinStateBuilder.capture();
		AgentMinActionSpace space = AgentMinActionSpaceBuilder.build(state);
		AgentMinAction action = space.get(actionId);
		return execute(action, state);
	}

	public static boolean execute(AgentMinAction action, AgentMinState state) {
		if (action == null || !action.valid) {
			AgentMinRuntimeLog.log("invalid action");
			return false;
		}
		AgentMinHistoryTracker.recordChosenAction(action, state);
		return executeAction(action);
	}

	private static boolean executeAction(AgentMinAction action) {
		Hero hero = Dungeon.hero;
		switch (action.kind) {
			case ZERO_RANDOM_MOVE:
				return executeRandomVisibleMove(hero);
			case MOVE:
			case ATTACK:
			case PICK_UP:
			case USE_STAIRS:
				return action.targetCell >= 0 && handleHeroCell(hero, action.targetCell);
			case WAIT:
				AgentMinRewardTracker.onHeroWait();
				hero.rest(false);
				return true;
			case DRINK_HEALING:
				return executeItemAction(action, "DRINK", false);
			case EAT_FOOD:
				return executeItemAction(action, "EAT", false);
			case THROW_WEAPON:
				return executeItemAction(action, "THROW", true);
			case ZAP_WAND:
				return executeWandAction(action);
			default:
				return false;
		}
	}

	private static boolean executeRandomVisibleMove(Hero hero) {
		ArrayList<Integer> candidates = new ArrayList<>();
		if (Dungeon.level == null || Dungeon.level.heroFOV == null || Dungeon.level.passable == null) {
			return false;
		}
		int length = Math.min(Dungeon.level.length(), Math.min(Dungeon.level.heroFOV.length, Dungeon.level.passable.length));
		for (int cell = 0; cell < length; cell++) {
			if (cell != hero.pos
					&& Dungeon.level.heroFOV[cell]
					&& Dungeon.level.passable[cell]
					&& Dungeon.level.findMob(cell) == null) {
				candidates.add(cell);
			}
		}
		if (candidates.isEmpty()) {
			return false;
		}
		int target = Random.element(candidates);
		int step = stepToward(hero.pos, target);
		if (canStep(hero, step)) {
			return handleHeroCell(hero, step);
		}
		ArrayList<Integer> adjacent = new ArrayList<>();
		int width = Dungeon.level.width();
		int[] offsets = new int[]{-width - 1, -width, -width + 1, -1, 1, width - 1, width, width + 1};
		for (int offset : offsets) {
			int cell = hero.pos + offset;
			if (canStep(hero, cell)) {
				adjacent.add(cell);
			}
		}
		return !adjacent.isEmpty() && handleHeroCell(hero, Random.element(adjacent));
	}

	private static boolean handleHeroCell(Hero hero, int cell) {
		if (!hero.handle(cell)) {
			return false;
		}
		hero.next();
		return true;
	}

	private static boolean executeWandAction(AgentMinAction action) {
		if (action == null || action.targetCell < 0) {
			return false;
		}
		ArrayList<Item> items = liveItems();
		if (action.itemRow < 0 || action.itemRow >= items.size()) {
			AgentMinRuntimeLog.log("wand action has invalid item row=" + action.itemRow);
			return false;
		}
		Item item = items.get(action.itemRow);
		if (!item.actions(Dungeon.hero).contains("ZAP")) {
			AgentMinRuntimeLog.log("ZAP unavailable on " + item.getClass().getSimpleName());
			return false;
		}
		try {
			if (Dungeon.level != null && Dungeon.level.distance(Dungeon.hero.pos, action.targetCell) <= 1) {
				AgentMinRewardTracker.onAdjacentRangedResourceUse(action.targetCell);
			}
			item.execute(Dungeon.hero, "ZAP");
			Field zapperField = Wand.class.getDeclaredField("zapper");
			zapperField.setAccessible(true);
			CellSelector.Listener zapper = (CellSelector.Listener)zapperField.get(null);
			if (zapper == null) {
				AgentMinRuntimeLog.log("wand zapper listener missing");
				return false;
			}
			zapper.onSelect(action.targetCell);
			return true;
		} catch (Exception e) {
			AgentMinRuntimeLog.log("failed to execute wand action: " + e.getMessage());
			return false;
		}
	}

	private static int stepToward(int from, int to) {
		int width = Dungeon.level.width();
		int fromX = from % width;
		int fromY = from / width;
		int toX = to % width;
		int toY = to / width;
		return from + Integer.signum(toX - fromX) + Integer.signum(toY - fromY) * width;
	}

	private static boolean canStep(Hero hero, int cell) {
		return cell >= 0
				&& cell < Dungeon.level.length()
				&& Dungeon.level.heroFOV[cell]
				&& Dungeon.level.passable[cell]
				&& Dungeon.level.findMob(cell) == null
				&& cell != hero.pos;
	}

	private static boolean executeItemAction(AgentMinAction action, String expectedAction, boolean targetedThrow) {
		ArrayList<Item> items = liveItems();
		if (action.itemRow < 0 || action.itemRow >= items.size()) {
			AgentMinRuntimeLog.log("item action has invalid item row=" + action.itemRow);
			return false;
		}
		Item item = items.get(action.itemRow);
		if (!item.actions(Dungeon.hero).contains(expectedAction)) {
			AgentMinRuntimeLog.log("item action unavailable: " + expectedAction + " on " + item.getClass().getSimpleName());
			return false;
		}
		if (targetedThrow) {
			if (action.targetCell < 0) {
				return false;
			}
			if (Dungeon.level != null && Dungeon.level.distance(Dungeon.hero.pos, action.targetCell) <= 1) {
				AgentMinRewardTracker.onAdjacentRangedResourceUse(action.targetCell);
			}
			item.cast(Dungeon.hero, action.targetCell);
		} else {
			item.execute(Dungeon.hero, expectedAction);
		}
		return true;
	}

	private static ArrayList<Item> liveItems() {
		ArrayList<Item> items = new ArrayList<>();
		Hero hero = Dungeon.hero;
		addIfNotNull(items, hero.belongings.weapon());
		addIfNotNull(items, hero.belongings.armor());
		addIfNotNull(items, hero.belongings.artifact());
		addIfNotNull(items, hero.belongings.misc());
		addIfNotNull(items, hero.belongings.ring());
		addIfNotNull(items, hero.belongings.secondWep());
		items.addAll(hero.belongings.backpack.items);
		return items;
	}

	private static void addIfNotNull(ArrayList<Item> items, Item item) {
		if (item != null) {
			items.add(item);
		}
	}
}
