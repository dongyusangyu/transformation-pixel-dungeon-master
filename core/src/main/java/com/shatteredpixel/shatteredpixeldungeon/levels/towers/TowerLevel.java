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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;
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
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TowerLevel extends RegularLevel {

	private static final int FIRST_CONTENT_DEPTH = 16;
	private static final int LAST_CONTENT_DEPTH = 25;
	// Stored as a positive branch depth; MenuPane presents these floors as -1, -2, and so on.
	public static final int BRANCH = 3;

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
		Dungeon.depth = contentDepthForFloor(towerDepth);
		try {
			built = super.build();
		} finally {
			Dungeon.depth = towerDepth;
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
		switch (TowerMobRules.select(towerFloor(), Random.Float())) {
			case CAMOUFLAGE_GNOLL:
				return new CamouflageGnoll();
			case CORROSIVE_SWARM:
				return new CorrosiveSwarm();
			case DEFAULT_POOL:
			default:
				break;
		}
		int actualDepth = Dungeon.depth;
		Dungeon.depth = contentDepth();
		try {
			return super.createMob();
		} finally {
			Dungeon.depth = actualDepth;
		}
	}

	@Override
	protected void createItems() {
		int actualDepth = Dungeon.depth;
		Dungeon.depth = contentDepth();
		try {
			super.createItems();
		} finally {
			Dungeon.depth = actualDepth;
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
		return Math.max(1, Dungeon.depth);
	}

	private int contentDepth() {
		return contentDepthForFloor(towerFloor());
	}

	private int contentDepthForFloor(int floor) {
		int offset = Math.min(
				LAST_CONTENT_DEPTH - FIRST_CONTENT_DEPTH,
				Math.max(0, floor - 1));
		return FIRST_CONTENT_DEPTH + offset;
	}
}
