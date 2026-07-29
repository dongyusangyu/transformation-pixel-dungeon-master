package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AgentMinMetamorphPlannerTest {

	@Test
	public void huntressSublimationTargetsMatchDm300() {
		assertEquals(
				AgentMinMetamorphPlanner.sublimationTargets("DM300"),
				AgentMinMetamorphPlanner.sublimationTargets("HUNTRESS"));
		assertEquals(
				Arrays.asList(Talent.FASTING, Talent.THUNDER_STRIKE, Talent.DIRECTIONAL_COLLAPSE),
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
