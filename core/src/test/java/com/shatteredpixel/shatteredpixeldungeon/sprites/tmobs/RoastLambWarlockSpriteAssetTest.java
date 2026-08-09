package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RoastLambWarlockSpriteAssetTest {

	private static final int FRAME_WIDTH = 12;
	private static final int FRAME_HEIGHT = 15;
	private static final int GAMEPLAY_FRAMES = 11;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		RoastLambWarlock warlock = new RoastLambWarlock();

		assertEquals("sprites/roast_lamb_warlock.png", Assets.Sprites.ROAST_LAMB_WARLOCK);
		assertSame(RoastLambWarlockSprite.class, warlock.spriteClass);
	}

	@Test
	public void spriteSheetHasHardEdgesAndSmallLimitedPaletteFrames() throws IOException {
		Path asset = spritesDirectory().resolve("roast_lamb_warlock.png");
		assertTrue(Files.isRegularFile(asset));

		BufferedImage image = ImageIO.read(asset.toFile());
		assertEquals(256, image.getWidth());
		assertEquals(16, image.getHeight());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < GAMEPLAY_FRAMES; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int argb = image.getRGB(frame * FRAME_WIDTH + localX, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 0) {
						assertEquals(0, argb);
					} else {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertTrue("frame " + frame + " must not be empty", nonEmpty);
		}
		assertTrue("sprite palette must stay at or below 12 colors", visibleColors.size() <= 12);
	}

	@Test
	public void gameplayFramesKeepExactDwarfWarlockSilhouettes() throws IOException {
		Path sprites = spritesDirectory();
		BufferedImage source = ImageIO.read(sprites.resolve("warlock.png").toFile());
		BufferedImage result = ImageIO.read(sprites.resolve("roast_lamb_warlock.png").toFile());

		for (int frame = 0; frame < GAMEPLAY_FRAMES; frame++) {
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int x = frame * FRAME_WIDTH + localX;
					assertEquals(
							"frame " + frame + " silhouette must match the dwarf warlock",
							source.getRGB(x, y) >>> 24,
							result.getRGB(x, y) >>> 24);
				}
			}
		}
	}

	@Test
	public void paletteContainsWoolAndRoastingFlameDetails() throws IOException {
		BufferedImage image = ImageIO.read(
				spritesDirectory().resolve("roast_lamb_warlock.png").toFile());
		Set<Integer> colors = new HashSet<>();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) == 255) {
					colors.add(image.getRGB(x, y) & 0xFFFFFF);
				}
			}
		}

		assertTrue(colors.contains(0xEEE2B8));
		assertTrue(colors.contains(0xE14716));
		assertTrue(colors.contains(0xFFB82E));
	}

	private static Path spritesDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		return coreDirectory.resolve("src/main/assets/sprites");
	}
}
