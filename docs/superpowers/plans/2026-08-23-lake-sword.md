# 湖中剑 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实装拥有三态、拔剑直线攻击、装备恢复加速、跨楼层剑鞘回收和决斗家武技“风王加护”的 6 阶近战武器湖中剑。

**Architecture:** 使用单一 `LakeSword` 物品实例保存 `SHEATHED/CHARGED/SPENT` 状态和剩余魔力时间，内嵌 `MagicTracker` 统一推进英雄持有的多把湖中剑，并内嵌 `Scabbard` 处理直接拾取恢复。拔剑通过 `Ballistica.WONT_STOP` 构造定长路径，以湖中剑临时成为 `Belongings.attackingWeapon()` 的方式逐个执行必中普通攻击；`Regeneration` 和 `RegularLevel` 只调用湖中剑暴露的窄接口。

**Tech Stack:** Java 8、Shattered Pixel Dungeon actor/buff/item/level API、libGDX/Noosa、JUnit 4、Gradle、Python 3 + Pillow（仅用于确定性复制 PNG 像素）。

**Spec:** `docs/superpowers/specs/2026-08-23-lake-sword-design.md`

## Global Constraints

- 初始状态为 `SHEATHED`；状态变化不得替换物品实例或重置等级、附魔、诅咒、鉴定和快捷栏引用。
- 基础属性固定为 6 阶、20 力量、`5+等级`～`30+7×等级`、精准 1、延迟 1、距离 1。
- 拔剑距离固定为 `max(1, 5+拔剑时的 buffedLvl())`，射线不被角色或任何地形截断，只破坏路径上的可燃地形。
- 拔剑只攻击路径上的存活敌对单位，无视魅惑；每个目标独立执行一次必中普通物理武器攻击。
- 拔剑攻击不享受随后产生的充盈增伤；完整动作只消耗一次湖中剑普通攻击延迟并解除隐身。
- `CHARGED` 时仅该实例的标准近战攻击获得 1.5 倍武器掷伤和 +1 距离，持续 `max(1, 4+拔剑时的 buffedLvl())` 回合。
- 充盈计时不会因换武器或放入背包暂停；丢弃充盈武器立即转为 `SPENT`。
- 只有装备在主武器槽或副武器槽的 `SHEATHED` 湖中剑将最终自然恢复间隔减半；效果不叠加且不绕过原有恢复阻断规则。
- 剑鞘每次只恢复一把武器，优先级严格为主武器、副武器、背包第一把。
- 只有首次生成的主地牢普通程序化楼层和高塔普通楼层生成剑鞘；每个符合条件的新楼层恰好一个，其他固定或特殊楼层不生成。
- 风王加护消耗 1 点武技充能、不耗回合，给予 `max(1, 2+buffedLvl())` 回合隐身和 5 回合普通急速。
- 精灵素材只做无插值逐像素复制，必须保留 alpha=102 的黑色半透明像素；`ex_items.png` 非目标区域不得变化。
- 执行每项任务前先运行 `git status --short` 和相应文件的 `git diff --`。如果目标文件已有不属于湖中剑的改动，只编辑所需局部且不把既有改动混入本任务提交。

## 文件结构

- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java` — 武器属性、三态、拔剑、充盈追踪、剑鞘和武技的唯一生产类。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java` — 完成全背包存档恢复后重建充盈追踪器。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Regeneration.java` — 在最终自然恢复间隔上应用湖中剑的 0.5 系数。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/RegularLevel.java` — 普通新楼层生成一个剑鞘。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/LastShopLevel.java` — 显式禁止最终商店层生成剑鞘。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java` — 明确允许高塔普通楼层生成剑鞘。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java` — 注册等权重 6 阶湖中剑。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java` — 定义三个扩展贴图区域。
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java` — 登记湖中剑资料条目。
- Modify: `core/src/main/assets/sprites/ex_items.png` — 写入两种剑图和一个剑鞘图。
- Modify: `core/src/main/assets/messages/items/items.properties` — 英文物品、动作、状态、Buff 和警告文本。
- Modify: `core/src/main/assets/messages/items/items_zh.properties` — 中文物品、动作、状态、Buff 和警告文本。
- Modify: `core/src/main/assets/messages/custom/custom.properties` — 英文详细资料。
- Modify: `core/src/main/assets/messages/custom/custom_zh.properties` — 中文详细资料。
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java` — 属性、状态、计时、拔剑、恢复和武技专项测试。
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/RegenerationLakeSwordTest.java` — 恢复系数与装备位置测试。
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/LakeSwordGenerationTest.java` — 楼层资格、生成数量和多武器条件测试。
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java` — 索引、尺寸、像素摘要和非目标区域保护。
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponIntegrationTest.java` — 生成池和测试生成器登记。
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponTextTest.java` — 中英文文本和资料登记。
- Create outside repository: `D:/STUDY/Dungeon/cache/lake_sword/insert_lake_sword.py` — 可复现素材写入脚本和备份，不纳入项目。

---

### Task 1: 写入湖中剑精灵并锁定扩展贴图契约

**Files:**
- Create outside repository: `D:/STUDY/Dungeon/cache/lake_sword/insert_lake_sword.py`
- Modify: `core/src/main/assets/sprites/ex_items.png`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

**Interfaces:**
- Consumes: 原图 `D:/桌面/湖中剑/湖中剑素材.png`；`EXItemSpriteSheet.encode(int image, int w, int h)`。
- Produces: `LAKE_SWORD`、`LAKE_SWORD_SHEATHED`、`LAKE_SWORD_SCABBARD` 三个编码常量。

- [ ] **Step 1: 在贴图测试中先声明失败契约**

在 `EXItemSpriteSheetTest` 的尺寸数组中加入三个常量，并增加独立测试：

```java
@Test
public void lakeSwordCellsPreserveSourceGeometryAndPartialAlpha() throws Exception {
    assertEquals(158, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.LAKE_SWORD));
    assertEquals(159, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.LAKE_SWORD_SHEATHED));
    assertEquals(288, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.LAKE_SWORD_SCABBARD));
    assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.LAKE_SWORD));
    assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.LAKE_SWORD_SHEATHED));
    assertEquals(14, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.LAKE_SWORD_SCABBARD));
    assertEquals(14, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.LAKE_SWORD_SCABBARD));

    BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
    assertCellDigest(sheet, 158, "7470907c7dd0280afc4d65a60ddac72ebffd522f1a5572f820f4a55c0674a571");
    assertCellDigest(sheet, 159, "bfc1fafb1730ce971887857d53eda47f9c2b637712900d527110afa7e3e4c96a");
    assertCellDigest(sheet, 288, "c1b29389a6a62414adfb4253b31286b55b178f8225e8d4b34359715577da2398");
    assertEquals(74, alphaCount(sheet, 158, 102));
    assertEquals(51, alphaCount(sheet, 159, 102));
    assertEquals(31, alphaCount(sheet, 288, 102));
}
```

加入以下两个测试辅助方法。摘要顺序必须是 RGBA，统计范围必须是完整 16×16 单元，不能只看编码后的 14×14 显示区域：

```java
private static void assertCellDigest(BufferedImage sheet, int index, String expected)
        throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    int cellX = index % 16 * 16;
    int cellY = index / 16 * 16;
    for (int y = 0; y < 16; y++) {
        for (int x = 0; x < 16; x++) {
            int argb = sheet.getRGB(cellX + x, cellY + y);
            digest.update((byte) ((argb >>> 16) & 0xFF));
            digest.update((byte) ((argb >>> 8) & 0xFF));
            digest.update((byte) (argb & 0xFF));
            digest.update((byte) ((argb >>> 24) & 0xFF));
        }
    }
    StringBuilder actual = new StringBuilder();
    for (byte value : digest.digest()) actual.append(String.format("%02x", value));
    assertEquals(expected, actual.toString());
}

