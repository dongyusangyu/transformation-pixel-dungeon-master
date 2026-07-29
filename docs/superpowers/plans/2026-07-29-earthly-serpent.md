# 尘世巨蟒实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在高塔等概率怪物池中加入具备酸雾、固定时间再生、近战成长、近战反毒和一次性拉拽爆炸的尘世巨蟒，并提供与原下水道巨蛇同轮廓的完整像素动画。

**架构：** `EarthlySerpent` 继承 `Mob`，以可单测的纯辅助方法承载成长、再生、来源判定与能力条件，以专属 `Hunting` 状态编排拉拽、近战和酸雾喷吐。延迟爆炸使用携带所有者 ID 的 `SerpentBomb`，固定时间再生使用 `Actor.now()` 时间差与持久化进度，精灵由确定性脚本直接从 `snake.png` 换色并补充逐像素细节。

**技术栈：** Java、JUnit 4、Shattered Pixel Dungeon Actor/Mob/Blob/Bomb API、PNG、Python Pillow、Gradle。

---

## 文件结构

### 新建

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpent.java`：数值、成长、反毒、再生、酸雾、拉拽、爆炸和存档。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/EarthlySerpentSprite.java`：基础与专属动画。
- `core/src/main/assets/sprites/earthly_serpent.png`：12×11 单帧、21 帧的精灵表。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpentTest.java`：怪物行为和状态测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/EarthlySerpentSpriteAssetTest.java`：资源结构测试。
- `tools/generate_earthly_serpent_sprite.py`：从 `snake.png` 生成最终资源。
- `tools/test_generate_earthly_serpent_sprite.py`：生成器测试。
- `docs/pixel-art/earthly-serpent/earthly_serpent_concept.png`：经确认的 A 方向概念参考。
- `docs/pixel-art/earthly-serpent/earthly_serpent_preview.png`：最近邻放大的验收预览。

### 修改

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`：新增资源常量。
- `core/src/main/assets/messages/actors/actors.properties`：英文名称、描述和发现提示。
- `core/src/main/assets/messages/actors/actors_zh.properties`：中文名称、描述和发现提示。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`：注册到 `TOWER_MOBS`。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerMobRules.java`：增加等概率枚举项。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`：实例化尘世巨蟒。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevelCamouflageGnollTest.java`：更新四等分边界测试。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/TowerMobPlacer.java`：加入测试放置器。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/TowerMobPlacerTest.java`：验证测试放置器。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`：验证图鉴接入。

## 任务 1：建立基础数值与二距近战

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpentTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpent.java`

- [ ] **步骤 1：编写基础数值与攻击距离失败测试**

在 `EarthlySerpentTest` 中建立可访问保护方法的测试子类：

```java
private static final class TestSerpent extends EarthlySerpent {
    boolean canStrike(Char target) {
        return canMeleeAttack(target);
    }
}
```

增加独立测试：

```java
@Test
public void baseStatsMatchSpecification() {
    TestSerpent serpent = new TestSerpent();
    assertEquals(240, serpent.HT);
    assertEquals(240, serpent.HP);
    assertEquals(20, serpent.defenseSkill);
    assertEquals(40, serpent.attackSkill(null));
    assertEquals(13, serpent.EXP);
    assertEquals(30, serpent.maxLvl);
    assertEquals(0.5f, serpent.attackDelay(), 0.0001f);
}

@Test
public void damageAndArmorStayInsideBaseRanges() {
    TestSerpent serpent = new TestSerpent();
    for (int i = 0; i < 500; i++) {
        assertTrue(serpent.damageRoll() >= 24 && serpent.damageRoll() <= 36);
        assertTrue(serpent.drRoll() >= 8 && serpent.drRoll() <= 16);
    }
}
```

使用测试 `Level` 初始化直线地图，分别验证距离 1、2、3 以及二格墙体阻挡：

```java
assertTrue(serpent.canStrike(targetAtDistanceOne));
assertTrue(serpent.canStrike(targetAtDistanceTwo));
assertFalse(serpent.canStrike(targetAtDistanceThree));
assertFalse(serpent.canStrike(targetBehindWall));
```

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report core:test --tests "*EarthlySerpentTest"
```

