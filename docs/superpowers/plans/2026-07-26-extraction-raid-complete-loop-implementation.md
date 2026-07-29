# 搜打撤小游戏完整闭环实现计划

> **面向 AI 代理的工作者：** 使用测试驱动开发执行本计划。当前工作区包含本功能
> 依赖的未提交搜打撤楼层与用户改动，不创建新 worktree，不暂存或提交文件。

**目标：** 完成第 31 层分支 1 搜打撤小游戏的价格、掉落、钥匙、撤离、失败损失、
负重和 0 层店主入口。

**架构：** `ExtractionRaidLevel` 负责本局地图实体与战利品；新的局次管理器和持久化
Buff 负责跨楼层生命周期；`Item` 持久化局次标记避免与入场物品混淆；店主只负责
菜单入口；负重逻辑集中在独立 Buff/工具中，由三种装备加速入口调用。

**技术栈：** Java 8、JUnit 4、Gradle、现有 Bundle/Random/Notes/InterlevelScene API。

---

### 任务 1：藏品价格公式

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/Treasures.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/` 下 20 个具体类
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasuresTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/items/treasures/TreasureCatalogTest.java`

- [ ] 先写价格倍率、500/5,000,000 边界和 5001.06075 解析期望测试。
- [ ] 运行藏品测试，确认旧的固定价格行为导致红灯。
- [ ] 实现品质倍率、TOP 传奇溢价和新基础价格表。
- [ ] 运行藏品测试确认绿灯。

### 任务 2：局次物品标记与负重数学

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRun.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/ExtractionOverburden.java`
- 修改：`RingOfHaste.java`、`Swiftness.java`、`Flow.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/ExtractionRaidRunTest.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/ExtractionOverburdenTest.java`

- [ ] 先写标记 Bundle 往返、不同局次不合并和按背包格计数测试。
- [ ] 写 2/3 指数衰减、最低 1 倍和非加速倍率不变测试并确认红灯。
- [ ] 实现 `Item` 局次标记和负重三级 Buff。
- [ ] 将负重衰减接入戒指、迅捷和涌流倍率。
- [ ] 运行两组定向测试确认绿灯。

### 任务 3：楼层战利品、钥匙与撤离点

**文件：**
- 修改：`ExtractionRaidLevel.java`
- 修改：`RaidExtractionRoom.java`
- 创建：`levels/minigame/extraction/mobs/RaidKeyCarrier.java`（若无需包装则使用工具方法）
- 修改：`ExtractionRaidLevelTest.java`

- [ ] 先写 3–4 藏品、1–2 组数量 3 的附魔投掷武器测试，并断言没有额外散落近战武器或护甲。
- [ ] 先写一只钥匙携带怪、一把金钥匙和装有第二把水晶钥匙的金箱测试。
- [ ] 运行楼层测试确认红灯。
- [ ] 实现分散选点、战利品标记、最终章节 20%/80% 等阶生成和强数量保证。
- [ ] 实现撤离点触发入口并运行楼层测试确认绿灯。

### 任务 4：成功、失败与死亡拦截

**文件：**
- 修改：`ExtractionRaidRun.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/Level.java` 或撤离层触发点
- 修改：`ExtractionRaidRunTest.java`

- [ ] 先写成功清除标记并按双钥匙奖励 2000 的测试。
- [ ] 先写失败删除局内物品、损失 1–2 件合格消耗品且保护贵重物品的测试。
- [ ] 先写死亡被局次逻辑接管而不是进入 `reallyDie` 的契约测试并确认红灯。
- [ ] 实现持久化局次 Buff、主动放弃、成功/失败结算和返回 0 层。
- [ ] 确保所有入场路径附加负重 Buff，所有成功/失败离场路径移除负重 Buff。
- [ ] 运行局次测试确认绿灯。

### 任务 5：0 层店主唯一入口

**文件：**
- 修改：`SurfaceShopkeeper.java`
- 修改：`SurfaceShopkeeperTest.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/main/assets/messages/levels/levels.properties`
- 修改：`core/src/main/assets/messages/levels/levels_zh.properties`

- [ ] 先写入场费、金币不足、活动局次防重复进入和新楼层强制生成测试。
- [ ] 运行店主测试确认红灯。
- [ ] 在店主菜单插入搜打撤选项和风险确认窗口。
- [ ] 实现扣费、保存返回位置和新楼层切换。
- [ ] 补齐双语文本并运行店主测试确认绿灯。

### 任务 6：集成验收

**文件：** 本计划涉及的全部文件。

- [ ] 运行所有藏品、店主和 `levels.minigame.extraction` 定向测试。
- [ ] 运行 `:core:compileJava :core:compileTestJava`。
- [ ] 检查局次保存/恢复、上一局钥匙清理、死亡回城和再次入场重新随机。
- [ ] 运行限定任务文件的 `git diff --check`。
- [ ] 独立审查需求覆盖，再审查代码质量；修复所有 Critical/Important 问题。
