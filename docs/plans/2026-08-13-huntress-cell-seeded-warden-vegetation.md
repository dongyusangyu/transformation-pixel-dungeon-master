# 女猎手二阶段逐格植被生成实现计划

> **面向 AI 代理的工作者：** 使用 subagent-driven-development 执行本计划，并在实现后依次进行规格审查与代码质量审查。

**目标：** 将女猎手二阶段的定额聚簇生成替换为按合法战斗格独立判定的可复现生成：2% 触手、10% 植物、50% 枯草、38% 保持原地形。

**架构：** `HuntressBossLevel` 遍历稳定顺序的合法格，对每格临时压入 `Dungeon.seed + cell` 的随机流，用一个随机值选择互斥结果；植物类型继续使用同一格随机流的下一次抽取。外层随机流用 `try/finally` 完整恢复，现有角色、Heap、植物、掩体及保留格过滤保持不变。

**技术栈：** Java、JUnit 4、项目 `com.watabou.utils.Random`、Gradle Core 测试。

---

### 任务 1：二阶段逐格确定性生成

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`

- [ ] **步骤 1：先编写失败测试**

  在 `HuntressBossLevelTest` 中替换旧的定额/聚簇断言并新增：

  1. 单个随机值的边界映射为 `[0,.02)` 触手、`[.02,.12)` 植物、`[.12,.62)` 枯草、其余保持原地形。
  2. 同一 `Dungeon.seed` 与同一格索引始终得到相同结果，遍历顺序改变也不影响任一格；不同游戏种子至少改变一部分格。
  3. 每个格子的结果等于用 `Dungeon.seed + cell` 独立初始化项目随机数生成器后的结果。
  4. 植物类型由同一格随机流在类别判定之后继续抽取，且不依赖其他植物格的数量或遍历顺序。
  5. 初始 `FURROWED_GRASS` 若判定为植物/触手则清除枯草，判定为枯草或保持原地形时维持枯草。
  6. 真实二阶段落图仍保护 Actor、Heap、已有植物、掩体和保留格，三类生成物两两互斥。
  7. 调用生成前后的外层 `Random` 序列完全一致。

- [ ] **步骤 2：运行测试并确认 RED**

  运行：

  ```powershell
  .\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevelTest" --no-daemon --no-problems-report --rerun-tasks --console=plain
  ```

  预期：新增逐格判定 API 缺失或旧定额/聚簇行为导致测试失败；确认失败来自目标行为尚未实现。

- [ ] **步骤 3：实现最小生产改动**

  在 `HuntressBossLevel` 中：

  1. 删除二阶段对 `phaseTwoCounts`、`clusteredOrder` 和植被聚簇盐的依赖。
  2. 增加单值到 `WardenFeature` 的纯映射，并对每个未阻塞合法格使用 `Random.pushGenerator(Dungeon.seed + cell)`。
  3. 在同一格随机流内先抽类别；若为植物，再抽 `ARENA_SEEDS` 索引并创建植物；每格均用 `finally` 弹出随机流。
  4. `FURROW` 设置 `FURROWED_GRASS`；`PLANT`/`TENTACLE` 将该格恢复为其合法基础空地表现；`NONE` 不改动触发二阶段前的地形。
  5. 保持现有阻塞格汇总、触手阵营继承、GameScene 提交和生成物互斥。

- [ ] **步骤 4：运行定向测试并确认 GREEN**

  重复步骤 2 的命令，要求 `HuntressBossLevelTest` 全部通过且没有 failure/error。

- [ ] **步骤 5：回归、静态检查与双审**

  运行：

  ```powershell
  .\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBossTest" --tests "com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevelTest" --no-daemon --no-problems-report --rerun-tasks --console=plain
  git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java
  ```

  规格审查逐项核对概率、逐格种子、原地形语义、合法格过滤与随机隔离；通过后再进行质量审查，检查随机栈恢复、重复生成、副作用和测试全局状态清理。发现问题时由同一实现代理按 RED→GREEN 修复并重新审查。
