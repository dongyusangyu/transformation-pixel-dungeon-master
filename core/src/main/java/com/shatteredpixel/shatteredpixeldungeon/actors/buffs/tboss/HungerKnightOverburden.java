package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.Overburden;
import com.watabou.utils.Bundle;

/** Hunger-Knight-owned backpack pressure; only the haste ring consumes it. */
public class HungerKnightOverburden extends Overburden {

    private static final String OWNER_ID = "owner_id";
    private int ownerId = -1;

    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public int ownerId() { return ownerId; }

    public static HungerKnightOverburden attach(Hero hero, int ownerId) {
        if (hero == null) return null;
        for (HungerKnightOverburden buff : hero.buffs(HungerKnightOverburden.class)) {
            if (buff.ownerId == ownerId) return buff;
        }
        HungerKnightOverburden buff = new HungerKnightOverburden();
        buff.ownerId = ownerId;
        return buff.attachTo(hero) ? buff : null;
    }

    public static boolean release(Hero hero, int ownerId) {
        if (hero == null) return false;
        boolean released = false;
        for (HungerKnightOverburden buff : hero.buffs(HungerKnightOverburden.class)) {
            if (buff.ownerId == ownerId) {
                buff.detach();
                released = true;
            }
        }
        return released;
    }

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
