package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PortableBlackHoleTest {

	@Test
	public void usesSpecifiedTierSixStatsAndSetSize() {
		PortableBlackHole weapon = new PortableBlackHole();

		assertEquals(6, weapon.tier);
		assertEquals(19, weapon.STRReq(0));
		assertEquals(10, weapon.min(0));
		assertEquals(25, weapon.max(0));
		assertEquals(17, weapon.min(7));
		assertEquals(67, weapon.max(7));
		assertEquals(10, weapon.min(-3));
		assertEquals(25, weapon.max(-3));
		assertEquals(3, weapon.defaultQuantity());
		assertEquals(EXItemSpriteSheet.PORTABLE_BLACK_HOLE, weapon.image);
	}

	@Test
	public void unupgradedPieceHasFiveDurabilityUses() {
		PortableBlackHole weapon = new PortableBlackHole();

		assertEquals(MissileWeapon.MAX_DURABILITY / 5f,
				weapon.durabilityPerUse(0), 0.01f);
	}

	@Test
	public void quantityCannotExceedThree() {
		PortableBlackHole weapon = new PortableBlackHole();

		weapon.quantity(10);

		assertEquals(3, weapon.quantity());
	}

	@Test
	public void mergingStacksLeavesRemainderAboveTheThreeItemCap() {
		PortableBlackHole first = new PortableBlackHole();
		first.quantity(2);
		PortableBlackHole second = new PortableBlackHole();
		second.quantity(3);

		first.merge(second);

		assertEquals(3, first.quantity());
		assertEquals(2, second.quantity());
	}

	@Test
	public void onlyAnEmptyNonPitLandingTriggersTeleport() {
		assertTrue(PortableBlackHole.shouldTeleport(false, false));
		assertFalse(PortableBlackHole.shouldTeleport(false, true));
		assertFalse(PortableBlackHole.shouldTeleport(true, false));
		assertFalse(PortableBlackHole.shouldTeleport(true, true));
	}
}
