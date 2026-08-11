package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.watabou.utils.Bundle;

/** Stack-based plague carried by the hero during the Pestilence encounter. */
public class Infection extends Buff implements Char.HealingModifier {

    public static final int MAX_STACKS = 5;
    public static final float HEALING_REDUCTION_PER_STACK = 0.08f;

    private static final String STACKS = "stacks";
    private static final String THIRD_THRESHOLD_ARMED = "third_threshold_armed";
    private static final String POTION_RELIEF_PENDING = "potion_relief_pending";

    private int stacks;
    private boolean thirdThresholdArmed;
    private boolean potionReliefPending;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    public static int stacks(Char target) {
        Infection infection = target.buff(Infection.class);
        return infection == null ? 0 : infection.stacks;
    }

    public static void set(Char target, int value) {
        int clamped = Math.max(0, Math.min(MAX_STACKS, value));
        if (clamped == 0) {
            clear(target);
            return;
        }
        Infection infection = Buff.affect(target, Infection.class);
        infection.applyStacks(clamped);
    }

    public static void addStacks(Char target, int amount) {
        if (amount == 0) return;
        set(target, stacks(target) + amount);
    }

    public static void clear(Char target) {
        Infection infection = target.buff(Infection.class);
        if (infection != null) {
            infection.potionReliefPending = false;
            infection.detach();
        }
    }

    public static void markHealingPotionRelief(Char target) {
        Infection infection = target.buff(Infection.class);
        if (infection != null && infection.stacks > 0) infection.potionReliefPending = true;
    }

    private void applyStacks(int value) {
        int old = stacks;
        stacks = value;
        if (stacks < 3) thirdThresholdArmed = false;
        if (old < 3 && stacks >= 3 && !thirdThresholdArmed) {
            thirdThresholdArmed = true;
            Buff.prolong(target, Weakness.class, 4f);
            Buff.prolong(target, Hex.class, 4f);
        }
        if (stacks >= MAX_STACKS) {
            target.damage(25, Infection.class);
            Buff.prolong(target, Vulnerable.class, 4f);
            stacks = 3;
            thirdThresholdArmed = true;
        }
    }

    @Override
    public float incomingHealingReduction() {
        return stacks * HEALING_REDUCTION_PER_STACK;
    }

    @Override
    public void afterIncomingHealing(int requested, int actual) {
        if (!potionReliefPending) return;
        potionReliefPending = false;
        set(target, stacks - 1);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STACKS, stacks);
        bundle.put(THIRD_THRESHOLD_ARMED, thirdThresholdArmed);
        bundle.put(POTION_RELIEF_PENDING, potionReliefPending);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stacks = Math.max(0, Math.min(MAX_STACKS, bundle.getInt(STACKS)));
        thirdThresholdArmed = bundle.getBoolean(THIRD_THRESHOLD_ARMED) && stacks >= 3;
        potionReliefPending = bundle.getBoolean(POTION_RELIEF_PENDING) && stacks > 0;
    }

    int stacksForTest() { return stacks; }
    boolean thirdThresholdArmedForTest() { return thirdThresholdArmed; }
    boolean potionReliefPendingForTest() { return potionReliefPending; }
}
