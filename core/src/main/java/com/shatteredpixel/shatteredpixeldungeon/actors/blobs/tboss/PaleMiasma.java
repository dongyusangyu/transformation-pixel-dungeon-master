package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.watabou.utils.Bundle;

/** Final-phase miasma: non-spreading and decays only every second evolve. */
public class PaleMiasma extends PlagueMiasma {
    private static final String DECAY_PARITY = "decay_parity";
    private boolean decayParity;

    @Override
    protected void evolve() {
        beginEvolution();
        decayParity = !decayParity;
        for (int cell = 0; cell < cur.length; cell++) {
            if (cur[cell] > 0 && !blockedByIncense(cell)) {
                keep(cell, decayAt(cell, decayParity ? 0 : 1));
            }
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(DECAY_PARITY, decayParity);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        decayParity = bundle.getBoolean(DECAY_PARITY);
    }
}
