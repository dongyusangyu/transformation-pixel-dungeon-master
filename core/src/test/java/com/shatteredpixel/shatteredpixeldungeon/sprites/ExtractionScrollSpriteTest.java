package com.shatteredpixel.shatteredpixeldungeon.sprites;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtractionScrollSpriteTest {

	private static final int FRAME_SIZE = 16;
	private static final int EXTRACT_X = 0;
	private static final int EXTRACT_Y = 32;
	private static final int META_X = 240;
	private static final int META_Y = 288;

	@Test
	public void extractionScrollPreservesMetaScrollOutsideItsCenter() throws IOException {
		BufferedImage items = readSprite("items.png");
		BufferedImage exItems = readSprite("ex_items.png");

		assertEquals(256, exItems.getWidth());
		assertEquals(512, exItems.getHeight());

		boolean centerChanged = false;
		for (int y = 0; y < FRAME_SIZE; y++) {
			for (int x = 0; x < FRAME_SIZE; x++) {
				int original = items.getRGB(META_X + x, META_Y + y);
				int extraction = exItems.getRGB(EXTRACT_X + x, EXTRACT_Y + y);
				if (insideSymbol(x, y)) {
					centerChanged |= original != extraction;
				} else {
					assertEquals("Unexpected change at " + x + "," + y,
							original, extraction);
				}
			}
		}
		assertTrue("The extraction symbol must differ from SCROLL_META", centerChanged);
	}

	private static boolean insideSymbol(int x, int y) {
		return x >= 5 && x <= 9 && y >= 4 && y <= 8;
	}

	private static BufferedImage readSprite(String fileName) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		return ImageIO.read(coreDirectory.resolve(
				"src/main/assets/sprites/" + fileName).toFile());
	}
}
