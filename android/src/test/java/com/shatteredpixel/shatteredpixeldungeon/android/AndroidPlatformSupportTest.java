package com.shatteredpixel.shatteredpixeldungeon.android;

import android.view.View;
import android.view.WindowManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AndroidPlatformSupportTest {

	@Test
	public void windowModeMatchesUpstreamFullscreenPolicy() {
		assertEquals(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				AndroidPlatformSupport.windowModeFlags(true));
		assertEquals(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				AndroidPlatformSupport.windowModeFlags(false));
		assertEquals(WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				AndroidPlatformSupport.WINDOW_MODE_FLAGS_MASK);
	}

	@Test
	public void nonImmersiveModeStillUsesFullscreenLayoutWithoutHidingNavigation() {
		int flags = AndroidPlatformSupport.systemUiFlags(false);

		assertEquals(AndroidPlatformSupport.NON_IMMERSIVE_SYSTEM_UI_FLAGS, flags);
		assertTrue((flags & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0);
		assertTrue((flags & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0);
		assertFalse((flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0);
		assertFalse((flags & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0);
	}

	@Test
	public void immersiveModeHidesNavigationAndStatusBars() {
		int flags = AndroidPlatformSupport.systemUiFlags(true);

		assertEquals(AndroidPlatformSupport.IMMERSIVE_SYSTEM_UI_FLAGS, flags);
		assertTrue((flags & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0);
		assertTrue((flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0);
		assertTrue((flags & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0);
	}

	@Test
	public void windowFocusLossDoesNotRefreshSystemUI() {
		assertTrue(AndroidLauncher.shouldUpdateSystemUIOnWindowFocusChange(true));
		assertFalse(AndroidLauncher.shouldUpdateSystemUIOnWindowFocusChange(false));
	}
}
