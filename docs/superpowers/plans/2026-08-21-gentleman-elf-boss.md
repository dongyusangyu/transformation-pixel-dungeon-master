# 高塔 Boss「绅士精灵」实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在高塔 Boss 轮换中加入三阶段绅士精灵，实现单层醉意/昂扬、全局宴饮、酒杯争夺、幻影辨真、永久狂暴及两件专属奖励。

**架构：** `GentlemanElf` 只保存 Boss 状态机与技能调度；`GentlemanElfArena` 管理全局回合计时、酒杯/幻影实体和清场；`GentlemanElfTelegraph` 纯计算技能区域；醉意/昂扬通过通用伤害修改接口参与普通伤害。所有动画均遵循“先提交状态、可见时回调、不可见时同步完成”的现有 Boss 模式。

**技术栈：** Java、Shattered Pixel Dungeon Actor/Buff/Bundle、JUnit 4、Gradle、Noosa 精灵动画、PNG 像素素材。

---

## 文件结构

**新建生产文件**

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/Drunkenness.java`：醉意互斥状态与 0.8 倍输出/承伤。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/Exhilaration.java`：昂扬互斥状态与 1.2 倍输出/承伤。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java`：Boss 面板、阶段锁、固定节奏、转阶段、狂暴技能。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTelegraph.java`：3×3、单线、宽 3 走廊、5×5 震席和合法落点计算。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfIllusion.java`：两次正伤害后消失的幻影。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/ElfWineCup.java`：第二阶段中立酒杯与最后一击归属。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArena.java`：全局 20 回合宴饮、酒杯重生、实体重新绑定与清理。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/GreenGlowFruit.java`：3 次、150 回合冷却、醉意与 40 Barrier。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/SourWineAroma.java`：敌人首次进入 5×5 时施加 5 回合 Vertigo。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/GentlemanElfSprite.java`：Boss/幻影动画和投酒、突进接口。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/ElfWineCupSprite.java`：绿色酒杯动画。
- `core/src/main/assets/sprites/gentleman_elf.png`：32×32 帧表。
- `core/src/main/assets/sprites/elf_wine_cup.png`：绿色酒杯帧表。

**修改生产文件**

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java`：加入小型通用伤害倍率接口，并在最终伤害前统一应用一次。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossGenerator.java`：第三个等权 Boss 条目。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java`：挂载/保存/恢复 `GentlemanElfArena`。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossMusic.java`：绅士精灵音乐回退到通用 Boss 曲目，未提供专曲时不新增音频。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`：Boss、酒杯精灵常量。
- `core/src/main/assets/messages/actors/actors.properties`、`actors_zh.properties`：Boss、幻影、酒杯、Buff、技能提示。
- `core/src/main/assets/messages/items/items.properties`、`items_zh.properties`：两件奖励文案。

**新建测试文件**

- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/GentlemanWineStateTest.java`
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTelegraphTest.java`
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java`
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArenaTest.java`
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/GentlemanElfRewardsTest.java`
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/GentlemanElfSpriteAssetTest.java`

## 任务 1：建立双向伤害倍率接缝

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/CharDamageModifierTest.java`

- [ ] **步骤 1：编写失败测试**

测试定义两个 Buff：攻击方返回 `0.8f`，防守方返回 `1.2f`；调用真实 `damage()` 路径，断言 100 点基础伤害最终为 96，并断言同一 Buff 只调用一次。接口固定为：

```java
public interface DamageMultiplier {
    default float outgoingDamageMultiplier(Object source, DamageTag... tags) { return 1f; }
    default float incomingDamageMultiplier(Object source, DamageTag... tags) { return 1f; }
}
```

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.actors.CharDamageModifierTest --no-daemon
```

预期：编译失败，`Char.DamageMultiplier` 不存在。

- [ ] **步骤 3：最小实现**

在 `Char` 中加入上面的 public 接口；在攻击伤害完成护甲/武器修正后乘攻击者 Buff 的 outgoing；在 `modifyFinalDamage` 返回前乘受击者 Buff 的 incoming。每个阶段使用快照遍历 `buffs()`，最后 `Math.round`，并将负倍率夹到 0。

