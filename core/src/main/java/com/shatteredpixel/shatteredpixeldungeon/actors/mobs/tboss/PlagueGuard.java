package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Infection;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.watabou.utils.Bundle;

/** A plague-bound statue that recovers after being felled. */
public class PlagueGuard extends Statue {

    private static final String RECOVERING = "recovering";
    private static final int RECOVERY_PER_TURN = 25;

    private boolean recovering;

    {
        HP = HT = 120;
        defenseSkill = 18;
        EXP = 0;
        maxLvl = -2;
        state = HUNTING;
        properties.add(Property.BOSS);
    }

    public PlagueGuard() {
        createWeapon(false);
        if (Dungeon.hero != null) {
            enemy = Dungeon.hero;
            target = Dungeon.hero.pos;
        }
    }

    @Override
    protected boolean act() {
        if (recovering) {
            heal(RECOVERY_PER_TURN, false);
            if (HP >= HT) recovering = false;
            spend(TICK);
            return true;
        }
        return super.act();
    }

    @Override
    public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
        damage = super.attackProc(enemy, damage, damageTags);
        if (enemy != null && enemy.isAlive()) Infection.addStacks(enemy, 1);
        return damage;
    }

    @Override
    public boolean isAlive() {
        if (HP <= 0) {
            HP = 1;
            if (!recovering) {
                recovering = true;
                for (Buff buff : buffs()) {
                    if (!(buff instanceof Doom || buff instanceof Cripple)) buff.detach();
                }
            }
        }
        return super.isAlive();
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return recovering || super.isInvulnerable(effect);
    }

    public boolean isActiveGuard() {
        return !recovering && isAlive();
    }

    public boolean recovering() {
        return recovering;
    }

    public void reviveAfterPurifier() {
        if (!recovering) return;
        HP = HT;
        recovering = false;
        state = HUNTING;
        if (Dungeon.hero != null) {
            enemy = Dungeon.hero;
            target = Dungeon.hero.pos;
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(RECOVERING, recovering);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        recovering = bundle.getBoolean(RECOVERING);
    }
}
