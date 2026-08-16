package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.watabou.utils.Bundle;

import org.junit.Test;

public class MarshSlimeTest {

	@Test
	public void baseStatsMatchSpecification() {
		MarshSlime slime = new MarshSlime();

		assertEquals(200, slime.HT);
		assertEquals(200, slime.HP);
		assertEquals(40, slime.attackSkill(null));
		assertEquals(20, slime.defenseSkill);
		assertEquals(0, slime.drRoll());
		assertEquals(13, slime.EXP);
		assertEquals(30, slime.maxLvl);

		for (int i = 0; i < 500; i++) {
			int damage = slime.damageRoll();
			assertTrue(damage >= 15 && damage <= 25);
		}
	}

	@Test
	public void firstEffectiveMagicalHitDealsDamageThenAllMagicIsImmune() {
		TestMarshSlime slime = new TestMarshSlime();

		slime.damage(17, this, DamageTag.MAGICAL, DamageTag.FIRE);
		assertEquals(17, slime.lastAppliedDamage);
		assertEquals(183, slime.HP);
		assertEquals(MarshSlime.AdaptedDamageType.MAGICAL, slime.adaptedDamageType());

		slime.damage(31, this, DamageTag.MAGICAL, DamageTag.FROST);
		assertEquals(0, slime.lastAppliedDamage);
		assertEquals(183, slime.HP);

		slime.damage(11, this, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(11, slime.lastAppliedDamage);
		assertEquals(172, slime.HP);
	}

	@Test
	public void firstEffectivePhysicalHitMakesEveryPhysicalDeliveryImmune() {
		TestMarshSlime slime = new TestMarshSlime();

		slime.damage(9, this, DamageTag.PHYSICAL, DamageTag.RANGED);
		assertEquals(MarshSlime.AdaptedDamageType.PHYSICAL, slime.adaptedDamageType());
		assertEquals(191, slime.HP);

		slime.damage(40, this, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(0, slime.lastAppliedDamage);
		assertEquals(191, slime.HP);
	}

	@Test
	public void zeroOrFullyNegatedDamageDoesNotChooseAnImmunity() {
		TestMarshSlime slime = new TestMarshSlime();

		slime.damage(0, this, DamageTag.MAGICAL);
		assertNull(slime.adaptedDamageType());

		slime.negateNextDamage = true;
		slime.damage(20, this, DamageTag.MAGICAL);
		assertNull(slime.adaptedDamageType());

		slime.damage(8, this, DamageTag.PHYSICAL);
		assertEquals(MarshSlime.AdaptedDamageType.PHYSICAL, slime.adaptedDamageType());
	}

	@Test
	public void immunitySurvivesBundleAndUnknownValuesFailSafeToUnadapted() {
		TestMarshSlime original = new TestMarshSlime();
		original.damage(6, this, DamageTag.MAGICAL);
		Bundle saved = new Bundle();
		original.storeInBundle(saved);

		TestMarshSlime restored = new TestMarshSlime();
		restored.restoreFromBundle(saved);
		assertEquals(MarshSlime.AdaptedDamageType.MAGICAL, restored.adaptedDamageType());
		restored.damage(20, this, DamageTag.MAGICAL);
		assertEquals(0, restored.lastAppliedDamage);

		Bundle corrupt = new Bundle();
		new TestMarshSlime().storeInBundle(corrupt);
		corrupt.put("adapted_damage_type", "NOT_A_DAMAGE_TYPE");
		TestMarshSlime recovered = new TestMarshSlime();
		recovered.restoreFromBundle(corrupt);
		assertNull(recovered.adaptedDamageType());
	}

	private static class TestMarshSlime extends MarshSlime {

		private int lastAppliedDamage = -1;
		private boolean negateNextDamage;

		@Override
		protected void applyDamage(int damage, Object source, DamageTag... damageTags) {
			lastAppliedDamage = damage;
			if (negateNextDamage) {
				negateNextDamage = false;
				return;
			}
			HP = Math.max(0, HP - damage);
		}
	}
}
