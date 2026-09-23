package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class IconsFeelingTest {

	@Test
	public void newTowerFeelingsUseDedicatedSmallAndLargeIcons() {
		assertSame(Icons.DEPTH_SKY_ISLAND, Icons.smallFeelingIcon(Level.Feeling.SKY_ISLAND));
		assertSame(Icons.DEPTH_BARREN, Icons.smallFeelingIcon(Level.Feeling.BARREN));
		assertSame(Icons.DEPTH_CHAOS, Icons.smallFeelingIcon(Level.Feeling.CHAOS));
		assertSame(Icons.STAIRS_SKY_ISLAND, Icons.largeFeelingIcon(Level.Feeling.SKY_ISLAND));
		assertSame(Icons.STAIRS_BARREN, Icons.largeFeelingIcon(Level.Feeling.BARREN));
		assertSame(Icons.STAIRS_CHAOS, Icons.largeFeelingIcon(Level.Feeling.CHAOS));
	}

	@Test
	public void chaosIconsAlwaysUseTheAtlasThatContainsTheirFrames() {
		assertEquals("interfaces/icons.png", Icons.textureFor(Icons.DEPTH_CHAOS));
		assertEquals("interfaces/icons.png", Icons.textureFor(Icons.STAIRS_CHAOS));
	}

	@Test
	public void bothUiStylesContainEveryNewFeelingFrame() throws IOException {
		assertNewFeelingFrames("interfaces/icons.png");
		assertNewFeelingFrames("interfaces/SPD/icons.png");
		assertChaosFeelingFrames("interfaces/icons.png");
	}

	private static void assertChaosFeelingFrames(String relativePath) throws IOException {
		Path asset = coreDirectory().resolve("src/main/assets").resolve(relativePath);
		BufferedImage image = ImageIO.read(asset.toFile());

		assertFrameIsDrawn(image, 32, 128, 15, 16);
		for (int y : new int[]{144, 152}) {
			for (int x : new int[]{16, 80}) {
				assertFrameIsDrawn(image, x, y, 7, 7);
			}
		}
	}

	private static void assertNewFeelingFrames(String relativePath) throws IOException {
		Path asset = coreDirectory().resolve("src/main/assets").resolve(relativePath);
		BufferedImage image = ImageIO.read(asset.toFile());
		assertEquals(256, image.getWidth());
		assertEquals(160, image.getHeight());

		assertFrameIsDrawn(image, 0, 128, 15, 16);
		assertFrameIsDrawn(image, 16, 128, 15, 16);
		for (int y : new int[]{144, 152}) {
			for (int x : new int[]{0, 8, 64, 72}) {
				assertFrameIsDrawn(image, x, y, 7, 7);
			}
		}
	}

	private static void assertFrameIsDrawn(BufferedImage image, int x, int y, int width, int height) {
		for (int yy = y; yy < y + height; yy++) {
			for (int xx = x; xx < x + width; xx++) {
				if ((image.getRGB(xx, yy) >>> 24) != 0) return;
			}
		}
		assertTrue("expected a drawn icon frame at " + x + "," + y, false);
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
	}
}
