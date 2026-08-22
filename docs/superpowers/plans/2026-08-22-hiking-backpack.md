# Hiking Backpack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增由第 20 层最终小恶魔商人出售的“登山包”，为所有非背包物品提供 19 格最低优先级扩展容量，并在高优先级容器出现空间时自动归位。

**Architecture:** `HikingBackpack` 通过 `Bag.isFallbackStorage()` 声明自身为溢出容器，`Item.collect(Bag)` 将此类容器延迟到当前容器之后尝试。`Belongings.Backpack` 集中负责幂等再平衡，`Bag` 的既有收集与空位回填入口只负责触发该服务；商店、日志和页签顺序分别接入现有 `ImpShopRoom`、`Catalog` 与 `Belongings.getBags()`。

**Tech Stack:** Java、libGDX、Shattered Pixel Dungeon Item/Bag/Bundle 体系、JUnit 4、Gradle。

**Spec:** `docs/superpowers/specs/2026-08-22-hiking-backpack-design.md`

## Global Constraints

- Java 类名固定为 `HikingBackpack`，中文名称固定为“登山包”。
- 容量固定为 19 格，基础价值固定为 40，第 20 层标准售价固定为 1000 金币。
- 贴图必须复用 `ItemSpriteSheet.BACKPACK`；不得修改 `items.png` 或新增精灵帧。
- 登山包收纳所有非 `Bag` 物品，但不能嵌套主背包或其他专用背包。
- 收纳优先级固定为“可用专用背包 → 主背包 → 登山包”。
- 页签顺序固定为“主背包 → 登山包 → 绒布袋（种子带）→ 卷轴筒 → 药剂挎带 → 魔法筒袋”。
- 登山包只加入第 20 层最终小恶魔商店，不加入 `ShopRoom.ChooseBag` 的普通商店池。
- 保留 `LostInventory`、堆叠来源、快捷栏、投掷武器集合和专用背包副作用。
- 不改变现有“四种背包全部购入”徽章的判定。
- 只补充英文回退文本和中文文本，不生成其他语言的机械翻译。
- 当前共享工作区存在与本功能无关的未提交改动；Tasks 1-6 只建立差异检查点，不自动执行 `git add`/`git commit`。实现完成后如用户要求提交，再按文件与具体差异选择性暂存，严禁把原有改动一并提交。

## File Structure

- Create `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpack.java`: 登山包数值、贴图、收纳范围和 fallback 标识。
- Modify `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java`: fallback 契约、获得背包和空位变化后的统一再平衡触发。
- Modify `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java`: 新物品进入背包体系时延迟 fallback 容器。
- Modify `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java`: 主背包再平衡实现、读档归一化和页签顺序。
- Modify `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoom.java`: 最终小恶魔商店稳定追加登山包。
- Modify `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java`: `HIKING_BACKPACK` limited-drop 状态。
- Modify `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java`: 杂项装备日志登记。
- Modify `core/src/main/assets/messages/items/items.properties`: 英文名称和描述。
- Modify `core/src/main/assets/messages/items/items_zh.properties`: 中文名称和描述。
- Create `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java`: 属性、路由、再平衡、存档和页签测试。
- Create `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoomHikingBackpackTest.java`: 商店、唯一性与价格测试。
- Create `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/HikingBackpackCatalogTest.java`: 日志与文本测试。

---

### Task 1: 登山包模型与 fallback 契约

**Files:**
- Create: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpack.java`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java:45-110`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java`

**Interfaces:**
- Consumes: `Bag.canHold(Item)`, `Bag.capacity()`, `ItemSpriteSheet.BACKPACK`。
- Produces: `public boolean Bag.isFallbackStorage()`、`protected boolean Bag.autoGrabOnCollect()`、`HikingBackpack`。

- [ ] **Step 1: 创建无图形窗口测试环境和失败的模型测试**

在 `HikingBackpackTest` 中按现有 `SurpriseWeaponSpearShieldTest.HeadlessFiles` 模式安装 `Gdx.files`，并在 `@After` 恢复全局状态。先加入以下测试：

```java
@Test
public void hasExpectedCapacityValueAndSprite() {
	HikingBackpack bag = new HikingBackpack();

	assertEquals(19, bag.capacity());
	assertEquals(40, bag.value());
	assertEquals(ItemSpriteSheet.BACKPACK, bag.image);
	assertTrue(bag.isFallbackStorage());
}

@Test
public void acceptsAnyNonBagItemButRejectsBags() {
	HikingBackpack bag = new HikingBackpack();

	assertTrue(bag.canHold(new Item()));
	assertTrue(bag.canHold(new Armor(1)));
	assertFalse(bag.canHold(new VelvetPouch()));
	assertFalse(bag.canHold(new HikingBackpack()));
}
```

测试类中的文件适配器使用以下核心映射，确保 `ItemSpriteSheet` 可从 core 资源目录初始化：

```java
private static class HeadlessFiles implements Files {
	private FileHandle asset(String path) {
		return new FileHandle("src/main/assets/" + path);
	}

