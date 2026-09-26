package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class WandOfCursedFlameRecipeTest {

    private static HeadlessItemSprites sprites;
    private Hero oldHero;
    @BeforeClass public static void setUpSheets() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void tearDownSheets() { sprites.close(); }
    @org.junit.Before public void rememberHero() { oldHero = Dungeon.hero; }
    @After public void restoreHero() { Dungeon.hero = oldHero; }

    private static ArrayList<Item> inputs(WandOfMagicMissile missile, WandOfFireblast fire) {
        return new ArrayList<>(Arrays.asList(missile, fire, new MetalShard()));
    }

    private static WandOfMagicMissile missile(int level) {
        WandOfMagicMissile wand = new WandOfMagicMissile();
        wand.level(level);
        wand.identify(false);
        return wand;
    }

    private static WandOfFireblast fire(int level) {
        WandOfFireblast wand = new WandOfFireblast();
        wand.level(level);
        wand.identify(false);
        return wand;
    }

    @Test public void acceptsEitherOrderForFiveEnergyAndIsDiscoveredByAlchemy() {
        ArrayList<Item> items = inputs(missile(1), fire(2));
        Recipe recipe = new WandOfCursedFlame.Recipe();
        assertTrue(recipe.testIngredients(items));
        assertEquals(5, recipe.cost(items));
        assertTrue(Recipe.findRecipes(items).stream().anyMatch(r -> r instanceof WandOfCursedFlame.Recipe));
        ArrayList<Item> reversed = new ArrayList<>(items);
        java.util.Collections.reverse(reversed);
        assertTrue(recipe.testIngredients(reversed));
    }

    @Test public void rejectsUnidentifiedCursedOrWrongInput() {
        Recipe recipe = new WandOfCursedFlame.Recipe();
        WandOfMagicMissile unidentified = new WandOfMagicMissile();
        assertFalse(recipe.testIngredients(inputs(unidentified, fire(0))));
        WandOfMagicMissile cursed = missile(0);
        cursed.cursed = true;
        assertFalse(recipe.testIngredients(inputs(cursed, fire(0))));
        WandOfFireblast cursedFire = fire(0);
        cursedFire.cursed = true;
        assertFalse(recipe.testIngredients(inputs(missile(0), cursedFire)));
        assertFalse(recipe.testIngredients(new ArrayList<>(Arrays.asList(missile(0), new MetalShard(), new MetalShard()))));
    }

    @Test public void rejectsWandEquippedInsideMageStaff() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        WandOfMagicMissile equipped = missile(1);
        hero.belongings.weapon = new MagesStaff(equipped);
        Recipe recipe = new WandOfCursedFlame.Recipe();
        assertFalse(recipe.testIngredients(inputs(equipped, fire(1))));
    }

    @Test public void rejectsWandInsideSecondaryMageStaff() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        WandOfMagicMissile equipped = missile(1);
        hero.belongings.secondWep = new MagesStaff(equipped);
        assertFalse(new WandOfCursedFlame.Recipe().testIngredients(inputs(equipped, fire(1))));
    }

    @Test public void outputCeilsAverageCapsAtThreeAndStartsFullyCharged() {
        Recipe recipe = new WandOfCursedFlame.Recipe();
        ArrayList<Item> ingredients = inputs(missile(1), fire(2));
        WandOfCursedFlame output = (WandOfCursedFlame) recipe.brew(ingredients);
        assertNotNull(output);
        assertEquals(2, output.trueLevel());
        assertEquals(4, output.maxCharges);
        assertEquals(4, output.curCharges);
        assertFalse(output.cursed);
        assertTrue(output.isIdentified());
        assertEquals(0, ingredients.get(0).quantity());
        assertEquals(0, ingredients.get(1).quantity());
        assertEquals(0, ingredients.get(2).quantity());

        assertEquals(3, recipe.sampleOutput(inputs(missile(8), fire(8))).trueLevel());
        assertEquals(0, recipe.sampleOutput(inputs(missile(0), fire(0))).trueLevel());
    }

}