预期：编译失败，提示 `EarthlySerpent` 不存在。

- [ ] **步骤 3：编写最小基础类**

实现字段与纯方法：

```java
public class EarthlySerpent extends Mob {
    private static final int BASE_HT = 240;
    private static final float BASE_ATTACK_DELAY = 0.5f;

    {
        HP = HT = BASE_HT;
        defenseSkill = 20;
        EXP = 13;
        maxLvl = 30;
        loot = null;
        lootChance = 0f;
        immunities.add(CorrosiveGas.class);
        immunities.add(Corrosion.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(24, 36);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(8, 16);
    }

    @Override
    public float attackDelay() {
        return super.attackDelay() * BASE_ATTACK_DELAY;
    }

    protected boolean canMeleeAttack(Char target) {
        if (Dungeon.level == null || target == null
                || Dungeon.level.distance(pos, target.pos) > 2) {
            return false;
        }
        Ballistica path = new Ballistica(pos, target.pos, Ballistica.PROJECTILE);
        return path.collisionPos == target.pos;
    }
}
```

不要叠加 `super.drRoll()`，最终护甲区间必须严格为 8–16。

- [ ] **步骤 4：运行测试并确认绿灯**

运行同一步骤 2；预期：基础数值与二距攻击测试全部通过。

- [ ] **步骤 5：提交独立新增文件**

仅在暂存区不包含其他工作时运行：

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpent.java
git add core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpentTest.java
git diff --cached --check
git commit -m "feat: 添加尘世巨蟒基础数值"
```

## 任务 2：实现成长、固定时间再生与近战反毒

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpentTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpent.java`

- [ ] **步骤 1：编写成长失败测试**

测试子类公开：

```java
void growBy(int damage) {
    recordMeleeDamage(damage);
}

float growth() {
    return growthMultiplier();
}
```

分别验证：

```java
@Test
public void meleeDamageGrowsCombatStatsAndMaxHealth() {
    TestSerpent serpent = new TestSerpent();
    serpent.HP = 100;
    serpent.growBy(30);
    assertEquals(1.30f, serpent.growth(), 0.0001f);
    assertEquals(300, serpent.HT);
    assertEquals(100, serpent.HP);
    assertEquals(1.30f, serpent.speed(), 0.0001f);
    assertEquals(0.5f / 1.30f, serpent.attackDelay(), 0.0001f);
}

@Test
public void growthCapsAtTwiceBaseStats() {
    TestSerpent serpent = new TestSerpent();
    serpent.growBy(500);
    assertEquals(2f, serpent.growth(), 0.0001f);
    assertEquals(480, serpent.HT);
    assertEquals(2f, serpent.speed(), 0.0001f);
    assertEquals(0.25f, serpent.attackDelay(), 0.0001f);
}
```

为随机伤害测试使用固定测试钩子 `scaledDamage(int baseDamage)`，断言基础 24/36 在满成长时变为 48/72。

- [ ] **步骤 2：运行成长测试并确认红灯**

运行专项测试；预期：找不到成长方法或断言仍为基础值。

- [ ] **步骤 3：实现成长**

使用累计实际近战伤害：

```java
private int meleeDamageDealt;

protected float growthMultiplier() {
    return 1f + Math.min(meleeDamageDealt, 100) / 100f;
}

protected void recordMeleeDamage(int actualDamage) {
    if (actualDamage <= 0) return;
    meleeDamageDealt += actualDamage;
    HT = Math.min(BASE_HT * 2, HT + actualDamage * 2);
}

protected int scaledDamage(int baseDamage) {
    return Math.round(baseDamage * growthMultiplier());
}

@Override
public float speed() {
    return super.speed() * growthMultiplier();
}

@Override
public float attackDelay() {
    return super.attackDelay() * BASE_ATTACK_DELAY / growthMultiplier();
}
```

