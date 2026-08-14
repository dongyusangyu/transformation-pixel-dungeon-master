/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt;

import com.watabou.utils.Bundle;

import java.util.Arrays;

public final class TreasureHuntRecords {

	private static final int MAX_RANKED_SCORES = 5;

	private static final String RECORDS = "treasure_hunt_records";
	private static final String TEN_STEP_STARTED = "ten_step_started";
	private static final String TEN_STEP_REWARD_CLAIMED = "ten_step_reward_claimed";
	private static final String TEN_STEP_SCORE = "ten_step_score";
	private static final String TEN_STEP_COMPLETED = "ten_step_completed";
	private static final String LAST_ENDLESS_SCORE = "last_endless_score";
	private static final String BEST_ENDLESS_SCORE = "best_endless_score";
	private static final String ENDLESS_GAMES_PLAYED = "endless_games_played";
	private static final String TOTAL_ENDLESS_SCORE = "total_endless_score";
	private static final String TOP_SCORES = "top_scores";
	private static final String EXTRACTION_RAID_SETTLEMENTS = "extraction_raid_settlements";

	private static boolean tenStepStarted;
	private static boolean tenStepRewardClaimed;
	private static int tenStepScore;
	private static boolean tenStepCompleted;

	private static int lastEndlessScore;
	private static int bestEndlessScore;
	private static int endlessGamesPlayed;
	private static int totalEndlessScore;
	private static int[] topScores = new int[0];
	private static int extractionRaidSettlements;

	private TreasureHuntRecords() {
	}

	public static void reset() {
		tenStepStarted = false;
		tenStepRewardClaimed = false;
		tenStepScore = 0;
		tenStepCompleted = false;

		lastEndlessScore = 0;
		bestEndlessScore = 0;
		endlessGamesPlayed = 0;
		totalEndlessScore = 0;
		topScores = new int[0];
		extractionRaidSettlements = 0;
	}

	public static boolean beginTenStep() {
		if (tenStepStarted) {
			return false;
		}

		tenStepStarted = true;
		tenStepRewardClaimed = false;
		tenStepScore = 0;
		tenStepCompleted = false;
		return true;
	}

	public static boolean tenStepStarted() {
		return tenStepStarted;
	}

	public static void updateTenStepScore(int score) {
		updateTenStepProgress(score, false);
	}

	public static void updateTenStepProgress(int score, boolean completed) {
		if (tenStepStarted && !tenStepRewardClaimed) {
			tenStepScore = Math.max(tenStepScore, Math.max(0, score));
			tenStepCompleted |= completed;
		}
	}

	public static int tenStepScore() {
		return tenStepScore;
	}

	public static boolean tenStepCompleted() {
		return tenStepCompleted;
	}

	public static boolean canClaimTenStepReward() {
		return tenStepStarted && !tenStepRewardClaimed;
	}

	public static boolean claimTenStepReward() {
		if (!canClaimTenStepReward()) {
			return false;
		}

		tenStepRewardClaimed = true;
		return true;
	}

	public static void recordEndlessScore(int score) {
		score = Math.max(0, score);
		lastEndlessScore = score;
		bestEndlessScore = Math.max(bestEndlessScore, score);
		endlessGamesPlayed = saturatedAdd(endlessGamesPlayed, 1);
		totalEndlessScore = saturatedAdd(totalEndlessScore, score);
		insertRankedScore(score);
	}

	public static int lastEndlessScore() {
		return lastEndlessScore;
	}

	public static int bestEndlessScore() {
		return bestEndlessScore;
	}

	public static int endlessGamesPlayed() {
		return endlessGamesPlayed;
	}

	public static int totalEndlessScore() {
		return totalEndlessScore;
	}

	public static int[] topScores() {
		return Arrays.copyOf(topScores, topScores.length);
	}

	public static void recordExtractionRaidSettlement() {
		extractionRaidSettlements = saturatedAdd(extractionRaidSettlements, 1);
	}

	public static int extractionRaidSettlements() {
		return extractionRaidSettlements;
	}

	public static void storeInBundle(Bundle bundle) {
		Bundle records = new Bundle();
		records.put(TEN_STEP_STARTED, tenStepStarted);
		records.put(TEN_STEP_REWARD_CLAIMED, tenStepRewardClaimed);
		records.put(TEN_STEP_SCORE, tenStepScore);
		records.put(TEN_STEP_COMPLETED, tenStepCompleted);
		records.put(LAST_ENDLESS_SCORE, lastEndlessScore);
		records.put(BEST_ENDLESS_SCORE, bestEndlessScore);
		records.put(ENDLESS_GAMES_PLAYED, endlessGamesPlayed);
		records.put(TOTAL_ENDLESS_SCORE, totalEndlessScore);
		records.put(TOP_SCORES, topScores);
		records.put(EXTRACTION_RAID_SETTLEMENTS, extractionRaidSettlements);
		bundle.put(RECORDS, records);
	}

	public static void restoreFromBundle(Bundle bundle) {
		reset();
		if (!bundle.contains(RECORDS)) {
			return;
		}

		Bundle records = bundle.getBundle(RECORDS);
		if (records.isNull()) {
			return;
		}

		tenStepStarted = records.getBoolean(TEN_STEP_STARTED);
		tenStepRewardClaimed = tenStepStarted && records.getBoolean(TEN_STEP_REWARD_CLAIMED);
		tenStepScore = Math.max(0, records.getInt(TEN_STEP_SCORE));
		tenStepCompleted = tenStepStarted && records.getBoolean(TEN_STEP_COMPLETED);

		lastEndlessScore = Math.max(0, records.getInt(LAST_ENDLESS_SCORE));
		bestEndlessScore = Math.max(lastEndlessScore, Math.max(0, records.getInt(BEST_ENDLESS_SCORE)));
		endlessGamesPlayed = Math.max(0, records.getInt(ENDLESS_GAMES_PLAYED));
		totalEndlessScore = Math.max(0, records.getInt(TOTAL_ENDLESS_SCORE));
		restoreRankedScores(records.getIntArray(TOP_SCORES));
		extractionRaidSettlements =
				Math.max(0, records.getInt(EXTRACTION_RAID_SETTLEMENTS));
	}

	private static void insertRankedScore(int score) {
		int newLength = Math.min(MAX_RANKED_SCORES, topScores.length + 1);
		int[] ranked = new int[newLength];
		int sourceIndex = 0;
		boolean inserted = false;

		for (int index = 0; index < newLength; index++) {
			if (!inserted && (sourceIndex >= topScores.length || score >= topScores[sourceIndex])) {
				ranked[index] = score;
				inserted = true;
			} else {
				ranked[index] = topScores[sourceIndex++];
			}
		}

		topScores = ranked;
	}

	private static void restoreRankedScores(int[] restoredScores) {
		if (restoredScores == null || restoredScores.length == 0) {
			topScores = new int[0];
			return;
		}

		int[] sanitized = new int[Math.min(MAX_RANKED_SCORES, restoredScores.length)];
		for (int index = 0; index < sanitized.length; index++) {
			sanitized[index] = Math.max(0, restoredScores[index]);
		}

		Arrays.sort(sanitized);
		topScores = new int[sanitized.length];
		for (int index = 0; index < sanitized.length; index++) {
			topScores[index] = sanitized[sanitized.length - index - 1];
		}
	}

	private static int saturatedAdd(int left, int right) {
		long sum = (long) left + right;
		return sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
	}
}
