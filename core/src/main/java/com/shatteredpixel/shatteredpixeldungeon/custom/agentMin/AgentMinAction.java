package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

public class AgentMinAction {

	public enum Kind {
		ZERO_RANDOM_MOVE,
		MOVE,
		WAIT,
		ATTACK,
		PICK_UP,
		UNLOCK,
		USE_STAIRS,
		DRINK_HEALING,
		EAT_FOOD,
		THROW_WEAPON,
		ZAP_WAND,
		ITEM_ACTION,
		ALCHEMY,
		ACTION_INDICATOR,
		MONITOR_CELL,
		MONITOR_ITEM,
		MONITOR_OPTION,
		TALENT_UPGRADE
	}

	public enum Skill {
		EXPLORE,
		COMBAT,
		EAT,
		UNLOCK,
		PICKUP,
		DESCEND,
		ITEM,
		ALCHEMY,
		TALENT,
		DROP
	}

	public enum StairMode {
		NONE,
		ASCEND,
		DESCEND
	}

	public int actionId;
	public Kind kind;
	public String label;
	public boolean valid = true;
	public int directionIndex = -1;
	public int dx;
	public int dy;
	public int fromCell = -1;
	public int targetCell = -1;
	public int targetMobRow = -1;
	public int itemRow = -1;
	public int itemActionIndex = -1;
	public int itemActionCount;
	public int optionIndex = -1;
	public int quickSlot = -1;
	public Skill skill = Skill.EXPLORE;
	public String itemClassName;
	public String itemName;
	public String itemAction;
	public String talentName;
	public int talentTier = -1;
	public int talentSlot = -1;
	public int talentPoints;
	public int talentMaxPoints;
	public StairMode stairMode = StairMode.NONE;
	public float priority;

	public AgentMinAction(Kind kind, String label) {
		this.kind = kind;
		this.label = label;
	}
}
