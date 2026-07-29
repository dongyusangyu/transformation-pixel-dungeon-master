package com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreasureHuntGameTest {

	private static final Set<TreasureHuntGame.Encounter> RISKY_EVENTS =
			EnumSet.of(
					TreasureHuntGame.Encounter.RAT_AMBUSH,
					TreasureHuntGame.Encounter.TOXIC_THICKET,
					TreasureHuntGame.Encounter.FALLING_ROCKS,
					TreasureHuntGame.Encounter.STONE_GUARDIAN,
					TreasureHuntGame.Encounter.DARK_RIDDLE,
					TreasureHuntGame.Encounter.CURSED_ALTAR,
					TreasureHuntGame.Encounter.MIMIC_CHEST,
					TreasureHuntGame.Encounter.ABYSS_SHORTCUT);

	@Test
	public void startsWithSixCourageAndThreeRandomChoices() {
		TreasureHuntGame game = new TreasureHuntGame(TreasureHuntGame.Mode.TEN_STEP, 1L);

		assertEquals(0, game.step());
		assertEquals(0, game.score());
		assertEquals(TreasureHuntGame.MAX_COURAGE, game.courage());
		assertEquals(0, game.afterglow());
		assertEquals(0, game.riskStreak());
		assertEquals(3, game.choices().size());
		assertEquals(3, encountersOf(game).size());
	}

	@Test
	public void fifteenEventsAreRandomlyOfferedWithoutAStableOpeningEvent() {
		assertEquals(15, TreasureHuntGame.Encounter.values().length);
		Set<TreasureHuntGame.Encounter> openingIntersection =
				new HashSet<>(Arrays.asList(TreasureHuntGame.Encounter.values()));
		Set<TreasureHuntGame.Encounter> seen = new HashSet<>();
		Random chooser = new Random(20260726L);

		for (long seed = 0; seed < 4096; seed++) {
			TreasureHuntGame game =
					new TreasureHuntGame(TreasureHuntGame.Mode.TEN_STEP, seed);
			Set<TreasureHuntGame.Encounter> opening = encountersOf(game);
			assertEquals(3, opening.size());
			openingIntersection.retainAll(opening);

			while (!game.finished()) {
				seen.addAll(encountersOf(game));
				game.choose(chooser.nextInt(game.choices().size()));
			}
		}

		assertTrue(openingIntersection.isEmpty());
		assertEquals(
				new HashSet<>(Arrays.asList(TreasureHuntGame.Encounter.values())),
				seen);
	}

	@Test
	public void generatedOutcomesStayWithinEveryEncounterRange() {
		Set<TreasureHuntGame.Encounter> seen = new HashSet<>();
		Random chooser = new Random(26L);

		for (long seed = 0; seed < 4096; seed++) {
			TreasureHuntGame game =
					new TreasureHuntGame(TreasureHuntGame.Mode.TEN_STEP, seed);
			while (!game.finished()) {
				for (TreasureHuntGame.Choice choice : game.choices()) {
					assertChoiceWithinRange(choice, game.riskStreak());
					seen.add(choice.encounter());
				}
				game.choose(chooser.nextInt(game.choices().size()));
			}
		}

		assertEquals(TreasureHuntGame.Encounter.values().length, seen.size());
	}

	@Test
	public void sameSeedProducesIdenticalInitialChoiceValues() {
		TreasureHuntGame first =
				new TreasureHuntGame(TreasureHuntGame.Mode.ENDLESS, 20260726L);
		TreasureHuntGame second =
				new TreasureHuntGame(TreasureHuntGame.Mode.ENDLESS, 20260726L);

		for (int index = 0; index < first.choices().size(); index++) {
			TreasureHuntGame.Choice expected = first.choices().get(index);
			TreasureHuntGame.Choice actual = second.choices().get(index);
			assertEquals(expected.encounter(), actual.encounter());
			assertEquals(expected.scoreGain(), actual.scoreGain());
			assertEquals(expected.courageDelta(), actual.courageDelta());
			assertEquals(expected.afterglowDelta(), actual.afterglowDelta());
		}
	}

	@Test
	public void tenStepModeCanReachAndEndsAtTheTenthChoice() {
		TreasureHuntGame completed = findGameThatSurvivesTenSteps(
				TreasureHuntGame.Mode.TEN_STEP);

		assertEquals(TreasureHuntGame.TEN_STEP_LIMIT, completed.step());
		assertTrue(completed.finished());
		assertTrue(completed.completedTenSteps());
	}

	@Test
	public void endlessModeHasNoFixedTenStepLimit() {
		TreasureHuntGame game = findGameThatSurvivesTenSteps(
				TreasureHuntGame.Mode.ENDLESS);

		assertEquals(TreasureHuntGame.TEN_STEP_LIMIT, game.step());
		assertFalse(game.finished());
		assertFalse(game.completedTenSteps());
	}

	@Test
	public void everyFifthStepAppliesFatigueAndCreatesAfterglow() {
		TreasureHuntGame game = new TreasureHuntGame(TreasureHuntGame.Mode.ENDLESS, 4L);
		TreasureHuntGame.TurnResult result = null;

		for (int index = 0; index < 5; index++) {
			result = game.choose(mostCourageousChoice(game));
		}

		assertEquals(5, game.step());
		assertTrue(result.fatigue());
	}

	@Test
	public void dangerousChoicesBuildRiskAndOtherChoicesResetIt() {
		TreasureHuntGame game = null;
		for (long seed = 0; seed < 128 && game == null; seed++) {
			TreasureHuntGame candidate =
					new TreasureHuntGame(TreasureHuntGame.Mode.ENDLESS, seed);
			int danger = indexOfRiskyChoice(candidate);
			if (danger >= 0) {
				candidate.choose(danger);
				if (!candidate.finished() && indexOfNonRiskyChoice(candidate) >= 0) {
					game = candidate;
				}
			}
		}

		assertTrue(game != null);
		assertEquals(1, game.riskStreak());

		game.choose(indexOfNonRiskyChoice(game));

		assertEquals(0, game.riskStreak());
	}

	@Test
	public void uniformRandomPlayStaysInsideBalanceTargets() {
		final int games = 100_000;
		long totalSteps = 0;
		long totalScore = 0;
		Random chooser = new Random(20260726L);

		for (int seed = 1; seed <= games; seed++) {
			TreasureHuntGame game =
					new TreasureHuntGame(TreasureHuntGame.Mode.TEN_STEP, seed);
			while (!game.finished()) {
				game.choose(chooser.nextInt(game.choices().size()));
			}
			totalSteps += game.step();
			totalScore += game.score();
		}

		double meanSteps = totalSteps / (double) games;
		double meanScore = totalScore / (double) games;
		assertTrue("mean steps was " + meanSteps,
				meanSteps >= 5.9 && meanSteps <= 6.5);
		assertTrue("mean score was " + meanScore,
				meanScore >= 380 && meanScore <= 420);
	}

	@Test
	public void cashingOutFinishesEitherModeWithoutChangingScore() {
		TreasureHuntGame game = new TreasureHuntGame(TreasureHuntGame.Mode.ENDLESS, 7L);
		game.choose(0);
		int score = game.score();

		game.cashOut();

		assertTrue(game.finished());
		assertEquals(score, game.score());
	}

	private static TreasureHuntGame findGameThatSurvivesTenSteps(
			TreasureHuntGame.Mode mode) {
		for (long seed = 0; seed < 4096; seed++) {
			TreasureHuntGame game = new TreasureHuntGame(mode, seed);
			while (!game.finished() && game.step() < TreasureHuntGame.TEN_STEP_LIMIT) {
				game.choose(mostCourageousChoice(game));
			}
			if (game.step() == TreasureHuntGame.TEN_STEP_LIMIT) {
				return game;
			}
		}
		throw new AssertionError("No deterministic seed survived ten steps");
	}

	private static int mostCourageousChoice(TreasureHuntGame game) {
		int best = 0;
		for (int index = 1; index < game.choices().size(); index++) {
			if (game.choices().get(index).courageDelta()
					> game.choices().get(best).courageDelta()) {
				best = index;
			}
		}
		return best;
	}

	private static Set<TreasureHuntGame.Encounter> encountersOf(TreasureHuntGame game) {
		Set<TreasureHuntGame.Encounter> encounters =
				EnumSet.noneOf(TreasureHuntGame.Encounter.class);
		for (TreasureHuntGame.Choice choice : game.choices()) {
			encounters.add(choice.encounter());
		}
		return encounters;
	}

	private static int indexOfRiskyChoice(TreasureHuntGame game) {
		for (int index = 0; index < game.choices().size(); index++) {
			if (RISKY_EVENTS.contains(game.choices().get(index).encounter())) {
				return index;
			}
		}
		return -1;
	}

	private static int indexOfNonRiskyChoice(TreasureHuntGame game) {
		for (int index = 0; index < game.choices().size(); index++) {
			if (!RISKY_EVENTS.contains(game.choices().get(index).encounter())) {
				return index;
			}
		}
		return -1;
	}

	private static void assertChoiceWithinRange(
			TreasureHuntGame.Choice choice, int riskStreak) {
		switch (choice.encounter()) {
			case MOSSY_PATH:
				assertChoice(choice, 18, 38, 0, 0, 0, 0);
				break;
			case ANCIENT_RUNES:
				assertChoice(choice, 23, 43, 0, 0, 1, 1);
				break;
			case BROKEN_BRIDGE:
				assertChoice(choice, 36, 61, -1, 0, 0, 0);
				break;
			case LOST_GHOST:
				assertChoice(choice, 36, 66, -1, 0, 1, 1);
				break;
			case RAT_AMBUSH:
				assertChoice(choice, 46 + 3 * riskStreak, 71 + 3 * riskStreak,
						-1, -1, 0, 1);
				break;
			case TOXIC_THICKET:
				assertChoice(choice, 51 + 3 * riskStreak, 81 + 3 * riskStreak,
						-1, -1, 1, 2);
				break;
			case FALLING_ROCKS:
				assertChoice(choice, 54 + 5 * riskStreak, 86 + 5 * riskStreak,
						-2, -1, 1, 1);
				break;
			case STONE_GUARDIAN:
				assertChoice(choice, 58 + 5 * riskStreak, 94 + 5 * riskStreak,
						-2, -1, 1, 2);
				break;
			case DARK_RIDDLE:
				assertChoice(choice, 54 + 5 * riskStreak, 98 + 5 * riskStreak,
						-2, -1, 0, 1);
				break;
			case CURSED_ALTAR:
				assertChoice(choice, 74 + 8 * riskStreak, 111 + 8 * riskStreak,
						-2, -2, 2, 2);
				break;
			case MIMIC_CHEST:
				assertChoice(choice, 84 + 8 * riskStreak, 131 + 8 * riskStreak,
						-3, -2, 2, 3);
				break;
			case ABYSS_SHORTCUT:
				assertChoice(choice, 94 + 8 * riskStreak, 151 + 8 * riskStreak,
						-3, -2, 1, 2);
				break;
			case EMBER_CAMP:
				assertChoice(choice, 6, 18, 1, 2, 0, 0);
				break;
			case MOON_WELL:
				assertChoice(choice, 14, 28, 1, 1, 0, 0);
				break;
			case EMBER_PACT:
				assertChoice(choice, 28, 48, 1, 2, -2, -1);
				break;
			default:
				throw new AssertionError("Unhandled encounter " + choice.encounter());
		}
	}

	private static void assertChoice(TreasureHuntGame.Choice choice,
			int scoreMin, int scoreMax, int courageMin, int courageMax,
			int afterglowMin, int afterglowMax) {
		assertBetween(choice.scoreGain(), scoreMin, scoreMax);
		assertBetween(choice.courageDelta(), courageMin, courageMax);
		assertBetween(choice.afterglowDelta(), afterglowMin, afterglowMax);
	}

	private static void assertBetween(int value, int min, int max) {
		assertTrue(value + " must be between " + min + " and " + max,
				value >= min && value <= max);
	}
}
