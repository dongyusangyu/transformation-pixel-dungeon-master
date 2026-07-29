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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

final class DungeonDoctorQuestionBankValidator {

	static final int QUESTION_COUNT = 200;

	private DungeonDoctorQuestionBankValidator() {
	}

	static void validateIds(Iterable<String> ids) {
		if (ids == null) {
			throw new IllegalArgumentException("production question ids are required");
		}
		Iterator<String> iterator = ids.iterator();
		for (int i = 1; i <= QUESTION_COUNT; i++) {
			String expected = String.format(Locale.ROOT, "Q%03d", i);
			if (!iterator.hasNext() || !expected.equals(iterator.next())) {
				throw new IllegalArgumentException(
						"production questions must be Q001 through Q200 in order");
			}
		}
		if (iterator.hasNext()) {
			throw new IllegalArgumentException(
					"production question bank contains more than 200 questions");
		}
	}

	static void validateQuestions(List<QuizQuestion> questions) {
		if (questions == null) {
			throw new IllegalArgumentException("production questions are required");
		}
		ArrayList<String> ids = new ArrayList<>(questions.size());
		for (QuizQuestion question : questions) {
			if (question == null) {
				throw new IllegalArgumentException(
						"production question cannot be null");
			}
			ids.add(question.id());
		}
		validateIds(ids);
	}
}
