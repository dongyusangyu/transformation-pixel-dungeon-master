package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TapirCrocodile;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import sun.misc.Unsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TapirCrocodileSpriteAssetTest {

	@Test
	public void variantUsesDedicatedTextureAndMimicAnimationBase() {
		assertEquals("sprites/tapir_crocodile.png", Assets.Sprites.TAPIR_CROCODILE);
		assertSame(MimicCrocodileSprite.class, TapirCrocodileSprite.class.getSuperclass());
		assertSame(TapirCrocodileSprite.class, new TapirCrocodile().spriteClass);
	}

	@Test
	public void textureKeepsThirteenSixteenPixelHardEdgedFrames() throws Exception {
		BufferedImage image = ImageIO.read(spritePath().toFile());
		assertEquals(208, image.getWidth());
		assertEquals(16, image.getHeight());

		Set<Integer> colors = new HashSet<>();
		for (int frame = 0; frame < 13; frame++) {
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
		assertTrue(colors.size() <= 16);
	}

	@Test
	public void dedicatedTextureStartsOnOneSixteenPixelIdleFrame() throws Exception {
		HashMap<Object, SmartTexture> textures = textureCache();
		SmartTexture previousBase = textures.put(
				Assets.Sprites.MIMIC_CROCODILE, headlessTexture(208, 16));
		SmartTexture previousTapir = textures.put(
				Assets.Sprites.TAPIR_CROCODILE, headlessTexture(208, 16));
		try {
			TapirCrocodileSprite sprite = new TapirCrocodileSprite();

			assertEquals(16f, sprite.width(), 0f);
			assertEquals(16f, sprite.height(), 0f);
		} finally {
			restoreTexture(textures, Assets.Sprites.MIMIC_CROCODILE, previousBase);
			restoreTexture(textures, Assets.Sprites.TAPIR_CROCODILE, previousTapir);
		}
	}

	private static Path spritePath() {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = working.resolve("core");
		if (!Files.isDirectory(core)) core = working;
		return core.resolve("src/main/assets/sprites/tapir_crocodile.png");
	}

	@SuppressWarnings("unchecked")
	private static HashMap<Object, SmartTexture> textureCache() throws Exception {
		Field field = TextureCache.class.getDeclaredField("all");
		field.setAccessible(true);
		return (HashMap<Object, SmartTexture>) field.get(null);
	}

	private static SmartTexture headlessTexture(int width, int height) throws Exception {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		SmartTexture texture = (SmartTexture) ((Unsafe) field.get(null))
				.allocateInstance(SmartTexture.class);
		texture.width = width;
		texture.height = height;
		return texture;
	}

	private static void restoreTexture(HashMap<Object, SmartTexture> textures,
			Object key, SmartTexture previous) {
		if (previous == null) {
			textures.remove(key);
		} else {
			textures.put(key, previous);
		}
	}
}
