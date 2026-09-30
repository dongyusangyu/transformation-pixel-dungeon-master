package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.sprites.EyeSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RaidDroneSprites;
import com.watabou.noosa.TextureFilm;

/**
 * Releases the raid eye's cone through the ordinary zap timing without drawing
 * the parent eye's single-target death-ray beam.
 */
public class VeilbreakerEyeSprite extends EyeSprite {

	private final RaidDroneSprites.DeathSound deathSound = new RaidDroneSprites.DeathSound();

	public VeilbreakerEyeSprite() {
		TextureFilm frames = RaidDroneSprites.configure(this, 1);
		charging = new Animation(12, true);
		charging.frames(frames, 31, 32);
	}

	@Override public int blood() { return 0xFFBBBBBB; }
	@Override public void die() { deathSound.play(this); super.die(); }

	@Override
	public void onComplete(Animation anim) {
		if (anim == zap) {
			idle();
			if (ch instanceof VeilbreakerEye && ch.isAlive()) {
				((VeilbreakerEye) ch).deathGaze();
				ch.next();
			}
		} else {
			super.onComplete(anim);
		}
	}
}