覆盖直接攻击并比较目标 HP 前后：

```java
@Override
public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
        DamageTag... damageTags) {
    int before = enemy.HP;
    boolean hit = super.attack(enemy, dmgMulti, dmgBonus, accMulti, damageTags);
    if (hit && canMeleeAttack(enemy)) {
        recordMeleeDamage(Math.max(0, before - enemy.HP));
    }
    return hit;
}
```

- [ ] **步骤 4：编写固定时间再生失败测试**

公开 `advanceRegeneration(float elapsed)`，测试：

```java
serpent.HT = 300;
serpent.HP = 100;
serpent.advanceRegen(0.5f);
assertEquals(100, serpent.HP);
serpent.advanceRegen(0.5f);
assertEquals(130, serpent.HP);
serpent.advanceRegen(2f);
assertEquals(190, serpent.HP);
```

再将成长调至 2 倍，重复两个 0.5 时间片，仍只回复一次。

- [ ] **步骤 5：运行再生测试并确认红灯**

预期：找不到再生方法。

- [ ] **步骤 6：实现固定时间再生**

```java
private float regenerationProgress;
private float lastObservedTime = Float.NaN;

protected void advanceRegeneration(float elapsed) {
    if (elapsed <= 0) return;
    regenerationProgress += elapsed;
    while (regenerationProgress >= 1f) {
        heal((int)Math.ceil(HT * 0.10f));
        regenerationProgress -= 1f;
    }
}

@Override
protected boolean act() {
    float now = Actor.now();
    if (!Float.isNaN(lastObservedTime)) {
        advanceRegeneration(now - lastObservedTime);
    }
    lastObservedTime = now;
    return super.act();
}
```

不要以“每次 `act()` 直接治疗”代替时间累计。

- [ ] **步骤 7：编写反毒来源与实际伤害失败测试**

使用真实 `Hero`、`Mob`、`Poison` 和地图：

- `source == Dungeon.hero` 且 `hero.belongings.attackingWeapon()` 不是 `MissileWeapon`：触发。
- 玩家投掷武器攻击期间 `attackingWeapon()` 为 `MissileWeapon`：不触发。
- 相邻 `Mob` 来源：触发。
- 距离大于 1 的 `Mob` 来源：不触发。
- 腐蚀、毒、燃烧和非 `Char` 来源：不触发。
- 传入伤害被护盾完全吸收，HP 不变：不触发。
- 实际损失 17 HP：玩家获得或延长 17 回合 `Poison`。

- [ ] **步骤 8：运行反毒测试并确认红灯**

预期：玩家没有 `Poison`。

- [ ] **步骤 9：实现反毒**

```java
@Override
public void damage(int damage, Object source, DamageTag... damageTags) {
    int before = HP;
    super.damage(damage, source, damageTags);
    int lost = Math.max(0, before - HP);
    if (lost > 0 && isRetaliatoryMelee(source)) {
        applyPoisonToHero(lost);
    }
}

protected boolean isRetaliatoryMelee(Object source) {
    if (source == Dungeon.hero) {
        return !(Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon);
    }
    return source instanceof Mob
            && Dungeon.level != null
            && Dungeon.level.adjacent(pos, ((Mob)source).pos);
}
```

`applyPoisonToHero` 必须对已有毒使用 `extend(lost)`，新毒使用 `set(lost)`，并保护 `Dungeon.hero == null`。

- [ ] **步骤 10：实现成长与再生存档测试及代码**

先写 Bundle 往返失败测试，再持久化：

```java
private static final String MELEE_DAMAGE_DEALT = "melee_damage_dealt";
private static final String REGENERATION_PROGRESS = "regeneration_progress";
```

