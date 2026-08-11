package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

/** Deterministic rewards shared by tower bosses. */
public final class TowerBossRewardGenerator {

    private static final long REWARD_SALT = 0x544F5745525F5257L;

    private TowerBossRewardGenerator() {
    }

    public static MeleeWeapon createTierSixWeapon(long dungeonSeed, int depth, String bossId) {
        Random.pushGenerator(rewardSeed(dungeonSeed, depth, bossId));
        try {
            Class<? extends MeleeWeapon> weaponClass = selectTierSixWeaponClassFromCurrentGenerator();
            return configureReward((MeleeWeapon) Reflection.newInstance(weaponClass));
        } finally {
            Random.popGenerator();
        }
    }

    @SuppressWarnings("unchecked")
    static Class<? extends MeleeWeapon> selectTierSixWeaponClass(
            long dungeonSeed, int depth, String bossId) {
        Random.pushGenerator(rewardSeed(dungeonSeed, depth, bossId));
        try {
            return selectTierSixWeaponClassFromCurrentGenerator();
        } finally {
            Random.popGenerator();
        }
    }

    private static long rewardSeed(long dungeonSeed, int depth, String bossId) {
        long idHash = bossId == null ? 0L : bossId.hashCode();
        return TowerBossGenerator.mix64(dungeonSeed
                ^ ((long) depth << 32) ^ idHash ^ REWARD_SALT);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends MeleeWeapon> selectTierSixWeaponClassFromCurrentGenerator() {
        Class<?>[] pool = Generator.Category.WEP_T6.classes;
        return (Class<? extends MeleeWeapon>) pool[Random.Int(pool.length)];
    }

    static <T extends MeleeWeapon> T configureReward(T weapon) {
        weapon.level(3);
        weapon.cursed = false;
        weapon.cursedKnown = true;
        weapon.identify(false);
        return weapon;
    }
}
