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

	@Test
	public void activeLowerLevelMemberIsConsumedAsStaleDust() {
		MissileWeapon.UpgradedSetTracker tracker = new MissileWeapon.UpgradedSetTracker();
		TestMissileA member = new TestMissileA();
		member.setID = 44L;
		member.level(0);
		member.upgradeScrollUses = 0;
		tracker.levelThresholds.put(44L, 1);
		tracker.upgradeScrollCredits.put(44L, 1);

		assertFalse(tracker.synchronizeMember(member));
		assertEquals(0, member.trueLevel());
		assertEquals(0, member.upgradeScrollUses);
	}

	@Test
	public void duplicateSetConsolidatesQuantityWithoutSwallowingTheRemainder() {
		long setID = 45L;
		TestMissileA first = new TestMissileA();
		first.quantity(2);
		first.setID = setID;
		TestMissileB second = new TestMissileB();
		second.quantity(2);
		second.setID = setID;

		Bag backpack = new Bag();
		backpack.items.add(first);
		backpack.items.add(second);
		MissileWeapon.sanitizeInventorySets(backpack, null, false);

		assertEquals(3, first.quantity() + second.quantity());
	}

	@Test
	public void cappedCollectionReportsTheConsumedIncomingQuantity() {
		assertEquals(1, MissileWeapon.collectionLoss(1, 3, 0, 3));
	}

	@Test
	public void collectionAtOrBelowTheStackLimitDoesNotReportLoss() {
		assertEquals(0, MissileWeapon.collectionLoss(1, 2, 0, 3));
	}

	@Test
	public void merchantHeldSetRejectsMapMemberButBuybackRestoresCentralRecord() {
		MissileWeapon.UpgradedSetTracker tracker = new MissileWeapon.UpgradedSetTracker();
		TestMissileA sold = new TestMissileA();
		sold.setID = 46L;
		sold.level(5);
		sold.upgradeScrollUses = 5;
		tracker.setCanonicalLevel(sold, 5);
		tracker.upgradeScrollCredits.put(46L, 5);
		tracker.markSold(sold);

		TestMissileA mapMember = new TestMissileA();
		mapMember.setID = 46L;
		mapMember.level(5);
		assertFalse(tracker.synchronizeMember(mapMember));
		assertEquals(0, tracker.availableUpgradeScrollUses(mapMember));
		assertEquals(0, tracker.consumeUpgradeScrollUses(mapMember));

		assertTrue(tracker.restoreFromMerchant(sold));
		assertTrue(tracker.synchronizeMember(sold));
		assertEquals(5, sold.trueLevel());
		assertEquals(5, sold.upgradeScrollUses);
	}

	@Test
	public void merchantStateSurvivesSaveAndLoad() {
		MissileWeapon.UpgradedSetTracker original =
				new MissileWeapon.UpgradedSetTracker();
		TestMissileA sold = new TestMissileA();
		sold.setID = 47L;
		sold.level(4);
		sold.upgradeScrollUses = 4;
		original.setCanonicalLevel(sold, 4);
		original.upgradeScrollCredits.put(47L, 4);
		original.markSold(sold);

		com.watabou.utils.Bundle bundle = new com.watabou.utils.Bundle();
		original.storeInBundle(bundle);
		MissileWeapon.UpgradedSetTracker restored =
				new MissileWeapon.UpgradedSetTracker();
		restored.restoreFromBundle(bundle);

		TestMissileA mapMember = new TestMissileA();
		mapMember.setID = 47L;
		mapMember.level(4);
		assertFalse(restored.synchronizeMember(mapMember));
		assertTrue(restored.restoreFromMerchant(sold));
		assertEquals(4, sold.upgradeScrollUses);
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
