package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RadiantGoldHalberdTest {

	@Test
	public void heavyTierSixProfileMatchesAllBreakpoints() {
		TestableRadiantGoldHalberd weapon = new TestableRadiantGoldHalberd();

		assertEquals(6, RadiantGoldHalberd.TIER);
		assertEquals(1f, RadiantGoldHalberd.ACCURACY, 0f);
		assertEquals(2f, RadiantGoldHalberd.DELAY, 0f);
		assertEquals(3, RadiantGoldHalberd.RANGE);
		assertEquals(6, weapon.min(0));
		assertEquals(48, weapon.max(0));
		assertEquals(10, weapon.min(3));
		assertEquals(77, weapon.max(3));
		assertEquals(18, weapon.min(9));
		assertEquals(135, weapon.max(9));
		assertEquals(23, weapon.min(12));
		assertEquals(166, weapon.max(12));
		assertEquals(30, weapon.min(15));
		assertEquals(201, weapon.max(15));
		assertEquals(22, weapon.STRReq(0));
		assertEquals(22, weapon.STRReq(8));
		assertEquals(21, weapon.STRReq(9));
		assertEquals(21, weapon.STRReq(14));
		assertEquals(20, weapon.STRReq(15));
		assertEquals(EXItemSpriteSheet.RADIANT_GOLD_HALBERD, weapon.image);
		assertEquals(1f, weapon.actualAccuracy(), 0f);
		assertEquals(2f, weapon.actualDelay(), 0f);
		assertEquals(3, weapon.actualRange());
	}

	@Test
	public void negativeLevelsClampToZeroAndMasteryStillReducesTwoStrength() {
		TestableRadiantGoldHalberd weapon = new TestableRadiantGoldHalberd();
		assertEquals(6, weapon.min(-5));
		assertEquals(48, weapon.max(-5));
		assertEquals(22, weapon.STRReq(-5));
		weapon.masteryPotionBonus = true;
		assertEquals(20, weapon.STRReq(0));
		assertEquals(19, weapon.STRReq(9));
		assertEquals(18, weapon.STRReq(15));
	}

	@Test
	public void abilityFormulaUsesTheApprovedRange() {
		assertEquals(7, RadiantGoldHalberd.abilityMin(-3));
		assertEquals(7, RadiantGoldHalberd.abilityMax(-3));
		assertEquals(7, RadiantGoldHalberd.abilityMin(0));
		assertEquals(7, RadiantGoldHalberd.abilityMax(0));
		assertEquals(10, RadiantGoldHalberd.abilityMin(3));
		assertEquals(40, RadiantGoldHalberd.abilityMax(3));
		assertEquals(19, RadiantGoldHalberd.abilityMin(12));
		assertEquals(139, RadiantGoldHalberd.abilityMax(12));
	}

	@Test
	public void lineEligibilityIgnoresVisibilityButRejectsInvalidTargets() {
		assertTrue(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ENEMY, false, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				false, Char.Alignment.ENEMY, false, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ALLY, false, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ENEMY, true, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ENEMY, false, false));
	}

	@Test
	public void onlyTerrainOrBoundaryAddsWallDamage() {
		assertEquals(1.2f, RadiantGoldHalberd.wallDamageMultiplier(true), 0f);
		assertEquals(1f, RadiantGoldHalberd.wallDamageMultiplier(false), 0f);
		assertFalse(RadiantGoldHalberd.wallCollision(false, false));
		assertTrue(RadiantGoldHalberd.wallCollision(true, false));
		assertTrue(RadiantGoldHalberd.wallCollision(false, true));
	}

	@Test
	public void passiveProbabilitiesUseIndependentStrictBoundaries() {
		assertEquals(0.25f, RadiantGoldHalberd.controlChance(false), 0f);
		assertEquals(0.75f, RadiantGoldHalberd.controlChance(true), 0f);
		assertEquals(0.15f, RadiantGoldHalberd.dazeChance(false), 0f);
		assertEquals(0.45f, RadiantGoldHalberd.dazeChance(true), 0f);
		assertTrue(RadiantGoldHalberd.triggers(0.2499f, 0.25f));
		assertFalse(RadiantGoldHalberd.triggers(0.25f, 0.25f));
		assertTrue(RadiantGoldHalberd.triggers(0.4499f, 0.45f));
		assertFalse(RadiantGoldHalberd.triggers(0.45f, 0.45f));
	}

	@Test
	public void levelZeroDoesNotCreateZeroBleeding() {
		assertEquals(0, RadiantGoldHalberd.bleedingAmountForLevel(0));
		assertEquals(12, RadiantGoldHalberd.bleedingAmountForLevel(4));
	}

	private static class TestableRadiantGoldHalberd extends RadiantGoldHalberd {
		float actualAccuracy() {
			return ACC;
		}

		float actualDelay() {
			return DLY;
		}

		int actualRange() {
			return RCH;
		}
	}

}
