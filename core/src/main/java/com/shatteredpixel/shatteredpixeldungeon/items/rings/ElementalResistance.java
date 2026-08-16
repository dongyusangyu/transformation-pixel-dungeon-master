/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;

import java.util.HashSet;

final class ElementalResistance {

	static final HashSet<Class> RESISTS = new HashSet<>();

	static {
		RESISTS.add(Burning.class);
		RESISTS.add(Chill.class);
		RESISTS.add(Frost.class);
		RESISTS.add(Ooze.class);
		RESISTS.add(Paralysis.class);
		RESISTS.add(Poison.class);
		RESISTS.add(Corrosion.class);
		RESISTS.add(ToxicGas.class);
		RESISTS.add(Electricity.class);
		RESISTS.addAll(AntiMagic.RESISTS);
        RESISTS.add(RoastLambWarlock.class);
	}

	private ElementalResistance() {
	}

	static boolean resists(Class effect) {
		for (Class resistedEffect : RESISTS) {
			if (resistedEffect.isAssignableFrom(effect)) {
				return true;
			}
		}
		return false;
	}

	static float magicFeatherMultiplier(Class effect, int featherLevel) {
		return resists(effect) ? 1.15f + 0.15f * featherLevel : 1f;
	}

	static float resistanceMultiplier(Class effect, float originalMonsterMultiplier,
									  float magicFeatherMultiplier, int ringBonus) {
		if (resists(effect)) {
			return originalMonsterMultiplier * magicFeatherMultiplier
					* (float)Math.pow(0.825, ringBonus);
		}
		return 1f;
	}
}
