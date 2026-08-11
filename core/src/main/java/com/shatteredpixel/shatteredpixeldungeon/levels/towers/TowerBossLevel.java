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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TowerBossLevel extends TowerLevel {

	public static final int FLOORS_PER_BOSS = 5;
	private static final String PESTILENCE_ARENA = "pestilence_arena";
	private final TowerBossEncounter encounter = new TowerBossEncounter();
	private PestilenceArenaController pestilenceArena;
	private transient boolean purifierClearingMiasma;

	@Override
	public String tilesTex() {
		String tiles = Dungeon.towerTilesTexForLocation(
				Math.max(1, Dungeon.depth), TowerLevel.BRANCH);
		return tiles == null ? super.tilesTex() : tiles;
	}

	@Override
	public String waterTex() {
		String water = Dungeon.towerWaterTexForLocation(
				Math.max(1, Dungeon.depth), TowerLevel.BRANCH);
		return water == null ? super.waterTex() : water;
	}

	@Override
	public void playLevelMusic() {
		if (locked) {
			Music.INSTANCE.play(Assets.Music.HALLS_BOSS, true);
		} else {
			super.playLevelMusic();
		}
	}

	@Override
	protected boolean build() {
		setSize(TowerBossLayout.WIDTH, TowerBossLayout.HEIGHT);
		rooms = new ArrayList<>();
		int[] generated = TowerBossLayout.generateMap();
		System.arraycopy(generated, 0, map, 0, generated.length);

		transitions.add(new LevelTransition(this, TowerBossLayout.ENTRANCE,
				LevelTransition.Type.REGULAR_ENTRANCE,
				Dungeon.depth - 1, TowerLevel.BRANCH, LevelTransition.Type.REGULAR_EXIT));
		transitions.add(new LevelTransition(this, TowerBossLayout.EXIT,
				LevelTransition.Type.REGULAR_EXIT,
				Dungeon.depth + 1, TowerLevel.BRANCH, LevelTransition.Type.REGULAR_ENTRANCE));
		encounter.ensureSelected(Dungeon.seed, Dungeon.depth, Dungeon.branch);
		return true;
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	public int mobLimit() {
		return 0;
	}

	@Override
	public int randomRespawnCell(Char ch) {
		for (int attempts = 0; attempts < 20; attempts++) {
			int x = Random.Int(13, 16);
			int y = Random.Int(31, 34);
			int cell = TowerBossLayout.cell(x, y);
			if (passable[cell] && Actor.findChar(cell) == null
					&& (ch == null || !Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell])) {
				return cell;
			}
		}
		return -1;
	}

	@Override
	public void occupyCell(Char ch) {
		boolean hero = ch == Dungeon.hero;
		boolean encounterBegun = encounter.bossEncounterStarted()
				|| encounter.bossEncounterDefeated()
				|| pestilenceArena != null && pestilenceArena.preludeStarted();
		if (TowerBossLayout.shouldBeginPrelude(encounterBegun, hero, ch.pos)) {
			beginEncounterPrelude();
		}
		super.occupyCell(ch);
		if (hero && pestilenceArena != null && !encounter.bossEncounterDefeated()) {
			handlePurifierEntry((Hero) ch);
		}
	}

	private void beginEncounterPrelude() {
		encounter.ensureSelected(Dungeon.seed, Dungeon.depth, Dungeon.branch);
		if (!TowerBossGenerator.PESTILENCE_KNIGHT_ID.equals(encounter.selectedBossId())) {
			startEncounter();
			return;
		}
		int bossCell = selectBossSpawnCell();
		if (pestilenceArena == null) pestilenceArena = new PestilenceArenaController();
		if (!pestilenceArena.beginPrelude(this, bossCell)) {
			startEncounter();
			return;
		}
		seal();
		Statistics.qualifiedForBossChallengeBadge = true;
	}

	private void handlePurifierEntry(Hero hero) {
		PestilenceArenaController.ActivationResult result = pestilenceArena.onHeroEntered(
				this, hero, encounter.bossEncounterStarted());
		if (result == PestilenceArenaController.ActivationResult.START_BOSS) {
			startEncounter();
		} else if (result == PestilenceArenaController.ActivationResult.DAMAGE_BOSS) {
			for (Mob mob : mobs) {
				if (mob instanceof PestilenceKnight && mob.isAlive()) {
					mob.damage(PestilenceArenaController.PURIFIER_BOSS_DAMAGE, pestilenceArena);
					break;
				}
			}
		}
	}

	protected void startEncounter() {
		encounter.ensureSelected(Dungeon.seed, Dungeon.depth, Dungeon.branch);
		encounter.start(encounterHost());
		Statistics.qualifiedForBossChallengeBadge = true;
	}

	public void onPaleMiasmaClearedExternally(int removedVolume) {
		if (PestilenceArenaController.shouldStartFromExternalPaleClear(
				pestilenceArena != null && pestilenceArena.preludeStarted(),
				encounter.bossEncounterStarted(), encounter.bossEncounterDefeated(),
				purifierClearingMiasma, removedVolume)) {
			startEncounter();
		}
	}

	void beginPurifierMiasmaClear() {
		purifierClearingMiasma = true;
	}

	void endPurifierMiasmaClear() {
		purifierClearingMiasma = false;
	}

	protected TowerBoss createSelectedBoss() {
		return TowerBossGenerator.create(encounter.selectedBossId());
	}

	protected void launchBoss(TowerBoss boss) {
		boss.aggro(Dungeon.hero);
		BossHealthBar.assignBoss(boss);
		GameScene.add(boss, 1f);
	}

	protected int selectBossSpawnCell() {
		if (pestilenceArena != null && pestilenceArena.preludeStarted()
				&& pestilenceArena.bossCell() >= 0) {
			return pestilenceArena.bossCell();
		}
		for (int attempts = 0; attempts < 200; attempts++) {
			int cell = TowerBossLayout.cell(Random.Int(6, 23), Random.Int(7, 16));
			if (passable[cell] && Actor.findChar(cell) == null) {
				return cell;
			}
		}
		return TowerBossLayout.cell(14, 10);
	}

	public void onTowerBossDefeated(TowerBoss boss) {
		encounter.onBossDefeated(boss, encounterHost());
	}

	public boolean preparePestilenceArena(int bossCell) {
		if (pestilenceArena == null) pestilenceArena = new PestilenceArenaController();
		return pestilenceArena.prepare(this, bossCell);
	}

	public PestilenceArenaController pestilenceArenaController() {
		return pestilenceArena;
	}

	@Override
	public void onHeroTurnStarted(Hero hero) {
		if (pestilenceArena != null && !encounter.bossEncounterDefeated()) {
			pestilenceArena.onHeroTurnStarted(hero);
		}
	}

	@Override
	public void onHeroWaited(Hero hero) {
		if (pestilenceArena != null && !encounter.bossEncounterDefeated()) {
			pestilenceArena.onHeroWaited(hero);
		}
	}

	@Override
	public void onHeroConsumableUsed(Hero hero, Item item) {
		if (pestilenceArena != null && !encounter.bossEncounterDefeated()) {
			pestilenceArena.onHeroConsumableUsed(hero, item);
		}
	}

	String selectedBossId() {
		return encounter.selectedBossId();
	}

	boolean bossEncounterStarted() {
		return encounter.bossEncounterStarted();
	}

	boolean bossEncounterDefeated() {
		return encounter.bossEncounterDefeated();
	}

	private TowerBossEncounter.Host encounterHost() {
		return new TowerBossEncounter.Host() {
			@Override
			public TowerBoss createBoss(String id) {
				return createSelectedBoss();
			}

			@Override
			public int selectBossSpawnCell() {
				return TowerBossLevel.this.selectBossSpawnCell();
			}

			@Override
			public boolean prepareArena(TowerBoss boss, int spawnCell) {
				return boss.prepareArena(TowerBossLevel.this, spawnCell);
			}

			@Override
			public void sealArena() {
				seal();
			}

			@Override
			public void launchBoss(TowerBoss boss) {
				TowerBossLevel.this.launchBoss(boss);
			}

			@Override
			public void cleanupArena(TowerBoss boss) {
				boss.cleanupArena(TowerBossLevel.this);
			}

			@Override
			public void unsealArena() {
				unseal();
			}
		};
	}

	@Override
	public void seal() {
		if (!locked) {
			super.seal();
			set(TowerBossLayout.SAFE_GATE, Terrain.WALL, this);
			GameScene.updateMap(TowerBossLayout.SAFE_GATE);
			Dungeon.observe();
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					Music.INSTANCE.play(Assets.Music.HALLS_BOSS, true);
				}
			});
		}
	}

	@Override
	public void unseal() {
		boolean changed = locked
				|| map[TowerBossLayout.SAFE_GATE] != Terrain.DOOR
				|| map[TowerBossLayout.EXIT_GATE] != Terrain.UNLOCKED_EXIT;
		super.unseal();
		set(TowerBossLayout.SAFE_GATE, Terrain.DOOR, this);
		set(TowerBossLayout.EXIT_GATE, Terrain.UNLOCKED_EXIT, this);
		if (changed) {
			GameScene.updateMap(TowerBossLayout.SAFE_GATE);
			GameScene.updateMap(TowerBossLayout.EXIT_GATE);
			Dungeon.observe();
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					Music.INSTANCE.fadeOut(5f, new Callback() {
						@Override
						public void call() {
							Music.INSTANCE.end();
						}
					});
				}
			});
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		encounter.storeInBundle(bundle);
		if (pestilenceArena != null) bundle.put(PESTILENCE_ARENA, pestilenceArena);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		pestilenceArena = bundle.contains(PESTILENCE_ARENA)
				? (PestilenceArenaController) bundle.get(PESTILENCE_ARENA) : null;
		if (pestilenceArena != null) pestilenceArena.syncPurifierVisual(this);
		boolean exitUnlocked = map[TowerBossLayout.EXIT_GATE] == Terrain.UNLOCKED_EXIT;
		encounter.restoreFromBundle(bundle, Dungeon.seed, Dungeon.depth, Dungeon.branch,
				locked, hasRestoredTowerBoss(), exitUnlocked);
	}

	private boolean hasRestoredTowerBoss() {
		for (Mob mob : mobs) {
			if (mob instanceof TowerBoss) {
				return true;
			}
		}
		return false;
	}
}
