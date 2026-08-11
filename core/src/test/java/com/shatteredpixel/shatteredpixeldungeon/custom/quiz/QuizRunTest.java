package com.shatteredpixel.shatteredpixeldungeon.custom.quiz;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class QuizRunTest {

	@Test
	public void recordsEachPresentedQuestionOnlyOnceAndContinues() {
		List<String> recorded = new ArrayList<>();
		QuizRun run = new QuizRun(new QuizSession(Arrays.asList(
				question("q1", 0),
				question("q2", 1)), new java.util.Random(7L)),
				(question, correct) -> recorded.add(question.id() + ":" + correct));

		QuizQuestion first = run.nextQuestion();
		QuizRun.Answer firstAnswer = run.answer(first.correctIndex());

		assertTrue(firstAnswer.correct());
		assertEquals(first, firstAnswer.question());
		assertEquals(Arrays.asList(first.id() + ":true"), recorded);
		assertTrue(run.hasNextQuestion());
		assertEquals(QuizRun.State.RESULT, run.state());
		assertIllegalState(() -> run.answer(first.correctIndex()));

		QuizQuestion second = run.nextQuestion();
		int wrongIndex = (second.correctIndex() + 1) % QuizQuestion.OPTION_COUNT;
		QuizRun.Answer secondAnswer = run.answer(wrongIndex);

		assertFalse(secondAnswer.correct());
		assertEquals(Arrays.asList(
				first.id() + ":true", second.id() + ":false"), recorded);
		assertFalse(run.hasNextQuestion());
		assertEquals(QuizRun.State.RESULT, run.state());
	}

	@Test
	public void finishingStopsTheRunWithoutRecordingAnotherAnswer() {
		List<String> recorded = new ArrayList<>();
		QuizRun run = new QuizRun(new QuizSession(Arrays.asList(
				question("q1", 0),
				question("q2", 1)), new java.util.Random(7L)),
				(question, correct) -> recorded.add(question.id() + ":" + correct));

		QuizQuestion first = run.nextQuestion();
		run.answer(first.correctIndex());
		run.finish();

		assertEquals(QuizRun.State.FINISHED, run.state());
		assertEquals(Arrays.asList(first.id() + ":true"), recorded);
		assertIllegalState(run::nextQuestion);
	}

	@Test
	public void emptyRunCompletesWithoutRecording() {
		List<String> recorded = new ArrayList<>();
		QuizRun run = new QuizRun(new QuizSession(new ArrayList<>()),
				(question, correct) -> recorded.add(question.id() + ":" + correct));

		assertNull(run.nextQuestion());
		assertEquals(QuizRun.State.COMPLETE, run.state());
		assertTrue(recorded.isEmpty());
	}

	@Test
	public void invalidOptionDoesNotRecordOrConsumeQuestion() {
		List<String> recorded = new ArrayList<>();
		QuizRun run = new QuizRun(new QuizSession(
				Arrays.asList(question("q1", 0))),
				(question, correct) -> recorded.add(question.id() + ":" + correct));

		run.nextQuestion();
		try {
			run.answer(QuizQuestion.OPTION_COUNT);
			fail("expected invalid option");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}

		assertEquals(QuizRun.State.QUESTION, run.state());
		assertTrue(recorded.isEmpty());
	}

	private static QuizQuestion question(String id, int correctIndex) {
		return new QuizQuestion(id, id,
				Arrays.asList("甲", "乙", "丙", "丁"), correctIndex, "");
	}

	private static void assertIllegalState(Runnable action) {
		try {
			action.run();
			fail("expected illegal state");
		} catch (IllegalStateException expected) {
			// Expected.
		}
	}
}
