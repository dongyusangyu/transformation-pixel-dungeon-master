package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.GreatGreatGreatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Gungnir;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Trident;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WeaponRecipeTest {

	@Test
	public void threeGreatswordsCraftRegisteredGreatGreatGreatswordRecipe()
			throws ReflectiveOperationException {
		Field recipesField = Recipe.class.getDeclaredField("weaponRecipes");
		recipesField.setAccessible(true);
		Recipe.WeaponRecipe[] registered =
				(Recipe.WeaponRecipe[]) recipesField.get(null);

		Recipe.WeaponRecipe found = null;
		for (Recipe.WeaponRecipe recipe : registered) {
			if (recipe.output == GreatGreatGreatsword.class) {
				found = recipe;
				break;
			}
		}

		assertNotNull(found);
		assertArrayEquals(new Class[]{Greatsword.class}, found.inputs);
		assertArrayEquals(new int[]{3}, found.inQuantity);
		assertEquals(5, found.cost);
		assertEquals(GreatGreatGreatsword.class, found.output);
		assertEquals(0, found.baseLevel);
	}

	@Test
	public void tridentPotionAndRetributionScrollCraftGungnirRecipeIsRegistered()
			throws ReflectiveOperationException {
		Field recipesField = Recipe.class.getDeclaredField("weaponRecipes");
		recipesField.setAccessible(true);
		Recipe.WeaponRecipe[] registered =
				(Recipe.WeaponRecipe[]) recipesField.get(null);

		Recipe.WeaponRecipe found = null;
		for (Recipe.WeaponRecipe recipe : registered) {
			if (recipe.output == Gungnir.class) {
				found = recipe;
				break;
			}
		}

		assertNotNull(found);
		assertArrayEquals(new Class[]{Trident.class, PotionOfHealing.class,
				ScrollOfRetribution.class}, found.inputs);
		assertArrayEquals(new int[]{3, 1, 2}, found.inQuantity);
		assertEquals(5, found.cost);
		assertEquals(Gungnir.class, found.output);
		assertEquals(0, found.baseLevel);
	}

	@Test
	public void tridentRecipeRequiresAFullTridentSetAndTwoRetributionScrolls() {
		Recipe.WeaponRecipe recipe = new Recipe.WeaponRecipe(
				new Class[]{InputWeaponA.class, TestIngredient.class, TestIngredientC.class},
				new int[]{3, 1, 2}, 5, OutputWeapon.class, 0);

		assertTrue(recipe.testIngredients(ingredients(
				identified(new InputWeaponA().quantity(3)),
				identified(new TestIngredient()),
				identified(new TestIngredientC().quantity(2)))));
		assertFalse(recipe.testIngredients(ingredients(
				identified(new InputWeaponA().quantity(2)),
				identified(new TestIngredient()),
				identified(new TestIngredientC().quantity(2)))));
		assertFalse(recipe.testIngredients(ingredients(
				identified(new InputWeaponA().quantity(3)),
				identified(new TestIngredient()),
				identified(new TestIngredientC()))));
	}

	@Test
	public void weaponRecipeConsumesRequiredQuantitiesAndProducesOneIdentifiedWeapon() {
		Recipe.WeaponRecipe recipe = new Recipe.WeaponRecipe(
				new Class[]{InputWeaponA.class, TestIngredient.class, TestIngredientC.class},
				new int[]{3, 1, 2}, 5, OutputWeapon.class, 0);
		InputWeaponA trident = (InputWeaponA) identified(new InputWeaponA().quantity(3));
		trident.level(4);
		TestIngredient potion = identified(new TestIngredient());
		TestIngredientC scroll = identified(new TestIngredientC());
		scroll.quantity(4);

		Weapon output = (Weapon) recipe.brew(ingredients(trident, potion, scroll));

		assertEquals(OutputWeapon.class, output.getClass());
		assertEquals(1, output.quantity());
		assertEquals(3, output.trueLevel());
		assertTrue(output.isIdentified());
		assertEquals(0, trident.quantity());
		assertEquals(0, potion.quantity());
		assertEquals(2, scroll.quantity());
	}

	@Test
	public void weaponRecipeMarksMultiQuantityIngredientsForWholeStackInput() {
		assertTrue(Recipe.weaponRecipeRequiresWholeStack(ScrollOfRetribution.class));
		assertTrue(Recipe.weaponRecipeRequiresWholeStack(Trident.class));
		assertFalse(Recipe.weaponRecipeRequiresWholeStack(PotionOfHealing.class));
	}

	@Test
	public void sampleOutputIsFixedIdentifiedAndUsesCeilingAverageLevel() {
		Recipe.WeaponRecipe recipe = recipe(0, OutputWeapon.class,
				InputWeaponA.class, InputWeaponB.class);
		ArrayList<Item> ingredients = ingredients(
				weapon(new InputWeaponA(), 3, null),
				weapon(new InputWeaponB(), 0, null));

		Item output = recipe.sampleOutput(ingredients);

		assertEquals(OutputWeapon.class, output.getClass());
		assertEquals(1, output.quantity());
		assertEquals(2, output.trueLevel());
		assertTrue(output.isIdentified());
	}

	@Test
	public void outputLevelIgnoresTierDifferenceAndCapsAtThree() {
		Recipe.WeaponRecipe recipe = recipe(0, OutputWeapon.class, InputWeaponA.class);
		ArrayList<Item> ingredients = ingredients(weapon(new InputWeaponA(), 8, null));

		assertEquals(3, recipe.sampleOutput(ingredients).trueLevel());
	}

	@Test
	public void recipeBaseLevelAppliesWithoutWeaponIngredients() {
		Recipe.WeaponRecipe recipe = recipe(1, OutputWeapon.class, TestIngredient.class);
		ArrayList<Item> ingredients = ingredients(new TestIngredient().identify(false));

		assertEquals(1, recipe.sampleOutput(ingredients).trueLevel());
	}

	@Test
	public void enchantmentInheritanceChanceUsesRequiredTable() {
		assertEquals(0f, Recipe.WeaponRecipe.enchantmentInheritanceChance(0), 0f);
		assertEquals(0.5f, Recipe.WeaponRecipe.enchantmentInheritanceChance(1), 0f);
		assertEquals(0.75f, Recipe.WeaponRecipe.enchantmentInheritanceChance(2), 0f);
		assertEquals(1f, Recipe.WeaponRecipe.enchantmentInheritanceChance(3), 0f);
	}

	@Test
	public void enchantmentCandidatesPreserveDuplicateWeighting() {
		Recipe.WeaponRecipe recipe = recipe(0, OutputWeapon.class,
				InputWeaponA.class, InputWeaponB.class, InputWeaponC.class);
		ArrayList<Item> ingredients = ingredients(
				weapon(new InputWeaponA(), 0, new Blazing()),
				weapon(new InputWeaponB(), 0, new Blazing()),
				weapon(new InputWeaponC(), 0, new Shocking()));

		ArrayList<Class<? extends Weapon.Enchantment>> candidates =
				recipe.enchantmentCandidates(ingredients);

		assertEquals(Arrays.asList(Blazing.class, Blazing.class, Shocking.class), candidates);
	}

	@Test
	public void brewingAlwaysProducesOneIdentifiedWeaponAndConsumesIngredients() {
		Recipe.WeaponRecipe recipe = recipe(0, OutputWeapon.class,
				InputWeaponA.class, InputWeaponB.class, InputWeaponC.class);
		ArrayList<Item> ingredients = ingredients(
				weapon(new InputWeaponA(), 1, new Blazing()),
				weapon(new InputWeaponB(), 1, new Blazing()),
				weapon(new InputWeaponC(), 1, new Blazing()));

		Weapon output = (Weapon) recipe.brew(ingredients);

		assertEquals(1, output.quantity());
		assertEquals(1, output.trueLevel());
		assertTrue(output.isIdentified());
		assertEquals(Blazing.class, output.enchantment.getClass());
		for (Item ingredient : ingredients) {
			assertEquals(0, ingredient.quantity());
		}
	}

	@SafeVarargs
	private static Recipe.WeaponRecipe recipe(int baseLevel,
			Class<? extends Weapon> output,
			Class<? extends Item>... inputs) {
		int[] quantities = new int[inputs.length];
		Arrays.fill(quantities, 1);
		return new Recipe.WeaponRecipe(inputs, quantities, 5, output, baseLevel);
	}

	private static <T extends Weapon> T weapon(T weapon, int level,
			Weapon.Enchantment enchantment) {
		weapon.level(level);
		weapon.enchant(enchantment);
		weapon.identify(false);
		return weapon;
	}

	private static <T extends Item> T identified(T item) {
		item.identify(false);
		return item;
	}

	private static ArrayList<Item> ingredients(Item... items) {
		return new ArrayList<>(Arrays.asList(items));
	}

	public static class InputWeaponA extends MeleeWeapon {
		{
			tier = 1;
		}
	}

	public static class InputWeaponB extends MeleeWeapon {
		{
			tier = 2;
		}
	}

	public static class InputWeaponC extends MeleeWeapon {
		{
			tier = 3;
		}
	}

	public static class OutputWeapon extends MeleeWeapon {
		{
			tier = 5;
		}
	}

	public static class TestIngredient extends Item {
	}

	public static class TestIngredientC extends Item {
	}
}
