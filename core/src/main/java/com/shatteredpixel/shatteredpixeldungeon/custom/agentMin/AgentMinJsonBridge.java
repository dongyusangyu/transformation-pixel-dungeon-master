package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

public class AgentMinJsonBridge {

	public static String observationJson(AgentMinEncodedState encoded) {
		StringBuilder sb = new StringBuilder(1 << 20);
		sb.append('{');
		field(sb, "type", "observation").append(',');
		field(sb, "schema", encoded.schemaVersion).append(',');
		field(sb, "width", encoded.width).append(',');
		field(sb, "height", encoded.height).append(',');
		field(sb, "pending_reward", encoded.pendingReward).append(',');
		field(sb, "episode_reward", encoded.episodeReward).append(',');
		field(sb, "level_tensor", encoded.levelTensor).append(',');
		field(sb, "explored_global_matrix", encoded.exploredGlobalMatrix).append(',');
		field(sb, "hero_vector", encoded.heroVector).append(',');
		field(sb, "inventory_matrix", encoded.inventoryMatrix).append(',');
		field(sb, "inventory_summary_vector", encoded.inventorySummaryVector).append(',');
		field(sb, "option_vector", encoded.optionVector).append(',');
		field(sb, "mob_matrix", encoded.mobMatrix).append(',');
		field(sb, "history_matrix", encoded.historyMatrix).append(',');
		field(sb, "agent_visited_matrix", encoded.agentVisitedMatrix).append(',');
		field(sb, "action_matrix", encoded.actionSpace == null ? null : encoded.actionSpace.actionMatrix).append(',');
		field(sb, "action_mask", encoded.actionSpace == null ? null : encoded.actionSpace.actionMask);
		sb.append('}');
		return sb.toString();
	}

	public static String sampleJson(AgentMinEncodedState encoded, AgentMinAction action, String runId, long sampleIndex) {
		StringBuilder sb = new StringBuilder(1 << 20);
		sb.append('{');
		field(sb, "type", "demo_sample").append(',');
		field(sb, "run_id", runId).append(',');
		field(sb, "sample_index", (int)Math.min(Integer.MAX_VALUE, sampleIndex)).append(',');
		field(sb, "schema", encoded.schemaVersion).append(',');
		field(sb, "width", encoded.width).append(',');
		field(sb, "height", encoded.height).append(',');
		field(sb, "pending_reward", encoded.pendingReward).append(',');
		field(sb, "episode_reward", encoded.episodeReward).append(',');
		field(sb, "level_tensor", encoded.levelTensor).append(',');
		field(sb, "explored_global_matrix", encoded.exploredGlobalMatrix).append(',');
		field(sb, "hero_vector", encoded.heroVector).append(',');
		field(sb, "inventory_matrix", encoded.inventoryMatrix).append(',');
		field(sb, "inventory_summary_vector", encoded.inventorySummaryVector).append(',');
		field(sb, "option_vector", encoded.optionVector).append(',');
		field(sb, "mob_matrix", encoded.mobMatrix).append(',');
		field(sb, "history_matrix", encoded.historyMatrix).append(',');
		field(sb, "agent_visited_matrix", encoded.agentVisitedMatrix).append(',');
		field(sb, "action_matrix", encoded.actionSpace == null ? null : encoded.actionSpace.actionMatrix).append(',');
		field(sb, "action_mask", encoded.actionSpace == null ? null : encoded.actionSpace.actionMask).append(',');
		field(sb, "action_id", action == null ? -1 : action.actionId).append(',');
		field(sb, "action_kind", action == null || action.kind == null ? null : action.kind.name()).append(',');
		field(sb, "action_label", action == null ? null : action.label).append(',');
		field(sb, "action_item_action", action == null ? null : action.itemAction).append(',');
		field(sb, "action_from_cell", action == null ? -1 : action.fromCell).append(',');
		field(sb, "action_target_cell", action == null ? -1 : action.targetCell).append(',');
		field(sb, "action_target_mob_row", action == null ? -1 : action.targetMobRow).append(',');
		field(sb, "action_item_row", action == null ? -1 : action.itemRow).append(',');
		field(sb, "action_quick_slot", action == null ? -1 : action.quickSlot);
		sb.append('}');
		return sb.toString();
	}

	public static int parseActionId(String json) {
		if (json == null) {
			return -1;
		}
		int idx = json.indexOf("\"action\"");
		if (idx < 0) {
			idx = json.indexOf("\"action_id\"");
		}
		if (idx < 0) {
			return -1;
		}
		int colon = json.indexOf(':', idx);
		if (colon < 0) {
			return -1;
		}
		int start = colon + 1;
		while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
			start++;
		}
		int end = start;
		while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
			end++;
		}
		if (end <= start) {
			return -1;
		}
		try {
			return Integer.parseInt(json.substring(start, end));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static StringBuilder field(StringBuilder sb, String name, String value) {
		sb.append('"').append(name).append("\":");
		if (value == null) {
			sb.append("null");
		} else {
			sb.append('"').append(escape(value)).append('"');
		}
		return sb;
	}

	private static StringBuilder field(StringBuilder sb, String name, int value) {
		return sb.append('"').append(name).append("\":").append(value);
	}

	private static StringBuilder field(StringBuilder sb, String name, float value) {
		return sb.append('"').append(name).append("\":").append(value);
	}

	private static StringBuilder field(StringBuilder sb, String name, float[] values) {
		sb.append('"').append(name).append("\":");
		appendArray(sb, values);
		return sb;
	}

	private static StringBuilder field(StringBuilder sb, String name, float[][] values) {
		sb.append('"').append(name).append("\":");
		appendArray(sb, values);
		return sb;
	}

	private static StringBuilder field(StringBuilder sb, String name, float[][][] values) {
		sb.append('"').append(name).append("\":");
		appendArray(sb, values);
		return sb;
	}

	private static void appendArray(StringBuilder sb, float[] values) {
		if (values == null) {
			sb.append("null");
			return;
		}
		sb.append('[');
		for (int i = 0; i < values.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(values[i]);
		}
		sb.append(']');
	}

	private static void appendArray(StringBuilder sb, float[][] values) {
		if (values == null) {
			sb.append("null");
			return;
		}
		sb.append('[');
		for (int i = 0; i < values.length; i++) {
			if (i > 0) sb.append(',');
			appendArray(sb, values[i]);
		}
		sb.append(']');
	}

	private static void appendArray(StringBuilder sb, float[][][] values) {
		if (values == null) {
			sb.append("null");
			return;
		}
		sb.append('[');
		for (int i = 0; i < values.length; i++) {
			if (i > 0) sb.append(',');
			appendArray(sb, values[i]);
		}
		sb.append(']');
	}

	private static String escape(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
