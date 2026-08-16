package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerBossRewardGeneratorTest {

    @Test
    public void tierSixRewardIsDeterministicIdentifiedUncursedAndPlusThree() {
        Class<? extends MeleeWeapon> first = TowerBossRewardGenerator.selectTierSixWeaponClass(
                123456789L, 20, TowerBossGenerator.PESTILENCE_KNIGHT_ID);
        Class<? extends MeleeWeapon> second = TowerBossRewardGenerator.selectTierSixWeaponClass(
                123456789L, 20, TowerBossGenerator.PESTILENCE_KNIGHT_ID);

        assertEquals(first, second);
        MeleeWeapon configured = TowerBossRewardGenerator.configureReward(new TestWeapon());
        assertEquals(3, configured.trueLevel());
        assertTrue(configured.isIdentified());
        assertFalse(configured.cursed);
        assertTrue(configured.cursedKnown);
    }

    @Test
    public void rewardSeedIncludesDepthAndBossIdentity() {
        TowerBossRewardGenerator.RewardType baseline =
                TowerBossRewardGenerator.selectRewardType(7L, 5, "boss_a");
        boolean differs = false;
        for (int depth = 10; depth <= 40; depth += 5) {
            TowerBossRewardGenerator.RewardType candidate =
                    TowerBossRewardGenerator.selectRewardType(7L, depth, "boss_b");
            differs |= candidate != baseline;
        }
        assertTrue("reward inputs should affect at least one deterministic selection", differs);
    }

    @Test
    public void rewardTypeIsDeterministicAndCanSelectEveryRewardCategory() {
        EnumSet<TowerBossRewardGenerator.RewardType> selected =
                EnumSet.noneOf(TowerBossRewardGenerator.RewardType.class);

        for (long seed = 0; seed < 500; seed++) {
            TowerBossRewardGenerator.RewardType first =
                    TowerBossRewardGenerator.selectRewardType(seed, 15, "tower_boss");
            TowerBossRewardGenerator.RewardType second =
                    TowerBossRewardGenerator.selectRewardType(seed, 15, "tower_boss");
            assertEquals(first, second);
            selected.add(first);
        }

        assertEquals(EnumSet.allOf(TowerBossRewardGenerator.RewardType.class), selected);
    }

    @Test
    public void equipmentRewardsAreIdentifiedUncursedAndPlusThree() {
        assertPlusThreeReward(TowerBossRewardGenerator.configureReward(new TestItem()));
        assertPlusThreeReward(TowerBossRewardGenerator.configureReward(new TestWeapon()));
    }

    @Test
    public void artifactRewardRemainsLevelZeroButIsIdentifiedAndUncursed() {
        TestArtifact artifact = TowerBossRewardGenerator.configureReward(new TestArtifact());

        assertEquals(0, artifact.level());
        assertTrue(artifact.isIdentified());
        assertFalse(artifact.cursed);
        assertTrue(artifact.cursedKnown);
    }

    private static void assertPlusThreeReward(Item item) {
        assertEquals(3, item.level());
        assertTrue(item.isIdentified());
        assertFalse(item.cursed);
        assertTrue(item.cursedKnown);
    }

    public static class TestWeapon extends MeleeWeapon {
    }

    public static class TestItem extends Item {
    }

    public static class TestArtifact extends Artifact {
    }
}
