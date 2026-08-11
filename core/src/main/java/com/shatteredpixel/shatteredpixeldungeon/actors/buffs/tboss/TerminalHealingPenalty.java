package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.watabou.utils.Bundle;

/** Additional final-phase healing reduction, additive with Infection. */
public class TerminalHealingPenalty extends Buff implements Char.HealingModifier {

    private static final String REDUCTION = "reduction";
    private float reduction;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    public static void set(Char target, float reduction) {
        TerminalHealingPenalty penalty = Buff.affect(target, TerminalHealingPenalty.class);
        penalty.reduction = Math.max(0f, Math.min(0.95f, reduction));
    }

    @Override
    public float incomingHealingReduction() {
        return reduction;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(REDUCTION, reduction);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        reduction = Math.max(0f, Math.min(0.95f, bundle.getFloat(REDUCTION)));
    }
}