private static int alphaCount(BufferedImage sheet, int index, int expectedAlpha) {
    int count = 0;
    int cellX = index % 16 * 16;
    int cellY = index / 16 * 16;
    for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
        if (((sheet.getRGB(cellX + x, cellY + y) >>> 24) & 0xFF) == expectedAlpha) count++;
    }
    return count;
}
```

将原先“158～159 必须透明”的断言改为验证 158、159 后从 160 开始仍透明；增加 288 以外相邻单元仍透明的断言。

- [ ] **Step 2: 运行测试确认常量和素材均缺失**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest`

Expected: FAIL，编译阶段报告 `LAKE_SWORD` 等常量不存在，或像素摘要不匹配。

- [ ] **Step 3: 定义精灵索引常量**

在 `SOUL_BLADE` 后和扩展表后续空白区注释中加入：

```java
public static final int LAKE_SWORD = encode(158, 16, 16);
public static final int LAKE_SWORD_SHEATHED = encode(159, 16, 16);

// Row 19 (indices 288-303): special weapon recovery components.
public static final int LAKE_SWORD_SCABBARD = encode(288, 14, 14);
```

- [ ] **Step 4: 创建并运行确定性素材脚本**

脚本必须验证原图 SHA-256 为 `4754c3b72e8f437980deaa79812fa035b204e54801521c1c496fe4c030e4adb8`，把三个 16×16 单元逐像素复制到索引 158、159、288，并把修改前的表备份到缓存目录：

```python
from pathlib import Path
from PIL import Image
import hashlib, shutil

source = Path(r"D:/桌面/湖中剑/湖中剑素材.png")
sheet_path = Path(r"D:/STUDY/Dungeon/transformation-pixel-dungeon-master/core/src/main/assets/sprites/ex_items.png")
cache = Path(r"D:/STUDY/Dungeon/cache/lake_sword")
cache.mkdir(parents=True, exist_ok=True)
assert hashlib.sha256(source.read_bytes()).hexdigest() == "4754c3b72e8f437980deaa79812fa035b204e54801521c1c496fe4c030e4adb8"
src = Image.open(source).convert("RGBA")
sheet = Image.open(sheet_path).convert("RGBA")
assert src.size == (48, 16)
assert sheet.size == (256, 512)
shutil.copy2(sheet_path, cache / "ex_items.before_lake_sword.png")
before = sheet.copy()
for source_slot, target_index in enumerate((158, 159, 288)):
    cell = src.crop((source_slot * 16, 0, source_slot * 16 + 16, 16))
    x = target_index % 16 * 16
    y = target_index // 16 * 16
    sheet.paste(cell, (x, y))
allowed = {158, 159, 288}
for y in range(sheet.height):
    for x in range(sheet.width):
        index = x // 16 + (y // 16) * 16
        if index not in allowed:
            assert sheet.getpixel((x, y)) == before.getpixel((x, y)), (x, y)
sheet.save(sheet_path)
```

Run: `D:/anaconda/envs/wy/python.exe D:/STUDY/Dungeon/cache/lake_sword/insert_lake_sword.py`

Expected: `ex_items.png` 仍为 256×512，三个目标单元分别拥有 124、122、82 个非透明像素，并保留 alpha=102 像素。

- [ ] **Step 5: 运行贴图测试并比较非目标像素**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest`

Expected: PASS；测试同时逐像素比较缓存备份与新图，允许变化的坐标仅属于 158、159、288 三个单元。

- [ ] **Step 6: 提交资源契约**

```bash
git add core/src/main/assets/sprites/ex_items.png core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java
git diff --cached --check
git commit -m "feat: add lake sword sprite cells"
```

### Task 2: 建立湖中剑基础属性、三态和存档模型

**Files:**
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java`

**Interfaces:**
- Consumes: Task 1 的三个 `EXItemSpriteSheet` 常量。
- Produces: `LakeSword.State`、`state()`、`magicTurns()`、`enterCharged(Hero,int)`、`exhaustMagic()`、`restoreSheath()`、`tickMagic()`、`drawRangeForLevel(int)`、`chargeDurationForLevel(int)`。

- [ ] **Step 1: 编写基础数值和三态失败测试**

创建与生产类相同包名的 `LakeSwordTest`，使包级测试可以检查状态辅助方法：

```java
@Test
public void baseStatsAndScalingMatchTierSixSpecification() {
    LakeSword sword = new LakeSword();
    assertEquals(5, sword.min(0));
    assertEquals(30, sword.max(0));
    assertEquals(8, sword.min(3));
    assertEquals(51, sword.max(3));
    assertEquals(20, sword.STRReq(0));
    assertEquals(1f, sword.ACC, 0f);
    assertEquals(1f, sword.DLY, 0f);
    assertEquals(1, sword.RCH);
}

@Test
public void stateTransitionsChangeOnlyStateTimerAndImage() {
    LakeSword sword = new LakeSword();
    assertEquals(LakeSword.State.SHEATHED, sword.state());
    assertEquals(EXItemSpriteSheet.LAKE_SWORD_SHEATHED, sword.image);
    sword.enterCharged(null, 3);
    assertEquals(LakeSword.State.CHARGED, sword.state());
    assertEquals(7, sword.magicTurns());
    assertEquals(EXItemSpriteSheet.LAKE_SWORD, sword.image);
    sword.exhaustMagic();
    assertEquals(LakeSword.State.SPENT, sword.state());
    assertTrue(sword.restoreSheath());
    assertEquals(LakeSword.State.SHEATHED, sword.state());
}
```

另加 Bundle 往返测试，确认 `state` 和 `magicTurns` 恢复，同时等级、附魔和诅咒字段仍由父类正常恢复；对缺少新字段的 Bundle 断言默认 `SHEATHED`。

- [ ] **Step 2: 运行测试确认生产类缺失**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: FAIL，编译报告 `LakeSword` 不存在。

- [ ] **Step 3: 写出最小基础类与状态转换**

核心结构必须使用以下字段和签名，避免后续任务出现命名漂移：

