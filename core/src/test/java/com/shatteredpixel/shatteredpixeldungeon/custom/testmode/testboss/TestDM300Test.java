package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDM300Test {

	@Test
	public void superchargeStateUsesParentFields() throws Exception {
		assertEquals(DM300.class, TestDM300.class.getField("supercharged").getDeclaringClass());
		assertEquals(DM300.class, TestDM300.class.getField("pylonsActivated").getDeclaringClass());
		assertEquals(DM300.class, TestDM300.class.getField("chargeAnnounced").getDeclaringClass());
	}

	@Test
	public void invisibleTargetIsRetainedWhileSupercharged() {
		assertFalse(TestDM300.shouldClearEnemy(true, false));
		assertTrue(TestDM300.shouldClearEnemy(false, false));
		assertFalse(TestDM300.shouldClearEnemy(false, true));
	}
}
