package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MarshSlime;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class MarshSlimeSpriteAssetTest {

	private static final int FRAME_WIDTH = 14;
	private static final int FRAME_HEIGHT = 12;
	private static final int FRAME_COUNT = 8;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		MarshSlime slime = new MarshSlime();

		assertEquals("sprites/marsh_slime.png", Assets.Sprites.MARSH_SLIME);
		assertSame(MarshSlimeSprite.class, slime.spriteClass);
	}

	@Test
	public void sheetIsPixelPerfectAndEveryFrameFitsWithinSixteenPixels() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("marsh_slime.png").toFile());

		assertEquals(FRAME_WIDTH * FRAME_COUNT, image.getWidth());
		assertEquals(FRAME_HEIGHT, image.getHeight());
		assertTrue(FRAME_WIDTH <= 16);
		assertTrue(FRAME_HEIGHT <= 16);

		Set<Integer> colors = new HashSet<>();
		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int argb = image.getRGB(frame * FRAME_WIDTH + localX, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						colors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertTrue(nonEmpty);
		}
		assertTrue(colors.size() >= 7);
		assertTrue(colors.size() <= 14);
	}

	@Test
	public void recolorPreservesNormalSlimeSilhouette() throws IOException {
		BufferedImage original = ImageIO.read(spritePath("slime.png").toFile());
		BufferedImage recolor = ImageIO.read(spritePath("marsh_slime.png").toFile());

		for (int y = 0; y < FRAME_HEIGHT; y++) {
			for (int x = 0; x < FRAME_WIDTH * FRAME_COUNT; x++) {
				assertEquals(
						(original.getRGB(x, y) >>> 24) == 0,
						(recolor.getRGB(x, y) >>> 24) == 0);
			}
		}
	}

	@Test
	public void paletteContainsMudSwampHighlightsAndAdaptiveCore() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("marsh_slime.png").toFile());
		boolean mud = false;
		boolean swampGreen = false;
		boolean adaptiveCore = false;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				int r = (argb >> 16) & 0xFF;
				int g = (argb >> 8) & 0xFF;
				int b = argb & 0xFF;
				mud |= r >= 55 && r <= 115 && g >= 45 && g <= 100 && b < 55;
				swampGreen |= g > r + 15 && g > b + 20;
				adaptiveCore |= b > 120 && b > r + 25
						&& (b > g + 10 || g > r + 60);
			}
		}

		assertTrue(mud);
		assertTrue(swampGreen);
		assertTrue(adaptiveCore);
	}

	@Test
	public void animationUsesEstablishedSlimeSequences() throws IOException {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/MarshSlimeSprite.java");
		String java = Files.readString(source);

		assertTrue(java.contains("idle.frames(frames, 0, 1, 1, 0)"));
		assertTrue(java.contains("run.frames(frames, 0, 2, 3, 3, 2, 0)"));
		assertTrue(java.contains("attack.frames(frames, 2, 3, 4, 6, 5)"));
		assertTrue(java.contains("die.frames(frames, 0, 5, 6, 7)"));
	}

	private static Path spritePath(String name) {
		return coreDirectory().resolve("src/main/assets/sprites").resolve(name);
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