```java
public class LakeSword extends MeleeWeapon {
    public static final String AC_DRAW = "DRAW";
    public static final int TIER = 6;
    private static final String STATE = "state";
    private static final String MAGIC_TURNS = "magic_turns";

    public enum State { SHEATHED, CHARGED, SPENT }

    private State state = State.SHEATHED;
    private int magicTurns;

    {
        tier = TIER;
        ACC = 1f;
        DLY = 1f;
        RCH = 1;
        hitSound = Assets.Sounds.HIT_SLASH;
        syncImage();
    }

    @Override public int min(int level) { return 5 + level; }
    @Override public int max(int level) { return 30 + 7 * level; }
    @Override public int STRReq(int level) { return STRReq(TIER, level); }

    public State state() { return state; }
    public int magicTurns() { return magicTurns; }
    public boolean isSheathed() { return state == State.SHEATHED; }
    public boolean isCharged() { return state == State.CHARGED; }
    public static int drawRangeForLevel(int level) { return Math.max(1, 5 + level); }
    public static int chargeDurationForLevel(int level) { return Math.max(1, 4 + level); }

    public void enterCharged(Hero hero, int effectiveLevel) {
        state = State.CHARGED;
        magicTurns = chargeDurationForLevel(effectiveLevel);
        syncImage();
    }

    public void exhaustMagic() {
        if (state == State.CHARGED) state = State.SPENT;
        magicTurns = 0;
        syncImage();
        updateQuickslot();
    }

    public boolean restoreSheath() {
        if (state == State.SHEATHED) return false;
        state = State.SHEATHED;
        magicTurns = 0;
        syncImage();
        updateQuickslot();
        return true;
    }

    private void syncImage() {
        image = state == State.SHEATHED
                ? EXItemSpriteSheet.LAKE_SWORD_SHEATHED
                : EXItemSpriteSheet.LAKE_SWORD;
    }
}
```

Task 2 保留 `Hero` 参数以固定后续接口，但尚不创建追踪器。Bundle 实现写为：

```java
@Override public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(STATE, state.name());
    bundle.put(MAGIC_TURNS, magicTurns);
}

@Override public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    State restored = State.SHEATHED;
    if (bundle.contains(STATE)) {
        try { restored = State.valueOf(bundle.getString(STATE)); }
        catch (IllegalArgumentException ignored) { restored = State.SHEATHED; }
    }
    state = restored;
    magicTurns = state == State.CHARGED
            ? Math.max(1, bundle.getInt(MAGIC_TURNS)) : 0;
    syncImage();
}
```

Bundle 读取枚举时对缺失值和非法值回退到 `SHEATHED`，且 `CHARGED` 以外的状态强制令 `magicTurns=0`。

- [ ] **Step 4: 运行基础状态测试**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: PASS 当前数值、转换与 Bundle 用例。

- [ ] **Step 5: 提交基础模型**

```bash
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java
git diff --cached --check
git commit -m "feat: add lake sword state model"
```

### Task 3: 实现充盈计时、近战强化和丢弃耗尽

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java`

**Interfaces:**
- Consumes: Task 2 的状态与 `enterCharged` 接口；`Hero.belongings.getAllItems(LakeSword.class)`。
- Produces: `MagicTracker.sync(Hero)`、`tickMagic()`、`chargedDamage(int)`、`hasChargedSword(Hero)`。

- [ ] **Step 1: 增加充盈行为失败测试**

```java
@Test
public void chargedDamageRoundsAfterWeaponAndStrengthRoll() {
    assertEquals(15, LakeSword.chargedDamage(10));
    assertEquals(17, LakeSword.chargedDamage(11));
}

@Test
public void independentTimersExpireWithoutDependingOnEquipmentSlot() {
    LakeSword first = new LakeSword();
    LakeSword second = new LakeSword();
    first.enterCharged(null, 0);
    second.enterCharged(null, 2);
    for (int i = 0; i < 4; i++) first.tickMagic();
    for (int i = 0; i < 4; i++) second.tickMagic();
    assertEquals(LakeSword.State.SPENT, first.state());
    assertEquals(LakeSword.State.CHARGED, second.state());
    assertEquals(2, second.magicTurns());
}
```

构造英雄主手、次手和背包场景，断言只有 `hero.belongings.attackingWeapon()==sword` 时 `damageRoll` 乘 1.5 且 `reachFactor` 加 1；换成另一把武器后不加成。增加丢弃成功才耗尽、诅咒导致丢弃失败不耗尽的测试。

再增加两个边界：充盈后升级武器不改变已经固定的 `magicTurns`；把湖中剑作为普通投掷物扔出时不获得 1.5 倍伤害，并立即变为 `SPENT`。

- [ ] **Step 2: 运行测试确认强化和追踪尚未实现**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: FAIL，缺少 `chargedDamage`、`tickMagic` 或强化断言不成立。

- [ ] **Step 3: 实现单实例计时与统一追踪器**

```java
public static int chargedDamage(int baseDamage) {
    return Math.round(baseDamage * 1.5f);
}

void tickMagic() {
    if (state != State.CHARGED) return;
    if (--magicTurns <= 0) exhaustMagic();
}

public static boolean hasChargedSword(Hero hero) {
    for (LakeSword sword : hero.belongings.getAllItems(LakeSword.class)) {
        if (sword.isCharged()) return true;
    }
    return false;
}

public static class MagicTracker extends Buff {
    public static void sync(Hero hero) {
        if (hero == null) return;
        if (hasChargedSword(hero)) Buff.affect(hero, MagicTracker.class);
        else {
            MagicTracker tracker = hero.buff(MagicTracker.class);
            if (tracker != null) tracker.detach();
        }
    }

    @Override public boolean act() {
        Hero hero = (Hero) target;
        for (LakeSword sword : hero.belongings.getAllItems(LakeSword.class)) sword.tickMagic();
        if (!hasChargedSword(hero)) { detach(); return true; }
        spend(TICK);
        return true;
    }
}
```

同时把 Task 2 的 `enterCharged` 末尾补为 `MagicTracker.sync(hero);`；`sync(null)` 的空保护保证纯数值测试仍可使用 `enterCharged(null, level)`。

在 `Belongings.restoreFromBundle(Bundle)` 已恢复背包、主手和副手并完成 `onSecondaryWeaponChanged()` 后，精确加入：

```java
LakeSword.MagicTracker.sync(owner);
```

这使背包内的充盈湖中剑也能在读档后重建追踪器。`MagicTracker.sync` 必须允许 `owner==null` 并直接返回，且不得为每把武器附加一个独立 Buff。对应测试先构造一个 Bundle，恢复含背包充盈湖中剑的 `Belongings`，再断言英雄只有一个 `MagicTracker`。

- [ ] **Step 4: 实现精确的伤害、距离和丢弃约束**

```java
private boolean chargedMeleeAttack(Char owner) {
    return isCharged() && owner instanceof Hero
            && isEquipped((Hero) owner)
            && ((Hero) owner).belongings.attackingWeapon() == this;
}

@Override public int damageRoll(Char owner) {
    int rolled = super.damageRoll(owner);
    return chargedMeleeAttack(owner) ? chargedDamage(rolled) : rolled;
}

@Override public int reachFactor(Char owner) {
    int reach = super.reachFactor(owner);
    return chargedMeleeAttack(owner) ? reach + 1 : reach;
}

@Override public void doDrop(Hero hero) {
    super.doDrop(hero);
    if (isCharged() && !hero.belongings.contains(this)) {
        exhaustMagic();
        MagicTracker.sync(hero);
    }
}

