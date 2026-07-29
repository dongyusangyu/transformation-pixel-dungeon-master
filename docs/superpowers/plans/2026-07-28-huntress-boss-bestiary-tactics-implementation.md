# 女猎手？图鉴与战术行为调整实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:executing-plans 逐任务实现本计划。

**目标：** 按已确认的方案 A，为女猎手？、干扰飞鹰和女猎手触手补齐独立图鉴条目，并实现 30 点单次伤害上限、掩体诱敌、近战不逃、2—4 格二选一战术、二阶段互斥植被生成、初始致盲飞镖以及女猎手专属升华类别标识。

**架构：** 保持现有 `HuntressBoss` 与 `HuntressBossLevel` 的职责边界：Boss 类负责伤害结算、攻击方式和战术决策；楼层类负责竞技场、初始道具与二阶段环境生成；`Bestiary` 只登记实体分类；`ScrollOfSublimation`、`Talent` 和自动规划器共同识别新的 `HUNTRESS` 类型，但将其映射到 DM-300 的现有天赋槽及天赋池。随机战术和环境配额通过可测试的纯函数/小型辅助方法验证，集成行为通过现有 JUnit 4 测试覆盖。

**技术栈：** Java 8、Shattered Pixel Dungeon 现有 Actor/Level API、JUnit 4、Gradle。

---

## 任务 1：补齐图鉴分类、文案与系统清场计数保护

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 新建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/HuntressBossBestiaryTest.java`

### 步骤 1：先写失败测试

验证 `Bestiary.BOSSES.entities()` 独立包含：

- `HuntressBoss.class`
- `HuntressBoss.DistractingHawk.class`
- `HuntressBoss.HuntressTentacle.class`

并验证三者没有被错误登记到普通生物分类。

### 步骤 2：运行测试，确认红灯

运行：

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.journal.HuntressBossBestiaryTest"
```

预期：因三类尚未登记到 `Bestiary.BOSSES` 而失败。

### 步骤 3：完成最小实现

- 在 `Bestiary.BOSSES` 的 DM-300 条目后登记三个女猎手相关实体。
- 为三个实体补齐中英文图鉴描述和发现提示；主 Boss 描述体现限伤、掩体追踪、远近战切换，衍生怪物描述体现致盲/残废和触手牵制。
- `HuntressBossLevel.onBossDefeated()` 清理存活衍生怪物时，以 `try/finally` 临时开启 `Bestiary.skipCountingEncounters`，确保系统清场不增加图鉴击杀计数。

### 步骤 4：运行测试，确认绿灯

再次运行任务 1 的定向测试，预期通过。

---

## 任务 2：实现 30 点限伤与距离战术

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`

### 步骤 1：先写失败测试

新增确定性测试覆盖：

- 任意单次传入伤害最多按 30 点交给基础伤害流程。
- 距离 1：选择近战，且不选择撤离。
- 距离 2—4 且有射线：随机分支为“射击”或“优先脱离玩家视野”。
- 距离至少 5 且有射线：射击。
- Boss 视野内无敌人或无有效射线：像普通怪物一样追踪目标。
- 第一阶段仅远程射击无视护甲，贴身近战仍按目标护甲结算。

优先把战术决策抽成包内可见的纯函数/枚举，避免依赖随机数的脆弱测试。

### 步骤 2：运行测试，确认红灯

运行：

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBossTest"
```

预期：新增断言因缺少伤害上限和新战术决策而失败。

### 步骤 3：完成最小实现

- 在 `damage(int, Object)` 进入 `super.damage` 前将伤害限制到 `[0, 30]`。
- 引入清晰的战术决策：
  - 相邻：近战。
  - 2—4 格：每个可行动回合只抽取一次 50/50 分支。
  - 至少 5 格且可射击：远程攻击。
  - 看不见敌人或不可射击：走普通追踪路径。
