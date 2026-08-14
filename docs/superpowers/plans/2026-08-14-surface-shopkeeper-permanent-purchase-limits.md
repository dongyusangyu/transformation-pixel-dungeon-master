# 地表商人永久购买上限实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 解除地表商人的查询购买记录与搜打撤结算次数之间的关联，使每种道具 5 个的购买上限永久保存在商人对象中。

**架构：** `SurfaceShopkeeper.queryPurchases` 作为购买次数的唯一数据源，保存和恢复时只序列化道具类名与数量。`ExtractionRaidRun` 继续把成功撤离和死亡记录到 `TreasureHuntRecords`，但不再通知商人；仅用于购买刷新周期的常量、方法和存档字段全部移除。

**技术栈：** Java、JUnit 4、Gradle、Watabou `Bundle`

---

## 文件结构

- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeperTest.java` —— 锁定结算不刷新与旧存档兼容行为。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeper.java` —— 移除购买周期同步与周期序列化。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRun.java` —— 保留结算统计，移除商人同步并修正方法命名。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRunTest.java` —— 按新职责名称验证结算统计口径。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecords.java` —— 删除只服务购买刷新周期的计算接口。
- 创建：`tools/surface_shopkeeper_purchase_limits_test.init.gradle` —— 将编译范围限制到本次相关测试，避开工作区内无关测试改动。

### 任务 1：用失败测试锁定永久购买上限

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeperTest.java:89-129`
- 创建：`tools/surface_shopkeeper_purchase_limits_test.init.gradle`

- [ ] **步骤 1：添加结算不刷新测试**

将旧的 `fifthSettlementClearsEveryQueryPurchaseLimit` 替换为：

```java
@Test
public void extractionRaidSettlementsNeverRefreshQueryPurchaseLimits() {
    SurfaceShopkeeper merchant = new SurfaceShopkeeper();
    merchant.recordQueryPurchase(PotionOfHealing.class, 5);
    merchant.recordQueryPurchase(SmallRation.class, 3);

    for (int settlement = 0; settlement < 12; settlement++) {
        TreasureHuntRecords.recordExtractionRaidSettlement();
    }

    assertEquals(5, merchant.purchasedCount(PotionOfHealing.class));
    assertEquals(3, merchant.purchasedCount(SmallRation.class));
    assertFalse(merchant.canPurchase(PotionOfHealing.class, 1));
    assertTrue(merchant.canPurchase(SmallRation.class, 2));
    assertFalse(merchant.canPurchase(SmallRation.class, 3));
}
```

- [ ] **步骤 2：添加旧存档周期字段兼容测试**

将旧的 `purchaseLimitCycleSurvivesMerchantSave` 替换为：

```java
@Test
public void legacyPurchaseCycleFieldCannotRefreshRestoredLimits() {
    Bundle legacyBundle = new Bundle();
    legacyBundle.put("query_classes", new String[]{ScrollOfExtraction.class.getName()});
    legacyBundle.put("query_counts", new int[]{2});
    legacyBundle.put("query_cycle", 0);
    for (int settlement = 0; settlement < 10; settlement++) {
        TreasureHuntRecords.recordExtractionRaidSettlement();
    }

    SurfaceShopkeeper restored = new SurfaceShopkeeper();
    restored.restoreFromBundle(legacyBundle);

    assertEquals(2, restored.purchasedCount(ScrollOfExtraction.class));
    assertTrue(restored.canPurchase(ScrollOfExtraction.class, 3));
    assertFalse(restored.canPurchase(ScrollOfExtraction.class, 4));
}
```

- [ ] **步骤 3：创建限定测试初始化脚本**

创建 `tools/surface_shopkeeper_purchase_limits_test.init.gradle`：

```groovy
allprojects {
    afterEvaluate { project ->
        if (project.path == ":core" && project.plugins.hasPlugin("java")) {
            project.sourceSets.test.java.setIncludes([
                    "**/SurfaceShopkeeperTest.java",
                    "**/ExtractionRaidRunTest.java",
                    "**/TreasureHuntRecordsTest.java"
            ])
        }
    }
}
```

- [ ] **步骤 4：运行测试验证红灯**

运行：

```powershell
.\gradlew.bat core:test --no-problems-report --no-daemon --rerun-tasks --init-script tools\surface_shopkeeper_purchase_limits_test.init.gradle --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SurfaceShopkeeperTest
```

预期：FAIL。旧实现会在第 5 次结算后清空 `queryPurchases`，因此两个新测试得到 0 而不是 5、3 或 2。

### 任务 2：解除购买记录与结算周期的耦合

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeper.java:20,107-108,203-243,430-465`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRun.java:18,252,277,285-291`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRunTest.java:110-127`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecords.java:31,159-161`

- [ ] **步骤 1：让商人只读写自身购买映射**

在 `SurfaceShopkeeper` 中删除 `TreasureHuntRecords` import、`purchaseLimitCycle`、`syncPurchaseLimitCycle()`、`syncCurrentMerchantPurchaseLimits()`、`QUERY_CYCLE`，并删除交互、保存、恢复过程中的所有周期同步。

