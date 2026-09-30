package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class ExtractionGrassFeaturesTest {
    private HeadlessItemSprites sheets;
    private Level oldLevel;
    private int oldDepth;
    private int oldBranch;
    private byte[] oldVariance;
    private Object oldInstance;

    @Before public void setUp() throws Exception {
        GdxNativesLoader.load();
        sheets = new HeadlessItemSprites();
        sheets.addSheet(Assets.Environment.TERRAIN_FEATURES, 256, 256);
        oldLevel = Dungeon.level;
        oldDepth = Dungeon.depth;
        oldBranch = Dungeon.branch;
        oldVariance = DungeonTileSheet.tileVariance;
        Field instance = TerrainFeaturesTilemap.class.getDeclaredField("instance");
        instance.setAccessible(true);
        oldInstance = instance.get(null);
    }

    @After public void tearDown() throws Exception {
        Dungeon.level = oldLevel;
        Dungeon.depth = oldDepth;
        Dungeon.branch = oldBranch;
        DungeonTileSheet.tileVariance = oldVariance;
        Field instance = TerrainFeaturesTilemap.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, oldInstance);
        sheets.close();
    }

    @Test public void raidGrassAlwaysUsesSewerDecorationRegardlessOfEntranceDepth() {
        TerrainFeaturesTilemap features = features(new ExtractionRaidLevel(), new SparseArray<Trap>());
        Dungeon.branch = ExtractionRaidLevel.BRANCH;
        for (int depth : new int[]{1, 6, 11, 16, 21, 31, 100}) {
            Dungeon.depth = depth;
            for (int variance : new int[]{0, 75}) {
                DungeonTileSheet.tileVariance[12] = (byte) variance;
                int alternate = variance >= 50 ? 1 : 0;
                assertEquals("raid high grass at depth " + depth, 9 + alternate,
                        features.getTileVisual(12, Terrain.HIGH_GRASS, false));
                assertEquals(11 + alternate, features.getTileVisual(12, Terrain.FURROWED_GRASS, false));
                assertEquals(13 + alternate, features.getTileVisual(12, Terrain.GRASS, false));
            }
        }
    }

    @Test public void ordinaryDemonFloorsRetainDemonGrassDecoration() {
        TerrainFeaturesTilemap features = features(new TestLevel(), new SparseArray<Trap>());
        Dungeon.depth = 31;
        Dungeon.branch = 0;
        assertEquals(73, features.getTileVisual(12, Terrain.HIGH_GRASS, false));
        assertEquals(75, features.getTileVisual(12, Terrain.FURROWED_GRASS, false));
        assertEquals(77, features.getTileVisual(12, Terrain.GRASS, false));
    }

    @Test public void surfaceFloorUsesSewerGrassDecoration() {
        TerrainFeaturesTilemap features = features(new SurfaceTownLevel(), new SparseArray<Trap>());
        Dungeon.depth = 0;
        Dungeon.branch = 0;
        assertEquals(9, features.getTileVisual(12, Terrain.HIGH_GRASS, false));
        assertEquals(11, features.getTileVisual(12, Terrain.FURROWED_GRASS, false));
        assertEquals(13, features.getTileVisual(12, Terrain.GRASS, false));
    }

    @Test public void raidTrapGraphicsStillTakePrecedenceOverGrass() {
        SparseArray<Trap> traps = new SparseArray<>();
        BurningTrap trap = new BurningTrap();
        trap.visible = true;
        trap.active = true;
        traps.put(12, trap);
        TerrainFeaturesTilemap features = features(new ExtractionRaidLevel(), traps);
        Dungeon.depth = 31;
        assertEquals(trap.color + trap.shape * 16, features.getTileVisual(12, Terrain.GRASS, false));
    }

    private TerrainFeaturesTilemap features(Level level, SparseArray<Trap> traps) {
        level.setSize(5, 5);
        Dungeon.level = level;
        DungeonTileSheet.tileVariance = new byte[level.length()];
        return new TerrainFeaturesTilemap(new SparseArray<Plant>(), traps);
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
