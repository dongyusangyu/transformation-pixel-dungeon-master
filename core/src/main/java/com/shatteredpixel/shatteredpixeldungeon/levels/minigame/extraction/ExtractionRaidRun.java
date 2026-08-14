/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRecords;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.Treasures;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Owns the lifecycle and settlement rules for one extraction-raid run.
 */
public final class ExtractionRaidRun {

	public static final int EXTRA_CRYSTAL_KEY_BONUS = 2000;
	public static final int ENTRY_FEE = 1000;

	private static boolean pendingEntry;
	private static long pendingRaidId;
	private static int pendingReturnDepth;
	private static int pendingReturnBranch;
	private static int pendingReturnPos;
	private static boolean extractionPromptQueued;

	private ExtractionRaidRun() {
	}

	public static RaidSession startSession(
			Hero hero, long raidId, int returnDepth, int returnBranch, int returnPos) {
		if (hero == null) return null;
		RaidSession session = hero.buff(RaidSession.class);
		if (session == null) {
			session = new RaidSession();
			if (!session.attachTo((Char) hero)) return null;
		}
		session.configure(raidId, returnDepth, returnBranch, returnPos);
		ActionIndicator.setAction(session);
		return session;
	}

	public static boolean canPayEntry(int gold) {
		return gold >= ENTRY_FEE;
	}

	public static boolean beginFromSurfaceShop() {
		Hero hero = Dungeon.hero;
		if (hero == null
				|| hasActiveSession(hero)
				|| pendingEntry
				|| !canPayEntry(Dungeon.gold)) {
			return false;
		}

		pendingEntry = true;
		pendingRaidId = normalizeRaidId(Random.Long(false));
		pendingReturnDepth = Dungeon.depth;
		pendingReturnBranch = Dungeon.branch;
		pendingReturnPos = hero.pos;

		Level.beforeTransition();
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnDepth = ExtractionRaidLevel.DEPTH;
		InterlevelScene.returnBranch = ExtractionRaidLevel.BRANCH;
		InterlevelScene.returnPos = -1;
		Game.switchScene(InterlevelScene.class);
		return true;
	}

	public static boolean hasPendingEntry() {
		return pendingEntry;
	}

	public static boolean isPendingEntryDestination(int depth, int branch) {
		return pendingEntry
				&& ExtractionRaidLevel.isRaidLocation(depth, branch);
	}

	public static void cancelPendingEntry() {
		clearPendingEntry();
	}

	/**
	 * Called by InterlevelScene only after the clean surface state has been saved.
	 */
	public static boolean commitPendingEntry(int destinationDepth, int destinationBranch) {
		if (!pendingEntry) return true;
		try {
			if (!ExtractionRaidLevel.isRaidLocation(
					destinationDepth, destinationBranch)) {
				return false;
			}
			Hero hero = Dungeon.hero;
			if (hero == null || hasActiveSession(hero) || !canPayEntry(Dungeon.gold)) {
				return false;
			}
			RaidSession session = startSession(
					hero,
					pendingRaidId,
					pendingReturnDepth,
					pendingReturnBranch,
					pendingReturnPos);
			if (session == null) return false;

			Dungeon.gold -= ENTRY_FEE;
			ExtractionRaidLevel.clearRaidKeys();
			Dungeon.generatedLevels.remove(Integer.valueOf(
					ExtractionRaidLevel.DEPTH + 1000 * ExtractionRaidLevel.BRANCH));
			return true;
		} finally {
			clearPendingEntry();
		}
	}

	private static void clearPendingEntry() {
		pendingEntry = false;
		pendingRaidId = 0;
		pendingReturnDepth = 0;
		pendingReturnBranch = 0;
		pendingReturnPos = 0;
	}

	public static boolean hasActiveSession(Hero hero) {
		return hero != null && hero.buff(RaidSession.class) != null;
	}

	public static boolean handleHeroDeath(Hero hero, Object cause) {
		if (!hasActiveSession(hero)
				|| !ExtractionRaidLevel.isRaidLocation(Dungeon.depth, Dungeon.branch)) {
			return false;
		}
		fail(hero, true);
		return true;
	}

