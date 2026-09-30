package com.shatteredpixel.shatteredpixeldungeon.items;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LiquidMetalBoundaryTest {

	@Test
	public void replenishingAmmoDetachesMetalWhenRoundedCostUsesEntireStack() {
		assertFalse(LiquidMetal.hasMetalLeftAfterReplenishing(10.2f, 11));
		assertTrue(LiquidMetal.hasMetalLeftAfterReplenishing(10.2f, 12));
		assertTrue(LiquidMetal.hasMetalLeftAfterReplenishing(10f, 11));
	}
}
