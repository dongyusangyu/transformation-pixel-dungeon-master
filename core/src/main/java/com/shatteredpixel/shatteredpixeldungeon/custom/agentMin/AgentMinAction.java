package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

public class AgentMinAction {

	public enum Kind {
		MOVE,
		WAIT,
		ATTACK,
		PICK_UP,
		USE_STAIRS,
		DRINK_HEALING,
		THROW_WEAPON,
		ZAP_WAND
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
	public int quickSlot = -1;
	public String itemClassName;
	public String itemName;
	public String itemAction;
	public StairMode stairMode = StairMode.NONE;
	public float priority;

	public AgentMinAction(Kind kind, String label) {
		this.kind = kind;
		this.label = label;
	}
}
