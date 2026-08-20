# 死亡骑士掩体消耗战增强实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将死亡骑士的高塔 Boss 战改造成渐进拆场的掩体消耗战：实体掩体全部可摧毁，轰炸结算后优先拆除英雄附近的相关掩体，分层处刑场无视场内掩体但仍受外围永久墙限制。

**架构：** `TowerBossLayout`/`TowerBossLevel` 负责生成、识别和安全摧毁高塔 Boss 层掩体；`DeathKnightBombardment` 只提供确定性的处刑穿透和候选掩体筛选；`DeathKnight` 在一次轰炸的伤害与状态结算完成后调用地图破坏接口并触发表现。已毁地形直接持久化为 `Terrain.EMBERS`，不新增第二份地图状态。

**技术栈：** Java、Gradle/JUnit 4、Shattered Pixel Dungeon `Level`/`Terrain`/`Bundle`、`CellEmitter`/`BlastParticle`、现有 `TargetedCell` 与 `DeathKnightSlash` 特效。

---

## 文件清单与职责

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLayout.java` —— 将 28 个实体障碍统一生成成 `BARRICADE`，集中定义可摧毁地形和固定保护区域。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java` —— 向 Boss 与测试提供竞技场掩体识别、安全摧毁、地图刷新接口，并保护动态净化触发器。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardment.java` —— 扩展网格掩体/竞技场掩码，新增无视内部掩体的处刑判定与近英雄候选筛选。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnight.java` —— 保存锁定目标格，在轰炸结算后只拆一次掩体，执行近英雄优先随机、首次喊话、碎裂表现和存档恢复。
- 修改：`core/src/main/assets/messages/actors/actors.properties` —— 增加英文首次拆场提示，并在死亡骑士描述中说明轰炸会拆除附近掩体、处刑场无视场内掩体。
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties` —— 增加中文首次拆场提示，并在中文死亡骑士描述中说明轰炸会拆除附近掩体、处刑场无视场内掩体。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java` —— 覆盖生成掩体类型、固定地形保护和掩体识别边界。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardmentTest.java` —— 覆盖处刑穿透墙体、竞技场边界和近英雄候选筛选。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightTest.java` —— 覆盖结算时序、一次性拆场、取消/转阶段不拆、目标格存档和首次提示持久化。
- 创建：`tools/death_knight_cover_test.init.gradle` —— 为目标测试提供隔离 build 输出，避免共享 `build/reports/problems` 和并行测试覆盖 JUnit 报告。

### 任务 1：先锁定高塔 Boss 层掩体契约

**文件：**
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java`
- 修改实现：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLayout.java`

- [ ] **步骤 1：编写失败测试，固定生成与保护规则**

在 `TowerBossLevelTest` 中替换旧的“允许 `STATUE`/`REGION_DECO`”断言，并新增以下测试。测试使用现有 `generateMap(seed)` 辅助函数：

```java
@Test
public void generatedSolidObstaclesAreFlammableBarricades() {
    int[] map = generateMap(0x5B055L);
    int barricades = 0;
    for (int cell = 0; cell < map.length; cell++) {
        if (map[cell] == Terrain.STATUE || map[cell] == Terrain.REGION_DECO) {
            fail("solid boss obstacle must use BARRICADE");
        }
        if (map[cell] == Terrain.BARRICADE) {
            barricades++;
            assertTrue((Terrain.flags[map[cell]] & Terrain.SOLID) != 0);
            assertTrue((Terrain.flags[map[cell]] & Terrain.LOS_BLOCKING) != 0);
            assertTrue((Terrain.flags[map[cell]] & Terrain.FLAMABLE) != 0);
            assertTrue(TowerBossLayout.isArenaCell(cell));
        }
    }
    assertEquals(28, barricades);
}

@Test
public void destructibleTerrainIncludesSoftCoverButNotWaterOrDoors() {
    assertTrue(TowerBossLayout.isDestructibleTerrain(Terrain.BARRICADE));
    assertTrue(TowerBossLayout.isDestructibleTerrain(Terrain.HIGH_GRASS));
    assertTrue(TowerBossLayout.isDestructibleTerrain(Terrain.FURROWED_GRASS));
    assertFalse(TowerBossLayout.isDestructibleTerrain(Terrain.WATER));
    assertFalse(TowerBossLayout.isDestructibleTerrain(Terrain.DOOR));
    assertFalse(TowerBossLayout.isDestructibleTerrain(Terrain.UNLOCKED_EXIT));
}

@Test
public void fixedRoutesAndGatesAreProtectedFromCoverDestruction() {
    assertTrue(TowerBossLayout.isProtectedCell(TowerBossLayout.ENTRANCE));
    assertTrue(TowerBossLayout.isProtectedCell(TowerBossLayout.EXIT));
    assertTrue(TowerBossLayout.isProtectedCell(TowerBossLayout.SAFE_GATE));
    assertTrue(TowerBossLayout.isProtectedCell(TowerBossLayout.EXIT_GATE));
    assertFalse(TowerBossLayout.isProtectedCell(cell(10, 10)));
}
```

