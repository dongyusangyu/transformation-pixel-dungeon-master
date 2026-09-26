package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.watabou.utils.Bundle;

import java.util.HashMap;
import java.util.Map;

/** One target's execution progress, kept separately for each Death Knight. */
public class DeathKnightExecutionMark extends Buff {
    private static final String OWNERS = "owners";
    private static final String STACKS = "stacks";
    private final Map<Integer, Integer> stacks = new HashMap<>();
    private transient boolean executing;

    {
        type = buffType.NEGATIVE;
        announced = true;
        revivePersists = false;
    }

    public static DeathKnightExecutionMark addHit(Char target, int ownerId) {
        if (target == null || !target.isAlive()) return null;
        DeathKnightExecutionMark mark = Buff.affect(target, DeathKnightExecutionMark.class);
        if (mark != null) {
            mark.stacks.put(ownerId, mark.stacks.getOrDefault(ownerId, 0) + 1);
            mark.checkExecution();
        }
        return mark;
    }

    public static int stacks(Char target, int ownerId) {
        DeathKnightExecutionMark mark = target == null ? null : target.buff(DeathKnightExecutionMark.class);
        return mark == null ? 0 : mark.stacks.getOrDefault(ownerId, 0);
    }

    public static void clear(Char target, int ownerId) {
        DeathKnightExecutionMark mark = target == null ? null : target.buff(DeathKnightExecutionMark.class);
        if (mark == null) return;
        mark.stacks.remove(ownerId);
        if (mark.stacks.isEmpty()) mark.detach();
    }

    public static void clearAll(Char target) {
        if (target != null) Buff.detach(target, DeathKnightExecutionMark.class);
    }

    public int stacks() {
        int total = 0;
        for (int value : stacks.values()) total += value;
        return total;
    }

    public boolean shouldExecute() {
        if (target == null || !target.isAlive()) return false;
        for (int count : stacks.values()) {
            if ((long) target.HP * 20L < (long) count * target.HT) return true;
        }
        return false;
    }

    public void checkExecution() {
        if (executing || target == null || !target.isAlive()) return;
        for (Map.Entry<Integer, Integer> entry : new HashMap<>(stacks).entrySet()) {
            Actor owner = Actor.findById(entry.getKey());
            if (!(owner instanceof DeathKnight) || !((DeathKnight) owner).isActive()) continue;
            if ((long) target.HP * 20L >= (long) entry.getValue() * target.HT) continue;
            executing = true;
            int lethal = Math.max(1, target.HP + target.shielding());
            target.damage(lethal, owner, DamageTag.UNAVOIDABLE);
            executing = false;
            if (target.isAlive()) clear(target, entry.getKey());
            return;
        }
    }

    @Override
    public boolean act() {
        checkExecution();
        spend(TICK);
        return true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        int[] owners = new int[stacks.size()];
        int[] values = new int[stacks.size()];
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : stacks.entrySet()) {
            owners[i] = entry.getKey();
            values[i++] = entry.getValue();
        }
        bundle.put(OWNERS, owners);
        bundle.put(STACKS, values);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stacks.clear();
        int[] owners = bundle.getIntArray(OWNERS);
        int[] values = bundle.getIntArray(STACKS);
        for (int i = 0; i < Math.min(owners.length, values.length); i++) {
            if (values[i] > 0) stacks.put(owners[i], values[i]);
        }
    }
}
