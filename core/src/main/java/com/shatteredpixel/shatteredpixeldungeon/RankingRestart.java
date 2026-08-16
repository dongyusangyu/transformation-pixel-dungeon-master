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
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Reflection;

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
		return (challenges & Challenges.RED_ENVELOPE) == 0;
	}

	public static boolean begin(Rankings.Record record) {
		if (!isEligible(record) || pendingRecord != null) {
			return false;
		}
		pendingRecord = record;
		return true;
	}

	public static boolean hasPendingRestart() {
		return pendingRecord != null;
	}

	public static void prepareDungeon() {
		if (!isEligible(pendingRecord)) {
			throw new IllegalStateException("Invalid ranking restart record");
		}

		pendingRecord.restarted = true;
		if (!Rankings.INSTANCE.saveWithResult()) {
			pendingRecord.restarted = false;
			throw new IllegalStateException("Unable to reserve ranking restart");
		}
		pendingRecordMarked = true;

		Rankings.INSTANCE.loadGameData(pendingRecord);
		if (Dungeon.hero == null) {
			throw new IllegalStateException("Ranking record has no hero data");
		}

		Preparation preparation = prepareHero(Dungeon.hero);
		int newGold = calculateStartingGold(pendingRecord.score, preparation.convertedGold);

		Dungeon.daily = false;
		Dungeon.dailyReplay = false;
		Dungeon.customSeedText = "";
		Dungeon.seed = nextNewCycleSeed();
		Dungeon.reinit(false);
		identifyAnonymousItemTypes();
		Dungeon.hero.belongings.identify();
		claimCarriedArtifacts(Dungeon.hero.belongings);
		ensureFullReason(Dungeon.hero, Dungeon.hero.heroClass);
		Dungeon.newCycle = true;
		Dungeon.newCycleSourceGameID = pendingRecord.gameID;
		Dungeon.challenges = 0;
		Dungeon.gold = newGold;
		// reinit() calls hero.live() before newCycle is set; restore subclass-owned
		// action buffs again after the new-cycle state and talents are in place.
		Dungeon.hero.ensureSubclassBuffs();
		refreshHealthForNewCycle(Dungeon.hero);

		ScrollOfUpgrade upgrades = new ScrollOfUpgrade();
		upgrades.quantity(UPGRADE_SCROLLS);
		if (!upgrades.collect()) {
			Dungeon.hero.belongings.backpack.items.add(upgrades);
		}
	}

	static void refreshHealthForNewCycle(Hero hero) {
		hero.updateHT(false);
		hero.HP = hero.HT;
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

		clearNegativeTalents(hero);
		clearRandomMode(hero);
		return preparation;
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
		return !isRemovedFromNewCycle(item.getClass())
				&& (item.unique
				|| item instanceof PotionOfStrength
				|| item instanceof EquipableItem
				|| item instanceof Wand);
	}

	static boolean isRemovedFromNewCycle(Class<? extends Item> itemClass) {
		return Amulet.class.isAssignableFrom(itemClass);
	}

	private static void resetEquipmentLevel(Item item) {
		if (item != null
				&& !(item instanceof Artifact)
				&& (item instanceof EquipableItem || item instanceof Wand)
				&& item.isUpgradable()) {
			item.level(0);
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
		Rankings.Record released = findRestartRecord(
				Rankings.INSTANCE.records, info.newCycleSourceGameID, info);
		if (released == null) {
			return;
		}
		released.restarted = false;
		if (!Rankings.INSTANCE.saveWithResult()) {
			released.restarted = true;
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
			pendingRecord.restarted = false;
			if (!Rankings.INSTANCE.saveWithResult()) {
				pendingRecord.restarted = true;
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
