# 预知眼实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法跟踪进度。

**目标：** 实装预知眼神器、其热量/过热/必闪 Buff、命中流程接入、三位一体效果、生成与本地化，并提供回归测试。

**架构：** `PrecognitiveEye` 是负责充能、升级、装备状态与三个内部 Buff 的独立 Artifact。`Char.hit(...)` 增加预知眼专用轻量钩子，以统一处理诅咒强制命中、必闪、最小闪避值和闪避结果通知。三位一体通过 `SpiritForm` 创建独立的必闪效果，不依赖真实物品充能。

**技术栈：** Java、JUnit 4、Gradle、Shattered Pixel Dungeon `Artifact`/`Buff`/`Char.hit`/`Trinity` 框架、PNG 16×16 精灵表。

---

## 文件结构

- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEye.java` — 神器、三种 Buff、充能、经验与闪避辅助方法。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java` — 命中判定调用预知眼钩子。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java` — 加入神器生成池。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/abilities/cleric/Trinity.java` — 固定 50 护甲充能。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells/SpiritForm.java` — 灵体形态主动效果。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java` — 索引 272 语义常量。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/BuffIndicator.java` — 图标 146/147/148 具名常量。
- 修改：`core/src/main/assets/sprites/ex_items.png` — 只写入第 18 行首格。
- 修改：`core/src/main/assets/messages/items/items.properties` 与 `items_zh.properties` — 正式物品、Buff 与三位一体文案；内部 Buff 按现有 Artifact 模式使用该文件。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestArtifact.java` — 测试工具入口。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEyeTest.java` — 核心行为与生成/文本回归。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/PrecognitiveEyeSpriteTest.java` — 索引、透明度和外部像素保护。

### 任务 1：建立红灯测试与神器最小骨架

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEyeTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEye.java`

- [ ] **步骤 1：写失败的数值/热量测试**

```java
@Test public void minimumEvasionAndProgressionMatchSpecification() {
    assertEquals(5, PrecognitiveEye.minimumEvasion(100, 0));
    assertEquals(20, PrecognitiveEye.minimumEvasion(100, 5));
    assertEquals(100, PrecognitiveEye.expToNextLevel(0));
    assertEquals(550, PrecognitiveEye.expToNextLevel(9));
}

@Test public void heatDoesNotResetItsIndependentDecayClock() {
    PrecognitiveEye.PrecognitiveHeat heat = new PrecognitiveEye.PrecognitiveHeat();
    heat.set(3, 2);
    heat.addLayer();
    assertEquals(4, heat.layers());
    assertEquals(2, heat.turnsToDecay());
}
```

- [ ] **步骤 2：运行并确认红灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEyeTest"`

预期：编译失败，提示 `PrecognitiveEye` 不存在。

- [ ] **步骤 3：写最小神器与 Buff 实现**

```java
public static int minimumEvasion(int maximum, int level) {
    return Math.min(maximum, Math.round(5 + maximum * level * 0.03f));
}

public static int expToNextLevel(int level) { return 100 + 50 * level; }

public static class PrecognitiveHeat extends Buff {
    private int layers;
    private int turnsToDecay = 10;
    public void addLayer() { layers = Math.min(10, layers + 1); }
}
```

实现完整的装备生命周期、+10 封顶、Buff 序列化、10 回合独立衰减、100 回合过热和图标显示。

- [ ] **步骤 4：运行并确认绿灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEyeTest"`

预期：核心数值和独立时钟测试通过。

### 任务 2：命中钩子、诅咒与必闪

**文件：**

- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEyeTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEye.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java`

- [ ] **步骤 1：写失败的命中优先级测试**

```java
@Test public void cursedEyeForcesEnemyHitBeforeMomentaryForesight() {
    assertTrue(PrecognitiveEye.forceEnemyHit(true, true));
}

@Test public void momentaryForesightConsumesExactlyOneUse() {
    MomentaryForesight buff = new MomentaryForesight().set(2);
    assertTrue(buff.consumeDodge());
    assertEquals(1, buff.uses());
}
```

- [ ] **步骤 2：运行并确认红灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEyeTest"`

预期：缺少强制命中和必闪 API。

- [ ] **步骤 3：接入命中流程**

在 `Char.hit(...)` 中，在无限闪避/无限精准判定之前依序调用：

```java
if (PrecognitiveEye.forcesEnemyHit(attacker, defender)) return true;
if (PrecognitiveEye.consumeMomentaryForesight(attacker, defender)) return false;
defRoll = PrecognitiveEye.rollEvasion(attacker, defender, defStat);
```

