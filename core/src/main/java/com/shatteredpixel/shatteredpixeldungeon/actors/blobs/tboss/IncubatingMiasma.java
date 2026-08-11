package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

/** Slowly spreading miasma used in the opening phase. */
public class IncubatingMiasma extends SpreadingPlagueMiasma {
    @Override
    protected int particleColor() {
        return 0x9EAD48;
    }

    protected int spreadInterval() {
        return 2;
    }
}
