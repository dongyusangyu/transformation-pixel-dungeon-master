package com.shatteredpixel.shatteredpixeldungeon.sprites;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DemonTailWhipSpriteTest {

	@Test
	public void frameUsesIndex154AndMeasuredBounds() {
		assertEquals(154,
				EXItemSpriteSheet.frameFor(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(160,
				EXItemSpriteSheet.frameX(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(144,
				EXItemSpriteSheet.frameY(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(16,
				EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(16,
				EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.DEMON_TAIL_WHIP));
	}

	@Test
	public void redrawnSpriteFillsItsDeclaredBounds()
			throws IOException {
		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		int cellX = 154 % 16 * 16;
		int cellY = 154 / 16 * 16;
		Set<Integer> colors = new HashSet<>();
		int minX = 16;
		int minY = 16;
		int maxX = -1;
		int maxY = -1;

		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int argb = sheet.getRGB(cellX + x, cellY + y);
				int alpha = argb >>> 24;
				if (alpha != 0) {
					colors.add(argb);
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}

		assertFalse("demon tail whip cell must not be empty", colors.isEmpty());
		assertEquals(0, minX);
		assertEquals(0, minY);
		assertEquals(15, maxX);
		assertEquals(15, maxY);
		assertTrue("redrawn whip has a visible palette", colors.size() > 1);
	}

	private static Path exItemSpritePath() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return coreDirectory.resolve("src/main/assets/sprites/ex_items.png");
	}
}
