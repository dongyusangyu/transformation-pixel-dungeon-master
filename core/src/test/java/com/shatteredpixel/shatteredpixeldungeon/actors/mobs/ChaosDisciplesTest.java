package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChaosDisciplesTest {

	@Test
	public void eyeBossTeleportCandidatesAreFiniteWhenNoCellIsValid() {
		boolean[] heroFOV = {true, true, true, true};
		boolean[] solid = {false, false, false, false};
		boolean[] occupied = {false, false, false, false};
		int[] distance = {0, 1, 2, 3};

		assertTrue(ChaosDisciples.EyeBoss.teleportCandidates(
				heroFOV, solid, occupied, distance).isEmpty());
	}

	@Test
	public void eyeBossTeleportCandidatesOnlyContainHiddenReachableEmptyCells() {
		boolean[] heroFOV = {false, false, false, false, false};
		boolean[] solid = {false, true, false, false, false};
		boolean[] occupied = {false, false, true, false, false};
		int[] distance = {0, 1, 2, Integer.MAX_VALUE, 4};

		ArrayList<Integer> candidates = ChaosDisciples.EyeBoss.teleportCandidates(
				heroFOV, solid, occupied, distance);

		assertEquals(Arrays.asList(0, 4), candidates);
	}
}
