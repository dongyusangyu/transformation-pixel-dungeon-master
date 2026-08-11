package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/** Aggressive miasma which attempts one deterministic cardinal spread every other evolve. */
public class OutbreakMiasma extends PlagueMiasma {

    private static final String SPREAD_PARITY = "spread_parity";
    private static final String RNG_STATE = "rng_state";
    private boolean spreadParity;
    private long rngState = 0x6A09E667F3BCC909L;

    @Override
    protected void evolve() {
        beginEvolution();
        ArrayList<Integer> sources = new ArrayList<>();
        for (int cell = 0; cell < cur.length; cell++) {
            if (cur[cell] <= 0 || blockedByIncense(cell)) continue;
            keep(cell, decayAt(cell, 1));
            if (off[cell] > 0 && canSpreadFrom(cell)) sources.add(cell);
        }
        spreadParity = !spreadParity;
        if (spreadParity && !sources.isEmpty()) spreadFrom(sources);
    }

    private void spreadFrom(ArrayList<Integer> sources) {
        int width = Dungeon.level.width();
        int source = sources.get(nextInt(sources.size()));
        int[] neighbours = {source - width, source + 1, source + width, source - 1};
        int start = nextInt(neighbours.length);
        for (int i = 0; i < neighbours.length; i++) {
            int next = neighbours[(start + i) % neighbours.length];
            if (next < 0 || next >= cur.length || Dungeon.level.solid[next]
                    || Dungeon.level.water[next] || blockedByIncense(next)) continue;
            if (Math.abs(next % width - source % width) > 1) continue;
            if (cur[next] == 0 && off[next] == 0) {
                keep(next, 2);
                return;
            }
        }
    }

    private int nextInt(int bound) {
        rngState += 0x9E3779B97F4A7C15L;
        long z = rngState;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (int) Long.remainderUnsigned(z, bound);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SPREAD_PARITY, spreadParity);
        bundle.put(RNG_STATE, rngState);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        spreadParity = bundle.getBoolean(SPREAD_PARITY);
        rngState = bundle.contains(RNG_STATE) ? bundle.getLong(RNG_STATE) : 0x6A09E667F3BCC909L;
    }
}
