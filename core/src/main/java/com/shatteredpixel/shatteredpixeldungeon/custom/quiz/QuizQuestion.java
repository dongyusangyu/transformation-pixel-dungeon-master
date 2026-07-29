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
import java.util.Collections;
import java.util.List;

public final class QuizQuestion {

	public static final int OPTION_COUNT = 4;
	public static final int MAX_OPTION_LENGTH = 18;

	private final String id;
	private final String prompt;
	private final List<String> options;
	private final int correctIndex;
	private final String explanation;

	public QuizQuestion(String id, String prompt, List<String> options,
			int correctIndex, String explanation) {
		if (isBlank(id) || isBlank(prompt) || options == null
				|| options.size() != OPTION_COUNT
				|| correctIndex < 0 || correctIndex >= OPTION_COUNT) {
			throw new IllegalArgumentException("invalid quiz question");
		}

		ArrayList<String> optionCopy = new ArrayList<>(OPTION_COUNT);
		for (String option : options) {
			if (isBlank(option)
					|| option.codePointCount(0, option.length()) > MAX_OPTION_LENGTH) {
				throw new IllegalArgumentException("invalid quiz option");
			}
			optionCopy.add(option);
		}

		this.id = id;
		this.prompt = prompt;
		this.options = Collections.unmodifiableList(optionCopy);
		this.correctIndex = correctIndex;
		this.explanation = explanation == null ? "" : explanation;
	}

	public String id() {
		return id;
	}

	public String prompt() {
		return prompt;
	}

	public List<String> options() {
		return options;
	}

	public int correctIndex() {
		return correctIndex;
	}

	public String correctAnswer() {
		return options.get(correctIndex);
	}

	public String explanation() {
		return explanation;
	}

	public boolean isCorrect(int selectedIndex) {
		return selectedIndex == correctIndex;
	}

	private static boolean isBlank(String text) {
		return text == null || text.trim().isEmpty();
	}
}
