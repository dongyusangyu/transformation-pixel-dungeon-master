# 猛毒小镰刀实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `executing-plans` 在当前共享工作区内逐任务执行；每项生产改动遵循 `test-driven-development`，完成后使用 `requesting-code-review` 与 `verification-before-completion`。由于 `Hero.java`、`Char.java`、`Mob.java`、`Generator.java`、`EXItemSpriteSheet.java` 和素材表当前含有其他未提交改动，推荐内联执行并逐段保护既有差异。

**目标：** 实装六阶近战武器“猛毒小镰刀”，包括巨镰式面板、四类随机毒效、走位触发的零回合追击、决斗家武技“取消”、真正的原生伏击语义、生成器、正式/资料文本和已确认的 16×16 透明贴图。

**架构：** `VenomousSickle` 实例保存不可跨读档的短期状态机，区分普通攻击、追镰免费攻击和“取消”延长的免费伏击。`MeleeWeapon` 增加默认无行为的通用攻击/走路生命周期钩子；`Hero` 在实际攻击和正常走路路径上调用钩子；`Char` 与 `Mob` 通过 `MeleeWeapon` 的通用强制伏击查询进入现有 `surprisedBy`、`FloatingText.HIT_SUPR` 和 `Surprise.hit` 链路。随机毒效使用可测试的纯公式和确定性候选表，运行时才注入随机数。

**技术栈：** Java、JUnit 4、Gradle、Shattered Pixel Dungeon 战斗/Buff API、PNG 16×16 像素表、Pillow（仅用于目标单元格的确定性写入和校验）。

**设计规格：** `docs/superpowers/specs/2026-08-20-venomous-sickle-design.md`

---

## 文件结构

### 创建

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickle.java` —— 武器面板、状态机、毒性池和“取消”。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickleTest.java` —— 面板、概率、毒性池、状态机、能力与生成器测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeaponAttackLifecycleTest.java` —— 通用钩子和零延迟查询不被提前消费的回归测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/ForcedSurpriseAttackTest.java` —— 必中与原生伏击显示/结算测试。

### 修改

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java` —— 默认无行为的攻击、移动、延迟完成和强制伏击接口。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java` —— 在普通攻击与玩家走路路径中调用通用钩子。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java` —— 在命中判定中识别通用强制伏击。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java` —— `surprisedBy` 识别通用强制伏击，从而复用伏击反馈与联动。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java` —— 定义索引 155 的 `VENOMOUS_SICKLE`。
- `core/src/main/assets/sprites/ex_items.png` —— 仅写入索引 155。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java` —— 登记新常量和像素约束，将剩余空白范围收窄到 156–159。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java` —— 追加 `WEP_T6` 类和权重。
- `core/src/main/assets/messages/items/items.properties` —— 英文正式文本。
- `core/src/main/assets/messages/items/items_zh.properties` —— 中文正式文本。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java` —— 图鉴登记。
- `core/src/main/assets/messages/custom/custom.properties` —— 英文详细资料。
- `core/src/main/assets/messages/custom/custom_zh.properties` —— 中文详细资料。

### 项目外中间产物

- `D:\STUDY\Dungeon\cache\venomous_sickle\render_venomous_sickle.py`
- `D:\STUDY\Dungeon\cache\venomous_sickle\source_reference.png`
- `D:\STUDY\Dungeon\cache\venomous_sickle\venomous_sickle_16x16.png`
- `D:\STUDY\Dungeon\cache\venomous_sickle\venomous_sickle_preview_16x.png`

---

## 任务 1：保护共享工作区并建立红灯基线

**文件：**

- 读取：上述全部生产文件与现有相关测试。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickleTest.java`

- [ ] **步骤 1：记录所有共享文件的当前差异**

运行：

```powershell
git status --short
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/main/assets/sprites/ex_items.png
```

保存基线输出用于每个任务后对照。不得恢复、覆盖或顺带格式化已有差异；每次修改共享文件前重新读取目标片段。

- [ ] **步骤 2：编写基础面板失败测试**

测试固定以下数值：

```java
@Test
public void tierSixWarScytheProfileMatchesSpecification() {
    VenomousSickle weapon = new VenomousSickle();
    assertEquals(6, weapon.tierForTest());
    assertEquals(20, weapon.STRReq(0));
    assertEquals(6, weapon.min(0));
    assertEquals(47, weapon.max(0));
    assertEquals(13, weapon.min(7));
    assertEquals(96, weapon.max(7));
    assertEquals(0.8f, weapon.accuracyForTest(), 0f);
    assertEquals(1f, weapon.delayForTest(), 0f);
    assertEquals(1, weapon.rangeForTest());
}
```

若字段为受保护成员，用测试子类只暴露值，不复制生产公式。

- [ ] **步骤 3：运行测试并确认红灯来自类缺失**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest"
```

