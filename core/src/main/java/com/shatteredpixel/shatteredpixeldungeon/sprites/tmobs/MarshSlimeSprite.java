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

public class MarshSlimeSprite extends MobSprite {

	public MarshSlimeSprite() {
		texture(Assets.Sprites.MARSH_SLIME);

		TextureFilm frames = new TextureFilm(texture, 14, 12);

		idle = new Animation(3, true);
		idle.frames(frames, 0, 1, 1, 0);

		run = new Animation(10, true);
		run.frames(frames, 0, 2, 3, 3, 2, 0);

		attack = new Animation(15, false);
		attack.frames(frames, 2, 3, 4, 6, 5);

		die = new Animation(10, false);
		die.frames(frames, 0, 5, 6, 7);

		play(idle);
	}

	@Override
	public int blood() {
		return 0xFF66743B;
	}
}
