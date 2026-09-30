package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/** Characterizes the terrain ignition timing before the shared ignition fix. */
public class FlameCombustionDurationCharacterizationTest {

    private Level oldLevel;
    private TestLevel level;
    private static final int CENTER = 24;

    @Before public void setUp() {
        oldLevel = Dungeon.level;
        level = new TestLevel();
        level.setSize(7, 7);
        level.blobs = new HashMap<>();
        level.heaps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.traps = new SparseArray<>();
        level.customTiles = new ArrayList<>();
        Dungeon.level = level;
    }

    @After public void tearDown() {
        Dungeon.level = oldLevel;
    }

    @Test public void ordinaryFireSpreadingToCombustibleTerrainLastsFourTurns() {
        level.flamable[CENTER + 1] = true;
        Fire fire = Blob.seed(CENTER, 2, Fire.class);

        fire.act();

        assertEquals(4, fire.cur[CENTER + 1]);
    }

    @Test public void directCursedFlameIgnitionCurrentlyLastsOnlyTwoTurns() {
        level.flamable[CENTER] = true;
        CursedFlame flame = Blob.seed(CENTER, 2, CursedFlame.class);

        flame.act();
        assertEquals(1, flame.cur[CENTER]);
        flame.act();

        assertEquals(0, flame.cur[CENTER]);
        assertEquals(1, level.destroyed);
    }

    private static class TestLevel extends Level {
        int destroyed;

        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}

        @Override public void destroy(int cell) {
            destroyed++;
            flamable[cell] = false;
        }
    }
}
