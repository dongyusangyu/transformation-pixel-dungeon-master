package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroRandomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RankingRestartTest {

	@After
	public void resetDungeonState() {
		RankingRestart.complete();
		Dungeon.depth = 0;
		Dungeon.challenges = 0;
		Dungeon.newCycle = false;
		Dungeon.newCycleSourceGameID = null;
		Dungeon.quickslot.reset();
		Dungeon.hero = null;
		Dungeon.LimitedDrops.reset();
		Ghost.Quest.reset();
		Generator.fullReset();
	}

	@Test
	public void newCycleResetDoesNotGrantStartingItemsAgain() throws Exception {
		Dungeon.hero = headlessHeroWithBelongings();
		Dungeon.hero.HP = 0;
		UniqueItem inherited = new UniqueItem();
		Dungeon.hero.belongings.backpack.items.add(inherited);

		Dungeon.rebuildBackpack(Dungeon.hero, false);

		assertEquals(1, Dungeon.hero.belongings.backpack.items.size());
		assertTrue(Dungeon.hero.belongings.backpack.items.contains(inherited));
	}

	@Test
	public void newCycleRestoresLimitedDropFlagsForInheritedBags() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		hero.belongings.backpack.items.add(emptyBag(VelvetPouch.class));
		hero.belongings.backpack.items.add(emptyBag(ScrollHolder.class));
		hero.belongings.backpack.items.add(emptyBag(PotionBandolier.class));
		hero.belongings.backpack.items.add(emptyBag(MagicalHolster.class));
		hero.belongings.backpack.items.add(emptyBag(HikingBackpack.class));
		Dungeon.LimitedDrops.reset();

		RankingRestart.restoreOwnedBagLimitedDrops(hero);

		assertTrue(Dungeon.LimitedDrops.VELVET_POUCH.dropped());
		assertTrue(Dungeon.LimitedDrops.SCROLL_HOLDER.dropped());
		assertTrue(Dungeon.LimitedDrops.POTION_BANDOLIER.dropped());
		assertTrue(Dungeon.LimitedDrops.MAGICAL_HOLSTER.dropped());
		assertTrue(Dungeon.LimitedDrops.HIKING_BACKPACK.dropped());
	}

	@Test
	public void legacyInventoryKeepsOneQuickslottedWaterskinWithHighestVolume() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		Waterskin quickslotted = waterskin(4);
		Waterskin duplicate = waterskin(15);
		hero.belongings.backpack.items.add(quickslotted);
		hero.belongings.backpack.items.add(duplicate);
		Dungeon.quickslot.setSlot(2, quickslotted);

		RankingRestart.sanitizeLegacyInventory(hero);

		assertEquals(1, hero.belongings.getAllItems(Waterskin.class).size());
		assertSame(quickslotted, hero.belongings.getItem(Waterskin.class));
		assertEquals(15, quickslotted.volume);
		assertSame(quickslotted, Dungeon.quickslot.getItem(2));
	}

	@Test
	public void legacyInventoryRemovesQuickslottedTopLevelCopyOfNestedItem() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		PersistentStateItem nested = new PersistentStateItem(7);
		PersistentStateItem duplicate = (PersistentStateItem) nested.duplicate();
		assertTrue(QuickSlot.rankingSnapshotMatches(nested, duplicate));
		Bag holder = new Bag();
		holder.items.add(nested);
		hero.belongings.backpack.items.add(holder);
		hero.belongings.backpack.items.add(duplicate);
		Dungeon.quickslot.setSlot(1, duplicate);
		assertTrue(QuickSlot.rankingSnapshotMatches(nested, duplicate));

		RankingRestart.removeLegacyFlattenedItems(
				hero.belongings.backpack, PersistentStateItem.class);

		assertTrue(holder.items.contains(nested));
		assertFalse(hero.belongings.backpack.items.contains(duplicate));
		assertEquals(1, hero.belongings.getAllItems(PersistentStateItem.class).size());
		assertSame(nested, Dungeon.quickslot.getItem(1));
	}

	@Test
	public void legacyInventoryKeepsDifferentSameClassItems() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		PersistentStateItem nested = new PersistentStateItem(0);
		PersistentStateItem topLevel = new PersistentStateItem(1);
		Bag holder = new Bag();
		holder.items.add(nested);
		hero.belongings.backpack.items.add(holder);
		hero.belongings.backpack.items.add(topLevel);
		Dungeon.quickslot.setSlot(1, topLevel);

		RankingRestart.removeLegacyFlattenedItems(
				hero.belongings.backpack, PersistentStateItem.class);

		assertEquals(2, hero.belongings.getAllItems(PersistentStateItem.class).size());
		assertTrue(holder.items.contains(nested));
		assertTrue(hero.belongings.backpack.items.contains(topLevel));
		assertSame(topLevel, Dungeon.quickslot.getItem(1));
	}

	@Test
	public void legacyInventoryKeepsUnslottedIdenticalItems() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		PersistentStateItem nested = new PersistentStateItem(7);
		PersistentStateItem topLevel = (PersistentStateItem) nested.duplicate();
		Bag holder = new Bag();
		holder.items.add(nested);
		hero.belongings.backpack.items.add(holder);
		hero.belongings.backpack.items.add(topLevel);

		RankingRestart.removeLegacyFlattenedItems(
				hero.belongings.backpack, PersistentStateItem.class);

		assertEquals(2, hero.belongings.getAllItems(PersistentStateItem.class).size());
		assertTrue(holder.items.contains(nested));
		assertTrue(hero.belongings.backpack.items.contains(topLevel));
	}

	@Test
	public void newCycleSanitizerAppliesFlattenedItemMigrationToWands() throws Exception {
		String source = readCoreSource("RankingRestart.java");

		assertTrue(source.contains("removeLegacyFlattenedItems("
				+ "hero.belongings.backpack, Wand.class)"));
	}

	@Test
	public void newCycleIdentifiesEveryAnonymousItemTypeAfterReset() throws Exception {
		String source = readCoreSource("RankingRestart.java");

		assertTrue(source.contains("Scroll.getUnknown()"));
		assertTrue(source.contains("Potion.getUnknown()"));
		assertTrue(source.contains("Ring.getUnknown()"));
		assertTrue(source.contains("Wand.getUnknown()"));
		assertTrue(source.indexOf("Dungeon.reinit(false);")
				< source.indexOf("identifyAnonymousItemTypes();"));
		assertTrue(source.indexOf("identifyAnonymousItemTypes();")
				< source.indexOf("Dungeon.hero.belongings.identify();"));
	}

	@Test
	public void completingGhostQuestAlwaysProducesCompletedState() {
		Ghost.Quest.reset();
		Notes.reset();

		Ghost.Quest.complete();

		assertTrue(Ghost.Quest.completed());
	}

	@Test
	public void newCycleCompletesGhostQuestAfterReinitializingDungeon() throws Exception {
		String source = readCoreSource("RankingRestart.java");

		assertTrue(source.indexOf("Dungeon.reinit(false);")
				< source.indexOf("Ghost.Quest.complete();"));
	}

	@Test
	public void onlyUnusedNormalVictoriesCanRestart() {
		Rankings.Record record = eligibleRecord();

		assertTrue(RankingRestart.isEligible(record));

		record.gameData.put(Rankings.CHALLENGES, Challenges.NO_FOOD);
		assertTrue(RankingRestart.isEligible(record));

		record.customSeed = "CUSTOM";
		assertFalse(RankingRestart.isEligible(record));

		record.customSeed = "";
		record.gameData.put(Rankings.CHALLENGES, Challenges.RED_ENVELOPE);
		assertFalse(RankingRestart.isEligible(record));

		record.gameData.put(Rankings.CHALLENGES, 0);
		record.restarted = true;
		assertFalse(RankingRestart.isEligible(record));

		record.restarted = false;
		record.newCycle = true;
		assertFalse(RankingRestart.isEligible(record));

		record.newCycle = false;
		record.win = false;
		assertFalse(RankingRestart.isEligible(record));
	}

	@Test
	public void restartedFlagSurvivesRankingSerialization() {
		Rankings.Record original = eligibleRecord();
		original.restarted = true;
		original.customSeed = "";
		original.randomTalents = new String[0];
		original.selectedTalents = new String[0];

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		Rankings.Record restored = new Rankings.Record();
		restored.restoreFromBundle(bundle);

		assertTrue(restored.restarted);
		assertFalse(RankingRestart.isEligible(restored));
	}

	@Test
	public void heroHallCopyCannotBypassReservedNormalRanking() {
		ArrayList<Rankings.Record> previousRecords = Rankings.INSTANCE.records;
		try {
			Rankings.Record normal = eligibleRecord();
			normal.gameID = "shared-ranking";
			normal.restarted = true;
			Rankings.Record hallCopy = eligibleRecord();
			hallCopy.gameID = normal.gameID;
			Rankings.INSTANCE.records = new ArrayList<>();
			Rankings.INSTANCE.records.add(normal);

			assertFalse(RankingRestart.begin(hallCopy));
		} finally {
			RankingRestart.complete();
			Rankings.INSTANCE.records = previousRecords;
		}
	}

	@Test
	public void heroHallCopyUsesMatchingNormalRankingAsRestartSource() throws Exception {
		ArrayList<Rankings.Record> previousRecords = Rankings.INSTANCE.records;
		try {
			Rankings.Record normal = eligibleRecord();
			normal.gameID = "shared-ranking";
			Rankings.Record hallCopy = eligibleRecord();
			hallCopy.gameID = normal.gameID;
			Rankings.INSTANCE.records = new ArrayList<>();
			Rankings.INSTANCE.records.add(normal);

			assertTrue(RankingRestart.begin(hallCopy));
			assertSame(normal, pendingRestartRecord());
		} finally {
			RankingRestart.complete();
			Rankings.INSTANCE.records = previousRecords;
		}
	}

	@Test
	public void heroHallOnlyRankingRemainsAValidRestartSource() throws Exception {
		ArrayList<Rankings.Record> previousRecords = Rankings.INSTANCE.records;
		try {
			Rankings.Record hallOnly = eligibleRecord();
			hallOnly.gameID = "hall-only-ranking";
			Rankings.INSTANCE.records = new ArrayList<>();

			assertTrue(RankingRestart.begin(hallOnly));
			assertSame(hallOnly, pendingRestartRecord());
		} finally {
			RankingRestart.complete();
			Rankings.INSTANCE.records = previousRecords;
		}
	}

	@Test
	public void clearingNewCycleSaveUnlocksOnlyItsSourceRanking() {
		Rankings.Record source = eligibleRecord();
		source.gameID = "source-ranking";
		source.restarted = true;
		Rankings.Record other = eligibleRecord();
		other.gameID = "other-ranking";
		other.restarted = true;
		ArrayList<Rankings.Record> records = new ArrayList<>();
		records.add(source);
		records.add(other);

		assertTrue(RankingRestart.releaseRestartRecord(records, "source-ranking"));
		assertFalse(source.restarted);
		assertTrue(other.restarted);
		assertFalse(RankingRestart.releaseRestartRecord(records, "missing-ranking"));
	}

	@Test
	public void legacySaveOnlyUnlocksAnUnambiguousSourceRanking() {
		Rankings.Record source = eligibleRecord();
		source.restarted = true;
		ArrayList<Rankings.Record> records = new ArrayList<>();
		records.add(source);

		assertTrue(RankingRestart.releaseRestartRecord(records, null));
		assertFalse(source.restarted);
	}

	@Test
	public void legacySaveDoesNotGuessBetweenMultipleSourceRankings() {
		Rankings.Record first = eligibleRecord();
		first.restarted = true;
		Rankings.Record second = eligibleRecord();
		second.restarted = true;
		ArrayList<Rankings.Record> records = new ArrayList<>();
		records.add(first);
		records.add(second);

		assertFalse(RankingRestart.releaseRestartRecord(records, null));
		assertTrue(first.restarted);
		assertTrue(second.restarted);
	}

	@Test
	public void deletedNewCycleSaveReleasesItsSourceReservation() throws Exception {
		String dungeonSource = readCoreSource("Dungeon.java");
		String gamesInProgressSource = readCoreSource("GamesInProgress.java");
		String rankingRestartSource = readCoreSource("RankingRestart.java");

		assertTrue(dungeonSource.contains("GamesInProgress.Info info = GamesInProgress.check(save);"));
		assertTrue(dungeonSource.contains("RankingRestart.releaseRestartForDeletedSave(info);"));
		assertTrue(dungeonSource.contains("NEW_CYCLE_SOURCE_GAME_ID"));
		assertTrue(gamesInProgressSource.contains(
				"info.newCycleSourceGameID = Dungeon.newCycleSourceGameID;"));
		assertTrue(rankingRestartSource.contains(
				"Dungeon.newCycleSourceGameID = pendingRecord.gameID;"));
	}

	@Test
	public void rankingTrimMayRemoveRecordsReservedByNewCycleSaves() {
		ArrayList<Rankings.Record> records = new ArrayList<>();
		Rankings.Record reserved = eligibleRecord();
		reserved.gameID = "reserved";
		reserved.score = 0;
		reserved.restarted = true;
		records.add(reserved);
		for (int i = 1; i <= Rankings.TABLE_SIZE; i++) {
			Rankings.Record record = eligibleRecord();
			record.gameID = "record-" + i;
			record.score = i;
			records.add(record);
		}

		Rankings.normalizeRecords(records, records.get(records.size() - 1));

		assertEquals(Rankings.TABLE_SIZE, records.size());
		assertFalse(records.contains(reserved));
	}

	@Test
	public void newCycleSeedsDifferForBackToBackRestarts() {
		long first = RankingRestart.timeBasedSeed(1_725_000_000_000L, 123_456L, 1L);
		long second = RankingRestart.timeBasedSeed(1_725_000_000_000L, 123_456L, 2L);

		assertNotEquals(first, second);
		assertTrue(first >= 0 && first < com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed.TOTAL_SEEDS);
		assertTrue(second >= 0 && second < com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed.TOTAL_SEEDS);
	}

	@Test
	public void restartPreparationConvertsConsumablesAtDepthOneAndKeepsEquipment() {
		Dungeon.depth = 7;
		Bag backpack = new Bag();

		DepthPricedItem looseConsumable = new DepthPricedItem();
		looseConsumable.quantity(2);
		DepthPricedItem nestedConsumable = new DepthPricedItem();
		Bag bag = new Bag();
		bag.items.add(nestedConsumable);

		UniqueItem strength = new UniqueItem();
		TestEquipment backpackWeapon = new TestEquipment();
		backpackWeapon.level(3);

		backpack.items.add(looseConsumable);
		backpack.items.add(bag);
		backpack.items.add(strength);
		backpack.items.add(backpackWeapon);

		TestEquipment equippedArmor = new TestEquipment();
		equippedArmor.level(5);
		RankingRestart.Preparation result =
				RankingRestart.prepareInventory(backpack, equippedArmor);

		assertEquals(3, result.convertedGold);
		assertEquals(7, Dungeon.depth);
		assertFalse(backpack.items.contains(looseConsumable));
		assertFalse(bag.items.contains(nestedConsumable));
		assertTrue(backpack.items.contains(strength));
		assertTrue(backpack.items.contains(backpackWeapon));
		assertEquals(0, backpackWeapon.trueLevel());
		assertEquals(0, equippedArmor.trueLevel());
	}

	@Test
	public void restartPreparationDoesNotKeepUnusedUpgradeScrolls() throws Exception {
		String source = readCoreSource("RankingRestart.java");
		assertTrue(source.contains("!(item instanceof ScrollOfUpgrade)"));
	}

	@Test
	public void normalizeUpgradeScrollsKeepsExactlyFifteenAcrossNestedBags() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		Dungeon.hero = hero;

		TestUpgradeScroll first = new TestUpgradeScroll();
		first.quantity(8);
		TestUpgradeScroll second = new TestUpgradeScroll();
		second.quantity(7);
		Bag scrollHolder = new Bag();
		scrollHolder.items.add(second);
		hero.belongings.backpack.items.add(first);
		hero.belongings.backpack.items.add(scrollHolder);

		RankingRestart.normalizeUpgradeScrolls(hero, TestUpgradeScroll.class, new TestUpgradeScroll());

		assertEquals(1, hero.belongings.getAllItems(TestUpgradeScroll.class).size());
		assertEquals(15, hero.belongings.getItem(TestUpgradeScroll.class).quantity());
	}

	@Test
	public void restartPreparationResetsDriedRoseStoredEquipment() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		MeleeWeapon weapon = (MeleeWeapon) unsafe().allocateInstance(MeleeWeapon.class);
		weapon.level(4);
		weapon.upgradeScrollUses = 4;
		Armor armor = (Armor) unsafe().allocateInstance(Armor.class);
		armor.level(3);
		armor.upgradeScrollUses = 3;
		DriedRose rose = roseWithStoredEquipment(weapon, armor, 42);
		hero.belongings.artifact = rose;

		RankingRestart.prepareHero(hero);

		assertEquals(0, rose.ghostWeapon().trueLevel());
		assertEquals(0, rose.ghostWeapon().upgradeScrollUses);
		assertEquals(0, rose.ghostArmor().trueLevel());
		assertEquals(0, rose.ghostArmor().upgradeScrollUses);
		assertEquals(0, storedGhostId(rose));
	}

	@Test
	public void startingGoldUsesScoreAndCapsConvertedInventoryValue() {
		assertEquals(25_000, RankingRestart.calculateStartingGold(2_000_000, 8_000));
		assertEquals(20_000, RankingRestart.calculateStartingGold(2_000_000, 0));
		assertEquals(5_000, RankingRestart.calculateStartingGold(0, 8_000));
		assertEquals(0, RankingRestart.calculateStartingGold(-100, -50));
		assertEquals(Integer.MAX_VALUE,
				RankingRestart.calculateStartingGold(1_000_000_000_000d, 0));
		assertEquals(12_345,
				RankingRestart.calculateStartingGold(1_234_599.99d, 0));
	}

	@Test
	public void restartPreparationCapsInventoryGoldAtFiveThousand() {
		Bag backpack = new Bag();
		DepthPricedItem stockpile = new DepthPricedItem();
		stockpile.quantity(6_000);
		backpack.items.add(stockpile);

		RankingRestart.Preparation result = RankingRestart.prepareInventory(backpack);

		assertEquals(5_000, result.convertedGold);
		assertFalse(backpack.items.contains(stockpile));
	}

	@Test
	public void restartPreparationRemovesTheAmuletDespiteItsUniqueFlag() {
		assertTrue(RankingRestart.isRemovedFromNewCycle(Amulet.class));
		assertFalse(RankingRestart.isRemovedFromNewCycle(TestEquipment.class));
	}

	@Test
	public void carriedArtifactsAreClaimedForTheNewCycleGeneratorPool() {
		Generator.fullReset();
		Bag carriedItems = new Bag();
		carriedItems.items.add(new FirstTestArtifact());
		Bag nestedBag = new Bag();
		nestedBag.items.add(new SecondTestArtifact());
		carriedItems.items.add(nestedBag);

		RankingRestart.claimCarriedArtifacts(carriedItems);

		assertFalse(Generator.claimArtifact(FirstTestArtifact.class));
		assertFalse(Generator.claimArtifact(SecondTestArtifact.class));
	}

	@Test
	public void newCycleFriarAlwaysStartsWithFullReason() {
		TestChar friar = new TestChar();

		RankingRestart.ensureFullReason(friar, HeroClass.FRIAR);

		Reason reason = friar.buff(Reason.class);
		assertTrue(reason != null);
		assertEquals(100, reason.reason);

		reason.reason = 12;
		reason.kaoyan = true;
		RankingRestart.ensureFullReason(friar, HeroClass.FRIAR);

		assertEquals(100, reason.reason);
		assertFalse(reason.kaoyan);
	}

	@Test
	public void newCycleReasonResetDoesNotAffectOtherClasses() {
		TestChar warrior = new TestChar();

		RankingRestart.ensureFullReason(warrior, HeroClass.WARRIOR);

		assertTrue(warrior.buff(Reason.class) == null);
	}

	@Test
	public void restartPreparationRemovesNegativeTalentsFromEveryRepresentation() {
		LinkedHashMap<Talent, Integer> firstTier = new LinkedHashMap<>();
		firstTier.put(Talent.IRON_WILL, 2);
		firstTier.put(Talent.ENDLESS_MALICE, 1);
		java.util.ArrayList<LinkedHashMap<Talent, Integer>> tiers = new java.util.ArrayList<>();
		tiers.add(firstTier);
		LinkedHashMap<Talent, Integer> negativeTalents = new LinkedHashMap<>();
		negativeTalents.put(Talent.ENDLESS_MALICE, 0);

		RankingRestart.clearNegativeTalents(
				tiers, negativeTalents, Talent.ENDLESS_MALICE);

		assertTrue(negativeTalents.isEmpty());
		assertTrue(firstTier.containsKey(Talent.IRON_WILL));
		assertFalse(firstTier.containsKey(Talent.ENDLESS_MALICE));
	}

	@Test
	public void newCycleHealthIsRecalculatedAndFullyRestored() throws Exception {
		HealthTrackingHero hero = headlessHero(HealthTrackingHero.class);
		hero.heroClass = HeroClass.WARRIOR;
		hero.subClass = HeroSubClass.NONE;
		hero.HT = 1;
		hero.HP = 1;

		RankingRestart.refreshHealthForNewCycle(hero);

		assertTrue(hero.updatedHT);
		assertFalse(hero.boostedHPDuringUpdate);
		assertEquals(65, hero.HT);
		assertEquals(hero.HT, hero.HP);
	}

	@Test
	public void newCycleReactivatesEveryEquippedItem() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		TrackingWeapon weapon = new TrackingWeapon();
		TrackingArmor armor = new TrackingArmor();
		TrackingArtifact artifact = new TrackingArtifact();
		TrackingArtifact misc = new TrackingArtifact();
		TrackingWeapon secondWeapon = new TrackingWeapon();
		hero.belongings.weapon = weapon;
		hero.belongings.armor = armor;
		hero.belongings.artifact = artifact;
		hero.belongings.misc = misc;
		hero.belongings.secondWep = secondWeapon;

		RankingRestart.reactivateEquippedItems(hero);

		assertEquals(1, weapon.activationCount);
		assertEquals(1, armor.activationCount);
		assertEquals(1, artifact.activationCount);
		assertEquals(1, misc.activationCount);
		assertEquals(1, secondWeapon.activationCount);
		assertTrue(readCoreSource("RankingRestart.java")
				.contains("hero.belongings.ring"));
	}

	@Test
	public void newCycleRestoreKeepsInheritedPositiveTalentTree() throws Exception {
		Hero original = headlessHero();
		original.heroClass = HeroClass.WARRIOR;
		original.subClass = HeroSubClass.BERSERKER;
		original.armorAbility = new HeroicLeap();
		for (int i = 0; i < Talent.MAX_TALENT_TIERS; i++) {
			original.talents.add(new LinkedHashMap<>());
		}
		original.talents.get(0).put(Talent.EMPOWERING_MEAL, 1);
		original.talents.get(0).put(Talent.BACKUP_BARRIER, 0);
		Talent.initSubclassTalents(original);
		Talent.initArmorTalents(original);

		Talent subclassTalent = original.talents.get(2).keySet().iterator().next();
		Talent armorTalent = original.armorAbility.talents()[0];
		original.talents.get(2).put(subclassTalent, 2);
		original.talents.get(3).put(armorTalent, 3);

		Bundle bundle = new Bundle();
		Dungeon.newCycle = true;
		Talent.storeTalentsInBundle(bundle, original);

		Hero restored = headlessHero();
		restored.heroClass = original.heroClass;
		restored.subClass = original.subClass;
		restored.armorAbility = new HeroicLeap();
		Dungeon.newCycle = true;
		Talent.restoreTalentsFromBundle(bundle, restored);

		for (int i = 0; i < Talent.MAX_TALENT_TIERS; i++) {
			assertEquals(original.talents.get(i).keySet(), restored.talents.get(i).keySet());
		}
		assertEquals(1, (int) restored.talents.get(0).get(Talent.EMPOWERING_MEAL));
		assertEquals(0, (int) restored.talents.get(0).get(Talent.BACKUP_BARRIER));
		assertFalse(restored.talents.get(0).containsKey(Talent.HEARTY_MEAL));
		assertEquals(2, (int) restored.talents.get(2).get(subclassTalent));
		assertEquals(3, (int) restored.talents.get(3).get(armorTalent));
	}

	@Test
	public void completeNewCycleLayoutRestoresBeforeGlobalNewCycleFlag() throws Exception {
		Hero original = headlessHero();
		original.heroClass = HeroClass.WARRIOR;
		original.subClass = HeroSubClass.BERSERKER;
		original.armorAbility = new HeroicLeap();
		for (int i = 0; i < Talent.MAX_TALENT_TIERS; i++) {
			original.talents.add(new LinkedHashMap<>());
		}
		original.talents.get(0).put(Talent.EMPOWERING_MEAL, 1);
		original.talents.get(0).put(Talent.BACKUP_BARRIER, 0);
		Talent.initSubclassTalents(original);
		Talent.initArmorTalents(original);

		Talent subclassTalent = original.talents.get(2).keySet().iterator().next();
		Talent armorTalent = original.armorAbility.talents()[0];
		original.talents.get(2).put(subclassTalent, 2);
		original.talents.get(3).put(armorTalent, 3);

		Bundle bundle = new Bundle();
		Dungeon.newCycle = true;
		Talent.storeTalentsInBundle(bundle, original);

		Hero restored = headlessHero();
		restored.heroClass = original.heroClass;
		restored.subClass = original.subClass;
		restored.armorAbility = new HeroicLeap();
		// Dungeon.loadGame used to restore this flag only after rebuilding the hero.
		Dungeon.newCycle = false;
		Talent.restoreTalentsFromBundle(bundle, restored);

		for (int i = 0; i < Talent.MAX_TALENT_TIERS; i++) {
			assertEquals(original.talents.get(i).keySet(), restored.talents.get(i).keySet());
		}
		assertEquals(1, (int) restored.talents.get(0).get(Talent.EMPOWERING_MEAL));
		assertEquals(2, (int) restored.talents.get(2).get(subclassTalent));
		assertEquals(3, (int) restored.talents.get(3).get(armorTalent));

		ArrayList<LinkedHashMap<Talent, Integer>> sources = Talent.metamorphSources(restored);
		assertTrue(sources.get(0).containsKey(Talent.EMPOWERING_MEAL));
		assertFalse(sources.get(0).containsKey(Talent.POTENTIAL_1));
		Dungeon.newCycle = true;
		assertTrue(HeroRandomizer.randomInitialTalentNeedsMetaDesc(restored, Talent.EMPOWERING_MEAL));
	}

	@Test
	public void legacyNewCycleRestoreKeepsInvestedInheritedTalents() throws Exception {
		Hero original = headlessHero();
		original.heroClass = HeroClass.WARRIOR;
		Talent.initClassTalents(original);
		original.talents.get(0).put(Talent.EMPOWERING_MEAL, 1);

		Bundle bundle = new Bundle();
		Dungeon.newCycle = false;
		Talent.storeTalentsInBundle(bundle, original);

		Hero restored = headlessHero();
		restored.heroClass = HeroClass.WARRIOR;
		Dungeon.newCycle = true;
		Talent.restoreTalentsFromBundle(bundle, restored);

		assertEquals(1, (int) restored.talents.get(0).get(Talent.EMPOWERING_MEAL));
	}

	@Test
	public void normalRestoreStillRejectsTalentsOutsideCurrentTalentTree() throws Exception {
		Hero original = headlessHero();
		original.heroClass = HeroClass.WARRIOR;
		Talent.initClassTalents(original);
		original.talents.get(0).put(Talent.EMPOWERING_MEAL, 1);

		Bundle bundle = new Bundle();
		Talent.storeTalentsInBundle(bundle, original);

		Hero restored = headlessHero();
		restored.heroClass = HeroClass.WARRIOR;
		Dungeon.newCycle = false;
		Talent.restoreTalentsFromBundle(bundle, restored);

		assertFalse(restored.talents.get(0).containsKey(Talent.EMPOWERING_MEAL));
	}

	@Test
	public void briefcaseWeaponMasterySurvivesRankingBuffCleanup() throws Exception {
		Hero hero = headlessHeroWithBelongings();
		hero.heroClass = HeroClass.FREEMAN;
		hero.subClass = HeroSubClass.NONE;
		Dungeon.hero = hero;

		hero.grantMartialMastery();
		for (com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff
				: new ArrayList<>(hero.buffs())) {
			if (!(buff instanceof MeleeWeapon.Charger)) {
				buff.detach();
			}
		}

		assertTrue(MeleeWeapon.canUseWeaponAbility(hero));
	}

	private static Hero headlessHero() throws Exception {
		return headlessHero(Hero.class);
	}

	private static <T extends Hero> T headlessHero(Class<T> type) throws Exception {
		T hero = (T) unsafe().allocateInstance(type);
		hero.talents = new ArrayList<>();
		hero.metamorphedTalents = new LinkedHashMap<>();
		hero.sublimationTalents = new LinkedHashMap<>();
		hero.negativeTalents = new LinkedHashMap<>();
		Field buffs = Char.class.getDeclaredField("buffs");
		buffs.setAccessible(true);
		buffs.set(hero, new LinkedHashSet<>());
		Field resistances = Char.class.getDeclaredField("resistances");
		resistances.setAccessible(true);
		resistances.set(hero, new java.util.HashSet<>());
		Field immunities = Char.class.getDeclaredField("immunities");
		immunities.setAccessible(true);
		immunities.set(hero, new java.util.HashSet<>());
		Field properties = Char.class.getDeclaredField("properties");
		properties.setAccessible(true);
		properties.set(hero, new java.util.HashSet<>());
		return hero;
	}

	private static Hero headlessHeroWithBelongings() throws Exception {
		Hero hero = headlessHero();
		hero.heroClass = HeroClass.WARRIOR;
		hero.subClass = HeroSubClass.NONE;
		hero.HP = hero.HT = 20;

		Belongings belongings = (Belongings) unsafe().allocateInstance(Belongings.class);
		Belongings.Backpack backpack =
				(Belongings.Backpack) unsafe().allocateInstance(Belongings.Backpack.class);
		backpack.items = new ArrayList<>();
		backpack.owner = hero;
		belongings.backpack = backpack;
		Field owner = Belongings.class.getDeclaredField("owner");
		owner.setAccessible(true);
		owner.set(belongings, hero);
		hero.belongings = belongings;
		return hero;
	}

	private static Unsafe unsafe() throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		return (Unsafe) unsafeField.get(null);
	}

	private static <T extends Bag> T emptyBag(Class<T> type) throws Exception {
		T bag = (T) unsafe().allocateInstance(type);
		bag.items = new ArrayList<>();
		return bag;
	}

	private static Waterskin waterskin(int volume) throws Exception {
		Waterskin waterskin = (Waterskin) unsafe().allocateInstance(Waterskin.class);
		waterskin.volume = volume;
		return waterskin;
	}

	private static DriedRose roseWithStoredEquipment(
			MeleeWeapon weapon, Armor armor, int ghostId) throws Exception {
		DriedRose rose = (DriedRose) unsafe().allocateInstance(DriedRose.class);
		setField(DriedRose.class, rose, "weapon", weapon);
		setField(DriedRose.class, rose, "armor", armor);
		setField(DriedRose.class, rose, "ghostID", ghostId);
		return rose;
	}

	private static int storedGhostId(DriedRose rose) throws Exception {
		Field ghostId = DriedRose.class.getDeclaredField("ghostID");
		ghostId.setAccessible(true);
		return ghostId.getInt(rose);
	}

	private static void setField(Class<?> type, Object target, String fieldName, Object value)
			throws Exception {
		Field field = type.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static String readCoreSource(String fileName) throws Exception {
		Path coreDirectory = Paths.get(System.getProperty("user.dir"));
		if (!Files.isDirectory(coreDirectory.resolve("src/main/java"))) {
			coreDirectory = coreDirectory.resolve("core");
		}
		return new String(Files.readAllBytes(coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon").resolve(fileName)),
				StandardCharsets.UTF_8);
	}

	private static class HealthTrackingHero extends Hero {
		boolean updatedHT;
		boolean boostedHPDuringUpdate;

		@Override
		public void updateHT(boolean boostHP) {
			updatedHT = true;
			boostedHPDuringUpdate = boostHP;
			HT = 65;
		}
	}

	private Rankings.Record pendingRestartRecord() throws Exception {
		Field field = RankingRestart.class.getDeclaredField("pendingRecord");
		field.setAccessible(true);
		return (Rankings.Record) field.get(null);
	}

	private Rankings.Record eligibleRecord() {
		Rankings.Record record = new Rankings.Record();
		record.win = true;
		record.heroClass = HeroClass.WARRIOR;
		record.customSeed = "";
		record.gameData = new Bundle();
		record.gameData.put(Rankings.CHALLENGES, 0);
		return record;
	}

	private static class DepthPricedItem extends Item {
		@Override
		public int value() {
			return Dungeon.depth * quantity();
		}
	}

	private static class UniqueItem extends Item {
		UniqueItem() {
			unique = true;
		}
	}

	private static class TestUpgradeScroll extends Item {
	}

	public static class PersistentStateItem extends Item {
		private static final String STATE = "state";
		int state;

		public PersistentStateItem() {
		}

		PersistentStateItem(int state) {
			this.state = state;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STATE, state);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			state = bundle.getInt(STATE);
		}
	}

	private static class TestEquipment extends EquipableItem {
		@Override
		public boolean doEquip(Hero hero) {
			return false;
		}
	}

	private static class TrackingWeapon extends MeleeWeapon {
		int activationCount;

		@Override
		public void activate(Char ch) {
			activationCount++;
		}
	}

	private static class TrackingArmor extends Armor {
		int activationCount;

		private TrackingArmor() {
			super(1);
		}

		@Override
		public void activate(Char ch) {
			activationCount++;
		}
	}

	private static class TrackingArtifact extends Artifact {
		int activationCount;

		@Override
		public void activate(Char ch) {
			activationCount++;
		}
	}

	private static class FirstTestArtifact extends Artifact {
	}

	private static class SecondTestArtifact extends Artifact {
	}

	private static class TestChar extends Char {
		@Override
		protected boolean act() {
			return true;
		}
	}

}
