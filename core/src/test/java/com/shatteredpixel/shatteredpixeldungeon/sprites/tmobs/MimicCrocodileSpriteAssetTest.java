package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;

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

public class MimicCrocodileSpriteAssetTest {

	private static final int FRAME_SIZE = 16;
	private static final int FRAME_COUNT = 13;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		MimicCrocodile crocodile = new MimicCrocodile();

		assertEquals("sprites/mimic_crocodile.png", Assets.Sprites.MIMIC_CROCODILE);
		assertSame(MimicCrocodileSprite.class, crocodile.spriteClass);
	}

	@Test
	public void spriteSheetUsesThirteenHardEdgedSixteenPixelFrames() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("mimic_crocodile.png").toFile());

		assertEquals(FRAME_SIZE * FRAME_COUNT, image.getWidth());
		assertEquals(FRAME_SIZE, image.getHeight());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_SIZE; y++) {
				for (int localX = 0; localX < FRAME_SIZE; localX++) {
					int argb = image.getRGB(frame * FRAME_SIZE + localX, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					} else {
						assertEquals(0, argb & 0xFFFFFF);
					}
				}
			}
			assertTrue(nonEmpty);
		}
		assertTrue(visibleColors.size() <= 12);
	}

	@Test
	public void idleFrameUsesTallBipedalKaijuSilhouette() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("mimic_crocodile.png").toFile());
		int minY = FRAME_SIZE;
		int maxY = -1;
		for (int y = 0; y < FRAME_SIZE; y++) {
			for (int x = 0; x < FRAME_SIZE; x++) {
				if ((image.getRGB(x, y) >>> 24) == 255) {
					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
				}
			}
		}

		assertTrue(minY <= 1);
		assertTrue(maxY >= 14);
		assertTrue(maxY - minY + 1 >= 14);
	}

	@Test
	public void animationClassDefinesAllActionsAndRuntimeLurkingAlpha() throws IOException {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/MimicCrocodileSprite.java");
		String java = Files.readString(source);

		assertTrue(java.contains("idle.frames(frames, 0, 1)"));
		assertTrue(java.contains("run.frames(frames, 2, 3, 4, 5, 6, 7)"));
		assertTrue(java.contains("attack.frames(frames, 8, 9, 10)"));
		assertTrue(java.contains("die.frames(frames, 11, 12)"));
		assertTrue(java.contains("MimicCrocodile.LURKING_ALPHA"));
		assertTrue(java.contains("applyLurkingAlpha(ch)"));
		assertTrue(java.contains("alpha(1f)"));
	}

	@Test
	public void lurkingSuppressesStatusIconsAndAttachedEffects() throws IOException {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/MimicCrocodileSprite.java");
		Path charSpriteSource = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/CharSprite.java");
		String java = Files.readString(source);
		String charSprite = Files.readString(charSpriteSource);

		assertTrue(java.contains("return super.visualEffectsVisible() && !isLurking()"));
		assertTrue(charSprite.contains("protected boolean visualEffectsVisible()"));
		assertTrue(charSprite.contains("boolean effectsVisible = visualEffectsVisible()"));
		assertTrue(charSprite.contains("if (visualEffectsVisible())"));
		assertTrue(charSprite.contains("emo.visible = visualEffectsVisible()"));
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