预期：FAIL，原因是 `VenomousSickle` 尚不存在；若先因工作区无关编译错误失败，记录完整错误并先隔离该阻塞，不伪造红灯结论。

---

## 任务 2：以 TDD 引入通用攻击与移动生命周期钩子

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeaponAttackLifecycleTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java`

- [ ] **步骤 1：编写钩子顺序失败测试**

用一个 `TrackingWeapon extends MeleeWeapon` 记录事件，验证一次普通攻击按以下顺序且各调用一次：

```text
beforeHeroAttack
实际命中/伤害流程
afterHeroAttack
Hero.spend(attackDelay)
afterHeroAttackDelayResolved
```

另测：

- 命中与未命中都调用 `afterHeroAttack`。
- 主武器与副武器轮换时，只通知本次实际攻击实例。
- 能力攻击可由武器自行识别 `hero.belongings.abilityWeapon`，通用钩子不假定它一定是普通攻击。
- `delayFactor()` 在 `attackSkill()` 中被提前查询时，零延迟标志仍保留到最终 `spend(attackDelay())`。

- [ ] **步骤 2：编写正常走路通知失败测试**

验证 `MeleeWeapon.notifyHeroStep(hero, from, to)`：

- 玩家正常完成一个实际位移时通知当前装备的主、副近战武器，各实例最多一次。
- `from == to` 不通知。
- 传送、突进、击退等未经过 `Hero.getCloser` 的位移不通知。

- [ ] **步骤 3：运行新测试确认红灯**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeaponAttackLifecycleTest"
```

预期：FAIL，通用钩子尚不存在。

- [ ] **步骤 4：在 `MeleeWeapon` 增加默认无行为接口**

接口形状保持通用，不引用 `VenomousSickle`：

```java
public void beforeHeroAttack(Hero hero, Char target) {}
public void afterHeroAttack(Hero hero, Char target, boolean hit) {}
public void afterHeroAttackDelayResolved(Hero hero) {}
public void onHeroStep(Hero hero, int from, int to) {}

public boolean forcesSurpriseAttack(Hero hero, Char target) {
    return false;
}

public static boolean isForcedSurpriseAttack(Char attacker, Char defender) {
    if (!(attacker instanceof Hero)) return false;
    KindOfWeapon weapon = ((Hero) attacker).belongings.attackingWeapon();
    return weapon instanceof MeleeWeapon
            && ((MeleeWeapon) weapon).forcesSurpriseAttack((Hero) attacker, defender);
}
```

再提供一个去重通知主/副武器的 `notifyHeroStep` 静态帮助方法。

- [ ] **步骤 5：在 `Hero` 接入攻击钩子**

在 `Hero.attack(...)` 开始时捕获一次 `belongings.attackingWeapon()`，若为 `MeleeWeapon`：

1. 在 `super.attack(...)` 前调用 `beforeHeroAttack`。
2. 在 `super.attack(...)` 完整返回后调用 `afterHeroAttack`。
3. 使用 `try/finally` 保证异常或提前退出不会遗留“本次攻击中”状态。

在两个普通攻击完成路径的 `spend(attackDelay())` 之后调用同一武器实例的 `afterHeroAttackDelayResolved`。不要让 `delayFactor` 的第一次查询直接清除零延迟标志，因为 `Hero.attackSkill()` 会在真正耗时前查询攻击延迟。

- [ ] **步骤 6：在玩家正常走路路径接入移动钩子**

在 `Hero.getCloser` 中记录实际移动前位置，在 `move(step)` 完成且 `oldPos != pos` 后调用 `MeleeWeapon.notifyHeroStep(this, oldPos, pos)`。不要在通用 `Hero.move` 中调用，以免传送或强制位移被误认为追击走位。

