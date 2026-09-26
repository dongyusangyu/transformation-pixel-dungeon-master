package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.IncubatingMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.OutbreakMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.PaleMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** One anomaly roll per world tick, even when several plague gases overlap. */
public class PestilenceAnomaly extends Buff {
    private static final String OWNER_ID = "owner_id";
    private int ownerId = -1;

    {
        type = buffType.NEUTRAL;
        announced = false;
    }

    public static void acquire(Hero hero, int ownerId) {
        if (hero == null || !TowerBoss.towerRulesActive()) return;
        for (PestilenceAnomaly anomaly : hero.buffs(PestilenceAnomaly.class)) {
            if (anomaly.ownerId == ownerId) return;
        }
        PestilenceAnomaly anomaly = new PestilenceAnomaly();
        anomaly.ownerId = ownerId;
        anomaly.attachTo(hero);
    }

    public static void release(Hero hero, int ownerId) {
        if (hero == null) return;
        for (PestilenceAnomaly anomaly : hero.buffs(PestilenceAnomaly.class)) {
            if (anomaly.ownerId == ownerId) anomaly.detach();
        }
    }

    public static boolean inMiasma(int cell) {
        return cell >= 0 && Dungeon.level != null && cell < Dungeon.level.length()
                && (Blob.volumeAt(cell, IncubatingMiasma.class) > 0
                || Blob.volumeAt(cell, OutbreakMiasma.class) > 0
                || Blob.volumeAt(cell, PaleMiasma.class) > 0);
    }

    static Class<? extends Buff> effectForRoll(int roll) {
        switch (roll) {
            case 0: return Burning.class;
            case 1: return Frost.class;
            case 2: return MagicalSleep.class;
            default: return PlagueFrailty.class;
        }
    }

    @Override
    public boolean act() {
        Char owner = Actor.findCharById(ownerId);
        if (!TowerBoss.towerRulesActive() || !(owner instanceof PestilenceKnight)
                || !owner.isAlive() || target != Dungeon.hero || Dungeon.level == null
                || !Dungeon.level.mobs.contains(owner)) {
            detach();
            return true;
        }
        if (inMiasma(target.pos) && Random.Int(3) == 0) {
            switch (Random.Int(4)) {
                case 0:
                    Burning burning = Buff.affect(target, Burning.class);
                    if (burning != null) burning.reignite(target);
                    break;
                case 1: Buff.prolong(target, Frost.class, 5f); break;
                case 2: Buff.affect(target, MagicalSleep.class); break;
                default: Buff.prolong(target, PlagueFrailty.class, 10f); break;
            }
        }
        spend(TICK);
        return true;
    }

    @Override public int icon() { return BuffIndicator.NONE; }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(OWNER_ID, ownerId);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        ownerId = bundle.getInt(OWNER_ID);
    }
}
