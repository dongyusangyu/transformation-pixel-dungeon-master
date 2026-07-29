/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class TreasureHuntGame {

	public static final int MAX_COURAGE = 6;
	public static final int TEN_STEP_LIMIT = 10;

	public enum Mode {
		TEN_STEP,
		ENDLESS
	}

	public enum Encounter {
		MOSSY_PATH,
		ANCIENT_RUNES,
		BROKEN_BRIDGE,
		LOST_GHOST,
		RAT_AMBUSH,
		TOXIC_THICKET,
		FALLING_ROCKS,
		STONE_GUARDIAN,
		DARK_RIDDLE,
		CURSED_ALTAR,
		MIMIC_CHEST,
		ABYSS_SHORTCUT,
		EMBER_CAMP,
		MOON_WELL,
		EMBER_PACT
	}

	public static final class Choice {

		private final Encounter encounter;
		private final int scoreGain;
		private final int courageDelta;
		private final int afterglowDelta;

		private Choice(Encounter encounter, int scoreGain, int courageDelta, int afterglowDelta) {
			this.encounter = encounter;
			this.scoreGain = scoreGain;
			this.courageDelta = courageDelta;
			this.afterglowDelta = afterglowDelta;
		}

		public Encounter encounter() {
			return encounter;
		}

		public int scoreGain() {
			return scoreGain;
		}

		public int courageDelta() {
			return courageDelta;
		}

		public int afterglowDelta() {
			return afterglowDelta;
		}
	}

	public static final class TurnResult {

		private final Encounter encounter;
		private final int scoreGained;
		private final int courageChange;
		private final int afterglowChange;
		private final boolean fatigue;

		private TurnResult(Encounter encounter, int scoreGained, int courageChange,
				int afterglowChange, boolean fatigue) {
			this.encounter = encounter;
			this.scoreGained = scoreGained;
			this.courageChange = courageChange;
			this.afterglowChange = afterglowChange;
			this.fatigue = fatigue;
		}

		public Encounter encounter() {
			return encounter;
		}

		public int scoreGained() {
			return scoreGained;
		}

		public int courageChange() {
			return courageChange;
		}

		public int afterglowChange() {
			return afterglowChange;
		}

		public boolean fatigue() {
			return fatigue;
		}
	}

	private final Mode mode;
	private final Random random;

	private int step;
	private int score;
	private int courage = MAX_COURAGE;
	private int afterglow;
	private int riskStreak;
	private boolean finished;
	private List<Choice> choices;

	public TreasureHuntGame(Mode mode, long seed) {
		if (mode == null) {
			throw new IllegalArgumentException("mode must not be null");
		}
		this.mode = mode;
		this.random = new Random(seed);
		choices = buildChoices();
	}

	public Mode mode() {
		return mode;
	}

	public int step() {
		return step;
	}

	public int score() {
		return score;
	}

	public int courage() {
		return courage;
	}

	public int afterglow() {
		return afterglow;
	}

	public int riskStreak() {
		return riskStreak;
	}

	public boolean finished() {
		return finished;
	}

	public boolean completedTenSteps() {
		return mode == Mode.TEN_STEP && step >= TEN_STEP_LIMIT;
	}

	public List<Choice> choices() {
		return choices;
	}

	public TurnResult choose(int index) {
		if (finished) {
			throw new IllegalStateException("the treasure hunt has already finished");
		}
		if (index < 0 || index >= choices.size()) {
			throw new IllegalArgumentException("choice index is out of range");
		}

		Choice choice = choices.get(index);
		int courageBefore = courage;
		int afterglowBefore = afterglow;

		score = saturatedAdd(score, choice.scoreGain);
		courage = clamp(courage + choice.courageDelta, 0, MAX_COURAGE);
		afterglow = Math.max(0, afterglow + choice.afterglowDelta);

		if (isRisky(choice.encounter)) {
			riskStreak = saturatedAdd(riskStreak, 1);
		} else {
			riskStreak = 0;
		}

		step++;
		boolean fatigue = step % 5 == 0;
		if (fatigue) {
			courage = Math.max(0, courage - 1);
			afterglow++;
		}

		if (courage == 0 || mode == Mode.TEN_STEP && step >= TEN_STEP_LIMIT) {
			finished = true;
			choices = Collections.emptyList();
		} else {
			choices = buildChoices();
		}

		return new TurnResult(choice.encounter, choice.scoreGain,
				courage - courageBefore, afterglow - afterglowBefore, fatigue);
	}

	public void cashOut() {
		finished = true;
		choices = Collections.emptyList();
	}

	private List<Choice> buildChoices() {
		ArrayList<Encounter> candidates = new ArrayList<>();
		Collections.addAll(candidates, Encounter.values());

		if (courage >= MAX_COURAGE) {
			candidates.remove(Encounter.EMBER_CAMP);
			candidates.remove(Encounter.MOON_WELL);
		}
		if (afterglow < 2) {
			candidates.remove(Encounter.EMBER_PACT);
		}

		Collections.shuffle(candidates, random);
		ArrayList<Choice> result = new ArrayList<>(3);
		for (int index = 0; index < 3; index++) {
			result.add(buildChoice(candidates.get(index)));
		}
		return Collections.unmodifiableList(result);
	}

	private Choice buildChoice(Encounter encounter) {
		switch (encounter) {
			case MOSSY_PATH:
				return choice(encounter, 18, 38, 0, 0, 0, 0, 0);
			case ANCIENT_RUNES:
				return choice(encounter, 23, 43, 0, 0, 1, 1, 0);
			case BROKEN_BRIDGE:
				return choice(encounter, 36, 61, -1, 0, 0, 0, 0);
			case LOST_GHOST:
				return choice(encounter, 36, 66, -1, 0, 1, 1, 0);
			case RAT_AMBUSH:
				return choice(encounter, 46, 71, -1, -1, 0, 1, 3);
			case TOXIC_THICKET:
				return choice(encounter, 51, 81, -1, -1, 1, 2, 3);
			case FALLING_ROCKS:
				return choice(encounter, 54, 86, -2, -1, 1, 1, 5);
			case STONE_GUARDIAN:
				return choice(encounter, 58, 94, -2, -1, 1, 2, 5);
			case DARK_RIDDLE:
				return choice(encounter, 54, 98, -2, -1, 0, 1, 5);
			case CURSED_ALTAR:
				return choice(encounter, 74, 111, -2, -2, 2, 2, 8);
			case MIMIC_CHEST:
				return choice(encounter, 84, 131, -3, -2, 2, 3, 8);
			case ABYSS_SHORTCUT:
				return choice(encounter, 94, 151, -3, -2, 1, 2, 8);
			case EMBER_CAMP:
				return choice(encounter, 6, 18, 1, 2, 0, 0, 0);
			case MOON_WELL:
				return choice(encounter, 14, 28, 1, 1, 0, 0, 0);
			case EMBER_PACT:
				return choice(encounter, 28, 48, 1, 2, -2, -1, 0);
			default:
				throw new IllegalStateException("unknown encounter");
		}
	}

	private Choice choice(Encounter encounter, int scoreMin, int scoreMax,
			int courageMin, int courageMax, int afterglowMin, int afterglowMax,
			int riskMultiplier) {
		int riskBonus = saturatedMultiply(riskStreak, riskMultiplier);
		return new Choice(encounter,
				randomBetween(saturatedAdd(scoreMin, riskBonus),
						saturatedAdd(scoreMax, riskBonus)),
				randomBetween(courageMin, courageMax),
				randomBetween(afterglowMin, afterglowMax));
	}

	private int randomBetween(int min, int max) {
		return min + random.nextInt(max - min + 1);
	}

	private static boolean isRisky(Encounter encounter) {
		switch (encounter) {
			case RAT_AMBUSH:
			case TOXIC_THICKET:
			case FALLING_ROCKS:
			case STONE_GUARDIAN:
			case DARK_RIDDLE:
			case CURSED_ALTAR:
			case MIMIC_CHEST:
			case ABYSS_SHORTCUT:
				return true;
			default:
				return false;
		}
	}

	private static int saturatedMultiply(int value, int multiplier) {
		long product = (long) value * multiplier;
		return product >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
	}

	private static int saturatedAdd(int left, int right) {
		if (right > 0 && left > Integer.MAX_VALUE - right) {
			return Integer.MAX_VALUE;
		}
		return left + right;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