- [ ] **步骤 7：验证钩子测试与既有武器回归**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeaponAttackLifecycleTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.PalermoSwordTest"
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeaponAttackLifecycleTest.java
```

预期：PASS；巴勒莫之剑的既有零回合逻辑未被破坏。

- [ ] **步骤 8：提交时只包含本任务差异**

若 `Hero.java` 在基线已脏，不得直接 `git add Hero.java`。使用逐段暂存或等价的索引补丁，并在提交前检查：

```powershell
git diff --cached --check
git diff --cached --stat
git diff --cached
```

建议提交信息：`refactor: 增加近战攻击生命周期钩子`。

---

## 任务 3：实现武器面板与不可循环的追击状态机

**文件：**

- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickle.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickleTest.java`

- [ ] **步骤 1：编写状态机失败测试**

使用包可见的纯状态对象或包可见帮助方法，覆盖：

1. 普通成功命中活目标后进入 `TRACKING`，记录目标 ID 和命中位置。
2. 未命中、目标已死或能力攻击不进入追踪。
3. 强制位移通知不会推进；正常步骤离开原位置并最终相邻时进入 `PURSUIT_READY`。
4. 仍在原格、未相邻、目标 ID 不同或目标失效时不能免费攻击。
5. 对同一目标开始攻击时得到 `PURSUIT_FREE`，资格立即消费；命中与未命中最终延迟都为 0。
6. `PURSUIT_FREE` 完成后只打开一次 `cancelWindow`，不重新追踪。
7. 攻击错误目标会清除旧资格，并把该攻击按 `NORMAL` 结算。
8. 卸下、丢弃、投掷、换层等清理入口可幂等调用。

建议状态字段：

```java
enum AttackMode { NORMAL, PURSUIT_FREE, FORCED_AMBUSH_FREE }

int trackedTargetId = -1;
int originPos = -1;
boolean movedSinceHit;
boolean pursuitReady;
boolean cancelWindow;
boolean forcedAmbushArmed;

transient AttackMode activeAttackMode = AttackMode.NORMAL;
transient int activeTargetId = -1;
transient boolean freeDelayPending;
```

- [ ] **步骤 2：运行状态测试确认红灯**

运行定向 `VenomousSickleTest`，预期因状态 API 尚未实现而失败。

- [ ] **步骤 3：实现基础武器类**

构造器设置：

```java
image = EXItemSpriteSheet.VENOMOUS_SICKLE;
hitSound = Assets.Sounds.HIT_SLASH;
hitSoundPitch = 0.9f;
tier = 6;
ACC = 0.8f;
RCH = 1;
```

`min(level)` 返回 `6 + level`；`max(level)` 返回 `47 + 7 * level`。专属概率与持续时间统一使用 `effectiveLevel() = Math.max(0, buffedLvl())`。

- [ ] **步骤 4：实现攻击模式固定与零延迟**

`beforeHeroAttack` 只在本武器是实际攻击实例且不是能力攻击/投掷时选择模式：

1. `forcedAmbushArmed` 优先选择 `FORCED_AMBUSH_FREE`。
2. 否则，追镰资格与同一目标、相邻关系均有效时选择 `PURSUIT_FREE`。
3. 否则选择 `NORMAL`，并在发起新攻击时关闭未使用的 `cancelWindow`。

免费模式设置 `freeDelayPending = true`。`delayFactor(owner)` 在该标志存续期间返回 0，但不自行消费；只有 `afterHeroAttackDelayResolved` 清理，以抵抗 `attackSkill()` 的提前查询。

- [ ] **步骤 5：实现普通命中、移动与攻击完成结算**

- `afterHeroAttack` 在 `NORMAL + hit + target alive` 时记录目标。
- `onHeroStep` 只更新当前武器实例的追踪，并在实际离开原位且最终相邻时置 `pursuitReady`。
- `PURSUIT_FREE` 无论命中与否都打开 `cancelWindow`，并清除追踪。
- `FORCED_AMBUSH_FREE` 只清理自身，不打开窗口。
- `doUnequip`、`onThrow` 及实例失效路径统一调用 `clearCombatState()`。
- 不覆写 `storeInBundle` 保存这些短期字段，使真实读档自然清除连击状态。

