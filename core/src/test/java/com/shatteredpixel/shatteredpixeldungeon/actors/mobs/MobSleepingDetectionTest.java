package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MobSleepingDetectionTest {

	@Test
	public void adjacentVisibleTargetAlwaysHasFullDetectionChance() {
		assertEquals(1f, Mob.sleepingDetectionChanceAtDistance(1, 0.5f, false, false), 0f);
	}

	@Test
	public void distantTargetKeepsItsCalculatedDetectionChance() {
		assertEquals(0.2f, Mob.sleepingDetectionChanceAtDistance(5, 0.2f, false, false), 0f);
	}

	@Test
	public void sleepingMobChoosesTheMostDetectableThreat() {
		assertTrue(Mob.shouldReplaceSleepingThreat(1f, 0.25f));
		assertFalse(Mob.shouldReplaceSleepingThreat(0.25f, 1f));
	}

	@Test
	public void adjacencyOverridesStealthButNotExplicitDistanceBasedExemptions() {
		assertEquals(1f, Mob.sleepingDetectionChanceAtDistance(1, 5f, false, false), 0f);
		assertEquals(0f, Mob.sleepingDetectionChanceAtDistance(2, 1f, true, false), 0f);
		assertEquals(0f, Mob.sleepingDetectionChanceAtDistance(2, 1f, false, true), 0f);
		assertEquals(1f, Mob.sleepingDetectionChanceAtDistance(1, 5f, true, true), 0f);
	}
}
