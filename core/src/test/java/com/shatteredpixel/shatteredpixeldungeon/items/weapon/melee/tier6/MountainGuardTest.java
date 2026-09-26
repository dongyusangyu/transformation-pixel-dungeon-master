package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Bundle;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MountainGuardTest {

    @Test
    public void baseStatsAndGrowthMatchDesign() {
        MountainGuard guard = newGuard();
        assertEquals(6, guard.weaponTier());
        assertEquals(6, guard.min(0));
        assertEquals(21, guard.max(0));
        assertEquals(9, guard.min(3));
        assertEquals(36, guard.max(3));
        assertEquals(22, guard.STRReq());
        assertEquals(7, MountainGuard.maxBlockForLevel(0));
        assertEquals(16, MountainGuard.maxBlockForLevel(3));
    }

    @Test
    public void energyClampsAndClears() {
        MountainGuard guard = newGuard();
        assertEquals(0, guard.energy());
        assertEquals(37, guard.addEnergy(37));
        assertEquals(100, guard.addEnergy(90));
        assertEquals(100, guard.addEnergy(1));
        assertEquals(100, guard.addEnergy(-4));
        guard.clearEnergy();
        assertEquals(0, guard.energy());
    }

    @Test
    public void formulasUseExactDurationsAndFloorReflection() {
        assertEquals(5, MountainGuard.slowDurationForLevel(0));
        assertEquals(10, MountainGuard.slowDurationForLevel(5));
        assertEquals(2f, MountainGuard.wallDurationForLevel(0), 0f);
        assertEquals(5f, MountainGuard.wallDurationForLevel(3), 0f);
        assertEquals(0, MountainGuard.reflectedDamageFor(1));
        assertEquals(5, MountainGuard.reflectedDamageFor(10));
        assertEquals(5, MountainGuard.reflectedDamageFor(11));
    }

    @Test
    public void energySurvivesBundleAndInvalidValuesAreClamped() {
        MountainGuard original = newGuard();
        original.addEnergy(64);
        Bundle saved = new Bundle();
        original.storeInBundle(saved);

        MountainGuard restored = newGuard();
        restored.restoreFromBundle(saved);
        assertEquals(64, restored.energy());

        MountainGuard legacy = newGuard();
        legacy.restoreFromBundle(new Bundle());
        assertEquals(0, legacy.energy());
    }

    @Test
    public void nullHeroHasNoPrimaryGuard() {
        assertEquals(null, MountainGuard.primaryGuard(null));
    }

    @Test
    public void fullEnergyIsTheOnlyReleaseReadyState() {
        MountainGuard guard = newGuard();
        assertEquals(false, guard.isReleaseReady());
        guard.addEnergy(99);
        assertEquals(false, guard.isReleaseReady());
        guard.addEnergy(1);
        assertEquals(true, guard.isReleaseReady());
    }

    @Test
    public void onlyEnemyCharsAreEnergySources() {
        Char enemy = new Char() {};
        enemy.HT = enemy.HP = 10;
        enemy.alignment = Char.Alignment.ENEMY;
        Char ally = new Char() {};
        ally.HT = ally.HP = 10;
        ally.alignment = Char.Alignment.ALLY;

        assertEquals(true, MountainGuard.isEnemyDamageSource(enemy));
        assertEquals(false, MountainGuard.isEnemyDamageSource(ally));
        assertEquals(false, MountainGuard.isEnemyDamageSource(new Object()));
        assertEquals(false, MountainGuard.isEnemyDamageSource(null));
        enemy.HP = 0;
        assertEquals(true, MountainGuard.isEnemyDamageSource(enemy));
        assertEquals(false, MountainGuard.isLivingEnemyDamageSource(enemy));
    }

    @Test
    public void energyTrackerSurvivesRevivalAndInventoryChangesCanResyncIt() throws Exception {
        assertEquals(true, new MountainGuard.MountainEnergyTracker().revivePersists);
        MountainGuard.class.getDeclaredMethod("onInventoryAvailabilityChanged", Hero.class);
    }

    @Test
    public void releaseAffectsAllLivingEnemiesInHeroFov() {
        assertTrue(MountainGuard.canReleaseAffect(true, true, true));
        assertFalse(MountainGuard.canReleaseAffect(false, true, true));
        assertFalse(MountainGuard.canReleaseAffect(true, false, true));
        assertFalse(MountainGuard.canReleaseAffect(true, true, false));
    }

    @Test
    public void wallCounterDamageScalesFromHalfWeaponDamage() {
        MountainGuard guard = newGuard();
        assertEquals(3, guard.counterDamageMinForLevel(0));
        assertEquals(10, guard.counterDamageMaxForLevel(0));
        assertEquals(3, guard.counterDamageMinForLevel(1));
        assertEquals(13, guard.counterDamageMaxForLevel(1));
    }

    @Test
    public void wallCounterUsesTwoChargesAndExactDuration() {
        MountainGuard guard = newGuard();
        assertEquals(2, guard.baseChargeUseForTest());
        assertEquals(2f, MountainGuard.wallDurationForLevel(0), 0f);
        assertEquals(9f, MountainGuard.wallDurationForLevel(7), 0f);
        assertEquals(0f, MountainGuard.wallFadePercent(4f, 4f), 0f);
        assertEquals(0.5f, MountainGuard.wallFadePercent(4f, 2f), 0f);
        assertEquals(1f, MountainGuard.wallFadePercent(4f, 0f), 0f);
    }

    @Test
    public void wallCounterBlocksDirectEnemyDamageAndReflectsWeaponDamage() {
        Hero hero = newBareHero();
        MountainGuard.MountainWallCounter counter = new MountainGuard.MountainWallCounter();
        counter.target = hero;
        hero.add(counter);
        hero.belongings = (com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings)
                allocate(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings.class);
        hero.belongings.weapon = newGuard();
        class CapturingEnemy extends Char {
            @Override
            public void damage(int damage, Object source, DamageTag... tags) {
                HP -= damage;
            }
        }
        Char enemy = new CapturingEnemy();
        enemy.HT = enemy.HP = 20;
        enemy.alignment = Char.Alignment.ENEMY;

        assertEquals(0, MountainGuard.interceptEnemyDamage(
                hero, 11, enemy, DamageTag.PHYSICAL, DamageTag.UNAVOIDABLE));
        assertTrue(enemy.HP <= 17 && enemy.HP >= 10);
        assertEquals(7, MountainGuard.interceptEnemyDamage(
                hero, 7, new Object(), DamageTag.UNAVOIDABLE));
    }

    private static MountainGuard newGuard() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            MountainGuard guard = (MountainGuard) ((Unsafe) field.get(null))
                    .allocateInstance(MountainGuard.class);
            Field tier = com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.class
                    .getDeclaredField("tier");
            tier.setAccessible(true);
            tier.setInt(guard, MountainGuard.TIER);
            Field augment = Weapon.class.getDeclaredField("augment");
            augment.setAccessible(true);
            augment.set(guard, Weapon.Augment.NONE);
            return guard;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static Object allocate(Class<?> type) {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return ((Unsafe) field.get(null)).allocateInstance(type);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static Hero newBareHero() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Hero hero = (Hero) ((Unsafe) field.get(null)).allocateInstance(Hero.class);
            Field buffs = Char.class.getDeclaredField("buffs");
            buffs.setAccessible(true);
            buffs.set(hero, new LinkedHashSet<>());
            Field resistances = Char.class.getDeclaredField("resistances");
            resistances.setAccessible(true);
            resistances.set(hero, new HashSet<>());
            Field immunities = Char.class.getDeclaredField("immunities");
            immunities.setAccessible(true);
            immunities.set(hero, new HashSet<>());
            Field properties = Char.class.getDeclaredField("properties");
            properties.setAccessible(true);
            properties.set(hero, new HashSet<>());
            hero.HT = hero.HP = 20;
            return hero;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
