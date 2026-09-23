package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestBag;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestBag1;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import sun.misc.Unsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class WndBagIconTest {

	@Test
	public void specialBagsHaveDistinctTabIcons() throws Exception {
		assertSame(Icons.HIKING_BACKPACK, WndBag.iconType(emptyBag(HikingBackpack.class)));
		assertSame(Icons.TEST_BAG_1, WndBag.iconType(emptyBag(TestBag.class)));
		assertSame(Icons.TEST_BAG_2, WndBag.iconType(emptyBag(TestBag1.class)));
		assertSame(Icons.SCROLL_HOLDER, WndBag.iconType(emptyBag(ScrollHolder.class)));
	}

	@Test
	public void bothThemesContainMatchingBagIconsAndUpdatedPortrait() throws IOException {
		Path assets = coreDirectory().resolve("src/main/assets/interfaces");
		BufferedImage normal = ImageIO.read(assets.resolve("icons.png").toFile());
		BufferedImage spd = ImageIO.read(assets.resolve("SPD/icons.png").toFile());
		assertEquals(256, spd.getWidth());
		assertEquals(160, spd.getHeight());

		for (int x = 226; x < 256; x++) {
			for (int y = 80; y < 90; y++) {
				assertEquals("bag icon pixel " + x + "," + y,
						normal.getRGB(x, y), spd.getRGB(x, y));
			}
		}
		for (int x : new int[]{226, 236, 246}) {
			assertTrue("bag icon at " + x + " is empty", hasVisiblePixel(spd, x, 80, 10, 10));
		}
		for (int x = 64; x < 96; x++) {
			for (int y = 96; y < 128; y++) {
				assertEquals("portrait pixel " + x + "," + y,
						normal.getRGB(x, y), spd.getRGB(x, y));
			}
		}
	}

	private static boolean hasVisiblePixel(BufferedImage image, int x, int y, int width, int height) {
		for (int yy = y; yy < y + height; yy++) {
			for (int xx = x; xx < x + width; xx++) {
				if ((image.getRGB(xx, yy) >>> 24) != 0) return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Bag> T emptyBag(Class<T> type) throws Exception {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (T) ((Unsafe) field.get(null)).allocateInstance(type);
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
	}
}
