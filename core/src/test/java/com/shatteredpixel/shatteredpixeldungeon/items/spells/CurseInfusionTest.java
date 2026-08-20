package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CurseInfusionTest {

	@Test
	public void randomModeRerollsAnExistingCursedAffixOnFirstInfusion() {
		assertTrue(CurseInfusion.shouldRerollExistingAffix(true, false, false));
	}

	@Test
	public void normalModeKeepsAnExistingCursedAffixOnFirstInfusion() {
		assertFalse(CurseInfusion.shouldRerollExistingAffix(false, false, false));
	}

	@Test
	public void goodAffixesAreRerolledInBothModes() {
		assertTrue(CurseInfusion.shouldRerollExistingAffix(false, true, false));
		assertTrue(CurseInfusion.shouldRerollExistingAffix(true, true, false));
	}

	@Test
	public void previouslyInfusedAffixesAreRerolledInBothModes() {
		assertTrue(CurseInfusion.shouldRerollExistingAffix(false, false, true));
		assertTrue(CurseInfusion.shouldRerollExistingAffix(true, false, true));
	}
}
