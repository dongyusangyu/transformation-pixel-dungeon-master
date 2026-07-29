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
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntGame;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntGame.Choice;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntGame.Encounter;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntGame.Mode;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntGame.TurnResult;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRecords;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRewards;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.List;

public class WndTreasureHunt extends WndOptions {

	private final TreasureHuntGame game;
	private boolean settled;

	public WndTreasureHunt(Mode mode, long seed) {
		this(new TreasureHuntGame(mode, seed), null);
	}

	private WndTreasureHunt(TreasureHuntGame game, String lastResult) {
		super(title(game), status(game, lastResult), options(game));
		this.game = game;
	}

	@Override
	protected void onSelect(int index) {
		if (settled) {
			return;
		}

		if (index == game.choices().size()) {
			game.cashOut();
			settle(null);
			return;
		}

		TurnResult result = game.choose(index);
		if (game.mode() == Mode.TEN_STEP) {
			TreasureHuntRecords.updateTenStepProgress(
					game.score(), game.completedTenSteps());
			WndTreasureHuntMenu.saveRecords();
		}

		if (game.finished()) {
			settle(turnResult(result));
		} else {
			GameScene.show(new WndTreasureHunt(game, turnResult(result)));
		}
	}

	@Override
	public void onBackPressed() {
		if (!settled) {
			game.cashOut();
			settle(null);
		}
		super.onBackPressed();
	}

	private void settle(String lastResult) {
		if (settled) {
			return;
		}
		settled = true;

		if (game.mode() == Mode.TEN_STEP) {
			settleTenStep(lastResult);
		} else {
			settleEndless(lastResult);
		}
	}

	private void settleTenStep(String lastResult) {
		TreasureHuntRecords.updateTenStepProgress(
				game.score(), game.completedTenSteps());
		if (!TreasureHuntRecords.claimTenStepReward()) {
			return;
		}

		Item reward = TreasureHuntRewards.grantTenStepReward(
				Dungeon.hero, game.score(), game.completedTenSteps());
		WndTreasureHuntMenu.saveRecords();
		GLog.p(Messages.get(WndTreasureHunt.class, "reward_received", reward.name()));

		String resultKey = game.completedTenSteps() ? "ten_complete" : "ten_ended";
		GameScene.show(new WndOptions(
				Messages.get(WndTreasureHunt.class, "result_title"),
				settlementMessage(lastResult, Messages.get(
						WndTreasureHunt.class, resultKey, game.score(), reward.name())),
				Messages.get(WndTreasureHunt.class, "close")
		));
	}

	private void settleEndless(String lastResult) {
		TreasureHuntRecords.recordEndlessScore(game.score());
		WndTreasureHuntMenu.saveRecords();
		GameScene.show(new WndOptions(
				Messages.get(WndTreasureHunt.class, "result_title"),
				settlementMessage(lastResult, Messages.get(
						WndTreasureHunt.class, "endless_ended",
						game.score(), TreasureHuntRecords.bestEndlessScore())),
				Messages.get(WndTreasureHunt.class, "close")
		));
	}

	private static String settlementMessage(String lastResult, String settlement) {
		return lastResult == null ? settlement : lastResult + "\n\n" + settlement;
	}

	private static String title(TreasureHuntGame game) {
		return Messages.get(WndTreasureHunt.class,
				game.mode() == Mode.TEN_STEP ? "title_ten" : "title_endless");
	}

	private static String status(TreasureHuntGame game, String lastResult) {
		String status;
		if (game.mode() == Mode.TEN_STEP) {
			status = Messages.get(WndTreasureHunt.class, "status_ten",
					game.step(), game.courage(), game.afterglow(),
					game.riskStreak(), game.score());
		} else {
			status = Messages.get(WndTreasureHunt.class, "status_endless",
					game.step() + 1, game.courage(), game.afterglow(),
					game.riskStreak(), game.score());
		}

		if (lastResult != null) {
			status += "\n\n" + lastResult;
		}
		return status;
	}

