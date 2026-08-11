package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.BlindingDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HuntressBossLevelTest {
	private long originalDungeonSeed;
	private int originalDungeonDepth;
	private int originalDungeonBranch;
	private Level originalDungeonLevel;
	private Hero originalDungeonHero;
	private boolean originalQualifiedForBossChallengeBadge;
	private PathFinderState originalPathFinderState;
	private final ArrayList<Actor> registeredActors = new ArrayList<>();

	@Before
	public void rememberDungeonPosition() {
		originalDungeonSeed = Dungeon.seed;
		originalDungeonDepth = Dungeon.depth;
		originalDungeonBranch = Dungeon.branch;
		originalDungeonLevel = Dungeon.level;
		originalDungeonHero = Dungeon.hero;
		originalQualifiedForBossChallengeBadge =
				Statistics.qualifiedForBossChallengeBadge;
		originalPathFinderState = PathFinderState.capture();
	}

	@After
	public void restoreDungeonPosition() {
		for (int i = registeredActors.size() - 1; i >= 0; i--) {
			Actor.remove(registeredActors.get(i));
		}
		Dungeon.seed = originalDungeonSeed;
		Dungeon.depth = originalDungeonDepth;
		Dungeon.branch = originalDungeonBranch;
		Dungeon.level = originalDungeonLevel;
		Dungeon.hero = originalDungeonHero;
		Statistics.qualifiedForBossChallengeBadge =
				originalQualifiedForBossChallengeBadge;
		if (originalPathFinderState != null) {
			originalPathFinderState.restore();
		}
	}

	@Test
	public void pathFinderSnapshotRestoresNonBossMapDimensionsAndSemantics() {
		HuntressBossLevel previousLevel = newHeadlessLevel();
		previousLevel.setSize(7, 5);
		Dungeon.level = previousLevel;
		int[] previousDistance = PathFinder.distance;
		previousDistance[3] = 73;
		PathFinderState previousPathFinder = PathFinderState.capture();
		try {
			createSpawnTestLevel(123456L);
			assertEquals(31 * 32, PathFinder.distance.length);
		} finally {
			Dungeon.level = previousLevel;
			previousPathFinder.restore();
		}

		assertEquals(35, PathFinder.distance.length);
		assertTrue(previousDistance == PathFinder.distance);
		assertEquals(73, PathFinder.distance[3]);
		assertArrayEquals(new int[]{-7, -1, 1, 7}, PathFinder.NEIGHBOURS4);
		boolean[] passable = new boolean[35];
		Arrays.fill(passable, true);
		PathFinder.buildDistanceMap(8, passable);
		assertEquals(1, PathFinder.distance[9]);
		assertEquals(1, PathFinder.distance[15]);
	}

	@Test
	public void occupyCellRunsRealOpeningLifecycleAfterClosingGate() {
		RecordingHuntressBossLevel level = createLifecycleTestLevel(123456L);
		Hero hero = createTestHero(level.triggerCell());
		Dungeon.hero = hero;

		level.occupyCell(hero);

		assertEquals(HuntressBossLevel.State.FIGHT, level.state());
		assertTrue(level.locked);
		assertEquals(Terrain.WALL, level.map[cell(level, 15, 23)]);
		assertEquals(Terrain.WALL, level.gateTerrainWhenUpdated);
		assertTrue(level.openingBoss != null);
		assertTrue(HuntressBossLevel.isPreferredBossSpawnDistance(
				level.distance(level.openingBoss.pos, hero.pos)));
		assertEquals(hero.pos, new Ballistica(level.openingBoss.pos,
				hero.pos, Ballistica.PROJECTILE).collisionPos.intValue());

		int bosses = 0;
		int hawks = 0;
		for (Mob mob : level.scheduledMobs) {
			if (mob instanceof HuntressBoss) {
				bosses++;
			} else if (mob instanceof HuntressBoss.DistractingHawk) {
				hawks++;
			}
		}
		assertEquals(1, bosses);
		assertEquals(1, hawks);
		assertEquals(Arrays.asList("gate.closed", "boss.startEncounter",
				"boss.schedule.1.0", "hawk.schedule.1.0", "music"), level.events);
	}

	@Test
	public void restoredIntroAndFightStatesDoNotRerollOrRepeatOpeningActors() {
		for (HuntressBossLevel.State restoredState : new HuntressBossLevel.State[]{
				HuntressBossLevel.State.INTRO, HuntressBossLevel.State.FIGHT}) {
			RecordingHuntressBossLevel source = createLifecycleTestLevel(123456L);
			HuntressBoss existingBoss = new HuntressBoss();
			existingBoss.pos = cell(source, 15, 5);
			HuntressBoss.DistractingHawk existingHawk =
					new HuntressBoss.DistractingHawk();
			existingHawk.pos = cell(source, 5, 5);
			source.mobs.add(existingBoss);
			source.mobs.add(existingHawk);
			Bundle bundle = new Bundle();
			source.storeInBundle(bundle);
			bundle.put("version", ShatteredPixelDungeon.v2_4_2);
			bundle.put("huntress_boss_state", restoredState);

			RecordingHuntressBossLevel restored = new RecordingHuntressBossLevel();
			restored.restoreFromBundle(bundle);
			Dungeon.level = restored;
			Hero hero = createTestHero(restored.triggerCell());
			Dungeon.hero = hero;

			restored.occupyCell(hero);

			assertEquals(restoredState, restored.state());
			assertTrue(restored.events.isEmpty());
			assertTrue(restored.scheduledMobs.isEmpty());
			assertTrue(restored.openingBoss == null);
			int bosses = 0;
			int hawks = 0;
			for (Mob mob : restored.mobs) {
				if (mob instanceof HuntressBoss) {
					bosses++;
					assertEquals(existingBoss.pos, mob.pos);
				} else if (mob instanceof HuntressBoss.DistractingHawk) {
					hawks++;
					assertEquals(existingHawk.pos, mob.pos);
				}
			}
			assertEquals(1, bosses);
			assertEquals(1, hawks);
		}
	}

	@Test
	public void preferredOpeningDistanceIncludesSixAndNineButNotFiveOrTen() {
		assertFalse(HuntressBossLevel.isPreferredBossSpawnDistance(5));
		assertTrue(HuntressBossLevel.isPreferredBossSpawnDistance(6));
		assertTrue(HuntressBossLevel.isPreferredBossSpawnDistance(9));
		assertFalse(HuntressBossLevel.isPreferredBossSpawnDistance(10));

		assertEquals(1, HuntressBossLevel.preferredRangeDeviation(5));
		assertEquals(0, HuntressBossLevel.preferredRangeDeviation(6));
		assertEquals(0, HuntressBossLevel.preferredRangeDeviation(9));
		assertEquals(1, HuntressBossLevel.preferredRangeDeviation(10));

		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		ArrayList<Integer> strict = level.strictBossSpawnCandidates(heroPos);
		int atFive = firstBasicBallisticAtDistance(level, heroPos, 5);
		int atSix = firstBasicBallisticAtDistance(level, heroPos, 6);
		int atNine = firstBasicBallisticAtDistance(level, heroPos, 9);
		int atTen = firstBasicBallisticAtDistance(level, heroPos, 10);
		assertTrue(atFive != -1 && atSix != -1 && atNine != -1 && atTen != -1);
		assertFalse(strict.contains(atFive));
		assertTrue(strict.contains(atSix));
		assertTrue(strict.contains(atNine));
		assertFalse(strict.contains(atTen));
	}

	@Test
	public void strictOpeningCandidatesUseRealDistanceAndProjectileBallistics() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		ArrayList<Integer> candidates = level.strictBossSpawnCandidates(heroPos);
		assertFalse(candidates.isEmpty());

		int candidate = candidates.get(0);
		int distance = level.distance(candidate, heroPos);
		assertTrue(distance >= 6 && distance <= 9);
		Ballistica clear = new Ballistica(candidate, heroPos, Ballistica.PROJECTILE);
		assertEquals(heroPos, clear.collisionPos.intValue());
		assertTrue(clear.path.size() > 2);

		int blocker = clear.path.get(1);
		int previousTerrain = level.map[blocker];
		try {
			Level.set(blocker, Terrain.WALL, level);
			assertFalse(level.hasBossSpawnProjectileLine(candidate, heroPos));
			assertFalse(level.strictBossSpawnCandidates(heroPos).contains(candidate));
		} finally {
			Level.set(blocker, previousTerrain, level);
		}
	}

	@Test
	public void basicOpeningCellRejectsOccupantsFeaturesAndReservedCells() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		ArrayList<Integer> candidates = level.strictBossSpawnCandidates(level.triggerCell());
		assertFalse(candidates.isEmpty());
		int candidate = candidates.get(0);

		Mob occupant = registerActor(new Mob() {
		});
		occupant.pos = candidate;
		assertFalse(level.isBasicBossSpawnCell(candidate));
		Actor.remove(occupant);
		registeredActors.remove(occupant);

		Firebloom plant = new Firebloom();
		plant.pos = candidate;
		level.plants.put(candidate, plant);
		try {
			assertFalse(level.isBasicBossSpawnCell(candidate));
		} finally {
			level.plants.remove(candidate);
		}

		Heap heap = new Heap();
		heap.pos = candidate;
		level.heaps.put(candidate, heap);
		try {
			assertFalse(level.isBasicBossSpawnCell(candidate));
		} finally {
			level.heaps.remove(candidate);
		}

		int previousTerrain = level.map[candidate];
		try {
			Level.set(candidate, Terrain.WALL, level);
			assertFalse(level.isBasicBossSpawnCell(candidate));
		} finally {
			Level.set(candidate, previousTerrain, level);
		}

		assertFalse(level.isBasicBossSpawnCell(level.entrance()));
		assertFalse(level.isBasicBossSpawnCell(level.exit()));
		assertFalse(level.isBasicBossSpawnCell(level.triggerCell()));
		assertFalse(level.isBasicBossSpawnCell(cell(level, 15, 23)));
		for (int cell = 0; cell < level.length(); cell++) {
			if (level.distance(cell, level.triggerCell()) <= 2) {
				assertFalse(level.isBasicBossSpawnCell(cell));
			}
		}
		for (Set<Integer> cluster : level.coverClusters()) {
			for (int cover : cluster) {
				assertFalse(level.isBasicBossSpawnCell(cover));
			}
		}
	}

	@Test
	public void finalCoverLayoutsAlwaysLeaveStrictOpeningCandidates() {
		for (long levelSeed = 1; levelSeed <= 20; levelSeed++) {
			HuntressBossLevel level = createSpawnTestLevel(levelSeed);
			ArrayList<Integer> candidates =
					level.strictBossSpawnCandidates(level.triggerCell());
			assertFalse("seed " + levelSeed, candidates.isEmpty());
			for (int candidate : candidates) {
				assertTrue(level.isBasicBossSpawnCell(candidate));
				assertTrue(HuntressBossLevel.isPreferredBossSpawnDistance(
						level.distance(candidate, level.triggerCell())));
				assertEquals(level.triggerCell(), new Ballistica(candidate,
						level.triggerCell(), Ballistica.PROJECTILE).collisionPos.intValue());
			}
		}
	}

	@Test
	public void strictOpeningSelectionIsSeededReproducibleAndVaried() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		Set<Integer> strict = new HashSet<>(level.strictBossSpawnCandidates(heroPos));
		assertTrue(strict.size() > 1);
		Set<Integer> selected = new HashSet<>();
		for (long selectionSeed = 1; selectionSeed <= 20; selectionSeed++) {
			int first = selectWithSeed(level, heroPos, selectionSeed);
			int repeated = selectWithSeed(level, heroPos, selectionSeed);
			assertEquals(first, repeated);
			assertTrue(strict.contains(first));
			selected.add(first);
		}
		assertTrue(selected.size() > 1);
	}

	@Test
	public void emptyStrictSetFallsBackToOriginalFixedBallisticCellFirst() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		int fixed = cell(level, 15, 5);
		fillArena(level, Terrain.WATER);
		Level.set(fixed, Terrain.EMPTY, level);

		assertTrue(level.isBasicBossSpawnCell(fixed));
		assertTrue(level.hasBossSpawnProjectileLine(fixed, heroPos));
		assertTrue(level.strictBossSpawnCandidates(heroPos).isEmpty());
		assertEquals(fixed, selectWithSeed(level, heroPos, 7L));
	}

	@Test
	public void invalidFixedCellFallsBackToClosestBallisticRangeDeviation() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		fillArena(level, Terrain.WATER);
		int closer = cell(level, 10, 17);
		int farther = cell(level, 4, 11);
		Level.set(closer, Terrain.EMPTY, level);
		Level.set(farther, Terrain.EMPTY, level);

		assertEquals(5, level.distance(closer, heroPos));
		assertEquals(11, level.distance(farther, heroPos));
		assertTrue(level.hasBossSpawnProjectileLine(closer, heroPos));
		assertTrue(level.hasBossSpawnProjectileLine(farther, heroPos));
		assertFalse(level.isBasicBossSpawnCell(cell(level, 15, 5)));
		assertTrue(level.strictBossSpawnCandidates(heroPos).isEmpty());
		assertEquals(closer, selectWithSeed(level, heroPos, 11L));
	}

	@Test
	public void closestBallisticFallbackBreaksEqualDeviationTiesWithGameRandom() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		fillArena(level, Terrain.WATER);
		int left = cell(level, 10, 17);
		int right = cell(level, 20, 17);
		Level.set(left, Terrain.EMPTY, level);
		Level.set(right, Terrain.EMPTY, level);
		Set<Integer> eligible = setOf(left, right);
		Set<Integer> selected = new HashSet<>();

		assertEquals(5, level.distance(left, heroPos));
		assertEquals(5, level.distance(right, heroPos));
		assertTrue(level.hasBossSpawnProjectileLine(left, heroPos));
		assertTrue(level.hasBossSpawnProjectileLine(right, heroPos));
		for (long seed = 1; seed <= 20; seed++) {
			int first = selectWithSeed(level, heroPos, seed);
			assertEquals(first, selectWithSeed(level, heroPos, seed));
			assertTrue(eligible.contains(first));
			selected.add(first);
		}
		assertEquals(2, selected.size());
	}

	@Test
	public void noBallisticCellFallsBackToRandomBasicSafeCell() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		fillArena(level, Terrain.WALL);
		int safe = cell(level, 5, 5);
		Level.set(safe, Terrain.EMPTY, level);

		assertTrue(level.isBasicBossSpawnCell(safe));
		assertFalse(level.hasBossSpawnProjectileLine(safe, heroPos));
		assertEquals(safe, selectWithSeed(level, heroPos, 13L));
	}

	@Test
	public void noBasicSafeCellFailsExplicitly() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		try {
			level.selectBossSpawnCell(level.triggerCell());
			fail("missing safe opening spawn must fail explicitly");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("Huntress"));
		}
	}

	@Test
	public void phaseTwoDistributionUsesDesignedPercentages() {
		assertArrayEquals(new int[]{50, 1, 5},
				HuntressBossLevel.phaseTwoCounts(100));
		assertArrayEquals(new int[]{17, 1, 2},
				HuntressBossLevel.phaseTwoCounts(33));
	}

	@Test
	public void vegetationOnlyUsesLegalEmptyTerrain() {
		assertTrue(HuntressBossLevel.isLegalVegetationTerrain(Terrain.EMPTY));
		assertTrue(HuntressBossLevel.isLegalVegetationTerrain(Terrain.EMPTY_DECO));
		assertTrue(HuntressBossLevel.isLegalVegetationTerrain(Terrain.FURROWED_GRASS));
		assertFalse(HuntressBossLevel.isLegalVegetationTerrain(Terrain.ENTRANCE));
		assertFalse(HuntressBossLevel.isLegalVegetationTerrain(Terrain.EXIT));
		assertFalse(HuntressBossLevel.isLegalVegetationTerrain(Terrain.WALL));
	}

	@Test
	public void buildCreatesEntranceBufferArenaAndTenPercentInitialFurrows() {
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;

		HuntressBossLevel level = newHeadlessLevel();
		level.create();
		assertTrue(level.entrance() != level.exit());
		assertFalse(level.isArenaCell(level.entrance()));
		assertTrue(level.isArenaCell(level.triggerCell()));
		assertFalse(level.triggersFightAt(level.entrance()));
		assertTrue(level.triggersFightAt(level.triggerCell()));
		assertTrue(level.vegetationCells().size() > 0);
		PathFinder.buildDistanceMap(level.triggerCell(), level.passable);
		assertTrue(PathFinder.distance[level.exit()] < Integer.MAX_VALUE);

		int furrows = 0;
		for (int cell : level.vegetationCells()) {
			if (level.map[cell] == Terrain.FURROWED_GRASS) {
				furrows++;
			}
		}
		assertTrue(Math.abs(furrows - Math.round(level.vegetationCells().size() * 0.10f)) <= 1);
		assertTrue(level.state() == HuntressBossLevel.State.START);
	}

	@Test
	public void createPlacesTwoSeparateSingleBlindingDartsInSafeEntranceBufferCells() {
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;

		HuntressBossLevel level = newHeadlessLevel();
		level.create();

		assertEquals(BlindingDart.class, HuntressBossLevel.entranceSupplyItemClass());
		assertEquals(2, level.heaps.valueList().size());
		Set<Integer> heapCells = new HashSet<>();
		Set<Item> supplies = new HashSet<>();
		for (Heap heap : level.heaps.valueList()) {
			assertTrue(heapCells.add(heap.pos));
			assertEquals(Heap.Type.HEAP, heap.type);
			assertTrue(level.passable[heap.pos]);
			assertFalse(level.solid[heap.pos]);
			assertFalse(level.triggersFightAt(heap.pos));
			assertTrue(heap.pos != level.entrance());

			int x = heap.pos % level.width();
			int y = heap.pos / level.width();
			assertTrue(x >= 13 && x <= 17);
			assertTrue(y >= 24 && y <= 29);
			assertTrue(x != 15);

			assertEquals(1, heap.items.size());
			assertTrue(supplies.add(heap.peek()));
			assertEquals(1, heap.peek().quantity());
		}
		assertEquals(2, supplies.size());
	}

	@Test
	public void buildCreatesEightFourCellCoverClustersAndKeepsCriticalPathsOpen() {
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;

		HuntressBossLevel level = newHeadlessLevel();
		level.create();

		assertEquals(HuntressBossLevel.COVER_CLUSTER_COUNT, level.coverClusters().size());
		Set<Integer> allCover = new HashSet<>();
		for (Set<Integer> cluster : level.coverClusters()) {
			assertEquals(HuntressBossLevel.COVER_CELLS_PER_CLUSTER, cluster.size());
			assertTrue(HuntressBossLevel.isOrthogonallyConnected(cluster));
			for (int cell : cluster) {
				assertTrue(level.isArenaCell(cell));
				assertTrue(allCover.add(cell));
				assertEquals(Terrain.WALL, level.map[cell]);
				assertFalse(level.isReservedEncounterCell(cell));
			}
		}
		assertEquals(HuntressBossLevel.COVER_CLUSTER_COUNT
				* HuntressBossLevel.COVER_CELLS_PER_CLUSTER, allCover.size());
		assertTrue(level.hasCriticalArenaPaths(allCover));
		assertTrue(level.isReservedEncounterCell(level.entrance()));
		assertTrue(level.isReservedEncounterCell(level.exit()));
		assertTrue(level.isReservedEncounterCell(level.triggerCell()));
		assertFalse(level.hasCriticalArenaPaths(
				setOf(level.triggerCell() + level.width())));
	}

	@Test
	public void coverLayoutIsSeededIndependentlyAndReproducibly() {
		Set<Integer> first = HuntressBossLevel.coverCellsForSeed(123456L);
		Set<Integer> repeated = HuntressBossLevel.coverCellsForSeed(123456L);
		Set<Integer> different = HuntressBossLevel.coverCellsForSeed(654321L);
		assertEquals(first, repeated);
		assertFalse(first.equals(different));

		assertArrayEquals(randomPairAroundCoverGeneration(false),
				randomPairAroundCoverGeneration(true));
		assertEquals(builtCoverCells(123456L), builtCoverCells(123456L));
		assertFalse(builtCoverCells(123456L).equals(builtCoverCells(654321L)));
	}

	@Test
	public void coverClustersReturnsDefensiveCopies() {
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;

		HuntressBossLevel level = newHeadlessLevel();
		level.create();
		List<Set<Integer>> firstRead = level.coverClusters();
		Set<Integer> expectedFirstCluster = new HashSet<>(firstRead.get(0));

		firstRead.get(0).clear();
		firstRead.clear();

		List<Set<Integer>> secondRead = level.coverClusters();
		assertEquals(HuntressBossLevel.COVER_CLUSTER_COUNT, secondRead.size());
		assertEquals(expectedFirstCluster, secondRead.get(0));
	}

	@Test
	public void builtCoverCellsRestoresDungeonSeedDepthAndBranch() {
		Dungeon.seed = 777L;
		Dungeon.depth = 3;
		Dungeon.branch = 2;

		builtCoverCells(123456L);

		assertEquals(777L, Dungeon.seed);
		assertEquals(3, Dungeon.depth);
		assertEquals(2, Dungeon.branch);
	}

	@Test
	public void wardenAllocationKeepsInitialFurrowsBlockedCellsAndFeaturesDisjoint() {
		Set<Integer> initialFurrows = setOf(1, 2);
		Set<Integer> blocked = setOf(3);

		HuntressBossLevel.WardenArenaAllocation allocation =
				HuntressBossLevel.allocateWardenArena(
						Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
						initialFurrows,
						blocked,
						new int[]{4, 2, 2});

		assertEquals(4, allocation.furrowCells.size());
		assertEquals(2, allocation.tentacleCells.size());
		assertEquals(2, allocation.plantCells.size());
		assertTrue(allocation.furrowCells.containsAll(initialFurrows));
		assertTrue(disjoint(allocation.tentacleCells, initialFurrows));
		assertTrue(disjoint(allocation.plantCells, initialFurrows));
		assertFalse(allocation.furrowCells.contains(3));
		assertFalse(allocation.tentacleCells.contains(3));
		assertFalse(allocation.plantCells.contains(3));
		assertPairwiseDisjoint(allocation);
	}

	@Test
	public void wardenAllocationNeverReusesCellsWhenCandidatesAreInsufficient() {
		HuntressBossLevel.WardenArenaAllocation allocation =
				HuntressBossLevel.allocateWardenArena(
						Arrays.asList(1, 2, 3),
						setOf(1),
						setOf(2),
						new int[]{3, 2, 2});

		assertTrue(allocation.furrowCells.size() <= 3);
		assertTrue(allocation.tentacleCells.size() <= 2);
		assertTrue(allocation.plantCells.size() <= 2);
		assertPairwiseDisjoint(allocation);

		Set<Integer> allAssigned = new HashSet<>(allocation.furrowCells);
		allAssigned.addAll(allocation.tentacleCells);
		allAssigned.addAll(allocation.plantCells);
		assertEquals(2, allAssigned.size());
		assertTrue(allAssigned.contains(1));
		assertTrue(allAssigned.contains(3));
		assertFalse(allAssigned.contains(2));
	}

	@Test
	public void wardenFeatureTerrainOnlyMarksFurrowAssignmentsAsFurrowed() {
		assertEquals(Terrain.FURROWED_GRASS,
				HuntressBossLevel.terrainForWardenFeature(
						Terrain.EMPTY, HuntressBossLevel.WardenFeature.FURROW));
		assertEquals(Terrain.EMPTY,
				HuntressBossLevel.terrainForWardenFeature(
						Terrain.EMPTY, HuntressBossLevel.WardenFeature.PLANT));
		assertEquals(Terrain.EMPTY_DECO,
				HuntressBossLevel.terrainForWardenFeature(
						Terrain.EMPTY_DECO, HuntressBossLevel.WardenFeature.TENTACLE));
	}

	@Test
	public void encounterCountingGuardRestoresFalseAndSuppressesCounts() {
		boolean originalSkipCounting = Bestiary.skipCountingEncounters;
		try {
			Bestiary.skipCountingEncounters = false;
			int encountersBefore =
					Bestiary.encounterCount(HuntressBoss.DistractingHawk.class);
			boolean[] guardedDuringCleanup = {false};

			HuntressBossLevel.runWithoutEncounterCounting(() -> {
				guardedDuringCleanup[0] = Bestiary.skipCountingEncounters;
				Bestiary.countEncounter(HuntressBoss.DistractingHawk.class);
			});

			assertTrue(guardedDuringCleanup[0]);
			assertFalse(Bestiary.skipCountingEncounters);
			assertEquals(encountersBefore,
					Bestiary.encounterCount(HuntressBoss.DistractingHawk.class));
		} finally {
			Bestiary.skipCountingEncounters = originalSkipCounting;
		}
	}

	@Test
	public void encounterCountingGuardRestoresTrue() {
		boolean originalSkipCounting = Bestiary.skipCountingEncounters;
		try {
			Bestiary.skipCountingEncounters = true;

			HuntressBossLevel.runWithoutEncounterCounting(
					() -> assertTrue(Bestiary.skipCountingEncounters));

			assertTrue(Bestiary.skipCountingEncounters);
		} finally {
			Bestiary.skipCountingEncounters = originalSkipCounting;
		}
	}

	@Test
	public void encounterCountingGuardRestoresStateAfterFailure() {
		boolean originalSkipCounting = Bestiary.skipCountingEncounters;
		try {
			Bestiary.skipCountingEncounters = false;
			try {
				HuntressBossLevel.runWithoutEncounterCounting(() -> {
					throw new IllegalStateException("cleanup failed");
				});
				fail("cleanup exception should propagate");
			} catch (IllegalStateException expected) {
				assertEquals("cleanup failed", expected.getMessage());
			}
			assertFalse(Bestiary.skipCountingEncounters);
		} finally {
			Bestiary.skipCountingEncounters = originalSkipCounting;
		}
	}

	private static void assertPairwiseDisjoint(
			HuntressBossLevel.WardenArenaAllocation allocation) {
		assertTrue(disjoint(allocation.furrowCells, allocation.tentacleCells));
		assertTrue(disjoint(allocation.furrowCells, allocation.plantCells));
		assertTrue(disjoint(allocation.tentacleCells, allocation.plantCells));
	}

	private static boolean disjoint(Set<Integer> first, Set<Integer> second) {
		Set<Integer> overlap = new HashSet<>(first);
		overlap.retainAll(second);
		return overlap.isEmpty();
	}

	private static Set<Integer> setOf(Integer... cells) {
		return new HashSet<>(Arrays.asList(cells));
	}

	private <T extends Actor> T registerActor(T actor) {
		Actor.add(actor);
		registeredActors.add(actor);
		return actor;
	}

	private static HuntressBossLevel createSpawnTestLevel(long seed) {
		Dungeon.seed = seed;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		Dungeon.level = null;
		HuntressBossLevel level = createHeadlessLevel();
		Dungeon.level = level;
		return level;
	}

	private static RecordingHuntressBossLevel createLifecycleTestLevel(long seed) {
		Dungeon.seed = seed;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		Dungeon.level = null;
		RecordingHuntressBossLevel level = new RecordingHuntressBossLevel();
		level.create();
		Dungeon.level = level;
		return level;
	}

	private Hero createTestHero(int pos) {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			Unsafe unsafe = (Unsafe) unsafeField.get(null);
			Hero hero = (Hero) unsafe.allocateInstance(Hero.class);
			hero.pos = pos;
			return hero;
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static int selectWithSeed(HuntressBossLevel level,
			int heroPos, long seed) {
		Random.pushGenerator(seed);
		try {
			return level.selectBossSpawnCell(heroPos);
		} finally {
			Random.popGenerator();
		}
	}

	private static void fillArena(HuntressBossLevel level, int terrain) {
		for (int cell = 0; cell < level.length(); cell++) {
			if (level.isArenaCell(cell)) {
				Level.set(cell, terrain, level);
			}
		}
	}

	private static int cell(HuntressBossLevel level, int x, int y) {
		return x + y * level.width();
	}

	private static int firstBasicBallisticAtDistance(HuntressBossLevel level,
			int heroPos, int distance) {
		for (int cell = 0; cell < level.length(); cell++) {
			if (level.distance(cell, heroPos) == distance
					&& level.isBasicBossSpawnCell(cell)
					&& level.hasBossSpawnProjectileLine(cell, heroPos)) {
				return cell;
			}
		}
		return -1;
	}

	private static int[] randomPairAroundCoverGeneration(boolean generateCovers) {
		Random.pushGenerator(998877L);
		try {
			int before = Random.Int();
			if (generateCovers) {
				HuntressBossLevel.coverCellsForSeed(123456L);
			}
			return new int[]{before, Random.Int()};
		} finally {
			Random.popGenerator();
		}
	}

	private static Set<Integer> builtCoverCells(long seed) {
		long previousSeed = Dungeon.seed;
		int previousDepth = Dungeon.depth;
		int previousBranch = Dungeon.branch;
		try {
			Dungeon.seed = seed;
			Dungeon.depth = 15;
			Dungeon.branch = 0;
			Set<Integer> result = new HashSet<>();
			for (Set<Integer> cluster : createHeadlessLevel().coverClusters()) {
				result.addAll(cluster);
			}
			return result;
		} finally {
			Dungeon.seed = previousSeed;
			Dungeon.depth = previousDepth;
			Dungeon.branch = previousBranch;
		}
	}

	private static HuntressBossLevel createHeadlessLevel() {
		HuntressBossLevel level = newHeadlessLevel();
		level.create();
		return level;
	}

	private static HuntressBossLevel newHeadlessLevel() {
		return new HuntressBossLevel() {
			@Override
			Item createEntranceDart() {
				// Dart construction initializes GPU-backed sprite metadata, unavailable here.
				return new Item();
			}
		};
	}

	private static final class PathFinderState {
		private final ArrayList<Field> fields;
		private final ArrayList<Object> values;

		private PathFinderState(ArrayList<Field> fields, ArrayList<Object> values) {
			this.fields = fields;
			this.values = values;
		}

		static PathFinderState capture() {
			ArrayList<Field> fields = new ArrayList<>();
			ArrayList<Object> values = new ArrayList<>();
			try {
				for (Field field : PathFinder.class.getDeclaredFields()) {
					int modifiers = field.getModifiers();
					if (!Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
						continue;
					}
					field.setAccessible(true);
					fields.add(field);
					values.add(field.get(null));
				}
				return new PathFinderState(fields, values);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		void restore() {
			try {
				for (int i = 0; i < fields.size(); i++) {
					fields.get(i).set(null, values.get(i));
				}
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}
	}

	private static final class RecordingHuntressBossLevel extends HuntressBossLevel {
		final ArrayList<String> events = new ArrayList<>();
		final ArrayList<Mob> scheduledMobs = new ArrayList<>();
		HuntressBoss openingBoss;
		int gateTerrainWhenUpdated = -1;

		@Override
		Item createEntranceDart() {
			return new Item();
		}

		@Override
		HuntressBoss createOpeningBoss() {
			openingBoss = new HuntressBoss() {
				@Override
				public void startEncounter() {
					events.add("boss.startEncounter");
				}
			};
			return openingBoss;
		}

		@Override
		void afterOpeningGateClosed(int gate) {
			gateTerrainWhenUpdated = map[gate];
			events.add("gate.closed");
		}

		@Override
		void scheduleEncounterMob(Mob mob, float delay) {
			scheduledMobs.add(mob);
			mobs.add(mob);
			events.add((mob instanceof HuntressBoss ? "boss" : "hawk")
					+ ".schedule." + delay);
		}

		@Override
		void startFightMusic() {
			events.add("music");
		}

		@Override
		void sealEncounter() {
			locked = true;
		}

		@Override
		void completeOccupyCell(Char ch) {
			// The real trigger and startFight already ran; base terrain effects need a full Hero.
		}
	}
}