在测试 imports 中加入 `fail`。三个标志断言必须使用 `!= 0` 转成布尔表达式；不要放宽为只检查地形 ID。

- [ ] **步骤 2：运行目标测试确认 RED**

运行：

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevelTest" --max-workers=1 --no-daemon --console=plain
```

预期：编译通过但新增测试失败：当前生成器仍会产生 `STATUE`/`REGION_DECO`，且 `isDestructibleTerrain`/`isProtectedCell` 尚不存在。

- [ ] **步骤 3：实现最小布局契约**

在 `TowerBossLayout` 中新增包可见方法，避免让 `DeathKnight` 直接复制地形判断：

```java
static boolean isDestructibleTerrain(int terrain) {
    return terrain == Terrain.BARRICADE
            || terrain == Terrain.HIGH_GRASS
            || terrain == Terrain.FURROWED_GRASS;
}

static boolean isProtectedCell(int cell) {
    return !isArenaCell(cell) || isReservedRoute(cell % WIDTH, cell / WIDTH);
}
```

将 `paintNaturalTerrain()` 中的障碍选择改为直接写入 `Terrain.BARRICADE`，保留 `OBSTACLE_COUNT == 28`、种子随机位置和 `isReservedRoute` 过滤。不要改水域、植被密度或安全路线生成。

- [ ] **步骤 4：运行测试确认 GREEN**

重复步骤 2 的命令，预期 `TowerBossLevelTest` 全部通过，旧的连通性、入口出口和自然地形断言仍通过。

- [ ] **步骤 5：提交独立变更**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLayout.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java
git commit -m "feat: make tower boss cover destructible"
```

### 任务 2：为轰炸几何增加掩体候选与竞技场掩码

**文件：**
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardmentTest.java`
- 修改实现：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardment.java`

- [ ] **步骤 1：编写失败测试，先定义纯函数接口**

给 `DeathKnightBombardmentTest` 增加一个带掩体数组的网格构造辅助函数，并写以下测试。新 API 的结果是“距离目标最近的全部并列候选”，不在纯几何层消耗随机数：

