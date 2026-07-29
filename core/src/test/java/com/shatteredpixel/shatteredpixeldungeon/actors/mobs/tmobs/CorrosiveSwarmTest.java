package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CorrosiveSwarmTest {

	@Test
	public void usesApprovedTowerBaselineWithAdjustedHealthAndDamage() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();
		Gnoll attacker = new Gnoll();

		assertEquals(200, swarm.HT);
		assertEquals(200, swarm.HP);
		assertEquals(40, swarm.attackSkill(null));
		assertEquals(20, swarm.defenseSkill(attacker));
		assertEquals(1f, swarm.speed(), 0f);
		assertEquals(1f, swarm.attackDelay(), 0f);
		assertEquals(13, swarm.EXP);
		assertEquals(26, swarm.maxLvl);

		for (int i = 0; i < 500; i++) {
			int damage = swarm.damageRoll();
			int armor = swarm.rollArmor();
			assertTrue(damage >= 15 && damage <= 25);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void splitFactoryKeepsCorrosiveSubtype() {
		assertTrue(new TestCorrosiveSwarm().makeSplit() instanceof CorrosiveSwarm);
	}

	@Test
	public void onlySplitsAboveTenHp() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();

		swarm.HP = 11;
		assertTrue(swarm.maySplit(1));

		swarm.HP = 10;
		assertFalse(swarm.maySplit(1));

		swarm.HP = 9;
		assertFalse(swarm.maySplit(1));
	}

	@Test
	public void corrosionStartsAtOneDamageForTwoTurnsAndStacksDuration() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();
		Gnoll target = new Gnoll();

		swarm.corroded(target);
		Corrosion corrosion = target.buff(Corrosion.class);
		assertNotNull(corrosion);
		assertEquals(1f, stored(corrosion, "damage"), 0f);
		assertEquals(2f, stored(corrosion, "left"), 0f);

		swarm.corroded(target);
		assertSame(corrosion, target.buff(Corrosion.class));
		assertEquals(1f, stored(corrosion, "damage"), 0f);
		assertEquals(4f, stored(corrosion, "left"), 0f);
	}

	@Test
	public void positiveDamageStartsChainAndOnlyChainDamageAcceptsZero() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();

		CorrosiveSwarm.BurstChain initial = swarm.chainFor(1, new Object());
		assertNotNull(initial);
		assertSame(initial, swarm.chainFor(0, initial));
		assertNull(swarm.chainFor(0, new Object()));
	}

	@Test
	public void eachSwarmEntersOneBurstChainOnlyOnce() {
		CorrosiveSwarm.BurstChain chain = new CorrosiveSwarm.BurstChain();
		CorrosiveSwarm first = new CorrosiveSwarm();
		CorrosiveSwarm second = new CorrosiveSwarm();

		assertTrue(chain.enter(first));
		assertFalse(chain.enter(first));
		assertTrue(chain.enter(second));
	}

	private static float stored(Corrosion corrosion, String key) {
		Bundle bundle = new Bundle();
		corrosion.storeInBundle(bundle);
		return bundle.getFloat(key);
	}

	private static class TestCorrosiveSwarm extends CorrosiveSwarm {

		private int rollArmor() {
			return super.armorRoll();
		}

		private Swarm makeSplit() {
			return createSplit();
		}

		private boolean maySplit(int damage) {
			return canSplit(damage);
		}

		private void corroded(Gnoll target) {
			applyCorrosion(target);
		}

		private BurstChain chainFor(int damage, Object source) {
			return burstChainFor(damage, source);
		}
	}
}
