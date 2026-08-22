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

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TestStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Dongyusangyu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DungeonDoctor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SurfaceShopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.Stylus;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SurfaceTownLevel extends Level {

	private static final int WIDTH = 46;
	private static final int HEIGHT = 34;

	private static final int MANOR_LEFT = 16;
	private static final int MANOR_TOP = 11;
	private static final int MANOR_WIDTH = 14;
	private static final int MANOR_HEIGHT = 10;
	private static final int MANOR_DOOR_X = 22;
	private static final int MANOR_DOOR_Y = 20;
	private static final int MANOR_STAIR_X = 22;
	private static final int MANOR_STAIR_Y = 14;

	private static final int DONGYUSANGYU_X = 7;
	private static final int DONGYUSANGYU_Y = 26;

	private static final int DUNGEON_DOCTOR_X = 22;
	private static final int DUNGEON_DOCTOR_Y = 4;
	private static final int TEST_STATUE_X = 27;
	private static final int TEST_STATUE_Y = 30;

	private static final int SHOP_LEFT = 33;
	private static final int SHOP_TOP = 3;
	private static final int SHOP_RIGHT = 43;
	private static final int SHOP_BOTTOM = 10;
	private static final int SHOP_DOOR_X = 38;
	private static final int SHOPKEEPER_X = 38;
	private static final int SHOPKEEPER_Y = 7;
	private static final int SHOP_SALE_DEPTH = 20;

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
		return SurfaceSeason.tilesTexture();
	}

	@Override
	public String waterTex() {
		return SurfaceSeason.waterTexture();
	}

	@Override
	protected boolean build() {
		setSize(WIDTH, HEIGHT);
		//fill(Terrain.GRASS);
		paintForest();
		paintLake();
		paintVillagePaths();
		paintHouse(4, 4, 8, 6, 8, 9,true);
		paintHouse(18, 2, 10, 6, 22, 7,true);
		paintHouse(SHOP_LEFT, SHOP_TOP,
				SHOP_RIGHT - SHOP_LEFT + 1, SHOP_BOTTOM - SHOP_TOP + 1,
				SHOP_DOOR_X, SHOP_BOTTOM,false);
		paintHouse(4, 23, 8, 7, 8, 29,true);
		paintHouse(35, 24, 7, 6, 38, 29,true);
		paintLockedManor();
		paintDongyusangyuHouseBookshelves();
		paintTownDetails();
		customTiles.add(new SurfaceGrassTilemap());
		customTiles.add(new SurfacePathTilemap());
		customTiles.add(new SurfaceInteriorFloorTilemap());

		entrance = cell(22, 32);
		exit = cell(MANOR_STAIR_X, MANOR_STAIR_Y);
		map[entrance] = Terrain.EXIT;
		map[exit] = Terrain.ENTRANCE;
		transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));
		transitions.add(new LevelTransition(
				this,
				exit,
				LevelTransition.Type.REGULAR_EXIT,
				1,
				TowerLevel.BRANCH,
				LevelTransition.Type.REGULAR_ENTRANCE));

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
				boolean southApproach = y >= height() - 4 && x >= 21 && x <= 24;
				boolean forestBand = x <= 2 || x >= width() - 3 || y <= 2 || y >= height() - 3;
				boolean grove = (x >= 2 && x <= 5 && y >= 10 && y <= 14)
						|| (x >= 42 && x <= 44 && y >= 3 && y <= 9)
						|| (x >= 42 && x <= 44 && y >= 24 && y <= 30);
				if (border && !southApproach) {
					map[cell(x, y)] = ((x * 13 + y * 7) % 4 == 0)
							? Terrain.REGION_DECO_ALT
							: Terrain.REGION_DECO;
				} else if ((forestBand || grove) && !southApproach) {
					paintForestTile(x, y);
				}
			}
		}
	}

	private void paintForestTile(int x, int y) {
		int roll = Math.abs(x * 31 + y * 17) % 5;
		if (roll <= 2) {
			map[cell(x, y)] = Terrain.REGION_DECO;
		} else {
			map[cell(x, y)] = Terrain.REGION_DECO_ALT;
		}
	}

	private void paintLake() {
		int cx = 39;
		int cy = 17;
		for (int y = 11; y <= 23; y++) {
			for (int x = 34; x <= 44; x++) {
				int dx = x - cx;
				int dy = y - cy;
				boolean ellipse = dx * dx * 36 + dy * dy * 25 <= 25 * 36;
				boolean westInlet = x == 34 && y >= 16 && y <= 19;
				if (ellipse || westInlet) {
					map[cell(x, y)] = Terrain.WATER;
				}
			}
		}
	}

	private void paintVillagePaths() {
		// Keep a clear row below the southern road so tree canopies do not cover it.
		for (int x = 7; x <= 39; x++) {
			map[cell(x, 31)] = Terrain.GRASS;
		}

		paintHorizontalPath(8, 38, 10, false);
		paintHorizontalPath(8, 38, 30, false);
		paintHorizontalPath(32, SHOP_DOOR_X, SHOP_BOTTOM + 1, true);
		paintVerticalPath(13, 10, 30);
		paintVerticalPath(32, 10, 30);
		paintVerticalPath(22, 8, 10);
		paintVerticalPath(22, 21, 32);
	}

	private void paintHorizontalPath(int left, int right, int y, boolean carveWater) {
		for (int x = left; x <= right; x++) {
			paintPath(x, y, carveWater);
		}
	}

	private void paintVerticalPath(int x, int top, int bottom) {
		for (int y = top; y <= bottom; y++) {
			paintPath(x, y, false);
		}
	}

	private void paintPath(int x, int y, boolean carveWater) {
		if (x <= 0 || y <= 0 || x >= width() - 1 || y >= height() - 1) {
			return;
		}
		int pos = cell(x, y);
		if (carveWater || map[pos] != Terrain.WATER) {
			map[pos] = Terrain.EMPTY_DECO;
		}
	}

	private void paintHouse(int left, int top, int w, int h, int doorX, int doorY,boolean statue) {
		for (int y = top; y < top + h; y++) {
			for (int x = left; x < left + w; x++) {
				boolean edge = x == left || y == top || x == left + w - 1 || y == top + h - 1;
				map[cell(x, y)] = edge ? Terrain.WALL_DECO : Terrain.EMPTY_SP;
			}
		}
		map[cell(doorX, doorY)] = Terrain.DOOR;
		map[cell(doorX, doorY + 1)] = Terrain.EMPTY_DECO;
		if (doorX + 1 < left + w - 1 && statue) {
			map[cell(doorX + 2, doorY - 1)] = Terrain.STATUE_SP;
		}
	}

	private void paintLockedManor() {
		int right = MANOR_LEFT + MANOR_WIDTH - 1;
		int bottom = MANOR_TOP + MANOR_HEIGHT - 1;
		for (int y = MANOR_TOP; y <= bottom; y++) {
			for (int x = MANOR_LEFT; x <= right; x++) {
				boolean edge = x == MANOR_LEFT || x == right || y == MANOR_TOP || y == bottom;
				map[cell(x, y)] = edge ? Terrain.WALL_DECO : Terrain.EMPTY_SP;
			}
		}
        map[cell(MANOR_DOOR_X, MANOR_DOOR_Y)] = Terrain.DOOR;
		map[cell(MANOR_STAIR_X, MANOR_STAIR_Y)] = Terrain.ENTRANCE;

		map[cell(MANOR_LEFT + 3, bottom - 2)] = Terrain.STATUE_SP;
		map[cell(right - 3, bottom - 2)] = Terrain.STATUE_SP;
		map[cell(MANOR_STAIR_X + 2, MANOR_STAIR_Y + 2)] = Terrain.PEDESTAL;
	}

	private void paintDongyusangyuHouseBookshelves() {
		for (int x = 5; x <= 10; x++) {
			map[cell(x, 24)] = Terrain.BOOKSHELF;
		}
		map[cell(10, 27)] = Terrain.ALCHEMY;
	}

	private void paintTownDetails() {
		map[cell(10, 17)] = Terrain.WELL;
		map[cell(27, 25)] = Terrain.STATUE;

		int[][] decorativeGrass = {
				{6, 13}, {7, 13},
				{13, 5}, {14, 5},
				{10, 15}, {11, 15},
				{30, 12}, {31, 12},
				{14, 24}, {14, 25},
				{30, 26}, {31, 26}
		};
        /*
		for (int[] point : decorativeGrass) {
			int pos = cell(point[0], point[1]);
			if (map[pos] == Terrain.GRASS) {
				map[pos] = Terrain.HIGH_GRASS;
			}
		}

         */

	}

	public static class SurfacePathTilemap extends CustomTilemap {

		static String tilesTexture() {
			return SurfaceSeason.tilesTexture();
		}

		{
			texture = tilesTexture();
			tileW = WIDTH;
			tileH = HEIGHT;
		}

		@Override
		public Tilemap create() {
			Tilemap visual = super.create();
			Level level = Dungeon.level;
			int[] data = new int[tileW * tileH];

			for (int y = 0; y < tileH; y++) {
				for (int x = 0; x < tileW; x++) {
					int index = x + y * tileW;
					if (level.map[index] != Terrain.EMPTY_DECO) {
						data[index] = -1;
						continue;
					}

					data[index] = tileForCell(level, x, y);
				}
			}

			visual.map(data, tileW);
			return visual;
		}

		static int tileForConnections(boolean top, boolean right, boolean bottom, boolean left) {
			boolean vertical = top || bottom;
			boolean horizontal = left || right;
			if (vertical && !horizontal) {
				return DungeonTileSheet.FLOOR_DECO;
			}
			if (horizontal && !vertical) {
				return DungeonTileSheet.FLOOR_ALT_1;
			}
			return DungeonTileSheet.FLOOR_DECO_ALT;
		}

		static int tileForCell(Level level, int x, int y) {
			return tileForConnections(
					connectsToPath(level, x, y - 1),
					connectsToPath(level, x + 1, y),
					connectsToPath(level, x, y + 1),
					connectsToPath(level, x - 1, y));
		}

		private static boolean connectsToPath(Level level, int x, int y) {
			if (x < 0 || y < 0 || x >= level.width() || y >= level.height()) {
				return false;
			}
			int terrain = level.map[x + y * level.width()];
			return terrain == Terrain.EMPTY_DECO
					|| terrain == Terrain.DOOR
					|| terrain == Terrain.OPEN_DOOR
					|| terrain == Terrain.LOCKED_DOOR
					|| terrain == Terrain.HERO_LKD_DR
					|| terrain == Terrain.CRYSTAL_DOOR
					|| terrain == Terrain.ENTRANCE
					|| terrain == Terrain.EXIT;
		}
	}

	public static class SurfaceGrassTilemap extends CustomTilemap {

		private static final int GRASS_BLOCK_SIZE = 3;
		private static final int GRASS_SPECKLE_RGBA = 0x57A244FF;
		private static final int GRASS_BASE_RGBA = 0x549B42FF;
		private static final int[] SURFACE_GRASS_TILES = {
				DungeonTileSheet.FLOOR,
				DungeonTileSheet.GRASS,
				DungeonTileSheet.EMBERS,
				DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.EMBERS_ALT
		};
		private static final int[] SURFACE_GRASS_PATTERN = {
				DungeonTileSheet.FLOOR,
				DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.EMBERS,
				DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.EMBERS,
				DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.EMBERS_ALT,
				DungeonTileSheet.EMBERS,
				DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.EMBERS,
				DungeonTileSheet.GRASS,
				DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.EMBERS,
				DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.EMBERS
		};

		static String tilesTexture() {
			return SurfaceSeason.tilesTexture();
		}

		static String cleanGrassTextureKey() {
			return SurfaceSeason.grassTextureCacheKey();
		}

		{
			texture = tilesTexture();
			tileW = WIDTH;
			tileH = HEIGHT;
		}

		@Override
		public Tilemap create() {
			texture = cleanGrassTexture();
			Tilemap visual = super.create();
			Level level = Dungeon.level;
			int[] data = new int[tileW * tileH];

			for (int y = 0; y < tileH; y++) {
				for (int x = 0; x < tileW; x++) {
					int index = x + y * tileW;
					data[index] = tileForCell(level, x, y);
				}
			}

			visual.map(data, tileW);
			return visual;
		}

		private static Object cleanGrassTexture() {
			String sourceTexture = tilesTexture();
			String cacheKey = cleanGrassTextureKey();
			if (TextureCache.contains(cacheKey)) {
				return cacheKey;
			}

			Pixmap source = TextureCache.getBitmap(sourceTexture);
			if (source == null) {
				return sourceTexture;
			}

			SmartTexture cleaned = TextureCache.create(
					cacheKey, source.getWidth(), source.getHeight());
			cleaned.filter(SmartTexture.NEAREST, SmartTexture.NEAREST);
			cleaned.bitmap.setBlending(Pixmap.Blending.None);
			cleaned.bitmap.drawPixmap(source, 0, 0);

			int tilesPerRow = source.getWidth() / SIZE;
			for (int tile : SURFACE_GRASS_TILES) {
				int left = (tile % tilesPerRow) * SIZE;
				int top = (tile / tilesPerRow) * SIZE;
				for (int y = top; y < top + SIZE; y++) {
					for (int x = left; x < left + SIZE; x++) {
						int color = source.getPixel(x, y);
						cleaned.bitmap.drawPixel(x, y, cleanGrassPixel(color));
					}
				}
			}

			source.dispose();
			return cacheKey;
		}

		static int cleanGrassPixel(int color) {
			return color == GRASS_SPECKLE_RGBA ? GRASS_BASE_RGBA : color;
		}

		static int tileForCell(Level level, int x, int y) {
			if (x < 0 || y < 0 || x >= level.width() || y >= level.height()) {
				return -1;
			}
			return tileForTerrain(level.map[x + y * level.width()], x, y);
		}

		static int tileForTerrain(int terrain, int x, int y) {
			if (terrain != Terrain.GRASS
					&& terrain != Terrain.HIGH_GRASS
					&& terrain != Terrain.FURROWED_GRASS) {
				return -1;
			}

			int blockX = x / GRASS_BLOCK_SIZE;
			int blockY = y / GRASS_BLOCK_SIZE;
			int patternIndex = Math.floorMod(blockX * 7 + blockY * 11, SURFACE_GRASS_PATTERN.length);
			return SURFACE_GRASS_PATTERN[patternIndex];
		}

		static int variantForTile(int tile) {
			for (int i = 0; i < SURFACE_GRASS_TILES.length; i++) {
				if (SURFACE_GRASS_TILES[i] == tile) {
					return i;
				}
			}
			return -1;
		}
	}

	public static class SurfaceInteriorFloorTilemap extends CustomTilemap {

		static String tilesTexture() {
			return SurfaceSeason.tilesTexture();
		}

		{
			texture = tilesTexture();
			tileW = WIDTH;
			tileH = HEIGHT;
		}

		@Override
		public Tilemap create() {
			Tilemap visual = super.create();
			Level level = Dungeon.level;
			int[] data = new int[tileW * tileH];

			for (int y = 0; y < tileH; y++) {
				for (int x = 0; x < tileW; x++) {
					int index = x + y * tileW;
					data[index] = tileForTerrain(level.map[index]);
				}
			}

			visual.map(data, tileW);
			return visual;
		}

		static int tileForTerrain(int terrain) {
			return terrain == Terrain.EMPTY_SP ? DungeonTileSheet.FLOOR_SP : -1;
		}
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition.type == LevelTransition.Type.REGULAR_ENTRANCE) {
			return false;
		}
		if (Dungeon.depth == 0 && Dungeon.branch == 0
				&& transition.destDepth == 1 && transition.destBranch == TowerLevel.BRANCH) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions(
							Messages.get(SurfaceTownLevel.class, "tower_entry_title"),
							Messages.get(SurfaceTownLevel.class, "tower_entry_desc"),
							Messages.get(SurfaceTownLevel.class, "tower_entry_enter"),
							Messages.get(SurfaceTownLevel.class, "tower_entry_cancel")) {
						@Override
						protected void onSelect(int index) {
							if (index == 0) {
								SurfaceTownLevel.super.activateTransition(hero, transition);
							}
						}
					});
				}
			});
			return false;
		}
		return super.activateTransition(hero, transition);
	}

	@Override
	public void buildFlagMaps() {
		super.buildFlagMaps();
		int manorDoorInterior = cell(MANOR_DOOR_X, MANOR_DOOR_Y - 1);
		passable[manorDoorInterior] = false;
		avoid[manorDoorInterior] = false;
		solid[manorDoorInterior] = true;
		losBlocking[manorDoorInterior] = true;
		openSpace[manorDoorInterior] = false;
	}

	@Override
	protected void createMobs() {
		boolean hasDongyusangyu = false;
		boolean hasDungeonDoctor = false;
		boolean hasShopkeeper = false;
		boolean hasTestStatue = false;
		for (Mob mob : mobs) {
			if (mob instanceof Dongyusangyu) {
				hasDongyusangyu = true;
			}
			if (mob instanceof SurfaceShopkeeper) {
				hasShopkeeper = true;
			}
			if (mob instanceof DungeonDoctor) {
				hasDungeonDoctor = true;
			}
			if (mob instanceof TestStatue) {
				hasTestStatue = true;
			}
		}
		if (!hasDongyusangyu) {
			Dongyusangyu dongyusangyu = new Dongyusangyu();
			dongyusangyu.pos = cell(DONGYUSANGYU_X, DONGYUSANGYU_Y);
			mobs.add(dongyusangyu);
		}
		if (!hasShopkeeper) {
			SurfaceShopkeeper shopkeeper = new SurfaceShopkeeper();
			shopkeeper.pos = cell(SHOPKEEPER_X, SHOPKEEPER_Y);
			mobs.add(shopkeeper);
		}
		if (!hasDungeonDoctor) {
			DungeonDoctor dungeonDoctor = new DungeonDoctor();
			dungeonDoctor.pos = cell(DUNGEON_DOCTOR_X, DUNGEON_DOCTOR_Y);
			mobs.add(dungeonDoctor);
		}
		if (!hasTestStatue) {
			TestStatue testStatue = new TestStatue();
			testStatue.pos = cell(TEST_STATUE_X, TEST_STATUE_Y);
			mobs.add(testStatue);
		}
	}

	@Override
	protected void createItems() {
		ArrayList<Item> stock = generateSurfaceShopItems();
		ArrayList<Integer> shelfCells = surfaceShopShelfCells();
		for (int i = 0; i < stock.size() && i < shelfCells.size(); i++) {
			Heap heap = drop(stock.get(i), shelfCells.get(i));
			heap.type = Heap.Type.FOR_SALE;
			heap.saleDepth(SHOP_SALE_DEPTH);
		}
	}

	private ArrayList<Integer> surfaceShopShelfCells() {
		ArrayList<Integer> result = new ArrayList<>();
		int left = SHOP_LEFT + 1;
		int top = SHOP_TOP + 1;
		int right = SHOP_RIGHT - 1;
		int bottom = SHOP_BOTTOM - 1;

		for (int x = left; x <= right; x++) {
			result.add(cell(x, top));
		}
		for (int y = top + 1; y <= bottom; y++) {
			result.add(cell(right, y));
		}
		for (int x = right - 1; x >= left; x--) {
			if (x != SHOP_DOOR_X) {
				result.add(cell(x, bottom));
			}
		}
		for (int y = bottom - 1; y > top; y--) {
			result.add(cell(left, y));
		}
		return result;
	}

	static ArrayList<Item> generateSurfaceShopItems() {
		ArrayList<Item> items = new ArrayList<>();

		MeleeWeapon weapon = Generator.randomWeapon(4);
		weapon.enchant(null);
		weapon.cursed = false;
		weapon.level(0);
		weapon.identify(false);
		items.add(weapon);

		Armor armor = Generator.randomArmor(4);
		armor.cursed = false;
		armor.level(0);
		armor.identify(false);
		items.add(armor);

		MissileWeapon missile = Generator.randomMissile(4);
		missile.enchant(null);
		missile.cursed = false;
		missile.level(0);
		missile.identify(false);
		items.add(missile);

		items.add(TippedDart.randomTipped(2));
		items.add(new PotionOfHealing());
		items.add(Generator.randomUsingDefaults(Generator.Category.POTION));
		items.add(Generator.randomUsingDefaults(Generator.Category.POTION));
		items.add(new ScrollOfIdentify());
		items.add(new ScrollOfRemoveCurse());
		items.add(new ScrollOfMagicMapping());
		for (int i = 0; i < 2; i++) {
			items.add(Random.Int(2) == 0
					? Generator.randomUsingDefaults(Generator.Category.POTION)
					: Generator.randomUsingDefaults(Generator.Category.SCROLL));
		}
		items.add(new SmallRation());
		items.add(new SmallRation());
		switch (Random.Int(4)) {
			case 0:
				items.add(new Bomb());
				break;
			case 1:
			case 2:
				items.add(new Bomb.DoubleBomb());
				break;
			default:
				items.add(new Honeypot());
				break;
		}
		items.add(new Ankh());
		items.add(new StoneOfAugmentation());
		items.add(new Torch());
		items.add(new Stylus());
		items.add(new ScrollOfSublimation().type("GOO"));
		items.add(new ScrollOfSublimation().type("TENGU"));
		items.add(new ScrollOfSublimation().type("DM300"));
		items.add(new ScrollOfSublimation().type("DWARFKING"));
		items.add(new ScrollOfSublimation().type("YOG"));

		Random.shuffle(items);
		return items;
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
