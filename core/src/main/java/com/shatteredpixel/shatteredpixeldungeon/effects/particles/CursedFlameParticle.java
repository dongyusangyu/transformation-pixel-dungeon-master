package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;

/** Green variant with a separate recycle pool, so ordinary fire keeps its colour. */
public class CursedFlameParticle extends FlameParticle {

    public static final int COLOR = 0x60F802;

    public static final Emitter.Factory FACTORY = new Emitter.Factory() {
        @Override public void emit(Emitter emitter, int index, float x, float y) {
            ((CursedFlameParticle) emitter.recycle(CursedFlameParticle.class)).reset(x, y);
        }
        @Override public boolean lightMode() { return true; }
    };

    public CursedFlameParticle() {
        super();
        color(COLOR);
    }
}
