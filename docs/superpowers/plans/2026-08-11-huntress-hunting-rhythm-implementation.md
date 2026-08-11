# 女猎手？狩猎节奏型重构实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将女猎手？Boss 战重构为可读取的“普通狩猎 → 预警/争夺 → 反击窗口”循环，同时以确定性脱离阻止永久贴身逃课。

**架构：** `HuntressBoss` 保存并推进贯风箭、普通战术、贴身警戒与植物狩猎状态；`HuntressBossLevel` 负责合法地形候选、植物标记通知和事务传送；`SpiritBow` 暴露与英雄自然之怒共用的有害植物池和激活入口。所有计数只在 Boss 有效行动时推进，并通过 Bundle 真实往返测试锁定兼容语义。

**技术栈：** Java、Shattered Pixel Dungeon Actor/Mob/Level、JUnit 4、Gradle 8、Bundle、PathFinder、Ballistica。

---

## 文件结构与职责

- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
  - Boss 状态机、伤害/射击入口、普通战术、贴身脱离、自然狩猎、Bundle。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java`
  - 10%植物场地分配、植物目标选择、植物消耗通知、翻越和传送候选。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBow.java`
  - 抽取英雄与 Boss 共享的有害植物池和真实植物激活入口。
- 修改 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java`
  - 将两个直接读取旧公开数组的英雄/护甲分支切换到防御副本入口，避免删除旧数组后编译回归。
- 修改 `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`
  - Boss 纯状态、真实行动、控制优先、Bundle 和自然狩猎测试。
- 修改 `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`
  - 场地比例、植物选择、植物通知、翻越和事务回退测试。
- 创建 `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBowNaturePowerTest.java`
  - 共享有害植物池及索引选择测试。
- 修改 `core/src/main/assets/messages/actors/actors.properties`
- 修改 `core/src/main/assets/messages/actors/actors_zh.properties`
  - Boss 描述、贯风箭收弓、警戒、植物标记、自然狩猎、失去踪迹等文本。

工作区已有大量未提交改动，并且索引中已存在其他任务的 staged 文件。本轮实现子代理不得执行 `git add` 或 `git commit`；每个任务只用限定路径的 `git diff --check -- <paths>` 与 `git diff --stat -- <paths>`形成检查点。最终也不改变既有索引，是否提交由主代理在全部审查通过后单独处理。消息文件包含无关改动时只编辑女猎手键所在窄区块，不得覆盖或回滚其他内容。

---

### 任务 1：共享英雄“自然之怒”的有害植物语义

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBow.java:97-126`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java:3266,3365`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBowNaturePowerTest.java`

- [ ] **步骤 1：编写失败的共享池测试**

创建测试，要求生产代码提供防御副本和稳定索引：

```java
package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SpiritBowNaturePowerTest {
    @Test
    public void harmfulPlantPoolMatchesNaturesWrathAndIsDefensive() {
        Class<? extends Plant>[] expected = new Class[]{
                Blindweed.class, Firebloom.class, Icecap.class,
                Sorrowmoss.class, Stormvine.class
        };
        Class<? extends Plant>[] first = SpiritBow.harmfulPlantPool();
        assertArrayEquals(expected, first);
        first[0] = Stormvine.class;
        assertArrayEquals(expected, SpiritBow.harmfulPlantPool());
    }

    @Test
    public void harmfulPlantSelectionUsesExactIndex() {
        assertEquals(Blindweed.class, SpiritBow.harmfulPlantClass(0));
        assertEquals(Stormvine.class, SpiritBow.harmfulPlantClass(4));
    }
}
```

- [ ] **步骤 2：运行测试验证 RED**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBowNaturePowerTest" --console=plain
```

预期：`:core:compileTestJava FAILED`，缺少 `harmfulPlantPool()` 和 `harmfulPlantClass(int)`。

- [ ] **步骤 3：实现共享池和真实激活入口**

在 `SpiritBow` 中用带泛型的私有数组替换公开原数组，并让英雄原逻辑调用共享入口：

```java
@SuppressWarnings("unchecked")
private static final Class<? extends Plant>[] HARMFUL_PLANTS = new Class[]{
        Blindweed.class, Firebloom.class, Icecap.class,
        Sorrowmoss.class, Stormvine.class
};

public static Class<? extends Plant>[] harmfulPlantPool() {
    return HARMFUL_PLANTS.clone();
}

public static Class<? extends Plant> harmfulPlantClass(int index) {
    if (index < 0 || index >= HARMFUL_PLANTS.length) {
        throw new IllegalArgumentException("invalid harmful plant index: " + index);
    }
    return HARMFUL_PLANTS[index];
}

public static void activateHarmfulPlant(Char defender, int index) {
    Plant plant = Reflection.newInstance(harmfulPlantClass(index));
    plant.pos = defender.pos;
    plant.activate(defender.isAlive() ? defender : null);
}
```

将 `SpiritBow.proc` 中的实例化替换为：

```java
if (Random.Int(12) < ((Hero) attacker).pointsInTalent(Talent.NATURES_WRATH)) {
    activateHarmfulPlant(defender, Random.Int(HARMFUL_PLANTS.length));
}
```

将 `Talent` 中两处 `Random.element(SpiritBow.harmfulPlants)` 改为
`Random.element(SpiritBow.harmfulPlantPool())`；第二处仍保留其对 `Icecap` 的额外判断。完成后全仓搜索 `harmfulPlants` 必须为零，确保旧可变公开数组已彻底移除。

- [ ] **步骤 4：运行共享池与现有女猎手测试**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBowNaturePowerTest" --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBossTest" --console=plain
```

预期：两个测试类全部通过。

