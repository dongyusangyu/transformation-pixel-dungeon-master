package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.utils.Callback;
import com.watabou.noosa.TextureFilm;

/** 32px gentleman sprite; animation frames are kept in one deterministic strip. */
public class GentlemanElfSprite extends MobSprite {
	private final Animation dash;
	private final Animation leap;

	public GentlemanElfSprite() {
		texture(Assets.Sprites.GENTLEMAN_ELF);
		TextureFilm film = new TextureFilm(texture, 32, 32);
		idle = new Animation(3, true); idle.frames(film, 0, 1, 2);
		run = new Animation(6, true); run.frames(film, 3, 4, 5, 6);
		attack = new Animation(10, false); attack.frames(film, 7, 8, 9);
		zap = new Animation(8, false); zap.frames(film, 10, 11, 12);
		dash = new Animation(12, false); dash.frames(film, 13, 14, 15);
		operate = dash;
		leap = new Animation(10, false); leap.frames(film, 16, 17, 18);
		die = new Animation(8, false); die.frames(film, 20, 21, 22, 23, 24);
		play(idle);
	}
	public void toast() { play(zap); }
	public void devour() { play(dash); }
	public void dash() { play(dash); }
	@Override public void jump(int from, int to, float height, float duration, Callback callback) {
		play(leap);
		super.jump(from, to, height, duration, callback);
	}
	@Override public int blood() { return 0xFF4B8C50; }
}
