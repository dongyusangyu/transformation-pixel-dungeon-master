package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

/** Temporarily suppresses weapon enchantments and armor glyphs without mutating items. */
public class HungerKnightEquipmentSeal extends Buff {

    private static final String OWNER_ID = "owner_id";
    private int ownerId = -1;

    {
        type = buffType.NEGATIVE;
        announced = true;
        revivePersists = true;
    }

    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public int ownerId() { return ownerId; }

    public static HungerKnightEquipmentSeal attach(Hero hero, int ownerId) {
        if (hero == null) return null;
        for (HungerKnightEquipmentSeal seal : hero.buffs(HungerKnightEquipmentSeal.class)) {
            if (seal.ownerId == ownerId) return seal;
        }
        HungerKnightEquipmentSeal seal = new HungerKnightEquipmentSeal();
        seal.ownerId = ownerId;
        return seal.attachTo(hero) ? seal : null;
    }

    public static boolean isActive(Char owner) {
        return owner instanceof Hero
                && !owner.buffs(HungerKnightEquipmentSeal.class).isEmpty();
    }

    public static boolean release(Hero hero, int ownerId) {
        if (hero == null) return false;
        boolean released = false;
        for (HungerKnightEquipmentSeal seal : hero.buffs(HungerKnightEquipmentSeal.class)) {
            if (seal.ownerId == ownerId) {
                seal.detach();
                released = true;
            }
        }
        return released;
    }

    @Override public boolean act() { spend(TICK); return true; }

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
