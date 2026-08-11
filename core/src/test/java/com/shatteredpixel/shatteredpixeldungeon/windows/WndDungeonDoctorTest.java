package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.Preferences;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.custom.quiz.QuizQuestion;
import com.watabou.utils.GameSettings;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WndDungeonDoctorTest {

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
					if (name.equals("getInteger") || name.equals("getString")) {
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
	public void mapsEveryMainMenuIndex() {
		assertEquals(WndDungeonDoctor.MainAction.START,
				WndDungeonDoctor.mainAction(0));
		assertEquals(WndDungeonDoctor.MainAction.STATISTICS,
				WndDungeonDoctor.mainAction(1));
		assertEquals(WndDungeonDoctor.MainAction.RESET,
				WndDungeonDoctor.mainAction(2));
		assertEquals(WndDungeonDoctor.MainAction.LEAVE,
				WndDungeonDoctor.mainAction(3));
	}

	@Test
	public void distinguishesUnavailableMasteredAndReadyQuizStates() {
		assertEquals(WndDungeonDoctor.StartState.UNAVAILABLE,
				WndDungeonDoctor.startState(true, null));
		assertEquals(WndDungeonDoctor.StartState.MASTERED,
				WndDungeonDoctor.startState(false, null));
		assertEquals(WndDungeonDoctor.StartState.READY,
				WndDungeonDoctor.startState(false, question("Q001")));
	}

	@Test
	public void recordsEveryAnswerButExcludesOnlyCorrectQuestions() {
		WndDungeonDoctor.recordAnswer(question("Q001"), true);
		WndDungeonDoctor.recordAnswer(question("Q002"), false);

		assertEquals(2, SPDSettings.quizAnswersTotal());
		assertEquals(1, SPDSettings.quizAnswersCorrect());
		assertEquals(1, SPDSettings.quizCorrectQuestionIds().size());
		assertTrue(SPDSettings.quizCorrectQuestionIds().contains("Q001"));
		assertFalse(SPDSettings.quizCorrectQuestionIds().contains("Q002"));
	}

	@Test
	public void resetRequiresConfirmationAndPreservesStatistics() {
		WndDungeonDoctor.recordAnswer(question("Q001"), true);

		assertFalse(WndDungeonDoctor.resetIfConfirmed(1));
		assertTrue(SPDSettings.quizCorrectQuestionIds().contains("Q001"));

		assertTrue(WndDungeonDoctor.resetIfConfirmed(0));
		assertTrue(SPDSettings.quizCorrectQuestionIds().isEmpty());
		assertEquals(1, SPDSettings.quizAnswersTotal());
		assertEquals(1, SPDSettings.quizAnswersCorrect());
	}

	private static QuizQuestion question(String id) {
		return new QuizQuestion(id, id,
				Arrays.asList("甲", "乙", "丙", "丁"), 0, "");
	}
}
