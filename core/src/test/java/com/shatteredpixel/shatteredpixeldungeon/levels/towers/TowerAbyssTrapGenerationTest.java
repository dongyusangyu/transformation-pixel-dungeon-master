package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AbyssExplosiveTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ExplosiveTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.utils.SparseArray;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TowerAbyssTrapGenerationTest {

	@Test
	public void skyIslandGeneratesFiveToTenAbyssTraps() {
		assertEquals(5, TowerGenerationRules.abyssTrapCount(Level.Feeling.SKY_ISLAND, 0));
		assertEquals(10, TowerGenerationRules.abyssTrapCount(Level.Feeling.SKY_ISLAND, 5));
	}

	@Test
	public void otherTowerFloorsGenerateAtMostTwoAbyssTraps() {
		assertEquals(0, TowerGenerationRules.abyssTrapCount(Level.Feeling.NONE, 0));
		assertEquals(2, TowerGenerationRules.abyssTrapCount(Level.Feeling.CHASM, 2));
	}

	@Test
	public void abyssTrapCellsOnlyUseUnoccupiedInteriorChasms() {
		TestLevel level = new TestLevel();
		level.setSize(8, 8);
		Arrays.fill(level.map, Terrain.WALL);
		level.transitions = new ArrayList<>();
		level.traps = new SparseArray<>();

		level.map[0] = Terrain.CHASM;
		level.map[9] = Terrain.CHASM;
		level.map[17] = Terrain.CHASM;
		level.map[27] = Terrain.CHASM;
		level.map[10] = Terrain.EMPTY;
		level.map[18] = Terrain.EMPTY;
		level.traps.put(17, new ExplosiveTrap().set(17));

		ArrayList<Integer> cells = TowerGenerationRules.abyssTrapCells(level, 10);

		assertEquals(2, cells.size());
		assertTrue(cells.contains(9));
		assertTrue(cells.contains(27));
		assertFalse(cells.contains(0));
		assertFalse(cells.contains(17));
	}

	@Test
	public void transitionLookupUsesLevelBeingGeneratedWhenGlobalLevelIsNull() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = levelWithTransition(8, 8, 18);
			Dungeon.level = null;

			assertSame(level.transitions.get(0), level.getTransition(18));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void transitionLookupDoesNotUseAnotherGlobalLevelsCoordinates() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = levelWithTransition(8, 8, 18);
			TestLevel otherLevel = new TestLevel();
			otherLevel.setSize(5, 5);
			Dungeon.level = otherLevel;

			assertSame(level.transitions.get(0), level.getTransition(18));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void abyssTrapCellsExcludeTransitionsDuringLevelGeneration() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = levelWithTransition(8, 8, 18);
			Arrays.fill(level.map, Terrain.WALL);
			level.traps = new SparseArray<>();
			level.map[18] = Terrain.CHASM;
			level.map[27] = Terrain.CHASM;
			Dungeon.level = null;

			ArrayList<Integer> cells = TowerGenerationRules.abyssTrapCells(level, 10);

			assertEquals(1, cells.size());
			assertTrue(cells.contains(27));
			assertFalse(cells.contains(18));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void placingSkyIslandTrapsKeepsChasmsAndMixesVisibility() {
		TestLevel level = new TestLevel();
		level.feeling = Level.Feeling.SKY_ISLAND;
		level.setSize(12, 12);
		level.transitions = new ArrayList<>();
		level.traps = new SparseArray<>();

		TowerGenerationRules.placeAbyssTraps(level, level.feeling, 0);

		assertEquals(5, level.traps.valueList().size());
		int visible = 0;
		int hidden = 0;
		for (Trap trap : level.traps.valueList()) {
			assertTrue(trap instanceof AbyssExplosiveTrap);
			assertEquals(Terrain.CHASM, level.map[trap.pos]);
			if (trap.visible) visible++;
			else hidden++;
		}
		assertTrue(visible > 0);
		assertTrue(hidden > 0);
	}

	@Test
	public void towerLevelBuildPlacesAbyssTraps() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java");
		String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);

		assertTrue(source.contains("\t\tplaceAbyssTraps();"));
	}

	private static TestLevel levelWithTransition(int width, int height, int cell) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		level.transitions = new ArrayList<>();
		level.transitions.add(new LevelTransition(level, cell,
				LevelTransition.Type.REGULAR_ENTRANCE, 0, 0,
				LevelTransition.Type.REGULAR_EXIT));
		return level;
	}

	private static class TestLevel extends Level {
		@Override protected boolean build() { return true; }
		@Override protected void createMobs() {}
		@Override protected void createItems() {}
	}
}