- [ ] **步骤 4：运行测试验证通过并回归 Char 测试**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.actors.CharDamageModifierTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.CharAttackResolvedTest --no-daemon
```

预期：所有目标测试通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/CharDamageModifierTest.java
git commit -m "feat: add generic damage multiplier buffs"
```

## 任务 2：实现醉意与昂扬

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/Drunkenness.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/Exhilaration.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/GentlemanWineStateTest.java`

- [ ] **步骤 1：编写失败测试**

覆盖以下断言：`Drunkenness.affect(target)` 后无昂扬；`Exhilaration.affect(target)` 会移除醉意；重复施加不叠层；两个 Buff 不随 `act()` 衰减；倍率分别为 0.8/0.8 和 1.2/1.2。

```java
assertEquals(0.8f, drunk.outgoingDamageMultiplier(null), 0.001f);
assertEquals(0.8f, drunk.incomingDamageMultiplier(null), 0.001f);
assertEquals(1.2f, excited.outgoingDamageMultiplier(null), 0.001f);
assertEquals(1.2f, excited.incomingDamageMultiplier(null), 0.001f);
```

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.GentlemanWineStateTest --no-daemon
```

预期：两个 Buff 类不存在。

- [ ] **步骤 3：最小实现**

两个类继承永久 `Buff`，实现 `Char.DamageMultiplier`。各自提供静态 `affect(Char)`：先 `Buff.detach` 相反类型，再 `Buff.affect` 当前类型。类型设为正面/中性可见状态，不使用 duration 或 level 字段。

- [ ] **步骤 4：运行测试验证通过**

运行步骤 2 的命令，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/GentlemanWineStateTest.java
git commit -m "feat: add drunkenness and exhilaration states"
```

## 任务 3：实现纯地格与落点计算

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTelegraph.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTelegraphTest.java`

- [ ] **步骤 1：编写失败测试**

使用 `DeathKnightBombardment.Grid` 同样的无场景网格替身，覆盖：边缘 3×3 不越界；单线被墙截断；宽 3 走廊以 Boss—目标为中轴且延伸到墙/边界；5×5 不越界；目标落点被占时回退到最近可达格。

```java
assertArrayEquals(new int[]{44,45,46,54,55,56,64,65,66},
        GentlemanElfTelegraph.square(grid, 55, 1));
assertFalse(plan.contains(cellBehindWall));
assertEquals(nearestReachable, plan.landingCell);
```

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfTelegraphTest --no-daemon
```

- [ ] **步骤 3：最小实现**

实现不可实例化工具类与不可变 `Plan(int[] cells, int landingCell)`。所有结果按距离、格号稳定排序，便于存档和确定性测试；只接受网格接口，不直接访问 `Dungeon.level`。

- [ ] **步骤 4：运行测试验证通过**

运行步骤 2，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTelegraph.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTelegraphTest.java
git commit -m "feat: add gentleman elf telegraph geometry"
```

## 任务 4：建立 Boss 面板、锁血与第一阶段状态机

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java`

- [ ] **步骤 1：编写失败测试**

测试面板、50 点单次伤害上限、1200/600 锁血只触发一次、转阶段消耗完整 TICK、第一阶段动作序列 `NORMAL,NORMAL,TOAST,NORMAL,NORMAL,DEVOUR`、睡眠/麻痹不推进节奏、Bundle 往返保持进度。

```java
assertArrayEquals(new Skill[]{NORMAL, NORMAL, TOAST, NORMAL, NORMAL, DEVOUR},
        boss.nextSixActionsForTest());
boss.damage(200, source);
assertEquals(1450, boss.HP);
```

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfTest --no-daemon
```

- [ ] **步骤 3：最小实现**

`GentlemanElf extends TowerBoss`，枚举 `Phase { TOAST_GAME, CUP_CONTEST, MIRROR_TEST, BERSERK }` 与 `Skill { NONE, TOAST, DEVOUR, CUP_DASH, TABLE_SHOCK }`。`modifyFinalDamage` 先调用 super 再做 50 上限和锁血；`act()` 优先级固定为：无法行动 → 转阶段 → 待结算技能 → 当前阶段调度 → `super.act()`。

