package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import java.util.Calendar;
import java.util.GregorianCalendar;

public final class SurfaceSeason {

	enum Theme {
		LUSH,
		WINTER
	}

	private static Theme current = Theme.LUSH;

	private SurfaceSeason() {
	}

	public static void initializeFromSystemDate() {
		Calendar calendar = GregorianCalendar.getInstance();
		initializeForMonth(calendar.get(Calendar.MONTH) + 1);
	}

	static void initializeForMonth(int month) {
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("month must be between 1 and 12: " + month);
		}
		current = month <= 2 || month >= 11 ? Theme.WINTER : Theme.LUSH;
	}

	static Theme current() {
		return current;
	}

	public static String tilesTexture() {
		return current == Theme.WINTER
				? Assets.Environment.TILES_SURFACE_WINTER
				: Assets.Environment.TILES_SURFACE_LUSH;
	}

	public static String waterTexture() {
		return current == Theme.WINTER
				? Assets.Environment.WATER_SURFACE_WINTER
				: Assets.Environment.WATER_SURFACE_LUSH;
	}

	static String grassTextureCacheKey() {
		return current == Theme.WINTER
				? "surface-town-clean-grass-winter-v1"
				: "surface-town-clean-grass-lush-v1";
	}
}
