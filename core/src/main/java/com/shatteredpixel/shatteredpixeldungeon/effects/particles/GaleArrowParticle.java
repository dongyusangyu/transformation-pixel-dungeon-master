package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;

public class GaleArrowParticle extends CursedFlameParticle {
    public static final Emitter.Factory FACTORY = new Emitter.Factory() {
        @Override public void emit(Emitter emitter, int index, float x, float y) {
            GaleArrowParticle particle = (GaleArrowParticle) emitter.recycle(GaleArrowParticle.class);
            particle.resetForSource(x, y, -1);
            particle.color(index % 4 == 0 ? 0xB794D4 : 0x65358F);
            particle.size = 3;
        }
        @Override public boolean lightMode() { return true; }
    };

    public GaleArrowParticle() { lifespan = 0.35f; }
}
