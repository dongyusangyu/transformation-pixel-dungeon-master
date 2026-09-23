package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RoastSheepTrapTest {

	@Test
	public void usesOrangeCrosshairAndNormalHiddenTrapRules() {
		RoastSheepTrap trap = new RoastSheepTrap();

		assertEquals(Trap.ORANGE, trap.color);
		assertEquals(Trap.CROSSHAIR, trap.shape);
		assertFalse(trap.preservesTerrain());
		assertFalse(trap.triggersOnEntry());
		assertTrue(trap.canBeHidden);
		assertTrue(trap.canBeSearched);
	}

	@Test
	public void flockAreaMatchesTheFiveByFiveRadiusTwoFlockPattern() {
		assertTrue(RoastSheepTrap.isInFlockArea(0, 0));
		assertTrue(RoastSheepTrap.isInFlockArea(2, 0));
		assertTrue(RoastSheepTrap.isInFlockArea(-1, 1));
		assertFalse(RoastSheepTrap.isInFlockArea(2, 1));
		assertFalse(RoastSheepTrap.isInFlockArea(-2, -1));
	}

	@Test
	public void sheepOnlySpawnOnOpenUnoccupiedNonPitCells() {
		assertTrue(RoastSheepTrap.canSpawnSheep(true, false, false, false));
		assertFalse(RoastSheepTrap.canSpawnSheep(false, false, false, false));
		assertFalse(RoastSheepTrap.canSpawnSheep(true, true, false, false));
		assertFalse(RoastSheepTrap.canSpawnSheep(true, false, true, false));
		assertFalse(RoastSheepTrap.canSpawnSheep(true, false, false, true));
	}
}
