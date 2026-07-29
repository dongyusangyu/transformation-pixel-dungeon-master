# 怪物测试场实现计划

**目标：** 新增独立怪物测试场，并通过阵营调试器安全进入和离开。

**架构：** `TestArenaLevel` 负责纯圆形地图；`Dungeon.newLevel()` 负责 branch 2 路由；`TestAlignment` 负责保存返回点并发起跨层切换。

**技术栈：** Java、JUnit 4、现有 `Level`/`InterlevelScene`/`Buff` API。

---

### 任务 1：测试场地图

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/levels/TestArenaLevelTest.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/levels/TestArenaLevel.java`

- [ ] 编写尺寸、圆形边界、无转换和无初始内容测试。
- [ ] 运行测试并确认因类不存在而失败。
- [ ] 实现最小圆形楼层。
- [ ] 运行测试并确认通过。

### 任务 2：楼层路由与出入

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestAlignment.java`

- [ ] 将测试模式的 branch 2、depth 30 路由到 `TestArenaLevel`。
- [ ] 为阵营调试器添加动态进入/离开动作。
- [ ] 用持久化 Buff 保存返回深度、分支和位置。
- [ ] 切换前解除时间冻结并调用楼层过渡钩子。

### 任务 3：文本与验证

**文件：**
- 修改：`core/src/main/assets/messages/custom/custom.properties`
- 修改：`core/src/main/assets/messages/custom/custom_zh.properties`

- [ ] 添加动作名称和错误提示。
- [ ] 运行聚焦测试与核心编译。
- [ ] 执行 `git diff --check` 并核对改动范围。
