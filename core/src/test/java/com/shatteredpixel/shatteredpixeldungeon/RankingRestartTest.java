package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
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

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RankingRestartTest {

	@After
	public void resetDungeonState() {
		Dungeon.depth = 0;
		Dungeon.challenges = 0;
		Generator.fullReset();
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
