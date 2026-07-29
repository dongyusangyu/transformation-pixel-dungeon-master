# 15 层“女猎手？”Boss 与竞技场实现计划

> **面向 AI 代理的工作者：** 在当前工作区原地执行本计划。每项功能遵循测试驱动开发，先运行失败测试，再添加最小实现并重新验证。

**目标：** 在 15 层加入与 DM-300 确定性二选一的“女猎手？”Boss 楼层，实现狙击手、守望者、植物增益、双飞鹰干扰、临时美术和安全的跨门槛开战流程。

**架构：** 新增独立的 `HuntressBossLevel`、`HuntressBoss` 与 `HuntressBossSprite`。楼层负责竞技场、入场状态、植被和随从生成；Boss 负责攻击、阶段、增益和奖励。通用攻击流程增加一个受保护的护甲修正钩子，使狙击攻击保留原有命中、防御触发、黏滞和护盾流程，只将物理护甲值改为零。

**技术栈：** Java 8、Shattered Pixel Dungeon Actor/Level/Sprite API、JUnit 4、Gradle。

---

### 任务 1：建立可测试的 Boss 规则

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/HuntressBossSelectionTest.java`

- [ ] **步骤 1：编写失败测试**

```java
@Test
public void sniperIgnoresArmorButWardenDoesNot() {
    HuntressBoss boss = new HuntressBoss();
    assertEquals(0, boss.armorForCurrentShot(20));
    boss.enterWardenPhaseForTest();
    assertEquals(20, boss.armorForCurrentShot(20));
}

@Test
public void distractingHawkOnlyDealsOneDamage() {
    assertEquals(1, new HuntressBoss.DistractingHawk().damageRoll());
}
```

- [ ] **步骤 2：运行测试并确认因新类型尚不存在而失败**

运行：`.\gradlew.bat --no-problems-report core:test --tests "*HuntressBoss*"`

预期：`compileTestJava` 失败，报告 `HuntressBoss`、`HuntressBossLevel` 或选择函数不存在。

### 任务 2：实现 Boss、护甲钩子、飞鹰和临时精灵

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/HuntressBossSprite.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/Level.java`

- [ ] **步骤 1：给 `Char.attack` 增加护甲修正钩子**

```java
protected int modifyEnemyArmor(Char enemy, int armor) {
    return armor;
}
```

在读取防御方 `drRoll()` 后调用该钩子；`HuntressBoss` 在狙击普通箭和贯风箭期间返回 `0`，守望者普通箭返回原护甲。

- [ ] **步骤 2：实现阶段和射击**

```java
public enum Phase { SNIPER, WARDEN }

protected boolean canAttack(Char enemy) {
    Ballistica shot = new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE);
    return shot.collisionPos == enemy.pos;
}
```

普通箭使用 `SpiritBow.SpiritArrow`；每三次普通射击准备一次贯风箭，瞄准一回合后沿固定路径发射 `Dart`。贯风箭造成 20–28 点无视护甲伤害并施加 2 回合残废。

- [ ] **步骤 3：实现守望者效果**

守望者普通箭造成 12–20 点伤害且保留护甲；命中后有 33% 概率施加致盲草、火焰花、冰冠花、断肠苔或风暴藤的负面效果。每 10 个自身行动获得一个非腐莓种子的守望者增益，新增益替换旧增益。

- [ ] **步骤 4：实现近身击退**

女猎手命中相邻的玩家或玩家盟友时，有 33% 概率沿远离女猎手的方向将目标击退最多 3 格；墙体、角色和不可移动状态会自然缩短或阻止位移，击退本身不追加碰撞伤害。

- [ ] **步骤 4：实现双飞鹰**

`DistractingHawk` 使用 `SpiritHawk.HawkSprite`，敌对、飞行、固定 1 点伤害、无经验、无掉落，并带 `BOSS_MINION`；每次命中与普通飞鹰一样施加失明和残废。开战事件只生成第一只；半血进入守望者阶段时只额外生成一只，无论第一只是否存活都不按当前数量补足；Boss 死亡时销毁全部此类飞鹰。

- [ ] **步骤 5：实现临时精灵**

独立 `HuntressBossSprite` 使用 `Assets.Sprites.HUNTRESS` 和英雄女猎手默认帧序列。隐身时透明度设为约 `0.35`。灵能箭和普通飞镖沿用 `MissileSprite` 中已经为零的旋转速度。

- [ ] **步骤 6：运行 Boss 测试**

运行：`.\gradlew.bat --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBossTest`

预期：全部通过。

### 任务 3：实现竞技场、植被和入场触发

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`

- [ ] **步骤 1：实现固定竞技场**

构建南侧安全缓冲区、单一竞技场门槛、洞穴竞技场、北侧出口和硬掩体。安全区不生成植被；竞技场约 10% 合法空地初始化为 `FURROWED_GRASS`。

- [ ] **步骤 2：实现状态机**

```java
public enum State { START, INTRO, FIGHT, WON }
```

英雄首次跨过门槛时执行幂等 `startFight()`：中断移动、封门、迁移门口物品和角色、安置友军、生成 Boss 与第一只飞鹰、显示血条、切换音乐，并给予 Boss 一个初始行动延迟。传送越界同样通过 `occupyCell` 触发。

- [ ] **步骤 3：实现守望者植被**

从合法空地中确定性选择约 50% 设为 `FURROWED_GRASS`、约 5% 栽种非腐莓植物、约 1% 生成无掉落 `RotLasher` 变体。Boss 踩到植物时在植物通常效果触发前消费它并获得对应守望者增益。

- [ ] **步骤 4：实现存档与胜利**

持久化 `State`；`FIGHT` 读档重新封锁、绑定血条和音乐，不重播演出。胜利清除飞鹰和触手、开放出入口并保持剩余植被。

- [ ] **步骤 5：运行楼层测试**

运行：`.\gradlew.bat --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevelTest`

预期：全部通过。

### 任务 4：接入 15 层、奖励、升华和文案

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改：`core/src/main/assets/messages/levels/levels.properties`
- 修改：`core/src/main/assets/messages/levels/levels_zh.properties`

- [ ] **步骤 1：实现确定性二选一**

深度 15 使用独立种子偏移计算一次 50% 选择，不污染全局随机数栈；相同游戏种子始终选择同一 Boss 楼层。

- [ ] **步骤 2：实现奖励**

Boss 死亡时掉落 `BowFragment`、与 DM-300 等量的 `MetalShard`，并在 `Statistics.subLimation[2]` 尚未领取时掉落 `type("DM300")` 的升华卷轴。保留 Boss 徽章、计分和信标升级。

- [ ] **步骤 3：添加中英文文案**

覆盖名称、描述、入场、瞄准、贯风箭、阶段变化、植物增益、击杀、失败和楼层提示。

- [ ] **步骤 4：运行选择与 Boss 测试**

运行：`.\gradlew.bat --no-problems-report core:test --tests "*HuntressBoss*"`

预期：全部通过。

### 任务 5：完整验证

**文件：**
- 检查：所有本计划修改文件

- [ ] **步骤 1：运行定向测试**

运行：`.\gradlew.bat --no-problems-report core:test --tests "*HuntressBoss*"`

- [ ] **步骤 2：运行完整核心测试**

运行：`.\gradlew.bat --no-problems-report core:test`

- [ ] **步骤 3：运行核心编译**

运行：`.\gradlew.bat --no-problems-report core:compileJava`

- [ ] **步骤 4：检查差异**

运行：`git diff --check`，并逐项核对 Boss 阶段、双飞鹰、植被比例、存档、奖励、升华和贴图动画要求。
