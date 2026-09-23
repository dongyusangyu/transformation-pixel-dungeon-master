package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SaleSelfGoldRewardTest {

    private int previousDepth;

    @Before
    public void rememberDepth() {
        previousDepth = Dungeon.depth;
    }

    @After
    public void restoreDepth() {
        Dungeon.depth = previousDepth;
    }

    @Test
    public void contractAttackGoldIsDisabledOnSurfaceFloor() {
        Dungeon.depth = 0;

        assertFalse(Mob.canGrantSaleGold());
    }

    @Test
    public void contractAttackGoldRemainsEnabledInNormalDungeon() {
        Dungeon.depth = 1;

        assertTrue(Mob.canGrantSaleGold());
    }

    @Test
    public void contractAttackGoldRemainsEnabledInTower() {
        Dungeon.depth = -1;

        assertTrue(Mob.canGrantSaleGold());
    }
}
