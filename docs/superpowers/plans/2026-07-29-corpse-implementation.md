# 死尸实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `executing-plans` 在当前会话逐任务实现本计划。所有生产代码必须先有失败测试。

**目标：** 新增只能被治疗伤害的高塔怪物“死尸”，将三种高塔普通生物改为等概率独占负层生成，并统一其经验上限为 30。

**架构：** `Corpse` 在怪物类内部覆写伤害和治疗入口，不改动全局 `Char.heal()`；治疗伤害绕过本类的普通伤害免疫，进入父类死亡流程。`TowerMobRules` 只保留三个高塔生物结果，按结果数量等权映射随机值，`TowerLevel` 不再回退到普通楼层怪物池。独立 `CorpseSprite` 使用从战士布甲层机械提取并逐像素改色的 12×15、16 帧精灵表。

**技术栈：** Java、JUnit 4、Gradle、Python 3、Pillow、PNG RGBA。

---

## 文件结构

### 新建

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/Corpse.java`：数值、亡灵属性、普通伤害免疫、治疗反转。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/CorpseSprite.java`：16 帧动画映射。
- `core/src/main/assets/sprites/corpse.png`：12×15、16 帧腐绿死尸精灵表。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CorpseTest.java`：行为和数值测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/CorpseSpriteAssetTest.java`：资源、轮廓、文案和动画测试。
- `tools/generate_corpse_sprite.py`：从 `warrior.png` 布甲层生成独立素材和预览。
- `tools/corpse_test.init.gradle`：收集死尸及高塔接入相关测试。
- `docs/pixel-art/corpse/corpse_concept.png`：A“腐绿尸斑”动作与材质概念参考。
- `docs/pixel-art/corpse/corpse_preview.png`：最终精灵表 8 倍最近邻预览。

### 修改

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CamouflageGnoll.java`：`maxLvl` 改为 30。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CorrosiveSwarm.java`：`maxLvl` 改为 30。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerMobRules.java`：三个结果等权选择，移除默认池结果。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`：实例化死尸，不再回退 `super.createMob()`。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`：死尸追加到 `TOWER_MOBS`。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`：增加 `Assets.Sprites.CORPSE`。
- `core/src/main/assets/messages/actors/actors.properties`：英文名称、描述、发现提示。
- `core/src/main/assets/messages/actors/actors_zh.properties`：中文名称、描述、发现提示。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CamouflageGnollTest.java`：经验上限期望改为 30。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CorrosiveSwarmTest.java`：经验上限期望改为 30。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevelCamouflageGnollTest.java`：更新为三等分边界测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`：期望三种高塔生物并校验经验上限。

## 任务 1：行为和数值红灯

- [ ] **步骤 1：创建测试初始化脚本**

`tools/corpse_test.init.gradle` 收集以下测试：

```groovy
project.sourceSets.test.java.setIncludes([
    "**/CamouflageGnollTest.java",
    "**/CorrosiveSwarmTest.java",
    "**/CorpseTest.java",
    "**/TowerLevelCamouflageGnollTest.java",
    "**/TowerBestiaryCategoriesTest.java",
    "**/TowerMobPlacerTest.java",
    "**/CamouflageGnollSpriteAssetTest.java",
    "**/CorrosiveSwarmSpriteAssetTest.java",
    "**/CorpseSpriteAssetTest.java"
])
```

- [ ] **步骤 2：编写 `CorpseTest`**

测试预期 API：