- [ ] **步骤 4：运行测试验证通过**

运行步骤 2；再运行 `TowerBossRecoveryTest`，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java
git commit -m "feat: add gentleman elf boss state machine"
```

## 任务 5：完成第一阶段投酒与突进

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java`

- [ ] **步骤 1：补充失败测试**

覆盖蓝色醉意/黄色昂扬严格交替；预警锁定旧位置；投酒 10 固定伤害并切换状态；DEVOUR 优先锁定当前有效 `enemy`、无敌人才回退英雄；有目标留在警告区时造成 40～60 并击退 2；目标全部离开警告区时不造成伤害、沿中轴 `jump` 最多 4 格；墙体截断和无落点取消；可见动画与不可见同步路径只结算/`next()` 一次。

- [ ] **步骤 2：运行测试验证失败**

运行任务 4 的聚焦测试，预期新增断言失败。

- [ ] **步骤 3：最小实现**

提交技能时立即保存 `pendingSkill`、`pendingCells`、`pendingLandingCell`、`pendingWineState` 和中轴方向；结算只读取保存值。警告颜色常量：醉意 `0x4F8DFF`、昂扬 `0xF2D34F`。DEVOUR 结算前只检查保存区域内的有效敌对目标：集合为空时调用 `jump` 动画移动最多 4 格，非空时结算伤害/击退。投射、冲刺和跳跃使用 `Callback`，同步分支调用同一 `resolvePendingSkill()`。

- [ ] **步骤 4：运行测试验证通过**

运行聚焦测试，预期全部通过且无 Actor 队列超时。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java
git commit -m "feat: implement gentleman elf toast duel"
```

## 任务 6：实现酒杯实体与第二阶段争夺

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/ElfWineCup.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java`

- [ ] **步骤 1：编写失败测试**

测试酒杯面板、无行动/经验/掉落、最后一击归属；生成时全场昂扬转醉意；英雄奖励 5% HP+5 Haste；Boss 奖励 100 HP 不越 1200+40 Barrier；其他击杀只获昂扬；杯消失会取消 TABLE_SHOCK。

- [ ] **步骤 2：运行测试验证失败**

运行 `GentlemanElfTest`，预期找不到 `ElfWineCup`。

- [ ] **步骤 3：最小实现**

酒杯继承 `Mob` 但 `act()` 只 `spend(TICK)`；`damage` 记录造成最后正伤害的 `Char`，`die` 回调拥有者 Arena。Boss 的争杯决策顺序：可震席 → 可扑杯 → 可攻击酒杯 → 向酒杯移动 → 普通 AI。

- [ ] **步骤 4：运行测试验证通过**

运行聚焦测试，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/ElfWineCup.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java
git commit -m "feat: add gentleman elf wine cup contest"
```

## 任务 7：实现全局宴饮与酒杯重生 Arena

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArena.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArenaTest.java`

- [ ] **步骤 1：编写失败测试**

以无渲染 Host 测试：第 19 回合只预警，第 20 回合造成 10 固定伤害并施加 3 `Vertigo`+3 `Weakness`；不影响 Boss/幻影/酒杯；阶段变化不重置；Boss 死亡停止；酒杯死亡后第 20 回合只重生一只；受阻时每回合重试但不复制；Bundle 重新绑定实体 ID。

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArenaTest --no-daemon
```

- [ ] **步骤 3：最小实现**

Arena 使用单一 `Actor` 以 `Actor.TICK` 推进，字段 `banquetTurns`、`cupRespawnTurns`、`cupId`、`illusionIds`、`bossId`。第 19 回合调用 Host 警告，第 20 回合调用 Host 结算并归零；目标集合先快照去重。`TowerBossLevel` 只负责创建、保存、恢复和清理 Arena。

- [ ] **步骤 4：运行测试验证通过**

运行步骤 2，并运行 `TowerBossLevelTest`，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArena.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArenaTest.java
git commit -m "feat: add gentleman elf arena clock"
```