所有敌对攻击对英雄的未命中出口调用：

```java
PrecognitiveEye.onEnemyAttackDodged(attacker, defender);
```

该回调只在真实装备、正常状态下增热/加经验，并保证单次 `Char.hit` 只调用一次。

- [ ] **步骤 4：运行并确认绿灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEyeTest"`

预期：诅咒优先级、必闪次数、热量转化、过热清理主动均通过。

### 任务 3：充能、经验深度和三位一体

**文件：**

- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEyeTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEye.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/abilities/cleric/Trinity.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells/SpiritForm.java`

- [ ] **步骤 1：写失败的充能/三位一体测试**

```java
@Test public void rechargeAmountsAndTrinityUsesMatchSpecification() {
    assertEquals(0.5f, PrecognitiveEye.naturalChargePerTurn(), 0f);
    assertEquals(3f, PrecognitiveEye.artifactRechargePerTurn(), 0f);
    assertEquals(1, PrecognitiveEye.trinityDodgeUses(0));
    assertEquals(4, PrecognitiveEye.trinityDodgeUses(3));
}
```

- [ ] **步骤 2：运行并确认红灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEyeTest"`

预期：辅助方法和三位一体分支不存在。

- [ ] **步骤 3：实现充能、经验和幻化**

自然充能每英雄回合累加 `0.5f * RingOfEnergy.artifactChargeMultiplier(hero)`；`ArtifactRecharge` 回调每有效回合固定加 3。过热/诅咒禁止两者。

实现：

```java
public static int effectiveDepth() {
    return Dungeon.level instanceof TowerLevel ? 30 : Dungeon.depth;
}

public static int trinityDodgeUses(int points) { return 1 + points; }
```

将 `PrecognitiveEye.class` 加入 `Trinity.trinityChargeUsePerEffect` 的 50 点分支，并在 `SpiritForm.applyActiveArtifactEffect` 施加 `MomentaryForesight`；过热时返回 `false` 且不扣费。

- [ ] **步骤 4：运行并确认绿灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEyeTest"`

预期：自然/外部充能、楼层阈值、+10 封顶、三位一体次数和 50 点消耗测试通过。

### 任务 4：生成、文本、图标与精灵表

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestArtifact.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/BuffIndicator.java`
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`
- 修改：`core/src/main/assets/sprites/ex_items.png`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/PrecognitiveEyeSpriteTest.java`

- [ ] **步骤 1：写失败的注册与素材测试**

```java
@Test public void eyeUsesReservedArtifactCellAndGeneratorContainsIt() {
    assertEquals(272, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.PRECOGNITIVE_EYE));
    assertTrue(Arrays.asList(Generator.Category.ARTIFACT.classes)
        .contains(PrecognitiveEye.class));
}
```

`PrecognitiveEyeSpriteTest` 读取 `ex_items.png`，断言索引 272 不为空、Alpha 仅 0/255、索引 209～271 透明。

- [ ] **步骤 2：运行并确认红灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEye*"`

预期：常量、生成器类和精灵单元尚不存在。

- [ ] **步骤 3：实现注册与素材写入**

添加 `PRECOGNITIVE_EYE = encode(272, 16, 16)`、三个 `BuffIndicator` 常量、生成器等权条目和 `TestArtifact` 项。将 `D:\桌面\预知眼.png` 原样复制到 `ex_items.png` 索引 272，并用像素检查保证目标格外内容未变。

添加成对中英文正式文本键；`desc()` 仅在已装备巴勒莫之剑时追加彩蛋，正常和诅咒副简介互斥。

- [ ] **步骤 4：运行并确认绿灯**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEye*"`

预期：注册、文本、图标和像素测试通过。

### 任务 5：回归验证

**文件：**

- 修改：仅限前述实现/测试文件。

- [ ] **步骤 1：运行聚焦回归**

运行：`./gradlew.bat core:test --tests "*PrecognitiveEye*" --tests "*EXItemSpriteSheetTest"`

预期：预知眼测试全部通过；若既有精灵断言因其他并发修改失败，记录精确失败项且不得调整无关素材。

- [ ] **步骤 2：运行全量核心测试**

运行：`./gradlew.bat core:test`

预期：通过；若工作区既有并发改动造成独立于本功能的失败，保留失败日志并另以聚焦测试证明本功能。

- [ ] **步骤 3：检查差异**

运行：`git diff --check -- <本任务文件列表>`

预期：无空白错误。
