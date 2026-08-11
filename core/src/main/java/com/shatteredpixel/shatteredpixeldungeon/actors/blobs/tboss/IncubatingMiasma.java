package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

/** Stable, non-spreading miasma used in the opening phase. */
public class IncubatingMiasma extends PlagueMiasma {
    @Override
    protected void evolve() {
        beginEvolution();
        for (int cell = 0; cell < cur.length; cell++) {
            if (cur[cell] > 0 && !blockedByIncense(cell)) keep(cell, decayAt(cell, 1));
        }
    }
}
