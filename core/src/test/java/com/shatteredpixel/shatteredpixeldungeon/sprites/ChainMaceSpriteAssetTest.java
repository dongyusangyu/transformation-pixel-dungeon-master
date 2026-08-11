package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.ChainMace;

import org.junit.Test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChainMaceSpriteAssetTest {

	@Test
	public void suppliedBallIsOneHardEdgedThirteenByThirteenDrawing() throws IOException {
		BufferedImage image = ImageIO.read(spritesDirectory().resolve("ball.png").toFile());

		assertEquals(16, image.getWidth());
		assertEquals(16, image.getHeight());
		assertEquals(new Rectangle(1, 3, 13, 13), opaqueBounds(image));
		assertNoPartialAlpha(image);
	}

	@Test
	public void nestedFollowerSpriteLoadsTheSuppliedAssetAsOneFrame() throws IOException {
		assertTrue(MobSprite.class.isAssignableFrom(ChainMace.BallFollowerSprite.class));

		String source = chainMaceSource();
		assertTrue(source.contains("texture(\"sprites/ball.png\")"));
		assertTrue(source.contains("new TextureFilm(texture, 16, 16)"));
		assertTrue(source.contains("idle.frames(frames, 0)"));
		assertTrue(source.contains("run = idle.clone()"));
		assertTrue(source.contains("attack = idle.clone()"));
		assertTrue(source.contains("die = idle.clone()"));
	}

	@Test
	public void chainMaceItemCellIsCrispLimitedAndFillsItsEncodedBounds()
			throws IOException {
		BufferedImage image = ImageIO.read(spritesDirectory().resolve("ex_items.png").toFile());
		int cellX = 48;
		int cellY = 144;
		Set<Integer> colors = new HashSet<>();
		int minX = 16;
		int minY = 16;
		int maxX = -1;
		int maxY = -1;

		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int argb = image.getRGB(cellX + x, cellY + y);
				int alpha = argb >>> 24;
				assertTrue(alpha == 0 || alpha == 255);
				if (alpha == 255) {
					colors.add(argb);
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}

		assertEquals(0, minX);
		assertEquals(0, minY);
		assertEquals(15, maxX);
		assertEquals(15, maxY);
		assertTrue(colors.size() <= 8);
	}

	private static Rectangle opaqueBounds(BufferedImage image) {
		int minX = image.getWidth();
		int minY = image.getHeight();
		int maxX = -1;
		int maxY = -1;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}
		return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}

	private static void assertNoPartialAlpha(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int alpha = image.getRGB(x, y) >>> 24;
				assertTrue("partial alpha at " + x + "," + y,
						alpha == 0 || alpha == 255);
			}
		}
	}

	private static String chainMaceSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/"
						+ "tier6/ChainMace.java")), StandardCharsets.UTF_8);
	}

	private static Path spritesDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return coreDirectory.resolve("src/main/assets/sprites");
	}
}
