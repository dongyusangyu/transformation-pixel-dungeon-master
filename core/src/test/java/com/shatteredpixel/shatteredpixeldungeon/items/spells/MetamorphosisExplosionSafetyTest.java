package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class MetamorphosisExplosionSafetyTest {

    @Test
    public void metamorphosisCrystalIsMarkedUniqueForExplosionProtection() throws Exception {
        assertTrue(sourceAt("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/spells/TransformSpell.java")
                .contains("unique = true;"));
    }

    @Test
    public void metamorphosisPrismIsMarkedUniqueForExplosionProtection() throws Exception {
        assertTrue(sourceAt("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/spells/MetamorphosisPrism.java")
                .contains("unique = true;"));
    }

    private static String sourceAt(String fileName) throws Exception {
        Path working = Paths.get(System.getProperty("user.dir"));
        Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
        return Files.readString(core.resolve(fileName), StandardCharsets.UTF_8);
    }
}
