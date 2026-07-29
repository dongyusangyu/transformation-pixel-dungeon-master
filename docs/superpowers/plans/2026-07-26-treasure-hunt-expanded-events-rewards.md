# 寻宝小游戏事件扩展与奖励分层实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 把寻宝小游戏扩展到 15 个无固定出现事件，校准随机选择策略下的局长与得分期望，并实现完整十步优先的三档随机消耗品奖励和事件专属叙事。

**架构：** `TreasureHuntGame` 负责事件合法性、等权抽取、局内资源和可复现随机；`TreasureHuntRewards` 使用独立 `java.util.Random` 从显式白名单抽奖；`TreasureHuntRecords` 保存异常中断后仍需使用的完整十步标记；窗口层只负责事件键映射、叙事和结算。核心行为由真实对象测试，平衡由固定种子的 100,000 局模拟测试。

**技术栈：** Java、JUnit 4、libGDX/Shattered Pixel Dungeon 窗口与消息资源、Gradle。

---

## 文件结构

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGame.java`
  - 定义 15 个事件、状态合法性、随机抽取和事件效果。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRewards.java`
  - 定义三档奖励优先级和独立随机白名单。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecords.java`
  - 保存十步是否完整完成，支持异常中断后正确奖励。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java`
  - 映射 15 个按钮和专属叙事，结算时传递完整十步状态。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHuntMenu.java`
  - 恢复未领奖局时使用保存的完整十步状态。
- 修改：`core/src/main/assets/messages/windows/windows.properties`
  - 增加英文事件按钮、叙事和新规则。
- 修改：`core/src/main/assets/messages/windows/windows_zh.properties`
  - 增加中文事件按钮、叙事和新规则。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGameTest.java`
  - 测试事件池、数值、可复现性和数学期望。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRewardsTest.java`
  - 测试奖励优先级、500 分边界和白名单。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecordsTest.java`
  - 测试完整十步标记的存档生命周期。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/DongyusangyuTreasureHuntTest.java`
  - 测试 15 个中英文按钮与叙事资源完整性。

### 任务 1：扩展并校准事件核心

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGameTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGame.java`

- [ ] **步骤 1：编写失败的事件池测试**

增加测试，要求枚举恰好 15 项、每次 3 个互不重复选项、跨种子没有任何首回合事件稳定出现，并且模拟过程中 15 项均可出现：

```java
@Test
public void fifteenEventsAreRandomlyOfferedWithoutAStableOpeningEvent() {
    assertEquals(15, TreasureHuntGame.Encounter.values().length);
    Set<TreasureHuntGame.Encounter> openingIntersection =
            new HashSet<>(Arrays.asList(TreasureHuntGame.Encounter.values()));
    Set<TreasureHuntGame.Encounter> seen = new HashSet<>();

    for (long seed = 0; seed < 4096; seed++) {
        TreasureHuntGame game = new TreasureHuntGame(TreasureHuntGame.Mode.TEN_STEP, seed);
        Set<TreasureHuntGame.Encounter> opening = encountersOf(game);
        assertEquals(3, opening.size());
        openingIntersection.retainAll(opening);
        while (!game.finished()) {
            seen.addAll(encountersOf(game));
            game.choose((int) Math.floorMod(seed + game.step(), game.choices().size()));
        }
    }

    assertTrue(openingIntersection.isEmpty());
    assertEquals(15, seen.size());
}
```

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntGameTest"
```

预期：FAIL，事件枚举仍只有 5 项且首步固定。

- [ ] **步骤 3：编写失败的数值与平衡测试**

增加每项事件的上下限断言，并以固定游戏种子和独立选择种子运行 100,000 局：

```java
@Test
public void uniformRandomPlayStaysInsideBalanceTargets() {
    long totalSteps = 0;
    long totalScore = 0;
    Random chooser = new Random(20260726L);

    for (int seed = 1; seed <= 100_000; seed++) {
        TreasureHuntGame game =
                new TreasureHuntGame(TreasureHuntGame.Mode.TEN_STEP, seed);
        while (!game.finished()) {
            game.choose(chooser.nextInt(game.choices().size()));
        }
        totalSteps += game.step();
        totalScore += game.score();
    }

    double meanSteps = totalSteps / 100_000d;
    double meanScore = totalScore / 100_000d;
    assertTrue(meanSteps >= 5.9 && meanSteps <= 6.5);
    assertTrue(meanScore >= 380 && meanScore <= 420);
}
```

- [ ] **步骤 4：实现 15 事件与统一随机抽取**

将枚举替换为：

```java
public enum Encounter {
    MOSSY_PATH, ANCIENT_RUNES, BROKEN_BRIDGE, LOST_GHOST,
    RAT_AMBUSH, TOXIC_THICKET, FALLING_ROCKS, STONE_GUARDIAN,
    DARK_RIDDLE, CURSED_ALTAR, MIMIC_CHEST, ABYSS_SHORTCUT,
    EMBER_CAMP, MOON_WELL, EMBER_PACT
}
```

`buildChoices()` 构造所有当前合法事件，`Collections.shuffle(candidates, random)` 后取前三项；删除 `step == 0` 的固定分支。`buildChoice()` 严格实现规格表中的区间和 `3r/5r/8r` 连险奖励。危险事件为鼠群伏击至深渊捷径，其余事件重置连险。

- [ ] **步骤 5：运行事件测试并确认绿灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntGameTest"
```

预期：BUILD SUCCESSFUL，平衡测试均值位于规定容差。

