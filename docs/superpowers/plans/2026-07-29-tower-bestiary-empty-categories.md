# 高塔单位图鉴空分组实现计划

**目标：** 在单位图鉴现有分组末尾增加“高塔生物”和“高塔 Boss”两个空分组。

**架构：** 直接扩展 `Bestiary` 枚举，使 `WndJournal.CatalogTab` 现有的 `Bestiary.values()` 渲染逻辑自动展示新分组。两个枚举值暂不注册实体，因此计数保持为 `0/0`，后续可以通过现有 `addEntities` 初始化方式填充。

**技术栈：** Java、JUnit 4、Gradle、properties 本地化资源。

---

### 任务 1：固定空分组行为

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`

- [ ] 测试两个分组位于 `Bestiary.values()` 末尾。
- [ ] 测试两个分组的实体集合为空。
- [ ] 测试默认语言和简体中文标题存在且内容正确。
- [ ] 运行目标测试，确认因枚举值尚不存在而失败。

### 任务 2：增加高塔图鉴分组

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`
- 修改：`core/src/main/assets/messages/journal/journal.properties`
- 修改：`core/src/main/assets/messages/journal/journal_zh.properties`

- [ ] 在 `PLANT` 后追加 `TOWER_MOBS` 与 `TOWER_BOSSES`。
- [ ] 添加 `Tower Creatures`、`Tower Bosses` 默认标题。
- [ ] 添加“高塔生物”“高塔 Boss”中文标题。

### 任务 3：验证

- [ ] 运行 `TowerBestiaryCategoriesTest`。
- [ ] 运行单位图鉴相关测试。
- [ ] 强制重新编译 `core` 模块并检查目标文件差异。
