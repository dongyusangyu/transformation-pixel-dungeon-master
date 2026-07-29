# 寻宝小游戏短标签与随机结果实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 让寻宝按钮在竖屏完整显示，并把准确得分、勇气与余烬变化改为选择后揭示的区间随机结果。

**架构：** `TreasureHuntGame` 在生成 `Choice` 时使用已有的种子随机数确定全部结果，`choose()` 只应用已生成值。`TurnResult` 返回勇气和余烬的实际净变化，`WndTreasureHunt` 只显示短风险标签并在下一窗口正文揭示结果。

**技术栈：** Java 8、Shattered Pixel Dungeon `WndOptions`、`java.util.Random`、JUnit 4、Gradle。

**工作区说明：** 当前功能及相关地图仍包含用户未提交文件。本计划在现有工作区内增量修改，不创建 worktree、不提交、不清理其他改动。

---

## 文件结构

- 修改 `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGameTest.java`：先定义随机区间、种子确定性和余烬净变化行为。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGame.java`：生成并应用随机结果。
- 修改 `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/DongyusangyuTreasureHuntTest.java`：验证中英文按钮文案长度与无数字格式参数。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java`：不再向选项文案传入准确得分，结算正文显示余烬变化。
- 修改 `core/src/main/assets/messages/windows/windows.properties`：英文短标签和揭示文本。
- 修改 `core/src/main/assets/messages/windows/windows_zh.properties`：中文短标签和揭示文本。

### 任务 1：锁定随机区间和确定性

**文件：**
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGameTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGame.java`

- [ ] **步骤 1：编写失败测试**

新增测试，遍历种子 `0L` 到 `63L`，检查首步三个选项：

```java
TreasureHuntGame game = new TreasureHuntGame(Mode.ENDLESS, seed);
Choice quiet = choice(game, Encounter.QUIET_PATH);
assertBetween(quiet.scoreGain(), 15, 30);
assertEquals(0, quiet.courageDelta());
assertEquals(0, quiet.afterglowDelta());

Choice monster = choice(game, Encounter.MONSTER_NEST);
assertBetween(monster.scoreGain(), 50, 80);
assertBetween(monster.courageDelta(), -2, -1);
assertBetween(monster.afterglowDelta(), 1, 2);

Choice chest = choice(game, Encounter.FORGOTTEN_CHEST);
assertBetween(chest.scoreGain(), 100, 160);
assertBetween(chest.courageDelta(), -3, -2);
assertBetween(chest.afterglowDelta(), 2, 3);
```

收集怪物分数、勇气和余烬的组合，断言至少存在两个不同组合。再创建两个相同种子的游戏，逐项断言首步 `Choice` 数值相同。

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntGameTest"
```

预期：随机多样性断言失败，因为现有同类遭遇在所有种子下数值固定。

- [ ] **步骤 3：实现区间随机**

在 `TreasureHuntGame` 中加入闭区间工具：

```java
private int randomBetween(int min, int max) {
    return min + random.nextInt(max - min + 1);
}
```

`buildChoice()` 按规格表创建 `Choice`。步数和连击加成使用当前 `step`、`riskStreak`，分数结果继续通过饱和加法写入总分。

- [ ] **步骤 4：重跑测试并确认绿灯**

运行相同命令，预期 `BUILD SUCCESSFUL`。

### 任务 2：揭示实际余烬变化

**文件：**
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGameTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGame.java`

- [ ] **步骤 1：编写失败测试**

在危险选择测试中保存选择前余烬，选择后断言：

```java
int afterglowBefore = game.afterglow();
TurnResult result = game.choose(monsterIndex);
assertEquals(game.afterglow() - afterglowBefore, result.afterglowChange());
```

- [ ] **步骤 2：运行并确认编译红灯**

运行 `TreasureHuntGameTest`，预期因 `afterglowChange()` 尚不存在而编译失败。

- [ ] **步骤 3：最小实现**

给 `TurnResult` 增加 `afterglowChange` 字段、构造参数和 getter。`choose()` 在修改前保存 `afterglowBefore`，返回 `afterglow - afterglowBefore`，其中包含第五步疲劳和下限裁剪。

- [ ] **步骤 4：重跑测试并确认绿灯**

运行相同专项测试，预期 `BUILD SUCCESSFUL`。

### 任务 3：缩短按钮并在正文揭示结果

**文件：**
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/DongyusangyuTreasureHuntTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java`
- 修改：`core/src/main/assets/messages/windows/windows.properties`
- 修改：`core/src/main/assets/messages/windows/windows_zh.properties`

- [ ] **步骤 1：编写失败测试**

读取中英文属性文件的五个 `choice_*` 值，断言：

```java
assertFalse(value.contains("%"));
assertTrue(chineseValue.length() <= 10);
assertTrue(englishValue.length() <= 22);
```

读取 `WndTreasureHunt.java`，断言选项文案调用不再传入 `choice.scoreGain()`，并包含 `result.afterglowChange()`。

- [ ] **步骤 2：运行并确认红灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*DongyusangyuTreasureHuntTest"
```

预期：现有选项仍含 `%d`，文案长度断言失败。

- [ ] **步骤 3：实现短标签和揭示文本**

把五个中英文 `choice_*` 改为规格中的短标签。`WndTreasureHunt.options()` 只用 `Messages.get(..., choiceKey)`；`turnResult()` 传入遭遇名、得分、勇气净变化、余烬净变化和疲劳后缀。

中文结果模板：

```properties
windows.wndtreasurehunt.turn_result=上一步：%1$s\n得分 +%2$d，勇气 %3$+d，余烬 %4$+d%5$s
```

- [ ] **步骤 4：运行两组专项测试**

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntGameTest" --tests "*DongyusangyuTreasureHuntTest"
```

预期：`BUILD SUCCESSFUL`。

### 任务 4：最终验证

**文件：**
- 检查所有本计划修改文件。

- [ ] **步骤 1：运行全部寻宝专项测试**

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHunt*"
```

预期：`BUILD SUCCESSFUL`。

- [ ] **步骤 2：打包 Android Debug**

```powershell
.\gradlew.bat --no-problems-report :android:assembleDebug
```

预期：`BUILD SUCCESSFUL`，输出 `android/build/outputs/apk/debug/android-debug.apk`。

- [ ] **步骤 3：检查差异**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGame.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java core/src/main/assets/messages/windows/windows.properties core/src/main/assets/messages/windows/windows_zh.properties
```

预期：无空白错误；CRLF 提示不视为失败。

- [ ] **步骤 4：需求核对**

确认五个按钮不再显示具体数值，随机结果只生成一次，选择后能看到实际得分/勇气/余烬变化，十步奖励和无限排行保持原状。

