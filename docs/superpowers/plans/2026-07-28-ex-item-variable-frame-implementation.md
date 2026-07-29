# EXItemSpriteSheet 可变尺寸编码实现计划

> **面向 AI 代理的工作者：** 使用 `executing-plans` 在当前工作区内联执行。每个生产改动必须遵循 TDD 红—绿循环，并保护现有未提交改动。

**目标：** 让扩展物品贴图的整数常量同时编码素材索引、宽度和高度，并让 `ItemSprite` 按编码尺寸截取 UV。

**架构：** 在 `EXItemSpriteSheet` 的单个 `int` 中以位字段保存索引、EX 标志、宽度和高度。图集定位继续使用固定 16×16 网格，`ItemSprite` 仅将每格 UV 的右、下边界改为编码的 `w`、`h`。

**技术栈：** Java、JUnit 4、Gradle、PNG alpha 边界分析。

**工作区约束：** 当前仓库包含大量用户未提交改动；不创建分支、不暂存、不提交，不修改 `ex_items.png`。

---

### 任务 1：用测试定义新的编码协议与尺寸表

**文件：**

- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

- [ ] **步骤 1：替换固定 16×16 断言**

新增测试，要求：

```java
assertEquals(48, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.META_INFUSE));
assertEquals(10, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.META_INFUSE));
assertEquals(15, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.META_INFUSE));
```

并以完整数组逐项验证规格中的 23 个常量（22 个已绘制素材和保留 16×16 的 `SEAL`）。

- [ ] **步骤 2：定义普通索引兼容行为**

```java
assertFalse(EXItemSpriteSheet.isEX(ItemSpriteSheet.SEAL));
assertEquals(ItemSpriteSheet.SEAL,
        EXItemSpriteSheet.frameFor(ItemSpriteSheet.SEAL));
```

- [ ] **步骤 3：运行测试确认红灯**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report :core:test `
  --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest
```

预期：编译失败，提示 `frameWidth`、`frameHeight` 不存在，证明新 API 尚未实现。

### 任务 2：实现位打包编码与全部常量尺寸

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

- [ ] **步骤 1：定义位字段**

```java
private static final int INDEX_MASK = 0xFFFF;
private static final int EX_SHEET_FLAG = 1 << 16;
private static final int DIMENSION_MASK = 0x1F;
private static final int WIDTH_SHIFT = 17;
private static final int HEIGHT_SHIFT = 22;
```

- [ ] **步骤 2：实现三参数编码和解码**

```java
private static int encode(int image, int width, int height) {
    if (image < 0 || image > INDEX_MASK
            || width < 1 || width > ItemSpriteSheet.SIZE
            || height < 1 || height > ItemSpriteSheet.SIZE) {
        throw new IllegalArgumentException();
    }
    return image | EX_SHEET_FLAG | width << WIDTH_SHIFT | height << HEIGHT_SHIFT;
}

static int frameWidth(int image) {
    return isEX(image)
            ? image >> WIDTH_SHIFT & DIMENSION_MASK
            : ItemSpriteSheet.film.width(image);
}

static int frameHeight(int image) {
    return isEX(image)
            ? image >> HEIGHT_SHIFT & DIMENSION_MASK
            : ItemSpriteSheet.film.height(image);
}
```

`isEX` 改为检查标志位，`frameFor` 对扩展编码使用 `INDEX_MASK`。

- [ ] **步骤 3：按规格尺寸表更新所有常量**

例如：

```java
public static final int MUISCA_GOLDEN_RAFT = encode(0, 14, 14);
public static final int META_INFUSE = encode(48, 10, 15);
public static final int SEAL = encode(ItemSpriteSheet.SEAL, 16, 16);
```

移除旧的单参数 `encode(image)`，确保未来新增素材必须声明尺寸。

- [ ] **步骤 4：运行聚焦测试确认绿灯**

运行任务 1 的命令。

预期：`EXItemSpriteSheetTest` 全部通过。

### 任务 3：让 ItemSprite 使用编码宽高

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSprite.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

- [ ] **步骤 1：添加 ItemSprite 源码契约测试**

测试读取 `ItemSprite.java`，确认扩展分支分别调用：

```java
EXItemSpriteSheet.frameWidth(image)
EXItemSpriteSheet.frameHeight(image)
targetTexture.uvRect(left, top, left + width, top + height)
```

并确认不再调用 `EXItemSpriteSheet.frameSize()`。

- [ ] **步骤 2：运行测试确认红灯**

预期：源码契约断言失败，因为当前实现仍固定截取 16×16。

- [ ] **步骤 3：最小修改扩展贴图分支**

```java
int width = EXItemSpriteSheet.frameWidth(image);
int height = EXItemSpriteSheet.frameHeight(image);
frame(targetTexture.uvRect(left, top, left + width, top + height));
```

删除不再使用的 `frameSize()`；普通图集分支保持不变。

- [ ] **步骤 4：运行聚焦测试确认绿灯**

预期：编码测试和 ItemSprite 源码契约测试全部通过。

### 任务 4：完整验证与差异审查

**文件：**

- 验证：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- 验证：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSprite.java`
- 验证：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`
- 验证未修改：`core/src/main/assets/sprites/ex_items.png`

- [ ] **步骤 1：运行关联测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report :core:test `
  --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.items.treasures.TreasureSpriteMappingTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestPotionTreasureTest
```

- [ ] **步骤 2：强制重新编译生产代码**

```powershell
.\gradlew.bat --no-daemon --no-problems-report :core:compileJava --rerun-tasks
```

- [ ] **步骤 3：验证贴图未变化**

确认 `core/src/main/assets/sprites/ex_items.png` 的 SHA-256 仍为：

```text
9F8CDE10A31F731923AB1C8E4E0EBD11C756A763E0F95C299929CFB11B7793BB
```

- [ ] **步骤 4：检查差异**

```powershell
git diff --check -- `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSprite.java
```

对未跟踪的 `EXItemSpriteSheet.java` 和测试文件额外检查尾随空白，并人工确认没有覆盖用户已有的 EX 图集接入逻辑。

