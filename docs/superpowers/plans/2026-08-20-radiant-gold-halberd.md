# 耀金战戟实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `executing-plans` 在当前共享工作区内逐任务实现；每项生产改动遵循 `test-driven-development`，完成后使用 `requesting-code-review` 与 `verification-before-completion`。由于 `Weapon.java`、`MeleeWeapon.java`、`Hero.java`、`Generator.java`、`MissileSprite.java`、`EXItemSpriteSheet.java`、文本和素材表都可能包含其他未提交改动，执行时必须逐段保护既有差异。

**目标：** 实装六阶近战武器“耀金战戟”，包括特殊重型面板、伏击增幅的两组独立命中效果、可命中视野外敌人的方向贯穿武技、逐目标击退与撞墙增伤、不旋转投影、生成器、文本、图鉴和索引 156 的硬边贴图。

**架构：** `RadiantGoldHalberd` 单类保存所有专属公式、被动、射线路径、伏击快照、武技伤害模式和异步击退队列。`Weapon` 提供统一且默认兼容的负重查询，供精准、延迟、武技门槛和英雄武器减伤共同使用；`MissileSprite` 只提取已有角速度表的查询方法并登记耀金战戟为 0。武技通过 `Ballistica.STOP_SOLID`、`MissileSprite` 和 `WandOfBlastWave.throwCharImmediately` 组合，不修改这些底层机制。

**技术栈：** Java、JUnit 4、Gradle、Shattered Pixel Dungeon 战斗/Buff/CellSelector/Ballistica/Pushing API、PNG 16×16 像素表、Pillow（只用于项目外的确定性贴图清理和目标单元格写入）。

**设计规格：** `docs/superpowers/specs/2026-08-20-radiant-gold-halberd-design.md`

---

## 文件结构

### 创建

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java` —— 武器面板、被动、方向射线、武技伤害模式、击退队列与结束出口。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java` —— 面板、概率、Buff、路径、武技和集成回归。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/WeaponEncumbranceTest.java` —— 通用负重查询和虚假之力差异测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/MissileSpriteTest.java` —— 角速度 0 与默认角速度回归。

### 修改

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/Weapon.java` —— 统一有效负重计算，并让精准与延迟调用它。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java` —— 武技力量门槛调用统一有效负重。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java` —— 武器负重减伤调用统一有效负重，护甲逻辑不变。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/MissileSprite.java` —— 提取角速度查询并登记耀金战戟。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java` —— 定义索引 156 的常量。
- `core/src/main/assets/sprites/ex_items.png` —— 仅写入索引 156。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java` —— 新常量和像素约束；保留 155，空白范围收窄为 157~159。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java` —— 追加 `WEP_T6` 类和权重。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponIntegrationTest.java` —— 生成器与测试工具数组回归。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java` —— 图鉴登记。
- `core/src/main/assets/messages/items/items.properties` —— 英文正式文本。
- `core/src/main/assets/messages/items/items_zh.properties` —— 中文正式文本。
- `core/src/main/assets/messages/custom/custom.properties` —— 英文详细资料。
- `core/src/main/assets/messages/custom/custom_zh.properties` —— 中文详细资料。

### 项目外中间产物

- `D:\STUDY\Dungeon\cache\radiant_gold_halberd\clean_radiant_gold_halberd.py`
- `D:\STUDY\Dungeon\cache\radiant_gold_halberd\source_reference.png`
- `D:\STUDY\Dungeon\cache\radiant_gold_halberd\radiant_gold_halberd_16x16.png`
- `D:\STUDY\Dungeon\cache\radiant_gold_halberd\radiant_gold_halberd_preview_16x.png`

---

## 任务 1：保护共享工作区并建立面板红灯

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java:54-66`

- [ ] **步骤 1：记录共享文件基线**

运行：

```powershell
git status --short
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/Weapon.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/MissileSprite.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/main/assets/sprites/ex_items.png
```

保存输出用于每个任务后对照。不得恢复、覆盖、格式化或暂存任何既有差异；修改共享文件前重新读取目标片段。

- [ ] **步骤 2：编写面板失败测试**

创建测试，直接锁定已批准的五个关键等级：

```java
@Test
public void heavyTierSixProfileMatchesAllBreakpoints() {
    TestableRadiantGoldHalberd weapon = new TestableRadiantGoldHalberd();

    assertEquals(6, RadiantGoldHalberd.TIER);
    assertEquals(1f, RadiantGoldHalberd.ACCURACY, 0f);
    assertEquals(2f, RadiantGoldHalberd.DELAY, 0f);
    assertEquals(3, RadiantGoldHalberd.RANGE);
    assertEquals(6, weapon.min(0));
    assertEquals(48, weapon.max(0));
    assertEquals(10, weapon.min(3));
    assertEquals(77, weapon.max(3));
    assertEquals(18, weapon.min(9));
    assertEquals(135, weapon.max(9));
    assertEquals(23, weapon.min(12));
    assertEquals(166, weapon.max(12));
    assertEquals(30, weapon.min(15));
    assertEquals(201, weapon.max(15));
    assertEquals(22, weapon.STRReq(0));
    assertEquals(22, weapon.STRReq(8));
    assertEquals(21, weapon.STRReq(9));
    assertEquals(21, weapon.STRReq(14));
    assertEquals(20, weapon.STRReq(15));
    assertEquals(EXItemSpriteSheet.RADIANT_GOLD_HALBERD, weapon.image);
    assertEquals(1f, weapon.actualAccuracy(), 0f);
    assertEquals(2f, weapon.actualDelay(), 0f);
    assertEquals(3, weapon.actualRange());
}

@Test
public void negativeLevelsClampToZeroAndMasteryStillReducesTwoStrength() {
    TestableRadiantGoldHalberd weapon = new TestableRadiantGoldHalberd();
    assertEquals(6, weapon.min(-5));
    assertEquals(48, weapon.max(-5));
    assertEquals(22, weapon.STRReq(-5));
    weapon.masteryPotionBonus = true;
    assertEquals(20, weapon.STRReq(0));
    assertEquals(19, weapon.STRReq(9));
    assertEquals(18, weapon.STRReq(15));
}
```

