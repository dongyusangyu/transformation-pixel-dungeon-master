package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public abstract class DronesSprite extends MobSprite {


    protected abstract  int texOffset();
    public DronesSprite() {
        super();
        int c = texOffset();

        texture( Assets.Sprites.DRONES );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new MovieClip.Animation( 2, true );
        idle.frames( frames, c+0, c+1, c+0, c+1 );

        run = new MovieClip.Animation( 10, true );
        run.frames( frames, c+1, c+2, c+3, c+4, c+5 );

        attack = new MovieClip.Animation( 15, false );
        attack.frames( frames, c+11, c+12, c+13, c+14, c+15, c+0 );

        die = new MovieClip.Animation( 10, false );
        die.frames( frames, c+6, c+7, c+8, c+9 , c+10  );

        play( idle );
    }

    public static class DroneSprite extends DronesSprite {
        @Override
        protected int texOffset() {
            return 0;
        }
    }
}
