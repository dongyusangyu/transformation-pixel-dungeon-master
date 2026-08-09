# 烤全羊术士实现计划

> **面向 AI 代理的工作者：** 必须使用 `executing-plans` 在当前会话中逐任务执行本计划；每一步按复选框更新，并严格遵循红—绿—重构。

**目标：** 加入只在高塔生成的烤全羊术士，使每个术士能独立围困每个目标一次，之后持续施放二充能炎浪，并完成专属像素精灵、图鉴和生成接入。

**架构：** 新怪物实现 `MagicalRangedAttack`，在 `doAttack` 中先判断术士自身的 `flockedTargetIds`；未记录时执行羊群围困，已记录时交由标准近战/远程分派。炎浪在怪物类内复刻固定二充能锥形规则，精灵类只承担施法动画和回调，不修改原版 `Warlock`、`Sheep` 或 `WandOfFireblast`。

**技术栈：** Java、Shattered Pixel Dungeon Actor/Mob/Blob/ConeAOE、JUnit 4、Python Pillow、PNG 像素精灵。

---

## 文件结构

**创建：**

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java`：数值、独立围困记录、羊群召唤、炎浪逻辑和存档。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/RoastLambWarlockSprite.java`：矮人术士帧布局、羊群/炎浪施法动画和回调。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlockTest.java`：数值、围困、独立记录、存档和炎浪行为测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/RoastLambWarlockSpriteAssetTest.java`：资源、帧、调色板和轮廓测试。
- `tools/generate_roast_lamb_warlock_sprite.py`：从 `warlock.png` 生成确定性的改色素材和预览。
- `tools/test_generate_roast_lamb_warlock_sprite.py`：生成器测试。
- `tools/roast_lamb_warlock_test.init.gradle`：隔离本功能相关测试，避免脏工作区的无关测试源阻塞。
- `core/src/main/assets/sprites/roast_lamb_warlock.png`：256×16 精灵表。
- `docs/pixel-art/roast-lamb-warlock/roast_lamb_warlock_concept.png`：美术概念参考。
- `docs/pixel-art/roast-lamb-warlock/roast_lamb_warlock_preview.png`：8 倍最近邻预览。

**修改：**

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`：注册精灵路径。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerMobRules.java`：加入第五个等概率枚举值。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`：创建新怪物。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`：加入高塔生物图鉴。
- `core/src/main/assets/messages/actors/actors.properties`：英文名称、描述和提示。
- `core/src/main/assets/messages/actors/actors_zh.properties`：中文名称、描述和提示。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevelCamouflageGnollTest.java`：验证五等分边界。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`：验证图鉴、经验上限和文本。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/TowerMobPlacerTest.java`：验证测试放置器自动包含新怪物。

## 任务 1：基础怪物与攻击分派

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlockTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java`
- 创建：`tools/roast_lamb_warlock_test.init.gradle`

- [ ] **步骤 1：编写基础数值和攻击类型测试**

```java
@Test
public void baseStatsMatchSpecification() {
    TestWarlock warlock = new TestWarlock();
    assertEquals(150, warlock.HT);
    assertEquals(150, warlock.HP);
    assertEquals(20, warlock.defenseSkill);
    assertEquals(40, warlock.attackSkill(null));
    assertEquals(13, warlock.EXP);
    assertEquals(30, warlock.maxLvl);
    assertEquals(RangedAttack.Type.RANGED_MAGIC, warlock.rangedAttackType());
    assertEquals(0f, warlock.lootChance(), 0f);
}

@Test
public void meleeDamageAndArmorStayInsideRanges() {
    RoastLambWarlock warlock = new RoastLambWarlock();
    for (int i = 0; i < 500; i++) {
        int damage = warlock.damageRoll();
        int armor = warlock.drRoll();
        assertTrue(damage >= 0 && damage <= 20);
        assertTrue(armor >= 0 && armor <= 10);
    }
}
```

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat -I tools/roast_lamb_warlock_test.init.gradle --no-daemon --no-problems-report core:test --tests "*RoastLambWarlockTest"
```

预期：编译失败，提示 `RoastLambWarlock` 不存在。

- [ ] **步骤 3：实现最小基础类**

```java
public class RoastLambWarlock extends Mob implements MagicalRangedAttack {
    {
        HP = HT = 150;
        defenseSkill = 20;
        EXP = 13;
        maxLvl = 30;
        loot = null;
        lootChance = 0f;
    }

