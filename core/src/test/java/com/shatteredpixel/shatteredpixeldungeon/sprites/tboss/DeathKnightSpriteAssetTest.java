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

public class DeathKnightSpriteAssetTest {

    @Test
    public void bossSheetContainsThirtyHardEdgedThirtyTwoPixelFrames() throws IOException {
        BufferedImage image = ImageIO.read(asset("sprites/death_knight.png").toFile());
        assertNotNull(image);
        assertEquals(960, image.getWidth());
        assertEquals(32, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());

        Set<Integer> palette = new HashSet<>();
        for (int frame = 0; frame < 30; frame++) {
            int visible = 0;
            for (int y = 0; y < 32; y++) {
                for (int x = frame * 32; x < frame * 32 + 32; x++) {
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
            assertTrue("under-drawn frame " + frame, visible >= 28);
        }
        assertTrue("death knight palette should remain deliberate", palette.size() <= 16);
    }

    @Test
    public void swordWaveSheetContainsFourHardEdgedFrames() throws IOException {
        BufferedImage image = ImageIO.read(asset("effects/death_knight_slash.png").toFile());
        assertNotNull(image);
        assertEquals(128, image.getWidth());
        assertEquals(32, image.getHeight());
        for (int frame = 0; frame < 4; frame++) {
            int visible = 0;
            for (int y = 0; y < 32; y++) {
                for (int x = frame * 32; x < frame * 32 + 32; x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = argb >>> 24;
                    assertTrue(alpha == 0 || alpha == 255);
                    if (alpha == 255) visible++;
                }
            }
            assertTrue(visible >= 8);
        }
    }

    @Test
    public void productionCodeUsesApprovedFrameMapAndAssets() throws IOException {
        Path java = coreDirectory().resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon");
        String assets = read(java.resolve("Assets.java"));
        String sprite = read(java.resolve("sprites/tboss/DeathKnightSprite.java"));
        String slash = read(java.resolve("effects/DeathKnightSlash.java"));
        String boss = read(java.resolve("actors/mobs/tboss/DeathKnight.java"));

        assertTrue(assets.contains("DEATH_KNIGHT = \"sprites/death_knight.png\""));
        assertTrue(assets.contains("DEATH_KNIGHT_SLASH = \"effects/death_knight_slash.png\""));
        assertTrue(sprite.contains("new TextureFilm(texture, 32, 32)"));
        assertTrue(sprite.contains("idle.frames(film, 0, 1, 2, 3)"));
        assertTrue(sprite.contains("run.frames(film, 4, 5, 6, 7, 8, 9)"));
        assertTrue(sprite.contains("attack.frames(film, 10, 11, 12, 13, 14)"));
        assertTrue(sprite.contains("charge.frames(film, 15, 16, 17, 18)"));
        assertTrue(sprite.contains("leap.frames(film, 19, 20, 21, 22, 23)"));
        assertTrue(sprite.contains("void phaseTransition()"));
        assertTrue(sprite.contains("ShadowParticle.UP"));
        assertTrue(sprite.contains("die.frames(film, 24, 25, 26, 27, 28, 29)"));
        assertTrue(slash.contains("new TextureFilm(texture, 32, 32)"));
        assertTrue(slash.contains("public static void showVolley"));
        assertTrue(slash.contains("speed.set"));
        assertTrue(slash.contains("travelTime"));
        assertTrue(slash.contains("public void update()"));
        assertTrue(boss.contains("spriteClass = DeathKnightSprite.class"));
        assertTrue(boss.contains("DeathKnightSlash.showVolley"));
        assertTrue(!boss.contains("DeathKnightSlash.show(sprite.parent"));
    }

    @Test
    public void generatorLocksThreeQuarterFacingThickLegsAndLowBoots() throws IOException {
        String generator = read(coreDirectory().getParent()
                .resolve("tools/generate_death_knight_sprites.ps1"));
        assertTrue(generator.contains("$facing = 'right-three-quarter'"));
        assertTrue(generator.contains("$legWidth = 6"));
        assertTrue(generator.contains("$bootHeight = 2"));

        BufferedImage image = ImageIO.read(asset("sprites/death_knight.png").toFile());
        int frameOffset = 0;
        int ivoryCount = 0;
        int ivoryX = 0;
        int armoredLegPixels = 0;
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                int rgb = image.getRGB(frameOffset + x, y) & 0x00FFFFFF;
                if (rgb == 0xCDC4A8 || rgb == 0xF5EBCA || rgb == 0x8B826F) {
                    ivoryCount++;
                    ivoryX += x;
                }
                if (y >= 25 && y <= 29
                        && (rgb == 0x535258 || rgb == 0x827E7A
                        || rgb == 0x282731 || rgb == 0x0F0D14)) {
                    armoredLegPixels++;
                }
            }
        }
        assertTrue("side-facing mask should sit on the forward/right half",
                ivoryCount > 0 && ivoryX / ivoryCount >= 17);
        assertTrue("idle lower body should read as thick armored legs",
                armoredLegPixels >= 38);
    }

    private static Path asset(String relative) {
        return coreDirectory().resolve("src/main/assets").resolve(relative);
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static Path coreDirectory() {
        Path working = Paths.get(System.getProperty("user.dir"));
        return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
    }
}
