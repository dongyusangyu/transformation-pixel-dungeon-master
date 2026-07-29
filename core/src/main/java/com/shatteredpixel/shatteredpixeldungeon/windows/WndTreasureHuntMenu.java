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
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntGame;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRecords;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRewards;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class WndTreasureHuntMenu extends WndOptions {

	private static final int TEN_STEP = 0;
	private static final int ENDLESS = 1;
	private static final int SCORES = 2;
	private static final int RULES = 3;

	public WndTreasureHuntMenu() {
		super(
				Messages.get(WndTreasureHuntMenu.class, "title"),
				Messages.get(WndTreasureHuntMenu.class, "prompt"),
				tenStepOption(),
				Messages.get(WndTreasureHuntMenu.class, "endless"),
				Messages.get(WndTreasureHuntMenu.class, "scores"),
				Messages.get(WndTreasureHuntMenu.class, "rules"),
				Messages.get(WndTreasureHuntMenu.class, "leave")
		);
	}

	@Override
	protected boolean enabled(int index) {
		return index != TEN_STEP
				|| !TreasureHuntRecords.tenStepStarted()
				|| TreasureHuntRecords.canClaimTenStepReward();
	}

	@Override
	protected void onSelect(int index) {
		switch (index) {
			case TEN_STEP:
				if (TreasureHuntRecords.canClaimTenStepReward()) {
					settlePendingTenStep();
				} else {
					startTenStep();
				}
				break;
			case ENDLESS:
				GameScene.show(new WndTreasureHunt(
						TreasureHuntGame.Mode.ENDLESS, System.nanoTime()));
				break;
			case SCORES:
				showScores();
				break;
			case RULES:
				showRules();
				break;
			default:
				break;
		}
	}

	private static String tenStepOption() {
		if (!TreasureHuntRecords.tenStepStarted()) {
			return Messages.get(WndTreasureHuntMenu.class, "ten_step");
		} else if (TreasureHuntRecords.canClaimTenStepReward()) {
			return Messages.get(WndTreasureHuntMenu.class, "ten_step_pending");
		} else {
			return Messages.get(WndTreasureHuntMenu.class, "ten_step_played");
		}
	}

	private static void startTenStep() {
		if (!TreasureHuntRecords.beginTenStep()) {
			return;
		}

		saveRecords();
		GameScene.show(new WndTreasureHunt(
				TreasureHuntGame.Mode.TEN_STEP, System.nanoTime()));
	}

	private static void settlePendingTenStep() {
		if (!TreasureHuntRecords.claimTenStepReward()) {
			return;
		}

		Item reward = TreasureHuntRewards.grantTenStepReward(
				Dungeon.hero, TreasureHuntRecords.tenStepScore(),
				TreasureHuntRecords.tenStepCompleted());
		saveRecords();
		GLog.p(Messages.get(WndTreasureHuntMenu.class, "reward_received", reward.name()));
		GameScene.show(new WndOptions(
				Messages.get(WndTreasureHuntMenu.class, "pending_title"),
				Messages.get(WndTreasureHuntMenu.class, "pending_result",
						TreasureHuntRecords.tenStepScore(), reward.name()),
				Messages.get(WndTreasureHuntMenu.class, "close")
		));
	}

	private static void showScores() {
		StringBuilder ranking = new StringBuilder();
		int[] topScores = TreasureHuntRecords.topScores();
		if (topScores.length == 0) {
			ranking.append(Messages.get(WndTreasureHuntMenu.class, "no_scores"));
		} else {
			for (int index = 0; index < topScores.length; index++) {
				if (index > 0) {
					ranking.append('\n');
				}
				ranking.append(Messages.get(WndTreasureHuntMenu.class, "rank_line",
						index + 1, topScores[index]));
			}
		}

		GameScene.show(new WndOptions(
				Messages.get(WndTreasureHuntMenu.class, "scores_title"),
				Messages.get(WndTreasureHuntMenu.class, "scores_info",
						TreasureHuntRecords.lastEndlessScore(),
						TreasureHuntRecords.bestEndlessScore(),
						TreasureHuntRecords.endlessGamesPlayed(),
						TreasureHuntRecords.totalEndlessScore(),
						ranking.toString()),
				Messages.get(WndTreasureHuntMenu.class, "close")
		));
	}

	private static void showRules() {
		GameScene.show(new WndOptions(
				Messages.get(WndTreasureHuntMenu.class, "rules_title"),
				Messages.get(WndTreasureHuntMenu.class, "rules_info"),
				Messages.get(WndTreasureHuntMenu.class, "close")
		));
	}

	static void saveRecords() {
		if (GamesInProgress.curSlot >= 0) {
			Dungeon.saveGame(GamesInProgress.curSlot);
		}
	}
}
