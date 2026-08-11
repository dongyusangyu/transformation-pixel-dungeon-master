# 恶魔尾鞭实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `subagent-driven-development`（推荐）或 `executing-plans` 逐任务实现本计划。步骤使用复选框跟踪进度。

**目标：** 实装六阶近战武器“恶魔尾鞭”，包括成长射程、50% 随机负面效果、长鞭“狠抽”武技、生成器、双语文本和 14×14 盘绕式贴图。

**架构：** 新武器保持为一个专注的 `DemonTailWhip` 类，复用 `Whip.WhipAbility`，并在类内维护两个不可变的负面效果权重池。概率边界、射程增长和效果池过滤拆成包可见的纯辅助方法，以便进行无随机波动的单元测试。贴图只写入 `ex_items.png` 索引 154，代码通过 `EXItemSpriteSheet` 的实测尺寸编码读取。

**技术栈：** Java、JUnit 4、Gradle、PNG/16×16 像素表、Pillow（仅用于确定性写入目标贴图单元格）。

---

## 文件结构

- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhip.java` —— 武器面板、射程、负面效果和武技委托。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhipTest.java` —— 面板、概率池、回退、武技和生成器测试。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java` —— 定义索引 154、14×14 的 `DEMON_TAIL_WHIP`。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java` —— 追加六阶生成类别和等权概率。
- 修改：`core/src/main/assets/messages/items/items.properties` —— 英文名称、描述与武技文本。
- 修改：`core/src/main/assets/messages/items/items_zh.properties` —— 中文名称、描述与武技文本。
- 修改：`core/src/main/assets/sprites/ex_items.png` —— 仅写入索引 154 的 16×16 单元格。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java` —— 在最终工作区状态中登记索引 154，并保留索引 153 的并行武器断言。
- 创建（忽略目录）：`build/brainstorm/demon-tail-whip-20260811/render_demon_tail_whip.py` —— 从钢鞭轮廓生成不含部分透明度的恶魔配色，并在写回前后核对目标外像素。

## 任务 1：建立基线并编写武器面板失败测试

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhipTest.java`
- 读取：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/Whip.java`
- 读取：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/wands/WandOfCorruption.java`

- [ ] **步骤 1：记录共享工作区基线**

运行：

```powershell
git status --short
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/main/assets/sprites/ex_items.png core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java
```

确认索引 153 的 `ORACLE_TERMINAL` 及任何新加入的六阶生成器条目均被保留。后续每次修改共享文件前都重新读取对应片段。

- [ ] **步骤 2：编写面板与边界测试**

测试应直接表达已确认数值：

```java
@Test
public void tierSixStatsAndReachGrowthMatchSpecification() {
    TestableDemonTailWhip weapon = new TestableDemonTailWhip();
    assertEquals(6, DemonTailWhip.TIER);
    assertEquals(20, DemonTailWhip.strengthRequirementForLevel(0));
    assertEquals(6, weapon.min(0));
    assertEquals(30, weapon.max(0));
    assertEquals(13, weapon.min(7));
    assertEquals(72, weapon.max(7));
    assertEquals(0, DemonTailWhip.reachBonusForLevel(6));
    assertEquals(1, DemonTailWhip.reachBonusForLevel(7));
    assertEquals(1, DemonTailWhip.reachBonusForLevel(13));
    assertEquals(2, DemonTailWhip.reachBonusForLevel(14));
    assertEquals(0, DemonTailWhip.reachBonusForLevel(-7));
    assertEquals(3, weapon.baseRange());
}
```

测试子类仅暴露受保护的 `RCH`，不复制生产逻辑。

- [ ] **步骤 3：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhipTest"
```

预期：FAIL，编译器报告 `DemonTailWhip` 尚不存在。

## 任务 2：实现最小武器类与贴图常量

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhip.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhipTest.java`

- [ ] **步骤 1：定义贴图常量且不覆盖索引 153**

在第 10 行六阶武器常量末尾追加：

```java
public static final int DEMON_TAIL_WHIP = encode(154, 14, 14);
```

- [ ] **步骤 2：实现面板、射程和武技委托**

类的核心结构：

