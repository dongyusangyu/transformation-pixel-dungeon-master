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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HuntressBossLevel extends Level {

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
	private static final Rect ARENA = new Rect(3, 2, 28, 24);
	private static final String STATE = "huntress_boss_state";

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

	private State state = State.START;

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

	static int[] phaseTwoCounts(int legalCells) {
		return new int[]{
				Math.round(legalCells * 0.50f),
				Math.max(1, Math.round(legalCells * 0.01f)),
				Math.round(legalCells * 0.05f)
		};
	}

	static boolean isLegalVegetationTerrain(int terrain) {
		return terrain == Terrain.EMPTY
				|| terrain == Terrain.EMPTY_DECO
				|| terrain == Terrain.FURROWED_GRASS;
	}

	static WardenArenaAllocation allocateWardenArena(List<Integer> legalCells,
			Set<Integer> existingFurrows, Set<Integer> blockedCells, int[] counts) {
		Set<Integer> legal = new LinkedHashSet<>(legalCells);
		Set<Integer> furrows = new LinkedHashSet<>();
		for (int cell : existingFurrows) {
			if (legal.contains(cell)) {
				furrows.add(cell);
			}
		}

		ArrayList<Integer> candidates = new ArrayList<>();
		for (int cell : legal) {
			if (!existingFurrows.contains(cell) && !blockedCells.contains(cell)) {
				candidates.add(cell);
			}
		}

		Iterator<Integer> candidate = candidates.iterator();
		Set<Integer> tentacles = takeCells(candidate, Math.max(0, counts[1]));
		Set<Integer> plants = takeCells(candidate, Math.max(0, counts[2]));
		int furrowsToAdd = Math.max(0, counts[0] - furrows.size());
		furrows.addAll(takeCells(candidate, furrowsToAdd));
		return new WardenArenaAllocation(furrows, plants, tentacles);
	}

	private static Set<Integer> takeCells(Iterator<Integer> candidates, int count) {
		Set<Integer> result = new LinkedHashSet<>();
		while (result.size() < count && candidates.hasNext()) {
			result.add(candidates.next());
		}
		return result;
	}

	static int terrainForWardenFeature(int baseTerrain, WardenFeature feature) {
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

		// Four small stone clusters provide hard cover against the sniper.
		Painter.fill(this, 8, 8, 2, 2, Terrain.WALL);
		Painter.fill(this, 21, 8, 2, 2, Terrain.WALL);
		Painter.fill(this, 8, 16, 2, 2, Terrain.WALL);
		Painter.fill(this, 21, 16, 2, 2, Terrain.WALL);

		int entrance = ENTRANCE_X + ENTRANCE_Y * width();
		int exit = EXIT_X + EXIT_Y * width();
		Painter.set(this, entrance, Terrain.ENTRANCE);
		Painter.set(this, exit, Terrain.EXIT);
		transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));
		transitions.add(new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT));

		ArrayList<Integer> vegetation = vegetationCells();
		Random.pushGenerator(Dungeon.seedForDepth(15, 0) + 150015L);
		Random.shuffle(vegetation);
		int initialFurrows = Math.round(vegetation.size() * 0.10f);
		for (int i = 0; i < initialFurrows; i++) {
			map[vegetation.get(i)] = Terrain.FURROWED_GRASS;
		}
		Random.popGenerator();

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
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		state = bundle.contains(STATE) ? bundle.getEnum(STATE, State.class) : State.START;
	}

	@Override
	public void occupyCell(Char ch) {
		if (ch instanceof HuntressBoss) {
			Plant plant = plants.get(ch.pos);
			if (plant != null) {
				HuntressBoss.WardenBoon boon = HuntressBoss.boonForPlant(plant);
				uproot(ch.pos);
				set(ch.pos, Terrain.FURROWED_GRASS, this);
				((HuntressBoss) ch).grantBoon(boon);
			}
		}

		if (ch == Dungeon.hero && state == State.START && triggersFightAt(ch.pos)) {
			startFight();
		}
		super.occupyCell(ch);
	}

	private void startFight() {
		if (state != State.START) {
			return;
		}
		state = State.INTRO;
		super.seal();
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
		for (Mob mob : new ArrayList<>(mobs)) {
			if (mob.alignment == Char.Alignment.ALLY && !isArenaCell(mob.pos)) {
				int destination = randomArenaCell(mob, Dungeon.hero.pos);
				if (destination != -1) {
					ScrollOfTeleportation.appear(mob, destination);
				}
			}
		}

		set(gate, Terrain.WALL, this);
		GameScene.updateMap(gate);
		Dungeon.observe();

		HuntressBoss boss = new HuntressBoss();
		boss.pos = BOSS_X + BOSS_Y * width();
		if (Actor.findChar(boss.pos) != null || !passable[boss.pos]) {
			boss.pos = randomArenaCell(null, -1);
		}
		boss.startEncounter();
		GameScene.add(boss, 1f);

		for (int i = 0; i < HuntressBoss.hawksSpawnedAtFightStart(); i++) {
			spawnHawk(boss);
		}

		state = State.FIGHT;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.play(Assets.Music.CAVES_BOSS, true);
			}
		});
	}

	public void onWardenPhase(HuntressBoss boss) {
		populateWardenArena();
		for (int i = 0; i < HuntressBoss.hawksSpawnedAtWardenTransition(); i++) {
			spawnHawk(boss);
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.play(Assets.Music.CAVES_BOSS_FINALE, true);
			}
		});
	}

	private void spawnHawk(HuntressBoss boss) {
		HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
		hawk.pos = randomArenaCell(hawk, boss.pos);
		if (hawk.pos == -1) {
			return;
		}
		hawk.aggro(Dungeon.hero);
		GameScene.add(hawk, 1f);
	}

	private void populateWardenArena() {
		ArrayList<Integer> legal = vegetationCells();
		Random.shuffle(legal);
		int[] counts = phaseTwoCounts(legal.size());

		Set<Integer> existingFurrows = new HashSet<>();
		Set<Integer> blocked = new HashSet<>();
		for (int cell : legal) {
			if (map[cell] == Terrain.FURROWED_GRASS) {
				existingFurrows.add(cell);
			}
			if (Actor.findChar(cell) != null || plants.get(cell) != null) {
				blocked.add(cell);
			}
		}

		WardenArenaAllocation allocation =
				allocateWardenArena(legal, existingFurrows, blocked, counts);

		for (int cell : allocation.furrowCells) {
			if (map[cell] != Terrain.FURROWED_GRASS) {
				set(cell, terrainForWardenFeature(map[cell], WardenFeature.FURROW), this);
			}
		}

		for (int cell : allocation.plantCells) {
			int baseTerrain = map[cell];
			Plant.Seed seed = com.watabou.utils.Reflection.newInstance(
					Random.element(ARENA_SEEDS));
			Plant plant = seed.couch(cell, this);
			plants.put(cell, plant);
			set(cell, terrainForWardenFeature(baseTerrain, WardenFeature.PLANT), this);
			GameScene.add(plant);
		}

		for (int cell : allocation.tentacleCells) {
			set(cell, terrainForWardenFeature(map[cell], WardenFeature.TENTACLE), this);
			HuntressBoss.HuntressTentacle tentacle = new HuntressBoss.HuntressTentacle();
			tentacle.pos = cell;
			tentacle.aggro(Dungeon.hero);
			GameScene.add(tentacle, 1f);
		}

		GameScene.updateMap();
		Dungeon.observe();
	}

	public void teleportBossAndHero(HuntressBoss boss) {
		int bossDestination = randomArenaCell(boss, boss.pos);
		if (bossDestination != -1) {
			ScrollOfTeleportation.appear(boss, bossDestination);
		}
		int heroDestination = randomArenaCell(Dungeon.hero, Dungeon.hero.pos);
		if (heroDestination != -1) {
			ScrollOfTeleportation.appear(Dungeon.hero, heroDestination);
		}
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
			if (!isArenaCell(i) || !isLegalVegetationTerrain(map[i])) {
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