	@Override public FileHandle getFileHandle(String path, FileType type) { return asset(path); }
	@Override public FileHandle classpath(String path) { return asset(path); }
	@Override public FileHandle internal(String path) { return asset(path); }
	@Override public FileHandle external(String path) { return asset(path); }
	@Override public FileHandle absolute(String path) { return asset(path); }
	@Override public FileHandle local(String path) { return asset(path); }
	@Override public String getExternalStoragePath() { return ""; }
	@Override public boolean isExternalStorageAvailable() { return false; }
	@Override public String getLocalStoragePath() { return "."; }
	@Override public boolean isLocalStorageAvailable() { return true; }
}
```

测试类还要显式保存和恢复会被这些用例修改的全局对象：

```java
private Files previousFiles;
private Hero previousHero;
private QuickSlot previousQuickslot;

@Before
public void setUp() {
	previousFiles = Gdx.files;
	previousHero = Dungeon.hero;
	previousQuickslot = Dungeon.quickslot;
	Gdx.files = new HeadlessFiles();
	Dungeon.quickslot = new QuickSlot();
}

@After
public void tearDown() {
	Dungeon.quickslot = previousQuickslot;
	Dungeon.hero = previousHero;
	Gdx.files = previousFiles;
}
```

- [ ] **Step 2: 运行模型测试并确认红灯**

Run:

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest --no-problems-report
```

Expected: `compileTestJava` 因 `HikingBackpack` 或 `Bag.isFallbackStorage()` 尚不存在而失败。

- [ ] **Step 3: 在 Bag 增加最小 fallback 契约**

在 `Bag` 中加入默认实现，不改变现有背包行为：

```java
public boolean isFallbackStorage() {
	return false;
}

protected boolean autoGrabOnCollect() {
	return true;
}
```

把 `Bag.collect(Bag container)` 开头的自动整理改为：

```java
if (autoGrabOnCollect()) {
	grabItems(container);
}
```

- [ ] **Step 4: 实现 HikingBackpack**

创建以下类：

```java
package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class HikingBackpack extends Bag {

	{
		image = ItemSpriteSheet.BACKPACK;
	}

	@Override
	public boolean canHold(Item item) {
		return !(item instanceof Bag) && super.canHold(item);
	}

	@Override
	public boolean isFallbackStorage() {
		return true;
	}

	@Override
	protected boolean autoGrabOnCollect() {
		return false;
	}

	@Override
	public int capacity() {
		return 19;
	}

	@Override
	public int value() {
		return 40;
	}
}
```

- [ ] **Step 5: 运行模型测试并确认绿灯**

Run:

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest --no-problems-report
```

Expected: `hasExpectedCapacityValueAndSprite` 与 `acceptsAnyNonBagItemButRejectsBags` 通过。

- [ ] **Step 6: 记录模型任务差异检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpack.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
git status --short -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpack.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
```

### Task 2: 最低优先级收纳路由

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java:287-355`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java`

**Interfaces:**
- Consumes: `Bag.isFallbackStorage()`、`HikingBackpack.canHold(Item)`。
- Produces: `Item.collect(Bag)` 的“普通子背包 → 当前容器 → fallback 子背包”顺序。

- [ ] **Step 1: 编写三层优先级失败测试**

在测试类中创建英雄、设置 `Dungeon.hero`，并通过 `bag.collect(hero.belongings.backpack)` 建立 owner。加入三个测试，分别验证专用背包、主背包和登山包：

```java
@Test
public void specializedBagWinsBeforeBackpackAndFallback() {
	Hero hero = heroWith(new ScrollHolder(), new HikingBackpack());
	Scroll scroll = new ScrollOfIdentify();

	assertTrue(scroll.collect(hero.belongings.backpack));

	assertTrue(hero.belongings.getItem(ScrollHolder.class).contains(scroll));
	assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(scroll));
}

@Test
public void backpackWinsWhenSpecializedBagIsFull() {
	Hero hero = heroWith(fullScrollHolder(), new HikingBackpack());
	Scroll scroll = new ScrollOfIdentify();

	assertTrue(scroll.collect(hero.belongings.backpack));

	assertTrue(hero.belongings.backpack.items.contains(scroll));
}

@Test
public void fallbackReceivesItemOnlyAfterOtherContainersAreFull() {
	Hero hero = heroWith(fullScrollHolder(), new HikingBackpack());
	fillDirectSlots(hero.belongings.backpack);
	Scroll scroll = new ScrollOfIdentify();

	assertTrue(scroll.collect(hero.belongings.backpack));

	assertTrue(hero.belongings.getItem(HikingBackpack.class).contains(scroll));
}

@Test
public void collectingHikingBackpackDoesNotStealExistingMainBagItems() {
	Hero hero = newHero();
	Item existing = new Item();
	assertTrue(existing.collect(hero.belongings.backpack));
	HikingBackpack hiking = new HikingBackpack();

	assertTrue(hiking.collect(hero.belongings.backpack));

	assertTrue(hero.belongings.backpack.items.contains(existing));
	assertFalse(hiking.contains(existing));
}
```

