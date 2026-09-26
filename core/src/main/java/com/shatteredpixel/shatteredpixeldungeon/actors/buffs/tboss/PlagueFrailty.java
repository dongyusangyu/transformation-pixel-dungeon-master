package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** Temporary strength loss from the Pestilence Knight's miasma. */
public class PlagueFrailty extends FlavourBuff {
    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    @Override public int icon() { return BuffIndicator.WEAKNESS; }
}
