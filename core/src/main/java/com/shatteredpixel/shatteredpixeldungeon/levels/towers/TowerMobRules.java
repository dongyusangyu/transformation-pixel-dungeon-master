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

final class TowerMobRules {

	private static final float CAMOUFLAGE_GNOLL_END = 0.2f;
	private static final float CORROSIVE_SWARM_END = 0.4f;

	private TowerMobRules() {
	}

	static Selection select(int towerFloor, float roll) {
		if (roll < CAMOUFLAGE_GNOLL_END) {
			return Selection.CAMOUFLAGE_GNOLL;
		}
		if (roll < CORROSIVE_SWARM_END) {
			return Selection.CORROSIVE_SWARM;
		}
		return Selection.DEFAULT_POOL;
	}

	enum Selection {
		CAMOUFLAGE_GNOLL,
		CORROSIVE_SWARM,
		DEFAULT_POOL
	}
}
