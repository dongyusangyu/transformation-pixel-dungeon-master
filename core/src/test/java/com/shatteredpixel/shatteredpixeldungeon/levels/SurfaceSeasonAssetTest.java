package com.shatteredpixel.shatteredpixeldungeon.levels;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SurfaceSeasonAssetTest {

	@Test
	public void surfaceTilesetsShareTheFixedTileGridContract() throws IOException {
		for (String name : new String[]{"tiles_surface_lush.png", "tiles_surface_winter.png"}) {
			BufferedImage image = readEnvironmentImage(name);
			assertEquals(name, 256, image.getWidth());
			assertEquals(name, 256, image.getHeight());
			assertTrue(name, image.getColorModel().hasAlpha());
			assertEquals(name, 0, image.getWidth() % 16);
			assertEquals(name, 0, image.getHeight() % 16);
		}
	}

	@Test
	public void surfaceWaterTexturesShareTheFixedAnimationContract() throws IOException {
		for (String name : new String[]{"water_surface_lush.png", "water_surface_winter.png"}) {
			BufferedImage image = readEnvironmentImage(name);
			assertEquals(name, 32, image.getWidth());
			assertEquals(name, 32, image.getHeight());
			assertTrue(name, image.getColorModel().hasAlpha());
		}
	}

	private static BufferedImage readEnvironmentImage(String name) throws IOException {
		Path path = environmentDirectory().resolve(name);
		assertTrue("missing environment asset: " + path, Files.isRegularFile(path));
		BufferedImage image = ImageIO.read(path.toFile());
		assertNotNull("unreadable PNG: " + path, image);
		return image;
	}

	private static Path environmentDirectory() {
		Path workingDirectory = Paths.get("").toAbsolutePath();
		Path fromRoot = workingDirectory.resolve("core/src/main/assets/environment");
		if (Files.isDirectory(fromRoot)) {
			return fromRoot;
		}
		return workingDirectory.resolve("src/main/assets/environment");
	}
}