恢复时从 `meleeDamageDealt` 重算 `HT`，并把 `lastObservedTime` 重置为 `Float.NaN`，防止加载瞬间重复计算离线时间。

- [ ] **步骤 11：运行专项测试并提交**

运行 `*EarthlySerpentTest`；预期全部通过。只暂存本任务两个文件并检查 staged diff 后提交：

```powershell
git commit -m "feat: 实现尘世巨蟒成长与反毒"
```

## 任务 3：实现酸雾、拉拽与延迟爆炸

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpentTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpent.java`

- [ ] **步骤 1：编写酸雾失败测试**

通过测试子类公开 `emitAura()` 与 `spitAt(int cell)`，初始化 `Dungeon.level.blobs`，断言：

- 周身酸雾包含自身格和可通行邻格。
- 周身 `CorrosiveGas` 的 strength 为 1。
- 远程目标格与邻格被播种，strength 为 8。
- 墙格和越界格不播种。
- 巨蟒对 `CorrosiveGas` 和 `Corrosion` 免疫。

如果 `CorrosiveGas.strength` 无公开读取器，不为生产类增加仅测试 API；通过让真实 Blob 演化并检查测试角色获得的 `Corrosion` 初始伤害，或通过反射读取既有私有字段。

- [ ] **步骤 2：运行酸雾测试并确认红灯**

预期：Blob 不存在或强度不符。

- [ ] **步骤 3：实现酸雾**

```java
protected void emitAura() {
    seedCorrosiveGas(pos, 5, 1);
    for (int offset : PathFinder.NEIGHBOURS8) {
        int cell = pos + offset;
        if (validOpenCell(cell)) seedCorrosiveGas(cell, 2, 1);
    }
}

protected void spitAt(int target) {
    seedCorrosiveGas(target, 15, 8);
    for (int offset : PathFinder.NEIGHBOURS8) {
        int cell = target + offset;
        if (validOpenCell(cell)) seedCorrosiveGas(cell, 5, 8);
    }
}
```

使用：

```java
GameScene.add(Blob.seed(cell, volume, CorrosiveGas.class)
        .setStrength(strength, EarthlySerpent.class));
```

测试环境无活动场景时，遵循现有测试方式直接把 Blob 放入 `Dungeon.level.blobs`，不要吞掉生产路径。

- [ ] **步骤 4：编写拉拽条件与落点失败测试**

测试：

- 距离 1、2：`canPull` 为 false。
- 距离 3、4：存在路径和空邻格时为 true。
- 距离 5：false。
- 玩家具有 `IMMOVABLE`、墙体阻挡、邻格全部占用：false。
- 无合法落点时 `pullUsed` 保持 false。
- 成功时玩家落在巨蟒邻格、`pullUsed == true`、`pendingExplosionCell` 等于落点。

- [ ] **步骤 5：运行拉拽测试并确认红灯**

预期：拉拽状态不存在。

- [ ] **步骤 6：实现拉拽**

字段：

```java
private boolean pullUsed;
private int pendingExplosionCell = -1;
```

使用 `Ballistica` 验证路径，从 `PathFinder.NEIGHBOURS8` 中选择可通行、开放、未占用且与原拉拽线路最接近的格。成功后使用 `Pushing` 动画移动玩家，在回调中设置 `hero.pos`、调用 `hero.interrupt()`、`Dungeon.observe()` 和 `GameScene.updateFog()`。

拉拽动画可见时让 `EarthlySerpentSprite.pull(hero.pos)` 返回异步动作；不可见时立即完成逻辑。

- [ ] **步骤 7：编写延迟固定格爆炸失败测试**

测试：

- 成功拉拽后第一次能力行动只拉拽，不爆炸。
- 巨蟒下一次行动在旧落点爆炸，即使玩家已经移动。
- 爆炸清除 `pendingExplosionCell`。
- 同一个巨蟒只拉拽一次。
- `SerpentBomb` 对所有者造成 0 伤害，但对另一只巨蟒和其他角色正常造成伤害。
- 死亡时清除待爆炸格。

将炸弹的实际范围与地形破坏交给 `Bomb.explode`；测试至少使用一个范围内角色和一个可破坏地形。

- [ ] **步骤 8：运行爆炸测试并确认红灯**

预期：没有爆炸或所有者受到伤害。

- [ ] **步骤 9：实现所有者炸弹**

```java
static final class SerpentBomb extends Bomb {
    private final int ownerId;

