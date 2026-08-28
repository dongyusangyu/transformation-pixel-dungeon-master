package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DeathButterfly;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DeathCurseTest {

	@Test
	public void attachesForTenTurnsAndRemembersItsSource() {
		DeathButterfly source = new DeathButterfly();
		Gnoll target = new Gnoll();

		DeathCurse curse = DeathCurse.apply(target, source);

		assertSame(curse, target.buff(DeathCurse.class));
		assertEquals(source.id(), curse.sourceId());
		assertEquals(DeathCurse.DURATION, curse.remaining(), 0f);
		assertEquals(1f, curse.cooldown(), 0f);
		assertEquals(Buff.buffType.NEGATIVE, curse.type);
	}

	@Test
	public void doesNotStackOrRefreshAnExistingCurse() {
		DeathButterfly firstSource = new DeathButterfly();
		DeathButterfly secondSource = new DeathButterfly();
		Gnoll target = new Gnoll();
		DeathCurse original = DeathCurse.apply(target, firstSource);
		float originalCooldown = original.cooldown();

		DeathCurse duplicate = DeathCurse.apply(target, secondSource);

		assertNull(duplicate);
		assertSame(original, target.buff(DeathCurse.class));
		assertEquals(firstSource.id(), original.sourceId());
		assertEquals(originalCooldown, original.cooldown(), 0f);
	}

	@Test
	public void bossCannotReceiveDeathCurse() {
		DeathButterfly source = new DeathButterfly();
		Gnoll target = new Gnoll();
		target.addProperties(Char.Property.BOSS);

		assertNull(DeathCurse.apply(target, source));
		assertNull(target.buff(DeathCurse.class));
	}

	@Test
	public void minibossCannotReceiveDeathCurse() {
		DeathButterfly source = new DeathButterfly();
		Gnoll target = new Gnoll();
		target.addProperties(Char.Property.MINIBOSS);

		assertNull(DeathCurse.apply(target, source));
		assertNull(target.buff(DeathCurse.class));
	}

	@Test
	public void ordinaryDetachIsAlwaysSafe() {
		TestTarget target = new TestTarget();
		DeathCurse curse = DeathCurse.apply(target, new DeathButterfly());

		curse.detach();

		assertEquals(target.HT, target.HP);
		assertFalse(target.died);
		assertNull(target.buff(DeathCurse.class));
	}

	@Test
	public void orphanedCurseDetachesSafelyWhenItsTurnArrives() {
		TestTarget target = new TestTarget();
		DeathCurse curse = DeathCurse.apply(target, new DeathButterfly());

		curse.act();

		assertEquals(target.HT, target.HP);
		assertFalse(target.died);
		assertNull(target.buff(DeathCurse.class));
	}

	@Test
	public void liveSourceIsRecheckedEveryTurnWhileCountdownDecreases() {
		DeathButterfly source = new DeathButterfly();
		TestTarget target = new TestTarget();
		Actor.add(source);
		try {
			DeathCurse curse = DeathCurse.apply(target, source);

			curse.act();

			assertSame(curse, target.buff(DeathCurse.class));
			assertEquals(9f, curse.remaining(), 0f);

			Actor.remove(source);
			curse.act();
			assertNull(target.buff(DeathCurse.class));
			assertFalse(target.died);
		} finally {
			Actor.remove(source);
		}
	}

	@Test
	public void naturalExpiryKillsNonHeroThroughNormalDeathEntryPoint() {
		TestTarget target = new TestTarget();
		DeathCurse curse = DeathCurse.apply(target, new DeathButterfly());

		curse.expireNaturally();

		assertEquals(0, target.HP);
		assertTrue(target.died);
		assertSame(curse, target.deathCause);
		assertNull(target.buff(DeathCurse.class));
	}

	@Test
	public void targetBecomingBossBeforeExpiryIsReleasedWithoutDeath() {
		TestTarget target = new TestTarget();
		DeathCurse curse = DeathCurse.apply(target, new DeathButterfly());
		target.addProperties(Char.Property.BOSS);

		curse.expireNaturally();

		assertEquals(target.HT, target.HP);
		assertFalse(target.died);
		assertNull(target.buff(DeathCurse.class));
	}

	@Test
	public void sourceIdentityAndRemainingTimeSurviveBundleRoundTrip() {
		DeathButterfly source = new DeathButterfly();
		Gnoll target = new Gnoll();
		DeathCurse original = DeathCurse.apply(target, source);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		DeathCurse restored = new DeathCurse();
		restored.restoreFromBundle(bundle);

		assertEquals(source.id(), restored.sourceId());
		assertEquals(original.remaining(), restored.remaining(), 0f);
	}

	private static final class TestTarget extends Gnoll {
		private boolean died;
		private Object deathCause;

		private TestTarget() {
			HT = HP = 50;
		}

		@Override
		public void die(Object cause) {
			died = true;
			deathCause = cause;
		}
	}
}
