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

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.connection.ConnectionRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.LaboratoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SacrificeRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AbyssExplosiveTrap;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

final class TowerGenerationRules {
	static final Class<? extends Item> GUARANTEED_SHOP_ITEM = ScrollOfExtraction.class;
	static final Class<? extends Item> METAMORPHOSIS_ITEM = ScrollOfMetamorphosis.class;

	private TowerGenerationRules() {
	}

	static boolean isShopFloor(int floor) {
		return floor >= 1 && (floor - 1) % 5 == 0;
	}

	static boolean shouldGenerateNaturalFood(boolean bossFloor) {
		return !bossFloor;
	}

	static boolean shouldGenerateLevelFeeling(int towerFloor, boolean bossFloor) {
		return towerFloor > 1 && !bossFloor;
	}

	static void ensureLaboratoryRewards(Level level, List<? extends Room> rooms) {
		for (Room room : rooms) {
			if (room instanceof LaboratoryRoom) {
				((LaboratoryRoom) room).ensureTowerRewards(level);
			}
		}
	}

	static Level.Feeling feelingForRoll(int roll) {
		if (roll < 0 || roll >= 20) {
			throw new IllegalArgumentException("Tower feeling roll must be between 0 and 19");
		}
		switch (roll) {
			case 0: return Level.Feeling.CHASM;
			case 1: return Level.Feeling.SKY_ISLAND;

			case 2: return Level.Feeling.BARREN;
            case 3: return Level.Feeling.CHAOS;
			case 9: return Level.Feeling.WATER;
			case 4: return Level.Feeling.GRASS;
			case 5: return Level.Feeling.DARK;
			case 6: return Level.Feeling.LARGE;
			case 7: return Level.Feeling.TRAPS;
			case 8: return Level.Feeling.SECRETS;

			default: return Level.Feeling.NONE;
		}
	}

	static float waterFill(Level.Feeling feeling) {
		if (feeling == Level.Feeling.BARREN) return 0f;
		return feeling == Level.Feeling.WATER ? 0.65f : 0.15f;
	}

	static float grassFill(Level.Feeling feeling) {
		if (feeling == Level.Feeling.BARREN) return 0f;
		return feeling == Level.Feeling.GRASS ? 0.55f : 0.12f;
	}

	static void clearBarrenTerrain(Level level) {
		for (int cell = 0; cell < level.map.length; cell++) {
			switch (level.map[cell]) {
				case Terrain.WATER:
				case Terrain.HIGH_GRASS:
				case Terrain.GRASS:
				case Terrain.FURROWED_GRASS:
					level.map[cell] = Terrain.EMPTY;
					break;
				default:
					break;
			}
		}
	}

	static int abyssTrapCount(Level.Feeling feeling, int randomValue) {
		int maxRandomValue = feeling == Level.Feeling.SKY_ISLAND ? 5 : 2;
		if (randomValue < 0 || randomValue > maxRandomValue) {
			throw new IllegalArgumentException("Invalid tower abyss trap roll");
		}
		return feeling == Level.Feeling.SKY_ISLAND ? 5 + randomValue : randomValue;
	}

	static ArrayList<Integer> abyssTrapCells(Level level, int count) {
		ArrayList<Integer> preferred = new ArrayList<>();
		ArrayList<Integer> fallback = new ArrayList<>();
		for (int cell = level.width() + 1; cell < level.length() - level.width() - 1; cell++) {
			int x = cell % level.width();
			if (x == 0 || x == level.width() - 1
					|| level.map[cell] != Terrain.CHASM
					|| level.traps.get(cell) != null
					|| level.getTransition(cell) != null) {
				continue;
			}

			boolean nextToFloor = false;
			for (int offset : PathFinder.NEIGHBOURS8) {
				if ((Terrain.flags[level.map[cell + offset]] & Terrain.PASSABLE) != 0) {
					nextToFloor = true;
					break;
				}
			}
			(nextToFloor ? preferred : fallback).add(cell);
		}

		Random.shuffle(preferred);
		Random.shuffle(fallback);
		ArrayList<Integer> result = new ArrayList<>();
		for (int cell : preferred) {
			if (result.size() >= count) break;
			result.add(cell);
		}
		for (int cell : fallback) {
			if (result.size() >= count) break;
			result.add(cell);
		}
		return result;
	}

