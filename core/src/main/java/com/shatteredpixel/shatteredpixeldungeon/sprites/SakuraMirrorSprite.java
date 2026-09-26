package com.shatteredpixel.shatteredpixeldungeon.sprites;

public class SakuraMirrorSprite extends MirrorSprite {

	public SakuraMirrorSprite() {
		super();
		applyTint();
	}

	@Override
	public void resetColor() {
		super.resetColor();
		applyTint();
	}

	private void applyTint() {
		tint(0.35f, 0.02f, 0.02f, 0.45f);
	}
}
