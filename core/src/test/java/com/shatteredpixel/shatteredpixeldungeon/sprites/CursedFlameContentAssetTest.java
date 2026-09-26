package com.shatteredpixel.shatteredpixeldungeon.sprites;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CursedFlameContentAssetTest {

	private static final int CELL_SIZE = 16;
	private static final int SHEET_COLUMNS = 16;

	@Test
	public void greenFoodCandidateHasExpectedOpaqueBounds() throws IOException {
		assertOpaqueBounds(readExtensionItemSheet(), 23, 15, 11);
	}

	@Test
	public void pairedEyesCandidateHasExpectedOpaqueBounds() throws IOException {
		assertOpaqueBounds(readExtensionItemSheet(), 274, 15, 15);
	}

	@Test
	public void wandCandidateHasExpectedOpaqueBounds() throws IOException {
		assertOpaqueBounds(readExtensionItemSheet(), 336, 14, 14);
	}

	@Test
	public void candidateCellsDoNotOverlap() {
		assertDisjointCells(23, 274);
		assertDisjointCells(23, 336);
		assertDisjointCells(274, 336);
	}

	private static BufferedImage readExtensionItemSheet() throws IOException {
		Path path = Paths.get("src/main/assets/sprites/ex_items.png");
		if (!path.toFile().isFile()) {
			path = Paths.get("core/src/main/assets/sprites/ex_items.png");
		}
		BufferedImage image = ImageIO.read(path.toFile());
		assertTrue("Could not read extension item sprite sheet at " + path, image != null);
		return image;
	}

	private static void assertOpaqueBounds(BufferedImage sheet, int index,
											   int expectedWidth, int expectedHeight) {
		int originX = index % SHEET_COLUMNS * CELL_SIZE;
		int originY = index / SHEET_COLUMNS * CELL_SIZE;
		assertTrue("Sprite cell " + index + " is outside the sheet",
				originX + CELL_SIZE <= sheet.getWidth() && originY + CELL_SIZE <= sheet.getHeight());

		int minX = CELL_SIZE;
		int minY = CELL_SIZE;
		int maxX = -1;
		int maxY = -1;
		for (int y = 0; y < CELL_SIZE; y++) {
			for (int x = 0; x < CELL_SIZE; x++) {
				if ((sheet.getRGB(originX + x, originY + y) >>> 24) != 0) {
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}

		assertEquals("Unexpected opaque min X in cell " + index, 0, minX);
		assertEquals("Unexpected opaque min Y in cell " + index, 0, minY);
		assertEquals("Unexpected opaque width in cell " + index, expectedWidth, maxX - minX + 1);
		assertEquals("Unexpected opaque height in cell " + index, expectedHeight, maxY - minY + 1);
	}

	private static void assertDisjointCells(int firstIndex, int secondIndex) {
		int firstX = firstIndex % SHEET_COLUMNS;
		int firstY = firstIndex / SHEET_COLUMNS;
		int secondX = secondIndex % SHEET_COLUMNS;
		int secondY = secondIndex / SHEET_COLUMNS;
		boolean disjoint = firstX + 1 <= secondX || secondX + 1 <= firstX
				|| firstY + 1 <= secondY || secondY + 1 <= firstY;
		assertTrue("Sprite cells " + firstIndex + " and " + secondIndex + " overlap", disjoint);
	}
}