- [ ] **步骤 5：任务 1 范围检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBow.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBowNaturePowerTest.java
git diff --stat -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBow.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBowNaturePowerTest.java
```

---

### 任务 2：建立 Boss 状态类型与 Bundle 契约

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java:80-168,319-400`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`

- [ ] **步骤 1：添加状态默认值和真实 Bundle 往返测试**

测试应创建新 Boss 调用 `restoreFromBundle`，不能只调用静态 helper：

```java
@Test
public void huntingRhythmStateRoundTripsAndLegacyDefaultsAreSafe() {
    TestHuntress saved = new TestHuntress();
    saved.setRhythmStateForTest(
            HuntressBoss.CombatStep.MOVE,
            HuntressBoss.GaleState.RECOVERING,
            HuntressBoss.PlantHuntState.SELECTED,
            true, 77, 3, 44, 4);
    Bundle bundle = new Bundle();
    saved.storeInBundle(bundle);

    TestHuntress restored = new TestHuntress();
    restored.restoreFromBundle(bundle);
    assertEquals(HuntressBoss.CombatStep.MOVE, restored.combatStep());
    assertEquals(HuntressBoss.GaleState.RECOVERING, restored.galeState());
    assertEquals(HuntressBoss.PlantHuntState.SELECTED, restored.plantHuntState());
    assertTrue(restored.contactArmed());
    assertEquals(3, restored.escapeCooldown());
    assertEquals(44, restored.markedPlantCell());
    assertEquals(4, restored.plantHuntTurns());

    TestHuntress legacy = new TestHuntress();
    legacy.restoreFromBundle(new Bundle());
    assertEquals(HuntressBoss.CombatStep.SHOOT, legacy.combatStep());
    assertEquals(HuntressBoss.GaleState.HUNTING, legacy.galeState());
    assertEquals(HuntressBoss.PlantHuntState.GRACE, legacy.plantHuntState());
    assertFalse(legacy.contactArmed());
    assertEquals(0, legacy.escapeCooldown());
    assertEquals(-1, legacy.markedPlantCell());
}
```

在现有 `TestHuntress`中补测试专用反射设置器，避免为生产类开放写 API：

```java
private void setRhythmStateForTest(CombatStep step, GaleState gale,
        PlantHuntState plant, boolean armed, int targetId,
        int cooldown, int plantCell, int plantTurns) {
    setPrivateObject("combatStep", step);
    setPrivateObject("galeState", gale);
    setPrivateObject("plantHuntState", plant);
    setPrivateObject("contactArmed", armed);
    setPrivateObject("contactTargetId", targetId);
    setPrivateObject("escapeCooldown", cooldown);
    setPrivateObject("markedPlantCell", plantCell);
    setPrivateObject("plantHuntTurns", plantTurns);
}

private void setPrivateObject(String name, Object value) {
    try {
        Field field = HuntressBoss.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(this, value);
    } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
    }
}
```

- [ ] **步骤 2：运行测试验证 RED**

运行定向 `HuntressBossTest`，预期编译失败，缺少三个枚举、访问器和测试设置入口。

- [ ] **步骤 3：添加枚举、常量、字段和 Bundle 键**

```java
public enum CombatStep { SHOOT, MOVE }
public enum GaleState { HUNTING, READY, AIMED, RECOVERING }
public enum PlantHuntState { GRACE, SELECTED, BOON_ACTIVE, LOST_TRACK, NO_PLANTS }

public static final int GALE_RECOVERY_TURNS = 1;
public static final int ESCAPE_COOLDOWN = 4;
public static final int PLANT_HUNT_GRACE = 3;
public static final int PLANT_HUNT_DURATION = 5;
public static final float NATURE_HUNT_MOVE_MULTIPLIER = 2f;
public static final float NATURE_HUNT_ATTACK_DELAY_MULTIPLIER = 0.75f;
public static final int NATURE_HUNT_PLANT_CHANCE_DENOMINATOR = 4;

private CombatStep combatStep = CombatStep.SHOOT;
private GaleState galeState = GaleState.HUNTING;
private PlantHuntState plantHuntState = PlantHuntState.GRACE;
private int galeAimDx;
private int galeAimDy;
private int galeAimOrigin = -1;
private int galeAimTarget = -1;
private int galeRecoveryTurns;
private boolean contactArmed;
private int contactTargetId = -1;
private int escapeCooldown;
private transient boolean escapeCooldownStartedThisAction;
private int markedPlantCell = -1;
private int plantHuntTurns;
private int plantGraceTurns = PLANT_HUNT_GRACE;
```

为每个字段添加唯一 Bundle 键；`storeInBundle` 写入；`restoreFromBundle` 对枚举使用 `contains` 守卫，对计数夹紧到合法范围，对无效格使用 `-1`。不要增加与 `PlantHuntState.LOST_TRACK` 重复的布尔字段。

补齐测试所用的包级只读访问器：`combatStep()`、`galeState()`、`plantHuntState()`、`contactArmed()`、`escapeCooldown()`、`markedPlantCell()`、`plantHuntTurns()`、`plantGraceTurns()`、`galeAimOrigin()`、`galeAimTarget()`。这些方法只读，所有写入仍通过生产状态机或测试反射完成。

旧档迁移必须继续读取旧 `galeTarget`：仅当恢复后的 `phase == SNIPER`，Boss 位置与旧目标格都在地图内且方向非零时，才以恢复位置为 `galeAimOrigin`、旧目标格为 `galeAimTarget`，重建 `galeAimDx/galeAimDy` 并进入 `AIMED`；越界、零向量或无地图时清目标并安全进入 `READY`。恢复结束后执行阶段归一化：`WARDEN` 必须清除全部 Gale 状态并恢复/校验植物状态；`SNIPER` 必须清除 `SELECTED/BOON_ACTIVE/LOST_TRACK` 等守望者状态。增加狙击手旧档正常迁移、守望者残留 Gale 和异常值回退测试。

- [ ] **步骤 4：运行定向测试验证 GREEN**

运行 `HuntressBossTest`，预期全部通过，且不改变现有行为测试。

阶段转换测试必须断言现有 `enterWardenPhase()`：取消未发射的 `READY/AIMED/RECOVERING` 与所有贯风箭警示，重置 `combatStep=SHOOT`，设置 `plantHuntState=GRACE`、`plantGraceTurns=3`、`markedPlantCell=-1`、`plantHuntTurns=0`，并且仍只生成原规格中的第二只飞鹰，不补充死亡的第一只。

- [ ] **步骤 5：任务 2 范围检查点**

不暂存；只检查 `HuntressBoss.java` 和 `HuntressBossTest.java`：

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java
```

---

### 任务 3：重构贯风箭固定方向与收弓循环

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java:404-447,623-821`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`

- [ ] **步骤 1：编写贯风箭状态机失败测试**

覆盖真实行动入口：

