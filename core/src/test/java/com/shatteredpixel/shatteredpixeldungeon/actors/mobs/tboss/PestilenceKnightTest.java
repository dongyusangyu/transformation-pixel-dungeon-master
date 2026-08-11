package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PestilenceKnightTest {

    @Test
    public void baseAndGrowingPanelsUseEncounterDepthAndCapAtSix() {
        PestilenceKnight first = new PestilenceKnight(5);
        assertEquals(1500, first.HT);
        assertEquals(1f, first.activeDamageMultiplier(), 0.001f);
        assertEquals(10, first.drRollMinForTest());
        assertEquals(25, first.drRollMaxForTest());

        PestilenceKnight second = new PestilenceKnight(10);
        assertEquals(1575, second.HT);
        assertEquals(1.03f, second.activeDamageMultiplier(), 0.001f);

        PestilenceKnight capped = new PestilenceKnight(40);
        assertEquals(1950, capped.HT);
        assertEquals(1.18f, capped.activeDamageMultiplier(), 0.001f);
        assertEquals(13, capped.drRollMinForTest());
        assertEquals(28, capped.drRollMaxForTest());
    }

    @Test
    public void combatPanelAndRestrictionsMatchBossSpecification() {
        PestilenceKnight boss = new PestilenceKnight(5);
        assertEquals(50, boss.attackSkill(null));
        assertEquals(30, boss.defenseSkill);
        assertEquals(20, boss.damageRollMinForTest());
        assertEquals(30, boss.damageRollMaxForTest());
        assertEquals(1f, boss.speed(), 0.001f);
        assertEquals(1f, boss.attackDelay(), 0.001f);
        assertEquals(0, boss.EXP);
        assertEquals(30, boss.maxLvl);
        assertEquals(1, PestilenceKnight.KNOCKBACK_DISTANCE);
        assertTrue(boss.properties().contains(Char.Property.BOSS));
        assertTrue(boss.properties().contains(Char.Property.IMMOVABLE));
        assertTrue(boss.properties().contains(Char.Property.UNSLEEP));
    }

    @Test
    public void eachFinalDamageEventIsCappedAtOneHundredFifty() {
        PestilenceKnight boss = new PestilenceKnight(5);
        assertEquals(150, boss.capFinalDamageForTest(999));
        assertEquals(80, boss.capFinalDamageForTest(80));
        assertEquals(150, boss.capFinalDamageForTest(400));
    }

    @Test
    public void bundleRestoresGrowthAndSafeStateDefaults() {
        PestilenceKnight original = new PestilenceKnight(35);
        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        PestilenceKnight restored = new PestilenceKnight(5);
        restored.restoreFromBundle(bundle);

        assertEquals(1950, restored.HT);
        assertEquals(6, restored.growthForTest());
        assertEquals(PestilenceKnight.Phase.INCUBATION, restored.phaseForTest());
        assertEquals(PestilenceKnight.HarvestState.NONE, restored.harvestForTest());
    }

    @Test
    public void firstLockCannotBeCrossedAndImmediatelyArmsInvulnerability() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.HP = 1100;

        assertEquals(50, boss.capFinalDamageForTest(500));
        assertEquals(PestilenceKnight.HarvestState.ARMED, boss.harvestForTest());
        assertEquals(0, boss.capFinalDamageForTest(20));
        assertTrue(boss.isInvulnerable(Object.class));
    }

    @Test
    public void harvestChannelsOneActionThenHealsAndChangesPhase() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.HP = 1050;
        boss.armHarvestForTest();

        boss.advanceHarvestForTest(40);
        assertEquals(PestilenceKnight.HarvestState.CHANNELING, boss.harvestForTest());
        assertEquals(1050, boss.HP);

        boss.advanceHarvestForTest(40);
        assertEquals(PestilenceKnight.HarvestState.NONE, boss.harvestForTest());
        assertEquals(PestilenceKnight.Phase.OUTBREAK, boss.phaseForTest());
        assertEquals(1250, boss.HP);
    }

    @Test
    public void eachLockTriggersOnlyOnceEvenAfterHealingAboveIt() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.HP = 1050;
        boss.armHarvestForTest();
        boss.advanceHarvestForTest(0);
        boss.advanceHarvestForTest(0);

        boss.HP = 1400;
        assertEquals(150, boss.capFinalDamageForTest(150));
        assertEquals(PestilenceKnight.HarvestState.NONE, boss.harvestForTest());

        boss.HP = 550;
        assertEquals(25, boss.capFinalDamageForTest(100));
        assertEquals(PestilenceKnight.HarvestState.ARMED, boss.harvestForTest());
    }

    @Test
    public void plagueFlaskUsesTelegraphThenResolutionAndFourActionCooldown() {
        PestilenceKnight boss = new PestilenceKnight(5);
        assertTrue(boss.telegraphSkillForTest("plague_flask", new int[]{10, 11}));
        assertEquals("plague_flask", boss.pendingSkillForTest());
        assertEquals(0, boss.skillCooldownForTest(0));

        boss.resolveSkillForTest();
        assertEquals("", boss.pendingSkillForTest());
        assertEquals(4, boss.skillCooldownForTest(0));
        for (int i = 0; i < 4; i++) boss.finishBossActionForTest();
        assertEquals(0, boss.skillCooldownForTest(0));
    }

    @Test
    public void outbreakAndTerminalPanelsMatchPhaseRules() {
        PestilenceKnight boss = new PestilenceKnight(40);
        boss.setPhaseForTest(PestilenceKnight.Phase.OUTBREAK);
        assertEquals(120, boss.outbreakReductionForTest(150));

        boss.setPhaseForTest(PestilenceKnight.Phase.TERMINAL);
        assertEquals(2f, boss.speed(), 0.001f);
        assertEquals(1f, boss.attackDelay(), 0.001f);
        assertEquals(8, boss.drRollMinForTest());
        assertEquals(21, boss.drRollMaxForTest());
    }

    @Test
    public void terminalTenacityCanRejectOrAcceptExternalNegativeBuffs() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.setPhaseForTest(PestilenceKnight.Phase.TERMINAL);
        boss.forceTenacityRollForTest(true);
        assertFalse(boss.acceptNegativeForTest());
        boss.forceTenacityRollForTest(false);
        assertTrue(boss.acceptNegativeForTest());
    }

    @Test
    public void pendingSkillAndCooldownSurviveBundleRoundTrip() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.telegraphSkillForTest("plague_flask", new int[]{3, 4, 5});
        boss.setSkillCooldownForTest(2, 3);
        Bundle bundle = new Bundle();
        boss.storeInBundle(bundle);

        PestilenceKnight restored = new PestilenceKnight(5);
        restored.restoreFromBundle(bundle);
        assertEquals("plague_flask", restored.pendingSkillForTest());
        assertEquals(3, restored.pendingCellsForTest().length);
        assertEquals(3, restored.skillCooldownForTest(2));
    }

    @Test
    public void committedRewardFlagSurvivesBundleRoundTrip() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.markRewardDroppedForTest();
        Bundle bundle = new Bundle();
        boss.storeInBundle(bundle);

        PestilenceKnight restored = new PestilenceKnight(5);
        restored.restoreFromBundle(bundle);
        assertTrue(restored.rewardDroppedForTest());
    }
}
