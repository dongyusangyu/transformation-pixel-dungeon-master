package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Heavy;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SharedEnchantmentSnapshotTest {

	@Test
	public void sharedEnchantmentUsesConfiguredTalentRollBoundaries() {
		assertNull(MissileWeapon.createSharedEnchantmentSnapshot(new Heavy(), 0, 0));
		assertNotNull(MissileWeapon.createSharedEnchantmentSnapshot(new Heavy(), 1, 0));
		assertNull(MissileWeapon.createSharedEnchantmentSnapshot(new Heavy(), 1, 1));
		assertNotNull(MissileWeapon.createSharedEnchantmentSnapshot(new Heavy(), 2, 1));
		assertNull(MissileWeapon.createSharedEnchantmentSnapshot(new Heavy(), 2, 2));
		assertNotNull(MissileWeapon.createSharedEnchantmentSnapshot(new Heavy(), 3, 2));
	}

	@Test
	public void sharedEnchantmentSnapshotTreatsCursesAsEffectiveEnchantments() {
		MissileWeapon.SharedEnchantmentSnapshot snapshot =
				MissileWeapon.createSharedEnchantmentSnapshot(new Heavy(), 3, 0);

		assertTrue(snapshot.matches(Heavy.class));
		assertFalse(snapshot.matches(Weapon.Enchantment.class));
	}
}
