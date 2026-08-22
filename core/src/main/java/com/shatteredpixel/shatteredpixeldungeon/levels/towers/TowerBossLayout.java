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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.levels.Patch;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

final class TowerBossLayout {

	static final int WIDTH = 29;
	static final int HEIGHT = 35;

	static final int ENTRANCE = cell(14, 32);
	static final int EXIT = cell(14, 2);
	static final int SAFE_GATE = cell(14, 30);
	static final int EXIT_GATE = cell(14, 4);

	private static final int ARENA_LEFT = 2;
	private static final int ARENA_TOP = 5;
	private static final int ARENA_SIZE = 25;
	private static final int OBSTACLE_COUNT = 28;

	private TowerBossLayout() {
	}

	static int[] generateMap() {
		int[] map = new int[WIDTH * HEIGHT];
		Arrays.fill(map, Terrain.WALL);

		fill(map, ARENA_LEFT, ARENA_TOP, ARENA_SIZE, ARENA_SIZE, Terrain.EMPTY);
		fill(map, 13, 1, 3, 3, Terrain.EMPTY);
		fill(map, 13, 31, 3, 3, Terrain.EMPTY);

		map[ENTRANCE] = Terrain.ENTRANCE;
		map[EXIT] = Terrain.EXIT;
		map[SAFE_GATE] = Terrain.DOOR;
		map[EXIT_GATE] = Terrain.LOCKED_EXIT;

		paintNaturalTerrain(map);
		return map;
	}

	private static void paintNaturalTerrain(int[] map) {
		boolean[] water = Patch.generate(ARENA_SIZE, ARENA_SIZE, 0.18f, 2, true);
		boolean[] vegetation = Patch.generate(ARENA_SIZE, ARENA_SIZE, 0.17f, 2, true);

		for (int localY = 0; localY < ARENA_SIZE; localY++) {
			for (int localX = 0; localX < ARENA_SIZE; localX++) {
				int x = ARENA_LEFT + localX;
				int y = ARENA_TOP + localY;
				int cell = cell(x, y);
				int localCell = localX + localY * ARENA_SIZE;
				if (isReservedRoute(x, y)) {
					continue;
				}
				if (water[localCell]) {
					map[cell] = Terrain.WATER;
				} else if (vegetation[localCell]) {
					map[cell] = Random.Int(4) == 0
							? Terrain.FURROWED_GRASS
							: Terrain.HIGH_GRASS;
				}
			}
		}

		int placed = 0;
		int attempts = 0;
		while (placed < OBSTACLE_COUNT && attempts++ < 2000) {
			int x = Random.Int(ARENA_LEFT, ARENA_LEFT + ARENA_SIZE);
			int y = Random.Int(ARENA_TOP, ARENA_TOP + ARENA_SIZE);
			int cell = cell(x, y);
			if (map[cell] != Terrain.EMPTY || isReservedRoute(x, y)) {
				continue;
			}
			map[cell] = Terrain.BARRICADE;
			placed++;
		}
	}

	private static boolean isReservedRoute(int x, int y) {
		return x >= 13 && x <= 15
				|| (x >= 11 && x <= 17 && y >= 15 && y <= 19);
	}

	static boolean isArenaCell(int cell) {
		int x = cell % WIDTH;
		int y = cell / WIDTH;
		return x >= ARENA_LEFT && x < ARENA_LEFT + ARENA_SIZE
				&& y >= ARENA_TOP && y < ARENA_TOP + ARENA_SIZE;
	}

	static boolean isDestructibleTerrain(int terrain) {
		return terrain == Terrain.BARRICADE
				|| terrain == Terrain.HIGH_GRASS
				|| terrain == Terrain.FURROWED_GRASS;
	}

	static boolean isProtectedCell(int cell) {
		return !isArenaCell(cell) || isReservedRoute(cell % WIDTH, cell / WIDTH);
	}

	static int selectRandomGentlemanCupCell(ArrayList<Integer> preferred,
			ArrayList<Integer> fallback) {
		ArrayList<Integer> candidates = preferred != null && !preferred.isEmpty()
				? preferred : fallback;
		return candidates == null || candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	static boolean isSafeZoneCell(int cell) {
		int x = cell % WIDTH;
		int y = cell / WIDTH;
		return x >= 13 && x <= 15 && y >= 31 && y <= 33;
	}

	static boolean shouldResetForSafeArrival(boolean locked, boolean encounterDefeated) {
		return locked && !encounterDefeated;
	}

	static boolean shouldBeginPrelude(boolean encounterBegun, boolean hero, int cell) {
		return !encounterBegun && hero && isArenaCell(cell);
	}

	static boolean shouldRelocateAlly(Char.Alignment alignment, int cell) {
		return alignment == Char.Alignment.ALLY && !isArenaCell(cell);
	}

	static boolean shouldRelocateCreature(int cell) {
		return !isArenaCell(cell);
	}

	static boolean isLegalAllyDestination(int cell, boolean passable, boolean openSpace,
			boolean large, int reservedCell, boolean occupied) {
		return isArenaCell(cell) && passable && (!large || openSpace)
				&& cell != reservedCell && !occupied;
	}

	static boolean isLegalCreatureDestination(int cell, boolean passable, boolean openSpace,
			boolean large, int reservedCell, boolean occupied) {
		return isArenaCell(cell) && passable && (!large || openSpace)
				&& cell != reservedCell && !occupied;
	}

	static boolean isLegalTeleportDestination(int cell, boolean passable, boolean openSpace,
			boolean large, boolean occupied) {
		return isArenaCell(cell) && passable && (!large || openSpace) && !occupied;
	}

	static void sealArena(int[] map) {
		map[SAFE_GATE] = Terrain.WALL;
	}

	static void unlockArena(int[] map) {
		map[SAFE_GATE] = Terrain.DOOR;
		map[EXIT_GATE] = Terrain.UNLOCKED_EXIT;
	}

	static int cell(int x, int y) {
		return x + y * WIDTH;
	}

	private static void fill(int[] map, int left, int top,
			int width, int height, int terrain) {
		for (int y = top; y < top + height; y++) {
			for (int x = left; x < left + width; x++) {
				map[cell(x, y)] = terrain;
			}
		}
	}
}
