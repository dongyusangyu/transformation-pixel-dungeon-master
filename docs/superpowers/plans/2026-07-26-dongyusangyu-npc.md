# 东隅桑榆 NPC 实现计划

> **面向 AI 代理的工作者：** 在当前会话内逐项执行，遵循测试驱动开发和完成前验证。

**目标：** 新增完全免伤、不可移动、可对话且固定生成于 0 层的 `Dongyusangyu` NPC。

**架构：** NPC 行为封装在独立的 `Dongyusangyu` 类中；显示文本走现有 `Messages` 资源；`SurfaceTownLevel` 负责唯一且确定性的生成。现有 `DongyusangyuSprite` 和 `Assets.Sprites.DONGYUSANGYU` 直接复用。

**技术栈：** Java、JUnit 4、Shattered Pixel Dungeon 的 `NPC`/`Level`/`Messages` 框架、Gradle。

---

### 任务 1：NPC 核心行为

**文件：**

- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Dongyusangyu.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/DongyusangyuTest.java`

- [ ] 编写失败测试，断言 `spriteClass` 为 `DongyusangyuSprite`、包含 `IMMOVABLE`、闪避为 `INFINITE_EVASION`、伤害后生命不变、Buff 无法添加。
- [ ] 运行 `gradlew.bat core:test --tests "*DongyusangyuTest"`，确认因类不存在而失败。
- [ ] 编写最小 NPC 类，实现初始化块、`defenseSkill`、`damage`、`add` 和 `interact`。
- [ ] 重跑针对性测试并确认通过。

### 任务 2：0 层固定生成

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java`

- [ ] 编写失败测试，通过测试子类构建地表小镇并调用 `createMobs()`，断言只有一个 `Dongyusangyu` 且位置是 `22 + 26 * 48`。
- [ ] 运行该测试并确认因 NPC 未生成而失败。
- [ ] 在 `SurfaceTownLevel.createMobs()` 中创建 NPC、设置固定坐标并加入 `mobs`。
- [ ] 重跑关卡测试并确认通过。

### 任务 3：本地化与集成验证

**文件：**

- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`

- [ ] 添加 `name`、`desc`、`hello` 默认回退键和中文键。
- [ ] 运行全部新增测试。
- [ ] 运行 `gradlew.bat core:test`。
- [ ] 运行 `gradlew.bat core:compileJava`。
- [ ] 检查 `git diff --check` 和限定范围内的差异，确保未覆盖工作区中的其他修改。
