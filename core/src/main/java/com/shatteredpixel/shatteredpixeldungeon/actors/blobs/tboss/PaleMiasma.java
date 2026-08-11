package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.watabou.utils.Bundle;

/** Final-phase miasma: non-spreading and decays only every second evolve. */
public class PaleMiasma extends PlagueMiasma {
    private static final String DECAY_PARITY = "decay_parity";
    private boolean decayParity;

    @Override
    protected int particleColor() {
        return 0xC8C3E8;
    }

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
    public void clear(int cell) {
        int removed = cur != null && cell >= 0 && cell < cur.length ? cur[cell] : 0;
        super.clear(cell);
        notifyExplicitClear(removed);
    }

    @Override
    public void fullyClear() {
        int removed = volume;
        super.fullyClear();
        notifyExplicitClear(removed);
    }

    private static void notifyExplicitClear(int removedVolume) {
        if (removedVolume > 0 && Dungeon.level instanceof TowerBossLevel) {
            ((TowerBossLevel) Dungeon.level).onPaleMiasmaClearedExternally(removedVolume);
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
