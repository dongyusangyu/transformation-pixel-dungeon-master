package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TestStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Dongyusangyu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DungeonDoctor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SurfaceShopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SurfaceTownLevelDongyusangyuTest {

	@Test
	public void buildsCompactForestTownWithoutDownstairs() {
		SurfaceTownLevel level = testLevel();

		level.create();

		assertEquals(46, level.width());
		assertEquals(34, level.height());
		assertEquals(1, countTerrain(level, Terrain.EXIT));
		assertEquals(1, countTerrain(level, Terrain.ENTRANCE));
		assertEquals(5, countTerrain(level, Terrain.DOOR));
		assertEquals(1, countTerrain(level, Terrain.LOCKED_DOOR));
		assertEquals(1, countTerrain(level, Terrain.WELL));
		assertEquals(Terrain.WELL, level.map[cell(level, 10, 17)]);
		assertTrue(countTerrain(level, Terrain.WATER) >= 70);
		assertEquals(countTerrain(level, Terrain.WATER), connectedTerrainCount(level, Terrain.WATER));
		assertTrue(countTerrain(level, Terrain.REGION_DECO)
				+ countTerrain(level, Terrain.REGION_DECO_ALT) >= 150);
		for (int interior : new int[]{
				cell(level, 8, 8),
				cell(level, 22, 6),
				cell(level, 38, 8),
				cell(level, 8, 28),
				cell(level, 38, 28)}) {
			assertTrue(reachable(level, level.entrance, interior));
		}
		for (int statue : new int[]{
				cell(level, 10, 8),
				cell(level, 24, 6),
				cell(level, 10, 28),
				cell(level, 40, 28)}) {
			assertEquals("decorative statue at cell " + statue,
					Terrain.STATUE_SP, level.map[statue]);
		}
		assertEquals(Terrain.EMPTY_SP, level.map[cell(level, 40, 9)]);
		assertEquals(Terrain.STATUE, level.map[cell(level, 27, 25)]);
		assertEquals(1, countTerrain(level, Terrain.PEDESTAL));
		assertEquals(Terrain.PEDESTAL, level.map[cell(level, 24, 16)]);
	}

	@Test
	public void usesFixedSparseDecorativeGrass() {
		SurfaceTownLevel level = testLevel();

		level.create();

		int[][] grass = {
				{6, 13}, {7, 13},
				{13, 5}, {14, 5},
				{10, 15}, {11, 15},
				{30, 12}, {31, 12},
				{14, 24}, {14, 25},
				{30, 26}, {31, 26}
		};
		assertEquals(grass.length, countTerrain(level, Terrain.HIGH_GRASS));
		for (int[] point : grass) {
			assertEquals(Terrain.HIGH_GRASS, level.map[cell(level, point[0], point[1])]);
		}
	}

	@Test
	public void usesDirectionAwareTilesForTheConnectedVillagePaths() {
		SurfaceTownLevel level = testLevel();

		level.create();

		assertTrue(countTerrain(level, Terrain.EMPTY_DECO) >= 90);
		assertEquals(countTerrain(level, Terrain.EMPTY_DECO),
				connectedTerrainCount(level, Terrain.EMPTY_DECO));
		for (int[] point : new int[][]{
				{8, 10},
				{22, 8},
				{38, 11},
				{32, 11},
				{22, 21},
				{8, 30},
				{38, 30},
				{22, 31}}) {
			assertEquals(Terrain.EMPTY_DECO, level.map[cell(level, point[0], point[1])]);
		}
		for (int y = 10; y <= 30; y++) {
			assertEquals(Terrain.EMPTY_DECO, level.map[cell(level, 32, y)]);
		}

		assertEquals(1, SurfaceTownLevel.SurfacePathTilemap.tileForConnections(
				true, false, true, false));
		assertEquals(6, SurfaceTownLevel.SurfacePathTilemap.tileForConnections(
				false, true, false, true));
		assertEquals(7, SurfaceTownLevel.SurfacePathTilemap.tileForConnections(
				true, true, false, false));
		assertEquals(7, SurfaceTownLevel.SurfacePathTilemap.tileForConnections(
				true, true, true, true));
		assertEquals(1, SurfaceTownLevel.SurfacePathTilemap.tileForCell(level, 13, 15));
		assertEquals(6, SurfaceTownLevel.SurfacePathTilemap.tileForCell(level, 14, 10));
		assertEquals(7, SurfaceTownLevel.SurfacePathTilemap.tileForCell(level, 13, 10));
		assertEquals(7, SurfaceTownLevel.SurfacePathTilemap.tileForCell(level, 32, 11));
		assertEquals(7, SurfaceTownLevel.SurfacePathTilemap.tileForCell(level, 38, 11));

		boolean hasPathVisual = false;
		for (CustomTilemap tile : level.customTiles) {
			if (tile instanceof SurfaceTownLevel.SurfacePathTilemap) {
				hasPathVisual = true;
				break;
			}
		}
		assertTrue(hasPathVisual);
		for (int x = 7; x <= 39; x++) {
			int terrain = level.map[cell(level, x, 31)];
			assertFalse(terrain == Terrain.REGION_DECO || terrain == Terrain.REGION_DECO_ALT);
		}
	}

	@Test
	public void usesOneContinuousWoodTileForEveryIndoorFloor() {
		SurfaceTownLevel level = testLevel();

		level.create();

		assertEquals(DungeonTileSheet.FLOOR_SP,
				SurfaceTownLevel.SurfaceInteriorFloorTilemap.tileForTerrain(Terrain.EMPTY_SP));
		assertEquals(-1,
				SurfaceTownLevel.SurfaceInteriorFloorTilemap.tileForTerrain(Terrain.EMPTY));

		boolean hasInteriorFloorVisual = false;
		for (CustomTilemap tile : level.customTiles) {
			if (tile instanceof SurfaceTownLevel.SurfaceInteriorFloorTilemap) {
				hasInteriorFloorVisual = true;
				break;
			}
		}
		assertTrue(hasInteriorFloorVisual);
	}

	@Test
	public void surroundsTownWithTreesInsteadOfPerimeterWalls() {
		SurfaceTownLevel level = testLevel();

		level.create();

		for (int x = 0; x < level.width(); x++) {
			assertNotWall(level.map[x]);
			assertNotWall(level.map[x + (level.height() - 1) * level.width()]);
		}
		for (int y = 0; y < level.height(); y++) {
			assertNotWall(level.map[y * level.width()]);
			assertNotWall(level.map[level.width() - 1 + y * level.width()]);
		}
	}

	@Test
	public void keepsCentralManorLockedAndItsUpstairsUnreachable() {
		SurfaceTownLevel level = testLevel();

		level.create();

		int manorStair = cell(level, 22, 14);
		int manorDoorInterior = cell(level, 22, 19);
		assertEquals(Terrain.ENTRANCE, level.map[manorStair]);
		assertEquals(manorStair, level.exit);
		assertEquals(Terrain.LOCKED_DOOR, level.map[cell(level, 22, 20)]);
		assertEquals(Terrain.EMPTY_SP, level.map[manorDoorInterior]);
		assertTrue(level.solid[manorDoorInterior]);
		assertFalse(reachable(level, level.entrance, manorStair));

		level.map[cell(level, 22, 20)] = Terrain.OPEN_DOOR;
		level.buildFlagMaps();
		assertFalse(reachable(level, level.entrance, manorStair));
	}

	@Test
	public void usesDownstairsAsBlockedArrivalAndUpstairsAsLockedExit() {
		SurfaceTownLevel level = testLevel();

		level.create();

		assertEquals(Terrain.EXIT, level.map[level.entrance]);
		assertEquals(Terrain.ENTRANCE, level.map[level.exit]);

		LevelTransition arrival = null;
		LevelTransition lockedExit = null;
		for (LevelTransition transition : level.transitions) {
			if (transition.type == LevelTransition.Type.REGULAR_ENTRANCE) {
				arrival = transition;
			} else if (transition.type == LevelTransition.Type.REGULAR_EXIT) {
				lockedExit = transition;
			}
		}
		assertTrue(arrival != null);
		assertTrue(lockedExit != null);
		assertEquals(level.entrance, arrival.cell());
		assertEquals(level.exit, lockedExit.cell());
		assertEquals(1, lockedExit.destDepth);
		assertEquals(3, lockedExit.destBranch);
		assertEquals(LevelTransition.Type.REGULAR_ENTRANCE, lockedExit.destType);
		assertFalse(level.activateTransition(null, arrival));
	}

	@Test
	public void createsDongyusangyuInsideSouthwestHouse() {
		SurfaceTownLevel level = testLevel();

		level.create();

		assertEquals(4, level.mobs.size());
		Mob dongyusangyu = findMob(level, Dongyusangyu.class);
		Mob dungeonDoctor = findMob(level, DungeonDoctor.class);
		Mob shopkeeper = findMob(level, SurfaceShopkeeper.class);
		Mob testStatue = findMob(level, TestStatue.class);
		assertTrue(dongyusangyu != null);
		assertTrue(dungeonDoctor != null);
		assertTrue(shopkeeper != null);
		assertTrue(testStatue != null);
		assertEquals(cell(level, 7, 26), dongyusangyu.pos);
		assertEquals(cell(level, 22, 4), dungeonDoctor.pos);
		assertEquals(cell(level, 38, 7), shopkeeper.pos);
		assertEquals(cell(level, 27, 30), testStatue.pos);
		assertEquals(Terrain.EMPTY_SP, level.map[dongyusangyu.pos]);
		assertEquals(Terrain.EMPTY_SP, level.map[dungeonDoctor.pos]);
		assertEquals(Terrain.EMPTY_SP, level.map[shopkeeper.pos]);
		assertEquals(Terrain.EMPTY_DECO, level.map[testStatue.pos]);
		assertFalse(dongyusangyu.pos / level.width() == shopkeeper.pos / level.width());
	}

	@Test
	public void doesNotDuplicateSurfaceNpcsWhenMobCreationRunsAgain() {
		SurfaceTownLevel level = testLevel();
		level.create();

		level.createMobs();

		assertEquals(4, level.mobs.size());
		assertEquals(1, countMobs(level, DungeonDoctor.class));
		assertEquals(1, countMobs(level, Dongyusangyu.class));
		assertEquals(1, countMobs(level, SurfaceShopkeeper.class));
		assertEquals(1, countMobs(level, TestStatue.class));
	}

	@Test
	public void placesBookshelvesOnlyAlongDongyusangyuHouseNorthWall() {
		SurfaceTownLevel level = testLevel();

		level.create();

		assertEquals(6, countTerrain(level, Terrain.BOOKSHELF));
		for (int x = 5; x <= 10; x++) {
			assertEquals(Terrain.BOOKSHELF, level.map[cell(level, x, 24)]);
		}
	}

	@Test
	public void placesAlchemyPotInsideDongyusangyuHouse() {
		SurfaceTownLevel level = testLevel();

		level.create();

		assertEquals(1, countTerrain(level, Terrain.ALCHEMY));
		assertEquals(Terrain.ALCHEMY, level.map[cell(level, 10, 27)]);
		assertTrue(reachable(level, level.entrance, cell(level, 9, 27)));
	}

	private static int countTerrain(SurfaceTownLevel level, int terrain) {
		int result = 0;
		for (int tile : level.map) {
			if (tile == terrain) {
				result++;
			}
		}
		return result;
	}

	private static SurfaceTownLevel testLevel() {
		return new SurfaceTownLevel() {
			@Override
			protected void createItems() {
				// Item sprites need a running libGDX application; topology tests do not.
			}
		};
	}

	private static Mob findMob(SurfaceTownLevel level, Class<? extends Mob> mobClass) {
		for (Mob mob : level.mobs) {
			if (mobClass.isInstance(mob)) {
				return mob;
			}
		}
		return null;
	}

	private static int countMobs(SurfaceTownLevel level, Class<? extends Mob> mobClass) {
		int count = 0;
		for (Mob mob : level.mobs) {
			if (mobClass.isInstance(mob)) {
				count++;
			}
		}
		return count;
	}

	private static int cell(SurfaceTownLevel level, int x, int y) {
		return x + y * level.width();
	}

	private static void assertNotWall(int terrain) {
		assertFalse(terrain == Terrain.WALL || terrain == Terrain.WALL_DECO);
	}

	private static boolean reachable(SurfaceTownLevel level, int start, int target) {
		boolean[] visited = new boolean[level.length()];
		Queue<Integer> pending = new ArrayDeque<>();
		visited[start] = true;
		pending.add(start);

		int[] offsets = {-level.width(), 1, level.width(), -1};
		while (!pending.isEmpty()) {
			int current = pending.remove();
			if (current == target) {
				return true;
			}
			for (int offset : offsets) {
				int next = current + offset;
				if (next < 0 || next >= level.length() || visited[next]) {
					continue;
				}
				if (offset == 1 && next % level.width() == 0
						|| offset == -1 && current % level.width() == 0) {
					continue;
				}
				if (level.passable[next] || level.avoid[next]) {
					visited[next] = true;
					pending.add(next);
				}
			}
		}
		return false;
	}

	private static int connectedTerrainCount(SurfaceTownLevel level, int terrain) {
		boolean[] visited = new boolean[level.length()];
		Queue<Integer> pending = new ArrayDeque<>();
		for (int i = 0; i < level.length(); i++) {
			if (level.map[i] == terrain) {
				visited[i] = true;
				pending.add(i);
				break;
			}
		}

		int result = 0;
		int[] offsets = {-level.width(), 1, level.width(), -1};
		while (!pending.isEmpty()) {
			int current = pending.remove();
			result++;
			for (int offset : offsets) {
				int next = current + offset;
				if (next < 0 || next >= level.length() || visited[next] || level.map[next] != terrain) {
					continue;
				}
				if (offset == 1 && next % level.width() == 0
						|| offset == -1 && current % level.width() == 0) {
					continue;
				}
				visited[next] = true;
				pending.add(next);
			}
		}
		return result;
	}
}
