package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestArenaLevelTest {

	private static final String LEVEL_CLASS =
			"com.shatteredpixel.shatteredpixeldungeon.custom.testmode.levels.TestArenaLevel";

	@Test
	public void buildsEmptyRadiusFifteenArenaWithoutStairs() throws Exception {
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			Dungeon.depth = 30;
			Dungeon.branch = 2;

			Level level = (Level) Class.forName(LEVEL_CLASS).getDeclaredConstructor().newInstance();
			level.create();

			assertEquals(33, level.width());
			assertEquals(33, level.height());
			assertEquals(Terrain.EMPTY, level.map[cell(level, 16, 16)]);
			assertEquals(Terrain.EMPTY, level.map[cell(level, 1, 16)]);
			assertEquals(Terrain.EMPTY, level.map[cell(level, 31, 16)]);
			assertEquals(Terrain.WALL, level.map[cell(level, 0, 16)]);
			assertEquals(Terrain.WALL, level.map[cell(level, 32, 16)]);
			assertEquals(Terrain.WALL, level.map[cell(level, 5, 5)]);
			assertTrue(level.passable[cell(level, 16, 16)]);
			assertTrue(level.transitions.isEmpty());
			assertTrue(level.mobs.isEmpty());
			assertTrue(level.heaps.valueList().isEmpty());
			assertEquals(0, countTerrain(level, Terrain.ENTRANCE));
			assertEquals(0, countTerrain(level, Terrain.EXIT));
		} finally {
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
		}
	}

	@Test
	public void onlyMatchesDedicatedDepthAndBranch() throws Exception {
		Class<?> levelClass = Class.forName(LEVEL_CLASS);
		Method isLocation = levelClass.getMethod("isLocation", int.class, int.class);

		assertTrue((Boolean) isLocation.invoke(null, 30, 2));
		assertFalse((Boolean) isLocation.invoke(null, 30, 0));
		assertFalse((Boolean) isLocation.invoke(null, 29, 2));
	}

	@Test
	public void blocksOrdinaryInterfloorTravelAndUsesDedicatedMode() throws Exception {
		Level oldLevel = Dungeon.level;
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			Dungeon.depth = 30;
			Dungeon.branch = 2;
			Level level = (Level) Class.forName(LEVEL_CLASS).getDeclaredConstructor().newInstance();
			level.create();
			Dungeon.level = level;

			assertFalse(Dungeon.interfloorTeleportAllowed());
			assertEquals("TEST_ARENA", InterlevelScene.Mode.valueOf("TEST_ARENA").name());
		} finally {
			Dungeon.level = oldLevel;
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
		}
	}

	private static int cell(Level level, int x, int y) {
		return x + y * level.width();
	}

	private static int countTerrain(Level level, int terrain) {
		int result = 0;
		for (int tile : level.map) {
			if (tile == terrain) {
				result++;
			}
		}
		return result;
	}
}