	static void placeAbyssTraps(Level level, Level.Feeling feeling, int randomValue) {
		ArrayList<Integer> cells = abyssTrapCells(level, abyssTrapCount(feeling, randomValue));
		for (int i = 0; i < cells.size(); i++) {
			int cell = cells.get(i);
			AbyssExplosiveTrap trap = new AbyssExplosiveTrap();
			trap.set(cell);
			if (cells.size() == 1 ? Random.Int(2) == 0 : i % 2 == 0) {
				trap.reveal();
			} else {
				trap.hide();
			}
			level.setTrap(trap, cell);
		}
	}

	static void retainSkyIslandDoorLandings(Level level, List<? extends Room> rooms) {
		if (level == null || level.feeling != Level.Feeling.SKY_ISLAND || rooms == null) {
			return;
		}
		for (Room room : rooms) {
			if (!(room instanceof ConnectionRoom)) continue;
			for (Room destination : room.connected.keySet()) {
				if (destination instanceof ConnectionRoom) continue;
				Room.Door door = room.connected.get(destination);
				int doorCell = level.pointToCell(door);
				int connectionSideCell = level.pointToCell(room.pointInside(door, 1));
				int roomSideCell = level.pointToCell(destination.pointInside(door, 1));
				if (level.insideMap(doorCell)
						&& level.insideMap(connectionSideCell)
						&& level.insideMap(roomSideCell)
						&& door.type != Room.Door.Type.WALL
						&& level.map[connectionSideCell] == Terrain.CHASM
						&& (Terrain.flags[level.map[roomSideCell]] & Terrain.PASSABLE) != 0
						&& level.getTransition(doorCell) == null
						&& level.getTransition(connectionSideCell) == null) {
					if (level.map[doorCell] == Terrain.CHASM) {
						level.map[doorCell] = destination instanceof SacrificeRoom
								? Terrain.EMPTY : Terrain.EMPTY_SP;
					}
					level.map[connectionSideCell] = Terrain.EMPTY_SP;
				}
			}
		}
	}

	static ArrayList<Integer> skyIslandPotionCells(Level level, List<? extends Room> rooms) {
		ArrayList<Integer> result = new ArrayList<>();
		for (Room room : rooms) {
			if (!(room instanceof StandardRoom) || room.isEntrance() || room.isExit()) {
				continue;
			}

			ArrayList<Integer> preferred = new ArrayList<>();
			ArrayList<Integer> fallback = new ArrayList<>();
			for (Point point : room.itemPlaceablePoints(level)) {
				int cell = level.pointToCell(point);
				if (cell < 0 || cell >= level.length()
						|| !level.passable[cell] || level.pit[cell]
						|| level.getTransition(cell) != null) {
					continue;
				}
				fallback.add(cell);
				if (level.heaps.get(cell) == null
						&& level.findMob(cell) == null
						&& level.traps.get(cell) == null
						&& level.plants.get(cell) == null) {
					preferred.add(cell);
				}
			}

			ArrayList<Integer> candidates = preferred.isEmpty() ? fallback : preferred;
			if (!candidates.isEmpty()) {
				result.add(Random.element(candidates));
			}
		}
		return result;
	}

	static int secretRoomCount(Level.Feeling feeling) {
		return feeling == Level.Feeling.SECRETS ? 1 : 0;
	}

	static int shopPriceDepth(int floor) {
		return 35 + 5 * ((Math.max(1, floor) - 1) / 5);
	}

	static int driedRosePetalProgressDepth(int towerFloor) {
		return 25 + Math.max(1, towerFloor);
	}

	static boolean driedRosePetalGenerationAllowed(int droppedPetals, boolean roseMaxLevel) {
		return droppedPetals < 11 || !roseMaxLevel;
	}

	static boolean driedRosePetalGenerationEnabled(int branch) {
		return branch == 0 || branch == TowerLevel.BRANCH;
	}

	static boolean isForbiddenNaturalItem(Item item) {
		return item != null && isForbiddenNaturalItemClass(item.getClass());
	}

	static boolean isForbiddenNaturalItemClass(Class<? extends Item> itemClass) {
		return PotionOfStrength.class.isAssignableFrom(itemClass)
				|| ScrollOfUpgrade.class.isAssignableFrom(itemClass)
				|| ScrollOfMetamorphosis.class.isAssignableFrom(itemClass);
	}

	static Item guaranteedShopItem() {
		return new ScrollOfExtraction();
	}

	static Item guaranteedShopMetamorphosis() {
		return new ScrollOfMetamorphosis();
	}

	static Item prepareFloorSpawn(Item item, int towerFloor) {
		if (isForbiddenNaturalItem(item)) {
			return null;
		}
		if (item instanceof Key) {
			((Key) item).depth = towerFloor;
		}
		return item;
	}

}
