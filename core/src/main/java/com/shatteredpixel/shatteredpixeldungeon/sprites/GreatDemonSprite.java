package com.shatteredpixel.shatteredpixeldungeon.sprites;

import static com.badlogic.gdx.scenes.scene2d.ui.Table.Debug.cell;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpear;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GreatDemonSprite extends MobSprite {
    private int cellToAttack;
    public GreatDemonSprite() {
        super();

        texture( Assets.Sprites.GREATDEMON );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 2, true );
        idle.frames( frames, 0, 1);

        run = new Animation( 15, true );
        run.frames( frames, 0, 2, 3, 4 );

        attack = new Animation( 12, false );
        attack.frames( frames, 0, 5, 6 );

        zap = attack.clone();

        die = new Animation( 5, false );
        die.frames( frames, 0, 7, 8, 8, 9, 10 );

        play( idle );
    }

    @Override
    public void attack( int cell ) {
        if (!Dungeon.level.adjacent( cell, ch.pos )) {
            cellToAttack = cell;
            zap(cell);
        } else {
            super.attack( cell );
        }
    }

    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap) {
            idle();
            ((MissileSprite)parent.recycle( MissileSprite.class )).
                    reset( this, cellToAttack, new Shot(), new Callback() {
                        @Override
                        public void call() {
                            ch.onAttackComplete();
                        }
                    } );
        } else if (anim == die) {
            remove(State.SHIELDED);

            emitter().burst( Speck.factory( Speck.WOOL ), 15 );
            killAndErase();}
        else{
            super.onComplete( anim );
        }
    }
    @Override
    public void link( Char ch ) {
        super.link( ch );
        add(State.SHIELDED);
    }


    public class Shot extends Item {
        {
            image = ItemSpriteSheet.THROWING_SPEAR;
        }
    }

}