```java
@Test
public void nearbyCoverCandidatesPreferNearestChebyshevDistance() {
    DeathKnightBombardment.Grid grid = coverGrid();
    int[] blast = {cell(7, 7)};
    grid.destructibleCover[cell(6, 7)] = true; // distance 1
    grid.destructibleCover[cell(8, 7)] = true; // distance 1, tie
    grid.destructibleCover[cell(10, 7)] = true; // distance 3, excluded

    assertArrayEquals(new int[]{cell(6, 7), cell(8, 7)},
            DeathKnightBombardment.nearbyCoverCandidates(
                    grid, blast, cell(7, 7)));
}

@Test
public void nearbyCoverCandidatesIncludeBlastNeighborsAndSoftCover() {
    DeathKnightBombardment.Grid grid = coverGrid();
    grid.destructibleCover[cell(9, 9)] = true;
    assertArrayEquals(new int[]{cell(9, 9)},
            DeathKnightBombardment.nearbyCoverCandidates(
                    grid, new int[]{cell(8, 8)}, cell(12, 12)));
}

@Test
public void nearbyCoverCandidatesReturnEmptyWhenNoCoverIsNearby() {
    DeathKnightBombardment.Grid grid = coverGrid();
    grid.destructibleCover[cell(1, 1)] = true;
    assertEquals(0, DeathKnightBombardment.nearbyCoverCandidates(
            grid, new int[]{cell(7, 7)}, cell(7, 7)).length);
}

@Test
public void executionIgnoresInternalSolidCoverButRespectsArenaMask() {
    DeathKnightBombardment.Grid grid = coverGrid();
    grid.solid[cell(7, 5)] = true;
    grid.passable[cell(7, 5)] = false;
    DeathKnightBombardment.Plan execution = DeathKnightBombardment.execution(
            grid, cell(7, 1), cell(7, 7));

    assertTrue(execution.contains(cell(7, 6))); // behind internal cover
    grid.arena[cell(7, 6)] = false;
    assertFalse(DeathKnightBombardment.execution(
            grid, cell(7, 1), cell(7, 7)).contains(cell(7, 6)));
}
```

在 `Grid` 中保留现有三数组构造器以兼容已有测试，并新增完整构造器：

```java
public Grid(int width, int height, boolean[] passable, boolean[] solid,
            boolean[] occupied, boolean[] arena, boolean[] destructibleCover)
```

旧构造器将 `arena` 默认填充为 `true`，将 `destructibleCover` 默认填充为 `false`。`coverGrid()` 使用全 `true` 的 `arena`、全 `false` 的 `destructibleCover`，以免改变既有几何测试。

在测试类中保留现有 `openGrid()`，并新增明确的 `coverGrid()`：

```java
private static DeathKnightBombardment.Grid coverGrid() {
    boolean[] passable = new boolean[WIDTH * WIDTH];
    boolean[] arena = new boolean[WIDTH * WIDTH];
    Arrays.fill(passable, true);
    Arrays.fill(arena, true);
    return new DeathKnightBombardment.Grid(
            WIDTH, WIDTH, passable,
            new boolean[WIDTH * WIDTH],
            new boolean[WIDTH * WIDTH],
            arena,
            new boolean[WIDTH * WIDTH]);
}
```

- [ ] **步骤 2：运行测试确认 RED**

运行：

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnightBombardmentTest" --max-workers=1 --no-daemon --console=plain
```

预期：新增测试因 `Grid.destructibleCover`、`Grid.arena` 和 `nearbyCoverCandidates` 缺失而编译失败，属于预期 RED。

- [ ] **步骤 3：实现候选筛选与掩体穿透**

在 `Grid` 中保存并校验两组新数组；新增：

```java
public static int[] nearbyCoverCandidates(Grid grid, int[] blastCells, int targetCell)
```

实现要求：

- 遍历每个冻结轰炸格及 `dx/dy ∈ [-1, 1]` 的相邻格，使用 `LinkedHashSet` 去重。
- 只保留 `grid.valid(cell) && grid.destructibleCover[cell]` 的格子；不使用 `passable`/`solid` 排除实体 `BARRICADE`。
- 使用 `Math.max(Math.abs(x-targetX), Math.abs(y-targetY))` 计算 Chebyshev 距离。
- 只返回最小距离的一组，并按扫描顺序输出，随机选择留给 `DeathKnight`。
- 空输入、越界目标或无候选返回长度为 0，不抛异常。

修改 `execution()`：保留 `!grid.passable[cell] || grid.solid[cell]` 和 `!grid.arena[cell]` 过滤，但删除 `visible(grid, origin, cell)` 条件。这样处刑场会穿过内部掩体，仍不会包括实体墙、场外地格或不可站立格；普通 `line/cone/cross/ring` 继续使用 `visible()`。

- [ ] **步骤 4：运行测试确认 GREEN**

重复步骤 2 的命令，预期现有全部几何测试和新增候选/处刑穿透测试通过。

- [ ] **步骤 5：提交独立变更**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardment.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardmentTest.java
git commit -m "feat: add death knight cover selection geometry"
```

