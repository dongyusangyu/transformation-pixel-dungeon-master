package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import java.util.ArrayList;

public class AgentMinStateEncoder {

	private static final Talent[] TALENTS = Talent.values();

	private static final int LC_KNOWN = 0;
	private static final int LC_VISIBLE = 1;
	private static final int LC_TERRAIN = 2;
	private static final int LC_PASSABLE = 3;
	private static final int LC_AVOID = 4;
	private static final int LC_SOLID = 5;
	private static final int LC_LIQUID = 6;
	private static final int LC_PIT = 7;
	private static final int LC_ITEM = 8;
	private static final int LC_TRAP = 9;
	private static final int LC_PLANT = 10;
	private static final int LC_MOB = 11;
	private static final int LC_STAIRS = 12;
	private static final int LC_VISITED = 13;
	private static final int LC_MAPPED = 14;
	private static final int LC_HERO = 15;
	private static final int LC_EXIT = 16;
	private static final int LC_EXPLORED_NOT_VISIBLE = 17;

	public static AgentMinEncodedState captureEncoded() {
		return encode(AgentMinStateBuilder.capture());
	}

	public static AgentMinEncodedState encode(AgentMinState state) {
		AgentMinEncodedState encoded = new AgentMinEncodedState();
		encoded.source = state;
		if (state == null) {
			return encoded;
		}

		encoded.width = Math.max(0, state.level.width);
		encoded.height = Math.max(0, state.level.height);
		encoded.talentOffset = AgentMinEncodedState.HERO_BASE_FEATURES;
		encoded.negativeTalentOffset = encoded.talentOffset + TALENTS.length;
		encoded.heroVectorSize = AgentMinEncodedState.HERO_BASE_FEATURES + TALENTS.length * 2;
		encoded.pendingReward = AgentMinRewardTracker.pendingReward();
		encoded.episodeReward = AgentMinRewardTracker.episodeReward();

		encoded.levelTensor = encodeLevel(state);
		encoded.heroVector = encodeHero(state, encoded.heroVectorSize);
		encoded.inventoryMatrix = encodeInventory(state);
		encoded.actionMatrix = encodeActions(state);
		encoded.mobMatrix = encodeMobs(state);
		encoded.historyMatrix = AgentMinHistoryTracker.encode(state);
		encoded.actionSpace = AgentMinActionSpaceBuilder.build(state);
		return encoded;
	}

	private static float[][][] encodeLevel(AgentMinState state) {
		int width = Math.max(0, state.level.width);
		int height = Math.max(0, state.level.height);
		float[][][] tensor = new float[AgentMinEncodedState.LEVEL_CHANNELS][height][width];
		if (width == 0 || height == 0 || state.level.exploredMap == null || state.level.visibleMap == null) {
			return tensor;
		}

		for (int cell = 0; cell < Math.min(state.level.length, width * height); cell++) {
			int x = cell % width;
			int y = cell / width;
			int exploredCode = state.level.exploredMap[cell];
			int visibleCode = state.level.visibleMap[cell];
			int code = visibleCode != AgentMinState.CELL_UNKNOWN ? visibleCode : exploredCode;
			boolean known = code != AgentMinState.CELL_UNKNOWN;
			boolean visible = hasFlag(visibleCode, AgentMinState.FLAG_VISIBLE);

			tensor[LC_KNOWN][y][x] = known ? 1f : 0f;
			tensor[LC_VISIBLE][y][x] = visible ? 1f : 0f;
			tensor[LC_TERRAIN][y][x] = known ? terrain(code) / 255f : 0f;
			tensor[LC_PASSABLE][y][x] = hasFlag(code, AgentMinState.FLAG_PASSABLE) ? 1f : 0f;
			tensor[LC_AVOID][y][x] = hasFlag(code, AgentMinState.FLAG_AVOID) ? 1f : 0f;
			tensor[LC_SOLID][y][x] = hasFlag(code, AgentMinState.FLAG_SOLID) ? 1f : 0f;
			tensor[LC_LIQUID][y][x] = hasFlag(code, AgentMinState.FLAG_LIQUID) ? 1f : 0f;
			tensor[LC_PIT][y][x] = hasFlag(code, AgentMinState.FLAG_PIT) ? 1f : 0f;
			tensor[LC_ITEM][y][x] = hasFlag(code, AgentMinState.FLAG_ITEM) ? 1f : 0f;
			tensor[LC_TRAP][y][x] = hasFlag(code, AgentMinState.FLAG_TRAP) ? 1f : 0f;
			tensor[LC_PLANT][y][x] = hasFlag(code, AgentMinState.FLAG_PLANT) ? 1f : 0f;
			tensor[LC_MOB][y][x] = hasFlag(code, AgentMinState.FLAG_MOB) ? 1f : 0f;
			tensor[LC_STAIRS][y][x] = hasFlag(code, AgentMinState.FLAG_STAIRS) ? 1f : 0f;
			tensor[LC_VISITED][y][x] = hasFlag(code, AgentMinState.FLAG_VISITED) ? 1f : 0f;
			tensor[LC_MAPPED][y][x] = hasFlag(code, AgentMinState.FLAG_MAPPED) ? 1f : 0f;
			tensor[LC_EXPLORED_NOT_VISIBLE][y][x] = known && !visible ? 1f : 0f;
		}

		writePoint(tensor, LC_HERO, state.level.heroPos, width, height, 1f);
		writePoint(tensor, LC_EXIT, state.level.exit, width, height, 1f);
		return tensor;
	}