```java
public class DemonTailWhip extends MeleeWeapon {
    public static final int TIER = 6;
    public static final int BASE_RANGE = 3;

    {
        image = EXItemSpriteSheet.DEMON_TAIL_WHIP;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        tier = TIER;
        RCH = BASE_RANGE;
    }

    @Override
    public int min(int level) {
        return 6 + level;
    }

    @Override
    public int max(int level) {
        return 30 + 6 * level;
    }

    public static int reachBonusForLevel(int level) {
        return Math.max(0, level) / 7;
    }

    @Override
    public int reachFactor(Char owner) {
        return super.reachFactor(owner) + reachBonusForLevel(buffedLvl());
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        Whip.WhipAbility(hero, target, 1f, 0, this);
    }
}
```

同时加入 `strengthRequirementForLevel`、`abilityInfo` 和 `upgradeAbilityStat`，其格式与 `Whip` 一致。

- [ ] **步骤 3：运行面板测试确认绿灯**

运行同任务 1 的定向命令。

预期：面板与射程测试 PASS；尚未加入的负面效果测试不存在。

- [ ] **步骤 4：提交最小武器骨架**

```powershell
git add -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhip.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhipTest.java
git commit -m "feat: 添加恶魔尾鞭基础面板"
```

## 任务 3：以确定性测试驱动随机负面效果

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhipTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhip.java`

- [ ] **步骤 1：增加概率、权重和过滤失败测试**

不做易抖动的万次随机统计；测试确定性边界和生产代码实际使用的权重表：

```java
assertFalse(DemonTailWhip.debuffTriggers(0.50f));
assertTrue(DemonTailWhip.debuffTriggers(0.4999f));
assertEquals(DebuffTier.MINOR, DemonTailWhip.preferredTier(0.7499f));
assertEquals(DebuffTier.MAJOR, DemonTailWhip.preferredTier(0.75f));
assertEquals(2f, DemonTailWhip.minorDebuffWeights().get(Weakness.class), 0f);
assertEquals(3f, DemonTailWhip.majorDebuffWeights().get(Amok.class), 0f);
assertEquals(4f, DemonTailWhip.durationFor(DebuffTier.MINOR), 0f);
assertEquals(2f, DemonTailWhip.durationFor(DebuffTier.MAJOR), 0f);
```

使用一个最小 `TestChar extends Char` 验证 `availableDebuffs` 会排除已附加的 `Weakness` 和测试角色声明免疫的 `Vulnerable`。再通过包可见的 `poolOrder` 验证轻度为空时尝试重度、重度为空时尝试轻度。

- [ ] **步骤 2：运行测试确认新断言失败**

运行定向 `DemonTailWhipTest`。

预期：FAIL，报告概率与效果池辅助方法不存在。

- [ ] **步骤 3：实现两个加权池和安全回退**

生产代码使用 `LinkedHashMap<Class<? extends FlavourBuff>, Float>` 保存腐化法杖的实际非零权重。每次命中流程为：

```java
@Override
public int proc(Char attacker, Char defender, int damage) {
    int result = super.proc(attacker, defender, damage);
    if (defender.isAlive() && debuffTriggers(Random.Float())) {
        tryApplyRandomDebuff(defender, preferredTier(Random.Float()));
    }
    return result;
}
```

`tryApplyRandomDebuff` 先过滤已有与免疫效果，再用 `Random.chances` 选择；首选池为空时只回退一次，两个池均空时直接返回 `false`。使用 `Buff.append(target, selected, durationFor(tier))`，不进入腐化或臣服流程。

- [ ] **步骤 4：验证测试与代码质量**

运行：

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhipTest"
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhip.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhipTest.java
```

预期：PASS，且 `git diff --check` 无输出。

- [ ] **步骤 5：提交负面效果实现**

仅暂存上述两个文件并提交：

```powershell
git commit -m "feat: 为恶魔尾鞭添加随机负面效果"
```

## 任务 4：生成器与双语文本集成

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java`
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/DemonTailWhipTest.java`

- [ ] **步骤 1：编写生成器与文本失败测试**

断言 `Generator.Category.WEP_T6.classes` 包含 `DemonTailWhip.class`、类别和概率数组等长且所有权重相同。读取两份 properties，断言以下键各出现一次：

```text
items.weapon.melee.tier6.demontailwhip.name
items.weapon.melee.tier6.demontailwhip.ability_name
items.weapon.melee.tier6.demontailwhip.ability_no_target
items.weapon.melee.tier6.demontailwhip.typical_ability_desc
items.weapon.melee.tier6.demontailwhip.ability_desc
items.weapon.melee.tier6.demontailwhip.desc
items.weapon.melee.tier6.demontailwhip.stats_desc
```

- [ ] **步骤 2：运行测试确认缺失集成**

预期：FAIL，生成器尚未包含新类且文本键不存在。

