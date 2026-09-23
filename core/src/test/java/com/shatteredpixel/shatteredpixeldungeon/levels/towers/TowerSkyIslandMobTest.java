package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TowerSkyIslandMobTest {

    @Test
    public void flyingPoolIsDerivedFromTheRegisteredTowerMonsters() {
        ArrayList<TowerMobRules.Selection> flying =
                TowerMobRules.eligibleSelections(true);
        assertEquals(Arrays.asList(
                TowerMobRules.Selection.CORROSIVE_SWARM,
                TowerMobRules.Selection.DEATH_BUTTERFLY), flying);
        assertEquals(TowerMobRules.Selection.values().length,
                TowerMobRules.eligibleSelections(false).size());
    }

    @Test
    public void everySkyIslandNaturalSpawnFliesAndStartsWandering() {
        for (int i = 0; i < 200; i++) {
            Mob mob = TowerMobRules.createNaturalSpawn(true);
            assertTrue(mob.flying);
            assertSame(mob.WANDERING, mob.state);
        }
    }
}
