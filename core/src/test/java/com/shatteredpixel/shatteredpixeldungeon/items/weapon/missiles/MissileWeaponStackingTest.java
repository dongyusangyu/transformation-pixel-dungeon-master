package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MissileWeaponStackingTest {

	@Test
	public void nonUpgradableDartDoesNotMatchDifferentMissileType() {
		assertFalse(new TestDart().isSimilar(new TestKnife()));
	}

	@Test
	public void sameTypeDartsStackWithoutQuantityLimit() {
		TestDart stack = new TestDart();
		stack.quantity(1000);
		TestDart recovered = new TestDart();
		recovered.quantity(1);

		stack.merge(recovered);

		assertEquals(1001, stack.quantity());
		assertEquals(0, recovered.quantity());
	}

	private abstract static class TestMissile extends MissileWeapon {
		{
			tier = 1;
			baseUses = 5;
		}

		@Override
		public int min(int lvl) {
			return 1;
		}

		@Override
		public int max(int lvl) {
			return 1;
		}
	}

	private static class TestDart extends TestMissile {
		@Override
		public boolean isUpgradable() {
			return false;
		}

		@Override
		public int defaultQuantity() {
			return 2;
		}
	}

	private static class TestKnife extends TestMissile {
	}
}
