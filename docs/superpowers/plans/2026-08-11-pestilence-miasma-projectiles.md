# 瘟疫骑士二、三阶段瘴气投掷动画实现计划

> **面向 AI 代理的工作者：** 在当前会话内使用测试驱动开发完成本计划。

**目标：** 为检疫封锁、苍白冲锋和末日行列的每次 Blob 播种增加颜色匹配的药瓶投掷动画。

**架构：** 扩展 `PestilenceKnight` 现有 `MissileSprite` 投掷辅助方法，使技能决定药瓶贴图、飞溅颜色和播种代表格。既有技能状态机保持不变。

**技术栈：** Java、JUnit 4、Shattered Pixel Dungeon `MissileSprite`/`Splash` API。

---

### 任务 1：锁定动画映射

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnightTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/PestilenceKnightSpriteAssetTest.java`

- [ ] 新增失败测试，断言检疫封锁使用翡翠药瓶和绿色飞溅，苍白冲锋/末日行列使用银白药瓶和苍白飞溅。
- [ ] 新增失败测试，断言代表格来自实际播种格中点，空数组返回无效格。
- [ ] 运行两类测试并确认因缺少映射接口和接线而失败。

### 任务 2：实现并验证

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnight.java`

- [ ] 将现有投掷方法泛化为按技能选择贴图、颜色和代表格。
- [ ] 在首次预警及末日行列后续每次预警建立时播放对应投掷动画。
- [ ] 运行聚焦测试并确认通过。
- [ ] 运行瘟疫骑士、瘴气及素材回归测试，检查 JUnit XML 与 `git diff --check`。
