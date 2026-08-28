/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class MechanicalFistSprite extends MobSprite {

	private static final float SLAM_TIME = 0.33f;

	public MechanicalFistSprite() {
		super();

		texture(Assets.Sprites.MECHANICAL_FIST);
		updateMechanicalFistFrames();
		play(idle);
	}

	/** Builds frames from the texture currently installed on this sprite. */
	protected void updateMechanicalFistFrames() {
		TextureFilm frames = new TextureFilm(texture, 24, 17);

		idle = new Animation(2, true);
		idle.frames(frames, 0, 0, 1);

		run = new Animation(3, true);
		run.frames(frames, 0, 1);

		attack = new Animation(8, false);
		attack.frames(frames, 0, 5, 6, 0);

		zap = attack.clone();

		die = new Animation(10, false);
		die.frames(frames, 0, 2, 3, 4);
	}

	@Override
	public void attack(int cell) {
		super.attack(cell);
		jump(ch.pos, ch.pos, 9, SLAM_TIME, null);
	}

	@Override
	public void onComplete(Animation animation) {
		if (animation == attack) {
			PixelScene.shake(4, 0.2f);
		}
		super.onComplete(animation);
	}

	@Override
	public int blood() {
		return 0xFF22A3AE;
	}
}
