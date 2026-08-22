package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import org.junit.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.watabou.noosa.MovieClip.Animation;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class GentlemanElfSpriteAssetTest {
	@Test public void cupIdleAnimationCannotBeMistakenForAttackOrDeath() {
		Animation[] animations = ElfWineCupSprite.createAnimations(null);

		assertNotSame(animations[0], animations[1]);
		assertNotSame(animations[0], animations[2]);
		assertNotSame(animations[0], animations[3]);
		assertTrue(animations[0].looped);
		assertFalse(animations[2].looped);
		assertFalse(animations[3].looped);
	}

	@Test public void bossSheetHasTwentyFiveHardEdgedThirtyTwoPixelFrames() throws Exception {
        BufferedImage image = ImageIO.read(path("sprites/gentleman_elf.png").toFile());
        assertEquals(800, image.getWidth()); assertEquals(32, image.getHeight());
        for (int frame = 0; frame < 25; frame++) {
            int visible = 0;
            for (int y=0;y<32;y++) for (int x=frame*32;x<frame*32+32;x++) {
                int argb=image.getRGB(x,y), alpha=argb>>>24;
                assertTrue(alpha==0 || alpha==255);
                if(alpha==255) visible++;
                else assertEquals(0,argb&0x00FFFFFF);
            }
            assertTrue("empty frame "+frame, visible >= 20);
        }
    }
	@Test public void cupIsAStaticSixteenPixelFrame() throws Exception {
		BufferedImage cup=ImageIO.read(path("sprites/elf_wine_cup.png").toFile());
		assertEquals(16,cup.getWidth()); assertEquals(16,cup.getHeight());
		int visible = 0;
		for (int y = 0; y < cup.getHeight(); y++) for (int x = 0; x < cup.getWidth(); x++) {
			if ((cup.getRGB(x, y) >>> 24) != 0) visible++;
		}
		assertTrue("wine cup sprite must contain visible pixels", visible > 0);
	}

	@Test public void defaultPoseFacesRightAndKeepsTieReadable() throws Exception {
		BufferedImage image = ImageIO.read(path("sprites/gentleman_elf.png").toFile());
		assertEquals(0xFF0A1F1F, image.getRGB(29, 10));
		assertEquals(0xFF070E14, image.getRGB(21, 20));
		for (int frame = 0; frame < 20; frame++) {
			int tiePixels = 0;
			for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
				if (image.getRGB(frame * 32 + x, y) == 0xFF070E14) tiePixels++;
			}
			assertTrue("tie disappeared in frame " + frame, tiePixels >= 17);
		}
	}

	@Test public void majorActionsUseDistinctSilhouettes() throws Exception {
		BufferedImage image = ImageIO.read(path("sprites/gentleman_elf.png").toFile());
		Set<Integer> silhouettes = new HashSet<>();
		for (int frame : new int[]{0, 7, 8, 11, 14, 17, 22, 24}) {
			int hash = 1;
			for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
				hash = 31 * hash + (image.getRGB(frame * 32 + x, y) >>> 24 == 0 ? 0 : 1);
			}
			silhouettes.add(hash);
		}
		assertEquals(8, silhouettes.size());
	}
	private static Path path(String relative) {
        Path cwd=Paths.get(System.getProperty("user.dir"));
        Path root=Files.isDirectory(cwd.resolve("core"))?cwd:cwd.getParent();
        return root.resolve("core/src/main/assets").resolve(relative);
    }

}
