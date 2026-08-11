package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

/** A visible, non-triggering floor fixture activated by waiting on its cell. */
public class PlagueBrazier extends Trap {

    public PlagueBrazier() {
        color = WHITE;
        shape = LARGE_DOT;
        visible = true;
        canBeHidden = false;
        canBeSearched = false;
        disarmedByActivation = false;
    }

    @Override
    public void trigger() {
        // Intentionally inert on entry. TowerBossLevel handles an explicit wait action.
    }

    @Override
    public void activate() {
    }
}
