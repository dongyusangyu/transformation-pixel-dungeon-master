/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class HuntressBossSprite extends MobSprite {

	public HuntressBossSprite() {
		super();

		texture(Assets.Sprites.HUNTRESSBOSS);
		TextureFilm frames = new TextureFilm(texture, 12, 15);

		idle = new Animation(1, true);
		idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);

		run = new Animation(20, true);
		run.frames(frames, 2, 3, 4, 5, 6, 7);

		die = new Animation(20, false);
		die.frames(frames, 8, 9, 10, 11, 12, 11);

		attack = new Animation(15, false);
		attack.frames(frames, 13, 14, 15, 0);

		zap = attack.clone();
		play(idle);
	}

	@Override
	public void update() {
		super.update();
		if (isState(State.INVISIBLE)) {
			alpha(0.35f);
		}
	}
}
