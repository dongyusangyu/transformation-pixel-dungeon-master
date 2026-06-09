package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class RatKingHeroSprite extends HeroSprite {

    private static final int FRAME_WIDTH = 16;
    private static final int FRAME_HEIGHT = 17;
    public static int frameWidth() {  return FRAME_WIDTH;  }
    public static int frameHeight() {
        return FRAME_HEIGHT;
    }

    @Override
    public void updateArmor() {
        TextureFilm film = new TextureFilm( tiers(), 0, FRAME_WIDTH, FRAME_HEIGHT );

        // 使用您原来的帧索引（完全未改动）
        idle = new Animation(2, true);
        idle.frames(film, 0, 0, 0, 1);

        run = new Animation(RUN_FRAMERATE, true);
        run.frames(film, 6, 7, 8, 9, 10);

        attack = new Animation(15, false);
        attack.frames(film, 2, 3, 4, 5, 0);

        die = new Animation(10, false);
        die.frames(film, 11, 12, 13, 14);

        zap = attack.clone();

        operate = new Animation(8, false);
        operate.frames(film, 2, 6, 2, 6);

        fly = new Animation(1, true);
        fly.frames(film, 0);

        read = operate;

        // 根据英雄存活状态播放相应动画
        if (Dungeon.hero.isAlive())
            idle();
        else
            die();
    }

    public static TextureFilm tiers() {
        return tiers(Assets.Sprites.RATKING_HERO, frameHeight());
    }
}