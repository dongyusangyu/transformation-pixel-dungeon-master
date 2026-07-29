# 提取卷轴实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增具有独立 EX 精灵、固定名称和背包选择流程的提取卷轴，将装备记录的升级制品次数转换回升级卷轴并安全清除对应等级与计数。

**架构：** `ScrollOfExtraction` 继承 `InventoryScroll` 复用卷轴消耗和选择器流程，将筛选与数值提取拆成可单测的包级静态方法。精灵以 `EXItemSpriteSheet` 编码帧 `32` 读取，PNG 由确认过的 `SCROLL_META` 帧复制后仅修改中心 `5x5` 符号。

**技术栈：** Java、JUnit 4、LibGDX/Noosa、Java ImageIO、Gradle、RGBA PNG 像素精灵表。

---

## 文件结构

- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtraction.java`
  - 固定识别、物品筛选、提取算法、升级卷轴返还与日志。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtractionTest.java`
  - 筛选、等级下限、返还数量和计数清零的行为测试。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
  - 公开提取卷轴帧常量。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`
  - 验证 EX 帧编码与坐标。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ExtractionScrollSpriteTest.java`
  - 验证精灵表尺寸、META 基底复用范围及中心符号变化。
- 修改：`core/src/main/assets/sprites/ex_items.png`
  - 在 `(0,32)` 写入确认的 16x16 精灵。
- 修改：`core/src/main/assets/messages/items/items.properties`
  - 英文名称、描述、选择器和反馈文本。
- 修改：`core/src/main/assets/messages/items/items_zh.properties`
  - 简体中文名称、描述、选择器和反馈文本。

### 任务 1：锁定精灵帧接口和 PNG 约束

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ExtractionScrollSpriteTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- 修改：`core/src/main/assets/sprites/ex_items.png`

- [ ] **步骤 1：编写失败的帧编码测试**

在 `EXItemSpriteSheetTest` 中添加：

```java
@Test
public void extractionScrollStartsASeparateThirdRow() {
    assertEquals(32,
            EXItemSpriteSheet.frameFor(EXItemSpriteSheet.SCROLL_EXTRACTION));
    assertEquals(0,
            EXItemSpriteSheet.frameX(EXItemSpriteSheet.SCROLL_EXTRACTION));
    assertEquals(32,
            EXItemSpriteSheet.frameY(EXItemSpriteSheet.SCROLL_EXTRACTION));
}
```

- [ ] **步骤 2：编写失败的 PNG 像素测试**

`ExtractionScrollSpriteTest` 使用 `ImageIO.read` 加载 `items.png` 与 `ex_items.png`，断言：

```java
assertEquals(256, exItems.getWidth());
assertEquals(512, exItems.getHeight());
assertEquals(meta.getRGB(x, y), extraction.getRGB(x, y)); // 5x5 中心之外
assertNotEquals(meta.getRGB(7, 5), extraction.getRGB(7, 5)); // 中心符号
```

中心区域定义为 `x=5..9, y=4..8`；目标 EX 原点为 `(0,32)`；META 原点为 `(240,288)`。

- [ ] **步骤 3：运行测试验证红灯**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest --tests com.shatteredpixel.shatteredpixeldungeon.sprites.ExtractionScrollSpriteTest
```

预期：`SCROLL_EXTRACTION` 不存在，测试编译失败。

- [ ] **步骤 4：添加最少帧常量**

在 `EXItemSpriteSheet` 中添加：

```java
public static final int SCROLL_EXTRACTION = encode(32);
```

- [ ] **步骤 5：生成正式 16x16 像素**

逐像素复制 `(240,288,16,16)` 到 `(0,32,16,16)`，将 `x=5..9, y=4..8` 清为 META 纸张底色，再绘制 B“晶核上浮”：

```text
..*..
.ccc.
..c..
d...d
ddddd
```

其中 `*` 为亮青高光、`c` 为青色升级能量、`d` 为深色容器。保存时保持 `256x512` RGBA PNG，禁止插值和抗锯齿。

- [ ] **步骤 6：运行精灵测试验证绿灯**

运行任务 1 步骤 3 的命令，预期相关测试全部通过。

### 任务 2：用纯逻辑测试驱动提取算法

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtractionTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtraction.java`

- [ ] **步骤 1：编写筛选和提取失败测试**

测试定义一个最小 `TestUpgradableItem extends Item`，覆盖 `isUpgradable()` 返回配置值，并覆盖 `degrade()` 调用父类。覆盖：

```java
assertFalse(ScrollOfExtraction.canExtract(itemWithZeroUses));
assertFalse(ScrollOfExtraction.canExtract(nonUpgradableItem));
assertTrue(ScrollOfExtraction.canExtract(upgradableItemWithUses));
assertEquals(3, ScrollOfExtraction.extractUpgradeUses(level5Uses3));
assertEquals(2, level5Uses3.trueLevel());
assertEquals(0, level5Uses3.upgradeScrollUses);
assertEquals(5, ScrollOfExtraction.extractUpgradeUses(level2Uses5));
assertEquals(0, level2Uses5.trueLevel());
```

