package com.shatteredpixel.shatteredpixeldungeon.items.treasures;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TreasureGeneratorTest {

	@Test
	public void collectionRarityThresholdsMatchOneFourTenEightyFivePercent() {
		assertEquals(Treasures.CollectionRarity.TOP,
				TreasureGenerator.collectionRarityForRoll(0f));
		assertEquals(Treasures.CollectionRarity.TOP,
				TreasureGenerator.collectionRarityForRoll(0.009999f));

		assertEquals(Treasures.CollectionRarity.RARE,
				TreasureGenerator.collectionRarityForRoll(0.01f));
		assertEquals(Treasures.CollectionRarity.RARE,
				TreasureGenerator.collectionRarityForRoll(0.049999f));

		assertEquals(Treasures.CollectionRarity.UNCOMMON,
				TreasureGenerator.collectionRarityForRoll(0.05f));
		assertEquals(Treasures.CollectionRarity.UNCOMMON,
				TreasureGenerator.collectionRarityForRoll(0.149999f));

		assertEquals(Treasures.CollectionRarity.COMMON,
				TreasureGenerator.collectionRarityForRoll(0.15f));
		assertEquals(Treasures.CollectionRarity.COMMON,
				TreasureGenerator.collectionRarityForRoll(0.999999f));
	}

	@Test
	public void fixedRarityGroupsContainExactlyTheApprovedClasses() {
		assertArrayEquals(new Class[]{GoryeoMaebyeong.class, JavaneseGoldCup.class,
				EthiopianProcessionalCross.class, GreatZimbabweBird.class, DjenneTerracottaFigure.class},
				TreasureGenerator.classesFor(Treasures.CollectionRarity.TOP));
		assertArrayEquals(new Class[]{
						LewisChessQueen.class, HarbavilleTriptych.class,
						AlMughiraPyxis.class, BlacasEwer.class, GreatKhanPaiza.class
				},
				TreasureGenerator.classesFor(Treasures.CollectionRarity.RARE));
		assertArrayEquals(new Class[]{
						CholaNataraja.class,
						DojigiriYasutsuna.class,
						TurquoiseSerpent.class,
						RuWareBowl.class,
						IncaGoldenLlama.class
				},
				TreasureGenerator.classesFor(Treasures.CollectionRarity.UNCOMMON));
		assertArrayEquals(new Class[]{
						MuiscaGoldenRaft.class, ImperialCrown.class, PakalJadeMask.class,
						SuttonHooHelmet.class, BookOfKells.class
				},
				TreasureGenerator.classesFor(Treasures.CollectionRarity.COMMON));
	}

	@Test
	public void configuredEconomyHasFiveThousandExpectedRevenuePerRaid() throws Exception {
		double topBase = averageCommonQualityValue(
				TreasureGenerator.classesFor(Treasures.CollectionRarity.TOP));
		double rareBase = averageCommonQualityValue(
				TreasureGenerator.classesFor(Treasures.CollectionRarity.RARE));
		double uncommonBase = averageCommonQualityValue(
				TreasureGenerator.classesFor(Treasures.CollectionRarity.UNCOMMON));
		double commonBase = averageCommonQualityValue(
				TreasureGenerator.classesFor(Treasures.CollectionRarity.COMMON));

		double expectedBase = 0.01 * topBase
				+ 0.04 * rareBase
				+ 0.10 * uncommonBase
				+ 0.85 * commonBase;
		double expectedQualityMultiplier = 0.001 * 10
				+ 0.01 * 5
				+ 0.10 * 2
				+ 0.889;
		double expectedTopLegendaryPremium = 0.01 * 0.001 * 4_950_000;
		double expectedUnitValue =
				expectedBase * expectedQualityMultiplier + expectedTopLegendaryPremium;

		assertEquals(1200.5, expectedBase, 0.000001);
		assertEquals(1.149, expectedQualityMultiplier, 0.000001);
		assertEquals(49.5, expectedTopLegendaryPremium, 0.000001);
		assertEquals(1428.8745, expectedUnitValue, 0.000001);
		assertEquals(5001.06075, expectedUnitValue * 3.5, 0.000001);
	}

	private static double averageCommonQualityValue(
			Class<? extends Treasures>[] classes) throws Exception {
		double total = 0;
		for (Class<? extends Treasures> treasureClass : classes) {
			Treasures treasure = treasureClass.getDeclaredConstructor().newInstance();
			treasure.setRarity(Treasures.Rarity.COMMON);
			total += treasure.value();
		}
		return total / classes.length;
	}

	@Test public void seededGenerationKeepsExpectedFrequencyAndQualityAdjustedValue() {
		int samples = 200000;
		int[] counts = new int[4];
		long qualityValue = 0;
		com.watabou.utils.Random.pushGenerator(0x554553L);
		try {
			for (int i = 0; i < samples; i++) {
				Treasures treasure = TreasureGenerator.random();
				counts[treasure.collectionRarity().ordinal()]++;
				int value = treasure.value();
				if (treasure.collectionRarity() == Treasures.CollectionRarity.TOP
						&& treasure.rarity() == Treasures.Rarity.LEGENDARY) value -= 4_950_000;
				qualityValue += value;
			}
		} finally {
			com.watabou.utils.Random.popGenerator();
		}
		assertEquals(0.85, counts[Treasures.CollectionRarity.COMMON.ordinal()] / (double) samples, 0.003);
		assertEquals(0.10, counts[Treasures.CollectionRarity.UNCOMMON.ordinal()] / (double) samples, 0.003);
		assertEquals(0.04, counts[Treasures.CollectionRarity.RARE.ordinal()] / (double) samples, 0.002);
		assertEquals(0.01, counts[Treasures.CollectionRarity.TOP.ordinal()] / (double) samples, 0.001);
		// Add the analytic jackpot expectation, avoiding extreme noise from very rare jackpots.
		double projectedRaidValue = (qualityValue / (double) samples + 49.5) * 3.5;
		assertEquals(5001.06075, projectedRaidValue, 80);
		System.out.println("UES economy: samples=" + samples + ", projected raid value=" + projectedRaidValue);
	}
}