### 任务 3：在 Boss 层集中提供掩体识别与安全摧毁

**文件：**
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java`
- 修改实现：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java`

- [ ] **步骤 1：编写失败测试，覆盖动态机关保护与地图刷新出口**

新增契约测试，直接通过 `TowerBossLayout` 验证纯规则；同一测试类的集成夹具再用真实生成地图调用 `TowerBossLevel.isDestructibleBossCover(int)` 和 `destroyBossCover(int)`，不在测试中复制地形判断：

```java
@Test
public void bossCoverPredicateAcceptsArenaCoverAndRejectsFixedCells() {
    int[] map = generateMap(0x5B055L);
    int barricade = -1;
    for (int cell = 0; cell < map.length; cell++) {
        if (map[cell] == Terrain.BARRICADE) {
            barricade = cell;
            break;
        }
    }
    assertTrue(barricade >= 0);
    assertTrue(TowerBossLayout.isArenaCell(barricade));
    assertTrue(TowerBossLayout.isDestructibleTerrain(map[barricade]));
    assertTrue(TowerBossLayout.isProtectedCell(TowerBossLayout.EXIT));
    assertTrue(TowerBossLayout.isProtectedCell(TowerBossLayout.ENTRANCE));
}

@Test
public void coverDestroyContractRejectsProtectedAndNonFlammableTerrain() {
    assertFalse(TowerBossLayout.isDestructibleTerrain(Terrain.WALL));
    assertFalse(TowerBossLayout.isDestructibleTerrain(Terrain.DOOR));
    assertTrue(TowerBossLayout.isProtectedCell(TowerBossLayout.EXIT_GATE));
}
```

集成夹具使用以下测试子类装载同一张生成地图，然后调用生产的 `isDestructibleBossCover(int)` 与 `destroyBossCover(int)`；子类只初始化 `Level` 数组，不复制掩体判定：

```java
private static final class TestTowerBossLevel extends TowerBossLevel {
    void loadGeneratedMap(int[] generated) {
        setSize(TowerBossLayout.WIDTH, TowerBossLayout.HEIGHT);
        map = generated.clone();
        for (int cell = 0; cell < map.length; cell++) {
            int flags = Terrain.flags[map[cell]];
            passable[cell] = (flags & Terrain.PASSABLE) != 0;
            losBlocking[cell] = (flags & Terrain.LOS_BLOCKING) != 0;
            flamable[cell] = (flags & Terrain.FLAMABLE) != 0;
            solid[cell] = (flags & Terrain.SOLID) != 0;
        }
    }
}
```

使用该夹具补充地图刷新断言：选取生成的 `BARRICADE`，设置 `Dungeon.level = level`，断言 `isDestructibleBossCover(cell)` 为真、`destroyBossCover(cell)` 返回真且 `level.map[cell] == Terrain.EMBERS`；随后再次调用返回假。测试结束在 `finally` 中恢复原 `Dungeon.level`。

- [ ] **步骤 2：运行 `TowerBossLevelTest` 确认 RED**

预期：编译失败，因为 `TowerBossLevel` 尚未提供 Boss 掩体接口。

- [ ] **步骤 3：实现 Boss 层公共接口**

在 `TowerBossLevel` 增加以下生产方法：

```java
public boolean isBossArenaCell(int cell) {
    return TowerBossLayout.isArenaCell(cell);
}

public boolean isDestructibleBossCover(int cell) {
    return cell >= 0 && cell < length()
            && TowerBossLayout.isArenaCell(cell)
            && !TowerBossLayout.isProtectedCell(cell)
            && TowerBossLayout.isDestructibleTerrain(map[cell])
            && (pestilenceArena == null
                || pestilenceArena.purifierCell() != cell);
}

public boolean destroyBossCover(int cell) {
    if (!isDestructibleBossCover(cell)) return false;
    destroy(cell);
    GameScene.updateMap(cell);
    Dungeon.observe();
    return map[cell] == Terrain.EMBERS;
}
```

