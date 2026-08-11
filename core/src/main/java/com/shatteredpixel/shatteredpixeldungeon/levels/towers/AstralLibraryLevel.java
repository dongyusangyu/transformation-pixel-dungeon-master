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

public class AstralLibraryLevel extends TowerLevel {

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_ASTRAL_LIBRARY;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_ASTRAL_LIBRARY;
	}
}
