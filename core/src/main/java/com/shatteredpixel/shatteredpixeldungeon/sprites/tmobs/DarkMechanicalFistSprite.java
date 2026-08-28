package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

public class DarkMechanicalFistSprite extends MechanicalFistSprite {

	public DarkMechanicalFistSprite() {
		super();
		texture(Assets.Sprites.DARK_MECHANICAL_FIST);
		updateMechanicalFistFrames();
		play(idle);
	}

	@Override
	public int blood() {
		return 0xFF9B315F;
	}
}
