package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LakeSwordStrengthRequirementTest {

	@Test
	public void masteryPotionReducesLakeSwordStrengthRequirement() {
		LakeSword sword = new LakeSword();
		int normalRequirement = sword.STRReq(0);

		sword.masteryPotionBonus = true;

		assertEquals(normalRequirement - 2, sword.STRReq(0));
	}
}