```java
@Test
public void galeUsesFiveHuntsAimFireAndOneRecoveryAction() {
    assertEquals(HuntressBoss.GaleState.HUNTING,
            HuntressBoss.nextGaleState(HuntressBoss.GaleState.HUNTING, 4, true));
    assertEquals(HuntressBoss.GaleState.AIMED,
            HuntressBoss.nextGaleState(HuntressBoss.GaleState.HUNTING, 0, true));
    assertEquals(HuntressBoss.GaleState.READY,
            HuntressBoss.nextGaleState(HuntressBoss.GaleState.HUNTING, 0, false));
    assertEquals(HuntressBoss.GaleState.RECOVERING,
            HuntressBoss.nextGaleState(HuntressBoss.GaleState.AIMED, 0, true));
    assertEquals(HuntressBoss.GaleState.HUNTING,
            HuntressBoss.nextGaleState(HuntressBoss.GaleState.RECOVERING, 0, true));
}

@Test
public void forcedMovementPreservesSavedGaleVector() {
    assertEquals(113, HuntressBoss.galeEndpointForVector(76, 6, 1, 31, 32));
    assertEquals(61, HuntressBoss.galeEndpointForVector(60, 6, 0, 31, 32));
    assertEquals(0, HuntressBoss.galeEndpointForVector(37, -6, -1, 31, 32));
}
```

`nextGaleState`与`galeEndpointForVector`是包级纯函数。另修改既有真实入口测试 `fifthActionableTurnAimsAndNextTurnFires`、`pendingGaleFiresAtLockedCellAfterTargetMoves` 和 `lockedGaleCellSurvivesBundleRoundTripAndFiresNextTurn`，使其断言 `RECOVERING` 与下一行动无攻击；保留控制不推进、无目标保持 `READY`、同阵营飞鹰免疫。

- [ ] **步骤 2：运行 `HuntressBossTest` 验证 RED**

预期失败于状态仍直接以 `galeTarget != -1` 分支发射、缺少 `RECOVERING` 与保存向量。

- [ ] **步骤 3：实现状态推进**

将一阶段分支集中为：

```java
private boolean actSniperRhythm() {
    switch (galeState) {
        case AIMED:
            return fireGale();
        case RECOVERING:
            spend(TICK);
            if (--galeRecoveryTurns <= 0) {
                galeState = GaleState.HUNTING;
                galeTurnsRemaining = GALE_INTERVAL;
            }
            return true;
        case READY:
            return tryAimReadyGale();
        case HUNTING:
        default:
            if (galeTurnsRemaining <= 0) {
                ArrayList<Char> candidates = visibleGaleTargets();
                if (candidates.isEmpty()) {
                    galeState = GaleState.READY;
                    return false;
                }
                return aimGaleAt(Random.element(candidates));
            }
            return false;
    }
}

private boolean tryAimReadyGale() {
    ArrayList<Char> candidates = visibleGaleTargets();
    if (candidates.isEmpty()) {
        return false;
    }
    return aimGaleAt(Random.element(candidates));
}

static GaleState nextGaleState(GaleState state, int remaining, boolean hasTarget) {
    if (state == GaleState.AIMED) return GaleState.RECOVERING;
    if (state == GaleState.RECOVERING) return GaleState.HUNTING;
    if ((state == GaleState.HUNTING || state == GaleState.READY) && remaining == 0) {
        return hasTarget ? GaleState.AIMED : GaleState.READY;
    }
    return state;
}

static int galeEndpointForVector(int origin, int dx, int dy,
        int width, int height) {
    if (width <= 0 || height <= 0 || origin < 0
            || origin >= width * height || dx == 0 && dy == 0) return -1;
    int ox = origin % width;
    int oy = origin / width;
    double scale = 1d;
    if (dx > 0) scale = Math.min(scale, (width - 1 - ox) / (double) dx);
    if (dx < 0) scale = Math.min(scale, ox / (double) -dx);
    if (dy > 0) scale = Math.min(scale, (height - 1 - oy) / (double) dy);
    if (dy < 0) scale = Math.min(scale, oy / (double) -dy);
    int x = (int) Math.round(ox + dx * scale);
    int y = (int) Math.round(oy + dy * scale);
    return x + y * width;
}
```

`actSniperRhythm()` 是普通行动前置检查：`HUNTING` 剩余值大于零时不在这里递减，而是允许本回合普通行动。只有普通攻击、追踪或战术移动真实完成后，统一的 `onEffectiveActionCompleted()` 才将 `galeTurnsRemaining` 减一；因此初值 5 会严格产生五个完整普通行动，第六个有效行动才瞄准。瞄准、发射和收弓本身不再额外递减狩猎计数。

`aimGaleAt` 同时保存 `galeAimOrigin=pos`、警示用 `galeAimTarget=target.pos` 与 `dx/dy`；`fireGale` 用 `galeEndpointForVector` 按二维坐标裁剪到地图边界，禁止线性 cell 相加跨行，完成后进入 `RECOVERING`而不是立即重置为五回合。外部强制移动只改变发射起点，不改变保存向量。增加逐行动序列和四边界测试，严格断言前五次是普通行动、第六次瞄准、第七次发射、第八次仅收弓。

- [ ] **步骤 4：验证状态、真实射线和旧档**

运行 `HuntressBossTest`；预期所有贯风箭测试通过，原同阵营免疫和控制测试保持通过。

- [ ] **步骤 5：任务 3 范围检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java
```

---

### 任务 4：普通射击/移动交替与失明约束

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java:225-247,556-704,1079-1142`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`

- [ ] **步骤 1：编写战术令牌矩阵和真实入口测试**

```java
@Test
public void successfulShotAndMoveAlternateWithoutRandomRoll() {
    assertEquals(HuntressBoss.TacticalAction.SHOOT,
            HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
                    true, false, true, 3, 6, false));
    assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
            HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
                    true, false, true, 3, 6, false));
    assertEquals(HuntressBoss.TacticalAction.SEEK_PLANT,
            HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
                    true, false, true, 6, 6, true));
    assertEquals(HuntressBoss.CombatStep.MOVE,
            HuntressBoss.stepAfterCompletedAction(
                    HuntressBoss.CombatStep.SHOOT, true, false));
    assertEquals(HuntressBoss.CombatStep.SHOOT,
            HuntressBoss.stepAfterCompletedAction(
                    HuntressBoss.CombatStep.MOVE, false, true));
}