## 任务 8：实现幻影辨真与永久狂暴

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfIllusion.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArena.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java`

- [ ] **步骤 1：编写失败测试**

覆盖：最多 2 幻影；三实体落点唯一且合法；幻影两次正伤害后消失、0 伤不计；本体两次正伤害清全部幻影并移除英雄醉意；逐个清幻影保留醉意；全阶段 1.6 速度/0.5 攻击；狂暴只使用宽 3 DEVOUR；优先瞄准当前有效 `enemy`；目标位置锁定；结算时区域无目标则 jump 4 格且不伤害；读档恢复命中计数和实体绑定；Boss 死亡清全部派生实体。

- [ ] **步骤 2：运行测试验证失败**

运行 `GentlemanElfTest`，预期幻影类/阶段行为缺失。

- [ ] **步骤 3：最小实现**

幻影保存 `positiveHits` 与 `ownerId`，`damage()` 通过 HP/护盾差计算正伤害事件；达到 2 时调用 Arena 清除。Boss 保存 `trueBodyPositiveHits`，只有 Arena 报告存在有效幻影时累计。进入狂暴后 `act()` 始终交替提交/结算宽 3 DEVOUR。

- [ ] **步骤 4：运行测试验证通过**

运行 `GentlemanElfTest` 和 `GentlemanElfArenaTest`，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfIllusion.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/GentlemanElfArena.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java
git commit -m "feat: add gentleman elf mirror finale"
```

## 任务 9：实现两件专属奖励

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/GreenGlowFruit.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/SourWineAroma.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/GentlemanElfRewardsTest.java`

- [ ] **步骤 1：编写失败测试**

测试果实 3 次、每次 150 冷却、醉意+40 Barrier；酒香对首次进入 5×5 的敌人施加 5 Vertigo，同敌人不重复、离开本层清跟踪、多件不叠加；已有酒香时掉落必为果实；50/50 阈值两侧。

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.artifacts.GentlemanElfRewardsTest --no-daemon
```

- [ ] **步骤 3：最小实现**

果实使用 Artifact 充能字段表示剩余次数和冷却。酒香保存当前层已触发 Actor ID 集合，Actor 不存在或换层时清理。Boss `die()` 在 `super.die()` 前生成一次专属奖励，使用可注入 roll 接缝测试 0.5 阈值。

- [ ] **步骤 4：运行测试验证通过**

运行步骤 2，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/GreenGlowFruit.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/SourWineAroma.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/GentlemanElfRewardsTest.java
git commit -m "feat: add gentleman elf rewards"
```

## 任务 10：接入 Boss 轮换与场地生命周期

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossGenerator.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossMusic.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossGeneratorTest.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java`

- [ ] **步骤 1：编写失败测试**

断言 entries 为 3，三者 weight 都为 1；固定 seed/depth/branch 可重复并能覆盖第三个 ID；`create(GENTLEMAN_ELF_ID)` 返回正确类型；场地 prepare/cleanup 只操作绅士精灵 Arena；死亡后出口解锁且专属实体全部清理。

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGeneratorTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevelTest --no-daemon
```

- [ ] **步骤 3：最小实现**

新增 `GENTLEMAN_ELF_ID = "gentleman_elf"` 和等权 Entry。`GentlemanElf.prepareArena()` 调用 `TowerBossLevel.prepareGentlemanElfArena()`；音乐映射无专曲时返回现有通用 Boss 音乐常量。

- [ ] **步骤 4：运行测试验证通过**

运行步骤 2，预期全部通过。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossGenerator.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossMusic.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossGeneratorTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java
git commit -m "feat: register gentleman elf tower boss"
```

## 任务 11：绘制并接线精灵、警告和动画