    SerpentBomb(int ownerId) {
        this.ownerId = ownerId;
    }

    boolean ownedBy(EarthlySerpent serpent) {
        return ownerId == serpent.id();
    }
}
```

在 `damage` 最前面识别自己的 `SerpentBomb` 并把伤害改为 0，仍让父类处理 0 伤害显示。引爆：

```java
new SerpentBomb(id()).explode(pendingExplosionCell);
pendingExplosionCell = -1;
spend(attackDelay());
```

- [ ] **步骤 10：编写并实现状态机**

先测试优先级：

1. 待爆炸。
2. 3–4 格拉拽。
3. 1–2 格近战。
4. 超过 2 格喷吐。
5. 普通追踪。

实现 `Hunting extends Mob.Hunting`，并令 `doAttack` 在 `canMeleeAttack` 时调用父类近战，否则播放喷吐动画并在 `onZapComplete()` 调用 `spitAt(enemy.pos)`。所有攻击行动使用成长后的 `attackDelay()`。

- [ ] **步骤 11：编写并实现拉拽/爆炸存档**

先写 Bundle 往返失败测试，再保存：

```java
private static final String PULL_USED = "pull_used";
private static final String PENDING_EXPLOSION_CELL = "pending_explosion_cell";
```

- [ ] **步骤 12：运行专项测试并提交**

运行 `*EarthlySerpentTest`；预期全部通过。检查 staged diff 后提交：

```powershell
git commit -m "feat: 实现尘世巨蟒酸雾与拉拽爆炸"
```

## 任务 4：接入高塔生成、图鉴、文本与测试放置器

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerMobRules.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/TowerMobPlacer.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevelCamouflageGnollTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/TowerMobPlacerTest.java`

- [ ] **步骤 1：更新等概率规则失败测试**

将三等分测试改为四等分：

```java
assertSame(CAMOUFLAGE_GNOLL, select(0f));
assertSame(CAMOUFLAGE_GNOLL, select(Math.nextDown(0.25f)));
assertSame(CORROSIVE_SWARM, select(0.25f));
assertSame(CORPSE, select(0.5f));
assertSame(EARTHLY_SERPENT, select(0.75f));
assertSame(EARTHLY_SERPENT, select(Math.nextDown(1f)));
assertEquals(4, Selection.values().length);
```

在图鉴和测试放置器测试中断言包含 `EarthlySerpent.class`。

- [ ] **步骤 2：运行三个接入测试并确认红灯**

```powershell
.\gradlew.bat --no-daemon --no-problems-report core:test --tests "*TowerLevelCamouflageGnollTest" --tests "*TowerBestiaryCategoriesTest" --tests "*TowerMobPlacerTest"
```

预期：缺少 `EARTHLY_SERPENT` 或对应类未注册。

- [ ] **步骤 3：完成 Java 接入**

- `TowerMobRules.Selection` 末尾加入 `EARTHLY_SERPENT`。
- `TowerLevel.createMob()` 新增 `return new EarthlySerpent()` 分支。
- 将图鉴注册改为
  `TOWER_MOBS.addEntities(CamouflageGnoll.class, CorrosiveSwarm.class, Corpse.class, EarthlySerpent.class)`。
- `TowerMobPlacer` 按现有 API 加入尘世巨蟒。