- [ ] **步骤 6：运行面板与状态测试**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest"
```

预期：基础面板和状态机测试 PASS；毒效与伏击集成测试尚未加入。

- [ ] **步骤 7：提交武器骨架和状态机**

建议提交信息：`feat: 添加猛毒小镰刀追击状态机`。提交前确认未把共享文件中的用户差异一并暂存。

---

## 任务 4：以确定性测试驱动四类毒性效果

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickle.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickleTest.java`

- [ ] **步骤 1：编写公式和候选池失败测试**

不使用大样本概率统计；测试生产代码实际调用的确定性边界：

```java
assertEquals(0.20f, VenomousSickle.procChanceForLevel(0), 0f);
assertEquals(0.25f, VenomousSickle.procChanceForLevel(1), 0f);
assertEquals(0.70f, VenomousSickle.procChanceForLevel(10), 0f);
assertEquals(1.00f, VenomousSickle.procChanceForLevel(99), 0f);
assertEquals(0.20f, VenomousSickle.procChanceForLevel(-3), 0f);

assertEquals(10, VenomousSickle.poisonDuration(0));
assertEquals(15, VenomousSickle.corrosionDamage(10));
assertEquals(13, VenomousSickle.corrosionDuration(10));
assertEquals(20, VenomousSickle.oozeDuration(10));
assertEquals(36, VenomousSickle.corruptionDebuffDuration(10));
```

断言四个主类别权重相等；断言第四类恰好包含九个正权重效果，权重与 `WandOfCorruption` 一致，且不包含 `Corruption`、`Doom` 或任何 0 权重项。

- [ ] **步骤 2：编写实际附加与回退失败测试**

使用最小 `TestChar`：

- 验证中毒、酸蚀和淤泥的数值被正确设置。
- 验证第四类排除已有与免疫效果。
- 使用拒绝附加某一类负面 Buff 的测试角色，确认实现检查 `target.buff(selected) == applied`，附加失败后会继续候选和主类别回退。
- 全部负面 Buff 被拒绝时安全返回 `false`，无递归、死循环或假成功。
- `PURSUIT_FREE` 与 `FORCED_AMBUSH_FREE` 命中时不执行随机数供应器，证明免费攻击完全跳过毒效。

- [ ] **步骤 3：运行测试确认红灯**

运行定向 `VenomousSickleTest`，预期公式/候选池方法缺失。

- [ ] **步骤 4：实现确定性池与运行时随机选择**

在武器类中定义：

- `enum ToxicCategory { POISON, CORROSION, OOZE, CORRUPTION_DEBUFF }`。
- 四项等权主类别列表。
- `LinkedHashMap<Class<? extends FlavourBuff>, Float>` 或等价不可变正权重表。
- 接收随机浮点值/随机供应器的包可见选择方法，生产 `proc` 使用 `Random.Float()`。

运行时流程：先通过概率门槛，再对尚未尝试的主类别做无放回随机；类别内部候选附加失败时移除后重试。酸蚀调用：

```java
Buff.affect(defender, Corrosion.class)
        .set(3 + level, 5 + level, VenomousSickle.class);
```

第四类持续时间为 `6 + 3 * level`。每次附加后验证目标实际持有对应对象。

- [ ] **步骤 5：在 `proc` 中只允许普通命中触发**

先调用 `super.proc` 保留附魔和通用武器逻辑；只有 `activeAttackMode == NORMAL`、目标存活且本武器为实际攻击武器时才进行毒性判定。免费模式仍允许父类附魔照常运行，但跳过本武器专属毒性。

- [ ] **步骤 6：运行定向测试与静态检查**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest"
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickle.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickleTest.java
```

预期：PASS，无空白错误。

- [ ] **步骤 7：提交毒性效果**

建议提交信息：`feat: 添加猛毒小镰刀随机毒效`。

---

## 任务 5：实现“取消”与真正的原生伏击语义

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/ForcedSurpriseAttackTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickle.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickleTest.java`

- [ ] **步骤 1：编写武技窗口与充能失败测试**

覆盖：