	private static float[] encodeHero(AgentMinState state, int size) {
		float[] vector = new float[size];
		AgentMinState.HeroState hero = state.hero;
		vector[0] = norm(state.depth, 30f);
		vector[1] = norm(state.branch, 10f);
		vector[2] = norm(hero.level, 30f);
		vector[3] = norm(hero.exp, 50f);
		vector[4] = norm(hero.str, 30f);
		vector[5] = norm(hero.ht, 250f);
		vector[6] = norm(hero.hp, 250f);
		vector[7] = clamp(hero.hpRatio);
		vector[8] = norm(hero.gold, 5000f);
		vector[9] = hero.starving ? 1f : 0f;
		vector[10] = norm(hero.hungerLevel, 450f);
		vector[11] = norm(hero.speed, 4f);
		vector[12] = norm(hero.attackDelay, 4f);
		vector[13] = norm(hero.attackSkill, 120f);
		vector[14] = norm(hero.defenseSkill, 120f);
		vector[15] = norm(hero.damageMin, 120f);
		vector[16] = norm(hero.damageMax, 160f);
		vector[17] = norm(hero.expectedDamage, 140f);
		vector[18] = norm(hero.armorMin, 80f);
		vector[19] = norm(hero.armorMax, 120f);
		vector[20] = norm(hero.expectedArmor, 100f);
		vector[21] = norm(state.combat.heroPowerScore, 500f);
		vector[22] = norm(state.combat.visibleEnemyThreatScore, 500f);
		vector[23] = normSigned(state.combat.fightScore, 500f);
		vector[24] = recommendation(state.combat.recommendation);
		vector[25] = norm(state.level.width, 64f);
		vector[26] = norm(state.level.height, 64f);
		vector[27] = norm(state.level.heroPos, Math.max(1, state.level.length));

		for (int i = 0; i < TALENTS.length; i++) {
			String key = TALENTS[i].name();
			Integer points = hero.talents.get(key);
			Integer negative = hero.negativeTalents.get(key);
			vector[AgentMinEncodedState.HERO_BASE_FEATURES + i] = points == null ? 0f : norm(points, 5f);
			vector[AgentMinEncodedState.HERO_BASE_FEATURES + TALENTS.length + i] = negative == null ? 0f : norm(negative, 5f);
		}
		return vector;
	}

	private static float[][] encodeInventory(AgentMinState state) {
		float[][] matrix = new float[AgentMinEncodedState.INVENTORY_ROWS][AgentMinEncodedState.INVENTORY_FEATURES];
		int row = 0;
		for (AgentMinState.ItemState item : state.inventory.equipped) {
			if (row >= matrix.length) return matrix;
			encodeItemRow(matrix[row++], item);
		}
		for (AgentMinState.ItemState item : state.inventory.backpack) {
			if (row >= matrix.length) return matrix;
			encodeItemRow(matrix[row++], item);
		}
		return matrix;
	}

