package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

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
    private static final String LAST_RECEIVED_TICK = "last_received_tick";
    private static final String CONTRACTED_TICK = "contracted_tick";

    private int stacks;
    private boolean thirdThresholdArmed;
    private boolean potionReliefPending;
    private float lastReceivedTick = -1f;
    private float contractedTick = -1f;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    public static int stacks(Char target) {
        Infection infection = target.buff(Infection.class);
        return infection == null ? 0 : infection.stacks;
    }

    public static void set(Char target, int value) {
        if (target == null || target instanceof PestilenceKnight) return;
        int clamped = Math.max(0, Math.min(MAX_STACKS, value));
        if (clamped == 0) {
            clear(target);
            return;
        }
        Infection infection = target.buff(Infection.class);
        if (infection == null) {
            infection = Buff.affect(target, Infection.class);
            if (infection == null) return;
            infection.spend(decayTurnsFor(target));
            infection.contractedTick = worldTime();
        }
        Buff.affect(target, Contagion.class);
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
        Buff.detach(target, Contagion.class);
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
            Buff.prolong(target, Weakness.class, Susceptible.plagueDuration(target, 5f));
            Buff.prolong(target, Hex.class, Susceptible.plagueDuration(target, 5f));
        }
        if (stacks >= MAX_STACKS) {
            showRuptureFeedback();
            target.damage(burstDamageFor(target), Infection.class,
                    DamageTag.PHYSICAL, DamageTag.PLAGUE);
            Buff.prolong(target, Vulnerable.class, Susceptible.plagueDuration(target, 5f));
            stacks = 3;
            thirdThresholdArmed = true;
        }
    }

    private void showRuptureFeedback() {
        if (target.sprite != null) {
            target.sprite.burst(INFECTION_BURST_COLOR, 8);
        }
        if (target == Dungeon.hero) GLog.w(Messages.get(this, "rupture_log"));
    }




    @Override
    public float incomingHealingReduction() {
        float perStack = Susceptible.active(target)
                ? HEALING_REDUCTION_PER_STACK * 2f : HEALING_REDUCTION_PER_STACK;
        return stacks * perStack;
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
        if (target.buff(Infection.class) == this) spend(decayTurnsFor(target));
		if (target.buff(Infection.class) == this) Buff.affect(target, Contagion.class);
        return true;
    }

    private static float worldTime() {
        return Statistics.duration + Actor.now();
    }

    private static int decayTurnsFor(Char target) {
        return Susceptible.active(target) ? DECAY_TURNS * 2 : DECAY_TURNS;
    }

    static void extendDecayForSusceptibility(Char target) {
        Infection infection = target == null ? null : target.buff(Infection.class);
        if (infection != null) infection.spend(DECAY_TURNS * 2);
    }

    static void resumeNormalDecay(Char target) {
        Infection infection = target == null ? null : target.buff(Infection.class);
        if (infection != null && !Susceptible.active(target)) infection.spend(DECAY_TURNS);
    }

    static int burstDamageFor(Char target) {
        return Susceptible.active(target) ? 50 : 25;
    }

    static float plagueDebuffDurationFor(Char target, float duration) {
        return Susceptible.plagueDuration(target, duration);
    }

    static int burstDamageForTest(Char target) {
        return burstDamageFor(target);
    }

    static float plagueDebuffDurationForTest(Char target, float duration) {
        return plagueDebuffDurationFor(target, duration);
    }

    private static boolean plagueEncounterActive() {
        if (Dungeon.level == null) return false;
        for (Mob mob : Dungeon.level.mobs) {
            if (mob instanceof PestilenceKnight && mob.isAlive()) return true;
        }
        return false;
    }

    public static class Contagion extends Buff {
        {
            type = buffType.NEUTRAL;
        }

        @Override
        public boolean act() {
            Infection source = target == null ? null : target.buff(Infection.class);
            if (source == null || !target.isAlive()) {
                detach();
                return true;
            }
            float now = worldTime();
            if (plagueEncounterActive() && source.contractedTick < now
                    && Dungeon.level != null && target.pos >= 0
                    && target.pos < Dungeon.level.length()) {
                for (int offset : PathFinder.NEIGHBOURS8) {
                    int cell = target.pos + offset;
                    if (cell < 0 || cell >= Dungeon.level.length()
                            || !Dungeon.level.adjacent(target.pos, cell)) continue;
                    Char other = Actor.findChar(cell);
                    if (other == null || !other.isAlive() || other == target
                            || other instanceof NPC && other.alignment == Char.Alignment.NEUTRAL) continue;
                    Infection existing = other.buff(Infection.class);
                    if (existing != null && existing.lastReceivedTick == now) continue;
                    int before = stacks(other);
                    addStacks(other, 1);
                    Infection received = other.buff(Infection.class);
                    if (received != null && received.stacks > before) {
                        received.lastReceivedTick = now;
                        if (before == 0) received.contractedTick = now;
                    }
                }
            }
            spend(TICK);
            return true;
        }
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
        bundle.put(LAST_RECEIVED_TICK, lastReceivedTick);
        bundle.put(CONTRACTED_TICK, contractedTick);
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
        lastReceivedTick = bundle.contains(LAST_RECEIVED_TICK)
                ? bundle.getFloat(LAST_RECEIVED_TICK) : -1f;
        contractedTick = bundle.contains(CONTRACTED_TICK)
                ? bundle.getFloat(CONTRACTED_TICK) : -1f;
    }

    int stacksForTest() { return stacks; }
    void decayOneIntervalForTest() { act(); }
    boolean thirdThresholdArmedForTest() { return thirdThresholdArmed; }
    boolean potionReliefPendingForTest() { return potionReliefPending; }
}
