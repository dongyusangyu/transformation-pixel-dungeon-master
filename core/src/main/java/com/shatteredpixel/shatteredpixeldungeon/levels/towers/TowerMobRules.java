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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

final class TowerMobRules {

	private TowerMobRules() {
	}

	static Selection select(float roll) {
		Selection[] selections = Selection.values();
		int index = (int) (roll * selections.length);
		index = Math.max(0, Math.min(index, selections.length - 1));
		return selections[index];
	}

	static Mob prepareNaturalSpawn(Mob mob) {
		mob.state = mob.WANDERING;
		return mob;
	}

	enum Selection {
		CAMOUFLAGE_GNOLL,
		CORROSIVE_SWARM,
		CORPSE,
		EARTHLY_SERPENT,
		ROAST_LAMB_WARLOCK,
		MECHANICAL_FIST,
		MIMIC_CROCODILE,
		OBSCURA,
		ALIENATED_PRISMATIC_GUARD,
		SOUL_COLLECTOR,
		HEAVY_CRABIFICATION,
		MARSH_SLIME,
		RUNE_SPINNER
	}
}
