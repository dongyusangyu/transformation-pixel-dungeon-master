package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MissileWeaponDurabilityWarningTest {

    @Test
    public void warningUsesAreLimitedToThreeWithoutWarningFreshFragileStacks() {
        assertEquals(3, MissileWeapon.durabilityWarningThreshold(10f));
        assertEquals(2, MissileWeapon.durabilityWarningThreshold(34f));
        assertEquals(1, MissileWeapon.durabilityWarningThreshold(50f));
        assertEquals(0, MissileWeapon.durabilityWarningThreshold(100f));
        assertEquals(0, MissileWeapon.durabilityWarningThreshold(0f));
    }

    @Test
    public void warningUsesFollowTheActualDurabilityCost() {
        assertEquals(4, MissileWeapon.remainingDurabilityUses(40f, 10f));
        assertEquals(3, MissileWeapon.remainingDurabilityUses(30f, 10f));
        assertTrue(MissileWeapon.isNearBreaking(30f, 10f));
        assertFalse(MissileWeapon.isNearBreaking(40f, 10f));
        assertFalse(MissileWeapon.isNearBreaking(0f, 10f));
        assertFalse(MissileWeapon.isNearBreaking(100f, 100f));
        assertEquals(Integer.MAX_VALUE, MissileWeapon.remainingDurabilityUses(100f, 0f));
    }

    @Test
    public void warningIsEmittedOnlyWhenCrossingIntoWarningRange() {
        assertTrue(MissileWeapon.crossedDurabilityWarning(40f, 30f, 10f));
        assertFalse(MissileWeapon.crossedDurabilityWarning(30f, 20f, 10f));
        assertFalse(MissileWeapon.crossedDurabilityWarning(20f, 10f, 10f));
        assertFalse(MissileWeapon.crossedDurabilityWarning(100f, 0f, 100f));
    }
}
