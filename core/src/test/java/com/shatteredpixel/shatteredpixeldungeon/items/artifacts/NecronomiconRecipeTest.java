package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.RubbingsTome;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NecronomiconRecipeTest {

    @After
    public void clearDungeonHero() {
        Dungeon.hero = null;
    }

    @Test
    public void wandIngredientRequiresIdentifiedKnownUncursedUnequippedState() {
        Necronomicon.Recipe recipe = new Necronomicon.Recipe();
        assertTrue(Necronomicon.Recipe.isValidWandIngredient(true, true, false, false));
        assertFalse(Necronomicon.Recipe.isValidWandIngredient(false, true, false, false));
        assertFalse(Necronomicon.Recipe.isValidWandIngredient(true, false, false, false));
        assertFalse(Necronomicon.Recipe.isValidWandIngredient(true, true, true, false));
        assertFalse(Necronomicon.Recipe.isValidWandIngredient(true, true, false, true));

        ArrayList<Item> ingredients = new ArrayList<>(Arrays.asList(
                TestHeroFactory.allocateItem(CorpseDust.class),
                TestHeroFactory.allocateItem(RubbingsTome.class),
                TestHeroFactory.allocateItem(CorpseDust.class)));
        assertFalse(recipe.testIngredients(ingredients));
        assertEquals(10, recipe.cost(ingredients));
    }

    @Test
    public void rejectsCursedWandAndWrongIngredientSets() {
        Necronomicon.Recipe recipe = new Necronomicon.Recipe();
        ArrayList<Item> wrong = new ArrayList<>(Arrays.asList(
                TestHeroFactory.allocateItem(CorpseDust.class),
                TestHeroFactory.allocateItem(RubbingsTome.class),
                TestHeroFactory.allocateItem(RubbingsTome.class)));
        assertFalse(recipe.testIngredients(wrong));
    }

    @Test
    public void outputIsFreshIdentifiedFiveOfFiveBook() {
        Necronomicon.Recipe recipe = new Necronomicon.Recipe();
        Necronomicon result = (Necronomicon) recipe.sampleOutput(null);
        assertNotNull(result);
        assertTrue(result.isIdentified());
        assertFalse(result.cursed);
        assertEquals(0, result.level());
        assertEquals(5, result.getArtifactCharge(), 0f);
        assertEquals(5, Necronomicon.chargeCapForLevel(result.level()));
    }

    @Test
    public void brewingSynchronizesSpawnerOnlyAfterConsumingIngredients() throws IOException {
        String source = readCoreSource("items/artifacts/Necronomicon.java");
        int quantityUpdate = source.indexOf("ingredient.quantity(ingredient.quantity() - 1)");
        int sourceSync = source.indexOf("CorpseDust.syncGhostSpawner(Dungeon.hero)", quantityUpdate);

        assertTrue(quantityUpdate >= 0);
        assertTrue(sourceSync > quantityUpdate);
    }

    private static String readCoreSource(String relativePath) throws IOException {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path coreDirectory = workingDirectory.resolve("core");
        if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
        Path source = coreDirectory.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
                .resolve(relativePath);
        return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
    }
}