最终的核心方法应为：

```java
public int purchasedCount(Class<? extends Item> itemClass) {
    Integer count = queryPurchases.get(itemClass.getName());
    return count == null ? 0 : count;
}

public boolean canPurchase(Class<? extends Item> itemClass, int quantity) {
    return quantity > 0 && purchasedCount(itemClass) + quantity <= QUERY_LIMIT;
}

public void recordQueryPurchase(Class<? extends Item> itemClass, int quantity) {
    queryPurchases.put(itemClass.getName(),
            Math.min(QUERY_LIMIT, purchasedCount(itemClass) + quantity));
}
```

`storeInBundle` 只写 `QUERY_CLASSES` 与 `QUERY_COUNTS`。`restoreFromBundle` 清空映射后读取这两个数组，并保留以下恢复循环：

```java
for (int i = 0; i < Math.min(classes.length, counts.length); i++) {
    if (counts[i] > 0) {
        queryPurchases.put(classes[i], Math.min(QUERY_LIMIT, counts[i]));
    }
}
```

旧存档中的 `query_cycle` 不读取也不删除；`Bundle` 的未知字段自然被忽略。

- [ ] **步骤 2：让搜打撤结算只维护统计**

在 `ExtractionRaidRun` 中删除 `SurfaceShopkeeper` import，把三处方法名改为 `recordExtractionRaidSettlement`，并移除商人同步调用。最终方法为：

```java
static void recordExtractionRaidSettlement(boolean successful, boolean died) {
    if (!successful && !died) {
        return;
    }
    TreasureHuntRecords.recordExtractionRaidSettlement();
}
```

成功撤离仍调用 `recordExtractionRaidSettlement(true, false)`；失败结算仍调用 `recordExtractionRaidSettlement(false, died)`，所以死亡计数、主动放弃不计数的规则保持不变。

- [ ] **步骤 3：更新结算统计测试名称和调用**

在 `ExtractionRaidRunTest` 中将测试替换为：

```java
@Test
public void onlySuccessfulExtractionAndDeathAdvanceRaidSettlementStats() throws IOException {
    TreasureHuntRecords.reset();

    ExtractionRaidRun.recordExtractionRaidSettlement(true, false);
    assertEquals(1, TreasureHuntRecords.extractionRaidSettlements());

    ExtractionRaidRun.recordExtractionRaidSettlement(false, true);
    assertEquals(2, TreasureHuntRecords.extractionRaidSettlements());

    ExtractionRaidRun.recordExtractionRaidSettlement(false, false);
    assertEquals(2, TreasureHuntRecords.extractionRaidSettlements());

    String treasureHuntWindow = readCoreSource(
            "com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java");
    assertFalse(treasureHuntWindow.contains("recordExtractionRaidSettlement"));
}
```

- [ ] **步骤 4：删除无调用方的购买周期计算**

从 `TreasureHuntRecords` 删除：

```java
private static final int SETTLEMENTS_PER_PURCHASE_CYCLE = 5;

public static int purchaseLimitCycle() {
    return extractionRaidSettlements / SETTLEMENTS_PER_PURCHASE_CYCLE;
}
```

保留 `extractionRaidSettlements`、`recordExtractionRaidSettlement()`、getter 以及存档恢复代码。

- [ ] **步骤 5：运行限定测试验证绿灯**

运行：

```powershell
.\gradlew.bat core:test --no-problems-report --no-daemon --rerun-tasks --init-script tools\surface_shopkeeper_purchase_limits_test.init.gradle --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SurfaceShopkeeperTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidRunTest --tests com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRecordsTest
```

预期：三个测试类全部通过，退出码为 0。

- [ ] **步骤 6：扫描残留耦合并检查差异**

运行：

```powershell
$files = Get-ChildItem core\src\main,core\src\test -Recurse -File | Where-Object { $_.FullName -notmatch '\\build\\' }
$files | Select-String -Pattern 'purchaseLimitCycle|syncPurchaseLimitCycle|syncCurrentMerchantPurchaseLimits|recordMerchantPurchaseCycleSettlement|SETTLEMENTS_PER_PURCHASE_CYCLE|QUERY_CYCLE' -Encoding UTF8
git diff --check
git diff -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeper.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRun.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecords.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeperTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRunTest.java tools/surface_shopkeeper_purchase_limits_test.init.gradle
```

预期：第一条命令没有命中；`git diff --check` 没有空白错误；差异只包含本规格要求的解耦、测试和限定测试脚本。

- [ ] **步骤 7：提交实现**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeper.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRun.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/treasurehunt/TreasureHuntRecords.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeperTest.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRunTest.java tools/surface_shopkeeper_purchase_limits_test.init.gradle
git commit -m "fix: preserve surface shop purchase limits"
```

提交前再次执行步骤 5 的限定测试命令，并确认输出为 `BUILD SUCCESSFUL`。