```java
@Test
public void usesApprovedStats() {
    TestCorpse corpse = new TestCorpse();
    assertEquals(1, corpse.HT);
    assertEquals(1, corpse.HP);
    assertEquals(40, corpse.attackSkill(null));
    assertEquals(20, corpse.defenseSkill(new Gnoll()));
    assertEquals(0, corpse.drRoll());
    assertEquals(13, corpse.EXP);
    assertEquals(30, corpse.maxLvl);
    assertTrue(corpse.properties().contains(Char.Property.UNDEAD));
}

@Test
public void ignoresEveryOrdinaryDamageCall() {
    Corpse corpse = new Corpse();
    corpse.damage(100, new Object(), DamageTag.PHYSICAL);
    corpse.damage(100, new Object(), DamageTag.MAGICAL);
    assertEquals(1, corpse.HP);
}

@Test
public void positiveHealingAtFullHealthKillsAndReturnsZero() {
    Corpse corpse = new Corpse();
    assertEquals(0, corpse.heal(1));
    assertFalse(corpse.isAlive());
}

@Test
public void nonPositiveHealingDoesNothing() {
    Corpse corpse = new Corpse();
    assertEquals(0, corpse.heal(0));
    assertEquals(0, corpse.heal(-1, false));
    assertEquals(1, corpse.HP);
}
```

另以循环验证 `damageRoll()` 始终位于 10–40，默认速度和攻击间隔均为 1。

- [ ] **步骤 3：先修改现有经验上限测试期望**

把 `CamouflageGnollTest` 和 `CorrosiveSwarmTest` 中的 `maxLvl` 期望改为 30。此时生产代码仍为 26。

- [ ] **步骤 4：运行红灯**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report -I tools\corpse_test.init.gradle core:test `
  --tests "*CorpseTest" --tests "*CamouflageGnollTest" --tests "*CorrosiveSwarmTest"
```

预期：编译因 `Corpse` 不存在而失败；创建空类后，数值、治疗反转和两个经验上限断言失败。

## 任务 2：最小行为实现

- [ ] **步骤 1：实现 `Corpse`**

核心结构：

```java
public class Corpse extends Mob {

    private enum HealingDamage {
        INSTANCE
    }

    {
        spriteClass = CorpseSprite.class;
        HP = HT = 1;
        defenseSkill = 20;
        EXP = 13;
        maxLvl = 30;
        loot = null;
        lootChance = 0f;
        properties.add(Property.UNDEAD);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(10, 40);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int drRoll() {
        return 0;
    }

    @Override
    public void damage(int damage, Object source, DamageTag... tags) {
        // Only healing may enter the parent damage pipeline.
    }

    @Override
    public int heal(int amount, boolean visual) {
        if (amount > 0 && isAlive()) {
            super.damage(amount, HealingDamage.INSTANCE,
                    DamageTag.UNAVOIDABLE, DamageTag.NO_ARMOR);
        }
        return 0;
    }
}
```

`heal(int)` 可继承 `Char` 的委托实现；测试必须证明两个重载都有效。若父类伤害流程在无 `Dungeon.hero` 的单元测试中访问空引用，则仅在测试夹具中提供最小英雄环境，不改写生产死亡流程。

- [ ] **步骤 2：统一现有高塔普通生物经验上限**

将：

```java
maxLvl = 26;
```

改为：

```java
maxLvl = 30;
```

只修改 `CamouflageGnoll` 和 `CorrosiveSwarm`，不改高塔 Boss。

- [ ] **步骤 3：运行行为绿灯**

重新运行任务 1 的命令。预期所有选定测试通过。

- [ ] **步骤 4：提交行为变更**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs `
        core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs `
        tools/corpse_test.init.gradle
git commit -m "feat: 实现高塔死尸治疗弱点"
```

## 任务 3：等权生成、图鉴和文案接入

- [ ] **步骤 1：先更新接入测试**

`TowerLevelCamouflageGnollTest` 使用等权边界：

```java
assertSame(CAMOUFLAGE_GNOLL, TowerMobRules.select(0f));
assertSame(CAMOUFLAGE_GNOLL, TowerMobRules.select(Math.nextDown(1f / 3f)));
assertSame(CORROSIVE_SWARM, TowerMobRules.select(1f / 3f));
assertSame(CORPSE, TowerMobRules.select(2f / 3f));
assertSame(CORPSE, TowerMobRules.select(Math.nextDown(1f)));
```

另断言枚举中不存在 `DEFAULT_POOL`。`TowerBestiaryCategoriesTest` 断言：

```java
assertEquals(Arrays.asList(
        CamouflageGnoll.class,
        CorrosiveSwarm.class,
        Corpse.class
), Bestiary.TOWER_MOBS.entities());
```

并实例化三个登记类，断言 `maxLvl == 30`。

- [ ] **步骤 2：运行接入红灯**

```powershell
.\gradlew.bat --no-daemon --no-problems-report -I tools\corpse_test.init.gradle core:test `
  --tests "*TowerLevelCamouflageGnollTest" `
  --tests "*TowerBestiaryCategoriesTest" `
  --tests "*TowerMobPlacerTest"
```

