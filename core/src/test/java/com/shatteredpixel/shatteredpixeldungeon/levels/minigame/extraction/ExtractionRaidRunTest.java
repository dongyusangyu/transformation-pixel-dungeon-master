package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRecords;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.GreatZimbabweBird;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExtractionRaidRunTest {

	@Test
	public void removesOnlyCurrentRaidLootRecursively() {
		Bag backpack = new Bag();
		Bag nested = new Bag();
		Item current = new TestItem().markForExtractionRaid(77);
		Item old = new TestItem().markForExtractionRaid(76);
		Item normal = new TestItem();
		nested.items.add(current);
		backpack.items.add(nested);
		backpack.items.add(old);
		backpack.items.add(normal);

		assertEquals(1, ExtractionRaidRun.removeRaidLoot(backpack, 77));
		assertFalse(nested.items.contains(current));
		assertTrue(backpack.items.contains(old));
		assertTrue(backpack.items.contains(normal));
	}

	@Test
	public void clearsOnlyCurrentRaidMarksRecursively() {
		Bag backpack = new Bag();
		Bag nested = new Bag();
		Item current = new TestItem().markForExtractionRaid(77);
		Item old = new TestItem().markForExtractionRaid(76);
		nested.items.add(current);
		backpack.items.add(nested);
		backpack.items.add(old);

		assertEquals(1, ExtractionRaidRun.clearRaidMarks(backpack, 77));
		assertEquals(0, current.extractionRaidId());
		assertEquals(76, old.extractionRaidId());
	}

	@Test
	public void ordinaryLossCandidateProtectsValuableItems() {
		assertTrue(ExtractionRaidRun.isOrdinaryConsumableClass(PotionOfHealing.class));
		assertTrue(ExtractionRaidRun.isOrdinaryConsumableClass(ScrollOfIdentify.class));
		assertFalse(ExtractionRaidRun.isOrdinaryConsumableClass(PotionOfStrength.class));
		assertFalse(ExtractionRaidRun.isOrdinaryConsumableClass(ScrollOfUpgrade.class));
		assertFalse(ExtractionRaidRun.isOrdinaryConsumableClass(GreatZimbabweBird.class));
		assertFalse(ExtractionRaidRun.isOrdinaryConsumableClass(PotionBandolier.class));
	}

	@Test
	public void raidSessionRoundTripsThroughBundle() {
		ExtractionRaidRun.RaidSession source =
				new ExtractionRaidRun.RaidSession().configure(1234, 0, 0, 219);
		Bundle bundle = new Bundle();
		source.storeInBundle(bundle);
		ExtractionRaidRun.RaidSession restored = new ExtractionRaidRun.RaidSession();
		restored.restoreFromBundle(bundle);

		assertEquals(1234, restored.raidId());
		assertEquals(0, restored.returnDepth());
		assertEquals(0, restored.returnBranch());
		assertEquals(219, restored.returnPos());
	}

	@Test
	public void onlyTwoKeysGrantExtractionBonus() {
		assertEquals(0, ExtractionRaidRun.bonusForKeyCount(0));
		assertEquals(0, ExtractionRaidRun.bonusForKeyCount(1));
		assertEquals(2000, ExtractionRaidRun.bonusForKeyCount(2));
		assertEquals(2000, ExtractionRaidRun.bonusForKeyCount(3));
	}

	@Test
	public void entryRequiresFullFee() {
		assertFalse(ExtractionRaidRun.canPayEntry(999));
		assertTrue(ExtractionRaidRun.canPayEntry(1000));
		assertTrue(ExtractionRaidRun.canPayEntry(5000));
		assertTrue(ExtractionRaidLevel.isRaidLocation(31, 1));
		assertFalse(ExtractionRaidLevel.isRaidLocation(31, 0));
	}

	@Test
	public void ordinaryLossCountsUnitsRatherThanStacks() {
		assertEquals(0, ExtractionRaidRun.ordinaryLossCount(0, 2));
		assertEquals(1, ExtractionRaidRun.ordinaryLossCount(1, 2));
		assertEquals(2, ExtractionRaidRun.ordinaryLossCount(10, 2));
	}

	@Test
	public void onlySuccessfulExtractionAndDeathAdvanceMerchantPurchaseCycle() throws IOException {
		TreasureHuntRecords.reset();

		ExtractionRaidRun.recordMerchantPurchaseCycleSettlement(true, false);
		assertEquals(1, TreasureHuntRecords.extractionRaidSettlements());

		ExtractionRaidRun.recordMerchantPurchaseCycleSettlement(false, true);
		assertEquals(2, TreasureHuntRecords.extractionRaidSettlements());

		ExtractionRaidRun.recordMerchantPurchaseCycleSettlement(false, false);
		assertEquals(2, TreasureHuntRecords.extractionRaidSettlements());

		String treasureHuntWindow = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java");

		assertFalse(treasureHuntWindow.contains("recordExtractionRaidSettlement"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static class TestItem extends Item {
	}
}
