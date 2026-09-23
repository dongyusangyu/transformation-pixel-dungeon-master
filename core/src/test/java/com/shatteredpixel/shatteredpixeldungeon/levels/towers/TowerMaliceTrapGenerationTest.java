package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.traps.MaliceTrap;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerMaliceTrapGenerationTest {

    @Test
    public void towerTrapPoolIncludesMaliceTrapWithLowWeight() throws IOException {
        String source = readCoreSource("com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java");

        assertTrue(source.contains("MaliceTrap.class"));
        assertTrue(source.contains("new float[]{4, 4, 3, 2, 2, 1, 1, 1, 1}"));
    }

    private static String readCoreSource(String relativePath) throws IOException {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path coreDirectory = workingDirectory.resolve("core");
        if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
        return new String(Files.readAllBytes(coreDirectory.resolve("src/main/java").resolve(relativePath)),
                StandardCharsets.UTF_8);
    }
}
