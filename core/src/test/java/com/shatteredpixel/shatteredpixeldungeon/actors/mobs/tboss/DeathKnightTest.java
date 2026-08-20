package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DeathKnightTest {

    @Test
    public void basePanelMatchesApprovedValues() {
        DeathKnight boss = new DeathKnight();

        assertEquals(1500, boss.HT);
        assertEquals(1500, boss.HP);
        assertEquals(50, boss.attackSkill(null));
        assertEquals(30, boss.defenseSkill);
        assertEquals(28, boss.damageRollMinForTest());
        assertEquals(42, boss.damageRollMaxForTest());
        assertEquals(15, boss.drRollMinForTest());
        assertEquals(30, boss.drRollMaxForTest());
        assertEquals(2f, boss.speed(), 0.001f);
        assertEquals(0.5f, boss.attackDelay(), 0.001f);
        assertEquals(0, boss.EXP);
        assertEquals(30, boss.maxLvl);
        assertTrue(boss.properties().contains(Char.Property.BOSS));
        assertFalse(boss.properties().contains(Char.Property.IMMOVABLE));
        assertTrue(boss.properties().contains(Char.Property.UNSLEEP));
    }

    @Test
    public void bombardmentDamageBypassesDefenseAndArmor() {
        DeathKnight boss = new DeathKnight();
        RecordingBombardmentTarget target = new RecordingBombardmentTarget();

        boss.damageWithBombardmentForTest(target, 70);

        assertEquals(70, target.lastDamage);
        assertEquals(0, target.defenseProcCalls);
        assertEquals(0, target.drRollCalls);
        assertTrue(Arrays.asList(target.lastTags).contains(DamageTag.PHYSICAL));
        assertTrue(Arrays.asList(target.lastTags).contains(DamageTag.RANGED));
        assertTrue(Arrays.asList(target.lastTags).contains(DamageTag.NO_ARMOR));
    }

    @Test
    public void finalDamageCapsAtOneHundredFiftyAndCannotCrossFirstLock() {
        DeathKnight boss = new DeathKnight();
        boss.HP = 1300;
        assertEquals(150, boss.capFinalDamageForTest(999));
        assertEquals(DeathKnight.PhaseTransition.NONE, boss.transitionForTest());

        boss.HP = 1100;
        assertEquals(50, boss.capFinalDamageForTest(999));
        assertEquals(DeathKnight.PhaseTransition.ARMED, boss.transitionForTest());
        assertEquals(0, boss.capFinalDamageForTest(40));
        assertTrue(boss.isInvulnerable(Object.class));
    }

    @Test
    public void transitionConsumesOneActionAndClearsPendingSkill() {
        DeathKnight boss = new DeathKnight();
        boss.HP = DeathKnight.FIRST_LOCK_HP;
        boss.armTransitionForTest();
        boss.setPendingForTest(DeathKnight.Skill.LINE, new int[]{10, 11}, 1);

        assertTrue(boss.advanceTransitionForTest());
        assertEquals(DeathKnight.Phase.BREAK_FORMATION, boss.phase());
        assertEquals(DeathKnight.PhaseTransition.NONE, boss.transitionForTest());
        assertEquals(DeathKnight.Skill.NONE, boss.pendingSkillForTest());
        assertEquals(1f, boss.cooldownForTest(), 0.001f);
        assertFalse(boss.isInvulnerable(Object.class));
    }

    @Test
    public void secondLockTriggersOnceAndDeathDuelArmorIsZero() {
        DeathKnight boss = transitionedToSecondPhase();
        boss.HP = 550;

        assertEquals(25, boss.capFinalDamageForTest(100));
        assertTrue(boss.advanceTransitionForTest());
        assertEquals(DeathKnight.Phase.DEATH_DUEL, boss.phase());
        assertEquals(0, boss.drRollMinForTest());
        assertEquals(0, boss.drRollMaxForTest());
        assertEquals(3f, boss.speed(), 0.001f);

        boss.HP = 900;
        assertEquals(100, boss.capFinalDamageForTest(100));
        assertEquals(DeathKnight.PhaseTransition.NONE, boss.transitionForTest());
    }

    @Test
    public void bundleRestoresPhaseLocksAndTransitionSafely() {
        DeathKnight original = transitionedToSecondPhase();
        original.HP = 525;
        original.armTransitionForTest();
        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        DeathKnight restored = new DeathKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(DeathKnight.Phase.BREAK_FORMATION, restored.phase());
        assertEquals(DeathKnight.PhaseTransition.ARMED, restored.transitionForTest());
        assertEquals(1, restored.phaseLocksForTest());
        assertEquals(525, restored.HP);
    }

    @Test
    public void phaseOneAlternatesAfterThreeNormalActions() {
        DeathKnight boss = new DeathKnight();
        assertEquals(DeathKnight.Skill.LINE, boss.nextSkillForTest());
        boss.commitAndResolveForTest(DeathKnight.Skill.LINE);
        assertFalse(boss.bombardmentReadyForTest());
        boss.finishNormalActionForTest();
        boss.finishNormalActionForTest();
        assertFalse(boss.bombardmentReadyForTest());
        boss.finishNormalActionForTest();
        assertTrue(boss.bombardmentReadyForTest());
        assertEquals(DeathKnight.Skill.CONE, boss.nextSkillForTest());
    }

    @Test
    public void executionFrequencyAndRecoveryWindowMatchPhase() {
        DeathKnight boss = transitionedToSecondPhase();
        boss.setMainBombardmentsForTest(3);
        assertEquals(DeathKnight.Skill.EXECUTION, boss.nextSkillForTest());
        boss.commitAndResolveForTest(DeathKnight.Skill.EXECUTION);
        boss.finishNormalActionForTest();
        assertFalse(boss.bombardmentReadyForTest());
        boss.finishNormalActionForTest();
        assertTrue(boss.bombardmentReadyForTest());
        assertFalse(boss.nextSkillForTest() == DeathKnight.Skill.EXECUTION);

        boss.forcePhaseForTest(DeathKnight.Phase.DEATH_DUEL, 3);
        boss.setMainBombardmentsForTest(2);
        assertEquals(DeathKnight.Skill.EXECUTION, boss.nextSkillForTest());
    }

    @Test
    public void skillDamageRangesAndTelegraphTurnsAreExact() {
        DeathKnight boss = new DeathKnight();
        assertArrayEquals(new int[]{45, 60}, boss.damageRangeForTest(DeathKnight.Skill.LINE));
        assertArrayEquals(new int[]{40, 55}, boss.damageRangeForTest(DeathKnight.Skill.CONE));
        assertEquals(1, boss.telegraphTurnsForTest(DeathKnight.Skill.LINE));
        assertEquals(2, boss.telegraphTurnsForTest(DeathKnight.Skill.EXECUTION));

        boss.forcePhaseForTest(DeathKnight.Phase.BREAK_FORMATION, 1);
        assertArrayEquals(new int[]{55, 75}, boss.damageRangeForTest(DeathKnight.Skill.CROSS));
        assertArrayEquals(new int[]{50, 70}, boss.damageRangeForTest(DeathKnight.Skill.RING));
        assertArrayEquals(new int[]{60, 80}, boss.damageRangeForTest(DeathKnight.Skill.SOUL_LINE));
        assertArrayEquals(new int[]{70, 70}, boss.damageRangeForTest(DeathKnight.Skill.EXECUTION));

        boss.forcePhaseForTest(DeathKnight.Phase.DEATH_DUEL, 3);
        assertArrayEquals(new int[]{70, 90}, boss.damageRangeForTest(DeathKnight.Skill.LINE));
        assertArrayEquals(new int[]{90, 90}, boss.damageRangeForTest(DeathKnight.Skill.EXECUTION));
    }

    @Test
    public void phaseControlRulesAreDynamicAndDoNotAffectDamageDebuffs() {
        DeathKnight boss = new TestDeathKnight();
        assertEquals(1f, boss.resist(Paralysis.class), 0.001f);

        boss.forcePhaseForTest(DeathKnight.Phase.BREAK_FORMATION, 1);
        assertEquals(0.5f, boss.resist(Paralysis.class), 0.001f);
        assertEquals(0.5f, boss.resist(Frost.class), 0.001f);
        assertEquals(0.5f, boss.resist(Sleep.class), 0.001f);
        assertEquals(0.5f, boss.resist(Terror.class), 0.001f);
        assertEquals(0.5f, boss.resist(Charm.class), 0.001f);
        assertEquals(0.5f, boss.resist(Blindness.class), 0.001f);
        assertEquals(1f, boss.resist(Poison.class), 0.001f);

        boss.forcePhaseForTest(DeathKnight.Phase.DEATH_DUEL, 3);
        assertTrue(boss.isImmune(Paralysis.class));
        assertTrue(boss.isImmune(Frost.class));
        assertTrue(boss.isImmune(Sleep.class));
        assertFalse(boss.isImmune(Cripple.class));
        assertEquals(0.5f, boss.resist(Terror.class), 0.001f);
        assertEquals(1f, boss.resist(Poison.class), 0.001f);
    }

    @Test
    public void telegraphCountdownAndResolutionEachConsumeOneAction() {
        TestDeathKnight boss = new TestDeathKnight();
        assertTrue(boss.telegraphForTest(DeathKnight.Skill.LINE,
                new int[]{10, 11}, new DeathKnightBombardment.Band[]{
                        DeathKnightBombardment.Band.NONE, DeathKnightBombardment.Band.NONE}, -1));
        assertEquals(1f, boss.cooldownForTest(), 0.001f);
        assertEquals(DeathKnight.Skill.LINE, boss.pendingSkillForTest());
        assertEquals(1, boss.pendingTurnsForTest());

        assertTrue(boss.advancePendingForTest());
        assertEquals(2f, boss.cooldownForTest(), 0.001f);
        assertEquals(DeathKnight.Skill.NONE, boss.pendingSkillForTest());
        assertEquals(1, boss.resolveCalls);
    }

    @Test
    public void executionRewarnsBeforeResolving() {
        TestDeathKnight boss = new TestDeathKnight();
        boss.forcePhaseForTest(DeathKnight.Phase.BREAK_FORMATION, 1);
        boss.telegraphForTest(DeathKnight.Skill.EXECUTION,
                new int[]{10}, new DeathKnightBombardment.Band[]{
                        DeathKnightBombardment.Band.CORE}, -1);

        assertTrue(boss.advancePendingForTest());
        assertEquals(1, boss.pendingTurnsForTest());
        assertEquals(0, boss.resolveCalls);
        assertEquals(2f, boss.cooldownForTest(), 0.001f);
        assertTrue(boss.advancePendingForTest());
        assertEquals(1, boss.resolveCalls);
        assertEquals(DeathKnight.Skill.NONE, boss.pendingSkillForTest());
    }

    @Test
    public void executionTelegraphColorsMatchDamageBands() {
        DeathKnight boss = new DeathKnight();

        assertEquals(0xE8C84A, boss.telegraphColorForTest(
                DeathKnight.Skill.EXECUTION, DeathKnightBombardment.Band.OUTER));
        assertEquals(0xE98232, boss.telegraphColorForTest(
                DeathKnight.Skill.EXECUTION, DeathKnightBombardment.Band.INNER));
        assertEquals(0xD83C32, boss.telegraphColorForTest(
                DeathKnight.Skill.EXECUTION, DeathKnightBombardment.Band.CORE));
        assertEquals(0xD34B3F, boss.telegraphColorForTest(
                DeathKnight.Skill.LINE, DeathKnightBombardment.Band.NONE));
    }

    @Test
    public void hardControlPausesPendingAndRecoveryRearmsBeforeCountdown() {
        TestDeathKnight boss = new TestDeathKnight();
        boss.telegraphForTest(DeathKnight.Skill.EXECUTION,
                new int[]{10}, new DeathKnightBombardment.Band[]{
                        DeathKnightBombardment.Band.CORE}, -1);
        boss.pausePendingForTest();
        assertEquals(2, boss.pendingTurnsForTest());
        assertTrue(boss.pendingPausedForTest());

        assertTrue(boss.rearmPendingForTest());
        assertEquals(2, boss.pendingTurnsForTest());
        assertFalse(boss.pendingPausedForTest());
        assertEquals(2f, boss.cooldownForTest(), 0.001f);
        assertEquals(0, boss.resolveCalls);
    }

    @Test
    public void hardControlFreezesBombardmentCadence() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(5, 5);
            TestDeathKnight boss = new TestDeathKnight();
            boss.pos = 6;
            boss.sprite = new CharSprite();
            boss.commitAndResolveForTest(DeathKnight.Skill.LINE);
            boss.paralysed = 1;

            assertTrue(boss.actForTest());
            assertEquals(0, boss.normalActionsForTest());
            assertFalse(boss.bombardmentReadyForTest());
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void hardControlPreventsReadyBombardmentFromStarting() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(5, 5);
            DeathKnight boss = new DeathKnight();
            Gnoll target = new Gnoll();
            target.pos = 12;

            assertTrue(boss.canUseBombardmentForTest(target));
            boss.paralysed = 1;
            assertFalse(boss.canUseBombardmentForTest(target));
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void bundleNormalizesBrokenPendingAndProvidesRestoreGrace() {
        Bundle broken = new Bundle();
        DeathKnight seed = new DeathKnight();
        seed.storeInBundle(broken);
        broken.put("pending_skill", DeathKnight.Skill.EXECUTION.ordinal());
        broken.put("pending_cells", new int[]{10, -5});
        broken.put("pending_bands", new int[]{DeathKnightBombardment.Band.CORE.ordinal()});
        broken.put("pending_turns", 99);

        TestDeathKnight restored = new TestDeathKnight();
        restored.restoreFromBundle(broken);
        assertEquals(2, restored.pendingTurnsForTest());
        assertArrayEquals(new int[]{10}, restored.pendingCellsForTest());
        assertTrue(restored.restoreGraceForTest());
        assertTrue(restored.consumeRestoreGraceForTest());
        assertEquals(2, restored.pendingTurnsForTest());
        assertEquals(1f, restored.cooldownForTest(), 0.001f);
        assertEquals(0, restored.resolveCalls);
    }

    @Test
    public void soulLinePushesOccupiedWhiteCellThenLandsExactlyOnce() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(11, 7);
            TestDeathKnight boss = new TestDeathKnight();
            Gnoll target = new Gnoll();
            boss.pos = 37;
            target.pos = 40;
            boss.leapTarget = target;

            assertFalse(boss.beginLeapForTest(40));
            assertEquals(4, boss.leapPower);
            assertFalse(boss.leapCloseDoors);
            assertFalse(boss.leapCollisionDamage);
            assertEquals(40, boss.leapTrajectory.sourcePos.intValue());

            boss.completeLeapForTest(42, true, 2);
            assertEquals(42, target.pos);
            assertEquals(40, boss.pos);
            assertEquals(1, boss.nextCalls);

            boss.completeLeapForTest(42, true, 2);
            assertEquals(1, boss.nextCalls);
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void soulLineAcceptsShortenedPushWhenWallLimitsDistance() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(11, 7);
            TestDeathKnight boss = new TestDeathKnight();
            Gnoll target = new Gnoll();
            boss.pos = 37;
            target.pos = 40;
            boss.leapTarget = target;

            assertFalse(boss.beginLeapForTest(40));
            boss.completeLeapForTest(41, true, 1);

            assertEquals(41, target.pos);
            assertEquals(40, boss.pos);
            assertEquals(1, boss.nextCalls);
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void soulLineStopsBeforeWhiteCellWhenOccupantCannotBePushed() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(11, 7);
            TestDeathKnight boss = new TestDeathKnight();
            Gnoll target = new Gnoll();
            boss.pos = 37;
            target.pos = 40;
            boss.leapTarget = target;

            assertFalse(boss.beginLeapForTest(40));
            boss.completeLeapForTest(40, true, 0);

            assertEquals(40, target.pos);
            assertEquals(39, boss.pos);
            assertEquals(1, boss.nextCalls);
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void everySecondLandedNormalMeleeAttackPushesOnce() {
        TestDeathKnight boss = new TestDeathKnight();
        Gnoll target = new Gnoll();

        boss.normalMeleeResolvedForTest(target, true,
                DamageTag.PHYSICAL, DamageTag.MELEE);
        assertEquals(0, boss.normalPushCalls);

        boss.normalMeleeResolvedForTest(target, false,
                DamageTag.PHYSICAL, DamageTag.MELEE);
        assertEquals(0, boss.normalPushCalls);

        boss.normalMeleeResolvedForTest(target, true,
                DamageTag.PHYSICAL, DamageTag.RANGED);
        assertEquals(0, boss.normalPushCalls);

        boss.normalMeleeResolvedForTest(target, true,
                DamageTag.PHYSICAL, DamageTag.MELEE);
        assertEquals(1, boss.normalPushCalls);
        assertTrue(boss.lastNormalPushTarget == target);
    }

    @Test
    public void severingLinePushesEachAffectedTargetOneCell() {
        TestDeathKnight boss = new TestDeathKnight();
        Gnoll target = new Gnoll();

        boss.applySkillAftermathForTest(DeathKnight.Skill.LINE, target,
                DeathKnightBombardment.Band.NONE);
        assertEquals(1, boss.bombardmentPushCalls);
        assertTrue(boss.lastBombardmentPushTarget == target);

        boss.applySkillAftermathForTest(DeathKnight.Skill.CROSS, target,
                DeathKnightBombardment.Band.NONE);
        assertEquals(1, boss.bombardmentPushCalls);
    }

    @Test
    public void pressureTargetPrefersTheMostWallConstrainedHeroNeighbor() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(9, 9);
            TestDeathKnight boss = new TestDeathKnight();
            boss.pos = 4 * 9 + 1;
            int heroCell = 4 * 9 + 4;
            Dungeon.level.solid[2 * 9 + 6] = true;
            Dungeon.level.solid[3 * 9 + 6] = true;
            Dungeon.level.solid[4 * 9 + 6] = true;
            Dungeon.level.passable[2 * 9 + 6] = false;
            Dungeon.level.passable[3 * 9 + 6] = false;
            Dungeon.level.passable[4 * 9 + 6] = false;

            assertEquals(3 * 9 + 5, boss.pressureTargetForTest(heroCell));
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void bombardmentPrefersCurrentLivingEnemyOverPlayerFallback() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(15, 15);
            TestDeathKnight boss = new TestDeathKnight();
            Gnoll enemy = new Gnoll();
            Gnoll fallback = new Gnoll();
            boss.pos = 2 + 7 * 15;
            enemy.pos = 5 + 7 * 15;
            fallback.pos = 2 + 3 * 15;
            boss.setEnemyForTest(enemy);
            boss.fallbackTarget = fallback;

            assertSame(enemy, boss.bombardmentTargetForTest());
            DeathKnightBombardment.Plan plan = boss.createPlanForTest(
                    DeathKnight.Skill.LINE, boss.bombardmentTargetForTest());
            assertTrue(plan.contains(14 + 7 * 15));
            assertFalse(plan.contains(2));
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void bombardmentFallsBackWhenCurrentEnemyIsDeadOrInvalid() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(15, 15);
            TestDeathKnight boss = new TestDeathKnight();
            Gnoll enemy = new Gnoll();
            Gnoll fallback = new Gnoll();
            enemy.pos = 20;
            fallback.pos = 21;
            boss.fallbackTarget = fallback;

            enemy.HP = 0;
            boss.setEnemyForTest(enemy);
            assertSame(fallback, boss.bombardmentTargetForTest());

            enemy.HP = enemy.HT;
            enemy.pos = -1;
            assertSame(fallback, boss.bombardmentTargetForTest());
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void bombardmentReadinessIgnoresVisibilityAndInvisibility() {
        Level previous = Dungeon.level;
        try {
            Dungeon.level = openLevel(15, 15);
            TestDeathKnight boss = new TestDeathKnight();
            Gnoll target = new Gnoll();
            target.pos = 14 + 14 * 15;
            target.invisible = 10;

            assertTrue(boss.canUseBombardmentForTest(target));
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void committedRewardFlagSurvivesBundleRoundTrip() {
        DeathKnight original = new DeathKnight();
        original.markRewardDroppedForTest();
        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        DeathKnight restored = new DeathKnight();
        restored.restoreFromBundle(bundle);

        assertTrue(restored.rewardDroppedForTest());
    }

    @Test
    public void meleeBreakCadenceSurvivesBundleRoundTrip() {
        TestDeathKnight original = new TestDeathKnight();
        Gnoll target = new Gnoll();
        original.normalMeleeResolvedForTest(target, true,
                DamageTag.PHYSICAL, DamageTag.MELEE);
        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        TestDeathKnight restored = new TestDeathKnight();
        restored.restoreFromBundle(bundle);
        restored.normalMeleeResolvedForTest(target, true,
                DamageTag.PHYSICAL, DamageTag.MELEE);

        assertEquals(1, restored.normalPushCalls);
    }

    @Test
    public void phaseTransitionStartsTheNewPhaseWithFreshBombardmentCadence() {
        DeathKnight boss = new DeathKnight();
        boss.setMainBombardmentsForTest(3);
        boss.commitAndResolveForTest(DeathKnight.Skill.CONE);
        boss.HP = DeathKnight.FIRST_LOCK_HP;
        boss.armTransitionForTest();

        assertTrue(boss.advanceTransitionForTest());
        assertEquals(0, boss.mainBombardmentsForTest());
        assertEquals(0, boss.skillIndexForTest());
        assertFalse(boss.bombardmentReadyForTest());
        boss.finishNormalActionForTest();
        assertFalse(boss.bombardmentReadyForTest());
        boss.finishNormalActionForTest();
        assertTrue(boss.bombardmentReadyForTest());
    }

    @Test
    public void coverBreakRunsAfterAllDamageAndOnlyOncePerResolution() {
        TestDeathKnight boss = new TestDeathKnight();
        boss.suppressAftermath = true;
        RecordingBombardmentTarget target = new RecordingBombardmentTarget();
        target.pos = 10;
        boss.coverTarget = target;
        Actor.add(target);
        try {
            boss.setPendingForTest(DeathKnight.Skill.LINE, new int[]{10}, 1);

            assertTrue(boss.advancePendingForTest());
            assertTrue(target.lastDamage >= 45 && target.lastDamage <= 60);
            assertEquals(1, boss.coverBreakCalls);
            assertEquals(target.lastDamage, boss.coverBreakDamageAtCall);
            assertEquals(DeathKnight.Skill.NONE, boss.pendingSkillForTest());
        } finally {
            Actor.remove(target);
        }
    }

    @Test
    public void cancelledOrPausedBombardmentDoesNotBreakCover() {
        TestDeathKnight boss = new TestDeathKnight();
        boss.suppressAftermath = true;
        boss.forcePhaseForTest(DeathKnight.Phase.BREAK_FORMATION, 1);
        boss.setPendingForTest(DeathKnight.Skill.EXECUTION, new int[]{10}, 2);
        boss.armTransitionForTest();

        assertTrue(boss.advanceTransitionForTest());
        assertEquals(0, boss.coverBreakCalls);
    }

    @Test
    public void allResolvedSkillKindsUseTheSameCoverBreakHook() {
        for (DeathKnight.Skill skill : new DeathKnight.Skill[]{
                DeathKnight.Skill.LINE, DeathKnight.Skill.CONE,
                DeathKnight.Skill.CROSS, DeathKnight.Skill.RING,
                DeathKnight.Skill.SOUL_LINE, DeathKnight.Skill.EXECUTION}) {
            TestDeathKnight boss = new TestDeathKnight();
            boss.suppressAftermath = true;
            RecordingBombardmentTarget target = new RecordingBombardmentTarget();
            target.pos = 10;
            boss.coverTarget = target;
            Actor.add(target);
            try {
                int turns = skill == DeathKnight.Skill.EXECUTION ? 2 : 1;
                boss.setPendingForTest(skill, new int[]{10}, turns);
                boss.advancePendingForTest();
                if (skill == DeathKnight.Skill.EXECUTION) boss.advancePendingForTest();
                assertEquals(skill.name(), 1, boss.coverBreakCalls);
            } finally {
                Actor.remove(target);
            }
        }
    }

    @Test
    public void pendingTargetAndCoverNoticeSurviveBundleRoundTrip() {
        DeathKnight original = new DeathKnight();
        original.setPendingTargetForTest(17);
        original.markCoverBreakNoticeForTest();

        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        DeathKnight restored = new DeathKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(17, restored.pendingTargetCellForTest());
        assertTrue(restored.coverBreakNoticeAnnouncedForTest());
    }

    @Test
    public void invalidPendingTargetFallsBackWithoutBreakingRestore() {
        Bundle bundle = new Bundle();
        new DeathKnight().storeInBundle(bundle);
        bundle.put("pending_target_cell", -999);

        DeathKnight restored = new DeathKnight();
        restored.restoreFromBundle(bundle);

        assertEquals(-1, restored.pendingTargetCellForTest());
    }

    private static DeathKnight transitionedToSecondPhase() {
        DeathKnight boss = new DeathKnight();
        boss.HP = DeathKnight.FIRST_LOCK_HP;
        boss.armTransitionForTest();
        boss.advanceTransitionForTest();
        return boss;
    }

    private static TestLevel openLevel(int width, int height) {
        TestLevel level = new TestLevel();
        level.setSize(width, height);
        level.blobs = new HashMap<>();
        Arrays.fill(level.passable, true);
        Arrays.fill(level.openSpace, true);
        Arrays.fill(level.solid, false);
        Arrays.fill(level.avoid, false);
        return level;
    }

    private static final class TestDeathKnight extends DeathKnight {
        int resolveCalls;
        Char leapTarget;
        Char pushedTarget;
        Ballistica leapTrajectory;
        int leapPower;
        boolean leapCloseDoors;
        boolean leapCollisionDamage;
        WandOfBlastWave.KnockbackCallback leapCallback;
        int nextCalls;
        int normalPushCalls;
        Char lastNormalPushTarget;
        int bombardmentPushCalls;
        Char lastBombardmentPushTarget;
        Char fallbackTarget;
        RecordingBombardmentTarget coverTarget;
        int coverBreakCalls;
        int coverBreakDamageAtCall = -1;
        boolean suppressAftermath;

        @Override
        protected float baseResist(Class effect) {
            return 1f;
        }

        @Override
        protected void applySkillAftermath(DeathKnight.Skill skill, Char target,
                                            DeathKnightBombardment.Band band) {
            if (!suppressAftermath) super.applySkillAftermath(skill, target, band);
        }

        @Override
        protected Char bombardmentFallbackTarget() {
            return fallbackTarget;
        }

        @Override
        protected boolean resolvePendingSkill() {
            resolveCalls++;
            return super.resolvePendingSkill();
        }

        @Override
        protected Char charAt(int cell) {
            return leapTarget != null && leapTarget.pos == cell ? leapTarget : null;
        }

        @Override
        protected void startLeapPush(Char target, Ballistica trajectory, int power,
                                     boolean closeDoors, boolean collisionDamage,
                                     WandOfBlastWave.KnockbackCallback callback) {
            pushedTarget = target;
            leapTrajectory = trajectory;
            leapPower = power;
            leapCloseDoors = closeDoors;
            leapCollisionDamage = collisionDamage;
            leapCallback = callback;
        }

        @Override
        protected void pushNormalMeleeTarget(Char target) {
            normalPushCalls++;
            lastNormalPushTarget = target;
        }

        @Override
        protected void pushBombardmentTarget(Char target) {
            bombardmentPushCalls++;
            lastBombardmentPushTarget = target;
        }

        @Override
        protected boolean breakNearbyCoverAfterResolution(int fallbackTargetCell) {
            coverBreakCalls++;
            coverBreakDamageAtCall = coverTarget == null ? -1 : coverTarget.lastDamage;
            return true;
        }

        @Override
        protected void occupyBossCell() {
        }

        @Override
        public void next() {
            nextCalls++;
        }

        void completeLeapForTest(int targetCell, boolean resolved, int distance) {
            pushedTarget.pos = targetCell;
            leapCallback.call(resolved, distance);
        }
    }

    private static final class TestLevel extends Level {
        @Override
        protected boolean build() {
            return true;
        }

        @Override
        protected void createMobs() {
        }

        @Override
        protected void createItems() {
        }
    }

    private static final class RecordingBombardmentTarget extends Gnoll {
        int lastDamage = -1;
        int defenseProcCalls;
        int drRollCalls;
        DamageTag[] lastTags = new DamageTag[0];

        @Override
        public int defenseProc(Char enemy, int damage, DamageTag... damageTags) {
            defenseProcCalls++;
            return 0;
        }

        @Override
        public int drRoll() {
            drRollCalls++;
            return 999;
        }

        @Override
        public void damage(int damage, Object source, DamageTag... damageTags) {
            lastDamage = damage;
            lastTags = damageTags;
        }
    }
}
