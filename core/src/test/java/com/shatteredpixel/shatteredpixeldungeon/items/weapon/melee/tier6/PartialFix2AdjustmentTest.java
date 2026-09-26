package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.PortableBlackHole;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PartialFix2AdjustmentTest {

	@Test
	public void mercuryProjectionUsesTierFourDamageCurve() {
		assertEquals(8, MercuryBlade.throwMinForLevel(0));
		assertEquals(20, MercuryBlade.throwMaxForLevel(0));
		assertEquals(14, MercuryBlade.throwMinForLevel(3));
		assertEquals(32, MercuryBlade.throwMaxForLevel(3));
	}

	@Test
	public void hundredTonHammerUsesDocumentedAccuracyAndKnockbackCurve() {
		HundredTonHammer hammer = new HundredTonHammer();
		assertEquals(1.18f, hammer.ACC, 0.0001f);
		assertEquals(2, HundredTonHammer.normalKnockbackDistance(0));
		assertEquals(2, HundredTonHammer.normalKnockbackDistance(3));
		assertEquals(3, HundredTonHammer.normalKnockbackDistance(4));
		assertEquals(4, HundredTonHammer.normalKnockbackDistance(8));
	}

	@Test
	public void halberdAbilityDamageIsHalfNormalDamagePlusLevelBonus() {
		assertEquals(7, RadiantGoldHalberd.abilityMin(0));
		assertEquals(28, RadiantGoldHalberd.abilityMax(0));
		assertEquals(10, RadiantGoldHalberd.abilityMin(1));
		assertEquals(35, RadiantGoldHalberd.abilityMax(1));
	}

	@Test
	public void portableWormholeUsesTwoPointMinimumGrowth() {
		PortableBlackHole wormhole = new PortableBlackHole();
		assertEquals(10, wormhole.min(0));
		assertEquals(16, wormhole.min(3));
		assertEquals(25, wormhole.max(0));
	}

	@Test
	public void documentedUpgradeRowsAreExposedAsWeaponFeatures() {
		assertEquals("8-20", new MercuryBlade().upgradeFeatureStats(0).get(0).value);
		assertEquals("2", new HundredTonHammer().upgradeFeatureStats(0).get(0).value);
		assertEquals("20%", new VenomousSickle().upgradeFeatureStats(0).get(0).value);
		assertEquals("20%", new SoulBlade().upgradeFeatureStats(0).get(0).value);
		assertEquals("5-30", new LakeSword().upgradeFeatureStats(0).get(0).value);
		assertEquals("5", new LakeSword().upgradeFeatureStats(0).get(1).value);
	}

	@Test
	public void oracleTerminalWeightedFormsAndSpoonThresholdFollowSpec() {
		assertEquals(OracleTerminal.Form.BLUNT,
				OracleTerminal.formForWeightedRoll(24, 25, 30, 30, 15));
		assertEquals(OracleTerminal.Form.SLASH,
				OracleTerminal.formForWeightedRoll(25, 25, 30, 30, 15));
		assertEquals(OracleTerminal.Form.THRUST,
				OracleTerminal.formForWeightedRoll(55, 25, 30, 30, 15));
		assertEquals(OracleTerminal.Form.SCYTHE,
				OracleTerminal.formForWeightedRoll(85, 25, 30, 30, 15));
		assertTrue(OracleTerminal.spoonThresholdReached(9, 100));
		assertFalse(OracleTerminal.spoonThresholdReached(10, 100));
		assertTrue(OracleTerminal.resetWeightsAfterRecovery(10, 100));
		assertTrue(OracleTerminal.resetWeightsAfterRecovery(11, 100));
	}

	@Test
	public void auxiliaryCoreRecipeUsesResinAndAugmentationStones() throws Exception {
		Field recipesField = Recipe.class.getDeclaredField("weaponRecipes");
		recipesField.setAccessible(true);
		Recipe.WeaponRecipe[] recipes = (Recipe.WeaponRecipe[]) recipesField.get(null);
		Recipe.WeaponRecipe recipe = null;
		Field outputField = Recipe.WeaponRecipe.class.getDeclaredField("output");
		outputField.setAccessible(true);
		for (Recipe.WeaponRecipe candidate : recipes) {
			if (outputField.get(candidate) == AuxiliaryCore.class) recipe = candidate;
		}

		assertNotNull(recipe);
		Field quantityField = Recipe.WeaponRecipe.class.getDeclaredField("inQuantity");
		quantityField.setAccessible(true);
		assertArrayEquals(new int[]{1, 8, 6}, (int[]) quantityField.get(recipe));
		Field costField = Recipe.WeaponRecipe.class.getDeclaredField("cost");
		costField.setAccessible(true);
		assertEquals(5, costField.getInt(recipe));
	}
}
