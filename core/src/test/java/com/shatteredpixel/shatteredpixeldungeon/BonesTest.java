package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BonesTest {

	@After
	public void resetChallenges() {
		Dungeon.challenges = 0;
	}

	@Test
	public void testModeDoesNotLeaveTransferableItems() {
		Dungeon.challenges = Challenges.TEST_MODE;

		assertFalse(Bones.leavesTransferableItem());
	}

	@Test
	public void redEnvelopeDoesNotLeaveTransferableItems() {
		Dungeon.challenges = Challenges.RED_ENVELOPE;

		assertFalse(Bones.leavesTransferableItem());
	}

	@Test
	public void otherChallengesStillLeaveTransferableItems() {
		Dungeon.challenges = Challenges.NO_FOOD;

		assertTrue(Bones.leavesTransferableItem());
	}

	@Test
	public void normalRunLeavesTransferableItems() {
		assertTrue(Bones.leavesTransferableItem());
	}
}
