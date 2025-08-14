package com.shatteredpixel.shatteredpixeldungeon.sprites;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ChaosDisciples;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public class EyeBossSprite extends MobSprite {

    private int zapPos;

    private Animation charging;
    private Emitter chargeParticles;

    public EyeBossSprite() {
        super();

        texture( Assets.Sprites.EYEBOSS );

        TextureFilm frames = new TextureFilm( texture, 16, 18 );
        /*
        idle = new Animation( 8, true );
        idle.frames( frames, 0,  2 );

        charging = new Animation( 12, true);
        charging.frames( frames, 3, 4 );

        run = new Animation( 12, true );
        run.frames( frames, 0, 2,1 );

        attack = new Animation( 8, false );
        attack.frames( frames, 5, 6 );
        zap = attack.clone();

         */
        idle = new Animation( 8, true );
        idle.frames( frames, 0, 1, 2 );

        charging = new Animation( 12, true);
        charging.frames( frames, 3, 4 );

        run = new Animation( 4, true );
        run.frames( frames, 5, 6 );

        attack = new Animation( 4, false );
        attack.frames( frames, 4, 3 );
        zap = attack.clone();

        die = new Animation( 8, false );
        die.frames( frames, 7, 8, 9 );

        die = new Animation( 8, false );
        die.frames( frames, 7, 8, 9 );

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
                        ((ChaosDisciples.EyeBoss)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }



    @Override
    public void update() {
        super.update();
        if (chargeParticles != null){
            chargeParticles.pos( center() );
            chargeParticles.visible = visible;
        }
    }

    @Override
    public void die() {
        super.die();
        if (chargeParticles != null){
            chargeParticles.on = false;
        }
    }

    @Override
    public void kill() {
        super.kill();
        if (chargeParticles != null){
            chargeParticles.killAndErase();
        }
    }

    public void charge( int pos ){
        turnTo(ch.pos, pos);
        play(charging);
        if (visible) Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
    }

    @Override
    public void play(Animation anim) {
        if (chargeParticles != null) chargeParticles.on = anim == charging;
        super.play(anim);
    }



    @Override
    public void onComplete( Animation anim ) {
        super.onComplete( anim );

        if (anim == zap) {
            idle();
        }
    }
}

