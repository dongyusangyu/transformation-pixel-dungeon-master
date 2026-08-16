package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

public class SpiritBowNaturePowerTest {

	private static final Class<? extends Plant>[] EXPECTED_POOL = plantClasses(
			Blindweed.class,
			Firebloom.class,
			Icecap.class,
			Sorrowmoss.class,
			Stormvine.class);

	@Test
	public void harmfulPlantPoolHasStableOrderAndReturnsDefensiveCopy() {
		Class<? extends Plant>[] first = SpiritBow.harmfulPlantPool();
		Class<? extends Plant>[] second = SpiritBow.harmfulPlantPool();

		assertArrayEquals(EXPECTED_POOL, first);
		assertArrayEquals(EXPECTED_POOL, second);
		assertNotSame(first, second);

		first[0] = Stormvine.class;
		assertArrayEquals(EXPECTED_POOL, SpiritBow.harmfulPlantPool());
	}

	@Test
	public void harmfulPlantClassReturnsFirstAndLastEntries() {
		assertEquals(Blindweed.class, SpiritBow.harmfulPlantClass(0));
		assertEquals(Stormvine.class, SpiritBow.harmfulPlantClass(4));
	}

	@Test
	public void harmfulPlantClassRejectsInvalidIndexes() {
		assertIllegalArgument(-1);
		assertIllegalArgument(5);
	}

	private static void assertIllegalArgument(int index) {
		try {
			SpiritBow.harmfulPlantClass(index);
			fail("expected IllegalArgumentException for index " + index);
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	@SafeVarargs
	private static Class<? extends Plant>[] plantClasses(Class<? extends Plant>... classes) {
		return classes;
	}
}
