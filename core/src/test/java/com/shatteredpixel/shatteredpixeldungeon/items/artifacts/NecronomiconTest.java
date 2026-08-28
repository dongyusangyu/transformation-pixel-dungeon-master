package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
