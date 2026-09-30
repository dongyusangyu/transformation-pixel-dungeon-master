package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.RubbingsTome;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QuickRecipeNecronomiconTest {

    @Test
    public void necronomiconRecipeAppearsOnWeaponCraftingPageOnly() {
        assertFalse(QuickRecipe.enhancedWeaponGuideRecipes().stream().anyMatch(
                guide -> guide.recipe instanceof Necronomicon.Recipe));
        QuickRecipe.GuideRecipe displayedRecipe = QuickRecipe.weaponCraftingGuideRecipes()
                .stream()
                .filter(guide -> guide.recipe instanceof Necronomicon.Recipe)
                .findFirst()
                .orElseThrow(AssertionError::new);
        Class<?>[] ingredientTypes = displayedRecipe.ingredientTypes;
        assertEquals(CorpseDust.class, ingredientTypes[0]);
        assertEquals(RubbingsTome.class, ingredientTypes[1]);
        assertEquals("com.shatteredpixel.shatteredpixeldungeon.items.artifacts."
                        + "Necronomicon$AlchemyWandPlaceholder",
                ingredientTypes[2].getName());
        assertEquals(Necronomicon.class, displayedRecipe.outputType);
        assertEquals(10, displayedRecipe.recipe.cost(null));
    }

}
