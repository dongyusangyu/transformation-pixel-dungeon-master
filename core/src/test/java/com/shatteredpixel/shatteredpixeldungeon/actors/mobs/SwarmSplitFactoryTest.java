package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SwarmSplitFactoryTest {

	@Test
	public void ordinarySwarmFactoryStillCreatesOrdinarySwarm() {
		assertEquals(Swarm.class, new TestSwarm().makeSplit().getClass());
	}

	@Test
	public void ordinarySwarmKeepsItsOriginalSplitThreshold() {
		TestSwarm swarm = new TestSwarm();

		swarm.HP = 10;
		assertTrue(swarm.maySplit(8));
		assertFalse(swarm.maySplit(9));
	}

	private static class TestSwarm extends Swarm {

		private Swarm makeSplit() {
			return createSplit();
		}

		private boolean maySplit(int damage) {
			return canSplit(damage);
		}
	}
}
