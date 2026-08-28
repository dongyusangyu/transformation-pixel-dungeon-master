package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DeathButterfly;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DeathButterflySpriteAssetTest {

	@Test
	public void monsterUsesDedicatedSixteenPixelSprite() {
		DeathButterfly butterfly = new DeathButterfly();

		assertEquals("sprites/death_butterfly.png", Assets.Sprites.DEATH_BUTTERFLY);
		assertSame(DeathButterflySprite.class, butterfly.spriteClass);
	}

	@Test
	public void sheetHasFifteenHardEdgedFramesAndLimitedPalette() throws Exception {
		Path asset = coreDirectory().resolve("src/main/assets/sprites/death_butterfly.png");
		BufferedImage image = ImageIO.read(asset.toFile());
		assertEquals(240, image.getWidth());
		assertEquals(16, image.getHeight());

		Set<Integer> colors = new HashSet<>();
		for (int frame = 0; frame < 15; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < 16; y++) {
				for (int x = frame * 16; x < (frame + 1) * 16; x++) {
					int argb = image.getRGB(x, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						colors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertTrue("frame " + frame + " must not be empty", nonEmpty);
		}
		assertTrue(colors.size() <= 12);
	}

	@Test
	public void spriteUsesTheConfirmedSwarmFrameTiming() throws Exception {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/DeathButterflySprite.java");
		String java = new String(Files.readAllBytes(source), StandardCharsets.UTF_8)
				.replaceAll("\\s+", "");

		assertTrue(java.contains("newTextureFilm(texture,16,16)"));
		assertTrue(java.contains("idle=newAnimation(15,true)"));
		assertTrue(java.contains("idle.frames(frames,0,1,2,3,4,5)"));
		assertTrue(java.contains("run.frames(frames,0,1,2,3,4,5)"));
		assertTrue(java.contains("attack=newAnimation(20,false)"));
		assertTrue(java.contains("attack.frames(frames,6,7,8,9)"));
		assertTrue(java.contains("die.frames(frames,10,11,12,13,14)"));
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
