package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
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
import static org.junit.Assert.assertTrue;

public class RankingRestartTest {

	@After
	public void resetDungeonState() {
		Dungeon.depth = 0;
		Dungeon.challenges = 0;
		Dungeon.newCycle = false;
		Dungeon.newCycleSourceGameID = null;
		Dungeon.hero = null;
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
	public void rankingTrimKeepsRecordsReservedByNewCycleSaves() {
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
		assertTrue(records.contains(reserved));
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

	private static class TestEquipment extends EquipableItem {
		@Override
		public boolean doEquip(Hero hero) {
			return false;
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
