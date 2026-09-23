package com.shatteredpixel.shatteredpixeldungeon.sprites;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NecronomiconSpriteTest {

	@Test
	public void redrawnBookUsesItsDedicatedArtifactCell() throws Exception {
		int frame = EXItemSpriteSheet.NECRONOMICON;
		assertEquals(273, EXItemSpriteSheet.frameFor(frame));
		assertEquals(16, EXItemSpriteSheet.frameX(frame));
		assertEquals(272, EXItemSpriteSheet.frameY(frame));
		assertEquals(13, EXItemSpriteSheet.frameWidth(frame));
		assertEquals(16, EXItemSpriteSheet.frameHeight(frame));

		File cwd = new File(System.getProperty("user.dir"));
		File core = cwd.getName().equals("core") ? cwd : new File(cwd, "core");
		BufferedImage sheet = ImageIO.read(new File(core, "src/main/assets/sprites/ex_items.png"));
		int visible = 0;
		for (int y = 272; y < 288; y++) {
			for (int x = 16; x < 32; x++) {
				if ((sheet.getRGB(x, y) >>> 24) == 0) continue;
				visible++;
				assertTrue("book art must fit its declared width", x < 29);
			}
		}
		assertTrue("book art must remain visible", visible > 40);
	}
}
