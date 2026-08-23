package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TalentAttackProcContextTest {

	@Test
	public void actualMeleeAttackAllowsBothMeleeChannels() {
		MeleeWeapon weapon = new TestMeleeWeapon();
		Talent.AttackProcContext context = Talent.AttackProcContext.create(
				weapon, DamageTag.Delivery.MELEE, false, false);

		assertTrue(context.allows(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertTrue(context.allows(Talent.AttackProcChannel.MELEE_SPECIAL));
		assertTrue(context.isActualMeleeAttack());
		assertTrue(context.isActualMeleeWeapon());
		assertSame(weapon, context.weapon());
	}

	@Test
	public void rangedAttackCanInheritOnlyMeleeDamage() {
		MissileWeapon weapon = new TestMissileWeapon();
		Talent.AttackProcContext context = Talent.AttackProcContext.create(
				weapon, DamageTag.Delivery.RANGED, true, false);

		assertTrue(context.allows(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertFalse(context.allows(Talent.AttackProcChannel.MELEE_SPECIAL));
		assertTrue(context.inherits(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertFalse(context.inherits(Talent.AttackProcChannel.MELEE_SPECIAL));
		assertFalse(context.isActualMeleeAttack());
		assertFalse(context.isActualMeleeWeapon());
		assertSame(weapon, context.weapon());
	}

	@Test
	public void rangedAttackCanInheritBothMeleeChannels() {
		Talent.AttackProcContext context = Talent.AttackProcContext.create(
				new TestMissileWeapon(), DamageTag.Delivery.RANGED, true, true);

		assertTrue(context.allows(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertTrue(context.allows(Talent.AttackProcChannel.MELEE_SPECIAL));
		assertTrue(context.inherits(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertTrue(context.inherits(Talent.AttackProcChannel.MELEE_SPECIAL));
	}

	@Test
	public void inheritedMeleeSpecialAlsoEnablesMeleeDamage() {
		Talent.AttackProcContext context = Talent.AttackProcContext.create(
				new TestMissileWeapon(), DamageTag.Delivery.RANGED, false, true);

		assertTrue(context.allows(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertTrue(context.allows(Talent.AttackProcChannel.MELEE_SPECIAL));
	}

	@Test
	public void rangedAttackWithoutInheritanceAllowsNeitherMeleeChannel() {
		Talent.AttackProcContext context = Talent.AttackProcContext.create(
				new TestMissileWeapon(), DamageTag.Delivery.RANGED, false, false);

		assertFalse(context.allows(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertFalse(context.allows(Talent.AttackProcChannel.MELEE_SPECIAL));
	}

	@Test
	public void magicalAttackIsNotInferredAsUnarmedMelee() {
		Talent.AttackProcContext context = Talent.AttackProcContext.forAttack(
				null, false, false, DamageTag.MAGICAL);

		assertSame(DamageTag.Delivery.NONE, context.delivery());
		assertFalse(context.allows(Talent.AttackProcChannel.MELEE_DAMAGE));
		assertFalse(context.allows(Talent.AttackProcChannel.MELEE_SPECIAL));
	}

	private static class TestMeleeWeapon extends MeleeWeapon {
		@Override
		public int min(int lvl) {
			return 1;
		}

		@Override
		public int max(int lvl) {
			return 1;
		}
	}

	private static class TestMissileWeapon extends MissileWeapon {
		@Override
		public int min(int lvl) {
			return 1;
		}

		@Override
		public int max(int lvl) {
			return 1;
		}
	}
}
