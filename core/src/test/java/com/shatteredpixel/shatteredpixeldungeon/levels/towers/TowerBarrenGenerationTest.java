package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TowerBarrenGenerationTest {

    @Test
    public void barrenFeelingRemovesGeneratedWaterAndGrassTerrain() {
        TestLevel level = new TestLevel();
        level.setSize(3, 3);
        level.map = new int[]{
                Terrain.WATER, Terrain.HIGH_GRASS, Terrain.GRASS,
                Terrain.FURROWED_GRASS, Terrain.EMPTY, Terrain.EMPTY_SP,
                Terrain.WALL, Terrain.CHASM, Terrain.DOOR
        };

        TowerGenerationRules.clearBarrenTerrain(level);

        assertArrayEquals(new int[]{
                Terrain.EMPTY, Terrain.EMPTY, Terrain.EMPTY,
                Terrain.EMPTY, Terrain.EMPTY, Terrain.EMPTY_SP,
                Terrain.WALL, Terrain.CHASM, Terrain.DOOR
        }, level.map);
    }

    @Test
    public void barrenFeelingDisablesRandomWaterAndGrassPainting() {
        assertEquals(0f, TowerGenerationRules.waterFill(Level.Feeling.BARREN), 0f);
        assertEquals(0f, TowerGenerationRules.grassFill(Level.Feeling.BARREN), 0f);
        assertEquals(0.65f, TowerGenerationRules.waterFill(Level.Feeling.WATER), 0f);
        assertEquals(0.55f, TowerGenerationRules.grassFill(Level.Feeling.GRASS), 0f);
        assertEquals(0.15f, TowerGenerationRules.waterFill(Level.Feeling.NONE), 0f);
        assertEquals(0.12f, TowerGenerationRules.grassFill(Level.Feeling.NONE), 0f);
    }

    private static class TestLevel extends Level {
        @Override
        protected boolean build() {
            return true;
        }

        @Override
        protected void createMobs() {
        }

        @Override
        protected void createItems() {
        }
    }
}
