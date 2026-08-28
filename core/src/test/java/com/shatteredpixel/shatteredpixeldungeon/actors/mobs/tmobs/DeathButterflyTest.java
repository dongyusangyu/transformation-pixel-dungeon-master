package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DeathCurse;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DeathButterflyTest {

	@Test
	public void usesConfirmedTowerStats() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();

		assertEquals(120, butterfly.HT);
		assertEquals(120, butterfly.HP);
		assertEquals(30, butterfly.defenseSkill(target));
		assertEquals(40, butterfly.attackSkill(target));
		assertEquals(13, butterfly.EXP);
		assertEquals(30, butterfly.maxLvl);
		assertTrue(butterfly.flying);
		assertTrue(butterfly.properties().contains(Char.Property.UNDEAD));
		for (int i = 0; i < 500; i++) {
			int damage = butterfly.damageRoll();
			int armor = butterfly.armorRollForTest();
			assertTrue(damage >= 10 && damage <= 20);
			assertTrue(armor >= 0 && armor <= 15);
		}
	}

	@Test
	public void dropsAHealingPotionAtOneEighthBaseChance() {
		TestButterfly butterfly = new TestButterfly(true);

		assertSame(PotionOfHealing.class, butterfly.lootClassForTest());
		assertEquals(1f / 8f, butterfly.baseLootChanceForTest(), 0f);
	}

	@Test
	public void fiftyPercentBoundaryIsExact() {
		assertTrue(DeathButterfly.curseRollSucceeds(0f));
		assertTrue(DeathButterfly.curseRollSucceeds(0.499999f));
		assertFalse(DeathButterfly.curseRollSucceeds(0.5f));
		assertFalse(DeathButterfly.curseRollSucceeds(0.999999f));
	}

	@Test
	public void successfulMeleeHitCursesOnceAndStartsFleeing() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();

		butterfly.curseTargetForTest(target);

		DeathCurse curse = target.buff(DeathCurse.class);
		assertNotNull(curse);
		assertEquals(butterfly.id(), curse.sourceId());
		assertSame(butterfly.FLEEING, butterfly.state);
		assertTrue(butterfly.isFleeingFromForTest(target));
	}

	@Test
	public void failedRollDoesNotCurseOrFlee() {
		TestButterfly butterfly = new TestButterfly(false);
		Gnoll target = new Gnoll();

		butterfly.curseTargetForTest(target);

		assertNull(target.buff(DeathCurse.class));
		assertFalse(butterfly.state == butterfly.FLEEING);
	}

	@Test
	public void bossIsNotCursedAndDoesNotMakeButterflyFlee() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();
		target.addProperties(Char.Property.BOSS);

		butterfly.curseTargetForTest(target);

		assertNull(target.buff(DeathCurse.class));
		assertFalse(butterfly.state == butterfly.FLEEING);
		assertEquals(-1, butterfly.cursedTargetIdForTest());
	}

	@Test
	public void minibossIsNotCursedAndDoesNotMakeButterflyFlee() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();
		target.addProperties(Char.Property.MINIBOSS);

		butterfly.curseTargetForTest(target);

		assertNull(target.buff(DeathCurse.class));
		assertFalse(butterfly.state == butterfly.FLEEING);
		assertEquals(-1, butterfly.cursedTargetIdForTest());
	}

	@Test
	public void existingCurseCannotBeStolenOrRefreshedByAnotherButterfly() {
		TestButterfly first = new TestButterfly(true);
		TestButterfly second = new TestButterfly(true);
		Gnoll target = new Gnoll();
		first.curseTargetForTest(target);
		DeathCurse original = target.buff(DeathCurse.class);
		float remaining = original.remaining();

		second.curseTargetForTest(target);

		assertSame(original, target.buff(DeathCurse.class));
		assertEquals(first.id(), original.sourceId());
		assertEquals(remaining, original.remaining(), 0f);
		assertFalse(second.state == second.FLEEING);
	}

	@Test
	public void removingOwnedCurseReturnsButterflyToHunting() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();
		butterfly.curseTargetForTest(target);
		target.buff(DeathCurse.class).detach();

		butterfly.refreshFleeingStateForTest();

		assertSame(butterfly.HUNTING, butterfly.state);
		assertFalse(butterfly.isFleeingFromForTest(target));
	}

	@Test
	public void huntingStateReturnsToFleeingWhileOwnedCurseRemains() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();
		butterfly.curseTargetForTest(target);
		butterfly.state = butterfly.HUNTING;

		butterfly.reconcileBeforeActionForTest();

		assertSame(butterfly.FLEEING, butterfly.state);
	}

	@Test
	public void corneredButterflyGetsOneHuntingActionBeforeFleeingAgain() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();
		butterfly.curseTargetForTest(target);
		assertTrue(butterfly.markCorneredForTest());

		boolean counterattacking = butterfly.reconcileBeforeActionForTest();
		assertTrue(counterattacking);
		assertSame(butterfly.HUNTING, butterfly.state);

		butterfly.finishBoundActionForTest(counterattacking);
		assertSame(butterfly.FLEEING, butterfly.state);
	}

	@Test
	public void sameBlockedPositionCannotTriggerRepeatedCounterattacks() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll target = new Gnoll();
		butterfly.pos = 17;
		butterfly.curseTargetForTest(target);

		assertTrue(butterfly.markCorneredForTest());
		butterfly.finishBoundActionForTest(true);

		assertFalse(butterfly.markCorneredForTest());
		assertSame(butterfly.FLEEING, butterfly.state);

		butterfly.pos = 18;
		butterfly.reconcileBeforeActionForTest();
		assertTrue(butterfly.markCorneredForTest());
	}

	@Test
	public void butterflyWithAnOwnedCurseCannotCurseASecondTarget() {
		TestButterfly butterfly = new TestButterfly(true);
		Gnoll firstTarget = new Gnoll();
		Gnoll secondTarget = new Gnoll();
		butterfly.curseTargetForTest(firstTarget);

		butterfly.curseTargetForTest(secondTarget);

		assertNotNull(firstTarget.buff(DeathCurse.class));
		assertNull(secondTarget.buff(DeathCurse.class));
		assertEquals(firstTarget.id(), butterfly.cursedTargetIdForTest());
	}

	@Test
	public void casterCleanupRemovesOnlyItsOwnCurse() {
		TestButterfly first = new TestButterfly(true);
		TestButterfly second = new TestButterfly(true);
		Gnoll firstTarget = new Gnoll();
		Gnoll secondTarget = new Gnoll();
		first.curseTargetForTest(firstTarget);
		second.curseTargetForTest(secondTarget);

		first.clearOwnedCursesForTest();

		assertNull(firstTarget.buff(DeathCurse.class));
		assertNotNull(secondTarget.buff(DeathCurse.class));
		assertEquals(second.id(), secondTarget.buff(DeathCurse.class).sourceId());
	}

	@Test
	public void cursedTargetIdentitySurvivesSaveAndLoad() {
		TestButterfly original = new TestButterfly(true);
		Gnoll target = new Gnoll();
		original.curseTargetForTest(target);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TestButterfly restored = new TestButterfly(true);
		restored.restoreFromBundle(bundle);

		assertEquals(target.id(), restored.cursedTargetIdForTest());
	}

	private static final class TestButterfly extends DeathButterfly {
		private final boolean curseRoll;

		private TestButterfly(boolean curseRoll) {
			this.curseRoll = curseRoll;
		}

		@Override
		protected boolean rollCurse() {
			return curseRoll;
		}

		private int armorRollForTest() {
			return drRoll();
		}

		private Class<?> lootClassForTest() {
			return (Class<?>) loot;
		}

		private float baseLootChanceForTest() {
			return lootChance;
		}

		private void curseTargetForTest(Char target) {
			processCurseOnHit(target);
		}

		private boolean isFleeingFromForTest(Char target) {
			return isFleeingFrom(target);
		}

		private void refreshFleeingStateForTest() {
			refreshFleeingState();
		}

		private int cursedTargetIdForTest() {
			return cursedTargetId();
		}

		private void clearOwnedCursesForTest() {
			clearOwnedCurses();
		}

		private boolean reconcileBeforeActionForTest() {
			return reconcileCurseBeforeAction();
		}

		private boolean markCorneredForTest() {
			return markCorneredCounterattack();
		}

		private void finishBoundActionForTest(boolean counterattacking) {
			finishCurseBoundAction(counterattacking);
		}
	}
}
