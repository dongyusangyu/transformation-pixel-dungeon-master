package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.HungerKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** World-time clock for the Hunger Knight's yellow and red hunger anomalies. */
public class HungerKnightPressure extends Buff {

    private static final String OWNER_ID = "owner_id";
    private static final String BAND = "band";
    private static final String TICKS = "ticks";

    private int ownerId = -1;
    private int band;
    private int ticks;

    {
        type = buffType.NEUTRAL;
        announced = false;
    }

    public int ownerId() { return ownerId; }

    public static void acquire(Hero hero, int ownerId) {
        if (hero == null || !TowerBoss.towerRulesActive()) return;
        for (HungerKnightPressure pressure : hero.buffs(HungerKnightPressure.class)) {
            if (pressure.ownerId == ownerId) return;
        }
        HungerKnightPressure pressure = new HungerKnightPressure();
        pressure.ownerId = ownerId;
        pressure.attachTo(hero);
    }

    public static void release(Hero hero, int ownerId) {
        if (hero == null) return;
        for (HungerKnightPressure pressure : hero.buffs(HungerKnightPressure.class)) {
            if (pressure.ownerId == ownerId) pressure.detach();
        }
    }

    int advance(int hungerLevel) {
        int currentBand = hungerLevel >= Hunger.STARVING ? 2
                : hungerLevel >= Hunger.HUNGRY ? 1 : 0;
        if (currentBand != band) {
            band = currentBand;
            ticks = 0;
        }
        if (band == 0) return 0;
        if (++ticks >= (band == 2 ? 5 : 30)) {
            ticks = 0;
            return band;
        }
        return 0;
    }

    @Override
    public boolean act() {
        Char owner = Actor.findCharById(ownerId);
        if (!TowerBoss.towerRulesActive() || !(owner instanceof HungerKnight)
                || !owner.isAlive() || Dungeon.level == null
                || !Dungeon.level.mobs.contains(owner) || target != Dungeon.hero) {
            detach();
            return true;
        }
        Hunger hunger = target.buff(Hunger.class);
        int due = advance(hunger == null ? 0 : hunger.hunger());
        if (due == 1) applyYellow();
        else if (due == 2) applyRed();
        spend(TICK);
        return true;
    }

    private void applyYellow() {
        switch (Random.Int(3)) {
            case 0: Buff.prolong(target, Hex.class, 10f); break;
            case 1: Buff.prolong(target, Weakness.class, 10f); break;
            default: Buff.prolong(target, Vertigo.class, 10f); break;
        }
    }

    private void applyRed() {
        switch (Random.Int(5)) {
            case 0: Buff.prolong(target, Daze.class, 10f); break;
            case 1: Buff.prolong(target, Blindness.class, 10f); break;
            case 2: Buff.prolong(target, Slow.class, 10f); break;
            case 3: Buff.prolong(target, Weakness.class, 10f); break;
            default: Buff.prolong(target, Amok.class, 10f); break;
        }
    }

    @Override public int icon() { return BuffIndicator.NONE; }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(OWNER_ID, ownerId);
        bundle.put(BAND, band);
        bundle.put(TICKS, ticks);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        ownerId = bundle.getInt(OWNER_ID);
        band = Math.max(0, Math.min(2, bundle.getInt(BAND)));
        ticks = Math.max(0, Math.min(band == 2 ? 4 : 29, bundle.getInt(TICKS)));
    }
}
