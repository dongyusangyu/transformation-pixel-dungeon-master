/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Insets;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.provider.Settings;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.PackageTrie;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicBoolean;

import dalvik.system.DexFile;

public class AndroidPlatformSupport extends PlatformSupport {

	static final int IMMERSIVE_SYSTEM_UI_FLAGS =
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

	static final int NON_IMMERSIVE_SYSTEM_UI_FLAGS =
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

	static final int WINDOW_MODE_FLAGS_MASK =
			WindowManager.LayoutParams.FLAG_FULLSCREEN
			| WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;

	static final class SafeNavigationInsets {
		final int left;
		final int right;
		final int bottom;

		SafeNavigationInsets(int left, int right, int bottom) {
			this.left = left;
			this.right = right;
			this.bottom = bottom;
		}
	}

	static final class SystemInsets {
		final int left;
		final int right;
		final int bottom;
		final boolean fullscreen;
		final boolean multiWindow;
		final boolean navigationVisible;
		final boolean imeVisible;

		SystemInsets(int left, int right, int bottom, boolean fullscreen,
				boolean multiWindow, boolean navigationVisible) {
			this(left, right, bottom, fullscreen, multiWindow, navigationVisible, false);
		}

		SystemInsets(int left, int right, int bottom, boolean fullscreen,
				boolean multiWindow, boolean navigationVisible, boolean imeVisible) {
			this.left = left;
			this.right = right;
			this.bottom = bottom;
			this.fullscreen = fullscreen;
			this.multiWindow = multiWindow;
			this.navigationVisible = navigationVisible;
			this.imeVisible = imeVisible;
		}
	}

	private View insetsDecorView;
	private volatile SystemInsets lastSystemInsets;
	private volatile boolean keyboardRequested;
	private final AtomicBoolean insetRefreshPending = new AtomicBoolean();

	static SafeNavigationInsets safeNavigationInsets(int left, int right, int bottom,
			boolean fullscreen, boolean multiWindow, boolean navigationVisible) {
		if (!navigationVisible || multiWindow) {
			return new SafeNavigationInsets(0, 0, 0);
		}
		return new SafeNavigationInsets(Math.max(0, left), Math.max(0, right), Math.max(0, bottom));
	}

	static boolean shouldRelayoutForSystemInsets(SystemInsets previous, SystemInsets current) {
		if (current.imeVisible) return false;
		SafeNavigationInsets currentSafe = safeNavigationInsets(current.left, current.right,
				current.bottom, current.fullscreen, current.multiWindow, current.navigationVisible);
		if (previous == null) {
			return currentSafe.left > 0 || currentSafe.right > 0 || currentSafe.bottom > 0;
		}
		SafeNavigationInsets previousSafe = safeNavigationInsets(previous.left, previous.right,
				previous.bottom, previous.fullscreen, previous.multiWindow, previous.navigationVisible);
		return previousSafe.left != currentSafe.left
				|| previousSafe.right != currentSafe.right
				|| previousSafe.bottom != currentSafe.bottom;
	}

