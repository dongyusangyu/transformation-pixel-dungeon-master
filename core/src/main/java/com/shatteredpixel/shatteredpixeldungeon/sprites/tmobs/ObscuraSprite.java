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
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class ObscuraSprite extends MobSprite {

	private final Animation summon;

	public ObscuraSprite() {
		super();
		texture(Assets.Sprites.OBSCURA);
		TextureFilm frames = new TextureFilm(texture, 16, 16);

		idle = new Animation(2, true);
		idle.frames(frames, 0, 1);

		run = new Animation(10, true);
		run.frames(frames, 2, 3, 4, 5);

		attack = new Animation(12, false);
		attack.frames(frames, 6, 7, 8);

		summon = new Animation(12, false);
		summon.frames(frames, 9, 10, 11, 12);
		zap = summon;

		die = new Animation(12, false);
		die.frames(frames, 13, 14, 15, 16);

		play(idle);
	}

	public void summon() {
		play(summon);
	}

	@Override
	public void onComplete(Animation animation) {
		if (animation == summon) {
			idle();
		}
		super.onComplete(animation);
	}

	@Override
	public int blood() {
		return 0xFF3FDC9F;
	}
}
