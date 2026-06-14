package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Read-only observation schema for the local baseline agent.
 *
 * <p>The schema is intentionally plain Java data so it can later be serialized
 * to JSON, protobuf, or a training tensor without coupling the game to a
 * specific reinforcement learning framework.</p>
 */
public class AgentMinState {

	public static final int SCHEMA_VERSION = 1;

	public static final int CELL_UNKNOWN = 0;
	public static final int FLAG_MAPPED = 1 << 16;
	public static final int FLAG_VISITED = 1 << 17;
	public static final int FLAG_VISIBLE = 1 << 18;
	public static final int FLAG_PASSABLE = 1 << 19;
	public static final int FLAG_AVOID = 1 << 20;
	public static final int FLAG_SOLID = 1 << 21;
	public static final int FLAG_LIQUID = 1 << 22;
	public static final int FLAG_PIT = 1 << 23;
	public static final int FLAG_ITEM = 1 << 24;
	public static final int FLAG_TRAP = 1 << 25;
	public static final int FLAG_PLANT = 1 << 26;
	public static final int FLAG_MOB = 1 << 27;
	public static final int FLAG_STAIRS = 1 << 28;

	public int schemaVersion = SCHEMA_VERSION;
	public long observationId;
	public int depth;
	public int branch;
	public LevelState level = new LevelState();
	public HeroState hero = new HeroState();
	public InventoryState inventory = new InventoryState();
	public CombatState combat = new CombatState();

	public static class LevelState {
		public int width;
		public int height;
		public int length;
		public int heroPos;
		public int entrance;
		public int exit;

		// Same cell index as Dungeon.level. Unknown cells are 0.
		public int[] exploredMap;
		public int[] visibleMap;

		public ArrayList<CellEntityState> visibleItems = new ArrayList<>();
		public ArrayList<CellEntityState> visibleTraps = new ArrayList<>();
		public ArrayList<CellEntityState> visiblePlants = new ArrayList<>();
		public ArrayList<MobState> visibleMobs = new ArrayList<>();
	}

	public static class HeroState {
		public String heroClass;
		public String subClass;
		public int pos;
		public int level;
		public int exp;
		public int str;
		public int ht;
		public int hp;
		public float hpRatio;
		public int gold;
		public boolean starving;
		public float hungerLevel;
		public float speed;
		public float attackDelay;
		public int attackSkill;
		public int defenseSkill;
		public int damageMin;
		public int damageMax;
		public float expectedDamage;
		public int armorMin;
		public int armorMax;
		public float expectedArmor;
		public LinkedHashMap<String, Integer> talents = new LinkedHashMap<>();
		public LinkedHashMap<String, Integer> negativeTalents = new LinkedHashMap<>();
		public ArrayList<BuffState> buffs = new ArrayList<>();
	}

	public static class InventoryState {
		public ArrayList<ItemState> equipped = new ArrayList<>();
		public ArrayList<ItemState> backpack = new ArrayList<>();
		public ArrayList<QuickSlotState> quickSlots = new ArrayList<>();
		public ArrayList<ActionState> availableActions = new ArrayList<>();
	}

	public static class CombatState {
		public float heroPowerScore;
		public float visibleEnemyThreatScore;
		public float floorPressureScore;
		public float fightScore;
		public String recommendation;
		public ArrayList<MobCombatState> visibleEnemies = new ArrayList<>();
	}

	public static class MobState {
		public String className;
		public String name;
		public String alignment;
		public int pos;
		public int ht;
		public int hp;
		public float hpRatio;
		public int distanceToHero;
		public boolean visible;
		public boolean alive;
		public ArrayList<BuffState> buffs = new ArrayList<>();
	}

	public static class MobCombatState extends MobState {
		public int estimatedDamage;
		public int estimatedArmor;
		public float threatScore;
		public float targetPriority;
	}

	public static class BuffState {
		public String className;
		public String name;
		public int icon;
		public String iconText;
		public float fadePercent;
	}

	public static class ItemState {
		public String slot;
		public String className;
		public String name;
		public int image;
		public int quantity;
		public int level;
		public int buffedLevel;
		public boolean levelKnown;
		public boolean cursed;
		public boolean cursedKnown;
		public boolean equipped;
		public boolean usesTargeting;
		public String defaultAction;
		public ArrayList<String> actions = new ArrayList<>();
	}

	public static class ActionState {
		public String itemClassName;
		public String itemName;
		public String slot;
		public String action;
		public String actionName;
		public boolean defaultAction;
		public boolean usesTargeting;
		public int quickSlot = -1;
	}

	public static class QuickSlotState {
		public int slot;
		public boolean placeholder;
		public ItemState item;
	}

	public static class CellEntityState {
		public int pos;
		public String className;
		public String name;
		public int image;
		public int count;
		public boolean visible;
	}
}
