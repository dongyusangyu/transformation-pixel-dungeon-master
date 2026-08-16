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

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;

import java.util.Arrays;
import java.util.HashSet;

public class Statistics {

	public static long goldCollected;
	public static int deepestFloor;
	public static int deepestTowerFloor;
	public static int highestAscent;
	public static int enemiesSlain;
	public static int foodEaten;
	public static int itemsCrafted;
	public static int piranhasKilled;
	public static int hazardAssistedKills;
	public static int ankhsUsed;
	//tracks every item type 'seen' this run (i.e. would be added to catalogs)
	public static HashSet<Class> itemTypesDiscovered = new HashSet<>();

	public static void discoverItemType(Class<?> itemType) {
		if (itemType != null) itemTypesDiscovered.add(itemType);
	}

	//These are used for score calculation
	// some are built incrementally, most are assigned when full score is calculated
	public static double progressScore;
	public static double heldItemValue;
	public static double treasureScore;
	//public static SparseArray<Boolean> floorsExplored = new SparseArray<>();
	public static SparseArray<Float> floorsExplored = new SparseArray<>();
	public static double exploreScore;
	public static double[] bossScores = new double[10];
	public static double totalBossScore;
	public static double[] questScores = new double[5];
	public static boolean[] subLimation = new boolean[5];
	public static boolean[] negativetalents = new boolean[4];
	public static double totalQuestScore;
	public static double winMultiplier;
	public static double chalMultiplier;
	public static double totalScore;

	//used for hero unlock badges
	public static int upgradesUsed;
	public static int metamorphosis;
	public static int sneakAttacks;
	public static int thrownAttacks;

	public static int spawnersAlive;
	
	public static float duration;

	public static boolean qualifiedForNoKilling = false;
	public static boolean completedWithNoKilling = false;
	public static boolean qualifiedForBossRemainsBadge = false;
	public static boolean qualifiedForBossChallengeBadge = false;
	
	public static boolean amuletObtained = false;
	public static boolean gameWon = false;
	public static boolean ascended = false;
	
	public static void reset() {
		
		goldCollected	= 0;
		deepestFloor	= 0;
		deepestTowerFloor = 0;
		highestAscent	= 0;
		enemiesSlain	= 0;
		foodEaten		= 0;
		itemsCrafted    = 0;
		piranhasKilled	= 0;
		hazardAssistedKills = 0;
		ankhsUsed		= 0;
		itemTypesDiscovered.clear();

		progressScore   = 0;
		heldItemValue   = 0;
		treasureScore   = 0;
		floorsExplored  = new SparseArray<>();
		exploreScore    = 0;
		bossScores      = new double[10];
		totalBossScore  = 0;
		questScores     = new double[5];
		totalQuestScore = 0;
		winMultiplier   = 1;
		chalMultiplier  = 1;
		totalScore      = 0;
		hazardAssistedKills = 0;
		upgradesUsed    = 0;
		metamorphosis   = 0;
		sneakAttacks    = 0;
		thrownAttacks   = 0;

		spawnersAlive   = 0;
		
		duration	    = 0;
		itemTypesDiscovered.clear();

		for(int t = 0;t < subLimation.length;t++){
			subLimation[t] = false;
		}
		for(int t = 0;t < negativetalents.length;t++){
			negativetalents[t] = false;
		}

		
		qualifiedForNoKilling = false;
		qualifiedForBossRemainsBadge = false;
		qualifiedForBossChallengeBadge = false;
		
		amuletObtained = false;
		gameWon = false;
		ascended = false;
		
	}
	
	private static final String GOLD		= "score";
	private static final String DEEPEST		= "maxDepth";
	private static final String DEEPEST_TOWER = "maxTowerDepth";
	private static final String HIGHEST		= "maxAscent";
	private static final String SLAIN		= "enemiesSlain";
	private static final String FOOD		= "foodEaten";
	private static final String ALCHEMY		= "potionsCooked";
	private static final String PIRANHAS	= "priranhas";
	private static final String HAZARD_ASSISTS	= "hazard_assists";
	private static final String ANKHS		= "ankhsUsed";

	private static final String PROG_SCORE	    = "prog_score";
	private static final String ITEM_VAL	    = "item_val";
	private static final String TRES_SCORE      = "tres_score";
	private static final String FLR_EXPL        = "flr_expl_";
	private static final String FLR_EXPL_KEYS   = "flr_expl_keys";
	private static final String FLR_EXPL_VALUES = "flr_expl_values";
	private static final String EXPL_SCORE      = "expl_score";
	private static final String BOSS_SCORES		= "boss_scores";
	private static final String TOT_BOSS		= "tot_boss";
	private static final String QUEST_SCORES	= "quest_scores";
	private static final String TOT_QUEST		= "tot_quest";
	private static final String WIN_MULT		= "win_mult";
	private static final String CHAL_MULT		= "chal_mult";
	private static final String TOTAL_SCORE		= "total_score";
	
	private static final String UPGRADES	= "upgradesUsed";

