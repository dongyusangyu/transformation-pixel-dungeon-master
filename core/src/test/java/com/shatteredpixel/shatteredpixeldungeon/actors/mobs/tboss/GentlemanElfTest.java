package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Exhilaration;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import com.watabou.utils.Bundle;
import org.junit.Test;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	@Test public void specialSkillsRequireACurrentlyPerceptibleEnemy() {
		GentlemanElf boss = new GentlemanElf();
		DeathKnight target = new DeathKnight();
		target.alignment = Char.Alignment.ALLY;
		target.pos = 1;
		boss.fieldOfView = new boolean[]{false, true};

		assertSame(target, boss.toastTargetForTest(target));
		assertSame(target, boss.devourTargetForTest(target, null));

		target.invisible = 1;
		assertNull(boss.toastTargetForTest(target));
		assertNull(boss.devourTargetForTest(target, null));
	}
    @Test public void cupDashOnlyCoversFourThroughEightTilesAndStopsThreeAway() {
        assertFalse(GentlemanElf.cupDashEligibleForTest(3));
        assertTrue(GentlemanElf.cupDashEligibleForTest(4));
        assertTrue(GentlemanElf.cupDashEligibleForTest(8));
        assertFalse(GentlemanElf.cupDashEligibleForTest(9));
        assertEquals(3, GentlemanElf.cupDashLandingDistanceForTest());
    }
    @Test public void phaseTwoWithoutCupUsesDevourBeforeNormalActions() {
        GentlemanElf boss = new GentlemanElf();
        boss.setPhaseForTest(GentlemanElf.Phase.CUP_CONTEST, 1);
        assertEquals(GentlemanElf.Skill.DEVOUR, boss.nextSkillForTest());
    }
    @Test public void cupDevourCooldownArmsImmediatelyAndSurvivesSaving() {
        GentlemanElf boss = new GentlemanElf();
        boss.setPhaseForTest(GentlemanElf.Phase.CUP_CONTEST, 1);
        boss.setCupDevourCooldownForTest(7);
        boss.armCupDevourForTest();
        assertEquals(0, boss.cupDevourCooldownForTest());
        boss.resolveCupDevourForTest();
        assertEquals(10, boss.cupDevourCooldownForTest());

        Bundle bundle = new Bundle();
        boss.storeInBundle(bundle);
        GentlemanElf restored = new GentlemanElf();
        restored.restoreFromBundle(bundle);
        assertEquals(10, restored.cupDevourCooldownForTest());

        for (int i = 0; i < 9; i++) {
            boss.finishBossActionForTest();
        }
        assertEquals(1, boss.cupDevourCooldownForTest());
        boss.finishBossActionForTest();
        assertEquals(0, boss.cupDevourCooldownForTest());
    }
    @Test public void cupObjectiveIgnoresExternalAggroWhileTheCupExists() {
        GentlemanElf boss = new GentlemanElf();
        boss.setPhaseForTest(GentlemanElf.Phase.CUP_CONTEST, 1);
        CupHost host = new CupHost(boss);
        GentlemanElfArena arena = new GentlemanElfArena(host);
        ElfWineCup cup = new ElfWineCup();
        host.entities.put(cup.id(), cup);
        arena.cupId(cup.id());
        boss.bindArena(arena);

        boss.aggro(new DeathKnight());

        assertSame(cup, boss.enemyForTest());
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
		assertEquals(600, illusion.HT); assertEquals(600, illusion.HP);
		assertEquals(25, illusion.defenseSkill);
        assertEquals(15, illusion.damageRollMinForTest());
        assertEquals(25, illusion.damageRollMaxForTest());
		assertEquals(GentlemanElf.class, illusion.identityMessageClassForTest());
		assertSame(illusion.HUNTING, illusion.state);
    }
	@Test public void mirrorIllusionsKeep600HealthButDisappearOnTheSecondPositiveHit() {
		GentlemanElf boss = new GentlemanElf();
		boss.setPhaseForTest(GentlemanElf.Phase.MIRROR_TEST, 2);
		GentlemanElfIllusion illusion = boss.createIllusionsForTest()[0];
		assertEquals(301, illusion.modifyFinalDamage(1, boss, com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag.PHYSICAL));
		assertEquals(301, illusion.modifyFinalDamage(999, boss, com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag.PHYSICAL));
		boss.illusionDied(); boss.illusionDied();
		assertEquals(GentlemanElf.Phase.BERSERK, boss.phase());
		assertEquals(1.6f, boss.speed(), 0.001f); assertEquals(0.5f, boss.attackDelay(), 0.001f);
	}
	@Test public void trueBodyTakesOneDamageWhileAnyMirrorIsAlive() {
		GentlemanElf boss = new GentlemanElf();
		boss.setPhaseForTest(GentlemanElf.Phase.MIRROR_TEST, 2);
		boss.syncActiveIllusions(2);
		assertEquals(1, boss.capFinalDamageForTest(999));
	}
	@Test public void oldOneHitMirrorSaveMigratesToEquivalentRemainingHealth() {
		Bundle oldSave = new Bundle();
		oldSave.put("HP", 1);
		oldSave.put("HT", 1);
		oldSave.put("illusion_positive_hits", 1);
		oldSave.put("illusion_owner", -1);
		GentlemanElfIllusion restored = new GentlemanElfIllusion();
		restored.restoreFromBundle(oldSave);
		assertEquals(600, restored.HT);
		assertEquals(299, restored.HP);
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
    private static class CupHost implements GentlemanElfArena.Host {
        final GentlemanElf boss;
        final Map<Integer, Actor> entities = new HashMap<>();
        CupHost(GentlemanElf boss) { this.boss = boss; }
        public GentlemanElf boss() { return boss; }
        public Iterable<Char> characters() { return new ArrayList<>(); }
        public void warnBanquet(int turnsUntilResolution) { }
        public void resolveBanquet(Iterable<Char> targets) { }
        public boolean respawnCup() { return false; }
        public Actor actorById(int id) { return entities.get(id); }
    }
}