	@SuppressLint("NewApi")
	private SafeNavigationInsets navigationBarInsets(WindowInsets windowInsets) {
		if (windowInsets == null) {
			return new SafeNavigationInsets(0, 0, 0);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars());
			return new SafeNavigationInsets(insets.left, insets.right, insets.bottom);
		}
		return new SafeNavigationInsets(windowInsets.getStableInsetLeft(),
				windowInsets.getStableInsetRight(), windowInsets.getStableInsetBottom());
	}

	private SystemInsets currentSystemInsets(WindowInsets windowInsets) {
		SafeNavigationInsets navigation = navigationBarInsets(windowInsets);
		boolean multiWindow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
				&& AndroidLauncher.instance != null && AndroidLauncher.instance.isInMultiWindowMode();
		return new SystemInsets(navigation.left, navigation.right, navigation.bottom,
				SPDSettings.fullscreen(), multiWindow, navigationBarVisible(windowInsets),
				keyboardRequested || imeVisible(windowInsets));
	}

	@SuppressLint("NewApi")
	private boolean imeVisible(WindowInsets windowInsets) {
		if (windowInsets == null) return false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return windowInsets.isVisible(WindowInsets.Type.ime());
		}
		return windowInsets.getSystemWindowInsetBottom()
				> windowInsets.getStableInsetBottom() + 24;
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean value, boolean multiline) {
		keyboardRequested = value;
		super.setOnscreenKeyboardVisible(value, multiline);
	}

	@SuppressLint("NewApi")
	private boolean navigationBarVisible(WindowInsets windowInsets) {
		if (windowInsets == null) return false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return windowInsets.isVisible(WindowInsets.Type.navigationBars());
		}
		return windowInsets.getSystemWindowInsetLeft() > 0
				|| windowInsets.getSystemWindowInsetRight() > 0
				|| windowInsets.getSystemWindowInsetBottom() > 0;
	}

	public void installWindowInsetsListener() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || AndroidLauncher.instance == null) {
			return;
		}

		AndroidLauncher.instance.runOnUiThread(() -> {
			if (AndroidLauncher.instance == null) {
				return;
			}
			View decor = AndroidLauncher.instance.getWindow().getDecorView();
			if (insetsDecorView != decor) {
				insetsDecorView = decor;
				lastSystemInsets = null;
				decor.setOnApplyWindowInsetsListener((view, insets) -> {
					SystemInsets current = currentSystemInsets(insets);
					if (current.imeVisible) return insets;
					boolean changed = shouldRelayoutForSystemInsets(lastSystemInsets, current);
					lastSystemInsets = current;
					if (changed) {
						requestSafeInsetRelayout();
					}
					return insets;
				});
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
				decor.requestApplyInsets();
			}
		});
	}

	private void requestSafeInsetRelayout() {
		if (Gdx.app == null || Game.width <= 0 || Game.height <= 0
				|| ShatteredPixelDungeon.scene() == null
				|| !insetRefreshPending.compareAndSet(false, true)) {
			return;
		}

		Gdx.app.postRunnable(() -> {
			try {
					if (AndroidLauncher.instance != null
							&& ShatteredPixelDungeon.scene() instanceof PixelScene) {
						((PixelScene) ShatteredPixelDungeon.scene()).onSafeInsetsChanged();
				}
			} finally {
				insetRefreshPending.set(false);
			}
		});
	}

	public PackageTrie findClasses(String pkgName) throws ClassNotFoundException {
		PackageTrie trie = new PackageTrie();
		try {
			Enumeration<String> entries = new DexFile(AndroidLauncher.instance
					.getContext()
					.getPackageCodePath()
			).entries();
			while (entries.hasMoreElements()) {
				String name = entries.nextElement();
				if (name.startsWith(pkgName)) {
					try {
						trie.addPlatformClass(PackageTrie.loadClassWithoutInitialization(
								name, AndroidPlatformSupport.class.getClassLoader()), pkgName);
					} catch (ClassNotFoundException | LinkageError ignored) {
						// Some generated or platform-specific classes cannot be loaded here.
					}
				}
			}
		} catch (IOException e) {
			throw new ClassNotFoundException(pkgName, e);
		}
		return trie;
	}

	@Override
	public boolean supportsFullScreen(){
		// Match upstream behavior: the setting only matters when there is a
		// navigation or gesture bar to hide.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && AndroidLauncher.instance != null) {
			WindowInsets rootInsets = AndroidLauncher.instance.getWindow().getDecorView().getRootWindowInsets();
			SafeNavigationInsets insets = navigationBarInsets(rootInsets);
			return insets.bottom > 0 || insets.right > 0 || insets.left > 0;
		} else {
			return true;
		}
	}

	@Override
	public RectF getDisplayCutout() {
		RectF cutoutRect = new RectF();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && AndroidLauncher.instance != null) {
			WindowInsets rootInsets = AndroidLauncher.instance.getWindow().getDecorView().getRootWindowInsets();
			DisplayCutout cutout = rootInsets == null ? null : rootInsets.getDisplayCutout();

			Rect largest = null;
			if (cutout != null) {
				for (Rect r : cutout.getBoundingRects()) {
					if (largest == null
							|| Math.abs(r.height() * r.width()) > Math.abs(largest.height() * largest.width())) {
						largest = r;
					}
				}
			}

			if (largest != null){
				cutoutRect.left = Math.min(largest.left, largest.right);
				cutoutRect.right = Math.max(largest.left, largest.right);
				cutoutRect.top  = Math.min(largest.top, largest.bottom);
				cutoutRect.bottom  = Math.max(largest.top, largest.bottom);
			}
		}

		return cutoutRect;
	}

	@Override
	public RectF getSafeInsets( int level ) {
		RectF insets = new RectF();

		// Android 9+ exposes stable insets for gesture/nav bars and display cutouts.
		// Older versions are left to the system window handling, matching upstream.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
				&& AndroidLauncher.instance != null
				&& !AndroidLauncher.instance.isInMultiWindowMode()) {
			WindowInsets rootInsets = AndroidLauncher.instance.getWindow().getDecorView().getRootWindowInsets();
			if (rootInsets != null) {

				// Android 11+ reports navigation bars and taskbars through this inset.
				SystemInsets systemInsets = lastSystemInsets == null
						? currentSystemInsets(rootInsets) : lastSystemInsets;
				SafeNavigationInsets safe = safeNavigationInsets(systemInsets.left, systemInsets.right,
						systemInsets.bottom, systemInsets.fullscreen,
						systemInsets.multiWindow, systemInsets.navigationVisible);
				insets.left = Math.max(insets.left, safe.left);
				insets.right = Math.max(insets.right, safe.right);
				insets.bottom = Math.max(insets.bottom, safe.bottom);

				if (level > INSET_BLK) {
					DisplayCutout cutout = rootInsets.getDisplayCutout();

					if (cutout != null) {
						boolean largeCutout = false;
						boolean cutoutsPresent = false;

						int screenSize = Game.width * Game.height;
						for (Rect r : cutout.getBoundingRects()) {
							int cutoutSize = Math.abs(r.height() * r.width());
							if (cutoutSize > 0){
								cutoutsPresent = true;
								if (cutoutSize * 133.33f >= screenSize) {
									largeCutout = true;
								}
							}
						}

						if (!cutoutsPresent){
							largeCutout = true;
						}

						if (largeCutout || level == INSET_ALL) {
							insets.left = Math.max(insets.left, cutout.getSafeInsetLeft());
							insets.top = Math.max(insets.top, cutout.getSafeInsetTop());
							insets.right = Math.max(insets.right, cutout.getSafeInsetRight());
							insets.bottom = Math.max(insets.bottom, cutout.getSafeInsetBottom());
						}
					}
				}
			}
		}
		return insets;
	}
	
	public void updateDisplaySize(){
		if (SPDSettings.landscape() != null) {
			AndroidLauncher.instance.setRequestedOrientation( SPDSettings.landscape() ?
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE :
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT );
		}

		GLSurfaceView view = (GLSurfaceView) ((AndroidGraphics)Gdx.graphics).getView();
		
		if (view.getMeasuredWidth() == 0 || view.getMeasuredHeight() == 0)
			return;
		
		Game.dispWidth = view.getMeasuredWidth();
		Game.dispHeight = view.getMeasuredHeight();

		boolean fullscreen = Build.VERSION.SDK_INT < Build.VERSION_CODES.N
				|| !AndroidLauncher.instance.isInMultiWindowMode();

		if (fullscreen && SPDSettings.landscape() != null
				&& (Game.dispWidth >= Game.dispHeight) != SPDSettings.landscape()){
			int tmp = Game.dispWidth;
			Game.dispWidth = Game.dispHeight;
			Game.dispHeight = tmp;
		}
		
		float dispRatio = Game.dispWidth / (float)Game.dispHeight;
		
		float renderWidth = dispRatio > 1 ? PixelScene.MIN_WIDTH_L : PixelScene.MIN_WIDTH_P;
		float renderHeight = dispRatio > 1 ? PixelScene.MIN_HEIGHT_L : PixelScene.MIN_HEIGHT_P;
		
		//force power saver in this case as all devices must run at at least 2x scale.
		if (Game.dispWidth < renderWidth*2 || Game.dispHeight < renderHeight*2)
			SPDSettings.put( SPDSettings.KEY_POWER_SAVER, true );
		
		if (SPDSettings.powerSaver() && fullscreen){
			
			int maxZoom = (int)Math.min(Game.dispWidth/renderWidth, Game.dispHeight/renderHeight);
			
			renderWidth *= Math.max( 2, Math.round(1f + maxZoom*0.4f));
			renderHeight *= Math.max( 2, Math.round(1f + maxZoom*0.4f));
			
			if (dispRatio > renderWidth / renderHeight){
				renderWidth = renderHeight * dispRatio;
			} else {
				renderHeight = renderWidth / dispRatio;
			}
			
			final int finalW = Math.round(renderWidth);
			final int finalH = Math.round(renderHeight);
			if (finalW != Game.width || finalH != Game.height){
				
				AndroidLauncher.instance.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						view.getHolder().setFixedSize(finalW, finalH);
					}
				});
				
			}
		} else {
			AndroidLauncher.instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					view.getHolder().setSizeFromLayout();
				}
			});
		}
	}
	
	private boolean canUseFullscreen() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.N
				|| !AndroidLauncher.instance.isInMultiWindowMode();
	}

	static int windowModeFlags(boolean fullscreenAvailable) {
		return fullscreenAvailable
				? WindowManager.LayoutParams.FLAG_FULLSCREEN
				: WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
	}

	static int systemUiFlags(boolean hideSystemBars) {
		return hideSystemBars ? IMMERSIVE_SYSTEM_UI_FLAGS : NON_IMMERSIVE_SYSTEM_UI_FLAGS;
	}

	public void updateSystemUI() {
		if (AndroidLauncher.instance == null) {
			return;
		}
		
		AndroidLauncher.instance.runOnUiThread(new Runnable() {
			@SuppressLint("NewApi")
			@Override
			public void run() {
				if (AndroidLauncher.instance == null) {
					return;
				}
				// The IME temporarily changes system bar visibility. Restoring immersive
				// flags while it owns the screen can dismiss the keyboard.
				if (keyboardRequested) return;
				boolean fullscreenAvailable = canUseFullscreen();
				View decor = AndroidLauncher.instance.getWindow().getDecorView();
				
				AndroidLauncher.instance.getWindow().setFlags(
						windowModeFlags(fullscreenAvailable),
						WINDOW_MODE_FLAGS_MASK);
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
					boolean hideSystemBars = supportsFullScreen() && SPDSettings.fullscreen();
					decor.setSystemUiVisibility(systemUiFlags(hideSystemBars));
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
					decor.requestApplyInsets();
				}
			}
		});
		
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean connectedToUnmeteredNetwork() {
		//Returns true if using unmetered connection, use shortcut method if available
		ConnectivityManager cm = (ConnectivityManager) AndroidLauncher.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			return !cm.isActiveNetworkMetered();
		} else {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			return activeNetwork != null && activeNetwork.isConnectedOrConnecting() &&
					(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
					|| activeNetwork.getType() == ConnectivityManager.TYPE_WIMAX
					|| activeNetwork.getType() == ConnectivityManager.TYPE_BLUETOOTH
					|| activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET);
		}
	}

	@Override
	public boolean supportsVibration() {
		return true; //always true on Android
	}

	@Override
	public String cloudDeviceFingerprint() {
		try {
			String androidId = Settings.Secure.getString(AndroidLauncher.instance.getContentResolver(), Settings.Secure.ANDROID_ID);
			if (androidId != null && androidId.length() > 0) {
				return "android:" + androidId;
			}
		} catch (Exception ignored) {
		}
		return super.cloudDeviceFingerprint();
	}

	/* FONT SUPPORT */
	
	//droid sans / roboto, or a custom pixel font, for use with Latin and Cyrillic languages
	private static FreeTypeFontGenerator basicFontGenerator;
	//droid sans / nanum gothic / noto sans, for use with Korean
	private static FreeTypeFontGenerator KRFontGenerator;
	//droid sans / noto sans, for use with Simplified Chinese
	private static FreeTypeFontGenerator SCFontGenerator;
	//droid sans / noto sans, for use with Japanese
	private static FreeTypeFontGenerator JPFontGenerator;
	
	//special logic for handling korean android 6.0 font oddities
	private static boolean koreanAndroid6OTF = false;
	
	@Override
	public void setupFontGenerators(int pageSize, boolean systemfont) {
		//don't bother doing anything if nothing has changed
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont){
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemfont;

		resetGenerators(false);
		fonts = new HashMap<>();
		basicFontGenerator = KRFontGenerator = SCFontGenerator = JPFontGenerator = null;


		if (systemfont && Gdx.files.absolute("/system/fonts/Roboto-Regular.ttf").exists()) {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/Roboto-Regular.ttf"));
		} else if (systemfont && Gdx.files.absolute("/system/fonts/DroidSans.ttf").exists()){
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/DroidSans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
		}


		
		//android 7.0+. all asian fonts are nicely contained in one spot
		if (Gdx.files.absolute("/system/fonts/NotoSansCJK-Regular.ttc").exists()) {
			//typefaces are 0-JP, 1-KR, 2-SC, 3-TC.
			int typeFace;
			switch (SPDSettings.language()) {
				case JAPANESE:
					typeFace = 0;
					break;
				case KOREAN:
					typeFace = 1;
					break;
				case CHI_SMPL:
				default:
					typeFace = 2;
			}

			KRFontGenerator = SCFontGenerator = JPFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/NotoSansCJK-Regular.ttc"), typeFace);
			//KRFontGenerator = SCFontGenerator = JPFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("fonts/pixel_font.ttf"), typeFace);

			//otherwise we have to go over a few possibilities.
		} else {
			
			//Korean font generators
			if (Gdx.files.absolute("/system/fonts/NanumGothic.ttf").exists()){
				KRFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/NanumGothic.ttf"));
			} else if (Gdx.files.absolute("/system/fonts/NotoSansKR-Regular.otf").exists()){
				KRFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/NotoSansKR-Regular.otf"));
				koreanAndroid6OTF = true;
			}
			
			//Chinese font generators
			if (Gdx.files.absolute("/system/fonts/NotoSansSC-Regular.otf").exists()){
				SCFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/NotoSansSC-Regular.otf"));
			} else if (Gdx.files.absolute("/system/fonts/NotoSansHans-Regular.otf").exists()){
				SCFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/NotoSansHans-Regular.otf"));
			}
			
			//Japaneses font generators
			if (Gdx.files.absolute("/system/fonts/NotoSansJP-Regular.otf").exists()){
				JPFontGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/NotoSansJP-Regular.otf"));
			}
			
			//set up a fallback generator for any remaining fonts
			FreeTypeFontGenerator fallbackGenerator;
			if (Gdx.files.absolute("/system/fonts/DroidSansFallback.ttf").exists()){
				fallbackGenerator = new FreeTypeFontGenerator(Gdx.files.absolute("/system/fonts/DroidSansFallback.ttf"));
			} else {
				//no fallback font, just set to null =/
				fallbackGenerator = null;
			}
			
			if (KRFontGenerator == null) KRFontGenerator = fallbackGenerator;
			if (SCFontGenerator == null) SCFontGenerator = fallbackGenerator;
			if (JPFontGenerator == null) JPFontGenerator = fallbackGenerator;
			
		}
		
		if (basicFontGenerator != null) fonts.put(basicFontGenerator, new HashMap<>());
		if (KRFontGenerator != null) fonts.put(KRFontGenerator, new HashMap<>());
		if (SCFontGenerator != null) fonts.put(SCFontGenerator, new HashMap<>());
		if (JPFontGenerator != null) fonts.put(JPFontGenerator, new HashMap<>());
		
		//would be nice to use RGBA4444 to save memory, but this causes problems on some gpus =S
		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}

	private static Matcher KRMatcher = Pattern.compile("\\p{InHangul_Syllables}").matcher("");
	private static Matcher SCMatcher = Pattern.compile("\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}").matcher("");
	private static Matcher JPMatcher = Pattern.compile("\\p{InHiragana}|\\p{InKatakana}").matcher("");

	@Override
	protected FreeTypeFontGenerator getGeneratorForString( String input ){
		if (KRMatcher.reset(input).find()){
			return KRFontGenerator;
		} else if (SCMatcher.reset(input).find()){
			return SCFontGenerator;
		} else if (JPMatcher.reset(input).find()){
			return JPFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}
	
	//splits on newlines, underscores, and chinese/japaneses characters
	private Pattern regularsplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})|" +
					"(?<=\\p{InHalfwidth_and_Fullwidth_Forms})|(?=\\p{InHalfwidth_and_Fullwidth_Forms})");
	
	//additionally splits on words, so that each word can be arranged individually
	private Pattern regularsplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})|" +
					"(?<=\\p{InHalfwidth_and_Fullwidth_Forms})|(?=\\p{InHalfwidth_and_Fullwidth_Forms})");
	
	//splits on each non-hangul character. Needed for weird android 6.0 font files
	private Pattern android6KRSplitter = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|" +
					"(?!\\p{InHangul_Syllables})|(?<!\\p{InHangul_Syllables})");
	
	@Override
	public String[] splitforTextBlock(String text, boolean multiline) {
		if (koreanAndroid6OTF && getGeneratorForString(text) == KRFontGenerator){
			return android6KRSplitter.split(text);
		} else if (multiline) {
			return regularsplitterMultiline.split(text);
		} else {
			return regularsplitter.split(text);
		}
	}
	
}
