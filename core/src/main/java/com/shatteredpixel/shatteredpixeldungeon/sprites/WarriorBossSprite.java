package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Camera;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class WarriorBossSprite extends MobSprite {
    private Animation fly;
    private Animation read;
    public  WarriorBossSprite() {
        super();

        texture( Assets.Sprites.WARRIORBOSS );

        TextureFilm film = new TextureFilm( texture, 12, 15 );

        idle = new Animation( 1, true );
        idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new Animation( 12, true );
        run.frames( film, 2, 3, 4, 5, 6, 7 );

        die = new Animation( 20, false );
        die.frames( film, 8, 9, 10, 11, 12, 11 );

        attack = new Animation( 15, false );
        attack.frames( film, 13, 14, 15, 0 );

        zap = attack.clone();

        operate = new Animation( 8, false );
        operate.frames( film, 16, 17, 16, 17 );

        fly = new Animation( 1, true );
        fly.frames( film, 18 );

        read = new Animation( 20, false );
        read.frames( film, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19 );

        play( idle );
    }
    @Override
    public void jump( int from, int to, float height, float duration,  Callback callback ) {
        super.jump( from, to, height, duration, callback );
        play( fly );
    }
}
