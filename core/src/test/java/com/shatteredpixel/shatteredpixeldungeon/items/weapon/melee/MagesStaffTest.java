package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MagesStaffTest {

	@Test
	public void playerImbueIdentifiesWandTypeInRandomMode() {
		assertTrue(MagesStaff.shouldIdentifyWandTypeOnImbue(true, true));
	}

	@Test
	public void imbueDoesNotIdentifyTypeOutsideRandomMode() {
		assertFalse(MagesStaff.shouldIdentifyWandTypeOnImbue(false, true));
	}

	@Test
	public void internalImbueDoesNotIdentifyType() {
		assertFalse(MagesStaff.shouldIdentifyWandTypeOnImbue(true, false));
	}
}
