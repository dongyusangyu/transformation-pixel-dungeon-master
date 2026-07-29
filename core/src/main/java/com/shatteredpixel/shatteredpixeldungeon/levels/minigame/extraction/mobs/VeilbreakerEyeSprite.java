package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.sprites.EyeSprite;

/**
 * Releases the raid eye's cone through the ordinary zap timing without drawing
 * the parent eye's single-target death-ray beam.
 */
public class VeilbreakerEyeSprite extends EyeSprite {

	@Override
	public void onComplete(Animation anim) {
		if (anim == zap) {
			idle();
			((VeilbreakerEye) ch).deathGaze();
			ch.next();
		} else {
			super.onComplete(anim);
		}
	}
}
