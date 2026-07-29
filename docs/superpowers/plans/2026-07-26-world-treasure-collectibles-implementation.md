# 世界文明藏品道具实现计划

> **面向 AI 代理的工作者：** 使用 `executing-plans`，严格按 TDD
> 红灯—绿灯顺序执行。当前工作区包含本功能依赖的未提交用户改动，不创建
> 新 worktree，也不擅自提交。

**目标：** 实现 20 件使用 `ex_items.png` 的藏品道具及按
1%/4%/10%/85% 分组生成的 `TreasureGenerator`。

**架构：** `Treasures` 保存随机品质并承载固定 `CollectionRarity`、
图标和价格；20 个无参具体类只声明自身元数据；`TreasureGenerator`
先选固定稀有度组，再在组内等概率实例化；`EXItemSpriteSheet` 使用独立的
16×16 完整格坐标。

**技术栈：** Java 8、JUnit 4、Gradle、Noosa 纹理 UV API、现有
Messages/Bundle/Random API。

---

### 任务 1：先写藏品目录、生成概率和扩展格坐标的失败测试

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasureCatalogTest.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasureGeneratorTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheetTest.java`

- [ ] 编写 20 个类的图片编号、固定稀有度、价格和资源键断言。
- [ ] 编写 0.01、0.05、0.15 三处概率边界及 1/3/6/10 组成员断言。
- [ ] 编写 EX 第 0、19 帧使用完整 16×16 格坐标的断言。
- [ ] 运行聚焦测试，确认因新 API/类尚不存在而失败。

运行：

```powershell
.\gradlew.bat :core:test `
  --tests com.shatteredpixel.shatteredpixeldungeon.items.treasures.TreasureCatalogTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.items.treasures.TreasureGeneratorTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheetTest
```

预期：FAIL，缺少 `CollectionRarity`、20 个具体类、生成器和 EX 常量/格坐标。

### 任务 2：实现扩展贴图常量和完整格坐标

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/EXItemSpriteSheet.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSprite.java`

- [ ] 定义第 0–19 格的具名编码常量。
- [ ] 增加 EX 专用 16×16 坐标计算函数。
- [ ] 让 `ItemSprite.frame()` 从路由结果选择纹理，并为 EX 图标设置完整格 UV。
- [ ] 运行 `EXItemSpriteSheetTest`，确认绿灯。

### 任务 3：扩展 Treasures 的固定元数据模型

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/Treasures.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasuresTest.java`

- [ ] 新增 `CollectionRarity` 及只读访问器。
- [ ] 增加受保护构造函数，接收图标、固定稀有度和价格，同时随机品质。
- [ ] `value()` 返回固定价格乘数量。
- [ ] 描述分别显示固定藏品稀有度和随机品质。
- [ ] 保留现有品质概率与 Bundle 持久化行为并运行 `TreasuresTest`。

### 任务 4：实现 20 个具体藏品类与双语文本

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/` 下规格列出的 20 个类
- 修改：`core/src/main/assets/messages/items/items.properties`
- 修改：`core/src/main/assets/messages/items/items_zh.properties`

- [ ] 每个类提供公开无参构造函数并调用父类元数据构造函数。
- [ ] 添加默认英文 `name`、`desc`。
- [ ] 添加中文 `name`、`desc`，涵盖来历、时代、文明和特点。
- [ ] 运行 `TreasureCatalogTest`，确认 20 组断言全部通过。

### 任务 5：实现分组生成器

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasureGenerator.java`

- [ ] 建立 1/3/6/10 个类的四组不可变目录。
- [ ] 实现可测试的概率边界选择方法。
- [ ] 实现 `random()`，组内使用 `Random.Int(group.length)` 等概率选择。
- [ ] 运行 `TreasureGeneratorTest`，确认边界和成员全部通过。

### 任务 6：全量验收

**文件：**
- 核对本计划涉及的全部文件。

- [ ] 运行三个聚焦测试类。
- [ ] 运行 `.\gradlew.bat :core:compileJava`。
- [ ] 运行 `git diff --check` 限定到本任务文件。
- [ ] 核对 `ex_items.png` 未被修改，20 个图标常量按 0–19 顺序对应。
