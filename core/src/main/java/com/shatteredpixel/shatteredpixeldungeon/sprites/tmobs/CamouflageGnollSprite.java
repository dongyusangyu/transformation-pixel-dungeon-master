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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class CamouflageGnollSprite extends MobSprite {

	private final Animation camouflage;

	public CamouflageGnollSprite() {
		super();

		texture(Assets.Sprites.CAMOUFLAGE_GNOLL);

		TextureFilm frames = new TextureFilm(texture, 12, 16);

		idle = new Animation(2, true);
		idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);

		run = new Animation(12, true);
		run.frames(frames, 4, 5, 6, 7);

		attack = new Animation(12, false);
		attack.frames(frames, 2, 3, 0);

		die = new Animation(12, false);
		die.frames(frames, 8, 9, 10);

		camouflage = new Animation(3, true);
		camouflage.frames(frames, 11, 12);

		play(idle);
	}

	@Override
	public void idle() {
		if (ch instanceof CamouflageGnoll
				&& ((CamouflageGnoll) ch).isCamouflaged()) {
			play(camouflage);
		} else {
			super.idle();
		}
	}
}
