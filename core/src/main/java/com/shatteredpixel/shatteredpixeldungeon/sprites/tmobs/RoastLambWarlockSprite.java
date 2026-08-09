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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class RoastLambWarlockSprite extends MobSprite {

	public RoastLambWarlockSprite() {
		super();

		texture(Assets.Sprites.ROAST_LAMB_WARLOCK);
		TextureFilm frames = new TextureFilm(texture, 12, 15);

		idle = new Animation(2, true);
		idle.frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);

		run = new Animation(15, true);
		run.frames(frames, 0, 2, 3, 4);

		attack = new Animation(12, false);
		attack.frames(frames, 0, 5, 6);

		zap = attack.clone();

		die = new Animation(15, false);
		die.frames(frames, 0, 7, 8, 8, 9, 10);

		play(idle);
	}

	public void flock(int cell) {
		beginCast(cell);
		if (Dungeon.level != null) {
			for (int offset : PathFinder.NEIGHBOURS9) {
				int woolCell = cell + offset;
				if (Dungeon.level.insideMap(woolCell)) {
					CellEmitter.get(woolCell).burst(Speck.factory(Speck.WOOL), 2);
				}
			}
		}
	}

	public void fireblast(int cell) {
		beginCast(cell);
		if (ch instanceof RoastLambWarlock && parent != null) {
			ConeAOE cone = ((RoastLambWarlock) ch).fireCone(cell);
			for (Ballistica ray : cone.outerRays) {
				if (ray.dist > 0) {
					((MagicMissile) parent.recycle(MagicMissile.class)).reset(
							MagicMissile.FIRE_CONE,
							this,
							ray.path.get(ray.dist),
							null);
				}
			}
		}
	}

	private void beginCast(int cell) {
		super.zap(cell, new Callback() {
			@Override
			public void call() {
				if (ch instanceof RoastLambWarlock) {
					((RoastLambWarlock) ch).onCastComplete();
				}
			}
		});
	}

	@Override
	public void onComplete(Animation animation) {
		if (animation == zap) {
			idle();
		}
		super.onComplete(animation);
	}
}
