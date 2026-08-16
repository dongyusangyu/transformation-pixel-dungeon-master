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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class MimicCrocodileSprite extends MobSprite {

	public MimicCrocodileSprite() {
		super();

		texture(Assets.Sprites.MIMIC_CROCODILE);
		TextureFilm frames = new TextureFilm(texture, 16, 16);

		idle = new Animation(2, true);
		idle.frames(frames, 0, 1);

		run = new Animation(8, true);
		run.frames(frames, 2, 3, 4, 5, 6, 7);

		attack = new Animation(12, false);
		attack.frames(frames, 8, 9, 10);

		zap = attack.clone();

		die = new Animation(5, false);
		die.frames(frames, 11, 12);

		play(idle);
	}

	@Override
	public void linkVisuals(Char ch) {
		super.linkVisuals(ch);
		applyLurkingAlpha(ch);
	}

	@Override
	public void resetColor() {
		super.resetColor();
		applyLurkingAlpha(ch);
	}

	@Override
	public void play(Animation animation) {
		super.play(animation);
		applyLurkingAlpha(ch);
	}

	@Override
	protected boolean visualEffectsVisible() {
		return super.visualEffectsVisible() && !isLurking();
	}

	private boolean isLurking() {
		return ch instanceof MimicCrocodile && ((MimicCrocodile) ch).isLurking();
	}

	private void applyLurkingAlpha(Char character) {
		if (character instanceof MimicCrocodile
				&& ((MimicCrocodile) character).isLurking()) {
			alpha(MimicCrocodile.LURKING_ALPHA);
		} else {
			alpha(1f);
		}
	}

	@Override
	public int blood() {
		return 0xFF69211D;
	}
}
