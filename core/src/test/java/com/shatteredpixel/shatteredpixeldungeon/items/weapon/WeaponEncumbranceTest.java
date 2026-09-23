package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeaponEncumbranceTest {

	@Test
	public void reductionsClampEffectiveEncumbranceAtZero() {
		assertEquals(3, Weapon.adjustedEncumbrance(22, 19, 0));
		assertEquals(1, Weapon.adjustedEncumbrance(22, 19, 2));
		assertEquals(0, Weapon.adjustedEncumbrance(22, 19, 99));
		assertEquals(0, Weapon.adjustedEncumbrance(19, 22, 0));
	}

	@Test
	public void falsehoodPowerUsesTheCommonReduction() {
		int noTalent = Weapon.adjustedEncumbrance(22, 19, 0);
		int ordinaryTalent = Weapon.adjustedEncumbrance(22, 18, 3);
		assertEquals(2, noTalent - ordinaryTalent);
	}
}
