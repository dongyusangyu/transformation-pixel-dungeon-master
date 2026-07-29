package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.BlindingDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.watabou.utils.PathFinder;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HuntressBossLevelTest {

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

	private static HuntressBossLevel newHeadlessLevel() {
		return new HuntressBossLevel() {
			@Override
			Item createEntranceDart() {
				// Dart construction initializes GPU-backed sprite metadata, unavailable here.
				return new Item();
			}
		};
	}
}
