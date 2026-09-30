package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CursedFlameDamageTest {

	@Test
	public void damageMaximumScalesByEightFloors() {
		assertEquals(3, maxBurningDamageForDepth(0));
		assertEquals(3, maxBurningDamageForDepth(7));
		assertEquals(4, maxBurningDamageForDepth(8));
		assertEquals(13, maxBurningDamageForDepth(80));
	}

	private static int maxBurningDamageForDepth(int depth) {
		try {
			return ((Number) CursedFlameDamage.class.getDeclaredMethod("maxBurningDamageForDepth", int.class)
					.invoke(null, depth)).intValue();
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Cursed flame damage must expose its floor scaling rule", e);
		}
	}
}
