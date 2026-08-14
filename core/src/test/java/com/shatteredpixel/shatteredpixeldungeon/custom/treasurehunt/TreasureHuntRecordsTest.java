package com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt;

import com.watabou.utils.Bundle;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreasureHuntRecordsTest {

	@Before
	public void resetRecords() {
		TreasureHuntRecords.reset();
	}

	@Test
	public void tenStepCanStartOnlyOnce() {
		assertTrue(TreasureHuntRecords.beginTenStep());
		assertFalse(TreasureHuntRecords.beginTenStep());
		assertTrue(TreasureHuntRecords.tenStepStarted());
	}

	@Test
	public void tenStepRewardCanBeClaimedOnlyOnce() {
		assertFalse(TreasureHuntRecords.claimTenStepReward());

		TreasureHuntRecords.beginTenStep();
		TreasureHuntRecords.updateTenStepScore(650);

		assertTrue(TreasureHuntRecords.canClaimTenStepReward());
		assertTrue(TreasureHuntRecords.claimTenStepReward());
		assertFalse(TreasureHuntRecords.claimTenStepReward());
		assertFalse(TreasureHuntRecords.canClaimTenStepReward());
		assertEquals(650, TreasureHuntRecords.tenStepScore());
	}

	@Test
	public void claimedTenStepScoreCannotBeChanged() {
		TreasureHuntRecords.beginTenStep();
		TreasureHuntRecords.updateTenStepProgress(500, false);
		TreasureHuntRecords.claimTenStepReward();

		TreasureHuntRecords.updateTenStepProgress(900, true);

		assertEquals(500, TreasureHuntRecords.tenStepScore());
		assertFalse(TreasureHuntRecords.tenStepCompleted());
	}

	@Test
	public void completedTenStepStateSurvivesBundleRoundTrip() {
		TreasureHuntRecords.beginTenStep();
		TreasureHuntRecords.updateTenStepProgress(640, true);

		Bundle bundle = new Bundle();
		TreasureHuntRecords.storeInBundle(bundle);
		TreasureHuntRecords.reset();
		TreasureHuntRecords.restoreFromBundle(bundle);

		assertEquals(640, TreasureHuntRecords.tenStepScore());
		assertTrue(TreasureHuntRecords.tenStepCompleted());
	}

	@Test
	public void endlessLeaderboardKeepsFiveHighestScoresInDescendingOrder() {
		TreasureHuntRecords.recordEndlessScore(125);
		TreasureHuntRecords.recordEndlessScore(450);
		TreasureHuntRecords.recordEndlessScore(300);
		TreasureHuntRecords.recordEndlessScore(700);
		TreasureHuntRecords.recordEndlessScore(50);
		TreasureHuntRecords.recordEndlessScore(600);

		assertArrayEquals(new int[]{700, 600, 450, 300, 125},
				TreasureHuntRecords.topScores());
		assertEquals(600, TreasureHuntRecords.lastEndlessScore());
		assertEquals(700, TreasureHuntRecords.bestEndlessScore());
		assertEquals(6, TreasureHuntRecords.endlessGamesPlayed());
		assertEquals(2225, TreasureHuntRecords.totalEndlessScore());
	}

	@Test
	public void bundleRoundTripPreservesAllRecords() {
		TreasureHuntRecords.beginTenStep();
		TreasureHuntRecords.updateTenStepScore(810);
		TreasureHuntRecords.recordEndlessScore(240);
		TreasureHuntRecords.recordEndlessScore(720);

		Bundle bundle = new Bundle();
		TreasureHuntRecords.storeInBundle(bundle);
		TreasureHuntRecords.reset();
		TreasureHuntRecords.restoreFromBundle(bundle);

		assertTrue(TreasureHuntRecords.tenStepStarted());
		assertTrue(TreasureHuntRecords.canClaimTenStepReward());
		assertEquals(810, TreasureHuntRecords.tenStepScore());
		assertFalse(TreasureHuntRecords.tenStepCompleted());
		assertEquals(720, TreasureHuntRecords.lastEndlessScore());
		assertEquals(720, TreasureHuntRecords.bestEndlessScore());
		assertEquals(2, TreasureHuntRecords.endlessGamesPlayed());
		assertEquals(960, TreasureHuntRecords.totalEndlessScore());
		assertArrayEquals(new int[]{720, 240}, TreasureHuntRecords.topScores());
	}

	@Test
	public void extractionRaidSettlementCountTracksEverySettlement() {
		for (int settlement = 0; settlement < 4; settlement++) {
			TreasureHuntRecords.recordExtractionRaidSettlement();
		}

		assertEquals(4, TreasureHuntRecords.extractionRaidSettlements());

		TreasureHuntRecords.recordExtractionRaidSettlement();

		assertEquals(5, TreasureHuntRecords.extractionRaidSettlements());

		for (int settlement = 0; settlement < 5; settlement++) {
			TreasureHuntRecords.recordExtractionRaidSettlement();
		}

		assertEquals(10, TreasureHuntRecords.extractionRaidSettlements());
	}

	@Test
	public void extractionRaidSettlementCountSurvivesBundleRoundTrip() {
		for (int settlement = 0; settlement < 7; settlement++) {
			TreasureHuntRecords.recordExtractionRaidSettlement();
		}

		Bundle bundle = new Bundle();
		TreasureHuntRecords.storeInBundle(bundle);
		TreasureHuntRecords.reset();
		TreasureHuntRecords.restoreFromBundle(bundle);

		assertEquals(7, TreasureHuntRecords.extractionRaidSettlements());
	}

	@Test
	public void missingOldSaveDataRestoresInitialState() {
		TreasureHuntRecords.beginTenStep();
		TreasureHuntRecords.recordEndlessScore(999);

		TreasureHuntRecords.restoreFromBundle(new Bundle());

		assertFalse(TreasureHuntRecords.tenStepStarted());
		assertFalse(TreasureHuntRecords.canClaimTenStepReward());
		assertEquals(0, TreasureHuntRecords.endlessGamesPlayed());
		assertEquals(0, TreasureHuntRecords.extractionRaidSettlements());
		assertArrayEquals(new int[0], TreasureHuntRecords.topScores());
	}
}
