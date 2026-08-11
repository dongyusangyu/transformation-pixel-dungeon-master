package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TalentMagicGirlTest {

	@Test
	public void magicGirlWandDamageIsAppliedOnce() {
		assertEquals(120, Talent.magicGirlWandDamage(100, 1, true));
		assertEquals(140, Talent.magicGirlWandDamage(100, 2, true));
	}

	@Test
	public void magicGirlWandDamageRequiresDisguise() {
		assertEquals(100, Talent.magicGirlWandDamage(100, 2, false));
	}
}