	private static String[] options(TreasureHuntGame game) {
		List<Choice> choices = game.choices();
		String[] options = new String[choices.size() + 1];
		for (int index = 0; index < choices.size(); index++) {
			Choice choice = choices.get(index);
			options[index] = Messages.get(WndTreasureHunt.class,
					choiceKey(choice.encounter()));
		}
		options[choices.size()] = Messages.get(WndTreasureHunt.class, "cash_out");
		return options;
	}

	private static String turnResult(TurnResult result) {
		String fatigue = result.fatigue()
				? Messages.get(WndTreasureHunt.class, "fatigue")
				: "";
		return Messages.get(WndTreasureHunt.class, "turn_result",
				encounterNarrative(result.encounter()), result.scoreGained(),
				result.courageChange(), result.afterglowChange(), fatigue);
	}

	private static String choiceKey(Encounter encounter) {
		switch (encounter) {
			case MOSSY_PATH:
				return "choice_mossy_path";
			case ANCIENT_RUNES:
				return "choice_ancient_runes";
			case BROKEN_BRIDGE:
				return "choice_broken_bridge";
			case LOST_GHOST:
				return "choice_lost_ghost";
			case RAT_AMBUSH:
				return "choice_rat_ambush";
			case TOXIC_THICKET:
				return "choice_toxic_thicket";
			case FALLING_ROCKS:
				return "choice_falling_rocks";
			case STONE_GUARDIAN:
				return "choice_stone_guardian";
			case DARK_RIDDLE:
				return "choice_dark_riddle";
			case CURSED_ALTAR:
				return "choice_cursed_altar";
			case MIMIC_CHEST:
				return "choice_mimic_chest";
			case ABYSS_SHORTCUT:
				return "choice_abyss_shortcut";
			case EMBER_CAMP:
				return "choice_ember_camp";
			case MOON_WELL:
				return "choice_moon_well";
			case EMBER_PACT:
				return "choice_ember_pact";
			default:
				throw new IllegalArgumentException("unknown encounter");
		}
	}

	private static String encounterNarrative(Encounter encounter) {
		switch (encounter) {
			case MOSSY_PATH:
				return Messages.get(WndTreasureHunt.class, "narrative_mossy_path");
			case ANCIENT_RUNES:
				return Messages.get(WndTreasureHunt.class, "narrative_ancient_runes");
			case BROKEN_BRIDGE:
				return Messages.get(WndTreasureHunt.class, "narrative_broken_bridge");
			case LOST_GHOST:
				return Messages.get(WndTreasureHunt.class, "narrative_lost_ghost");
			case RAT_AMBUSH:
				return Messages.get(WndTreasureHunt.class, "narrative_rat_ambush");
			case TOXIC_THICKET:
				return Messages.get(WndTreasureHunt.class, "narrative_toxic_thicket");
			case FALLING_ROCKS:
				return Messages.get(WndTreasureHunt.class, "narrative_falling_rocks");
			case STONE_GUARDIAN:
				return Messages.get(WndTreasureHunt.class, "narrative_stone_guardian");
			case DARK_RIDDLE:
				return Messages.get(WndTreasureHunt.class, "narrative_dark_riddle");
			case CURSED_ALTAR:
				return Messages.get(WndTreasureHunt.class, "narrative_cursed_altar");
			case MIMIC_CHEST:
				return Messages.get(WndTreasureHunt.class, "narrative_mimic_chest");
			case ABYSS_SHORTCUT:
				return Messages.get(WndTreasureHunt.class, "narrative_abyss_shortcut");
			case EMBER_CAMP:
				return Messages.get(WndTreasureHunt.class, "narrative_ember_camp");
			case MOON_WELL:
				return Messages.get(WndTreasureHunt.class, "narrative_moon_well");
			case EMBER_PACT:
				return Messages.get(WndTreasureHunt.class, "narrative_ember_pact");
			default:
				throw new IllegalArgumentException("unknown encounter");
		}
	}
}
