package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThief;

import org.junit.Test;

public class ChainShadowThiefSpriteAssetTest {

	private static final int FRAME_WIDTH = 12;
	private static final int FRAME_HEIGHT = 13;
	private static final int FRAME_COUNT = 13;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		ChainShadowThief thief = new ChainShadowThief();

		assertEquals("sprites/chain_shadow_thief.png", Assets.Sprites.CHAIN_SHADOW_THIEF);
		assertSame(ChainShadowThiefSprite.class, thief.spriteClass);
	}

	@Test
	public void sheetIsPixelPerfectAndEveryFrameFitsWithinSixteenPixels() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("chain_shadow_thief.png").toFile());

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
		assertTrue(colors.size() >= 4);
		assertTrue(colors.size() <= 12);
	}

	@Test
	public void recolorPreservesThiefSilhouette() throws IOException {
		BufferedImage original = ImageIO.read(spritePath("thief.png").toFile());
		BufferedImage recolor = ImageIO.read(spritePath("chain_shadow_thief.png").toFile());

		for (int y = 0; y < FRAME_HEIGHT; y++) {
			for (int x = 0; x < FRAME_WIDTH * FRAME_COUNT; x++) {
				assertEquals(
						(original.getRGB(x, y) >>> 24) == 0,
						(recolor.getRGB(x, y) >>> 24) == 0);
			}
		}
	}

	@Test
	public void paletteIsPredominantlyNeutralGray() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("chain_shadow_thief.png").toFile());
		int opaque = 0;
		int neutral = 0;
		int steel = 0;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				opaque++;
				int r = (argb >> 16) & 0xFF;
				int g = (argb >> 8) & 0xFF;
				int b = argb & 0xFF;
				if (Math.abs(r - g) <= 20 && Math.abs(g - b) <= 20) neutral++;
				if (r >= 150 && g >= 160 && b >= 165) steel++;
			}
		}

		assertTrue(neutral * 100 >= opaque * 85);
		assertTrue(steel > 0);
	}

	@Test
	public void animationUsesThiefSequences() throws IOException {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/ChainShadowThiefSprite.java");
		String java = Files.readString(source);

		assertTrue(java.contains("idle.frames(film, 0, 0, 0, 1, 0, 0, 0, 0, 1)"));
		assertTrue(java.contains("run.frames(film, 0, 0, 2, 3, 3, 4)"));
		assertTrue(java.contains("die.frames(film, 5, 6, 7, 8, 9)"));
		assertTrue(java.contains("attack.frames(film, 10, 11, 12, 0)"));
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
