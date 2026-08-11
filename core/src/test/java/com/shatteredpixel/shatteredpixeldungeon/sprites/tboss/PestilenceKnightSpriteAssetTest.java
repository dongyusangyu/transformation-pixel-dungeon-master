package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PestilenceKnightSpriteAssetTest {

    @Test
    public void sheetContainsSeventeenHardEdgedSixteenPixelFrames() throws IOException {
        BufferedImage image = ImageIO.read(spritePath().toFile());
        assertNotNull(image);
        assertEquals(272, image.getWidth());
        assertEquals(16, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());

        Set<Integer> palette = new HashSet<>();
        for (int frame = 0; frame < 17; frame++) {
            int visible = 0;
            for (int y = 0; y < 16; y++) {
                for (int x = frame * 16; x < frame * 16 + 16; x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = argb >>> 24;
                    assertTrue("semi-transparent pixel in frame " + frame,
                            alpha == 0 || alpha == 255);
                    if (alpha == 255) {
                        visible++;
                        palette.add(argb);
                    } else {
                        assertEquals(0, argb & 0x00FFFFFF);
                    }
                }
            }
            assertTrue("empty animation frame " + frame, visible >= 8);
        }
        assertTrue("16px boss palette should stay disciplined", palette.size() <= 12);
    }

    @Test
    public void animationAndPhaseFxUseTheApprovedFrameMap() throws IOException {
        Path root = coreDirectory().resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon");
        String assets = read(root.resolve("Assets.java"));
        String sprite = read(root.resolve("sprites/tboss/PestilenceKnightSprite.java"));
        String boss = read(root.resolve("actors/mobs/tboss/PestilenceKnight.java"));
        String infection = read(root.resolve("actors/buffs/tboss/Infection.java"));

        assertTrue(assets.contains("PESTILENCE_KNIGHT = \"sprites/pestilence_knight.png\""));
        assertTrue(sprite.contains("idle.frames(film, 0, 1, 2, 1)"));
        assertTrue(sprite.contains("run.frames(film, 3, 4, 5, 6)"));
        assertTrue(sprite.contains("attack.frames(film, 7, 8, 9)"));
        assertTrue(sprite.contains("zap.frames(film, 10, 11, 12)"));
        assertTrue(sprite.contains("die.frames(film, 13, 14, 15, 16)"));
        assertTrue(sprite.contains("ShadowParticle.UP"));
        assertTrue(sprite.contains("tint(0.30f, 0.05f, 0.42f, 0.24f)"));
        assertTrue("terminal particles must follow the moving boss",
                sprite.contains("terminalShadow.pos(x, y + height, width, 0)"));
        assertTrue("terminal particles must be erased immediately when the effect ends",
                sprite.contains("terminalShadow.killAndErase()"));
        assertTrue(boss.contains("spriteClass = PestilenceKnightSprite.class"));
        assertTrue("telegraphed skills should announce their names before resolving",
                boss.contains("announceSkill(skill)"));
        assertTrue("plague flask should launch an existing potion sprite",
                boss.contains("recycle(MissileSprite.class)"));
        assertTrue("the projectile target must be the center used to build the warning area",
                boss.contains("telegraphSkill(PLAGUE_FLASK, squareAround(target), target)"));
        assertTrue(boss.contains("ItemSpriteSheet.POTION_JADE"));
        assertTrue(boss.contains("ItemSpriteSheet.POTION_GOLDEN"));
        assertTrue(boss.contains("ItemSpriteSheet.POTION_BISTRE"));
        assertTrue("the bottle should fly to the warning area's center",
                boss.contains("reset(sprite, target, flask"));
        assertTrue("the bottle should splash in the incubating miasma color",
                boss.contains("Splash.at(target, PLAGUE_FLASK_IMPACT_COLOR"));
        assertTrue("harvest should explain the invulnerable channel",
                boss.contains("announceSkill(\"harvest\")"));
        assertTrue("infection rupture needs visible status feedback",
                infection.contains("Messages.get(this, \"rupture\")"));
        assertTrue("infection rupture needs a distinct particle burst",
                infection.contains("target.sprite.burst(INFECTION_BURST_COLOR"));
        assertEquals("both retreat and approach movement must synchronize the sprite",
                2, occurrences(boss, "moveSprite(oldPos, pos)"));
    }

    private static Path spritePath() {
        return coreDirectory().resolve("src/main/assets/sprites/pestilence_knight.png");
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }

    private static Path coreDirectory() {
        Path working = Paths.get(System.getProperty("user.dir"));
        return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
    }
}
