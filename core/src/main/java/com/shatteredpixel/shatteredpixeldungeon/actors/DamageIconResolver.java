/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;

import java.util.EnumSet;

public final class DamageIconResolver {

	private DamageIconResolver() {
	}

	public static int resolve(EnumSet<DamageTag> tags) {
		switch (DamageTag.primaryIconTag(tags)) {
			case PICKAXE:        return FloatingText.PICK_DMG;
			case HUNGER:         return FloatingText.HUNGER;
			case FIRE:           return FloatingText.BURNING;
			case FROST:          return FloatingText.FROST;
			case WATER:          return FloatingText.WATER;
			case ELECTRIC:       return FloatingText.SHOCKING;
			case BLEEDING:       return FloatingText.BLEEDING;
			case TOXIC:          return FloatingText.TOXIC;
			case CORROSION:      return FloatingText.CORROSION;
			case POISON:         return FloatingText.POISON;
			case OOZE:           return FloatingText.OOZE;
			case DEFERRED:       return FloatingText.DEFERRED;
			case CORRUPTION:     return FloatingText.CORRUPTION;
			case AMULET:         return FloatingText.AMULET;
			case REASON:         return FloatingText.MISS_SUFFER;
			case ENDLESS_MALICE: return FloatingText.ENDLESS_MALICE;
			case LIFE_SPORT:     return FloatingText.LIFE_SPORT;
			case MAGICAL:        return FloatingText.MAGIC_DMG;
			case NO_ARMOR:       return FloatingText.PHYS_DMG_NO_BLOCK;
			default:             return FloatingText.PHYS_DMG;
		}
	}
}
