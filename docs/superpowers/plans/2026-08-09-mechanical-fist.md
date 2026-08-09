# 机械拳头实现计划

> **面向 AI 代理的工作者：** 必须使用 `executing-plans` 在当前会话中逐任务执行本计划；每一步按复选框更新，并严格遵循红—绿—重构。

**目标：** 加入只在高塔生成的机械拳头，以 250 HP、30–70 护甲和每次成功近战命中后的纯三格击退形成高属性近战敌人，并完成科技风 YOG 拳头衍生精灵、图鉴和等概率生成接入。

**架构：** 新怪物直接继承 `Mob`，通过 `attackProc` 在命中结算后调用可覆盖的 `knockBack` 方法；默认实现使用 `Ballistica` 与 `WandOfBlastWave.throwChar`，关闭碰撞伤害。精灵由确定性 Pillow 脚本从 `yog_fists.png` 的锈蚀拳头行提取和改色，独立动画类不依赖 `FistSprite` 的 YOG 类型强制转换。

**技术栈：** Java、Shattered Pixel Dungeon Mob/Ballistica/WandOfBlastWave、JUnit 4、Python Pillow、PNG 像素精灵。

---

## 文件结构

**创建：**

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/MechanicalFist.java`：数值和命中后击退。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/MechanicalFistSprite.java`：24×17 专属动画。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/MechanicalFistTest.java`：数值和击退行为测试。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/MechanicalFistSpriteAssetTest.java`：资源、帧、色板和轮廓测试。
- `tools/generate_mechanical_fist_sprite.py`：确定性换色和细节生成器。
- `tools/test_generate_mechanical_fist_sprite.py`：生成器测试。
- `tools/mechanical_fist_test.init.gradle`：隔离本功能相关测试。
- `core/src/main/assets/sprites/mechanical_fist.png`：24×17 帧图集。
- `docs/pixel-art/mechanical-fist/mechanical_fist_concept.png`：科技风美术概念参考。
- `docs/pixel-art/mechanical-fist/mechanical_fist_preview.png`：最近邻放大预览。

**修改：**

- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`：注册精灵路径。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerMobRules.java`：加入第六个等概率枚举值。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java`：创建机械拳头。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java`：加入高塔生物图鉴。
- `core/src/main/assets/messages/actors/actors.properties`：英文名称、描述和提示。
- `core/src/main/assets/messages/actors/actors_zh.properties`：中文名称、描述和提示。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevelCamouflageGnollTest.java`：验证六等分边界。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/journal/TowerBestiaryCategoriesTest.java`：验证图鉴、经验上限和文本。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/TowerMobPlacerTest.java`：验证测试放置器自动包含新怪物。

## 任务 1：基础数值和击退行为

- [ ] 创建 `MechanicalFistTest`，断言 HP 250、闪避 20、命中 40、经验 13、上限 30、无掉落，循环采样伤害 10–35 与护甲 30–70。
- [ ] 运行 `./gradlew.bat -I tools/mechanical_fist_test.init.gradle --no-daemon --no-problems-report core:test --tests "*MechanicalFistTest"`，确认因 `MechanicalFist` 不存在而失败。
- [ ] 在测试子类覆盖 `knockBack(Char)` 记录目标；断言 `attackProc` 返回原伤害并调用一次击退。
- [ ] 实现最小 `MechanicalFist`：构造块设置数值，`damageRoll`、`attackSkill`、`drRoll` 和 `lootChance` 返回规格值；`attackProc` 先调用父类再执行击退。
- [ ] 默认 `knockBack` 从 `target.pos` 向 `target.pos + 3 * (target.pos - pos)` 创建 `Ballistica.MAGIC_BOLT`，调用 `WandOfBlastWave.throwChar(target, trajectory, 3, false, false, this)`；英雄目标调用 `interrupt()`。
- [ ] 重跑定向测试，确认绿灯。

## 任务 2：科技概念图和像素素材

- [ ] 使用 ImageGen 生成仅作造型参考的机械拳头概念图：YOG 拳头轮廓、深枪铁装甲、黄铜关节、冷青核心、活塞和铆钉，无文字无背景叙事；保存到文档目录。
- [ ] 先创建失败的 Python 测试，要求输出图集为 240×17（10 个 24×17 帧）、透明度只有 0/255、可见色不超过 12、每帧非空，并保留原锈蚀拳头行的主体 alpha 轮廓。
- [ ] 运行 `python tools/test_generate_mechanical_fist_sprite.py`，确认生成器或输出不存在而失败。
- [ ] 用 Pillow 读取 240×102 的 `yog_fists.png`，提取锈蚀拳头第 4 行（帧偏移 30），确定性映射为枪铁灰、黄铜和冷青色板；仅在原不透明区域增加铆钉、装甲缝、活塞与能源高光，透明像素 RGB 归零。
- [ ] 生成 `mechanical_fist.png` 和 8×最近邻预览，重跑 Python 测试并目视检查。
- [ ] 创建失败的 `MechanicalFistSpriteAssetTest`，断言资源常量、专属精灵绑定、帧尺寸、硬边透明度、有限色板和关键动画字段。
- [ ] 在 `Assets.Sprites` 添加 `MECHANICAL_FIST`，实现独立 `MechanicalFistSprite`：待机 0/1、移动 0/1、攻击 0/5/6/0、死亡 0/2/3/4，并在攻击时使用原拳头跃起和震屏反馈。
- [ ] 运行 Java 素材测试，确认绿灯。

## 任务 3：六等分生成、图鉴和本地化

- [ ] 先修改现有集成测试：六段边界分别为 `[0,1/6)` 至 `[5/6,1)`，枚举数为 6；图鉴实体末尾加入 `MechanicalFist`，并断言不在普通区域、经验上限 30、测试放置器包含新类、中英文文本存在。
- [ ] 运行定向集成测试，确认因生产接线缺失而失败。
- [ ] 在 `TowerMobRules.Selection` 添加 `MECHANICAL_FIST`，在 `TowerLevel.createMob()` 返回新实例，在 `Bestiary.TOWER_MOBS` 末尾追加新类。
- [ ] 添加 `actors.mobs.tmobs.mechanicalfist.name/desc/discover_hint` 的中英文消息；描述包含用户提供的背景和三格击退信息。
- [ ] 重跑集成测试，确认绿灯。

## 任务 4：完整验证和复核

- [ ] 运行 `python tools/test_generate_mechanical_fist_sprite.py`。
- [ ] 运行 `./gradlew.bat -I tools/mechanical_fist_test.init.gradle --no-daemon --no-problems-report --rerun-tasks core:test --tests "*MechanicalFist*" --tests "*TowerLevelCamouflageGnollTest" --tests "*TowerBestiaryCategoriesTest" --tests "*TowerMobPlacerTest"`。
- [ ] 运行 `./gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:compileJava`，记录生产编译结果。
- [ ] 运行 `./gradlew.bat --no-daemon --no-problems-report --rerun-tasks core:test`，把本功能结果与工作区既有失败分开记录。
- [ ] 运行 `git diff --check` 并逐个检查本功能文件差异，确认未覆盖用户已有改动。
- [ ] 使用 `requesting-code-review` 复核需求、击退边界、资源引用和测试证据；修复所有重要问题后再交付。