保持 `select` 的 `roll * selections.length` 算法不变，从而自动实现四等分。

- [ ] **步骤 4：增加中英文文本**

```properties
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.earthlyserpent.name=earthly serpent
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.earthlyserpent.desc=Heavy earthen scales cover pulsing acid sacs. This serpent constantly vents corrosive mist, grows stronger through melee damage, and drags distant prey onto a marked blast point.
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.earthlyserpent.discover_hint=This creature lurks in the depths of the tower.
```

```properties
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.earthlyserpent.name=尘世巨蟒
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.earthlyserpent.desc=厚重的土色鳞片下鼓动着酸液囊。它会不断散发酸雾，以近战伤害强化自身，并把远处的猎物拖到预定爆点。
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.earthlyserpent.discover_hint=这种生物出没于高塔深处。
```

- [ ] **步骤 5：运行接入测试并确认绿灯**

运行步骤 2；预期全部通过。

- [ ] **步骤 6：谨慎提交共享文件**

这些文件当前可能包含用户的其他未提交修改。逐文件检查 `git diff`；只有能保证 staged diff 只包含本任务改动时才提交，否则保留未提交并在交付时说明。

## 任务 5：制作确定性像素素材

**文件：**
- 创建：`tools/test_generate_earthly_serpent_sprite.py`
- 创建：`tools/generate_earthly_serpent_sprite.py`
- 创建：`core/src/main/assets/sprites/earthly_serpent.png`
- 创建：`docs/pixel-art/earthly-serpent/earthly_serpent_concept.png`
- 创建：`docs/pixel-art/earthly-serpent/earthly_serpent_preview.png`

- [ ] **步骤 1：生成 A 方向概念参考**

使用 `imagegen` 编辑现有绝对路径：

```text
core/src/main/assets/sprites/snake.png
```

提示词必须要求：保持原精灵表逐帧轮廓和动作，只做暗土褐/黄铜褐/沙金/黄绿色酸液换色，并给喉囊与背鳞提供像素细节参考；禁止改变帧尺寸和身体比例。概念图只作为配色与细节参考，不直接作为游戏资源。

- [ ] **步骤 2：编写生成器失败测试**

测试生成器必须：

- 输入 `snake.png` 为 256×16。
- 输出 `earthly_serpent.png` 为 256×16。
- 保留帧 0–13 的非透明像素包围盒，允许局部外扩最多 1px。
- 帧 14–20 非空。
- 可见像素 alpha 只能为 255。
- 透明像素 RGB 为 0。
- 使用不超过 12 个可见颜色。
- 每个 12×11 帧不越界。

运行：

```powershell
python tools/test_generate_earthly_serpent_sprite.py
```

预期：失败，提示生成器不存在。

- [ ] **步骤 3：编写最小生成器**

使用 Pillow 逐色替换原图七色调色板：

```python
PALETTE = {
    (56, 104, 0): (43, 33, 25),
    (103, 181, 0): (101, 80, 46),
    (123, 217, 0): (146, 116, 58),
    (204, 183, 81): (185, 155, 77),
    (255, 229, 101): (221, 242, 106),
    (255, 0, 0): (229, 96, 58),
    (0, 0, 0): (0, 0, 0),
}
```

逐帧手工坐标表添加背鳞、喉囊和酸滴，不能使用缩放、旋转、模糊、抗锯齿或半透明。帧 14–16 从原攻击帧派生喷吐姿态；帧 17–20 从待机/攻击帧派生盘绕、牵引、释放和警示姿态。

- [ ] **步骤 4：运行生成器测试并生成资源**

```powershell
python tools/test_generate_earthly_serpent_sprite.py
python tools/generate_earthly_serpent_sprite.py
```

预期：测试通过并写出游戏资源及 8× 最近邻预览。

- [ ] **步骤 5：人工检查素材**

在 1× 和 8× 下确认：

