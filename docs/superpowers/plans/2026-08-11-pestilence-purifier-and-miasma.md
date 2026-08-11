# 瘟疫骑士净化触发器与瘴气强化实现计划

> **面向 AI 代理的工作者：** 在当前会话内按测试驱动方式逐项执行，每一步先验证 RED，再编写最小实现并验证 GREEN。

**目标：** 实现移动式即时全图净化触发器、入场净化教学、十倍且会扩散的前两阶段瘴气，以及二阶段紫色混乱的明确视觉反馈。

**架构：** `PestilenceArenaController` 负责触发器、教学瘴气、迁移与持久化；`TowerBossLevel` 负责“教学开始”和“净化后生成 Boss”的编排；瘴气扩散由新的共享基类封装；`PestilenceKnight` 只负责技能播种量、紫色视觉和净化器伤害规则。

**技术栈：** Java、JUnit 4、Shattered Pixel Dungeon Blob/Trap/GameScene API、Bundle 持久化。

---

### 任务 1：锁定瘴气数量和扩散规格

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/blobs/tboss/SpreadingPlagueMiasma.java`
- 修改：`IncubatingMiasma.java`、`OutbreakMiasma.java`、`PestilenceKnight.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/blobs/tboss/PlagueMiasmaTest.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnightTest.java`

- [ ] 新增失败测试，断言第一、二阶段属于扩散瘴气，第三阶段不属于；扩散量为 20，第一阶段隔轮、第二阶段每轮。
- [ ] 新增失败测试，断言四种技能播种量为 60、80、80、70。
- [ ] 运行两类测试，确认因共享扩散类型和新常量缺失而失败。
- [ ] 提取可存档确定性扩散基类，改造第一、二阶段，并更新技能播种量。
- [ ] 重跑测试确认通过。

### 任务 2：实现单个移动式即时净化触发器

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/PestilenceArenaController.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/traps/PlagueBrazier.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/PestilenceArenaControllerTest.java`

- [ ] 将旧四香炉测试改写为单个左侧、可达、唯一触发器，并新增迁移候选距离测试。
- [ ] 新增失败测试，断言进入格立即激活、全图清除、冷却 12、迁移且 Bundle 往返保持随机状态。
- [ ] 运行测试确认旧固定四香炉和等待逻辑导致失败。
- [ ] 将控制器状态改为单触发器，增加安全道路、全图净化、确定性迁移和旧字段兼容。
- [ ] 重跑控制器测试确认通过。

### 任务 3：实现入场教学和净化后生成 Boss

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLayout.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java`

- [ ] 新增失败测试，断言进入竞技场只开始教学并封门，不生成 Boss。
- [ ] 新增失败测试，断言首次踩触发器清场后只生成一个满血 Boss，后续触发才施加 100 点伤害。
- [ ] 新增失败测试，断言入场 20000 层苍白瘴气覆盖大部分场地且安全道路无瘴气。
- [ ] 实现教学启动、预定 Boss 格、触发回调和生成时序；保留 `TowerBossEncounter` 的通用生成生命周期。
- [ ] 重跑楼层与生成器测试确认通过。

### 任务 4：补足紫色处方和引导反馈

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnight.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/main/assets/messages/levels/levels.properties`
- 修改：`core/src/main/assets/messages/levels/levels_zh.properties`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/PestilenceKnightSpriteAssetTest.java`

- [ ] 新增失败测试，断言紫色法术弹、紫色飞溅和诅咒粒子接线。
- [ ] 新增失败测试，断言中英文文本不再要求等待，并说明全图净化、迁移与 100 点伤害。
- [ ] 实现非阻塞视觉反馈和教学/触发/冷却提示。
- [ ] 重跑视觉及文本测试确认通过。

### 任务 5：整体验证与提交

**文件：** 上述全部生产与测试文件。

- [ ] 运行瘟疫 Blob、Boss、场地控制器、Boss 楼层、生成器、奖励和素材测试。
- [ ] 检查 JUnit XML 中测试数、失败、错误和 `system-err`。
- [ ] 运行 `git diff --check`，逐项核对需求并检查共享脏文件只暂存本次文本 hunk。
- [ ] 在干净临时工作树复跑相关测试。
- [ ] 提交为独立功能提交，不带入用户其他未提交修改。
