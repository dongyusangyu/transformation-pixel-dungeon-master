package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class MissileWeaponStackingTest {

	@Test
	public void newUpgradableMissileSetsReceiveUniqueIdsImmediately() {
		TestKnife first = new TestKnife();
		TestKnife second = new TestKnife();

		assertNotEquals(MissileWeapon.UNASSIGNED_SET_ID, first.setID);
		assertNotEquals(MissileWeapon.UNASSIGNED_SET_ID, second.setID);
		assertNotEquals(first.setID, second.setID);
	}

	@Test
	public void independentlyGeneratedMissileSetsDoNotMergeOnTheGround() {
		TestKnife first = new TestKnife();
		TestKnife second = new TestKnife();
		first.quantity(2);
		second.quantity(2);
		Heap heap = new Heap();

		heap.drop(first);
		heap.drop(second);

		assertEquals(2, heap.items.size());
		assertEquals(2, first.quantity());
		assertEquals(2, second.quantity());
	}

	@Test
	public void fragmentsFromTheSameMissileSetStillMerge() {
		TestKnife first = new TestKnife();
		TestKnife second = new TestKnife();
		first.setID = 42L;
		second.setID = 42L;
		first.quantity(2);
		second.quantity(1);
		Heap heap = new Heap();

		heap.drop(first);
		heap.drop(second);

		assertEquals(1, heap.items.size());
		assertEquals(3, heap.peek().quantity());
	}

	@Test
	public void restoringAnUnassignedMissileSetRepairsItsId() {
		TestKnife saved = new TestKnife();
		saved.setID = MissileWeapon.UNASSIGNED_SET_ID;
		Bundle bundle = new Bundle();
		saved.storeInBundle(bundle);

		TestKnife restored = new TestKnife();
		restored.restoreFromBundle(bundle);

		assertNotEquals(MissileWeapon.UNASSIGNED_SET_ID, restored.setID);
	}

	@Test
	public void assignedMissileSetIdSurvivesBundleRoundTrip() {
		TestKnife saved = new TestKnife();
		saved.setID = 42L;
		Bundle bundle = new Bundle();
		saved.storeInBundle(bundle);

		TestKnife restored = new TestKnife();
		restored.restoreFromBundle(bundle);

		assertEquals(42L, restored.setID);
	}

	@Test
	public void unassignedIndependentSetsAreNeverConsideredSimilar() {
		TestKnife first = new TestKnife();
		TestKnife second = new TestKnife();
		first.setID = MissileWeapon.UNASSIGNED_SET_ID;
		second.setID = MissileWeapon.UNASSIGNED_SET_ID;

		assertFalse(first.isSimilar(second));
	}

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
		@Override
		public boolean isUpgradable() {
			return true;
		}
	}
}