在同一测试类中加入以下完整辅助方法；`Dungeon.hero` 由 Task 1 的 `@After` 统一恢复：

```java
private Hero newHero() {
	Hero hero = new Hero();
	Dungeon.hero = hero;
	return hero;
}

private Hero heroWith(Bag... bags) {
	Hero hero = newHero();
	for (Bag bag : bags) {
		assertTrue(bag.collect(hero.belongings.backpack));
	}
	return hero;
}

private ScrollHolder fullScrollHolder() {
	ScrollHolder holder = new ScrollHolder();
	for (int i = 0; i < holder.capacity(); i++) {
		holder.items.add(new ScrollOfIdentify());
	}
	return holder;
}

private MagicalHolster fullMagicalHolster() {
	MagicalHolster holster = new MagicalHolster();
	for (int i = 0; i < holster.capacity(); i++) {
		holster.items.add(new ThrowingHammer());
	}
	return holster;
}

private HikingBackpack fullHikingBackpack() {
	HikingBackpack bag = new HikingBackpack();
	for (int i = 0; i < bag.capacity(); i++) {
		bag.items.add(new Item());
	}
	return bag;
}

private void fillDirectSlots(Belongings.Backpack backpack) {
	while (backpack.items.size() < backpack.capacity()) {
		backpack.items.add(new Item());
	}
}

private ScrollOfIdentify putScrollInFallback(Hero hero) {
	ScrollOfIdentify scroll = new ScrollOfIdentify();
	hero.belongings.getItem(HikingBackpack.class).items.add(scroll);
	return scroll;
}

private Item putGeneralItemInFallback(Hero hero) {
	Item item = new Item();
	hero.belongings.getItem(HikingBackpack.class).items.add(item);
	return item;
}

private Item firstDirectNonBag(Belongings.Backpack backpack) {
	for (Item item : backpack.items) {
		if (!(item instanceof Bag)) return item;
	}
	throw new AssertionError("main backpack contains no ordinary item");
}

private int countIdentity(Bag bag, Item target) {
	int count = 0;
	for (Item item : bag.items) {
		if (item == target) count++;
		if (item instanceof Bag) count += countIdentity((Bag) item, target);
	}
	return count;
}
```

- [ ] **Step 2: 运行路由测试并确认 fallback 抢占或无法落位**

Run:

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest.*Wins*" --tests "com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest.fallbackReceivesItemOnlyAfterOtherContainersAreFull" --no-problems-report
```

Expected: 至少一个测试失败，证明当前递归遍历会在检查主背包前进入能收纳全部物品的登山包。

- [ ] **Step 3: 修改 Item.collect 的 fallback 顺序**

在现有子背包循环中记录 fallback，而不是立即递归进入：

```java
Bag fallback = null;
for (Item item : items) {
	if (item instanceof Bag) {
		Bag bag = (Bag) item;
		if (bag.isFallbackStorage()) {
			fallback = bag;
		} else if (bag.canHold(this) && collect(bag)) {
			return true;
		}
	}
}

if (!container.canHold(this)) {
	return fallback != null && fallback.canHold(this) && collect(fallback);
}
```

保留该段之后原有的堆叠合并、徽章、日志、排序和快捷栏逻辑。不得把这些副作用复制到新分支。

- [ ] **Step 4: 增加“所有容器都满时失败”和堆叠合并测试**

```java
@Test
public void collectionFailsWhenFallbackIsAlsoFull() {
	Hero hero = heroWith(fullScrollHolder(), fullHikingBackpack());
	fillDirectSlots(hero.belongings.backpack);

	assertFalse(new ScrollOfIdentify().collect(hero.belongings.backpack));
}

@Test
public void existingHigherPriorityStackBeatsFallbackStack() {
	Hero hero = heroWith(new ScrollHolder(), new HikingBackpack());
	ScrollOfIdentify preferred = new ScrollOfIdentify();
	preferred.quantity(2);
	preferred.collect(hero.belongings.getItem(ScrollHolder.class));
	ScrollOfIdentify incoming = new ScrollOfIdentify();

	assertTrue(incoming.collect(hero.belongings.backpack));
	assertEquals(3, preferred.quantity());
}
```

- [ ] **Step 5: 运行完整背包模型测试**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest --no-problems-report
```

Expected: Task 1 和 Task 2 的全部测试通过。

- [ ] **Step 6: 记录路由任务差异检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
git status --short -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
```

### Task 3: 自动再平衡与读档归一化

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java:70-135`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java:54-80`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java`

**Interfaces:**
- Consumes: Task 2 的 fallback 路由。
- Produces: `public void Belongings.Backpack.rebalanceFallbackStorage()`；`Bag.grabItems()` 和 `Bag.collect()` 触发该方法。

- [ ] **Step 1: 编写空位归位失败测试**

加入专用背包与主背包两类归位测试：