测试子类只暴露 `ACC`、`DLY`、`RCH`，不复制生产公式。

- [ ] **步骤 3：运行测试确认类和常量缺失**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest"
```

预期：FAIL，原因是 `RadiantGoldHalberd` 或 `RADIANT_GOLD_HALBERD` 尚不存在。

- [ ] **步骤 4：添加贴图常量和最小武器骨架**

在六阶武器常量区加入：

```java
// Index 155 is reserved for VenomousSickle.
public static final int RADIANT_GOLD_HALBERD = encode(156, 16, 16);
```

创建武器类的面板部分：

```java
public class RadiantGoldHalberd extends MeleeWeapon {

    public static final int TIER = 6;
    public static final float ACCURACY = 1f;
    public static final float DELAY = 2f;
    public static final int RANGE = 3;

    {
        image = EXItemSpriteSheet.RADIANT_GOLD_HALBERD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 0.8f;
        tier = TIER;
        ACC = ACCURACY;
        DLY = DELAY;
        RCH = RANGE;
    }

    static int effectiveLevel(int level) {
        return Math.max(0, level);
    }

    public static int minForLevel(int level) {
        int l = effectiveLevel(level);
        return 6 + l + l / 3 + Math.max(0, l - 11);
    }

    public static int maxForLevel(int level) {
        int l = effectiveLevel(level);
        return 48 + 9 * l + 2 * (l / 3) + 2 * Math.max(0, l - 11);
    }

    public static int strengthRequirementForLevel(int level) {
        int l = effectiveLevel(level);
        return 22 - (l >= 9 ? 1 : 0) - (l >= 15 ? 1 : 0);
    }

    @Override
    public int min(int level) {
        return minForLevel(level);
    }

    @Override
    public int max(int level) {
        return maxForLevel(level);
    }

    @Override
    public int STRReq(int level) {
        int requirement = strengthRequirementForLevel(level);
        return masteryPotionBonus ? requirement - 2 : requirement;
    }
}
```

- [ ] **步骤 5：运行面板测试并检查差异**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest"
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java
```

预期：PASS，无空白错误；索引 155 的现有内容不被修改。

- [ ] **步骤 6：提交面板骨架**

只暂存本任务新增文件和 `EXItemSpriteSheet` 的单个新增常量。建议提交信息：`feat: 添加耀金战戟基础面板`。

---

## 任务 2：统一负重查询并实现虚假之力减半

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/WeaponEncumbranceTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/Weapon.java:416-459`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java:128-153`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java:1149-1156`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java`

- [ ] **步骤 1：编写负重纯函数失败测试**

```java
@Test
public void reductionsClampEffectiveEncumbranceAtZero() {
    assertEquals(3, Weapon.adjustedEncumbrance(22, 19, 0));
    assertEquals(1, Weapon.adjustedEncumbrance(22, 19, 2));
    assertEquals(0, Weapon.adjustedEncumbrance(22, 19, 99));
    assertEquals(0, Weapon.adjustedEncumbrance(19, 22, 0));
}

@Test
public void falsehoodNetBenefitIsTwoNormallyAndOneForTheHalberd() {
    int noTalent = Weapon.adjustedEncumbrance(22, 19, 0);
    int ordinaryTalent = Weapon.adjustedEncumbrance(22, 18, 3);
    int halberdTalent = Weapon.adjustedEncumbrance(22, 18, 2);
    assertEquals(2, noTalent - ordinaryTalent);
    assertEquals(1, noTalent - halberdTalent);
    assertEquals(2, RadiantGoldHalberd.falsehoodPowerReduction(true));
    assertEquals(0, RadiantGoldHalberd.falsehoodPowerReduction(false));
}
```

再加入源码级回归，要求 `accuracyFactor`、`baseDelay`、`MeleeWeapon.execute` 和 `Hero.drRoll` 的武器分支都出现 `effectiveEncumbrance`，且旧的四处重复武器算法不再存在。

- [ ] **步骤 2：运行测试确认统一查询缺失**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.WeaponEncumbranceTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest"
```

预期：FAIL，`adjustedEncumbrance`、`effectiveEncumbrance` 和武器专属抵消尚未定义。

- [ ] **步骤 3：在 `Weapon` 实现唯一算法**

```java
static int adjustedEncumbrance(int strengthRequirement, int strength, int reduction) {
    return Math.max(0, strengthRequirement - strength - Math.max(0, reduction));
}

protected int falsehoodPowerEncumbranceReduction(Hero owner) {
    return owner.hasTalent(Talent.FALSEHOOD_POWER)
            ? owner.pointsInTalent(Talent.FALSEHOOD_POWER) + 2 : 0;
}

public int effectiveEncumbrance(Hero owner) {
    return adjustedEncumbrance(STRReq(), owner.STR(),
            falsehoodPowerEncumbranceReduction(owner));
}
```

然后将 `accuracyFactor` 和 `baseDelay` 中的重复计算替换为：

```java
int encumbrance = owner instanceof Hero
        ? effectiveEncumbrance((Hero) owner) : 0;
```

默认普通武器的结果必须与修改前相同。

- [ ] **步骤 4：让其他武器负重消费者复用统一查询**

`MeleeWeapon.execute`：

```java
int aEnc = effectiveEncumbrance(hero);
```

`Hero.drRoll` 的武器分支：

```java
int aEnc = ((Weapon) belongings.weapon()).effectiveEncumbrance(this);
```

不要改动护甲负重分支，也不要修改其他无关减伤逻辑。

- [ ] **步骤 5：覆写耀金战戟抵消量**

```java
static int falsehoodPowerReduction(boolean hasTalent) {
    return hasTalent ? 2 : 0;
}

@Override
protected int falsehoodPowerEncumbranceReduction(Hero owner) {
    return falsehoodPowerReduction(owner.hasTalent(Talent.FALSEHOOD_POWER));
}
```

- [ ] **步骤 6：运行负重及邻接战斗回归**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.WeaponEncumbranceTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.TwoHandedGreatswordTest"
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/Weapon.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java
```