	private static void encodeItemRow(float[] row, AgentMinState.ItemState item) {
		row[0] = 1f;
		row[1] = category(item.className);
		row[2] = item.equipped ? 1f : 0f;
		row[3] = slot(item.slot);
		row[4] = norm(item.quantity, 20f);
		row[5] = item.levelKnown ? normSigned(item.level, 20f) : 0f;
		row[6] = item.levelKnown ? normSigned(item.buffedLevel, 20f) : 0f;
		row[7] = item.levelKnown ? 1f : 0f;
		row[8] = item.cursedKnown ? 1f : 0f;
		row[9] = item.cursedKnown && item.cursed ? 1f : 0f;
		row[10] = item.usesTargeting ? 1f : 0f;
		row[11] = norm(item.image, 1024f);
		row[12] = hasAction(item, "EQUIP") ? 1f : 0f;
		row[13] = hasAction(item, "UNEQUIP") ? 1f : 0f;
		row[14] = hasAction(item, "DRINK") ? 1f : 0f;
		row[15] = hasAction(item, "READ") ? 1f : 0f;
		row[16] = hasAction(item, "EAT") ? 1f : 0f;
		row[17] = hasAction(item, "ZAP") ? 1f : 0f;
		row[18] = hasAction(item, "CAST") ? 1f : 0f;
		row[19] = hasAction(item, "THROW") ? 1f : 0f;
		row[20] = hasAction(item, "DROP") ? 1f : 0f;
		row[21] = hasAction(item, "AC_") ? 1f : 0f;
		row[22] = item.defaultAction == null ? 0f : actionKind(item.defaultAction);
		row[23] = item.actions == null ? 0f : norm(item.actions.size(), 12f);
		row[24] = item.slot != null && item.slot.startsWith("quickslot_") ? 1f : 0f;
		row[25] = item.slot != null && item.slot.contains("/") ? 1f : 0f;
	}

	private static float[][] encodeActions(AgentMinState state) {
		float[][] matrix = new float[AgentMinEncodedState.ACTION_ROWS][AgentMinEncodedState.ACTION_FEATURES];
		ArrayList<AgentMinState.ActionState> actions = state.inventory.availableActions;
		for (int i = 0; i < actions.size() && i < matrix.length; i++) {
			AgentMinState.ActionState action = actions.get(i);
			float[] row = matrix[i];
			row[0] = 1f;
			row[1] = category(action.itemClassName);
			row[2] = actionKind(action.action);
			row[3] = action.defaultAction ? 1f : 0f;
			row[4] = action.usesTargeting ? 1f : 0f;
			row[5] = action.quickSlot >= 0 ? 1f : 0f;
			row[6] = action.quickSlot >= 0 ? norm(action.quickSlot + 1, 6f) : 0f;
			row[7] = slot(action.slot);
			row[8] = action.action != null && action.action.equals("DROP") ? 1f : 0f;
			row[9] = action.action != null && action.action.equals("THROW") ? 1f : 0f;
			row[10] = action.action != null && action.action.equals("EQUIP") ? 1f : 0f;
			row[11] = action.action != null && action.action.equals("UNEQUIP") ? 1f : 0f;
			row[12] = action.action != null && action.action.equals("DRINK") ? 1f : 0f;
			row[13] = action.action != null && action.action.equals("READ") ? 1f : 0f;
			row[14] = action.action != null && action.action.equals("EAT") ? 1f : 0f;
			row[15] = action.action != null && action.action.equals("ZAP") ? 1f : 0f;
			row[16] = action.action != null && action.action.equals("CAST") ? 1f : 0f;
		}
		return matrix;
	}

	private static float[][] encodeMobs(AgentMinState state) {
		float[][] matrix = new float[AgentMinEncodedState.MOB_ROWS][AgentMinEncodedState.MOB_FEATURES];
		ArrayList<AgentMinState.MobCombatState> mobs = state.combat.visibleEnemies;
		for (int i = 0; i < mobs.size() && i < matrix.length; i++) {
			AgentMinState.MobCombatState mob = mobs.get(i);
			float[] row = matrix[i];
			row[0] = 1f;
			row[1] = norm(mob.pos, Math.max(1, state.level.length));
			row[2] = norm(mob.ht, 500f);
			row[3] = norm(mob.hp, 500f);
			row[4] = clamp(mob.hpRatio);
			row[5] = norm(mob.distanceToHero, 32f);
			row[6] = mob.visible ? 1f : 0f;
			row[7] = mob.alive ? 1f : 0f;
			row[8] = norm(mob.estimatedDamage, 120f);
			row[9] = norm(mob.estimatedArmor, 80f);
			row[10] = norm(mob.threatScore, 500f);
			row[11] = norm(mob.targetPriority, 50f);
			row[12] = alignment(mob.alignment);
			row[13] = norm(mob.buffs == null ? 0 : mob.buffs.size(), 20f);
			row[14] = classHash(mob.className);
		}
		return matrix;
	}