    @Override public int damageRoll() { return Random.NormalIntRange(0, 20); }
    @Override public int attackSkill(Char target) { return 40; }
    @Override public int drRoll() { return Random.NormalIntRange(0, 10); }
    @Override public float lootChance() { return 0f; }
    @Override public boolean doRangedAttack(Char target) { return false; }
}
```

- [ ] **步骤 4：运行测试确认绿灯**

运行同上；预期：基础测试全部通过。

- [ ] **步骤 5：提交独立检查点**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlockTest.java tools/roast_lamb_warlock_test.init.gradle
git commit -m "feat: 添加烤全羊术士基础数值"
```

## 任务 2：每个术士独立的羊群围困记录

**文件：**

- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlockTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java`

- [ ] **步骤 1：编写独立记录、合法格召唤和存档测试**

```java
@Test
public void eachWarlockCanFlockTheSameTargetOnce() {
    TestWarlock first = new TestWarlock();
    TestWarlock second = new TestWarlock();
    Gnoll target = new Gnoll();

    assertTrue(first.needsFlockForTest(target));
    first.markFlockedForTest(target);
    assertFalse(first.needsFlockForTest(target));
    assertTrue(second.needsFlockForTest(target));
}

@Test
public void flockRecordSurvivesSaveAndLoad() {
    TestWarlock original = new TestWarlock();
    Gnoll target = new Gnoll();
    original.markFlockedForTest(target);
    Bundle bundle = new Bundle();
    original.storeInBundle(bundle);

    TestWarlock restored = new TestWarlock();
    restored.restoreFromBundle(bundle);
    assertFalse(restored.needsFlockForTest(target));
}

@Test
public void flockUsesOnlyEmptyAdjacentNonPitCellsAndMarksEvenWhenBlocked() {
    TestLevel level = openLevel(7, 7);
    Dungeon.level = level;
    TestWarlock warlock = new TestWarlock();
    Gnoll target = new Gnoll();
    target.pos = 24;
    level.pit[17] = true;
    level.solid[18] = true;

    assertEquals(6, warlock.flockForTest(target));
    assertFalse(warlock.needsFlockForTest(target));
}
```

- [ ] **步骤 2：运行测试确认因缺少集合与围困方法失败**

运行任务 1 命令；预期：`needsFlockForTest`、`performFlock` 等符号不存在。

- [ ] **步骤 3：实现围困集合、召唤和存档**

```java
private static final String FLOCKED_TARGET_IDS = "flocked_target_ids";
private final HashSet<Integer> flockedTargetIds = new HashSet<>();

protected boolean needsFlock(Char target) {
    return target != null && !flockedTargetIds.contains(target.id());
}

protected int performFlock(Char target) {
    flockedTargetIds.add(target.id());
    int summoned = 0;
    for (int offset : PathFinder.NEIGHBOURS8) {
        int cell = target.pos + offset;
        if (validSheepCell(cell)) {
            Sheep sheep = new Sheep();
            sheep.initialize(6);
            sheep.pos = cell;
            GameScene.add(sheep);
            Dungeon.level.occupyCell(sheep);
            CellEmitter.get(cell).burst(Speck.factory(Speck.WOOL), 4);
            summoned++;
        }
    }
    Sample.INSTANCE.play(Assets.Sounds.PUFF);
    Sample.INSTANCE.play(Assets.Sounds.SHEEP);
    return summoned;
}

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(FLOCKED_TARGET_IDS,
            flockedTargetIds.stream().mapToInt(Integer::intValue).toArray());
}
```

`restoreFromBundle` 清空集合后读取 `bundle.getIntArray(FLOCKED_TARGET_IDS)`；缺失键按空集合处理。`validSheepCell` 必须同时检查 `insideMap`、`!solid`、`!pit` 和 `Actor.findChar(cell) == null`。

- [ ] **步骤 4：将围困置于所有攻击之前**

```java
@Override
protected boolean doAttack(Char target) {
    if (needsFlock(target)) {
        performFlock(target);
        spend(attackDelay());
        return true;
    }
    return super.doAttack(target);
}
```

补充测试：首次近战和首次远程都进入围困分支，且不会调用普通攻击或炎浪。

- [ ] **步骤 5：运行测试确认绿灯并提交**

运行任务 1 命令；预期：全部通过。

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlockTest.java
git commit -m "feat: 实现烤全羊术士独立围困"
```

