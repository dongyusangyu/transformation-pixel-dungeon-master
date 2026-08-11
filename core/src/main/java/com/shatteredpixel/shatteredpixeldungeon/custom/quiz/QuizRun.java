/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.quiz;

public final class QuizRun {

	public interface AnswerRecorder {
		void record(QuizQuestion question, boolean correct);
	}

	public enum State {
		READY,
		QUESTION,
		RESULT,
		COMPLETE,
		FINISHED
	}

	private final QuizSession session;
	private final AnswerRecorder recorder;
	private State state = State.READY;
	private QuizQuestion currentQuestion;

	public QuizRun(QuizSession session, AnswerRecorder recorder) {
		if (session == null || recorder == null) {
			throw new IllegalArgumentException("quiz run dependencies cannot be null");
		}
		this.session = session;
		this.recorder = recorder;
	}

	public QuizQuestion nextQuestion() {
		if (state != State.READY && state != State.RESULT) {
			throw new IllegalStateException("quiz run cannot present another question");
		}
		currentQuestion = session.next();
		state = currentQuestion == null ? State.COMPLETE : State.QUESTION;
		return currentQuestion;
	}

	public Answer answer(int selectedIndex) {
		if (state != State.QUESTION) {
			throw new IllegalStateException("quiz run is not awaiting an answer");
		}
		if (selectedIndex < 0 || selectedIndex >= QuizQuestion.OPTION_COUNT) {
			throw new IllegalArgumentException("invalid quiz option");
		}

		boolean correct = currentQuestion.isCorrect(selectedIndex);
		recorder.record(currentQuestion, correct);
		state = State.RESULT;
		return new Answer(currentQuestion, correct);
	}

	public boolean hasNextQuestion() {
		return state == State.RESULT && session.hasNext();
	}

	public void finish() {
		if (state != State.RESULT && state != State.COMPLETE) {
			throw new IllegalStateException("quiz run cannot finish from the current state");
		}
		state = State.FINISHED;
		currentQuestion = null;
	}

	public State state() {
		return state;
	}

	public static final class Answer {

		private final QuizQuestion question;
		private final boolean correct;

		private Answer(QuizQuestion question, boolean correct) {
			this.question = question;
			this.correct = correct;
		}

		public QuizQuestion question() {
			return question;
		}

		public boolean correct() {
			return correct;
		}
	}
}