	private static final String METAMORPHOSIS	= "metamorphosis";
	private static final String SNEAKS		= "sneakAttacks";
	private static final String THROWN		= "thrownAssists";

	private static final String ITEM_TYPES_DISCOVERED    = "item_types_discovered";

	private static final String SPAWNERS	= "spawnersAlive";
	
	private static final String DURATION	= "duration";

	private static final String NO_KILLING_QUALIFIED	= "qualifiedForNoKilling";
	private static final String BOSS_REMAINS_QUALIFIED	= "qualifiedForBossRemainsBadge";
	private static final String BOSS_CHALLENGE_QUALIFIED= "qualifiedForBossChallengeBadge";
	
	private static final String AMULET          = "amuletObtained";
	private static final String WON		        = "won";
	private static final String ASCENDED		= "ascended";


	private static final String SUBLIMATION	= "sublimation";
	private static final String NEGATIVETALENTS	= "negativetalents";
	
	public static void storeInBundle( Bundle bundle ) {
		bundle.put( GOLD,		goldCollected );
		bundle.put( DEEPEST,	deepestFloor );
		bundle.put( DEEPEST_TOWER, deepestTowerFloor );
		bundle.put( HIGHEST,	highestAscent );
		bundle.put( SLAIN,		enemiesSlain );
		bundle.put( FOOD,		foodEaten );
		bundle.put( ALCHEMY,    itemsCrafted );
		bundle.put( PIRANHAS,	piranhasKilled );
		bundle.put(HAZARD_ASSISTS, hazardAssistedKills);
		bundle.put( ANKHS,		ankhsUsed );
		bundle.put( ITEM_TYPES_DISCOVERED, itemTypesDiscovered.toArray(new Class<?>[0]) );

		bundle.put( PROG_SCORE,  progressScore );
		bundle.put( ITEM_VAL,    heldItemValue );
		bundle.put( TRES_SCORE,  treasureScore );
		int[] exploredKeys = floorsExplored.keyArray();
		float[] exploredValues = new float[exploredKeys.length];
		for (int i = 0; i < exploredKeys.length; i++) {
			exploredValues[i] = floorsExplored.get(exploredKeys[i]);
		}
		bundle.put( FLR_EXPL_KEYS, exploredKeys );
		bundle.put( FLR_EXPL_VALUES, exploredValues );

		bundle.put( EXPL_SCORE,  exploreScore );
		bundle.put( BOSS_SCORES, bossScores );
		bundle.put( TOT_BOSS,    totalBossScore );
		bundle.put( QUEST_SCORES,questScores );
		bundle.put( TOT_QUEST,   totalQuestScore );
		bundle.put( WIN_MULT,    winMultiplier );
		bundle.put( CHAL_MULT,   chalMultiplier );
		bundle.put( TOTAL_SCORE, totalScore );

		bundle.put( UPGRADES,   upgradesUsed );
		bundle.put( SNEAKS,		sneakAttacks );
		bundle.put( THROWN,     thrownAttacks);

		bundle.put( SPAWNERS,	spawnersAlive );
		
		bundle.put( DURATION,	duration );

		bundle.put(NO_KILLING_QUALIFIED, qualifiedForNoKilling);
		bundle.put(BOSS_REMAINS_QUALIFIED, qualifiedForBossRemainsBadge);
		bundle.put(BOSS_CHALLENGE_QUALIFIED, qualifiedForBossChallengeBadge);
		
		bundle.put( AMULET,		amuletObtained );
		bundle.put( WON,        gameWon );
		bundle.put( ASCENDED,   ascended );
		bundle.put( ITEM_TYPES_DISCOVERED, itemTypesDiscovered.toArray(new Class<?>[0]) );
		bundle.put( SUBLIMATION,  subLimation );
		bundle.put( NEGATIVETALENTS,  negativetalents );

	}
	
