package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.watabou.utils.Random;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TowerBossGeneratorTest {

    @Test
    public void onlyRegisteredBossIsAlwaysSelected() {
        for (long seed = 0; seed < 64; seed++) {
            assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID,
                    TowerBossGenerator.selectId(seed, 5, TowerLevel.BRANCH));
            assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID,
                    TowerBossGenerator.selectId(seed, 40, TowerLevel.BRANCH));
        }
    }

    @Test
    public void selectionIsStableAndDoesNotConsumeGlobalRandom() {
        Random.pushGenerator(0xCAFE);
        int expectedFirst = Random.Int();
        int expectedSecond = Random.Int();
        Random.popGenerator();

        Random.pushGenerator(0xCAFE);
        assertEquals(expectedFirst, Random.Int());
        String first = TowerBossGenerator.selectId(0x1234L, 10, TowerLevel.BRANCH);
        String second = TowerBossGenerator.selectId(0x1234L, 10, TowerLevel.BRANCH);
        assertEquals(first, second);
        assertEquals(expectedSecond, Random.Int());
        Random.popGenerator();
    }

    @Test
    public void registryEntryKeepsFutureWeightAndFloorBounds() {
        TowerBossGenerator.Entry entry = TowerBossGenerator.entries().get(0);

        assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID, entry.id());
        assertTrue(entry.weight() > 0);
        assertEquals(1, entry.minBossIndex());
        assertEquals(Integer.MAX_VALUE, entry.maxBossIndex());
        assertFalse(TowerBossGenerator.entries().isEmpty());
    }

    @Test
    public void registeredIdCreatesTheExpectedBoss() {
        TowerBoss boss = TowerBossGenerator.create(TowerBossGenerator.PESTILENCE_KNIGHT_ID);

        assertTrue(boss instanceof PestilenceKnight);
        assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID, boss.towerBossId());
    }

    @Test
    public void unknownIdAndEmptyEligibleRegistryFailExplicitly() {
        try {
            TowerBossGenerator.create("missing");
            fail("unknown ids must fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("missing"));
        }

        try {
            TowerBossGenerator.selectId(Collections.<TowerBossGenerator.Entry>emptyList(), 1L);
            fail("empty registries must fail");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("eligible"));
        }
    }
}
