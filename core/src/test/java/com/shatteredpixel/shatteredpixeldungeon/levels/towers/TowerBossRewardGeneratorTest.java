package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerBossRewardGeneratorTest {

    @Test
    public void tierSixRewardIsDeterministicIdentifiedUncursedAndPlusThree() {
        Class<? extends MeleeWeapon> first = TowerBossRewardGenerator.selectTierSixWeaponClass(
                123456789L, 20, TowerBossGenerator.PESTILENCE_KNIGHT_ID);
        Class<? extends MeleeWeapon> second = TowerBossRewardGenerator.selectTierSixWeaponClass(
                123456789L, 20, TowerBossGenerator.PESTILENCE_KNIGHT_ID);

        assertEquals(first, second);
        MeleeWeapon configured = TowerBossRewardGenerator.configureReward(new TestWeapon());
        assertEquals(3, configured.trueLevel());
        assertTrue(configured.isIdentified());
        assertFalse(configured.cursed);
        assertTrue(configured.cursedKnown);
    }

    @Test
    public void rewardSeedIncludesDepthAndBossIdentity() {
        Class<? extends MeleeWeapon> baseline =
                TowerBossRewardGenerator.selectTierSixWeaponClass(7L, 5, "boss_a");
        boolean differs = false;
        for (int depth = 10; depth <= 40; depth += 5) {
            Class<? extends MeleeWeapon> candidate =
                    TowerBossRewardGenerator.selectTierSixWeaponClass(7L, depth, "boss_b");
            differs |= candidate != baseline;
        }
        assertTrue("reward inputs should affect at least one deterministic selection", differs);
    }

    public static class TestWeapon extends MeleeWeapon {
    }
}