预期：全部 PASS。普通武器净收益仍为 2，耀金战戟为 1。

- [ ] **步骤 7：提交负重钩子**

提交前只暂存上述方法和调用点，不带入 `Hero.java` 的既有差异。建议提交信息：`refactor: 统一武器负重计算`。

---

## 任务 3：以确定性测试驱动两组独立命中效果

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java`

- [ ] **步骤 1：编写概率边界和实际 Buff 失败测试**

```java
@Test
public void passiveProbabilitiesUseIndependentStrictBoundaries() {
    assertEquals(0.25f, RadiantGoldHalberd.controlChance(false), 0f);
    assertEquals(0.75f, RadiantGoldHalberd.controlChance(true), 0f);
    assertEquals(0.15f, RadiantGoldHalberd.dazeChance(false), 0f);
    assertEquals(0.45f, RadiantGoldHalberd.dazeChance(true), 0f);
    assertTrue(RadiantGoldHalberd.triggers(0.2499f, 0.25f));
    assertFalse(RadiantGoldHalberd.triggers(0.25f, 0.25f));
    assertTrue(RadiantGoldHalberd.triggers(0.4499f, 0.45f));
    assertFalse(RadiantGoldHalberd.triggers(0.45f, 0.45f));
}

@Test
public void bothIndependentRollsMayApplyOnTheSameHit() {
    TestChar target = new TestChar();
    RadiantGoldHalberd.applyPassiveEffects(target, 4, true, 0f, 0f);
    assertNotNull(target.buff(Cripple.class));
    assertEquals(12f, target.buff(Bleeding.class).level(), 0f);
    assertNotNull(target.buff(Daze.class));
}

@Test
public void levelZeroAppliesCrippleWithoutCreatingZeroBleeding() {
    TestChar target = new TestChar();
    RadiantGoldHalberd.applyPassiveEffects(target, 0, false, 0f, 1f);
    assertNotNull(target.buff(Cripple.class));
    assertNull(target.buff(Bleeding.class));
    assertNull(target.buff(Daze.class));
}
```

`TestChar` 覆写 `act()` 返回 `true`，其余使用真实 Buff 容器。

- [ ] **步骤 2：运行测试确认被动方法缺失**

运行定向 `RadiantGoldHalberdTest`，预期上述方法缺失。

- [ ] **步骤 3：实现纯概率和附加方法**

```java
static float controlChance(boolean surprised) {
    return surprised ? 0.75f : 0.25f;
}

static float dazeChance(boolean surprised) {
    return surprised ? 0.45f : 0.15f;
}

static boolean triggers(float roll, float chance) {
    return roll < chance;
}

static void applyPassiveEffects(Char defender, int level, boolean surprised,
        float controlRoll, float dazeRoll) {
    int l = effectiveLevel(level);
    if (triggers(controlRoll, controlChance(surprised))) {
        Buff.prolong(defender, Cripple.class, 3f);
        if (l > 0) {
            Buff.affect(defender, Bleeding.class)
                    .set(3f * l, RadiantGoldHalberd.class);
        }
    }
    if (triggers(dazeRoll, dazeChance(surprised))) {
        Buff.prolong(defender, Daze.class, 3f);
    }
}
```

- [ ] **步骤 4：在 `proc` 保留父类逻辑并接入伏击判定**

```java
@Override
public int proc(Char attacker, Char defender, int damage) {
    int result = super.proc(attacker, defender, damage);
    if (!defender.isAlive() || defender.alignment != Char.Alignment.ENEMY) {
        return result;
    }
    boolean surprised = isSurprisedHit(attacker, defender);
    applyPassiveEffects(defender, buffedLvl(), surprised,
            Random.Float(), Random.Float());
    return result;
}
```

`isSurprisedHit` 先查询任务 4 将填充的武技伏击快照；不在武技中时，仅对 `Mob` 调用原生 `surprisedBy`，其他 `Char` 返回 false。

- [ ] **步骤 5：运行被动测试**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest"
```

预期：PASS；源码断言确认 `super.proc` 位于专属效果之前，并且存在两次独立 `Random.Float()`。

- [ ] **步骤 6：提交命中被动**

建议提交信息：`feat: 添加耀金战戟命中效果`。

---

## 任务 4：构建方向射线、目标快照和特殊伤害模式

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java`

- [ ] **步骤 1：编写武技公式和目标边界失败测试**

```java
@Test
public void abilityDamageUsesTheApprovedIndependentRange() {
    assertEquals(7, RadiantGoldHalberd.abilityMin(-3));
    assertEquals(7, RadiantGoldHalberd.abilityMax(-3));
    assertEquals(7, RadiantGoldHalberd.abilityMin(0));
    assertEquals(7, RadiantGoldHalberd.abilityMax(0));
    assertEquals(10, RadiantGoldHalberd.abilityMin(3));
    assertEquals(40, RadiantGoldHalberd.abilityMax(3));
    assertEquals(19, RadiantGoldHalberd.abilityMin(12));
    assertEquals(139, RadiantGoldHalberd.abilityMax(12));
}

@Test
public void visibilityIsNotPartOfLineTargetEligibility() {
    assertTrue(RadiantGoldHalberd.abilityTargetAllowed(
            true, Char.Alignment.ENEMY, false, true));
    assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
            false, Char.Alignment.ENEMY, false, true));
    assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
            true, Char.Alignment.ALLY, false, true));
    assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
            true, Char.Alignment.ENEMY, true, true));
    assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
            true, Char.Alignment.ENEMY, false, false));
}

