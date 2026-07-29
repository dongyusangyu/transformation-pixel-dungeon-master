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
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;

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
		Dungeon.seed = DungeonSeed.randomSeed();
		Dungeon.reinit();
		claimCarriedArtifacts(Dungeon.hero.belongings);
		ensureFullReason(Dungeon.hero, Dungeon.hero.heroClass);
		Dungeon.newCycle = true;
		Dungeon.challenges = 0;
		Dungeon.gold = newGold;
		Dungeon.hero.HP = Dungeon.hero.HT;

		ScrollOfUpgrade upgrades = new ScrollOfUpgrade();
		upgrades.quantity(UPGRADE_SCROLLS);
		if (!upgrades.collect()) {
			Dungeon.hero.belongings.backpack.items.add(upgrades);
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

	static int calculateStartingGold(int score, int convertedInventoryGold) {
		long scoreGold = Math.max(0L, (long) score) / SCORE_PER_GOLD;
		long inventoryGold = Math.min(MAX_INVENTORY_GOLD,
				Math.max(0L, (long) convertedInventoryGold));
		return (int) Math.min(Integer.MAX_VALUE, scoreGold + inventoryGold);
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
	}

	static final class Preparation {
		final int convertedGold;

		Preparation(int convertedGold) {
			this.convertedGold = convertedGold;
		}
	}
}
