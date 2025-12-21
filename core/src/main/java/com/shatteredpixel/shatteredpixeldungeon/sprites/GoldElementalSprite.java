package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldBoss;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public class GoldElementalSprite extends MobSprite {

    public GoldElementalSprite() {
        super();
        texture( Assets.Sprites.GOLDELEMETAL );

        int c = 0;


        TextureFilm frames = new TextureFilm( texture, 12, 14 );

        idle = new Animation( 10, true );
        idle.frames( frames, c+0, c+1, c+2 );

        run = new Animation( 12, true );
        run.frames( frames, c+0, c+1, c+3 );

        attack = new Animation( 15, false );
        attack.frames( frames, c+4, c+5, c+6 );

        zap = attack.clone();

        die = new Animation( 15, false );
        die.frames( frames, c+7, c+8, c+9, c+10, c+11, c+12, c+13, c+12 );

        play( idle );
    }

    public void zap( int cell ) {
        super.zap( cell );

        MagicMissile.boltFromChar( parent,
                MagicMissile.FIRE,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((GoldBoss.GoldElemental)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }

    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }



    @Override
    public int blood() {
        return 0xFFFFBB33;
    }
}