@Test
public void blindnessDisablesRelayAndNewAimButNotLockedShot() {
    assertFalse(HuntressBoss.canUseHawkRelay(true, true));
    assertTrue(HuntressBoss.canUseHawkRelay(false, true));
    assertFalse(HuntressBoss.canAcquireGaleTarget(true));
    assertTrue(HuntressBoss.canAcquireGaleTarget(false));
}
```

- [ ] **步骤 2：运行测试验证 RED**

预期旧 `Random.Int(2)` 战术和中继逻辑不满足新断言。

- [ ] **步骤 3：实现确定性令牌**

给 `TacticalAction`增加 `SEEK_PLANT`。删除距离 2–4 的随机 roll，并新增以下包级纯函数；生产 `hasHawkRelay`和`visibleGaleTargets`必须调用相应失明函数：

```java
static TacticalAction tacticalAction(CombatStep step,
        boolean enemySeenDirectly, boolean enemySeenByHawk,
        boolean clearProjectile, int distance, int shotRange,
        boolean natureHunt) {
    boolean normalShotEligible = clearProjectile
            && ((enemySeenDirectly && distance <= shotRange)
            || enemySeenByHawk);
    if (distance <= 1) return TacticalAction.MELEE;
    if (natureHunt && step == CombatStep.MOVE) {
        return TacticalAction.SEEK_PLANT;
    }
    if (!normalShotEligible) return TacticalAction.CHASE;
    if (step == CombatStep.MOVE && distance >= 2 && distance <= 4) {
        return TacticalAction.SEEK_COVER;
    }
    return TacticalAction.SHOOT;
}

static CombatStep stepAfterCompletedAction(CombatStep current,
        boolean completedShot, boolean completedTacticalMove) {
    if (completedShot) return CombatStep.MOVE;
    if (completedTacticalMove) return CombatStep.SHOOT;
    return current;
}

static boolean canUseHawkRelay(boolean bossBlind, boolean hawkAvailable) {
    return !bossBlind && hawkAvailable;
}

static boolean canAcquireGaleTarget(boolean bossBlind) {
    return !bossBlind;
}
```

只在实际普通射击完成后以 `completedShot=true` 切到 `MOVE`；只在真实战术转移成功后以 `completedTacticalMove=true` 切回 `SHOOT`。追击失败、控制让行、收弓、瞄准和翻越均不切换令牌。

`enemySeenByHawk` 只能由“目标位于存活飞鹰半径 4 内、Boss 未失明、飞鹰未失明且 Boss→目标有真实 `PROJECTILE` 弹道”产生；中继目标不再受 Boss 自身 6 格射程限制，从而保留飞鹰带来的微量射程扩展。

飞鹰的自定义 `HUNTING` 增加失明门控：`SEEKING` 与 `WAITING` 状态在 `Blindness` 存在时不得选择新目标，只能消耗普通等待/游荡行动；已经进入 `RETREATING` 的飞鹰可以继续完成撤离。添加真实 Blindness 状态测试，保留 Amok、睡眠和基础控制让行。

- [ ] **步骤 4：运行 Boss 测试验证 GREEN**

同时检查无视护甲、伤害范围、飞鹰中继真实弹道测试未回归。

- [ ] **步骤 5：任务 4 范围检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java
```

---

### 任务 5：确定性猎手翻越与二阶段消逝回退

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java:188-257,710-873`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`

- [ ] **步骤 1：编写贴身节奏与候选优先级失败测试**

Boss 真实行动测试应断言：首次贴身攻击、第二次仍贴身只脱离不攻击、离开后清警戒、玩家/盟友换人不重置、冷却期间近战且归零后脱离。

楼层测试使用固定候选集合：

```java
@Test
public void escapePrefersClearShotAtThreeToFiveThenFarthestFallback() {
    HuntressBossLevel level = newHeadlessLevel();
    fillArena(level, Terrain.EMPTY);
    level.buildFlagMaps();
    Dungeon.level = level;
    HuntressBoss boss = registerActor(new HuntressBoss());
    boss.pos = cell(level, 10, 10);
    Mob target = registerActor(new Mob() {});
    target.pos = cell(level, 11, 10);
    int selected = level.selectHuntressEscapeCell(boss, target);
    assertTrue(level.distance(selected, target.pos) >= 3);
    assertTrue(level.distance(selected, target.pos) <= 5);
    assertEquals(target.pos,
            new Ballistica(selected, target.pos, Ballistica.PROJECTILE).collisionPos);
}
```

再覆盖：入口、出口、门、触发缓冲区和其他保留格排除，植物/heap/Actor/openSpace 排除、无 3–5 格候选时全场最远、双消逝失败回滚后单独翻越。

- [ ] **步骤 2：运行两个定向测试类验证 RED**

预期缺少翻越 API，旧 33%击退和独立 33%消逝草测试需要删除或改写。

- [ ] **步骤 3：实现楼层目的地选择器**

新增：

```java
public int selectHuntressEscapeCell(HuntressBoss boss, Char target) {
    ArrayList<Integer> legal = new ArrayList<>();
    int oldDistance = distance(boss.pos, target.pos);
    for (int cell = 0; cell < length(); cell++) {
        if (!isArenaCell(cell) || isReservedEncounterCell(cell) || !passable[cell]
                || plants.get(cell) != null || heaps.get(cell) != null
                || Actor.findChar(cell) != null
                || Char.hasProp(boss, Char.Property.LARGE) && !openSpace[cell]
                || distance(cell, target.pos) <= oldDistance) {
            continue;
        }
        legal.add(cell);
    }
    if (legal.isEmpty()) return -1;

    ArrayList<Integer> preferredDistance = new ArrayList<>();
    for (int cell : legal) {
        int d = distance(cell, target.pos);
        if (d >= 3 && d <= 5) preferredDistance.add(cell);
    }
    if (preferredDistance.isEmpty()) {
        int farthest = -1;
        ArrayList<Integer> farthestCells = new ArrayList<>();
        for (int cell : legal) {
            int d = distance(cell, target.pos);
            if (d > farthest) {
                farthest = d;
                farthestCells.clear();
            }
            if (d == farthest) farthestCells.add(cell);
        }
        return farthestCells.get(Random.Int(farthestCells.size()));
    }
    ArrayList<Integer> pool = preferredDistance;

    ArrayList<Integer> clearShot = new ArrayList<>();
    for (int cell : pool) {
        if (new Ballistica(cell, target.pos, Ballistica.PROJECTILE).collisionPos
                == target.pos) clearShot.add(cell);
    }
    if (!clearShot.isEmpty()) pool = clearShot;

    int bestCoverDistance = Integer.MAX_VALUE;
    ArrayList<Integer> nearCover = new ArrayList<>();
    for (int cell : pool) {
        int d = distanceToNearestCover(cell);
        if (d < bestCoverDistance) {
            bestCoverDistance = d;
            nearCover.clear();
        }
        if (d == bestCoverDistance) nearCover.add(cell);
    }
    if (!nearCover.isEmpty()) pool = nearCover;

    int farthest = -1;
    ArrayList<Integer> farthestCells = new ArrayList<>();
    for (int cell : pool) {
        int d = distance(cell, target.pos);
        if (d > farthest) {
            farthest = d;
            farthestCells.clear();
        }
        if (d == farthest) farthestCells.add(cell);
    }
    return farthestCells.get(Random.Int(farthestCells.size()));
}

private int distanceToNearestCover(int cell) {
    int best = Integer.MAX_VALUE;
    for (Set<Integer> cluster : coverClusters) {
        for (int cover : cluster) best = Math.min(best, distance(cell, cover));
    }
    return best;
}

public boolean moveHuntressToEscapeCell(HuntressBoss boss, Char target) {
    int destination = selectHuntressEscapeCell(boss, target);
    if (destination == -1) return false;
    ScrollOfTeleportation.appear(boss, destination);
    return boss.pos == destination;
}
```

