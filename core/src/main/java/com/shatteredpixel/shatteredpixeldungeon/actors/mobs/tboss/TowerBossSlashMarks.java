package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class TowerBossSlashMarks extends Buff {

    private static final String OWNERS = "owners";
    private static final String STACKS = "stacks";
    private static final int MAX_STACKS = 3;

    private final ArrayList<Integer> owners = new ArrayList<>();
    private final ArrayList<Integer> stacks = new ArrayList<>();

    {
        type = buffType.NEGATIVE;
        announced = false;
        revivePersists = false;
    }

    public static void add(Char target, int ownerId) {
        if (target == null || ownerId <= 0) return;
        TowerBossSlashMarks marks = Buff.affect(target, TowerBossSlashMarks.class);
        if (marks == null) return;
        int index = marks.owners.indexOf(ownerId);
        if (index < 0) {
            marks.owners.add(ownerId);
            marks.stacks.add(1);
        } else {
            marks.stacks.set(index, Math.min(MAX_STACKS, marks.stacks.get(index) + 1));
        }
    }

    public static int consume(Char target, int ownerId) {
        TowerBossSlashMarks marks = target == null ? null : target.buff(TowerBossSlashMarks.class);
        return marks == null ? 0 : marks.consume(ownerId);
    }

    public static int stacks(Char target, int ownerId) {
        TowerBossSlashMarks marks = target == null ? null : target.buff(TowerBossSlashMarks.class);
        if (marks == null) return 0;
        int index = marks.owners.indexOf(ownerId);
        return index < 0 ? 0 : marks.stacks.get(index);
    }

    public static void clearOwner(Char target, int ownerId) {
        consume(target, ownerId);
    }

    public static void clearOwnerFromAll(int ownerId) {
        if (ownerId <= 0) return;
        for (Char ch : com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars()) {
            clearOwner(ch, ownerId);
        }
        clearOwner(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero, ownerId);
    }

    public static void clearAll(Char target) {
        TowerBossSlashMarks marks = target == null ? null : target.buff(TowerBossSlashMarks.class);
        if (marks != null) marks.detach();
    }

    public static void clearAllFromAll() {
        for (Char ch : com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars()) {
            clearAll(ch);
        }
        clearAll(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
    }

    public static void clear(Char target, int ownerId) {
        consume(target, ownerId);
    }

    int consume(int ownerId) {
        int index = owners.indexOf(ownerId);
        if (index < 0) return 0;
        int count = stacks.remove(index);
        owners.remove(index);
        if (owners.isEmpty() && target != null) detach();
        return count;
    }

    private int displayedStacks() {
        int result = 0;
        for (int count : stacks) result = Math.max(result, count);
        return result;
    }

    @Override
    public boolean act() {
        spend(TICK);
        return true;
    }

    @Override
    public int icon() {
        return BuffIndicator.INVERT_MARK;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(1f, 0.2f, 0.2f);
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(displayedStacks());
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", displayedStacks());
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        int[] ownerIds = new int[owners.size()];
        int[] counts = new int[stacks.size()];
        for (int i = 0; i < ownerIds.length; i++) {
            ownerIds[i] = owners.get(i);
            counts[i] = stacks.get(i);
        }
        bundle.put(OWNERS, ownerIds);
        bundle.put(STACKS, counts);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        owners.clear();
        stacks.clear();
        int[] ownerIds = bundle.getIntArray(OWNERS);
        int[] counts = bundle.getIntArray(STACKS);
        if (ownerIds == null || counts == null) return;
        for (int i = 0; i < Math.min(ownerIds.length, counts.length); i++) {
            if (ownerIds[i] > 0 && counts[i] > 0) {
                owners.add(ownerIds[i]);
                stacks.add(Math.min(MAX_STACKS, counts[i]));
            }
        }
    }
}
