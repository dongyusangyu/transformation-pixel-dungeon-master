/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RankingRestart {

	public static final int UPGRADE_SCROLLS = 15;
	private static final int CONVERSION_DEPTH = 1;
	private static final int SCORE_PER_GOLD = 100;
	private static final int MAX_INVENTORY_GOLD = 5_000;

	private static Rankings.Record pendingRecord;
	private static boolean pendingRecordMarked;
	private static long seedSequence;
	private static long lastNewCycleSeed = -1;

	private RankingRestart() {
	}

	public static boolean isEligible(Rankings.Record record) {
		if (record == null || !record.win || record.restarted
				|| record.newCycle || record.gameData == null) {
			return false;
		}
		if (record.customSeed != null && !record.customSeed.isEmpty()) {
			return false;
		}
		int challenges = record.gameData.contains(Rankings.CHALLENGES)
				? record.gameData.getInt(Rankings.CHALLENGES)
				: 0;
		return (challenges & Challenges.RED_ENVELOPE) == 0
				&& !Rankings.INSTANCE.isRestartReserved(record.gameID);
	}

	public static boolean begin(Rankings.Record record) {
		Rankings.Record source = Rankings.INSTANCE.resolveRestartSource(record);
		if (!isEligible(source) || pendingRecord != null) {
			return false;
		}
		pendingRecord = source;
		return true;
	}

	public static boolean canBegin(Rankings.Record record) {
		return pendingRecord == null
				&& isEligible(Rankings.INSTANCE.resolveRestartSource(record));
	}

	public static boolean hasPendingRestart() {
		return pendingRecord != null;
	}

	public static void prepareDungeon() {
		if (!isEligible(pendingRecord)) {
			throw new IllegalStateException("Invalid ranking restart record");
		}

		Rankings.INSTANCE.setRestartReserved(pendingRecord.gameID, true);
		if (!Rankings.INSTANCE.saveWithResult()) {
			Rankings.INSTANCE.setRestartReserved(pendingRecord.gameID, false);
			throw new IllegalStateException("Unable to reserve ranking restart");
		}
		pendingRecordMarked = true;

		Rankings.INSTANCE.loadGameData(pendingRecord);
		if (Dungeon.hero == null) {
			throw new IllegalStateException("Ranking record has no hero data");
		}

		sanitizeLegacyInventory(Dungeon.hero);
		Preparation preparation = prepareHero(Dungeon.hero);
		int newGold = calculateStartingGold(pendingRecord.score, preparation.convertedGold);

		Dungeon.daily = false;
		Dungeon.dailyReplay = false;
		Dungeon.customSeedText = "";
		Dungeon.seed = nextNewCycleSeed();
		Dungeon.reinit(false);
		restoreOwnedBagLimitedDrops(Dungeon.hero);
		Ghost.Quest.complete();
		identifyAnonymousItemTypes();
		Dungeon.hero.belongings.identify();
		claimCarriedArtifacts(Dungeon.hero.belongings);
		ensureFullReason(Dungeon.hero, Dungeon.hero.heroClass);
		Dungeon.newCycle = true;
		Dungeon.newCycleSourceGameID = pendingRecord.gameID;
		Dungeon.challenges = 0;
		Dungeon.gold = newGold;
		// reinit() calls hero.live() before newCycle is set; restore subclass-owned
		// action and equipment buffs again after the new-cycle state and talents are in place.
		Dungeon.hero.ensureSubclassBuffs();
		reactivateEquippedItems(Dungeon.hero);
		refreshHealthForNewCycle(Dungeon.hero);
		normalizeUpgradeScrolls(Dungeon.hero);
	}

	static void reactivateEquippedItems(Hero hero) {
		if (hero == null || hero.belongings == null) {
			return;
		}
		EquipableItem[] equippedItems = {
				hero.belongings.weapon,
				hero.belongings.armor,
				hero.belongings.artifact,
				hero.belongings.misc,
				hero.belongings.ring,
				hero.belongings.secondWep
		};
		for (EquipableItem item : equippedItems) {
			if (item != null) {
				item.activate(hero);
			}
		}
	}

	static void refreshHealthForNewCycle(Hero hero) {
		hero.updateHT(false);
		hero.HP = hero.HT;
	}

	static void restoreOwnedBagLimitedDrops(Hero hero) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null) {
			return;
		}
		for (Item item : hero.belongings.backpack) {
			if (item instanceof VelvetPouch) {
				Dungeon.LimitedDrops.VELVET_POUCH.drop();
			} else if (item instanceof ScrollHolder) {
				Dungeon.LimitedDrops.SCROLL_HOLDER.drop();
			} else if (item instanceof PotionBandolier) {
				Dungeon.LimitedDrops.POTION_BANDOLIER.drop();
			} else if (item instanceof MagicalHolster) {
				Dungeon.LimitedDrops.MAGICAL_HOLSTER.drop();
			} else if (item instanceof HikingBackpack) {
				Dungeon.LimitedDrops.HIKING_BACKPACK.drop();
			}
		}
	}

	static Preparation prepareHero(Hero hero) {
		if (hero == null) {
			throw new IllegalArgumentException("hero must not be null");
		}

		Preparation preparation = prepareInventory(
				hero.belongings.backpack,
				hero.belongings.weapon,
				hero.belongings.armor,
				hero.belongings.artifact,
				hero.belongings.misc,
				hero.belongings.ring,
				hero.belongings.secondWep);
		prepareDriedRose(hero);
		MissileWeapon.UpgradedSetTracker.resetForNewCycle(hero);

		clearNegativeTalents(hero);
		clearRandomMode(hero);
		return preparation;
	}

	static void sanitizeLegacyInventory(Hero hero) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null) {
			return;
		}

		ArrayList<ItemLocation> locations = new ArrayList<>();
		collectItemLocations(hero.belongings.backpack, 0, locations);
		normalizeWaterskins(locations);

		removeLegacyFlattenedItems(hero.belongings.backpack, Wand.class);
	}

	private static void normalizeWaterskins(List<ItemLocation> locations) {
		ArrayList<ItemLocation> waterskins = new ArrayList<>();
		for (ItemLocation location : locations) {
			if (location.item instanceof Waterskin) {
				waterskins.add(location);
			}
		}
		if (waterskins.size() <= 1) {
			return;
		}

		ItemLocation canonical = waterskins.get(0);
		for (ItemLocation location : waterskins) {
			if (Dungeon.quickslot.contains(location.item)) {
				canonical = location;
				break;
			}
		}

		Waterskin kept = (Waterskin) canonical.item;
		for (ItemLocation location : waterskins) {
			Waterskin candidate = (Waterskin) location.item;
			kept.volume = Math.max(kept.volume, candidate.volume);
			if (location != canonical) {
				location.container.items.remove(candidate);
				rebindQuickslot(candidate, kept);
			}
		}
		rebindDetachedWaterskinSlots(kept);
	}

	static void removeLegacyFlattenedItems(Bag backpack,
			Class<? extends Item> migratedItemType) {
		if (backpack == null || migratedItemType == null) {
			return;
		}
		ArrayList<ItemLocation> locations = new ArrayList<>();
		collectItemLocations(backpack, 0, locations);
		ArrayList<ItemLocation> nestedItems = new ArrayList<>();
		for (ItemLocation location : locations) {
			if (location.depth > 0 && migratedItemType.isInstance(location.item)) {
				nestedItems.add(location);
			}
		}

		for (ItemLocation location : locations) {
			if (location.depth != 0 || !migratedItemType.isInstance(location.item)) {
				continue;
			}
			for (ItemLocation nested : nestedItems) {
				if (QuickSlot.rankingSnapshotMatches(location.item, nested.item)
						&& quickslotContainsLegacyCopy(location.item, nested.item)) {
					location.container.items.remove(location.item);
					rebindQuickslot(location.item, nested.item);
					rebindMatchingDetachedSlots(location.item, nested.item);
					break;
				}
			}
		}
	}

	private static boolean quickslotContainsLegacyCopy(Item first, Item second) {
		for (int slot = 0; slot < QuickSlot.SIZE; slot++) {
			Item quickslotItem = Dungeon.quickslot.getItem(slot);
			if (quickslotItem == first || quickslotItem == second
					|| QuickSlot.rankingSnapshotMatches(quickslotItem, first)) {
				return true;
			}
		}
		return false;
	}

	private static void collectItemLocations(Bag container, int depth,
			List<ItemLocation> locations) {
		for (Item item : container.items) {
			locations.add(new ItemLocation(container, item, depth));
			if (item instanceof Bag) {
				collectItemLocations((Bag) item, depth + 1, locations);
			}
		}
	}

	private static void rebindQuickslot(Item removed, Item kept) {
		for (int slot = 0; slot < QuickSlot.SIZE; slot++) {
			if (Dungeon.quickslot.getItem(slot) == removed) {
				Dungeon.quickslot.setSlot(slot, kept);
				return;
			}
		}
	}

	private static void rebindDetachedWaterskinSlots(Waterskin kept) {
		for (int slot = 0; slot < QuickSlot.SIZE; slot++) {
			Item item = Dungeon.quickslot.getItem(slot);
			if (item instanceof Waterskin && item != kept) {
				Dungeon.quickslot.setSlot(slot, kept);
				return;
			}
		}
	}

	private static void rebindMatchingDetachedSlots(Item removed, Item kept) {
		for (int slot = 0; slot < QuickSlot.SIZE; slot++) {
			Item item = Dungeon.quickslot.getItem(slot);
			if (item != kept && QuickSlot.rankingSnapshotMatches(item, removed)) {
				Dungeon.quickslot.setSlot(slot, kept);
				return;
			}
		}
	}

	private static final class ItemLocation {
		final Bag container;
		final Item item;
		final int depth;

		ItemLocation(Bag container, Item item, int depth) {
			this.container = container;
			this.item = item;
			this.depth = depth;
		}
	}

	static Preparation prepareInventory(Bag backpack, Item... equippedItems) {
		int previousDepth = Dungeon.depth;
		long convertedGold;
		try {
			Dungeon.depth = CONVERSION_DEPTH;
			convertedGold = prepareBag(backpack);
		} finally {
			Dungeon.depth = previousDepth;
		}
		for (Item item : equippedItems) {
			resetEquipmentLevel(item);
		}
		return new Preparation((int) Math.min(MAX_INVENTORY_GOLD, convertedGold));
	}

	private static void prepareDriedRose(Hero hero) {
		DriedRose rose = hero.belongings.getItem(DriedRose.class);
		if (rose != null) {
			resetEquipmentLevel(rose.ghostWeapon());
			resetEquipmentLevel(rose.ghostArmor());
			rose.resetGhostForNewCycle();
		}
	}

	private static long prepareBag(Bag bag) {
		long convertedGold = 0;
		for (Item item : bag.items.toArray(new Item[0])) {
			if (item instanceof Bag) {
				convertedGold += prepareBag((Bag) item);
			}

			if (shouldKeep(item)) {
				resetEquipmentLevel(item);
			} else {
				Dungeon.quickslot.clearItem(item);
				bag.items.remove(item);
				convertedGold += Math.max(0, item.value());
			}
		}
		return Math.min(Integer.MAX_VALUE, convertedGold);
	}

	private static boolean shouldKeep(Item item) {
		return !(item instanceof ScrollOfUpgrade)
				&& !isRemovedFromNewCycle(item.getClass())
				&& (item.unique
				|| item instanceof PotionOfStrength
				|| item instanceof EquipableItem
				|| item instanceof Wand);
	}

	static void normalizeUpgradeScrolls(Hero hero) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null) {
			return;
		}

		ScrollOfUpgrade upgrades = new ScrollOfUpgrade();
		normalizeUpgradeScrolls(hero, ScrollOfUpgrade.class, upgrades);
	}

	static void normalizeUpgradeScrolls(Hero hero, Class<? extends Item> upgradeScrollType, Item upgrades) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null
				|| upgradeScrollType == null || upgrades == null) {
			return;
		}

		removeUpgradeScrolls(hero.belongings.backpack, upgradeScrollType);
		upgrades.quantity(UPGRADE_SCROLLS);
		if (!upgrades.collect()) {
			hero.belongings.backpack.items.add(upgrades);
		}
	}

	private static void removeUpgradeScrolls(Bag bag, Class<? extends Item> upgradeScrollType) {
		for (Item item : bag.items.toArray(new Item[0])) {
			if (item instanceof Bag) {
				removeUpgradeScrolls((Bag) item, upgradeScrollType);
			}
			if (upgradeScrollType.isInstance(item)) {
				Dungeon.quickslot.clearItem(item);
				bag.items.remove(item);
			}
		}
	}

	static boolean isRemovedFromNewCycle(Class<? extends Item> itemClass) {
		return Amulet.class.isAssignableFrom(itemClass);
	}

	private static void resetEquipmentLevel(Item item) {
		if (item != null
				&& !(item instanceof Artifact)
				&& (item instanceof EquipableItem || item instanceof Wand)
				&& item.isUpgradable()) {
			item.resetUpgradeStateForNewCycle();
		}
	}

	private static void clearNegativeTalents(Hero hero) {
		clearNegativeTalents(hero.talents, hero.negativeTalents, hero.testModeNegativeTalent);
		hero.testModeNegativeTalent = null;
	}

	static void clearNegativeTalents(
			List<LinkedHashMap<Talent, Integer>> tiers,
			Map<Talent, Integer> storedNegativeTalents,
			Talent testModeNegativeTalent) {
		Set<Talent> talentsToRemove = new LinkedHashSet<>(storedNegativeTalents.keySet());
		if (testModeNegativeTalent != null) {
			talentsToRemove.add(testModeNegativeTalent);
		}
		for (LinkedHashMap<Talent, Integer> tier : tiers) {
			for (Talent talent : talentsToRemove) {
				tier.remove(talent);
			}
		}
		storedNegativeTalents.clear();
	}

	private static void clearRandomMode(Hero hero) {
		hero.randomMode = false;
		hero.randomTalentClass = null;
		hero.randomClassTalents = null;
		hero.randomSubClasses = null;
		hero.randomArmorAbilities = null;
	}

	static void claimCarriedArtifacts(Iterable<Item> items) {
		for (Item item : items) {
			if (item instanceof Artifact) {
				Generator.claimArtifact(((Artifact) item).getClass());
			}
		}
	}

	static void identifyAnonymousItemTypes() {
		for (Class<? extends Scroll> itemClass : Scroll.getUnknown()) {
			Reflection.newInstance(itemClass).identify(false);
		}
		for (Class<? extends Potion> itemClass : Potion.getUnknown()) {
			Reflection.newInstance(itemClass).identify(false);
		}
		for (Class<? extends Ring> itemClass : Ring.getUnknown()) {
			Reflection.newInstance(itemClass).identify(false);
		}
		for (Class<? extends Wand> itemClass : Wand.getUnknown()) {
			Wand.setKnown(itemClass);
		}
	}

	static void ensureFullReason(Char target, HeroClass heroClass) {
		if (target == null || heroClass != HeroClass.FRIAR) {
			return;
		}
		Reason reason = Buff.affect(target, Reason.class);
		if (reason != null) {
			reason.reason = 100;
			reason.kaoyan = false;
		}
	}

	static int calculateStartingGold(double score, int convertedInventoryGold) {
		double scoreGold = Math.floor(Math.max(0d, score) / SCORE_PER_GOLD);
		long inventoryGold = Math.min(MAX_INVENTORY_GOLD,
				Math.max(0L, (long) convertedInventoryGold));
		return (int) Math.min(Integer.MAX_VALUE, scoreGold + inventoryGold);
	}

	private static synchronized long nextNewCycleSeed() {
		long seed;
		do {
			seed = timeBasedSeed(System.currentTimeMillis(), System.nanoTime(), ++seedSequence);
		} while (seed == lastNewCycleSeed);
		lastNewCycleSeed = seed;
		return seed;
	}

	static long timeBasedSeed(long currentTimeMillis, long nanoTime, long sequence) {
		long entropy = currentTimeMillis
				^ Long.rotateLeft(nanoTime, 21)
				^ sequence * 0x9E3779B97F4A7C15L;
		while (true) {
			entropy += 0x9E3779B97F4A7C15L;
			long mixed = entropy;
			mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
			mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
			mixed ^= mixed >>> 31;
			long seed = Math.floorMod(mixed, DungeonSeed.TOTAL_SEEDS);
			String code = DungeonSeed.convertToCode(seed);
			if (!code.contains("A") && !code.contains("E")
					&& !code.contains("I") && !code.contains("O")
					&& !code.contains("U")) {
				return seed;
			}
		}
	}

	static void releaseRestartForDeletedSave(GamesInProgress.Info info) {
		if (info == null || !info.newCycle) {
			return;
		}
		if (GamesInProgress.hasSaveBoundTo(info.newCycleSourceGameID)) {
			return;
		}
		Rankings.INSTANCE.load();
		String sourceGameID = info.newCycleSourceGameID;
		if (sourceGameID == null || sourceGameID.isEmpty()) {
			Rankings.Record released = findRestartRecord(
					Rankings.INSTANCE.records, null, info);
			if (released == null) {
				released = findRestartRecord(
						Rankings.INSTANCE.heroHallRecords(), null, info);
			}
			if (released == null) {
				return;
			}
			sourceGameID = released.gameID;
		}
		Rankings.INSTANCE.setRestartReserved(sourceGameID, false);
		if (!Rankings.INSTANCE.saveWithResult()) {
			Rankings.INSTANCE.setRestartReserved(sourceGameID, true);
		}
	}

	static boolean releaseRestartRecord(List<Rankings.Record> records, String sourceGameID) {
		Rankings.Record record = findRestartRecord(records, sourceGameID, null);
		if (record == null) {
			return false;
		}
		record.restarted = false;
		return true;
	}

	private static Rankings.Record findRestartRecord(List<Rankings.Record> records,
			String sourceGameID, GamesInProgress.Info legacyInfo) {
		if (sourceGameID != null && !sourceGameID.isEmpty()) {
			for (Rankings.Record record : records) {
				if (sourceGameID.equals(record.gameID) && record.restarted && !record.newCycle) {
					return record;
				}
			}
			return null;
		}

		Rankings.Record candidate = null;
		Rankings.Record fallback = null;
		int fallbackCount = 0;
		for (Rankings.Record record : records) {
			if (!isLegacySourceCandidate(record, null)) {
				continue;
			}
			fallbackCount++;
			if (fallbackCount == 1) {
				fallback = record;
			}
			if (legacyInfo != null && isLegacySourceCandidate(record, legacyInfo)) {
				if (candidate != null) {
					return null;
				}
				candidate = record;
			}
		}
		return candidate != null ? candidate : fallbackCount == 1 ? fallback : null;
	}

	private static boolean isLegacySourceCandidate(Rankings.Record record,
			GamesInProgress.Info info) {
		if (record == null || !record.restarted || !record.win || record.newCycle
				|| record.customSeed == null || !record.customSeed.isEmpty()) {
			return false;
		}
		if (info != null && (record.heroClass != info.heroClass
				|| record.herolevel != info.level || record.skin != info.skin)) {
			return false;
		}
		int challenges = record.gameData != null && record.gameData.contains(Rankings.CHALLENGES)
				? record.gameData.getInt(Rankings.CHALLENGES) : 0;
		return (challenges & Challenges.RED_ENVELOPE) == 0;
	}

	public static void complete() {
		if (pendingRecord != null) {
			pendingRecord = null;
			pendingRecordMarked = false;
		}
	}

	public static void cancel() {
		if (pendingRecord != null && pendingRecordMarked) {
			Rankings.INSTANCE.setRestartReserved(pendingRecord.gameID, false);
			if (!Rankings.INSTANCE.saveWithResult()) {
				Rankings.INSTANCE.setRestartReserved(pendingRecord.gameID, true);
			}
		}
		pendingRecord = null;
		pendingRecordMarked = false;
		Dungeon.newCycleSourceGameID = null;
	}

	static final class Preparation {
		final int convertedGold;

		Preparation(int convertedGold) {
			this.convertedGold = convertedGold;
		}
	}
}
