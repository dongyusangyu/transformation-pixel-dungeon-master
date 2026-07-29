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

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;

import java.util.Collections;
import java.util.List;

public final class DungeonDoctorQuiz {

	private static final String RESOURCE =
			"quiz/dungeon_doctor_questions.txt";

	private DungeonDoctorQuiz() {
	}

	public static List<QuizQuestion> questions() {
		return QuestionsHolder.QUESTIONS;
	}

	public static QuizSession newSession() {
		return new QuizSession(questions());
	}

	private static List<QuizQuestion> loadQuestions() {
		return loadOrEmpty(() -> {
			String resource = Gdx.files.internal(RESOURCE).readString("UTF-8");
			List<QuizQuestion> questions = DungeonDoctorQuestionParser.parse(
					resource, DungeonDoctorAnswerRegistry.answers());
			DungeonDoctorQuestionBankValidator.validateQuestions(questions);
			return questions;
		}, ShatteredPixelDungeon::reportException);
	}

	static List<QuizQuestion> loadOrEmpty(
			QuestionLoader loader, FailureReporter reporter) {
		if (loader == null || reporter == null) {
			throw new IllegalArgumentException(
					"question loader and failure reporter are required");
		}
		try {
			return loader.load();
		} catch (RuntimeException | ExceptionInInitializerError failure) {
			reporter.report(failure);
			return Collections.emptyList();
		}
	}

	interface QuestionLoader {
		List<QuizQuestion> load();
	}

	interface FailureReporter {
		void report(Throwable failure);
	}

	private static final class QuestionsHolder {

		private static final List<QuizQuestion> QUESTIONS = loadQuestions();
	}
}
