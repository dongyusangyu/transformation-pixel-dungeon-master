package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.BlindingDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.utils.Bundle;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class HuntressArenaDecorationTest {
    private final java.util.Map<java.lang.reflect.Field, Object> previousPaths = new java.util.HashMap<>();
    @org.junit.Before public void rememberPathFinder() throws Exception {
        for (java.lang.reflect.Field field : com.watabou.utils.PathFinder.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                previousPaths.put(field, field.get(null));
            }
        }
    }
    @org.junit.After public void restorePathFinder() throws Exception {
        for (java.util.Map.Entry<java.lang.reflect.Field, Object> entry : previousPaths.entrySet()) {
            entry.getKey().set(null, entry.getValue());
        }
    }
    @Test public void newArenaHasSeparateCityApproachAndClosedFence() {
        long previous = Dungeon.seed;
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            Dungeon.seed = 123456;
            HuntressBossLevel level = new HuntressBossLevel() {
                @Override protected void createItems() {}
            };
            level.create();
            assertEquals(43, level.height());
            assertEquals(15 + 40 * 31, level.entrance());
            assertEquals(15 + 2 * 31, level.exit());
            assertFalse(level.isArenaCell(level.exit()));
            assertFalse(level.isArenaCell(15 + 12 * 31));
            for (int x = 13; x <= 17; x++) {
                int gate = x + 12 * 31;
                assertEquals(Terrain.CUSTOM_DECO, level.map[gate]);
                assertTrue(level.solid[gate]);
                assertFalse(level.passable[gate]);
            }
            com.watabou.utils.PathFinder.buildDistanceMap(level.entrance(), level.passable);
            assertEquals(Integer.MAX_VALUE, com.watabou.utils.PathFinder.distance[level.exit()]);
            assertEquals(3, level.customTiles.size() + level.customWalls.size());
            assertTrue(Arrays.stream(level.map).anyMatch(t -> t == Terrain.EMPTY_DECO));
            assertTrue(Arrays.stream(level.map).anyMatch(t -> t == Terrain.WALL_DECO));
            level.buildFlagMaps();
            Bundle saved = new Bundle();
            level.storeInBundle(saved);
            saved.put("version", com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.v2_4_2);
            HuntressBossLevel restored = new HuntressBossLevel();
            restored.restoreFromBundle(saved);
            assertArrayEquals(level.map, restored.map);
            assertEquals(level.triggerCell(), restored.triggerCell());
            assertEquals(level.coverClusters(), restored.coverClusters());
        } finally { Dungeon.seed = previous; }
    }

    @Test public void cityCarpetHasOneWideExitAndWallsAreActuallySolid() {
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            HuntressBossLevel level = createEmptyLevel();
            LevelTransition exit = level.getTransition(LevelTransition.Type.REGULAR_EXIT);
            assertEquals(15 + 2 * 31, exit.cell());
            for (int y = 0; y <= 2; y++) {
                for (int x = 14; x <= 16; x++) {
                    int cell = x + y * 31;
                    assertEquals(Terrain.EXIT, level.map[cell]);
                    assertEquals(y > 0, level.passable[cell]);
                    assertTrue(exit.inside(level, cell));
                }
                for (int x : new int[]{13, 17}) {
                    assertTrue(level.solid[x + y * 31]);
                    assertFalse(level.passable[x + y * 31]);
                }
            }
            assertFalse(exit.inside(level, 15 + 3 * 31));
            for (int x : new int[]{8, 22}) {
                for (int y = 3; y <= 8; y++) {
                    assertEquals(Terrain.REGION_DECO_ALT, level.map[x + y * 31]);
                    assertTrue(level.solid[x + y * 31]);
                }
            }
            for (CustomTilemap tile : level.customTiles) {
                if (tile instanceof CavesBossLevel.CityEntrance) {
                    assertEquals(0, tile.tileX);
                    assertEquals(level.width(), tile.tileW);
                }
            }
        }
    }

    @Test public void expandedLegacySaveRepairsOnlyCityAndPreservesEncounterAndItems() {
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            HuntressBossLevel source = createEmptyLevel();
            makeLegacyCity(source);
            source.locked = true;
            LevelTransition exit = source.getTransition(LevelTransition.Type.REGULAR_EXIT);
            exit.destDepth = 16;
            exit.destBranch = 0;
            Heap heap = new Heap();
            heap.pos = 13 + 2 * 31;
            heap.items.add(new BlindingDart().quantity(2));
            source.heaps.put(heap.pos, heap);
            Rat rat = new Rat();
            rat.pos = 17 + 31;
            source.mobs.add(rat);
            Bundle saved = save(source);
            saved.put("huntress_city_approach_version", 0);
            saved.put("huntress_boss_state", HuntressBossLevel.State.FIGHT);
            HuntressBossLevel restored = new HuntressBossLevel();
            restored.restoreFromBundle(saved);
            assertEquals(HuntressBossLevel.State.FIGHT, restored.state());
            assertTrue(restored.locked);
            assertArrayEquals(Arrays.copyOfRange(source.map, 13 * 31, source.length()),
                    Arrays.copyOfRange(restored.map, 13 * 31, restored.length()));
            assertEquals(source.coverClusters(), restored.coverClusters());
            assertEquals(Terrain.CUSTOM_DECO, restored.map[15 + 12 * 31]);
            assertTrue(restored.invalidHeroPos(13 + 2 * 31));
            LevelTransition repairedExit = restored.getTransition(LevelTransition.Type.REGULAR_EXIT);
            assertEquals(exit.cell(), repairedExit.cell());
            assertEquals(16, repairedExit.destDepth);
            assertEquals(0, repairedExit.destBranch);
            assertTrue(repairedExit.inside(restored, 14 + 2 * 31));
            assertEquals(1, restored.heaps.valueList().size());
            Heap restoredHeap = restored.heaps.valueList().get(0);
            assertTrue(restored.passable[restoredHeap.pos]);
            assertEquals(2, restoredHeap.items.getFirst().quantity());
            assertEquals(1, restored.mobs.size());
            assertTrue(restored.passable[restored.mobs.iterator().next().pos]);
            assertEquals(3, restored.customTiles.size() + restored.customWalls.size());
            HuntressBossLevel secondLoad = new HuntressBossLevel();
            secondLoad.restoreFromBundle(save(restored));
            assertArrayEquals(restored.map, secondLoad.map);
            assertEquals(restoredHeap.pos, secondLoad.heaps.valueList().get(0).pos);
        }
    }

    @Test public void expandedLegacyVictoryKeepsGateOpenAfterRepair() {
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            HuntressBossLevel source = createEmptyLevel();
            makeLegacyCity(source);
            Bundle saved = save(source);
            saved.put("huntress_city_approach_version", 0);
            saved.put("huntress_boss_state", HuntressBossLevel.State.WON);
            HuntressBossLevel restored = new HuntressBossLevel();
            restored.restoreFromBundle(saved);
            for (int x = 13; x <= 17; x++) assertTrue(restored.passable[x + 12 * 31]);
        }
    }

    private HuntressBossLevel createEmptyLevel() {
        HuntressBossLevel level = new HuntressBossLevel() {
            @Override protected void createItems() {}
        };
        level.create();
        return level;
    }

    private Bundle save(HuntressBossLevel level) {
        Bundle saved = new Bundle();
        level.storeInBundle(saved);
        saved.put("version", com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.v2_4_2);
        return saved;
    }

    private void makeLegacyCity(HuntressBossLevel level) {
        Painter.fill(level, 0, 0, 31, 13, Terrain.WALL);
        Painter.fill(level, 0, 3, 31, 9, Terrain.CHASM);
        Painter.fill(level, 13, 0, 5, 13, Terrain.EMPTY);
        Painter.fill(level, 14, 0, 3, 5, Terrain.EMPTY_SP);
        for (int y : new int[]{5, 7, 9}) {
            Painter.set(level, 14, y, Terrain.STATUE);
            Painter.set(level, 16, y, Terrain.STATUE);
        }
        Painter.fill(level, 15, 5, 1, 6, Terrain.EMPTY_SP);
        Painter.fill(level, 13, 12, 5, 1, Terrain.WALL);
        Painter.set(level, level.exit(), Terrain.EXIT);
        for (CustomTilemap tile : level.customTiles) {
            if (tile instanceof CavesBossLevel.CityEntrance) tile.setRect(13, 0, 5, 11);
        }
        for (CustomTilemap tile : level.customWalls) tile.setRect(13, 0, 5, 11);
        level.getTransition(LevelTransition.Type.REGULAR_EXIT).set(15, 2, 15, 2);
        level.buildFlagMaps();
    }

    @Test public void cityFenceSelectsClosedAndOpenFramesFromTheRealAtlas() throws Exception {
        Level previous = Dungeon.level;
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites sprites =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            java.awt.image.BufferedImage atlas = javax.imageio.ImageIO.read(
                    java.nio.file.Path.of("src/main/assets/environment/custom_tiles/caves_boss.png").toFile());
            sprites.addSheet(com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.CAVES_BOSS,
                    atlas.getWidth(), atlas.getHeight());
            HuntressBossLevel level = new HuntressBossLevel() {
                @Override protected void createItems() {}
            };
            level.create();
            Dungeon.level = level;
            for (com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap tile : level.customTiles) {
                com.watabou.noosa.Tilemap visual = tile.create();
                java.lang.reflect.Field field = com.watabou.noosa.Tilemap.class.getDeclaredField("data");
                field.setAccessible(true);
                int[] data = (int[]) field.get(visual);
                for (int frame : data) assertTrue(frame < atlas.getWidth() / 16 * (atlas.getHeight() / 16));
                if (tile instanceof HuntressBossLevel.CityFence) {
                    assertArrayEquals(new int[]{40, 41, 42, 43, 44}, data);
                    for (int x = 13; x <= 17; x++) Level.set(x + 12 * level.width(), Terrain.EMPTY, level);
                    ((HuntressBossLevel.CityFence) tile).updateState(level);
                    assertArrayEquals(new int[]{32, 33, 34, 35, 36}, (int[]) field.get(visual));
                }
            }
            for (com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap tile : level.customWalls) tile.create();
        } finally { Dungeon.level = previous; }
    }

    @Test public void cityLayersMatchDm300WhenCenteredOnTheNarrowerMap() throws Exception {
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites sprites =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            java.awt.image.BufferedImage atlas = javax.imageio.ImageIO.read(
                    java.nio.file.Path.of("src/main/assets/environment/custom_tiles/caves_boss.png").toFile());
            sprites.addSheet(com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.CAVES_BOSS,
                    atlas.getWidth(), atlas.getHeight());
            java.lang.reflect.Field dataField = com.watabou.noosa.Tilemap.class.getDeclaredField("data");
            dataField.setAccessible(true);
            for (boolean overhang : new boolean[]{false, true}) {
                CustomTilemap dm = overhang ? new CavesBossLevel.EntranceOverhang() : new CavesBossLevel.CityEntrance();
                dm.setRect(0, 0, 33, 11);
                int[] original = (int[]) dataField.get(dm.create());
                CustomTilemap huntress = overhang ? new CavesBossLevel.EntranceOverhang() : new CavesBossLevel.CityEntrance();
                huntress.setRect(0, 0, 31, 11);
                int[] adapted = (int[]) dataField.get(huntress.create());
                for (int y = 0; y < 11; y++) {
                    for (int x = 0; x < 31; x++) {
                        assertEquals("frame at " + x + "," + y, original[x + 1 + y * 33], adapted[x + y * 31]);
                    }
                }
                if (!overhang) {
                    assertEquals(-1, original[9 + 3 * 33]);
                    assertEquals(-1, original[23 + 3 * 33]);
                    assertEquals(13, adapted[3 + 2 * 31]);
                    assertEquals(21, adapted[3 + 3 * 31]);
                }
            }
        }
    }

    @Test public void realRenderLayersKeepTheFenceFreeOfWallOverhang() throws Exception {
        Level oldLevel = Dungeon.level;
        byte[] oldVariance = com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.tileVariance;
        java.lang.reflect.Field instance = com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap.class
                .getDeclaredField("instance");
        instance.setAccessible(true);
        Object oldInstance = instance.get(null);
        java.util.Set<Integer> oldSkipCells = new java.util.HashSet<>(
                com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap.skipCells);
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites sprites =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            java.awt.image.BufferedImage caves = javax.imageio.ImageIO.read(java.nio.file.Path.of("src/main/assets",
                    com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.TILES_CAVES).toFile());
            java.awt.image.BufferedImage city = javax.imageio.ImageIO.read(java.nio.file.Path.of("src/main/assets",
                    com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.CAVES_BOSS).toFile());
            sprites.addSheet(com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.TILES_CAVES,
                    caves.getWidth(), caves.getHeight());
            sprites.addSheet(com.shatteredpixel.shatteredpixeldungeon.Assets.Environment.CAVES_BOSS,
                    city.getWidth(), city.getHeight());
            HuntressBossLevel level = createEmptyLevel();
            Dungeon.level = level;
            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.setupVariance(level.length(), 150015L);
            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap terrain =
                    new com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap();
            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap walls =
                    new com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap();
            java.lang.reflect.Field dataField = com.watabou.noosa.Tilemap.class.getDeclaredField("data");
            dataField.setAccessible(true);
            java.awt.image.BufferedImage preview = new java.awt.image.BufferedImage(31 * 16 * 2, 15 * 16,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = preview.createGraphics();
            try {
                graphics.setColor(java.awt.Color.BLACK);
                graphics.fillRect(0, 0, preview.getWidth(), preview.getHeight());
                for (int stage = 0; stage < 2; stage++) {
                    if (stage == 1) for (int x = 13; x <= 17; x++) Level.set(x + 12 * 31, Terrain.EMPTY, level);
                    terrain.updateMap();
                    walls.updateMap();
                    int[] wallFrames = (int[]) dataField.get(walls);
                    for (int x = 13; x <= 17; x++) assertEquals(-1, wallFrames[x + 11 * 31]);
                    int offset = stage * 31 * 16;
                    drawFrames(graphics, caves, (int[]) dataField.get(terrain), 31, offset, 0);
                    for (CustomTilemap tile : level.customTiles) {
                        drawFrames(graphics, city, (int[]) dataField.get(tile.create()), tile.tileW,
                                offset + tile.tileX * 16, tile.tileY * 16);
                    }
                    drawFrames(graphics, caves, wallFrames, 31, offset, 0);
                    for (CustomTilemap tile : level.customWalls) {
                        drawFrames(graphics, city, (int[]) dataField.get(tile.create()), tile.tileW,
                                offset + tile.tileX * 16, tile.tileY * 16);
                    }
                }
            } finally { graphics.dispose(); }
            java.nio.file.Path output = java.nio.file.Path.of("build/reports/huntress-city-visuals.png");
            java.nio.file.Files.createDirectories(output.getParent());
            assertTrue(javax.imageio.ImageIO.write(preview, "png", output.toFile()));
        } finally {
            Dungeon.level = oldLevel;
            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.tileVariance = oldVariance;
            instance.set(null, oldInstance);
            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap.skipCells.clear();
            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap.skipCells.addAll(oldSkipCells);
        }
    }

    private void drawFrames(java.awt.Graphics2D graphics, java.awt.image.BufferedImage atlas, int[] data,
                            int width, int left, int top) {
        int columns = atlas.getWidth() / 16;
        int count = columns * (atlas.getHeight() / 16);
        for (int i = 0; i < data.length; i++) {
            assertTrue("atlas frame " + data[i], data[i] >= -1 && data[i] < count);
            if (data[i] < 0 || top + i / width * 16 >= 15 * 16) continue;
            int sx = data[i] % columns * 16;
            int sy = data[i] / columns * 16;
            int x = left + i % width * 16;
            int y = top + i / width * 16;
            graphics.drawImage(atlas, x, y, x + 16, y + 16, sx, sy, sx + 16, sy + 16, null);
        }
    }

    @Test public void winningOpensTheCityRoadAndTheFenceStaysOpenAfterReload() {
        Level previousLevel = Dungeon.level;
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero previousHero = Dungeon.hero;
        com.badlogic.gdx.Application previousApp = com.badlogic.gdx.Gdx.app;
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            com.badlogic.gdx.Gdx.app = (com.badlogic.gdx.Application) java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class[]{com.badlogic.gdx.Application.class},
                    (proxy, method, args) -> null);
            HuntressBossLevel level = new HuntressBossLevel() {
                @Override protected void createItems() {}
            };
            level.create();
            Dungeon.level = level;
            Dungeon.hero = null;
            level.onBossDefeated();
            assertEquals(HuntressBossLevel.State.WON, level.state());
            assertFalse(level.locked);
            com.watabou.utils.PathFinder.buildDistanceMap(level.entrance(), level.passable);
            assertTrue(com.watabou.utils.PathFinder.distance[level.exit()] < Integer.MAX_VALUE);
            Bundle saved = new Bundle();
            level.storeInBundle(saved);
            saved.put("version", com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.v2_4_2);
            HuntressBossLevel restored = new HuntressBossLevel();
            restored.restoreFromBundle(saved);
            assertEquals(HuntressBossLevel.State.WON, restored.state());
            for (int x = 13; x <= 17; x++) assertTrue(restored.passable[x + 12 * restored.width()]);
        } finally {
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
            com.badlogic.gdx.Gdx.app = previousApp;
        }
    }

    @Test public void legacyMapKeepsItsOriginalCoordinatesAndSafeArrivalArea() {
        HuntressBossLevel legacy = new HuntressBossLevel();
        legacy.setSize(31, 32);
        assertEquals(15 + 22 * 31, legacy.triggerCell());
        assertTrue(legacy.isArenaCell(15 + 2 * 31));
        assertFalse(legacy.isArenaCell(15 + 29 * 31));
    }

    @Test public void legacyBundleRestoresWithoutExpandingOrAddingCityTiles() {
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
            HuntressBossLevel source = new HuntressBossLevel() {
                @Override protected void createItems() {}
            };
            source.create();
            source.setSize(31, 32);
            source.transitions.clear();
            source.customTiles.clear();
            source.customWalls.clear();
            Arrays.fill(source.map, Terrain.WALL);
            com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter.fill(source, 3, 2, 25, 22, Terrain.EMPTY);
            com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter.fill(source, 13, 24, 5, 6, Terrain.EMPTY);
            source.map[15 + 23 * 31] = Terrain.EMPTY;
            source.map[15 + 29 * 31] = Terrain.ENTRANCE;
            source.map[15 + 2 * 31] = Terrain.EXIT;
            source.transitions.add(new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition(
                    source, 15 + 29 * 31, com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_ENTRANCE));
            source.transitions.add(new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition(
                    source, 15 + 2 * 31, com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_EXIT));
            source.buildFlagMaps();
            Bundle bundle = new Bundle();
            source.storeInBundle(bundle);
            bundle.put("version", com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.v2_4_2);
            bundle.put("cover_cluster_cells", new int[0]);
            bundle.put("cover_cluster_sizes", new int[0]);
            HuntressBossLevel restored = new HuntressBossLevel();
            restored.restoreFromBundle(bundle);
            assertArrayEquals(source.map, restored.map);
            assertEquals(32, restored.height());
            assertEquals(source.entrance(), restored.entrance());
            assertEquals(source.exit(), restored.exit());
            assertEquals(source.triggerCell(), restored.triggerCell());
            assertTrue(restored.customTiles.isEmpty());
            assertTrue(restored.customWalls.isEmpty());
        }
    }
}