@Override public void cast(Hero hero, int dst) {
    super.cast(hero, dst);
    if (isCharged() && !hero.belongings.contains(this)) {
        exhaustMagic();
        MagicTracker.sync(hero);
    }
}
```

- [ ] **Step 5: 运行专项测试**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: PASS，包括独立计时、换武器不停表、攻击武器身份、舍入和丢弃用例。

- [ ] **Step 6: 提交充盈机制**

```bash
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java
git diff --cached --check
git commit -m "feat: add lake sword charged state"
```

### Task 4: 实现拔剑选择、穿透射线和多目标必中攻击

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java`

**Interfaces:**
- Consumes: Task 2/3 的状态、计时和强化接口；`Ballistica.WONT_STOP`、`Beam.LightRay`、`Level.destroy`。
- Produces: `canDraw(Hero)`、`drawPath(Hero,int)`、`isDrawTarget(Char)`、`resolveDraw(Hero,int)` 和 `AC_DRAW` 完整动作。

- [ ] **Step 1: 写拔剑路径与原子取消失败测试**

测试地图使用直线同时放置可燃地形、不可燃墙、敌人、盟友和中立单位：

```java
@Test
public void drawPathUsesDirectionAndIgnoresBlockingTerrain() {
    List<Integer> path = sword.drawPath(hero, targetBeyondWall);
    assertEquals(LakeSword.drawRangeForLevel(sword.buffedLvl()), path.size());
    assertTrue(path.contains(flammableCell));
    assertTrue(path.contains(solidWallCell));
    assertTrue(path.contains(enemyBehindWall.pos));
}

@Test
public void drawTargetFilterOnlyAcceptsLivingEnemies() {
    assertTrue(LakeSword.isDrawTarget(livingEnemy));
    assertFalse(LakeSword.isDrawTarget(hero));
    assertFalse(LakeSword.isDrawTarget(ally));
    assertFalse(LakeSword.isDrawTarget(neutral));
    enemy.HP = 0;
    assertFalse(LakeSword.isDrawTarget(enemy));
}
```

增加动作测试：`null`、英雄自身格子、主手不可卸下时，英雄已用时间、物品槽位、武器状态和目标生命值均不变化。背包拔剑成功后只计算一次 `hero.attackDelay()`，湖中剑进入主手，原主手进入背包或在背包满时掉落。

- [ ] **Step 2: 运行测试确认拔剑入口缺失**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: FAIL，缺少路径、筛选和动作实现。

- [ ] **Step 3: 增加动作菜单、默认动作和前置检查**

```java
@Override public ArrayList<String> actions(Hero hero) {
    ArrayList<String> result = super.actions(hero);
    if (isSheathed()) result.add(AC_DRAW);
    return result;
}

@Override public String defaultAction() {
    Hero hero = Dungeon.hero;
    if (hero != null && isSheathed() && canDraw(hero)) return AC_DRAW;
    if (hero != null && isEquipped(hero) && canUseWeaponAbility(hero)) return AC_ABILITY;
    return hero != null && isEquipped(hero) ? super.defaultAction() : AC_EQUIP;
}
```

`canDraw(Hero)` 必须镜像 `EquipableItem.doUnequip` 的诅咒条件：

```java
boolean canDraw(Hero hero) {
    if (!isSheathed()) return false;
    if (isEquipped(hero)) return true;
    KindOfWeapon primary = hero.belongings.weapon();
    return primary == null
            || !primary.cursed
            || hero.buff(MagicImmune.class) != null
            || (hero.belongings.lostInventory() && !primary.keptThroughLostInventory());
}
```

`execute` 在打开选择器前检查一次，并在目标回调中再次检查，以保证动作原子性。

`execute` 对 `AC_DRAW` 先设置 `usesTargeting=true` 再调用 `super.execute(hero, action)`，通过 `GameScene.selectCell` 打开带十字准星的 `CellSelector.Listener`。监听器对 `null` 和 `cell==hero.pos` 直接返回；其他动作把 `usesTargeting=false` 后交还父类，防止拔剑后快捷栏仍残留目标模式。

- [ ] **Step 4: 实现无额外耗时的背包换入主手**

在 `LakeSword` 内实现私有 `equipPrimaryForDraw(Hero)`，只复用现有字段和钩子，不调用会自行花费回合的 `doEquip`/`doUnequip`：

```java
private boolean equipPrimaryForDraw(Hero hero) {
    if (isEquipped(hero)) return true;
    if (!canDraw(hero) || !hero.belongings.contains(this)) return false;

    KindOfWeapon previous = hero.belongings.weapon;
    int ownSlot = Dungeon.quickslot.getSlot(this);
    detachAll(hero.belongings.backpack);
    if (previous != null) {
        hero.belongings.weapon = null;
        if (!previous.collect(hero.belongings.backpack)) {
            Dungeon.quickslot.clearItem(previous);
            Dungeon.level.drop(previous, hero.pos).sprite.drop();
        }
    }
    hero.belongings.weapon = this;
    activate(hero);
    Talent.onItemEquipped(hero, this);
    Badges.validateDuelistUnlock();
    cursedKnown = true;
    if (cursed) EquipableItem.equipCursed(hero);
    if (ownSlot >= 0) Dungeon.quickslot.setSlot(ownSlot, this);
    updateQuickslot();
    return true;
}
```

不得用“先正常装备、再扣除装备时间”的补偿做法。测试必须记录 `Dungeon.quickslot.getSlot(this)`，确认无耗时换装前后仍指向同一湖中剑实例。

- [ ] **Step 5: 实现定长路径、地形破坏和逐目标攻击**

```java
List<Integer> drawPath(Hero hero, int selectedCell) {
    Ballistica beam = new Ballistica(hero.pos, selectedCell, Ballistica.WONT_STOP);
    int end = Math.min(drawRangeForLevel(buffedLvl()), beam.dist);
    return new ArrayList<>(beam.subPath(1, end));
}

static boolean isDrawTarget(Char ch) {
    return ch != null && ch.isAlive() && ch.alignment == Char.Alignment.ENEMY;
}
```

`resolveDraw` 必须按以下顺序执行：重新验证目标与装备条件；必要时无耗时换入主手；计算一次固定路径；显示从英雄到路径终点的 `Beam.LightRay`；逐格调用 `Dungeon.level.destroy(cell)` 和 `GameScene.updateMap(cell)` 处理 `Dungeon.level.flamable[cell]`；收集路径上的敌对单位；临时令 `hero.belongings.abilityWeapon=this`，对每个仍存活目标调用：

```java
hero.attack(target, 1f, 0f, Float.POSITIVE_INFINITY,
        DamageTag.PHYSICAL, DamageTag.MELEE);
```

用 `try/finally` 恢复进入拔剑前的 `abilityWeapon`。所有格子和目标处理完毕后调用 `enterCharged(hero, buffedLvlAtStart)`；随后 `Invisibility.dispel()`、`Dungeon.observe()`（仅在破坏过地形时）、`hero.spendAndNext(hero.attackDelay())`。状态切换必须在最后一个目标攻击之后，以防拔剑自身吃到 1.5 倍增伤。

