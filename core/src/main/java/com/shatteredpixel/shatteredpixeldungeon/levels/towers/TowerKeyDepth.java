package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

/** Depth mapping used for keys generated while tower floors use regular-level content tables. */
public final class TowerKeyDepth {
	private static final int FIRST_CONTENT_DEPTH = 16;
	private static final int LAST_CONTENT_DEPTH = 25;

	private TowerKeyDepth() {
	}

	public static int forGeneration(int towerFloor, int fallbackDepth) {
		return towerFloor > 0 ? towerFloor : fallbackDepth;
	}

	public static int legacyContentDepth(int towerFloor) {
		int offset = Math.min(LAST_CONTENT_DEPTH - FIRST_CONTENT_DEPTH,
				Math.max(0, towerFloor - 1));
		return FIRST_CONTENT_DEPTH + offset;
	}
}
