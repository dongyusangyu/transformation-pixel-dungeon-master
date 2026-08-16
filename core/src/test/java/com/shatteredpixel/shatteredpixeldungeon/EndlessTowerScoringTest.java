package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EndlessTowerScoringTest {

	private final long originalSeed = Dungeon.seed;

	@After
	public void restoreGlobals() {
		Dungeon.seed = originalSeed;
		Statistics.reset();
	}

	@Test
	public void towerDispatchHasNoHundredFloorLimit() {
		assertNotNull(Dungeon.towerLevelClassForLocation(101, TowerLevel.BRANCH));
		assertNotNull(Dungeon.towerLevelClassForLocation(1_000, TowerLevel.BRANCH));
		assertNotNull(Dungeon.towerLevelClassForLocation(1_000_001, TowerLevel.BRANCH));
	}

	@Test
	public void deepTowerSeedsAreStableAndFloorSpecific() {
		Dungeon.seed = 0x1234_5678_9ABCL;
		long first = Dungeon.seedForDepth(1_000_000, TowerLevel.BRANCH);
		assertEquals(first, Dungeon.seedForDepth(1_000_000, TowerLevel.BRANCH));
		assertNotEquals(first, Dungeon.seedForDepth(1_000_001, TowerLevel.BRANCH));
	}

	@Test
	public void oldSavesRecoverHistoricalTowerDepthFromGeneratedLevels() {
		int[] generated = {
				Dungeon.generatedLevelKey(10, TowerLevel.BRANCH),
				Dungeon.generatedLevelKey(101, TowerLevel.BRANCH),
				Dungeon.generatedLevelKey(25, 0)
		};

		assertEquals(101, Dungeon.deepestTowerFloorInGeneratedLevels(generated));
	}

	@Test
	public void towerDepthIsTrackedSeparatelyAndSurvivesSave() {
		Statistics.reset();
		Statistics.observeDepth(25, 0);
		Statistics.observeDepth(101, TowerLevel.BRANCH);

		assertEquals(25, Statistics.deepestFloor);
		assertEquals(101, Statistics.deepestTowerFloor);

		Bundle bundle = new Bundle();
		Statistics.storeInBundle(bundle);
		Statistics.reset();
		Statistics.restoreFromBundle(bundle);

		assertEquals(25, Statistics.deepestFloor);
		assertEquals(101, Statistics.deepestTowerFloor);
	}

	@Test
	public void oldStatisticsDefaultTowerDepthToCurrentTowerFloor() {
		Bundle oldSave = new Bundle();
		oldSave.put("maxDepth", 25);
		oldSave.put("negativetalents", new boolean[4]);

		Statistics.restoreFromBundle(oldSave);
		Statistics.recoverTowerDepth(101, TowerLevel.BRANCH);

		assertEquals(101, Statistics.deepestTowerFloor);
	}

	@Test
	public void exploredTowerFloorsSurviveSaveWithoutFixedRange() {
		Statistics.reset();
		Statistics.floorsExplored.put(1, 1f);
		Statistics.floorsExplored.put(-1, 0.9f);
		Statistics.floorsExplored.put(-101, 0.75f);
		Statistics.floorsExplored.put(-1_001, 0.5f);

		Bundle bundle = new Bundle();
		Statistics.storeInBundle(bundle);
		Statistics.reset();
		Statistics.restoreFromBundle(bundle);

		assertEquals(1f, Statistics.floorsExplored.get(1), 0f);
		assertEquals(0.9f, Statistics.floorsExplored.get(-1), 0f);
		assertEquals(0.75f, Statistics.floorsExplored.get(-101), 0f);
		assertEquals(0.5f, Statistics.floorsExplored.get(-1_001), 0f);
	}

	@Test
	public void newCycleUsesTowerDepthAndDoesNotCapScores() {
		assertEquals(101, Rankings.progressDepth(true, 25, 101));
		assertEquals(25, Rankings.progressDepth(false, 25, 101));
		assertEquals(5_000_000_000d,
				Rankings.applyScoreLimit(5_000_000_000d, 50_000d, true), 0d);
		assertEquals(50_000d,
				Rankings.applyScoreLimit(5_000_000_000d, 50_000d, false), 0d);
	}

	@Test
	public void totalScoreCanExceedIntegerRange() {
		double total = Rankings.combineScore(
				2_000_000_000d,
				1_500_000_000d,
				750_000_000d,
				250_000_000d,
				125_000_000d,
				2d,
				1.25d);

		assertEquals(11_562_500_000d, total, 0d);
		assertTrue(total > Integer.MAX_VALUE);
	}

	@Test
	public void bundleReadsLegacyIntegersAndStoresDoubles() {
		Bundle bundle = new Bundle();
		bundle.put("legacy", 123);
		bundle.put("score", 3_000_000_000.25d);
		bundle.put("scores", new double[]{1d, 2_500_000_000.5d});

		assertEquals(123d, bundle.getDouble("legacy"), 0d);
		assertEquals(3_000_000_000.25d, bundle.getDouble("score"), 0d);
		assertEquals(2_500_000_000.5d, bundle.getDoubleArray("scores")[1], 0d);
	}

	@Test
	public void statisticsReadsLegacyGoldAndScoreArraysWithoutNarrowing() {
		Bundle oldSave = new Bundle();
		oldSave.put("score", Integer.MAX_VALUE);
		oldSave.put("boss_scores", new int[]{1_000, 2_000});
		oldSave.put("quest_scores", new int[]{3_000});
		oldSave.put("negativetalents", new boolean[4]);

		Statistics.restoreFromBundle(oldSave);

		assertEquals((long) Integer.MAX_VALUE, Statistics.goldCollected);
		assertEquals(10, Statistics.bossScores.length);
		assertEquals(2_000d, Statistics.bossScores[1], 0d);
		assertEquals(5, Statistics.questScores.length);
		assertEquals(3_000d, Statistics.questScores[0], 0d);
		assertEquals(0d, Statistics.questScores[4], 0d);
	}

	@Test
	public void unlimitedScoreSourcesSurviveStatisticsSave() {
		Statistics.reset();
		Statistics.goldCollected = 5_000_000_000L;
		Statistics.bossScores[0] = 6_000_000_000d;
		Statistics.questScores[0] = 7_000_000_000d;
		Bundle bundle = new Bundle();
		Statistics.storeInBundle(bundle);

		Statistics.reset();
		Statistics.restoreFromBundle(bundle);

		assertEquals(5_000_000_000L, Statistics.goldCollected);
		assertEquals(6_000_000_000d, Statistics.bossScores[0], 0d);
		assertEquals(7_000_000_000d, Statistics.questScores[0], 0d);
	}

	@Test
	public void newCycleSavePreviewUsesTowerDepthAndMigratesCurrentFloor() {
		GamesInProgress.Info info = new GamesInProgress.Info();
		info.newCycle = true;
		info.branch = TowerLevel.BRANCH;
		info.depth = 101;
		Statistics.preview(info, new Bundle());

		assertEquals(101, info.maxDepth);
	}

	@Test
	public void rankingRecordPreservesAndSortsLargeDoubleScores() {
		Rankings.Record lower = record(3_000_000_000.25d, "lower");
		Rankings.Record higher = record(8_000_000_000.5d, "higher");
		Bundle bundle = new Bundle();
		higher.storeInBundle(bundle);

		Rankings.Record restored = new Rankings.Record();
		restored.restoreFromBundle(bundle);

		assertEquals(higher.score, restored.score, 0d);
		assertTrue(Rankings.scoreComparator.compare(lower, higher) > 0);
		assertTrue(Rankings.scoreComparator.compare(higher, lower) < 0);
	}

	private Rankings.Record record(double score, String id) {
		Rankings.Record record = new Rankings.Record();
		record.score = score;
		record.heroClass = HeroClass.WARRIOR;
		record.customSeed = "";
		record.gameID = id;
		return record;
	}
}
