package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScrollOfTransmutationTierTest {

	@Test
	public void tierSixEquipmentTransmutesUsingTierFivePools() {
		assertEquals(Generator.Category.WEP_T5,
				Generator.weaponTransmutationCategory(false, 6));
		assertEquals(Generator.Category.MIS_T5,
				Generator.weaponTransmutationCategory(true, 6));
	}

	@Test
	public void tiersOneThroughFiveKeepTheirOriginalPools() {
		for (int tier = 1; tier <= 5; tier++) {
			assertEquals(Generator.wepTiers[tier - 1],
					Generator.weaponTransmutationCategory(false, tier));
			assertEquals(Generator.misTiers[tier - 1],
					Generator.weaponTransmutationCategory(true, tier));
		}
	}
}
