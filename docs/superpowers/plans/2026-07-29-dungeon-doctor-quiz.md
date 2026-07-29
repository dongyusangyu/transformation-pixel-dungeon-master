# 地牢博士问答 NPC 实现计划

> **面向 AI 代理的工作者：** 在当前工作区内按测试驱动方式执行。工作区已有大量用户改动，不创建新 worktree、不提交、不整理无关文件。

**目标：** 在第 0 层加入使用老杖匠贴图的“地牢博士”，提供连续四选一答题框架，并以 `SPDSettings` 保存跨存档统计。

**架构：** `QuizQuestion`、`QuizSession` 和 `QuizRun` 保持纯 Java、便于测试；`DungeonDoctorQuiz` 是后续 PDF 题库的单一入口；`WndDungeonDoctor` 只负责渲染由 `QuizRun` 驱动的窗口；`DungeonDoctor` 只负责 NPC 行为；`SurfaceTownLevel` 只负责固定生成。

**技术栈：** Java、SPD `NPC`/`WndOptions`/`Messages`、`GameSettings`、JUnit 4。

---

### 任务 1：题目模型和连续会话

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/QuizQuestion.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/QuizSession.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/DungeonDoctorQuiz.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/quiz/DungeonDoctorQuizTest.java`

- [x] 先测试恰好四个选项、18 字符限制、正确索引和同轮不重复。
- [x] 运行 `core:test --tests "*DungeonDoctorQuizTest"`，确认因类型不存在而失败。
- [x] 实现不可变题目、洗牌会话和空生产题库。
- [x] 重跑测试并确认通过。

### 任务 2：全局统计

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/SPDSettings.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/SPDSettingsQuizTest.java`

- [x] 先测试累计总数、正确数、正确率、重置及非法持久值修正。
- [x] 运行测试，确认缺少统计 API。
- [x] 添加 `quizAnswersTotal()`、`quizAnswersCorrect()`、`recordQuizAnswer()`、`quizAccuracyPercent()` 和 `resetQuizStatistics()`。
- [x] 重跑测试并确认通过。

### 任务 3：NPC 和连续窗口

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/DungeonDoctor.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndDungeonDoctor.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/DungeonDoctorTest.java`

- [x] 先测试 NPC 的贴图类、不可移动、无限闪避、免伤和拒绝 Buff。
- [x] 实现 NPC 交互并打开主菜单。
- [x] 实现开始答题、四选一、结果、下一题/结束、统计和空题库窗口。
- [x] 抽取并测试 `QuizRun`，覆盖单题只记一次、继续、结束和耗尽状态。
- [x] 重跑 NPC 及题库测试。

### 任务 4：第 0 层生成与本地化

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/main/assets/messages/windows/windows.properties`
- 修改：`core/src/main/assets/messages/windows/windows_zh.properties`

- [x] 先将地表层测试改为期望三个 NPC，并验证地牢博士在 `(22, 4)`。
- [x] 在 `createMobs()` 中保证只生成一个地牢博士。
- [x] 添加完整中英文消息并检查关键消息唯一存在。
- [x] 运行地牢博士全部定向测试。

### 任务 5：验证

- [x] 运行地牢博士、地表层和 `SPDSettings` 定向测试。
- [x] 运行完整 `core:test`，区分本次回归与工作区既有失败。
- [x] 运行 `git diff --check` 并审查本次文件差异。
