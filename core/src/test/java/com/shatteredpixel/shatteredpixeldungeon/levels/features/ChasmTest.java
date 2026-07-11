package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChasmTest {

	@Test
	public void featherFallDoesNotPreventPotentialEnergyTrigger() {
		assertTrue(Chasm.shouldApplyPotentialEnergy(true, true));
	}

	@Test
	public void landingWithoutTalentDoesNotGrantHaste() {
		assertFalse(Chasm.shouldApplyPotentialEnergy(true, false));
	}
}
