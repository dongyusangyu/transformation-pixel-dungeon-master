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

package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class GamesInProgress {
	
	public static final int MAX_SLOTS = SaveManager.MAX_SLOTS;
	
	//null means we have loaded info and it is empty, no entry means unknown.
	private static HashMap<Integer, Info> slotStates = new HashMap<>();
	public static int curSlot;
	
	public static HeroClass selectedClass;
	public static int skin;
	
	private static final String GAME_FOLDER = "game%d";
	private static final String GAME_FILE	= "game.dat";
	private static final String DEPTH_FILE	= "depth%d.dat";
	private static final String DEPTH_BRANCH_FILE	= "depth%d-branch%d.dat";
	
	public static boolean gameExists( int slot ){
		return SaveManager.saveExists(slot);
	}
	
	public static String gameFolder( int slot ){
		return Messages.format(GAME_FOLDER, slot);
	}
	
	public static String gameFile( int slot ){
		return gameFolder(slot) + "/" + GAME_FILE;
	}
	
	public static String depthFile( int slot, int depth, int branch ) {
		if (branch == 0) {
			return gameFolder(slot) + "/" + Messages.format(DEPTH_FILE, depth);
		} else {
			return gameFolder(slot) + "/" + Messages.format(DEPTH_BRANCH_FILE, depth, branch);
		}
	}
	
	public static int firstEmpty(){
		return SaveManager.getFirstEmptySlot();
	}

	public static ArrayList<Info> checkAll(){
		ArrayList<Info> result = new ArrayList<>();
		for (int i = 1; i <= MAX_SLOTS; i++){
			Info curr = check(i);
			if (curr != null) result.add(curr);
		}
		switch (SPDSettings.gamesInProgressSort()){
			case "level": default:
				Collections.sort(result, levelComparator);
				break;
			case "last_played":
				Collections.sort(result, lastPlayedComparator);
				break;
		}

		return result;
	}

	public static ArrayList<Info> checkAll(boolean newCycle) {
		return filterByCycle(checkAll(), newCycle);
	}

	static ArrayList<Info> filterByCycle(ArrayList<Info> games, boolean newCycle) {
		ArrayList<Info> result = new ArrayList<>();
		for (Info game : games) {
			if (game.newCycle == newCycle) {
				result.add(game);
			}
		}
		return result;
	}
	
	public static Info check( int slot ) {
		
		if (slotStates.containsKey( slot )) {
			
			return slotStates.get( slot );
			
		} else if (!gameExists( slot )) {
			
			slotStates.put(slot, null);
			return null;
			
		} else {
			
			Info info;
			try {

				Bundle bundle = SaveManager.loadGame(slot);

				if (bundle.getInt( "version" ) < ShatteredPixelDungeon.v2_4_2) {
					info = null;
				} else {

					info = new Info();
					info.slot = slot;
					Dungeon.preview(info, bundle);
				}

			} catch (IOException e) {
				info = null;
			} catch (Exception e){
				ShatteredPixelDungeon.reportException( e );
				info = null;
			}
			
			slotStates.put( slot, info );
			return info;
			
		}
	}

	public static void set(int slot) {
		Info info = new Info();
		info.slot = slot;

		info.lastPlayed = Dungeon.lastPlayed;

		info.depth = Dungeon.depth;
		info.challenges = Dungeon.challenges;

		info.seed = Dungeon.seed;
		info.customSeed = Dungeon.customSeedText;
		info.daily = Dungeon.daily;
		info.dailyReplay = Dungeon.dailyReplay;

		info.level = Dungeon.hero.lvl;
		info.str = Dungeon.hero.STR;
		info.strBonus = Dungeon.hero.STR() - Dungeon.hero.STR;
		info.exp = Dungeon.hero.exp;
		info.hp = Dungeon.hero.HP;
		info.ht = Dungeon.hero.HT;
		info.shld = Dungeon.hero.shielding();
		info.heroClass = Dungeon.hero.heroClass;
		info.subClass = Dungeon.hero.subClass;
		info.armorTier = Dungeon.hero.tier();

		info.goldCollected = Statistics.goldCollected;
		info.maxDepth = Statistics.deepestFloor;
		info.skin=Dungeon.skin;
		info.newCycle = Dungeon.newCycle;

		slotStates.put( slot, info );
	}

	public static void setUnknown( int slot ) {
		slotStates.remove( slot );
	}

	public static void delete( int slot ) {
		slotStates.put( slot, null );
	}

	/**
	 * 将指定存档复制到剪贴板
	 * 直接导出JSON格式，便于分享和备份
	 */
	public static boolean copyToClipboard(int slot) {
		return SaveManager.exportToClipboard(slot);
	}

	/**
	 * 从剪贴板导入存档
	 * 返回导入到的存档槽位，失败时返回-1
	 */
	public static int importFromClipboard() {
		int slot = SaveManager.importFromClipboard();
		if (slot != -1) {
			setUnknown(slot);
		}
		return slot;
	}

	public static class Info {
		public int slot;

		public int depth;
		public int branch;
		public int version;
		public int challenges;

		public long seed;
		public String customSeed;
		public boolean daily;
		public boolean dailyReplay;
		public long lastPlayed;

		public int level;
		public int str;
		public int strBonus;
		public int exp;
		public int hp;
		public int ht;
		public int shld;
		public HeroClass heroClass;
		public HeroSubClass subClass;
		public int armorTier;

		public int goldCollected;
		public int maxDepth;
		public int skin;
		public boolean newCycle;
	}

	public static final Comparator<GamesInProgress.Info> levelComparator = new Comparator<GamesInProgress.Info>() {
		@Override
		public int compare(GamesInProgress.Info lhs, GamesInProgress.Info rhs ) {
			if (rhs.level != lhs.level){
				return (int)Math.signum( rhs.level - lhs.level );
			} else {
				return lastPlayedComparator.compare(lhs, rhs);
			}
		}
	};

	public static final Comparator<GamesInProgress.Info> lastPlayedComparator = new Comparator<GamesInProgress.Info>() {
		@Override
		public int compare(GamesInProgress.Info lhs, GamesInProgress.Info rhs ) {
			return (int)Math.signum( rhs.lastPlayed - lhs.lastPlayed );
		}
	};
}
