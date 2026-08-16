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
import java.util.HashMap;
import java.util.Map;

public final class TowerStyleSchedule {

	public static final String CHINESE_HALL = "chinese_hall";
	public static final String GOTHIC_CASTLE = "gothic_castle";
	public static final String ASTRAL_LIBRARY = "astral_library";
	public static final String TOWER_CORE_GEARWORKS = "tower_core_gearworks";
	public static final String SKY_ALCHEMY_GREENHOUSE = "sky_alchemy_greenhouse";
	public static final String FROST_ARCHIVE = "frost_archive";

	private static final String ORDER = "order";
	private static final String BANDS = "bands";
	private static final String EPOCH_STARTS = "epoch_starts";
	private static final String EPOCH_COUNTS = "epoch_counts";
	private static final String EPOCH_SHIFTS = "epoch_shifts";
	private static final String HIGHEST_BAND = "highest_band";
	private static final String OVERRIDE_BANDS = "override_bands";
	private static final String OVERRIDE_STYLES = "override_styles";
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
	private final ArrayList<StyleEpoch> epochs = new ArrayList<>();
	private final HashMap<Integer, String> bandOverrides = new HashMap<>();
	private int highestAssignedBand = -1;

	public TowerStyleSchedule() {
		this(CURRENT_STYLES);
	}

	TowerStyleSchedule(String... availableStyles) {
		this.availableStyles = new ArrayList<>(Arrays.asList(availableStyles));
	}

	public void reset() {
		styleOrder.clear();
		epochs.clear();
		bandOverrides.clear();
		highestAssignedBand = -1;
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
		highestAssignedBand = Math.max(highestAssignedBand, band);
		return styleForBand(band);
	}

	public void preserveLegacyGeneratedFloor(int floor, long runSeed) {
		int band = (floor - 1) / FLOORS_PER_STYLE;
		styleIdForFloor(floor, runSeed);
		bandOverrides.put(band, CHINESE_HALL);
	}

	String[] styleOrder() {
		return styleOrder.toArray(new String[0]);
	}

	public void storeInBundle(Bundle bundle) {
		bundle.put(ORDER, styleOrder.toArray(new String[0]));
		int[] starts = new int[epochs.size()];
		int[] counts = new int[epochs.size()];
		int[] shifts = new int[epochs.size()];
		for (int i = 0; i < epochs.size(); i++) {
			StyleEpoch epoch = epochs.get(i);
			starts[i] = epoch.startBand;
			counts[i] = epoch.styleCount;
			shifts[i] = epoch.shift;
		}
		bundle.put(EPOCH_STARTS, starts);
		bundle.put(EPOCH_COUNTS, counts);
		bundle.put(EPOCH_SHIFTS, shifts);
		bundle.put(HIGHEST_BAND, highestAssignedBand);

		int[] overrideBands = new int[bandOverrides.size()];
		String[] overrideStyles = new String[bandOverrides.size()];
		int i = 0;
		for (Map.Entry<Integer, String> entry : bandOverrides.entrySet()) {
			overrideBands[i] = entry.getKey();
			overrideStyles[i] = entry.getValue();
			i++;
		}
		bundle.put(OVERRIDE_BANDS, overrideBands);
		bundle.put(OVERRIDE_STYLES, overrideStyles);
	}

	public boolean restoreFromBundle(Bundle bundle, long runSeed) {
		reset();
		boolean restored = bundle != null && !bundle.isNull() && bundle.contains(ORDER);
		if (restored) {
			Collections.addAll(styleOrder, bundle.getStringArray(ORDER));
			if (bundle.contains(EPOCH_STARTS)) {
				int[] starts = bundle.getIntArray(EPOCH_STARTS);
				int[] counts = bundle.getIntArray(EPOCH_COUNTS);
				int[] shifts = bundle.getIntArray(EPOCH_SHIFTS);
				int length = Math.min(starts.length, Math.min(counts.length, shifts.length));
				for (int i = 0; i < length; i++) {
					epochs.add(new StyleEpoch(starts[i], counts[i], shifts[i]));
				}
				highestAssignedBand = bundle.getInt(HIGHEST_BAND);
			} else if (bundle.contains(BANDS)) {
				String[] legacyBands = bundle.getStringArray(BANDS);
				epochs.add(new StyleEpoch(0, styleOrder.size(), 0));
				highestAssignedBand = legacyBands.length - 1;
				for (int i = 0; i < legacyBands.length; i++) {
					if (!legacyBands[i].equals(styleForBand(i))) {
						bandOverrides.put(i, legacyBands[i]);
					}
				}
			}
			if (bundle.contains(OVERRIDE_BANDS) && bundle.contains(OVERRIDE_STYLES)) {
				int[] bands = bundle.getIntArray(OVERRIDE_BANDS);
				String[] styles = bundle.getStringArray(OVERRIDE_STYLES);
				for (int i = 0; i < Math.min(bands.length, styles.length); i++) {
					bandOverrides.put(bands[i], styles[i]);
				}
			}
		}
		ensureStyleOrder(runSeed);
		return restored;
	}

	private void ensureStyleOrder(final long runSeed) {
		if (styleOrder.isEmpty()) {
			styleOrder.addAll(availableStyles);
			Collections.shuffle(styleOrder, new java.util.Random(mixedSeed(runSeed)));
			epochs.add(new StyleEpoch(0, styleOrder.size(), 0));
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
		if (!additions.isEmpty()) {
			if (epochs.isEmpty()) {
				epochs.add(new StyleEpoch(0, styleOrder.size(), 0));
			}
			styleOrder.addAll(additions);
			if (highestAssignedBand < 0) {
				epochs.clear();
				epochs.add(new StyleEpoch(0, styleOrder.size(), 0));
			} else {
				int startBand = highestAssignedBand + 1;
				String previousStyle = styleForBand(highestAssignedBand);
				String nextStyle = styleOrder.get(startBand % styleOrder.size());
				int shift = styleOrder.size() > 1 && nextStyle.equals(previousStyle) ? 1 : 0;
				epochs.add(new StyleEpoch(startBand, styleOrder.size(), shift));
			}
		}
	}

	private String styleForBand(int band) {
		String override = bandOverrides.get(band);
		if (override != null) {
			return override;
		}
		StyleEpoch selected = epochs.get(0);
		for (int i = 1; i < epochs.size() && epochs.get(i).startBand <= band; i++) {
			selected = epochs.get(i);
		}
		int index = (band % selected.styleCount + selected.shift) % selected.styleCount;
		return styleOrder.get(index);
	}

	private static final class StyleEpoch {
		final int startBand;
		final int styleCount;
		final int shift;

		StyleEpoch(int startBand, int styleCount, int shift) {
			this.startBand = startBand;
			this.styleCount = styleCount;
			this.shift = shift;
		}
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
