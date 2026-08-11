package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;

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
    public void firstTwoPhasesSpreadButTerminalMiasmaDoesNot() {
        assertTrue(new IncubatingMiasma() instanceof SpreadingPlagueMiasma);
        assertTrue(new OutbreakMiasma() instanceof SpreadingPlagueMiasma);
        assertFalse(((Object) new PaleMiasma()) instanceof SpreadingPlagueMiasma);
        assertEquals(20, SpreadingPlagueMiasma.SPREAD_AMOUNT);
        assertEquals(2, ((SpreadingPlagueMiasma) new IncubatingMiasma()).spreadInterval());
        assertEquals(1, ((SpreadingPlagueMiasma) new OutbreakMiasma()).spreadInterval());
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
    public void incubationSpreadStateAlsoSurvivesBundle() {
        IncubatingMiasma original = new IncubatingMiasma();
        Bundle first = new Bundle();
        original.storeInBundle(first);

        IncubatingMiasma restored = new IncubatingMiasma();
        restored.restoreFromBundle(first);
        Bundle second = new Bundle();
        restored.storeInBundle(second);

        assertEquals(first.getInt("spread_clock"), second.getInt("spread_clock"));
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
    public void purityThrowRetainsAllHarmfulBlobsIncludingTheThreeMiasmas() throws Exception {
        BlobImmunity immunity = new BlobImmunity();
        assertTrue(immunity.immunities().contains(ToxicGas.class));
        assertTrue(immunity.immunities().contains(IncubatingMiasma.class));
        assertTrue(immunity.immunities().contains(OutbreakMiasma.class));
        assertTrue(immunity.immunities().contains(PaleMiasma.class));
        assertFalse(immunity.immunities().contains(PurifyingIncense.class));

        String source = source("items/potions/PotionOfPurity.java");
        assertTrue(source.contains(
                "affectedBlobs = new ArrayList<>(new BlobImmunity().immunities())"));
    }

    @Test
    public void everyMiasmaDefinesVisibleDistinctGasAndAnInspectableDescription() throws Exception {
        assertSame(PlagueMiasma.class,
                PlagueMiasma.class.getMethod("use", BlobEmitter.class).getDeclaringClass());
        assertSame(PlagueMiasma.class,
                PlagueMiasma.class.getMethod("tileDesc").getDeclaringClass());

        Method color = PlagueMiasma.class.getDeclaredMethod("particleColor");
        color.setAccessible(true);
        Set<Integer> colors = new HashSet<>();
        colors.add((Integer) color.invoke(new IncubatingMiasma()));
        colors.add((Integer) color.invoke(new OutbreakMiasma()));
        colors.add((Integer) color.invoke(new PaleMiasma()));
        assertEquals("each phase needs a readable, distinct gas color", 3, colors.size());

        String source = source("actors/blobs/tboss/PlagueMiasma.java");
        assertTrue(source.contains("emitter.pour"));
        assertTrue(source.contains("Messages.get(this, \"desc\")"));
    }

    private static String source(String relative) throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path base = Files.isDirectory(root.resolve("src/main/java"))
                ? root.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
                : root.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon");
        return Files.readString(base.resolve(relative), StandardCharsets.UTF_8);
    }
}