- 没有完成自然追镰时 `canUseWeaponAbilityAction(hero)` 为 false。
- 完成 `PURSUIT_FREE` 后能力可用，基础消耗恰好为 1。
- 充能不足时不关闭窗口。
- 成功使用后 `cancelWindow` 关闭、`forcedAmbushArmed` 打开，英雄未消耗行动时间。
- 不选择目标，不打开 `CellSelector`。
- 下一次合法普通攻击使用 `FORCED_AMBUSH_FREE`；攻击后状态消费。
- 延长伏击不触发毒效、不追踪目标、不再次打开“取消”。

- [ ] **步骤 2：编写伏击语义失败测试**

在 `ForcedSurpriseAttackTest` 中构造已看见英雄、正常情况下不会被伏击的敌对 `Mob`，为武器预置 `FORCED_AMBUSH_FREE`，验证：

1. `MeleeWeapon.isForcedSurpriseAttack(hero, mob)` 为 true。
2. `mob.surprisedBy(hero)` 为 true，即使 `enemySeen == true` 且英雄未隐身。
3. `Char.hit(hero, mob, ...)` 必定返回 true，包括测试目标具有普通高闪避或临时无限闪避时。
4. `Char.hitMissIcon == FloatingText.HIT_SUPR`，不是普通必中、精准或祝福图标。
5. 命中后的 `Mob.defenseProc` 进入现有 `Surprise.hit` 路径；可通过统计计数、测试替身或可观察的伏击反馈断言。
6. 状态结算后 `mob.surprisedBy(hero)` 恢复正常结果。
7. 非法目标和 `isInvulnerable` 仍由攻击前置校验拒绝，强制伏击不绕过无敌。

- [ ] **步骤 3：运行测试确认红灯**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest" --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ForcedSurpriseAttackTest"
```

预期：FAIL，能力和强制伏击路径尚未实现。

- [ ] **步骤 4：实现“取消”**

`VenomousSickle`：

- 覆写 `canUseWeaponAbilityAction`，要求通用决斗家能力条件和 `cancelWindow` 同时成立。
- `targetingPrompt()` 返回 `null`。
- `baseChargeUse` 使用默认 1，或显式返回 1 以便测试。
- `duelistAbility` 二次检查窗口；有效时调用 `beforeAbilityUsed(hero, null)`，切换到 `forcedAmbushArmed`，调用 `afterAbilityUsed(hero)` 和 `updateQuickslot()`。
- 不调用 `hero.spend`、`spendAndNext` 或攻击动画，因此能力本身为零时间状态转换。

- [ ] **步骤 5：在 `Char.hit` 中实现真正的必中**

在普通精准/闪避随机比较和无限闪避短路之前查询：

```java
if (MeleeWeapon.isForcedSurpriseAttack(attacker, defender)) {
    hitMissIcon = FloatingText.getHitReasonIcon(
            attacker, INFINITE_ACCURACY, defender, 0);
    return true;
}
```

位置必须保证：

- 已执行基本空目标/不可攻击前置校验。
- “取消”的明确必定命中不被普通闪避或临时无限闪避覆盖。
- `FloatingText.getHitReasonIcon` 查询时武器的活动攻击状态尚未清除。

- [ ] **步骤 6：在 `Mob.surprisedBy` 中接入原生伏击**

在既有隐身/未发现判断之前加入通用强制伏击分支：

```java
if (MeleeWeapon.isForcedSurpriseAttack(enemy, this)) return true;
```

不要直接导入 `VenomousSickle`。这样 `defenseSkill`、伤害天赋、`FloatingText.HIT_SUPR`、`Statistics.sneakAttacks`、强击音效和 `Surprise.hit` 都沿用同一现有语义。

- [ ] **步骤 7：验证伏击与 busy/零延迟回归**

除定向测试外，增加一个完整攻击完成测试：播放攻击回调后英雄重新进入 ready 状态，`freeDelayPending` 被清除，第二次普通攻击恢复正常耗时。测试追镰和“取消”延长攻击各一次，防止类似连续突刺曾出现的 `busy` 卡死。

运行：

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest" --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ForcedSurpriseAttackTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeaponAttackLifecycleTest"
```

预期：全部 PASS。

- [ ] **步骤 8：提交能力与伏击链路**