@Test
public void onlyTerrainOrBoundaryAddsWallDamage() {
    assertEquals(1.2f, RadiantGoldHalberd.wallDamageMultiplier(true), 0f);
    assertEquals(1f, RadiantGoldHalberd.wallDamageMultiplier(false), 0f);
    assertFalse(RadiantGoldHalberd.wallCollision(false, false));
    assertTrue(RadiantGoldHalberd.wallCollision(true, false));
    assertTrue(RadiantGoldHalberd.wallCollision(false, true));
}
```

两个 `wallCollision` 参数依次表示“地图边界、实体地形”。角色占位不进入撞墙判定，只交给后续击退 API 处理，因此永不提供 20% 增伤。

- [ ] **步骤 2：编写目标顺序和路径源码失败测试**

要求生产代码：

- 使用 `new Ballistica(hero.pos, selectedCell, Ballistica.STOP_SOLID)`，不包含 `STOP_TARGET` 或 `STOP_CHARS`。
- 目标排序按 `pathIndex` 降序。
- 目标收集方法不读取 `Dungeon.level.heroFOV`。
- 路径格从索引 1 开始，排除英雄格和实体障碍格。
- 快照保存 Actor ID、预期格、路径索引、伏击状态和身后一格。

- [ ] **步骤 3：运行测试确认路径模型缺失**

运行定向 `RadiantGoldHalberdTest`，预期武技公式和路径辅助方法缺失。

- [ ] **步骤 4：实现武技纯函数和 `LineTarget`**

```java
static int abilityMin(int level) {
    return 7 + effectiveLevel(level);
}

static int abilityMax(int level) {
    return 7 + 11 * effectiveLevel(level);
}

static boolean abilityTargetAllowed(boolean alive, Char.Alignment alignment,
        boolean charmed, boolean onPath) {
    return alive && alignment == Char.Alignment.ENEMY && !charmed && onPath;
}

static boolean validDirectionCell(int heroCell, int selectedCell, int levelLength) {
    return selectedCell >= 0 && selectedCell < levelLength
            && selectedCell != heroCell;
}

static boolean wallCollision(boolean boundary, boolean terrainObstacle) {
    return boundary || terrainObstacle;
}

static float wallDamageMultiplier(boolean wallCollision) {
    return wallCollision ? 1.2f : 1f;
}

static final class LineTarget {
    final int actorId;
    final int expectedCell;
    final int pathIndex;
    final int nextCell;
    final boolean surprised;
    final boolean wallCollision;

    LineTarget(int actorId, int expectedCell, int pathIndex, int nextCell,
            boolean surprised, boolean wallCollision) {
        this.actorId = actorId;
        this.expectedCell = expectedCell;
        this.pathIndex = pathIndex;
        this.nextCell = nextCell;
        this.surprised = surprised;
        this.wallCollision = wallCollision;
    }
}

static final class AbilityLine {
    final int endCell;
    final ArrayList<LineTarget> targets;

    AbilityLine(int endCell, ArrayList<LineTarget> targets) {
        this.endCell = endCell;
        this.targets = targets;
    }
}
```

- [ ] **步骤 5：实现有效路径裁剪和目标快照**

`buildLine` 必须：

1. 拒绝 `null`、英雄格和地图外格。
2. 建立 `STOP_SOLID` 弹道。
3. 从 `path[1]` 开始找到障碍物前最后一个可飞行格。
4. 若没有有效格返回 `null`。
5. 使用路径格到索引的映射收集 `Actor.chars()`。
6. 对 `Mob` 在收集时调用 `surprisedBy(hero)`；非 `Mob` 为 false。
7. 不读取视野数组。
8. 按 `pathIndex` 从大到小排序。

路径裁剪的实际形状：

```java
private AbilityLine buildLine(Hero hero, Integer selectedCell) {
    if (selectedCell == null || !validDirectionCell(
            hero.pos, selectedCell, Dungeon.level.length())) return null;

    Ballistica trajectory = new Ballistica(hero.pos, selectedCell,
            Ballistica.STOP_SOLID);
    int travelEnd = 0;
    for (int i = 1; i <= trajectory.dist && i < trajectory.path.size(); i++) {
        int cell = trajectory.path.get(i);
        if (terrainObstacle(cell)) break;
        travelEnd = i;
    }
    if (travelEnd == 0) return null;

    HashMap<Integer, Integer> pathIndices = new HashMap<>();
    for (int i = 1; i <= travelEnd; i++) {
        pathIndices.put(trajectory.path.get(i), i);
    }

    ArrayList<LineTarget> targets = collectLineTargets(
            hero, trajectory, pathIndices);
    return new AbilityLine(trajectory.path.get(travelEnd), targets);
}

private boolean terrainObstacle(int cell) {
    return Dungeon.level.solid[cell]
            || (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]);
}
```

目标快照的实际形状：

```java
private ArrayList<LineTarget> collectLineTargets(Hero hero,
        Ballistica trajectory, HashMap<Integer, Integer> pathIndices) {
    ArrayList<LineTarget> targets = new ArrayList<>();
    for (Char target : Actor.chars()) {
        Integer pathIndex = pathIndices.get(target.pos);
        boolean allowed = abilityTargetAllowed(target.isAlive(), target.alignment,
                hero.isCharmedBy(target), pathIndex != null);
        if (!allowed) continue;

        int nextIndex = pathIndex + 1;
        int nextCell = nextIndex < trajectory.path.size()
                ? trajectory.path.get(nextIndex) : -1;
        boolean boundary = nextCell < 0 || !Dungeon.level.insideMap(nextCell);
        boolean obstacle = !boundary && terrainObstacle(nextCell);
        boolean surprised = target instanceof Mob
                && ((Mob) target).surprisedBy(hero);
        targets.add(new LineTarget(target.id(), target.pos, pathIndex, nextCell,
                surprised, wallCollision(boundary, obstacle)));
    }
    Collections.sort(targets, (left, right) ->
            Integer.compare(right.pathIndex, left.pathIndex));
    return targets;
}
```

- [ ] **步骤 6：实现只在武技期间生效的基础伤害替换**

增加 transient 状态：

```java
private transient boolean abilityDamageActive;
private transient int currentAbilityTargetId = -1;
private transient HashSet<Integer> abilitySurprisedTargetIds = new HashSet<>();
```

覆写 `damageRoll`：

```java
@Override
public int damageRoll(Char owner) {
    if (!abilityDamageActive) return super.damageRoll(owner);
    int damage = augment.damageFactor(Random.NormalIntRange(
            abilityMin(buffedLvl()), abilityMax(buffedLvl())));
    if (owner instanceof Hero) {
        int excessStrength = ((Hero) owner).STR() - STRReq();
        if (excessStrength > 0) {
            damage += Hero.heroDamageIntRange(0, excessStrength);
        }
    }
    return damage;
}
```

`isSurprisedHit` 在 `abilityDamageActive` 且 Actor ID 匹配时返回快照值；否则使用普通原生伏击查询。

```java
private boolean isSurprisedHit(Char attacker, Char defender) {
    if (abilityDamageActive && defender.id() == currentAbilityTargetId) {
        return abilitySurprisedTargetIds.contains(defender.id());
    }
    return defender instanceof Mob && ((Mob) defender).surprisedBy(attacker);
}
```

- [ ] **步骤 7：运行路径与伤害测试**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest"
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java
```

