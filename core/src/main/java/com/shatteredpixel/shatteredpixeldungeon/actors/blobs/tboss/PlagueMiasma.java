package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;

import java.util.Arrays;

/** Shared evolution rules for Pestilence's arena-only miasmas. */
public abstract class PlagueMiasma extends Blob {

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
