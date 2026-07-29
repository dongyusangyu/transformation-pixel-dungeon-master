# 迟创魔蝎六连击实现计划

> **面向 AI 代理的工作者：** 使用 `executing-plans` 逐项实现。

**目标：** 将迟创魔蝎的延迟伤害攻击替换为每次 2–8 点、攻击间隔为原版六分之一的常规攻击。

**架构：** `DeferredScorpio` 公开战斗常量，覆写伤害掷骰和攻击延迟。保留父类的一次攻击流程，并将每次攻击后的时间消耗缩短至六分之一。

**技术栈：** Java、现有 Gradle/JUnit 4 测试。

---

### 任务 1：锁定战斗参数并验证

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/mobs/ExtractionRaidMobsTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/minigame/extraction/mobs/DeferredScorpio.java`

- [ ] **步骤 1：编写失败测试**

```java
DeferredScorpio scorpio = new DeferredScorpio();
assertEquals(new Scorpio().attackDelay() / 6f, scorpio.attackDelay(), 0.0001f);
for (int i = 0; i < 128; i++) {
    assertTrue(scorpio.damageRoll() >= 2 && scorpio.damageRoll() <= 8);
}
```

- [ ] **步骤 2：运行测试，确认失败**

运行：`./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ExtractionRaidMobsTest`

预期：失败，因尚未定义六连击常量或伤害范围不符。

- [ ] **步骤 3：实现最小战斗覆盖**

```java
@Override
public int damageRoll() {
    return Random.NormalIntRange(2, 8);
}

@Override
public float attackDelay() {
    return super.attackDelay() / 6f;
}
```

- [ ] **步骤 4：运行测试与生产编译**

运行：`./gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ExtractionRaidMobsTest`，随后运行 `./gradlew.bat :core:compileJava`。

预期：相关测试与生产编译成功；若其余既有测试暴露无关失败，记录失败来源。
