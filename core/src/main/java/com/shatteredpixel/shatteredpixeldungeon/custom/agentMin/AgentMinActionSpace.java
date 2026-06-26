package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import java.util.ArrayList;

public class AgentMinActionSpace {

	public static final int SCHEMA_VERSION = 7;
	public static final int MAX_ACTIONS = 160;
	public static final int ACTION_FEATURES = 52;
	public static final int MAX_SKILLS = 10;
	public static final int MONITOR_CELL_SIZE = AgentMinEncodedState.LOCAL_MAP_SIZE * AgentMinEncodedState.LOCAL_MAP_SIZE;
	public static final int MONITOR_OPTION_ROWS = 32;

	public int schemaVersion = SCHEMA_VERSION;
	public ArrayList<AgentMinAction> actions = new ArrayList<>();
	public float[][] actionMatrix = new float[MAX_ACTIONS][ACTION_FEATURES];
	public float[] actionMask = new float[MAX_ACTIONS];
	public float[][] actionSkillMask = new float[MAX_SKILLS][MAX_ACTIONS];
	public float[] skillMask = new float[MAX_SKILLS];
	public float[] monitorItemMask = new float[AgentMinEncodedState.INVENTORY_ROWS];
	public float[] monitorCellMask = new float[MONITOR_CELL_SIZE];
	public float[] monitorOptionMask = new float[MONITOR_OPTION_ROWS];
	public int actionCount;
	public int forcedSkill = -1;
	public String monitorType;

	public AgentMinAction get(int index) {
		return index >= 0 && index < actions.size() ? actions.get(index) : null;
	}
}