先构造全部合法格。存在 3–5 格候选时，才按真实弹道、靠近掩体、最大距离分层筛选；不存在 3–5 格候选时，必须直接从全部合法格选择与目标距离最远者，不能再被弹道或掩体筛掉。最终同分用 `Random.Int(size)`。禁止先移动后选择。

- [ ] **步骤 4：实现 Boss 警戒与回退链**

`tryCloseQuartersEscape`：

1. 校验活动敌对贴身目标；
2. 首次只设警戒，让普通近战继续；
3. 冷却为零且警戒已建立时，二阶段先尝试 `teleportBossAndTargetApart`；
4. 双传送失败时调用单独翻越；
5. 成功才消耗完整行动、设置 4 冷却和清警戒；
6. 删除 `attackProc` 中随机击退和旧 `tryFadeleafEscape` 概率入口。

在本任务同时建立统一行动入口，后续植物状态机只扩展该入口，不另起平行计时：

```java
// act() 在调用 super.act() 前依次执行，任一返回 true 即结束本回合：
// 1 基础控制/逃跑/混乱让行；2 开场延迟；3 AIMED 发射；
// 4 RECOVERING 收弓；5 LOST_TRACK；6 已满足的贴身脱离；
// 7 当前阶段状态推进；8 super.act 普通行动。
private void onEffectiveActionCompleted(boolean ordinaryAction) {
    if (escapeCooldownStartedThisAction) {
        escapeCooldownStartedThisAction = false;
    } else if (escapeCooldown > 0) {
        escapeCooldown--;
    }
    if (ordinaryAction && phase == Phase.SNIPER
            && galeState == GaleState.HUNTING
            && galeTurnsRemaining > 0) {
        galeTurnsRemaining--;
    }
}
```

控制、逃跑、混乱和两类睡眠分支不得调用 `onEffectiveActionCompleted`。每个完整行动只调用一次；任意脱离成功时同时设 `escapeCooldown=ESCAPE_COOLDOWN` 与 `escapeCooldownStartedThisAction=true`，统一完成入口先清标志而不递减，保证该同一行动结束仍显示 4。增加移动踩 Fadeleaf 后仍为 4、随后 4→3→2→1→0 的逐行动测试，证明冷却不会永久停留，也不会少一回合。该瞬时标志不写 Bundle。

将植物来源的 `triggerFadeleafBoon()` 与其 `teleportBossAndHero()` 调用链改为返回 `boolean`；先选择双方目的地并保存双方原位置，再执行双传送。仅 Boss/玩家都到达预期落点才返回 true；第二步失败、落点不符或异常时必须事务性回滚双方原位置后返回 false。它不消耗三次贴身消逝额度，但成功后同样设置统一冷却与行动级新冷却标志；失败不进入冷却。二阶段贴身双传送成功才消耗一次额度，失败后同一行动继续尝试 Boss 单独翻越。增加“Boss 已移动、玩家第二步失败”真实回滚测试。

- [ ] **步骤 5：运行两个测试类验证 GREEN**

预期贴身、事务回滚和既有 Fadeleaf 目的地测试全部通过。

- [ ] **步骤 6：任务 5 范围检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java
```

---

### 任务 6：二阶段 10%植物和连通植被区

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java:155-166,398-435,663-707`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`

- [ ] **步骤 1：把比例与互斥测试改为 50/10/1**

```java
@Test
public void wardenArenaUsesFiftyTenOneWithoutOverlap() {
    int legal = 400;
    assertArrayEquals(new int[]{200, 4, 40},
            HuntressBossLevel.phaseTwoCounts(legal));
    ArrayList<Integer> cells = new ArrayList<>();
    for (int i = 0; i < legal; i++) cells.add(i);
    HuntressBossLevel.WardenArenaAllocation a =
            HuntressBossLevel.allocateWardenArena(
                    cells, 20, Collections.<Integer>emptySet(),
                    Collections.<Integer>emptySet(),
                    HuntressBossLevel.phaseTwoCounts(legal));
    assertPairwiseDisjoint(a);
}
```

在测试文件显式导入 `java.util.Collections`。新增真实二阶段分配测试：`level.create()` 只验证初始地图；随后通过包级无场景副作用 seam `populateWardenArenaForTest()` 调用生产 `populateWardenArena()` 的同一分配/落图主体，检查掩体、入口、出口、关键路径、角色和 heap 未被覆盖，并验证新增植被格中大多数至少有一个正交植被邻居。不要调用会生成飞鹰或播放音乐的完整 `onWardenPhase()`。

- [ ] **步骤 2：运行 Level 测试验证 RED**

预期 `phaseTwoCounts(400)` 仍返回 20 株植物。

- [ ] **步骤 3：实现 10%与聚簇候选顺序**

将植物比例改为 `0.10f`。增加确定性聚簇方法：

```java
static ArrayList<Integer> clusteredOrder(
        List<Integer> legal, int width, int requested, int clusterCount) {
    LinkedHashSet<Integer> remaining = new LinkedHashSet<>(legal);
    ArrayList<Integer> result = new ArrayList<>();
    ArrayDeque<Integer> frontier = new ArrayDeque<>();
    int seedsLeft = Math.max(1, clusterCount);
    while (result.size() < requested && !remaining.isEmpty()) {
        if (frontier.isEmpty()) {
            ArrayList<Integer> choices = new ArrayList<>(remaining);
            int seed = choices.get(Random.Int(choices.size()));
            frontier.add(seed);
            seedsLeft--;
        }
        int cell = frontier.removeFirst();
        if (!remaining.remove(cell)) continue;
        result.add(cell);
        int[] neighbours = {cell - 1, cell + 1, cell - width, cell + width};
        Random.shuffle(neighbours);
        for (int next : neighbours) {
            if (remaining.contains(next)) frontier.addLast(next);
        }
        if (seedsLeft > 0 && result.size() * clusterCount >= requested * (clusterCount - seedsLeft)) {
            frontier.clear();
        }
    }
    return result;
}
```

新增明确常量：

```java
private static final long WARDEN_VEGETATION_SEED_SALT = 0x6A09E667F3BCC909L;
static final int WARDEN_VEGETATION_CLUSTER_COUNT = 12;
```

将纯函数签名改为 `allocateWardenArena(List<Integer> legalCells, int mapWidth, Set<Integer> existingFurrows, Set<Integer> blockedCells, int[] counts)`，生产调用传 `width()`，测试传夹具宽度；静态函数内部不得直接调用实例 `width()`。使用独立 `Random.pushGenerator(Dungeon.seedForDepth(15, 0) + WARDEN_VEGETATION_SEED_SALT)` 并在 `finally` 中 `popGenerator()`；植物、枯草、触手从互斥候选中分配，保留现有安全格验证。

`allocateWardenArena()` 只生成一次 `clusteredOrder(legalCandidates, mapWidth, requestedTotal, WARDEN_VEGETATION_CLUSTER_COUNT)`，其中 `requestedTotal = tentacleCount + plantCount + furrowCountToAdd`；随后严格按触手、植物、枯草顺序从同一迭代器取格，确保互斥。固定 seed 测试断言结果可复现、不同 seed 至少有一种布局不同、外层 `Random` 序列未被消耗，并断言至少 70%的新增植被格存在正交植被邻居。

生产构造 `blockedCells` 时除角色、已有植物、掩体和保留格外，必须显式加入所有 `heaps` 键。测试分别放置普通 heap 与开场两枚致盲飞镖 heap，断言枯草、植物和触手均不覆盖这些格。

把现有 `populateWardenArena()` 拆为不访问场景的 `applyWardenArenaFeatures()`（只计算 allocation、改 map/plants 并返回需要注册的触手列表）与外层场景提交步骤（`GameScene.add/updateMap`、`Dungeon.observe`）。包级 `populateWardenArenaForTest()` 仅调用纯落图主体，不生成飞鹰、不播音乐、不触发场景观察。

- [ ] **步骤 4：运行 Level 全类验证 GREEN**

预期比例、互斥、关键路径、随机隔离和既有开场测试全部通过。

- [ ] **步骤 5：任务 6 范围检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java
```

