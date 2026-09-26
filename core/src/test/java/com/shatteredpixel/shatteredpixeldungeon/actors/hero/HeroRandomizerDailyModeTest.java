package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HeroRandomizerDailyModeTest {

	@Test
	public void randomModePreferenceAppliesToNormalRunsButNotDailyRuns() {
		assertTrue(HeroRandomizer.randomModeForRun(true, false, HeroClass.WARRIOR));
		assertFalse(HeroRandomizer.randomModeForRun(true, true, HeroClass.WARRIOR));
		assertFalse(HeroRandomizer.randomModeForRun(false, false, HeroClass.WARRIOR));
		assertFalse(HeroRandomizer.randomModeForRun(true, false, HeroClass.RATKING));
	}
}