```java
@Test
public void freedSpecializedSlotPullsMatchingItemFromFallback() {
	Hero hero = heroWith(fullScrollHolder(), new HikingBackpack());
	fillDirectSlots(hero.belongings.backpack);
	ScrollOfIdentify overflow = putScrollInFallback(hero);
	ScrollHolder holder = hero.belongings.getItem(ScrollHolder.class);

	holder.items.get(0).detachAll(holder);

	assertTrue(holder.contains(overflow));
	assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(overflow));
}

@Test
public void freedBackpackSlotPullsGeneralItemFromFallback() {
	Hero hero = heroWith(new HikingBackpack());
	fillDirectSlots(hero.belongings.backpack);
	Item overflow = putGeneralItemInFallback(hero);
	Item direct = firstDirectNonBag(hero.belongings.backpack);

	direct.detachAll(hero.belongings.backpack);

	assertTrue(hero.belongings.backpack.items.contains(overflow));
}
```

- [ ] **Step 2: 运行归位测试并确认红灯**

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest.freed*" --no-problems-report
```

Expected: 物品仍留在登山包，两个测试失败。

- [ ] **Step 3: 在 Backpack 实现集中再平衡**

在 `Belongings.Backpack` 中加入非持久化重入字段和方法：

```java
private boolean rebalancingFallbackStorage;

public void rebalanceFallbackStorage() {
	if (rebalancingFallbackStorage || isLoading() || !(owner instanceof Hero)
			|| owner.buff(LostInventory.class) != null) {
		return;
	}

	HikingBackpack fallback = null;
	for (Item item : items) {
		if (item instanceof HikingBackpack) {
			fallback = (HikingBackpack) item;
			break;
		}
	}
	if (fallback == null || fallback.isLoading()) return;

	rebalancingFallbackStorage = true;
	try {
		ArrayList<Item> ordered = new ArrayList<>(fallback.items);
		Collections.sort(ordered, Item.itemComparator);
		for (Item item : ordered) {
			int quickslot = Dungeon.quickslot.getSlot(item);
			item.detachAll(fallback);
			if (!item.collect(this)) {
				fallback.items.add(item);
			}
			if (quickslot >= 0) {
				Item stored = ((Hero) owner).belongings.getSimilar(item);
				if (stored != null) Dungeon.quickslot.setSlot(quickslot, stored);
			}
		}
	} finally {
		rebalancingFallbackStorage = false;
	}
}
```

同时在 `Belongings.java` 增加 `java.util.Collections` 导入。实现时保留 `Item.collect(this)` 的 fallback 回落：没有高优先级空间时，它会把物品安全放回刚腾出一格的登山包。仅当 `collect` 返回 `false` 时才执行直接恢复，并立即按 `Item.itemComparator` 重新排序 fallback 内容。

- [ ] **Step 4: 从 Bag 的既有生命周期触发再平衡**

把 `Bag.grabItems()` 调整为：

```java
public void grabItems() {
	if (owner instanceof Hero) {
		Belongings.Backpack backpack = ((Hero) owner).belongings.backpack;
		if (this != backpack && !isFallbackStorage()) {
			grabItems(backpack);
		}
		backpack.rebalanceFallbackStorage();
	}
}
```

在 `Bag.collect(Bag)` 成功设置 `owner` 后调用：

```java
if (owner instanceof Hero) {
	((Hero) owner).belongings.backpack.rebalanceFallbackStorage();
}
```

在 `Belongings.Backpack.restoreFromBundle(Bundle)` 覆盖方法中，等待 `super.restoreFromBundle(bundle)` 完整结束后调用 `rebalanceFallbackStorage()`：

```java
@Override
public void restoreFromBundle(Bundle bundle) {
	super.restoreFromBundle(bundle);
	rebalanceFallbackStorage();
}
```

- [ ] **Step 5: 增加新专用背包、幂等、堆叠与 Bundle 测试**

```java
@Test
public void collectingSpecializedBagRebalancesFallback() {
	Hero hero = heroWith(new HikingBackpack());
	ScrollOfIdentify overflow = putScrollInFallback(hero);
	ScrollHolder holder = new ScrollHolder();

	assertTrue(holder.collect(hero.belongings.backpack));

	assertTrue(holder.contains(overflow));
}

@Test
public void repeatedRebalanceIsIdempotent() {
	Hero hero = heroWith(new HikingBackpack());
	Item overflow = putGeneralItemInFallback(hero);

	hero.belongings.backpack.rebalanceFallbackStorage();
	hero.belongings.backpack.rebalanceFallbackStorage();

	assertEquals(1, countIdentity(hero.belongings.backpack, overflow));
}

@Test
public void fullPreferredBagStillMergesFallbackStack() {
	Hero hero = heroWith(fullScrollHolder(), new HikingBackpack());
	fillDirectSlots(hero.belongings.backpack);
	ScrollHolder holder = hero.belongings.getItem(ScrollHolder.class);
	ScrollOfIdentify preferred = (ScrollOfIdentify) holder.items.get(0);
	preferred.quantity(2);
	ScrollOfIdentify overflow = putScrollInFallback(hero);
	overflow.quantity(3);

	hero.belongings.backpack.rebalanceFallbackStorage();

	assertEquals(5, preferred.quantity());
	assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(overflow));
}

