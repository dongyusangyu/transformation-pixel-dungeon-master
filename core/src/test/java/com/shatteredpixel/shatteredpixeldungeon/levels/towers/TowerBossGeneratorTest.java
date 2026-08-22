package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
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
    public void registeredBossesRotateDeterministicallyWithEqualWeights() {
        boolean sawPestilence = false;
        boolean sawDeath = false;
        boolean sawGentleman = false;
        for (long seed = 0; seed < 64; seed++) {
            String first = TowerBossGenerator.selectId(seed, 5, TowerLevel.BRANCH);
            String repeated = TowerBossGenerator.selectId(seed, 5, TowerLevel.BRANCH);
            assertEquals(first, repeated);
            sawPestilence |= TowerBossGenerator.PESTILENCE_KNIGHT_ID.equals(first);
            sawDeath |= TowerBossGenerator.DEATH_KNIGHT_ID.equals(first);
            sawGentleman |= TowerBossGenerator.GENTLEMAN_ELF_ID.equals(first);
        }
        assertTrue(sawPestilence);
        assertTrue(sawDeath);
        assertTrue(sawGentleman);
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
    public void predictionTargetsTheNextBossFloorAboveTheCurrentFloor() {
        assertEquals(5, TowerBossGenerator.nextBossDepthAfter(1));
        assertEquals(5, TowerBossGenerator.nextBossDepthAfter(4));
        assertEquals(10, TowerBossGenerator.nextBossDepthAfter(5));
        assertEquals(10, TowerBossGenerator.nextBossDepthAfter(9));
        assertEquals(5, TowerBossGenerator.predictionDepth(5, true));
        assertEquals(10, TowerBossGenerator.predictionDepth(5, false));

        long seed = 0x1234L;
        assertEquals(TowerBossGenerator.selectId(seed, 5, TowerLevel.BRANCH),
                TowerBossGenerator.predictId(seed, 4, TowerLevel.BRANCH, false));
        assertEquals(TowerBossGenerator.selectId(seed, 5, TowerLevel.BRANCH),
                TowerBossGenerator.predictId(seed, 5, TowerLevel.BRANCH, true));
        assertEquals(TowerBossGenerator.selectId(seed, 10, TowerLevel.BRANCH),
                TowerBossGenerator.predictId(seed, 5, TowerLevel.BRANCH, false));
    }

    @Test
    public void registryEntryKeepsFutureWeightAndFloorBounds() {
        assertEquals(3, TowerBossGenerator.entries().size());
        TowerBossGenerator.Entry entry = TowerBossGenerator.entries().get(0);
        TowerBossGenerator.Entry death = TowerBossGenerator.entries().get(1);
        TowerBossGenerator.Entry gentleman = TowerBossGenerator.entries().get(2);

        assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID, entry.id());
        assertTrue(entry.weight() > 0);
        assertEquals(1, entry.minBossIndex());
        assertEquals(Integer.MAX_VALUE, entry.maxBossIndex());
        assertEquals(TowerBossGenerator.DEATH_KNIGHT_ID, death.id());
        assertEquals(entry.weight(), death.weight());
        assertEquals(1, death.minBossIndex());
        assertEquals(Integer.MAX_VALUE, death.maxBossIndex());
        assertEquals(TowerBossGenerator.GENTLEMAN_ELF_ID, gentleman.id());
        assertEquals(entry.weight(), gentleman.weight());
        assertEquals(1, gentleman.minBossIndex());
        assertEquals(Integer.MAX_VALUE, gentleman.maxBossIndex());
        assertFalse(TowerBossGenerator.entries().isEmpty());
    }

    @Test
    public void registeredIdCreatesTheExpectedBoss() {
        TowerBoss boss = TowerBossGenerator.create(TowerBossGenerator.PESTILENCE_KNIGHT_ID);

        assertTrue(boss instanceof PestilenceKnight);
        assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID, boss.towerBossId());

        TowerBoss death = TowerBossGenerator.create(TowerBossGenerator.DEATH_KNIGHT_ID);
        assertTrue(death instanceof DeathKnight);
        assertEquals(TowerBossGenerator.DEATH_KNIGHT_ID, death.towerBossId());

        TowerBoss gentleman = TowerBossGenerator.create(TowerBossGenerator.GENTLEMAN_ELF_ID);
        assertTrue(gentleman instanceof GentlemanElf);
        assertEquals(TowerBossGenerator.GENTLEMAN_ELF_ID, gentleman.towerBossId());
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
