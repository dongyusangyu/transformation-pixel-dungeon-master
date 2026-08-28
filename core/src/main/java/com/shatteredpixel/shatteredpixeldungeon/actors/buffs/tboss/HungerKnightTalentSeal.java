package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.utils.Bundle;

import java.util.LinkedHashSet;

/** Owner-scoped list of hero talents suppressed by one Hunger Knight. */
public class HungerKnightTalentSeal extends Buff {

    private static final String OWNER_ID = "owner_id";
    private static final String TALENTS = "talents";

    private int ownerId = -1;
    private final LinkedHashSet<Talent> talents = new LinkedHashSet<>();

    {
        type = buffType.NEGATIVE;
        announced = true;
        revivePersists = true;
    }

    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public int ownerId() { return ownerId; }
    public void add(Talent talent) { if (talent != null) talents.add(talent); }
    public boolean contains(Talent talent) { return talents.contains(talent); }
    public int size() { return talents.size(); }

    public static boolean isSealed(Hero hero, Talent talent) {
        if (hero == null || talent == null) return false;
        for (HungerKnightTalentSeal seal : hero.buffs(HungerKnightTalentSeal.class)) {
            if (seal.contains(talent)) return true;
        }
        return false;
    }

    public static boolean isSealedBy(Hero hero, Talent talent, int ownerId) {
        if (hero == null || talent == null) return false;
        for (HungerKnightTalentSeal seal : hero.buffs(HungerKnightTalentSeal.class)) {
            if (seal.ownerId == ownerId && seal.contains(talent)) return true;
        }
        return false;
    }

    public static boolean release(Hero hero, int ownerId) {
        if (hero == null) return false;
        boolean released = false;
        for (HungerKnightTalentSeal seal : hero.buffs(HungerKnightTalentSeal.class)) {
            if (seal.ownerId == ownerId) {
                seal.detach();
                released = true;
            }
        }
        return released;
    }

    @Override
    public boolean act() {
        spend(TICK);
        return true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(OWNER_ID, ownerId);
        String[] names = new String[talents.size()];
        int i = 0;
        for (Talent talent : talents) names[i++] = talent.name();
        bundle.put(TALENTS, names);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        ownerId = bundle.getInt(OWNER_ID);
        talents.clear();
        String[] names = bundle.getStringArray(TALENTS);
        if (names == null) return;
        for (String name : names) {
            try {
                talents.add(Talent.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                // Old or removed talent: safely discard only that entry.
            }
        }
    }
}
