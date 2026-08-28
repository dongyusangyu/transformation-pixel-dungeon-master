package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AgentMinMetamorphPlannerTest {

	@Test
	public void huntressSublimationTargetsIncludeHuntingTechnique() {
		assertEquals(3, AgentMinMetamorphPlanner.sublimationTargets("DM300").size());
		assertEquals(
				Arrays.asList(Talent.HUNTING_TECHNIQUE, Talent.NATURAL_CHILD, Talent.FALCON_EYE),
				AgentMinMetamorphPlanner.sublimationTargets("HUNTRESS"));
	}

	@Test
	public void huntressSublimationTierAndIndexMatchDm300() {
		assertEquals(
				AgentMinMetamorphPlanner.sublimationTier("DM300"),
				AgentMinMetamorphPlanner.sublimationTier("HUNTRESS"));
		assertEquals(2, AgentMinMetamorphPlanner.sublimationTier("HUNTRESS"));
		assertEquals(
				AgentMinMetamorphPlanner.sublimationIndex("DM300"),
				AgentMinMetamorphPlanner.sublimationIndex("HUNTRESS"));
		assertEquals(2, AgentMinMetamorphPlanner.sublimationIndex("HUNTRESS"));
	}
}
