package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

/** Aggressive miasma which attempts one deterministic cardinal spread every evolve. */
public class OutbreakMiasma extends SpreadingPlagueMiasma {

    @Override
    protected int particleColor() {
        return 0x63D13F;
    }

    protected int spreadInterval() {
        return 1;
    }
}
