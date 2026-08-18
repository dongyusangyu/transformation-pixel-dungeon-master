package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

/** Small grey-green flames used while the pestilence knight is telegraphing. */
public class PlagueFlameParticle extends PixelParticle.Shrinking {

    public static final Emitter.Factory FACTORY = new Emitter.Factory() {
        @Override
        public void emit(Emitter emitter, int index, float x, float y) {
            ((PlagueFlameParticle) emitter.recycle(PlagueFlameParticle.class)).reset(x, y);
        }

        @Override
        public boolean lightMode() {
            return true;
        }
    };

    public PlagueFlameParticle() {
        color(Random.Int(2) == 0 ? 0x7F913F : 0xB2C56D);
        lifespan = 0.7f;
        acc.set(0, -70);
    }

    public void reset(float x, float y) {
        revive();
        this.x = x;
        this.y = y;
        left = lifespan;
        size = 4;
        speed.set(0);
    }

    @Override
    public void update() {
        super.update();
        float p = left / lifespan;
        am = p > 0.8f ? (1 - p) * 5 : 1;
    }
}
