package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

public class ChainShadowThiefSprite extends MobSprite {

	public ChainShadowThiefSprite() {
		super();

		texture(Assets.Sprites.CHAIN_SHADOW_THIEF);
		TextureFilm film = new TextureFilm(texture, 12, 13);

		idle = new Animation(1, true);
		idle.frames(film, 0, 0, 0, 1, 0, 0, 0, 0, 1);

		run = new Animation(15, true);
		run.frames(film, 0, 0, 2, 3, 3, 4);

		die = new Animation(10, false);
		die.frames(film, 5, 6, 7, 8, 9);

		attack = new Animation(12, false);
		attack.frames(film, 10, 11, 12, 0);

		idle();
	}

	@Override
	public int blood() {
		return 0xFF6F7880;
	}
}