@Test
public void rebalancePreservesQuickslotAndMissileHolsterState() {
	Hero hero = heroWith(fullMagicalHolster(), new HikingBackpack());
	fillDirectSlots(hero.belongings.backpack);
	ThrowingHammer overflow = new ThrowingHammer();
	overflow.ensureSetIDAssigned();
	long originalSetId = overflow.setID;
	hero.belongings.getItem(HikingBackpack.class).items.add(overflow);
	Dungeon.quickslot.setSlot(0, overflow);
	MagicalHolster holster = hero.belongings.getItem(MagicalHolster.class);

	holster.items.get(0).detachAll(holster);

	assertTrue(holster.contains(overflow));
	assertSame(overflow, Dungeon.quickslot.getItem(0));
	assertEquals(originalSetId, overflow.setID);
	assertTrue(overflow.holster);
}

@Test
public void belongingsBundleRoundTripRebalancesWithoutLosingItemState() {
	Hero original = heroWith(new ScrollHolder(), new HikingBackpack());
	ScrollOfIdentify overflow = putScrollInFallback(original);
	overflow.quantity(3);
	overflow.upgradeScrollUses = 2;
	Bundle bundle = new Bundle();
	original.belongings.storeInBundle(bundle);

	Hero restored = newHero();
	restored.belongings.restoreFromBundle(bundle);
	ScrollOfIdentify restoredScroll = restored.belongings.getItem(ScrollOfIdentify.class);

	assertNotNull(restoredScroll);
	assertEquals(3, restoredScroll.quantity());
	assertEquals(2, restoredScroll.upgradeScrollUses);
	assertTrue(restored.belongings.getItem(ScrollHolder.class).contains(restoredScroll));
}
```

Bundle 测试只使用内存中的 `Bundle`，不写磁盘；快捷栏测试验证再平衡继续走 `Item.collect`/`MissileWeapon.collect`，而不是绕过既有副作用直接改列表。

- [ ] **Step 6: 运行再平衡及现有物品恢复回归**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest --tests com.shatteredpixel.shatteredpixeldungeon.items.ItemCollectRestoreTest --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeaponStackingTest --no-problems-report
```

Expected: 新增再平衡测试、读档收集测试和投掷武器堆叠测试全部通过，无递归超时。

- [ ] **Step 7: 记录再平衡任务差异检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
git status --short -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
```

### Task 4: 背包页签稳定排序

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java:232-245`
- Modify: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java`

**Interfaces:**
- Consumes: `HikingBackpack` 和现有五种背包类。
- Produces: `Belongings.getBags()` 的稳定显式顺序；`WndBag` 无需修改。

- [ ] **Step 1: 编写与获得顺序无关的页签顺序测试**

```java
@Test
public void bagTabsUseStableDeclaredOrder() {
	Hero hero = newHero();
	new MagicalHolster().collect(hero.belongings.backpack);
	new VelvetPouch().collect(hero.belongings.backpack);
	new HikingBackpack().collect(hero.belongings.backpack);
	new PotionBandolier().collect(hero.belongings.backpack);
	new ScrollHolder().collect(hero.belongings.backpack);

	List<Bag> bags = hero.belongings.getBags();

	assertSame(hero.belongings.backpack, bags.get(0));
	assertTrue(bags.get(1) instanceof HikingBackpack);
	assertTrue(bags.get(2) instanceof VelvetPouch);
	assertTrue(bags.get(3) instanceof ScrollHolder);
	assertTrue(bags.get(4) instanceof PotionBandolier);
	assertTrue(bags.get(5) instanceof MagicalHolster);
}
```

- [ ] **Step 2: 运行顺序测试并确认红灯**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest.bagTabsUseStableDeclaredOrder --no-problems-report
```

Expected: 当前 `getBags()` 依赖物品迭代顺序，断言失败。

- [ ] **Step 3: 用显式类顺序构造 getBags 结果**

在 `Belongings` 中定义：

```java
private static final Class<? extends Bag>[] BAG_TAB_ORDER = new Class[]{
		HikingBackpack.class,
		VelvetPouch.class,
		ScrollHolder.class,
		PotionBandolier.class,
		MagicalHolster.class
};
```

重写 `getBags()` 的内容：

```java
ArrayList<Bag> result = new ArrayList<>();
result.add(backpack);

for (Class<? extends Bag> bagClass : BAG_TAB_ORDER) {
	for (Item item : backpack.items) {
		if (bagClass.isInstance(item) && !result.contains(item)) {
			result.add((Bag) item);
		}
	}
}
for (Item item : backpack.items) {
	if (item instanceof Bag && !result.contains(item)) {
		result.add((Bag) item);
	}
}
return result;
```

