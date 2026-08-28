package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightEquipmentSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightMagicLease;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightOverburden;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HungerKnightTalentSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HungerKnightTest {

    private long oldSeed;
    private int oldDepth;
    private Hero oldHero;

    @Before
    public void setUp() {
        oldSeed = Dungeon.seed;
        oldDepth = Dungeon.depth;
        oldHero = Dungeon.hero;
        Dungeon.seed = 0x484E474552L;
        Dungeon.depth = 5;
    }

    @After
    public void tearDown() {
        Dungeon.seed = oldSeed;
        Dungeon.depth = oldDepth;
        Dungeon.hero = oldHero;
    }

    @Test
    public void basePanelAndEquipmentDrivenCombatMatchTheContract() {
        HungerKnight boss = new HungerKnight();

        assertEquals(1500, boss.HT);
        assertEquals(1500, boss.HP);
        assertEquals(50, boss.baseAccuracyForTest());
        assertEquals(25, boss.baseEvasionForTest());
        assertEquals(0, boss.EXP);
        assertEquals(30, boss.maxLvl);
        assertEquals(TowerBossGenerator.HUNGER_KNIGHT_ID, boss.towerBossId());

        assertNotNull(boss.weaponClassForTest());
        assertNotNull(boss.armorClassForTest());
        assertNotNull(boss.weaponEnchantClassForTest());
        assertNotNull(boss.armorGlyphClassForTest());
        assertTrue(boss.weaponLevelForTest() >= 0 && boss.weaponLevelForTest() <= 5);
        assertTrue(boss.armorLevelForTest() >= 0 && boss.armorLevelForTest() <= 5);
    }

    @Test
    public void equipmentLevelDistributionHasSixExpectedBuckets() {
        int[] rolls = {0, 4, 5, 14, 15, 29, 30, 49, 50, 74, 75, 99};
        int[] expected = {0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5};

        for (int i = 0; i < rolls.length; i++) {
            assertEquals(expected[i], HungerKnight.equipmentLevelForRollForTest(rolls[i]));
        }
    }

    @Test
    public void plusFiveEquipmentLevelsSurviveBundleRoundTrip() {
        HungerKnight source = new HungerKnight();
        source.setEquipmentLevelsForTest(5, 5);

        Bundle bundle = new Bundle();
        source.storeInBundle(bundle);

        HungerKnight restored = new HungerKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(5, restored.weaponLevelForTest());
        assertEquals(5, restored.armorLevelForTest());
    }

    @Test
    public void equipmentRollIsDeterministicAndDoesNotConsumeGlobalRandom() {
        HungerKnight first = new HungerKnight();
        HungerKnight second = new HungerKnight();
        assertEquals(first.weaponClassForTest(), second.weaponClassForTest());
        assertEquals(first.weaponLevelForTest(), second.weaponLevelForTest());
        assertEquals(first.armorClassForTest(), second.armorClassForTest());
        assertEquals(first.armorLevelForTest(), second.armorLevelForTest());

        Random.pushGenerator(0xCAFE);
        int expectedFirst = Random.Int();
        int expectedSecond = Random.Int();
        Random.popGenerator();

        Random.pushGenerator(0xCAFE);
        assertEquals(expectedFirst, Random.Int());
        new HungerKnight();
        assertEquals(expectedSecond, Random.Int());
        Random.popGenerator();
    }

    @Test
    public void finalDamageIsCappedAndCannotCrossAnUnfinishedPhaseLock() {
        HungerKnight boss = new HungerKnight();
        assertEquals(150, boss.finalDamageForTest(999));

        boss.HP = 1080;
        assertEquals(80, boss.finalDamageForTest(150));
        assertTrue(boss.transitionArmedForTest());
        assertEquals(0, boss.finalDamageForTest(100));

        boss.completeTransitionForTest();
        assertEquals(HungerKnight.Phase.DEPLETION, boss.phase());
        assertFalse(boss.transitionArmedForTest());
    }

    @Test
    public void equipmentAndPhaseStateSurviveBundleRoundTrip() {
        HungerKnight source = new HungerKnight();
        source.HP = 1000;
        source.completeTransitionForTest();
        Bundle bundle = new Bundle();
        source.storeInBundle(bundle);

        Dungeon.seed++;
        HungerKnight restored = new HungerKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(source.phase(), restored.phase());
        assertEquals(source.weaponClassForTest(), restored.weaponClassForTest());
        assertEquals(source.weaponLevelForTest(), restored.weaponLevelForTest());
        assertEquals(source.weaponEnchantClassForTest(), restored.weaponEnchantClassForTest());
        assertEquals(source.armorClassForTest(), restored.armorClassForTest());
        assertEquals(source.armorLevelForTest(), restored.armorLevelForTest());
        assertEquals(source.armorGlyphClassForTest(), restored.armorGlyphClassForTest());
    }

    @Test
    public void phasePressuresReplaceAndOwnerScopedCleanupRestoresTheHero() {
        Hero hero = TestHeroFactory.create();
        hero.talents.add(new java.util.LinkedHashMap<Talent, Integer>() {{
            put(Talent.HEARTY_MEAL, 1);
            put(Talent.PROVOKED_ANGER, 1);
        }});
        Dungeon.hero = hero;
        HungerKnight boss = new HungerKnight();

        boss.initializeEncounterForTest();
        assertTrue(HungerKnightEquipmentSeal.isActive(hero));
        assertNotNull(hero.buff(HungerKnightTalentSeal.class));

        boss.HP = HungerKnight.FIRST_LOCK_HP;
        boss.completeTransitionForTest();
        assertFalse(HungerKnightEquipmentSeal.isActive(hero));
        assertNotNull(hero.buff(HungerKnightOverburden.class));

        boss.HP = HungerKnight.SECOND_LOCK_HP;
        boss.completeTransitionForTest();
        assertNotNull(hero.buff(HungerKnightMagicLease.class));
        assertNotNull(hero.buff(MagicImmune.class));

        boss.cleanupEncounterForTest();
        assertNull(hero.buff(HungerKnightTalentSeal.class));
        assertNull(hero.buff(HungerKnightOverburden.class));
        assertNull(hero.buff(HungerKnightMagicLease.class));
        assertNull(hero.buff(MagicImmune.class));
    }

    @Test
    public void orphanedEncounterEffectsAreRemovedWhenTheBossCannotBeRestored() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        int missingBossId = 41;

        Buff.affect(hero, MagicImmune.class, 12f);
        HungerKnightEquipmentSeal.attach(hero, missingBossId);
        HungerKnightOverburden.attach(hero, missingBossId);
        HungerKnightMagicLease.acquire(hero, missingBossId);
        HungerKnightTalentSeal talentSeal = new HungerKnightTalentSeal();
        talentSeal.setOwnerId(missingBossId);
        talentSeal.add(Talent.HEARTY_MEAL);
        talentSeal.attachTo(hero);

        HungerKnight.cleanupOrphanedEffects(hero);

        assertNull(hero.buff(HungerKnightEquipmentSeal.class));
        assertNull(hero.buff(HungerKnightOverburden.class));
        assertNull(hero.buff(HungerKnightMagicLease.class));
        assertNull(hero.buff(HungerKnightTalentSeal.class));
        assertNotNull("cleanup restores immunity that existed before the encounter",
                hero.buff(MagicImmune.class));

        hero.live();
        assertNull("resurrection clears the restored temporary immunity",
                hero.buff(MagicImmune.class));
    }

    @Test
    public void phaseChangesSkillCadenceAndFinalAttackSpeed() {
        HungerKnight boss = new HungerKnight();
        assertEquals(8, boss.heavyCooldownLengthForTest());
        assertEquals(6, boss.thrustCooldownLengthForTest());
        assertEquals(1f, boss.phaseAttackDelayMultiplierForTest(), 0.001f);
        boss.completeTransitionForTest();
        assertEquals(7, boss.heavyCooldownLengthForTest());
        assertEquals(5, boss.thrustCooldownLengthForTest());
        boss.completeTransitionForTest();
        assertEquals(0.75f, boss.phaseAttackDelayMultiplierForTest(), 0.001f);
    }

    @Test
    public void pendingSkillSurvivesSaveAndGetsOneRewarningAction() {
        HungerKnight source = new HungerKnight();
        source.setPendingForTest(HungerKnight.Skill.THRUST,
                new int[]{12, 13, 14}, 37, 1);
        source.setPendingPausedForTest(true);
        Bundle bundle = new Bundle();
        source.storeInBundle(bundle);

        HungerKnight restored = new HungerKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(HungerKnight.Skill.THRUST, restored.pendingSkillForTest());
        assertArrayEquals(new int[]{12, 13, 14}, restored.pendingCellsForTest());
        assertEquals(37, restored.pendingTargetCellForTest());
        assertEquals(1, restored.pendingTurnsForTest());
        assertTrue(restored.pendingPausedForTest());
        assertTrue(restored.restoreGraceForTest());
        assertTrue(restored.consumeRestoreGraceForTest());
        assertFalse(restored.restoreGraceForTest());
        assertFalse(restored.pendingPausedForTest());
        assertEquals(HungerKnight.Skill.THRUST, restored.pendingSkillForTest());
    }

    @Test
    public void comboResumePersistsTheNumberOfAlreadyResolvedStrikes() {
        HungerKnight source = new HungerKnight();
        source.setPendingForTest(HungerKnight.Skill.COMBO, new int[]{21, 22}, 22, 1);
        source.setComboStrikeIndexForTest(2);
        Bundle bundle = new Bundle();
        source.storeInBundle(bundle);

        HungerKnight restored = new HungerKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(HungerKnight.Skill.COMBO, restored.pendingSkillForTest());
        assertEquals(2, restored.comboStrikeIndexForTest());
    }

    @Test
    public void invalidSavedOrdinalsAndPendingStateFallBackSafely() {
        HungerKnight source = new HungerKnight();
        Bundle bundle = new Bundle();
        source.storeInBundle(bundle);
        bundle.put("phase", Integer.MAX_VALUE);
        bundle.put("transition", -7);
        bundle.put("adaptation", Integer.MAX_VALUE);
        bundle.put("pending_skill", Integer.MAX_VALUE);
        bundle.put("pending_cells", new int[]{-1, Integer.MAX_VALUE});
        bundle.put("pending_turns", -4);

        HungerKnight restored = new HungerKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(HungerKnight.Phase.AWAKENING, restored.phase());
        assertEquals(HungerKnight.Skill.NONE, restored.pendingSkillForTest());
        assertArrayEquals(new int[0], restored.pendingCellsForTest());
        assertFalse(restored.restoreGraceForTest());
    }

    @Test
    public void phaseTransitionRestartsTheCurrentAdaptationActionClock() {
        Hero hero = TestHeroFactory.create();
        Dungeon.hero = hero;
        HungerKnight boss = new HungerKnight();
        boss.initializeEncounterForTest();
        for (int i = 0; i < 12; i++) boss.finishBossActionForTest();
        assertEquals(12, boss.adaptationActionsForTest());

        boss.HP = HungerKnight.FIRST_LOCK_HP;
        boss.completeTransitionForTest();

        // The transition itself is the first completed action of the new phase.
        assertEquals(1, boss.adaptationActionsForTest());
    }

    @Test
    public void finalPhaseComboTriggersAreIndependentAndOneShot() {
        HungerKnight boss = new HungerKnight();
        boss.forcePhaseForTest(HungerKnight.Phase.EXHAUSTION, 3);

        assertEquals(1, boss.nextComboTriggerForTest());
        boss.consumeComboTriggerForTest(1);
        boss.HP = 199;
        assertEquals(2, boss.nextComboTriggerForTest());
        boss.consumeComboTriggerForTest(2);
        boss.HP = 99;
        assertEquals(4, boss.nextComboTriggerForTest());
        boss.consumeComboTriggerForTest(4);
        assertEquals(0, boss.nextComboTriggerForTest());
    }

    @Test
    public void specialCooldownsTickOnlyWhenABossActionFinishes() {
        HungerKnight boss = new HungerKnight();
        boss.setCooldownsForTest(2, 1, 3);

        boss.finishBossActionForTest();

        assertArrayEquals(new int[]{1, 0, 2}, boss.cooldownsForTest());
    }

    @Test
    public void thrustMotionCompletionReleasesPendingAction() {
        HungerKnight boss = new HungerKnight();
        boss.setPendingForTest(HungerKnight.Skill.THRUST, new int[]{12}, 12, 1);
        boss.setThrustResolvingForTest(true);

        boss.onMotionComplete();

        assertFalse(boss.thrustResolvingForTest());
        assertEquals(HungerKnight.Skill.NONE, boss.pendingSkillForTest());
    }

    @Test
    public void resourceAdaptationAddsHungerToEveryKindOfMeleeHit() {
        HungerKnight boss = new HungerKnight();

        assertEquals(15, boss.meleeHungerForTest(HungerKnight.Skill.NONE));
        assertEquals(55, boss.meleeHungerForTest(HungerKnight.Skill.HEAVY));
        assertEquals(30, boss.meleeHungerForTest(HungerKnight.Skill.THRUST));
        assertEquals(0, boss.meleeHungerForTest(HungerKnight.Skill.QUAKE));
        assertEquals(0, boss.meleeHungerForTest(HungerKnight.Skill.COMBO));

        boss.forceAdaptationForTest(HungerKnight.Adaptation.RESOURCE);
        assertEquals(35, boss.meleeHungerForTest(HungerKnight.Skill.NONE));
        assertEquals(75, boss.meleeHungerForTest(HungerKnight.Skill.HEAVY));
        assertEquals(50, boss.meleeHungerForTest(HungerKnight.Skill.THRUST));
        assertEquals(20, boss.meleeHungerForTest(HungerKnight.Skill.QUAKE));
        assertEquals(20, boss.meleeHungerForTest(HungerKnight.Skill.COMBO));
    }
}
