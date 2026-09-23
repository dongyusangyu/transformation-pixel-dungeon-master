package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DwarfKingTest {

	@Test
	public void attackIsSynchronousWhenAnimationsAreDisabled() {
		assertTrue(DwarfKing.shouldResolveAttackSynchronously(false));
		assertFalse(DwarfKing.shouldResolveAttackSynchronously(true));
	}

	@Test
	public void visualCallbackPathsAreSkippedWhenAnimationsAreDisabled() {
		assertFalse(DwarfKing.shouldUseAnimationCallbacks(false));
		assertTrue(DwarfKing.shouldUseAnimationCallbacks(true));
	}
}