- [ ] **步骤 2：运行测试验证红灯**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtractionTest
```

预期：`ScrollOfExtraction` 不存在，测试编译失败。

- [ ] **步骤 3：实现最少纯逻辑**

在新类中添加：

```java
static boolean canExtract(Item item) {
    return item.isUpgradable() && item.upgradeScrollUses > 0;
}

static int extractUpgradeUses(Item item) {
    int extracted = item.upgradeScrollUses;
    item.degrade(Math.min(Math.max(0, item.trueLevel()), extracted));
    item.upgradeScrollUses = 0;
    return extracted;
}
```

- [ ] **步骤 4：运行测试验证绿灯**

运行任务 2 步骤 2 的命令，预期全部通过。

### 任务 3：接入非匿名卷轴与背包返还流程

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtraction.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtractionTest.java`

- [ ] **步骤 1：编写结构与流程失败测试**

增加源码契约断言，验证新卷轴：

```java
assertTrue(source.contains("extends InventoryScroll"));
assertTrue(source.contains("return true;")); // isKnown
assertTrue(source.contains("image = EXItemSpriteSheet.SCROLL_EXTRACTION"));
assertTrue(source.contains("new ScrollOfUpgrade().quantity(extracted)"));
assertFalse(source.contains("anonymous = true"));
```

- [ ] **步骤 2：运行测试验证红灯**

运行任务 2 步骤 2 的命令，预期因完整流程缺失而失败。

- [ ] **步骤 3：实现固定卷轴与返还**

实现：

```java
@Override
public boolean isKnown() {
    return true;
}

@Override
public void reset() {
    super.reset();
    image = EXItemSpriteSheet.SCROLL_EXTRACTION;
}

@Override
protected boolean usableOnItem(Item item) {
    return canExtract(item);
}

@Override
protected void onItemSelected(Item item) {
    int extracted = extractUpgradeUses(item);
    Item upgrades = new ScrollOfUpgrade().quantity(extracted);
    if (!upgrades.collect(curUser.belongings.backpack)) {
        Dungeon.level.drop(upgrades, curUser.pos);
    }
    Item.updateQuickslot();
    GLog.p(Messages.get(this, "extract", extracted));
}
```

初始化块设置 `preferredBag = Belongings.Backpack.class` 和升级卷轴图标；不得设置 `anonymous`。

- [ ] **步骤 4：运行测试验证绿灯**

运行任务 2 步骤 2 的命令，预期全部通过。

### 任务 4：补充本地化并验证键

**文件：**
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtractionTest.java`

- [ ] **步骤 1：增加失败的属性键测试**

读取两份属性文件并断言以下键各出现一次：

```text
items.scrolls.scrollofextraction.name
items.scrolls.scrollofextraction.desc
items.scrolls.scrollofextraction.inv_title
items.scrolls.scrollofextraction.warning
items.scrolls.scrollofextraction.yes
items.scrolls.scrollofextraction.no
items.scrolls.scrollofextraction.extract
```

- [ ] **步骤 2：运行测试验证红灯**

运行任务 2 步骤 2 的命令，预期缺少属性键。

- [ ] **步骤 3：添加英文和简体中文文本**

中文核心文案：

```properties
items.scrolls.scrollofextraction.name=提取卷轴
items.scrolls.scrollofextraction.inv_title=选择要提取升级的物品
items.scrolls.scrollofextraction.extract=你从这件物品中提取出了%d张升级卷轴。
```

`desc` 明确返还数量、等级最低为零和记录清零；`warning/yes/no` 遵循现有 `InventoryScroll` 取消确认格式。

- [ ] **步骤 4：运行测试验证绿灯**

运行任务 2 步骤 2 的命令，预期全部通过。

### 任务 5：最终验证与视觉检查

**文件：**
- 检查本计划列出的全部文件。

- [ ] **步骤 1：运行差异与 PNG 完整性检查**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtraction.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtractionTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ExtractionScrollSpriteTest.java core/src/main/assets/messages/items/items.properties core/src/main/assets/messages/items/items_zh.properties
```

预期：无空白错误。

- [ ] **步骤 2：运行定向测试**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtractionTest --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest --tests com.shatteredpixel.shatteredpixeldungeon.sprites.ExtractionScrollSpriteTest
```

预期：全部通过；若工作区其他未完成测试阻塞 `compileTestJava`，记录具体无关错误并继续生产编译。

- [ ] **步骤 3：运行生产编译**

```powershell
.\gradlew.bat :core:compileJava :android:compileDebugJavaWithJavac
```

预期：`BUILD SUCCESSFUL`。

- [ ] **步骤 4：目视检查**

裁出 `ex_items.png` 的 `(0,32,16,16)` 并以 1x 与 8x 查看，确认：

- 卷轴轮廓与 `SCROLL_META` 完全一致
- 只有中心 `5x5` 区域变化
- 晶核与容器在 1x 下均可辨认
- 无半透明脏边、插值像素或越界像素

- [ ] **步骤 5：审阅最终差异**

确认没有修改 `Generator.Category.SCROLL`、普通卷轴符文表、升级计数规则或工作区其他并行文件。

> 当前工作区包含大量用户并行改动，本计划执行期间不自动提交生产文件，避免把同一未跟踪文件中的他人工作一并提交；仅在用户明确要求后再进行精确暂存。
