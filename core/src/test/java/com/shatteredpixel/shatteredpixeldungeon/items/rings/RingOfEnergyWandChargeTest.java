package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RingOfEnergyWandChargeTest {

	private static final float EPSILON = 0.0001f;

	@Test
	public void energyConversionAcceleratesNaturalWandCharging() {
		assertEquals(1f, RingOfEnergy.EnergyConversion.multiplier(3f, 0), EPSILON);
		assertEquals(1.5f, RingOfEnergy.EnergyConversion.multiplier(3f, 1), EPSILON);
		assertEquals(3f, RingOfEnergy.EnergyConversion.multiplier(3f, 2), EPSILON);
	}

	@Test
	public void energyConversionNeverSlowsNaturalWandCharging() {
		assertEquals(1f, RingOfEnergy.EnergyConversion.multiplier(1f, 1), EPSILON);
		assertEquals(1f, RingOfEnergy.EnergyConversion.multiplier(0.5f, 2), EPSILON);
	}
}
