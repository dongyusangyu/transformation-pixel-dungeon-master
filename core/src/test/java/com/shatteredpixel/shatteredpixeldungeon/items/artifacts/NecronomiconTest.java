package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NecronomiconTest {

    @Test
    public void levelAndChargeProfileIsFixed() {
        Necronomicon book = new Necronomicon();
        assertEquals(5, book.levelCap());
        assertEquals(5, Necronomicon.chargeCapForLevel(0));
        assertEquals(10, Necronomicon.chargeCapForLevel(5));
        assertEquals(100, Necronomicon.expThresholdForLevel(0));
        assertEquals(500, Necronomicon.expThresholdForLevel(4));
    }

    @Test
    public void missingChargeControlsNaturalRate() {
        assertEquals(1f / 95f,
                Necronomicon.naturalChargePerTurn(0, 0, 1f), 0.000001f);
        assertEquals(1f / 100f,
                Necronomicon.naturalChargePerTurn(0, 1, 1f), 0.000001f);
        assertEquals(0f,
                Necronomicon.naturalChargePerTurn(0, 5, 1f), 0f);
    }

    @Test
    public void heroProgressUsesFixedArtifactConversions() {
        assertEquals(0, Necronomicon.artifactExpFromHeroProgress(0f));
        assertEquals(50, Necronomicon.artifactExpFromHeroProgress(0.5f));
        assertEquals(100, Necronomicon.artifactExpFromHeroProgress(1f));
        assertEquals(3f, Necronomicon.expChargeFromHeroProgress(0.5f), 0f);
    }

    @Test
    public void potionExperienceReachesEquippedBookAndRestoresItsCharge() {
        Hero hero = TestHeroFactory.create();
        Necronomicon book = new Necronomicon();
        book.level(4);
        hero.belongings.artifact = book;
        Hero previousHero = Dungeon.hero;
        try {
            Dungeon.hero = hero;
            book.activate(hero);
            hero.earnExp(hero.maxExp(), PotionOfExperience.class);
            assertEquals(100, book.exp);
            assertTrue(book.charge > 0);
        } finally {
            Dungeon.hero = previousHero;
        }
    }
}
