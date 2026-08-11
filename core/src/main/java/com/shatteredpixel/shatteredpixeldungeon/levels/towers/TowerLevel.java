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

package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Corpse;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.EarthlySerpent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.HeavyCrabification;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Obscura;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.SoulCollector;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.HallwayRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.LibraryHallRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.PillarsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.RuinsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.SegmentedLibraryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StatuesRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.entrance.LibraryHallEntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.exit.LibraryHallExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CorrosionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisarmingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TowerLevel extends RegularLevel {

	private static final int FIRST_CONTENT_DEPTH = 16;
	private static final int LAST_CONTENT_DEPTH = 25;
	// Stored as a positive branch depth; UI presents these floors as T1, T2, and so on.
	public static final int BRANCH = 3;
	private int generationTowerFloor = -1;

	{
		color1 = 0x7B2D26;
		color2 = 0xD7B867;
	}

	@Override
	public void playLevelMusic() {
		if (Statistics.amuletObtained) {
			Music.INSTANCE.play(Assets.Music.CITY_TENSE, true);
		} else {
			Music.INSTANCE.playTracks(
					CityLevel.CITY_TRACK_LIST,
					CityLevel.CITY_TRACK_CHANCES,
					false);
		}
	}

	@Override
	protected boolean build() {
		int towerDepth = Dungeon.depth;
		boolean built;
		generationTowerFloor = towerDepth;
		Dungeon.depth = contentDepthForFloor(towerDepth);
		try {
			built = super.build();
		} finally {
			Dungeon.depth = towerDepth;
			generationTowerFloor = -1;
		}
		if (!built) {
			return false;
		}

		for (LevelTransition transition : transitions) {
			if (transition.type == LevelTransition.Type.REGULAR_ENTRANCE) {
				transition.destDepth = Dungeon.depth - 1;
				transition.destBranch = Dungeon.depth == 1 ? 0 : BRANCH;
				transition.destType = Dungeon.depth == 1
						? LevelTransition.Type.REGULAR_ENTRANCE
						: LevelTransition.Type.REGULAR_EXIT;
			} else if (transition.type == LevelTransition.Type.REGULAR_EXIT) {
				transition.destDepth = Dungeon.depth + 1;
				transition.destBranch = BRANCH;
				transition.destType = LevelTransition.Type.REGULAR_ENTRANCE;
			}
		}
		return true;
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (!Dungeon.towerTransitionAllowed(
				Dungeon.depth, Dungeon.branch, transition.destDepth, transition.destBranch)) {
			GLog.w(Messages.get(TowerLevel.class, "surface_blocked"));
			return false;
		}
		return super.activateTransition(hero, transition);
	}

	@Override
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> result = new ArrayList<>();
		result.add(roomEntrance = new LibraryHallEntranceRoom());
		result.add(roomExit = new LibraryHallExitRoom());

		int standards = standardRooms(feeling == Feeling.LARGE);
		if (feeling == Feeling.LARGE) {
			standards = (int) Math.ceil(standards * 1.5f);
		}
		for (int i = 0; i < standards; i++) {
			StandardRoom room;
			do {
				room = createTowerRoom();
			} while (!room.setSizeCat(standards - i));
			i += room.sizeFactor() - 1;
			result.add(room);
		}
		if (isShopFloor(towerFloor())) {
			result.add(new TowerShopRoom(towerFloor()));
		}
		return result;
	}

	private StandardRoom createTowerRoom() {
		switch (Random.Int(6)) {
			case 0:
				return new LibraryHallRoom();
			case 1:
				return new PillarsRoom();
			case 2:
				return new StatuesRoom();
			case 3:
				return new SegmentedLibraryRoom();
			case 4:
				return new RuinsRoom();
			default:
				return new HallwayRoom();
		}
	}

	@Override
	protected int standardRooms(boolean forceMax) {
		return forceMax ? 9 : 7 + Random.chances(new float[]{1, 2, 1});
	}

	@Override
	protected int specialRooms(boolean forceMax) {
		return 0;
	}

	@Override
	protected Painter painter() {
		return new TowerPainter()
				.setWater(feeling == Feeling.WATER ? 0.65f : 0.15f, 4)
				.setGrass(feeling == Feeling.GRASS ? 0.55f : 0.12f, 3)
				.setTraps(nTraps(), trapClasses(), trapChances());
	}

	@Override
	protected int nTraps() {
		return 4 + Math.min(4, (towerFloor() - 1) / 5);
	}

	@Override
	protected Class<?>[] trapClasses() {
		return new Class[]{
				FrostTrap.class,
				StormTrap.class,
				CorrosionTrap.class,
				GuardianTrap.class,
				DisarmingTrap.class,
				SummoningTrap.class,
				WarpingTrap.class
		};
	}

	@Override
	protected float[] trapChances() {
		return new float[]{4, 4, 3, 2, 2, 1, 1};
	}

	@Override
	public int mobLimit() {
		int mobs = 5 + Math.min(4, (towerFloor() - 1) / 3);
		if (feeling == Feeling.LARGE) {
			mobs = (int) Math.ceil(mobs * 1.33f);
		}
		return mobs;
	}

	@Override
	public Mob createMob() {
		Mob mob;
		switch (TowerMobRules.select(Random.Float())) {
			case CAMOUFLAGE_GNOLL:
				mob = new CamouflageGnoll();
				break;
			case CORROSIVE_SWARM:
				mob = new CorrosiveSwarm();
				break;
			case CORPSE:
				mob = new Corpse();
				break;
			case EARTHLY_SERPENT:
				mob = new EarthlySerpent();
				break;
			case ROAST_LAMB_WARLOCK:
				mob = new RoastLambWarlock();
				break;
			case MECHANICAL_FIST:
				mob = new MechanicalFist();
				break;
			case MIMIC_CROCODILE:
				mob = new MimicCrocodile();
				break;
			case OBSCURA:
				mob = new Obscura();
				break;
			case ALIENATED_PRISMATIC_GUARD:
				mob = new AlienatedPrismaticGuard();
				break;
			case SOUL_COLLECTOR:
				mob = new SoulCollector();
				break;
			case HEAVY_CRABIFICATION:
				mob = new HeavyCrabification();
				break;
			default:
				throw new IllegalStateException("Unknown tower mob selection");
		}
		return TowerMobRules.prepareNaturalSpawn(mob);
	}

	@Override
	protected void createItems() {
		int actualDepth = Dungeon.depth;
		generationTowerFloor = actualDepth;
		Dungeon.depth = contentDepthForFloor(actualDepth);
		try {
			addTowerGuaranteedItems();
			super.createItems();
		} finally {
			Dungeon.depth = actualDepth;
			generationTowerFloor = -1;
		}
	}

	protected void addTowerGuaranteedItems() {
		addItemToSpawn(TowerGenerationRules.guaranteedFloorItem());
	}

	@Override
	public void addItemToSpawn(Item item) {
		if (!TowerGenerationRules.isForbiddenNaturalItem(item)) {
			super.addItemToSpawn(item);
		}
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CHINESE_HALL;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CHINESE_HALL;
	}

	private int towerFloor() {
		return generationTowerFloor > 0
				? generationTowerFloor
				: Math.max(1, Dungeon.depth);
	}

	private int contentDepthForFloor(int floor) {
		int offset = Math.min(
				LAST_CONTENT_DEPTH - FIRST_CONTENT_DEPTH,
				Math.max(0, floor - 1));
		return FIRST_CONTENT_DEPTH + offset;
	}

	static boolean isShopFloor(int floor) {
		return TowerGenerationRules.isShopFloor(floor);
	}

	static int shopPriceDepth(int floor) {
		return TowerGenerationRules.shopPriceDepth(floor);
	}
}