## 任务 3：固定二充能炎浪

**文件：**

- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlockTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java`

- [ ] **步骤 1：编写锥形参数、伤害和状态测试**

```java
@Test
public void fireblastUsesFixedTwoChargeGeometryAndDamage() {
    TestWarlock warlock = preparedWarlockOnOpenLevel();
    assertEquals(7, warlock.fireblastDistanceForTest());
    assertEquals(70, warlock.fireblastAngleForTest());
    for (int i = 0; i < 500; i++) {
        int damage = warlock.magicDamageForTest();
        assertTrue(damage >= 15 && damage <= 35);
    }
}

@Test
public void fireblastBurnsAndCripplesAffectedTargetButNotCaster() {
    TestWarlock warlock = preparedWarlockOnOpenLevel();
    Gnoll target = addTargetInsideCone();
    warlock.castFireblastForTest(target.pos);

    assertTrue(target.HP < target.HT);
    assertNotNull(target.buff(Burning.class));
    assertNotNull(target.buff(Cripple.class));
    assertNull(warlock.buff(Burning.class));
    assertTrue(Blob.volumeAt(target.pos, Fire.class) > 0);
}
```

- [ ] **步骤 2：运行测试确认红灯**

运行任务 1 命令；预期：固定炎浪 API 不存在。

- [ ] **步骤 3：实现固定二充能锥形逻辑**

```java
private static final int FIREBLAST_DISTANCE = 7;
private static final int FIREBLAST_ANGLE = 70;
private static final int FIRE_VOLUME = 3;

