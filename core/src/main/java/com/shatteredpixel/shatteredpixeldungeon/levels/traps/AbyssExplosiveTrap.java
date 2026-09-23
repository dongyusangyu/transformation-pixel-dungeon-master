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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

public class AbyssExplosiveTrap extends ExplosiveTrap {

	@Override
	public boolean preservesTerrain() {
		return true;
	}

	@Override
	public boolean canPlaceOnTerrain(int terrain) {
		return terrain == Terrain.CHASM;
	}

	@Override
	public boolean triggersOnEntry() {
		return true;
	}

	@Override
	public boolean avoids(Char ch) {
		return active && visible && ch instanceof Mob;
	}
}