预期：缺少 `CORPSE` 选择、图鉴仍只有两项、旧默认池仍存在。

- [ ] **步骤 3：实现等权纯选择器**

`TowerMobRules`：

```java
static Selection select(float roll) {
    Selection[] values = Selection.values();
    int index = Math.min((int) (roll * values.length), values.length - 1);
    return values[Math.max(0, index)];
}

enum Selection {
    CAMOUFLAGE_GNOLL,
    CORROSIVE_SWARM,
    CORPSE
}
```

`TowerLevel.createMob()` 对三个枚举分支分别 `new` 对应怪物，不再调用 `super.createMob()`。

- [ ] **步骤 4：登记图鉴、资源常量与文案**

```java
TOWER_MOBS.addEntities(
        CamouflageGnoll.class,
        CorrosiveSwarm.class,
        Corpse.class
);
```

增加：

```java
public static final String CORPSE = "sprites/corpse.png";
```

文案键：

```properties
actors.mobs.tmobs.corpse.name=corpse
actors.mobs.tmobs.corpse.desc=This decaying body is untouched by blades and spells, as if death itself were holding it together. Any healing power that tries to repair its flesh instead makes it collapse.
actors.mobs.tmobs.corpse.discover_hint=Search the negative floors of the tower.
```

中文版使用：

```properties
actors.mobs.tmobs.corpse.name=死尸
actors.mobs.tmobs.corpse.desc=这具腐败的躯体对刀剑与法术毫无反应，维系它的似乎正是死亡本身。任何试图修复血肉的治疗力量，反而会让它迅速崩解。
actors.mobs.tmobs.corpse.discover_hint=在高塔的负数楼层寻找这具死尸。
```

- [ ] **步骤 5：运行接入绿灯并提交**

重复步骤 2 命令，预期全部通过。

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers `
        core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java `
        core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java `
        core/src/main/assets/messages/actors `
        core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers `
        core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal
git commit -m "feat: 接入死尸并等权生成高塔生物"
```

## 任务 4：概念图和像素素材

- [ ] **步骤 1：生成概念参考**

使用内置生图能力生成同一腐绿死尸的无遮挡动作参考：待机、两个行走极值、攻击蓄力、攻击命中、五阶段倒地趋势。提示词要求灰绿皮肤、褐灰破布、暗紫伤痕、凹陷眼窝、战士布甲轮廓、无武器换手、无场景、适合转为 12×15 像素；保存为 `docs/pixel-art/corpse/corpse_concept.png`。概念图仅指导材质和尸化细节，不用于缩放生成最终资源。

- [ ] **步骤 2：先编写素材红灯测试**

`CorpseSpriteAssetTest` 断言：

```java
assertEquals("sprites/corpse.png", Assets.Sprites.CORPSE);
assertSame(CorpseSprite.class, new Corpse().spriteClass);
assertEquals(192, image.getWidth());
assertEquals(15, image.getHeight());
```

逐帧验证：

- 16 帧均非空；
- Alpha 只为 0 或 255；
- 透明像素 RGB 为 0；
- 可见颜色不超过 12；
- `warrior.png` 首行前 16 帧的每个非透明像素，在 `corpse.png` 对应位置仍非透明。

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report -I tools\corpse_test.init.gradle core:test `
  --tests "*CorpseSpriteAssetTest"
```