	private static boolean hasFlag(int code, int flag) {
		return (code & flag) != 0;
	}

	private static int terrain(int code) {
		return code & 0xFFFF;
	}

	private static void writePoint(float[][][] tensor, int channel, int cell, int width, int height, float value) {
		if (cell < 0 || width <= 0 || height <= 0 || cell >= width * height) {
			return;
		}
		tensor[channel][cell / width][cell % width] = value;
	}

	private static boolean hasAction(AgentMinState.ItemState item, String action) {
		if (item.actions == null || action == null) {
			return false;
		}
		for (String candidate : item.actions) {
			if (candidate != null && (candidate.equals(action) || candidate.contains(action))) {
				return true;
			}
		}
		return false;
	}

	private static float category(String className) {
		if (className == null) return 0f;
		String c = className.toLowerCase();
		if (c.contains(".weapon.")) return 1f / 12f;
		if (c.contains(".armor.")) return 2f / 12f;
		if (c.contains(".rings.")) return 3f / 12f;
		if (c.contains(".artifacts.")) return 4f / 12f;
		if (c.contains(".wands.")) return 5f / 12f;
		if (c.contains(".potions.")) return 6f / 12f;
		if (c.contains(".scrolls.")) return 7f / 12f;
		if (c.contains(".food.")) return 8f / 12f;
		if (c.contains(".spells.")) return 9f / 12f;
		if (c.contains(".bags.")) return 10f / 12f;
		if (c.contains(".keys.")) return 11f / 12f;
		return 1f;
	}

	private static float slot(String slot) {
		if (slot == null) return 0f;
		if (slot.equals("weapon")) return 1f / 10f;
		if (slot.equals("armor")) return 2f / 10f;
		if (slot.equals("artifact")) return 3f / 10f;
		if (slot.equals("misc")) return 4f / 10f;
		if (slot.equals("ring")) return 5f / 10f;
		if (slot.equals("second_weapon")) return 6f / 10f;
		if (slot.startsWith("backpack")) return 7f / 10f;
		if (slot.startsWith("quickslot_")) return 8f / 10f;
		return 1f;
	}

	private static float actionKind(String action) {
		if (action == null) return 0f;
		if (action.equals("DROP")) return 1f / 16f;
		if (action.equals("THROW")) return 2f / 16f;
		if (action.equals("EQUIP")) return 3f / 16f;
		if (action.equals("UNEQUIP")) return 4f / 16f;
		if (action.equals("DRINK")) return 5f / 16f;
		if (action.equals("READ")) return 6f / 16f;
		if (action.equals("EAT")) return 7f / 16f;
		if (action.equals("ZAP")) return 8f / 16f;
		if (action.equals("CAST")) return 9f / 16f;
		if (action.equals("OPEN")) return 10f / 16f;
		if (action.equals("INSPECT")) return 11f / 16f;
		return 1f;
	}

	private static float recommendation(String recommendation) {
		if (recommendation == null) return 0f;
		if (recommendation.equals("EXPLORE")) return 0.25f;
		if (recommendation.equals("ENGAGE")) return 0.5f;
		if (recommendation.equals("CAUTION")) return 0.75f;
		if (recommendation.equals("AVOID")) return 1f;
		return 0f;
	}

	private static float alignment(String alignment) {
		if (alignment == null) return 0f;
		if (alignment.equals("ENEMY")) return 1f;
		if (alignment.equals("ALLY")) return 0.5f;
		return 0.25f;
	}

	private static float classHash(String className) {
		if (className == null) return 0f;
		return ((className.hashCode() & 0x7fffffff) % 10000) / 10000f;
	}

	private static float norm(float value, float max) {
		if (max <= 0) return 0f;
		return clamp(value / max);
	}

	private static float normSigned(float value, float absMax) {
		if (absMax <= 0) return 0f;
		return Math.max(-1f, Math.min(1f, value / absMax));
	}

	private static float clamp(float value) {
		return Math.max(0f, Math.min(1f, value));
	}
}
