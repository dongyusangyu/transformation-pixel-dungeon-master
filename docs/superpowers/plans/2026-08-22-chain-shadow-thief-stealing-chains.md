# 链影盗贼携物收链动画实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为链影盗贼增加“锁链伸向英雄—接触时偷窃—物品贴在活动链尾回收到盗贼”的双阶段动画，并保持现有战斗、掉落、距离和存档规则不变。

**架构：** 新建独立的 `StealingChains` 视觉组件，负责伸出/回收时间轴、链环布局、活动链尾坐标和 `ItemSprite` 跟随；组件通过返回 `Item` 的接触回调与无返回值的完成回调和怪物通信，不直接访问英雄背包或怪物状态。`ChainShadowThief` 继续拥有资格检查和真实偷窃逻辑，在锁链接触英雄时把最终持有的实际物品交给视觉组件，回收完成后才进入 `FLEEING` 并恢复 Actor 队列。

**技术栈：** Java 8、JUnit 4、Noosa `Group/Image/Game`、Shattered Pixel Dungeon `ItemSprite/Effects/Callback`、Gradle。

**已确认设计：** `docs/superpowers/specs/2026-08-22-chain-shadow-thief-stealing-chains-design.md`

---

## 文件结构

- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java`，只负责双阶段锁链视觉、回调和物品贴图跟随。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChainsTest.java`，用纯坐标测试锁定运动轨迹，不启动渲染器。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java`，把可见动画替换为专用特效并调整结算时序。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThiefAnimationContractTest.java`，锁定专用特效接线和回调职责。
- 复用：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThiefTest.java`，继续验证整组物品、一次性状态、Bundle 和普通 Thief 回归。

---

### 任务 1：以失败测试锁定活动链尾的双阶段轨迹

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChainsTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java`

- [ ] **步骤 1：编写失败的纯坐标测试**

```java
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.utils.PointF;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StealingChainsTest {

    private static final PointF FROM = new PointF(10f, 20f);
    private static final PointF TO = new PointF(30f, 40f);

    @Test
    public void tipMovesFromThiefToHeroAndBack() {
        assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, 0f));
        assertPoint(20f, 30f, StealingChains.tipPosition(FROM, TO, 0.25f));
        assertPoint(30f, 40f, StealingChains.tipPosition(FROM, TO, 0.5f));
        assertPoint(20f, 30f, StealingChains.tipPosition(FROM, TO, 0.75f));
        assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, 1f));
    }

    @Test
    public void progressIsClampedAtBothEnds() {
        assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, -1f));
        assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, 2f));
    }

    @Test
    public void lifecycleGuardsBothCallbacksAgainstDuplicateExecution() throws Exception {
        String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);
        assertTrue(source.contains("if (!contactCalled && progress >= 0.5f)"));
        assertTrue(source.contains("if (completionCalled) return;"));
    }

    private static void assertPoint(float x, float y, PointF actual) {
        assertEquals(x, actual.x, 0.0001f);
        assertEquals(y, actual.y, 0.0001f);
    }

    private static Path sourcePath() {
        Path working = Paths.get(System.getProperty("user.dir"));
        Path core = Files.isDirectory(working.resolve("core"))
                ? working.resolve("core") : working;
        return core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java");
    }
}
```

- [ ] **步骤 2：运行测试并验证红灯**

```powershell
.\gradlew.bat :core:test --no-problems-report --tests com.shatteredpixel.shatteredpixeldungeon.effects.StealingChainsTest
```

预期：因 `StealingChains` 或 `tipPosition` 不存在而编译失败。若 Gradle 先被工作区已有的 `GentlemanElfRewardsTest` 缺少 `GreenGlowFruit` 导入阻断，记录该无关阻断，不修改无关测试；任务 3 使用手动聚焦 JUnit 验证。

- [ ] **步骤 3：创建最小且完整的 `StealingChains`**

类的公开接口必须固定为：

```java
public class StealingChains extends Group {
    public interface ItemProvider {
        Item provide();
    }

    public StealingChains(PointF from, PointF to, Effects.Type type,
                          ItemProvider itemProvider, Callback completion)

    public static PointF tipPosition(PointF from, PointF to, float progress)
}
```

`tipPosition` 使用总进度 `0..1`：`0` 为盗贼端，`0.5` 为英雄端，`1` 回到盗贼端，并把越界进度钳制到两端。

构造器实现以下字段和初始化：

