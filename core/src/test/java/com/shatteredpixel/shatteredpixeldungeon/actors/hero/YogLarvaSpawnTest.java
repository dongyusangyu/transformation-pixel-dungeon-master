package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class YogLarvaSpawnTest {

	@Test
	public void rejectsHeroCellAndInvalidSpawnCells() {
		assertFalse(Talent.isValidLarvaSpawnCell(42, 42, true, true, false, false));
		assertFalse(Talent.isValidLarvaSpawnCell(42, 43, false, true, false, false));
		assertFalse(Talent.isValidLarvaSpawnCell(42, 43, true, false, true, false));
		assertFalse(Talent.isValidLarvaSpawnCell(42, 43, true, true, false, true));
	}

	@Test
	public void acceptsAnEmptyPassableCellBesideHero() {
		assertTrue(Talent.isValidLarvaSpawnCell(42, 43, true, true, false, false));
	}
}
