package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MeleeWeaponChargerTest {

	@Test
	public void masteryCapScalesWithHeroLevel() {
		assertEquals(2, MeleeWeapon.Charger.masteryBaseCap(1));
		assertEquals(3, MeleeWeapon.Charger.masteryBaseCap(4));
		assertEquals(8, MeleeWeapon.Charger.masteryBaseCap(19));
		assertEquals(8, MeleeWeapon.Charger.masteryBaseCap(25));
	}

	@Test
	public void championAndMartialTrainingOnlyAddToBaseCap() {
		assertEquals(12, MeleeWeapon.Charger.combinedCap(25, 2, true));
		assertEquals(4, MeleeWeapon.Charger.trainingOnlyCap(3));
	}

	@Test
	public void masteryRechargeUsesMissingChargeFormula() {
		assertEquals(1f / 60f, MeleeWeapon.Charger.masteryRechargeRate(6, 6), 0.000001f);
		assertEquals(1f / 54f, MeleeWeapon.Charger.masteryRechargeRate(6, 2), 0.000001f);
	}
}
