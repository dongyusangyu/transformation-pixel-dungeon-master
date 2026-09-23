package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.RubbingsTome;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuickRecipeNecronomiconTest {

    @Test
    public void enhancedWeaponsPageShowsNecronomiconAsAQuickAlchemyRecipe() {
        ArrayList<QuickRecipe.GuideRecipe> recipes =
                QuickRecipe.enhancedWeaponGuideRecipes();
        QuickRecipe.GuideRecipe displayedRecipe = recipes.get(recipes.size() - 1);
        assertTrue(displayedRecipe.recipe instanceof Necronomicon.Recipe);
        Class<?>[] ingredientTypes = displayedRecipe.ingredientTypes;
        assertEquals(CorpseDust.class, ingredientTypes[0]);
        assertEquals(RubbingsTome.class, ingredientTypes[1]);
        assertEquals("com.shatteredpixel.shatteredpixeldungeon.items.artifacts."
                        + "Necronomicon$AlchemyWandPlaceholder",
                ingredientTypes[2].getName());
        assertEquals(Necronomicon.class, displayedRecipe.outputType);
    }

}
