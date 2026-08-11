/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public final class TowerStyleSchedule {

	public static final String CHINESE_HALL = "chinese_hall";
	public static final String GOTHIC_CASTLE = "gothic_castle";
	public static final String ASTRAL_LIBRARY = "astral_library";
	public static final String TOWER_CORE_GEARWORKS = "tower_core_gearworks";
	public static final String SKY_ALCHEMY_GREENHOUSE = "sky_alchemy_greenhouse";
	public static final String FROST_ARCHIVE = "frost_archive";

	private static final String ORDER = "order";
	private static final String BANDS = "bands";
	private static final int FLOORS_PER_STYLE = 5;

	// These IDs are part of the save format. Keep existing IDs stable and append new styles.
	private static final String[] CURRENT_STYLES = {
			CHINESE_HALL,
			GOTHIC_CASTLE,
			ASTRAL_LIBRARY,
			TOWER_CORE_GEARWORKS,
			SKY_ALCHEMY_GREENHOUSE,
			FROST_ARCHIVE
	};

	private final ArrayList<String> availableStyles;
	private final ArrayList<String> styleOrder = new ArrayList<>();
	private final ArrayList<String> assignedBands = new ArrayList<>();

	public TowerStyleSchedule() {
		this(CURRENT_STYLES);
	}

	TowerStyleSchedule(String... availableStyles) {
		this.availableStyles = new ArrayList<>(Arrays.asList(availableStyles));
	}

	public void reset() {
		styleOrder.clear();
		assignedBands.clear();
	}

	public Class<? extends Level> levelClassForFloor(int floor, long runSeed) {
		return levelClassForStyle(styleIdForFloor(floor, runSeed));
	}

	public String tilesTexForFloor(int floor, long runSeed) {
		return tilesTexForStyle(styleIdForFloor(floor, runSeed));
	}

	public String waterTexForFloor(int floor, long runSeed) {
		return waterTexForStyle(styleIdForFloor(floor, runSeed));
	}

	String styleIdForFloor(int floor, long runSeed) {
		if (floor < 1) {
			throw new IllegalArgumentException("Tower floors start at one");
		}
		ensureStyleOrder(runSeed);
		int band = (floor - 1) / FLOORS_PER_STYLE;
		while (assignedBands.size() <= band) {
			int nextBand = assignedBands.size();
			int styleIndex = nextBand % styleOrder.size();
			String nextStyle = styleOrder.get(styleIndex);
			if (styleOrder.size() > 1 && !assignedBands.isEmpty()
					&& nextStyle.equals(assignedBands.get(nextBand - 1))) {
				nextStyle = styleOrder.get((styleIndex + 1) % styleOrder.size());
			}
			assignedBands.add(nextStyle);
		}
		return assignedBands.get(band);
	}

	public void preserveLegacyGeneratedFloor(int floor, long runSeed) {
		styleIdForFloor(floor, runSeed);
		assignedBands.set((floor - 1) / FLOORS_PER_STYLE, CHINESE_HALL);
	}

	String[] styleOrder() {
		return styleOrder.toArray(new String[0]);
	}

	public void storeInBundle(Bundle bundle) {
		bundle.put(ORDER, styleOrder.toArray(new String[0]));
		bundle.put(BANDS, assignedBands.toArray(new String[0]));
	}

	public boolean restoreFromBundle(Bundle bundle, long runSeed) {
		reset();
		boolean restored = bundle != null && !bundle.isNull() && bundle.contains(ORDER);
		if (restored) {
			Collections.addAll(styleOrder, bundle.getStringArray(ORDER));
			if (bundle.contains(BANDS)) {
				Collections.addAll(assignedBands, bundle.getStringArray(BANDS));
			}
		}
		ensureStyleOrder(runSeed);
		return restored;
	}

	private void ensureStyleOrder(final long runSeed) {
		if (styleOrder.isEmpty()) {
			styleOrder.addAll(availableStyles);
			Collections.shuffle(styleOrder, new java.util.Random(mixedSeed(runSeed)));
			return;
		}

		ArrayList<String> additions = new ArrayList<>();
		for (String style : availableStyles) {
			if (!styleOrder.contains(style)) {
				additions.add(style);
			}
		}
		Collections.sort(additions, new Comparator<String>() {
			@Override
			public int compare(String first, String second) {
				return Long.compare(styleHash(runSeed, first), styleHash(runSeed, second));
			}
		});
		styleOrder.addAll(additions);
	}

	private static Class<? extends Level> levelClassForStyle(String style) {
		if (GOTHIC_CASTLE.equals(style)) {
			return GothicCastleLevel.class;
		} else if (ASTRAL_LIBRARY.equals(style)) {
			return AstralLibraryLevel.class;
		} else if (TOWER_CORE_GEARWORKS.equals(style)) {
			return TowerCoreGearworksLevel.class;
		} else if (SKY_ALCHEMY_GREENHOUSE.equals(style)) {
			return SkyAlchemyGreenhouseLevel.class;
		} else if (FROST_ARCHIVE.equals(style)) {
			return FrostArchiveLevel.class;
		}
		return TowerLevel.class;
	}

	private static String tilesTexForStyle(String style) {
		if (GOTHIC_CASTLE.equals(style)) {
			return Assets.Environment.TILES_GOTHIC_CASTLE;
		} else if (ASTRAL_LIBRARY.equals(style)) {
			return Assets.Environment.TILES_ASTRAL_LIBRARY;
		} else if (TOWER_CORE_GEARWORKS.equals(style)) {
			return Assets.Environment.TILES_TOWER_CORE_GEARWORKS;
		} else if (SKY_ALCHEMY_GREENHOUSE.equals(style)) {
			return Assets.Environment.TILES_SKY_ALCHEMY_GREENHOUSE;
		} else if (FROST_ARCHIVE.equals(style)) {
			return Assets.Environment.TILES_FROST_ARCHIVE;
		}
		return Assets.Environment.TILES_CHINESE_HALL;
	}

	private static String waterTexForStyle(String style) {
		if (GOTHIC_CASTLE.equals(style)) {
			return Assets.Environment.WATER_GOTHIC_CASTLE;
		} else if (ASTRAL_LIBRARY.equals(style)) {
			return Assets.Environment.WATER_ASTRAL_LIBRARY;
		} else if (TOWER_CORE_GEARWORKS.equals(style)) {
			return Assets.Environment.WATER_TOWER_CORE_GEARWORKS;
		} else if (SKY_ALCHEMY_GREENHOUSE.equals(style)) {
			return Assets.Environment.WATER_SKY_ALCHEMY_GREENHOUSE;
		} else if (FROST_ARCHIVE.equals(style)) {
			return Assets.Environment.WATER_FROST_ARCHIVE;
		}
		return Assets.Environment.WATER_CHINESE_HALL;
	}

	private static long styleHash(long runSeed, String style) {
		return mixedSeed(runSeed ^ style.hashCode());
	}

	private static long mixedSeed(long value) {
		value += 0x6A09E667F3BCC909L;
		value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
		value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
		return value ^ (value >>> 31);
	}
}
