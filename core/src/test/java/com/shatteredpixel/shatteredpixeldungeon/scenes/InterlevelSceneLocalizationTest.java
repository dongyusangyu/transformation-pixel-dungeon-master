package com.shatteredpixel.shatteredpixeldungeon.scenes;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class InterlevelSceneLocalizationTest {

    @Test
    public void restartLoadingModeHasChineseAndEnglishText() throws IOException {
        String chinese = readAsset("messages/scenes/scenes_zh.properties");
        String english = readAsset("messages/scenes/scenes.properties");

        assertTrue(chinese.contains(
                "scenes.interlevelscene$mode.restart=新周目开启中"));
        assertTrue(english.contains(
                "scenes.interlevelscene$mode.restart=Starting new cycle"));
    }

    private static String readAsset(String relativePath) throws IOException {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path coreDirectory = workingDirectory.resolve("core");
        if (!Files.isDirectory(coreDirectory)) {
            coreDirectory = workingDirectory;
        }
        Path asset = coreDirectory.resolve("src/main/assets").resolve(relativePath);
        return new String(Files.readAllBytes(asset), StandardCharsets.UTF_8);
    }
}
