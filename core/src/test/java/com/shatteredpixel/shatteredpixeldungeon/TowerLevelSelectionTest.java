package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GothicCastleLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TowerLevelSelectionTest {

	@Test
	public void towerLevelLivesInDedicatedPackage() {
		assertEquals(
				"com.shatteredpixel.shatteredpixeldungeon.levels.towers",
				TowerLevel.class.getPackage().getName());
	}

	@Test
	public void dungeonDispatchesEveryTowerFloorToTowerLevel() {
		assertSame(TowerLevel.class, Dungeon.towerLevelClassForLocation(1, 3));
		assertSame(TowerLevel.class, Dungeon.towerLevelClassForLocation(2, 3));
		assertSame(TowerLevel.class, Dungeon.towerLevelClassForLocation(25, 3));
		assertSame(TowerLevel.class, Dungeon.towerLevelClassForLocation(100, 3));

		assertNull(Dungeon.towerLevelClassForLocation(0, 3));
		assertNull(Dungeon.towerLevelClassForLocation(1, 0));
		assertNull(Dungeon.towerLevelClassForLocation(30, 2));
	}

	@Test
	public void towerLevelUsesChineseHallEnvironmentTextures() {
		assertEquals(
				"environment/tiles_chinese_hall.png",
				Assets.Environment.TILES_CHINESE_HALL);
		assertEquals(
				"environment/water_chinese_hall.png",
				Assets.Environment.WATER_CHINESE_HALL);
	}

	@Test
	public void gothicCastleLevelProvidesAnExplicitTowerEnvironment() throws Exception {
		assertEquals(
				"com.shatteredpixel.shatteredpixeldungeon.levels.towers",
				GothicCastleLevel.class.getPackage().getName());
		assertSame(TowerLevel.class, GothicCastleLevel.class.getSuperclass());
		assertEquals(
				"environment/tiles_gothic_castle.png",
				Assets.Environment.TILES_GOTHIC_CASTLE);
		assertEquals(
				"environment/water_gothic_castle.png",
				Assets.Environment.WATER_GOTHIC_CASTLE);
		assertEquals(
				String.class,
				GothicCastleLevel.class.getDeclaredMethod("tilesTex").getReturnType());
		assertEquals(
				String.class,
				GothicCastleLevel.class.getDeclaredMethod("waterTex").getReturnType());
	}

	@Test
	public void towerFloorsDisplayAsNegativeDepths() {
		assertEquals(-1, Dungeon.displayDepthForLocation(1, 3));
		assertEquals(-2, Dungeon.displayDepthForLocation(2, 3));
		assertEquals(-100, Dungeon.displayDepthForLocation(100, 3));
		assertEquals(16, Dungeon.displayDepthForLocation(16, 0));
	}

	@Test
	public void towerDifficultyUsesCityThroughHallsDepthsAndCapsAtTwentyFive() {
		int originalDepth = Dungeon.depth;
		int originalBranch = Dungeon.branch;
		Hero originalHero = Dungeon.hero;
		try {
			Dungeon.hero = null;
			Dungeon.branch = 3;

			Dungeon.depth = 1;
			assertEquals(16, Dungeon.scalingDepth());
			Dungeon.depth = 5;
			assertEquals(20, Dungeon.scalingDepth());
			Dungeon.depth = 10;
			assertEquals(25, Dungeon.scalingDepth());
			Dungeon.depth = 100;
			assertEquals(25, Dungeon.scalingDepth());
		} finally {
			Dungeon.depth = originalDepth;
			Dungeon.branch = originalBranch;
			Dungeon.hero = originalHero;
		}
	}

	@Test
	public void towerFloorsAreNeverMainDungeonBossFloors() {
		int originalDepth = Dungeon.depth;
		int originalBranch = Dungeon.branch;
		try {
			Dungeon.branch = 0;
			Dungeon.depth = 5;
			assertTrue(Dungeon.bossLevel());

			Dungeon.branch = 3;
			assertFalse(Dungeon.bossLevel());
			Dungeon.depth = 10;
			assertFalse(Dungeon.bossLevel());
		} finally {
			Dungeon.depth = originalDepth;
			Dungeon.branch = originalBranch;
		}
	}

	@Test
	public void highTowerFloorsDoNotCollideWithOtherBranchKeys() {
		assertNotEquals(
				Dungeon.generatedLevelKey(1000, 3),
				Dungeon.generatedLevelKey(2000, 2));
	}
}
