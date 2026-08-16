package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.watabou.utils.Bundle;

import org.junit.Test;

public class HeavyCrabificationTest {

	@Test
	public void baseStatsAndLootMatchSpecification() {
		TestCrab crab = new TestCrab();

		assertEquals(150, crab.HT);
		assertEquals(150, crab.HP);
		assertEquals(40, crab.attackSkill(null));
		assertEquals(20, crab.defenseSkillValue());
		assertEquals(0.5f, crab.baseSpeedValue(), 0.001f);
		assertEquals(13, crab.EXP);
		assertEquals(30, crab.maxLvl);
		assertEquals(0.5f, crab.lootChance(), 0.001f);
		assertEquals(MysteryMeat.class, crab.lootValue());

		for (int i = 0; i < 500; i++) {
			int damage = crab.damageRoll();
			int armor = crab.drRoll();
			assertTrue(damage >= 15 && damage <= 45);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void frontTargetIsBlockedButAmbushAndFlankerHaveNoEvasion() {
		TestCrab crab = new TestCrab();
		TestAttacker front = new TestAttacker();
		TestAttacker flanker = new TestAttacker();
		crab.guardAgainst(front);

		assertEquals(Char.INFINITE_EVASION, crab.defenseSkill(front));
		assertEquals(0, crab.defenseSkill(flanker));

		crab.forceAmbush = true;
		assertEquals(0, crab.defenseSkill(front));
	}

	@Test
	public void frontCharAndDirectMagicAreBlockedButOtherSourcesPass() {
		TestCrab crab = new TestCrab();
		TestAttacker front = new TestAttacker();
		TestAttacker flanker = new TestAttacker();
		crab.guardAgainst(front);

		crab.damage(11, front, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(-1, crab.acceptedDamage);

		crab.damage(11, new Object(), DamageTag.MAGICAL);
		assertEquals(-1, crab.acceptedDamage);

		crab.damage(11, new Object(), DamageTag.PHYSICAL);
		assertEquals(11, crab.acceptedDamage);

		crab.acceptedDamage = -1;
		crab.damage(11, new DisintegrationTrap(), DamageTag.MAGICAL);
		assertEquals(11, crab.acceptedDamage);

		crab.acceptedDamage = -1;
		crab.damage(11, new ToxicGas(), DamageTag.MAGICAL, DamageTag.TOXIC);
		assertEquals(11, crab.acceptedDamage);

		crab.acceptedDamage = -1;
		crab.damage(11, new Poison(), DamageTag.MAGICAL, DamageTag.POISON);
		assertEquals(11, crab.acceptedDamage);

		crab.damage(11, flanker, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(6, crab.acceptedDamage);

		crab.acceptedDamage = -1;
		crab.forceAmbush = true;
		crab.damage(1, front, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(1, crab.acceptedDamage);
	}

	@Test
	public void corruptionPeriodicDamageBypassesShellAtFullValue() {
		TestCrab crab = new TestCrab();
		TestAttacker front = new TestAttacker();
		crab.guardAgainst(front);

		crab.damage(2, new Corruption(), DamageTag.PHYSICAL, DamageTag.CORRUPTION);

		assertEquals(2, crab.acceptedDamage);
	}

	@Test
	public void secondAttackerFlanksEvenBeforeTheCurrentTargetIsSeen() {
		TestCrab crab = new TestCrab();
		TestAttacker front = new TestAttacker();
		TestAttacker flanker = new TestAttacker();
		crab.trackWithoutSeeing(front);

		assertEquals(0, crab.defenseSkill(flanker));
		crab.damage(20, flanker, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(10, crab.acceptedDamage);
	}

	@Test
	public void fiftiethOutOfCombatActionMoltsAndHealsThirty() {
		TestCrab crab = new TestCrab();
		crab.HP = 100;
		crab.state = crab.WANDERING;

		for (int i = 0; i < 49; i++) {
			assertTrue(crab.actForTest());
		}

		assertEquals(49, crab.moltTurnsForTest());
		assertEquals(100, crab.HP);
		assertEquals(49, crab.baseActCalls);
		assertEquals(49f, crab.cooldown(), 0.001f);

		assertTrue(crab.actForTest());

		assertEquals(0, crab.moltTurnsForTest());
		assertEquals(130, crab.HP);
		assertEquals(49, crab.baseActCalls);
		assertEquals(50f, crab.cooldown(), 0.001f);
	}

	@Test
	public void moltHealingNeverExceedsMaximumHealth() {
		TestCrab crab = new TestCrab();
		crab.HP = 140;
		crab.state = crab.WANDERING;
		crab.setMoltTurnsForTest(49);

		crab.actForTest();

		assertEquals(150, crab.HP);
		assertEquals(0, crab.moltTurnsForTest());
	}

	@Test
	public void slowTwoTickActionsStillMoltAfterFiftyWorldTurns() {
		TestCrab crab = new TestCrab();
		crab.HP = 100;
		crab.state = crab.WANDERING;
		crab.elapsedPerAct = 2f;

		for (int i = 0; i < 24; i++) {
			crab.actForTest();
		}
		assertEquals(48, crab.moltTurnsForTest());
		assertEquals(100, crab.HP);

		crab.actForTest();
		assertEquals(0, crab.moltTurnsForTest());
		assertEquals(130, crab.HP);
		assertEquals(24, crab.baseActCalls);
	}

	@Test
	public void combatResetsProgressWhileSleepAndParalysisPauseIt() {
		TestCrab crab = new TestCrab();
		crab.state = crab.WANDERING;
		crab.setMoltTurnsForTest(12);
		crab.paralysed = 1;
		crab.actForTest();
		assertEquals(12, crab.moltTurnsForTest());

		crab.paralysed = 0;
		crab.state = crab.SLEEPING;
		crab.actForTest();
		assertEquals(12, crab.moltTurnsForTest());

		crab.state = crab.HUNTING;
		crab.actForTest();
		assertEquals(0, crab.moltTurnsForTest());

		crab.state = crab.WANDERING;
		crab.setMoltTurnsForTest(7);
		crab.enemySeenForTest = true;
		crab.actForTest();
		assertEquals(0, crab.moltTurnsForTest());
	}

	@Test
	public void moltProgressSurvivesBundleAndIsClamped() {
		TestCrab original = new TestCrab();
		original.setMoltTurnsForTest(37);
		Bundle saved = new Bundle();
		original.storeInBundle(saved);

		TestCrab restored = new TestCrab();
		restored.restoreFromBundle(saved);
		assertEquals(37, restored.moltTurnsForTest());

		Bundle negative = new Bundle();
		negative.put("molt_turns", -5);
		TestCrab restoredNegative = new TestCrab();
		restoredNegative.restoreFromBundle(negative);
		assertEquals(0, restoredNegative.moltTurnsForTest());

		Bundle excessive = new Bundle();
		excessive.put("molt_turns", 500);
		TestCrab restoredExcessive = new TestCrab();
		restoredExcessive.restoreFromBundle(excessive);
		assertEquals(49, restoredExcessive.moltTurnsForTest());
	}

	private static class TestCrab extends HeavyCrabification {

		private int acceptedDamage = -1;
		private int baseActCalls;
		private boolean forceAmbush;
		private boolean enemySeenForTest;
		private float elapsedPerAct = 1f;

		private void guardAgainst(Char target) {
			enemy = target;
			enemySeen = true;
			state = HUNTING;
		}

		private void trackWithoutSeeing(Char target) {
			enemy = target;
			enemySeen = false;
			state = HUNTING;
		}

		private int defenseSkillValue() {
			return defenseSkill;
		}

		private float baseSpeedValue() {
			return baseSpeed;
		}

		private Object lootValue() {
			return loot;
		}

		private boolean actForTest() {
			enemySeen = enemySeenForTest;
			return act();
		}

		private int moltTurnsForTest() {
			return moltTurns();
		}

		private void setMoltTurnsForTest(int turns) {
			setMoltTurns(turns);
		}

		@Override
		protected boolean isAmbushAttack(Char attacker) {
			return forceAmbush;
		}

		@Override
		protected void applyAllowedDamage(int damage, Object source, DamageTag... damageTags) {
			acceptedDamage = damage;
		}

		@Override
		protected void showShellBlock() {
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			return baseChance;
		}

		@Override
		protected boolean performBaseAct() {
			baseActCalls++;
			spend(TICK);
			return true;
		}

		@Override
		protected boolean canAdvanceMolt() {
			return state == WANDERING && paralysed == 0 && !enemySeenForTest;
		}

		@Override
		protected float elapsedSinceLastAct() {
			return elapsedPerAct;
		}
	}

	private static class TestAttacker extends Gnoll {
	}
}
