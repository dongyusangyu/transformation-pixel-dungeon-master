# 迷彩豺狼高塔怪物实现计划

> **执行方式：** 在当前会话中使用 `executing-plans`，按红—绿—重构顺序实施。

**目标：** 完成迷彩豺狼的怪物逻辑、概念图、12×16 像素动画、高塔生成和单位图鉴注册，并沉淀可复用的高塔怪物制作流程。

**架构：** 怪物逻辑位于 `actors.mobs.tmobs`，动画位于 `sprites.tmobs`，资源仍由 `Assets.Sprites` 统一索引。高塔第 6 层开始通过 `TowerLevel` 的独立生成规则，以 20% 概率替换普通生成结果；单位图鉴通过 `Bestiary.TOWER_MOBS` 注册。草地判定集中在怪物类中，覆盖草地、高草和犁过的草地。

**技术栈：** Java、JUnit 4、Gradle、PNG RGBA、Python/Pillow 精灵资源生成与校验。

---

### 任务 1：怪物数值与地形能力

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CamouflageGnoll.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CamouflageGnollTest.java`

- [ ] 先编写失败测试，要求 HP/HT=100、伤害 20–30、攻击 40、闪避 20、护甲 0–10。
- [ ] 测试普通地形中毒概率为 `1/3`，草地中毒概率为 `1`，草地闪避为 30。
- [ ] 运行测试并确认因类缺失而失败。
- [ ] 实现最小怪物逻辑：近战攻击、4 回合中毒、三类草地判定、默认移速和攻击间隔。
- [ ] 重跑测试并确认通过。

### 任务 2：动画、资源和美术

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/CamouflageGnollSprite.java`
- 创建：`core/src/main/assets/sprites/camouflage_gnoll.png`
- 创建：`docs/pixel-art/camouflage-gnoll/camouflage_gnoll_concept.png`
- 创建：`docs/pixel-art/camouflage-gnoll/camouflage_gnoll_preview.png`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`

- [ ] 用现有四张豺狼精灵表作为参考生成待机、行走、攻击、死亡、受击/中毒攻击和草地伪装概念图。
- [ ] 依据概念图绘制 13 帧、每帧 12×16 的透明 PNG：待机 0–1、攻击 2–3、行走 4–7、死亡 8–10、伪装 11–12。
- [ ] 使用硬边、无抗锯齿、整数坐标和不超过 12 个可见颜色。
- [ ] 实现标准动画与草地伪装待机动画。
- [ ] 生成 8 倍最近邻预览并检查轮廓、朝向、武器轨迹和死亡连续性。

### 任务 3：高塔生成和单位图鉴

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/TowerLevelSelectionTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`

- [ ] 先写失败测试，要求第 1–5 层不生成迷彩豺狼，第 6 层起具备 20% 生成概率。
- [ ] 测试 `Bestiary.TOWER_MOBS` 只新增迷彩豺狼，并保持高塔 Boss 栏目为空。
- [ ] 在 `TowerLevel` 中提取可测试的生成判定方法，并接入 `createMob()`。
- [ ] 将怪物注册到高塔生物栏目，补齐中英文名称、描述和发现提示。
- [ ] 重跑测试并确认通过。

### 任务 4：通用制作流程和自动校验

**文件：**
- 创建：`tools/tower_monster_creation_workflow.md`
- 创建：`tools/validate_tower_monster_sprite.py`
- 最终复制到：`D:\STUDY\Dungeon\tools\`

- [ ] 编写中文流程，覆盖需求表、21–25 层数值基线、相似怪物检索、概念图提示词、像素帧表、动画接入、图鉴/生成池和验证清单。
- [ ] 编写 PNG 校验工具，检查 RGBA、帧尺寸不超过 16×16、宽高整除、非空帧、硬透明边缘和可见颜色数量。
- [ ] 用迷彩豺狼资源运行校验工具并确认通过。
- [ ] 取得目录写权限后，将两个文件复制到指定外部目录。

### 任务 5：完成前验证

- [ ] 运行迷彩豺狼、高塔生成和图鉴针对性 JUnit 测试。
- [ ] 运行精灵 PNG 校验工具并检查全部 13 帧。
- [ ] 运行 `gradlew.bat core:test`，确认没有相关回归。
- [ ] 运行 `gradlew.bat core:compileJava` 或等价构建任务，确认生产代码可编译。
- [ ] 用 `git diff --check` 检查新增和修改文件的空白错误，并逐项核对本计划。
