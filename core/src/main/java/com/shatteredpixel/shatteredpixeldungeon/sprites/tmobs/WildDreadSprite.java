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

public class WildDreadSprite extends MobSprite {

	private final Animation down;
	private final Animation revive;

	public WildDreadSprite() {
		super();
		texture(Assets.Sprites.WILD_DREAD);
		TextureFilm frames = new TextureFilm(texture, 16, 16);

		idle = new Animation(2, true);
		idle.frames(frames, 0, 1);

		run = new Animation(12, true);
		run.frames(frames, 0, 4,5,6);

		attack = new Animation(12, false);
		attack.frames(frames, 0,1, 2, 3);

		down = new Animation(5, false);
		down.frames(frames, 0, 10, 9, 8, 7,11);

		revive = new Animation(5, false);
		revive.frames(frames, 7, 8, 9, 10, 0);

		die = down.clone();
		play(idle);
	}

	public void down() {
		play(down);
	}

	public void revive() {
		play(revive);
	}

	@Override
	public void onComplete(Animation animation) {
		if (animation == revive) {
			idle();
		}
		super.onComplete(animation);
	}

	@Override
	public int blood() {
		return 0xFF3FDC9F;
	}
}