### 任务 2：实现完整十步存档标记

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecordsTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecords.java`

- [ ] **步骤 1：编写失败的存档测试**

```java
@Test
public void completedTenStepStateSurvivesBundleRoundTrip() {
    TreasureHuntRecords.beginTenStep();
    TreasureHuntRecords.updateTenStepProgress(640, true);

    Bundle bundle = new Bundle();
    TreasureHuntRecords.storeInBundle(bundle);
    TreasureHuntRecords.reset();
    TreasureHuntRecords.restoreFromBundle(bundle);

    assertEquals(640, TreasureHuntRecords.tenStepScore());
    assertTrue(TreasureHuntRecords.tenStepCompleted());
}
```

同时断言 `beginTenStep()` 和 `reset()` 会清除完成标记，领取奖励后进度不能再改变。

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntRecordsTest"
```

预期：编译失败，缺少 `updateTenStepProgress` 和 `tenStepCompleted`。

- [ ] **步骤 3：实现并保存完成标记**

增加字段和 bundle 键：

```java
private static final String TEN_STEP_COMPLETED = "ten_step_completed";
private static boolean tenStepCompleted;

public static void updateTenStepProgress(int score, boolean completed) {
    if (tenStepStarted && !tenStepRewardClaimed) {
        tenStepScore = Math.max(tenStepScore, Math.max(0, score));
        tenStepCompleted |= completed;
    }
}

public static boolean tenStepCompleted() {
    return tenStepCompleted;
}
```

旧存档缺少该键时恢复为 `false`。

- [ ] **步骤 4：运行记录测试并确认绿灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntRecordsTest"
```

预期：BUILD SUCCESSFUL。

### 任务 3：实现三档随机奖励池

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRewardsTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRewards.java`

- [ ] **步骤 1：编写失败的奖励优先级测试**

使用固定 `java.util.Random` 重复抽取并收集类型：

```java
@Test
public void completingTenStepsOverridesScoreWithPremiumReward() {
    Set<Class<?>> classes = rewardClasses(0, true, 128);
    assertEquals(setOf(PotionOfExperience.class, ScrollOfTransmutation.class), classes);
}

@Test
public void scoreBoundaryUsesLowPoolAt500AndOrdinaryPoolAbove500() {
    assertOnlyLowRewards(rewardClasses(500, false, 1024));
    assertOnlyOrdinaryPotionOrScroll(rewardClasses(501, false, 1024));
}
```

明确断言中档不会出现力量/经验药剂或升级/嬗变卷轴，低档不会出现规格排除项。

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntRewardsTest"
```

预期：编译失败，旧 API 不接收完成标记与随机源。

- [ ] **步骤 3：实现奖励白名单**

新增：

```java
public static Class<? extends Item> tenStepRewardClass(
        int score, boolean completedTenSteps, Random random)
```

完整十步从 `{PotionOfExperience, ScrollOfTransmutation}` 二选一；未完成 `score > 500` 先二选一普通药剂/卷轴类别，再从 10 项白名单抽取；其余先二选一普通符石/种子类别，再从 10 项白名单抽取。`createTenStepReward` 使用 `Reflection.newInstance`，`grantTenStepReward` 内部以 `System.nanoTime()` 创建独立 `java.util.Random`。

- [ ] **步骤 4：运行奖励测试并确认绿灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHuntRewardsTest"
```

预期：BUILD SUCCESSFUL。

### 任务 4：接入事件叙事和新结算参数

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/DongyusangyuTreasureHuntTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHuntMenu.java`
- 修改：`core/src/main/assets/messages/windows/windows.properties`
- 修改：`core/src/main/assets/messages/windows/windows_zh.properties`

- [ ] **步骤 1：编写失败的资源覆盖测试**

把 `CHOICE_KEYS` 扩展为 15 个键，并对每个键断言中英文 `choice_<key>` 和 `narrative_<key>` 均存在。选择按钮继续断言中文不超过 10 字符、英文不超过 22 字符且不含 `%`。

增加源代码断言：

```java
assertTrue(windowSource.contains("encounterNarrative(result.encounter())"));
assertTrue(windowSource.contains("game.completedTenSteps()"));
assertTrue(menuSource.contains("TreasureHuntRecords.tenStepCompleted()"));
```

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*DongyusangyuTreasureHuntTest"
```

预期：FAIL，缺少新事件资源和叙事映射。

- [ ] **步骤 3：实现窗口映射和资源**

`choiceKey()`、`encounterNarrative()` 覆盖 15 个枚举。`turn_result` 的第一个参数改为专属叙事。十步每回合调用：

```java
TreasureHuntRecords.updateTenStepProgress(
        game.score(), game.completedTenSteps());
```

正常结算传入 `game.completedTenSteps()`，恢复未领奖结算传入 `TreasureHuntRecords.tenStepCompleted()`。

中英文资源增加规格中的 15 个按钮和叙事，更新 NPC 规则说明及奖励档位。

- [ ] **步骤 4：运行界面测试并确认绿灯**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*DongyusangyuTreasureHuntTest"
```

预期：BUILD SUCCESSFUL。

### 任务 5：回归验证

**文件：**
- 检查：所有上述生产与测试文件。

- [ ] **步骤 1：运行寻宝专项测试**

```powershell
.\gradlew.bat --no-problems-report :core:test --tests "*TreasureHunt*"
```

预期：BUILD SUCCESSFUL。

- [ ] **步骤 2：运行 Android Debug 构建**

```powershell
.\gradlew.bat --no-problems-report :android:assembleDebug
```

预期：BUILD SUCCESSFUL，并生成 `android/build/outputs/apk/debug/android-debug.apk`。

- [ ] **步骤 3：检查差异质量**

```powershell
git diff --check
git status --short
```

预期：`git diff --check` 退出码为 0；状态中只报告现有工作区改动和本次修改，不覆盖或删除用户文件。
