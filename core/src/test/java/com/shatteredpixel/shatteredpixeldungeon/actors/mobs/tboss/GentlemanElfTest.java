package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Exhilaration;
import com.watabou.utils.Bundle;
import org.junit.Test;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import static org.junit.Assert.*;

public class GentlemanElfTest {
    @Test public void basePanelAndBossRegistrationMatchSpec() {
        GentlemanElf boss = new GentlemanElf();
        assertEquals(1500, boss.HT); assertEquals(1500, boss.HP);
        assertEquals(50, boss.attackSkill(null)); assertEquals(25, boss.defenseSkill);
        assertEquals(0, boss.EXP); assertEquals(30, boss.maxLvl);
        assertEquals(0.8f, boss.speed(), 0.001f);
        assertEquals(20, boss.damageRollMinForTest());
        assertEquals(40, boss.damageRollMaxForTest());
        assertEquals(0, boss.drRollMinForTest());
        assertEquals(20, boss.drRollMaxForTest());
        assertTrue(boss.properties().contains(Char.Property.BOSS));
    }
	@Test public void toastAndDevourNeverFallbackToHeroWithoutAnEnemy() {
		Char hero = new GentlemanElf();
		assertNull(new GentlemanElf().devourTargetForTest(null, hero));
		assertNull(new GentlemanElf().toastTargetForTest(null));
	}
	@Test public void cupDashOnlyCoversFourThroughEightTilesAndStopsThreeAway() {
		assertFalse(GentlemanElf.cupDashEligibleForTest(3));
		assertTrue(GentlemanElf.cupDashEligibleForTest(4));
		assertTrue(GentlemanElf.cupDashEligibleForTest(8));
		assertFalse(GentlemanElf.cupDashEligibleForTest(9));
		assertEquals(3, GentlemanElf.cupDashLandingDistanceForTest());
	}
    @Test public void damageCapAndPhaseLocksAreMonotonic() {
        GentlemanElf boss = new GentlemanElf(); boss.HP = 1400;
        assertEquals(50, boss.capFinalDamageForTest(999));
        boss.HP = 1250; assertEquals(50, boss.capFinalDamageForTest(999));
        assertEquals(1, boss.phaseLocks());
        assertTrue(boss.isInvulnerable(Object.class));
        boss.setPhaseForTest(GentlemanElf.Phase.CUP_CONTEST, 1); boss.HP = 650;
        assertEquals(50, boss.capFinalDamageForTest(999)); assertEquals(2, boss.phaseLocks());
    }
    @Test public void phaseTransitionConsumesActionAndBundlePreservesState() {
        GentlemanElf boss = new GentlemanElf(); boss.HP = GentlemanElf.FIRST_LOCK_HP;
        boss.setPhaseForTest(GentlemanElf.Phase.TOAST_GAME, 1);
        Bundle b = new Bundle(); boss.storeInBundle(b);
        GentlemanElf restored = new GentlemanElf(); restored.restoreFromBundle(b);
        assertEquals(GentlemanElf.Phase.TOAST_GAME, restored.phase());
        assertEquals(1, restored.phaseLocks());
        assertEquals(GentlemanElf.Skill.NONE, restored.pendingSkill());
    }
    @Test public void firstPhaseRhythmIsStable() {
        assertArrayEquals(new GentlemanElf.Skill[]{GentlemanElf.Skill.NORMAL, GentlemanElf.Skill.NORMAL,
                GentlemanElf.Skill.TOAST, GentlemanElf.Skill.NORMAL, GentlemanElf.Skill.NORMAL,
                GentlemanElf.Skill.DEVOUR}, new GentlemanElf().nextSixActionsForTest());
        GentlemanElf boss = new GentlemanElf();
        assertEquals(GentlemanElf.Skill.NORMAL, boss.nextSkillForTest());
        boss.finishBossActionForTest();
        assertEquals(GentlemanElf.Skill.NORMAL, boss.nextSkillForTest());
        boss.finishBossActionForTest();
        assertEquals(GentlemanElf.Skill.TOAST, boss.nextSkillForTest());
        boss.finishBossActionForTest();
        assertEquals(GentlemanElf.Skill.NORMAL, boss.nextSkillForTest());
    }
    @Test public void toastWineStrictlyAlternatesAndPersists() {
        GentlemanElf boss = new GentlemanElf();
        assertEquals(GentlemanElf.WineState.DRUNKENNESS, boss.nextWineState());
        boss.advanceWineSequence();
        assertEquals(GentlemanElf.WineState.EXHILARATION, boss.nextWineState());
        Bundle bundle = new Bundle(); boss.storeInBundle(bundle);
        GentlemanElf restored = new GentlemanElf(); restored.restoreFromBundle(bundle);
        assertEquals(GentlemanElf.WineState.EXHILARATION, restored.nextWineState());
        restored.advanceWineSequence();
        assertEquals(GentlemanElf.WineState.DRUNKENNESS, restored.nextWineState());
    }
    @Test public void wineCupIsAttackableObjectiveAndMatchesPanel() {
        ElfWineCup cup = new ElfWineCup();
        assertEquals(30, cup.HT); assertEquals(30, cup.HP);
        assertFalse(cup.heroShouldInteract());
        assertEquals(0, cup.attackSkill(null));
        assertEquals(0, cup.damageRoll());
        assertEquals(0, cup.EXP);
    }
    @Test public void illusionAlwaysCreatesARealBossSprite() {
		GentlemanElf owner = new GentlemanElf();
		GentlemanElfIllusion illusion = new GentlemanElfIllusion(owner);
        assertEquals(com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.GentlemanElfSprite.class,
                illusion.spriteClassForTest());
        assertEquals(1, illusion.HT); assertEquals(1, illusion.HP);
        assertEquals(25, illusion.defenseSkill);
        assertEquals(15, illusion.damageRollMinForTest());
        assertEquals(25, illusion.damageRollMaxForTest());
		assertEquals(GentlemanElf.class, illusion.identityMessageClassForTest());
		assertSame(illusion.HUNTING, illusion.state);
    }
    @Test public void mirrorIllusionsTakeTwoPositiveHitsAndThenBerserk() {
        GentlemanElf boss = new GentlemanElf();
        boss.setPhaseForTest(GentlemanElf.Phase.MIRROR_TEST, 2);
        GentlemanElfIllusion illusion = boss.createIllusionsForTest()[0];
        illusion.HP = illusion.HT = 100;
        illusion.recordPositiveHitForTest(); assertEquals(1, illusion.positiveHits());
        illusion.recordPositiveHitForTest(); assertEquals(2, illusion.positiveHits());
        boss.illusionDied(); boss.illusionDied();
        assertEquals(GentlemanElf.Phase.BERSERK, boss.phase());
        assertEquals(1.6f, boss.speed(), 0.001f); assertEquals(0.5f, boss.attackDelay(), 0.001f);
    }
    @Test public void pendingTelegraphSnapshotSurvivesBundleRoundTrip() {
        GentlemanElf boss = new GentlemanElf();
        boss.stagePendingForTest(GentlemanElf.Skill.TOAST, new int[]{12,13,14}, 13,
                GentlemanElf.WineState.EXHILARATION);
        Bundle bundle = new Bundle(); boss.storeInBundle(bundle);
        GentlemanElf restored = new GentlemanElf(); restored.restoreFromBundle(bundle);
        assertEquals(GentlemanElf.Skill.TOAST, restored.pendingSkill());
        assertArrayEquals(new int[]{12,13,14}, restored.pendingCellsForTest());
        assertEquals(13, restored.pendingLandingForTest());
        assertEquals(GentlemanElf.WineState.EXHILARATION, restored.pendingWineForTest());
		assertTrue(restored.restoreGraceForTest());
	}
	@Test public void banquetTelegraphWithNoCellsSurvivesBundleRoundTrip() {
		GentlemanElf boss = new GentlemanElf();
		boss.stagePendingForTest(GentlemanElf.Skill.BANQUET, new int[0], -1,
				GentlemanElf.WineState.DRUNKENNESS);
		Bundle bundle = new Bundle(); boss.storeInBundle(bundle);
		GentlemanElf restored = new GentlemanElf(); restored.restoreFromBundle(bundle);
		assertEquals(GentlemanElf.Skill.BANQUET, restored.pendingSkill());
		assertTrue(restored.restoreGraceForTest());
	}
    @Test public void introChoiceIsSingleUseAndPersists() {
        GentlemanElf boss = new GentlemanElf();
        boss.resolveIntro(true);
        assertTrue(boss.introResolved());
        boss.resolveIntro(false);
        Bundle bundle = new Bundle(); boss.storeInBundle(bundle);
        GentlemanElf restored = new GentlemanElf(); restored.restoreFromBundle(bundle);
        assertTrue(restored.introResolved());
    }
    @Test public void cupRewardsMatchKillerAndRespectBossPhaseCap() {
        GentlemanElf boss = new GentlemanElf();
        DeathKnight other = new DeathKnight();
        boss.applyCupReward(other);
        assertNotNull(other.buff(Exhilaration.class));
        assertEquals(55, boss.heroCupHealedHpForTest(50, 100));
        assertEquals(5f, boss.heroCupHasteDurationForTest(), 0f);
        boss.HP = 1170;
        boss.applyCupReward(boss);
        assertEquals(1200, boss.HP);
        assertNotNull(boss.buff(Exhilaration.class));
        assertEquals(40, boss.buff(Barrier.class).shielding());
    }
	@Test public void allHealingRespectsTheCurrentPhaseCeiling() {
		GentlemanElf boss = new GentlemanElf();
		boss.setPhaseForTest(GentlemanElf.Phase.CUP_CONTEST, 1);
		boss.HP = 1100;
		boss.heal(500, false);
		assertEquals(1200, boss.HP);
		boss.setPhaseForTest(GentlemanElf.Phase.MIRROR_TEST, 2);
		boss.HP = 550;
		boss.heal(500, false);
		assertEquals(600, boss.HP);
	}
    @Test public void bothLocalesContainEveryEncounterPrompt() throws Exception {
        String prefix = "actors.mobs.tboss.gentlemanelf.";
        String[] keys = {"notice","intro_title","intro_text","drink","refuse","drink_reply","refuse_reply",
                "toast","devour","cup_dash","table_shock","cup_spawn","banquet","phase","defeated"};
        java.nio.file.Path working = Paths.get(System.getProperty("user.dir"));
        java.nio.file.Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
        for (String file : new String[]{"actors.properties","actors_zh.properties"}) {
            Properties messages = new Properties();
            try (Reader reader = Files.newBufferedReader(core.resolve("src/main/assets/messages/actors").resolve(file),
                    StandardCharsets.UTF_8)) { messages.load(reader); }
            for (String key : keys) assertNotNull(file + " missing " + key, messages.getProperty(prefix + key));
        }
    }
}