共享的 `Char.java`、`Mob.java` 当前有其他任务差异；提交前必须审查暂存补丁。建议提交信息：`feat: 实现取消武技与强制伏击`。

---

## 任务 6：生成器、正式文本和 custom 资料集成

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java`
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java`
- 修改：`core/src/main/assets/messages/custom/custom.properties`
- 修改：`core/src/main/assets/messages/custom/custom_zh.properties`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickleTest.java`

- [ ] **步骤 1：编写集成失败测试**

断言：

- `Generator.Category.WEP_T6.classes` 包含且只包含一次 `VenomousSickle.class`。
- `classes.length == defaultProbs.length == probs.length`，新条目默认权重为 1。
- 中英文正式文本包含且只包含一次以下键：

```text
items.weapon.melee.tier6.venomoussickle.name
items.weapon.melee.tier6.venomoussickle.desc
items.weapon.melee.tier6.venomoussickle.stats_desc
items.weapon.melee.tier6.venomoussickle.ability_name
items.weapon.melee.tier6.venomoussickle.ability_desc
items.weapon.melee.tier6.venomoussickle.ability_unavailable
```

- custom 中英文包含 `custom.dict.dict.melee_venomoussickle` 与 `_d`。
- `DictionaryJournal.WEAPONS` 将该 key 映射到 `EXItemSpriteSheet.VENOMOUS_SICKLE`。

- [ ] **步骤 2：运行测试确认缺失集成**

运行定向 `VenomousSickleTest`，预期生成器、文本或图鉴断言失败。

- [ ] **步骤 3：追加生成器条目并保护现有十件六阶武器**

重新读取 `WEP_T6` 当前数组，在末尾追加 `VenomousSickle.class` 和一个权重 `1`。不得用计划中的快照重建数组，也不得删除实现期间新增的其他武器。

- [ ] **步骤 4：写入简短正式文本**

中文采用设计规格中的正式文案。英文等义。正式物品描述只表达“不太精准、随机毒效、移动追击”和“取消”的核心作用，不展开九种减益与完整公式。

- [ ] **步骤 5：写入详细 custom 资料**

custom 资料完整列出面板、概率、四类效果、移动资格、免费攻击限制、1 点能力消耗和原生伏击定义。用现有 `_下划线_` 强调风格，不把详细公式塞回正式物品描述。

- [ ] **步骤 6：运行文本、生成器和属性测试**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest"
```

预期：PASS，且 properties 无重复键和占位符。

- [ ] **步骤 7：提交集成**

建议提交信息：`feat: 集成猛毒小镰刀生成与文本`。如果共享文本或生成器已有未提交修改，只暂存本武器新增行。

---

## 任务 7：重绘并写入索引 155 的 16×16 贴图

**技能：** 此任务必须使用 `pixel-art-sprites`；视觉方向已确认，无需重新开启浏览器索引展示。

**文件：**

- 读取：`D:\桌面\猛毒小镰刀.png`
- 创建：`D:\STUDY\Dungeon\cache\venomous_sickle\render_venomous_sickle.py`
- 修改：`core/src/main/assets/sprites/ex_items.png`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

- [ ] **步骤 1：编写贴图常量与像素失败测试**

在当前测试基础上增加：

- `frameFor(VENOMOUS_SICKLE) == 155`。
- `frameWidth == 16`、`frameHeight == 16`。
- 索引 155 存在不透明像素，alpha 只允许 0/255。
- 不透明颜色数不超过 10。
- 非透明包围盒与最终 16×16 图稿一致，并覆盖可辨识的右上刃与左下柄。
- 剩余透明断言由 155–159 收窄为 156–159；所有重复空白断言同步更新。

