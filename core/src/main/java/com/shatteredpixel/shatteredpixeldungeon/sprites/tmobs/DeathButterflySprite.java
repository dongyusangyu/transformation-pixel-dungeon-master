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

public class DeathButterflySprite extends MobSprite {

	public DeathButterflySprite() {
		super();

		texture(Assets.Sprites.DEATH_BUTTERFLY);
		TextureFilm frames = new TextureFilm(texture, 16, 16);

		idle = new Animation(15, true);
		idle.frames(frames, 0, 1, 2, 3, 4, 5);

		run = new Animation(15, true);
		run.frames(frames, 0, 1, 2, 3, 4, 5);

		attack = new Animation(20, false);
		attack.frames(frames, 6, 7, 8, 9);

		die = new Animation(15, false);
		die.frames(frames, 10, 11, 12, 13, 14);

		play(idle);
	}

	@Override
	public int blood() {
		return 0xFF55406B;
	}
}
