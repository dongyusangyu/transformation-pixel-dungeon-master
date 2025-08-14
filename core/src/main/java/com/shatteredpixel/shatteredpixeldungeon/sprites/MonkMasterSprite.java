package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Random;

public class MonkMasterSprite extends MobSprite {

    private Animation kick;

    public MonkMasterSprite() {
        super();

        texture( Assets.Sprites.MONKMASTER );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 12, true );
        idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2 );

        run = new Animation( 15, true );
        run.frames( frames, 3, 4, 5, 6, 7, 8 );

        attack = new Animation( 15, false );
        attack.frames( frames, 9, 10, 11, 12, 13,9);

        kick = new Animation( 15, false );
        kick.frames( frames, 9, 10, 11 );

        die = new Animation( 8, false );
        die.frames( frames, 0,  14, 15, 16 );

        play( idle );
    }

    @Override
    public void attack( int cell ) {
        super.attack( cell );
        if (Random.Float() < 0.5f) {
            play( kick );
        }
    }

    @Override
    public void onComplete( Animation anim ) {
        super.onComplete( anim == kick ? attack : anim );
    }
}

