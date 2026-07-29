# 地牢博士 200 题题库实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法跟踪进度。当前工作区包含大量用户改动，不创建 worktree、不提交、不整理无关文件。

**目标：** 将原稿 Q001–Q200 接入地牢博士问答系统，同时保证发布资源只包含题干和选项，答案与解析只存在于 Java 注册表。

**架构：** `DungeonDoctorQuestionParser` 负责解析无答案资源并与 `DungeonDoctorAnswerRegistry` 合并；`DungeonDoctorQuiz` 只负责读取、缓存和安全回退。解析入口接收普通字符串，可在无 libGDX 环境中测试；生产入口使用 libGDX 内部资源 API。

**技术栈：** Java、libGDX `Gdx.files.internal`、JUnit 4、UTF-8 分块文本资源。

---

## 文件结构

- 创建 `core/src/main/assets/quiz/dungeon_doctor_questions.txt`：Q001–Q200 的题号、题干和 A/B/C/D，不含答案信息。
- 创建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/DungeonDoctorAnswerRegistry.java`：200 个正确索引和解析。
- 创建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/DungeonDoctorQuestionParser.java`：严格解析和资源/答案合并。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/DungeonDoctorQuiz.java`：延迟加载、缓存和失败回退。
- 修改 `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/DungeonDoctorQuizTest.java`：200 题完整性和典型题核对。
- 创建 `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/DungeonDoctorQuestionParserTest.java`：非法资源和错配答案测试。

### 任务 1：先固定资源保密和 200 题完整性契约

- [x] **步骤 1：修改 `DungeonDoctorQuizTest`，编写失败测试**

测试从 `core/src/main/assets/quiz/dungeon_doctor_questions.txt` 读取 UTF-8 文本，并断言：

```java
assertFalse(resource.contains("答案="));
assertFalse(resource.contains("答案文本="));
assertFalse(resource.contains("解析="));
assertEquals(200, DungeonDoctorAnswerRegistry.answers().size());

List<QuizQuestion> questions = DungeonDoctorQuestionParser.parse(
        resource, DungeonDoctorAnswerRegistry.answers());
assertEquals(200, questions.size());
assertEquals("Q001", questions.get(0).id());
assertEquals("Q200", questions.get(199).id());
```

- [x] **步骤 2：创建 `DungeonDoctorQuestionParserTest`，覆盖错误输入**

使用最小题块和测试答案映射分别验证：

```java
assertInvalid("[Q001]\n题干=题目\nA=甲\nB=乙\nC=丙\n");
assertInvalid("[Q001]\n题干=题目\nA=甲\nB=乙\nC=丙\nD=丁\n未知=值\n");
assertInvalid(duplicateQ001Blocks);
assertInvalid(questionWithOptionLongerThan18CodePoints);
assertInvalid(questionWithoutMatchingAnswer);
```

- [x] **步骤 3：运行红灯测试**

运行：

```powershell
.\gradlew.bat core:test --tests "*DungeonDoctorQuizTest" --tests "*DungeonDoctorQuestionParserTest" --no-problems-report
```

预期：因资源、注册表和解析器尚不存在而编译或断言失败。

### 任务 2：机械生成题目资源和 Java 答案注册表

- [x] **步骤 1：从已审计原稿解析 Q001–Q200**

逐块读取：

```text
D:\STUDY\Dungeon\tools\地牢博士题库_SPD攻略_初稿_v1.txt
```

生成前再次验证题号连续、四个选项齐全、答案为 A–D、答案文本匹配、选项长度不超过 18。

- [x] **步骤 2：生成无答案资源**

每题输出：

```text
[Q001]
题干=跨局时，药剂颜色与效果如何对应？
A=每局随机对应
B=始终固定对应
C=按职业对应
D=按楼层对应
```

禁止复制 `答案`、`答案文本`、`解析`、`来源`、`状态`、`难度`、`分类`。

- [x] **步骤 3：生成 Java 注册表**

注册表使用不可变 `Answer` 值对象：

```java
register(answers, "Q001", 0,
        "药剂颜色与效果的映射会在每局重新随机。");
```

`register` 对重复 ID、非法索引和空解析抛出 `IllegalArgumentException`；最终映射用 `Collections.unmodifiableMap` 包装。

### 任务 3：实现严格解析和生产加载

- [x] **步骤 1：实现 `DungeonDoctorQuestionParser.parse`**

方法签名：

```java
public static List<QuizQuestion> parse(
        String resource,
        Map<String, DungeonDoctorAnswerRegistry.Answer> answers)
```

逐行状态机只接受 `[Qddd]`、`题干`、`A`、`B`、`C`、`D` 和空行。遇到未知字段、块外字段、重复字段或重复 ID 时抛出 `IllegalArgumentException`。每个题块结束时构造 `QuizQuestion`。

- [x] **步骤 2：验证资源与注册表集合完全相等**

解析完成后执行：

```java
if (!questionIds.equals(answers.keySet())) {
    throw new IllegalArgumentException("question and answer ids do not match");
}
return Collections.unmodifiableList(questions);
```

- [x] **步骤 3：修改 `DungeonDoctorQuiz`**

生产加载逻辑：

```java
private static final String RESOURCE =
        "quiz/dungeon_doctor_questions.txt";

private static List<QuizQuestion> loadQuestions() {
    try {
        String text = Gdx.files.internal(RESOURCE).readString("UTF-8");
        return DungeonDoctorQuestionParser.parse(
                text, DungeonDoctorAnswerRegistry.answers());
    } catch (RuntimeException e) {
        ShatteredPixelDungeon.reportException(e);
        return Collections.emptyList();
    }
}
```

通过延迟持有者缓存结果，`questions()` 返回同一不可变列表，`newSession()` 使用该列表。

- [x] **步骤 4：运行题库测试转绿**

运行任务 1 的定向命令，预期所有测试通过。

### 任务 4：核对典型题和运行时契约

- [x] **步骤 1：添加 Q001、Q100、Q200 精确核对**

断言题干、四个选项、正确索引、正确答案和解析与原稿一致，防止整体偏移一题。

- [x] **步骤 2：添加 200 个 ID 与选项限制遍历**

```java
for (int i = 0; i < questions.size(); i++) {
    assertEquals(String.format(Locale.ROOT, "Q%03d", i + 1),
            questions.get(i).id());
    assertEquals(4, questions.get(i).options().size());
}
```

- [x] **步骤 3：验证资源不泄露答案**

除禁止字段外，检查资源中不存在原稿头部或 `状态=待审核`，并确认 Java 注册表源文件包含解析记录。

### 任务 5：最终验证和审查

- [ ] **步骤 1：运行地牢博士定向测试**

```powershell
.\gradlew.bat core:test --tests "*DungeonDoctorQuizTest" --tests "*DungeonDoctorQuestionParserTest" --tests "*QuizRunTest" --tests "*SPDSettingsQuizTest" --tests "*DungeonDoctorTest" --tests "*SurfaceTownLevelDongyusangyuTest" --no-problems-report
```

- [ ] **步骤 2：运行完整回归**

```powershell
.\gradlew.bat core:test --no-problems-report
```

记录总测试数，并将工作区既有 `TreasureJournalViewTest` 失败与本次结果分开。

- [ ] **步骤 3：运行差异检查和独立代码审查**

对题库相关文件运行 `git diff --check`，核对资源无答案字段、注册表 200 项、解析器边界及延迟加载线程安全。
