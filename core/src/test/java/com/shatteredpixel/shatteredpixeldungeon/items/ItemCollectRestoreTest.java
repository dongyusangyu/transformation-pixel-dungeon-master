package com.shatteredpixel.shatteredpixeldungeon.items;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ItemCollectRestoreTest {

    @Test
    public void restoringLostDartsUsesTheActiveBagInsteadOfTheGlobalHero() throws IOException {
        String source = readCoreSource("com/shatteredpixel/shatteredpixeldungeon/items/Item.java");

        assertTrue(source.contains("d.collect(container)"));
        assertTrue(source.contains("if (container.isLoading())"));
        assertFalse(source.contains("if (!d.collect())"));
    }

    private static String readCoreSource(String relativePath) throws IOException {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path coreDirectory = workingDirectory.resolve("core");
        if (!Files.isDirectory(coreDirectory)) {
            coreDirectory = workingDirectory;
        }
        Path source = coreDirectory.resolve("src/main/java").resolve(relativePath);
        return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
    }
}
