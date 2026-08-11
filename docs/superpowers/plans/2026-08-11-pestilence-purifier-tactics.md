# 瘟疫骑士药瓶选点与净化器战术实现计划

> **面向 AI 代理的工作者：** 在当前会话内按测试驱动完成本计划；每项生产变更必须先有失败测试。

**目标：** 实现弹道降级投掷、净化器守点移动、10 英雄回合冷却和外部清除苍白瘴气开战。

**架构：** `PestilenceKnight` 负责投掷与移动决策，`PestilenceArenaController` 负责净化器位置、冷却与外观，`PaleMiasma` 向 `TowerBossLevel` 报告显式清除事件。净化器清场使用短生命周期抑制标记防止事件重入。

**技术栈：** Java、JUnit 4、Shattered Pixel Dungeon `Ballistica`/`Trap`/`Blob` API。

---

### 任务 1：锁定药瓶选点和守点移动

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnightTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnight.java`

- [ ] 新增失败测试：玩家弹道有效优先玩家；否则优先净化器；两者无效使用随机格。
- [ ] 新增失败测试：游荡或逃离时优先净化器守点，普通追击保持原目标。
- [ ] 运行聚焦测试确认因新决策接口缺失而失败。
- [ ] 接入 `Ballistica.PROJECTILE`、竞技场随机目标和净化器邻格守点。
- [ ] 运行聚焦测试确认通过。

### 任务 2：改为十英雄回合冷却与状态外观

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/PestilenceArenaControllerTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/PestilenceArenaController.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/traps/PlagueBrazier.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnight.java`

- [ ] 新增失败测试：激活后为 10，Boss 行动不推进，十次英雄回合推进到零。
- [ ] 新增失败测试：冷却时陷阱未填充，归零后恢复，Bundle 往返后可同步。
- [ ] 实现 `Arena.setPurifierReady`，由控制器在激活、推进和恢复时更新陷阱 `active`。
- [ ] 移除 Boss 行动和终末阶段对净化器冷却的推进或重置。
- [ ] 运行控制器与 Boss 聚焦测试确认通过。

### 任务 3：实现外部清除苍白瘴气开战

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/blobs/tboss/PlagueMiasmaTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/blobs/tboss/PaleMiasma.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/PestilenceArenaController.java`

- [ ] 新增失败测试：只有开场中、Boss 未启动、非净化器清场且实际移除苍白瘴气才启动。
- [ ] 新增接线测试：`clear` 与 `fullyClear` 均通知楼层，自然 `evolve` 不通知。
- [ ] 在楼层加入幂等开场回调与净化器清场抑制区间。
- [ ] 确认第二路径不调用 `clearAllMiasma`。
- [ ] 运行四类回归测试，检查 JUnit XML、`git diff --check` 与目标文件范围。
