package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class WandOfCursedFlameTest {

    private static HeadlessItemSprites sprites;
    private Hero previousHero;
    @BeforeClass public static void setUpSheets() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void tearDownSheets() { sprites.close(); }
    @org.junit.Before public void rememberHero() { previousHero = Dungeon.hero; }
    @After public void restoreHero() { Dungeon.hero = previousHero; }

    @Test public void newWandHasTwoChargesAndLevelledDamageBounds() {
        WandOfCursedFlame wand = new WandOfCursedFlame();
        assertEquals(2, wand.maxCharges);
        assertEquals(2, wand.curCharges);
        assertEquals(2, wand.min(0));
        assertEquals(8, wand.max(0));
        assertEquals(5, wand.min(3));
        assertEquals(23, wand.max(3));
    }

    @Test public void flameLifetimeCeilsOneTurnPerThreeLevels() {
        assertEquals(2, WandOfCursedFlame.flameDuration(0));
        assertEquals(3, WandOfCursedFlame.flameDuration(1));
        assertEquals(3, WandOfCursedFlame.flameDuration(3));
        assertEquals(4, WandOfCursedFlame.flameDuration(4));
        assertEquals(5, WandOfCursedFlame.flameDuration(7));
    }

    @Test public void craftedBadgeAndAtlasRemainStableInBothModesAndAfterReload() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        WandOfCursedFlame wand = new WandOfCursedFlame();
        wand.identify(false);
        for (boolean randomMode : new boolean[]{false, true}) {
            hero.randomMode = randomMode;
            assertEquals(EXItemSpriteSheet.WAND_CURSED_FLAME, wand.image());
            assertEquals("only random mode shows the crafted wand marker",
                    randomMode ? ItemSpriteSheet.Icons.WAND_CURSED_FLAME : -1,
                    wand.icon);
            Bundle saved = new Bundle();
            wand.storeInBundle(saved);
            WandOfCursedFlame restored = new WandOfCursedFlame();
            restored.restoreFromBundle(saved);
            assertEquals(EXItemSpriteSheet.WAND_CURSED_FLAME, restored.image());
            assertEquals("mode-dependent marker survives reload",
                    randomMode ? ItemSpriteSheet.Icons.WAND_CURSED_FLAME : -1,
                    restored.icon);
        }
    }

    @Test public void randomModeDoesNotGiveCraftedWandAnOrdinaryUnknownIdentity() {
        Hero hero = TestHeroFactory.create();
        hero.randomMode = true;
        Dungeon.hero = hero;
        WandOfCursedFlame wand = new WandOfCursedFlame();
        assertEquals(EXItemSpriteSheet.WAND_CURSED_FLAME, wand.image());
        assertTrue("crafted type is always known, despite not joining the random pool", wand.isKnown());
        wand.identify(false);
        assertTrue("identification survives random mode", wand.isIdentified());
    }

    @Test public void ordinaryWandLootAndAppearancePoolsCannotGenerateCraftedWand() {
        assertFalse(Arrays.asList(Generator.Category.WAND.classes).contains(WandOfCursedFlame.class));
        assertFalse(Arrays.asList(Wand.randomModeWandClasses()).contains(WandOfCursedFlame.class));
    }
}
