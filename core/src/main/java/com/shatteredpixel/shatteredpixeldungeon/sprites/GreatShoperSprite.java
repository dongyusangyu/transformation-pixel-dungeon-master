package com.shatteredpixel.shatteredpixeldungeon.sprites;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GreatShoperSprite extends MobSprite {

    private int cellToAttack;
    public GreatShoperSprite() {
        super();

        texture( Assets.Sprites.GREATSHOPER );

        TextureFilm frames = new TextureFilm( texture, 15, 16 );

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

        ((MissileSprite)parent.recycle( MissileSprite.class )).
                reset( this, cell, new MissileGold(), new Callback() {
                    @Override
                    public void call() {
                        ch.onAttackComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.GOLD );

    }


    @Override
    public void onComplete( MovieClip.Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }

    @Override
    public void die() {
        super.die();
        remove(State.SHIELDED);

    }



    public class MissileGold extends Item {
        {
            image = ItemSpriteSheet.GOLD;
        }
    }
}
