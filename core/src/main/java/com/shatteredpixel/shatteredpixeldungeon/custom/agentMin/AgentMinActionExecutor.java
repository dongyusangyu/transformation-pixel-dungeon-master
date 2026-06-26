package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TransformSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.TalentCatalog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator1;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class AgentMinActionExecutor {

	private static boolean lastFailureConsumesTime = true;

	public static boolean lastFailureConsumesTime() {
		return lastFailureConsumesTime;
	}

	public static boolean execute(int actionId) {
		lastFailureConsumesTime = true;
		if (Dungeon.hero == null || Dungeon.level == null) {
			return false;
		}
		AgentMinState state = AgentMinStateBuilder.capture();
		AgentMinActionSpace space = AgentMinActionSpaceBuilder.build(state);
		AgentMinAction action = space.get(actionId);
		return execute(action, state);
	}

	public static boolean execute(AgentMinAction action, AgentMinState state) {
		lastFailureConsumesTime = true;
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
			case UNLOCK:
			case USE_STAIRS:
				if (isBlockedFloorOneAscend(action, hero)) {
					return failWithoutTime("blocked floor one entrance ascend");
				}
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
			case ITEM_ACTION:
				return executeIndexedItemAction(action);
			case ALCHEMY:
				return action.targetCell >= 0 && handleHeroCell(hero, action.targetCell);
			case ACTION_INDICATOR:
				return executeActionIndicator(action);
			case MONITOR_CELL:
				return executeMonitorCell(action);
			case MONITOR_ITEM:
				return executeMonitorItem(action);
			case MONITOR_OPTION:
				return executeMonitorOption(action);
			case TALENT_UPGRADE:
				return executeTalentUpgrade(action);
			default:
				return false;
		}
	}

	private static boolean executeTalentUpgrade(AgentMinAction action) {
		Hero hero = Dungeon.hero;
		if (hero == null || action == null) {
			return failTalentUpgrade("talent upgrade action missing hero or talent");
		}
		Talent talent = AgentMinMetamorphPlanner.chooseUpgrade(hero, AgentMinPolicyContext.talentUpgradeTargetTalent());
		if (talent == null && action.talentName != null) {
			try {
				talent = Talent.valueOf(action.talentName);
			} catch (IllegalArgumentException e) {
				return failTalentUpgrade("talent upgrade action has invalid talent=" + action.talentName);
			}
		}
		if (talent == null) {
			return failTalentUpgrade("talent upgrade has no available candidate");
		}
		int tier = talent.tier();
		if (!AgentMinMetamorphPlanner.tierAllowed(hero, tier)) {
			return failTalentUpgrade("talent upgrade tier locked tier=" + tier + " hero_lvl=" + hero.lvl);
		}
		if (tier <= 0 || hero.talentPointsAvailable(tier) <= 0) {
			return failTalentUpgrade("talent upgrade has no available point tier=" + tier);
		}
		if (!hero.canUpgradeTalent(talent)) {
			return failTalentUpgrade("talent upgrade cannot upgrade " + talent.name());
		}
		hero.upgradeTalent(talent);
		AgentMinRewardTracker.onTalentUpgradeDecision(true);
		AgentMinRuntimeLog.log("agentmin talent head executed: " + talent.name()
				+ " tier=" + tier
				+ " slot=" + action.talentSlot
				+ " points=" + (action.talentPoints + 1) + "/" + action.talentMaxPoints);
		return true;
	}

	private static boolean failTalentUpgrade(String reason) {
		lastFailureConsumesTime = false;
		AgentMinRewardTracker.onTalentUpgradeDecision(false);
		AgentMinRuntimeLog.log(reason);
		return false;
	}

	private static boolean executeMonitorItem(AgentMinAction action) {
		WndBag.ItemSelector selector = GameScene.agentMinActiveItemSelector();
		if (selector == null) {
			return failMonitorWithoutTime("monitor item selector missing");
		}
		ArrayList<Item> items = liveItems();
		int row = AgentMinPolicyContext.monitorItemRow();
		if (row < 0) {
			row = action.itemRow;
		}
		if (row < 0 || row >= items.size()) {
			int fallbackRow = firstSelectableMonitorItem(selector, action, items);
			if (fallbackRow < 0) {
				return cancelMonitorItem("monitor item row empty or out of range row=" + row);
			}
			AgentMinRuntimeLog.log("monitor item row out of range row=" + row + " fallback=" + fallbackRow);
			row = fallbackRow;
		}
		Item selected = items.get(row);
		if (selected == null || !selector.itemSelectable(selected) || !monitorItemMatches(action, selected)) {
			int fallbackRow = firstSelectableMonitorItem(selector, action, items);
			if (fallbackRow < 0) {
				return cancelMonitorItem("monitor item row not selectable row=" + row);
			}
			AgentMinRuntimeLog.log("monitor item row not selectable row=" + row + " fallback=" + fallbackRow);
			selected = items.get(fallbackRow);
		}
		if (GameScene.agentMinSelectItem(selected)) {
			// A selector can accept a row but leave no spent turn and no follow-up
			// monitor (for example after an item became stale).  Close that terminal
			// UI state here so the next AgentMin decision remains runnable.
			boolean stillReady = Dungeon.hero != null && Dungeon.hero.ready;
			boolean followUpMonitor = GameScene.agentMinCellSelectorActive()
					|| GameScene.agentMinItemSelectorActive() || GameScene.agentMinOptionsActive();
			if (stillReady && !followUpMonitor) {
				return cancelMonitorItem("monitor item selection had no effect: "
						+ selected.getClass().getSimpleName());
			}
			AgentMinRewardTracker.onMonitorItemSelection(true);
			return true;
		}
		return cancelMonitorItem("monitor item selection failed: " + selected.getClass().getSimpleName());
	}

	private static boolean cancelMonitorItem(String reason) {
		lastFailureConsumesTime = false;
		boolean cancelled = GameScene.agentMinAbortMonitors();
		AgentMinRewardTracker.onMonitorItemSelection(false);
		AgentMinRuntimeLog.log(reason + (cancelled ? " cancelled=true" : " cancelled=false"));
		return cancelled;
	}

	private static boolean executeMonitorCell(AgentMinAction action) {
		int targetCell = policyMonitorCell(action == null ? -1 : action.targetCell, true);
		if (targetCell < 0) {
			GameScene.agentMinAbortMonitors();
			AgentMinRewardTracker.onMonitorCellSelection(false, Dungeon.hero == null ? -1 : Dungeon.hero.pos);
			return failMonitorWithoutTime("monitor cell action has invalid target");
		}
		if (GameScene.agentMinSelectCell(targetCell)) {
			AgentMinRewardTracker.onMonitorCellSelection(true, targetCell);
			return true;
		}
		GameScene.agentMinAbortMonitors();
		AgentMinRewardTracker.onMonitorCellSelection(false, targetCell);
		return failMonitorWithoutTime("monitor cell selection failed target=" + targetCell);
	}

	private static boolean executeMonitorOption(AgentMinAction action) {
		if (action == null) {
			return failMonitorWithoutTime("monitor option action missing");
		}
		int count = GameScene.agentMinOptionCount();
		int index = AgentMinPolicyContext.monitorOptionIndex();
		if (index < 0) {
			index = action.optionIndex;
		}
		if (index < 0 || index >= count) {
			boolean cancelled = GameScene.agentMinAbortMonitors();
			AgentMinRewardTracker.onMonitorOptionSelection(false);
			return failMonitorWithoutTime("monitor option index invalid index=" + index + " count=" + count
					+ (cancelled ? " cancelled=true" : " cancelled=false"));
		}
		if (GameScene.agentMinSelectOption(index)) {
			AgentMinRewardTracker.onMonitorOptionSelection(true);
			return true;
		}
		boolean cancelled = GameScene.agentMinAbortMonitors();
		AgentMinRewardTracker.onMonitorOptionSelection(false);
		return failMonitorWithoutTime("monitor option selection failed index=" + index
				+ (cancelled ? " cancelled=true" : " cancelled=false"));
	}

	private static boolean monitorItemMatches(AgentMinAction action, Item item) {
		if (action == null || item == null) {
			return false;
		}
		boolean classMatches = action.itemClassName == null || action.itemClassName.equals(item.getClass().getName());
		boolean nameMatches = action.itemName == null || action.itemName.equals(item.name());
		return classMatches && nameMatches;
	}

	private static int firstSelectableMonitorItem(WndBag.ItemSelector selector, AgentMinAction action, ArrayList<Item> items) {
		if (selector == null || items == null) {
			return -1;
		}
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			if (item != null && selector.itemSelectable(item) && monitorItemMatches(action, item)) {
				return i;
			}
		}
		return -1;
	}

	private static boolean failMonitorWithoutTime(String reason) {
		lastFailureConsumesTime = false;
		AgentMinRuntimeLog.log(reason);
		return false;
	}

	private static int policyMonitorCell(int fallbackCell, boolean visibleOnly) {
		int index = AgentMinPolicyContext.monitorCellIndex();
		if (index < 0) {
			return fallbackCell;
		}
		int cell = monitorCellIndexToCell(index);
		if (isValidMonitorCell(cell, visibleOnly)) {
			return cell;
		}
		AgentMinRewardTracker.onMonitorCellSelection(false, cell);
		return fallbackCell;
	}

	private static int monitorCellIndexToCell(int index) {
		Hero hero = Dungeon.hero;
		if (hero == null || Dungeon.level == null || index < 0 || index >= AgentMinActionSpace.MONITOR_CELL_SIZE) {
			return -1;
		}
		int size = AgentMinEncodedState.LOCAL_MAP_SIZE;
		int radius = size / 2;
		int width = Dungeon.level.width();
		int height = Dungeon.level.height();
		int x = hero.pos % width + index % size - radius;
		int y = hero.pos / width + index / size - radius;
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return -1;
		}
		int cell = y * width + x;
		return cell >= 0 && cell < Dungeon.level.length() ? cell : -1;
	}

	private static boolean isValidMonitorCell(int cell, boolean visibleOnly) {
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()) {
			return false;
		}
		if (visibleOnly && (Dungeon.level.heroFOV == null || cell >= Dungeon.level.heroFOV.length || !Dungeon.level.heroFOV[cell])) {
			return false;
		}
		return Dungeon.level.solid == null || cell >= Dungeon.level.solid.length || !Dungeon.level.solid[cell];
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

	private static boolean isBlockedFloorOneAscend(AgentMinAction action, Hero hero) {
		return action != null
				&& action.kind == AgentMinAction.Kind.USE_STAIRS
				&& action.stairMode == AgentMinAction.StairMode.ASCEND
				&& hero != null
				&& Dungeon.level != null
				&& Dungeon.depth == 1
				&& Dungeon.branch == 0
				&& hero.pos == Dungeon.level.entrance()
				&& action.targetCell == Dungeon.level.entrance();
	}

	private static boolean failWithoutTime(String reason) {
		lastFailureConsumesTime = false;
		AgentMinRewardTracker.onBlockedFloorOneAscend();
		AgentMinRuntimeLog.log(reason);
		return false;
	}

	private static boolean executeActionIndicator(AgentMinAction action) {
		if (Dungeon.hero == null || !Dungeon.hero.ready) {
			return false;
		}
		if ("ACTION_INDICATOR1".equals(action.itemAction)) {
			if (ActionIndicator1.action == null) {
				return false;
			}
			ActionIndicator1.action.doAction();
			return true;
		}
		if (ActionIndicator.action == null || !ActionIndicator.action.usable()) {
			return false;
		}
		ActionIndicator.action.doAction();
		return true;
	}

	private static boolean executeWandAction(AgentMinAction action) {
		if (action == null || action.targetCell < 0) {
			return false;
		}
		int targetCell = policyMonitorCell(action.targetCell, true);
		if (targetCell < 0) {
			return false;
		}
		if (Dungeon.hero == null || Dungeon.level == null || targetCell == Dungeon.hero.pos) {
			AgentMinRuntimeLog.log("wand action rejected invalid/self target=" + targetCell);
			return false;
		}
		ArrayList<Item> items = liveItems();
		if (action.itemRow < 0 || action.itemRow >= items.size()) {
			AgentMinRuntimeLog.log("wand action has invalid item row=" + action.itemRow);
			return false;
		}
		Item item = items.get(action.itemRow);
		String resolvedAction = resolveItemAction(item, action, "ZAP");
		if (!"ZAP".equals(resolvedAction)) {
			AgentMinRuntimeLog.log("ZAP unavailable on " + item.getClass().getSimpleName());
			return false;
		}
		final Hero hero = Dungeon.hero;
		final Item wand = item;
		final int selectedTarget = targetCell;
		try {
			if (Dungeon.level != null && Dungeon.level.distance(hero.pos, selectedTarget) <= 1) {
				AgentMinRewardTracker.onAdjacentRangedResourceUse(targetCell);
			}
			// Wand.execute opens a CellSelector and measures its prompt text. That is
			// UI work, so it must execute on the render thread just like a player's
			// click. The actual target is still the head-selected map position.
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					if (Dungeon.hero != hero || !hero.isAlive() || Dungeon.level == null) {
						return;
					}
					try {
						wand.execute(hero, "ZAP");
						if (!GameScene.agentMinSelectCell(selectedTarget)) {
							GameScene.agentMinAbortMonitors();
							AgentMinRewardTracker.onActionFailed();
							AgentMinRuntimeLog.log("wand action did not open a target selector target=" + selectedTarget);
							hero.rest(false);
						}
					} catch (Throwable t) {
						GameScene.agentMinAbortMonitors();
						AgentMinRewardTracker.onActionFailed();
						AgentMinRuntimeLog.log("wand render action failed: " + t.getMessage());
						hero.rest(false);
					}
				}
			});
			return true;
		} catch (Exception e) {
			GameScene.agentMinAbortMonitors();
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
		String resolvedAction = resolveItemAction(item, action, expectedAction);
		if (!expectedAction.equals(resolvedAction)) {
			AgentMinRuntimeLog.log("item action unavailable: " + expectedAction + " on " + item.getClass().getSimpleName());
			return false;
		}
		if (targetedThrow) {
			int targetCell = policyMonitorCell(action.targetCell, true);
			if (targetCell < 0) {
				return false;
			}
			float rewardBefore = AgentMinRewardTracker.pendingReward();
			if (Dungeon.level != null && Dungeon.level.distance(Dungeon.hero.pos, targetCell) <= 1) {
				AgentMinRewardTracker.onAdjacentRangedResourceUse(targetCell);
			}
			item.cast(Dungeon.hero, targetCell);
			AgentMinRewardTracker.onThrownResourceResolved(item, targetCell, rewardBefore);
		} else {
			item.execute(Dungeon.hero, resolvedAction);
		}
		return true;
	}

	private static boolean executeIndexedItemAction(AgentMinAction action) {
		ArrayList<Item> items = liveItems();
		if (action.itemRow < 0 || action.itemRow >= items.size()) {
			AgentMinRuntimeLog.log("indexed item action has invalid item row=" + action.itemRow);
			return false;
		}
		Item item = items.get(action.itemRow);
		String resolvedAction = resolveItemAction(item, action, null);
		if (resolvedAction == null) {
			AgentMinRuntimeLog.log("indexed item action unavailable on " + item.getClass().getSimpleName());
			return false;
		}
		if (isSublimationResourceAction(item, resolvedAction)) {
			return executeAgentMinSublimation((ScrollOfSublimation)item);
		}
		if (isMetamorphResourceAction(item, resolvedAction)) {
			return executeAgentMinMetamorph(item);
		}
		if (isUpgradeResourceAction(item, resolvedAction)) {
			return executeAgentMinUpgrade((ScrollOfUpgrade)item);
		}
		if (isTargetedThrowAction(resolvedAction)) {
			return executeTargetedThrowAction(item, resolvedAction, action.targetCell);
		}
		AgentMinRewardTracker.EquipmentActionSnapshot equipmentBefore =
				("EQUIP".equals(resolvedAction) || "UNEQUIP".equals(resolvedAction))
						? AgentMinRewardTracker.equipmentActionSnapshot(item)
						: null;
		if ("THROW".equals(resolvedAction)) {
			int targetCell = policyMonitorCell(action.targetCell, true);
			if (targetCell < 0) {
				return false;
			}
			float rewardBefore = AgentMinRewardTracker.pendingReward();
			item.cast(Dungeon.hero, targetCell);
			AgentMinRewardTracker.onThrownResourceResolved(item, targetCell, rewardBefore);
		} else if ("EQUIP".equals(resolvedAction) && item instanceof Armor) {
			if (!executeAgentMinArmorEquip((Armor)item)) {
				return false;
			}
		} else {
			try {
				item.execute(Dungeon.hero, resolvedAction);
			} catch (RuntimeException e) {
				if (isActorThreadTextMeasureError(e)) {
					lastFailureConsumesTime = false;
					boolean cancelled = GameScene.agentMinCancelItemSelection();
					AgentMinRewardTracker.onItemUiBlocked();
					AgentMinRuntimeLog.log("equipment/item UI text blocked on actor thread: "
							+ item.getClass().getSimpleName() + " " + resolvedAction
							+ (cancelled ? " cancelled=true" : " cancelled=false"));
					return false;
				}
				throw e;
			}
		}
		if (equipmentBefore != null) {
			AgentMinRewardTracker.onEquipmentAction(resolvedAction, equipmentBefore, item);
		}
		if (isTalentResourceItem(item) && isTalentResourceAction(resolvedAction)) {
			if (!consumeAgentMinTalentResource(item)) {
				return false;
			}
			AgentMinRewardTracker.onTalentResourceUsed(item);
		}
		return true;
	}

	private static String resolveItemAction(Item item, AgentMinAction action, String fallbackAction) {
		ArrayList<String> actions = item.actions(Dungeon.hero);
		if (action.itemActionIndex >= 0 && action.itemActionIndex < actions.size()) {
			return actions.get(action.itemActionIndex);
		}
		if (action.itemAction != null && actions.contains(action.itemAction)) {
			return action.itemAction;
		}
		return fallbackAction != null && actions.contains(fallbackAction) ? fallbackAction : null;
	}

	private static boolean executeTargetedThrowAction(Item item, String resolvedAction, int targetCell) {
		targetCell = policyMonitorCell(targetCell, true);
		if (item == null || targetCell < 0) {
			return false;
		}
		try {
			float rewardBefore = AgentMinRewardTracker.pendingReward();
			if (Dungeon.level != null && Dungeon.level.distance(Dungeon.hero.pos, targetCell) <= 1) {
				AgentMinRewardTracker.onAdjacentRangedResourceUse(targetCell);
			}
			if ("LIGHTTHROW".equals(resolvedAction)) {
				item.execute(Dungeon.hero, resolvedAction);
				Field throwerField = Item.class.getDeclaredField("thrower");
				throwerField.setAccessible(true);
				CellSelector.Listener thrower = (CellSelector.Listener)throwerField.get(null);
				if (thrower == null) {
					return false;
				}
				thrower.onSelect(targetCell);
			} else {
				item.cast(Dungeon.hero, targetCell);
			}
			AgentMinRewardTracker.onThrownResourceResolved(item, targetCell, rewardBefore);
			return true;
		} catch (Exception e) {
			AgentMinRuntimeLog.log("failed to execute targeted item action: " + resolvedAction + " on "
					+ item.getClass().getSimpleName() + ": " + e.getMessage());
			return false;
		}
	}

	private static boolean isTargetedThrowAction(String action) {
		return "THROW".equals(action) || "LIGHTTHROW".equals(action);
	}

	private static boolean executeAgentMinArmorEquip(Armor armor) {
		Hero hero = Dungeon.hero;
		if (hero == null || armor == null || hero.belongings == null || hero.belongings.backpack == null) {
			return false;
		}
		if (armor.isEquipped(hero) || (armor.cursedKnown && armor.cursed)) {
			return false;
		}
		Armor oldArmor = hero.belongings.armor();
		if (oldArmor != null && oldArmor.cursed) {
			return false;
		}
		if (!hero.belongings.backpack.contains(armor)) {
			return false;
		}

		armor.detach(hero.belongings.backpack);
		if (oldArmor != null && !oldArmor.doUnequip(hero, true, false)) {
			armor.collect(hero.belongings.backpack);
			return false;
		}

		hero.belongings.armor = armor;
		armor.cursedKnown = true;
		if (hero.sprite instanceof HeroSprite) {
			((HeroSprite)hero.sprite).updateArmor();
		}
		armor.activate(hero);
		Talent.onItemEquipped(hero, armor);
		autoTransferSeal(oldArmor, armor);
		hero.spend(1f);
		hero.next();
		armor.updateQuickslot();
		AgentMinRuntimeLog.log("agentmin armor equip: " + armor.getClass().getSimpleName());
		return true;
	}

	private static void autoTransferSeal(Armor oldArmor, Armor newArmor) {
		if (oldArmor == null || newArmor == null || newArmor.checkSeal() != null) {
			return;
		}
		BrokenSeal seal = oldArmor.checkSeal();
		if (seal == null || (newArmor.cursed && (seal.getGlyph() == null || !seal.getGlyph().curse()))) {
			return;
		}
		oldArmor.detachSeal();
		newArmor.affixSeal(seal);
		newArmor.updateQuickslot();
		AgentMinRuntimeLog.log("agentmin armor seal transferred");
	}

	private static boolean isActorThreadTextMeasureError(Throwable t) {
		while (t != null) {
			if (t.getMessage() != null && t.getMessage().contains("Text measured from the actor thread")) {
				return true;
			}
			t = t.getCause();
		}
		return false;
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
		for (Item item : hero.belongings.backpack.items) {
			addLiveItemTree(items, item);
		}
		return items;
	}

	private static void addLiveItemTree(ArrayList<Item> items, Item item) {
		addIfNotNull(items, item);
		if (item instanceof Bag) {
			for (Item child : ((Bag)item).items) {
				addLiveItemTree(items, child);
			}
		}
	}

	private static void addIfNotNull(ArrayList<Item> items, Item item) {
		if (item != null) {
			items.add(item);
		}
	}

	private static boolean isTalentResourceItem(Item item) {
		if (item == null) {
			return false;
		}
		String className = item.getClass().getName();
		return className.contains(".items.scrolls.exotic.ScrollOfMetamorphosis")
				|| className.contains(".items.spells.TransformSpell")
				|| className.contains(".items.ScrollOfSublimation");
	}

	private static boolean isTalentResourceAction(String action) {
		return "READ".equals(action) || "CAST".equals(action);
	}

	private static boolean isMetamorphResourceAction(Item item, String action) {
		return item != null
				&& (item instanceof ScrollOfMetamorphosis || item instanceof TransformSpell)
				&& isTalentResourceAction(action);
	}

	private static boolean isSublimationResourceAction(Item item, String action) {
		return item instanceof ScrollOfSublimation && isTalentResourceAction(action);
	}

	private static boolean isUpgradeResourceAction(Item item, String action) {
		return item instanceof ScrollOfUpgrade && "READ".equals(action);
	}

	private static boolean executeAgentMinUpgrade(ScrollOfUpgrade scroll) {
		Hero hero = Dungeon.hero;
		Item target = chooseUpgradeTarget(hero, scroll);
		if (target == null) {
			AgentMinRuntimeLog.log("agentmin upgrade skipped: no weapon/armor/wand/ring target");
			return false;
		}
		if (!scroll.isKnown()) {
			scroll.identify();
		}
		prepareItemContext(scroll);
		scroll.readAnimation();
		Item upgraded = scroll.upgradeItem(target);
		if (!consumeAgentMinItem(scroll, "upgrade resource")) {
			return false;
		}
		AgentMinRuntimeLog.log("agentmin upgrade: " + target.getClass().getSimpleName()
				+ " -> " + upgraded.getClass().getSimpleName() + " level=" + upgraded.level());
		return true;
	}

	private static Item chooseUpgradeTarget(Hero hero, Item resource) {
		Item best = null;
		float bestScore = Float.NEGATIVE_INFINITY;
		for (Item candidate : liveItems()) {
			if (candidate == null || candidate == resource || !candidate.isUpgradable() || !isUpgradeCandidate(candidate)) {
				continue;
			}
			float score = upgradeCandidateScore(hero, candidate);
			if (score > bestScore) {
				bestScore = score;
				best = candidate;
			}
		}
		return best;
	}

	private static boolean isUpgradeCandidate(Item item) {
		return item instanceof Weapon || item instanceof Armor || item instanceof Wand || item instanceof Ring;
	}

	private static float upgradeCandidateScore(Hero hero, Item item) {
		float score = item.buffedLvl() * 3f + item.level() * 2f;
		if (item instanceof Weapon) {
			score += 44f + strengthScore(hero, ((Weapon)item).STRReq()) + (item.isEquipped(hero) ? 14f : 0f);
		} else if (item instanceof Armor) {
			score += 42f + ((Armor)item).tier * 3f + strengthScore(hero, ((Armor)item).STRReq()) + (item.isEquipped(hero) ? 13f : 0f);
		} else if (item instanceof Wand) {
			score += 36f;
		} else if (item instanceof Ring) {
			score += 34f + (item.isEquipped(hero) ? 9f : 0f);
		}
		if (item.cursedKnown && item.cursed) {
			score -= 6f;
		}
		return score;
	}

	private static float strengthScore(Hero hero, int requirement) {
		if (hero == null || requirement <= 0) {
			return 0f;
		}
		int margin = hero.STR() - requirement;
		return margin >= 0 ? Math.min(8f, 3f + margin) : Math.max(-18f, margin * 3f);
	}

	private static void prepareItemContext(Item item) {
		try {
			Field curUser = Item.class.getDeclaredField("curUser");
			curUser.setAccessible(true);
			curUser.set(null, Dungeon.hero);
			Field curItem = Item.class.getDeclaredField("curItem");
			curItem.setAccessible(true);
			curItem.set(null, item);
		} catch (Exception e) {
			item.execute(Dungeon.hero, "__AGENTMIN_CONTEXT__");
		}
	}

	private static boolean executeAgentMinMetamorph(Item item) {
		Hero hero = Dungeon.hero;
		Talent.TalentType targetType = AgentMinMetamorphPlanner.typeFromOrdinal(AgentMinPolicyContext.metamorphTargetType());
		AgentMinMetamorphPlanner.Choice choice = AgentMinMetamorphPlanner.choose(
				hero,
				AgentMinPolicyContext.metamorphSourceTalent(),
				targetType,
				AgentMinPolicyContext.metamorphTargetTalent());
		if (choice == null) {
			AgentMinRuntimeLog.log("agentmin metamorph skipped: no available non-placeholder source talent");
			return false;
		}
		if (!consumeAgentMinTalentResource(item)) {
			return false;
		}
		if (!replaceTalent(hero, choice.oldTalent, choice.newTalent, choice.tier)) {
			AgentMinRuntimeLog.log("agentmin metamorph failed: talent map did not contain " + choice.oldTalent);
			return false;
		}
		hero.spendAndNext(1f);
		Statistics.metamorphosis++;
		Badges.validateFreemanUnlock();
		Talent.onTalentUpgraded(hero, hero.hasTalent(choice.newTalent) ? choice.newTalent : null);
		AgentMinRewardTracker.onTalentResourceUsed(item);
		AgentMinRewardTracker.onTalentUpgradeDecision(true);
		AgentMinRuntimeLog.log("agentmin metamorph head executed: " + choice.oldTalent.name()
				+ " -> " + choice.newTalent.name()
				+ " tier=" + choice.tier
				+ " type=" + choice.targetType
				+ " source_points=" + choice.points
				+ " preferred_source=" + AgentMinPolicyContext.metamorphSourceTalent()
				+ " preferred=" + AgentMinPolicyContext.metamorphTargetTalent());
		return true;
	}

	private static boolean executeAgentMinSublimation(ScrollOfSublimation scroll) {
		Hero hero = Dungeon.hero;
		if (hero == null || scroll == null) {
			return false;
		}
		int tier = AgentMinMetamorphPlanner.sublimationTier(scroll.type());
		if (!AgentMinMetamorphPlanner.tierAllowed(hero, tier)) {
			lastFailureConsumesTime = false;
			AgentMinRuntimeLog.log("agentmin sublimation skipped: tier locked type=" + scroll.type()
					+ " tier=" + tier + " hero_lvl=" + hero.lvl);
			AgentMinRewardTracker.onTalentUpgradeDecision(false);
			return false;
		}
		Talent talent = AgentMinMetamorphPlanner.chooseSublimation(scroll, AgentMinPolicyContext.sublimationTargetTalent());
		if (talent == null) {
			lastFailureConsumesTime = false;
			AgentMinRuntimeLog.log("agentmin sublimation skipped: no candidate type=" + scroll.type());
			return false;
		}
		if (!applySublimation(hero, scroll, talent, tier)) {
			AgentMinRuntimeLog.log("agentmin sublimation failed: cannot apply " + talent.name());
			return false;
		}
		prepareItemContext(scroll);
		if (!consumeAgentMinTalentResource(scroll)) {
			return false;
		}
		ScrollOfSublimation.onSublimation(talent);
		AgentMinRewardTracker.onTalentResourceUsed(scroll);
		AgentMinRewardTracker.onTalentUpgradeDecision(true);
		AgentMinRuntimeLog.log("agentmin sublimation head executed: type=" + scroll.type()
				+ " talent=" + talent.name()
				+ " preferred=" + AgentMinPolicyContext.sublimationTargetTalent());
		return true;
	}

	private static boolean applySublimation(Hero hero, ScrollOfSublimation scroll, Talent talent, int tier) {
		if (hero == null || scroll == null || talent == null || hero.talents == null || tier <= 0 || tier > hero.talents.size()) {
			return false;
		}
		LinkedHashMap<Talent, Integer> oldTier = hero.talents.get(tier - 1);
		if (oldTier == null) {
			return false;
		}
		String type = scroll.type();
		int index = AgentMinMetamorphPlanner.sublimationIndex(type);
		Talent targetSlot = Talent.bossTalentSlot(type);
		Talent currentSlotTalent = Talent.bossTalentForSlot(targetSlot, hero.sublimationTalents);
		LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
		boolean replacedSlot = false;
		boolean hasCorrespondingTalent = currentSlotTalent != targetSlot;
		for (Talent existing : oldTier.keySet()) {
			if (existing == targetSlot || existing == currentSlotTalent || Talent.bossTalentSlot(existing) == targetSlot) {
				newTier.put(talent, 0);
				replacedSlot = true;
				hasCorrespondingTalent = true;
			} else {
				newTier.put(existing, oldTier.get(existing));
			}
		}
		if (!replacedSlot && !hasCorrespondingTalent) {
			newTier.put(talent, 0);
		}
		hero.talents.set(tier - 1, newTier);
		hero.sublimationTalents.remove(currentSlotTalent);
		hero.sublimationTalents.put(targetSlot, talent.name());
		TalentCatalog.countUse(talent);
		if ("DM300".equals(type) || "YOG".equals(type)) {
			Buff.affect(hero, ScrollOfSublimation.Sublimation1.class).setBoosted(index);
		} else {
			Buff.affect(hero, ScrollOfSublimation.Sublimation.class).setBoosted(index);
		}
		WndHero.lastIdx = 1;
		return true;
	}

	private static boolean consumeAgentMinTalentResource(Item item) {
		return consumeAgentMinItem(item, "talent resource");
	}

	private static boolean consumeAgentMinItem(Item item, String reason) {
		Hero hero = Dungeon.hero;
		if (item == null || hero == null || hero.belongings == null || hero.belongings.backpack == null) {
			return false;
		}
		if (!hero.belongings.backpack.contains(item)) {
			return true;
		}
		int before = item.quantity();
		Item detached = item.detach(hero.belongings.backpack);
		boolean consumed = detached != null && (!hero.belongings.backpack.contains(item) || item.quantity() < before);
		if (!consumed && hero.belongings.backpack.contains(item)) {
			AgentMinRuntimeLog.log("agentmin " + reason + " consume failed: " + item.getClass().getSimpleName());
			return false;
		}
		return true;
	}

	private static MetamorphChoice chooseMetamorph(Hero hero) {
		if (hero == null || hero.talents == null) {
			return null;
		}
		for (int tierIndex = 0; tierIndex < hero.talents.size(); tierIndex++) {
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(tierIndex);
			if (tier == null) {
				continue;
			}
			for (Talent oldTalent : tier.keySet()) {
				Integer points = tier.get(oldTalent);
				if (points == null || oldTalent.maxPoints() <= 0 || Talent.excludedAsMetamorphSource(oldTalent)) {
					continue;
				}
				Talent newTalent = chooseMetamorphTarget(hero, tierIndex + 1, oldTalent);
				if (newTalent != null) {
					return new MetamorphChoice(tierIndex + 1, oldTalent, newTalent, points);
				}
			}
		}
		return null;
	}

	private static Talent chooseMetamorphTarget(Hero hero, int tier, Talent oldTalent) {
		HashSet<Talent> excluded = new HashSet<>(hero.talents.get(tier - 1).keySet());
		for (Talent.TalentType type : ScrollOfMetamorphosis.commonTypes()) {
			List<Talent> pool = Talent.metamorphCandidatePool(tier, type, excluded, 6);
			for (Talent candidate : pool) {
				if (candidate != null && candidate != oldTalent && !Talent.excludedFromMetamorphosis(candidate)) {
					return candidate;
				}
			}
		}
		return null;
	}

	private static boolean replaceTalent(Hero hero, Talent oldTalent, Talent newTalent, int tier) {
		LinkedHashMap<Talent, Integer> oldTier = hero.talents.get(tier - 1);
		if (oldTier == null || !oldTier.containsKey(oldTalent)) {
			return false;
		}
		LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
		for (Talent talent : oldTier.keySet()) {
			if (talent == oldTalent) {
				newTier.put(newTalent, oldTier.get(oldTalent));
				if (!hero.metamorphedTalents.containsValue(oldTalent)) {
					hero.metamorphedTalents.put(oldTalent, newTalent);
				} else if (hero.metamorphedTalents.get(newTalent) == oldTalent) {
					hero.metamorphedTalents.remove(newTalent);
				} else {
					for (Talent key : new ArrayList<>(hero.metamorphedTalents.keySet())) {
						if (hero.metamorphedTalents.get(key) == oldTalent) {
							hero.metamorphedTalents.put(key, newTalent);
						}
					}
				}
			} else {
				newTier.put(talent, oldTier.get(talent));
			}
		}
		hero.talents.set(tier - 1, newTier);
		return true;
	}

	private static class MetamorphChoice {
		final int tier;
		final Talent oldTalent;
		final Talent newTalent;
		final int points;

		MetamorphChoice(int tier, Talent oldTalent, Talent newTalent, int points) {
			this.tier = tier;
			this.oldTalent = oldTalent;
			this.newTalent = newTalent;
			this.points = points;
		}
	}
}
