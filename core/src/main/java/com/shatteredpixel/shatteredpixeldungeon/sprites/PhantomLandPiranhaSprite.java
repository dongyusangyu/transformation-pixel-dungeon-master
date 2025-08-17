package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;

public class PhantomLandPiranhaSprite extends MobSprite {
    private Emitter sparkles;

    public PhantomLandPiranhaSprite() {
        super();

        renderShadow = false;
        perspectiveRaise = 0.2f;

        texture( Assets.Sprites.LANDPIRANHA );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        int c = 16;

        idle = new MovieClip.Animation( 8, true );
        idle.frames( frames, c+0, c+1, c+0, c+1 );

        run = new MovieClip.Animation( 20, true );
        run.frames( frames, c+0, c+6, c+7, c+8, c+9, c+10 );

        attack = new MovieClip.Animation( 20, false );
        attack.frames( frames, c+0, c+2, c+3, c+4, c+5);

        die = new MovieClip.Animation( 4, false );
        die.frames( frames,  c+11 ,c+12, c+13, c+14 );

        play( idle );
    }


}