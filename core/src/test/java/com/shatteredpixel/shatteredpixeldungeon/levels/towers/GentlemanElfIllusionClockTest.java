package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
import com.watabou.utils.Bundle;
import org.junit.Test;
import java.util.Collections;
import static org.junit.Assert.*;

public class GentlemanElfIllusionClockTest {
	@Test public void twentyDrunkTicksSpawnOnceAndFinalPhaseStopsTheClock() {
		GentlemanElf boss = new GentlemanElf();
		Host host = new Host(boss);
		GentlemanElfArena arena = new GentlemanElfArena(host);
		host.drunk = true;
		for (int i = 0; i < 19; i++) arena.actForTest();
		assertEquals(0, host.spawns);
		arena.actForTest();
		assertEquals(1, host.spawns);
		boss.setPhaseForTest(GentlemanElf.Phase.FINAL_DUEL, 2);
		for (int i = 0; i < 40; i++) arena.actForTest();
		assertEquals(1, host.spawns);
	}

	@Test public void interruptedDrunkClockResumesAfterSave() {
		GentlemanElf boss = new GentlemanElf();
		Host host = new Host(boss);
		GentlemanElfArena arena = new GentlemanElfArena(host);
		host.drunk = true;
		for (int i = 0; i < 12; i++) arena.actForTest();
		Bundle bundle = new Bundle();
		arena.storeInBundle(bundle);
		GentlemanElfArena restored = new GentlemanElfArena();
		restored.restoreFromBundle(bundle);
		restored.bind(host);
		for (int i = 0; i < 7; i++) restored.actForTest();
		assertEquals(0, host.spawns);
		restored.actForTest();
		assertEquals(1, host.spawns);
	}

	private static class Host implements GentlemanElfArena.Host {
		final GentlemanElf boss;
		boolean drunk;
		int spawns;
		Host(GentlemanElf boss) { this.boss = boss; }
		public GentlemanElf boss() { return boss; }
		public Iterable<Char> characters() { return Collections.emptyList(); }
		public void warnBanquet(int turns) { }
		public boolean respawnCup() { return false; }
		public boolean heroIsEncounterDrunk() { return drunk; }
		public boolean spawnIllusion() { spawns++; return true; }
	}
}