- [ ] **步骤 3：以当前数组为基础追加生成器条目**

编辑前重新读取 `Generator.java` 的导入和 `WEP_T6` 数组，保留实现期间出现的 `OracleTerminal` 或其他条目。追加 `DemonTailWhip.class`，并在 `defaultProbs` 末尾追加一个 `1`；不得把数组重写回设计阶段看到的八项版本。

- [ ] **步骤 4：加入完整中英文文本**

中文武技说明沿用长鞭“狠抽”的伤害占位符 `%1$d`、`%2$d`；`stats_desc` 明确“命中后有 50% 概率”“每强化 7 级增加 1 格攻击距离”。英文文本等义，不增加额外背景。

- [ ] **步骤 5：运行定向测试并提交**

运行定向 `DemonTailWhipTest`，确认 PASS 后只暂存生成器、两份文本和测试文件，提交：

```powershell
git commit -m "feat: 集成恶魔尾鞭生成与文本"
```

## 任务 5：绘制并验证索引 154 的像素贴图

**文件：**
- 创建：`build/brainstorm/demon-tail-whip-20260811/render_demon_tail_whip.py`
- 修改：`core/src/main/assets/sprites/ex_items.png`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

- [ ] **步骤 1：编写贴图失败测试并保护并行索引**

编辑前重新读取 `EXItemSpriteSheetTest.java`。为 `DEMON_TAIL_WHIP` 断言：帧索引 154、宽高 14×14、目标非透明包围盒 `(0,0)-(13,13)`、最多 8 色、所有像素 alpha 仅为 0 或 255。把剩余空白断言收窄为 155–159；索引 153 的断言按当前 `ORACLE_TERMINAL` 实现保留。

- [ ] **步骤 2：运行贴图测试确认目标单元格为空**

运行：

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest"
```

预期：FAIL，索引 154 尚无非透明像素。

- [ ] **步骤 3：用钢鞭精确轮廓生成 A 方案**

渲染脚本读取 `items.png` 索引 135 的 14×14 alpha 轮廓，将所有非透明像素转为全不透明的 8 色以内调色板：深紫黑轮廓、深酒红、暗红、中红、鲜红高光、深褐握柄和暖棕握柄高光。脚本必须：

1. 在写入前确认 `ex_items.png` 索引 154 完全透明，否则立即退出。
2. 保存整张表的像素副本。
3. 只清理并写入索引 154。
4. 写入后逐像素确认目标单元格之外与副本完全一致。
5. 使用临时文件原子替换 `ex_items.png`。

- [ ] **步骤 4：视觉检查与像素验证**

生成 12×最近邻放大预览至 `build/brainstorm/demon-tail-whip-20260811/demon_tail_whip_12x.png`，使用图像查看工具确认盘绕结构、握柄方向和 1×辨识度。随后运行 `EXItemSpriteSheetTest`。

预期：PASS，无部分透明像素、无模糊边缘，索引 155–159 仍透明。

- [ ] **步骤 5：提交贴图与测试**

仅暂存 `ex_items.png`、`EXItemSpriteSheet.java` 和相关测试，提交：

```powershell
git commit -m "art: 绘制恶魔尾鞭贴图"
```

## 任务 6：最终回归、审查与交付

**文件：**
- 验证所有上述生产文件与测试文件。

- [ ] **步骤 1：运行两个定向测试类**

```powershell
.\gradlew.bat core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhipTest" --tests "com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest"
```

预期：PASS。

- [ ] **步骤 2：运行核心模块测试**

```powershell
.\gradlew.bat core:test
```

预期：PASS。若共享工作区中其他未完成任务导致失败，保存首个无关失败的文件、行号和错误，并再次运行两个定向测试证明本功能仍通过。

- [ ] **步骤 3：检查差异范围和文本编码**

```powershell
git diff --check
git status --short
git diff --stat
```

确认没有暂存或改写用户的其他文件，中文 properties 保持 UTF-8 可读，贴图之外的二进制资源未变化。

- [ ] **步骤 4：按 `requesting-code-review` 技能审查**

重点检查：`super.proc` 只调用一次、每目标独立 50% 判定、权重与持续时间一致、回退不会递归循环、`Whip.WhipAbility` 委托未复制、生成器概率等长、索引 153 未覆盖。

- [ ] **步骤 5：完成交付**

向用户报告：武器面板、概率规则、贴图索引、修改文件、定向测试和全量测试结果，并附上正式贴图的放大预览文件链接。