- [ ] **Step 6: 运行拔剑专项测试**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: PASS，包含取消原子性、诅咒锁定、背包换装、穿墙、地形破坏、敌对筛选、魅惑无视、独立掷伤、附魔触发和单回合结算。

- [ ] **Step 7: 提交拔剑机制**

```bash
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java
git diff --cached --check
git commit -m "feat: add lake sword draw attack"
```

### Task 5: 实现剑鞘直接恢复与装备自然恢复加速

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Regeneration.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/RegenerationLakeSwordTest.java`

**Interfaces:**
- Consumes: Task 2 的 `restoreSheath()`、Task 3 的 `MagicTracker.sync(Hero)`。
- Produces: `firstRestorable(Hero)`、`hasUnsheathedSword(Hero)`、`hasEquippedSheathedSword(Hero)`、`naturalRegenDelayFactor(Hero)`、`LakeSword.Scabbard`。

- [ ] **Step 1: 写恢复优先级和恢复系数失败测试**

```java
@Test
public void scabbardRestoresPrimaryThenSecondaryThenBackpack() {
    assertSame(primarySpent, LakeSword.firstRestorable(hero));
    assertTrue(new LakeSword.Scabbard().doPickUp(hero, hero.pos));
    assertEquals(LakeSword.State.SHEATHED, primarySpent.state());
    assertSame(secondaryCharged, LakeSword.firstRestorable(hero));
}

@Test
public void regenFactorOnlyChecksEquippedSheathedSwordAndNeverStacks() {
    assertEquals(1f, LakeSword.naturalRegenDelayFactor(hero), 0f);
    hero.belongings.weapon = sheathed;
    assertEquals(0.5f, LakeSword.naturalRegenDelayFactor(hero), 0f);
    hero.belongings.secondWep = anotherSheathed;
    assertEquals(0.5f, LakeSword.naturalRegenDelayFactor(hero), 0f);
    sheathed.enterCharged(null, 0);
    sheathed.exhaustMagic();
    anotherSheathed.enterCharged(null, 0);
    anotherSheathed.exhaustMagic();
    assertEquals(1f, LakeSword.naturalRegenDelayFactor(hero), 0f);
}
```

在 `RegenerationLakeSwordTest` 通过提取的 `adjustedDelayForLakeSword(Hero,float)` 断言已有修正后的 7.5 间隔变成 3.75，而无剑鞘、仅背包或两把有鞘武器分别为 7.5、7.5、3.75。

- [ ] **Step 2: 运行测试确认剑鞘和恢复接口缺失**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RegenerationLakeSwordTest`

Expected: FAIL，缺少 `Scabbard` 和恢复系数接口。

- [ ] **Step 3: 实现明确的武器选择和自然恢复查询**

```java
public static LakeSword firstRestorable(Hero hero) {
    if (hero.belongings.weapon() instanceof LakeSword
            && !((LakeSword) hero.belongings.weapon()).isSheathed())
        return (LakeSword) hero.belongings.weapon();
    if (hero.belongings.secondWep() instanceof LakeSword
            && !((LakeSword) hero.belongings.secondWep()).isSheathed())
        return (LakeSword) hero.belongings.secondWep();
    for (LakeSword sword : hero.belongings.getAllItems(LakeSword.class)) {
        if (sword != hero.belongings.weapon() && sword != hero.belongings.secondWep()
                && !sword.isSheathed()) return sword;
    }
    return null;
}

public static boolean hasUnsheathedSword(Hero hero) {
    return firstRestorable(hero) != null;
}

public static boolean hasEquippedSheathedSword(Hero hero) {
    return hero != null && ((hero.belongings.weapon() instanceof LakeSword
            && ((LakeSword) hero.belongings.weapon()).isSheathed())
            || (hero.belongings.secondWep() instanceof LakeSword
            && ((LakeSword) hero.belongings.secondWep()).isSheathed()));
}

public static float naturalRegenDelayFactor(Hero hero) {
    return hasEquippedSheathedSword(hero) ? 0.5f : 1f;
}
```

- [ ] **Step 4: 实现内嵌剑鞘的直接拾取**

```java
public static class Scabbard extends Item {
    { image = EXItemSpriteSheet.LAKE_SWORD_SCABBARD; stackable = false; }
    @Override public boolean isUpgradable() { return false; }
    @Override public boolean isIdentified() { return true; }
    @Override public int value() { return 0; }

    @Override public boolean doPickUp(Hero hero, int pos) {
        LakeSword sword = firstRestorable(hero);
        if (sword == null) {
            GLog.w(Messages.get(this, "no_sword"));
            return false;
        }
        sword.restoreSheath();
        MagicTracker.sync(hero);
        Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
        updateQuickslot();
        hero.spendAndNext(pickupDelay());
        return true;
    }
}
```

恢复音效固定复用 `Assets.Sounds.UNLOCK`，不增加新音频资源。

- [ ] **Step 5: 在最终恢复间隔处应用非叠加系数**

在 `Regeneration` 中加入可测试的包级静态方法，并在圣杯、盐立方、天赋、疫病疲劳和水史莱姆等现有修正全部完成后、`partialRegen += 1f / delay` 之前调用：

```java
static float adjustedDelayForLakeSword(Hero hero, float finalDelay) {
    return finalDelay * LakeSword.naturalRegenDelayFactor(hero);
}

delay = adjustedDelayForLakeSword((Hero) target, delay);
```

不得改变 `regenOn()`、饥饿、生命上限、楼层锁定或任何原有判定。

- [ ] **Step 6: 运行剑鞘和自然恢复测试**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RegenerationLakeSwordTest`

Expected: PASS，且原有 Regeneration 测试无回归。

- [ ] **Step 7: 提交恢复机制**

```bash
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Regeneration.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/RegenerationLakeSwordTest.java
git diff --cached --check
git commit -m "feat: add lake sword sheath recovery"
```

### Task 6: 在后续普通楼层生成遗失剑鞘

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/RegularLevel.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/LastShopLevel.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/LakeSwordGenerationTest.java`

**Interfaces:**
- Consumes: Task 5 的 `LakeSword.hasUnsheathedSword(Hero)` 和 `LakeSword.Scabbard`。
- Produces: `lakeSwordScabbardGenerationEnabled()`、`createLakeSwordScabbard()` 与每次 `createItems()` 最多一次的剑鞘掉落。

- [ ] **Step 1: 写楼层资格和数量失败测试**

创建继承 `SewerLevel` 的 `ExposedRegularLevel`、继承 `TowerLevel` 的 `ExposedTowerLevel`、继承 `LastShopLevel` 的 `ExposedLastShopLevel`。三个测试子类把 `lakeSwordScabbardGenerationEnabled()` 暴露为 `scabbardEnabled()`；`ExposedRegularLevel` 另外把 `createLakeSwordScabbard()` 暴露为 `runScabbardGeneration()`，并用可控 `randomDropCell()` 返回固定合法格：