---

### 任务 7：植物标记、自然狩猎和特定植物增益

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java:541-555,640-707`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`

- [ ] **步骤 1：编写植物选择与自然狩猎 RED 测试**

覆盖：标记行动不扣五回合、目标距离优先 4–8、无优选时回退任意 >2 可达植物、移动 2 倍、远程耗时 0.75、25%边界、近战/飞鹰不触发、任意植物取得结束狩猎。

```java
@Test
public void natureHuntAppliesExactMovementAttackAndPlantProcRules() {
    assertEquals(2f, HuntressBoss.natureHuntSpeed(1f, true), 0f);
    assertEquals(1f, HuntressBoss.natureHuntSpeed(1f, false), 0f);
    assertEquals(0.75f,
            HuntressBoss.natureHuntAttackDelay(1f, true, false, false), 0f);
    assertEquals(1f,
            HuntressBoss.natureHuntAttackDelay(1f, true, true, false), 0f);
    assertTrue(HuntressBoss.shouldProcNatureWrath(false, false, 0));
    assertFalse(HuntressBoss.shouldProcNatureWrath(true, false, 0));
    assertFalse(HuntressBoss.shouldProcNatureWrath(false, false, 1));
}
```

真实箭测试覆写 `activateNatureWrathPlant(Char,int)`记录调用，验证四次固定 roll 中只有 roll 0 调用 `SpiritBow.activateHarmfulPlant`语义入口。

- [ ] **步骤 2：运行两个定向测试类验证 RED**

预期缺少植物选择和自然狩猎 API。

- [ ] **步骤 3：实现 Level 植物选择与消耗通知**

新增：

```java
public int selectMarkedPlant(HuntressBoss boss) {
    PathFinder.buildDistanceMap(boss.pos, passable);
    ArrayList<Integer> preferred = new ArrayList<>();
    ArrayList<Integer> fallback = new ArrayList<>();
    for (int cell : plants.keyArray()) {
        int path = PathFinder.distance[cell];
        if (path == Integer.MAX_VALUE || path <= 2 || Actor.findChar(cell) != null) {
            continue;
        }
        fallback.add(cell);
        if (path >= 4 && path <= 8) preferred.add(cell);
    }
    ArrayList<Integer> pool = preferred.isEmpty() ? fallback : preferred;
    return pool.isEmpty() ? -1 : pool.get(Random.Int(pool.size()));
}
public boolean hasPlantAt(int cell) { return plants.get(cell) != null; }

public boolean hasReachablePlant(HuntressBoss boss) {
    PathFinder.buildDistanceMap(boss.pos, passable);
    for (int cell : plants.keyArray()) {
        if (PathFinder.distance[cell] != Integer.MAX_VALUE
                && PathFinder.distance[cell] > 2
                && Actor.findChar(cell) == null) return true;
    }
    return false;
}

public boolean isMarkedPlantReachable(HuntressBoss boss, int cell) {
    if (cell < 0 || cell >= length() || !hasPlantAt(cell)
            || Actor.findChar(cell) != null) return false;
    PathFinder.buildDistanceMap(boss.pos, passable);
    return PathFinder.distance[cell] != Integer.MAX_VALUE;
}
```

覆写 `uproot(int pos)`：在 `super.uproot(pos)` 前记录植物是否存在，移除后通知活动女猎手的公开入口 `public void onPlantRemoved(int pos)`。`occupyCell` 对 Boss 在 uproot 前先调用且只调用一次 `public void onPlantClaimed(int pos, WardenBoon boon)`，以区分取得与失去；Boss 取得分支必须抑制同一次 `uproot` 的 `onPlantRemoved`，防止先取得又被判为丢失。

- [ ] **步骤 4：实现 Boss 植物状态机**

