package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

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
}
