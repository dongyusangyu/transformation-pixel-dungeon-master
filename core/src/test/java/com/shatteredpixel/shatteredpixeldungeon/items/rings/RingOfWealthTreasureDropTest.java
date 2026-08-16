package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RingOfWealthTreasureDropTest {

	@Test
	public void treasureRingPoolNeverReturnsAnotherRingOfWealth() {
		for (int i = 0; i < 500; i++) {
			Class<?> result = Generator.randomClassUsingDefaultsExcluding(
					Generator.Category.RING, RingOfWealth.class);
			assertTrue(Ring.class.isAssignableFrom(result));
			assertFalse(RingOfWealth.class.isAssignableFrom(result));
		}
	}

	@Test
	public void equipmentDropLevelNeverExceedsTen() {
		assertEquals(10, RingOfWealth.EquipmentDropLevelPolicy.level(0, 100));
		assertEquals(10, RingOfWealth.EquipmentDropLevelPolicy.level(12, 1));
		assertEquals(4, RingOfWealth.EquipmentDropLevelPolicy.level(0, 7));
	}
}
