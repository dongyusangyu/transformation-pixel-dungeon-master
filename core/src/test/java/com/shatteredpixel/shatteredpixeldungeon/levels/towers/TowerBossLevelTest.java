package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TowerBossLevelTest {

	private int originalDepth;
	private int originalBranch;
	private long originalSeed;

	@Before
	public void setUp() {
		originalDepth = Dungeon.depth;
		originalBranch = Dungeon.branch;
		originalSeed = Dungeon.seed;
		Dungeon.depth = 5;
		Dungeon.branch = TowerLevel.BRANCH;
		Dungeon.seed = 0x5B055L;
	}

	@After
	public void tearDown() {
		Dungeon.depth = originalDepth;
		Dungeon.branch = originalBranch;
		Dungeon.seed = originalSeed;
	}

	@Test
	public void bossLevelUsesTheScheduledFiveFloorEnvironment() {
		String tiles = Dungeon.towerTilesTexForLocation(5, TowerLevel.BRANCH);
		String water = Dungeon.towerWaterTexForLocation(5, TowerLevel.BRANCH);

		assertNotNull(tiles);
		assertNotNull(water);
		assertEquals(Dungeon.towerTilesTexForLocation(1, TowerLevel.BRANCH), tiles);
		assertEquals(Dungeon.towerWaterTexForLocation(1, TowerLevel.BRANCH), water);
	}

	@Test
	public void fixedSkeletonHasSpecifiedDimensionsRoomsDoorsAndStairs() {
		int[] map = generateMap();

		assertEquals(29, TowerBossLayout.WIDTH);
		assertEquals(35, TowerBossLayout.HEIGHT);
		assertEquals(Terrain.ENTRANCE, map[cell(14, 32)]);
		assertEquals(Terrain.EXIT, map[cell(14, 2)]);
		assertEquals(Terrain.DOOR, map[cell(14, 30)]);
		assertEquals(Terrain.LOCKED_EXIT, map[cell(14, 4)]);

		for (int y = 1; y <= 3; y++) {
			for (int x = 13; x <= 15; x++) {
				if (x != 14 || y != 2) {
					assertEquals(Terrain.EMPTY, map[cell(x, y)]);
				}
			}
		}
		for (int y = 31; y <= 33; y++) {
			for (int x = 13; x <= 15; x++) {
				if (x != 14 || y != 32) {
					assertEquals(Terrain.EMPTY, map[cell(x, y)]);
				}
			}
		}
	}

	@Test
	public void randomTerrainStaysInArenaAndKeepsAConnectedMainRoute() {
		int[] first = generateMap(1L);
		int[] second = generateMap(2L);
		assertFalse(Arrays.equals(first, second));

		for (long seed = 1; seed <= 32; seed++) {
			assertValidNaturalTerrain(generateMap(seed));
		}
	}

	private static void assertValidNaturalTerrain(int[] map) {
		int water = 0;
		int vegetation = 0;
		int obstacles = 0;

		for (int cell = 0; cell < map.length; cell++) {
			int terrain = map[cell];
			if (terrain == Terrain.WATER) {
				water++;
				assertTrue(TowerBossLayout.isArenaCell(cell));
			} else if (terrain == Terrain.HIGH_GRASS
					|| terrain == Terrain.FURROWED_GRASS) {
				vegetation++;
				assertTrue(TowerBossLayout.isArenaCell(cell));
			} else if (terrain == Terrain.BARRICADE || terrain == Terrain.STATUE
					|| terrain == Terrain.REGION_DECO) {
				obstacles++;
				assertTrue(TowerBossLayout.isArenaCell(cell));
			}
		}

		assertTrue(water >= 40);
		assertTrue(vegetation >= 30);
		assertTrue(obstacles >= 20);
		assertTrue(reachable(map, cell(14, 29), cell(14, 5)));
		assertTrue(reachable(map, cell(14, 29), cell(14, 17)));
	}

	@Test
	public void encounterTriggerAndGateStateFollowBossLifecycle() {
		int[] map = generateMap();
		int arenaCell = cell(14, 29);

		assertFalse(TowerBossLayout.shouldStartEncounter(false, true, cell(14, 30)));
		assertFalse(TowerBossLayout.shouldStartEncounter(false, false, arenaCell));
		assertFalse(TowerBossLayout.shouldStartEncounter(true, true, arenaCell));
		assertTrue(TowerBossLayout.shouldStartEncounter(false, true, arenaCell));

		TowerBossLayout.sealArena(map);
		assertEquals(Terrain.WALL, map[TowerBossLayout.SAFE_GATE]);
		assertEquals(Terrain.LOCKED_EXIT, map[TowerBossLayout.EXIT_GATE]);

		TowerBossLayout.unlockArena(map);
		assertEquals(Terrain.DOOR, map[TowerBossLayout.SAFE_GATE]);
		assertEquals(Terrain.UNLOCKED_EXIT, map[TowerBossLayout.EXIT_GATE]);
	}

	@Test
	public void selectedBossIdSurvivesBundleRoundTrip() {
		TowerBossEncounter encounter = new TowerBossEncounter();
		encounter.ensureSelected(Dungeon.seed, Dungeon.depth, Dungeon.branch);
		assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID, encounter.selectedBossId());

		Bundle bundle = new Bundle();
		encounter.storeInBundle(bundle);
		TowerBossEncounter restored = new TowerBossEncounter();
		restored.restoreFromBundle(bundle, Dungeon.seed, Dungeon.depth, Dungeon.branch,
				false, false, false);

		assertEquals(encounter.selectedBossId(), restored.selectedBossId());
		assertFalse(restored.bossEncounterStarted());
		assertFalse(restored.bossEncounterDefeated());
	}

	@Test
	public void missingLegacyBossIdIsRecomputedFromSeedAndDepth() {
		TowerBossEncounter encounter = new TowerBossEncounter();
		encounter.ensureSelected(Dungeon.seed, Dungeon.depth, Dungeon.branch);
		Bundle current = new Bundle();
		encounter.storeInBundle(current);

		current.remove("tower_boss_id");
		TowerBossEncounter restored = new TowerBossEncounter();
		restored.restoreFromBundle(current, Dungeon.seed, Dungeon.depth, Dungeon.branch,
				false, false, false);

		assertEquals(TowerBossGenerator.selectId(Dungeon.seed, Dungeon.depth, Dungeon.branch),
				restored.selectedBossId());
	}

	@Test
	public void encounterCreatesSelectedBossAndUsesGenericLifecycle() {
		TowerBossEncounter encounter = new TowerBossEncounter();
		encounter.ensureSelected(Dungeon.seed, Dungeon.depth, Dungeon.branch);
		RecordingHost host = new RecordingHost();

		TowerBoss boss = encounter.start(host);

		assertTrue(encounter.bossEncounterStarted());
		assertEquals(1, host.sealCalls);
		assertNotNull(host.launchedBoss);
		assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID,
				host.launchedBoss.towerBossId());
		assertEquals(host.fixedSpawnCell, host.launchedBoss.pos);
		assertTrue(boss == host.launchedBoss);
		assertEquals(1, host.prepareCalls);
	}

	@Test
	public void genericBossDeathCleanupAndUnsealAreIdempotent() {
		TowerBossEncounter encounter = new TowerBossEncounter();
		encounter.ensureSelected(Dungeon.seed, Dungeon.depth, Dungeon.branch);
		RecordingHost host = new RecordingHost();
		TowerBoss boss = encounter.start(host);

		encounter.onBossDefeated(boss, host);
		encounter.onBossDefeated(boss, host);

		assertTrue(encounter.bossEncounterDefeated());
		assertEquals(1, host.unsealCalls);
		assertEquals(1, host.cleanupCalls);
	}

	private static int[] generateMap() {
		return generateMap(0x5B055L);
	}

	private static int[] generateMap(long seed) {
		Random.pushGenerator(seed);
		try {
			return TowerBossLayout.generateMap();
		} finally {
			Random.popGenerator();
		}
	}

	private static int cell(int x, int y) {
		return TowerBossLayout.cell(x, y);
	}

	private static boolean reachable(int[] map, int start, int target) {
		boolean[] seen = new boolean[map.length];
		ArrayDeque<Integer> queue = new ArrayDeque<>();
		seen[start] = true;
		queue.add(start);
		int[] offsets = {-TowerBossLayout.WIDTH, 1, TowerBossLayout.WIDTH, -1};
		while (!queue.isEmpty()) {
			int current = queue.removeFirst();
			if (current == target) {
				return true;
			}
			int currentX = current % TowerBossLayout.WIDTH;
			for (int offset : offsets) {
				int next = current + offset;
				if (next < 0 || next >= map.length || seen[next]) {
					continue;
				}
				int nextX = next % TowerBossLayout.WIDTH;
				if (Math.abs(nextX - currentX) > 1
						|| (Terrain.flags[map[next]] & Terrain.PASSABLE) == 0) {
					continue;
				}
				seen[next] = true;
				queue.addLast(next);
			}
		}
		return false;
	}

	private static class RecordingHost implements TowerBossEncounter.Host {
		private final int fixedSpawnCell = cell(14, 10);
		private TestTowerBoss launchedBoss;
		private int prepareCalls;
		private int cleanupCalls;
		private int sealCalls;
		private int unsealCalls;

		@Override
		public TowerBoss createBoss(String id) {
			assertEquals(TowerBossGenerator.PESTILENCE_KNIGHT_ID, id);
			return new TestTowerBoss();
		}

		@Override
		public int selectBossSpawnCell() {
			return fixedSpawnCell;
		}

		@Override
		public boolean prepareArena(TowerBoss boss, int spawnCell) {
			prepareCalls++;
			return true;
		}

		@Override
		public void launchBoss(TowerBoss boss) {
			launchedBoss = (TestTowerBoss) boss;
		}

		@Override
		public void sealArena() {
			sealCalls++;
		}

		@Override
		public void cleanupArena(TowerBoss boss) {
			cleanupCalls++;
		}

		@Override
		public void unsealArena() {
			unsealCalls++;
		}
	}

	private static class TestTowerBoss extends TowerBoss {
		@Override
		public String towerBossId() {
			return TowerBossGenerator.PESTILENCE_KNIGHT_ID;
		}
	}
}
