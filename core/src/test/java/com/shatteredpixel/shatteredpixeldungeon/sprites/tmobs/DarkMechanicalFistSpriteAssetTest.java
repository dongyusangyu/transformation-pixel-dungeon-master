package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DarkMechanicalFist;

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

public class DarkMechanicalFistSpriteAssetTest {

	@Test
	public void darkFistUsesDedicatedTextureAndSpriteClass() {
		assertEquals("sprites/dark_mechanical_fist.png", Assets.Sprites.DARK_MECHANICAL_FIST);
		assertSame(DarkMechanicalFistSprite.class, new DarkMechanicalFist().spriteClass);
	}

	@Test
	public void darkTexturePreservesMechanicalFistGridAndHardEdges() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("dark_mechanical_fist.png").toFile());
		assertEquals(240, image.getWidth());
		assertEquals(17, image.getHeight());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < 10; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < 17; y++) {
				for (int localX = 0; localX < 24; localX++) {
					int argb = image.getRGB(frame * 24 + localX, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertEquals(frame < 7, nonEmpty);
		}
		assertTrue(visibleColors.size() <= 12);
	}

	@Test
	public void activeFramesContainTheMicroEye() throws IOException {
		BufferedImage image = ImageIO.read(spritePath("dark_mechanical_fist.png").toFile());
		for (int frame = 0; frame < 7; frame++) {
			boolean eye = false;
			for (int y = 5; y <= 7; y++) {
				for (int x = 10; x <= 12; x++) {
					int argb = image.getRGB(frame * 24 + x, y);
					if ((argb >>> 24) == 255 && (argb & 0xFFFFFF) == 0xFFDCF4) {
						eye = true;
					}
				}
			}
			assertTrue(eye);
		}
	}

	private static Path spritePath(String name) {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return coreDirectory.resolve("src/main/assets/sprites").resolve(name);
	}
}