- 仍能立即认出原版下水道巨蛇。
- 土褐主体与黄绿色酸液区分清楚。
- 帧 14–16 喉囊按“收缩—膨胀—喷吐”变化。
- 帧 17–20 拉拽方向一致，死亡帧不回升。
- 无枕式阴影、光晕、半透明边缘和非整数缩放。

- [ ] **步骤 6：提交新增素材**

只暂存本任务新增的脚本、测试、PNG 和预览并提交：

```powershell
git commit -m "art: 制作尘世巨蟒像素动画"
```

## 任务 6：实现精灵类与资源常量

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/EarthlySerpentSpriteAssetTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/EarthlySerpentSprite.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpent.java`

- [ ] **步骤 1：编写资源与动画失败测试**

断言：

```java
assertEquals("sprites/earthly_serpent.png", Assets.Sprites.EARTHLY_SERPENT);
assertEquals(EarthlySerpentSprite.class, new EarthlySerpent().spriteClass);
```

读取 PNG 并验证 256×16、12×11 帧 0–20 非空、alpha 和颜色数。

- [ ] **步骤 2：运行精灵测试并确认红灯**

```powershell
.\gradlew.bat --no-daemon --no-problems-report core:test --tests "*EarthlySerpentSpriteAssetTest"
```

预期：资源常量或精灵类不存在。

- [ ] **步骤 3：实现资源常量和动画**

`Assets.Sprites`：

```java
public static final String EARTHLY_SERPENT = "sprites/earthly_serpent.png";
```

`EarthlySerpentSprite`：

```java
TextureFilm frames = new TextureFilm(texture, 12, 11);

idle.frames(frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 1, 1);
run.frames(frames, 4, 5, 6, 7);
attack.frames(frames, 8, 9, 10, 9, 0);
die.frames(frames, 11, 12, 13);
spit.frames(frames, 14, 15, 16, 0);
pull.frames(frames, 17, 18, 19, 20, 0);
warning.frames(frames, 20, 19);
```

提供 `spit(int cell)`、`pull(int cell)` 和 `warning(boolean active)`。在 `onComplete` 中调用角色的 `onZapComplete` / `onPullComplete`，不要让动画结束后重复结算能力。

- [ ] **步骤 4：运行精灵测试并确认绿灯**

运行步骤 2；预期全部通过。

- [ ] **步骤 5：检查共享文件并提交**

`Assets.java` 当前可能有其他未提交改动。先检查 staged diff，不能隔离时不提交共享文件；新增精灵类和测试可以独立提交。

## 任务 7：整体验证与清理

**文件：**
- 修改：`docs/superpowers/plans/2026-07-29-earthly-serpent.md`（完成勾选）

- [ ] **步骤 1：运行素材验证**

```powershell
python tools/test_generate_earthly_serpent_sprite.py
```

预期：全部通过。

- [ ] **步骤 2：运行尘世巨蟒专项测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:test --tests "*EarthlySerpent*"
```

预期：0 failures。

- [ ] **步骤 3：运行高塔接入测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:test --tests "*TowerMob*" --tests "*TowerBestiaryCategoriesTest"
```

预期：0 failures。

- [ ] **步骤 4：编译核心模块**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:compileJava
```

预期：`BUILD SUCCESSFUL`。

- [ ] **步骤 5：运行完整核心测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:test
```

记录测试总数、失败数和失败名称；只有新增或相关失败必须修复，既有无关失败需明确报告。

- [ ] **步骤 6：检查差异**

```powershell
git diff --check
git status --short
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/EarthlySerpent.java
```

确认没有空白错误、临时文件、意外二进制资源或其他任务的改动。

- [ ] **步骤 7：更新计划状态**

把本计划中实际完成的复选框改为 `[x]`。未执行的提交步骤若因共享脏工作区而跳过，保留未勾选并在最终交付中说明，不得虚报。
