package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** Additional final-phase healing reduction, additive with Infection. */
public class TerminalHealingPenalty extends FlavourBuff implements Char.HealingModifier {

    public static final float DURATION = 3f;

    private static final String REDUCTION = "reduction";
    private float reduction;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    public static void set(Char target, float reduction) {
        set(target, reduction, DURATION);
    }

    public static void set(Char target, float reduction, float duration) {
        TerminalHealingPenalty penalty = Buff.prolong(
                target, TerminalHealingPenalty.class, duration);
        penalty.reduction = Math.max(0f, Math.min(0.95f, reduction));
    }

    @Override
    public float incomingHealingReduction() {
        return reduction;
    }

    @Override
    public int icon() {
        return BuffIndicator.WEAKNESS;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0f, (DURATION - visualcooldown()) / DURATION);
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
