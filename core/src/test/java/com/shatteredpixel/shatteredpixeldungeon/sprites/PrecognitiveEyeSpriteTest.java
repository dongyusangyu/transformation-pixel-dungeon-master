package com.shatteredpixel.shatteredpixeldungeon.sprites;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrecognitiveEyeSpriteTest {

	@Test
	public void eyeUsesTheReservedArtifactCellWithoutFillingMissileReservations() throws Exception {
		assertEquals(272, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.PRECOGNITIVE_EYE));
		File sheetFile = new File("src/main/assets/sprites/ex_items.png");
		if (!sheetFile.isFile()) sheetFile = new File("core/src/main/assets/sprites/ex_items.png");
		BufferedImage sheet = ImageIO.read(sheetFile);
		assertEquals(256, sheet.getWidth());
		assertEquals(512, sheet.getHeight());

		int opaque = 0;
		for (int y = 272 / 16 * 16; y < 272 / 16 * 16 + 16; y++) {
			for (int x = 0; x < 16; x++) {
				int alpha = sheet.getRGB(x, y) >>> 24;
				assertTrue(alpha == 0 || alpha == 255);
				if (alpha == 255) opaque++;
			}
		}
		assertTrue("预知眼的 16x16 格必须包含不透明像素", opaque > 0);
	}

}