```java
@Test
public void mainAndTowerRegularFloorsEnableScabbardGeneration() {
    assertTrue(new ExposedRegularLevel().scabbardEnabled());
    assertTrue(new ExposedTowerLevel().scabbardEnabled());
    assertFalse(new ExposedLastShopLevel().scabbardEnabled());
}

@Test
public void oneUnsheathedSwordCreatesExactlyOneScabbardPerNewLevel() {
    spentSword.collect(hero.belongings.backpack);
    level.runScabbardGeneration();
    assertEquals(1, countHeapedItems(level, LakeSword.Scabbard.class));
}

@Test
public void sheathedOnlyInventoryCreatesNoScabbard() {
    new LakeSword().collect(hero.belongings.backpack);
    level.runScabbardGeneration();
    assertEquals(0, countHeapedItems(level, LakeSword.Scabbard.class));
}
```

多把无鞘武器仍断言当前楼层只有一个；在模拟下一份新 `RegularLevel` 时再次生成一个。另加落点回退测试：令 `randomDropCell()` 返回 -1，仍断言剑鞘被放入第一个合法普通格；再用类层级/source guard 断言 Boss、商店、任务与特殊固定楼层没有显式调用剑鞘生成方法。

- [ ] **Step 2: 运行测试确认生成钩子缺失**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.LakeSwordGenerationTest`

Expected: FAIL，缺少 `lakeSwordScabbardGenerationEnabled` 或没有剑鞘堆。

- [ ] **Step 3: 在普通楼层 createItems 加入独立随机域**

紧接干枯玫瑰花瓣的独立随机域之后加入：

```java
Random.pushGenerator(Random.Long());
    createLakeSwordScabbard();
Random.popGenerator();
```

并在 `RegularLevel` 定义：

```java
protected boolean lakeSwordScabbardGenerationEnabled() {
    return Dungeon.branch == 0;
}

protected void createLakeSwordScabbard() {
    if (!lakeSwordScabbardGenerationEnabled() || Dungeon.hero == null
            || !LakeSword.hasUnsheathedSword(Dungeon.hero)) return;
    int cell = lakeSwordScabbardDropCell();
    drop(new LakeSword.Scabbard(), cell).type = Heap.Type.HEAP;
    if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
        map[cell] = Terrain.GRASS;
        losBlocking[cell] = false;
    }
}

protected int lakeSwordScabbardDropCell() {
    int cell = randomDropCell();
    if (cell != -1) return cell;
    for (int i = 0; i < length(); i++) {
        if (passable[i] && !solid[i] && i != entrance() && i != exit()
                && heaps.get(i) == null && findMob(i) == null) return i;
    }
    return entrance();
}
```

`createItems()` 只在楼层首次构建运行，因此不要把同一逻辑放到 `onLevelLoad` 或跨层恢复流程。

- [ ] **Step 4: 明确高塔普通楼层与最终商店层资格**

在 `TowerLevel` 与干枯玫瑰覆写相邻的位置加入：

```java
@Override
protected boolean lakeSwordScabbardGenerationEnabled() {
    return true;
}
```

高塔继续通过 `super.createItems()` 进入相同逻辑；不在 `TowerBossLevel` 或其他固定高塔场景重复添加。由于 `LastShopLevel` 也继承 `RegularLevel`，必须在该类显式覆写：

```java
@Override
protected boolean lakeSwordScabbardGenerationEnabled() {
    return false;
}
```

- [ ] **Step 5: 运行楼层和高塔回归测试**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.LakeSwordGenerationTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevelGenerationTest`

Expected: PASS；主地牢普通层、高塔普通层各生成一次，其他状态不生成。

- [ ] **Step 6: 提交楼层生成机制**

```bash
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/RegularLevel.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/LastShopLevel.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/LakeSwordGenerationTest.java
git diff --cached --check
git commit -m "feat: generate lost lake sword scabbards"
```

### Task 7: 实现决斗家武技“风王加护”与快捷动作优先级

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java`

**Interfaces:**
- Consumes: `MeleeWeapon.beforeAbilityUsed/afterAbilityUsed`、`Invisibility`、`Haste`、Task 4 的 `defaultAction()`。
- Produces: `windProtectionDurationForLevel(int)`、`applyWindProtection(Hero)`、`duelistAbility(Hero,Integer)`。

- [ ] **Step 1: 写持续时间、充能和状态不变失败测试**

```java
@Test
public void windProtectionUsesBuffedLevelAndDoesNotChangeSwordState() {
    sword.enterCharged(null, 0);
    sword.exhaustMagic();
    sword.applyWindProtection(hero);
    assertNotNull(hero.buff(Invisibility.class));
    assertEquals(LakeSword.windProtectionDurationForLevel(sword.buffedLvl()),
            hero.buff(Invisibility.class).visualcooldown(), 0.001f);
    assertNotNull(hero.buff(Haste.class));
    assertEquals(5f, hero.buff(Haste.class).visualcooldown(), 0.001f);
    assertEquals(LakeSword.State.SPENT, sword.state());
}

@Test
public void defaultActionPrefersDrawThenAbility() {
    assertEquals(LakeSword.AC_DRAW, sword.defaultAction());
    sword.enterCharged(null, 0);
    sword.exhaustMagic();
    assertEquals(MeleeWeapon.AC_ABILITY, sword.defaultAction());
}
```

另通过受控 `Charger` 断言 `baseChargeUse` 为 1，武技执行后充能减少 1、英雄未增加已用回合；三种状态都能调用且状态不变。

- [ ] **Step 2: 运行测试确认武技未实现**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: FAIL，缺少持续时间和 Buff。

- [ ] **Step 3: 实现无目标、零耗时武技**

```java
public static int windProtectionDurationForLevel(int level) {
    return Math.max(1, 2 + level);
}

void applyWindProtection(Hero hero) {
    Buff.prolong(hero, Invisibility.class, windProtectionDurationForLevel(buffedLvl()));
    Buff.prolong(hero, Haste.class, 5f);
}

@Override protected void duelistAbility(Hero hero, Integer ignored) {
    beforeAbilityUsed(hero, null);
    applyWindProtection(hero);
    hero.sprite.operate(hero.pos);
    hero.next();
    afterAbilityUsed(hero);
}

@Override protected int baseChargeUse(Hero hero, Char target) { return 1; }
```

不调用 `hero.spend`。`hero.next()` 只结束无耗时动画流程，沿用当前 6 阶无目标武技模式。`targetingPrompt()` 保持 `null`，因此不会打开目标选择器。

- [ ] **Step 4: 完成默认动作边界**

确认 Task 4 的优先级仅在 `SHEATHED && canDraw(hero)` 时返回 `AC_DRAW`；有鞘但诅咒主手阻止自动换装时，不错误返回不可执行的默认拔剑，应退到已装备可用的武技或父类动作。`CHARGED/SPENT` 状态在武技可用时返回 `AC_ABILITY`。

- [ ] **Step 5: 运行武技和通用武技回归测试**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeaponAbilityNameTest --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeaponChargerTest`

Expected: PASS。

- [ ] **Step 6: 提交武技**

```bash
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java
git diff --cached --check
git commit -m "feat: add lake sword wind protection ability"
```

