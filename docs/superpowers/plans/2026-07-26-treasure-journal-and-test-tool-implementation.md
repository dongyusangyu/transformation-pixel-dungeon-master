# 藏品日志、价值描述与测试工具实现计划

> **面向 AI 代理的工作者：** 使用 `executing-plans` 在当前会话逐项执行。每个
> 任务严格遵循红灯、绿灯、重构顺序。

**目标：** 为实际藏品描述增加价值，在日志中加入带获取次数统计的独立藏品
类别，并在 `TestPotion` 中加入可生成全部 20 件藏品的第 16 个分类。

**架构：** `Treasures` 负责区分实际描述与日志描述；`Catalog.TREASURES`
负责 20 件藏品的发现状态和累计获取次数；`Item.collect` 在非堆叠藏品成功
加入玩家背包的既有成功路径上记录一次获取；`WndJournal` 负责藏品日志页及
日志专用展示；`TestPotion` 从 `Catalog.TREASURES` 构建选择列表并使用
`ItemSprite` 渲染 EX 图标。

**技术栈：** Java 8、JUnit 4、Gradle、现有 `Messages`、`Bundle`、
`Catalog`、`ItemSprite` API。

**工作区说明：** 当前相关文件已包含用户的未提交改动，特别是
`TestPotion.java` 的提取卷轴分类逻辑。本计划不创建提交，不覆盖或回退这些
改动，仅在其上增量实现。

---

### 任务 1：为描述分流和文本资源建立失败测试

**文件：**

- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasuresTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasureCatalogTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/Treasures.java`
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`

- [ ] **步骤 1：编写失败测试**

在 `TreasuresTest` 中读取 `Treasures.java`，声明日志描述 API 的契约。由于
`Messages` 需要 LibGDX 文件环境，JVM 单元测试不直接初始化界面资源：

```java
@Test
public void journalDescriptionKeepsOnlyBaseDescriptionAndCollectionRarity()
        throws IOException {
    String source = readCoreSource(
            "com/shatteredpixel/shatteredpixeldungeon/items/treasures/Treasures.java");
    int start = source.indexOf("public String journalDesc()");
    int end = source.indexOf("@Override", start);
    String method = source.substring(start, end);

    assertTrue(method.contains("super.desc()"));
    assertTrue(method.contains("collectionRarityName()"));
    assertFalse(method.contains("rarityName()"));
    assertFalse(method.contains("value()"));
}
```

在 `TreasureCatalogTest` 读取默认和中文 properties，断言新增 `value` 键，
且中文值精确为 `价值:%s`。

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.treasures.TreasuresTest --tests com.shatteredpixel.shatteredpixeldungeon.items.treasures.TreasureCatalogTest
```

预期：FAIL，原因是 `journalDesc()` 和 `value` 文本尚不存在。

- [ ] **步骤 3：实现最小描述分流**

在 `Treasures` 中形成两个明确方法：

```java
public String journalDesc() {
    return super.desc()
            + "\n\n" + Messages.get(Treasures.class,
                    "collection_rarity", collectionRarityName());
}

@Override
public String desc() {
    return journalDesc()
            + "\n" + Messages.get(Treasures.class, "quality", rarityName())
            + "\n" + Messages.get(Treasures.class, "value", value());
}
```

默认文本使用 `Value: %s`，中文文本严格使用 `价值:%s`。

- [ ] **步骤 4：重跑任务 1 测试**

预期：PASS。

---

### 任务 2：新增 Catalog.TREASURES 及获取次数持久化

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TreasureCatalogJournalTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java`
- 修改：`core/src/main/assets/messages/journal/journal.properties`
- 修改：`core/src/main/assets/messages/journal/journal_zh.properties`

- [ ] **步骤 1：编写 Catalog 失败测试**

测试精确顺序和数量：

