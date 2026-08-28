package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

/**
 * Source marker for a real, exact-class MagicImmune. It preserves any time
 * which existed before the encounter and restores only the unexpired part.
 */
public class HungerKnightMagicLease extends Buff {

    private static final float ENCOUNTER_DURATION = 3f;
    private static final float LEGACY_ENCOUNTER_DURATION = 1000000f;
    private static final String OWNER_ID = "owner_id";
    private static final String PREEXISTING = "preexisting";
    private static final String EXTERNAL = "external";
    private static final String LAST_IMMUNE_COOLDOWN = "last_immune_cooldown";

    private int ownerId = -1;
    private float preexistingRemaining;
    private float externalRemaining;
    private float lastImmuneCooldown;

    {
        // This is an ownership marker, not an ordinary debuff. Keeping it
        // neutral prevents cleansing effects from orphaning MagicImmune.
        type = buffType.NEUTRAL;
        announced = false;
        revivePersists = true;
    }

    public int ownerId() { return ownerId; }

    public static HungerKnightMagicLease acquire(Hero hero, int ownerId) {
        if (hero == null) return null;
        for (HungerKnightMagicLease lease : hero.buffs(HungerKnightMagicLease.class)) {
            if (lease.ownerId == ownerId) {
                lease.maintainEncounterImmune();
                return lease;
            }
            // The exact-class MagicImmune is a singleton, so a second owner must
            // not replace or absorb the first owner's lease.
            return null;
        }
        MagicImmune existing = hero.buff(MagicImmune.class);
        float remaining = existing == null ? 0f : Math.max(0f, existing.cooldown());
        if (existing != null) existing.detach();

        HungerKnightMagicLease lease = new HungerKnightMagicLease();
        lease.ownerId = ownerId;
        lease.preexistingRemaining = remaining;
        if (!lease.attachTo(hero)) {
            if (remaining > 0) Buff.affect(hero, MagicImmune.class, remaining);
            return null;
        }
        lease.replaceWithEncounterImmune();
        return lease;
    }

    public static boolean release(Hero hero, int ownerId) {
        if (hero == null) return false;
        for (HungerKnightMagicLease lease : hero.buffs(HungerKnightMagicLease.class)) {
            if (lease.ownerId == ownerId) {
                lease.releaseInternal(hero);
                return true;
            }
        }
        return false;
    }

    /** Releases every Hunger Knight lease, including leases with a stale owner id. */
    public static boolean releaseAll(Hero hero) {
        if (hero == null) return false;
        boolean released = false;
        for (HungerKnightMagicLease lease : hero.buffs(HungerKnightMagicLease.class)) {
            lease.releaseInternal(hero);
            released = true;
        }
        return released;
    }

    /** Removes the million-turn immunity left by versions which lost this marker. */
    public static void cleanupLegacyOrphanedImmunity(Hero hero) {
        if (hero == null) return;
        MagicImmune immune = hero.buff(MagicImmune.class);
        if (immune != null && immune.cooldown() >= LEGACY_ENCOUNTER_DURATION / 2f) {
            immune.detach();
        }
    }

    private void releaseInternal(Hero hero) {
        captureElapsedAndExternal(hero);
        MagicImmune immune = hero.buff(MagicImmune.class);
        if (immune != null) immune.detach();
        float restore = Math.max(0f, preexistingRemaining + externalRemaining);
        detach();
        if (restore > 0f) applyUnadjusted(hero, restore);
    }

    private void captureElapsedAndExternal(Hero hero) {
        MagicImmune immune = hero.buff(MagicImmune.class);
        if (immune == null) return;
        float currentCooldown = Math.max(0f, immune.cooldown());
        if (currentCooldown > lastImmuneCooldown) {
            // Fallback for any direct duration mutation which bypassed
            // MagicImmune.spend/postpone.
            externalRemaining += currentCooldown - lastImmuneCooldown;
            lastImmuneCooldown = currentCooldown;
        } else {
            decayToCooldown(currentCooldown);
        }
    }

    private void replaceWithEncounterImmune() {
        if (!(target instanceof Hero)) return;
        Hero hero = (Hero) target;
        MagicImmune immune = hero.buff(MagicImmune.class);
        if (immune != null) immune.detach();
        immune = applyUnadjusted(hero, ENCOUNTER_DURATION);
        lastImmuneCooldown = immune == null ? 0f : immune.cooldown();
    }

    private static MagicImmune applyUnadjusted(Hero hero, float duration) {
        MagicImmune immune = Buff.affect(hero, MagicImmune.class);
        if (immune != null) immune.postponeUnadjusted(duration);
        return immune;
    }

    /** Called by the exact MagicImmune instance after another source extends it. */
    public static void recordExternalDuration(Hero hero, float added, float currentCooldown) {
        if (hero == null || added <= 0f) return;
        for (HungerKnightMagicLease lease : hero.buffs(HungerKnightMagicLease.class)) {
            lease.decayToCooldown(Math.max(0f, currentCooldown - added));
            lease.externalRemaining += added;
            lease.lastImmuneCooldown = Math.max(0f, currentCooldown);
            return;
        }
    }

    private void decayToCooldown(float currentCooldown) {
        float elapsed = Math.max(0f, lastImmuneCooldown - currentCooldown);
        preexistingRemaining = Math.max(0f, preexistingRemaining - elapsed);
        externalRemaining = Math.max(0f, externalRemaining - elapsed);
        lastImmuneCooldown = currentCooldown;
    }

    private void maintainEncounterImmune() {
        if (!(target instanceof Hero)) return;
        Hero hero = (Hero) target;
        captureElapsedAndExternal(hero);
        replaceWithEncounterImmune();
    }

    @Override
    public boolean act() {
        maintainEncounterImmune();
        spend(TICK);
        return true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        if (target instanceof Hero) captureElapsedAndExternal((Hero) target);
        super.storeInBundle(bundle);
        bundle.put(OWNER_ID, ownerId);
        bundle.put(PREEXISTING, preexistingRemaining);
        bundle.put(EXTERNAL, externalRemaining);
        bundle.put(LAST_IMMUNE_COOLDOWN, lastImmuneCooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        ownerId = bundle.getInt(OWNER_ID);
        preexistingRemaining = Math.max(0f, bundle.getFloat(PREEXISTING));
        externalRemaining = Math.max(0f, bundle.getFloat(EXTERNAL));
        lastImmuneCooldown = bundle.contains(LAST_IMMUNE_COOLDOWN)
                ? Math.max(0f, bundle.getFloat(LAST_IMMUNE_COOLDOWN))
                : ENCOUNTER_DURATION;
    }
}