预期：PASS；+0 武技为 7~7，+3 为 10~40；源码不含目标收集视野门槛。

- [ ] **步骤 8：提交路径与伤害模式**

建议提交信息：`feat: 添加耀金战戟方向射线`。

---

## 任务 5：实现贯戟队列、不旋转投影和唯一结束出口

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberd.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/MissileSprite.java:118-181`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/MissileSpriteTest.java`

- [ ] **步骤 1：编写武技入口和充能失败测试**

测试子类暴露 `baseChargeUse`，断言：

```java
assertEquals(2, weapon.chargeCost(hero));
assertNotNull(weapon.targetingPrompt());
assertFalse(RadiantGoldHalberd.validDirectionCell(10, 10, 100));
assertFalse(RadiantGoldHalberd.validDirectionCell(10, -1, 100));
assertFalse(RadiantGoldHalberd.validDirectionCell(10, 100, 100));
assertTrue(RadiantGoldHalberd.validDirectionCell(10, 11, 100));
```

源码回归要求：

- 成功路径即调用一次 `beforeAbilityUsed(hero, null)` 和一次 `hero.busy()`。
- 无目标分支仍进入统一 `finishAbility`。
- 目标处理调用 `hero.attack(target, multiplier, 0f, Char.INFINITE_ACCURACY)`。
- 使用 `WandOfBlastWave.throwCharImmediately` 的 callback 重载，`collideDmg=false`。
- 下一目标只在击退回调中启动。
- `finishAbility` 同时清理状态、解除隐身、耗时并调用 `afterAbilityUsed`。

- [ ] **步骤 2：编写角速度失败测试**

```java
@Test
public void radiantGoldHalberdDoesNotSpinInFlight() {
    assertEquals(0, MissileSprite.angularSpeedFor(new RadiantGoldHalberd()));
}

@Test
public void unregisteredItemsKeepTheDefaultSpin() {
    assertEquals(720, MissileSprite.angularSpeedFor(new Item() {}));
}
```

- [ ] **步骤 3：运行测试确认武技和角速度尚未实现**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest" --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSpriteTest"
```

预期：FAIL，能力入口、队列和角速度查询缺失。

- [ ] **步骤 4：实现能力入口和投影启动**

```java
@Override
protected int baseChargeUse(Hero hero, Char target) {
    return 2;
}

@Override
public String targetingPrompt() {
    return Messages.get(this, "prompt");
}

@Override
protected void duelistAbility(final Hero hero, Integer selectedCell) {
    AbilityLine line = buildLine(hero, selectedCell);
    if (line == null) {
        GLog.w(Messages.get(this, "ability_no_path"));
        return;
    }
    beforeAbilityUsed(hero, null);
    hero.busy();
    abilityResolving = true;
    abilitySurprisedTargetIds.clear();
    for (LineTarget target : line.targets) {
        if (target.surprised) abilitySurprisedTargetIds.add(target.actorId);
    }
    Sample.INSTANCE.play(Assets.Sounds.MISS);
    launchProjection(hero, line.endCell, () -> resolveLineTarget(hero, line, 0));
}
```

能力开始前增加：

```java
private transient boolean abilityResolving;
```

`launchProjection` 在 `hero.sprite == null`、`parent == null` 时直接调用回调，否则执行：

```java
((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class))
        .reset(hero.sprite, endCell, this, callback);
```

- [ ] **步骤 5：实现从远到近的逐目标队列**

每一步严格按以下结构：

```java
private void resolveLineTarget(Hero hero, AbilityLine line, int index) {
    if (index >= line.targets.size()) {
        finishAbility(hero);
        return;
    }
    LineTarget record = line.targets.get(index);
    Char target = Actor.findCharById(record.actorId);
    if (!validRecordedTarget(hero, target, record)) {
        resolveLineTarget(hero, line, index + 1);
        return;
    }

    abilityDamageActive = true;
    currentAbilityTargetId = target.id();
    boolean hit;
    try {
        float multiplier = wallDamageMultiplier(record.wallCollision);
        hit = hero.attack(target, multiplier, 0f, Char.INFINITE_ACCURACY);
    } finally {
        abilityDamageActive = false;
        currentAbilityTargetId = -1;
    }

    if (hit && !target.isAlive()) onAbilityKill(hero, target);
    if (!hit || !target.isAlive() || record.wallCollision
            || target.pos != record.expectedCell
            || Pushing.pushingExistsForChar(target)) {
        resolveLineTarget(hero, line, index + 1);
        return;
    }

    Ballistica push = new Ballistica(target.pos, record.nextCell,
            Ballistica.PROJECTILE);
    WandOfBlastWave.throwCharImmediately(target, push, 1,
            true, false, this,
            () -> resolveLineTarget(hero, line, index + 1));
}
```

若 `record.nextCell` 在回调前被角色占据，`WandOfBlastWave` 报告零位移后仍继续队列；不得为这种占位追加撞墙伤害。

记录目标校验固定为：

```java
private boolean validRecordedTarget(Hero hero, Char target, LineTarget record) {
    return target != null && target.id() == record.actorId
            && target.pos == record.expectedCell
            && abilityTargetAllowed(target.isAlive(), target.alignment,
                    hero.isCharmedBy(target), true);
}
```

- [ ] **步骤 6：实现幂等结束出口**

```java
private void finishAbility(Hero hero) {
    if (!abilityResolving) return;
    abilityResolving = false;
    abilityDamageActive = false;
    currentAbilityTargetId = -1;
    abilitySurprisedTargetIds.clear();
    Invisibility.dispel();
    hero.spendAndNext(hero.attackDelay());
    afterAbilityUsed(hero);
}
```

所有成功发射分支，包括没有目标、目标全部失效、目标中途死亡、击退为 0 和无精灵，最终只能调用这个出口。非法方向在 `beforeAbilityUsed` 之前返回。

- [ ] **步骤 7：提取并登记角速度查询**

在 `MissileSprite` 中加入导入与映射：

```java
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberd;

