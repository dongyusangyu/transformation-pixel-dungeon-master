package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuickRecipeCursedFlameGuideTest {
    @Test public void enhancedWeaponPageOffersCursedFlameCraftingWithExactIngredients() {
        assertTrue(QuickRecipe.enhancedWeaponGuideRecipes().stream().anyMatch(guide ->
                guide.recipe instanceof WandOfCursedFlame.Recipe
                        && guide.outputType == WandOfCursedFlame.class
                        && Arrays.equals(guide.ingredientTypes, new Class[]{
                                WandOfMagicMissile.class, WandOfFireblast.class, MetalShard.class})
                        && guide.recipe.cost(null) == 5));
    }
}
