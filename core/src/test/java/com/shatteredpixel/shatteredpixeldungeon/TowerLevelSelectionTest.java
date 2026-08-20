package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.AstralLibraryLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.FrostArchiveLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GothicCastleLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.SkyAlchemyGreenhouseLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerCoreGearworksLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	public void dungeonDispatchesOnlyTowerBranchFloorsToTowerStyles() {
		assertNull(Dungeon.towerLevelClassForLocation(0, 3));
		assertNull(Dungeon.towerLevelClassForLocation(1, 0));
		assertNull(Dungeon.towerLevelClassForLocation(30, 2));
	}

	@Test
	public void everyFifthTowerFloorUsesTheCommonBossLevel() {
		assertSame(TowerBossLevel.class,
				Dungeon.towerLevelClassForLocation(5, TowerLevel.BRANCH));
		assertSame(TowerBossLevel.class,
				Dungeon.towerLevelClassForLocation(10, TowerLevel.BRANCH));
		assertSame(TowerBossLevel.class,
				Dungeon.towerLevelClassForLocation(15, TowerLevel.BRANCH));

		assertNotEquals(TowerBossLevel.class,
				Dungeon.towerLevelClassForLocation(4, TowerLevel.BRANCH));
		assertNotEquals(TowerBossLevel.class,
				Dungeon.towerLevelClassForLocation(6, TowerLevel.BRANCH));
		assertNull(Dungeon.towerLevelClassForLocation(5, 0));
	}

	@Test
	public void bossDispatchStillExposesItsScheduledEnvironmentClass() {
		Class<? extends TowerLevel> firstFloor =
				Dungeon.towerStyleLevelClassForLocation(1, TowerLevel.BRANCH);
		Class<? extends TowerLevel> fifthFloor =
				Dungeon.towerStyleLevelClassForLocation(5, TowerLevel.BRANCH);

		assertSame(firstFloor, fifthFloor);
		assertNotEquals(TowerBossLevel.class, fifthFloor);
		assertNull(Dungeon.towerStyleLevelClassForLocation(5, 0));
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
	public void newTowerLevelsProvideTheirOwnEnvironmentTextures() {
		assertSame(TowerLevel.class, AstralLibraryLevel.class.getSuperclass());
		assertEquals("environment/tiles_astral_library.png",
				Assets.Environment.TILES_ASTRAL_LIBRARY);
		assertEquals("environment/water_astral_library.png",
				Assets.Environment.WATER_ASTRAL_LIBRARY);

		assertSame(TowerLevel.class, TowerCoreGearworksLevel.class.getSuperclass());
		assertEquals("environment/tiles_tower_core_gearworks.png",
				Assets.Environment.TILES_TOWER_CORE_GEARWORKS);
		assertEquals("environment/water_tower_core_gearworks.png",
				Assets.Environment.WATER_TOWER_CORE_GEARWORKS);

		assertSame(TowerLevel.class, SkyAlchemyGreenhouseLevel.class.getSuperclass());
		assertEquals("environment/tiles_sky_alchemy_greenhouse.png",
				Assets.Environment.TILES_SKY_ALCHEMY_GREENHOUSE);
		assertEquals("environment/water_sky_alchemy_greenhouse.png",
				Assets.Environment.WATER_SKY_ALCHEMY_GREENHOUSE);

		assertSame(TowerLevel.class, FrostArchiveLevel.class.getSuperclass());
		assertEquals("environment/tiles_frost_archive.png",
				Assets.Environment.TILES_FROST_ARCHIVE);
		assertEquals("environment/water_frost_archive.png",
				Assets.Environment.WATER_FROST_ARCHIVE);
	}

	@Test
	public void towerFloorsDisplayAsNegativeDepths() {
		assertEquals(-1, Dungeon.displayDepthForLocation(1, 3));
		assertEquals(-2, Dungeon.displayDepthForLocation(2, 3));
		assertEquals(-100, Dungeon.displayDepthForLocation(100, 3));
		assertEquals(16, Dungeon.displayDepthForLocation(16, 0));
	}

	@Test
	public void towerFloorsUseTowerLabelsInTheUi() {
		assertEquals("T1", Dungeon.displayDepthLabel(1, TowerLevel.BRANCH));
		assertEquals("T2", Dungeon.displayDepthLabel(2, TowerLevel.BRANCH));
		assertEquals("T100", Dungeon.displayDepthLabel(100, TowerLevel.BRANCH));
		assertEquals("0", Dungeon.displayDepthLabel(0, 0));
		assertEquals("16", Dungeon.displayDepthLabel(16, 0));
	}

	@Test
	public void fallingMovesDownTheTowerInsteadOfUpIt() {
		assertEquals(2, Dungeon.fallDepthForLocation(3, TowerLevel.BRANCH));
		assertEquals(1, Dungeon.fallDepthForLocation(2, TowerLevel.BRANCH));
		assertEquals(1, Dungeon.fallDepthForLocation(1, TowerLevel.BRANCH));
		assertEquals(4, Dungeon.fallDepthForLocation(3, 0));
	}

	@Test
	public void towerEntryIsOneWayFromTheSurface() {
		assertFalse(Dungeon.towerTransitionAllowed(1, TowerLevel.BRANCH, 0, 0));
		assertTrue(Dungeon.towerTransitionAllowed(0, 0, 1, TowerLevel.BRANCH));
		assertTrue(Dungeon.towerTransitionAllowed(2, TowerLevel.BRANCH,
				1, TowerLevel.BRANCH));
		assertTrue(Dungeon.towerTransitionAllowed(1, TowerLevel.BRANCH,
				2, TowerLevel.BRANCH));
	}

	@Test
	public void towerFloorsCanContinuePastTheMainDungeonEnd() {
		assertTrue(Dungeon.levelTransitionAllowed(
				26, TowerLevel.BRANCH, LevelTransition.Type.REGULAR_EXIT));
		assertTrue(Dungeon.levelTransitionAllowed(
				100, TowerLevel.BRANCH, LevelTransition.Type.REGULAR_EXIT));
		assertTrue(Dungeon.levelTransitionAllowed(
				100, TowerLevel.BRANCH, LevelTransition.Type.REGULAR_ENTRANCE));
		assertTrue(Dungeon.levelTransitionAllowed(
				101, TowerLevel.BRANCH, LevelTransition.Type.REGULAR_EXIT));
		assertTrue(Dungeon.levelTransitionAllowed(
				1_000_001, TowerLevel.BRANCH, LevelTransition.Type.REGULAR_EXIT));
	}

	@Test
	public void heroUsesCentralTransitionPolicyForTowerExits() throws Exception {
		String source = readCoreSource("actors/hero/Hero.java");

		assertTrue(source.contains("Dungeon.levelTransitionAllowed("));
		assertFalse(source.contains(
				"Dungeon.depth < 26 || Dungeon.level.getTransition(cell).type"));
	}

	@Test
	public void mainDungeonKeepsItsExistingEndFloorRule() {
		assertFalse(Dungeon.levelTransitionAllowed(
				26, 0, LevelTransition.Type.REGULAR_EXIT));
		assertTrue(Dungeon.levelTransitionAllowed(
				26, 0, LevelTransition.Type.REGULAR_ENTRANCE));
		assertFalse(Dungeon.levelTransitionAllowed(
				26, 1, LevelTransition.Type.REGULAR_EXIT));
	}

	@Test
	public void towerDifficultyAlwaysUsesDepthThirty() {
		int originalDepth = Dungeon.depth;
		int originalBranch = Dungeon.branch;
		Hero originalHero = Dungeon.hero;
		try {
			Dungeon.hero = null;
			Dungeon.branch = 3;

			Dungeon.depth = 1;
			assertEquals(30, Dungeon.scalingDepth());
			Dungeon.depth = 5;
			assertEquals(30, Dungeon.scalingDepth());
			Dungeon.depth = 10;
			assertEquals(30, Dungeon.scalingDepth());
			Dungeon.depth = 100;
			assertEquals(30, Dungeon.scalingDepth());

			Dungeon.branch = 0;
			Dungeon.depth = 24;
			assertEquals(24, Dungeon.scalingDepth());
			Dungeon.branch = 2;
			Dungeon.depth = 7;
			assertEquals(7, Dungeon.scalingDepth());
		} finally {
			Dungeon.depth = originalDepth;
			Dungeon.branch = originalBranch;
			Dungeon.hero = originalHero;
		}
	}

	@Test
	public void depthScaledMonstersUseTowerDifficultyInsteadOfTowerFloorNumber() {
		int originalDepth = Dungeon.depth;
		int originalBranch = Dungeon.branch;
		Hero originalHero = Dungeon.hero;
		try {
			Dungeon.hero = null;
			Dungeon.branch = TowerLevel.BRANCH;
			Dungeon.depth = 1;

			Piranha piranha = new Piranha();
			assertEquals(160, piranha.HT);
			assertEquals(70, piranha.defenseSkill);
			assertEquals(80, piranha.attackSkill(null));
		} finally {
			Dungeon.depth = originalDepth;
			Dungeon.branch = originalBranch;
			Dungeon.hero = originalHero;
		}
	}

	@Test
	public void towerBossFloorsJoinTheSharedBossFloorClassification() {
		int originalDepth = Dungeon.depth;
		int originalBranch = Dungeon.branch;
		try {
			Dungeon.branch = 0;
			Dungeon.depth = 5;
			assertTrue(Dungeon.bossLevel());

			Dungeon.branch = TowerLevel.BRANCH;
			assertTrue(Dungeon.bossLevel());
			Dungeon.depth = 6;
			assertFalse(Dungeon.bossLevel());
			Dungeon.depth = 10;
			assertTrue(Dungeon.bossLevel());

			assertTrue(Dungeon.bossLevel(15, TowerLevel.BRANCH));
			assertFalse(Dungeon.bossLevel(14, TowerLevel.BRANCH));
			assertFalse(Dungeon.bossLevel(5, 2));
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

	private static String readCoreSource(String relativePath) throws Exception {
		Path coreDirectory = Paths.get(System.getProperty("user.dir"));
		if (!Files.isDirectory(coreDirectory.resolve("src/main/java"))) {
			coreDirectory = coreDirectory.resolve("core");
		}
		return new String(Files.readAllBytes(coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon")
				.resolve(relativePath)), StandardCharsets.UTF_8);
	}
}
