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
	public void visibleNavigationAndTaskbarInsetsAreReservedForGameControls() {
		AndroidPlatformSupport.SafeNavigationInsets insets =
				AndroidPlatformSupport.safeNavigationInsets(18, 0, 96, false, false, true);

		assertEquals(18, insets.left);
		assertEquals(96, insets.bottom);
	}

	@Test
	public void hiddenNavigationBarsDoNotReserveObsoleteInsets() {
		AndroidPlatformSupport.SafeNavigationInsets insets =
				AndroidPlatformSupport.safeNavigationInsets(18, 0, 96, true, false, false);

		assertEquals(0, insets.left);
		assertEquals(0, insets.bottom);
	}

	@Test
	public void visibleNavigationIsReservedEvenIfFullscreenWasRequested() {
		AndroidPlatformSupport.SafeNavigationInsets insets =
				AndroidPlatformSupport.safeNavigationInsets(0, 0, 96, true, false, true);

		assertEquals(96, insets.bottom);
	}

	@Test
	public void systemInsetChangesAndNavigationModeChangesRequestRelayout() {
		assertFalse(AndroidPlatformSupport.shouldRelayoutForSystemInsets(null,
				new AndroidPlatformSupport.SystemInsets(0, 0, 0, false, false, true)));
		assertTrue(AndroidPlatformSupport.shouldRelayoutForSystemInsets(null,
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, false, false, true)));
		assertTrue(AndroidPlatformSupport.shouldRelayoutForSystemInsets(
				new AndroidPlatformSupport.SystemInsets(0, 0, 64, false, false, true),
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, false, false, true)));
		assertTrue(AndroidPlatformSupport.shouldRelayoutForSystemInsets(
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, false, false, true),
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, true, false, false)));
		assertTrue(AndroidPlatformSupport.shouldRelayoutForSystemInsets(
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, false, false, true),
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, false, true, true)));
		assertFalse(AndroidPlatformSupport.shouldRelayoutForSystemInsets(
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, false, false, true),
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, false, false, true)));
		assertFalse(AndroidPlatformSupport.shouldRelayoutForSystemInsets(
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, true, false, false),
				new AndroidPlatformSupport.SystemInsets(0, 0, 144, true, false, false)));
	}

	@Test
	public void keyboardInsetsDoNotTriggerNavigationRelayout() {
		AndroidPlatformSupport.SystemInsets before =
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, true, false, false);
		AndroidPlatformSupport.SystemInsets keyboardVisible =
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, true, false, true, true);
		assertFalse(AndroidPlatformSupport.shouldRelayoutForSystemInsets(before, keyboardVisible));
		assertTrue(AndroidPlatformSupport.shouldRelayoutForSystemInsets(before,
				new AndroidPlatformSupport.SystemInsets(0, 0, 96, true, false, true)));
	}

	@Test
	public void windowFocusLossDoesNotRefreshSystemUI() {
		assertTrue(AndroidLauncher.shouldUpdateSystemUIOnWindowFocusChange(true));
		assertFalse(AndroidLauncher.shouldUpdateSystemUIOnWindowFocusChange(false));
	}
}