ANGULAR_SPEEDS.put(RadiantGoldHalberd.class, 0);
```

将当前查询循环提取为：

```java
static int angularSpeedFor(Item item) {
    int result = DEFAULT_ANGULAR_SPEED;
    if (item == null) return result;
    for (Class<? extends Item> cls : ANGULAR_SPEEDS.keySet()) {
        if (cls.isAssignableFrom(item.getClass())) {
            result = ANGULAR_SPEEDS.get(cls);
            break;
        }
    }
    return result;
}
```

`setup` 使用 `angularSpeed = angularSpeedFor(item)`；方向对齐的 `angle` 和水平翻转逻辑保持不变。

- [ ] **步骤 8：运行能力、角速度和 busy 回归**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest" --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSpriteTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.PalermoSwordTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.HundredTonHammerTest"
```

预期：全部 PASS。源码测试必须拒绝普通 `throwChar`、并发循环启动多次击退、多个耗时出口和在目标收集处读取 `heroFOV`。

- [ ] **步骤 9：提交贯戟与动画**

建议提交信息：`feat: 实现耀金战戟贯戟武技`。

---

## 任务 6：集成生成器、正式文本、custom 资料和图鉴

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java:206-216,534-547`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponIntegrationTest.java:1-50,101-124`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java:321-332`
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`
- 修改：`core/src/main/assets/messages/custom/custom.properties`
- 修改：`core/src/main/assets/messages/custom/custom_zh.properties`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/RadiantGoldHalberdTest.java`

- [ ] **步骤 1：编写集成与文本失败测试**

断言：

- `Generator.Category.WEP_T6.classes` 包含且只包含一次 `RadiantGoldHalberd.class`。
- 对应 `defaultProbs` 权重为 1，且三个数组长度一致。
- `TestMelee.weaponList(6)` 包含本武器。
- 中英文正式文本各只包含一次下列键：

```text
items.weapon.melee.tier6.radiantgoldhalberd.name
items.weapon.melee.tier6.radiantgoldhalberd.prompt
items.weapon.melee.tier6.radiantgoldhalberd.ability_name
items.weapon.melee.tier6.radiantgoldhalberd.ability_no_path
items.weapon.melee.tier6.radiantgoldhalberd.typical_ability_desc
items.weapon.melee.tier6.radiantgoldhalberd.ability_desc
items.weapon.melee.tier6.radiantgoldhalberd.desc
items.weapon.melee.tier6.radiantgoldhalberd.stats_desc
```

- custom 中英文各只包含 `custom.dict.dict.melee_radiantgoldhalberd` 与 `_d`。
- `DictionaryJournal.WEAPONS` 将 key 映射到 `RADIANT_GOLD_HALBERD`。

- [ ] **步骤 2：运行测试确认集成缺失**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponIntegrationTest"
```

预期：FAIL，生成器、文本或图鉴尚未登记。

- [ ] **步骤 3：追加生成器类与权重**

重新读取当前 `WEP_T6` 数组，保留执行时已经存在的全部条目，在尾部追加：

```java
RadiantGoldHalberd.class
```

并在同位置追加 `1`。同步更新 `Tier6WeaponIntegrationTest` 的两个精确数组；不得删除先前落地的六阶武器。

- [ ] **步骤 4：写入简短中文正式文本**

```properties
items.weapon.melee.tier6.radiantgoldhalberd.name=耀金战戟
items.weapon.melee.tier6.radiantgoldhalberd.prompt=选择贯戟的方向
items.weapon.melee.tier6.radiantgoldhalberd.ability_name=贯戟
items.weapon.melee.tier6.radiantgoldhalberd.ability_no_path=该方向紧邻障碍物，无法贯戟。
items.weapon.melee.tier6.radiantgoldhalberd.typical_ability_desc=决斗家可消耗_2_点充能，将耀金战戟的投影沿所选方向贯穿至障碍物，对路径上的所有敌人造成_%1$d~%2$d点伤害_，必定命中并将其击退。撞墙的敌人受到20%%额外伤害。
items.weapon.melee.tier6.radiantgoldhalberd.ability_desc=决斗家可消耗_2_点充能，将耀金战戟的投影沿所选方向贯穿至障碍物，对路径上的所有敌人造成_%1$d~%2$d点伤害_，必定命中并将其击退。撞墙的敌人受到20%%额外伤害。
items.weapon.melee.tier6.radiantgoldhalberd.desc=由耀金铸成的沉重战戟。宽阔的长刃在挥动时会留下刺目的金色轨迹。
items.weapon.melee.tier6.radiantgoldhalberd.stats_desc=这是一件非常沉重、攻击缓慢且攻击距离很远的武器。命中时可能使敌人残废、流血或恍惚，伏击会显著提高触发概率。
```

在武器类实现：

```java
@Override
public String abilityInfo() {
    int level = levelKnown ? buffedLvl() : 0;
    return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc",
            augment.damageFactor(abilityMin(level)),
            augment.damageFactor(abilityMax(level)));
}

@Override
public String upgradeAbilityStat(int level) {
    return augment.damageFactor(abilityMin(level)) + "-"
            + augment.damageFactor(abilityMax(level));
}
```

- [ ] **步骤 5：写入等义英文正式文本**

