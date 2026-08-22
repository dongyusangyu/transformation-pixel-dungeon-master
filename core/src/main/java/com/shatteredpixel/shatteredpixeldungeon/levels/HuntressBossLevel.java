/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.BlindingDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Mageroyal;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HuntressBossLevel extends Level {
	private boolean bossPlantClaimInProgress;

	private static final int WIDTH = 31;
	private static final int HEIGHT = 32;
	private static final int ENTRANCE_X = 15;
	private static final int ENTRANCE_Y = 29;
	private static final int EXIT_X = 15;
	private static final int EXIT_Y = 2;
	private static final int TRIGGER_X = 15;
	private static final int TRIGGER_Y = 22;
	private static final int GATE_X = 15;
	private static final int GATE_Y = 23;
	private static final int BOSS_X = 15;
	private static final int BOSS_Y = 5;
	static final int MIN_OPENING_SPAWN_DISTANCE = 6;
	static final int MAX_OPENING_SPAWN_DISTANCE = 9;
	private static final int TRIGGER_SPAWN_BUFFER = 2;
	private static final int PREFERRED_FADELEAF_SEPARATION = 6;
	private static final Rect ARENA = new Rect(3, 2, 28, 24);
	private static final String STATE = "huntress_boss_state";
	private static final String COVER_CLUSTER_CELLS = "cover_cluster_cells";
	private static final String COVER_CLUSTER_SIZES = "cover_cluster_sizes";
	static final int COVER_CLUSTER_COUNT = 8;
	static final int COVER_CELLS_PER_CLUSTER = 4;
	private static final long COVER_SEED_SALT = 150032L;
	private static final int COVER_LAYOUT_ATTEMPTS = 64;
	private static final int[][] COVER_ANCHORS = {
			{6, 6}, {11, 9}, {20, 6}, {24, 10},
			{6, 15}, {11, 18}, {20, 15}, {24, 19}
	};
	private static final int[][][] COVER_SHAPES = {
			{{0, 0}, {1, 0}, {0, 1}, {1, 1}},
			{{0, 0}, {0, 1}, {0, 2}, {1, 2}},
			{{0, 0}, {1, 0}, {2, 0}, {1, 1}},
			{{0, 0}, {1, 0}, {1, 1}, {2, 1}},
			{{0, 1}, {1, 1}, {1, 0}, {2, 0}},
			{{0, 0}, {1, 0}, {2, 0}, {0, 1}},
			{{0, 0}, {1, 0}, {2, 0}, {2, 1}},
			{{0, 0}, {0, 1}, {1, 1}, {2, 1}}
	};

	@SuppressWarnings("unchecked")
	private static final Class<? extends Plant.Seed>[] ARENA_SEEDS = new Class[]{
			Sungrass.Seed.class,
			Fadeleaf.Seed.class,
			Icecap.Seed.class,
			Firebloom.Seed.class,
			Sorrowmoss.Seed.class,
			Swiftthistle.Seed.class,
			Blindweed.Seed.class,
			Stormvine.Seed.class,
			Earthroot.Seed.class,
			Mageroyal.Seed.class,
			Starflower.Seed.class
	};

	public enum State {
		START,
		INTRO,
		FIGHT,
		WON
	}

	enum WardenFeature {
		NONE,
		FURROW,
		PLANT,
		TENTACLE
	}

	static final class WardenArenaAllocation {
		final Set<Integer> furrowCells;
		final Set<Integer> plantCells;
		final Set<Integer> tentacleCells;

		WardenArenaAllocation(Set<Integer> furrowCells,
				Set<Integer> plantCells, Set<Integer> tentacleCells) {
			this.furrowCells = furrowCells;
			this.plantCells = plantCells;
			this.tentacleCells = tentacleCells;
		}
	}

	private static final class WardenArenaApplication {
		final WardenArenaAllocation allocation;
		final List<Plant> plants;
		final List<HuntressBoss.HuntressTentacle> tentacles;

		WardenArenaApplication(WardenArenaAllocation allocation,
				List<Plant> plants, List<HuntressBoss.HuntressTentacle> tentacles) {
			this.allocation = allocation;
			this.plants = plants;
			this.tentacles = tentacles;
		}
	}

	static final class FadeleafDestinations {
		final int bossCell;
		final int targetCell;

		FadeleafDestinations(int bossCell, int targetCell) {
			this.bossCell = bossCell;
			this.targetCell = targetCell;
		}
	}

	private State state = State.START;
	private List<Set<Integer>> coverClusters = new ArrayList<>();

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

	static WardenFeature wardenFeatureForRoll(float roll) {
		if (roll < 0.02f) {
			return WardenFeature.TENTACLE;
		}
		if (roll < 0.12f) {
			return WardenFeature.PLANT;
		}
		if (roll < 0.62f) {
			return WardenFeature.FURROW;
		}
		return WardenFeature.NONE;
	}

	static int arenaSeedClassCount() {
		return ARENA_SEEDS.length;
	}

	static Class<? extends Plant.Seed> arenaSeedClassForIndex(int index) {
		return ARENA_SEEDS[index];
	}

	private static Class<? extends Plant.Seed> arenaSeedClassForCurrentGenerator() {
		return arenaSeedClassForIndex(Random.Int(arenaSeedClassCount()));
	}

	static boolean isLegalVegetationTerrain(int terrain) {
		return terrain == Terrain.EMPTY
				|| terrain == Terrain.EMPTY_DECO
				|| terrain == Terrain.FURROWED_GRASS;
	}

	static boolean isPreferredBossSpawnDistance(int distance) {
		return distance >= MIN_OPENING_SPAWN_DISTANCE
				&& distance <= MAX_OPENING_SPAWN_DISTANCE;
	}

	static int preferredRangeDeviation(int distance) {
		if (distance < MIN_OPENING_SPAWN_DISTANCE) {
			return MIN_OPENING_SPAWN_DISTANCE - distance;
		}
		if (distance > MAX_OPENING_SPAWN_DISTANCE) {
			return distance - MAX_OPENING_SPAWN_DISTANCE;
		}
		return 0;
	}

	boolean isBasicBossSpawnCell(int cell) {
		return isBasicArenaDestinationCell(cell) && Actor.findChar(cell) == null;
	}

	private boolean isBasicArenaDestinationCell(int cell) {
		return isArenaCell(cell)
				&& passable[cell]
				&& isLegalVegetationTerrain(map[cell])
				&& cell != entrance()
				&& cell != exit()
				&& cell != GATE_X + GATE_Y * width()
				&& distance(cell, triggerCell()) > TRIGGER_SPAWN_BUFFER
				&& plants.get(cell) == null
				&& heaps.get(cell) == null;
	}

	boolean hasBossSpawnProjectileLine(int from, int target) {
		return new Ballistica(from, target, Ballistica.PROJECTILE)
				.collisionPos.intValue() == target;
	}

	ArrayList<Integer> strictBossSpawnCandidates(int heroPos) {
		ArrayList<Integer> result = new ArrayList<>();
		for (int cell = 0; cell < length(); cell++) {
			if (isBasicBossSpawnCell(cell)
					&& isPreferredBossSpawnDistance(distance(cell, heroPos))
					&& hasBossSpawnProjectileLine(cell, heroPos)) {
				result.add(cell);
			}
		}
		return result;
	}

	int selectBossSpawnCell(int heroPos) {
		ArrayList<Integer> strict = strictBossSpawnCandidates(heroPos);
		if (!strict.isEmpty()) {
			return Random.element(strict);
		}
		throw new IllegalStateException("No strict Huntress boss opening spawn cell");
	}

	static List<Set<Integer>> coverClustersForSeed(long dungeonSeed) {
		Random.pushGenerator(dungeonSeed + COVER_SEED_SALT);
		try {
			ArrayList<Set<Integer>> result = new ArrayList<>();
			for (int i = 0; i < COVER_ANCHORS.length; i++) {
				int[][] shape = COVER_SHAPES[Random.Int(COVER_SHAPES.length)];
				LinkedHashSet<Integer> cluster = new LinkedHashSet<>();
				for (int[] offset : shape) {
					int x = COVER_ANCHORS[i][0] + offset[0];
					int y = COVER_ANCHORS[i][1] + offset[1];
					cluster.add(x + y * WIDTH);
				}
				result.add(cluster);
			}
			return result;
		} finally {
			Random.popGenerator();
		}
	}

	static Set<Integer> coverCellsForSeed(long dungeonSeed) {
		LinkedHashSet<Integer> result = new LinkedHashSet<>();
		for (Set<Integer> cluster : coverClustersForSeed(dungeonSeed)) {
			result.addAll(cluster);
		}
		return result;
	}

	public List<Set<Integer>> coverClusters() {
		ArrayList<Set<Integer>> copy = new ArrayList<>();
		for (Set<Integer> cluster : coverClusters) {
			copy.add(new LinkedHashSet<>(cluster));
		}
		return copy;
	}

	static boolean isOrthogonallyConnected(Set<Integer> cells) {
		if (cells.isEmpty()) {
			return false;
		}
		Set<Integer> visited = new HashSet<>();
		ArrayList<Integer> pending = new ArrayList<>();
		pending.add(cells.iterator().next());
		while (!pending.isEmpty()) {
			int cell = pending.remove(pending.size() - 1);
			if (!visited.add(cell)) {
				continue;
			}
			int x = cell % WIDTH;
			int[] neighbours = {cell - WIDTH, cell + WIDTH, cell - 1, cell + 1};
			for (int neighbour : neighbours) {
				if ((neighbour == cell - 1 && x == 0)
						|| (neighbour == cell + 1 && x == WIDTH - 1)) {
					continue;
				}
				if (cells.contains(neighbour) && !visited.contains(neighbour)) {
					pending.add(neighbour);
				}
			}
		}
		return visited.size() == cells.size();
	}

	boolean isReservedEncounterCell(int cell) {
		int entrance = ENTRANCE_X + ENTRANCE_Y * width();
		int exit = EXIT_X + EXIT_Y * width();
		int trigger = TRIGGER_X + TRIGGER_Y * width();
		int gate = GATE_X + GATE_Y * width();
		int boss = BOSS_X + BOSS_Y * width();
		return cell == entrance || cell == exit || cell == gate || cell == boss
				|| distance(cell, trigger) <= 2;
	}

	boolean hasCriticalArenaPaths(Set<Integer> blockedCoverCells) {
		boolean[] traversable = new boolean[length()];
		for (int cell = 0; cell < length(); cell++) {
			traversable[cell] = (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0
					&& !blockedCoverCells.contains(cell);
		}
		int entrance = ENTRANCE_X + ENTRANCE_Y * width();
		int exit = EXIT_X + EXIT_Y * width();
		int trigger = TRIGGER_X + TRIGGER_Y * width();
		int boss = BOSS_X + BOSS_Y * width();
		PathFinder.buildDistanceMap(entrance, traversable);
		boolean entrancePaths = PathFinder.distance[trigger] < Integer.MAX_VALUE
				&& PathFinder.distance[exit] < Integer.MAX_VALUE;
		PathFinder.buildDistanceMap(trigger, traversable);
		return entrancePaths && PathFinder.distance[boss] < Integer.MAX_VALUE;
	}

	private boolean validCoverLayout(List<Set<Integer>> clusters) {
		if (clusters.size() != COVER_CLUSTER_COUNT) {
			return false;
		}
		Set<Integer> all = new HashSet<>();
		for (Set<Integer> cluster : clusters) {
			if (cluster.size() != COVER_CELLS_PER_CLUSTER
					|| !isOrthogonallyConnected(cluster)) {
				return false;
			}
			for (int cell : cluster) {
				if (!isArenaCell(cell) || isReservedEncounterCell(cell)
						|| (Terrain.flags[map[cell]] & Terrain.PASSABLE) == 0
						|| !all.add(cell)) {
					return false;
				}
			}
		}
		return all.size() == COVER_CLUSTER_COUNT * COVER_CELLS_PER_CLUSTER
				&& hasCriticalArenaPaths(all);
	}

	private List<Set<Integer>> clustersUsingShape(int shapeIndex) {
		ArrayList<Set<Integer>> result = new ArrayList<>();
		for (int[] anchor : COVER_ANCHORS) {
			LinkedHashSet<Integer> cluster = new LinkedHashSet<>();
			for (int[] offset : COVER_SHAPES[shapeIndex]) {
				cluster.add(anchor[0] + offset[0]
						+ (anchor[1] + offset[1]) * WIDTH);
			}
			result.add(cluster);
		}
		return result;
	}

	private List<Set<Integer>> validatedCoverClusters(long dungeonSeed) {
		for (int attempt = 0; attempt < COVER_LAYOUT_ATTEMPTS; attempt++) {
			List<Set<Integer>> candidate = coverClustersForSeed(
					dungeonSeed + attempt * 0x9E3779B9L);
			if (validCoverLayout(candidate)) {
				return candidate;
			}
		}
		List<Set<Integer>> fallback = clustersUsingShape(0);
		if (!validCoverLayout(fallback)) {
			throw new IllegalStateException("Huntress arena fallback cover layout is invalid");
		}
		return fallback;
	}

	static WardenArenaAllocation allocateWardenArena(List<Integer> legalCells,
			Set<Integer> blockedCells) {
		Set<Integer> furrows = new LinkedHashSet<>();
		Set<Integer> plants = new LinkedHashSet<>();
		Set<Integer> tentacles = new LinkedHashSet<>();
		for (int cell : legalCells) {
			if (blockedCells.contains(cell)) {
				continue;
			}
			Random.pushGenerator(Dungeon.seed + cell);
			try {
				WardenFeature feature = wardenFeatureForRoll(Random.Float());
				if (feature == WardenFeature.FURROW) {
					furrows.add(cell);
				} else if (feature == WardenFeature.PLANT) {
					plants.add(cell);
				} else if (feature == WardenFeature.TENTACLE) {
					tentacles.add(cell);
				}
			} finally {
				Random.popGenerator();
			}
		}
		return new WardenArenaAllocation(furrows, plants, tentacles);
	}

	static int terrainForWardenFeature(int baseTerrain, WardenFeature feature) {
		if (feature == WardenFeature.NONE) {
			return baseTerrain;
		}
		if (feature == WardenFeature.FURROW) {
			return Terrain.FURROWED_GRASS;
		}
		return baseTerrain == Terrain.EMPTY_DECO ? Terrain.EMPTY_DECO : Terrain.EMPTY;
	}

	@Override
	protected boolean build() {
		setSize(WIDTH, HEIGHT);
		Painter.fill(this, 0, 0, WIDTH, HEIGHT, Terrain.WALL);

		// Main arena and its solid outer rim.
		Painter.fill(this, 2, 1, 27, 24, Terrain.WALL);
		Painter.fill(this, ARENA, Terrain.EMPTY);

		// A compact entrance buffer which remains vegetation-free.
		Painter.fill(this, 12, 23, 7, 8, Terrain.WALL);
		Painter.fill(this, 13, 24, 5, 6, Terrain.EMPTY);
		Painter.set(this, 15, 23, Terrain.EMPTY);

		int entrance = ENTRANCE_X + ENTRANCE_Y * width();
		int exit = EXIT_X + EXIT_Y * width();
		Painter.set(this, entrance, Terrain.ENTRANCE);
		Painter.set(this, exit, Terrain.EXIT);
		transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));
		transitions.add(new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT));

		coverClusters = validatedCoverClusters(Dungeon.seedForDepth(15, 0));
		for (Set<Integer> cluster : coverClusters) {
			for (int cell : cluster) {
				Painter.set(this, cell, Terrain.WALL);
			}
		}

		ArrayList<Integer> vegetation = vegetationCells();
		Random.pushGenerator(Dungeon.seedForDepth(15, 0) + 150015L);
		try {
			Random.shuffle(vegetation);
			int initialFurrows = Math.round(vegetation.size() * 0.10f);
			for (int i = 0; i < initialFurrows; i++) {
				map[vegetation.get(i)] = Terrain.FURROWED_GRASS;
			}
		} finally {
			Random.popGenerator();
		}

		state = State.START;
		return true;
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
		int leftSupply = ENTRANCE_X - 2 + (ENTRANCE_Y - 2) * width();
		int rightSupply = ENTRANCE_X + 2 + (ENTRANCE_Y - 2) * width();
		drop(createEntranceDart().quantity(1), leftSupply);
		drop(createEntranceDart().quantity(1), rightSupply);
	}

	Item createEntranceDart() {
		return com.watabou.utils.Reflection.newInstance(entranceSupplyItemClass());
	}

	static Class<? extends Item> entranceSupplyItemClass() {
		return BlindingDart.class;
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	public void playLevelMusic() {
		if (state == State.FIGHT) {
			Music.INSTANCE.play(BossHealthBar.isBleeding()
					? Assets.Music.CAVES_BOSS_FINALE : Assets.Music.CAVES_BOSS, true);
		} else if (state == State.WON) {
			Music.INSTANCE.playTracks(CavesLevel.CAVES_TRACK_LIST,
					CavesLevel.CAVES_TRACK_CHANCES, false);
		} else {
			Music.INSTANCE.playTracks(CavesLevel.CAVES_TRACK_LIST,
					CavesLevel.CAVES_TRACK_CHANCES, false);
		}
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CAVES;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CAVES;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STATE, state);
		int total = 0;
		for (Set<Integer> cluster : coverClusters) total += cluster.size();
		int[] cells = new int[total];
		int[] sizes = new int[coverClusters.size()];
		int index = 0;
		for (int i = 0; i < coverClusters.size(); i++) {
			Set<Integer> cluster = coverClusters.get(i);
			sizes[i] = cluster.size();
			for (int cell : cluster) cells[index++] = cell;
		}
		bundle.put(COVER_CLUSTER_CELLS, cells);
		bundle.put(COVER_CLUSTER_SIZES, sizes);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		state = bundle.contains(STATE) ? bundle.getEnum(STATE, State.class) : State.START;
		coverClusters = bundle.contains(COVER_CLUSTER_CELLS)
				&& bundle.contains(COVER_CLUSTER_SIZES)
				? restoreCoverClusters(bundle.getIntArray(COVER_CLUSTER_CELLS),
				bundle.getIntArray(COVER_CLUSTER_SIZES))
				: rebuildCoverClustersFromMap();
		for (Mob mob : mobs) {
			if (mob instanceof HuntressBoss) {
				((HuntressBoss) mob).finishLevelRestore(this);
			}
		}
	}

	private List<Set<Integer>> restoreCoverClusters(int[] cells, int[] sizes) {
		ArrayList<Set<Integer>> restored = new ArrayList<>();
		HashSet<Integer> used = new HashSet<>();
		int index = 0;
		for (int size : sizes) {
			if (size <= 0 || index + size > cells.length) break;
			LinkedHashSet<Integer> cluster = new LinkedHashSet<>();
			boolean valid = true;
			for (int i = 0; i < size; i++) {
				int cell = cells[index++];
				if (!isStoredCoverCell(cell) || used.contains(cell) || !cluster.add(cell)) {
					valid = false;
				}
			}
			if (valid) {
				used.addAll(cluster);
				restored.add(cluster);
			}
		}
		return restored;
	}

	private boolean isStoredCoverCell(int cell) {
		return cell >= 0 && cell < length() && isArenaCell(cell)
				&& !isReservedEncounterCell(cell) && map[cell] == Terrain.WALL;
	}

	private List<Set<Integer>> rebuildCoverClustersFromMap() {
		ArrayList<Set<Integer>> rebuilt = new ArrayList<>();
		HashSet<Integer> visited = new HashSet<>();
		for (int start = 0; start < length(); start++) {
			if (visited.contains(start) || !isStoredCoverCell(start)) continue;
			LinkedHashSet<Integer> cluster = new LinkedHashSet<>();
			ArrayList<Integer> pending = new ArrayList<>();
			pending.add(start);
			while (!pending.isEmpty()) {
				int cell = pending.remove(pending.size() - 1);
				if (!visited.add(cell) || !isStoredCoverCell(cell)) continue;
				cluster.add(cell);
				int x = cell % width();
				for (int neighbour : new int[]{cell - width(), cell + width(),
						cell - 1, cell + 1}) {
					if (neighbour < 0 || neighbour >= length()
							|| neighbour == cell - 1 && x == 0
							|| neighbour == cell + 1 && x == width() - 1) continue;
					if (!visited.contains(neighbour) && isStoredCoverCell(neighbour)) {
						pending.add(neighbour);
					}
				}
			}
			if (!cluster.isEmpty()) rebuilt.add(cluster);
		}
		return rebuilt;
	}

	@Override
	public void occupyCell(Char ch) {
		if (ch instanceof HuntressBoss) {
			int plantCell = ch.pos;
			Plant plant = plants.get(plantCell);
			HuntressBoss boss = (HuntressBoss) ch;
			HuntressBoss.WardenBoon boon = HuntressBoss.boonForPlant(plant);
			if (plant != null && boss.phase() == HuntressBoss.Phase.WARDEN
					&& boon != null) {
				bossPlantClaimInProgress = true;
				try {
					uproot(plantCell);
				} finally {
					bossPlantClaimInProgress = false;
				}
				set(plantCell, Terrain.FURROWED_GRASS, this);
				boss.onPlantClaimed(plantCell, boon);
			}
		}

		if (ch == Dungeon.hero && state == State.START && triggersFightAt(ch.pos)) {
			startFight();
		}
		completeOccupyCell(ch);
	}

	@Override
	public void uproot(int pos) {
		boolean removedPlant = plants.get(pos) != null;
		super.uproot(pos);
		if (removedPlant && !bossPlantClaimInProgress) {
			for (Char ch : Actor.chars()) {
				if (ch instanceof HuntressBoss && ch.isAlive()) {
					((HuntressBoss) ch).onPlantRemoved(pos);
				}
			}
		}
	}

	public int selectMarkedPlant(HuntressBoss boss) {
		if (boss == null || boss.pos < 0 || boss.pos >= length()) {
			return -1;
		}
		buildPlantDistanceMap(boss);
		ArrayList<Integer> preferred = new ArrayList<>();
		ArrayList<Integer> fallback = new ArrayList<>();
		for (int cell : plants.keyArray()) {
			int path = PathFinder.distance[cell];
			if (path == Integer.MAX_VALUE || path <= 2 || Actor.findChar(cell) != null) {
				continue;
			}
			fallback.add(cell);
			if (path >= 4 && path <= 8) {
				preferred.add(cell);
			}
		}
		ArrayList<Integer> pool = preferred.isEmpty() ? fallback : preferred;
		return pool.isEmpty() ? -1 : pool.get(Random.Int(pool.size()));
	}

	public boolean hasPlantAt(int cell) {
		return cell >= 0 && cell < length() && plants.get(cell) != null;
	}

	public boolean hasReachablePlant(HuntressBoss boss) {
		if (boss == null || boss.pos < 0 || boss.pos >= length()) {
			return false;
		}
		buildPlantDistanceMap(boss);
		for (int cell : plants.keyArray()) {
			if (PathFinder.distance[cell] != Integer.MAX_VALUE
					&& PathFinder.distance[cell] > 2 && Actor.findChar(cell) == null) {
				return true;
			}
		}
		return false;
	}

	public boolean isMarkedPlantReachable(HuntressBoss boss, int cell) {
		if (boss == null || boss.pos < 0 || boss.pos >= length()
				|| !hasPlantAt(cell) || Actor.findChar(cell) != null) {
			return false;
		}
		buildPlantDistanceMap(boss);
		return PathFinder.distance[cell] != Integer.MAX_VALUE;
	}

	private void buildPlantDistanceMap(HuntressBoss boss) {
		boolean[] plantPassable = passable.clone();
		for (Char ch : Actor.chars()) {
			if (ch != boss && ch.pos >= 0 && ch.pos < plantPassable.length) {
				plantPassable[ch.pos] = false;
			}
		}
		if (Char.hasProp(boss, Char.Property.LARGE)) {
			for (int cell = 0; cell < plantPassable.length; cell++) {
				plantPassable[cell] &= openSpace[cell];
			}
		}
		plantPassable[boss.pos] = true;
		PathFinder.buildDistanceMap(boss.pos, plantPassable);
	}

	void completeOccupyCell(Char ch) {
		super.occupyCell(ch);
	}

	private void startFight() {
		if (state != State.START) {
			return;
		}
		state = State.INTRO;
		sealEncounter();
		Statistics.qualifiedForBossChallengeBadge = true;

		int gate = GATE_X + GATE_Y * width();

		Heap heap = heaps.get(gate);
		while (heap != null && !heap.isEmpty()) {
			int destination = randomArenaCell(null, gate);
			if (destination == -1) {
				break;
			}
			Heap moved = drop(heap.pickUp(), destination);
			moved.seen = heap.seen;
		}

		Char gateOccupant = Actor.findChar(gate);
		if (gateOccupant != null) {
			int destination = randomArenaCell(gateOccupant, gate);
			if (destination != -1) {
				ScrollOfTeleportation.appear(gateOccupant, destination);
			}
		}
		relocateSafeZoneAllies();

		set(gate, Terrain.WALL, this);
		afterOpeningGateClosed(gate);

		HuntressBoss boss = createOpeningBoss();
		boss.pos = selectBossSpawnCell(Dungeon.hero.pos);
		boss.startEncounter();
		scheduleEncounterMob(boss, 1f);

		for (int i = 0; i < HuntressBoss.hawksSpawnedAtFightStart(); i++) {
			spawnHawk(boss);
		}

		state = State.FIGHT;
		startFightMusic();
	}

	private void relocateSafeZoneAllies() {
		for (Mob mob : new ArrayList<>(mobs)) {
			if (mob.alignment != Char.Alignment.ALLY || isArenaCell(mob.pos)) {
				continue;
			}
			int destination = selectEncounterAllyCell(mob);
			if (destination != -1) {
				teleportEncounterAlly(mob, destination);
			}
		}
	}

	protected int selectEncounterAllyCell(Mob ally) {
		return randomArenaCell(ally, Dungeon.hero.pos);
	}

	protected void teleportEncounterAlly(Mob ally, int destination) {
		ScrollOfTeleportation.appear(ally, destination);
	}

	HuntressBoss createOpeningBoss() {
		return new HuntressBoss();
	}

	void afterOpeningGateClosed(int gate) {
		GameScene.updateMap(gate);
		Dungeon.observe();
	}

	void scheduleEncounterMob(Mob mob, float delay) {
		GameScene.add(mob, delay);
	}

	void startFightMusic() {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.play(Assets.Music.CAVES_BOSS, true);
			}
		});
	}

	void sealEncounter() {
		super.seal();
	}

	public void onWardenPhase(HuntressBoss boss) {
		populateWardenArena(boss);
		for (int i = 0; i < HuntressBoss.hawksSpawnedAtWardenTransition(); i++) {
			spawnHawk(boss);
		}
		startWardenPhaseMusic();
	}

	void startWardenPhaseMusic() {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.play(Assets.Music.CAVES_BOSS_FINALE, true);
			}
		});
	}

	private void spawnHawk(HuntressBoss boss) {
		HuntressBoss.DistractingHawk hawk =
				new HuntressBoss.DistractingHawk(boss.alignment);
		hawk.pos = randomArenaCell(hawk, boss.pos);
		if (hawk.pos == -1) {
			return;
		}
		hawk.aggro(Dungeon.hero);
		scheduleEncounterMob(hawk, 1f);
	}

	public void spawnChallengeHawk(HuntressBoss boss) {
		spawnHawk(boss);
	}

	private void populateWardenArena(HuntressBoss boss) {
		WardenArenaApplication application = applyWardenArenaFeatures(
				boss == null ? Char.Alignment.ENEMY : boss.alignment);
		for (Plant plant : application.plants) {
			submitWardenPlant(plant);
		}
		for (HuntressBoss.HuntressTentacle tentacle : application.tentacles) {
			submitWardenTentacle(tentacle);
		}
		finishWardenArenaUpdate();
	}

	void submitWardenPlant(Plant plant) {
		GameScene.add(plant);
	}

	void submitWardenTentacle(HuntressBoss.HuntressTentacle tentacle) {
		GameScene.add(tentacle, 1f);
	}

	void finishWardenArenaUpdate() {
		GameScene.updateMap();
		Dungeon.observe();
	}

	WardenArenaAllocation populateWardenArenaForTest() {
		return applyWardenArenaFeatures(Char.Alignment.ENEMY).allocation;
	}

	private WardenArenaApplication applyWardenArenaFeatures(
			Char.Alignment huntressAlignment) {
		ArrayList<Integer> legal = vegetationCells();

		Set<Integer> blocked = new HashSet<>();
		for (int cell : legal) {
			if (Actor.findChar(cell) != null || plants.get(cell) != null
					|| heaps.get(cell) != null || isReservedEncounterCell(cell)) {
				blocked.add(cell);
			}
		}
		for (Set<Integer> cluster : coverClusters) {
			blocked.addAll(cluster);
		}
		for (int heapCell : heaps.keyArray()) {
			blocked.add(heapCell);
		}
		for (int plantCell : plants.keyArray()) {
			blocked.add(plantCell);
		}
		for (Char ch : Actor.chars()) {
			blocked.add(ch.pos);
		}

		WardenArenaAllocation allocation =
				allocateWardenArena(legal, blocked);

		for (int cell : allocation.furrowCells) {
			if (map[cell] != Terrain.FURROWED_GRASS) {
				set(cell, terrainForWardenFeature(map[cell], WardenFeature.FURROW), this);
			}
		}

		ArrayList<Plant> newPlants = new ArrayList<>();
		ArrayList<HuntressBoss.HuntressTentacle> newTentacles = new ArrayList<>();
		for (int cell : allocation.plantCells) {
			Random.pushGenerator(Dungeon.seed + cell);
			try {
				int baseTerrain = map[cell];
				Random.Float();
				Plant plant = createWardenArenaPlant(
						cell, arenaSeedClassForCurrentGenerator());
				plants.put(cell, plant);
				set(cell, terrainForWardenFeature(baseTerrain, WardenFeature.PLANT), this);
				newPlants.add(plant);
			} finally {
				Random.popGenerator();
			}
		}
		for (int cell : allocation.tentacleCells) {
			set(cell, terrainForWardenFeature(map[cell], WardenFeature.TENTACLE), this);
			HuntressBoss.HuntressTentacle tentacle =
					new HuntressBoss.HuntressTentacle(huntressAlignment);
			tentacle.pos = cell;
			tentacle.aggro(Dungeon.hero);
			newTentacles.add(tentacle);
		}
		return new WardenArenaApplication(allocation, newPlants, newTentacles);
	}

	protected Plant createWardenArenaPlant(int cell,
			Class<? extends Plant.Seed> seedClass) {
		Plant.Seed seed = com.watabou.utils.Reflection.newInstance(
				seedClass);
		return seed.couch(cell, this);
	}

	public boolean teleportBossAndHero(HuntressBoss boss) {
		return Dungeon.hero != null && teleportBossAndTargetApart(boss, Dungeon.hero);
	}

	public boolean teleportBossAndTargetApart(HuntressBoss boss, Char target) {
		if (boss == null || target == null) {
			return false;
		}
		int previousDistance = distance(boss.pos, target.pos);
		FadeleafDestinations destinations =
				selectRandomFadeleafDestinations(boss, target);
		if (destinations == null) {
			return false;
		}
		int oldBossPos = boss.pos;
		int oldTargetPos = target.pos;
		boolean succeeded = false;
		try {
			boolean bossMoved = teleportFadeleafChar(boss, destinations.bossCell);
			if (bossMoved && boss.pos == destinations.bossCell) {
				boolean targetMoved = teleportFadeleafChar(target, destinations.targetCell);
				succeeded = targetMoved
						&& boss.pos == destinations.bossCell
						&& target.pos == destinations.targetCell
						&& distance(boss.pos, target.pos) > previousDistance;
			}
		} catch (RuntimeException error) {
			succeeded = false;
		}
		if (!succeeded) {
			restoreFadeleafPair(boss, oldBossPos, target, oldTargetPos);
		}
		finishFadeleafTeleportSafely();
		if (succeeded && (boss.pos != destinations.bossCell
				|| target.pos != destinations.targetCell
				|| distance(boss.pos, target.pos) <= previousDistance)) {
			succeeded = false;
		}
		if (!succeeded) {
			restoreFadeleafPair(boss, oldBossPos, target, oldTargetPos);
		}
		return succeeded;
	}

	public int selectHuntressEscapeCell(HuntressBoss boss, Char target) {
		if (boss == null || target == null) {
			return -1;
		}
		ArrayList<Integer> legal = new ArrayList<>();
		ArrayList<Integer> preferredRange = new ArrayList<>();
		for (int cell = 0; cell < length(); cell++) {
			if (!isLegalHuntressEscapeCell(boss, target, cell)) {
				continue;
			}
			legal.add(cell);
			int distance = distance(cell, target.pos);
			if (distance >= 3 && distance <= 5) {
				preferredRange.add(cell);
			}
		}
		if (preferredRange.isEmpty()) {
			return farthestCellFromTarget(legal, target);
		}

		boolean hasBallisticCell = false;
		for (int cell : preferredRange) {
			if (hasProjectileLine(cell, target.pos)) {
				hasBallisticCell = true;
				break;
			}
		}
		int nearestCoverDistance = Integer.MAX_VALUE;
		for (int cell : preferredRange) {
			if (!hasBallisticCell || hasProjectileLine(cell, target.pos)) {
				nearestCoverDistance = Math.min(nearestCoverDistance,
						distanceToNearestCover(cell));
			}
		}
		int farthestTargetDistance = -1;
		for (int cell : preferredRange) {
			if ((!hasBallisticCell || hasProjectileLine(cell, target.pos))
					&& distanceToNearestCover(cell) == nearestCoverDistance) {
				farthestTargetDistance = Math.max(farthestTargetDistance,
						distance(cell, target.pos));
			}
		}
		ArrayList<Integer> finalists = new ArrayList<>();
		for (int cell : preferredRange) {
			if ((!hasBallisticCell || hasProjectileLine(cell, target.pos))
					&& distanceToNearestCover(cell) == nearestCoverDistance
					&& distance(cell, target.pos) == farthestTargetDistance) {
				finalists.add(cell);
			}
		}
		return finalists.isEmpty() ? -1 : Random.element(finalists);
	}

	boolean isLegalHuntressEscapeCell(HuntressBoss boss, Char target, int cell) {
		if (boss == null || target == null || cell < 0 || cell >= length()
				|| !isBasicArenaDestinationCell(cell)
				|| isReservedEncounterCell(cell) || isCoverCell(cell)) {
			return false;
		}
		Char occupant = Actor.findChar(cell);
		return (occupant == null || occupant == boss)
				&& (!Char.hasProp(boss, Char.Property.LARGE) || openSpace[cell])
				&& distance(cell, target.pos) > distance(boss.pos, target.pos);
	}

	public boolean moveHuntressToEscapeCell(HuntressBoss boss, int destination) {
		if (boss == null || destination < 0 || destination >= length()
				|| !isBasicArenaDestinationCell(destination)
				|| isReservedEncounterCell(destination) || isCoverCell(destination)
				|| Actor.findChar(destination) != null
				|| Char.hasProp(boss, Char.Property.LARGE) && !openSpace[destination]) {
			return false;
		}
		int origin = boss.pos;
		boolean succeeded = false;
		try {
			succeeded = teleportFadeleafChar(boss, destination)
					&& boss.pos == destination;
		} catch (RuntimeException error) {
			succeeded = false;
		}
		if (!succeeded) {
			restoreFadeleafPosition(boss, origin);
		}
		finishFadeleafTeleportSafely();
		if (succeeded && boss.pos != destination) {
			succeeded = false;
		}
		if (!succeeded) {
			restoreFadeleafPosition(boss, origin);
		}
		return succeeded;
	}

	private int farthestCellFromTarget(ArrayList<Integer> cells, Char target) {
		int farthest = -1;
		ArrayList<Integer> finalists = new ArrayList<>();
		for (int cell : cells) {
			int candidateDistance = distance(cell, target.pos);
			if (candidateDistance > farthest) {
				farthest = candidateDistance;
				finalists.clear();
			}
			if (candidateDistance == farthest) {
				finalists.add(cell);
			}
		}
		return finalists.isEmpty() ? -1 : Random.element(finalists);
	}

	private boolean hasProjectileLine(int from, int target) {
		return new Ballistica(from, target, Ballistica.PROJECTILE).collisionPos == target;
	}

	private boolean isCoverCell(int cell) {
		for (Set<Integer> cluster : coverClusters) {
			if (cluster.contains(cell)) {
				return true;
			}
		}
		return false;
	}

	private int distanceToNearestCover(int cell) {
		int result = Integer.MAX_VALUE;
		for (Set<Integer> cluster : coverClusters) {
			for (int cover : cluster) {
				result = Math.min(result, distance(cell, cover));
			}
		}
		return result;
	}

	private void restoreFadeleafPair(HuntressBoss boss, int bossPos,
			Char target, int targetPos) {
		restoreFadeleafPosition(target, targetPos);
		restoreFadeleafPosition(boss, bossPos);
	}

	private void restoreFadeleafPosition(Char ch, int origin) {
		if (ch == null) {
			return;
		}
		ch.pos = origin;
		if (ch.sprite != null) {
			try {
				ch.sprite.interruptMotion();
			} catch (RuntimeException ignored) {
				// The actor position is authoritative; visual recovery is best-effort.
			}
			try {
				ch.sprite.place(origin);
			} catch (RuntimeException ignored) {
				// Keep restoring the other actor even if one sprite is unavailable.
			}
		}
	}

	private void finishFadeleafTeleportSafely() {
		try {
			finishFadeleafTeleport();
		} catch (RuntimeException ignored) {
			// Observation is presentation cleanup and cannot change transaction result.
		}
	}

	FadeleafDestinations selectFadeleafDestinations(
			Char boss, Char target, int selection) {
		return selectFadeleafDestinations(boss, target, Integer.valueOf(selection));
	}

	private FadeleafDestinations selectRandomFadeleafDestinations(
			Char boss, Char target) {
		return selectFadeleafDestinations(boss, target, null);
	}

	private FadeleafDestinations selectFadeleafDestinations(
			Char boss, Char target, Integer selection) {
		if (boss == null || target == null) {
			return null;
		}
		ArrayList<Integer> bossCells = fadeleafCellsFor(boss);
		ArrayList<Integer> targetCells = fadeleafCellsFor(target);
		int previousDistance = distance(boss.pos, target.pos);
		int preferredCount = 0;
		int maximumDistance = previousDistance;
		int maximumCount = 0;
		for (int bossCell : bossCells) {
			for (int targetCell : targetCells) {
				int separation = distance(bossCell, targetCell);
				if (separation <= previousDistance) {
					continue;
				}
				if (separation >= PREFERRED_FADELEAF_SEPARATION) {
					preferredCount++;
				}
				if (separation > maximumDistance) {
					maximumDistance = separation;
					maximumCount = 1;
				} else if (separation == maximumDistance) {
					maximumCount++;
				}
			}
		}
		if (preferredCount == 0 && maximumCount == 0) {
			return null;
		}
		int candidateCount = preferredCount > 0 ? preferredCount : maximumCount;
		int chosen = selection == null ? Random.Int(candidateCount)
				: Math.floorMod(selection, candidateCount);
		for (int bossCell : bossCells) {
			for (int targetCell : targetCells) {
				int separation = distance(bossCell, targetCell);
				boolean candidate = preferredCount > 0
						? separation >= PREFERRED_FADELEAF_SEPARATION
						: separation == maximumDistance;
				if (candidate && separation > previousDistance && chosen-- == 0) {
					return new FadeleafDestinations(bossCell, targetCell);
				}
			}
		}
		return null;
	}

	boolean isLegalFadeleafCell(Char ch, int cell) {
		Char occupant = Actor.findChar(cell);
		return isBasicArenaDestinationCell(cell)
				&& (occupant == null || occupant == ch)
				&& (!Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell]);
	}

	private ArrayList<Integer> fadeleafCellsFor(Char ch) {
		ArrayList<Integer> cells = new ArrayList<>();
		for (int cell : vegetationCells()) {
			if (isLegalFadeleafCell(ch, cell)) {
				cells.add(cell);
			}
		}
		return cells;
	}

	protected boolean teleportFadeleafChar(Char ch, int destination) {
		ScrollOfTeleportation.appear(ch, destination);
		return ch.pos == destination;
	}

	protected void restoreFadeleafChar(Char ch, int origin) {
		ScrollOfTeleportation.appear(ch, origin);
	}

	protected void finishFadeleafTeleport() {
		Dungeon.observe();
		GameScene.updateFog();
	}

	private int randomArenaCell(Char ch, int oldPos) {
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int cell : vegetationCells()) {
			if (!passable[cell] || Actor.findChar(cell) != null
					|| plants.get(cell) != null || distance(cell, oldPos) < 4
					|| (ch != null && Char.hasProp(ch, Char.Property.LARGE) && !openSpace[cell])) {
				continue;
			}
			candidates.add(cell);
		}
		return candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	@Override
	public int randomRespawnCell(Char ch) {
		return randomArenaCell(ch, Dungeon.hero == null ? -1000 : Dungeon.hero.pos);
	}

	@Override
	public int safeArrivalCell(Char ch) {
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = entrance() + offset;
			if (cell >= 0 && cell < length() && passable[cell]
					&& Actor.findChar(cell) == null
					&& (!Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell])) {
				candidates.add(cell);
			}
		}
		if (!candidates.isEmpty()) {
			return Random.element(candidates);
		}
		return passable[entrance()] && Actor.findChar(entrance()) == null
				&& (!Char.hasProp(ch, Char.Property.LARGE) || openSpace[entrance()])
				? entrance() : -1;
	}

	@Override
	public int unblessedAnkhSafeRespawnCell(Char ch) {
		return state == State.START ? safeArrivalCell(ch) : -1;
	}

	@Override
	public void onSealedResurrectionReset() {
		int leftSupply = ENTRANCE_X - 2 + (ENTRANCE_Y - 2) * width();
		int rightSupply = ENTRANCE_X + 2 + (ENTRANCE_Y - 2) * width();
		for (int cell : new int[]{leftSupply, rightSupply}) {
			Heap heap = heaps.get(cell);
			if (heap == null || heap.type != Heap.Type.HEAP) {
				continue;
			}
			for (Item item : heap.items.toArray(new Item[0])) {
				if (item.getClass() == entranceSupplyItemClass()) {
					heap.items.remove(item);
				}
			}
			if (heap.items.isEmpty()) {
				heap.destroy(this);
			}
		}
	}

	@Override
	public boolean shouldResetForSafeArrival() {
		return locked && state != State.WON;
	}

	public void onBossDefeated() {
		state = State.WON;
		runWithoutEncounterCounting(() -> {
			for (Mob mob : new ArrayList<>(mobs)) {
				if (mob instanceof HuntressBoss.DistractingHawk
						|| mob instanceof HuntressBoss.HuntressTentacle) {
					mob.die(null);
				}
			}
		});
		super.unseal();
		int gate = GATE_X + GATE_Y * width();
		set(gate, Terrain.EMPTY, this);
		GameScene.updateMap(gate);
		Dungeon.observe();

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.fadeOut(3f, new Callback() {
					@Override
					public void call() {
						Music.INSTANCE.playTracks(CavesLevel.CAVES_TRACK_LIST,
								CavesLevel.CAVES_TRACK_CHANCES, false);
					}
				});
			}
		});
	}

	static void runWithoutEncounterCounting(Runnable cleanup) {
		boolean previousSkipCountingEncounters = Bestiary.skipCountingEncounters;
		Bestiary.skipCountingEncounters = true;
		try {
			cleanup.run();
		} finally {
			Bestiary.skipCountingEncounters = previousSkipCountingEncounters;
		}
	}

	State state() {
		return state;
	}

	int triggerCell() {
		return TRIGGER_X + TRIGGER_Y * width();
	}

	boolean triggersFightAt(int cell) {
		return isArenaCell(cell) && cell / width() <= TRIGGER_Y;
	}

	boolean isArenaCell(int cell) {
		if (cell < 0 || cell >= length()) {
			return false;
		}
		int x = cell % width();
		int y = cell / width();
		return x >= ARENA.left && x < ARENA.right
				&& y >= ARENA.top && y < ARENA.bottom;
	}

	ArrayList<Integer> vegetationCells() {
		ArrayList<Integer> cells = new ArrayList<>();
		for (int i = 0; i < length(); i++) {
			if (!isArenaCell(i)
					|| (Terrain.flags[map[i]] & Terrain.PASSABLE) == 0
					|| !isLegalVegetationTerrain(map[i])
					|| isReservedEncounterCell(i)) {
				continue;
			}
			if (distance(i, triggerCell()) <= 2 || distance(i, exit()) <= 2) {
				continue;
			}
			cells.add(i);
		}
		return cells;
	}
}
