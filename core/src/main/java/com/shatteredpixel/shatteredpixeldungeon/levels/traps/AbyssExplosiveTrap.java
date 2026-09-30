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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

import java.util.ArrayList;

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

	@Override
	protected void onExplosionComplete(ArrayList<Char> affectedChars) {
		for (Char affected : affectedChars) {
			if (shouldRemoveLevitation(Random.Int(3))
					&& affected != null && affected.buff(Levitation.class) != null) {
				Buff.detach(affected, Levitation.class);
			}
		}
	}

	static boolean shouldRemoveLevitation(int roll) {
		return roll == 0;
	}
}
