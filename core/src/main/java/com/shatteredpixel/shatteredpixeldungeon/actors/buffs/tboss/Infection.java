package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

/** Stack-based plague carried by the hero during the Pestilence encounter. */
public class Infection extends Buff implements Char.HealingModifier {

    public static final int MAX_STACKS = 5;
    public static final int DECAY_TURNS = 10;
    public static final float HEALING_REDUCTION_PER_STACK = 0.08f;
    public static final int DEFAULT_ICON_COLOR = -1;
    public static final int WARNING_ICON_COLOR = 0xFFFF00;
    public static final int DANGER_ICON_COLOR = 0xFF0000;
    private static final int INFECTION_BURST_COLOR = 0xFF718F3A;

    private static final String STACKS = "stacks";
    private static final String DECAY_SCHEDULED = "decay_scheduled";
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
        Infection infection = target.buff(Infection.class);
        if (infection == null) {
            infection = Buff.affect(target, Infection.class);
            infection.spend(DECAY_TURNS);
        }
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
    @Override
    public String desc(){
        return Messages.get(this,"desc",stacks);
    }
    public float iconFadePercent() { return Math.max(0, (5-stacks) / 5f); }

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
            showRuptureFeedback();
            target.damage(25, Infection.class);
            Buff.prolong(target, Vulnerable.class, 4f);
            stacks = 3;
            thirdThresholdArmed = true;
        }
    }

    private void showRuptureFeedback() {
        if (target.sprite != null) {
            target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "rupture"));
            target.sprite.burst(INFECTION_BURST_COLOR, 8);
        }
        if (target == Dungeon.hero) GLog.w(Messages.get(this, "rupture_log"));
    }




    @Override
    public float incomingHealingReduction() {
        return stacks * HEALING_REDUCTION_PER_STACK;
    }

    @Override
    public int icon() {
        return BuffIndicator.POISON;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.resetColor();
        int color = iconColorForStacks(stacks);
        if (color != DEFAULT_ICON_COLOR) icon.hardlight(color);
    }

    public static int iconColorForStacks(int stacks) {
        if (stacks >= 4) return DANGER_ICON_COLOR;
        if (stacks > 2) return WARNING_ICON_COLOR;
        return DEFAULT_ICON_COLOR;
    }

    @Override
    public boolean act() {
        if (target == null || !target.isAlive()) {
            detach();
            return true;
        }
        set(target, stacks - 1);
        if (target.buff(Infection.class) == this) spend(DECAY_TURNS);
        return true;
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(stacks);
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
        bundle.put(DECAY_SCHEDULED, true);
        bundle.put(THIRD_THRESHOLD_ARMED, thirdThresholdArmed);
        bundle.put(POTION_RELIEF_PENDING, potionReliefPending);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stacks = Math.max(0, Math.min(MAX_STACKS, bundle.getInt(STACKS)));
        if (!bundle.contains(DECAY_SCHEDULED)) {
            timeToNow();
            spend(DECAY_TURNS);
        }
        thirdThresholdArmed = bundle.getBoolean(THIRD_THRESHOLD_ARMED) && stacks >= 3;
        potionReliefPending = bundle.getBoolean(POTION_RELIEF_PENDING) && stacks > 0;
    }

    int stacksForTest() { return stacks; }
    void decayOneIntervalForTest() { act(); }
    boolean thirdThresholdArmedForTest() { return thirdThresholdArmed; }
    boolean potionReliefPendingForTest() { return potionReliefPending; }
}
