package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Preferences;
import com.watabou.utils.GameSettings;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SPDSettingsQuizTest {

	private Map<String, Object> values;

	@Before
	public void setUpPreferences() {
		values = new HashMap<>();
		Preferences preferences = (Preferences) Proxy.newProxyInstance(
				Preferences.class.getClassLoader(),
				new Class<?>[]{Preferences.class},
				(proxy, method, args) -> {
					String name = method.getName();
					if (name.startsWith("put")) {
						values.put((String) args[0], args[1]);
						return proxy;
					}
					if (name.equals("getInteger")) {
						return values.getOrDefault(args[0], args[1]);
					}
					if (name.equals("getString")) {
						return values.getOrDefault(args[0], args[1]);
					}
					if (name.equals("contains")) {
						return values.containsKey(args[0]);
					}
					if (name.equals("flush")) {
						return null;
					}
					throw new UnsupportedOperationException(name);
				});
		GameSettings.set(preferences);
	}

	@Test
	public void recordsGlobalQuizTotalsAndAccuracy() {
		SPDSettings.resetQuizStatistics();

		SPDSettings.recordQuizAnswer(true);
		SPDSettings.recordQuizAnswer(false);
		SPDSettings.recordQuizAnswer(true);

		assertEquals(3, SPDSettings.quizAnswersTotal());
		assertEquals(2, SPDSettings.quizAnswersCorrect());
		assertEquals(66.666, SPDSettings.quizAccuracyPercent(), 0.01);
		assertEquals(3, values.get(SPDSettings.KEY_QUIZ_ANSWERS_TOTAL));
		assertEquals(2, values.get(SPDSettings.KEY_QUIZ_ANSWERS_CORRECT));
	}

	@Test
	public void resetClearsQuizStatistics() {
		SPDSettings.recordQuizAnswer(true);

		SPDSettings.resetQuizStatistics();

		assertEquals(0, SPDSettings.quizAnswersTotal());
		assertEquals(0, SPDSettings.quizAnswersCorrect());
		assertEquals(0.0, SPDSettings.quizAccuracyPercent(), 0.0);
	}

	@Test
	public void invalidStoredCorrectCountIsClampedToTotal() {
		values.put(SPDSettings.KEY_QUIZ_ANSWERS_TOTAL, 2);
		values.put(SPDSettings.KEY_QUIZ_ANSWERS_CORRECT, 9);

		assertEquals(2, SPDSettings.quizAnswersTotal());
		assertEquals(2, SPDSettings.quizAnswersCorrect());
		assertEquals(100.0, SPDSettings.quizAccuracyPercent(), 0.0);
	}

	@Test
	public void negativeStoredCountsAreClampedToZero() {
		values.put(SPDSettings.KEY_QUIZ_ANSWERS_TOTAL, -3);
		values.put(SPDSettings.KEY_QUIZ_ANSWERS_CORRECT, -2);

		assertEquals(0, SPDSettings.quizAnswersTotal());
		assertEquals(0, SPDSettings.quizAnswersCorrect());
		assertEquals(0.0, SPDSettings.quizAccuracyPercent(), 0.0);
	}

	@Test
	public void answerCountSaturatesAtIntegerMaximum() {
		values.put(SPDSettings.KEY_QUIZ_ANSWERS_TOTAL, Integer.MAX_VALUE);
		values.put(SPDSettings.KEY_QUIZ_ANSWERS_CORRECT, Integer.MAX_VALUE - 1);

		SPDSettings.recordQuizAnswer(true);
		SPDSettings.recordQuizAnswer(false);

		assertEquals(Integer.MAX_VALUE, SPDSettings.quizAnswersTotal());
		assertEquals(Integer.MAX_VALUE - 1, SPDSettings.quizAnswersCorrect());
	}

	@Test
	public void correctQuestionIdsAreNormalizedAndDeduplicated() {
		SPDSettings.recordCorrectQuizQuestion("Q200");
		SPDSettings.recordCorrectQuizQuestion("Q001");
		SPDSettings.recordCorrectQuizQuestion("Q001");

		Set<String> expected = new LinkedHashSet<>(Arrays.asList("Q001", "Q200"));
		assertEquals(expected, SPDSettings.quizCorrectQuestionIds());
		assertEquals("Q001,Q200",
				values.get(SPDSettings.KEY_QUIZ_CORRECT_QUESTION_IDS));
	}

	@Test
	public void invalidStoredQuestionIdsAreIgnoredAndNormalized() {
		values.put(SPDSettings.KEY_QUIZ_CORRECT_QUESTION_IDS,
				"Q000,Q001,Q001,Q201,broken");

		assertEquals(new LinkedHashSet<>(Arrays.asList("Q001")),
				SPDSettings.quizCorrectQuestionIds());
		assertEquals("Q001", values.get(SPDSettings.KEY_QUIZ_CORRECT_QUESTION_IDS));
	}

	@Test
	public void correctQuestionIdsCannotBeModifiedByCallers() {
		SPDSettings.recordCorrectQuizQuestion("Q001");
		Set<String> ids = SPDSettings.quizCorrectQuestionIds();

		try {
			ids.add("Q002");
			fail("expected an immutable question ID set");
		} catch (UnsupportedOperationException expected) {
			// Expected.
		}

		assertEquals(new LinkedHashSet<>(Arrays.asList("Q001")),
				SPDSettings.quizCorrectQuestionIds());
	}

	@Test
	public void resetCorrectQuestionsPreservesLifetimeStatistics() {
		SPDSettings.recordQuizAnswer(true);
		SPDSettings.recordCorrectQuizQuestion("Q001");

		SPDSettings.resetCorrectQuizQuestions();

		assertTrue(SPDSettings.quizCorrectQuestionIds().isEmpty());
		assertEquals(1, SPDSettings.quizAnswersTotal());
		assertEquals(1, SPDSettings.quizAnswersCorrect());
	}
}