`destroyBossCover()` 只接受当前地图仍为可摧毁地形的格子，因此玩家火焰、其他技能或重复回调先一步烧毁掩体时会安全返回 `false`。不要调用 `Dungeon.level.drop()` 或任何物品生成逻辑；`Level.destroy()` 对 `BARRICADE`/高草只产生余烬。

为测试提供包可见的无场景桥接，或在已有测试夹具中构造地图数组后调用同一个静态判定；桥接不得改变生产结果。动态净化触发器通过 `pestilenceArena.purifierCell()` 排除，空控制器保持兼容死亡骑士场景。

- [ ] **步骤 4：运行测试确认 GREEN**

运行 `TowerBossLevelTest`，预期固定地图、自然地形连通性、净化器相关既有测试和新增掩体契约全部通过。

- [ ] **步骤 5：提交独立变更**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevelTest.java
git commit -m "feat: expose safe tower boss cover destruction"
```

### 任务 4：把拆场接入 Death Knight 的一次性结算出口

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnight.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightTest.java`

- [ ] **步骤 1：编写失败测试，验证时序和调用次数**

扩展现有 `TestDeathKnight` 测试替身：让它覆盖 `breakNearbyCoverAfterResolution(int fallbackTargetCell)` 并记录调用次数、目标格和调用时的 `RecordingBombardmentTarget.lastDamage`。新增：

```java
@Test
public void coverBreakRunsAfterAllDamageAndOnlyOncePerResolution() {
    TestDeathKnight boss = new TestDeathKnight();
    RecordingBombardmentTarget target = new RecordingBombardmentTarget();
    boss.targetForTest = target;
    boss.setPendingForTest(DeathKnight.Skill.LINE, new int[]{10}, 1);

    assertTrue(boss.advancePendingForTest());
    assertEquals(70, target.lastDamage);
    assertEquals(1, boss.coverBreakCalls);
    assertEquals(DeathKnight.Skill.NONE, boss.pendingSkillForTest());
}

@Test
public void cancelledOrPausedBombardmentDoesNotBreakCover() {
    TestDeathKnight boss = new TestDeathKnight();
    boss.forcePhaseForTest(DeathKnight.Phase.BREAK_FORMATION, 1);
    boss.setPendingForTest(DeathKnight.Skill.EXECUTION, new int[]{10}, 2);
    boss.armTransitionForTest();
    assertTrue(boss.advanceTransitionForTest());
    assertEquals(0, boss.coverBreakCalls);
}

@Test
public void allResolvedSkillKindsUseTheSameCoverBreakHook() {
    for (DeathKnight.Skill skill : new DeathKnight.Skill[]{
            DeathKnight.Skill.LINE, DeathKnight.Skill.CONE,
            DeathKnight.Skill.CROSS, DeathKnight.Skill.RING,
            DeathKnight.Skill.SOUL_LINE, DeathKnight.Skill.EXECUTION}) {
        TestDeathKnight boss = new TestDeathKnight();
        boss.setPendingForTest(skill, new int[]{10},
                skill == DeathKnight.Skill.EXECUTION ? 2 : 1);
        boss.advancePendingForTest();
        if (skill == DeathKnight.Skill.EXECUTION) boss.advancePendingForTest();
        assertEquals(skill.name(), 1, boss.coverBreakCalls);
    }
}
```

测试替身必须让 `resolvePendingSkill()` 经过现有 `damageWithBombardment()`，只覆写拆场 hook；禁止在测试中复制 `resolvePendingSkill()` 的流程。

- [ ] **步骤 2：运行 `DeathKnightTest` 确认 RED**

预期：新增测试因缺少 `breakNearbyCoverAfterResolution` 和统一结算调用而编译失败，或现有实现无法记录调用。

