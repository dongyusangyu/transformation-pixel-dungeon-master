package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TalentAfraidDeathTest {

	@Test
	public void afraidDeathKeepsPhysicalDamageAboveZeroAtEveryTalentLevel() {
		assertEquals(0.7f, Talent.afraidDeathDamageMultiplier(1), 0f);
		assertEquals(0.6f, Talent.afraidDeathDamageMultiplier(2), 0f);
		assertEquals(0.5f, Talent.afraidDeathDamageMultiplier(3), 0f);
	}
}
