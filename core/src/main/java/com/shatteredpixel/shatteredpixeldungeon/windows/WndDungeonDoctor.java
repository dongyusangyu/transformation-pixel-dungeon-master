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

	enum MainAction {
		START,
		STATISTICS,
		RESET,
		LEAVE
	}

	enum StartState {
		UNAVAILABLE,
		MASTERED,
		READY
	}

	private final DungeonDoctor doctor;

	public WndDungeonDoctor(DungeonDoctor doctor) {
		super(doctor.sprite(), Messages.titleCase(doctor.name()),
				Messages.get(WndDungeonDoctor.class, "welcome"),
				Messages.get(WndDungeonDoctor.class, "start"),
				Messages.get(WndDungeonDoctor.class, "statistics"),
				Messages.get(WndDungeonDoctor.class, "reset"),
				Messages.get(WndDungeonDoctor.class, "leave"));
		this.doctor = doctor;
	}

	@Override
	protected void onSelect(int index) {
		super.onSelect(index);
		switch (mainAction(index)) {
			case START:
				startQuiz();
				break;
			case STATISTICS:
				showStatistics();
				break;
			case RESET:
				confirmReset();
				break;
			case LEAVE:
				break;
		}
	}

	private void startQuiz() {
		if (startState(DungeonDoctorQuiz.questions().isEmpty(), null)
				== StartState.UNAVAILABLE) {
			GameScene.show(new WndOptions(
					Messages.get(WndDungeonDoctor.class, "empty_title"),
					Messages.get(WndDungeonDoctor.class, "empty_body"),
					Messages.get(WndDungeonDoctor.class, "close")));
			return;
		}

		QuizRun run = new QuizRun(
				DungeonDoctorQuiz.newSession(), WndDungeonDoctor::recordAnswer);
		QuizQuestion question = run.nextQuestion();
		if (startState(false, question) == StartState.MASTERED) {
			GameScene.show(new WndOptions(
					Messages.get(WndDungeonDoctor.class, "mastered_title"),
					Messages.get(WndDungeonDoctor.class, "mastered_body"),
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

	private void confirmReset() {
		GameScene.show(new WndOptions(
				Messages.get(WndDungeonDoctor.class, "reset_title"),
				Messages.get(WndDungeonDoctor.class, "reset_body"),
				Messages.get(WndDungeonDoctor.class, "reset_confirm"),
				Messages.get(WndDungeonDoctor.class, "cancel")) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (resetIfConfirmed(index)) {
					showResetComplete();
				}
			}
		});
	}

	private void showResetComplete() {
		GameScene.show(new WndOptions(
				Messages.get(WndDungeonDoctor.class, "reset_complete_title"),
				Messages.get(WndDungeonDoctor.class, "reset_complete_body"),
				Messages.get(WndDungeonDoctor.class, "close")));
	}

	static MainAction mainAction(int index) {
		switch (index) {
			case 0:
				return MainAction.START;
			case 1:
				return MainAction.STATISTICS;
			case 2:
				return MainAction.RESET;
			default:
				return MainAction.LEAVE;
		}
	}

	static StartState startState(
			boolean questionBankEmpty, QuizQuestion firstQuestion) {
		if (questionBankEmpty) {
			return StartState.UNAVAILABLE;
		}
		return firstQuestion == null ? StartState.MASTERED : StartState.READY;
	}

	static void recordAnswer(QuizQuestion question, boolean correct) {
		SPDSettings.recordQuizAnswer(correct);
		if (correct) {
			SPDSettings.recordCorrectQuizQuestion(question.id());
		}
	}

	static boolean resetIfConfirmed(int index) {
		if (index != 0) {
			return false;
		}
		SPDSettings.resetCorrectQuizQuestions();
		return true;
	}
}