未知未来背包保持在已知背包之后；不得使用递归 `Belongings` 迭代器重新加入同一实例。

- [ ] **Step 4: 运行有无登山包两种顺序测试**

先增加不含登山包的精确断言：

```java
@Test
public void bagTabsKeepOriginalOrderWithoutHikingBackpack() {
	Hero hero = newHero();
	new MagicalHolster().collect(hero.belongings.backpack);
	new PotionBandolier().collect(hero.belongings.backpack);
	new VelvetPouch().collect(hero.belongings.backpack);
	new ScrollHolder().collect(hero.belongings.backpack);

	List<Bag> bags = hero.belongings.getBags();

	assertEquals(5, bags.size());
	assertSame(hero.belongings.backpack, bags.get(0));
	assertTrue(bags.get(1) instanceof VelvetPouch);
	assertTrue(bags.get(2) instanceof ScrollHolder);
	assertTrue(bags.get(3) instanceof PotionBandolier);
	assertTrue(bags.get(4) instanceof MagicalHolster);
}
```

然后运行：

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest.*bagTabs*" --no-problems-report
```

Expected: 两种顺序测试都通过。

- [ ] **Step 5: 记录页签排序任务差异检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
git status --short -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpackTest.java
```

### Task 5: 第 20 层小恶魔商店库存

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java:257-335`
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoom.java`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoomHikingBackpackTest.java`

**Interfaces:**
- Consumes: `HikingBackpack`、`Dungeon.LimitedDrops`、`Shopkeeper.sellPrice(Item, int)`。
- Produces: `Dungeon.LimitedDrops.HIKING_BACKPACK`、`static void ImpShopRoom.appendHikingBackpackIfNeeded(ArrayList<Item>, Belongings)`。

- [ ] **Step 1: 编写库存唯一性和价格失败测试**

```java
@Test
public void appendsExactlyOneHikingBackpackAndMarksLimitedDrop() {
	ArrayList<Item> stock = new ArrayList<>();
	Hero hero = new Hero();
	Dungeon.hero = hero;
	Dungeon.LimitedDrops.HIKING_BACKPACK.count = 0;

	ImpShopRoom.appendHikingBackpackIfNeeded(stock, hero.belongings);
	ImpShopRoom.appendHikingBackpackIfNeeded(stock, hero.belongings);

	assertEquals(1, count(stock, HikingBackpack.class));
	assertTrue(Dungeon.LimitedDrops.HIKING_BACKPACK.dropped());
}

@Test
public void ownedHikingBackpackPreventsShopDuplicate() {
	Hero hero = new Hero();
	Dungeon.hero = hero;
	new HikingBackpack().collect(hero.belongings.backpack);
	ArrayList<Item> stock = new ArrayList<>();
	Dungeon.LimitedDrops.HIKING_BACKPACK.count = 0;

	ImpShopRoom.appendHikingBackpackIfNeeded(stock, hero.belongings);

	assertEquals(0, count(stock, HikingBackpack.class));
}

@Test
public void floorTwentyPriceIsOneThousandGold() {
	assertEquals(1000, Shopkeeper.sellPrice(new HikingBackpack(), 20));
}

private static int count(ArrayList<Item> stock, Class<? extends Item> itemClass) {
	int count = 0;
	for (Item item : stock) {
		if (itemClass.isInstance(item)) count++;
	}
	return count;
}
```

该测试类复用 Task 1 中完整的私有 `HeadlessFiles` 实现，并加入以下全局状态隔离代码：

```java
private int[] previousLimitedDropCounts;
private Hero previousHero;
private int previousVersion;
private int previousChallenges;
private Files previousFiles;

@Before
public void setUp() {
	Dungeon.LimitedDrops[] drops = Dungeon.LimitedDrops.values();
	previousLimitedDropCounts = new int[drops.length];
	for (int i = 0; i < drops.length; i++) {
		previousLimitedDropCounts[i] = drops[i].count;
	}
	previousHero = Dungeon.hero;
	previousVersion = Dungeon.version;
	previousChallenges = Dungeon.challenges;
	previousFiles = Gdx.files;
	Dungeon.version = Integer.MAX_VALUE;
	Dungeon.challenges = 0;
	Gdx.files = new HeadlessFiles();
}

@After
public void tearDown() {
	Dungeon.LimitedDrops[] drops = Dungeon.LimitedDrops.values();
	for (int i = 0; i < drops.length; i++) {
		drops[i].count = previousLimitedDropCounts[i];
	}
	Dungeon.hero = previousHero;
	Dungeon.version = previousVersion;
	Dungeon.challenges = previousChallenges;
	Gdx.files = previousFiles;
}
```

库存用例显式把新建英雄赋给 `Dungeon.hero`，保证商店和物品收集读取的是同一个英雄实例。

- [ ] **Step 2: 运行商店测试并确认红灯**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ImpShopRoomHikingBackpackTest --no-problems-report
```

Expected: limited-drop 枚举值与库存辅助方法尚不存在，编译失败。