	public static void restoreFromBundle( Bundle bundle ) {
		goldCollected	= bundle.getLong( GOLD );
		deepestFloor	= bundle.getInt( DEEPEST );
		deepestTowerFloor = bundle.getInt( DEEPEST_TOWER );
		highestAscent   = bundle.getInt( HIGHEST );
		enemiesSlain	= bundle.getInt( SLAIN );
		foodEaten		= bundle.getInt( FOOD );
		itemsCrafted    = bundle.getInt( ALCHEMY );
		piranhasKilled	= bundle.getInt( PIRANHAS );
		hazardAssistedKills = bundle.getInt( HAZARD_ASSISTS );
		ankhsUsed		= bundle.getInt( ANKHS );

		if (bundle.contains( ITEM_TYPES_DISCOVERED )) {
			itemTypesDiscovered = new HashSet<>(Arrays.asList(bundle.getClassArray(ITEM_TYPES_DISCOVERED)));
		} else {
			itemTypesDiscovered.clear();
		}

		progressScore   = bundle.getDouble( PROG_SCORE );
		heldItemValue   = bundle.getDouble( ITEM_VAL );
		treasureScore   = bundle.getDouble( TRES_SCORE );
		floorsExplored.clear();
		if (bundle.contains( FLR_EXPL_KEYS ) && bundle.contains( FLR_EXPL_VALUES )) {
			int[] keys = bundle.getIntArray( FLR_EXPL_KEYS );
			float[] values = bundle.getFloatArray( FLR_EXPL_VALUES );
			for (int i = 0; i < Math.min(keys.length, values.length); i++) {
				floorsExplored.put(keys[i], values[i]);
			}
		} else {
			for (int i = 1; i < 26; i++){
				if (bundle.contains( FLR_EXPL+i )){
					//we have this check to reduce an error with bad conversion specifically in v3.1-BETA-1.0
					if (!Dungeon.bossLevel(i) && i <= deepestFloor){
						floorsExplored.put(i, bundle.getFloat( FLR_EXPL+i ));
					}
				//pre-3.1 saves. The bundle key does have an underscore and is a boolean
				} else if (bundle.contains( "flr_expl"+i )){
					floorsExplored.put(i, bundle.getBoolean( "flr_expl"+i ) ? 1f : 0f);
				}
			}
		}
		exploreScore    = bundle.getDouble( EXPL_SCORE );
		if (bundle.contains( BOSS_SCORES )) {
			double[] a = bundle.getDoubleArray( BOSS_SCORES );
			bossScores = new double[10];
			for(int i = 0; i < Math.min(a.length, bossScores.length); i++){
				bossScores[i] = a[i];
			}

		}
		else                                bossScores = new double[10];
		totalBossScore  = bundle.getDouble( TOT_BOSS );
		questScores = new double[5];
		if (bundle.contains( QUEST_SCORES )) {
			double[] restoredQuestScores = bundle.getDoubleArray( QUEST_SCORES );
			System.arraycopy(restoredQuestScores, 0, questScores, 0,
					Math.min(restoredQuestScores.length, questScores.length));
		}
		totalQuestScore = bundle.getDouble( TOT_QUEST );
		winMultiplier   = bundle.getDouble( WIN_MULT );
		chalMultiplier  = bundle.getDouble( CHAL_MULT );
		totalScore      = bundle.getDouble( TOTAL_SCORE );
		
		upgradesUsed    = bundle.getInt( UPGRADES );
		metamorphosis   = bundle.getInt(METAMORPHOSIS);
		sneakAttacks    = bundle.getInt( SNEAKS );
		thrownAttacks   = bundle.getInt( THROWN );

		spawnersAlive   = bundle.getInt( SPAWNERS );
		
		duration		= bundle.getFloat( DURATION );

		qualifiedForNoKilling = bundle.getBoolean( NO_KILLING_QUALIFIED );
		qualifiedForBossRemainsBadge = bundle.getBoolean( BOSS_REMAINS_QUALIFIED );
		qualifiedForBossChallengeBadge = bundle.getBoolean( BOSS_CHALLENGE_QUALIFIED );
		
		amuletObtained	= bundle.getBoolean( AMULET );
		gameWon         = bundle.getBoolean( WON );
		ascended        = bundle.getBoolean( ASCENDED );
		if (bundle.contains( ITEM_TYPES_DISCOVERED )) {
			itemTypesDiscovered = new HashSet<>(Arrays.asList(bundle.getClassArray(ITEM_TYPES_DISCOVERED)));
		} else {
			itemTypesDiscovered.clear();
		}
		if(bundle.contains(SUBLIMATION)){
			subLimation = bundle.getBooleanArray( SUBLIMATION );
		}else{
			subLimation = 	new boolean[5];
			for(int t = 0;t < subLimation.length;t++){
				subLimation[t] = false;
			}
		}
		if(bundle.contains(NEGATIVETALENTS)){
			negativetalents = bundle.getBooleanArray( NEGATIVETALENTS );
		}else{
			negativetalents = 	new boolean[4];
			for(int t = 0;t < Dungeon.hero.negativeTalents.size();t++){
				negativetalents[t] = true;
			}
		}
	}

	public static boolean observeDepth(int depth, int branch) {
		if (branch == TowerLevel.BRANCH) {
			if (depth > deepestTowerFloor) {
				deepestTowerFloor = depth;
			}
			return false;
		}
		if (branch == 0 && depth > deepestFloor) {
			deepestFloor = depth;
			return true;
		}
		return false;
	}

	public static void recoverTowerDepth(int depth, int branch) {
		if (branch == TowerLevel.BRANCH && depth > deepestTowerFloor) {
			deepestTowerFloor = depth;
		}
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ){
		info.goldCollected  = bundle.getLong( GOLD );
		if (info.newCycle) {
			int savedTowerDepth = bundle.getInt(DEEPEST_TOWER);
			info.maxDepth = Math.max(savedTowerDepth,
					info.branch == TowerLevel.BRANCH ? info.depth : 0);
		} else {
			info.maxDepth = bundle.getInt( DEEPEST );
		}
	}

}