```java
private static final double DEGREES = 180 / Math.PI;
private static final float ITEM_OFFSET_Y = -2f;

private final PointF from;
private final PointF to;
private final Image[] chains;
private final float distance;
private final float legDuration;
private final ItemProvider itemProvider;
private final Callback completion;

private float spent;
private boolean contactCalled;
private boolean completionCalled;
private ItemSprite carriedSprite;
```

- 防御性复制 `from`、`to`。
- `distance = hypot(dx, dy)`。
- 单程时长与现有 `Chains` 一致：`distance / 320f + 0.2f`；总时长是单程两倍。
- 链环数量与现有 `Chains` 一致：`Math.round(distance / 6f) + 1`。
- 每个链环使用 `new Image(Effects.get(type))`，角度与原 `Chains` 一致。

核心 `update()` 必须按以下顺序执行：

```java
@Override
public void update() {
    super.update();
    float progress = Math.min(1f, (spent += Game.elapsed) / (legDuration * 2f));

    if (!contactCalled && progress >= 0.5f) {
        contactCalled = true;
        Item carried = itemProvider == null ? null : itemProvider.provide();
        if (carried != null) {
            carriedSprite = new ItemSprite(carried);
            carriedSprite.originToCenter();
            add(carriedSprite);
        }
    }

    PointF tip = tipPosition(from, to, progress);
    layoutChains(tip);
    if (carriedSprite != null) {
        carriedSprite.center(new PointF(tip.x, tip.y + ITEM_OFFSET_Y));
    }

    if (progress >= 1f) finish();
}
```

`layoutChains` 只显示从盗贼端到当前活动链尾所需的链环，并让可见链环均匀分布：

```java
private void layoutChains(PointF tip) {
    float currentDistance = (float) Math.hypot(tip.x - from.x, tip.y - from.y);
    int active = Math.min(chains.length,
            Math.max(1, Math.round(currentDistance / 6f) + 1));
    int denominator = Math.max(1, active - 1);
    for (int i = 0; i < chains.length; i++) {
        chains[i].visible = i < active;
        if (chains[i].visible) {
            float segment = i / (float) denominator;
            chains[i].center(new PointF(
                    from.x + (tip.x - from.x) * segment,
                    from.y + (tip.y - from.y) * segment));
        }
    }
}
```

完成回调必须用布尔门闩保证一次性：

```java
private void finish() {
    if (completionCalled) return;
    completionCalled = true;
    killAndErase();
    if (completion != null) completion.call();
}
```

不要向视觉组件传入 Hero、Mob、Bag 或状态对象，也不要在组件内复制物品。

- [ ] **步骤 4：重跑测试确认绿灯并编译主代码**

```powershell
.\gradlew.bat :core:compileJava --rerun-tasks --no-problems-report
.\gradlew.bat :core:test --no-problems-report --tests com.shatteredpixel.shatteredpixeldungeon.effects.StealingChainsTest
```

预期：主代码 `BUILD SUCCESSFUL`；3个特效测试通过，覆盖坐标、边界钳制和回调单次门闩，或仅被已记录的无关测试编译错误阻断。

- [ ] **步骤 5：提交专用特效与坐标测试**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChainsTest.java
git commit -m "feat: 添加链影盗贼携物收链特效"
```

---

### 任务 2：把真实偷窃移动到接触点，把逃跑移动到回收完成点

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThiefAnimationContractTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java`

- [ ] **步骤 1：编写失败的接线契约测试**

```java
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChainShadowThiefAnimationContractTest {

    @Test
    public void visibleTheftUsesDedicatedOutAndBackChains() throws Exception {
        String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);
        assertTrue(source.contains("new StealingChains("));
        assertFalse(source.contains("new Chains("));
    }

    @Test
    public void theftOccursAtContactAndEscapeStartsAtCompletion() throws Exception {
        String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);
        int effect = source.indexOf("new StealingChains(");
        int provider = source.indexOf("public Item provide()", effect);
        int steal = source.indexOf("stealItem(hero, toSteal)", provider);
        int completion = source.indexOf("public void call()", steal);
        int flee = source.indexOf("state = FLEEING", completion);
        int next = source.indexOf("next()", flee);
        assertTrue(effect >= 0);
        assertTrue(provider > effect);
        assertTrue(steal > provider);
        assertTrue(completion > steal);
        assertTrue(flee > completion);
        assertTrue(next > flee);
    }

    private static Path sourcePath() {
        Path working = Paths.get(System.getProperty("user.dir"));
        Path core = Files.isDirectory(working.resolve("core"))
                ? working.resolve("core") : working;
        return core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java");
    }
}
```

- [ ] **步骤 2：运行测试确认正确红灯**

