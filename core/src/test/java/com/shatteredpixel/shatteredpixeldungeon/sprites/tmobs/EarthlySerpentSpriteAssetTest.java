package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.EarthlySerpent;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class EarthlySerpentSpriteAssetTest {

	private static final int FRAME_WIDTH = 12;
	private static final int FRAME_HEIGHT = 11;
	private static final int FRAME_COUNT = 21;

	@Test
	public void monsterUsesDedicatedEarthlySerpentSprite() {
		EarthlySerpent serpent = new EarthlySerpent();

		assertEquals("sprites/earthly_serpent.png", Assets.Sprites.EARTHLY_SERPENT);
		assertSame(EarthlySerpentSprite.class, serpent.spriteClass);
	}

	@Test
	public void spriteExposesSpitPullAndWarningAnimations() throws Exception {
		assertNotNull(EarthlySerpentSprite.class.getMethod("spit", int.class));
		assertNotNull(EarthlySerpentSprite.class.getMethod("pull", int.class));
		assertNotNull(EarthlySerpentSprite.class.getMethod("warning", boolean.class));
	}

	@Test
	public void sheetContainsTwentyOneHardEdgedTwelveByElevenFrames() throws IOException {
		Path asset = spritesDirectory().resolve("earthly_serpent.png");
		BufferedImage image = ImageIO.read(asset.toFile());

		assertEquals(256, image.getWidth());
		assertEquals(16, image.getHeight());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int x = frame * FRAME_WIDTH; x < (frame + 1) * FRAME_WIDTH; x++) {
					int argb = image.getRGB(x, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertTrue("frame " + frame + " must not be empty", nonEmpty);
		}
		assertTrue(visibleColors.size() <= 12);
	}

	@Test
	public void originalGameplayFramesRetainSewerSnakeSilhouette() throws IOException {
		BufferedImage original = ImageIO.read(spritesDirectory().resolve("snake.png").toFile());
		BufferedImage earthly = ImageIO.read(spritesDirectory().resolve("earthly_serpent.png").toFile());

		for (int frame = 0; frame < 14; frame++) {
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int x = frame * FRAME_WIDTH + localX;
					if ((original.getRGB(x, y) >>> 24) == 255) {
						assertEquals(255, earthly.getRGB(x, y) >>> 24);
					}
				}
			}
		}
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
