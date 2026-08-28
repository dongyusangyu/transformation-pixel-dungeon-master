package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class GamesInProgressCycleTest {

	@Test
	public void filtersNormalAndNewCycleSavesIntoSeparateLists() {
		GamesInProgress.Info normal = new GamesInProgress.Info();
		GamesInProgress.Info newCycle = new GamesInProgress.Info();
		newCycle.newCycle = true;

		ArrayList<GamesInProgress.Info> all =
				new ArrayList<>(Arrays.asList(normal, newCycle));

		ArrayList<GamesInProgress.Info> normalSaves =
				GamesInProgress.filterByCycle(all, false);
		ArrayList<GamesInProgress.Info> newCycleSaves =
				GamesInProgress.filterByCycle(all, true);

		assertEquals(1, normalSaves.size());
		assertSame(normal, normalSaves.get(0));
		assertEquals(1, newCycleSaves.size());
		assertSame(newCycle, newCycleSaves.get(0));
	}

	@Test
	public void oldSaveInfoDefaultsToNormalCycle() {
		GamesInProgress.Info oldSave = new GamesInProgress.Info();

		assertFalse(oldSave.newCycle);
	}

	@Test
	public void saveInfoCopiesCurrentTowerBranchIntoTheListCache() throws IOException {
		String source = readCoreSource("GamesInProgress.java");
		int setStart = source.indexOf("public static void set(int slot)");
		int setEnd = source.indexOf("public static void setUnknown", setStart);

		assertTrue(setStart >= 0 && setEnd > setStart);
		assertTrue(source.substring(setStart, setEnd).contains(
				"info.branch = Dungeon.branch;"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectsDistinctRestartSourcesFromActiveNewCycleSaves() throws Exception {
		Field field = GamesInProgress.class.getDeclaredField("slotStates");
		field.setAccessible(true);
		HashMap<Integer, GamesInProgress.Info> states =
				(HashMap<Integer, GamesInProgress.Info>) field.get(null);
		HashMap<Integer, GamesInProgress.Info> previous = new HashMap<>(states);
		try {
			states.clear();
			for (int slot = 1; slot <= GamesInProgress.MAX_SLOTS; slot++) {
				states.put(slot, null);
			}
			GamesInProgress.Info first = new GamesInProgress.Info();
			first.newCycle = true;
			first.newCycleSourceGameID = "source-a";
			GamesInProgress.Info duplicate = new GamesInProgress.Info();
			duplicate.newCycle = true;
			duplicate.newCycleSourceGameID = "source-a";
			GamesInProgress.Info second = new GamesInProgress.Info();
			second.newCycle = true;
			second.newCycleSourceGameID = "source-b";
			states.put(1, first);
			states.put(2, duplicate);
			states.put(3, second);

			Set<String> sourceIDs = GamesInProgress.activeNewCycleSourceGameIDs();

			assertEquals(2, sourceIDs.size());
			assertTrue(sourceIDs.contains("source-a"));
			assertTrue(sourceIDs.contains("source-b"));
		} finally {
			states.clear();
			states.putAll(previous);
		}
	}

	private static String readCoreSource(String filename) throws IOException {
		Path path = Paths.get("src/main/java/com/shatteredpixel/shatteredpixeldungeon", filename);
		if (!Files.exists(path)) {
			path = Paths.get("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon", filename);
		}
		return Files.readString(path);
	}
}
