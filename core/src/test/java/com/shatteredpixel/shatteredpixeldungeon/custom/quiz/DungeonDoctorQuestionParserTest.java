package com.shatteredpixel.shatteredpixeldungeon.custom.quiz;

import org.junit.Test;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DungeonDoctorQuestionParserTest {

	@Test
	public void parsesACompleteQuestionAndMergesItsAnswer() {
		List<QuizQuestion> questions = DungeonDoctorQuestionParser.parse(
				validQuestion("Q001"),
				answers("Q001", 2, "测试解析。"));

		assertEquals(1, questions.size());
		assertEquals("Q001", questions.get(0).id());
		assertEquals(2, questions.get(0).correctIndex());
		assertEquals("丙", questions.get(0).correctAnswer());
		assertEquals("测试解析。", questions.get(0).explanation());
	}

	@Test
	public void rejectsMissingOptionAndUnknownField() {
		assertInvalid(
				"[Q001]\n题干=题目\nA=甲\nB=乙\nC=丙\n",
				answers("Q001", 0, "解析。"));
		assertInvalid(
				validQuestion("Q001") + "未知=值\n",
				answers("Q001", 0, "解析。"));
	}

	@Test
	public void rejectsDuplicateQuestionAndDuplicateField() {
		assertInvalid(
				validQuestion("Q001") + "\n" + validQuestion("Q001"),
				answers("Q001", 0, "解析。"));
		assertInvalid(
				validQuestion("Q001") + "A=重复\n",
				answers("Q001", 0, "解析。"));
	}

	@Test
	public void rejectsOverlongOption() {
		String resource = validQuestion("Q001").replace(
				"A=甲", "A=这是一个超过十八个中文字符长度限制的错误答案选项");

		assertInvalid(resource, answers("Q001", 0, "解析。"));
	}

	@Test
	public void rejectsMissingOrExtraAnswerRegistration() {
		assertInvalid(validQuestion("Q001"), Collections.emptyMap());

		Map<String, DungeonDoctorAnswerRegistry.Answer> answers =
				answers("Q001", 0, "解析。");
		answers.put("Q002", new DungeonDoctorAnswerRegistry.Answer(1, "多余解析。"));
		assertInvalid(validQuestion("Q001"), answers);
	}

	@Test
	public void productionIdsMustBeExactlyQ001ThroughQ200InOrder() {
		List<String> validIds = new ArrayList<>();
		for (int i = 1; i <= 200; i++) {
			validIds.add(String.format(Locale.ROOT, "Q%03d", i));
		}
		DungeonDoctorQuestionBankValidator.validateIds(validIds);

		assertInvalidProductionIds(validIds.subList(0, 199));

		List<String> startsAtZero = new ArrayList<>(validIds);
		startsAtZero.set(0, "Q000");
		assertInvalidProductionIds(startsAtZero);

		List<String> endsAtTwoHundredOne = new ArrayList<>(validIds);
		endsAtTwoHundredOne.set(199, "Q201");
		assertInvalidProductionIds(endsAtTwoHundredOne);

		List<String> outOfOrder = new ArrayList<>(validIds);
		Collections.swap(outOfOrder, 0, 1);
		assertInvalidProductionIds(outOfOrder);
	}

	private static String validQuestion(String id) {
		return "[" + id + "]\n"
				+ "题干=题目\n"
				+ "A=甲\n"
				+ "B=乙\n"
				+ "C=丙\n"
				+ "D=丁\n";
	}

	private static Map<String, DungeonDoctorAnswerRegistry.Answer> answers(
			String id, int correctIndex, String explanation) {
		Map<String, DungeonDoctorAnswerRegistry.Answer> answers =
				new LinkedHashMap<>();
		answers.put(id,
				new DungeonDoctorAnswerRegistry.Answer(correctIndex, explanation));
		return answers;
	}

	private static void assertInvalid(
			String resource,
			Map<String, DungeonDoctorAnswerRegistry.Answer> answers) {
		try {
			DungeonDoctorQuestionParser.parse(resource, answers);
			fail("expected invalid dungeon doctor question resource");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	private static void assertInvalidProductionIds(Iterable<String> ids) {
		try {
			DungeonDoctorQuestionBankValidator.validateIds(ids);
			fail("expected invalid production question ids");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}
}