- [ ] **步骤 2：运行 `EXItemSpriteSheetTest` 确认索引 155 为空**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest"
```

预期：FAIL，新常量或非透明像素断言尚未满足。

- [ ] **步骤 3：在项目外生成 16×16 像素稿**

脚本以 512×512 参考图为造型参考，手工定义/重绘硬边像素，不使用双线性或双三次缩放。调色板目标：

- 深紫黑轮廓。
- 深铁灰、中铁灰和冷色高光。
- 深紫、中紫、亮紫毒性附着物。
- 深褐握柄与一档暖色高光。

生成透明 16×16 PNG 和 16 倍最近邻预览，均存放在 `D:\STUDY\Dungeon\cache\venomous_sickle`。

- [ ] **步骤 4：视觉与逻辑自检**

检查：

- 1× 下首先读作短镰，而不是法杖、长剑或虫洞。
- 右上刃、左下柄方向与已确认方案一致。
- 紫色附着物附着于镰身，不遮断主要轮廓。
- 无半透明、抗锯齿、背景色或超出 16×16 的像素。

- [ ] **步骤 5：只写入目标单元格**

脚本必须：

1. 读取整张 `ex_items.png` 并保存写入前像素摘要。
2. 确认索引 155 在写入前透明；若非透明则停止并报告冲突。
3. 只清空并写入索引 155 的 16×16 区域。
4. 使用临时文件原子替换。
5. 逐像素断言索引 155 之外完全未变化。

在 `EXItemSpriteSheet` 的六阶近战武器区追加：

```java
public static final int VENOMOUS_SICKLE = encode(155, 16, 16);
```

- [ ] **步骤 6：运行贴图回归**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest"
```

预期：PASS；索引 156–159 保持透明，其他既有精灵断言不变。

- [ ] **步骤 7：提交贴图**

只提交 `ex_items.png`、常量和测试；项目外 cache 文件不进入仓库。建议提交信息：`art: 绘制猛毒小镰刀贴图`。

---

## 任务 8：综合回归、代码审查与交付

**文件：** 验证本计划涉及的全部生产与测试文件。

- [ ] **步骤 1：运行四个重点测试类**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickleTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeaponAttackLifecycleTest" --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ForcedSurpriseAttackTest" --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest"
```

预期：BUILD SUCCESSFUL。

- [ ] **步骤 2：运行相邻机制回归**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.PalermoSwordTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhipTest" --tests "com.shatteredpixel.shatteredpixeldungeon.items.GeneratorTest"
```

若 `GeneratorTest` 的实际类名不同，先定位仓库中当前生成器测试再运行，不创建虚假命令结果。

- [ ] **步骤 3：运行核心模块测试与编译**

```powershell
.\gradlew.bat core:test
.\gradlew.bat core:compileJava
```

预期：BUILD SUCCESSFUL。若被工作区中无关未完成改动阻塞，保留完整命令、首个错误和与本任务无关的证据，同时确保所有定向测试已通过。

- [ ] **步骤 4：执行静态与资源检查**

```powershell
git diff --check
git status --short
git diff --stat
```

另外确认：

- 无 `TODO`、`TBD`、临时调试日志或未替换占位文本。
- `VenomousSickle` 没有保存短期状态到 Bundle。
- 免费攻击的零延迟标志最终总会被清除。
- 强制伏击状态在 `Mob.defenseProc` 完成前保持，在攻击完成后消失。
- 任何免费攻击都不会重新触发本武器毒效或追击。
- `ex_items.png` 目标外像素哈希与写入前一致。

- [ ] **步骤 5：执行代码审查**

审查重点：

1. 是否真的复用了 `surprisedBy`/`Surprise.hit`，而不是只有命中率或图标。
2. 是否存在 `attackSkill()` 提前查询导致零延迟被吞掉的问题。
3. 命中、未命中、无敌和动画回调是否都会完成状态清理，避免 `busy`。
4. Buff 附加失败是否真实回退，避免把未附加对象当成功。
5. 共享脏文件中是否混入无关修改。

- [ ] **步骤 6：最终提交前审查暂存区**

```powershell
git diff --cached --check
git diff --cached --stat
git diff --cached
```

只有在暂存区完全属于本任务时才提交。若此前按任务分提交，此步骤只提交遗漏的本任务文件；建议最终提交信息：`feat: 实装六阶武器猛毒小镰刀`。

- [ ] **步骤 7：交付报告**

报告应简洁列出：

- 武器面板、毒效和追击/取消链已实现的结果。
- “伏击”已复用的原生链路与必定命中边界。
- 贴图索引 155 和项目外 cache 路径。
- 实际运行的测试命令及结果。
- 若存在无关测试阻塞，明确区分“本任务定向验证通过”和“全量验证被何处阻塞”。
