package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TowerBossRecoveryTest {

    private int originalChallenges;

    @Before
    public void setUp() {
        originalChallenges = Dungeon.challenges;
    }

    @After
    public void tearDown() {
        Dungeon.challenges = originalChallenges;
    }

    @Test
    public void lockedFloorStartsAtFiftyOrTwentyTurns() {
        Dungeon.challenges = 0;
        assertEquals(50f, remainingTime(new LockedFloor()), 0.001f);

        Dungeon.challenges = Challenges.STRONGER_BOSSES;
        assertEquals(20f, remainingTime(new LockedFloor()), 0.001f);
    }

    @Test
    public void recoveryTimeUsesYogDamageRatios() {
        assertEquals(12f, TowerBoss.recoveryTimeForDamage(24, false), 0.001f);
        assertEquals(8f, TowerBoss.recoveryTimeForDamage(24, true), 0.001f);
    }

    @Test
    public void zeroDamageDoesNotGrantRecoveryTime() {
        assertEquals(0f, TowerBoss.recoveryTimeForDamage(0, false), 0.001f);
        assertEquals(0f, TowerBoss.recoveryTimeForDamage(0, true), 0.001f);
    }

    @Test
    public void everyTowerBossHasAFixedMetamorphosisReward() {
        assertEquals(ScrollOfMetamorphosis.class, TowerBoss.GUARANTEED_TOWER_REWARD);
    }

    private static float remainingTime(LockedFloor lock) {
        Bundle bundle = new Bundle();
        lock.storeInBundle(bundle);
        return bundle.getFloat("left");
    }
}
