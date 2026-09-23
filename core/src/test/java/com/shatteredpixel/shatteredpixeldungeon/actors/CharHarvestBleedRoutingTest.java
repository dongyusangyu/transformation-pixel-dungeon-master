package com.shatteredpixel.shatteredpixeldungeon.actors;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharHarvestBleedRoutingTest {

	@Test
	public void onlyMeleeDeliveryConsumesHarvestBleedTracker() {
		assertTrue(Char.shouldConsumeHarvestBleedTracker(DamageTag.PHYSICAL, DamageTag.MELEE));
		assertTrue(Char.shouldConsumeHarvestBleedTracker(DamageTag.MAGICAL, DamageTag.MELEE));
		assertFalse(Char.shouldConsumeHarvestBleedTracker(DamageTag.MAGICAL));
		assertFalse(Char.shouldConsumeHarvestBleedTracker(DamageTag.PHYSICAL, DamageTag.RANGED));
	}
}
