package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MyriadBlackShadow;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MyriadBlackShadowSpriteAssetTest {

	private static final Set<Integer> ALLOWED_COLORS = new HashSet<>(Arrays.asList(
			0x050507, 0x100B16, 0x21132E, 0x3A1B4C, 0x6B2A76));

	@Test
	public void monsterUsesFilteredGuardSpriteAndDedicatedOverlay() {
		assertEquals("sprites/myriad_black_shadow_overlay.png",
				Assets.Sprites.MYRIAD_BLACK_SHADOW_OVERLAY);
		assertSame(AlienatedPrismaticGuardSprite.class,
				MyriadBlackShadowSprite.class.getSuperclass());
		assertSame(MyriadBlackShadowSprite.class, new MyriadBlackShadow().spriteClass);
	}

	@Test
	public void overlayHasFourHardEdgedNonemptyFramesAndSpecifiedPalette() throws Exception {
		BufferedImage image = ImageIO.read(spritePath().toFile());
		assertEquals(64, image.getWidth());
		assertEquals(16, image.getHeight());
		assertTrue(image.getColorModel().hasAlpha());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < 4; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < 16; y++) {
				for (int x = frame * 16; x < (frame + 1) * 16; x++) {
					int argb = image.getRGB(x, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertTrue("overlay frame " + frame + " must not be empty", nonEmpty);
		}
		assertTrue(visibleColors.size() <= 6);
		assertTrue(ALLOWED_COLORS.containsAll(visibleColors));
	}

	@Test
	public void spriteDefinesDistinctStateCellsAndCleansUpItsChildClip() throws Exception {
		String source = Files.readString(spriteSource(), StandardCharsets.UTF_8)
				.replaceAll("\\s+", "");

		assertTrue(source.contains("overlayIdle=newAnimation(1,true).frames(film,0)"));
		assertTrue(source.contains("overlayRun=newAnimation(1,true).frames(film,1)"));
		assertTrue(source.contains("overlayAttack=newAnimation(1,false).frames(film,2)"));
		assertTrue(source.contains("overlayDie=newAnimation(1,false).frames(film,3)"));
		assertTrue(source.contains("animation==attack||animation==zap||animation==operate"));
		assertTrue(source.contains("voidkill()"));
		assertTrue(source.contains("voiddestroy()"));
		assertTrue(source.contains("detachOverlay();"));
	}

	@Test
	public void sixteenPixelOverlayIsCenteredAndFloorAlignedToHeroFrames() {
		assertEquals(-2f, MyriadBlackShadowSprite.overlayOffsetX(12f, 16f), 0.001f);
		assertEquals(-1f, MyriadBlackShadowSprite.overlayOffsetY(15f, 16f), 0.001f);
	}

	private static Path spritePath() {
		return coreDirectory().resolve("src/main/assets/sprites/myriad_black_shadow_overlay.png");
	}

	private static Path spriteSource() {
		return coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/MyriadBlackShadowSprite.java");
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = working.resolve("core");
		return Files.isDirectory(core) ? core : working;
	}
}
