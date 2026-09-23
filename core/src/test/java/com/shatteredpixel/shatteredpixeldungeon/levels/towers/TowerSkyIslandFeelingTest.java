package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class TowerSkyIslandFeelingTest {

    @Test
    public void towerUsesTwentyStableFeelingBuckets() {
        assertSame(Level.Feeling.CHASM, TowerGenerationRules.feelingForRoll(0));
        assertSame(Level.Feeling.SKY_ISLAND, TowerGenerationRules.feelingForRoll(1));
        assertSame(Level.Feeling.BARREN, TowerGenerationRules.feelingForRoll(2));
        assertSame(Level.Feeling.WATER, TowerGenerationRules.feelingForRoll(3));
        assertSame(Level.Feeling.SECRETS, TowerGenerationRules.feelingForRoll(8));
        assertSame(Level.Feeling.CHAOS, TowerGenerationRules.feelingForRoll(9));
        for (int roll = 10; roll < 20; roll++) {
            assertSame(Level.Feeling.NONE, TowerGenerationRules.feelingForRoll(roll));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void towerRejectsFeelingRollOutsideItsTable() {
        TowerGenerationRules.feelingForRoll(20);
    }
}
