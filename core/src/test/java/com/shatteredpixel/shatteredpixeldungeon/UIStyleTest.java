package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Preferences;
import com.watabou.noosa.ui.Cursor;
import com.watabou.utils.GameSettings;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UIStyleTest {

	private Map<String, Object> values;

	@Before
	public void setUpPreferences() {
		values = new HashMap<>();
		Preferences preferences = (Preferences) Proxy.newProxyInstance(
				Preferences.class.getClassLoader(),
				new Class<?>[]{Preferences.class},
				(proxy, method, args) -> {
					String name = method.getName();
					if (name.startsWith("put")) {
						values.put((String) args[0], args[1]);
						return proxy;
					}
					if (name.equals("getString")) {
						return values.getOrDefault(args[0], args[1]);
					}
					if (name.equals("contains")) {
						return values.containsKey(args[0]);
					}
					if (name.equals("flush")) {
						return null;
					}
					throw new UnsupportedOperationException(name);
				});
		GameSettings.set(preferences);
	}

	@Test
	public void transformationIsTheDefaultStyle() {
		assertEquals(SPDSettings.UIStyle.TRANSFORMATION, SPDSettings.uiStyle());
		assertEquals("interfaces/chrome.png", Assets.Interfaces.chrome());
		assertEquals("gdx/cursor_mouse.png", Cursor.Type.DEFAULT.file);
	}

	@Test
	public void spdStyleUsesSpdAssetDirectories() {
		SPDSettings.uiStyle(SPDSettings.UIStyle.SPD);

		assertEquals(SPDSettings.UIStyle.SPD, SPDSettings.uiStyle());
		assertEquals("SPD", values.get(SPDSettings.KEY_UI_STYLE));
		assertEquals("interfaces/SPD/chrome.png", Assets.Interfaces.chrome());
		assertEquals("interfaces/SPD/toolbar.png", Assets.Interfaces.toolbar());
		assertEquals("gdx/cursor_mouse.png", Cursor.Type.DEFAULT.file);
	}

	@Test
	public void invalidStoredStyleFallsBackToTransformation() {
		values.put(SPDSettings.KEY_UI_STYLE, "UNKNOWN_STYLE");

		assertEquals(SPDSettings.UIStyle.TRANSFORMATION, SPDSettings.uiStyle());
		assertEquals("interfaces/icons.png", Assets.Interfaces.icons());
	}
}
