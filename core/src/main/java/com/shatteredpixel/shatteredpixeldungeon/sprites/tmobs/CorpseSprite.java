/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class CorpseSprite extends MobSprite {

	public CorpseSprite() {
		super();

		texture(Assets.Sprites.CORPSE);

		TextureFilm frames = new TextureFilm(texture, 12, 15);

		idle = new Animation(2, true);
		idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);

		run = new Animation(20, true);
		run.frames(frames, 2, 3, 4, 5, 6, 7);

		die = new Animation(20, false);
		die.frames(frames, 8, 9, 10, 11, 12, 11);

		attack = new Animation(15, false);
		attack.frames(frames, 13, 14, 15, 0);

		play(idle);
	}
}