### Task 8: 注册生成器、日志与中英文文本

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java`
- Modify: `core/src/main/assets/messages/items/items.properties`
- Modify: `core/src/main/assets/messages/items/items_zh.properties`
- Modify: `core/src/main/assets/messages/custom/custom.properties`
- Modify: `core/src/main/assets/messages/custom/custom_zh.properties`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponIntegrationTest.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponTextTest.java`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java`

**Interfaces:**
- Consumes: 完整 `LakeSword` 和 `EXItemSpriteSheet.LAKE_SWORD_SHEATHED`。
- Produces: WEP_T6 等权重登记、`melee_lakesword` 资料项、所有运行时消息键。

- [ ] **Step 1: 先扩展生成器与文本失败测试**

在 `Tier6WeaponIntegrationTest` 的两个 6 阶期望数组末尾加入 `LakeSword.class`，概率数组扩为 14 个 `1`。在 `Tier6WeaponTextTest.GUIDE_KEYS` 加入 `melee_lakesword`，并新增状态/剑鞘/动作消息键检查：

```java
String[] lakeSwordKeys = {
    "items.weapon.melee.tier6.lakesword.name",
    "items.weapon.melee.tier6.lakesword.desc",
    "items.weapon.melee.tier6.lakesword.state_sheathed",
    "items.weapon.melee.tier6.lakesword.state_charged",
    "items.weapon.melee.tier6.lakesword.state_spent",
    "items.weapon.melee.tier6.lakesword.ac_draw",
    "items.weapon.melee.tier6.lakesword.prompt",
    "items.weapon.melee.tier6.lakesword.cannot_draw",
    "items.weapon.melee.tier6.lakesword.ability_name",
    "items.weapon.melee.tier6.lakesword.typical_ability_desc",
    "items.weapon.melee.tier6.lakesword.ability_desc",
    "items.weapon.melee.tier6.lakesword$scabbard.name",
    "items.weapon.melee.tier6.lakesword$scabbard.desc",
    "items.weapon.melee.tier6.lakesword$scabbard.no_sword"
};
```

同时断言 `custom.dict.dict.melee_lakesword_d` 同时包含 `_6_阶`、力量需求、基础伤害、伤害成长、精准修正、延迟、距离、拔剑、魔力充盈、剑鞘生成和 `_决斗家_武技`。

- [ ] **Step 2: 运行测试确认登记与文本缺失**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponIntegrationTest --tests com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponTextTest`

Expected: FAIL，生成器数组和消息键不包含湖中剑。

- [ ] **Step 3: 注册生成池和字典**

在 `Generator` 导入并把 `LakeSword.class` 追加到 `WEP_T6.classes`，将 `defaultProbs` 同步扩为 14 个 `1`。在 `DictionaryJournal` 加入：

```java
WEAPONS.d.put("melee_lakesword", EXItemSpriteSheet.LAKE_SWORD_SHEATHED);
```

`TestMelee.weaponList(6)` 继续从 `Generator.Category.WEP_T6.classes` 取得结果，不新增测试工具专用分支。

- [ ] **Step 4: 增加简短物品文本和动态状态描述**

中文普通文本使用以下信息密度：

```properties
items.weapon.melee.tier6.lakesword.name=湖中剑
items.weapon.melee.tier6.lakesword.desc=这是一把由湖中仙灵相赠的宝剑，其上的魔法纹路你从未见过。
items.weapon.melee.tier6.lakesword.state_sheathed=这把剑正紧紧插在剑鞘中。装备时，剑鞘会加快你的自然恢复；拔剑则能释放一道贯穿前方的攻击，但剑鞘也会随之遗失。
items.weapon.melee.tier6.lakesword.state_charged=剑鞘已经遗失，剑刃正萦绕着强大的魔力，暂时拥有更高的伤害与攻击距离。
items.weapon.melee.tier6.lakesword.state_spent=剑刃上的魔力已经散尽，如同不知被何人投影出的赝品。找回剑鞘后，它才能恢复原状。
items.weapon.melee.tier6.lakesword.ac_draw=拔剑
items.weapon.melee.tier6.lakesword.prompt=选择拔剑攻击的方向
items.weapon.melee.tier6.lakesword.cannot_draw=当前主武器无法卸下，你不能拔出湖中剑。
items.weapon.melee.tier6.lakesword.ability_name=风王加护
items.weapon.melee.tier6.lakesword.typical_ability_desc=决斗家可消耗_1_点充能获得短暂隐身与5回合急速。该武技不消耗时间。
items.weapon.melee.tier6.lakesword.ability_desc=决斗家可消耗_1_点充能获得短暂隐身与5回合急速。该武技不消耗时间。
items.weapon.melee.tier6.lakesword.stats_desc=湖中剑会根据剑鞘与魔力状态改变效果，详细规则可在资料中查看。
items.weapon.melee.tier6.lakesword$scabbard.name=湖中剑鞘
items.weapon.melee.tier6.lakesword$scabbard.desc=湖中剑遗失的剑鞘。拾取后会立即为你持有的一把无鞘湖中剑恢复原状。
items.weapon.melee.tier6.lakesword$scabbard.no_sword=你没有需要这副剑鞘的湖中剑。
```

英文文件写入以下对应文本，不保留空值：

```properties
items.weapon.melee.tier6.lakesword.name=sword of the lake
items.weapon.melee.tier6.lakesword.desc=A wondrous sword bestowed by a spirit of the lake. Its arcane patterns are unlike any you have seen.
items.weapon.melee.tier6.lakesword.state_sheathed=The sword is held tightly in its scabbard. While equipped, the scabbard hastens natural recovery. Drawing the sword unleashes a piercing attack, but the scabbard will be lost.
items.weapon.melee.tier6.lakesword.state_charged=The scabbard is gone, and potent magic coils around the blade, temporarily increasing its damage and reach.
items.weapon.melee.tier6.lakesword.state_spent=The blade's magic has faded, leaving something like an unknown projection. Recovering its scabbard will restore it.
items.weapon.melee.tier6.lakesword.ac_draw=draw
items.weapon.melee.tier6.lakesword.prompt=Choose the direction of the draw attack
items.weapon.melee.tier6.lakesword.cannot_draw=Your current primary weapon cannot be removed, so the sword cannot be drawn.
items.weapon.melee.tier6.lakesword.ability_name=wind protection
items.weapon.melee.tier6.lakesword.typical_ability_desc=The Duelist can spend _1 charge_ to gain brief invisibility and 5 turns of haste. This ability takes no time.
items.weapon.melee.tier6.lakesword.ability_desc=The Duelist can spend _1 charge_ to gain brief invisibility and 5 turns of haste. This ability takes no time.
items.weapon.melee.tier6.lakesword.stats_desc=The sword changes its effects according to the state of its scabbard and magic. Full rules are recorded in the journal.
items.weapon.melee.tier6.lakesword$scabbard.name=lake sword scabbard
items.weapon.melee.tier6.lakesword$scabbard.desc=The lost scabbard of the sword of the lake. Picking it up immediately restores one unsheathed sword you carry.
items.weapon.melee.tier6.lakesword$scabbard.no_sword=You carry no sword that needs this scabbard.
```

`LakeSword.desc()` 返回 `super.desc()` 加当前状态对应的 `state_sheathed/state_charged/state_spent`；`statsInfo()` 和 `abilityInfo()` 分别读取上述短文本。

