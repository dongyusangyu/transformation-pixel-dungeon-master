# 女猎手？AI 控制状态与火焰吸收实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 让女猎手？遵守基础怪物控制状态和既定战术优先级，并将燃烧以 50% 概率转化为 15 回合火焰之力。

**架构：** 在 `HuntressBoss` 内增加可单测的回合优先级与燃烧转化判定，控制状态优先转交 `Mob.act()`。删除抢占战术的主动寻花分支，保留楼层已有的踩植物触发机制；复用现有 `FireImbue` 攻击附火管线。

**技术栈：** Java、JUnit 4、Gradle、现有 `Mob`/`Buff`/`FireImbue` 系统。

---

## 文件结构

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
  - 控制状态优先级、战术缓存清理、燃烧吸收与火焰之力。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`
  - 控制、战术优先级和燃烧吸收回归测试。

### 任务 1：控制状态和战术优先级

- [x] **步骤 1：编写失败测试**

为 `HuntressBossTest` 增加测试，断言 `paralysed > 0`、睡眠、恐惧/惧退和混乱时自定义行动不可执行；断言控制接管会清理 `TacticalDecision`，但不会要求清除贯风箭蓄力。

- [x] **步骤 2：验证测试正确失败**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report :core:test --tests "*HuntressBossTest" --rerun-tasks
```

预期：新增控制优先级断言失败，已有测试继续通过。

- [x] **步骤 3：实现最少修复**

在 `HuntressBoss.act()` 的自定义行动之前加入统一优先级判断；控制状态接管时清理战术缓存并调用 `super.act()`。删除 `plantSeekCooldown` 字段、存档键和主动寻花行动块，保留 `HuntressBossLevel.occupyCell` 的踩植物增益。

- [x] **步骤 4：验证测试通过**

运行同一步骤 2 的命令，预期全部 `HuntressBossTest` 通过。

### 任务 2：燃烧吸收与火焰之力

- [x] **步骤 1：编写失败测试**

增加纯判定测试，覆盖随机值 0 时转化、随机值 1 时不转化，以及持续时间为 `FireImbue.DURATION * 0.3f`。增加真实 Buff 测试，验证处理后 `Burning` 不存在，成功分支存在 `FireImbue`。

- [x] **步骤 2：验证测试正确失败**

运行：

```powershell
.\gradlew.bat --no-daemon --no-problems-report :core:test --tests "*HuntressBossTest" --rerun-tasks
```

预期：因燃烧吸收接口尚不存在而编译失败或新增断言失败。

- [x] **步骤 3：实现最少代码**

在 `HuntressBoss.add(Buff)` 中拦截成功附加的 `Burning`，立即移除，再以 `Random.Int(2)` 判定；这样即使女猎手正处于冻结、麻痹或魔法睡眠，燃烧也不会先结算一跳。成功时调用：

```java
Buff.affect(this, FireImbue.class).set(FireImbue.DURATION * 0.3f);
```

`FireImbue` 已由 `Char.attack` 自动对近战、普通箭矢和贯风箭命中的目标执行火焰附加效果，无需复制攻击代码。

- [x] **步骤 4：验证测试通过**

运行同一步骤 2 的命令，预期全部通过。

### 任务 3：回归与构建

- [x] **步骤 1：运行女猎手相关测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report :core:test --tests "*HuntressBoss*" --rerun-tasks
```

预期：全部通过。

- [x] **步骤 2：运行 Android Java 编译**

```powershell
.\gradlew.bat --no-daemon --no-problems-report :android:compileDebugJavaWithJavac --rerun-tasks
```

预期：`BUILD SUCCESSFUL`。

- [x] **步骤 3：运行 core 回归测试**

```powershell
.\gradlew.bat --no-daemon --no-problems-report :core:test --rerun-tasks
```

实际：301 项中 3 项失败；女猎手相关 33 项全部通过。失败项为此前两个 `TreasureJournalViewTest`，以及单独复跑仍失败的 `SurfaceTownLevelDongyusangyuTest`，均不在本次 Boss 代码路径。

- [x] **步骤 4：检查差异**

```powershell
git diff --check -- core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java
```

预期：无空白错误。
