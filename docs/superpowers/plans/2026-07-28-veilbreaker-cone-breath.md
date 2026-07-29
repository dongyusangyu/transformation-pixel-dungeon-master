# 破幕邪眼扇形喷射实现计划

> **面向 AI 代理的工作者：** 必须使用 `executing-plans` 逐任务实现本计划，并在完成后运行相关测试。

**目标：** 将破幕邪眼蓄力后的单线激光替换为射程不变的 60° 扇形范围伤害，并复用腐化元素风暴的粒子效果。

**架构：** `VeilbreakerEye` 覆盖邪眼的蓄力发射阶段；纯几何辅助方法计算以朝向为中心、无遮挡的扇形格；每个受影响角色沿用邪眼的原版伤害公式。视觉效果仅在每个命中格播放现有元素风暴粒子，不改变伤害结算。

**技术栈：** Java、Shattered Pixel Dungeon 的 `Eye` 状态机与 `Ballistica` 视线逻辑、既有元素风暴粒子、JUnit 4。

---

### 任务 1：扇形目标选区与伤害结算

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/mobs/VeilbreakerEye.java`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/mobs/ExtractionRaidMobsTest.java`

- [ ] **步骤 1：编写失败的扇形选区测试**

```java
@Test
public void veilbreakerConeCoversSixtyDegreesAndExcludesSideCells() throws Exception {
    Method cells = type("VeilbreakerEye").getMethod(
            "coneContains", float.class, float.class, float.class, float.class,
            float.class, float.class, float.class);
    assertTrue((Boolean) cells.invoke(null, 0f, 0f, 5f, 0f, 3f, 0f, 5f));
    assertTrue((Boolean) cells.invoke(null, 0f, 0f, 5f, 0f, 3f, 1f, 5f));
    assertFalse((Boolean) cells.invoke(null, 0f, 0f, 5f, 0f, 1f, 3f, 5f));
}
```

- [ ] **步骤 2：运行测试并确认失败**

运行：

```powershell
.\gradlew.bat --no-problems-report :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ExtractionRaidMobsTest.veilbreakerConeCoversSixtyDegreesAndExcludesSideCells
```

预期：失败，因为 `coneContains` 尚未定义。

- [ ] **步骤 3：实现最小化扇形选区和发射覆盖**

```java
public static boolean coneContains(float ox, float oy, float ax, float ay,
        float tx, float ty, float range) {
    float distance = (float) Math.hypot(tx - ox, ty - oy);
    return distance > 0 && distance <= range
            && dotToAim / (aimDistance * distance) >= COS_HALF_CONE_ANGLE;
}
```

在邪眼 `CHARGING` 的发射分支中遍历范围内可见格，只伤害扇形内的角色；每次伤害后播放元素风暴粒子。

- [ ] **步骤 4：运行测试并确认通过**

运行步骤 2 的命令。

预期：通过。

### 任务 2：更新说明并验证搜打撤怪物回归

**文件：**

- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/mobs/ExtractionRaidMobsTest.java`

- [ ] **步骤 1：更新破幕邪眼说明**

```properties
levels.minigame.extraction.mobs.veilbreakereye.desc=... After charging, it sweeps a 60-degree cone ...
```

中文说明同步明确“蓄力后以 60° 扇形喷射伤害”。

- [ ] **步骤 2：重跑完整相关测试组**

```powershell
.\gradlew.bat --no-problems-report --rerun-tasks :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevelTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ExtractionRaidMobsTest --tests com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidRunTest
```

预期：`BUILD SUCCESSFUL`。
