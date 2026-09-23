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

	private static final int META_X = 240;
	private static final int META_Y = 288;

	@Test
	public void redrawnExtractionScrollUsesItsOwnCompleteFrame() throws IOException {
		BufferedImage items = readSprite("items.png");
		BufferedImage exItems = readSprite("ex_items.png");

		assertEquals(256, exItems.getWidth());
		assertEquals(512, exItems.getHeight());
		assertEquals(32, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.SCROLL_EXTRACTION));
		assertEquals(13, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.SCROLL_EXTRACTION));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.SCROLL_EXTRACTION));

		boolean changed = false;
		int visible = 0;
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int original = items.getRGB(META_X + x, META_Y + y);
				int extraction = exItems.getRGB(x, 32 + y);
				changed |= original != extraction;
				if ((extraction >>> 24) != 0) {
					visible++;
					assertTrue("scroll art must fit its frame", x < 13);
				}
			}
		}
		assertTrue("redrawn scroll must be visible", visible > 0);
		assertTrue("extraction scroll must remain distinct from SCROLL_META", changed);
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
