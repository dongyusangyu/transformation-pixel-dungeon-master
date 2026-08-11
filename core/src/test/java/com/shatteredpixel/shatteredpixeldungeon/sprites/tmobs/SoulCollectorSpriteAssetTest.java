package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SoulCollectorSpriteAssetTest {

	@Test
	public void sheetsUseApprovedFrameDimensionsAndHardLimitedPalettes() throws IOException {
		assertSheet("soul_collector.png", 16, 16, 13);
		assertSheet("powerful_wraith.png", 14, 15, 8);
	}

	@Test
	public void everyFrameIsVisibleAndCloselyFollowsItsReferenceSilhouette() throws IOException {
		assertReferenceOverlap("soul_collector.png", "necromancer.png", 16, 16, 13);
		assertReferenceOverlap("powerful_wraith.png", "wraith.png", 14, 15, 8);
	}

	@Test
	public void newSheetsAreRealEditsRatherThanReferenceCopies() throws IOException {
		BufferedImage collector = readSprite("soul_collector.png");
		BufferedImage necromancer = readSprite("necromancer.png");
		BufferedImage wraith = readSprite("powerful_wraith.png");
		BufferedImage normalWraith = readSprite("wraith.png");

		assertFalse(samePixels(collector, necromancer, collector.getWidth(), collector.getHeight()));
		assertFalse(samePixels(wraith, normalWraith, wraith.getWidth(), wraith.getHeight()));
	}

	@Test
	public void assetConstantsAndAnimationFramesAreConnected() throws IOException {
		Path root = coreDirectory().resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon");
		String assets = readText(root.resolve("Assets.java"));
		String collector = readText(root.resolve("sprites/tmobs/SoulCollectorSprite.java"));
		String wraith = readText(root.resolve("sprites/tmobs/PowerfulWraithSprite.java"));

		assertTrue(assets.contains("SOUL_COLLECTOR = \"sprites/soul_collector.png\""));
		assertTrue(assets.contains("POWERFUL_WRAITH = \"sprites/powerful_wraith.png\""));
		assertTrue(collector.contains("frames, 0, 0, 0, 1"));
		assertTrue(collector.contains("frames, 0, 0, 0, 2, 3, 4"));
		assertTrue(collector.contains("frames, 5, 6, 7, 8"));
		assertTrue(collector.contains("frames, 9, 10, 11, 12"));
		assertTrue(collector.contains("Speck.RATTLE"));
		assertTrue(wraith.contains("frames, 0, 1"));
		assertTrue(wraith.contains("frames, 0, 2, 3"));
		assertTrue(wraith.contains("frames, 0, 4, 5, 6, 7"));
	}

	private static void assertSheet(String file, int frameWidth, int frameHeight, int frames)
			throws IOException {
		BufferedImage image = readSprite(file);
		assertNotNull(image);
		assertEquals(frameWidth * frames, image.getWidth());
		assertEquals(frameHeight, image.getHeight());
		assertTrue(image.getColorModel().hasAlpha());

		Set<Integer> palette = new HashSet<>();
		for (int frame = 0; frame < frames; frame++) {
			boolean visible = false;
			for (int y = 0; y < frameHeight; y++) {
				for (int x = frame * frameWidth; x < (frame + 1) * frameWidth; x++) {
					int argb = image.getRGB(x, y);
					int alpha = argb >>> 24;
					assertTrue("alpha must be 0 or 255", alpha == 0 || alpha == 255);
					if (alpha == 255) {
						visible = true;
						palette.add(argb);
					} else {
						assertEquals("transparent RGB must be zero", 0, argb & 0x00FFFFFF);
					}
				}
			}
			assertTrue(file + " frame " + frame + " is empty", visible);
		}
		assertTrue("palette must stay limited", palette.size() <= 12);
	}

	private static void assertReferenceOverlap(String targetName, String sourceName,
			int frameWidth, int frameHeight, int frames) throws IOException {
		BufferedImage target = readSprite(targetName);
		BufferedImage source = readSprite(sourceName);
		int matching = 0;
		int union = 0;
		for (int y = 0; y < frameHeight; y++) {
			for (int x = 0; x < frameWidth * frames; x++) {
				boolean targetVisible = (target.getRGB(x, y) >>> 24) != 0;
				boolean sourceVisible = (source.getRGB(x, y) >>> 24) >= 128;
				if (targetVisible || sourceVisible) union++;
				if (targetVisible && sourceVisible) matching++;
			}
		}
		assertTrue("edited silhouette drifted from reference", matching >= union * 0.95f);
	}

	private static boolean samePixels(BufferedImage first, BufferedImage second, int width, int height) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (first.getRGB(x, y) != second.getRGB(x, y)) return false;
			}
		}
		return true;
	}

	private static BufferedImage readSprite(String name) throws IOException {
		return ImageIO.read(coreDirectory().resolve("src/main/assets/sprites").resolve(name).toFile());
	}

	private static String readText(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
	}
}