```java
@Test
public void treasureCatalogContainsApprovedTwentyClassesInSpriteOrder() {
    assertArrayEquals(APPROVED_TREASURE_CLASSES,
            Catalog.TREASURES.items().toArray(new Class<?>[0]));
}
```

测试计数和 Bundle：

```java
@Test
public void treasureAcquisitionCountSurvivesCatalogBundleRoundTrip() {
    int before = Catalog.useCount(MuiscaGoldenRaft.class);
    Catalog.countUses(MuiscaGoldenRaft.class, 1);
    Bundle saved = new Bundle();
    Catalog.store(saved);

    Catalog.countUses(MuiscaGoldenRaft.class, 2);
    Catalog.restore(saved);

    assertEquals(before + 1, Catalog.useCount(MuiscaGoldenRaft.class));
}
```

测试 `Item.collect` 的藏品成功分支调用获取计数，并且中文、默认语言都存在
`journal.catalog.treasures.title`。

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.journal.TreasureCatalogJournalTest
```

预期：FAIL，原因是 `Catalog.TREASURES` 尚不存在。

- [ ] **步骤 3：登记 20 件藏品**

在 `Catalog` 的消耗品枚举之后增加 `TREASURES`，导入全部 20 个具体类并按
EX 索引 0–19 的顺序调用 `TREASURES.addItems(...)`。新增独立
`treasureCatalogs` 或直接由 `WndJournal` 使用 `Catalog.TREASURES`，不要
加入 `consumableCatalogs`。

- [ ] **步骤 4：记录成功获取**

在 `Item.collect(Bag)` 的非堆叠物品成功路径、确认背包可容纳且即将加入
`items` 后，紧邻 `Catalog.setSeen(getClass())` 增加：

```java
if (this instanceof Treasures) {
    Catalog.countUse(getClass());
}
```

该代码保持在 `hero != null && hero.isAlive()` 条件内。`Catalog.countUse`
现有测试模式保护保证 `TestPotion` 不污染正式统计。

- [ ] **步骤 5：增加目录标题文本并重跑测试**

默认标题为 `treasures`，中文标题为 `藏品`。预期任务 2 测试 PASS。

---

### 任务 3：在 WndJournal 增加独立藏品页

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/windows/TreasureJournalViewTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java`
- 修改：`core/src/main/assets/messages/windows/windows.properties`
- 修改：`core/src/main/assets/messages/windows/windows_zh.properties`

- [ ] **步骤 1：编写日志界面失败测试**

测试源码契约。日志 UI 依赖 LibGDX 图形环境，因此不为测试增加生产环境专用
getter：

```java
String source = readCoreSource(
        "com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");
assertTrue(source.contains("NUM_BUTTONS = 6"));
assertTrue(source.contains("TREASURE_IDX = 2"));
assertTrue(source.contains("Catalog.TREASURES.items()"));
assertTrue(source.contains("((Treasures) item).journalDesc()"));
```

并断言藏品详情组装使用 `Treasures.journalDesc()`、追加
`treasure_count`，且不调用藏品实例的 `info()`。

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.windows.TreasureJournalViewTest
```

预期：FAIL，原因是第 6 个类别和藏品详情分支尚不存在。

- [ ] **步骤 3：扩展日志类别**

将 `CatalogTab.NUM_BUTTONS` 改为 6，索引顺序调整为：

```java
EQUIP_IDX = 0;
CONSUM_IDX = 1;
TREASURE_IDX = 2;
BESTIARY_IDX = 3;
LORE_IDX = 4;
TALENT_IDX = 5;
```

藏品按钮使用：

```java
itemButtons[TREASURE_IDX].icon(
        new ItemSprite(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT));
