/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DungeonDoctor;
import com.shatteredpixel.shatteredpixeldungeon.custom.quiz.DungeonDoctorQuiz;
import com.shatteredpixel.shatteredpixeldungeon.custom.quiz.QuizQuestion;
import com.shatteredpixel.shatteredpixeldungeon.custom.quiz.QuizRun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

import java.util.Locale;

public class WndDungeonDoctor extends WndOptions {

	private final DungeonDoctor doctor;

	public WndDungeonDoctor(DungeonDoctor doctor) {
		super(doctor.sprite(), Messages.titleCase(doctor.name()),
				Messages.get(WndDungeonDoctor.class, "welcome"),
				Messages.get(WndDungeonDoctor.class, "start"),
				Messages.get(WndDungeonDoctor.class, "statistics"),
				Messages.get(WndDungeonDoctor.class, "leave"));
		this.doctor = doctor;
	}

	@Override
	protected void onSelect(int index) {
		super.onSelect(index);
		if (index == 0) {
			startQuiz();
		} else if (index == 1) {
			showStatistics();
		}
	}

	private void startQuiz() {
		QuizRun run = new QuizRun(
				DungeonDoctorQuiz.newSession(), SPDSettings::recordQuizAnswer);
		QuizQuestion question = run.nextQuestion();
		if (question == null) {
			GameScene.show(new WndOptions(
					Messages.get(WndDungeonDoctor.class, "empty_title"),
					Messages.get(WndDungeonDoctor.class, "empty_body"),
					Messages.get(WndDungeonDoctor.class, "close")));
			return;
		}
		showQuestion(run, question);
	}

	private void showNextQuestion(QuizRun run) {
		QuizQuestion question = run.nextQuestion();
		if (question == null) {
			return;
		}
		showQuestion(run, question);
	}

	private void showQuestion(QuizRun run, QuizQuestion question) {
		String[] options = question.options().toArray(new String[QuizQuestion.OPTION_COUNT]);
		GameScene.show(new WndOptions(
				Messages.get(WndDungeonDoctor.class, "question_title"),
				question.prompt(), options) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				showResult(run, run.answer(index));
			}
		});
	}

	private void showResult(QuizRun run, QuizRun.Answer answer) {
		QuizQuestion question = answer.question();
		String outcome = Messages.get(WndDungeonDoctor.class,
				answer.correct() ? "correct" : "incorrect");
		String body = Messages.get(WndDungeonDoctor.class, "result",
				outcome, question.correctAnswer());
		if (!question.explanation().isEmpty()) {
			body += "\n\n" + question.explanation();
		}

		String[] options = run.hasNextQuestion()
				? new String[]{
						Messages.get(WndDungeonDoctor.class, "next"),
						Messages.get(WndDungeonDoctor.class, "finish")}
				: new String[]{Messages.get(WndDungeonDoctor.class, "complete")};
		GameScene.show(new WndOptions(
				Messages.get(WndDungeonDoctor.class, "result_title"), body, options) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (run.hasNextQuestion() && index == 0) {
					showNextQuestion(run);
				} else {
					run.finish();
				}
			}
		});
	}

	private void showStatistics() {
		String accuracy = String.format(Locale.ROOT, "%.1f%%",
				SPDSettings.quizAccuracyPercent());
		GameScene.show(new WndOptions(
				Messages.get(WndDungeonDoctor.class, "statistics_title"),
				Messages.get(WndDungeonDoctor.class, "statistics_body",
						SPDSettings.quizAnswersTotal(),
						SPDSettings.quizAnswersCorrect(),
						accuracy),
				Messages.get(WndDungeonDoctor.class, "close")));
	}
}