protected ConeAOE fireCone(int targetCell) {
    Ballistica aim = new Ballistica(pos, targetCell,
            Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
    return new ConeAOE(aim, FIREBLAST_DISTANCE, FIREBLAST_ANGLE,
            Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
}

protected void castFireblast(int targetCell) {
    ConeAOE cone = fireCone(targetCell);
    for (int cell : cone.cells) {
        if (cell == pos) continue;
        seedFireAndOpenDoors(cell);
        Char affected = Actor.findChar(cell);
        if (affected != null) {
            affected.damage(Random.NormalIntRange(15, 35), this, DamageTag.MAGICAL);
            if (affected.isAlive()) {
                Buff.affect(affected, Burning.class).reignite(affected);
                Buff.affect(affected, Cripple.class, 4f);
            }
        }
    }
}
```

`seedFireAndOpenDoors` 对门执行 `Level.set(..., Terrain.OPEN_DOOR)` 和 `GameScene.updateMap`，其余格调用 `GameScene.add(Blob.seed(cell, 3, Fire.class))`；参照 `WandOfFireblast.onZap` 保留邻近格/可燃地形处理，避免越界。

- [ ] **步骤 4：接入连续远程攻击**

```java
@Override
public boolean doRangedAttack(Char target) {
    castFireblast(target.pos);
    spend(attackDelay());
    return true;
}
```

补充测试确认：已记录目标相邻时走 0–20 近战，非相邻且路径有效时每回合都能施放炎浪，不设置冷却。

- [ ] **步骤 5：运行测试确认绿灯并提交**

```powershell
.\gradlew.bat -I tools/roast_lamb_warlock_test.init.gradle --no-daemon --no-problems-report core:test --tests "*RoastLambWarlockTest"
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlockTest.java
git commit -m "feat: 实现烤全羊术士炎浪魔法"
```

## 任务 4：概念图、像素素材和动画类

**文件：**

- 创建：`docs/pixel-art/roast-lamb-warlock/roast_lamb_warlock_concept.png`
- 创建：`tools/test_generate_roast_lamb_warlock_sprite.py`
- 创建：`tools/generate_roast_lamb_warlock_sprite.py`
- 创建：`core/src/main/assets/sprites/roast_lamb_warlock.png`
- 创建：`docs/pixel-art/roast-lamb-warlock/roast_lamb_warlock_preview.png`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/RoastLambWarlockSpriteAssetTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/RoastLambWarlockSprite.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`

- [ ] **步骤 1：以 `warlock.png` 为参考生成概念图**

使用 ImageGen，仅将概念图作为造型参考：焦褐/暗红长袍、羊角或羊毛肩饰、烤肉叉和橙红火光；保持矮人术士矮壮轮廓。概念图写入上述文档路径，不直接作为游戏素材。

- [ ] **步骤 2：先写失败的 Pillow 和 Java 素材测试**

```python
def test_canvas_frame_geometry_and_palette(self):
    self.assertEqual((256, 16), self.output.size)
    self.assertLessEqual(len(self.visible_colors()), 12)
    self.assertEqual({0, 255}, {pixel[3] for pixel in self.output.getdata()})

def test_original_eleven_frames_keep_warlock_silhouette(self):
    for frame in range(11):
        self.assertEqual(
            self._frame(self.source, frame).getchannel("A").getbbox(),
            self._frame(self.output, frame).getchannel("A").getbbox())
```

```java
@Test
public void monsterUsesDedicatedSpriteAsset() {
    assertEquals("sprites/roast_lamb_warlock.png",
            Assets.Sprites.ROAST_LAMB_WARLOCK);
    assertSame(RoastLambWarlockSprite.class,
            new RoastLambWarlock().spriteClass);
}
```

- [ ] **步骤 3：运行测试确认红灯**

```powershell
python tools/test_generate_roast_lamb_warlock_sprite.py
.\gradlew.bat -I tools/roast_lamb_warlock_test.init.gradle --no-daemon --no-problems-report core:test --tests "*RoastLambWarlockSpriteAssetTest"
```

预期：生成器/PNG/资源常量/精灵类不存在。

- [ ] **步骤 4：实现确定性调色生成器**

读取 256×16 的 `warlock.png`，保持前 11 帧每个不透明像素的 alpha 轮廓不变，将原 21 色合并为最多 12 色：炭黑、三档焦褐、两档暗红、羊毛灰白、两档橙火与肤色。仅在原不透明区域内添加 1–3 像素的羊角、羊毛边和火光细节；透明像素 RGB 必须归零。使用 `Image.Resampling.NEAREST` 生成 8×预览。

- [ ] **步骤 5：实现资源常量和动画类**

```java
public class RoastLambWarlockSprite extends MobSprite {
    public RoastLambWarlockSprite() {
        texture(Assets.Sprites.ROAST_LAMB_WARLOCK);
        TextureFilm frames = new TextureFilm(texture, 12, 15);
        idle = new Animation(2, true); idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);
        run = new Animation(15, true); run.frames(frames, 0, 2, 3, 4);
        attack = new Animation(12, false); attack.frames(frames, 0, 5, 6);
        zap = attack.clone();
        die = new Animation(15, false); die.frames(frames, 0, 7, 8, 8, 9, 10);
        play(idle);
    }
}
```

增加 `flock(int cell)` 和 `fireblast(int cell)`：前者播放羊毛粒子与羊叫，后者按 `ConeAOE.outerRays` 发射 `MagicMissile.FIRE_CONE`；动画完成后回调怪物的 `onCastComplete()`，由怪物执行实际效果并 `next()`。

- [ ] **步骤 6：生成、测试并目视检查 1× 与 8× 预览**

```powershell
python tools/test_generate_roast_lamb_warlock_sprite.py
.\gradlew.bat -I tools/roast_lamb_warlock_test.init.gradle --no-daemon --no-problems-report core:test --tests "*RoastLambWarlockSpriteAssetTest"
```

预期：Python 和 Java 素材测试全部通过；1× 可辨认为火焰/羊主题术士，动作无跳帧，透明背景无光晕。

- [ ] **步骤 7：提交素材检查点**

仅暂存本任务的新文件和 `Assets.java` 中对应行；共享文件若含用户既有改动，使用交互式/补丁式暂存，不覆盖其他内容。

## 任务 5：高塔生成、图鉴与文本接入

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerMobRules.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：三个既有集成测试文件

- [ ] **步骤 1：先把集成测试改为五等分和五种图鉴实体**

```java
assertSame(CAMOUFLAGE_GNOLL, select(Math.nextDown(0.2f)));
assertSame(CORROSIVE_SWARM, select(0.2f));
assertSame(CORPSE, select(0.4f));
assertSame(EARTHLY_SERPENT, select(0.6f));
assertSame(ROAST_LAMB_WARLOCK, select(0.8f));
assertEquals(5, TowerMobRules.Selection.values().length);
```

图鉴断言顺序固定为迷彩豺狼、腐蚀蝇群、死尸、尘世巨蟒、烤全羊术士；同时验证新怪物不在 `Bestiary.REGIONAL`、`maxLvl == 30`、中英文消息存在，测试放置器包含新类。

- [ ] **步骤 2：运行集成测试确认红灯**

```powershell
.\gradlew.bat -I tools/roast_lamb_warlock_test.init.gradle --no-daemon --no-problems-report core:test --tests "*TowerLevelCamouflageGnollTest" --tests "*TowerBestiaryCategoriesTest" --tests "*TowerMobPlacerTest"
```

- [ ] **步骤 3：实现接入和文本**

在枚举末尾添加 `ROAST_LAMB_WARLOCK`，在 `TowerLevel.createMob()` 返回新实例，在 `Bestiary.TOWER_MOBS.addEntities` 末尾追加新类。消息键统一使用：

```properties
actors.mobs.tmobs.roastlambwarlock.name=roast lamb warlock
actors.mobs.tmobs.roastlambwarlock.desc=...
actors.mobs.tmobs.roastlambwarlock.discover_hint=...
```

中文名称固定为“烤全羊术士”，描述明确每个术士只会分别围困目标一次，之后连续释放炎浪。

- [ ] **步骤 4：运行集成测试确认绿灯**

运行步骤 2 命令；预期全部通过。

- [ ] **步骤 5：提交接入检查点**

只暂存本功能对应的共享文件片段；如果无法安全分离其他用户修改，则保留在工作树并在交付说明中列明，不进行覆盖或重置。

## 任务 6：完整验证与收尾

- [ ] **步骤 1：运行精灵生成器测试**

```powershell
python tools/test_generate_roast_lamb_warlock_sprite.py
```

- [ ] **步骤 2：运行本功能全部相关测试**

```powershell
.\gradlew.bat -I tools/roast_lamb_warlock_test.init.gradle --no-daemon --no-problems-report --rerun-tasks core:test --tests "*RoastLambWarlock*" --tests "*TowerLevelCamouflageGnollTest" --tests "*TowerBestiaryCategoriesTest" --tests "*TowerMobPlacerTest"
```

- [ ] **步骤 3：重新编译生产代码**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:compileJava
```

- [ ] **步骤 4：运行全量核心测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:test
```

记录总数和所有失败；将本功能失败与工作区已知基线失败区分，不能把全量测试失败表述为全部通过。

- [ ] **步骤 5：检查差异和工作区边界**

```powershell
git diff --check
git status --short
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/RoastLambWarlock.java
```

确认未重置、覆盖或提交不属于烤全羊术士的用户改动。完成后使用 `requesting-code-review` 和 `verification-before-completion` 做最终自审与证据核对。
