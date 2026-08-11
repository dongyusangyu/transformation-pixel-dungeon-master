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

public class EarthlySerpentSprite extends MobSprite {

	private final Animation spit;
	private final Animation pull;
	private final Animation warning;

	public EarthlySerpentSprite() {
		super();

		texture(Assets.Sprites.EARTHLY_SERPENT);
		TextureFilm frames = new TextureFilm(texture, 12, 11);

		idle = new Animation(10, true);
		idle.frames(frames,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 1, 1);

		run = new Animation(8, true);
		run.frames(frames, 4, 5, 6, 7);

		attack = new Animation(15, false);
		attack.frames(frames, 8, 9, 10, 9, 0);

		die = new Animation(10, false);
		die.frames(frames, 11, 12, 13);

		spit = new Animation(15, false);
		spit.frames(frames, 14, 15, 16, 0);

		pull = new Animation(12, false);
		pull.frames(frames, 17, 18, 19, 20);

		warning = new Animation(8, true);
		warning.frames(frames, 20, 19);

		play(idle);
	}

	@Override
	public void zap(int cell) {
		spit(cell);
	}

	public void spit(int cell) {
		if (ch != null) {
			turnTo(ch.pos, cell);
		}
		play(spit);
	}

	public void pull(int cell) {
		if (ch != null) {
			turnTo(ch.pos, cell);
		}
		play(pull);
	}

	public void warning(boolean active) {
		if (active) {
			play(warning);
		} else {
			idle();
		}
	}

	@Override
	public void onComplete(Animation animation) {
		super.onComplete(animation);
		if (animation == spit) {
			idle();
		} else if (animation == pull) {
			play(warning);
		}
	}

	@Override
	public int blood() {
		return 0xFF99B83A;
	}
}
