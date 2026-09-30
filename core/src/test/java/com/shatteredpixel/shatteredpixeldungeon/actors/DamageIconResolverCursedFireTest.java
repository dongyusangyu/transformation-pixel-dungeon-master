package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class DamageIconResolverCursedFireTest {

    private static HeadlessItemSprites sprites;

    @BeforeClass public static void installSheets() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void restoreSheets() { sprites.close(); }

    @Test
    public void cursedFireUsesItsOwnIconWithoutChangingDamageNatureTags() {
        assertEquals(FloatingText.CURSE_BURNING, DamageIconResolver.resolve(EnumSet.of(
                DamageTag.MAGICAL, DamageTag.FIRE, DamageTag.CURSED_FIRE,
                DamageTag.CURSED_FIRE_RESOLVED)));
    }

    @Test
    public void ordinaryFireStillUsesTheOrdinaryBurningIcon() {
        assertEquals(FloatingText.BURNING, DamageIconResolver.resolve(EnumSet.of(
                DamageTag.MAGICAL, DamageTag.FIRE)));
    }

    @Test
    public void directMagicDamageUsesTheMagicIcon() {
        assertEquals(FloatingText.MAGIC_DMG, DamageIconResolver.resolve(EnumSet.of(
                DamageTag.MAGICAL)));
    }
}