**文件：**
- 创建：`core/src/main/assets/sprites/gentleman_elf.png`
- 创建：`core/src/main/assets/sprites/elf_wine_cup.png`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/GentlemanElfSprite.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/ElfWineCupSprite.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/GentlemanElfSpriteAssetTest.java`

- [ ] **步骤 1：编写失败素材测试**

使用 ImageIO 断言 PNG 存在、单帧不超过 32×32、每个声明帧非空、透明背景、动画索引不越界；源代码断言 IDLE/RUN/ATTACK/THROW/DASH/HIT/DIE 都已定义。

- [ ] **步骤 2：运行测试验证失败**

```powershell
./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.GentlemanElfSpriteAssetTest --no-daemon
```

- [ ] **步骤 3：绘制并实现动画**

按原概念图制作 32×32 帧：绿色半透明黏液绅士、黑领带、手持酒杯、背部晶莹翅片；腿和肩形成稳定轮廓，滴液只占边缘。酒杯为 16×16。`throwWine(int cell, int color, Callback cb)`、`dash(int cell, Callback cb)` 和 `jump(int cell, Callback cb)` 必须在不可见时直接调用回调。

- [ ] **步骤 4：运行测试并人工查看帧表**

运行步骤 2；再打开两张 PNG，确认没有切帧错位、悬空像素、错误半透明边缘和方向跳变。

- [ ] **步骤 5：Commit**

```powershell
git add core/src/main/assets/sprites/gentleman_elf.png core/src/main/assets/sprites/elf_wine_cup.png core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/GentlemanElfSpriteAssetTest.java
git commit -m "feat: add gentleman elf pixel animations"
```

## 任务 12：文案、开场选择、日志与整体回归

**文件：**
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java`

- [ ] **步骤 1：补充失败测试**

测试开场选择只出现一次；饮用给醉意、拒绝给昂扬；读档不重复；所有技能消息键存在；图鉴归入高塔 Boss 栏目且顺序位于该栏目末尾；全局宴饮 message 明确写“迷乱/随机移动”而非“麻痹”。

- [ ] **步骤 2：运行测试验证失败**

运行 `GentlemanElfTest` 及现有 Bestiary 测试，预期新文案/图鉴断言失败。

- [ ] **步骤 3：实现文案和开场窗口**

复用现有 Boss 开场对话窗口；Bundle 保存 `introResolved`。中英文都提供 name/desc/notice/defeated、阶段提示、TOAST/DEVOUR/CUP_DASH/TABLE_SHOCK、宴饮倒计时与结算、两种状态和奖励描述。

- [ ] **步骤 4：运行完整目标回归**

```powershell
./gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.*" --tests "com.shatteredpixel.shatteredpixeldungeon.levels.towers.*" --tests "com.shatteredpixel.shatteredpixeldungeon.items.artifacts.GentlemanElfRewardsTest" --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.*" --no-daemon
```

预期：0 failures、0 errors；JUnit XML 的 `system-err` 为空。

- [ ] **步骤 5：运行静态检查与全 core 测试**

```powershell
git diff --check
./gradlew.bat :core:test --no-daemon
```

预期：`git diff --check` 无输出；`:core:test` 通过。若 Gradle 仅因共享 `build/reports/problems/problems-report.html` 的 AccessDenied 在测试任务后退出，必须读取最新 JUnit XML，只有测试 0 failures/0 errors 且 system-err 为空时才能记录为测试通过，并在交付中披露报告写入错误。

- [ ] **步骤 6：Commit**

```powershell
git add core/src/main/assets/messages core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElfTest.java
git commit -m "feat: finish gentleman elf boss encounter"
```

## 自检结果

- 规格的基础面板、双状态、全局宴饮、三阶段、奖励、视觉、存档和清理均有对应任务。
- `Vertigo` 在任务 7、9、12 中被明确引用；计划没有用 `Paralysis` 代替随机移动眩晕。
- `GentlemanElfArena` 是唯一全局计时器和实体所有者；Boss 状态机不重复生成酒杯或幻影。
- 所有异步技能都要求同步回退和单次 `next()`，所有待结算技能都有取消路径。
- 计划没有遗留占位语句或未定义的类型名；核心接口名称在各任务中保持一致。
