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

public class TapirCrocodileSprite extends MimicCrocodileSprite {

	private static final int DREAM_TINT = 0x76518F;

	public TapirCrocodileSprite() {
		super();
		texture(Assets.Sprites.TAPIR_CROCODILE);
		updateMimicCrocodileFrames();
		play(idle);
		applyDreamTint();
	}

	@Override
	public void linkVisuals(Char ch) {
		super.linkVisuals(ch);
		applyDreamTint();
	}

	@Override
	public void resetColor() {
		super.resetColor();
		applyDreamTint();
	}

	private void applyDreamTint() {
		hardlight(DREAM_TINT);
	}

	@Override
	public int blood() {
		return 0xFF5C315F;
	}
}
