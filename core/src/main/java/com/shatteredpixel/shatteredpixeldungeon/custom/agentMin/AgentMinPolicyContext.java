package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

public class AgentMinPolicyContext {

	private static String metamorphTargetTalent;
	private static String metamorphSourceTalent;
	private static int metamorphTargetType = -1;
	private static String sublimationTargetTalent;
	private static String talentUpgradeTargetTalent;
	private static float[] metamorphTargetEmbedding;
	private static int monitorItemRow = -1;
	private static int monitorCellIndex = -1;
	private static int monitorOptionIndex = -1;

	private AgentMinPolicyContext() {
	}

	public static synchronized void updateFromResponse(String json) {
		metamorphTargetTalent = AgentMinJsonBridge.parseString(json, "metamorph_target_talent");
		metamorphSourceTalent = AgentMinJsonBridge.parseString(json, "metamorph_source_talent");
		metamorphTargetType = AgentMinJsonBridge.parseInt(json, "metamorph_target_type", -1);
		sublimationTargetTalent = AgentMinJsonBridge.parseString(json, "sublimation_target_talent");
		talentUpgradeTargetTalent = AgentMinJsonBridge.parseString(json, "talent_upgrade_target_talent");
		metamorphTargetEmbedding = AgentMinJsonBridge.parseFloatArray(json, "talent_target_embedding");
		monitorItemRow = AgentMinJsonBridge.parseInt(json, "monitor_item_row", -1);
		monitorCellIndex = AgentMinJsonBridge.parseInt(json, "monitor_cell_index", -1);
		monitorOptionIndex = AgentMinJsonBridge.parseInt(json, "monitor_option_index", -1);
	}

	public static synchronized String metamorphTargetTalent() {
		return metamorphTargetTalent;
	}

	public static synchronized String metamorphSourceTalent() {
		return metamorphSourceTalent;
	}

	public static synchronized int metamorphTargetType() {
		return metamorphTargetType;
	}

	public static synchronized String sublimationTargetTalent() {
		return sublimationTargetTalent;
	}

	public static synchronized String talentUpgradeTargetTalent() {
		return talentUpgradeTargetTalent;
	}

	public static synchronized float[] metamorphTargetEmbedding() {
		return metamorphTargetEmbedding == null ? null : metamorphTargetEmbedding.clone();
	}

	public static synchronized int monitorItemRow() {
		return monitorItemRow;
	}

	public static synchronized int monitorCellIndex() {
		return monitorCellIndex;
	}

	public static synchronized int monitorOptionIndex() {
		return monitorOptionIndex;
	}
}
