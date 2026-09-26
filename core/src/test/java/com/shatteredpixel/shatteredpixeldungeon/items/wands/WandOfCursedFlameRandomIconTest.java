package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WandOfCursedFlameRandomIconTest {

    private static HeadlessItemSprites sprites;

    @BeforeClass public static void installSprites() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void restoreSprites() { sprites.close(); }

    private final Hero oldHero = Dungeon.hero;

    @After public void tearDown() {
        Dungeon.hero = oldHero;
    }

    @Test public void randomModeUsesTheGreenFlameCellFromTheWandIconRow() {
        Hero hero = TestHeroFactory.create();
        hero.randomMode = true;
        Dungeon.hero = hero;

        WandOfCursedFlame wand = new WandOfCursedFlame();
        wand.image();

        assertEquals("green flame is the 14th 8px cell in row 2", 29, wand.icon);
        assertEquals(29, Wand.randomModeIconForClass(WandOfCursedFlame.class));
    }
}