- [ ] **Step 3: 增加 limited-drop 并实现库存辅助方法**

在 `Dungeon.LimitedDrops` 的 containers 分组末尾加入：

```java
HIKING_BACKPACK,
```

在 `ImpShopRoom` 中实现：

```java
static void appendHikingBackpackIfNeeded(ArrayList<Item> stock, Belongings belongings) {
	if (!Dungeon.LimitedDrops.HIKING_BACKPACK.dropped()
			&& (belongings == null || belongings.getItem(HikingBackpack.class) == null)) {
		stock.add(new HikingBackpack());
		Dungeon.LimitedDrops.HIKING_BACKPACK.drop();
	}
}

@Override
protected ArrayList<Item> generateItems() {
	ArrayList<Item> stock = super.generateItems();
	appendHikingBackpackIfNeeded(stock,
			Dungeon.hero == null ? null : Dungeon.hero.belongings);
	return stock;
}
```

不要修改 `ShopRoom.ChooseBag`，这正是登山包不会进入普通商店的保证。

- [ ] **Step 4: 增加旧 Bundle 与普通商店池回归测试**

加入以下测试子类和用例：

```java
private static class ShopRoomProbe extends ShopRoom {
	static Bag chooseBag(Belongings belongings) {
		return ChooseBag(belongings);
	}
}

@Test
public void oldLimitedDropBundleDefaultsHikingBackpackToAvailable() {
	Dungeon.LimitedDrops.HIKING_BACKPACK.count = 1;
	Dungeon.LimitedDrops.restore(new Bundle());

	assertEquals(0, Dungeon.LimitedDrops.HIKING_BACKPACK.count);
}

@Test
public void ordinaryShopBagPoolDoesNotContainHikingBackpack() {
	Dungeon.LimitedDrops.VELVET_POUCH.drop();
	Dungeon.LimitedDrops.SCROLL_HOLDER.drop();
	Dungeon.LimitedDrops.POTION_BANDOLIER.drop();
	Dungeon.LimitedDrops.MAGICAL_HOLSTER.drop();
	Dungeon.LimitedDrops.HIKING_BACKPACK.count = 0;

	assertNull(ShopRoomProbe.chooseBag(new Hero().belongings));
	assertFalse(Dungeon.LimitedDrops.HIKING_BACKPACK.dropped());
}
```

`ShopRoomProbe` 与测试类位于 `levels.rooms.standard` 包时，需要显式导入实际定义于 `levels.rooms.special.ShopRoom` 的父类。

- [ ] **Step 5: 运行商店和 limited-drop 回归**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ImpShopRoomHikingBackpackTest --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlockTest --no-problems-report
```

Expected: 登山包库存、价格、旧存档默认值和既有 limited-drop Bundle 测试通过。

- [ ] **Step 6: 记录商店任务差异检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoom.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoomHikingBackpackTest.java
git status --short -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoom.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoomHikingBackpackTest.java
```

### Task 6: 日志与双语文本

**Files:**
- Modify: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java:35-55,265-275`
- Modify: `core/src/main/assets/messages/items/items.properties:507-518`
- Modify: `core/src/main/assets/messages/items/items_zh.properties:561-573`
- Create: `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/HikingBackpackCatalogTest.java`

**Interfaces:**
- Consumes: `HikingBackpack.class` 和 `Catalog.MISC_EQUIPMENT`。
- Produces: 英文键 `items.bags.hikingbackpack.*`、中文键 `items.bags.hikingbackpack.*`、日志中的登山包条目。

- [ ] **Step 1: 编写日志位置和文本失败测试**

测试直接读取源码及 properties，避免依赖完整场景初始化：

```java
@Test
public void catalogPlacesHikingBackpackAfterMagicalHolster() throws IOException {
	String source = readCoreSource("com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java");
	int holster = source.indexOf("MagicalHolster.class");
	int hiking = source.indexOf("HikingBackpack.class");
	int amulet = source.indexOf("Amulet.class", holster);

	assertTrue(holster >= 0);
	assertTrue(hiking > holster);
	assertTrue(amulet > hiking);
}

@Test
public void englishAndChineseTextsContainOverflowBehavior() throws IOException {
	String english = readAsset("messages/items/items.properties");
	String chinese = readAsset("messages/items/items_zh.properties");

	assertTrue(english.contains("items.bags.hikingbackpack.name=hiking backpack"));
	assertTrue(english.contains("more suitable container has space"));
	assertTrue(chinese.contains("items.bags.hikingbackpack.name=登山包"));
	assertTrue(chinese.contains("一旦更合适的容器腾出空间"));
}

private static String readCoreSource(String relativePath) throws IOException {
	return new String(Files.readAllBytes(coreDirectory().resolve("src/main/java")
			.resolve(relativePath)), StandardCharsets.UTF_8);
}

private static String readAsset(String relativePath) throws IOException {
	return new String(Files.readAllBytes(coreDirectory().resolve("src/main/assets")
			.resolve(relativePath)), StandardCharsets.UTF_8);
}

