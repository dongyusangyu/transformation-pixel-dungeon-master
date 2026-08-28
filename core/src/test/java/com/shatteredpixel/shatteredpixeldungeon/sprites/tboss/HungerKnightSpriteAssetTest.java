package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HungerKnightSpriteAssetTest {

    @Test
    public void runtimeSheetHasThirtyExactThirtyTwoPixelFrames() throws Exception {
        assertEquals("sprites/hunger_knight.png", Assets.Sprites.HUNGER_KNIGHT);
        BufferedImage image = ImageIO.read(asset(Assets.Sprites.HUNGER_KNIGHT).toFile());
        assertEquals(32 * 30, image.getWidth());
        assertEquals(32, image.getHeight());
        assertTrue("runtime sprite must retain an alpha channel", image.getColorModel().hasAlpha());

        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y);
                int alpha = color >>> 24;
                assertTrue("pixels must be fully opaque or fully transparent",
                        alpha == 0 || alpha == 255);
                if (alpha != 0) colors.add(color);
            }
        }
        assertTrue("sheet must retain a compact dungeon palette", colors.size() <= 16);
        for (int frame = 0; frame < 30; frame++) {
            boolean occupied = false;
            Set<Integer> frameColors = new HashSet<>();
            for (int y = 0; y < 32; y++) {
                for (int x = frame * 32; x < frame * 32 + 32; x++) {
                    int color = image.getRGB(x, y);
                    if ((color >>> 24) != 0) {
                        occupied = true;
                        frameColors.add(color);
                    }
                }
            }
            assertTrue("frame " + frame + " must not be empty", occupied);
            assertTrue("frame " + frame + " exceeds the sixteen-color limit",
                    frameColors.size() <= 16);
        }
    }

    @Test
    public void animationMapUsesFiniteChargeAndReturnsSpecialsToIdle() throws Exception {
        Path source = repositoryRoot().resolve(
                "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tboss/HungerKnightSprite.java");
        String java = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        assertTrue(java.contains("idle.frames(film, 0, 1, 2, 3)"));
        assertTrue(java.contains("run.frames(film, 4, 5, 6, 7, 8, 9)"));
        assertTrue(java.contains("attack.frames(film, 10, 11, 12, 13, 14)"));
        assertTrue(java.contains("charge = new Animation(8, false)"));
        assertTrue(java.contains("charge.frames(film, 15, 16, 17, 18)"));
        assertTrue(java.contains("thrust.frames(film, 19, 20, 21, 22, 23)"));
        assertTrue(java.contains("die.frames(film, 24, 25, 26, 27, 28, 29)"));
        assertTrue(java.contains("animation == thrust || animation == charge"));
    }

    @Test
    public void editableSemanticSourceAndQaReferencesExist() throws Exception {
        Path root = repositoryRoot();
        Path source = root.resolve("docs/pixel-art/hunger-knight/hunger_knight.pixel.json");
        assertTrue(Files.isRegularFile(source));
        String json = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        assertTrue(json.contains("scale-chain"));
        assertTrue(json.contains("left-leg"));
        assertTrue(json.contains("right-leg"));
        assertTrue(Files.isRegularFile(root.resolve(
                "docs/pixel-art/hunger-knight/reference/hunger_knight_concept.png")));
        assertTrue(Files.isRegularFile(root.resolve(
                "docs/pixel-art/hunger-knight/reference/hunger_knight_turnaround.png")));
        assertTrue(Files.isRegularFile(root.resolve(
                "docs/pixel-art/hunger-knight/reference/hunger_knight_actions.png")));
        assertTrue(Files.isRegularFile(root.resolve(
                "docs/pixel-art/hunger-knight/reference/hunger_knight_pool_32.png")));
    }

    private static Path asset(String relative) {
        return coreDirectory().resolve("src/main/assets").resolve(relative);
    }

    private static Path repositoryRoot() {
        Path working = Paths.get(System.getProperty("user.dir"));
        return Files.isDirectory(working.resolve("core")) ? working : working.getParent();
    }

    private static Path coreDirectory() {
        return repositoryRoot().resolve("core");
    }
}