- [ ] **步骤 3：保存锁定目标格并构造完整掩体网格**

在 `DeathKnight` 新增字段和 Bundle 键：

```java
private static final String PENDING_TARGET_CELL = "pending_target_cell";
private static final String COVER_BREAK_NOTICE = "cover_break_notice";
private int pendingTargetCell = -1;
private boolean coverBreakNoticeAnnounced;
```

将 `act()` 中的调用改为 `telegraph(skill, plan.cells, plan.bands, plan.landingCell, target.pos)`，让 `telegraph()` 在提交待结算技能时冻结 `pendingTargetCell`。`currentGrid()` 只在 `Dungeon.level instanceof TowerBossLevel` 时构建额外掩码：

```java
boolean[] arena = new boolean[Dungeon.level.length()];
boolean[] covers = new boolean[Dungeon.level.length()];
TowerBossLevel bossLevel = Dungeon.level instanceof TowerBossLevel
        ? (TowerBossLevel) Dungeon.level : null;
for (int cell = 0; cell < arena.length; cell++) {
    arena[cell] = bossLevel != null && bossLevel.isBossArenaCell(cell);
    covers[cell] = bossLevel != null && bossLevel.isDestructibleBossCover(cell);
}
return new DeathKnightBombardment.Grid(
        Dungeon.level.width(), Dungeon.level.height(),
        Dungeon.level.passable.clone(), Dungeon.level.solid.clone(),
        occupied, arena, covers);
```

非 Boss 层测试夹具仍使用旧构造器，默认所有网格在竞技场内且没有可拆掩体。

- [ ] **步骤 4：在伤害/状态之后、finishBombardment 之前接入拆场**

把 `resolvePendingSkill()` 的核心顺序固定为：

```java
for (Char target : Actor.chars()) {
    if (target == this || !contains(pendingCells, target.pos)) continue;
    int[] range = damageRange(skill);
    int raw = Random.NormalIntRange(range[0], range[1]);
    if (skill == Skill.EXECUTION) {
        raw = DeathKnightBombardment.scaledDamage(raw, bandAt(target.pos));
    }
    damageWithBombardment(target, raw);
    applySkillAftermath(skill, target, bandAt(target.pos));
}
breakNearbyCoverAfterResolution(pendingTargetCell);
finishBombardment(skill);
```

新增包可见/受保护方法：

```java
protected boolean breakNearbyCoverAfterResolution(int fallbackTargetCell) {
    if (!(Dungeon.level instanceof TowerBossLevel)) return false;
    TowerBossLevel level = (TowerBossLevel) Dungeon.level;
    int targetCell = Dungeon.hero != null && Dungeon.hero.isAlive()
            ? Dungeon.hero.pos : fallbackTargetCell;
    int[] candidates = DeathKnightBombardment.nearbyCoverCandidates(
            currentGrid(), pendingCells, targetCell);
    if (candidates.length == 0) return false;
    int cell = Random.element(candidates);
    if (!level.destroyBossCover(cell)) return false;
    showCoverBreak(cell);
    if (!coverBreakNoticeAnnounced) {
        coverBreakNoticeAnnounced = true;
        yell(Messages.get(this, "cover_break"));
    }
    return true;
}
```

`currentGrid()` 必须在 `destroyBossCover()` 前读取当前地图，确保玩家火焰或其他效果已经烧毁的掩体不会进入候选。候选为空、目标无效或地图无效时返回 `false`，但 `finishBombardment()` 仍然执行；不能让拆场失败阻塞 Boss 回合或跃迁回调。

- [ ] **步骤 5：运行测试确认 GREEN**

运行 `DeathKnightTest` 与 `DeathKnightBombardmentTest`，预期新增时序和所有技能入口测试通过，既有阶段、暂停、锁血、跃迁测试不变。

