package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatshield;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class MountainGuard extends Greatshield {

    public static final int TIER = 6;
    public static final int MAX_ENERGY = 100;
    public static final String AC_RELEASE = "RELEASE";
    private static final String ENERGY = "mountain_guard_energy";

    private int energy;

    {
        image = EXItemSpriteSheet.MOUNTAIN_GUARD;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.85f;
        tier = TIER;
        ACC = 1f;
        DLY = 1f;
        RCH = 1;
    }

    @Override
    public int min(int level) {
        return minForLevel(level);
    }

    @Override
    public int max(int level) {
        return maxForLevel(level);
    }

    @Override
    public int STRReq(int level) {
        // This shield is intentionally one strength tier heavier than other tier 6 weapons.
        int requirement = STRReq(TIER + 1, level);
        return masteryPotionBonus ? requirement - 2 : requirement;
    }

    @Override
    public int defenseFactor(com.shatteredpixel.shatteredpixeldungeon.actors.Char owner) {
        return maxBlockForLevel(buffedLvl());
    }

    @Override
    public int DRMax(int level) {
        return maxBlockForLevel(level);
    }

    public static int minForLevel(int level) {
        return 6 + level;
    }

    public static int maxForLevel(int level) {
        return 25 + 5 * level;
    }

    public static int maxBlockForLevel(int level) {
        return 7 + 3 * level;
    }

    public static int slowDurationForLevel(int level) {
        return Math.max(1, 3 + level);
    }

    public static float wallDurationForLevel(int level) {
        return Math.max(1f, 1f + 0.5f * level);
    }

    public static int reflectedDamageFor(int damage) {
        return (int) Math.floor(Math.max(0, damage) * 0.5f);
    }

    public static float wallFadePercent(float initialDuration, float remainingDuration) {
        if (initialDuration <= 0f) return 0f;
        return Math.max(0f, Math.min(1f,
                (initialDuration - Math.max(0f, remainingDuration)) / initialDuration));
    }

    public static boolean isInsideReleaseSquare(int center, int target, int width) {
        if (width <= 0 || center < 0 || target < 0) return false;
        int dx = Math.abs(center % width - target % width);
        int dy = Math.abs(center / width - target / width);
        return dx <= 4 && dy <= 4;
    }

    public static String preferredDefaultAction(boolean canRelease,
                                                boolean canUseAbility,
                                                String fallback) {
        if (canRelease) return AC_RELEASE;
        if (canUseAbility) return AC_ABILITY;
        return fallback;
    }

    public int energy() {
        return energy;
    }

    public boolean isReleaseReady() {
        return energy >= MAX_ENERGY;
    }

    public boolean isPrimary(Hero hero) {
        return hero != null && hero.belongings != null && hero.belongings.weapon() == this;
    }

    public static MountainGuard primaryGuard(Hero hero) {
        if (hero == null || hero.belongings == null
                || !(hero.belongings.weapon() instanceof MountainGuard)) return null;
        return (MountainGuard) hero.belongings.weapon();
    }

    public static boolean isEnemyDamageSource(Object source) {
        return source instanceof Char
                && ((Char) source).alignment == Char.Alignment.ENEMY;
    }

    public static boolean isLivingEnemyDamageSource(Object source) {
        return isEnemyDamageSource(source) && ((Char) source).isAlive();
    }

    public static void onHeroEffectiveDamage(Hero hero, Object source, int effectiveDamage) {
        if (effectiveDamage <= 0 || !isEnemyDamageSource(source)) return;
        MountainGuard guard = primaryGuard(hero);
        if (guard == null) return;
        guard.addEnergy(effectiveDamage);
        syncEnergyTracker(hero);
    }

    public static void syncEnergyTracker(Hero hero) {
        if (hero == null) return;
        if (primaryGuard(hero) != null) {
            Buff.affect(hero, MountainEnergyTracker.class);
        } else {
            Buff.detach(hero, MountainEnergyTracker.class);
        }
        BuffIndicator.refreshHero();
        updateQuickslot();
    }

    @Override
    public void activate(Char ch) {
        super.activate(ch);
        if (ch instanceof Hero) syncEnergyTracker((Hero) ch);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        boolean wasPrimary = isPrimary(hero);
        boolean result = super.doUnequip(hero, collect, single);
        if (result && wasPrimary) syncEnergyTracker(hero);
        return result;
    }

    @Override
    public void onInventoryAvailabilityChanged(Hero hero) {
        syncEnergyTracker(hero);
    }

    public int addEnergy(int amount) {
        if (amount > 0) energy = Math.min(MAX_ENERGY, energy + amount);
        return energy;
    }

    public void clearEnergy() {
        energy = 0;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ENERGY, energy);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        energy = Math.max(0, Math.min(MAX_ENERGY, bundle.getInt(ENERGY)));
    }

    private boolean canRelease(Hero hero) {
        return isPrimary(hero) && isReleaseReady();
    }

    private void releaseMountainPower(Hero hero) {
        if (!canRelease(hero)) return;
        int width = Dungeon.level == null ? 0 : Dungeon.level.width();
        if (width > 0) {
            for (Char ch : Actor.chars()) {
                if (ch != null && ch.isAlive() && ch.alignment == Char.Alignment.ENEMY
                        && isInsideReleaseSquare(hero.pos, ch.pos, width)) {
                    Buff.prolong(ch, Slow.class, slowDurationForLevel(buffedLvl()));
                }
            }
        }
        clearEnergy();
        syncEnergyTracker(hero);
        Invisibility.dispel(hero);
        Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1f, 0.75f);
        if (hero.sprite != null) hero.sprite.operate(hero.pos);
        hero.spendAndNext(Actor.TICK);
    }

    @Override
    public String defaultAction() {
        Hero hero = Dungeon.hero;
        return preferredDefaultAction(canRelease(hero), canUseWeaponAbilityAction(hero),
                super.defaultAction());
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> result = super.actions(hero);
        if (canRelease(hero)) result.add(AC_RELEASE);
        return result;
    }

    @Override
    public String actionName(String action, Hero hero) {
        if (AC_RELEASE.equals(action)) return Messages.upperCase(Messages.get(this, "ac_release"));
        return super.actionName(action, hero);
    }

    @Override
    public void execute(Hero hero, String action) {
        if (AC_RELEASE.equals(action)) {
            releaseMountainPower(hero);
            return;
        }
        super.execute(hero, action);
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target) {
        return 2;
    }

    int baseChargeUseForTest() {
        return baseChargeUse(null, null);
    }

    @Override
    public String statsInfo() {
        return Messages.get(this, "stats_desc");
    }

    @Override
    public String abilityInfo() {
        return Messages.get(this, "ability_desc");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        beforeAbilityUsed(hero, null);
        MountainWallCounter existing = hero.buff(MountainWallCounter.class);
        if (existing != null) existing.detach();
        MountainWallCounter counter = Buff.affect(hero, MountainWallCounter.class,
                wallDurationForLevel(buffedLvl()));
        counter.captureInitialDuration();
        if (hero.sprite != null) hero.sprite.operate(hero.pos);
        hero.spendAndNext(Actor.TICK);
        afterAbilityUsed(hero);
    }

    public static int interceptEnemyDamage(Hero hero, int damage, Object source,
                                           DamageTag... tags) {
        if (hero == null || hero.buff(MountainWallCounter.class) == null
                || !isLivingEnemyDamageSource(source)) {
            return damage;
        }
        int reflected = reflectedDamageFor(damage);
        Char attacker = (Char) source;
        if (reflected > 0 && attacker.isAlive()) {
            attacker.damage(reflected, hero.buff(MountainWallCounter.class),
                    DamageTag.PHYSICAL, DamageTag.NO_ARMOR);
        }
        return 0;
    }


    public static class MountainEnergyTracker extends Buff {

        {
            revivePersists = true;
        }

        @Override
        public boolean act() {
            if (!(target instanceof Hero) || primaryGuard((Hero) target) == null) {
                detach();
            } else {
                spend(TICK);
            }
            return true;
        }


        @Override
        public int icon() {
            return BuffIndicator.ARMOR;
        }

        @Override
        public String iconTextDisplay() {
            MountainGuard guard = target instanceof Hero ? primaryGuard((Hero) target) : null;
            return guard == null ? "" : Integer.toString(guard.energy());
        }

        @Override
        public String desc() {
            MountainGuard guard = target instanceof Hero ? primaryGuard((Hero) target) : null;
            int current = guard == null ? 0 : guard.energy();
            return Messages.get(this, guard != null && guard.isReleaseReady()
                    ? "desc_ready" : "desc", current);
        }

        @Override
        public void tintIcon(Image icon) {
            MountainGuard guard = target instanceof Hero ? primaryGuard((Hero) target) : null;
            if (guard != null && guard.isReleaseReady()) {
                icon.tint(0x59d3dc, 0.8f);
            } else {
                icon.tint(0x5b6970, 0.8f);
            }
        }
    }

    public static class MountainWallCounter extends FlavourBuff {

        private static final String INITIAL_DURATION = "initial_duration";

        private float initialDuration;

        {
            announced = true;
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.DUEL_GUARD;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.tint(0x89cbd0, 0.75f);
        }

        public void captureInitialDuration() {
            initialDuration = Math.max(1f, visualcooldown());
        }

        @Override
        public float iconFadePercent() {
            return wallFadePercent(initialDuration, visualcooldown());
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(INITIAL_DURATION, initialDuration);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            initialDuration = bundle.getFloat(INITIAL_DURATION);
            if (initialDuration <= 0f) captureInitialDuration();
        }
    }
}
