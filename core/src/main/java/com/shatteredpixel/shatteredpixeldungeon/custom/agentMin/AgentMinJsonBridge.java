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
		field(sb, "wand_probe", AgentMinBridgeConfig.WAND_PROBE ? 1 : 0).append(',');
		field(sb, "forced_skill", encoded.forcedSkill).append(',');
		field(sb, "level_tensor", encoded.levelTensor).append(',');
		field(sb, "explored_global_matrix", encoded.exploredGlobalMatrix).append(',');
		field(sb, "hero_vector", encoded.heroVector).append(',');
		field(sb, "inventory_matrix", encoded.inventoryMatrix).append(',');
		field(sb, "item_keys", itemKeys(encoded)).append(',');
		field(sb, "modifier_keys", modifierKeys(encoded)).append(',');
		field(sb, "inventory_summary_vector", encoded.inventorySummaryVector).append(',');
		field(sb, "option_vector", encoded.optionVector).append(',');
		field(sb, "mob_matrix", encoded.mobMatrix).append(',');
		field(sb, "mob_keys", mobKeys(encoded)).append(',');
		field(sb, "history_matrix", encoded.historyMatrix).append(',');
		field(sb, "agent_visited_matrix", encoded.agentVisitedMatrix).append(',');
		field(sb, "metamorph_candidate_names", metamorphCandidateNames()).append(',');
		field(sb, "metamorph_candidate_icons", metamorphCandidateIcons()).append(',');
		field(sb, "metamorph_source_names", metamorphSourceNames()).append(',');
		field(sb, "metamorph_source_icons", metamorphSourceIcons()).append(',');
		field(sb, "metamorph_choice_source_names", metamorphChoiceSourceNames()).append(',');
		field(sb, "metamorph_choice_target_names", metamorphChoiceTargetNames()).append(',');
		field(sb, "metamorph_choice_target_icons", metamorphChoiceTargetIcons()).append(',');
		field(sb, "metamorph_choice_target_types", metamorphChoiceTargetTypes()).append(',');
		field(sb, "sublimation_candidate_names", sublimationCandidateNames()).append(',');
		field(sb, "sublimation_candidate_icons", sublimationCandidateIcons()).append(',');
		field(sb, "talent_upgrade_candidate_names", talentUpgradeCandidateNames()).append(',');
		field(sb, "talent_upgrade_candidate_icons", talentUpgradeCandidateIcons()).append(',');
		field(sb, "action_matrix", encoded.actionSpace == null ? null : encoded.actionSpace.actionMatrix).append(',');
		field(sb, "action_mask", encoded.actionSpace == null ? null : encoded.actionSpace.actionMask).append(',');
		field(sb, "monitor_item_mask", encoded.actionSpace == null ? null : encoded.actionSpace.monitorItemMask).append(',');
		field(sb, "monitor_cell_mask", encoded.actionSpace == null ? null : encoded.actionSpace.monitorCellMask).append(',');
		field(sb, "monitor_option_mask", encoded.actionSpace == null ? null : encoded.actionSpace.monitorOptionMask).append(',');
		field(sb, "skill_mask", encoded.actionSpace == null ? null : encoded.actionSpace.skillMask).append(',');
		field(sb, "action_skill_mask", encoded.actionSpace == null ? null : encoded.actionSpace.actionSkillMask);
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
		field(sb, "forced_skill", encoded.forcedSkill).append(',');
		field(sb, "level_tensor", encoded.levelTensor).append(',');
		field(sb, "explored_global_matrix", encoded.exploredGlobalMatrix).append(',');
		field(sb, "hero_vector", encoded.heroVector).append(',');
		field(sb, "inventory_matrix", encoded.inventoryMatrix).append(',');
		field(sb, "item_keys", itemKeys(encoded)).append(',');
		field(sb, "modifier_keys", modifierKeys(encoded)).append(',');
		field(sb, "inventory_summary_vector", encoded.inventorySummaryVector).append(',');
		field(sb, "option_vector", encoded.optionVector).append(',');
		field(sb, "mob_matrix", encoded.mobMatrix).append(',');
		field(sb, "mob_keys", mobKeys(encoded)).append(',');
		field(sb, "history_matrix", encoded.historyMatrix).append(',');
		field(sb, "agent_visited_matrix", encoded.agentVisitedMatrix).append(',');
		field(sb, "action_matrix", encoded.actionSpace == null ? null : encoded.actionSpace.actionMatrix).append(',');
		field(sb, "action_mask", encoded.actionSpace == null ? null : encoded.actionSpace.actionMask).append(',');
		field(sb, "monitor_item_mask", encoded.actionSpace == null ? null : encoded.actionSpace.monitorItemMask).append(',');
		field(sb, "monitor_cell_mask", encoded.actionSpace == null ? null : encoded.actionSpace.monitorCellMask).append(',');
		field(sb, "monitor_option_mask", encoded.actionSpace == null ? null : encoded.actionSpace.monitorOptionMask).append(',');
		field(sb, "skill_mask", encoded.actionSpace == null ? null : encoded.actionSpace.skillMask).append(',');
		field(sb, "action_skill_mask", encoded.actionSpace == null ? null : encoded.actionSpace.actionSkillMask).append(',');
		field(sb, "action_id", action == null ? -1 : action.actionId).append(',');
		field(sb, "action_kind", action == null || action.kind == null ? null : action.kind.name()).append(',');
		field(sb, "action_skill", action == null || action.skill == null ? null : action.skill.name()).append(',');
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
		int value = parseInt(json, "action", Integer.MIN_VALUE);
		return value != Integer.MIN_VALUE ? value : parseInt(json, "action_id", -1);
	}

	public static int parseInt(String json, String name, int fallback) {
		if (json == null || name == null) {
			return fallback;
		}
		int idx = json.indexOf("\"" + name + "\"");
		if (idx < 0) {
			return fallback;
		}
		int colon = json.indexOf(':', idx);
		if (colon < 0) {
			return fallback;
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
			return fallback;
		}
		try {
			return Integer.parseInt(json.substring(start, end));
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	public static String parseString(String json, String name) {
		if (json == null || name == null) {
			return null;
		}
		int idx = json.indexOf("\"" + name + "\"");
		if (idx < 0) {
			return null;
		}
		int colon = json.indexOf(':', idx);
		if (colon < 0) {
			return null;
		}
		int start = colon + 1;
		while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
			start++;
		}
		if (start >= json.length() || json.charAt(start) == 'n') {
			return null;
		}
		if (json.charAt(start) != '"') {
			return null;
		}
		StringBuilder out = new StringBuilder();
		boolean escaping = false;
		for (int i = start + 1; i < json.length(); i++) {
			char c = json.charAt(i);
			if (escaping) {
				if (c == 'n') out.append('\n');
				else if (c == 'r') out.append('\r');
				else if (c == 't') out.append('\t');
				else out.append(c);
				escaping = false;
			} else if (c == '\\') {
				escaping = true;
			} else if (c == '"') {
				return out.toString();
			} else {
				out.append(c);
			}
		}
		return null;
	}

	public static float[] parseFloatArray(String json, String name) {
		if (json == null || name == null) {
			return null;
		}
		int idx = json.indexOf("\"" + name + "\"");
		if (idx < 0) {
			return null;
		}
		int colon = json.indexOf(':', idx);
		int open = colon < 0 ? -1 : json.indexOf('[', colon);
		int close = open < 0 ? -1 : json.indexOf(']', open);
		if (open < 0 || close < 0 || close <= open) {
			return null;
		}
		String[] parts = json.substring(open + 1, close).split(",");
		float[] values = new float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				values[i] = Float.parseFloat(parts[i].trim());
			} catch (NumberFormatException e) {
				values[i] = 0f;
			}
		}
		return values;
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

	private static StringBuilder field(StringBuilder sb, String name, String[] values) {
		sb.append('"').append(name).append("\":");
		appendArray(sb, values);
		return sb;
	}

	private static StringBuilder field(StringBuilder sb, String name, int value) {
		return sb.append('"').append(name).append("\":").append(value);
	}

	private static StringBuilder field(StringBuilder sb, String name, int[] values) {
		sb.append('"').append(name).append("\":");
		appendArray(sb, values);
		return sb;
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

	private static void appendArray(StringBuilder sb, int[] values) {
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

	private static void appendArray(StringBuilder sb, String[] values) {
		if (values == null) {
			sb.append("null");
			return;
		}
		sb.append('[');
		for (int i = 0; i < values.length; i++) {
			if (i > 0) sb.append(',');
			if (values[i] == null) {
				sb.append("null");
			} else {
				sb.append('"').append(escape(values[i])).append('"');
			}
		}
		sb.append(']');
	}

	private static String[] itemKeys(AgentMinEncodedState encoded) {
		String[] keys = new String[AgentMinEncodedState.INVENTORY_ROWS];
		if (encoded == null || encoded.source == null || encoded.source.inventory == null) {
			return keys;
		}
		int row = 0;
		for (AgentMinState.ItemState item : encoded.source.inventory.equipped) {
			if (row >= keys.length) return keys;
			keys[row++] = item == null ? "" : item.className;
		}
		for (AgentMinState.ItemState item : encoded.source.inventory.backpack) {
			if (row >= keys.length) return keys;
			keys[row++] = item == null ? "" : item.className;
		}
		return keys;
	}

	private static String[] modifierKeys(AgentMinEncodedState encoded) {
		String[] keys = new String[AgentMinEncodedState.INVENTORY_ROWS];
		if (encoded == null || encoded.source == null || encoded.source.inventory == null) {
			return keys;
		}
		int row = 0;
		for (AgentMinState.ItemState item : encoded.source.inventory.equipped) {
			if (row >= keys.length) return keys;
			keys[row++] = item == null ? "" : item.modifierClassName;
		}
		for (AgentMinState.ItemState item : encoded.source.inventory.backpack) {
			if (row >= keys.length) return keys;
			keys[row++] = item == null ? "" : item.modifierClassName;
		}
		return keys;
	}

	private static String[] mobKeys(AgentMinEncodedState encoded) {
		String[] keys = new String[AgentMinEncodedState.MOB_ROWS];
		if (encoded == null || encoded.source == null || encoded.source.combat == null) {
			return keys;
		}
		int row = 0;
		for (AgentMinState.MobCombatState mob : encoded.source.combat.visibleEnemies) {
			if (row >= keys.length) return keys;
			keys[row++] = mob == null ? "" : mob.className;
		}
		return keys;
	}

	private static String[] metamorphCandidateNames() {
		return AgentMinMetamorphPlanner.targetNames(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static int[] metamorphCandidateIcons() {
		return AgentMinMetamorphPlanner.targetIcons(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static String[] metamorphSourceNames() {
		return AgentMinMetamorphPlanner.sourceNames(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static int[] metamorphSourceIcons() {
		return AgentMinMetamorphPlanner.sourceIcons(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static String[] metamorphChoiceSourceNames() {
		return AgentMinMetamorphPlanner.choiceSourceNames(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static String[] metamorphChoiceTargetNames() {
		return AgentMinMetamorphPlanner.choiceTargetNames(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static int[] metamorphChoiceTargetIcons() {
		return AgentMinMetamorphPlanner.choiceTargetIcons(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static int[] metamorphChoiceTargetTypes() {
		return AgentMinMetamorphPlanner.choiceTargetTypes(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static String[] sublimationCandidateNames() {
		return AgentMinMetamorphPlanner.sublimationCandidateNames(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static int[] sublimationCandidateIcons() {
		return AgentMinMetamorphPlanner.sublimationCandidateIcons(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static String[] talentUpgradeCandidateNames() {
		return AgentMinMetamorphPlanner.upgradeCandidateNames(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static int[] talentUpgradeCandidateIcons() {
		return AgentMinMetamorphPlanner.upgradeCandidateIcons(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
	}

	private static String escape(String text) {
		return text.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}
