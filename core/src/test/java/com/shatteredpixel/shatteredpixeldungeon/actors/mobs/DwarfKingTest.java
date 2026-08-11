package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DwarfKingTest {

	@Test
	public void phaseThreeAttackIsSynchronousOnlyWhenAnimationsAreDisabled() {
		assertTrue(DwarfKing.shouldResolveAttackSynchronously(3, false));
		assertFalse(DwarfKing.shouldResolveAttackSynchronously(3, true));
		assertFalse(DwarfKing.shouldResolveAttackSynchronously(1, false));
		assertFalse(DwarfKing.shouldResolveAttackSynchronously(2, false));
	}
}
