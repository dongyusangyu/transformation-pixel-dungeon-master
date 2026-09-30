package com.shatteredpixel.shatteredpixeldungeon.testutil;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.utils.GameSettings;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/** Real localization bundles and in-memory preferences without a desktop renderer. */
public final class HeadlessGameMessages implements AutoCloseable {
    private final Application oldApp = Gdx.app;
    private final Files oldFiles = Gdx.files;
    private final Preferences oldPreferences;
    private final Languages oldLanguage;
    private final String oldVersion = Game.version;
    private final Map<String, Object> values = new HashMap<>();

    public HeadlessGameMessages() throws Exception {
        Game.version = "0.3.0";
        Field field = GameSettings.class.getDeclaredField("prefs");
        field.setAccessible(true);
        oldPreferences = (Preferences) field.get(null);
        Preferences preferences = (Preferences) Proxy.newProxyInstance(
                Preferences.class.getClassLoader(), new Class<?>[]{Preferences.class}, (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("get") && args != null && args.length == 2) {
                        return values.getOrDefault(args[0], args[1]);
                    }
                    if (name.startsWith("put") && args != null && args.length == 2) values.put((String) args[0], args[1]);
                    if (name.equals("contains")) return values.containsKey(args[0]);
                    if (method.getReturnType() == Preferences.class) return proxy;
                    if (method.getReturnType() == boolean.class) return false;
                    return null;
                });
        GameSettings.set(preferences);
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class<?>[]{Application.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getType")) return Application.ApplicationType.Desktop;
                    if (method.getName().equals("getPreferences")) return preferences;
                    return null;
                });
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class<?>[]{Files.class}, (proxy, method, args) -> {
                    File root = new File("src/main/assets");
                    if (!root.isDirectory()) root = new File("core/src/main/assets");
                    return new FileHandle(new File(root, (String) args[0]));
                });
        oldLanguage = Messages.lang();
        Messages.setup(Languages.CHI_SMPL);
    }

    @Override public void close() {
        Messages.setup(oldLanguage);
        Gdx.app = oldApp;
        Gdx.files = oldFiles;
        GameSettings.set(oldPreferences);
        Game.version = oldVersion;
    }
}
