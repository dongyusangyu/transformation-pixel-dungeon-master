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

import java.util.EnumSet;

/**
 * Orthogonal properties passed through damage calculations. A single hit may
 * carry several tags, such as MAGICAL + FIRE + NO_ARMOR.
 */
public enum DamageTag {

	PHYSICAL,
	MAGICAL,
	MELEE,
	RANGED,
	NO_ARMOR,
	UNAVOIDABLE,

	PICKAXE,
	HUNGER,
	FIRE,
	FROST,
	WATER,
	ELECTRIC,
	BLEEDING,
	TOXIC,
	CORROSION,
	POISON,
	OOZE,
	DEFERRED,
	CORRUPTION,
	AMULET,
	REASON,
	ENDLESS_MALICE,
	LIFE_SPORT;

	public enum Delivery {
		NONE,
		MELEE,
		RANGED
	}

	public static EnumSet<DamageTag> of(DamageTag... tags) {
		EnumSet<DamageTag> result = EnumSet.noneOf(DamageTag.class);
		if (tags != null) {
			for (DamageTag tag : tags) {
				if (tag != null) {
					result.add(tag);
				}
			}
		}
		return result;
	}

	public static boolean has(EnumSet<DamageTag> tags, DamageTag tag) {
		return tags != null && tags.contains(tag);
	}

	public static Delivery physicalDelivery(EnumSet<DamageTag> tags) {
		if (!has(tags, PHYSICAL) || has(tags, MAGICAL)) {
			return Delivery.NONE;
		}
		if (has(tags, RANGED)) {
			return Delivery.RANGED;
		}
		if (has(tags, MELEE)) {
			return Delivery.MELEE;
		}
		return Delivery.NONE;
	}

	public static DamageTag primaryIconTag(EnumSet<DamageTag> tags) {
		if (has(tags, PICKAXE))        return PICKAXE;
		if (has(tags, HUNGER))         return HUNGER;
		if (has(tags, FIRE))           return FIRE;
		if (has(tags, FROST))          return FROST;
		if (has(tags, WATER))          return WATER;
		if (has(tags, ELECTRIC))       return ELECTRIC;
		if (has(tags, BLEEDING))       return BLEEDING;
		if (has(tags, TOXIC))          return TOXIC;
		if (has(tags, CORROSION))      return CORROSION;
		if (has(tags, POISON))         return POISON;
		if (has(tags, OOZE))           return OOZE;
		if (has(tags, DEFERRED))       return DEFERRED;
		if (has(tags, CORRUPTION))     return CORRUPTION;
		if (has(tags, AMULET))         return AMULET;
		if (has(tags, REASON))         return REASON;
		if (has(tags, ENDLESS_MALICE)) return ENDLESS_MALICE;
		if (has(tags, LIFE_SPORT))     return LIFE_SPORT;
		if (has(tags, MAGICAL))        return MAGICAL;
		if (has(tags, NO_ARMOR))       return NO_ARMOR;
		return PHYSICAL;
	}
}