- [ ] **Step 5: 增加 custom 中英文详细资料**

中文资料必须明确写出：`5+武器等级~30+7×武器等级`、拔剑距离、穿透所有地形、只攻击敌对单位、破坏可燃地形、每目标必中普通攻击、强化时长与效果、装备有鞘恢复翻倍、丢弃耗尽、剑鞘每层生成和恢复优先级，以及风王加护公式。英文资料逐项对应，不能把 `level` 缩写为 `L`。

```properties
custom.dict.dict.melee_lakesword =湖中剑
custom.dict.dict.melee_lakesword_d =_6_阶近战武器，基础力量需求_20_。基础伤害为_5~30_，每级成长为_1~7_；精准修正_1_，攻击延迟_1_，基础距离_1_。\n\n有剑鞘并装备在主手或副手时，最终自然恢复速度提高到_2倍_，多把不叠加且不会绕过饥饿等恢复限制。_拔剑_会向所选方向发射最长_5+武器等级_格的射线，穿过角色和所有地形，破坏可燃障碍，并分别对路径上的每个敌对单位进行一次必中普通武器攻击。动作取消不会装备或耗时；从背包成功拔剑时会无额外耗时换入主手。\n\n拔剑后进入_魔力充盈_，固定持续_4+拔剑时武器等级_回合；期间仅湖中剑的普通近战攻击获得_1.5倍武器掷伤_和_+1攻击距离_。换下武器不会暂停计时，丢弃会立即耗尽魔力。无鞘时，后续首次生成的普通主地牢或高塔楼层会出现一个剑鞘；拾取一个剑鞘只恢复一把湖中剑，顺序为主手、副手、背包。\n\n_决斗家_武技(风王加护)：消耗_1_点充能且不消耗回合，获得_2+武器等级_回合隐身和_5_回合急速，不改变湖中剑状态。
```

英文资料写入：

```properties
custom.dict.dict.melee_lakesword=sword of the lake
custom.dict.dict.melee_lakesword_d=A _tier-6_ melee weapon with a base _20 strength requirement_. It deals _5+weapon level to 30+7×weapon level damage_, with _1 accuracy_, _1 attack delay_, and _1 base reach_.\n\nWhile a sheathed sword is equipped in either weapon slot, final natural regeneration is _twice as fast_. Multiple swords do not stack, and this never bypasses starvation or other regeneration blocks. _Draw_ fires a ray up to _5+weapon level tiles_ in the chosen direction. It crosses characters and every kind of terrain, destroys flammable obstacles, and makes one guaranteed-hit normal weapon attack against every enemy on the path. Cancelling neither equips the sword nor consumes time; drawing from the backpack equips it to the primary slot without extra time.\n\nAfter drawing, the sword is _charged_ for a fixed _4+weapon level at the time of drawing turns_. Only its normal melee attacks gain _1.5× rolled weapon damage_ and _+1 reach_. Switching weapons does not pause the timer, and dropping the sword immediately exhausts the magic. While unsheathed, each later newly generated ordinary dungeon or tower floor creates one scabbard. One scabbard restores one sword in primary, secondary, then backpack order.\n\n_Duelist ability (Wind Protection):_ costs _1 charge_ and no time, granting invisibility for _2+weapon level turns_ and _5 turns of haste_ without changing the sword's state.
```

- [ ] **Step 6: 运行登记和文本测试**

Run: `./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponIntegrationTest --tests com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponTextTest --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest`

Expected: PASS，动态描述随状态切换且生成器、日志、TestMelee 与中英文资料均可达。

- [ ] **Step 7: 提交登记和文案**

```bash
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java core/src/main/assets/messages/items/items.properties core/src/main/assets/messages/items/items_zh.properties core/src/main/assets/messages/custom/custom.properties core/src/main/assets/messages/custom/custom_zh.properties core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponIntegrationTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/Tier6WeaponTextTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSwordTest.java
git diff --cached --check
git commit -m "feat: register and localize lake sword"
```

### Task 9: 完整回归与游戏内验收

**Files:**
- Verify: 本计划列出的全部生产、资源和测试文件。

**Interfaces:**
- Consumes: Task 1～8 的完整功能。
- Produces: 可复现的自动化测试结果和游戏内三态验收记录。

- [ ] **Step 1: 运行湖中剑全部专项测试**

Run:

```bash
./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSwordTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RegenerationLakeSwordTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.LakeSwordGenerationTest --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest --tests com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponIntegrationTest --tests com.shatteredpixel.shatteredpixeldungeon.items.Tier6WeaponTextTest
```

Expected: PASS，零失败。

- [ ] **Step 2: 运行武器、楼层、恢复和资源邻近回归**

Run:

```bash
./gradlew.bat core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.* --tests com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevelTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevelGenerationTest --tests com.shatteredpixel.shatteredpixeldungeon.items.WeaponRecipeTest
```

Expected: PASS；现有 6 阶武器、玫瑰花瓣、高塔生成和普通近战生命周期不受影响。

- [ ] **Step 3: 运行 core 完整测试**

Run: `./gradlew.bat core:test`

Expected: `BUILD SUCCESSFUL`。若存在与本任务无关的既有失败，保存完整失败类名和堆栈，并再次运行专项测试证明湖中剑用例仍全部通过。

- [ ] **Step 4: 在 TestMelee 中进行游戏内状态验收**

按以下固定顺序检查：

1. 生成 0 级与高等级湖中剑，核对面板、贴图和短描述。
2. 把有鞘湖中剑分别放在背包、主手、副手并观察自然恢复；背包不加速，主副手加速且双持不叠加。
3. 从背包拔剑，分别测试取消、点自身、诅咒主手、普通直线、墙后敌人、路径盟友、魅惑敌人、可燃障碍和不可燃墙。
4. 确认一次拔剑只前进一次普通攻击时间，目标分别触发附魔，射线后进入充盈。
5. 充盈期间验证伤害和距离，换武器等待仍倒计时，丢弃立即耗尽。
6. 使用风王加护，核对充能减少 1、时间不前进、隐身和 5 回合急速。
7. 进入主地牢普通新楼层和高塔普通新楼层，确认各出现一个剑鞘；返回旧楼层不重复创建。
8. 携带主手、副手、背包三把无鞘湖中剑依次拾取剑鞘，核对恢复顺序与每个剑鞘只恢复一把。
9. 存档并重载 `CHARGED` 状态，确认贴图、剩余时间、快捷动作和追踪器继续正常。

- [ ] **Step 5: 检查最终差异范围与工作树保护**

Run:

```bash
git diff --check
git status --short
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Regeneration.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/RegularLevel.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/LastShopLevel.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java
```

Expected: 无空白错误；没有任务范围外文件被修改；任何原有未提交改动仍被保留。

- [ ] **Step 6: 提交只包含验收修正的最终变更**

若 Step 1～5 为通过测试而产生了湖中剑范围内的小修正，仅暂存这些修正并提交：

```bash
git diff --cached --check
git commit -m "test: complete lake sword regression coverage"
```

若没有新增修正，则不创建空提交。