```

藏品页标题统计 `Catalog.TREASURES.totalSeen()/totalItems()`，列表调用
`addGridItems(grid, Catalog.TREASURES.items())`。

- [ ] **步骤 4：分流藏品日志详情**

在 `addGridItems` 已发现物品分支中：

```java
if (item instanceof Treasures) {
    desc = ((Treasures) item).journalDesc();
    desc += "\n\n" + Messages.get(
            CatalogTab.class, "treasure_count", Catalog.useCount(itemClass));
} else {
    desc += item.info();
}
```

藏品获取次数即使为 1 也显示。未发现藏品继续使用现有剪影逻辑。

- [ ] **步骤 5：增加窗口文本并重跑测试**

默认：

```properties
title_treasures=Treasures
treasure_count=You have obtained this treasure _%,d_ times.
```

中文：

```properties
title_treasures=藏品
treasure_count=获取次数:_%,d_
```

预期任务 3 测试 PASS。

---

### 任务 4：为 TestPotion 增加第 16 个藏品分类

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestPotionTreasureTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestPotion.java`

- [ ] **步骤 1：编写 TestPotion 失败测试**

测试 `TestPotion` 暴露的包内分类辅助方法：

```java
@Test
public void sixteenthCategoryContainsAllTreasuresInCatalogOrder() {
    assertArrayEquals(
            Catalog.TREASURES.items().toArray(new Class<?>[0]),
            TestPotion.treasureClasses().toArray(new Class<?>[0]));
}
```

测试第 16 类索引为 15、最大类别为 15、分类和物品图标均通过
`ItemSprite` 渲染；测试创建所选类使用 `Reflection.newInstance` 且没有调用
`setRarity`，从而保留构造器的随机品质。

- [ ] **步骤 2：运行测试并确认红灯**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestPotionTreasureTest --tests com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestPotionExtractionTest
```

预期：新测试 FAIL，现有提取卷轴测试仍 PASS。

- [ ] **步骤 3：增加藏品列表与索引路由**

增加：

```java
private static final int TREASURE_CATEGORY = 15;
private static final ArrayList<Class<? extends Treasures>> treasureList =
        new ArrayList<>();
```

`buildList()` 从 `Catalog.TREASURES.items()` 填充列表；`idToItem()`、
`maxIndex()`、`idToCategoryImage()` 和 `maxCategory()` 增加索引 15。

- [ ] **步骤 4：统一 EX 感知图标渲染**

分类按钮和藏品物品按钮均使用 `new ItemSprite(image)`，不直接对 EX 编码调用
`ItemSpriteSheet.film.get(...)`。保留现有 `splitMiscCatalogItems` 和提取卷轴
分类逻辑。

- [ ] **步骤 5：重跑 TestPotion 测试**

预期新旧测试全部 PASS。

---

### 任务 5：综合验证与审查

**文件：**

- 检查本计划列出的所有生产、测试及文本文件。

- [ ] **步骤 1：运行所有藏品相关测试**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.treasures.* --tests com.shatteredpixel.shatteredpixeldungeon.journal.TreasureCatalogJournalTest --tests com.shatteredpixel.shatteredpixeldungeon.windows.TreasureJournalViewTest --tests com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestPotionTreasureTest --tests com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestPotionExtractionTest
```

预期：全部 PASS。

- [ ] **步骤 2：重新编译主代码**

```powershell
.\gradlew.bat :core:compileJava --rerun-tasks
```

预期：`BUILD SUCCESSFUL`。

- [ ] **步骤 3：检查资源与差异**

核对：

- 20 个默认和中文藏品名称、描述仍存在；
- `ex_items.png` SHA-256 仍为
  `A285AA064E61BF019D99D7FA2F1C0D0BDA54A99DCAEF2508B14B9916F77DB717`；
- `git diff --check` 无新增空白错误；
- 没有临时测试过滤文件；
- 不覆盖 `TestPotion` 现有提取卷轴支持。

- [ ] **步骤 4：执行代码审查**

重点审查获取次数是否只在成功收集时增加、测试模式是否被排除、日志是否泄露
随机品质或价值，以及第 16 类是否正确使用 EX 图标。
