package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import java.util.ArrayList;

public class AgentMinActionSpace {

	public static final int SCHEMA_VERSION = 1;
	public static final int MAX_ACTIONS = 96;
	public static final int ACTION_FEATURES = 40;

	public int schemaVersion = SCHEMA_VERSION;
	public ArrayList<AgentMinAction> actions = new ArrayList<>();
	public float[][] actionMatrix = new float[MAX_ACTIONS][ACTION_FEATURES];
	public float[] actionMask = new float[MAX_ACTIONS];
	public int actionCount;

	public AgentMinAction get(int index) {
		return index >= 0 && index < actions.size() ? actions.get(index) : null;
	}
}