- [ ] **步骤 6：提交独立变更**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnight.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightTest.java
git commit -m "feat: break nearby cover after death knight bombardments"
```

### 任务 5：实现处刑场无视内部掩体且保留边界

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardment.java`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardmentTest.java`

- [ ] **步骤 1：补齐边界测试**

确保测试同时覆盖：内部 `solid` 掩体后的可站立格仍在 `execution()`；`arena[cell] == false` 的入口/场外格不在；实体掩体本身因 `solid/passable` 过滤不在；外圈/内圈/核心比例测试保持 40%/70%/100%。

```java
@Test
public void executionDoesNotIncludeCoverCellOrOutsideArena() {
    DeathKnightBombardment.Grid grid = coverGrid();
    grid.solid[cell(7, 5)] = true;
    grid.passable[cell(7, 5)] = false;
    grid.destructibleCover[cell(7, 5)] = true;
    grid.arena[cell(7, 6)] = false;

    DeathKnightBombardment.Plan plan = DeathKnightBombardment.execution(
            grid, cell(7, 1), cell(7, 7));
    assertFalse(plan.contains(cell(7, 5)));
    assertFalse(plan.contains(cell(7, 6)));
}
```

- [ ] **步骤 2：运行测试确认边界行为**

运行测试并在报告中明确显示“内部掩体之后的可站立格包含、实体掩体/场外不包含”；实现步骤必须删除 `execution()` 中残留的 `visible()` 条件。

- [ ] **步骤 3：实现并确认最小修改**

`execution()` 只移除 LOS 条件，不改 `Band` 的垂直距离计算、处刑范围、伤害倍率和颜色；普通 `line/cone/cross/ring` 保持 `visible()`。不得把外围永久墙或场外区域加入 `arena` 掩码。

- [ ] **步骤 4：运行测试并提交**

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnightBombardmentTest" --max-workers=1 --no-daemon --console=plain
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardment.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightBombardmentTest.java
git commit -m "feat: let execution field ignore internal cover"
```

预期：目标测试通过，处刑穿透只影响处刑场，不影响普通剑技和位移路径。

### 任务 6：接入碎裂表现、首次喊话和存档归一化

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnight.java`
- 修改：`core/src/main/assets/messages/actors/actors.properties`
- 修改：`core/src/main/assets/messages/actors/actors_zh.properties`
- 修改测试：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightTest.java`

- [ ] **步骤 1：先写失败的存档/提示测试**

新增：

```java
@Test
public void pendingTargetAndCoverNoticeSurviveBundleRoundTrip() {
    DeathKnight original = new DeathKnight();
    original.setPendingTargetForTest(17);
    original.markCoverBreakNoticeForTest();
    Bundle bundle = new Bundle();
    original.storeInBundle(bundle);

    DeathKnight restored = new DeathKnight();
    restored.restoreFromBundle(bundle);
    assertEquals(17, restored.pendingTargetCellForTest());
    assertTrue(restored.coverBreakNoticeAnnouncedForTest());
}

@Test
public void invalidPendingTargetFallsBackWithoutBreakingRestore() {
    Bundle bundle = new Bundle();
    new DeathKnight().storeInBundle(bundle);
    bundle.put("pending_target_cell", -999);
    DeathKnight restored = new DeathKnight();
    restored.restoreFromBundle(bundle);
    assertEquals(-1, restored.pendingTargetCellForTest());
}
```

- [ ] **步骤 2：实现 Bundle 字段和表现**

在 `storeInBundle()`/`restoreFromBundle()` 保存 `PENDING_TARGET_CELL` 与 `COVER_BREAK_NOTICE`。读取时将目标格限制在 `[-1, Dungeon.level.length())`（没有当前楼层时保持 `-1`），缺失键默认 `-1/false`。恢复中的 `restoreGrace` 只重绘待结算警示，不调用拆场。

新增 `showCoverBreak(int cell)`，复用现有无阻塞粒子/音效：

```java
private void showCoverBreak(int cell) {
    if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()) return;
    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 8);
    Sample.INSTANCE.play(Assets.Sounds.ROCKS);
}
```

