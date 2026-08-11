package com.shatteredpixel.shatteredpixeldungeon.android;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AndroidMissingNativesHandlerTest {

	@Test
	public void narrowScreensUseVerticalActionButtons() {
		assertTrue(AndroidMissingNativesHandler.useVerticalActionButtons(320));
		assertFalse(AndroidMissingNativesHandler.useVerticalActionButtons(360));
	}

	@Test
	public void reportImageHeightIsBoundedByScreenAndDensity() {
		assertEquals(240, AndroidMissingNativesHandler.reportImageMaxHeight(600, 1f));
		assertEquals(640, AndroidMissingNativesHandler.reportImageMaxHeight(2400, 2f));
	}
}
