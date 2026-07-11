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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SurfaceTownLevel extends Level {

	private static final int WIDTH = 48;
	private static final int HEIGHT = 32;

	{
		color1 = 0x2f6d36;
		color2 = 0x9ad36a;
		viewDistance = 12;
	}

	@Override
	public void create() {
		Random.pushGenerator(Dungeon.seedCurDepth());
		width = height = length = 0;
		transitions = new ArrayList<>();
		mobs = new HashSet<>();
		heaps = new com.watabou.utils.SparseArray<>();
		blobs = new HashMap<>();
		plants = new com.watabou.utils.SparseArray<>();
		traps = new com.watabou.utils.SparseArray<>();
		customTiles = new ArrayList<>();
		customWalls = new ArrayList<>();

		build();
		buildFlagMaps();
		cleanWalls();
		createMobs();
		createItems();
		Random.popGenerator();
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_SURFACE_LUSH;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_SURFACE_LUSH;
	}

	@Override
	protected boolean build() {
		setSize(WIDTH, HEIGHT);
		fill(Terrain.GRASS);
		paintForest();
		paintLake();
		paintVillagePaths();
		paintHouse(6, 5, 10, 8, 10, 12);
		paintHouse(19, 4, 12, 9, 24, 12);
		paintHouse(9, 19, 12, 8, 15, 19);
		paintTownDetails();

		entrance = cell(24, 28);
		exit = cell(24, 15);
		map[entrance] = Terrain.ENTRANCE;
		map[exit] = Terrain.EXIT;
		transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));
		transitions.add(new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT));

		return true;
	}

	private void fill(int terrain) {
		for (int i = 0; i < length(); i++) {
			map[i] = terrain;
		}
	}

	private int cell(int x, int y) {
		return x + y * width();
	}

	private void paintForest() {
		for (int y = 0; y < height(); y++) {
			for (int x = 0; x < width(); x++) {
				boolean border = x == 0 || y == 0 || x == width() - 1 || y == height() - 1;
				boolean northWoods = y < 4 && (x < 18 || x > 29);
				boolean westWoods = x < 5 && y < 25;
				boolean eastWoods = x > 39 && y < 26;
				boolean southWoods = y > 28 && (x < 18 || x > 30);
				boolean grove = (x > 31 && x < 40 && y > 17 && y < 25)
						|| (x > 27 && x < 34 && y > 22 && y < 29)
						|| (x > 4 && x < 10 && y > 12 && y < 18);
				if (border) {
					map[cell(x, y)] = ((x * 13 + y * 7) % 5 == 0) ? Terrain.WALL_DECO : Terrain.WALL;
				} else if (northWoods || westWoods || eastWoods || southWoods || grove) {
					paintForestTile(x, y);
				} else if ((x * 17 + y * 11) % 13 == 0) {
					map[cell(x, y)] = Terrain.HIGH_GRASS;
				} else if ((x * 5 + y * 19) % 23 == 0) {
					map[cell(x, y)] = Terrain.EMPTY_DECO;
				}
			}
		}
	}

	private void paintForestTile(int x, int y) {
		int roll = Math.abs(x * 31 + y * 17) % 11;
		if (roll == 0) {
			map[cell(x, y)] = Terrain.REGION_DECO;
		} else if (roll == 1) {
			map[cell(x, y)] = Terrain.REGION_DECO_ALT;
		} else if (roll <= 7) {
			map[cell(x, y)] = Terrain.HIGH_GRASS;
		} else {
			map[cell(x, y)] = Terrain.GRASS;
		}
	}

	private void paintLake() {
		int cx = 33;
		int cy = 14;
		for (int y = 7; y <= 20; y++) {
			for (int x = 25; x <= 42; x++) {
				int dx = x - cx;
				int dy = y - cy;
				if (dx * dx * 36 + dy * dy * 64 <= 36 * 64) {
					map[cell(x, y)] = Terrain.WATER;
				}
			}
		}
		for (int x = 25; x <= 30; x++) {
			map[cell(x, 14)] = Terrain.EMPTY;
			map[cell(x, 15)] = Terrain.EMPTY;
		}
		for (int y = 12; y <= 17; y++) {
			map[cell(30, y)] = Terrain.EMPTY;
		}
	}

	private void paintVillagePaths() {
		for (int y = 6; y <= 28; y++) {
			paintPath(24, y);
		}
		for (int x = 9; x <= 31; x++) {
			paintPath(x, 15);
		}
		for (int x = 14; x <= 24; x++) {
			paintPath(x, 23);
		}
		for (int y = 15; y <= 23; y++) {
			paintPath(15, y);
		}
		for (int y = 12; y <= 15; y++) {
			paintPath(10, y);
		}
		for (int x = 10; x <= 24; x++) {
			paintPath(x, 12);
		}
	}

	private void paintPath(int x, int y) {
		if (x <= 0 || y <= 0 || x >= width() - 1 || y >= height() - 1) {
			return;
		}
		int pos = cell(x, y);
		if (map[pos] != Terrain.WATER) {
			map[pos] = Terrain.EMPTY;
		}
	}

	private void paintHouse(int left, int top, int w, int h, int doorX, int doorY) {
		for (int y = top; y < top + h; y++) {
			for (int x = left; x < left + w; x++) {
				boolean edge = x == left || y == top || x == left + w - 1 || y == top + h - 1;
				map[cell(x, y)] = edge ? Terrain.WALL_DECO : Terrain.EMPTY_SP;
			}
		}
		map[cell(doorX, doorY)] = Terrain.DOOR;
		map[cell(doorX, doorY + 1)] = Terrain.EMPTY;
		if (doorX - 1 > left) {
			map[cell(doorX - 2, doorY - 1)] = Terrain.BOOKSHELF;
		}
		if (doorX + 1 < left + w - 1) {
			map[cell(doorX + 2, doorY - 1)] = Terrain.STATUE;
		}
	}

	private void paintTownDetails() {
		for (int x = 17; x <= 20; x++) {
			for (int y = 13; y <= 16; y++) {
				if ((x + y) % 2 == 0) {
					map[cell(x, y)] = Terrain.HIGH_GRASS;
				}
			}
		}
		map[cell(21, 17)] = Terrain.WELL;
		map[cell(18, 18)] = Terrain.PEDESTAL;
		map[cell(28, 23)] = Terrain.STATUE_SP;
		map[cell(35, 22)] = Terrain.REGION_DECO;
		map[cell(36, 22)] = Terrain.REGION_DECO_ALT;
		for (int x = 6; x <= 13; x++) {
			map[cell(x, 17)] = Terrain.HIGH_GRASS;
		}
		for (int x = 34; x <= 38; x++) {
			map[cell(x, 26)] = Terrain.HIGH_GRASS;
		}
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition.type == LevelTransition.Type.REGULAR_ENTRANCE) {
			return false;
		}
		return super.activateTransition(hero, transition);
	}

	@Override
	protected void createMobs() {
		// The surface town is intentionally peaceful.
	}

	@Override
	protected void createItems() {
		// Keep this opening floor decorative and deterministic.
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	public int randomRespawnCell(Char ch) {
		return entrance;
	}

	@Override
	public int randomDestination(Char ch) {
		return entrance;
	}
}
