package com.shatteredpixel.shatteredpixeldungeon.windows;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WndTabbedLayoutTest {

	@Test
	public void centeredCameraOriginIncludesSafeInsetAndDesktopOffset() {
		assertEquals(178, WndTabbed.centeredCameraOrigin(8, 400, 120, 20, 1.5f));
		assertEquals(170, WndTabbed.centeredCameraOrigin(0, 400, 120, 20, 1.5f));
	}
}
