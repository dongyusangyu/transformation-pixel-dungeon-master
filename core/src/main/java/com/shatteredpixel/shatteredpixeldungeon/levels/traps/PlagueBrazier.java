package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

/** A visible floor fixture whose entry effect is coordinated by TowerBossLevel. */
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
        // Intentionally inert here. TowerBossLevel handles entry, relocation and encounter state.
    }

    @Override
    public void activate() {
    }
}
