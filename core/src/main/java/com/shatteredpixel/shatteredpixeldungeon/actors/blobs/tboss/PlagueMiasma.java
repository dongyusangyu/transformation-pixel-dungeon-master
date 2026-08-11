package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.particles.Emitter;

import java.util.Arrays;

/** Shared evolution rules for Pestilence's arena-only miasmas. */
public abstract class PlagueMiasma extends Blob {

    private static final float PARTICLE_INTERVAL = 0.25f;

    /** RGB color used by this phase's persistent gas cloud. */
    protected abstract int particleColor();

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        final int color = particleColor();
        emitter.pour(new Emitter.Factory() {
            @Override
            public void emit(Emitter source, int index, float x, float y) {
                Speck particle = (Speck) source.recycle(Speck.class);
                particle.reset(index, x, y, Speck.TOXIC);
                particle.hardlight(color);
            }
        }, PARTICLE_INTERVAL);
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }

    protected final boolean blockedByIncense(int cell) {
        return Blob.volumeAt(cell, PurifyingIncense.class) > 0;
    }

    protected final int decayAt(int cell, int normalDecay) {
        return Math.max(0, cur[cell] - normalDecay - (Dungeon.level.water[cell] ? 1 : 0));
    }

    protected final boolean canSpreadFrom(int cell) {
        return !Dungeon.level.water[cell] && !blockedByIncense(cell);
    }

    protected final void beginEvolution() {
        Arrays.fill(off, 0);
    }

    protected final void keep(int cell, int value) {
        off[cell] = Math.max(off[cell], value);
        volume += off[cell];
        if (off[cell] > 0) area.union(cell % Dungeon.level.width(), cell / Dungeon.level.width());
    }
}
