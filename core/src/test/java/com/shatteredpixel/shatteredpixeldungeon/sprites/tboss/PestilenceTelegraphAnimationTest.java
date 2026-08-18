package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class PestilenceTelegraphAnimationTest {

    @Test
    public void warningLocksFrameZeroAndUsesPersistentPlagueFlames() throws IOException {
        String sprite = read(coreDirectory().resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/PestilenceKnightSprite.java"));

        assertTrue(sprite.contains("telegraph"));
        assertTrue(sprite.contains("telegraph.frames(film, 0)"));
        assertTrue(sprite.contains("PlagueFlameParticle.FACTORY"));
        assertTrue(sprite.contains("beginTelegraph"));
        assertTrue(sprite.contains("endTelegraph"));
    }

    @Test
    public void delayedSkillsStartZapOnlyWhenTheirEffectsResolve() throws IOException {
        String boss = read(coreDirectory().resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/PestilenceKnight.java"));

        assertTrue(boss.contains("beginTelegraph()"));
        assertTrue(boss.contains("cast()"));
        assertTrue(boss.contains("launchMiasmaProjectile(skill, projectileTarget)"));
        assertTrue(boss.contains("resolvePendingSkill"));
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static Path coreDirectory() {
        Path working = Paths.get(System.getProperty("user.dir"));
        return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
    }
}
