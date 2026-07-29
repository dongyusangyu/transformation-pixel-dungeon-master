package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.watabou.utils.Bundle;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestAlignmentArenaTrackerTest {

	private static final String TRACKER_CLASS =
			"com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator"
					+ ".TestAlignment$ArenaReturnTracker";

	@Test
	public void persistsArenaReturnPoint() throws Exception {
		Class<?> trackerClass = Class.forName(TRACKER_CLASS);
		Object tracker = trackerClass.getDeclaredConstructor().newInstance();
		Method set = trackerClass.getMethod(
				"setReturnPoint", int.class, int.class, int.class);
		Method store = trackerClass.getMethod("storeInBundle", Bundle.class);
		Method restore = trackerClass.getMethod("restoreFromBundle", Bundle.class);

		set.invoke(tracker, 12, 1, 345);
		Bundle bundle = new Bundle();
		store.invoke(tracker, bundle);

		Object restored = trackerClass.getDeclaredConstructor().newInstance();
		restore.invoke(restored, bundle);

		assertTrue((Boolean) trackerClass.getMethod("hasReturnPoint").invoke(restored));
		assertEquals(12, trackerClass.getMethod("returnDepth").invoke(restored));
		assertEquals(1, trackerClass.getMethod("returnBranch").invoke(restored));
		assertEquals(345, trackerClass.getMethod("returnPos").invoke(restored));
	}

	@Test
	public void missingReturnDataRemainsInvalid() throws Exception {
		Class<?> trackerClass = Class.forName(TRACKER_CLASS);
		Object tracker = trackerClass.getDeclaredConstructor().newInstance();

		trackerClass.getMethod("restoreFromBundle", Bundle.class)
				.invoke(tracker, new Bundle());

		assertFalse((Boolean) trackerClass.getMethod("hasReturnPoint").invoke(tracker));
	}
}