```java
private boolean actWardenRhythm() {
    switch (plantHuntState) {
        case LOST_TRACK:
            plantHuntState = PlantHuntState.GRACE;
            plantGraceTurns = PLANT_HUNT_GRACE;
            spend(TICK);
            return true;
        case GRACE:
            if (plantGraceTurns <= 0) return markNextPlant();
            return false;
        case SELECTED:
            if (!levelHasMarkedPlant() || plantHuntTurns <= 0) {
                loseMarkedPlant();
                return actWardenRhythm();
            }
            return false;
        case BOON_ACTIVE:
            advanceBoon();
            if (activeBoon == null) beginPlantGrace();
            return false;
        case NO_PLANTS:
            if (hasReachablePlant()) beginPlantGrace();
            return false;
        default:
            return false;
    }
}
```

同时实现这些直接被状态机调用的窄接口，避免状态含义分散到 `act()`：

```java
private HuntressBossLevel huntressLevel() {
    return Dungeon.level instanceof HuntressBossLevel
            ? (HuntressBossLevel) Dungeon.level : null;
}

private boolean levelHasMarkedPlant() {
    HuntressBossLevel level = huntressLevel();
    return level != null && markedPlantCell >= 0
            && level.isMarkedPlantReachable(this, markedPlantCell);
}

private boolean hasReachablePlant() {
    HuntressBossLevel level = huntressLevel();
    return level != null && level.hasReachablePlant(this);
}

private boolean markNextPlant() {
    if (buff(Blindness.class) != null) return false;
    HuntressBossLevel level = huntressLevel();
    markedPlantCell = level == null ? -1 : level.selectMarkedPlant(this);
    if (markedPlantCell < 0) {
        plantHuntState = PlantHuntState.NO_PLANTS;
        return false;
    }
    plantHuntState = PlantHuntState.SELECTED;
    plantHuntTurns = PLANT_HUNT_DURATION;
    announcePlantMarked(markedPlantCell);
    spend(TICK);
    return true;
}

private void loseMarkedPlant() {
    markedPlantCell = -1;
    plantHuntTurns = 0;
    plantHuntState = PlantHuntState.LOST_TRACK;
    announceLostTrack();
}

private void beginPlantGrace() {
    markedPlantCell = -1;
    plantHuntTurns = 0;
    plantGraceTurns = PLANT_HUNT_GRACE;
    plantHuntState = PlantHuntState.GRACE;
}
```

失明时 `GRACE` 可以降到 0，但 `markNextPlant()` 不选择植物、不消耗标记行动；Boss 继续普通行动，失明解除后的下一有效行动再尝试标记。已经处于 `SELECTED` 的目标不会因失明取消。

`GRACE` 与 `SELECTED` 的计数都在普通有效行动真实完成后由统一入口递减：三个完整普通行动后 `plantGraceTurns` 才变为 0，下一有效行动执行标记；五个完整自然狩猎行动后 `plantHuntTurns` 才变为 0，下一有效行动进入丢失/缓冲处理。标记行动本身不递减五回合。增加逐行动序列测试锁定 3+标记和 5+超时边界。

在任务 5 建立的 `onEffectiveActionCompleted` 中追加：仅当本回合走到普通行动层时，`WARDEN/GRACE` 的正数 `plantGraceTurns` 减一，`WARDEN/SELECTED` 的正数 `plantHuntTurns` 减一；`LOST_TRACK`、植物标记、翻越、贯风箭、阶段切换和控制让行都不能顺带递减这些计数。

`onPlantRemoved(cell)` 仅在 `SELECTED && cell == markedPlantCell` 时调用 `loseMarkedPlant()`；`onPlantClaimed(cell, boon)` 先清除标记和自然狩猎，再调用一次 `grantBoon(boon)`。若调用后 `activeBoon != null`，进入 `BOON_ACTIVE`；`FADELEAF` 等即时效果使 `activeBoon == null` 时直接 `beginPlantGrace()`。因此随机自然之力箭与特定植物增益箭不会重叠。`announcePlantMarked`和`announceLostTrack`只负责表现，不改变状态。

自然狩猎必须接入真实移动，而不是只返回枚举。在现有 `getCloser(int target)` 中，`SEEK_PLANT` 且 `levelHasMarkedPlant()` 时调用 `super.getCloser(markedPlantCell)`；仅当该调用返回 true 时视为战术移动成功并把令牌切回 `SHOOT`，失败则保留令牌。真实路径测试设置一条可达植物路径，断言一次行动后 Boss 到标记植物的 PathFinder 距离下降，并验证 `speed()` 为基础值 2 倍。

标记植物本身消耗完整行动但将 `plantHuntTurns`设为 5，不立即递减。新增包级纯函数：

```java
static float natureHuntSpeed(float base, boolean active) {
    return active ? base * NATURE_HUNT_MOVE_MULTIPLIER : base;
}

static float natureHuntAttackDelay(float base, boolean active,
        boolean melee, boolean gale) {
    return active && !melee && !gale
            ? base * NATURE_HUNT_ATTACK_DELAY_MULTIPLIER : base;
}

static boolean shouldProcNatureWrath(boolean melee, boolean gale, int roll) {
    return !melee && !gale && roll == 0;
}
```

生产 `speed()`仅在 `SELECTED`乘 2；`attackDelay()`仅在 `SELECTED && !meleeAttack && !galeShot`乘 0.75。

普通箭命中时仅在 `SELECTED`调用 `Random.Int(4)`；成功时通过 protected seam 调用 `SpiritBow.activateHarmfulPlant(enemy, Random.Int(pool.length))`。

删除旧常态 33%手写 Buff 池。取得植物后的特定增益箭仅允许 `phase == WARDEN && !meleeAttack && !galeShot && activeBoon != null`，保留并补足盲草失明与风暴藤 `Vertigo`；增加真实普通远程命中测试，并断言近战、贯风箭、飞鹰与触手都不会触发。

明确删除现有无条件随机增益链：`WARDEN_BOON_INTERVAL`、`boonCooldown`、对应 Bundle 键/保存恢复、`act()` 中每 10 回合的 `Random.element(WardenBoon.values())` 与旧测试全部移除。植物增益只能由 Boss 实际踩到植物取得。

植物 `FADELEAF` 的即时传送保留为植物奖励，不消耗贴身消逝草 3 次额度；成功后设置统一 4 回合 `escapeCooldown`，失败不设置。它结束 `SELECTED` 后进入 `GRACE`，不伪造 `BOON_ACTIVE`。

删除或改写所有依赖旧机制的测试/API：`shouldAttemptFadeleafEscape`、`shouldApplyHarmfulPlantEffect`、`HuntressBoss.harmfulPlantPool`、`WARDEN_BOON_INTERVAL`、`shouldKnockBackCloseTarget`；全仓搜索这些符号必须无遗留生产引用。

