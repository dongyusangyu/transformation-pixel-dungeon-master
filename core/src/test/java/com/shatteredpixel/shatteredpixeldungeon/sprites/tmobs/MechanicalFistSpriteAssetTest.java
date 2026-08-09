package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MechanicalFist;

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

public class MechanicalFistSpriteAssetTest {

	private static final int FRAME_WIDTH = 24;
	private static final int FRAME_HEIGHT = 17;
	private static final int FRAME_COUNT = 10;
	private static final int ACTIVE_FRAME_COUNT = 7;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		MechanicalFist fist = new MechanicalFist();

		assertEquals("sprites/mechanical_fist.png", Assets.Sprites.MECHANICAL_FIST);
		assertSame(MechanicalFistSprite.class, fist.spriteClass);
	}

	@Test
	public void spriteSheetKeepsOriginalFistFrameGridAndHardEdges() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("mechanical_fist.png").toFile());

		assertEquals(FRAME_WIDTH * FRAME_COUNT, image.getWidth());
		assertEquals(FRAME_HEIGHT, image.getHeight());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int argb = image.getRGB(frame * FRAME_WIDTH + localX, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertEquals(frame < ACTIVE_FRAME_COUNT, nonEmpty);
		}
		assertTrue(visibleColors.size() <= 12);
	}

	@Test
	public void activeFramesPreserveRustedYogFistSilhouette() throws IOException {
		BufferedImage original = ImageIO.read(spritePath("yog_fists.png").toFile());
		BufferedImage mechanical = ImageIO.read(spritePath("mechanical_fist.png").toFile());
		int originalTop = FRAME_HEIGHT * 3;

		for (int frame = 0; frame < ACTIVE_FRAME_COUNT; frame++) {
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int x = frame * FRAME_WIDTH + localX;
					assertEquals(
							original.getRGB(x, originalTop + y) >>> 24,
							mechanical.getRGB(x, y) >>> 24);
				}
			}
		}
	}

	@Test
	public void animationClassDefinesIdleRunSlamAndDeathSequences() throws IOException {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/MechanicalFistSprite.java");
		String java = Files.readString(source);

		assertTrue(java.contains("idle.frames(frames, 0, 0, 1)"));
		assertTrue(java.contains("run.frames(frames, 0, 1)"));
		assertTrue(java.contains("attack.frames(frames, 0, 5, 6, 0)"));
		assertTrue(java.contains("die.frames(frames, 0, 2, 3, 4)"));
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
