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
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class CorrosiveSwarmSprite extends MobSprite {

	private final Animation burst;

	public CorrosiveSwarmSprite() {
		super();

		texture(Assets.Sprites.CORROSIVE_SWARM);

		TextureFilm frames = new TextureFilm(texture, 16, 16);

		idle = new Animation(15, true);
		idle.frames(frames, 0, 1, 2, 3, 4, 5);

		run = new Animation(15, true);
		run.frames(frames, 0, 1, 2, 3, 4, 5);

		attack = new Animation(20, false);
		attack.frames(frames, 6, 7, 8, 9);

		die = new Animation(15, false);
		die.frames(frames, 10, 11, 12, 13, 14);

		burst = new Animation(24, false);
		burst.frames(frames, 15, 16);

		play(idle);
	}

	public void burst() {
		if (parent == null) {
			play(burst);
			return;
		}

		BurstEffect effect = new BurstEffect();
		effect.x = x;
		effect.y = y;
		effect.flipHorizontal = flipHorizontal;
		effect.visible = visible;
		effect.scale.set(scale.x, scale.y);
		parent.add(effect);
	}

	@Override
	public void onComplete(Animation animation) {
		super.onComplete(animation);
		if (animation == burst) {
			idle();
		}
	}

	@Override
	public int blood() {
		return 0xFFA0B938;
	}

	private static final class BurstEffect extends MovieClip implements MovieClip.Listener {

		private BurstEffect() {
			listener = this;
			texture(Assets.Sprites.CORROSIVE_SWARM);
			TextureFilm frames = new TextureFilm(texture, 16, 16);
			Animation animation = new Animation(24, false);
			animation.frames(frames, 15, 16);
			play(animation);
		}

		@Override
		public void onComplete(Animation animation) {
			killAndErase();
		}
	}
}
