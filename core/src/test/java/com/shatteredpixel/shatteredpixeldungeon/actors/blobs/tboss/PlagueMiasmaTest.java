package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlagueMiasmaTest {

    @Test
    public void miasmaTypesShareRulesButIncenseIsNotHarmfulMiasma() {
        assertTrue(new IncubatingMiasma() instanceof PlagueMiasma);
        assertTrue(new OutbreakMiasma() instanceof PlagueMiasma);
        assertTrue(new PaleMiasma() instanceof PlagueMiasma);
        assertTrue(new PurifyingIncense() instanceof Blob);
        assertFalse(((Object) new PurifyingIncense()) instanceof PlagueMiasma);
    }

    @Test
    public void outbreakLocalEvolutionStateSurvivesBundle() {
        OutbreakMiasma original = new OutbreakMiasma();
        Bundle first = new Bundle();
        original.storeInBundle(first);

        OutbreakMiasma restored = new OutbreakMiasma();
        restored.restoreFromBundle(first);
        Bundle second = new Bundle();
        restored.storeInBundle(second);

        assertEquals(first.getBoolean("spread_parity"), second.getBoolean("spread_parity"));
        assertEquals(first.getLong("rng_state"), second.getLong("rng_state"));
    }

    @Test
    public void blobImmunityCoversOnlyHarmfulPlagueTypes() {
        BlobImmunity immunity = new BlobImmunity();
        assertTrue(immunity.immunities().contains(IncubatingMiasma.class));
        assertTrue(immunity.immunities().contains(OutbreakMiasma.class));
        assertTrue(immunity.immunities().contains(PaleMiasma.class));
        assertFalse(immunity.immunities().contains(PurifyingIncense.class));
    }

    @Test
    public void purityThrowTargetsOnlyTheThreeBossMiasmas() throws Exception {
        String source = source("items/potions/PotionOfPurity.java");
        int start = source.indexOf("affectedBlobs = new ArrayList<>()");
        int end = source.indexOf("}", start);
        String initializer = source.substring(start, end);
        assertTrue(initializer.contains("IncubatingMiasma.class"));
        assertTrue(initializer.contains("OutbreakMiasma.class"));
        assertTrue(initializer.contains("PaleMiasma.class"));
        assertFalse(initializer.contains("ToxicGas.class"));
        assertFalse(initializer.contains("Fire.class"));
        assertFalse(initializer.contains("Web.class"));
    }

    private static String source(String relative) throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path base = Files.isDirectory(root.resolve("src/main/java"))
                ? root.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
                : root.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon");
        return Files.readString(base.resolve(relative), StandardCharsets.UTF_8);
    }
}
