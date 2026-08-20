package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.BlindingDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Mageroyal;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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
	public void fallingIntoUnstartedHuntressBossLevelUsesEntranceBuffer() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		level.locked = true;
		assertTrue(level.shouldResetForSafeArrival());
		level.locked = false;

		for (int i = 0; i < 8; i++) {
			int fallCell = level.fallCell(false);
			assertTrue(fallCell != -1);
			assertFalse(level.isArenaCell(fallCell));
			assertFalse(level.triggersFightAt(fallCell));
		}
	}

	@Test
	public void unstartedHuntressBossAnkhRespawnUsesEntranceBuffer() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		level.locked = false;

		int respawnCell = level.unblessedAnkhSafeRespawnCell(null);

		assertTrue(respawnCell != -1);
		assertFalse(level.isArenaCell(respawnCell));
		assertFalse(level.triggersFightAt(respawnCell));
	}

	@Test
	public void sealedResurrectionResetRemovesFreshEntranceDarts() {
		HuntressBossLevel level = new HuntressBossLevel();
		level.setSize(31, 32);
		level.heaps = new com.watabou.utils.SparseArray<>();
		for (int cell : new int[]{15 - 2 + (29 - 2) * level.width(),
				15 + 2 + (29 - 2) * level.width()}) {
			Heap heap = new Heap();
			heap.pos = cell;
			heap.items.add(allocateWithoutConstructor(BlindingDart.class));
			level.heaps.put(cell, heap);
		}
		Dungeon.level = level;

		assertEquals(2, entranceDartHeapCount(level));

		level.onSealedResurrectionReset();

		assertEquals(0, entranceDartHeapCount(level));
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
	public void openingFightRelocatesOnlySafeZoneAlliesIntoTheArena() {
		RecordingHuntressBossLevel level = createLifecycleTestLevel(123456L);
		Hero hero = createTestHero(level.triggerCell());
		Dungeon.hero = hero;
		Wraith safeAlly = addMob(level, cell(level, 15, 29), Char.Alignment.ALLY);
		Wraith arenaAlly = addMob(level, cell(level, 5, 5), Char.Alignment.ALLY);
		Wraith safeEnemy = addMob(level, cell(level, 14, 29), Char.Alignment.ENEMY);
		level.allyDestination = level.vegetationCells().get(0);

		level.occupyCell(hero);

		assertEquals(1, level.teleportedAllies.size());
		assertTrue(level.teleportedAllies.get(0) == safeAlly);
		assertEquals(level.allyDestination, safeAlly.pos);
		assertTrue(level.isArenaCell(safeAlly.pos));
		assertTrue(level.passable[safeAlly.pos]);
		assertEquals(cell(level, 5, 5), arenaAlly.pos);
		assertEquals(cell(level, 14, 29), safeEnemy.pos);
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
	public void wholeLevelRestoreKeepsAReachableSelectedPlantForTheBoss() {
		HuntressBossLevel source = newHeadlessLevel();
		initializeLevelCollections(source);
		source.setSize(11, 7);
		Arrays.fill(source.map, Terrain.EMPTY);
		source.buildFlagMaps();
		Dungeon.level = source;
		int bossCell = cell(source, 5, 4);
		int plantCell = cell(source, 5, 1);
		putFirebloom(source, plantCell);
		HuntressBoss boss = new HuntressBoss();
		Bundle selectedBoss = new Bundle();
		selectedBoss.put("pos", bossCell);
		selectedBoss.put("state", "HUNTING");
		selectedBoss.put("phase", HuntressBoss.Phase.WARDEN);
		selectedBoss.put("combat_step", HuntressBoss.CombatStep.MOVE);
		selectedBoss.put("plant_hunt_state", HuntressBoss.PlantHuntState.SELECTED);
		selectedBoss.put("marked_plant_cell", plantCell);
		selectedBoss.put("plant_hunt_turns", HuntressBoss.PLANT_HUNT_DURATION);
		selectedBoss.put("plant_grace_turns", 0);
		boss.restoreFromBundle(selectedBoss);
		source.mobs.add(boss);

		Bundle bundle = new Bundle();
		source.storeInBundle(bundle);
		bundle.put("version", ShatteredPixelDungeon.v2_4_2);
		Dungeon.level = null;

		HuntressBossLevel restored = new NoTerrainOccupyLevel();
		Dungeon.level = null;
		restored.restoreFromBundle(bundle);
		Dungeon.level = restored;
		Dungeon.level = restored;

		HuntressBoss restoredBoss = null;
		for (Mob mob : restored.mobs) {
			if (mob instanceof HuntressBoss) {
				restoredBoss = (HuntressBoss) mob;
				break;
			}
		}
		assertNotNull(restoredBoss);
		Bundle restoredBossState = new Bundle();
		restoredBoss.storeInBundle(restoredBossState);
		assertEquals(HuntressBoss.PlantHuntState.SELECTED,
				restoredBossState.getEnum("plant_hunt_state",
						HuntressBoss.PlantHuntState.class));
		assertEquals(plantCell, restoredBossState.getInt("marked_plant_cell"));
		assertEquals(3f, restoredBoss.speed(), 0f);
		registerActor(restoredBoss);
		RecordingMovementSprite movementSprite = new RecordingMovementSprite();
		restoredBoss.sprite = movementSprite;
		restoredBoss.fieldOfView = new boolean[restored.length()];
		PathFinder.buildDistanceMap(plantCell, restored.passable);
		int distanceBefore = PathFinder.distance[restoredBoss.pos];
		assertTrue(invokeBossAct(restoredBoss));
		PathFinder.buildDistanceMap(plantCell, restored.passable);
		assertTrue(PathFinder.distance[restoredBoss.pos] < distanceBefore);
		assertEquals(1, movementSprite.moves + movementSprite.places);
		assertEquals(restoredBoss.pos, movementSprite.finalCell);
		if (movementSprite.moves == 1) {
			assertEquals(bossCell, movementSprite.from);
		}
	}

	private static boolean invokeBossAct(HuntressBoss boss) {
		try {
			java.lang.reflect.Method act = HuntressBoss.class.getDeclaredMethod("act");
			act.setAccessible(true);
			return (Boolean) act.invoke(boss);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static void initializeLevelCollections(HuntressBossLevel level) {
		level.mobs = new HashSet<>();
		level.heaps = new com.watabou.utils.SparseArray<>();
		level.blobs = new java.util.HashMap<>();
		level.plants = new com.watabou.utils.SparseArray<>();
		level.traps = new com.watabou.utils.SparseArray<>();
		level.transitions = new ArrayList<>();
		level.customTiles = new ArrayList<>();
		level.customWalls = new ArrayList<>();
	}

	private static final class NoTerrainOccupyLevel extends HuntressBossLevel {
		@Override
		void completeOccupyCell(Char ch) {
			// Bundle/movement integration only; terrain triggers need a full Hero scene.
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
	public void emptyStrictSetFailsInsteadOfUsingAnOutOfRangeFixedCell() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		int fixed = cell(level, 15, 5);
		fillArena(level, Terrain.WATER);
		Level.set(fixed, Terrain.EMPTY, level);

		assertTrue(level.isBasicBossSpawnCell(fixed));
		assertTrue(level.hasBossSpawnProjectileLine(fixed, heroPos));
		assertTrue(level.strictBossSpawnCandidates(heroPos).isEmpty());
		assertStrictOpeningSelectionFails(level, heroPos);
	}

	@Test
	public void outOfRangeBallisticCellsCannotBeOpeningFallbacks() {
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
		assertStrictOpeningSelectionFails(level, heroPos);
	}

	@Test
	public void equalOutOfRangeBallisticCellsCannotBeOpeningFallbacks() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		fillArena(level, Terrain.WATER);
		int left = cell(level, 10, 17);
		int right = cell(level, 20, 17);
		Level.set(left, Terrain.EMPTY, level);
		Level.set(right, Terrain.EMPTY, level);

		assertEquals(5, level.distance(left, heroPos));
		assertEquals(5, level.distance(right, heroPos));
		assertTrue(level.hasBossSpawnProjectileLine(left, heroPos));
		assertTrue(level.hasBossSpawnProjectileLine(right, heroPos));
		assertStrictOpeningSelectionFails(level, heroPos);
	}

	@Test
	public void nonBallisticSafeCellCannotBeOpeningFallback() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		int heroPos = level.triggerCell();
		fillArena(level, Terrain.WALL);
		int safe = cell(level, 5, 5);
		Level.set(safe, Terrain.EMPTY, level);

		assertTrue(level.isBasicBossSpawnCell(safe));
		assertFalse(level.hasBossSpawnProjectileLine(safe, heroPos));
		assertStrictOpeningSelectionFails(level, heroPos);
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

	private static void assertStrictOpeningSelectionFails(
			HuntressBossLevel level, int heroPos) {
		try {
			level.selectBossSpawnCell(heroPos);
			fail("missing strict opening spawn must fail explicitly");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("Huntress"));
		}
	}

	@Test
	public void fadeleafPairPrefersLegalDestinationsAtLeastSixCellsApart() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 14, 12);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 15, 12);

		for (int selection = 0; selection < 20; selection++) {
			HuntressBossLevel.FadeleafDestinations result =
					level.selectFadeleafDestinations(boss, target, selection);
			assertNotNull(result);
			assertTrue(level.isLegalFadeleafCell(boss, result.bossCell));
			assertTrue(level.isLegalFadeleafCell(target, result.targetCell));
			assertTrue(level.distance(result.bossCell, result.targetCell) >= 6);
			assertTrue(level.distance(result.bossCell, result.targetCell)
					> level.distance(boss.pos, target.pos));
		}
	}

	@Test
	public void fadeleafPairFallsBackToTheMaximumIncreasingDistance() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 13, 12);
		int left = cell(level, 10, 10);
		int right = cell(level, 15, 10);
		Level.set(boss.pos, Terrain.EMPTY, level);
		Level.set(target.pos, Terrain.EMPTY, level);
		Level.set(left, Terrain.EMPTY, level);
		Level.set(right, Terrain.EMPTY, level);

		HuntressBossLevel.FadeleafDestinations result =
				level.selectFadeleafDestinations(boss, target, 0);

		assertNotNull(result);
		assertEquals(5, level.distance(result.bossCell, result.targetCell));
	}

	@Test
	public void fadeleafPairMayKeepOneCharacterOnItsOwnCellWhenOnlyOneRemoteCellExists() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 13, 12);
		int remote = cell(level, 20, 12);
		Level.set(boss.pos, Terrain.EMPTY, level);
		Level.set(target.pos, Terrain.EMPTY, level);
		Level.set(remote, Terrain.EMPTY, level);

		HuntressBossLevel.FadeleafDestinations result =
				level.selectFadeleafDestinations(boss, target, 0);

		assertNotNull(result);
		assertTrue(result.bossCell == boss.pos && result.targetCell == remote
				|| result.bossCell == remote && result.targetCell == target.pos);
		assertFalse(result.bossCell == target.pos);
		assertFalse(result.targetCell == boss.pos);
		assertTrue(level.distance(result.bossCell, result.targetCell)
				> level.distance(boss.pos, target.pos));
	}

	@Test
	public void fadeleafPairRejectsFeaturesOccupantsAndNonImprovingLayouts() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 13, 12);
		int plantCell = cell(level, 10, 10);
		int heapCell = cell(level, 15, 10);
		int occupiedCell = cell(level, 10, 15);
		for (int candidate : new int[]{boss.pos, target.pos, plantCell, heapCell, occupiedCell}) {
			Level.set(candidate, Terrain.EMPTY, level);
		}
		Firebloom plant = new Firebloom();
		plant.pos = plantCell;
		level.plants.put(plantCell, plant);
		Heap heap = new Heap();
		heap.pos = heapCell;
		level.heaps.put(heapCell, heap);
		Mob occupant = registerActor(new Mob() {
		});
		occupant.pos = occupiedCell;

		assertFalse(level.isLegalFadeleafCell(boss, plantCell));
		assertFalse(level.isLegalFadeleafCell(boss, heapCell));
		assertFalse(level.isLegalFadeleafCell(boss, occupiedCell));
		assertNull(level.selectFadeleafDestinations(boss, target, 0));
	}

	@Test
	public void fadeleafTeleportMovesBossAndActualAllyThroughTheProductionPair() {
		RecordingFadeleafLevel level = new RecordingFadeleafLevel();
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		Dungeon.level = null;
		level.create();
		Dungeon.level = level;
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 14, 12);
		Mob ally = registerActor(new Mob() {
		});
		ally.alignment = Char.Alignment.ALLY;
		ally.pos = cell(level, 15, 12);

		Random.pushGenerator(112233L);
		try {
			assertTrue(level.teleportBossAndTargetApart(boss, ally));
		} finally {
			Random.popGenerator();
		}

		assertEquals(2, level.teleported.size());
		assertSame(boss, level.teleported.get(0));
		assertSame(ally, level.teleported.get(1));
		assertTrue(level.distance(boss.pos, ally.pos) >= 6);
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void fadeleafTeleportRollsBackIfTheSecondCharacterCannotMove() {
		RecordingFadeleafLevel level = new RecordingFadeleafLevel();
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		Dungeon.level = null;
		level.create();
		Dungeon.level = level;
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 14, 12);
		Mob ally = registerActor(new Mob() {
		});
		ally.alignment = Char.Alignment.ALLY;
		ally.pos = cell(level, 15, 12);
		int oldBossPos = boss.pos;
		int oldAllyPos = ally.pos;
		level.failedTeleportIndex = 2;

		Random.pushGenerator(445566L);
		try {
			assertFalse(level.teleportBossAndTargetApart(boss, ally));
		} finally {
			Random.popGenerator();
		}

		assertEquals(oldBossPos, boss.pos);
		assertEquals(oldAllyPos, ally.pos);
		assertEquals(2, level.teleported.size());
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void fadeleafWrongPositionIgnoresABrokenRollbackHookAndRestoresBothActors() {
		RecordingFadeleafLevel level = createRecordingFadeleafLevel();
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 14, 12);
		Mob ally = registerActor(new Mob() {
		});
		ally.alignment = Char.Alignment.ALLY;
		ally.pos = cell(level, 15, 12);
		int oldBossPos = boss.pos;
		int oldAllyPos = ally.pos;
		level.wrongPositionTeleportIndex = 2;
		level.rollbackWritesWrongPosition = true;

		Random.pushGenerator(778899L);
		try {
			assertFalse(level.teleportBossAndTargetApart(boss, ally));
		} finally {
			Random.popGenerator();
		}

		assertEquals(oldBossPos, boss.pos);
		assertEquals(oldAllyPos, ally.pos);
		assertEquals(2, level.teleported.size());
		assertTrue(level.restored.isEmpty());
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void fadeleafForwardThrowIgnoresAThrowingRollbackHookAndRestoresBothActors() {
		RecordingFadeleafLevel level = createRecordingFadeleafLevel();
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 14, 12);
		Mob ally = registerActor(new Mob() {
		});
		ally.alignment = Char.Alignment.ALLY;
		ally.pos = cell(level, 15, 12);
		int oldBossPos = boss.pos;
		int oldAllyPos = ally.pos;
		level.thrownTeleportIndex = 2;
		level.thrownRollbackIndex = 1;

		Random.pushGenerator(991122L);
		try {
			assertFalse(level.teleportBossAndTargetApart(boss, ally));
		} finally {
			Random.popGenerator();
		}

		assertEquals(oldBossPos, boss.pos);
		assertEquals(oldAllyPos, ally.pos);
		assertTrue(level.restored.isEmpty());
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void fadeleafFinishThrowDoesNotMaskATransactionFailure() {
		RecordingFadeleafLevel level = createRecordingFadeleafLevel();
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 14, 12);
		Mob ally = registerActor(new Mob() {
		});
		ally.alignment = Char.Alignment.ALLY;
		ally.pos = cell(level, 15, 12);
		int oldBossPos = boss.pos;
		int oldAllyPos = ally.pos;
		level.failedTeleportIndex = 2;
		level.finishThrows = true;

		Random.pushGenerator(334455L);
		try {
			assertFalse(level.teleportBossAndTargetApart(boss, ally));
		} finally {
			Random.popGenerator();
		}

		assertEquals(oldBossPos, boss.pos);
		assertEquals(oldAllyPos, ally.pos);
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void bossVaultPrefersThreeToFiveCellsEvenWhenAFartherCellExists() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 13, 12);
		int preferred = cell(level, 17, 12);
		int farther = cell(level, 27, 12);
		for (int open : new int[]{boss.pos, target.pos, preferred, farther}) {
			Level.set(open, Terrain.EMPTY, level);
		}

		assertEquals(preferred, level.selectHuntressEscapeCell(boss, target));
	}

	@Test
	public void bossVaultFallsBackDirectlyToTheFarthestLegalCell() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 13, 12);
		int far = cell(level, 22, 12);
		int farthest = cell(level, 27, 12);
		for (int open : new int[]{boss.pos, target.pos, far, farthest}) {
			Level.set(open, Terrain.EMPTY, level);
		}

		assertEquals(farthest, level.selectHuntressEscapeCell(boss, target));
		assertTrue(level.distance(farthest, target.pos) > 5);
	}

	@Test
	public void bossVaultRejectsReservedFeaturesOccupantsAndClosedLargeCells() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		HuntressBoss boss = registerActor(new HuntressBoss() {
			{
				properties.add(Char.Property.LARGE);
			}
		});
		boss.pos = cell(level, 12, 12);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 13, 12);
		int plantCell = cell(level, 16, 12);
		int heapCell = cell(level, 17, 12);
		int occupiedCell = cell(level, 18, 12);
		int closedLargeCell = cell(level, 19, 12);
		int legalCell = cell(level, 16, 13);
		for (int open : new int[]{boss.pos, target.pos, plantCell, heapCell,
				occupiedCell, closedLargeCell, legalCell}) {
			Level.set(open, Terrain.EMPTY, level);
			level.openSpace[open] = true;
		}
		level.openSpace[closedLargeCell] = false;
		Firebloom plant = new Firebloom();
		plant.pos = plantCell;
		level.plants.put(plantCell, plant);
		Heap heap = new Heap();
		heap.pos = heapCell;
		level.heaps.put(heapCell, heap);
		Mob occupant = registerActor(new Mob() {
		});
		occupant.pos = occupiedCell;

		assertFalse(level.isLegalHuntressEscapeCell(boss, target, level.triggerCell()));
		assertFalse(level.isLegalHuntressEscapeCell(boss, target, plantCell));
		assertFalse(level.isLegalHuntressEscapeCell(boss, target, heapCell));
		assertFalse(level.isLegalHuntressEscapeCell(boss, target, occupiedCell));
		assertFalse(level.isLegalHuntressEscapeCell(boss, target, closedLargeCell));
		assertTrue(level.isLegalHuntressEscapeCell(boss, target, legalCell));
	}

	@Test
	public void bossVaultMovementRequiresTheBossToReachTheSelectedCell() {
		RecordingFadeleafLevel level = new RecordingFadeleafLevel();
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		Dungeon.level = null;
		level.create();
		Dungeon.level = level;
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		int destination = cell(level, 17, 12);
		Level.set(destination, Terrain.EMPTY, level);

		assertTrue(level.moveHuntressToEscapeCell(boss, destination));
		assertEquals(destination, boss.pos);
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void bossVaultWrongPositionRestoresOriginWithoutUsingRollbackHook() {
		RecordingFadeleafLevel level = createRecordingFadeleafLevel();
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		int origin = boss.pos;
		int destination = cell(level, 17, 12);
		Level.set(destination, Terrain.EMPTY, level);
		level.wrongPositionTeleportIndex = 1;
		level.rollbackWritesWrongPosition = true;

		assertFalse(level.moveHuntressToEscapeCell(boss, destination));

		assertEquals(origin, boss.pos);
		assertTrue(level.restored.isEmpty());
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void bossVaultForwardThrowRestoresOriginDespiteAThrowingRollbackHook() {
		RecordingFadeleafLevel level = createRecordingFadeleafLevel();
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 12, 12);
		int origin = boss.pos;
		int destination = cell(level, 17, 12);
		Level.set(destination, Terrain.EMPTY, level);
		level.thrownTeleportIndex = 1;
		level.thrownRollbackIndex = 1;

		assertFalse(level.moveHuntressToEscapeCell(boss, destination));

		assertEquals(origin, boss.pos);
		assertTrue(level.restored.isEmpty());
		assertEquals(1, level.finishedTeleports);
	}

	@Test
	public void bossVaultSelectionRejectsTheFixedBossReservedCell() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		fillArena(level, Terrain.WALL);
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 15, 7);
		Mob target = registerActor(new Mob() {
		});
		target.pos = cell(level, 15, 8);
		int fixedBossCell = cell(level, 15, 5);
		for (int open : new int[]{boss.pos, target.pos, fixedBossCell}) {
			Level.set(open, Terrain.EMPTY, level);
		}

		assertTrue(level.isReservedEncounterCell(fixedBossCell));
		assertFalse(level.isLegalHuntressEscapeCell(boss, target, fixedBossCell));
		assertEquals(-1, level.selectHuntressEscapeCell(boss, target));
	}

	@Test
	public void bossVaultMovementRejectsTheFixedBossReservedCellAgain() {
		RecordingFadeleafLevel level = new RecordingFadeleafLevel();
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		Dungeon.level = null;
		level.create();
		Dungeon.level = level;
		HuntressBoss boss = registerActor(new HuntressBoss());
		boss.pos = cell(level, 15, 7);
		int origin = boss.pos;
		int fixedBossCell = cell(level, 15, 5);
		Level.set(fixedBossCell, Terrain.EMPTY, level);

		assertTrue(level.isReservedEncounterCell(fixedBossCell));
		assertFalse(level.moveHuntressToEscapeCell(boss, fixedBossCell));
		assertEquals(origin, boss.pos);
		assertTrue(level.teleported.isEmpty());
	}

	@Test
	public void markedPlantSelectionPrefersFourToEightPathStepsAndRejectsOccupants() {
		HuntressBossLevel level = createSpawnTestLevel(24680L);
		HuntressBoss boss = new HuntressBoss();
		boss.pos = cell(level, 15, 12);
		PathFinder.buildDistanceMap(boss.pos, level.passable);
		int fallback = firstPlantCellAtPathDistance(level, 3);
		int preferred = firstPlantCellAtPathDistance(level, 5);
		putFirebloom(level, fallback);
		putFirebloom(level, preferred);

		assertEquals(preferred, level.selectMarkedPlant(boss));
		assertTrue(level.hasReachablePlant(boss));
		assertTrue(level.isMarkedPlantReachable(boss, preferred));

		HuntressBoss occupant = registerActor(new HuntressBoss());
		occupant.pos = preferred;
		assertEquals(fallback, level.selectMarkedPlant(boss));
		assertFalse(level.isMarkedPlantReachable(boss, preferred));

		level.uproot(fallback);
		assertFalse(level.hasReachablePlant(boss));
	}

	@Test
	public void externalUprootNotifiesEveryLiveHuntressExactlyOnce() {
		RecordingPlantLevel level = createRecordingPlantLevel();
		Dungeon.level = level;
		RecordingPlantBoss first = registerActor(new RecordingPlantBoss());
		RecordingPlantBoss second = registerActor(new RecordingPlantBoss());
		first.pos = cell(level, 14, 12);
		second.pos = cell(level, 16, 12);
		int plantCell = cell(level, 15, 12);
		putFirebloom(level, plantCell);

		level.uproot(plantCell);

		assertFalse(level.hasPlantAt(plantCell));
		assertEquals(1, first.removedCalls);
		assertEquals(plantCell, first.removedCell);
		assertEquals(1, second.removedCalls);
		assertEquals(plantCell, second.removedCell);
	}

	@Test
	public void bossOccupyClaimsPlantWithoutBroadcastingExternalRemoval() {
		RecordingPlantLevel level = createRecordingPlantLevel();
		Dungeon.level = level;
		RecordingPlantBoss claimant = registerActor(new RecordingPlantBoss());
		claimant.enterWardenForTest();
		RecordingPlantBoss observer = registerActor(new RecordingPlantBoss());
		int plantCell = cell(level, 15, 12);
		claimant.pos = plantCell;
		observer.pos = cell(level, 16, 12);
		putFirebloom(level, plantCell);

		level.occupyCell(claimant);

		assertFalse(level.hasPlantAt(plantCell));
		assertEquals(Terrain.FURROWED_GRASS, level.map[plantCell]);
		assertEquals(1, claimant.claimedCalls);
		assertEquals(plantCell, claimant.claimedCell);
		assertEquals(HuntressBoss.WardenBoon.FIREBLOOM, claimant.claimedBoon);
		assertEquals(0, claimant.removedCalls);
		assertEquals(0, observer.removedCalls);
	}

	@Test
	public void sniperOrUnknownPlantUsesOrdinaryPlantOccupySemantics() {
		RecordingPlantLevel level = createRecordingPlantLevel();
		Dungeon.level = level;
		RecordingPlantBoss sniper = registerActor(new RecordingPlantBoss());
		int firebloomCell = cell(level, 15, 12);
		sniper.pos = firebloomCell;
		putFirebloom(level, firebloomCell);

		level.occupyCell(sniper);

		assertEquals(0, sniper.claimedCalls);
		assertTrue(level.hasPlantAt(firebloomCell));
	}

	@Test
	public void occupyingANonMarkedPlantEndsSelectionAndGrantsOnlyThatPlantOnce() {
		RecordingPlantLevel level = createRecordingPlantLevel();
		Dungeon.level = level;
		int markedCell = cell(level, 13, 12);
		int claimedCell = cell(level, 17, 12);
		putFirebloom(level, markedCell);
		putFirebloom(level, claimedCell);
		InspectablePlantBoss boss = registerActor(new InspectablePlantBoss());
		boss.restoreSelectedAt(claimedCell, markedCell);

		level.occupyCell(boss);
		level.occupyCell(boss);

		assertTrue(level.hasPlantAt(markedCell));
		assertFalse(level.hasPlantAt(claimedCell));
		assertEquals(HuntressBoss.PlantHuntState.BOON_ACTIVE,
				boss.savedPlantState());
		assertEquals(HuntressBoss.WardenBoon.FIREBLOOM, boss.savedActiveBoon());
		assertEquals(-1, boss.savedMarkedPlantCell());
		assertEquals(1, boss.boonAnnouncements);
	}

	@Test
	public void wardenFeatureRollUsesMutuallyExclusiveDesignedThresholds() {
		assertEquals(HuntressBossLevel.WardenFeature.TENTACLE,
				HuntressBossLevel.wardenFeatureForRoll(0f));
		assertEquals(HuntressBossLevel.WardenFeature.TENTACLE,
				HuntressBossLevel.wardenFeatureForRoll(0.019999f));
		assertEquals(HuntressBossLevel.WardenFeature.PLANT,
				HuntressBossLevel.wardenFeatureForRoll(0.02f));
		assertEquals(HuntressBossLevel.WardenFeature.PLANT,
				HuntressBossLevel.wardenFeatureForRoll(0.119999f));
		assertEquals(HuntressBossLevel.WardenFeature.FURROW,
				HuntressBossLevel.wardenFeatureForRoll(0.12f));
		assertEquals(HuntressBossLevel.WardenFeature.FURROW,
				HuntressBossLevel.wardenFeatureForRoll(0.619999f));
		assertEquals(HuntressBossLevel.WardenFeature.NONE,
				HuntressBossLevel.wardenFeatureForRoll(0.62f));
		assertEquals(HuntressBossLevel.WardenFeature.NONE,
				HuntressBossLevel.wardenFeatureForRoll(0.999999f));
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
			assertTrue(heap.peek() instanceof BlindingDart);
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
	public void wholeLevelBundleRoundTripPreservesCoverClusters() {
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		HuntressBossLevel source = newHeadlessLevel();
		source.create();
		source.heaps.clear();
		List<Set<Integer>> expected = source.coverClusters();
		assertEquals(8, expected.size());

		Bundle bundle = new Bundle();
		source.storeInBundle(bundle);
		bundle.put("version", ShatteredPixelDungeon.v2_4_2);
		HuntressBossLevel restored = newHeadlessLevel();
		restored.restoreFromBundle(bundle);

		assertEquals(expected, restored.coverClusters());
		assertEquals(8, restored.coverClusters().size());
		for (Set<Integer> cluster : restored.coverClusters()) {
			assertEquals(4, cluster.size());
			for (int cover : cluster) {
				assertEquals(Terrain.WALL, restored.map[cover]);
			}
		}
	}

	@Test
	public void restoredLevelStillProvidesCoverToTheBossSecondarySelector() {
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		HuntressBossLevel source = newHeadlessLevel();
		source.create();
		source.heaps.clear();
		Bundle bundle = new Bundle();
		source.storeInBundle(bundle);
		bundle.put("version", ShatteredPixelDungeon.v2_4_2);
		HuntressBossLevel restored = newHeadlessLevel();
		restored.restoreFromBundle(bundle);
		Dungeon.level = restored;

		Set<Integer> cover = restored.coverClusters().get(0);
		assertFalse(cover.isEmpty());
		int coverCell = cover.iterator().next();
		HuntressBoss boss = new HuntressBoss();
		boss.pos = findClearNeighbour(restored, coverCell);
		Mob target = new Mob() { };
		target.pos = findClearProjectileTarget(restored, boss.pos, coverCell);
		assertTrue(target.pos >= 0);
		Arrays.fill(restored.heroFOV, true);
		int selected = invokeSecondaryCoverSelector(boss, target);

		assertTrue(selected >= 0);
		assertTrue(restored.adjacent(boss.pos, selected));
		assertEquals(target.pos, new Ballistica(selected, target.pos,
				Ballistica.PROJECTILE).collisionPos.intValue());
		assertTrue(restored.distance(selected, coverCell)
				<= restored.distance(boss.pos, coverCell));
	}

	private static int invokeSecondaryCoverSelector(HuntressBoss boss, Char target) {
		try {
			java.lang.reflect.Method method = HuntressBoss.class.getDeclaredMethod(
					"nearestFirePositionByCover", Char.class);
			method.setAccessible(true);
			return (Integer) method.invoke(boss, target);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static int findClearNeighbour(HuntressBossLevel level, int cover) {
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = cover + offset;
			if (cell >= 0 && cell < level.length() && level.insideMap(cell)
					&& level.passable[cell]) return cell;
		}
		throw new AssertionError("cover has no clear neighbour");
	}

	private static int findClearProjectileTarget(
			HuntressBossLevel level, int boss, int cover) {
		for (int cell = 0; cell < level.length(); cell++) {
			if (level.insideMap(cell) && level.passable[cell]
					&& level.distance(cell, boss) >= 2
					&& new Ballistica(boss, cell, Ballistica.PROJECTILE)
					.collisionPos.intValue() == cell) return cell;
		}
		return -1;
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
	public void wardenAllocationIsCellSeededOrderIndependentDiverseAndRandomIsolated() {
		ArrayList<Integer> cells = new ArrayList<>();
		for (int cell = 0; cell < 400; cell++) {
			cells.add(cell);
		}
		Set<Integer> blocked = setOf(3, 77, 201);

		Dungeon.seed = 123456L;
		HuntressBossLevel.WardenArenaAllocation first =
				HuntressBossLevel.allocateWardenArena(cells, blocked);
		Collections.reverse(cells);
		HuntressBossLevel.WardenArenaAllocation repeated =
				HuntressBossLevel.allocateWardenArena(cells, blocked);
		assertAllocationEquals(first, repeated);
		assertPairwiseDisjoint(first);
		assertAllocationEquals(expectedCellSeededAllocation(123456L, cells, blocked), first);
		for (int blockedCell : blocked) {
			assertFalse(allFeatureCells(first).contains(blockedCell));
		}

		Dungeon.seed = 654321L;
		HuntressBossLevel.WardenArenaAllocation different =
				HuntressBossLevel.allocateWardenArena(cells, blocked);
		assertFalse(sameAllocation(first, different));

		assertArrayEquals(randomPairAroundWardenAllocation(false),
				randomPairAroundWardenAllocation(true));
	}

	@Test
	public void initialFurrowsAreReclassifiedPerCellWithoutStackingFeatures() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		Map<HuntressBossLevel.WardenFeature, Integer> cells =
				findCellsForEveryWardenFeature(level, Dungeon.seed);
		for (int cell : cells.values()) {
			level.map[cell] = Terrain.FURROWED_GRASS;
		}

		HuntressBossLevel.WardenArenaAllocation allocation =
				level.populateWardenArenaForTest();

		int plantCell = cells.get(HuntressBossLevel.WardenFeature.PLANT);
		int tentacleCell = cells.get(HuntressBossLevel.WardenFeature.TENTACLE);
		int furrowCell = cells.get(HuntressBossLevel.WardenFeature.FURROW);
		int noneCell = cells.get(HuntressBossLevel.WardenFeature.NONE);
		assertTrue(allocation.plantCells.contains(plantCell));
		assertNotNull(level.plants.get(plantCell));
		assertTrue(level.map[plantCell] != Terrain.FURROWED_GRASS);
		assertTrue(allocation.tentacleCells.contains(tentacleCell));
		assertNull(level.plants.get(tentacleCell));
		assertTrue(level.map[tentacleCell] != Terrain.FURROWED_GRASS);
		assertTrue(allocation.furrowCells.contains(furrowCell));
		assertEquals(Terrain.FURROWED_GRASS, level.map[furrowCell]);
		assertFalse(allFeatureCells(allocation).contains(noneCell));
		assertEquals(Terrain.FURROWED_GRASS, level.map[noneCell]);
		assertPairwiseDisjoint(allocation);
	}

	@Test
	public void noneLeavesAnEmptyCellTerrainUnchanged() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		Map<HuntressBossLevel.WardenFeature, Integer> cells =
				findCellsForEveryWardenFeature(level, Dungeon.seed);
		int noneCell = cells.get(HuntressBossLevel.WardenFeature.NONE);
		int originalTerrain = level.map[noneCell];

		HuntressBossLevel.WardenArenaAllocation allocation =
				level.populateWardenArenaForTest();

		assertFalse(allFeatureCells(allocation).contains(noneCell));
		assertEquals(originalTerrain, level.map[noneCell]);
		assertNull(level.plants.get(noneCell));
	}

	@Test
	public void plantCreationUsesNextDrawFromItsOwnCellRandomStream() {
		SeedClassRecordingLevel level = createSeedClassRecordingLevel(24681357L);
		HuntressBossLevel.WardenArenaAllocation allocation =
				level.populateWardenArenaForTest();
		Set<Class<? extends Plant.Seed>> selected = new HashSet<>();
		assertEquals(allocation.plantCells, level.selectedSeedClasses.keySet());
		for (int cell : allocation.plantCells) {
			Class<? extends Plant.Seed> expected =
					expectedArenaSeedClass(Dungeon.seed, cell);
			Class<? extends Plant.Seed> actual = level.selectedSeedClasses.get(cell);
			selected.add(actual);
			assertEquals(expected, actual);
		}
		assertTrue(selected.size() > 1);
	}

	@Test
	public void onWardenPhaseUsesCellSeededGenerationAndBossAlignment() {
		WardenPhaseRecordingLevel level = createWardenPhaseRecordingLevel(123456L);
		HuntressBoss boss = new HuntressBoss();
		boss.alignment = Char.Alignment.ALLY;
		boss.pos = cell(level, 15, 8);
		Set<Integer> protectedCells = new HashSet<>();
		int heapCell = nextUnoccupiedCell(level, level.vegetationCells(), protectedCells);
		Heap heap = new Heap();
		heap.pos = heapCell;
		level.heaps.put(heapCell, heap);
		protectedCells.add(heapCell);
		int plantCell = nextUnoccupiedCell(level, level.vegetationCells(), protectedCells);
		Firebloom existingPlant = new Firebloom();
		existingPlant.pos = plantCell;
		level.plants.put(plantCell, existingPlant);
		protectedCells.add(plantCell);

		level.onWardenPhase(boss);

		assertTrue(level.generatedPlantCount > 0);
		assertTrue(level.generatedTentacleCount > 0);
		assertEquals(level.generatedTentacleCount, level.scheduledTentacles.size());
		for (HuntressBoss.HuntressTentacle tentacle : level.scheduledTentacles) {
			assertEquals(Char.Alignment.ALLY, tentacle.alignment);
			assertNull(level.plants.get(tentacle.pos));
			assertTrue(level.map[tentacle.pos] != Terrain.FURROWED_GRASS);
		}
		assertSame(heap, level.heaps.get(heapCell));
		assertSame(existingPlant, level.plants.get(plantCell));
		assertTrue(level.mapUpdated);
		assertTrue(level.phaseMusicStarted);
	}

	@Test
	public void realWardenPopulationPreservesActorsPlantsHeapsCoversAndReservedCells() {
		HuntressBossLevel level = createSpawnTestLevel(123456L);
		Set<Integer> coverCells = new HashSet<>();
		for (Set<Integer> cluster : level.coverClusters()) {
			coverCells.addAll(cluster);
		}
		Set<Integer> entranceHeapCells = new HashSet<>();
		for (int heapCell : level.heaps.keyArray()) {
			entranceHeapCells.add(heapCell);
		}
		assertEquals(2, entranceHeapCells.size());
		ArrayList<Integer> legal = level.vegetationCells();
		int ordinaryHeapCell = nextUnoccupiedCell(level, legal, Collections.<Integer>emptySet());
		Heap ordinaryHeap = new Heap();
		ordinaryHeap.pos = ordinaryHeapCell;
		level.heaps.put(ordinaryHeapCell, ordinaryHeap);
		Set<Integer> used = setOf(ordinaryHeapCell);

		int plantCell = nextUnoccupiedCell(level, legal, used);
		Firebloom existingPlant = new Firebloom();
		existingPlant.pos = plantCell;
		level.plants.put(plantCell, existingPlant);
		used.add(plantCell);

		int actorCell = nextUnoccupiedCell(level, legal, used);
		Mob occupant = registerActor(new Mob() {
		});
		occupant.pos = actorCell;

		int expectedBefore;
		int expectedAfter;
		Random.pushGenerator(445533L);
		try {
			expectedBefore = Random.Int();
			expectedAfter = Random.Int();
		} finally {
			Random.popGenerator();
		}
		HuntressBossLevel.WardenArenaAllocation allocation;
		Random.pushGenerator(445533L);
		try {
			assertEquals(expectedBefore, Random.Int());
			allocation = level.populateWardenArenaForTest();
			assertEquals(expectedAfter, Random.Int());
		} finally {
			Random.popGenerator();
		}

		assertPairwiseDisjoint(allocation);
		for (int blocked : new int[]{ordinaryHeapCell, plantCell, actorCell}) {
			assertFalse(allFeatureCells(allocation).contains(blocked));
		}
		assertSame(ordinaryHeap, level.heaps.get(ordinaryHeapCell));
		assertSame(existingPlant, level.plants.get(plantCell));
		for (int generatedPlant : allocation.plantCells) {
			assertNotNull(level.plants.get(generatedPlant));
			assertTrue(level.map[generatedPlant] != Terrain.FURROWED_GRASS);
		}
		for (int tentacle : allocation.tentacleCells) {
			assertNull(level.plants.get(tentacle));
			assertTrue(level.map[tentacle] != Terrain.FURROWED_GRASS);
		}
		for (int furrow : allocation.furrowCells) {
			assertEquals(Terrain.FURROWED_GRASS, level.map[furrow]);
			assertNull(level.plants.get(furrow));
		}
		for (int heapCell : entranceHeapCells) {
			assertNotNull(level.heaps.get(heapCell));
			assertFalse(allFeatureCells(allocation).contains(heapCell));
		}
		for (int cover : coverCells) {
			assertEquals(Terrain.WALL, level.map[cover]);
			assertFalse(allFeatureCells(allocation).contains(cover));
		}
		for (int reserved = 0; reserved < level.length(); reserved++) {
			if (level.isReservedEncounterCell(reserved)) {
				assertFalse(allFeatureCells(allocation).contains(reserved));
			}
		}
		assertTrue(level.hasCriticalArenaPaths(coverCells));
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
		assertEquals(Terrain.FURROWED_GRASS,
				HuntressBossLevel.terrainForWardenFeature(
						Terrain.FURROWED_GRASS, HuntressBossLevel.WardenFeature.NONE));
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

	private static void assertAllocationEquals(
			HuntressBossLevel.WardenArenaAllocation expected,
			HuntressBossLevel.WardenArenaAllocation actual) {
		assertEquals(expected.furrowCells, actual.furrowCells);
		assertEquals(expected.plantCells, actual.plantCells);
		assertEquals(expected.tentacleCells, actual.tentacleCells);
	}

	private static boolean sameAllocation(
			HuntressBossLevel.WardenArenaAllocation first,
			HuntressBossLevel.WardenArenaAllocation second) {
		return first.furrowCells.equals(second.furrowCells)
				&& first.plantCells.equals(second.plantCells)
				&& first.tentacleCells.equals(second.tentacleCells);
	}

	private static Set<Integer> allFeatureCells(
			HuntressBossLevel.WardenArenaAllocation allocation) {
		Set<Integer> result = new HashSet<>(allocation.furrowCells);
		result.addAll(allocation.plantCells);
		result.addAll(allocation.tentacleCells);
		return result;
	}

	private static HuntressBossLevel.WardenArenaAllocation expectedCellSeededAllocation(
			long dungeonSeed, List<Integer> cells, Set<Integer> blocked) {
		Set<Integer> furrows = new HashSet<>();
		Set<Integer> plants = new HashSet<>();
		Set<Integer> tentacles = new HashSet<>();
		for (int cell : cells) {
			if (blocked.contains(cell)) continue;
			HuntressBossLevel.WardenFeature feature = expectedWardenFeature(dungeonSeed, cell);
			if (feature == HuntressBossLevel.WardenFeature.FURROW) furrows.add(cell);
			if (feature == HuntressBossLevel.WardenFeature.PLANT) plants.add(cell);
			if (feature == HuntressBossLevel.WardenFeature.TENTACLE) tentacles.add(cell);
		}
		return new HuntressBossLevel.WardenArenaAllocation(furrows, plants, tentacles);
	}

	private static HuntressBossLevel.WardenFeature expectedWardenFeature(
			long dungeonSeed, int cell) {
		Random.pushGenerator(dungeonSeed + cell);
		try {
			float roll = Random.Float();
			if (roll < 0.02f) return HuntressBossLevel.WardenFeature.TENTACLE;
			if (roll < 0.12f) return HuntressBossLevel.WardenFeature.PLANT;
			if (roll < 0.62f) return HuntressBossLevel.WardenFeature.FURROW;
			return HuntressBossLevel.WardenFeature.NONE;
		} finally {
			Random.popGenerator();
		}
	}

	private static Class<? extends Plant.Seed> expectedArenaSeedClass(
			long dungeonSeed, int cell) {
		Class<? extends Plant.Seed>[] seedClasses = expectedArenaSeedClasses();
		Random.pushGenerator(dungeonSeed + cell);
		try {
			Random.Float();
			return seedClasses[Random.Int(seedClasses.length)];
		} finally {
			Random.popGenerator();
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Plant.Seed>[] expectedArenaSeedClasses() {
		return new Class[]{
				Sungrass.Seed.class,
				Fadeleaf.Seed.class,
				Icecap.Seed.class,
				Firebloom.Seed.class,
				Sorrowmoss.Seed.class,
				Swiftthistle.Seed.class,
				Blindweed.Seed.class,
				Stormvine.Seed.class,
				Earthroot.Seed.class,
				Mageroyal.Seed.class,
				Starflower.Seed.class
		};
	}

	private static Map<HuntressBossLevel.WardenFeature, Integer>
			findCellsForEveryWardenFeature(HuntressBossLevel level, long dungeonSeed) {
		Map<HuntressBossLevel.WardenFeature, Integer> result = new LinkedHashMap<>();
		for (int cell : level.vegetationCells()) {
			if (Actor.findChar(cell) != null || level.heaps.get(cell) != null
					|| level.plants.get(cell) != null || level.isReservedEncounterCell(cell)) {
				continue;
			}
			HuntressBossLevel.WardenFeature feature = expectedWardenFeature(dungeonSeed, cell);
			if (!result.containsKey(feature)) result.put(feature, cell);
		}
		for (HuntressBossLevel.WardenFeature feature
				: HuntressBossLevel.WardenFeature.values()) {
			assertTrue("missing cell for " + feature, result.containsKey(feature));
		}
		return result;
	}

	private static int nextUnoccupiedCell(HuntressBossLevel level,
			List<Integer> candidates, Set<Integer> excluded) {
		for (int cell : candidates) {
			if (!excluded.contains(cell) && !level.isReservedEncounterCell(cell)
					&& level.map[cell] != Terrain.FURROWED_GRASS
					&& level.heaps.get(cell) == null && level.plants.get(cell) == null
					&& Actor.findChar(cell) == null) {
				return cell;
			}
		}
		throw new AssertionError("no unoccupied vegetation cell");
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

	private Wraith addMob(HuntressBossLevel level, int pos, Char.Alignment alignment) {
		Wraith mob = new Wraith();
		mob.pos = pos;
		mob.alignment = alignment;
		level.mobs.add(mob);
		return mob;
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

	private static RecordingFadeleafLevel createRecordingFadeleafLevel() {
		RecordingFadeleafLevel level = new RecordingFadeleafLevel();
		Dungeon.seed = 123456L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		Dungeon.level = null;
		level.create();
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

	private static int firstPlantCellAtPathDistance(HuntressBossLevel level, int distance) {
		for (int cell = 0; cell < level.length(); cell++) {
			if (PathFinder.distance[cell] == distance && level.passable[cell]
					&& Actor.findChar(cell) == null) {
				return cell;
			}
		}
		throw new AssertionError("no cell at path distance " + distance);
	}

	private static void putFirebloom(HuntressBossLevel level, int cell) {
		Firebloom plant = new Firebloom();
		plant.pos = cell;
		level.plants.put(cell, plant);
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

	private static int entranceDartHeapCount(HuntressBossLevel level) {
		int count = 0;
		for (Heap heap : level.heaps.valueList()) {
			if (heap.peek() instanceof BlindingDart) {
				count++;
			}
		}
		return count;
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

	private static int[] randomPairAroundWardenAllocation(boolean allocate) {
		Random.pushGenerator(887766L);
		try {
			int before = Random.Int();
			if (allocate) {
				ArrayList<Integer> cells = new ArrayList<>();
				for (int cell = 0; cell < 100; cell++) {
					cells.add(cell);
				}
				HuntressBossLevel.allocateWardenArena(cells,
						Collections.<Integer>emptySet());
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

	private static RecordingPlantLevel createRecordingPlantLevel() {
		Dungeon.seed = 24681357L;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		RecordingPlantLevel level = new RecordingPlantLevel();
		level.create();
		return level;
	}

	private static WardenPhaseRecordingLevel createWardenPhaseRecordingLevel(long seed) {
		Dungeon.seed = seed;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		WardenPhaseRecordingLevel level = new WardenPhaseRecordingLevel();
		level.create();
		Dungeon.level = level;
		return level;
	}

	private static SeedClassRecordingLevel createSeedClassRecordingLevel(long seed) {
		Dungeon.seed = seed;
		Dungeon.depth = 15;
		Dungeon.branch = 0;
		SeedClassRecordingLevel level = new SeedClassRecordingLevel();
		level.create();
		Dungeon.level = level;
		return level;
	}

	private static HuntressBossLevel newHeadlessLevel() {
		return new HuntressBossLevel() {
			@Override
			Item createEntranceDart() {
				// Skip GPU-backed sprite initialization while retaining the real item type.
				return allocateWithoutConstructor(BlindingDart.class);
			}

			@Override
			protected com.shatteredpixel.shatteredpixeldungeon.plants.Plant
					createWardenArenaPlant(int cell,
							Class<? extends Plant.Seed> seedClass) {
				Firebloom plant = new Firebloom();
				plant.pos = cell;
				return plant;
			}
		};
	}

	private static <T> T allocateWithoutConstructor(Class<T> type) {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			Unsafe unsafe = (Unsafe) unsafeField.get(null);
			return type.cast(unsafe.allocateInstance(type));
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
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
		final ArrayList<Mob> teleportedAllies = new ArrayList<>();
		int allyDestination = -1;
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
		protected void teleportEncounterAlly(Mob ally, int destination) {
			teleportedAllies.add(ally);
			ally.pos = destination;
		}

		@Override
		protected int selectEncounterAllyCell(Mob ally) {
			return allyDestination;
		}

		@Override
		void completeOccupyCell(Char ch) {
			// The real trigger and startFight already ran; base terrain effects need a full Hero.
		}
	}

	private static final class RecordingPlantLevel extends HuntressBossLevel {
		@Override
		Item createEntranceDart() {
			return new Item();
		}

		@Override
		protected Plant createWardenArenaPlant(int cell,
				Class<? extends Plant.Seed> seedClass) {
			Firebloom plant = new Firebloom();
			plant.pos = cell;
			return plant;
		}

		@Override
		void completeOccupyCell(Char ch) {
			// The test exercises the Huntress-specific plant transaction only.
		}
	}

	private static final class WardenPhaseRecordingLevel extends HuntressBossLevel {
		final ArrayList<HuntressBoss.HuntressTentacle> scheduledTentacles =
				new ArrayList<>();
		int generatedPlantCount;
		int generatedTentacleCount;
		boolean mapUpdated;
		boolean phaseMusicStarted;

		@Override
		Item createEntranceDart() {
			return new Item();
		}

		@Override
		protected Plant createWardenArenaPlant(int cell,
				Class<? extends Plant.Seed> seedClass) {
			generatedPlantCount++;
			Firebloom plant = new Firebloom();
			plant.pos = cell;
			return plant;
		}

		@Override
		void submitWardenPlant(Plant plant) {
			// Headless test: the plant is already present in the level map.
		}

		@Override
		void submitWardenTentacle(HuntressBoss.HuntressTentacle tentacle) {
			generatedTentacleCount++;
			scheduledTentacles.add(tentacle);
		}

		@Override
		void finishWardenArenaUpdate() {
			mapUpdated = true;
		}

		@Override
		void startWardenPhaseMusic() {
			phaseMusicStarted = true;
		}

		@Override
		void scheduleEncounterMob(Mob mob, float delay) {
			// The second hawk is outside this vegetation-entry contract.
		}
	}

	private static final class SeedClassRecordingLevel extends HuntressBossLevel {
		final Map<Integer, Class<? extends Plant.Seed>> selectedSeedClasses =
				new LinkedHashMap<>();

		@Override
		Item createEntranceDart() {
			return new Item();
		}

		@Override
		protected Plant createWardenArenaPlant(int cell,
				Class<? extends Plant.Seed> seedClass) {
			selectedSeedClasses.put(cell, seedClass);
			Firebloom plant = new Firebloom();
			plant.pos = cell;
			return plant;
		}
	}

	private static final class RecordingPlantBoss extends HuntressBoss {
		int removedCalls;
		int removedCell = -1;
		int claimedCalls;
		int claimedCell = -1;
		WardenBoon claimedBoon;

		void enterWardenForTest() {
			enterWardenPhase();
		}

		@Override
		public void onPlantRemoved(int cell) {
			removedCalls++;
			removedCell = cell;
		}

		@Override
		public void onPlantClaimed(int cell, WardenBoon boon) {
			claimedCalls++;
			claimedCell = cell;
			claimedBoon = boon;
		}
	}

	private static final class InspectablePlantBoss extends HuntressBoss {
		int boonAnnouncements;

		void restoreSelectedAt(int bossCell, int markedCell) {
			Bundle bundle = new Bundle();
			bundle.put("pos", bossCell);
			bundle.put("phase", Phase.WARDEN);
			bundle.put("plant_hunt_state", PlantHuntState.SELECTED);
			bundle.put("marked_plant_cell", markedCell);
			bundle.put("plant_hunt_turns", PLANT_HUNT_DURATION);
			bundle.put("plant_grace_turns", 0);
			restoreFromBundle(bundle);
		}

		PlantHuntState savedPlantState() {
			Bundle bundle = saved();
			return bundle.getEnum("plant_hunt_state", PlantHuntState.class);
		}

		WardenBoon savedActiveBoon() {
			Bundle bundle = saved();
			return bundle.getEnum("active_boon", WardenBoon.class);
		}

		int savedMarkedPlantCell() {
			return saved().getInt("marked_plant_cell");
		}

		private Bundle saved() {
			Bundle bundle = new Bundle();
			storeInBundle(bundle);
			return bundle;
		}

		@Override
		protected void announceBoon(WardenBoon boon) {
			boonAnnouncements++;
		}
	}

	private static final class RecordingFadeleafLevel extends HuntressBossLevel {
		final ArrayList<Char> teleported = new ArrayList<>();
		final ArrayList<Char> restored = new ArrayList<>();
		int finishedTeleports;
		int failedTeleportIndex = -1;
		int wrongPositionTeleportIndex = -1;
		int thrownTeleportIndex = -1;
		int thrownRollbackIndex = -1;
		boolean rollbackWritesWrongPosition;
		boolean finishThrows;

		@Override
		Item createEntranceDart() {
			return new Item();
		}

		@Override
		protected boolean teleportFadeleafChar(Char ch, int destination) {
			teleported.add(ch);
			int teleportIndex = teleported.size();
			if (teleportIndex == failedTeleportIndex) {
				return false;
			}
			if (teleportIndex == thrownTeleportIndex) {
				ch.pos = destination;
				throw new IllegalStateException("forward teleport failed");
			}
			if (teleportIndex == wrongPositionTeleportIndex) {
				ch.pos = destination + 1;
				return true;
			}
			ch.pos = destination;
			return true;
		}

		@Override
		protected void restoreFadeleafChar(Char ch, int origin) {
			restored.add(ch);
			if (restored.size() == thrownRollbackIndex) {
				throw new IllegalStateException("rollback failed");
			}
			ch.pos = rollbackWritesWrongPosition ? origin + 1 : origin;
		}

		@Override
		protected void finishFadeleafTeleport() {
			finishedTeleports++;
			if (finishThrows) {
				throw new IllegalStateException("finish failed");
			}
		}
	}

	private static final class RecordingMovementSprite extends CharSprite {
		int moves;
		int places;
		int from = -1;
		int to = -1;
		int finalCell = -1;

		private RecordingMovementSprite() {
			visible = true;
			parent = new com.watabou.noosa.Group();
		}

		@Override
		public void move(int from, int to) {
			moves++;
			this.from = from;
			this.to = to;
			finalCell = to;
		}

		@Override
		public void place(int cell) {
			places++;
			finalCell = cell;
		}

		@Override
		public void turnTo(int from, int to) {
			// Direction rendering is not part of this movement contract.
		}
	}
}
