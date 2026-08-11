package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.HeavyCrabification;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class HeavyCrabificationSpriteAssetTest {

	private static final int FRAME_SIZE = 16;
	private static final int FRAME_COUNT = 14;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		HeavyCrabification crab = new HeavyCrabification();

		assertEquals(
				"sprites/heavy_crabification.png",
				Assets.Sprites.HEAVY_CRABIFICATION);
		assertSame(HeavyCrabificationSprite.class, crab.spriteClass);
	}

	@Test
	public void sheetHasFourteenOpaqueOrTransparentPixelPerfectFrames() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("heavy_crabification.png").toFile());

		assertEquals(FRAME_SIZE * FRAME_COUNT, image.getWidth());
		assertEquals(FRAME_SIZE, image.getHeight());

		Set<Integer> colors = new HashSet<>();
		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_SIZE; y++) {
				for (int localX = 0; localX < FRAME_SIZE; localX++) {
					int argb = image.getRGB(frame * FRAME_SIZE + localX, y);
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
		assertTrue(colors.size() <= 16);
	}

	@Test
	public void recolorPreservesEveryGreatCrabFrameSilhouette() throws IOException {
		BufferedImage original = ImageIO.read(spritePath("crab.png").toFile());
		BufferedImage recolor = ImageIO.read(spritePath("heavy_crabification.png").toFile());

		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			for (int y = 0; y < FRAME_SIZE; y++) {
				for (int localX = 0; localX < FRAME_SIZE; localX++) {
					int x = frame * FRAME_SIZE + localX;
					assertEquals(
							original.getRGB(x, FRAME_SIZE * 2 + y) >>> 24,
							recolor.getRGB(x, y) >>> 24);
				}
			}
		}
	}

	@Test
	public void paletteContainsDarkShellCyanCracksAndBoneDetails() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("heavy_crabification.png").toFile());
		boolean darkShell = false;
		boolean cyanCrack = false;
		boolean boneDetail = false;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				int r = (argb >> 16) & 0xFF;
				int g = (argb >> 8) & 0xFF;
				int b = argb & 0xFF;
				darkShell |= r < 40 && g < 65 && b < 75;
				cyanCrack |= g > 150 && b > 150 && r < 100;
				boneDetail |= r > 180 && g > 165 && b > 135;
			}
		}

		assertTrue(darkShell);
		assertTrue(cyanCrack);
		assertTrue(boneDetail);
	}

	@Test
	public void animationClassUsesTheEstablishedCrabSequences() throws IOException {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/HeavyCrabificationSprite.java");
		String java = Files.readString(source);

		assertTrue(java.contains("idle.frames(frames, 0, 1, 0, 2)"));
		assertTrue(java.contains("run.frames(frames, 3, 4, 5, 6)"));
		assertTrue(java.contains("attack.frames(frames, 7, 8, 9)"));
		assertTrue(java.contains("die.frames(frames, 10, 11, 12, 13)"));
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
