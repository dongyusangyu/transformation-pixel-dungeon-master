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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class DungeonDoctorQuestionParser {

	private static final Pattern QUESTION_HEADER = Pattern.compile("\\[Q\\d{3}\\]");

	private DungeonDoctorQuestionParser() {
	}

	public static List<QuizQuestion> parse(
			String resource,
			Map<String, DungeonDoctorAnswerRegistry.Answer> answers) {
		if (resource == null || answers == null) {
			throw new IllegalArgumentException("question resource and answers are required");
		}

		LinkedHashMap<String, QuestionFields> parsed = new LinkedHashMap<>();
		QuestionFields current = null;
		String[] lines = resource.split("\\r?\\n", -1);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (i == 0 && line.startsWith("\uFEFF")) {
				line = line.substring(1);
			}
			if (line.trim().isEmpty()) {
				continue;
			}

			if (QUESTION_HEADER.matcher(line).matches()) {
				String id = line.substring(1, line.length() - 1);
				if (parsed.containsKey(id)) {
					throw invalid(i, "duplicate question " + id);
				}
				current = new QuestionFields(id);
				parsed.put(id, current);
				continue;
			}

			if (current == null) {
				throw invalid(i, "field outside a question block");
			}
			int separator = line.indexOf('=');
			if (separator <= 0) {
				throw invalid(i, "malformed question field");
			}
			current.put(line.substring(0, separator),
					line.substring(separator + 1), i);
		}

		if (!parsed.keySet().equals(answers.keySet())) {
			throw new IllegalArgumentException(
					"question and answer ids do not match");
		}

		ArrayList<QuizQuestion> questions = new ArrayList<>(parsed.size());
		for (Map.Entry<String, QuestionFields> entry : parsed.entrySet()) {
			DungeonDoctorAnswerRegistry.Answer answer = answers.get(entry.getKey());
			if (answer == null) {
				throw new IllegalArgumentException(
						"missing answer for " + entry.getKey());
			}
			QuestionFields fields = entry.getValue();
			questions.add(new QuizQuestion(
					entry.getKey(),
					fields.require("题干"),
					Arrays.asList(
							fields.require("A"),
							fields.require("B"),
							fields.require("C"),
							fields.require("D")),
					answer.correctIndex(),
					answer.explanation()));
		}
		return Collections.unmodifiableList(questions);
	}

	private static IllegalArgumentException invalid(int lineIndex, String message) {
		return new IllegalArgumentException(
				"invalid dungeon doctor question resource at line "
						+ (lineIndex + 1) + ": " + message);
	}

	private static final class QuestionFields {

		private final String id;
		private final LinkedHashMap<String, String> values = new LinkedHashMap<>();

		private QuestionFields(String id) {
			this.id = id;
		}

		private void put(String key, String value, int lineIndex) {
			if (!key.equals("题干")
					&& !key.equals("A")
					&& !key.equals("B")
					&& !key.equals("C")
					&& !key.equals("D")) {
				throw invalid(lineIndex, "unknown field " + key);
			}
			if (values.containsKey(key)) {
				throw invalid(lineIndex, "duplicate field " + key + " in " + id);
			}
			values.put(key, value);
		}

		private String require(String key) {
			String value = values.get(key);
			if (value == null || value.trim().isEmpty()) {
				throw new IllegalArgumentException(
						"missing field " + key + " in " + id);
			}
			return value;
		}
	}
}
