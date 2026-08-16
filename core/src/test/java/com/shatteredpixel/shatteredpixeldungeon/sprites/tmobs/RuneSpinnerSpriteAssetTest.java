package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuneSpinnerSpriteAssetTest {

	@Test
	public void runeSpinnerSheetPreservesSixteenPixelFrameGrid() throws Exception {
		Path asset = Paths.get("src/main/assets/sprites/rune_spinner.png");
		assertTrue(Files.exists(asset));
		BufferedImage image = ImageIO.read(asset.toFile());
		assertEquals(256, image.getWidth());
		assertEquals(16, image.getHeight());
	}

	@Test
	public void spriteReusesApprovedSpinnerAnimationLayout() throws Exception {
		Path source = Paths.get("src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/RuneSpinnerSprite.java");
		assertTrue(Files.exists(source));
		String java = new String(Files.readAllBytes(source), StandardCharsets.UTF_8)
				.replaceAll("\\s+", "");
		assertTrue(java.contains("newTextureFilm(texture,16,16)"));
		assertTrue(java.contains("idle.frames(frames,0,0,0,0,0,1,0,1)"));
		assertTrue(java.contains("run.frames(frames,0,2,0,3)"));
		assertTrue(java.contains("attack.frames(frames,0,4,5,0)"));
		assertTrue(java.contains("die.frames(frames,6,7,8,9)"));
	}
}
