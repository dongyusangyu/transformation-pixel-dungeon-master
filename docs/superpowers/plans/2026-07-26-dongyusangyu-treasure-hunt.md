# 东隅桑榆双模式寻宝小游戏实现计划

> **面向 AI 代理的工作者：** 在当前会话中按测试驱动开发逐任务实现。步骤使用复选框（`- [ ]`）跟踪进度。

**目标：** 为东隅桑榆 NPC 增加一次性十步寻宝和可无限重玩的无限寻宝，支持消耗品奖励、实时得分、存档记录和前五名排行。

**架构：** 用纯 Java `TreasureHuntGame` 管理单局，用静态 `TreasureHuntRecords` 跟随存档生命周期，用 `TreasureHuntRewards` 隔离奖励档位。两个窗口负责 NPC 菜单和逐步选择，NPC 只负责打开菜单。

**技术栈：** Java 8、Shattered Pixel Dungeon Window/Bundle API、JUnit 4、Gradle。

**工作区说明：** 当前仓库含用户未提交的地图、NPC、资源和其他改动。本计划不创建分支、不提交、不清理任何现有文件，只修改与小游戏直接相关的路径。

---

## 文件结构

- 创建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntGame.java`：纯单局状态、选项生成与计分。
- 创建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecords.java`：一次性资格、分数和排行榜存档。
- 创建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRewards.java`：十步奖励档位与背包/掉落发放。
- 创建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHuntMenu.java`：NPC 主菜单、规则、排行和开始模式。
- 创建 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java`：实时状态和逐步选项窗口。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Dongyusangyu.java`：打开菜单。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java`：初始化、保存、恢复记录。
- 修改 `core/src/main/assets/messages/windows/windows.properties`：英文窗口文本。
- 修改 `core/src/main/assets/messages/windows/windows_zh.properties`：中文窗口文本。
- 修改 `core/src/main/assets/messages/actors/actors.properties` 与 `actors_zh.properties`：NPC 邀请文本。
- 创建三个 `core/src/test/java/.../custom/treasurehunt/*Test.java`：核心、记录和奖励测试。
- 创建 `core/src/test/java/.../actors/mobs/npcs/DongyusangyuTreasureHuntTest.java`：NPC 菜单入口集成测试。

### 任务 1：单局状态引擎

- [ ] 创建 `TreasureHuntGameTest`，先断言：
  - 初始 6 勇气、0 步、0 分。
  - 第一步固定三个风险选项。
  - 十步模式第 10 次选择后结束。
  - 无限模式第 10 次选择后仍可继续。
  - 第 5 步应用疲劳。
  - 危险选择增加余晖和连险，安全选择重置连险。
- [ ] 运行：

```powershell
.\gradlew.bat :core:test --tests "*TreasureHuntGameTest"
```

预期：因 `TreasureHuntGame` 不存在而失败。

- [ ] 创建最小实现：

```java
public final class TreasureHuntGame {
    public enum Mode { TEN_STEP, ENDLESS }
    public enum Encounter { QUIET_PATH, MONSTER_NEST, FORGOTTEN_CHEST, CAMP, AFTERGLOW }
    public static final int MAX_COURAGE = 6;
    public static final int TEN_STEP_LIMIT = 10;

    public TreasureHuntGame(Mode mode, long seed) { ... }
    public List<Choice> choices() { ... }
    public TurnResult choose(int index) { ... }
    public void cashOut() { ... }
}
```

- [ ] 重跑专项测试，确认全部通过。

### 任务 2：存档记录与排行榜

- [ ] 创建 `TreasureHuntRecordsTest`，覆盖：
  - `beginTenStep()` 仅首次返回 `true`。
  - `claimTenStepReward()` 仅首次返回 `true`。
  - 十步分数只在奖励未领取时更新。
  - 无限成绩降序插入并只保留前 5 名。
  - `storeInBundle()` / `restoreFromBundle()` 往返保持全部字段。
  - 空旧存档恢复为初始状态。
- [ ] 运行测试并确认因类缺失失败。
- [ ] 创建 `TreasureHuntRecords`，实现：

```java
public static boolean beginTenStep();
public static void updateTenStepScore(int score);
public static boolean claimTenStepReward();
public static void recordEndlessScore(int score);
public static int[] topScores();
public static void reset();
public static void storeInBundle(Bundle bundle);
public static void restoreFromBundle(Bundle bundle);
```

- [ ] 重跑记录器测试并确认通过。
- [ ] 在 `Dungeon.init()`、`Dungeon.reinit()`、`Dungeon.saveGame()`、`Dungeon.loadGame()` 接入记录器。

### 任务 3：十步奖励

- [ ] 创建 `TreasureHuntRewardsTest`，断言：
  - 399 分返回 `PotionOfHaste`。
  - 400 和 799 分返回 `ScrollOfIdentify`。
  - 800 分返回 `PotionOfHealing`。
- [ ] 运行测试并确认因类缺失失败。
- [ ] 创建 `TreasureHuntRewards`：

```java
public static Item createTenStepReward(int score);
public static Item grantTenStepReward(Hero hero, int score);
```

`grantTenStepReward` 先使用 `reward.collect(hero.belongings.backpack)`，失败时调用 `Dungeon.level.drop()`。

- [ ] 重跑奖励测试并确认通过。

### 任务 4：运行窗口与 NPC 菜单

- [ ] 创建 `DongyusangyuTreasureHuntTest`，以源码/类结构断言 NPC 不再只调用 `yell()`，而是打开 `WndTreasureHuntMenu`。
- [ ] 运行测试并确认失败。
- [ ] 创建 `WndTreasureHunt`：
  - 正文显示模式、当前步、勇气、余晖、连险、实时分数与上一回合结果。
  - 前 3 个按钮调用 `game.choose(index)`。
  - 第 4 个按钮和返回键调用统一的 `finishRun()`。
  - 十步每次选择后调用 `updateTenStepScore()` 并保存；结束时原子领取并发奖。
  - 无限结束时调用 `recordEndlessScore()` 并保存。
- [ ] 创建 `WndTreasureHuntMenu`：
  - 根据十步状态显示“开始 / 领取未结算奖励 / 已游玩”。
  - 无限模式始终可用。
  - 得分窗口显示最近、最高、局数、累计与前五名。
  - 规则窗口说明无限模式无真实奖励。
- [ ] 修改 `Dongyusangyu.interact()`，在渲染线程打开菜单。
- [ ] 重跑 NPC 专项测试并确认通过。

### 任务 5：本地化与编译

- [ ] 在中英文窗口资源中添加全部菜单、状态、遭遇、结算、规则与排行键。
- [ ] 更新中英文 NPC `hello` 文本为小游戏邀请。
- [ ] 运行：

```powershell
.\gradlew.bat :core:compileJava
```

预期：`BUILD SUCCESSFUL`。

### 任务 6：最终验证

- [ ] 运行全部新增测试：

```powershell
.\gradlew.bat :core:test --tests "*TreasureHunt*"
```

- [ ] 运行现有 NPC 行为测试：

```powershell
.\gradlew.bat :core:test --tests "*DongyusangyuTest"
```

- [ ] 运行完整核心测试，记录但不擅自修复已确认的地图基线失败：

```powershell
.\gradlew.bat :core:test
```

- [ ] 检查 `git diff --check` 和本次相关文件差异。
- [ ] 对照设计规格逐项确认：一次性资格、一次性奖励、无限无奖励、实时分数、存档排行、无新楼层。
