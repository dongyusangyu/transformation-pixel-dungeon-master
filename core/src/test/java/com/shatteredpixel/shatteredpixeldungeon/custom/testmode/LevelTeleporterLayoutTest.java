package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class LevelTeleporterLayoutTest {

    @Test
    public void levelSelectionRefreshesNestedScrollPaneAfterWindowOffset() throws IOException {
        String source = readSource();
        assertTrue(source.contains("void offset(int xOffset, int yOffset)"));
        assertTrue(source.contains("pane.setPos(pane.left(), pane.top())"));
    }

    private static String readSource() throws IOException {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path coreDirectory = workingDirectory.resolve("core");
        if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
        Path source = coreDirectory.resolve("src/main/java")
                .resolve("com/shatteredpixel/shatteredpixeldungeon/custom/testmode/LevelTeleporter.java");
        return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
    }
}
