package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.watabou.noosa.MovieClip.Animation;
import com.watabou.utils.RectF;
import org.junit.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class RaidDroneSpritesTest {
	@Test public void dronesUseFourSeparateRowsAndSixteenPixelFrames() throws Exception {
		try (HeadlessItemSprites sheets = sheets()) {
			Mob[] mobs = {new VaultArmoredStatue(), new VeilbreakerEye(),
					new ChronoSuccubus(), new DeferredScorpio()};
			for (int row = 0; row < mobs.length; row++) {
				CharSprite sprite = mobs[row].sprite();
				assertSame(com.watabou.gltextures.TextureCache.get("sprites/UES_drones.png"), sprite.texture);
				assertEquals(16, sprite.width, 0.001f);
				assertEquals(16, sprite.height, 0.001f);
				assertFrames(sprite.idle, row, new int[]{0, 1});
				assertFrames(sprite.run, row, new int[]{2, 3, 4, 5, 6, 7});
				assertFrames(sprite.attack, row, new int[]{13, 14, 15, 16, 17, 0});
				assertFrames(sprite.die, row, new int[]{8, 9, 10, 11, 12});
				if (sprite instanceof StatueSprite) {
					((StatueSprite) sprite).setArmor(5);
					assertFrames(sprite.idle, row, new int[]{0, 1});
				}
				if (sprite instanceof VeilbreakerEyeSprite) {
					Field field = EyeSprite.class.getDeclaredField("charging");
					field.setAccessible(true);
					assertFrames((Animation) field.get(sprite), row, new int[]{13, 14});
				}
			}
		}
	}

	@Test public void droneBulletUsesTheIronGunBulletWithoutRotation() {
		try (HeadlessItemSprites sheets = sheets()) {
			DronesSprite.Bullet bullet = new DronesSprite.Bullet();
			assertEquals(ItemSpriteSheet.TAMARU, bullet.image);
			assertEquals(0, MissileSprite.angularSpeedFor(bullet));
		}
	}

	private static void assertFrames(Animation animation, int row, int[] columns) {
		assertEquals(columns.length, animation.frames.length);
		for (int i = 0; i < columns.length; i++) {
			RectF frame = animation.frames[i];
			assertEquals(columns[i] / 18f, frame.left, 0.0001f);
			assertEquals(row / 4f, frame.top, 0.0001f);
			assertEquals(1 / 18f, frame.width(), 0.0001f);
			assertEquals(1 / 4f, frame.height(), 0.0001f);
		}
	}

	static HeadlessItemSprites sheets() {
		HeadlessItemSprites sheets = new HeadlessItemSprites();
		try {
			for (String asset : new String[]{Assets.Sprites.STATUE, Assets.Sprites.EYE,
					Assets.Sprites.SUCCUBUS, Assets.Sprites.SCORPIO, "sprites/UES_drones.png"}) {
				File file = new File("src/main/assets", asset);
				if (!file.exists()) file = new File("core/src/main/assets", asset);
				BufferedImage image = ImageIO.read(file);
				sheets.addSheet(asset, image.getWidth(), image.getHeight());
			}
			return sheets;
		} catch (Exception error) { sheets.close(); throw new AssertionError(error); }
	}
}
