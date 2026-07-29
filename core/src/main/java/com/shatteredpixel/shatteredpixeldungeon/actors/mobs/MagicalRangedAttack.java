/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

public interface MagicalRangedAttack extends RangedAttack {

	@Override
	default Type rangedAttackType() {
		return Type.RANGED_MAGIC;
	}

	@Override
	default int rangedAttackBallisticaMode() {
		return Ballistica.MAGIC_BOLT;
	}
}
