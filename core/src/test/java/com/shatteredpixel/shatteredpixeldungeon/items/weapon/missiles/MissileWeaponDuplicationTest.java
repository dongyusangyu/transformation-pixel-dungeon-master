package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MissileWeaponDuplicationTest {

	@Test
	public void pickingUpHolsterRemovesTransmutationDuplicateSet() {
		long setID = 42L;
		TestMissileA originalRemainder = new TestMissileA();
		originalRemainder.quantity(2);
		originalRemainder.setID = setID;

		TestMissileB transmutedSet = new TestMissileB();
		transmutedSet.quantity(3);
		transmutedSet.setID = setID;

		Bag backpack = new Bag();
		Bag droppedHolster = new Bag();
		backpack.items.add(transmutedSet);
		backpack.items.add(droppedHolster);
		droppedHolster.items.add(originalRemainder);

		MissileWeapon.sanitizeInventorySets(backpack, droppedHolster, false);

		assertFalse(droppedHolster.items.contains(originalRemainder));
		assertTrue(backpack.items.contains(transmutedSet));
		assertEquals(3, transmutedSet.quantity());
		assertEquals(0, originalRemainder.quantity());
	}

	@Test
	public void upgradingLevelZeroSplitSetRemovesRemainder() {
		long setID = 43L;
		TestMissileA originalRemainder = new TestMissileA();
		originalRemainder.quantity(2);
		originalRemainder.setID = setID;

		TestMissileB transmutedRemainder = new TestMissileB();
		transmutedRemainder.quantity(1);
		transmutedRemainder.setID = setID;

		Bag backpack = new Bag();
		backpack.items.add(originalRemainder);
		backpack.items.add(transmutedRemainder);

		transmutedRemainder.quantity(transmutedRemainder.defaultQuantity());
		transmutedRemainder.sanitizeAfterUpgrade(backpack, false);

		assertFalse(backpack.items.contains(originalRemainder));
		assertTrue(backpack.items.contains(transmutedRemainder));
		assertEquals(3, transmutedRemainder.quantity());
		assertEquals(0, originalRemainder.quantity());
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

	private static class TestMissileA extends TestMissile {
	}

	private static class TestMissileB extends TestMissile {
	}
}
