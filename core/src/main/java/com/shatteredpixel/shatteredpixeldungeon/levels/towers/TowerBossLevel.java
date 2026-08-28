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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfIllusion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.HungerKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Drunkenness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Exhilaration;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.GentlemanElfSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TowerBossLevel extends TowerLevel {

	public static final int FLOORS_PER_BOSS = 5;
	private static final String PESTILENCE_ARENA = "pestilence_arena";
	private static final String GENTLEMAN_PRELUDE = "gentleman_elf_prelude";
	private final TowerBossEncounter encounter = new TowerBossEncounter();
	private PestilenceArenaController pestilenceArena;
	private transient boolean purifierClearingMiasma;
	private transient int reservedBossSpawnCell = -1;
	private GentlemanElfArena gentlemanElfArena;
	private GentlemanElfPrelude gentlemanElfPrelude;
	private transient boolean gentlemanIntroWindowOpen;

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
			Music.INSTANCE.play(TowerBossMusic.musicFor(encounter.selectedBossId()), true);
		} else {
			super.playLevelMusic();
		}
	}

	@Override
	public void updateLevelMusic(float elapsed) {
		if (!locked) super.updateLevelMusic(elapsed);
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
		if (locked) {
			return randomArenaTeleportCell(ch, true);
		}
		return randomSafeZoneCell(ch);
	}

	@Override
	public int safeArrivalCell(Char ch) {
		int cell = randomSafeZoneCell(ch);
		if (cell != -1) return cell;

		cell = entrance();
		return passable[cell] && Actor.findChar(cell) == null
				&& (ch == null || !Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell])
				? cell : -1;
	}

	@Override
	public boolean shouldResetForSafeArrival() {
		return TowerBossLayout.shouldResetForSafeArrival(locked, bossEncounterDefeated());
	}

	@Override
	public void onBeforeSealedResurrectionReset() {
		gentlemanIntroWindowOpen = false;
		if (gentlemanElfPrelude != null) gentlemanElfPrelude.reset();
		for (Mob mob : new ArrayList<>(mobs)) {
			if (mob instanceof TowerBoss) {
				((TowerBoss) mob).cleanupArena(this);
			}
		}
		HungerKnight.cleanupOrphanedEffects(Dungeon.hero);
	}

	private int randomSafeZoneCell(Char ch) {
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
	public int randomDestination(Char ch) {
		if (locked) {
			return randomArenaTeleportCell(ch, false);
		}
		return super.randomDestination(ch);
	}

	private int randomArenaTeleportCell(Char ch, boolean requireEmpty) {
		ArrayList<Integer> candidates = new ArrayList<>();
		boolean large = ch != null && Char.hasProp(ch, Char.Property.LARGE);
		for (int cell = 0; cell < length(); cell++) {
			if (TowerBossLayout.isLegalTeleportDestination(cell, passable[cell], openSpace[cell],
					large, requireEmpty && Actor.findChar(cell) != null)) {
				candidates.add(cell);
			}
		}
		return candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	public boolean isBossTeleportPositionAllowed(int pos) {
		return TowerBossLayout.isArenaCell(pos);
	}

	public boolean isBossArenaCell(int cell) {
		return TowerBossLayout.isArenaCell(cell);
	}

	public boolean isDestructibleBossCover(int cell) {
		return cell >= 0 && cell < length()
				&& TowerBossLayout.isArenaCell(cell)
				&& !TowerBossLayout.isProtectedCell(cell)
				&& TowerBossLayout.isDestructibleTerrain(map[cell])
				&& (pestilenceArena == null
				|| pestilenceArena.purifierCell() != cell);
	}

	public boolean destroyBossCover(int cell) {
		if (!isDestructibleBossCover(cell)) return false;
		destroy(cell);
		GameScene.updateMap(cell);
		Dungeon.observe();
		return map[cell] == Terrain.EMBERS;
	}

	@Override
	public void occupyCell(Char ch) {
		boolean hero = ch == Dungeon.hero;
		boolean encounterBegun = encounter.bossEncounterStarted()
				|| encounter.bossEncounterDefeated()
				|| pestilenceArena != null && pestilenceArena.preludeStarted()
				|| gentlemanElfPrelude != null && gentlemanElfPrelude.started();
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
		if (TowerBossGenerator.GENTLEMAN_ELF_ID.equals(encounter.selectedBossId())) {
			beginGentlemanElfPrelude();
			return;
		}
		if (!TowerBossGenerator.PESTILENCE_KNIGHT_ID.equals(encounter.selectedBossId())) {
			startEncounter();
			return;
		}
		int bossCell = selectBossSpawnCell();
		reservedBossSpawnCell = bossCell;
		if (pestilenceArena == null) pestilenceArena = new PestilenceArenaController();
		if (!pestilenceArena.beginPrelude(this, bossCell)) {
			startEncounter();
			return;
		}
		seal();
		Statistics.qualifiedForBossChallengeBadge = true;
	}

	private void beginGentlemanElfPrelude() {
		if (gentlemanElfPrelude == null) gentlemanElfPrelude = new GentlemanElfPrelude();
		if (!gentlemanElfPrelude.started()) {
			reservedBossSpawnCell = selectBossSpawnCell();
			gentlemanElfPrelude.begin(reservedBossSpawnCell);
			seal();
			Statistics.qualifiedForBossChallengeBadge = true;
		}
		continueGentlemanElfPrelude();
	}

	private void continueGentlemanElfPrelude() {
		if (gentlemanElfPrelude == null) return;
		GentlemanElfPrelude.Action action = gentlemanElfPrelude.nextAction(
				encounter.bossEncounterStarted());
		if (action == GentlemanElfPrelude.Action.SHOW_WINDOW) {
			requestGentlemanIntroWindow();
		} else if (action == GentlemanElfPrelude.Action.START_ENCOUNTER) {
			reservedBossSpawnCell = gentlemanElfPrelude.spawnCell();
			startEncounter();
		}
	}

	private void requestGentlemanIntroWindow() {
		if (gentlemanIntroWindowOpen || gentlemanElfPrelude == null
				|| gentlemanElfPrelude.nextAction(encounter.bossEncounterStarted())
				!= GentlemanElfPrelude.Action.SHOW_WINDOW) {
			return;
		}
		gentlemanIntroWindowOpen = true;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				if (Dungeon.level != TowerBossLevel.this || gentlemanElfPrelude == null
						|| gentlemanElfPrelude.nextAction(encounter.bossEncounterStarted())
						!= GentlemanElfPrelude.Action.SHOW_WINDOW) {
					gentlemanIntroWindowOpen = false;
					return;
				}
				GameScene.show(new WndOptions(new GentlemanElfSprite(),
						Messages.get(GentlemanElf.class, "intro_title"),
						Messages.get(GentlemanElf.class, "intro_text"),
						Messages.get(GentlemanElf.class, "drink"),
						Messages.get(GentlemanElf.class, "refuse")) {
					@Override
					protected void onSelect(int index) {
						gentlemanIntroWindowOpen = false;
						resolveGentlemanIntro(index == 0);
					}

					@Override
					public void onBackPressed() {
					}
				});
			}
		});
	}

	private void resolveGentlemanIntro(boolean drink) {
		if (gentlemanElfPrelude == null || !gentlemanElfPrelude.resolve(drink)) return;
		GentlemanElf restoredBoss = findGentlemanElf();
		if (restoredBoss != null) {
			restoredBoss.resolveIntro(drink);
			restoredBoss.yell(Messages.get(restoredBoss, drink ? "drink_reply" : "refuse_reply"));
			return;
		}
		continueGentlemanElfPrelude();
	}

	private GentlemanElf findGentlemanElf() {
		for (Mob mob : mobs) {
			if (mob instanceof GentlemanElf && mob.isAlive()) return (GentlemanElf) mob;
		}
		return null;
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
		if (boss instanceof GentlemanElf && gentlemanElfPrelude != null
				&& gentlemanElfPrelude.resolved()) {
			GentlemanElf gentleman = (GentlemanElf) boss;
			gentleman.resolveIntro(gentlemanElfPrelude.drink());
		}
		boss.aggro(Dungeon.hero);
		GameScene.add(boss, 1f);
		// Bind only after GameScene has inserted the boss into Dungeon.level.mobs.
		// BossHealthBar.update() clears assignments that are not present in the level.
		BossHealthBar.assignBoss(boss);
		if (boss instanceof GentlemanElf && gentlemanElfArena != null) {
			Actor.add(gentlemanElfArena);
			GentlemanElf gentleman = (GentlemanElf) boss;
			gentleman.beginEncounter(Dungeon.hero);
			if (gentlemanElfPrelude != null) {
				gentleman.yell(Messages.get(gentleman,
						gentlemanElfPrelude.drink() ? "drink_reply" : "refuse_reply"));
			}
		}
	}

	public GentlemanElfArena prepareGentlemanElfArena(final GentlemanElf boss) {
		gentlemanElfArena = new GentlemanElfArena(gentlemanElfHost(boss));
		return gentlemanElfArena;
	}

	private GentlemanElfArena.Host gentlemanElfHost(final GentlemanElf boss) {
		return new GentlemanElfArena.Host() {
			@Override public GentlemanElf boss() { return boss; }
			@Override public Iterable<Char> characters() { return Actor.chars(); }
			@Override public Actor actorById(int id) {
				if (boss.id() == id) return boss;
				if (Dungeon.hero != null && Dungeon.hero.id() == id) return Dungeon.hero;
				for (Mob mob : mobs) if (mob.id() == id) return mob;
				return Actor.findById(id);
			}
			@Override public void warnBanquet(int turnsUntilResolution) {
				if (boss.sprite != null) boss.yell(Messages.get(boss, "banquet"));
				if (boss.sprite != null && boss.sprite.parent != null) {
					for (int cell = 0; cell < length(); cell++) {
						if (passable[cell] && TowerBossLayout.isArenaCell(cell)) {
							boss.sprite.parent.addToBack(new TargetedCell(cell, 0x7560C9));
						}
					}
				}
			}
			@Override public boolean respawnCup() {
				if (gentlemanElfArena == null || gentlemanElfArena.cupId() >= 0) return false;
				for (Char character : Actor.chars()) {
					if (character == boss || character instanceof GentlemanElfIllusion
							|| character instanceof ElfWineCup) continue;
					if (character.buff(Exhilaration.class) != null) Drunkenness.affect(character);
				}
				ElfWineCup cup = new ElfWineCup();
				cup.listener(gentlemanElfArena);
				int cell = selectGentlemanCupCell(boss);
				if (cell < 0) return false;
				cup.pos = cell;
				GameScene.add(cup, 1f);
				gentlemanElfArena.cupId(cup.id());
				if (boss.sprite != null) boss.yell(Messages.get(boss, "cup_spawn"));
				Splash.at(cell, 0x55CC66, 12);
				return true;
			}
			@Override public void onCupDestroyed(Char lastHit) { boss.onArenaCupDestroyed(lastHit); }
			@Override public void showTrueBodyHint() {
				if (Dungeon.hero != null && Dungeon.hero.buff(Drunkenness.class) != null) {
					Splash.at(boss.pos, 0x4B8C50, 1);
				}
			}
			@Override public void spawnIllusions() {
				ArrayList<Integer> positions = new ArrayList<>();
				positions.add(boss.pos);
				for (int i = 0; i < 2; i++) {
					int cell = selectGentlemanDerivedCell(boss.pos, positions);
					if (cell < 0) break;
					positions.add(cell);
				}
				Random.shuffle(positions);
				int shuffledBossCell = positions.remove(0);
				if (shuffledBossCell != boss.pos && boss.sprite != null) {
					ScrollOfTeleportation.appear(boss, shuffledBossCell);
				}
				ArrayList<Integer> ids = new ArrayList<>();
				for (int cell : positions) {
					GentlemanElfIllusion illusion = new GentlemanElfIllusion(boss);
					illusion.pos = cell;
					GameScene.add(illusion, 1f);
					ids.add(illusion.id());
					Splash.at(cell, 0x55CC66, 8);
				}
				int[] values = new int[ids.size()];
				for (int i = 0; i < ids.size(); i++) values[i] = ids.get(i);
				gentlemanElfArena.illusionIds(values);
				boss.syncActiveIllusions(values.length);
			}
		};
	}

	private int selectGentlemanCupCell(GentlemanElf boss) {
		ArrayList<Integer> preferred = new ArrayList<>();
		ArrayList<Integer> fallback = new ArrayList<>();
		for (int cell = 0; cell < length(); cell++) {
			if (!isValidGentlemanDerivedCell(cell) || Actor.findChar(cell) != null) continue;
			fallback.add(cell);
			if (Dungeon.hero != null && distance(cell, Dungeon.hero.pos) >= 3
					&& distance(cell, boss.pos) >= 3) {
				preferred.add(cell);
			}
		}
		return TowerBossLayout.selectRandomGentlemanCupCell(preferred, fallback);
	}

	private int selectGentlemanDerivedCell(int center, ArrayList<Integer> used) {
		int best = -1;
		int bestDistance = Integer.MAX_VALUE;
		for (int cell = 0; cell < length(); cell++) {
			if (!isValidGentlemanDerivedCell(cell) || Actor.findChar(cell) != null || used.contains(cell)) continue;
			int d = distance(center, cell);
			if (d < bestDistance || d == bestDistance && (best < 0 || cell < best)) {
				best = cell;
				bestDistance = d;
			}
		}
		return best;
	}

	private boolean isValidGentlemanDerivedCell(int cell) {
		return cell >= 0 && cell < length() && passable[cell] && !solid[cell]
				&& TowerBossLayout.isArenaCell(cell) && !TowerBossLayout.isProtectedCell(cell)
				&& cell != entrance() && cell != exit();
	}

	protected int selectBossSpawnCell() {
		if (pestilenceArena != null && pestilenceArena.preludeStarted()
				&& pestilenceArena.bossCell() >= 0) {
			return pestilenceArena.bossCell();
		}
		boolean gentlemanElf = TowerBossGenerator.GENTLEMAN_ELF_ID.equals(encounter.selectedBossId());
		for (int attempts = 0; attempts < 200; attempts++) {
			int cell = TowerBossLayout.cell(Random.Int(6, 23), Random.Int(7, 16));
			if (passable[cell] && Actor.findChar(cell) == null
					&& (!gentlemanElf || isGentlemanElfSpawnCell(cell))) {
				return cell;
			}
		}
		if (gentlemanElf) {
			for (int cell = 0; cell < length(); cell++) {
				if (isGentlemanElfSpawnCell(cell) && Actor.findChar(cell) == null) return cell;
			}
		}
		return TowerBossLayout.cell(14, 10);
	}

	private boolean isGentlemanElfSpawnCell(int cell) {
		return cell >= 0 && cell < length() && passable[cell]
				&& TowerBossLayout.isGentlemanElfSpawnTerrainAllowed(map[cell]);
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
		if (hero != null && encounter.bossEncounterDefeated()
				&& TowerBossGenerator.HUNGER_KNIGHT_ID.equals(encounter.selectedBossId())) {
			HungerKnight.cleanupOrphanedEffects(hero);
		}
		if (gentlemanElfPrelude != null && !encounter.bossEncounterDefeated()) {
			continueGentlemanElfPrelude();
		}
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

	public boolean bossEncounterDefeated() {
		return encounter.bossEncounterDefeated();
	}

	public boolean isActiveTowerBoss(String bossId) {
		if (!encounter.bossEncounterStarted() || encounter.bossEncounterDefeated()
				|| !bossId.equals(encounter.selectedBossId())) {
			return false;
		}
		for (Mob mob : mobs) {
			if (mob instanceof TowerBoss && mob.isAlive()
					&& bossId.equals(((TowerBoss) mob).towerBossId())) {
				return true;
			}
		}
		return false;
	}

	private TowerBossEncounter.Host encounterHost() {
		return new TowerBossEncounter.Host() {
			@Override
			public TowerBoss createBoss(String id) {
				return createSelectedBoss();
			}

			@Override
			public int selectBossSpawnCell() {
				if (gentlemanElfPrelude == null || !gentlemanElfPrelude.started()
						|| gentlemanElfPrelude.spawnCell() < 0) {
					reservedBossSpawnCell = TowerBossLevel.this.selectBossSpawnCell();
				} else {
					reservedBossSpawnCell = gentlemanElfPrelude.spawnCell();
				}
				return reservedBossSpawnCell;
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
			relocateSafeZoneCreatures(reservedBossSpawnCell);
			set(TowerBossLayout.SAFE_GATE, Terrain.WALL, this);
			GameScene.updateMap(TowerBossLayout.SAFE_GATE);
			Dungeon.observe();
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					Music.INSTANCE.play(TowerBossMusic.musicFor(encounter.selectedBossId()), true);
				}
			});
		}
	}

	void relocateSafeZoneCreatures(int reservedCell) {
		for (Mob mob : new ArrayList<>(mobs)) {
			if (!TowerBossLayout.shouldRelocateCreature(mob.pos)) {
				continue;
			}
			int destination = randomEncounterCreatureCell(mob, reservedCell);
			if (destination != -1) {
				teleportEncounterCreature(mob, destination);
			}
		}
	}

	private int randomEncounterCreatureCell(Mob creature, int reservedCell) {
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int cell = 0; cell < length(); cell++) {
			if (!TowerBossLayout.isLegalCreatureDestination(cell, passable[cell], openSpace[cell],
					Char.hasProp(creature, Char.Property.LARGE), reservedCell,
					Actor.findChar(cell) != null)) {
				continue;
			}
			candidates.add(cell);
		}
		return candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	protected void teleportEncounterCreature(Mob creature, int destination) {
		ScrollOfTeleportation.appear(creature, destination);
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
		if (gentlemanElfPrelude != null) bundle.put(GENTLEMAN_PRELUDE, gentlemanElfPrelude);
		if (gentlemanElfArena != null) bundle.put("gentleman_elf_arena", gentlemanElfArena);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		gentlemanIntroWindowOpen = false;
		gentlemanElfPrelude = bundle.contains(GENTLEMAN_PRELUDE)
				? (GentlemanElfPrelude) bundle.get(GENTLEMAN_PRELUDE) : null;
		pestilenceArena = bundle.contains(PESTILENCE_ARENA)
				? (PestilenceArenaController) bundle.get(PESTILENCE_ARENA) : null;
		if (pestilenceArena != null) pestilenceArena.syncPurifierVisual(this);
		gentlemanElfArena = bundle.contains("gentleman_elf_arena")
				? (GentlemanElfArena) bundle.get("gentleman_elf_arena") : null;
		if (gentlemanElfArena != null) {
			boolean rebound = false;
			for (Mob mob : mobs) {
				if (mob instanceof GentlemanElf) {
					GentlemanElf boss = (GentlemanElf) mob;
					BossHealthBar.assignBoss(boss);
					boss.bindArena(gentlemanElfArena);
					gentlemanElfArena.bind(gentlemanElfHost(boss));
					rebound = true;
					break;
				}
			}
			if (rebound && gentlemanElfArena.active()) Actor.add(gentlemanElfArena);
		}
		// A restored boss is already in mobs, so restore the UI binding after the
		// level has rebuilt its actors. This also covers the other tower bosses.
		for (Mob mob : mobs) {
			if (mob instanceof TowerBoss && mob.isAlive()) {
				BossHealthBar.assignBoss(mob);
				break;
			}
		}
		boolean exitUnlocked = map[TowerBossLayout.EXIT_GATE] == Terrain.UNLOCKED_EXIT;
		encounter.restoreFromBundle(bundle, Dungeon.seed, Dungeon.depth, Dungeon.branch,
				locked, hasRestoredTowerBoss(), exitUnlocked);
		GentlemanElf restoredGentleman = findGentlemanElf();
		if (TowerBossGenerator.GENTLEMAN_ELF_ID.equals(encounter.selectedBossId())) {
			if (gentlemanElfPrelude == null && restoredGentleman != null
					&& !restoredGentleman.introResolved()) {
				// Migrate saves created while the old Boss-owned dialog was pending.
				gentlemanElfPrelude = new GentlemanElfPrelude();
				gentlemanElfPrelude.begin(restoredGentleman.pos);
			}
			if (gentlemanElfPrelude != null) reservedBossSpawnCell = gentlemanElfPrelude.spawnCell();
		}
		if (TowerBossGenerator.HUNGER_KNIGHT_ID.equals(encounter.selectedBossId())
				&& encounter.bossEncounterStarted() && !hasRestoredTowerBoss()) {
			HungerKnight.cleanupOrphanedEffects(Dungeon.hero);
		}
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
