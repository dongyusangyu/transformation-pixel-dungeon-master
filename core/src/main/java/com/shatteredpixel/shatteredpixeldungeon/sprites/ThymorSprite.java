package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class ThymorSprite extends MobSprite {
    public ThymorSprite() {
        super();

        texture( Assets.Sprites.THYMOR );

        TextureFilm frames = new TextureFilm( texture, 12, 15 );

        idle = new MovieClip.Animation( 2, true );
        idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new MovieClip.Animation( 15, true );
        run.frames( frames, 0, 2, 3, 4 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 0, 5, 6 );

        zap = attack.clone();

        die = new MovieClip.Animation( 15, false );
        die.frames( frames, 0, 7, 8, 8, 9, 10 );

        play( idle );
    }

    public void zap( int cell ) {

        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.SHADOW,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((GoldBoss.Thymor)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }


    @Override
    public void onComplete( MovieClip.Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }
}
