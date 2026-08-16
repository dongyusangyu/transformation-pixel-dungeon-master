package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

/** Deterministic rewards shared by tower bosses. */
public final class TowerBossRewardGenerator {

    private static final long REWARD_SALT = 0x544F5745525F5257L;

    private TowerBossRewardGenerator() {
    }

    enum RewardType {
        ARTIFACT,
        RING,
        WAND,
        PLATE_ARMOR,
        TIER_SIX_WEAPON
    }

    public static Item createReward(long dungeonSeed, int depth, String bossId) {
        Random.pushGenerator(rewardSeed(dungeonSeed, depth, bossId));
        try {
            Item reward;
            switch (selectRewardTypeFromCurrentGenerator()) {
                case ARTIFACT:
                    reward = Generator.random(Generator.Category.ARTIFACT);
                    break;
                case RING:
                    reward = Generator.random(Generator.Category.RING);
                    break;
                case WAND:
                    reward = Generator.random(Generator.Category.WAND);
                    break;
                case PLATE_ARMOR:
                    reward = new PlateArmor();
                    break;
                case TIER_SIX_WEAPON:
                default:
                    reward = Reflection.newInstance(selectTierSixWeaponClassFromCurrentGenerator());
                    break;
            }
            return configureReward(reward);
        } finally {
            Random.popGenerator();
        }
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

    static RewardType selectRewardType(long dungeonSeed, int depth, String bossId) {
        Random.pushGenerator(rewardSeed(dungeonSeed, depth, bossId));
        try {
            return selectRewardTypeFromCurrentGenerator();
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

    private static RewardType selectRewardTypeFromCurrentGenerator() {
        RewardType[] values = RewardType.values();
        return values[Random.Int(values.length)];
    }

    static <T extends Item> T configureReward(T item) {
        if (!(item instanceof Artifact)) {
            item.level(3);
        }
        if (item instanceof PlateArmor) {
            ((PlateArmor) item).inscribe(Armor.Glyph.random());
        }
        item.cursed = false;
        item.cursedKnown = true;
        item.identify(false);
        return item;
    }
}