```properties
items.weapon.melee.tier6.radiantgoldhalberd.name=radiant gold halberd
items.weapon.melee.tier6.radiantgoldhalberd.prompt=Choose the direction of transfix
items.weapon.melee.tier6.radiantgoldhalberd.ability_name=transfix
items.weapon.melee.tier6.radiantgoldhalberd.ability_no_path=An adjacent obstacle blocks transfix in that direction.
items.weapon.melee.tier6.radiantgoldhalberd.typical_ability_desc=The Duelist spends _2 charges_ to send a radiant projection through every enemy along the chosen direction until it meets an obstacle, typically dealing _%1$d-%2$d damage_. Every strike is guaranteed to hit and knocks its target back; enemies pinned against terrain take 20%% more damage.
items.weapon.melee.tier6.radiantgoldhalberd.ability_desc=The Duelist spends _2 charges_ to send a radiant projection through every enemy along the chosen direction until it meets an obstacle, dealing _%1$d-%2$d damage_. Every strike is guaranteed to hit and knocks its target back; enemies pinned against terrain take 20%% more damage.
items.weapon.melee.tier6.radiantgoldhalberd.desc=A ponderous halberd forged from radiant gold. Its broad blade leaves a dazzling golden trail through every swing.
items.weapon.melee.tier6.radiantgoldhalberd.stats_desc=This is a very heavy, slow weapon with tremendous reach. Its hits may cripple, bleed, or daze enemies, with much greater odds during surprise attacks.
```

- [ ] **步骤 6：写入完整 custom 资料并登记图鉴**

```properties
custom.dict.dict.melee_radiantgoldhalberd =耀金战戟
custom.dict.dict.melee_radiantgoldhalberd_d =_6_阶武器，_22_力量需求，_6~48_基础伤害。+9与+15时力量需求各降低1点；力量药剂精通仍额外降低2点。\n\n它的基础成长为每级_1~9_，每升3级额外增加_1~2_，从+12开始每级再额外增加_1~2_；两种额外成长可以同时生效。精准修正为_1_，攻击延迟为_2_，攻击距离为_3_。虚假之力对这件武器的净负重收益减半。\n\n普通命中分别进行两次独立判定：25%概率施加3回合残废与_3*武器等级_的流血，15%概率施加3回合恍惚；伏击时两项概率分别提高至75%与45%。\n\n_决斗家_武技(贯戟)：消耗_2_点充能，向所选方向发射不旋转的战戟投影，直到实体障碍物或地图边界。路径上的全部敌人都会受到_7+武器等级~7+11*武器等级_点必定命中的武器伤害，包括视野外敌人，并按从远到近的顺序击退1格。紧贴实体障碍或边界的敌人不会移动且本次伤害提高至120%；角色占位或击退免疫不提供增伤。有效路径上没有敌人时仍消耗充能和行动时间，紧邻障碍、没有飞行格时则取消施放。
custom.dict.dict.melee_radiantgoldhalberd =radiant gold halberd
custom.dict.dict.melee_radiantgoldhalberd_d =A _tier-6_ weapon requiring _22 strength_ and dealing _6-48 base damage_. Its requirement drops by 1 at +9 and +15, while mastery still reduces it by 2 more.\n\nIt grows by _1-9_ per level, gains another _1-2_ every third level, and gains a further _1-2_ each level from +12 onward; both extra curves can apply together. It has _1.0 accuracy_, _2.0 attack delay_, and _3-tile reach_. Falsehood Power provides only half of its normal net encumbrance benefit.\n\nEach normal hit makes two independent checks: 25% to cripple for 3 turns and inflict bleeding equal to _3*weapon level_, and 15% to daze for 3 turns. Surprise attacks raise these chances to 75% and 45%.\n\n_Duelist ability (transfix):_ spends _2 charges_ to launch a non-spinning projection along the chosen direction until solid terrain or the map boundary. Every enemy on the line, including unseen enemies, takes guaranteed weapon damage of _7+level to 7+11*level_ and is processed from farthest to nearest for a 1-tile knockback. An enemy pinned against terrain or the boundary stays in place and takes 120% damage; occupation by another character or knockback immunity grants no bonus. A valid empty line still costs charge and time, while an immediately blocked direction cancels the cast.
```

分别把中文两行放入 `custom_zh.properties`、英文两行放入 `custom.properties`，不要把四行同时写入同一文件。

图鉴追加：

```java
WEAPONS.d.put("melee_radiantgoldhalberd",
        EXItemSpriteSheet.RADIANT_GOLD_HALBERD);
```

