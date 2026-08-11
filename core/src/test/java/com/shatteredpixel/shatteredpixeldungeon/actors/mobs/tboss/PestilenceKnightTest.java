package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Infection;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.TerminalHealingPenalty;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    public void everyPlagueSkillSeedsExactlyTenTimesItsOriginalVolume() {
        assertEquals(60, PestilenceKnight.miasmaAmountForSkill("plague_flask"));
        assertEquals(80, PestilenceKnight.miasmaAmountForSkill("quarantine"));
        assertEquals(80, PestilenceKnight.miasmaAmountForSkill("pale_charge"));
        assertEquals(70, PestilenceKnight.miasmaAmountForSkill("doom_procession"));
    }

    @Test
    public void plagueFlaskCyclesThroughThreeDiseaseColoredPotionSprites() {
        assertEquals(0, PestilenceKnight.plagueFlaskPaletteIndexForTest(0));
        assertEquals(1, PestilenceKnight.plagueFlaskPaletteIndexForTest(1));
        assertEquals(2, PestilenceKnight.plagueFlaskPaletteIndexForTest(2));
        assertEquals(0, PestilenceKnight.plagueFlaskPaletteIndexForTest(3));
        assertEquals(0x9EAD48, PestilenceKnight.plagueFlaskImpactColorForTest());
    }

    @Test
    public void laterPhaseMiasmaUsesPhaseColoredPotionProjectiles() {
        assertEquals(ItemSpriteSheet.POTION_JADE,
                PestilenceKnight.miasmaProjectileImageForTest("quarantine"));
        assertEquals(0x63D13F,
                PestilenceKnight.miasmaProjectileColorForTest("quarantine"));

        assertEquals(ItemSpriteSheet.POTION_SILVER,
                PestilenceKnight.miasmaProjectileImageForTest("pale_charge"));
        assertEquals(0xC8C3E8,
                PestilenceKnight.miasmaProjectileColorForTest("pale_charge"));
        assertEquals(ItemSpriteSheet.POTION_SILVER,
                PestilenceKnight.miasmaProjectileImageForTest("doom_procession"));
        assertEquals(0xC8C3E8,
                PestilenceKnight.miasmaProjectileColorForTest("doom_procession"));
    }

    @Test
    public void miasmaProjectileTargetsAMiddleValidSeedCell() {
        assertEquals(30, PestilenceKnight.miasmaProjectileTargetForTest(
                new int[]{10, 20, 30, 40, 50}));
        assertEquals(40, PestilenceKnight.miasmaProjectileTargetForTest(
                new int[]{-1, -1, 40, -1}));
        assertEquals(-1, PestilenceKnight.miasmaProjectileTargetForTest(new int[0]));
        assertEquals(-1, PestilenceKnight.miasmaProjectileTargetForTest(
                new int[]{-1, -1, -1}));
    }

    @Test
    public void brazierTutorialFlagDoesNotCountAsASecondDiagnosis() {
        PestilenceKnight boss = new PestilenceKnight(5);

        boss.setDiagnosisForTest(1 | (1 << 8));
        assertFalse(boss.flaskDiagnosisBonusForTest());

        boss.setDiagnosisForTest(2 | (1 << 8));
        assertTrue(boss.flaskDiagnosisBonusForTest());
    }

    @Test
    public void blockedChargeSideUsesSentinelInsteadOfCenterCell() {
        assertEquals(-1, PestilenceKnight.chargeSideCellForTest(false, 42));
        assertEquals(42, PestilenceKnight.chargeSideCellForTest(true, 42));
    }

    @Test
    public void losingSightResetsOnlyTheConsecutiveDiagnosisCount() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.setDiagnosisForTest(2 | (1 << 8));

        boss.resetDiagnosisStreakForTest();

        assertFalse(boss.flaskDiagnosisBonusForTest());
        assertEquals(1 << 8, boss.diagnosisForTest());
    }

    @Test
    public void outbreakPrescriptionsDealDamageAndApplyTheirConfirmedEffects() {
        PestilenceKnight boss = new PestilenceKnight(5);

        TestChar yellow = freshTarget();
        boss.applyPrescriptionForTest(yellow, 0);
        assertTrue(yellow.damageTaken >= 10 && yellow.damageTaken <= 16);
        assertNotNull(yellow.buff(Weakness.class));
        assertEquals(1, Infection.stacks(yellow));

        TestChar red = freshTarget();
        boss.applyPrescriptionForTest(red, 1);
        assertTrue(red.damageTaken >= 10 && red.damageTaken <= 16);
        assertNotNull(red.buff(Bleeding.class));

        TestChar purple = freshTarget();
        boss.applyPrescriptionForTest(purple, 2);
        assertTrue(purple.damageTaken >= 8 && purple.damageTaken <= 14);
        assertNotNull(purple.buff(Vertigo.class));
    }

    @Test
    public void immediateSkillsUseSpecificAnnouncementKeys() {
        assertEquals("prescription_yellow", PestilenceKnight.prescriptionAnnouncementKeyForTest(0));
        assertEquals("prescription_red", PestilenceKnight.prescriptionAnnouncementKeyForTest(1));
        assertEquals("prescription_purple", PestilenceKnight.prescriptionAnnouncementKeyForTest(2));
        assertEquals("diagnosis_mild", PestilenceKnight.diagnosisAnnouncementKeyForTest(1));
        assertEquals("diagnosis_severe", PestilenceKnight.diagnosisAnnouncementKeyForTest(3));
        assertEquals("diagnosis_critical", PestilenceKnight.diagnosisAnnouncementKeyForTest(5));
    }

    @Test
    public void terminalDiagnosisUsesInfectionBandsAndFiniteHealingPenalty() {
        PestilenceKnight boss = new PestilenceKnight(5);

        TestChar mild = freshTarget();
        boss.applyTerminalDiagnosisForTest(mild, 1);
        assertNotNull(mild.buff(Poison.class));

        TestChar severe = freshTarget();
        boss.applyTerminalDiagnosisForTest(severe, 3);
        assertNotNull(severe.buff(Weakness.class));
        assertNotNull(severe.buff(Vulnerable.class));

        TestChar critical = freshTarget();
        boss.applyTerminalDiagnosisForTest(critical, 5);
        assertTrue(critical.damageTaken >= 25 && critical.damageTaken <= 40);
        TerminalHealingPenalty penalty = critical.buff(TerminalHealingPenalty.class);
        assertNotNull(penalty);
        assertEquals(0.25f, penalty.incomingHealingReduction(), 0.001f);
        assertTrue(penalty.cooldown() > 0f && penalty.cooldown() <= 3f);
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
    public void purifierDamageBypassesOutbreakMiasmaReduction() {
        PestilenceKnight boss = new PestilenceKnight(5);
        boss.setPhaseForTest(PestilenceKnight.Phase.OUTBREAK);

        assertEquals(80, boss.applyOutbreakReductionForTest(100, new Object(), true));
        assertEquals(100, boss.applyOutbreakReductionForTest(
                100, new PestilenceArenaController(), true));
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

    private static TestChar freshTarget() {
        TestChar target = new TestChar();
        target.HT = target.HP = 100;
        return target;
    }

    private static final class TestChar extends Char {
        int damageTaken;

        @Override
        public void damage(int damage, Object source) {
            damageTaken += damage;
        }

        @Override public int attackSkill(Char target) { return 0; }
        @Override public int defenseSkill(Char enemy) { return 0; }
        @Override public int drRoll() { return 0; }
        @Override public float resist(Class effect) { return 1f; }
    }
}
