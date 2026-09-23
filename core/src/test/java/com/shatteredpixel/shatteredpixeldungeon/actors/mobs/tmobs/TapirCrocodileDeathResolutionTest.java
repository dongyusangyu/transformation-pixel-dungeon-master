package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TapirCrocodileDeathResolutionTest {

    @Test
    public void rangedHeroKillUsesTheStandardDeathAndRankingHooks() throws Exception {
        String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);

        assertTrue(source.contains("Badges.validateDeathFromEnemyMagic()"));
        assertTrue(source.contains("Dungeon.fail(this)"));
        assertTrue(source.contains("Messages.get(Char.class, \"kill\", name())"));
    }

    private static Path sourcePath() {
        Path root = Paths.get("").toAbsolutePath();
        Path core = Files.isDirectory(root.resolve("src/main/java"))
                ? root
                : root.resolve("core");
        return core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/TapirCrocodile.java");
    }
}
