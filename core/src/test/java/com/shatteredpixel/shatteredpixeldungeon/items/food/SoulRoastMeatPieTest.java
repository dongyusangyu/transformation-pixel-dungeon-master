package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;

public class SoulRoastMeatPieTest {
    @Test public void soulMeatReplacesOrdinaryMeatWithoutChangingThePie() {
        try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
                     new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
        ArrayList<Item> inputs = new ArrayList<>(Arrays.asList(
                new Food().quantity(3), new Pasty().quantity(2), new SoulRoastMeat().quantity(4)));
        MeatPie.Recipe recipe = new MeatPie.Recipe();
        assertTrue(recipe.testIngredients(inputs));
        assertEquals(6, recipe.cost(inputs));
        assertEquals(MeatPie.class, recipe.brew(inputs).getClass());
        assertEquals(2, inputs.get(0).quantity());
        assertEquals(1, inputs.get(1).quantity());
        assertEquals(3, inputs.get(2).quantity());
        }
    }
}
