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
import java.util.Random;

public final class QuizSession {

	private final ArrayList<QuizQuestion> questions;
	private int nextIndex;

	public QuizSession(List<QuizQuestion> questions) {
		this(questions, new Random());
	}

	public QuizSession(List<QuizQuestion> questions, Random random) {
		this.questions = new ArrayList<>();
		if (questions != null) {
			this.questions.addAll(questions);
		}
		Collections.shuffle(this.questions, random == null ? new Random() : random);
	}

	public boolean hasNext() {
		return nextIndex < questions.size();
	}

	public QuizQuestion next() {
		return hasNext() ? questions.get(nextIndex++) : null;
	}

	public int remaining() {
		return questions.size() - nextIndex;
	}
}