使用项目现有的 `CellEmitter.center(cell).burst(BlastParticle.FACTORY, 8)` 调用签名，不新增同步等待或 `Pushing` 回调。首次成功摧毁后才设置 `coverBreakNoticeAnnounced = true`，失败、候选为空和转阶段不设置。

在 `actors.properties` 新增：

```properties
actors.mobs.tboss.deathknight.cover_break=The arena itself is breaking. Hiding will only delay death.
```

在 `actors_zh.properties` 新增：

```properties
actors.mobs.tboss.deathknight.cover_break=场地本身正在崩解。躲藏只会延缓死亡。
```

保留已有技能喊话和键名，不改 `notice`、阶段喊话或图鉴稳定 ID。

- [ ] **步骤 3：运行测试并提交**

```powershell
.\gradlew.bat :core:test --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnightTest" --max-workers=1 --no-daemon --console=plain
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnight.java core/src/main/assets/messages/actors/actors.properties core/src/main/assets/messages/actors/actors_zh.properties core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/DeathKnightTest.java
git commit -m "feat: persist death knight cover break feedback"
```

### 任务 7：构建隔离测试夹具并完成整体回归

**文件：**
- 创建：`tools/death_knight_cover_test.init.gradle`
- 只读验证：任务 1～6 涉及的 Java、资源和测试文件

- [ ] **步骤 1：创建隔离 Gradle init 脚本**

脚本必须把所有子项目的 `buildDirectory` 指向 `.codex-build-death-knight-cover/<project>`，并让 `:core:compileTestJava` 只编译以下目标测试：

```groovy
gradle.beforeProject { project ->
    def projectFolder = project.path == ':'
            ? 'root' : project.path.substring(1).replace(':', '/')
    project.layout.buildDirectory.set(
            new File(project.rootDir,
                    ".codex-build-death-knight-cover/${projectFolder}"))
}

gradle.projectsEvaluated {
    def coreProject = gradle.rootProject.findProject(':core')
    def focusedTests = coreProject.fileTree('src/test/java') {
        include '**/actors/mobs/tboss/DeathKnightTest.java'
        include '**/actors/mobs/tboss/DeathKnightBombardmentTest.java'
        include '**/levels/towers/TowerBossLevelTest.java'
    }
    coreProject.tasks.named('compileJava').configure {
        options.incremental = false
    }
    coreProject.tasks.named('compileTestJava').configure {
        options.incremental = false
        setSource(focusedTests)
    }
}
```

- [ ] **步骤 2：运行聚焦回归**

```powershell
.\gradlew.bat -I tools\death_knight_cover_test.init.gradle :core:test `
  --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnightTest" `
  --tests "com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnightBombardmentTest" `
  --tests "com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevelTest" `
  --max-workers=1 --no-daemon --console=plain
```

预期：`BUILD SUCCESSFUL`；三份 JUnit XML 的 `failures=0`、`errors=0`、`system-err` 为空。只读取 `.codex-build-death-knight-cover/core/test-results/test`，不使用共享 `core/build` 报告。

- [ ] **步骤 3：逐项核对规格验收点**

检查：28 个实体掩体是否全为 `BARRICADE`；软掩体是否可被选中；净化器/门/出口/永久墙是否保护；每次技能最多一次拆除且发生在伤害后；目标距离使用当前英雄位置；候选为空不全图补选；处刑穿透内部掩体但不穿墙/出界；阶段 3 的 1 个普通行动间隔自然加快拆场；读档后余烬、待结算技能、目标格和首次提示状态一致。

- [ ] **步骤 4：运行差异和空白检查**

```powershell
git diff --check
git status --short
```

确认没有把无关工作区修改加入本功能提交，也没有新增二进制资源或未引用的消息键。

- [ ] **步骤 5：提交测试夹具和最终变更**

```powershell
git add tools/death_knight_cover_test.init.gradle
git commit -m "test: isolate death knight cover regression"
```

完成后使用 `verification-before-completion` 重新读取最终测试输出，再报告实现结果；不能用旧 XML、静态推断或“没有看到失败日志”代替本次验证。
