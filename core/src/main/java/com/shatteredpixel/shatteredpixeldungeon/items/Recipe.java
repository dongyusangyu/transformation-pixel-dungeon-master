/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MeatPie;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StewedMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.BlizzardBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.CausticBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.InfernalBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfIcyTouch;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMysticalEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.BeaconOfReturning;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.CurseInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MetamorphosisPrism;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.PhaseShift;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Recycle;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.RubbingsTome;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TelekineticGrab;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TransformSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.WildEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Glaive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greataxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.GreatGreatGreatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.AuxiliaryCore;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberd;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SoulBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Gungnir;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Trident;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public abstract class Recipe {
	
	public abstract boolean testIngredients(ArrayList<Item> ingredients);
	
	public abstract int cost(ArrayList<Item> ingredients);
	
	public abstract Item brew(ArrayList<Item> ingredients);
	
	public abstract Item sampleOutput(ArrayList<Item> ingredients);
	
	//subclass for the common situation of a recipe with static inputs and outputs
	public static abstract class SimpleRecipe extends Recipe {
		
		//*** These elements must be filled in by subclasses
		protected Class<?extends Item>[] inputs; //each class should be unique
		protected int[] inQuantity;
		
		protected int cost;
		
		protected Class<?extends Item> output;
		protected int outQuantity;
		//***
		
		//gets a simple list of items based on inputs
		public ArrayList<Item> getIngredients() {
			ArrayList<Item> result = new ArrayList<>();
			for (int i = 0; i < inputs.length; i++) {
				Item ingredient = Reflection.newInstance(inputs[i]);
				ingredient.quantity(inQuantity[i]);
				result.add(ingredient);
			}
			return result;
		}
		
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (containsEquippedWeapon(ingredients)) return false;
			
			int[] needed = inQuantity.clone();
			
			for (Item ingredient : ingredients){
				if (!ingredient.isIdentified()) return false;
				for (int i = 0; i < inputs.length; i++){
					if (ingredient.getClass() == inputs[i]){
						needed[i] -= ingredient.quantity();
						break;
					}
				}
			}
			
			for (int i : needed){
				if (i > 0){
					return false;
				}
			}
			
			return true;
		}
		
		public int cost(ArrayList<Item> ingredients){
			return cost;
		}
		
		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			int[] needed = inQuantity.clone();
			
			for (Item ingredient : ingredients){
				for (int i = 0; i < inputs.length; i++) {
					if (ingredient.getClass() == inputs[i] && needed[i] > 0) {
						if (needed[i] <= ingredient.quantity()) {
							ingredient.quantity(ingredient.quantity() - needed[i]);
							needed[i] = 0;
						} else {
							needed[i] -= ingredient.quantity();
							ingredient.quantity(0);
						}
					}
				}
			}
			
			//sample output and real output are identical in this case.
			Item result = sampleOutput(null);
			result.quantity(outQuantity + Talent.miracleAlchemyBonus(hero, result));
			return result;
		}
		
		//ingredients are ignored, as output doesn't vary
		public Item sampleOutput(ArrayList<Item> ingredients){
			try {
				Item result = Reflection.newInstance(output);
				result.quantity(outQuantity);
				return result;
			} catch (Exception e) {
				ShatteredPixelDungeon.reportException( e );
				return null;
			}
		}
	}

	/**
	 * A fixed alchemy recipe whose output is exactly one identified weapon.
	 * Weapon type and base level are recipe-defined; input weapons may pass on
	 * their upgrade level and a positive enchantment.
	 */
	public static class WeaponRecipe extends Recipe {

		protected final Class<? extends Item>[] inputs;
		protected final int[] inQuantity;
		protected final int cost;
		protected final Class<? extends Weapon> output;
		protected final int baseLevel;

		public WeaponRecipe(Class<? extends Item>[] inputs, int[] inQuantity, int cost,
				Class<? extends Weapon> output, int baseLevel) {
			if (inputs == null || inQuantity == null || inputs.length != inQuantity.length
					|| inputs.length == 0 || output == null) {
				throw new IllegalArgumentException("Invalid weapon recipe");
			}
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] == null || inQuantity[i] <= 0) {
					throw new IllegalArgumentException("Invalid weapon recipe ingredient");
				}
				for (int j = 0; j < i; j++) {
					if (inputs[i] == inputs[j]) {
						throw new IllegalArgumentException("Duplicate weapon recipe ingredient");
					}
				}
			}
			this.inputs = inputs.clone();
			this.inQuantity = inQuantity.clone();
			this.cost = Math.max(0, cost);
			this.output = output;
			this.baseLevel = Math.max(0, Math.min(3, baseLevel));
		}

		public ArrayList<Item> getIngredients() {
			ArrayList<Item> result = new ArrayList<>();
			for (int i = 0; i < inputs.length; i++) {
				Item ingredient = Reflection.newInstance(inputs[i]);
				ingredient.quantity(inQuantity[i]);
				result.add(ingredient);
			}
			return result;
		}

		boolean acceptsIngredient(Item item) {
			if (item == null) return false;
			for (Class<? extends Item> input : inputs) {
				if (item.getClass() == input) return true;
			}
			return false;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients == null || ingredients.isEmpty()) return false;
			if (containsEquippedWeapon(ingredients)) return false;

			int[] needed = inQuantity.clone();
			for (Item ingredient : ingredients) {
				if (!ingredient.isIdentified() || ingredient.cursed) return false;
				if (ingredient instanceof Weapon && ((Weapon) ingredient).hasCurseEnchant()) {
					return false;
				}

				boolean matched = false;
				for (int i = 0; i < inputs.length; i++) {
					if (ingredient.getClass() == inputs[i]) {
						needed[i] -= ingredient.quantity();
						matched = true;
						break;
					}
				}
				if (!matched) return false;
			}

			for (int amount : needed) {
				if (amount > 0) return false;
			}
			return true;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return cost;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			Weapon result = createOutput(ingredients, true);
			if (result == null) return null;

			int[] needed = inQuantity.clone();
			for (Item ingredient : ingredients) {
				for (int i = 0; i < inputs.length; i++) {
					if (ingredient.getClass() == inputs[i] && needed[i] > 0) {
						int used = Math.min(needed[i], ingredient.quantity());
						if (used > 0 && ingredient instanceof MissileWeapon) {
							//The consumed set is replaced by the crafted weapon. Invalidate
							//all detached members so they cannot be reused for extraction.
							MissileWeapon.UpgradedSetTracker.invalidateSet(
									hero, (MissileWeapon) ingredient);
						}
						ingredient.quantity(ingredient.quantity() - used);
						needed[i] -= used;
						break;
					}
				}
			}

			result.identify();
			return result;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			Weapon result = createOutput(ingredients, false);
			if (result != null) result.identify(false);
			return result;
		}

		private Weapon createOutput(ArrayList<Item> ingredients, boolean inheritEnchantment) {
			Weapon result = Reflection.newInstance(output);
			if (result == null) return null;

			result.quantity(1);
			result.level(outputLevel(ingredients));
			result.enchant(null);
			result.cursed = false;
			result.curseInfusionBonus = false;
			result.enchantHardened = false;
			result.masteryPotionBonus = false;

			if (inheritEnchantment) {
				ArrayList<Class<? extends Weapon.Enchantment>> candidates =
						enchantmentCandidates(ingredients);
				if (!candidates.isEmpty()
						&& Random.Float() < enchantmentInheritanceChance(candidates.size())) {
					Weapon.Enchantment enchantment = Reflection.newInstance(Random.element(candidates));
					if (enchantment != null) result.enchant(enchantment);
				}
			}

			return result;
		}

		private int outputLevel(ArrayList<Item> ingredients) {
			int weaponCount = 0;
			int levelTotal = 0;
			if (ingredients != null) {
				for (Item ingredient : ingredients) {
					if (ingredient instanceof Weapon) {
						weaponCount++;
						levelTotal += ingredient.trueLevel();
					}
				}
			}

			int inheritedLevel = weaponCount == 0
					? 0
					: (levelTotal + weaponCount - 1) / weaponCount;
			return Math.min(3, Math.max(baseLevel, inheritedLevel));
		}

		ArrayList<Class<? extends Weapon.Enchantment>> enchantmentCandidates(
				ArrayList<Item> ingredients) {
			ArrayList<Class<? extends Weapon.Enchantment>> result = new ArrayList<>();
			if (ingredients == null) return result;

			for (Item ingredient : ingredients) {
				if (ingredient instanceof Weapon) {
					Weapon.Enchantment enchantment = ((Weapon) ingredient).enchantment;
					if (enchantment != null && !enchantment.curse()) {
						result.add(enchantment.getClass());
					}
				}
			}
			return result;
		}

		static float enchantmentInheritanceChance(int enchantedWeaponCount) {
			if (enchantedWeaponCount >= 3) return 1f;
			switch (enchantedWeaponCount) {
				case 0: default:
					return 0f;
				case 1:
					return 0.5f;
				case 2:
					return 0.75f;
			}
		}
	}
	
	
	//*******
	// Static members
	//*******

	private static Recipe[] variableRecipes = new Recipe[]{
			new LiquidMetal.Recipe()
	};

	// Fixed weapon-output recipes are registered here so they can match any
	// valid combination of one to three input slots.
	private static WeaponRecipe[] weaponRecipes = new WeaponRecipe[]{
			new WeaponRecipe(
					new Class[]{Greatsword.class},
					new int[]{3},
					5,
					GreatGreatGreatsword.class,
					0),
			new WeaponRecipe(
					new Class[]{Trident.class, PotionOfHealing.class, ScrollOfRetribution.class},
					new int[]{3, 1, 2},
					5,
					Gungnir.class,
					0),
            new WeaponRecipe(
                    new Class[]{Greataxe.class, RunicBlade.class, Glaive.class},
                    new int[]{1,1,1},
                    5,
                    RadiantGoldHalberd.class,
                    0),
			new WeaponRecipe(
					new Class[]{Greatsword.class, PotionOfMindVision.class, PotionOfShielding.class},
					new int[]{1, 3, 3},
					5,
					SoulBlade.class,
					0),
			new WeaponRecipe(
					new Class[]{ScrollOfMysticalEnergy.class, ArcaneResin.class, MetamorphosisPrism.class},
					new int[]{1, 8, 1},
					5,
					AuxiliaryCore.class,
					0),
	};

	public static ArrayList<WeaponRecipe> weaponRecipes() {
		ArrayList<WeaponRecipe> result = new ArrayList<>();
		for (WeaponRecipe recipe : weaponRecipes) {
			result.add(recipe);
		}
		return result;
	}

	public static boolean weaponRecipeRequiresWholeStack(Class<? extends Item> itemClass) {
		if (itemClass == null) return false;

		for (WeaponRecipe recipe : weaponRecipes) {
			for (int i = 0; i < recipe.inputs.length; i++) {
				if (recipe.inputs[i] == itemClass && recipe.inQuantity[i] > 1) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static Recipe[] oneIngredientRecipes = new Recipe[]{
		new Scroll.ScrollToStone(),
		new ExoticPotion.PotionToExotic(),
		new ExoticScroll.ScrollToExotic(),
		new ArcaneResin.Recipe(),
		new BlizzardBrew.Recipe(),
		new InfernalBrew.Recipe(),
		new AquaBrew.Recipe(),
		new ShockingBrew.Recipe(),
		new ElixirOfDragonsBlood.Recipe(),
		new ElixirOfIcyTouch.Recipe(),
		new ElixirOfToxicEssence.Recipe(),
		new ElixirOfMight.Recipe(),
		new ElixirOfFeatherFall.Recipe(),
		new MagicalInfusion.Recipe(),
		new BeaconOfReturning.Recipe(),
		new PhaseShift.Recipe(),
		new Recycle.Recipe(),
		new TelekineticGrab.Recipe(),
		new SummonElemental.Recipe(),
		new StewedMeat.oneMeat(),
		new TrinketCatalyst.Recipe(),
		new Trinket.UpgradeTrinket(),
		new TransformSpell.Recipe(),
		new MetamorphosisPrism.Recipe()

	};
	
	private static Recipe[] twoIngredientRecipes = new Recipe[]{
		new Blandfruit.CookFruit(),
		new Bomb.EnhanceBomb(),
		new UnstableBrew.Recipe(),
		new CausticBrew.Recipe(),
		new ElixirOfArcaneArmor.Recipe(),
		new ElixirOfAquaticRejuvenation.Recipe(),
		new ElixirOfHoneyedHealing.Recipe(),
		new UnstableSpell.Recipe(),
		new Alchemize.Recipe(),
		new CurseInfusion.Recipe(),
		new ReclaimTrap.Recipe(),
		new WildEnergy.Recipe(),
		new StewedMeat.twoMeat()
	};
	
	private static Recipe[] threeIngredientRecipes = new Recipe[]{
		new Potion.SeedToPotion(),
		new StewedMeat.threeMeat(),
		new MeatPie.Recipe(),
		new RubbingsTome.Recipe()
	};
	
	public static ArrayList<Recipe> findRecipes(ArrayList<Item> ingredients){

		ArrayList<Recipe> result = new ArrayList<>();

		for (WeaponRecipe recipe : weaponRecipes) {
			if (recipe.testIngredients(ingredients)) {
				result.add(recipe);
			}
		}

		for (Recipe recipe : variableRecipes){
			if (recipe.testIngredients(ingredients)){
				result.add(recipe);
			}
		}

		if (ingredients.size() == 1){
			for (Recipe recipe : oneIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
			
		} else if (ingredients.size() == 2){
			for (Recipe recipe : twoIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
			
		} else if (ingredients.size() == 3){
			for (Recipe recipe : threeIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					result.add(recipe);
				}
			}
		}
		
		return result;
	}
	
	public static boolean usableInRecipe(Item item){
		if (isEquippedWeapon(item)) return false;

		if (item instanceof MeleeWeapon && usableInWeaponRecipe(item)) {
			Weapon weapon = (Weapon) item;
			return item.isIdentified() && !item.cursed && !weapon.hasCurseEnchant();
		}
		//only upgradeable thrown weapons and wands allowed among equipment items
		if (item instanceof EquipableItem){
			return item.cursedKnown && !item.cursed &&
					item instanceof MissileWeapon && item.isUpgradable();
		} else if (item instanceof Wand) {
			return item.cursedKnown && !item.cursed;
		} else {
			//other items can be unidentified, but not cursed
			return !item.cursed;
		}
	}

	/**
	 * Equipped weapons remain in the hero's equipment slots and cannot be
	 * consumed as alchemy ingredients. This is shared by the UI filter and
	 * recipe validation so stale selectors cannot bypass the restriction.
	 */
	public static boolean isEquippedWeapon(Item item) {
		return item instanceof Weapon && item.isEquipped(hero);
	}

	private static boolean containsEquippedWeapon(ArrayList<Item> ingredients) {
		if (ingredients == null) return false;
		for (Item ingredient : ingredients) {
			if (isEquippedWeapon(ingredient)) return true;
		}
		return false;
	}

	private static boolean usableInWeaponRecipe(Item item) {
		for (WeaponRecipe recipe : weaponRecipes) {
			if (recipe.acceptsIngredient(item)) {
				return true;
			}
		}
		return false;
	}
}
