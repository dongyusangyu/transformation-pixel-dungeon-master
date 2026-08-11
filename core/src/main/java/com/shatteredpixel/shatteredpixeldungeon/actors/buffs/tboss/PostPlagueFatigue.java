package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;

/** Short post-battle exhaustion which affects actions and natural regeneration only. */
public class PostPlagueFatigue extends FlavourBuff {
    public static final float MOVE_DELAY_MULTIPLIER = 1.1f;
    public static final float ATTACK_DELAY_MULTIPLIER = 1.1f;
    public static final float NATURAL_REGEN_MULTIPLIER = 0.5f;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }
}
