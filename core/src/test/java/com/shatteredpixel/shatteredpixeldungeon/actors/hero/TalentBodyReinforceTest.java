package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TalentBodyReinforceTest {

	@Test
	public void plusOneRequiresAtLeastTwentyDamage() {
		assertFalse(Talent.bodyReinforceApplies(1, 19));
		assertTrue(Talent.bodyReinforceApplies(1, 20));
	}

	@Test
	public void plusTwoRequiresAtLeastTenDamage() {
		assertFalse(Talent.bodyReinforceApplies(2, 9));
		assertTrue(Talent.bodyReinforceApplies(2, 10));
	}
}
