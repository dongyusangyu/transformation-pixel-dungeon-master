package com.shatteredpixel.shatteredpixeldungeon.custom.quiz;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DungeonDoctorQuizTest {

	@Test
	public void questionRequiresExactlyFourShortOptions() {
		QuizQuestion question = question("q1", "哪一个是正确答案？",
				"甲", "乙", "丙", "丁", 2);

		assertEquals("q1", question.id());
		assertEquals("哪一个是正确答案？", question.prompt());
		assertEquals(4, question.options().size());
		assertEquals("丙", question.correctAnswer());
		assertTrue(question.isCorrect(2));
		assertFalse(question.isCorrect(1));

		assertInvalidQuestion("q2", "题目", Arrays.asList("甲", "乙", "丙"), 0);
		assertInvalidQuestion("q3", "题目",
				Arrays.asList("甲", "乙", "丙", "这是一个超过十八个中文字符长度限制的错误答案选项"), 0);
		assertInvalidQuestion("q4", "题目", Arrays.asList("甲", "乙", "丙", "丁"), 4);
	}

	@Test
	public void oneSessionReturnsEveryQuestionOnceThenEnds() {
		List<QuizQuestion> questions = Arrays.asList(
				question("q1", "一", "甲", "乙", "丙", "丁", 0),
				question("q2", "二", "甲", "乙", "丙", "丁", 1),
				question("q3", "三", "甲", "乙", "丙", "丁", 2));
		QuizSession session = new QuizSession(questions, new java.util.Random(1234L));
		Set<String> seen = new HashSet<>();

		while (session.hasNext()) {
			seen.add(session.next().id());
		}

		assertEquals(3, seen.size());
		assertEquals(0, session.remaining());
		assertFalse(session.hasNext());
	}

	@Test
	public void productionResourceContainsNoAnswerData() throws IOException {
		String resource = productionResource();

		assertFalse(resource.matches(
				"(?ms).*^(答案|答案文本|解析|难度|分类|来源|状态)=.*"));
		assertFalse(resource.contains("待审核"));
	}

	@Test
	public void productionResourceAndRegistryMergeIntoTwoHundredQuestions()
			throws IOException {
		List<QuizQuestion> questions = productionQuestions();

		assertEquals(200, DungeonDoctorAnswerRegistry.answers().size());
		assertEquals(200, questions.size());
		for (int i = 0; i < questions.size(); i++) {
			QuizQuestion question = questions.get(i);
			assertEquals(String.format(Locale.ROOT, "Q%03d", i + 1), question.id());
			assertEquals(QuizQuestion.OPTION_COUNT, question.options().size());
			for (String option : question.options()) {
				assertTrue(option.codePointCount(0, option.length())
						<= QuizQuestion.MAX_OPTION_LENGTH);
			}
		}
	}

	@Test
	public void representativeQuestionsMatchTheReviewedDraft() throws IOException {
		List<QuizQuestion> questions = productionQuestions();

		QuizQuestion q001 = questions.get(0);
		assertEquals("跨局时，药剂颜色与效果如何对应？", q001.prompt());
		assertEquals(Arrays.asList(
				"每局随机对应", "始终固定对应", "按职业对应", "按楼层对应"),
				q001.options());
		assertEquals(0, q001.correctIndex());
		assertEquals("每局随机对应", q001.correctAnswer());
		assertEquals("药剂颜色与效果的映射会在每局重新随机。", q001.explanation());

		QuizQuestion q100 = questions.get(99);
		assertEquals("炼金房固定生成多少炼金能量？", q100.prompt());
		assertEquals(Arrays.asList("10点", "5点", "3点", "8点"), q100.options());
		assertEquals(1, q100.correctIndex());
		assertEquals("5点", q100.correctAnswer());
		assertEquals("普通炼金房固定生成5点炼金能量。", q100.explanation());

		QuizQuestion q200 = questions.get(199);
		assertEquals("随机附魔池怎样兼顾随机与复现？", q200.prompt());
		assertEquals(Arrays.asList(
				"按英雄等级排序", "按地牢种子洗牌", "按系统时间洗牌", "每次拾取重新洗牌"),
				q200.options());
		assertEquals(1, q200.correctIndex());
		assertEquals("按地牢种子洗牌", q200.correctAnswer());
		assertEquals("附魔池内容用地牢种子与固定盐初始化后洗牌，同种子结果一致。",
				q200.explanation());
	}

	@Test
	public void productionLoadFallsBackForRuntimeAndStaticInitializationFailures() {
		List<Throwable> reported = new ArrayList<>();

		List<QuizQuestion> runtimeFallback = DungeonDoctorQuiz.loadOrEmpty(
				() -> {
					throw new IllegalArgumentException("bad resource");
				},
				reported::add);
		List<QuizQuestion> initializerFallback = DungeonDoctorQuiz.loadOrEmpty(
				() -> {
					throw new ExceptionInInitializerError(
							new IllegalArgumentException("bad registry"));
				},
				reported::add);

		assertTrue(runtimeFallback.isEmpty());
		assertTrue(initializerFallback.isEmpty());
		assertEquals(2, reported.size());
		assertTrue(reported.get(0) instanceof IllegalArgumentException);
		assertTrue(reported.get(1) instanceof ExceptionInInitializerError);
	}

	private static QuizQuestion question(String id, String prompt,
			String first, String second, String third, String fourth, int correctIndex) {
		return new QuizQuestion(id, prompt,
				Arrays.asList(first, second, third, fourth), correctIndex, "");
	}

	private static void assertInvalidQuestion(
			String id, String prompt, List<String> options, int correctIndex) {
		try {
			new QuizQuestion(id, prompt, options, correctIndex, "");
			fail("expected invalid quiz question");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	private static List<QuizQuestion> productionQuestions() throws IOException {
		return DungeonDoctorQuestionParser.parse(
				productionResource(), DungeonDoctorAnswerRegistry.answers());
	}

	private static String productionResource() throws IOException {
		Path coreDirectory = Paths.get(System.getProperty("user.dir"));
		if (!coreDirectory.endsWith("core")) {
			coreDirectory = coreDirectory.resolve("core");
		}
		Path resource = coreDirectory.resolve(
				"src/main/assets/quiz/dungeon_doctor_questions.txt");
		return new String(Files.readAllBytes(resource), StandardCharsets.UTF_8);
	}
}