	public static void showExtractionPrompt(ExtractionRaidLevel level) {
		if (level == null || !hasActiveSession(Dungeon.hero) || !level.isHeroAtExtraction()) {
			return;
		}
		if (!level.canExtract()) {
			GLog.w(Messages.get(ExtractionRaidRun.class, "missing_key"));
			return;
		}
		if (extractionPromptQueued) return;
		extractionPromptQueued = true;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				extractionPromptQueued = false;
				showExtractionPromptOnRenderThread(level);
			}
		});
	}

	private static void showExtractionPromptOnRenderThread(ExtractionRaidLevel level) {
		if (level == null || !hasActiveSession(Dungeon.hero) || !level.isHeroAtExtraction()) {
			return;
		}
		if (!level.canExtract()) {
			GLog.w(Messages.get(ExtractionRaidRun.class, "missing_key"));
			return;
		}
		GameScene.show(new WndOptions(
				Messages.get(ExtractionRaidRun.class, "extract_title"),
				Messages.get(ExtractionRaidRun.class, "extract_body", level.crystalKeyCount()),
				Messages.get(ExtractionRaidRun.class, "extract"),
				Messages.get(ExtractionRaidRun.class, "continue_raid")) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index == 0) completeSuccess(Dungeon.hero, level);
			}
		});
	}

	public static void showAbandonPrompt() {
		if (!hasActiveSession(Dungeon.hero)) return;
		GameScene.show(new WndOptions(
				Messages.get(ExtractionRaidRun.class, "abandon_title"),
				Messages.get(ExtractionRaidRun.class, "abandon_body"),
				Messages.get(ExtractionRaidRun.class, "abandon"),
				Messages.get(ExtractionRaidRun.class, "continue_raid")) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index == 0) fail(Dungeon.hero, false);
			}
		});
	}

	public static boolean completeSuccess(Hero hero, ExtractionRaidLevel level) {
		RaidSession session = hero == null ? null : hero.buff(RaidSession.class);
		if (session == null || level == null || !level.canExtract()) return false;

		int keyCount = level.crystalKeyCount();
		int bonus = bonusForKeyCount(keyCount);
		clearRaidMarks(hero.belongings.backpack, session.raidId());
		clearEquippedRaidMarks(hero, session.raidId());
		if (bonus > 0) {
			Dungeon.gold += bonus;
			Statistics.goldCollected += bonus;
			Catalog.countUses(Gold.class, bonus);
		}
		ExtractionRaidLevel.clearRaidKeys();
		recordExtractionRaidSettlement(true, false);
		finishSession(hero, session);

		if (bonus > 0) {
			GLog.p(Messages.get(ExtractionRaidRun.class, "success_bonus", bonus));
		} else {
			GLog.p(Messages.get(ExtractionRaidRun.class, "success"));
		}
		requestReturn(session);
		return true;
	}

	public static boolean fail(Hero hero, boolean died) {
		RaidSession session = hero == null ? null : hero.buff(RaidSession.class);
		if (session == null) return false;

		int raidLootLost = removeEquippedRaidLoot(hero, session.raidId());
		raidLootLost += removeRaidLoot(hero.belongings.backpack, session.raidId());
		int suppliesLost = removeOrdinaryConsumables(hero.belongings.backpack);
		ExtractionRaidLevel.clearRaidKeys();

		if (died) {
			hero.HP = Math.max(1, hero.HT / 4);
			PotionOfHealing.cure(hero);
		}
		recordExtractionRaidSettlement(false, died);
		finishSession(hero, session);
		GLog.w(Messages.get(
				ExtractionRaidRun.class, "failure", raidLootLost, suppliesLost));
		requestReturn(session);
		return true;
	}

	static void recordExtractionRaidSettlement(boolean successful, boolean died) {
		if (!successful && !died) {
			return;
		}
		TreasureHuntRecords.recordExtractionRaidSettlement();
	}

	private static void finishSession(Hero hero, RaidSession session) {
		ActionIndicator.clearAction(session);
		Overburden.detachFrom(hero);
		session.detach();
	}

	private static void requestReturn(RaidSession session) {
		Level.beforeTransition();
		InterlevelScene.mode = InterlevelScene.Mode.RETURN;
		InterlevelScene.returnDepth = session.returnDepth();
		InterlevelScene.returnBranch = session.returnBranch();
		InterlevelScene.returnPos = session.returnPos();
		Game.switchScene(InterlevelScene.class);
	}

	public static int bonusForKeyCount(int keyCount) {
		return keyCount >= 2 ? EXTRA_CRYSTAL_KEY_BONUS : 0;
	}

	private static long normalizeRaidId(long value) {
		if (value == Long.MIN_VALUE) return Long.MAX_VALUE;
		return Math.max(1L, Math.abs(value));
	}

	public static int removeRaidLoot(Bag bag, long raidId) {
		if (bag == null || raidId == 0) return 0;
		int removed = 0;
		LossCandidate candidate;
		while ((candidate = findRaidLoot(bag, raidId)) != null) {
			if (candidate.item.detachAll(candidate.bag) != null) removed++;
		}
		return removed;
	}

	private static LossCandidate findRaidLoot(Bag bag, long raidId) {
		for (Item item : bag.items) {
			if (item instanceof Bag) {
				LossCandidate nested = findRaidLoot((Bag) item, raidId);
				if (nested != null) return nested;
			} else if (item.extractionRaidId() == raidId) {
				return new LossCandidate(bag, item);
			}
		}
		return null;
	}

	public static int clearRaidMarks(Bag bag, long raidId) {
		if (bag == null || raidId == 0) return 0;
		int cleared = 0;
		for (Item item : bag.items) {
			if (item instanceof Bag) {
				cleared += clearRaidMarks((Bag) item, raidId);
			} else if (item.extractionRaidId() == raidId) {
				item.clearExtractionRaidMark();
				cleared++;
			}
		}
		return cleared;
	}

	public static boolean isOrdinaryConsumable(Item item) {
		if (item == null
				|| item.unique
				|| item instanceof Bag
				|| item instanceof Waterskin
				|| item instanceof Key
				|| item instanceof Treasures) {
			return false;
		}
		return isOrdinaryConsumableClass(item.getClass());
	}

	public static boolean isOrdinaryConsumableClass(Class<? extends Item> itemClass) {
		if (itemClass == null
				|| Bag.class.isAssignableFrom(itemClass)
				|| Waterskin.class.isAssignableFrom(itemClass)
				|| Key.class.isAssignableFrom(itemClass)
				|| Treasures.class.isAssignableFrom(itemClass)) {
			return false;
		}
		boolean consumable = Potion.class.isAssignableFrom(itemClass)
				|| Scroll.class.isAssignableFrom(itemClass)
				|| Runestone.class.isAssignableFrom(itemClass)
				|| Plant.Seed.class.isAssignableFrom(itemClass)
				|| Food.class.isAssignableFrom(itemClass);
		if (!consumable) return false;
		return !(PotionOfStrength.class.isAssignableFrom(itemClass)
				|| PotionOfMastery.class.isAssignableFrom(itemClass)
				|| ElixirOfMight.class.isAssignableFrom(itemClass)
				|| ScrollOfUpgrade.class.isAssignableFrom(itemClass)
				|| ScrollOfTransmutation.class.isAssignableFrom(itemClass)
				|| ScrollOfEnchantment.class.isAssignableFrom(itemClass)
				|| StoneOfEnchantment.class.isAssignableFrom(itemClass)
				|| Rotberry.Seed.class.isAssignableFrom(itemClass));
	}

	static int removeOrdinaryConsumables(Bag bag) {
		ArrayList<LossCandidate> initialCandidates = new ArrayList<>();
		collectOrdinaryConsumables(bag, initialCandidates);
		int available = 0;
		for (LossCandidate candidate : initialCandidates) {
			available += Math.max(0, candidate.item.quantity());
		}
		int target = ordinaryLossCount(available, Random.IntRange(1, 2));
		int removed = 0;
		for (int i = 0; i < target; i++) {
			ArrayList<LossCandidate> candidates = new ArrayList<>();
			collectOrdinaryConsumables(bag, candidates);
			if (candidates.isEmpty()) break;
			LossCandidate candidate = Random.element(candidates);
			if (candidate.item.detach(candidate.bag) != null) removed++;
		}
		return removed;
	}

	static int ordinaryLossCount(int availableUnits, int rolledLoss) {
		return Math.min(Math.max(0, availableUnits), Math.max(1, Math.min(2, rolledLoss)));
	}

	private static void collectOrdinaryConsumables(Bag bag, ArrayList<LossCandidate> result) {
		if (bag == null) return;
		for (Item item : bag.items) {
			if (item instanceof Bag) {
				collectOrdinaryConsumables((Bag) item, result);
			} else if (isOrdinaryConsumable(item)) {
				result.add(new LossCandidate(bag, item));
			}
		}
	}

	private static final class LossCandidate {
		final Bag bag;
		final Item item;

		LossCandidate(Bag bag, Item item) {
			this.bag = bag;
			this.item = item;
		}
	}

	private static Item[] equippedItems(Hero hero) {
		return new Item[]{
				hero.belongings.weapon,
				hero.belongings.armor,
				hero.belongings.artifact,
				hero.belongings.misc,
				hero.belongings.ring,
				hero.belongings.secondWep
		};
	}

	private static int clearEquippedRaidMarks(Hero hero, long raidId) {
		int cleared = 0;
		for (Item item : equippedItems(hero)) {
			if (item != null && item.extractionRaidId() == raidId) {
				item.clearExtractionRaidMark();
				cleared++;
			}
		}
		return cleared;
	}

	private static int removeEquippedRaidLoot(Hero hero, long raidId) {
		int removed = 0;
		for (Item item : equippedItems(hero)) {
			if (item != null
					&& item.extractionRaidId() == raidId
					&& item instanceof EquipableItem) {
				EquipableItem equipped = (EquipableItem) item;
				if (!equipped.doUnequip(hero, false, false)) {
					equipped.cursed = false;
					equipped.doUnequip(hero, false, false);
				}
				if (!isStillEquipped(hero, item)) removed++;
			}
		}
		return removed;
	}

	private static boolean isStillEquipped(Hero hero, Item item) {
		for (Item equipped : equippedItems(hero)) {
			if (equipped == item) return true;
		}
		return false;
	}

	public static class RaidSession extends Buff implements ActionIndicator.Action {

		private static final String RAID_ID = "raid_id";
		private static final String RETURN_DEPTH = "return_depth";
		private static final String RETURN_BRANCH = "return_branch";
		private static final String RETURN_POS = "return_pos";

		private long raidId;
		private int returnDepth;
		private int returnBranch;
		private int returnPos;

		{
			type = buffType.NEUTRAL;
			announced = true;
			revivePersists = true;
		}

		public RaidSession configure(
				long raidId, int returnDepth, int returnBranch, int returnPos) {
			this.raidId = raidId;
			this.returnDepth = returnDepth;
			this.returnBranch = returnBranch;
			this.returnPos = returnPos;
			return this;
		}

		public long raidId() {
			return raidId;
		}

		public int returnDepth() {
			return returnDepth;
		}

		public int returnBranch() {
			return returnBranch;
		}

		public int returnPos() {
			return returnPos;
		}

		@Override
		public boolean act() {
			if (!(target instanceof Hero)
					|| !ExtractionRaidLevel.isRaidLocation(Dungeon.depth, Dungeon.branch)) {
				Overburden.detachFrom(target instanceof Hero ? (Hero) target : null);
				detach();
				return true;
			}
			Overburden.ensureAttached((Hero) target);
			if (ActionIndicator.action == null) ActionIndicator.setAction(this);
			spend(TICK);
			return true;
		}

		@Override
		public void detach() {
			ActionIndicator.clearAction(this);
			super.detach();
		}

		@Override
		public int icon() {
			return BuffIndicator.LOCKED_FLOOR;
		}

		@Override
		public String name() {
			return Messages.get(ExtractionRaidRun.class, "session_name");
		}

		@Override
		public String desc() {
			return Messages.get(ExtractionRaidRun.class, "session_desc");
		}

		@Override
		public String actionName() {
			return Messages.get(ExtractionRaidRun.class, "abandon_action");
		}

		@Override
		public int actionIcon() {
			return HeroIcon.SMOKE_BOMB;
		}

		@Override
		public int indicatorColor() {
			return 0xB48A3C;
		}

		@Override
		public void doAction() {
			showAbandonPrompt();
		}

		@Override
		public boolean usable() {
			return target == Dungeon.hero
					&& ExtractionRaidLevel.isRaidLocation(Dungeon.depth, Dungeon.branch);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(RAID_ID, raidId);
			bundle.put(RETURN_DEPTH, returnDepth);
			bundle.put(RETURN_BRANCH, returnBranch);
			bundle.put(RETURN_POS, returnPos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			raidId = bundle.getLong(RAID_ID);
			returnDepth = bundle.getInt(RETURN_DEPTH);
			returnBranch = bundle.getInt(RETURN_BRANCH);
			returnPos = bundle.getInt(RETURN_POS);
		}
	}
}