- [ ] **步骤 5：验证真实抢植物窗口**

测试玩家/盟友/环境 uproot 均使 Boss 下一有效行动只执行 `LOST_TRACK`；Boss 取得目标或非目标植物均结束 `SELECTED`，持续型增益进入 `BOON_ACTIVE`，Fadeleaf 等即时效果成功或失败后都进入 `GRACE`；控制状态暂停五回合。

- [ ] **步骤 6：运行 SpiritBow、Boss、Level 三类测试**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBowNaturePowerTest" --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBossTest" --tests "com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevelTest" --console=plain
```

预期全部通过。

- [ ] **步骤 7：任务 7 范围检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java
```

---

### 任务 8：消息、说明与可见状态

**文件：**
- 修改：`core/src/main/assets/messages/actors/actors.properties:2262-2278`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties:3689-3705`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`

- [ ] **步骤 1：编写资源与真实提示 seam 测试**

不要为了测试增加 `rhythmMessageKeys()` 生产 API。测试直接加载英中文 `actors.properties`，断言 `actors.mobs.huntressboss.gale_recover`、`close_warning`、`plant_marked`、`nature_hunt`、`lost_track` 均存在且非空；再由 `TestHuntress` 覆写 `announceGaleRecovery()`、`announceCloseWarning()`、`announcePlantMarked(int)`、`announceNatureHunt()`、`announceLostTrack()`，通过真实状态转换断言每次只触发一次。

- [ ] **步骤 2：运行测试验证 RED**

预期资源键缺失，且状态转换尚未调用完整提示 seam。

- [ ] **步骤 3：补齐英文和中文文本**

英文键：

```properties
actors.mobs.huntressboss.gale_recover=The bowstring falls silent for a moment.
actors.mobs.huntressboss.close_warning=She shifts her weight, ready to vault away.
actors.mobs.huntressboss.plant_marked=That growth will answer my call.
actors.mobs.huntressboss.nature_hunt=The whole arena carries my trail.
actors.mobs.huntressboss.lost_track=The trail is broken.
```

中文键：

```properties
actors.mobs.huntressboss.gale_recover=弓弦暂时沉寂了下来。
actors.mobs.huntressboss.close_warning=她调整重心，准备翻越脱身。
actors.mobs.huntressboss.plant_marked=那株植物会回应我的召唤。
actors.mobs.huntressboss.nature_hunt=整座猎场都在为我指路。
actors.mobs.huntressboss.lost_track=踪迹断了。
```

更新 `desc`，删除“三次普通射击”和常态随机植物箭旧描述，写明五回合贯风箭、收弓、植物争夺、自然狩猎和确定性脱离。

- [ ] **步骤 4：添加最小可见状态**

复用现有 `yell`、`TargetedCell`、叶片粒子和 Buff 图标；不要新增贴图。标记、收弓、警戒、自然狩猎和失去踪迹只在状态真正变化时提示一次，存档恢复不重复喊话。

- [ ] **步骤 5：运行 Boss 与 Bestiary 测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBossTest" --tests "com.shatteredpixel.shatteredpixeldungeon.journal.HuntressBossBestiaryTest" --console=plain
```

- [ ] **步骤 6：任务 8 范围检查点**

不暂存；仅检查属于女猎手的消息 hunks，不得覆盖消息文件中其他用户改动。

```powershell
git diff --check -- core/src/main/assets/messages/actors/actors.properties core/src/main/assets/messages/actors/actors_zh.properties core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java
```

---

### 任务 9：全链路回归、兼容与交付审查

**文件：**
- 测试：前述所有目标测试文件
- 检查：全部本轮生产和消息文件

- [ ] **步骤 1：补真实 Bundle 全状态往返**

创建运行中的 `AIMED`、`RECOVERING`、贴身警戒、`SELECTED`自然狩猎、`BOON_ACTIVE`和 `LOST_TRACK` Boss；分别保存到 Bundle，创建新 Boss 恢复并执行下一有效行动，断言不是只恢复字段，而是恢复后的真实行为正确。

- [ ] **步骤 2：补控制优先与异常值测试**

逐项覆盖麻痹、冰冻、普通睡眠、魔法睡眠、逃跑、混乱：特殊计数不推进、锁定贯风箭不丢失、植物标记不超时、翻越冷却不减少。Bundle 中负数、超额回合、无效枚举、陈旧 Actor ID、无效植物格和越界向量必须恢复到安全默认。

- [ ] **步骤 3：运行全部相关 Core 测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBowNaturePowerTest" --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBossTest" --tests "com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevelTest" --tests "com.shatteredpixel.shatteredpixeldungeon.journal.HuntressBossBestiaryTest" --tests "com.shatteredpixel.shatteredpixeldungeon.HuntressBossSelectionTest" --console=plain
```

预期：所有目标测试 `failures=0, errors=0`。

- [ ] **步骤 4：运行 Android 编译**

```powershell
.\gradlew.bat --no-daemon --no-problems-report :android:compileDebugJavaWithJavac --console=plain
```

预期：`BUILD SUCCESSFUL`。

- [ ] **步骤 5：检查差异范围和空白**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBow.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBowNaturePowerTest.java core/src/main/assets/messages/actors/actors.properties core/src/main/assets/messages/actors/actors_zh.properties
git status --short
```

预期：无空白错误；仅目标文件包含本轮变更，其他脏文件保持不动。

- [ ] **步骤 6：执行双重审查**

规格审查逐条核对设计文档 1–19 节；质量审查重点核对：

- 状态优先级和控制让行；
- 计时是否按有效行动而非耗时重复推进；
- 翻越和双传送事务；
- `uproot`通知是否区分 Boss 取得与目标丢失；
- 自然狩猎与特定植物增益是否互斥；
- Bundle 与 Actor/Random/PathFinder 测试隔离；
- 旧随机击退、50/50战术、常态33%植物箭、独立随机消逝草是否完全移除。

- [ ] **步骤 7：处理审查问题并重跑步骤 3–5**

仅修复 P0–P2 或明确违反规格的问题；每个修复先加失败测试，再做最小实现。

- [ ] **步骤 8：最终工作树交付检查**

不得改变当前索引中其他任务的 staged 状态；只报告本轮限定文件的工作树差异：

```powershell
git diff --cached --name-only
git status --short
```

默认不创建实现提交；若用户之后明确要求提交，再由主代理使用限定路径或交互式 hunk 处理，绝不把现有 Pestilence 或其他模块的 staged 改动混入。
