package com.shatteredpixel.shatteredpixeldungeon.testutil;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;

/** Supplies sheet dimensions to tests that load item classes without a libGDX renderer. */
public final class HeadlessItemSprites implements AutoCloseable {
	private final Map<Object, SmartTexture> cache;
	private final SmartTexture oldItems;
	private final SmartTexture oldIcons;
	private final SmartTexture oldTextIcons;
	private final SmartTexture oldPixel;
	private final Map<Object, SmartTexture> extraSheets = new HashMap<>();

	@SuppressWarnings("unchecked")
	public HeadlessItemSprites() {
		try {
			Field field = TextureCache.class.getDeclaredField("all");
			field.setAccessible(true);
			cache = (Map<Object, SmartTexture>) field.get(null);
			oldItems = cache.put(Assets.Sprites.ITEMS, sheet(256, 1024));
			oldIcons = cache.put(Assets.Sprites.ITEM_ICONS, sheet(256, 256));
			oldTextIcons = cache.put(Assets.Effects.TEXT_ICONS, sheet(256, 256));
			oldPixel = cache.put("1x1:-1", sheet(1, 1));
		} catch (Exception error) {
			throw new AssertionError(error);
		}
	}

	private static SmartTexture sheet(int width, int height) throws Exception {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		SmartTexture sheet = (SmartTexture) ((Unsafe) field.get(null)).allocateInstance(SmartTexture.class);
		sheet.width = width;
		sheet.height = height;
		return sheet;
	}

	public void addSheet(Object key, int width, int height) {
		try {
			if (!extraSheets.containsKey(key)) extraSheets.put(key, cache.get(key));
			cache.put(key, sheet(width, height));
		} catch (Exception error) {
			throw new AssertionError(error);
		}
	}

	@Override public void close() {
		for (Map.Entry<Object, SmartTexture> entry : extraSheets.entrySet()) {
			if (entry.getValue() == null) cache.remove(entry.getKey());
			else cache.put(entry.getKey(), entry.getValue());
		}
		if (oldItems == null) cache.remove(Assets.Sprites.ITEMS);
		else cache.put(Assets.Sprites.ITEMS, oldItems);
		if (oldIcons == null) cache.remove(Assets.Sprites.ITEM_ICONS);
		else cache.put(Assets.Sprites.ITEM_ICONS, oldIcons);
		if (oldTextIcons == null) cache.remove(Assets.Effects.TEXT_ICONS);
		else cache.put(Assets.Effects.TEXT_ICONS, oldTextIcons);
		if (oldPixel == null) cache.remove("1x1:-1");
		else cache.put("1x1:-1", oldPixel);
	}
}
