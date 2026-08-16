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

import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ArmoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CryptRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalChoiceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalPathRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalVaultRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.GardenRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.LaboratoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.LibraryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicWellRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PoolRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.RunestoneRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SacrificeRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.StatueRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.StorageRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ToxicGasRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.TrapsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.TreasuryRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class TowerSpecialRoomRules {

	private static final long ROOM_SEED_SALT = 0x54A7E2C91D3B6F08L;
	private static final long LAB_SEED_SALT = 0x2C19B7A5E4D8036FL;

	private static final List<Class<? extends SpecialRoom>> ELIGIBLE_ROOMS =
			Collections.unmodifiableList(Arrays.asList(
					ArmoryRoom.class,
					CryptRoom.class,
					CrystalChoiceRoom.class,
					CrystalPathRoom.class,
					CrystalVaultRoom.class,
					GardenRoom.class,
					LibraryRoom.class,
					MagicalFireRoom.class,
					MagicWellRoom.class,
					PoolRoom.class,
					RunestoneRoom.class,
					SacrificeRoom.class,
					SentryRoom.class,
					StatueRoom.class,
					StorageRoom.class,
					ToxicGasRoom.class,
					TrapsRoom.class,
					TreasuryRoom.class));
	private static final List<Class<? extends SpecialRoom>> CRYSTAL_KEY_ROOMS =
			Arrays.asList(
					CrystalChoiceRoom.class,
					CrystalPathRoom.class,
					CrystalVaultRoom.class);
	private static final List<Class<? extends SpecialRoom>> POTION_PUZZLE_ROOMS =
			Arrays.asList(
					PoolRoom.class,
					SentryRoom.class,
					StorageRoom.class,
					ToxicGasRoom.class,
					MagicalFireRoom.class,
					TrapsRoom.class);

	private TowerSpecialRoomRules() {
	}

	static List<Class<? extends SpecialRoom>> eligibleRoomTypes() {
		return new ArrayList<>(ELIGIBLE_ROOMS);
	}

	static List<Class<? extends SpecialRoom>> roomTypesForFloor(
			int floor, long runSeed, boolean large) {
		if (floor < 1 || floor % TowerBossLevel.FLOORS_PER_BOSS == 0) {
			return new ArrayList<>();
		}

		java.util.Random random = new java.util.Random(
				mixedSeed(runSeed ^ ROOM_SEED_SALT ^ floor));
		int count = (large ? 2 : 1) + random.nextInt(2);
		ArrayList<Class<? extends SpecialRoom>> result = new ArrayList<>();
		if (isLaboratoryFloor(floor, runSeed)) {
			result.add(LaboratoryRoom.class);
		}

		ArrayList<Class<? extends SpecialRoom>> candidates =
				new ArrayList<>(ELIGIBLE_ROOMS);
		Collections.shuffle(candidates, random);
		for (Class<? extends SpecialRoom> type : candidates) {
			if (result.size() >= count) {
				break;
			}
			if (!conflictsWithSelectedRoom(type, result)) {
				result.add(type);
			}
		}
		return result;
	}

	private static boolean conflictsWithSelectedRoom(
			Class<? extends SpecialRoom> candidate,
			List<Class<? extends SpecialRoom>> selected) {
		return sharesGroup(candidate, selected, CRYSTAL_KEY_ROOMS)
				|| sharesGroup(candidate, selected, POTION_PUZZLE_ROOMS);
	}

	private static boolean sharesGroup(
			Class<? extends SpecialRoom> candidate,
			List<Class<? extends SpecialRoom>> selected,
			List<Class<? extends SpecialRoom>> group) {
		if (!group.contains(candidate)) {
			return false;
		}
		for (Class<? extends SpecialRoom> type : selected) {
			if (group.contains(type)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isLaboratoryFloor(int floor, long runSeed) {
		int band = (floor - 1) / TowerBossLevel.FLOORS_PER_BOSS;
		int firstFloor = band * TowerBossLevel.FLOORS_PER_BOSS + 1;
		java.util.Random random = new java.util.Random(
				mixedSeed(runSeed ^ LAB_SEED_SALT ^ band));
		int laboratoryFloor = firstFloor + 1 + random.nextInt(3);
		return floor == laboratoryFloor;
	}

	private static long mixedSeed(long value) {
		value += 0x9E3779B97F4A7C15L;
		value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
		value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
		return value ^ (value >>> 31);
	}
}