- “离开视野”分支优先寻找玩家当前 `heroFOV` 外的合法可达格；不存在时退回普通移动逻辑。
- 近战使用普通攻击动画和护甲结算；狙击阶段只有远程箭矢无视 100% 护甲。
- 保持贯风箭、守望者踩植物和既有击退优先级不变。

### 步骤 4：运行测试，确认绿灯

再次运行 `HuntressBossTest`，预期通过。

---

## 任务 3：实现二阶段三类地物互斥和两堆致盲飞镖

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`

### 步骤 1：先写失败测试

新增测试覆盖：

- 新楼层入口缓冲区有两处不同位置的 heap。
- 每个 heap 恰好含一枚 `BlindingDart`。
- 两个 heap 均在开战触发线外，不会因拾取立即开战。
- 二阶段配额分配后，枯草、随机植物、触手三组格子两两不相交。
- 已占有角色、植物或其他生成物的格子不会被另一类覆盖。

### 步骤 2：运行测试，确认红灯

运行：

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevelTest"
```

预期：致盲飞镖不存在，且旧二阶段生成允许枯草与植物/触手重叠，测试失败。

### 步骤 3：完成最小实现

- 在 `createItems()` 中选择入口缓冲区内两个固定且合法的不同格子，各放置一枚 `BlindingDart`。
- 将二阶段合法格洗牌后按一次性占用集合分配：
  1. 约 1% 触手；
  2. 约 5% 非辅酶随机植物；
  3. 约 50% 枯草。
- 每分配一个格子立即从候选集合移除，确保同格至多出现一种。
- 初始地图已有的 10% 枯草不被植物或触手覆盖；若某类候选不足，宁可少生成也不重叠。

### 步骤 4：运行测试，确认绿灯

再次运行 `HuntressBossLevelTest`，预期通过。

---

## 任务 4：增加 `HUNTRESS` 升华类别并复用 DM-300 天赋

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/ScrollOfSublimation.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agents/AgentMinMetamorphPlanner.java`
- 修改或新建：对应的 `ScrollOfSublimation`、`Talent`、`AgentMinMetamorphPlanner` JUnit 测试

### 步骤 1：先写失败测试

验证：

- 女猎手死亡掉落的密卷类型为 `HUNTRESS`。
- `HUNTRESS` 显示为女猎手类别。
- `HUNTRESS` 与 `DM300` 使用相同的 tier/index、Boss 天赋槽和天赋池。
- 自动规划器能识别 `HUNTRESS`，不会把它当成未知类别。

### 步骤 2：运行相关测试，确认红灯

只运行升华密卷、天赋槽和规划器的相关测试类，预期因未知 `HUNTRESS` 类型而失败。

### 步骤 3：完成最小实现

- 女猎手掉落改为 `new ScrollOfSublimation().type("HUNTRESS")`。
- 在密卷显示名、窗口选项、随机天赋池、tier/index 与增益类型白名单中增加 `HUNTRESS`。
- `Talent.bossTalentSlot("HUNTRESS")` 返回与 DM-300 相同槽位。
- 自动规划器的目标池、tier 和 index 对 `HUNTRESS` 采用 DM-300 映射。
- 不复制新天赋，明确保留未来替换空间。

### 步骤 4：运行测试，确认绿灯

再次运行相关定向测试，预期全部通过。

---

## 任务 5：整体验证

**文件：**

- 验证所有本次修改文件

### 步骤 1：运行女猎手相关定向测试

```powershell
.\gradlew.bat :core:test --tests "*HuntressBoss*"
```

### 步骤 2：运行 Android Java 编译

```powershell
.\gradlew.bat :android:compileDebugJavaWithJavac
```

### 步骤 3：运行 core 全量测试

```powershell
.\gradlew.bat :core:test
```

记录新旧失败差异。若仍只有已知的 `TreasureJournalViewTest` 两项无关失败，则如实报告，不把全量测试描述为全绿。

### 步骤 4：检查工作区差异

使用只读 Git 差异确认：

- 未覆盖用户已有改动。
- 没有误改女猎手功能范围外的文件。
- 没有生成临时文件、构建产物或提交。

