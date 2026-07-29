/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class NewCycleHungerProtection extends Buff {

	{
		revivePersists = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	public static void updateForCurrentFloor(Char target) {
		if (target == null) {
			return;
		}

		if (Dungeon.newCycle && Dungeon.depth == 0) {
			Buff.affect(target, NewCycleHungerProtection.class);
		} else {
			Buff.detach(target, NewCycleHungerProtection.class);
		}
	}
}
