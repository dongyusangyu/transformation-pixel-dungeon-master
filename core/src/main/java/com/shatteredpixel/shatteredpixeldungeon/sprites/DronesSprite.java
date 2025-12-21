package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public abstract class DronesSprite extends MobSprite {
    protected int boltType;
    protected abstract  int texOffset();
    public DronesSprite() {
        super();
        int c = texOffset();

        texture( Assets.Sprites.DRONES );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new MovieClip.Animation( 2, true );
        idle.frames( frames, c+0, c+1, c+0, c+1 );

        run = new MovieClip.Animation( 10, true );
        run.frames( frames, c+2, c+3, c+4, c+5, c+6,c+7 );

        attack = new MovieClip.Animation( 15, false );
        attack.frames( frames, c+13, c+14, c+15, c+16,c+17, c+0 );

        die = new MovieClip.Animation( 10, false );
        die.frames( frames, c+8, c+9, c+10, c+11 , c+12  );

        zap = attack.clone();

        play( idle );
    }
    @Override
    public void die() {
        emitter().burst( Speck.factory( Speck.WOOL ), 5 );
        super.die();
    }
    @Override
    public int blood() {
        return 0xFFFFFF88;
    }

    @Override
    public void zap( int cell ) {
        super.zap( cell, null );

        ((InstructionTool.Drone)ch).onZapComplete();
        parent.add( new Beam.LightRay(center(), DungeonTilemap.raisedTileCenterToWorld(cell)));
    }

    public static class DroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 0;
        }
    }
    public static class ScoutDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 18;
        }
    }

    public static class FlashDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 36;
        }
    }

    public static class MirrorDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 54;
        }
    }


    public static class LaserDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 72;
        }
        @Override
        public void zap( int cell ) {
            super.zap( cell, null );

            ((InstructionTool.Drone)ch).onZapComplete();
            parent.add( new Beam.HolyRay(center(), DungeonTilemap.raisedTileCenterToWorld(cell)));
        }
    }

    public static class ProtectDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 90;
        }
    }

    public static class AnesthesiaDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 108;
        }

    }

    public static class EscortDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 126;
        }

    }

    public static class RaidDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 144;
        }
        @Override
        public void zap( int cell ) {
            super.zap( cell, null );

            ((MissileSprite)parent.recycle( MissileSprite.class )).
                    reset( this, cell, new RBullet(), new Callback() {
                        @Override
                        public void call() {
                            ch.onAttackComplete();
                        }
                    } );
        }

    }

    public static class BombDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 162;
        }

    }
    public static class ShockDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 180;
        }
        @Override
        public void zap( int cell ) {
            super.zap( cell, null );

            ((MissileSprite)parent.recycle( MissileSprite.class )).
                    reset( this, cell, new SBullet(), new Callback() {
                        @Override
                        public void call() {
                            ch.onAttackComplete();
                        }
                    } );
        }

    }

    public static class ChaosDroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 198;
        }

    }

    public static class Bullet extends Item {

        {
            image = ItemSpriteSheet.TAMARU;
        }

        @Override
        public ItemSprite.Glowing glowing() {
            return new ItemSprite.Glowing(0xFFFFFF, 0.1f);
        }
        /*
        @Override
        public Emitter emitter() {
            Emitter emitter = new Emitter();
            emitter.pos( 5, 5, 0, 0);
            emitter.fillTarget = false;
            emitter.pour(SparkParticle.FACTORY, 0.025f);
            return emitter;
        }

         */
    }

    public static class RBullet extends Bullet {

        {
            image = ItemSpriteSheet.TAMARU;
        }

        @Override
        public ItemSprite.Glowing glowing() {
            return new ItemSprite.Glowing(0xFF0000, 0.5f);
        }

    }

    public static class SBullet extends Bullet {

        {
            image = ItemSpriteSheet.TAMARU;
        }

        @Override
        public ItemSprite.Glowing glowing() {
            return new ItemSprite.Glowing(0xFFFF00, 0.5f);
        }

    }




}
