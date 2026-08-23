package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Drunkenness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Exhilaration;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfIllusion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundle;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class GentlemanElfArenaTest {
	@Test public void clockChargesAtTwentyWithoutResolvingByItself() {
		GentlemanElf boss = new GentlemanElf();
		RecordingHost host = new RecordingHost(boss);
		GentlemanElfArena arena = new GentlemanElfArena(host);
		for (int i=0;i<19;i++) arena.actForTest();
		assertEquals(0, host.warningCalls); assertEquals(19, arena.banquetTurns());
		arena.actForTest();
		assertEquals(0, host.resolveCalls); assertTrue(arena.banquetReady());
		arena.actForTest();
		assertEquals(0, host.resolveCalls); assertEquals(20, arena.banquetTurns());
		arena.banquetResolved();
		assertFalse(arena.banquetReady()); assertEquals(0, arena.banquetTurns());
	}
    @Test public void clockStopsForDeadBossAndBundlePreservesProgress() {
        GentlemanElf boss = new GentlemanElf(); RecordingHost host=new RecordingHost(boss);
        GentlemanElfArena arena=new GentlemanElfArena(host); for(int i=0;i<7;i++) arena.actForTest();
        Bundle b=new Bundle(); arena.storeInBundle(b); GentlemanElfArena restored=new GentlemanElfArena(); restored.restoreFromBundle(b);
        assertEquals(7,restored.banquetTurns()); boss.HP=0; assertTrue(arena.actForTest()); assertFalse(arena.active());
    }
    @Test public void derivedEntitiesDoNotReceiveBanquetTargets() {
        GentlemanElf boss=new GentlemanElf(); RecordingHost host=new RecordingHost(boss);
        host.characters.add(new GentlemanElfIllusion(boss)); host.characters.add(new ElfWineCup());
        GentlemanElfArena arena=new GentlemanElfArena(host); for(int i=0;i<20;i++)arena.actForTest();
        assertEquals(0,host.lastTargets.size());
    }
    @Test public void banquetDoesNotTargetOtherCharactersInTheBossFaction() {
        GentlemanElf boss = new GentlemanElf();
        RecordingHost host = new RecordingHost(boss);
        GentlemanElf sameFaction = new GentlemanElf();
        DeathKnight ally = new DeathKnight();
        ally.alignment = Char.Alignment.ALLY;
        host.characters.add(sameFaction);
        host.characters.add(ally);
        GentlemanElfArena arena = new GentlemanElfArena(host);
        for (int i = 0; i < 21; i++) arena.actForTest();
        arena.resolveBanquetNow();
        assertEquals(1, host.lastTargets.size());
        assertSame(ally, host.lastTargets.get(0));
    }
    @Test public void cupDestructionArmsTheBossDevourImmediately() {
        GentlemanElf boss = new GentlemanElf();
        boss.setPhaseForTest(GentlemanElf.Phase.CUP_CONTEST, 1);
        RecordingHost host = new RecordingHost(boss);
        GentlemanElfArena arena = new GentlemanElfArena(host);
        boss.setCupDevourCooldownForTest(7);
        ElfWineCup cup = new ElfWineCup();
        host.entities.put(cup.id(), cup);
        arena.cupId(cup.id());
        arena.onCupDestroyed(cup, boss);
        assertEquals(0, boss.cupDevourCooldownForTest());
        assertEquals(GentlemanElf.Skill.DEVOUR, boss.nextSkillForTest());
    }
    @Test public void cupRespawnsExactlyTwentyTurnsAfterDestructionIndependentOfBanquet() {
        GentlemanElf boss=new GentlemanElf(); RecordingHost host=new RecordingHost(boss);
        GentlemanElfArena arena=new GentlemanElfArena(host);
        for(int i=0;i<7;i++) arena.actForTest();
        arena.onCupDestroyed(new ElfWineCup(), boss);
        for(int i=0;i<19;i++) arena.actForTest();
        assertEquals(0, host.respawnCalls);
        arena.actForTest();
        assertEquals(1, host.respawnCalls);
        assertEquals(-1, arena.cupRespawnTurns());
    }
    @Test public void cupRespawnCountdownSurvivesBundleRoundTrip() {
        GentlemanElf boss=new GentlemanElf(); RecordingHost host=new RecordingHost(boss);
        GentlemanElfArena arena=new GentlemanElfArena(host);
        arena.onCupDestroyed(new ElfWineCup(), boss);
        for(int i=0;i<6;i++) arena.actForTest();
        Bundle bundle=new Bundle(); arena.storeInBundle(bundle);
        GentlemanElfArena restored=new GentlemanElfArena(); restored.restoreFromBundle(bundle);
        assertEquals(14, restored.cupRespawnTurns());
    }
    @Test public void blockedCupRespawnStaysChargedAndRetriesWithoutDuplication() {
        GentlemanElf boss=new GentlemanElf(); RecordingHost host=new RecordingHost(boss);
        host.respawnSucceeds=false;
        GentlemanElfArena arena=new GentlemanElfArena(host);
        arena.onCupDestroyed(new ElfWineCup(), boss);
        for(int i=0;i<20;i++) arena.actForTest();
        assertEquals(1, host.respawnCalls); assertEquals(0, arena.cupRespawnTurns());
        host.respawnSucceeds=true;
        arena.actForTest();
        assertEquals(2, host.respawnCalls); assertEquals(-1, arena.cupRespawnTurns());
        arena.actForTest();
        assertEquals(2, host.respawnCalls);
    }
	@Test public void blockedInitialCupSpawnStaysChargedForRetry() {
		GentlemanElf boss=new GentlemanElf(); RecordingHost host=new RecordingHost(boss); host.respawnSucceeds=false;
		GentlemanElfArena arena=new GentlemanElfArena(host); arena.spawnCupNow();
		assertEquals(0, arena.cupRespawnTurns());
		host.respawnSucceeds=true; arena.actForTest();
		assertEquals(2, host.respawnCalls); assertEquals(-1, arena.cupRespawnTurns());
	}
	@Test public void duplicateCupDeathNotificationCannotGrantTwoRewardsOrResetTimer() {
		GentlemanElf boss = new GentlemanElf(); RecordingHost host = new RecordingHost(boss);
		GentlemanElfArena arena = new GentlemanElfArena(host);
		ElfWineCup cup = new ElfWineCup();
		arena.onCupDestroyed(cup, boss);
		arena.actForTest();
		arena.onCupDestroyed(cup, boss);
		assertEquals(1, host.cupRewardCalls);
		assertEquals(19, arena.cupRespawnTurns());
	}
	@Test public void restoreRebindCanUseLevelEntitiesBeforeActorInit() {
		GentlemanElf boss = new GentlemanElf(); RecordingHost host = new RecordingHost(boss);
		ElfWineCup cup = new ElfWineCup(); host.entities.put(cup.id(), cup);
		GentlemanElfArena arena = new GentlemanElfArena(host); arena.cupId(cup.id());
		Bundle bundle = new Bundle(); arena.storeInBundle(bundle);
		GentlemanElfArena restored = new GentlemanElfArena(); restored.restoreFromBundle(bundle); restored.bind(host);
		assertEquals(cup.id(), restored.cupId());
	}
	@Test public void missingIllusionsAfterRestoreTriggerOnlyOneRecoveryAttempt() {
		GentlemanElf boss = new GentlemanElf();
		boss.setPhaseForTest(GentlemanElf.Phase.MIRROR_TEST, 2);
		RecordingHost host = new RecordingHost(boss);
		GentlemanElfArena original = new GentlemanElfArena(host);
		Bundle bundle = new Bundle(); original.storeInBundle(bundle);
		GentlemanElfArena restored = new GentlemanElfArena(); restored.restoreFromBundle(bundle); restored.bind(host);
		restored.actForTest();
		assertEquals(1, host.illusionSpawnCalls);
		assertEquals(GentlemanElf.Phase.BERSERK, boss.phase());
		restored.actForTest();
		assertEquals(1, host.illusionSpawnCalls);
	}
	@Test public void cleanupRunsIllusionVisualDeathLifecycle() {
		GentlemanElf boss = new GentlemanElf();
		RecordingHost host = new RecordingHost(boss);
		TrackingIllusion illusion = new TrackingIllusion(boss);
		RecordingSprite sprite = new RecordingSprite();
		illusion.sprite = sprite;
		host.entities.put(illusion.id(), illusion);
		GentlemanElfArena arena = new GentlemanElfArena(host);
		arena.illusionIds(new int[]{illusion.id()});

		arena.cleanup();

		assertTrue(illusion.destroyed);
		assertTrue(sprite.deathStarted);
		assertArrayEquals(new int[0], arena.illusionIds());
		assertFalse(arena.active());
	}
	@Test public void cancellingTheCupAlsoStartsItsVisualDeathLifecycle() {
		GentlemanElf boss = new GentlemanElf();
		RecordingHost host = new RecordingHost(boss);
		TrackingCup cup = new TrackingCup();
		RecordingSprite sprite = new RecordingSprite();
		cup.sprite = sprite;
		host.entities.put(cup.id(), cup);
		GentlemanElfArena arena = new GentlemanElfArena(host);
		arena.cupId(cup.id());

		arena.cancelCup();

		assertTrue(sprite.deathStarted);
		assertEquals(-1, arena.cupId());
		assertFalse(cup.isAlive());
	}
	@Test public void cleanupRemovesWineStatesFromEveryCharacter() {
		GentlemanElf boss = new GentlemanElf();
		GentlemanElfIllusion illusion = new GentlemanElfIllusion(boss);
		DeathKnight enemy = new DeathKnight();
		RecordingHost host = new RecordingHost(boss);
		host.characters.add(boss);
		host.characters.add(illusion);
		host.characters.add(enemy);
		Drunkenness.affect(boss);
		Exhilaration.affect(illusion);
		Drunkenness.affect(enemy);
		GentlemanElfArena arena = new GentlemanElfArena(host);

		arena.cleanup();

		assertNull(boss.buff(Drunkenness.class));
		assertNull(illusion.buff(Exhilaration.class));
		assertNull(enemy.buff(Drunkenness.class));
	}
	private static class TrackingIllusion extends GentlemanElfIllusion {
		boolean destroyed;
		TrackingIllusion(GentlemanElf owner) { super(owner); }
		@Override public void destroy() { destroyed = true; HP = 0; }
	}
	private static class TrackingCup extends ElfWineCup { }
	private static class RecordingSprite extends CharSprite {
		boolean deathStarted;
		@Override public void die() { deathStarted = true; }
	}
    private static class RecordingHost implements GentlemanElfArena.Host {
		final GentlemanElf boss; final List<Char> characters=new ArrayList<>(); final Map<Integer, Actor> entities=new HashMap<>(); int warningCalls,resolveCalls,respawnCalls,cupRewardCalls,illusionSpawnCalls; boolean respawnSucceeds=true; List<Char> lastTargets=new ArrayList<>();
        RecordingHost(GentlemanElf boss){this.boss=boss;}
        public GentlemanElf boss(){return boss;} public Iterable<Char> characters(){return characters;}
		public void warnBanquet(int turns){warningCalls++;} public void resolveBanquet(Iterable<Char> t){resolveCalls++; for(Char c:t)lastTargets.add(c);} public boolean respawnCup(){respawnCalls++; return respawnSucceeds;}
		public void onCupDestroyed(Char lastHit){cupRewardCalls++; boss.onArenaCupDestroyed(lastHit);}
		public Actor actorById(int id){return entities.get(id);}
		public void spawnIllusions(){illusionSpawnCalls++; boss.syncActiveIllusions(0);}
    }
}
