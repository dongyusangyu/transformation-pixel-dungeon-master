package com.shatteredpixel.shatteredpixeldungeon.custom.quiz;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
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

		assertReviewedQuestion(questions, 87,
				"不计相关天赋且击杀任务强敌后，拿满巨魔铁匠任务奖励至少需要多少暗金矿？",
				Arrays.asList("30个", "50个", "40个", "20个"),
				2, "不计相关天赋时，每块暗金矿值50人情，击杀任务强敌另得1000人情；达到3000人情上限共需40块暗金矿。");

		assertReviewedQuestion(questions, 71,
				"非Boss主线楼层固定生成几张蜕变卷轴？",
				Arrays.asList("每层1张", "每5层1张", "仅首层1张", "不会固定生成"),
				0, "非Boss且处于主线分支的楼层创建时，会固定加入1张蜕变卷轴。");
		assertReviewedQuestion(questions, 176,
				"制作1个蜕变结晶需要哪项配方？",
				Arrays.asList("3张蜕变卷轴+3能量", "8张蜕变卷轴+1能量",
						"1张嬗变卷轴+6能量", "8张蜕变卷轴+3能量"),
				1, "蜕变结晶由8张蜕变卷轴加1点炼金能量制成。");

		QuizQuestion q200 = questions.get(199);
		assertEquals("+2指令餐的DM-400进食后，依次物攻4个无标记敌人。新标记时长序列是？", q200.prompt());
		assertEquals(Arrays.asList(
				"5、5、5、3回合", "6、6、6、3回合", "6、6、6、6回合", "3、3、3、3回合"),
				q200.options());
		assertEquals(1, q200.correctIndex());
		assertEquals("6、6、6、3回合", q200.correctAnswer());
		assertEquals("DM-400的物理攻击通常施加3回合指令标记；+2指令餐把进食后的前3次提高到6回合，效果耗尽后第4次恢复为3回合。",
				q200.explanation());
	}

	@Test
	public void simpleAndMediumQuestionsUseDistinctOptions() throws IOException {
		List<QuizQuestion> questions = productionQuestions();

		for (int i = 0; i < 180; i++) {
			Set<String> normalized = new HashSet<>();
			for (String option : questions.get(i).options()) {
				normalized.add(option.replaceAll("\\s+", ""));
			}
			assertEquals(questions.get(i).id(), QuizQuestion.OPTION_COUNT,
					normalized.size());
		}
	}

	@Test
	public void ambiguousSimpleAndMediumQuestionsHaveOnePreciseAnswer()
			throws IOException {
		List<QuizQuestion> questions = productionQuestions();

		assertReviewedQuestion(questions, 93,
				"血色哨卫能否锁定隐形玩家？",
				Arrays.asList("能够锁定", "完全无法锁定", "仅近身时锁定", "仅水中能锁定"),
				0, "血色哨卫的锁定逻辑不受玩家隐形状态影响。");
		assertReviewedQuestion(questions, 104,
				"刨墓碑时默认尝试在哪里生成怨灵？",
				Arrays.asList("玩家周围八格", "玩家上下左右", "墓碑所在格", "房间四个角落"),
				1, "墓碑会尝试在玩家的四个正交相邻格各生成一只怨灵；被阻挡的位置不会生成。");
		assertReviewedQuestion(questions, 109,
				"无挑战影响时，治疗泉会回满什么？",
				Arrays.asList("生命与饱食度", "法杖与神器充能", "经验与天赋点", "力量与护甲"),
				0, "无相关挑战影响时，治疗泉会直接回满生命与饱食度。");
		assertReviewedQuestion(questions, 113,
				"普通商店固定生成在什么附近？",
				Arrays.asList("上楼楼梯口", "下楼楼梯口", "炼金台", "水晶门"),
				0, "普通商店的生成位置固定参考本层入口，也就是上楼楼梯口。");
		assertReviewedQuestion(questions, 120,
				"随机模式交王冠会得到什么？",
				Arrays.asList("随机专精", "随机护甲技能", "固定鼠化术", "随机神器"),
				1, "随机模式的王冠界面会显示本局预先随机出的护甲技能候选。");
		assertReviewedQuestion(questions, 128,
				"非随机模式下，极速房装备最高自带多少级？",
				Arrays.asList("+4", "+3", "+1", "+2"),
				1, "非随机模式下，极速房生成装备最高自带+2，再有三分之一概率额外升1级，因此最高为+3。");
		assertReviewedQuestion(questions, 130,
				"饮冰霜药处理火墙时应站多远？",
				Arrays.asList("距火墙三格", "距火墙两格", "站在水中", "距火墙一格"),
				3, "有多个未知药剂时，建议在距火墙一格且不在水上饮用。");
		assertReviewedQuestion(questions, 139,
				"石像掉落装备的固定特征是什么？",
				Arrays.asList("武器护甲都必加三", "武器无附魔且护甲无刻印",
						"武器附魔且护甲刻印", "武器护甲都必诅咒"),
				2, "石像武器必有附魔，护甲必有刻印；两件装备掉落时也都会被鉴定。");
		assertReviewedQuestion(questions, 140,
				"六选三房间的药剂与卷轴依据什么排序？",
				Arrays.asList("物品出售价格", "默认生成权重", "物品名称", "完全随机次序"),
				1, "房间会分别按照药剂与卷轴的默认生成权重排序，再将其放入对应位置。");
		assertReviewedQuestion(questions, 141,
				"无挑战影响时，治疗泉完整效果是哪项？",
				Arrays.asList("回满生命饱食、治病并解装备诅咒", "只回复全部生命", "只解除装备诅咒", "只补满露水袋"),
				0, "治疗泉会回满生命与饱食度，清除治疗药剂可治愈的状态，并解除已装备物品的诅咒。");
		assertReviewedQuestion(questions, 149,
				"蜜蜂的索敌范围受什么限制？",
				Arrays.asList("蜂罐周围三格", "只能贴近蜂罐", "整层地图可索敌", "离罐后无法移动"),
				0, "敌对蜜蜂只会索敌蜂罐周围三格内的目标，并会返回蜂罐附近。");
		assertReviewedQuestion(questions, 156,
				"两同一种子加一异种时哪项为真？",
				Arrays.asList("必定产出治疗药剂", "按种子决定概率更高", "完全随机概率更高", "必定产出力量药剂"),
				1, "两同一种子加一异种时，75%按投入种子决定，25%从普通药剂池完全随机。");
		assertReviewedQuestion(questions, 177,
				"随机模式如何处理生成器中的非零物品权重？",
				Arrays.asList("统一改为相同权重", "全部权重翻倍", "按区域重新加权", "只保留最高权重"),
				0, "随机模式把物品生成器中每个原本大于0的权重改为1。");
		assertReviewedQuestion(questions, 179,
				"随机模式法杖何时显示效果图标？",
				Arrays.asList("仅升级后显示", "只有诅咒时显示", "类型已知、已鉴定或无诅咒施法", "拾取后立即显示"),
				2, "法杖类型已知、完整鉴定，或成功施放过非诅咒效果后，才显示对应效果图标。");
	}

	@Test
	public void hardQuestionsRequireCombinedKnowledgeAndOnePreciseAnswer()
			throws IOException {
		List<QuizQuestion> questions = productionQuestions();

		assertReviewedQuestion(questions, 181,
				"一区凑齐3张同图案卷轴，其中1张来自野生宝箱。能排除什么？",
				Arrays.asList("它们是升级卷轴", "它们不是升级卷轴", "它们必是祛邪卷轴", "无法排除任何卷轴"),
				1, "一区固定有3张升级卷轴，但野生宝箱不会生成升级卷轴；同图案又代表同类型，因此这3张都不是升级卷轴。");
		assertReviewedQuestion(questions, 182,
				"本层有火墙，野生宝箱开出冰霜药剂。哪项判断正确？",
				Arrays.asList("宝箱承担固定解密药", "冰霜只是随机巧合", "本层会再固定生成液火", "火墙不再生成解密药"),
				1, "火墙会在合规位置固定生成冰霜药剂，野生宝箱中的冰霜药剂只来自普通随机生成，二者恰好同类是巧合。");
		assertReviewedQuestion(questions, 183,
				"一区前三层已搜尽，仅毒气房容器未开。若必须找触媒，先做什么？",
				Arrays.asList("直接离开一区", "回毒气房搜容器", "反复刷新商店", "烧毁全部书架"),
				1, "一区前三层的魔能触媒可能藏在毒气房的2个宝箱或1个遗骸中；其他位置已搜尽时，应回去检查这些容器。");
		assertReviewedQuestion(questions, 184,
				"火墙前有多瓶未知药剂，又要避免误扔力量药。怎样试最稳妥？",
				Arrays.asList("贴墙站水中饮用", "隔一格离水饮用", "逐瓶砸向火墙", "站门口全部饮用"),
				1, "无法确定冰霜药剂时，应在距火墙1格且不在水上的位置饮用；这样能让冰霜生效，也避免把力量药剂误掷出去。");
		assertReviewedQuestion(questions, 185,
				"浮空房宝箱未开，只想为升级、力量或触媒耗浮空。先做什么？",
				Arrays.asList("先喝浮空再查看", "炸开宝箱先查看", "把药剂丢进深渊", "直接放弃整层"),
				1, "先用炸弹或震爆符石远程炸开宝箱即可查看物品，再决定是否值得消耗浮空药剂前往拾取。");
		assertReviewedQuestion(questions, 186,
				"鱼房有未知隐身药和虚空锁链，目标是保留药剂并取箱。怎么做？",
				Arrays.asList("喝药潜行取箱", "拉鱼上岸再取箱", "把药丢向食人鱼", "原地等待鱼离房"),
				1, "虚空锁链能把巨型食人鱼拉上岸解决威胁，因此可以保留本层固定生成的隐身药剂再取宝箱。");
		assertReviewedQuestion(questions, 187,
				"背包药卷外观与六选三房内相同。能排除哪组身份？",
				Arrays.asList("治疗与鉴定", "极速与传送", "力量与升级", "隐身与祛邪"),
				2, "六选三属于可能错过物品的选择结构，不会放入固定的力量药剂和升级卷轴；相同外观对应的背包物品也可排除这两类。");
		assertReviewedQuestion(questions, 188,
				"极速房宝箱开出液火药。想利用额外线索，应重点找什么？",
				Arrays.asList("隐藏附魔石房", "隐藏四金箱房", "毒气房入口", "献祭房祭台"),
				0, "极速房宝箱出现液火药剂，说明本层还有为隐藏附魔石房固定生成的液火药剂，应重点检查可烧开的书架。");
		assertReviewedQuestion(questions, 189,
				"四种包均未掉，种子符石、药剂、卷轴各2件。法杖投武至少几件才锁定筒袋？",
				Arrays.asList("4件", "3件", "2件", "1件"),
				0, "种子袋初始计1分，另三种包裹初始为0分；种子符石有2件时种子袋得3分，因此法杖与投掷武器合计至少4件才能让魔法筒袋严格最高。");
		assertReviewedQuestion(questions, 190,
				"从1瓶浮空药直接炼到水爆，路线与总能耗是？",
				Arrays.asList("骤雨再水爆，共12点", "羽落再水爆，共14点", "骤雨再水爆，共8点", "直接水爆，共12点"),
				0, "浮空药剂先花4点炼成暴风骤雨合剂，再花8点炼成水爆魔药，总能耗为12点。");
		assertReviewedQuestion(questions, 191,
				"有效等级+2的横扫附魔武器对A造成100点伤害，B也在攻击范围且忽略减伤，B受多少？",
				Arrays.asList("20点", "30点", "25点", "100点"),
				2, "横扫对其他可攻击敌人造成向上取整的100×(1+2)/(10+2)=25点物理伤害；主目标A不会重复受到横扫伤害。");
		assertReviewedQuestion(questions, 192,
				"19级绅士用公文包获得武技精通，随机转职勇士且无武技训练，充能上限与回复如何？",
				Arrays.asList("8点且回复快50%", "10点且回复快50%", "10点且回复不变", "12点且回复快50%"),
				1, "武技精通的基础上限为min(2+(19-1)/3,8)=8点；勇士再增加2点，上限为10点，并使武技充能回复速度提升50%。");
		assertReviewedQuestion(questions, 193,
				"+2符石专家的矮人公主连续将3颗符石转成附魔符石，共耗多少炼金能量？",
				Arrays.asList("4点", "18点", "24点", "12点"),
				3, "符石转换的基础消耗为8点，+2符石专家减少4点，因此每颗消耗4点；连续转换3颗共消耗12点炼金能量。");
		assertReviewedQuestion(questions, 194,
				"+1符文爆破引爆8层印记，3根未满充法杖均可充能，共回复多少？",
				Arrays.asList("每根2点，共6点", "三根合计2点", "每根8点，共24点", "仅施法杖回复2点"),
				0, "+1符文爆破按印记层数×0.25为所有法杖充能；8层使每根回复2点，3根未满充法杖合计回复6点。");
		assertReviewedQuestion(questions, 195,
				"+2极恶中队下，哪类单位的物理攻击会同步英雄的大部分物攻特效和增伤？",
				Arrays.asList("仅英雄亲自召唤者", "仅公文包召唤者", "任意盟友阵营怪物", "只有无人机类盟友"),
				2, "极恶中队不限定召唤来源或怪物种类；只要怪物处于盟友阵营，+1同步大部分物攻特效，+2还会同步大部分物攻增伤。");
		assertReviewedQuestion(questions, 196,
				"+2忍者便当下，背包没有手里剑箱，吃丰饶之角提供的食物会怎样？",
				Arrays.asList("丰饶之角自充2回合", "充能2回合，丰饶角除外", "生成一个手里剑箱", "不产生任何充能"),
				1, "背包没有手里剑箱时，+2忍者便当改为提供2回合神器充能；食物来自丰饶之角时，该充能不会作用于丰饶之角自身。");
		assertReviewedQuestion(questions, 197,
				"开启荒芜之地时，+2粘液温室的史莱姆娘在体内发芽一颗种子，耗时与饱食如何？",
				Arrays.asList("不耗回合并回复30饱食", "20回合且不回复饱食", "1回合并回复30饱食", "15回合并回复30饱食"),
				3, "粘液温室通常让体内发芽不耗回合，但荒芜之地会改用挑战耗时；升级后耗时由20回合降至15回合，+2同时回复30点饱食度。");
		assertReviewedQuestion(questions, 198,
				"非决斗家已有武技训练，并拥有+2专注一餐。进食后的充能与兼容增伤如何？",
				Arrays.asList("获1点充能，无兼容增伤", "获等级一半增伤，无充能", "充能和增伤同时获得", "两种效果都不触发"),
				0, "武技训练已经使非决斗家能够使用武技，因此+2专注一餐触发常规效果并获得1点武技充能，不再触发下次攻击增加英雄等级一半伤害的兼容效果。");
		assertReviewedQuestion(questions, 199,
				"+2炼金屏障的炼金师分两次耗能升级工具箱，随后被敌人造成正伤害，层数如何？",
				Arrays.asList("先叠至4层，受击后3层", "先叠至2层，受击后1层", "先叠至3层，受击后2层", "工具箱升级不加屏障"),
				2, "+2炼金屏障让每次消耗炼金能量获得2层屏障，工具箱供能升级也会触发；两次本应获得4层但上限为3层，敌人造成正伤害后消耗1层，剩2层。");
		assertReviewedQuestion(questions, 200,
				"+2指令餐的DM-400进食后，依次物攻4个无标记敌人。新标记时长序列是？",
				Arrays.asList("5、5、5、3回合", "6、6、6、3回合", "6、6、6、6回合", "3、3、3、3回合"),
				1, "DM-400的物理攻击通常施加3回合指令标记；+2指令餐把进食后的前3次提高到6回合，效果耗尽后第4次恢复为3回合。");

		for (int i = 180; i < 200; i++) {
			Set<String> normalized = new HashSet<>();
			for (String option : questions.get(i).options()) {
				normalized.add(option.replaceAll("\\s+", ""));
			}
			assertEquals(questions.get(i).id(), QuizQuestion.OPTION_COUNT,
					normalized.size());
		}
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

	@Test
	public void obsoleteMetamorphosisRecipeIsAbsent() throws IOException {
		String resource = productionResource();
		assertFalse(resource.contains("嬗变卷轴可炼成哪种秘卷"));
		assertFalse(resource.contains("制作蜕变秘卷需要哪项配方"));

		for (QuizQuestion question : productionQuestions()) {
			assertFalse(question.explanation().contains(
					"嬗变卷轴加6点炼金能量可制成蜕变秘卷"));
			assertFalse(question.explanation().contains(
					"蜕变秘卷由嬗变卷轴加6点炼金能量制成"));
		}
	}

	@Test
	public void availableQuestionsExcludeOnlyCorrectQuestionIds() {
		List<QuizQuestion> source = Arrays.asList(
				question("Q001", "一", "甲", "乙", "丙", "丁", 0),
				question("Q002", "二", "甲", "乙", "丙", "丁", 1));
		List<QuizQuestion> original = new ArrayList<>(source);
		Set<String> excluded = new HashSet<>(Collections.singleton("Q001"));

		List<QuizQuestion> available = DungeonDoctorQuiz.availableQuestions(
				source, excluded);

		assertEquals(1, available.size());
		assertEquals("Q002", available.get(0).id());
		assertEquals(original, source);
		assertEquals(Collections.singleton("Q001"), excluded);
	}

	@Test
	public void availableQuestionsCanExcludeEveryQuestion() {
		List<QuizQuestion> source = Arrays.asList(
				question("Q001", "一", "甲", "乙", "丙", "丁", 0),
				question("Q002", "二", "甲", "乙", "丙", "丁", 1));

		List<QuizQuestion> available = DungeonDoctorQuiz.availableQuestions(
				source, new HashSet<>(Arrays.asList("Q001", "Q002")));

		assertTrue(available.isEmpty());
	}

	private static QuizQuestion question(String id, String prompt,
			String first, String second, String third, String fourth, int correctIndex) {
		return new QuizQuestion(id, prompt,
				Arrays.asList(first, second, third, fourth), correctIndex, "");
	}

	private static void assertReviewedQuestion(List<QuizQuestion> questions, int number,
			String prompt, List<String> options, int correctIndex, String explanation) {
		QuizQuestion question = questions.get(number - 1);
		assertEquals(String.format(Locale.ROOT, "Q%03d", number), question.id());
		assertEquals(prompt, question.prompt());
		assertEquals(options, question.options());
		assertEquals(correctIndex, question.correctIndex());
		assertEquals(explanation, question.explanation());
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