- [ ] **步骤 7：运行生成器与文本测试**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponIntegrationTest"
```

预期：PASS；properties 无重复键、`TODO`、`TBD` 或 `%` 格式错误。

- [ ] **步骤 8：提交集成**

只暂存本武器新增行和数组新增项。建议提交信息：`feat: 集成耀金战戟生成与文本`。

---

## 任务 7：清理参考素材并只写入索引 156

**技能：** 此任务必须使用 `pixel-art-sprites`；参考构图已经确认，不开启浏览器视觉索引展示。

**文件：**

- 读取：`D:\桌面\耀金战戟.png`
- 创建：`D:\STUDY\Dungeon\cache\radiant_gold_halberd\clean_radiant_gold_halberd.py`
- 修改：`core/src/main/assets/sprites/ex_items.png`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

- [ ] **步骤 1：编写索引和像素失败测试**

增加：

```java
assertEquals(156, EXItemSpriteSheet.frameFor(
        EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
assertEquals(192, EXItemSpriteSheet.frameX(
        EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
assertEquals(144, EXItemSpriteSheet.frameY(
        EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
assertEquals(16, EXItemSpriteSheet.frameWidth(
        EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
assertEquals(16, EXItemSpriteSheet.frameHeight(
        EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
assertCrispSprite(sheet, 156, 0, 0, 15, 15, 10);
for (int index = 157; index < 160; index++) {
    assertTransparentCell(sheet, index);
}
```

删除对 155~159 整段透明的旧断言；155 不由本任务读取、清空或强制透明。

- [ ] **步骤 2：运行贴图测试确认 156 为空**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest"
```

预期：FAIL，索引 156 尚无硬边像素。

- [ ] **步骤 3：在项目外创建确定性清理脚本**

脚本读取原始 16×16 PNG，不缩放，使用以下固定调色板：

```python
PALETTE = [
    (37, 36, 36, 255),   # outline
    (46, 20, 1, 255),    # deepest brown
    (96, 42, 5, 255),    # handle
    (129, 109, 8, 255),  # gold shadow
    (181, 154, 14, 255), # dark gold
    (218, 185, 19, 255), # gold
    (248, 209, 13, 255), # gold highlight
    (110, 118, 120, 255),# steel shadow
    (184, 191, 193, 255),# steel
    (218, 225, 226, 255) # steel highlight
]
```

逐像素规则：

```python
if alpha == 0 or alpha == 1:
    output = (0, 0, 0, 0)
elif alpha == 67:
    output = PALETTE[0]
elif blue > red * 2 and blue > green * 2:
    output = PALETTE[0]
elif red > 140 and green < 60:
    output = PALETTE[2]
else:
    output = min(PALETTE, key=lambda c:
        (red-c[0])**2 + (green-c[1])**2 + (blue-c[2])**2)
```

这会把 70 个半透明黑色轮廓像素变成硬边轮廓、丢弃 1 个 alpha=1 杂点、把单个蓝色杂点并入轮廓，并将红褐连接像素归入握柄。输出必须保持原始坐标和 `(0,0)~(15,15)` 包围盒。

- [ ] **步骤 4：生成 16×16 清理稿和 16 倍预览**

预览只能使用最近邻：

```python
preview = cleaned.resize((256, 256), Image.Resampling.NEAREST)
```

断言：尺寸 16×16、alpha 仅 0/255、不透明颜色不超过 10、1× 下仍读作左下长柄和右上宽刃的耀金战戟。

- [ ] **步骤 5：只写入目标单元格并验证目标外像素不变**

脚本必须：

1. 读取 `ex_items.png` 并复制完整写入前像素数组。
2. 计算索引 156 左上角 `(156 % 16 * 16, 156 / 16 * 16) = (192, 144)`。
3. 只清空并粘贴 `(192,144)~(207,159)`。
4. 使用临时 PNG 原子替换素材表。
5. 对 156 单元格之外的每个像素执行相等断言。
6. 不读取或修改索引 155 的内容。

- [ ] **步骤 6：运行贴图与武器回归**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest"
```

预期：PASS；156 为硬边 16×16，157~159 透明，目标外像素完全不变。

- [ ] **步骤 7：提交贴图**

只提交 `ex_items.png` 和测试；cache 文件不进入仓库。建议提交信息：`art: 清理并写入耀金战戟贴图`。

---

## 任务 8：综合回归、代码审查与交付

**文件：** 验证本计划涉及的全部生产、测试与资源文件。

- [ ] **步骤 1：运行本功能定向测试**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberdTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.WeaponEncumbranceTest" --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSpriteTest" --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponIntegrationTest"
```

预期：BUILD SUCCESSFUL。

- [ ] **步骤 2：运行相邻六阶武器和击退回归**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.PalermoSwordTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.HundredTonHammerTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.TwoHandedGreatswordTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhipTest"
```

预期：BUILD SUCCESSFUL，现有连续突刺、立即击退、强制附魔和随机减益不受影响。

- [ ] **步骤 3：运行核心模块测试与编译**

```powershell
.\gradlew.bat core:test
.\gradlew.bat core:compileJava
```

预期：BUILD SUCCESSFUL。若共享工作区中的无关未完成改动阻塞全量测试，保留完整命令、首个错误和无关证据，但仍必须保证步骤 1、2 全部通过。

- [ ] **步骤 4：执行静态与资源检查**

```powershell
git diff --check
git status --short
git diff --stat
```

另外确认：

- 没有 `TODO`、`TBD`、临时日志、调试开关或未替换文案。
- 武技 transient 状态未写入 Bundle。
- `finishAbility` 只有一个耗时与 `afterAbilityUsed` 出口，并有幂等保护。
- 非法方向在 `beforeAbilityUsed` 之前返回，成功空射在结束出口扣费耗时。
- 目标收集不读取 `heroFOV`，但会排除友军、中立与魅惑来源。
- 每个目标的武技伤害模式和伏击快照在 `finally` 中清除。
- 角色占位、扎根和 `IMMOVABLE` 不会误得撞墙 20% 增伤。
- `MissileSprite` 保留方向对齐，仅角速度为 0。
- `ex_items.png` 的索引 156 外哈希与写入前一致。

- [ ] **步骤 5：执行代码审查**

审查重点：

1. `STOP_SOLID` 是否真的越过选择格且不因角色停止。
2. 视野外目标是否会被命中，又是否避免被设置成快捷攻击目标。
3. 远到近队列是否逐个等待 `throwCharImmediately` 回调，所有分支是否最终结束 `busy`。
4. 1.2 倍是否属于同一次攻击，是否只触发一次附魔与两组被动。
5. 普通与武技被动是否使用正确的伏击语义和两次独立随机数。
6. 统一负重查询是否覆盖精准、延迟、武技门槛和武器减伤，又没有改变护甲逻辑。
7. 共享脏文件是否只包含本任务新增片段。

- [ ] **步骤 6：最终审查暂存区**

```powershell
git diff --cached --check
git diff --cached --stat
git diff --cached
```

仅当暂存区完全属于本任务时提交遗漏文件。建议最终提交信息：`feat: 实装六阶武器耀金战戟`。

- [ ] **步骤 7：交付报告**

报告简洁列出：

- 基础面板、特殊力量阈值、被动和贯戟的实际结果。
- 视野外直线路径、远到近击退、撞墙单次 1.2 倍与空射规则。
- 虚假之力统一负重钩子及普通武器回归。
- 贴图索引 156 和项目外 cache 路径。
- 实际运行的定向、邻接与全量测试命令及结果。
- 若全量测试被无关脏改动阻塞，清楚区分“本任务定向验证通过”和“全量验证阻塞”。