预期：缺少资源和 `CorpseSprite`，测试失败。

- [ ] **步骤 3：编写机械换色生成器**

`tools/generate_corpse_sprite.py`：

1. 打开 `warrior.png` 并校验尺寸 256×128。
2. 从 `y=0..14` 提取前 16 个 `12×15` 帧。
3. 以最近颜色映射把皮肤替换为灰绿三阶、布甲替换为褐灰三阶、深色替换为暗紫轮廓。
4. 保留所有源非透明像素；只在原轮廓内部或相邻空像素补充眼窝、尸斑和衣角破损，不改变动作方向和脚底基线。
5. 清零透明像素 RGB，输出 `corpse.png`。
6. 使用最近邻放大 8 倍输出 `corpse_preview.png`。

- [ ] **步骤 4：运行生成器和双重验证器**

```powershell
python tools\generate_corpse_sprite.py
python tools\validate_tower_monster_sprite.py core\src\main\assets\sprites\corpse.png `
  --frame-width 12 --frame-height 15 --expected-frames 16 --max-colors 12
python D:\STUDY\Dungeon\tools\validate_tower_monster_sprite.py `
  core\src\main\assets\sprites\corpse.png `
  --frame-width 12 --frame-height 15 --expected-frames 16 --max-colors 12
```

预期：两个验证器均输出 `PASS`，尺寸为 192×15。

## 任务 5：动画实现与素材绿灯

- [ ] **步骤 1：实现 `CorpseSprite`**

```java
public class CorpseSprite extends MobSprite {
    public CorpseSprite() {
        texture(Assets.Sprites.CORPSE);
        TextureFilm frames = new TextureFilm(texture, 12, 15);

        idle = new Animation(2, true);
        idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);

        run = new Animation(20, true);
        run.frames(frames, 2, 3, 4, 5, 6, 7);

        die = new Animation(20, false);
        die.frames(frames, 8, 9, 10, 11, 12, 11);

        attack = new Animation(15, false);
        attack.frames(frames, 13, 14, 15, 0);

        play(idle);
    }
}
```

- [ ] **步骤 2：运行素材和全部专项测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks `
  -I tools\corpse_test.init.gradle core:test
```

预期：死尸、高塔生成、图鉴、测试放置器和三套素材测试全部通过。

- [ ] **步骤 3：人工像素检查**

以 1 倍检查每帧轮廓，以 8 倍预览检查：

- 眼窝没有随动作漂移到头部外；
- 布甲破损在相邻帧保持同一身体侧；
- 攻击帧手臂和身体没有断裂；
- 死亡帧重心持续降低；
- 灰绿皮肤与褐灰衣料在地牢暗背景上可区分。

- [ ] **步骤 4：提交素材和动画**

```powershell
git add core/src/main/assets/sprites/corpse.png `
        core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/CorpseSprite.java `
        core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/CorpseSpriteAssetTest.java `
        tools/generate_corpse_sprite.py docs/pixel-art/corpse
git commit -m "feat: 制作死尸像素动画"
```

## 任务 6：完成前验证

- [ ] **步骤 1：专项测试计数**

运行任务 5 的专项命令，并解析 `core/build/test-results/test/TEST-*.xml`，确认 `failures=0`、`errors=0`。

- [ ] **步骤 2：编译**

```powershell
.\gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:compileJava
```

预期：`BUILD SUCCESSFUL`。

- [ ] **步骤 3：完整核心测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report core:test
```

若完整工作区仍有无关失败，记录总测试数、失败测试全名，并确认死尸及高塔相关测试全部通过；不得报告为全绿。

- [ ] **步骤 4：差异检查**

```powershell
git diff --check -- `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java `
  core/src/main/assets/messages/actors `
  core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs `
  core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs
```

预期：无新增空白错误。检查 `git status --short`，只汇报本计划文件及相关代码，不覆盖或清理用户的其他工作区修改。
