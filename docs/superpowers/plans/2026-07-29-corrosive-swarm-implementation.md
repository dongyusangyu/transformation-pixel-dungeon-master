# 腐蚀蝇群实现计划

> **面向 AI 代理的工作者：** 在当前会话中使用 `executing-plans`，逐任务执行红—绿—重构。

**目标：** 完成仅在高塔负楼层生成的腐蚀蝇群，包括类型保持的分裂、可叠加腐蚀爆裂、图鉴文案、概念图和 16×16 动画资源。

**架构：** `CorrosiveSwarm` 继承 `Swarm`；基础类新增分裂对象工厂和分裂阈值两个钩子。`TowerMobRules` 以单次随机数返回三种生成结果，`TowerLevel` 负责实例化。爆裂效果使用每条链共享的已触发集合，同类通过 0 伤害继续触发但每只每链仅一次；腐蚀应用拆成单目标方法，以便在无图形环境的 JUnit 中测试。

**技术栈：** Java、JUnit 4、Gradle、PNG RGBA、Python/Pillow、内置图像生成。

---

### 任务 1：分裂类型与怪物数值

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Swarm.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CorrosiveSwarm.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/CorrosiveSwarmTest.java`

- [ ] 编写失败测试，断言 HP 200、伤害 15–25、命中 40、闪避 20、护甲 0–10、速度/攻击间隔 1、经验 13。
- [ ] 编写失败测试，断言 `CorrosiveSwarm.createSplit()` 返回同类，普通 `Swarm.createSplit()` 仍返回普通蝇群。
- [ ] 编写失败测试，断言腐蚀蝇群仅在受击前 HP > 10 时分裂，普通蝇群阈值保持原样。
- [ ] 运行 `gradlew.bat --no-problems-report core:compileTestJava`，确认因类和工厂钩子缺失而失败。
- [ ] 在 `Swarm` 添加 `protected Swarm createSplit() { return new Swarm(); }`，并让私有 `split()` 使用该工厂。
- [ ] 实现 `CorrosiveSwarm` 数值并覆写工厂，运行定向测试至通过。

### 任务 2：腐蚀爆裂

**文件：**
- 修改：`CorrosiveSwarm.java`
- 测试：`CorrosiveSwarmTest.java`

- [ ] 先写失败测试，使用真实 `Corrosion` Buff 验证首次 `set(2f, 1)` 和再次 `extend(2f)`。
- [ ] 测试普通 0 伤害不触发、正伤害开启新链、同类 0 伤害触发连锁，以及同一条链中每只最多触发一次。
- [ ] 实现 `applyCorrosion(Char)`、爆裂链上下文、`emitCorrosiveBurst()` 和 `damage()` 接线。
- [ ] 用粒子/飞溅和专属 Sprite 方法表现爆裂，重跑测试。

### 任务 3：高塔专属生成与图鉴

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerMobRules.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevelCamouflageGnollTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`

- [ ] 写失败测试，断言任意高塔楼层按 `[0,.2)`、`[.2,.4)`、`[.4,1)` 选择迷彩豺狼、腐蚀蝇群、默认池。
- [ ] 写测试断言两个类不属于正常 `REGIONAL`，且只注册在 `TOWER_MOBS`。
- [ ] 实现枚举式选择规则并让 `TowerLevel.createMob()` 只调用一次 `Random.Float()`。
- [ ] 添加中英文名称、描述和发现提示，重跑测试。

### 任务 4：概念图、像素表与动画

**文件：**
- 创建：`docs/pixel-art/corrosive-swarm/corrosive_swarm_concept.png`
- 创建：`core/src/main/assets/sprites/corrosive_swarm.png`
- 创建：`docs/pixel-art/corrosive-swarm/corrosive_swarm_preview.png`
- 创建：`tools/generate_corrosive_swarm_sprite.py`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/CorrosiveSwarmSprite.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/CorrosiveSwarmSpriteAssetTest.java`

- [ ] 使用 `swarm.png` 作为参考，通过内置生图生成待机、飞行、攻击、分裂、爆裂和死亡概念图。
- [ ] 写失败资源测试，要求 `Assets.Sprites.CORROSIVE_SWARM`、专属 Sprite、272×16、17 个非空帧、硬透明边缘和不超过 12 色。
- [ ] 依据概念图逐帧绘制 17 帧 16×16 PNG，并生成 8 倍最近邻预览。
- [ ] 实现原 15 帧动画映射和 15–16 爆裂动画；爆裂完成后回到待机。
- [ ] 使用外部校验器检查资源。

### 任务 5：完成前验证

- [ ] 运行腐蚀蝇群、分裂、高塔生成、图鉴和资源定向测试。
- [ ] 运行 `python tools/test_validate_tower_monster_sprite.py`。
- [ ] 运行 `python D:\STUDY\Dungeon\tools\validate_tower_monster_sprite.py ... --frame-width 16 --frame-height 16 --expected-frames 17`。
- [ ] 运行 `gradlew.bat --no-problems-report core:compileJava`。
- [ ] 运行完整 `core:test`；若仍被既有 `GothicCastleLevel` 缺失阻塞，记录其与本任务无关的完整错误。
- [ ] 对本次修改文件运行 `git diff --check` 并逐项核对设计规格。
