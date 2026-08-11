package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import org.junit.Test;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ObscuraSpriteAssetTest {

	@Test
	public void spriteSheetsUseSixteenPixelRgbaFramesWithLimitedPalettes() throws IOException {
		assertSheet("obscura.png", 17);
		assertSheet("wild_dread.png", 12);
	}

	@Test
	public void everyAnimationFrameContainsVisiblePixels() throws IOException {
		assertEveryFrameVisible("obscura.png", 17);
		assertEveryFrameVisible("wild_dread.png", 12);
	}

	@Test
	public void spriteClassesAndAssetConstantsMapAllApprovedAnimations() throws IOException {
		Path sourceRoot = coreDirectory().resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon");
		String assets = read(sourceRoot.resolve("Assets.java"));
		String obscura = read(sourceRoot.resolve("sprites/tmobs/ObscuraSprite.java"));
		String dread = read(sourceRoot.resolve("sprites/tmobs/WildDreadSprite.java"));

		assertTrue(assets.contains("OBSCURA = \"sprites/obscura.png\""));
		assertTrue(assets.contains("WILD_DREAD = \"sprites/wild_dread.png\""));
		assertTrue(obscura.contains("frames, 0, 1"));
		assertTrue(obscura.contains("frames, 2, 3, 4, 5"));
		assertTrue(obscura.contains("frames, 6, 7, 8"));
		assertTrue(obscura.contains("frames, 9, 10, 11, 12"));
		assertTrue(obscura.contains("frames, 13, 14, 15, 16"));
		assertTrue(dread.contains("frames, 0, 1"));
		assertTrue(dread.contains("frames, 2, 3, 4, 5"));
		assertTrue(dread.contains("frames, 6, 7, 8"));
		assertTrue(dread.contains("frames, 9, 10, 11"));
		assertTrue(dread.contains("frames, 11, 10, 9, 0"));
	}

	@Test
	public void nearestNeighbourPreviewsExistAtEightTimesNativeSize() throws IOException {
		BufferedImage obscura = ImageIO.read(pixelArtDirectory().resolve("obscura_preview.png").toFile());
		BufferedImage dread = ImageIO.read(pixelArtDirectory().resolve("wild_dread_preview.png").toFile());

		assertNotNull(obscura);
		assertNotNull(dread);
		assertEquals(17 * 16 * 8, obscura.getWidth());
		assertEquals(16 * 8, obscura.getHeight());
		assertEquals(12 * 16 * 8, dread.getWidth());
		assertEquals(16 * 8, dread.getHeight());
	}

	private static void assertSheet(String fileName, int frames) throws IOException {
		BufferedImage image = ImageIO.read(spriteDirectory().resolve(fileName).toFile());
		assertNotNull(image);
		assertEquals(frames * 16, image.getWidth());
		assertEquals(16, image.getHeight());
		assertTrue(image.getColorModel().hasAlpha());

		Set<Integer> palette = new HashSet<>();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				palette.add(image.getRGB(x, y));
			}
		}
		assertTrue("palette must remain readable at 1x", palette.size() <= 16);
	}

	private static void assertEveryFrameVisible(String fileName, int frames) throws IOException {
		BufferedImage image = ImageIO.read(spriteDirectory().resolve(fileName).toFile());
		for (int frame = 0; frame < frames; frame++) {
			boolean visible = false;
			for (int y = 0; y < 16 && !visible; y++) {
				for (int x = frame * 16; x < frame * 16 + 16; x++) {
					if (((image.getRGB(x, y) >>> 24) & 0xFF) != 0) {
						visible = true;
						break;
					}
				}
			}
			assertTrue(fileName + " frame " + frame + " is blank", visible);
		}
	}

	private static String read(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	private static Path spriteDirectory() {
		return coreDirectory().resolve("src/main/assets/sprites");
	}

	private static Path pixelArtDirectory() {
		return repositoryRoot().resolve("docs/pixel-art/obscura");
	}

	private static Path coreDirectory() {
		Path root = repositoryRoot();
		Path core = root.resolve("core");
		return Files.isDirectory(core) ? core : root;
	}

	private static Path repositoryRoot() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working : working.getParent();
	}
}
