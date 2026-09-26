package com.shatteredpixel.shatteredpixeldungeon.testutil;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;

/** Supplies sheet dimensions to tests that load item classes without a libGDX renderer. */
public final class HeadlessItemSprites implements AutoCloseable {
	private final Map<Object, SmartTexture> cache;
	private final SmartTexture oldItems;
	private final SmartTexture oldIcons;
	private final SmartTexture oldTextIcons;

	@SuppressWarnings("unchecked")
	public HeadlessItemSprites() {
		try {
			Field field = TextureCache.class.getDeclaredField("all");
			field.setAccessible(true);
			cache = (Map<Object, SmartTexture>) field.get(null);
			oldItems = cache.put(Assets.Sprites.ITEMS, sheet(256, 1024));
			oldIcons = cache.put(Assets.Sprites.ITEM_ICONS, sheet(256, 256));
			oldTextIcons = cache.put(Assets.Effects.TEXT_ICONS, sheet(256, 256));
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

	@Override public void close() {
		if (oldItems == null) cache.remove(Assets.Sprites.ITEMS);
		else cache.put(Assets.Sprites.ITEMS, oldItems);
		if (oldIcons == null) cache.remove(Assets.Sprites.ITEM_ICONS);
		else cache.put(Assets.Sprites.ITEM_ICONS, oldIcons);
		if (oldTextIcons == null) cache.remove(Assets.Effects.TEXT_ICONS);
		else cache.put(Assets.Effects.TEXT_ICONS, oldTextIcons);
	}
}