```powershell
.\gradlew.bat :core:test --no-problems-report --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThiefAnimationContractTest
```

预期：当前源文件仍使用 `new Chains(`，因此至少第一个测试失败。

- [ ] **步骤 3：只替换 `tryChainSteal()` 的可见动画分支**

在 `ChainShadowThief.java` 删除 `effects.Chains` 导入，添加 `effects.StealingChains`。保留以下现有用户改动：

- 初始化块中的戒指/神器固有掉落与 `lootChance = 0.03f`。
- `lootChance()` 的限量掉落倍率。
- 资格判断中的 `Dungeon.level.distance(pos, hero.pos) < 4`。

把 `new Chains(...)` 替换为：

```java
Sample.INSTANCE.play(Assets.Sounds.CHAINS);
sprite.parent.add(new StealingChains(
        sprite.center(),
        hero.sprite.destinationCenter(),
        Effects.Type.CHAIN,
        new StealingChains.ItemProvider() {
            @Override
            public Item provide() {
                return stealItem(hero, toSteal) ? item : null;
            }
        },
        new Callback() {
            @Override
            public void call() {
                if (chainUsed) {
                    state = FLEEING;
                }
                next();
            }
        }));
return ChainResult.WAITING;
```

不可见同步分支保持原状：

```java
if (!animate) {
    return stealItem(hero, toSteal) ? ChainResult.COMPLETE : ChainResult.NONE;
}
```

- [ ] **步骤 4：运行契约测试和既有偷窃测试**

```powershell
.\gradlew.bat :core:test --no-problems-report --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThiefAnimationContractTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThiefTest
```

预期：新契约测试通过；既有测试继续证明整组数量、第二次偷窃失败、Bundle 和普通 Thief 回归。

- [ ] **步骤 5：提交怪物接线与契约测试**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThiefAnimationContractTest.java
git commit -m "feat: 展示链影盗贼携物收链过程"
```

---

### 任务 3：整体验证与视觉审查

**文件：**
- 检查：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java`
- 检查：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java`
- 检查：本计划列出的全部测试文件

- [ ] **步骤 1：运行主代码编译**

```powershell
.\gradlew.bat :core:compileJava --rerun-tasks --no-problems-report
```

预期：`BUILD SUCCESSFUL`，没有 `StealingChains`、`ItemProvider`、`ItemSprite` 或回调签名错误。

- [ ] **步骤 2：运行全部相关 Gradle 聚焦测试**

```powershell
.\gradlew.bat :core:test --no-problems-report --tests com.shatteredpixel.shatteredpixeldungeon.effects.StealingChainsTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThiefAnimationContractTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThiefTest --tests com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.ChainShadowThiefSpriteAssetTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerChainShadowThiefGenerationTest
```

预期：全部指定测试通过。若 `:core:compileTestJava` 仍被已知无关测试阻断，复用仓库当前 `core/build/test-deps` 中的 JUnit、Hamcrest、GDX、controller 和 JSON jar，使用 `javac` 只编译上述5个测试到 `core/build/chain-steal-test-classes`，再由 `org.junit.runner.JUnitCore` 运行这5个类；预期输出 `OK` 且失败数为0。

- [ ] **步骤 3：游戏内视觉检查**

使用测试工具放置链影盗贼，准备至少一组可偷堆叠物并保持双方相距至少4格，逐项确认：

1. 锁链伸向英雄时不提前显示物品。
2. 接触英雄时物品才从背包整组消失，链尾出现对应图标。
3. 回收期间图标不旋转、不脱离活动链尾，并最终抵达盗贼附近。
4. 回收完成前盗贼不移动；完成后才进入逃跑。
5. 接触前物品失效时锁链空载回收，`chainUsed` 不置位，之后仍能重试。
6. 物品不会同时存在于英雄背包、链尾和盗贼携带字段中。

- [ ] **步骤 4：检查差异和需求边界**

```powershell
git diff --check
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java
```

确认：没有修改通用 `Chains.java`；最小距离仍为 `< 4`；戒指/神器掉落和限量倍率仍在；接触回调只调用一次 `stealItem`；完成回调只调用一次 `next()`；没有新增伤害、拉拽、残废、击退、数量文字或旋转。

- [ ] **步骤 5：仅在验证发现问题时提交必要修正**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThief.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChainsTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/ChainShadowThiefAnimationContractTest.java
git commit -m "fix: 完善链影盗贼收链动画边界"
```

若没有产生修正则跳过提交，不要为了形成提交而改动文件。
