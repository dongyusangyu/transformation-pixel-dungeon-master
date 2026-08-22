package com.shatteredpixel.shatteredpixeldungeon.sprites.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip.Animation;
import com.watabou.noosa.TextureFilm;

public class ElfWineCupSprite extends MobSprite {
	public ElfWineCupSprite() {
		texture(Assets.Sprites.ELF_WINE_CUP);
		TextureFilm film = new TextureFilm(texture, 16, 16);
		Animation[] animations = createAnimations(film);
		idle = animations[0];
		run = animations[1];
		attack = animations[2];
		die = animations[3];
		play(idle);
	}

	static Animation[] createAnimations(TextureFilm film) {
		Animation idle = new Animation(2, true);
		Animation attack = new Animation(2, false);
		Animation die = new Animation(2, false);
		if (film != null) {
			idle.frames(film, 0);
			attack.frames(film, 0);
			die.frames(film, 0);
		}
		Animation run = idle.clone();
		return new Animation[]{idle, run, attack, die};
	}
}