private static Path coreDirectory() {
	Path workingDirectory = Paths.get(System.getProperty("user.dir"));
	Path coreDirectory = workingDirectory.resolve("core");
	return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
}
```

- [ ] **Step 2: 运行日志文本测试并确认红灯**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.journal.HikingBackpackCatalogTest --no-problems-report
```

Expected: Catalog 类登记和文本键不存在，测试失败。

- [ ] **Step 3: 登记 Catalog 并加入精确文本**

在 `Catalog` 导入 `HikingBackpack`，把杂项装备尾部调整为：

```java
PotionBandolier.class, ScrollHolder.class, MagicalHolster.class,
HikingBackpack.class, Amulet.class
```

在英文文件加入：

```properties
items.bags.hikingbackpack.name=hiking backpack
items.bags.hikingbackpack.desc=This rugged, roomy backpack was made for long climbs, with layered straps and hidden pockets that can carry almost any adventuring supply.\n\nIt stores items only when your main backpack and other containers have no room. As soon as a more suitable container has space, those items are moved back automatically.
```

在中文文件加入：

```properties
items.bags.hikingbackpack.name=登山包
items.bags.hikingbackpack.desc=这只结实宽大的背包原本用于长途攀登，层叠的绑带与暗袋足以收纳各种冒险用品。\n\n当主背包与其他专用容器都无处存放物品时，它会接纳多出的物品；一旦更合适的容器腾出空间，物品便会自动归位。
```

不要修改 `Badges.validateAllBagsBought` 或新增背包徽章。

- [ ] **Step 4: 运行日志文本测试**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.journal.HikingBackpackCatalogTest --no-problems-report
```

Expected: 日志位置、英文回退和中文文本测试全部通过。

- [ ] **Step 5: 记录日志文本任务差异检查点**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java core/src/main/assets/messages/items/items.properties core/src/main/assets/messages/items/items_zh.properties core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/HikingBackpackCatalogTest.java
git status --short -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java core/src/main/assets/messages/items/items.properties core/src/main/assets/messages/items/items_zh.properties core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/HikingBackpackCatalogTest.java
```

### Task 7: 集成回归与验收

**Files:**
- Verify all files listed in Tasks 1-6.
- No production file is added in this task unless a failing regression identifies a defect inside the approved scope.

**Interfaces:**
- Consumes: Tasks 1-6 的全部接口。
- Produces: 可编译、可存档、无背包路由回归的完整功能。

- [ ] **Step 1: 运行登山包定向测试组**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpackTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ImpShopRoomHikingBackpackTest --tests com.shatteredpixel.shatteredpixeldungeon.journal.HikingBackpackCatalogTest --no-problems-report
```

Expected: 全部新增测试通过。

- [ ] **Step 2: 运行相邻背包、恢复和投掷武器回归**

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.items.ItemCollectRestoreTest --tests com.shatteredpixel.shatteredpixeldungeon.items.ItemUpgradeScrollUsesTest --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeaponStackingTest --tests com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeaponExtractionRaidTagTest --no-problems-report
```

Expected: 现有收集、Bundle、堆叠 ID 和提取来源测试全部通过。

- [ ] **Step 3: 运行 core 全量测试和编译**

```powershell
.\gradlew.bat :core:test :core:compileJava --no-problems-report
```

Expected: `BUILD SUCCESSFUL`。若工作区已有与本功能无关的失败，记录测试类和堆栈，不修改无关模块。

- [ ] **Step 4: 检查差异完整性**

```powershell
git diff --check
git diff --name-only
```

Expected: 没有空白错误；本功能差异只包含 File Structure 中列出的生产文件、测试文件和已批准文档。确认 `core/src/main/assets/sprites/items.png` 未出现在差异中，确认 `Badges.java` 未因本功能改变。

- [ ] **Step 5: 执行行为验收**

在桌面调试构建中生成并持有登山包、绒布袋、卷轴筒、药剂挎带和魔法筒袋，依次验证：

```text
1. 背包页签从左到右为：主背包、登山包、绒布袋、卷轴筒、药剂挎带、魔法筒袋。
2. 主背包有空间时，普通装备不会进入登山包。
3. 主背包填满后，新普通装备进入登山包。
4. 卷轴筒填满且主背包有空间时，新卷轴进入主背包；主背包也满时才进入登山包。
5. 从卷轴筒移走一组卷轴后，登山包内卷轴自动进入卷轴筒。
6. 从主背包移走一个普通物品后，登山包内普通装备自动进入主背包。
7. 保存并读档后，页签顺序、物品数量、快捷栏和归位结果保持一致。
```

- [ ] **Step 6: 处理回归结果而不扩大提交范围**

若 Step 1-5 没有发现缺陷，直接结束。若发现本功能范围内缺陷，则只修改 File Structure 已列出的具体文件，重跑对应定向测试和 Step 1-4；不使用目录级 `git add`，也不暂存工作区已有的无关改动。
