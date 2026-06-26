package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Alchemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blizzard;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Foliage;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.GooWarn;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Inferno;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Pheromone;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Regrowth;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SmokeScreen;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StenchGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.VaultFlameTraps;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfAwareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfHealth;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.ArrayList;
import java.util.Map;

public class AgentMinStateEncoder {

	private static final Talent[] TALENTS = Talent.values();

	private static final int LC_KNOWN = 0;
	private static final int LC_VISIBLE = 1;
	private static final int LC_TERRAIN = 2;
	private static final int LC_AVOID = 3;
	private static final int LC_SOLID = 4;
	private static final int LC_WATER_FLAMABLE = 5;
	private static final int LC_ITEM = 6;
	private static final int LC_TRAP = 7;
	private static final int LC_PLANT = 8;
	private static final int LC_MOB = 9;
	private static final int LC_STAIRS = 10;
	private static final int LC_VISITED = 11;
	private static final int LC_MAPPED = 12;
	private static final int LC_HERO = 13;
	private static final int LC_EXIT = 14;
	private static final int LC_EXPLORED_NOT_VISIBLE = 15;
	private static final int LC_VISIBLE_HEAP = 16;
	private static final int LC_DOOR = 17;
	private static final int LC_BLOB = 18;
	private static final int TALENT_ID_SCALE = 800;
	private static final int ITEM_ID_SCALE = 512;
	private static final int MODIFIER_ID_SCALE = 64;
	private static final int MOB_ID_SCALE = 384;

	public static AgentMinEncodedState captureEncoded() {
		return encode(AgentMinStateBuilder.capture());
	}

	public static AgentMinEncodedState encode(AgentMinState state) {
		AgentMinEncodedState encoded = new AgentMinEncodedState();
		encoded.source = state;
		if (state == null) {
			return encoded;
		}

		encoded.width = AgentMinEncodedState.LOCAL_MAP_SIZE;
		encoded.height = AgentMinEncodedState.LOCAL_MAP_SIZE;
		encoded.talentOffset = AgentMinEncodedState.HERO_BASE_FEATURES;
		encoded.negativeTalentOffset = encoded.talentOffset + TALENTS.length;
		encoded.heroVectorSize = AgentMinEncodedState.HERO_BASE_FEATURES + TALENTS.length * 2
				+ AgentMinEncodedState.TALENT_ICON_SLOTS * AgentMinEncodedState.TALENT_ICON_FEATURES;
		encoded.pendingReward = AgentMinRewardTracker.pendingReward();
		encoded.episodeReward = AgentMinRewardTracker.episodeReward();

		encoded.levelTensor = encodeLevel(state);
		encoded.exploredGlobalMatrix = encodeExploredGlobal(state);
		encoded.heroVector = encodeHero(state, encoded.heroVectorSize);
		encoded.inventoryMatrix = encodeInventory(state);
		encoded.inventorySummaryVector = encodeInventorySummary(state);
		encoded.optionVector = encodeOptions(state, encoded.inventorySummaryVector);
		encoded.actionMatrix = encodeActions(state);
		encoded.mobMatrix = encodeMobs(state);
		encoded.historyMatrix = AgentMinHistoryTracker.encode(state);
		encoded.agentVisitedMatrix = AgentMinVisitTracker.encodeMatrix(state);
		encoded.actionSpace = AgentMinActionSpaceBuilder.build(state);
		encoded.forcedSkill = encoded.actionSpace == null ? -1 : encoded.actionSpace.forcedSkill;
		return encoded;
	}

	private static float[][][] encodeLevel(AgentMinState state) {
		int localSize = AgentMinEncodedState.LOCAL_MAP_SIZE;
		int radius = localSize / 2;
		float[][][] tensor = new float[AgentMinEncodedState.LEVEL_CHANNELS][localSize][localSize];
		int mapWidth = Math.max(0, state.level.width);
		int mapHeight = Math.max(0, state.level.height);
		if (mapWidth == 0 || mapHeight == 0 || state.level.exploredMap == null || state.level.visibleMap == null) {
			return tensor;
		}

		int heroX = state.level.heroPos % mapWidth;
		int heroY = state.level.heroPos / mapWidth;
		for (int localY = 0; localY < localSize; localY++) {
			for (int localX = 0; localX < localSize; localX++) {
				int mapX = heroX + localX - radius;
				int mapY = heroY + localY - radius;
				if (mapX < 0 || mapY < 0 || mapX >= mapWidth || mapY >= mapHeight) {
					continue;
				}
				int cell = mapY * mapWidth + mapX;
				if (cell < 0 || cell >= state.level.length) {
					continue;
				}
				int exploredCode = state.level.exploredMap[cell];
				int visibleCode = state.level.visibleMap[cell];
				int code = visibleCode != AgentMinState.CELL_UNKNOWN ? visibleCode : exploredCode;
				boolean known = code != AgentMinState.CELL_UNKNOWN;
				boolean visible = hasFlag(visibleCode, AgentMinState.FLAG_VISIBLE);

				tensor[LC_KNOWN][localY][localX] = known ? 1f : 0f;
				tensor[LC_VISIBLE][localY][localX] = visible ? 1f : 0f;
				tensor[LC_TERRAIN][localY][localX] = terrainValue(code, known);
				tensor[LC_AVOID][localY][localX] = hasFlag(code, AgentMinState.FLAG_AVOID) ? 1f : 0f;
				tensor[LC_SOLID][localY][localX] = hasFlag(code, AgentMinState.FLAG_SOLID)
						&& !AgentMinStateBuilder.isPassableDoorTerrain(terrain(code)) ? 1f : 0f;
				tensor[LC_WATER_FLAMABLE][localY][localX] = waterFlamableValue(code, known);
				tensor[LC_ITEM][localY][localX] = itemValue(code);
				tensor[LC_TRAP][localY][localX] = hasFlag(code, AgentMinState.FLAG_TRAP) ? 1f : 0f;
				tensor[LC_PLANT][localY][localX] = hasFlag(code, AgentMinState.FLAG_PLANT) ? 1f : 0f;
				tensor[LC_STAIRS][localY][localX] = hasFlag(code, AgentMinState.FLAG_STAIRS) ? 1f : 0f;
				tensor[LC_VISITED][localY][localX] = hasFlag(code, AgentMinState.FLAG_VISITED) ? 1f : 0f;
				tensor[LC_MAPPED][localY][localX] = hasFlag(code, AgentMinState.FLAG_MAPPED) ? 1f : 0f;
				tensor[LC_EXPLORED_NOT_VISIBLE][localY][localX] = known && !visible ? 1f : 0f;
				tensor[LC_DOOR][localY][localX] = doorValue(code);
				tensor[LC_BLOB][localY][localX] = visible ? blobValue(cell) : 0f;
			}
		}

		tensor[LC_HERO][radius][radius] = 1f;
		writeLocalPoint(tensor, LC_EXIT, state.level.exit, mapWidth, heroX, heroY, radius, 1f);
		for (AgentMinState.CellEntityState item : state.level.visibleItems) {
			if (item.visible) {
				writeLocalPoint(tensor, LC_VISIBLE_HEAP, item.pos, mapWidth, heroX, heroY, radius, 0.6f + Math.min(0.4f, item.count * 0.08f));
			}
		}
		for (AgentMinState.MobState mob : state.level.visibleMobs) {
			if (mob.visible && mob.alive) {
				writeLocalPoint(tensor, LC_MOB, mob.pos, mapWidth, heroX, heroY, radius, mobMapThreatValue(mob));
			}
		}
		return tensor;
	}

	private static float mobMapThreatValue(AgentMinState.MobState mob) {
		float attackSpeed = 1f / Math.max(0.001f, mob.attackDelay);
		float moveSpeed = Math.max(0f, mob.speed);
		return Math.max(0, mob.hp) * attackSpeed * moveSpeed;
	}

	private static float[][] encodeExploredGlobal(AgentMinState state) {
		int size = AgentMinEncodedState.GLOBAL_MAP_SIZE;
		int radius = size / 2;
		float[][] matrix = new float[size][size];
		int mapWidth = Math.max(0, state.level.width);
		int mapHeight = Math.max(0, state.level.height);
		if (mapWidth == 0 || mapHeight == 0 || state.level.exploredMap == null) {
			return matrix;
		}
		int heroX = state.level.heroPos % mapWidth;
		int heroY = state.level.heroPos / mapWidth;
		for (int localY = 0; localY < size; localY++) {
			for (int localX = 0; localX < size; localX++) {
				int mapX = heroX + localX - radius;
				int mapY = heroY + localY - radius;
				if (mapX < 0 || mapY < 0 || mapX >= mapWidth || mapY >= mapHeight) {
					continue;
				}
				int cell = mapY * mapWidth + mapX;
				if (cell >= 0 && cell < state.level.length && state.level.exploredMap[cell] != AgentMinState.CELL_UNKNOWN) {
					matrix[localY][localX] = 1f;
				}
			}
		}
		return matrix;
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
		vector[28] = hero.hasIronKey ? 1f : 0f;
		vector[29] = hero.hasGoldenKey ? 1f : 0f;
		vector[30] = hero.hasCrystalKey ? 1f : 0f;
		vector[31] = hero.hasWornKey ? 1f : 0f;

		for (int i = 0; i < TALENTS.length; i++) {
			String key = TALENTS[i].name();
			Integer points = hero.talents.get(key);
			Integer negative = hero.negativeTalents.get(key);
			vector[AgentMinEncodedState.HERO_BASE_FEATURES + i] = points == null ? 0f : norm(points, 5f);
			vector[AgentMinEncodedState.HERO_BASE_FEATURES + TALENTS.length + i] = negative == null ? 0f : norm(negative, 5f);
		}
		encodeTalentIconSlots(vector, AgentMinEncodedState.HERO_BASE_FEATURES + TALENTS.length * 2, hero);
		return vector;
	}

	private static void encodeTalentIconSlots(float[] vector, int offset, AgentMinState.HeroState hero) {
		if (hero.talentSlots == null) {
			return;
		}
		for (AgentMinState.TalentSlotState slot : hero.talentSlots) {
			if (slot == null || slot.slot < 0 || slot.placeholder || !slot.unlocked) {
				continue;
			}
			int tierBase = talentTierBase(slot.tier);
			if (tierBase < 0) {
				continue;
			}
			int index = tierBase + slot.slot;
			if (index < 0 || index >= AgentMinEncodedState.TALENT_ICON_SLOTS) {
				continue;
			}
			int write = offset + index * AgentMinEncodedState.TALENT_ICON_FEATURES;
			if (write + AgentMinEncodedState.TALENT_ICON_FEATURES > vector.length) {
				continue;
			}
			int talentId = slot.talentIdentityId >= 0 && slot.talentIdentityId < TALENT_ID_SCALE ? slot.talentIdentityId : 0;
			vector[write] = norm(talentId, TALENT_ID_SCALE);
			vector[write + 1] = norm(slot.points, 5f);
			vector[write + 2] = norm(slot.maxPoints, 5f);
			vector[write + 3] = slot.unlocked ? 1f : 0f;
		}
	}

	private static int talentTierBase(int tier) {
		if (tier == 1) return 0;
		if (tier == 2) return 6;
		if (tier == 3) return 12;
		if (tier == 4) return 18;
		return -1;
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

	private static float[] encodeInventorySummary(AgentMinState state) {
		float[] out = new float[AgentMinEncodedState.INVENTORY_SUMMARY_FEATURES];
		ArrayList<AgentMinState.ItemState> all = new ArrayList<>();
		all.addAll(state.inventory.equipped);
		all.addAll(state.inventory.backpack);

		int totalItems = all.size();
		int equippedCount = state.inventory.equipped.size();
		int backpackCount = state.inventory.backpack.size();
		int healingCount = 0;
		int foodCount = 0;
		int wandCount = 0;
		int throwableCount = 0;
		int scrollCount = 0;
		int potionCount = 0;
		int keyCount = 0;
		int unidentifiedConsumables = 0;
		int cursedEquipped = 0;
		int targetedCount = 0;
		int quickReadyCount = 0;
		int dropCapableCount = 0;
		int weaponCount = 0;
		int armorCount = 0;
		float healingBudget = 0f;
		float foodBudget = 0f;
		float combatResourceBudget = 0f;

		for (AgentMinState.ItemState item : all) {
			if (item == null) {
				continue;
			}
			if (isHealingPotion(item)) {
				healingCount += Math.max(1, item.quantity);
				healingBudget += Math.max(1f, item.quantity);
			}
			if (isFood(item) || isEatLike(item)) {
				foodCount += Math.max(1, item.quantity);
				foodBudget += Math.max(1f, item.quantity);
			}
			if (isWandLike(item)) {
				wandCount++;
				combatResourceBudget += 1.5f;
			}
			if (isThrowableWeapon(item)) {
				throwableCount += Math.max(1, item.quantity);
				combatResourceBudget += Math.max(1f, item.quantity) * 0.25f;
			}
			if (item.className != null && item.className.contains(".scrolls.")) {
				scrollCount += Math.max(1, item.quantity);
			}
			if (item.className != null && item.className.contains(".potions.")) {
				potionCount += Math.max(1, item.quantity);
			}
			if (item.className != null && item.className.contains(".keys.")) {
				keyCount += Math.max(1, item.quantity);
			}
			if (!item.levelKnown && (hasAction(item, "DRINK") || hasAction(item, "READ") || hasAction(item, "EAT"))) {
				unidentifiedConsumables += Math.max(1, item.quantity);
			}
			if (item.equipped && item.cursedKnown && item.cursed) {
				cursedEquipped++;
			}
			if (item.usesTargeting) {
				targetedCount++;
			}
			if (item.slot != null && item.slot.startsWith("quickslot_")) {
				quickReadyCount++;
			}
			if (hasAction(item, "DROP")) {
				dropCapableCount++;
			}
			if (item.className != null && item.className.contains(".weapon.")) {
				weaponCount++;
			}
			if (item.className != null && item.className.contains(".armor.")) {
				armorCount++;
			}
		}

		out[0] = norm(totalItems, 80f);
		out[1] = norm(equippedCount, 8f);
		out[2] = norm(backpackCount, 64f);
		out[3] = clamp(backpackCount / 24f);
		out[4] = norm(healingCount, 20f);
		out[5] = norm(foodCount, 20f);
		out[6] = norm(wandCount, 12f);
		out[7] = norm(throwableCount, 40f);
		out[8] = norm(scrollCount, 30f);
		out[9] = norm(potionCount, 30f);
		out[10] = norm(keyCount, 12f);
		out[11] = norm(unidentifiedConsumables, 20f);
		out[12] = norm(cursedEquipped, 6f);
		out[13] = norm(targetedCount, 16f);
		out[14] = norm(quickReadyCount, 6f);
		out[15] = norm(dropCapableCount, 80f);
		out[16] = norm(weaponCount, 20f);
		out[17] = norm(armorCount, 12f);
		out[18] = clamp(healingBudget / 20f);
		out[19] = clamp(foodBudget / 20f);
		out[20] = clamp(combatResourceBudget / 20f);
		out[21] = state.hero.hasIronKey ? 1f : 0f;
		out[22] = state.hero.hasGoldenKey ? 1f : 0f;
		out[23] = (state.hero.hasCrystalKey || state.hero.hasWornKey) ? 1f : 0f;
		return out;
	}

	private static float[] encodeOptions(AgentMinState state, float[] summary) {
		float[] out = new float[AgentMinEncodedState.OPTION_FEATURES];
		boolean hasEnemy = !state.combat.visibleEnemies.isEmpty();
		boolean lowHp = state.hero.hpRatio <= 0.45f;
		boolean hungry = state.hero.starving || state.hero.hungerLevel <= 150f;
		int frontier = AgentMinExplorationTracker.frontierDistance(state, state.level.heroPos);
		boolean onItem = false;
		boolean unlockableDoorNearby = false;
		boolean unlockableChestNearby = false;

		for (AgentMinState.CellEntityState item : state.level.visibleItems) {
			if (item.pos == state.level.heroPos) {
				onItem = true;
				break;
			}
		}

		for (int cell : neighbors4(state.level.heroPos, state.level.width, state.level.height)) {
			if (cell < 0) continue;
			int code = codeAt(state, cell);
			if (code == AgentMinState.CELL_UNKNOWN) continue;
			int terr = terrain(code);
			if ((terr == Terrain.LOCKED_DOOR && state.hero.hasIronKey)
					|| (terr == Terrain.CRYSTAL_DOOR && state.hero.hasCrystalKey)
					|| (terr == Terrain.HERO_LKD_DR && state.hero.hasWornKey)) {
				unlockableDoorNearby = true;
			}
			if (hasFlag(code, AgentMinState.FLAG_LOCKED_CHEST) && (state.hero.hasGoldenKey || state.hero.hasCrystalKey)) {
				unlockableChestNearby = true;
			}
		}

		out[0] = !hasEnemy && frontier >= 0 ? clamp(0.55f + Math.max(0f, 0.35f - frontier * 0.03f)) : 0.1f;
		out[1] = hasEnemy && state.combat.fightScore >= 0 ? clamp(0.4f + state.hero.hpRatio * 0.5f) : 0.05f;
		out[2] = hasEnemy && (state.combat.fightScore < 0 || lowHp) ? clamp(0.5f + (1f - state.hero.hpRatio) * 0.4f) : 0.05f;
		out[3] = ((lowHp && summary[4] > 0f) || (summary[20] > 0.15f && hasEnemy)) ? 0.9f : 0.1f;
		out[4] = hungry && summary[5] > 0f ? 0.95f : 0.05f;
		out[5] = onItem ? 0.9f : (state.level.visibleItems.isEmpty() ? 0.05f : 0.2f);
		out[6] = unlockableDoorNearby || unlockableChestNearby ? 0.92f : 0.05f;
		out[7] = summary[3] > 0.85f ? 0.8f : summary[3] > 0.65f ? 0.35f : 0.05f;
		return out;
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
		row[26] = classHash(item.className);
		row[27] = item.stackable ? 1f : 0f;
		row[28] = item.unique ? 1f : 0f;
		row[29] = item.bones ? 1f : 0f;
		row[30] = item.icon < 0 ? 0f : norm(item.icon, 1024f);
		row[31] = isMeleeWeapon(item) ? 1f : 0f;
		row[32] = isThrowableWeapon(item) ? 1f : 0f;
		row[33] = hasPath(item, ".items.armor.") ? 1f : 0f;
		row[34] = hasPath(item, ".items.rings.") ? 1f : 0f;
		row[35] = hasPath(item, ".items.artifacts.") ? 1f : 0f;
		row[36] = isWandLike(item) ? 1f : 0f;
		row[37] = hasPath(item, ".items.potions.") ? 1f : 0f;
		row[38] = hasPath(item, ".items.scrolls.") || hasPath(item, ".items.ScrollOfSublimation") ? 1f : 0f;
		row[39] = isFood(item) || isEatLike(item) ? 1f : 0f;
		row[40] = hasPath(item, ".items.spells.") ? 1f : 0f;
		row[41] = hasPath(item, ".items.bags.") ? 1f : 0f;
		row[42] = hasPath(item, ".items.keys.") ? 1f : 0f;
		row[43] = hasPath(item, ".items.trinkets.") ? 1f : 0f;
		row[44] = isAlchemyMaterial(item) ? 1f : 0f;
		row[45] = isTalentResource(item) ? 1f : 0f;
		row[46] = defaultActionIndex(item);
		row[47] = norm(item.quantity * Math.max(1, item.actions == null ? 0 : item.actions.size()), 60f);
		row[48] = equipmentKind(item.equipmentKind);
		row[49] = norm(item.strengthRequirement, 25f);
		row[50] = normSigned(item.strengthMargin, 15f);
		row[51] = norm(item.equipmentScore, 80f);
		row[52] = item.strengthRequirement <= 0 || item.strengthMargin >= 0 ? 1f : 0f;
		row[53] = item.strengthRequirement > 0 && item.strengthMargin < 0 ? norm(-item.strengthMargin, 10f) : 0f;
		row[54] = item.equipmentScore > 0f ? 1f : 0f;
		row[55] = item.cursedKnown ? (item.cursed ? -1f : 1f) : 0f;
		row[56] = norm(item.itemRow + 1, AgentMinEncodedState.INVENTORY_ROWS);
		row[57] = item.inContainer ? 1f : 0f;
		row[58] = norm(item.containerDepth, 4f);
		row[59] = norm(item.containerItemIndex + 1, 24f);
		row[60] = norm(item.containerSize, 24f);
		row[61] = norm(item.containerCapacity, 24f);
		row[62] = item.bag ? 1f : 0f;
		row[63] = item.bagCapacity <= 0 ? 0f : clamp(item.bagSize / (float)item.bagCapacity);
		row[64] = normSigned(item.equipmentScoreDiff, 40f);
		row[65] = item.equipmentUpgradeCandidate ? 1f : 0f;
		row[66] = item.equipmentSidegrade ? 1f : 0f;
		row[67] = norm(item.equipmentCooldown, AgentMinRewardConfig.EQUIP_ACTION_COOLDOWN_TURNS);
		row[68] = item.equipmentScoreDiff >= AgentMinRewardConfig.EQUIP_MIN_SCORE_IMPROVEMENT ? 1f : 0f;
		row[69] = item.equipmentScoreDiff <= 0f && item.equipmentScore > 0f ? 1f : 0f;
		row[70] = item.equipped && item.strengthRequirement > 0 && item.strengthMargin < 0 ? 1f : 0f;
		row[71] = !item.equipped && item.strengthRequirement > 0 && item.strengthMargin >= 0 ? 1f : 0f;
		row[72] = norm(item.itemIdentityId, ITEM_ID_SCALE);
		row[73] = norm(item.modifierIdentityId, MODIFIER_ID_SCALE);
		row[74] = norm(item.modifierKind, 8f);
		row[75] = item.modifierIdentityId > 0 ? 1f : 0f;
	}

	private static float[][] encodeActions(AgentMinState state) {
		float[][] matrix = new float[AgentMinEncodedState.ACTION_ROWS][AgentMinEncodedState.ACTION_FEATURES];
		ArrayList<AgentMinState.ActionState> actions = state.inventory.availableActions;
		float[] optionVector = encodeOptions(state, encodeInventorySummary(state));
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
			row[17] = action.itemName == null ? 0f : clamp(action.itemName.length() / 24f);
			row[18] = action.quickSlot >= 0 && action.quickSlot < 6 ? 1f : 0f;
			row[19] = action.itemClassName != null && action.itemClassName.contains(".keys.") ? 1f : 0f;
			row[20] = action.itemClassName != null && action.itemClassName.contains(".bags.") ? 1f : 0f;
			row[21] = action.usesTargeting ? 1f : 0f;
			row[22] = norm(action.actionIndex + 1, 16f);
			row[23] = norm(action.actionCount, 16f);
			fillActionOptionAffinities(row, action, optionVector);
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
			row[15] = norm(mob.expectedDamageTaken, 120f);
			row[16] = norm(mob.expectedTurnsToKill, 20f);
			row[17] = clamp(mob.killChance);
			row[18] = clamp(mob.meleeDanger);
			row[19] = clamp(mob.rangedValue);
			row[20] = mob.distanceToHero <= 1 ? 1f : 0f;
			row[21] = mob.distanceToHero <= 3 ? 1f : 0f;
			row[22] = normSigned(state.combat.fightScore, 500f);
			row[23] = state.hero.hpRatio < 0.5f ? 1f : 0f;
			row[24] = norm(state.hero.expectedDamage, 140f);
			row[25] = norm(state.hero.expectedArmor, 100f);
			row[26] = norm(state.depth, 30f);
			row[27] = mob.hp <= state.hero.damageMax ? 1f : 0f;
			row[28] = 0f;
		}
		return matrix;
	}

	private static boolean hasFlag(int code, int flag) {
		return (code & flag) != 0;
	}

	private static int terrain(int code) {
		return code & 0xFFFF;
	}

	private static float terrainValue(int code, boolean known) {
		if (!known) {
			return 0f;
		}
		int terrain = terrain(code);
		if (terrain == Terrain.CHASM) {
			return 0f;
		}
		return hasFlag(code, AgentMinState.FLAG_PASSABLE) || AgentMinStateBuilder.isPassableDoorTerrain(terrain) ? 1f : -1f;
	}

	private static float waterFlamableValue(int code, boolean known) {
		if (!known) {
			return 0f;
		}
		if (hasFlag(code, AgentMinState.FLAG_LIQUID)) {
			return -1f;
		}
		int terrain = terrain(code);
		if (terrain >= 0 && terrain < Terrain.flags.length && (Terrain.flags[terrain] & Terrain.FLAMABLE) != 0) {
			return 1f;
		}
		return 0f;
	}

	private static float itemValue(int code) {
		if (hasFlag(code, AgentMinState.FLAG_LOCKED_CHEST)) {
			return -1f;
		}
		return hasFlag(code, AgentMinState.FLAG_ITEM) ? 1f : 0f;
	}

	private static float doorValue(int code) {
		if (!hasFlag(code, AgentMinState.FLAG_DOOR)) {
			return 0f;
		}
		return AgentMinStateBuilder.doorChannelValue(terrain(code));
	}

	private static float blobValue(int cell) {
		if (cell < 0 || stateLevelMissing()) {
			return 0f;
		}
		float total = 0f;
		for (Map.Entry<Class<? extends Blob>, Blob> entry : com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.blobs.entrySet()) {
			Blob blob = entry.getValue();
			if (blob == null || blob.cur == null || cell >= blob.cur.length || blob.cur[cell] <= 0) {
				continue;
			}
			total += classifyBlob(entry.getKey());
		}
		return Math.max(-1f, Math.min(1f, total));
	}

	private static boolean stateLevelMissing() {
		return com.shatteredpixel.shatteredpixeldungeon.Dungeon.level == null
				|| com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.blobs == null;
	}

	private static float classifyBlob(Class<? extends Blob> blobClass) {
		if (blobClass == null) {
			return 0f;
		}
		if (blobClass == Fire.class
				|| blobClass == Inferno.class
				|| blobClass == SacrificialFire.class
				|| blobClass == ToxicGas.class
				|| blobClass == CorrosiveGas.class
				|| blobClass == ConfusionGas.class
				|| blobClass == ParalyticGas.class
				|| blobClass == Electricity.class
				|| blobClass == StormCloud.class
				|| blobClass == Freezing.class
				|| blobClass == Blizzard.class
				|| blobClass == VaultFlameTraps.class
				|| blobClass == Web.class
				|| blobClass == StenchGas.class) {
			return -1f;
		}
		if (blobClass == WaterOfHealth.class) {
			return 1f;
		}
		if (blobClass == WaterOfAwareness.class) {
			return 0.35f;
		}
		if (blobClass == SmokeScreen.class || blobClass == Foliage.class) {
			return 0.15f;
		}
		if (blobClass == Regrowth.class) {
			return 0.2f;
		}
		if (blobClass == Pheromone.class) {
			return 0.05f;
		}
		if (blobClass == GooWarn.class) {
			return -0.35f;
		}
		if (blobClass == Alchemy.class) {
			return 0.05f;
		}
		return 0f;
	}

	private static void writeLocalPoint(float[][][] tensor, int channel, int cell, int mapWidth, int heroX, int heroY, int radius, float value) {
		if (cell < 0 || mapWidth <= 0) {
			return;
		}
		int mapX = cell % mapWidth;
		int mapY = cell / mapWidth;
		int localX = mapX - heroX + radius;
		int localY = mapY - heroY + radius;
		if (localY >= 0 && localY < tensor[channel].length && localX >= 0 && localX < tensor[channel][localY].length) {
			tensor[channel][localY][localX] = value;
		}
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

	private static void fillActionOptionAffinities(float[] row, AgentMinState.ActionState action, float[] optionVector) {
		row[24] = optionVector[0] * optionAffinity(action, "EXPLORE");
		row[25] = optionVector[1] * optionAffinity(action, "ENGAGE");
		row[26] = optionVector[2] * optionAffinity(action, "RETREAT");
		row[27] = optionVector[3] * optionAffinity(action, "USE_RESOURCE");
		row[28] = optionVector[4] * optionAffinity(action, "CONSUME");
		row[29] = optionVector[5] * optionAffinity(action, "PICKUP");
		row[30] = optionVector[6] * optionAffinity(action, "UNLOCK");
		row[31] = optionVector[7] * optionAffinity(action, "DROP");
	}

	private static float optionAffinity(AgentMinState.ActionState action, String option) {
		if (action == null || option == null) {
			return 0f;
		}
		String verb = action.action == null ? "" : action.action;
		String cls = action.itemClassName == null ? "" : action.itemClassName.toLowerCase();
		if (option.equals("EXPLORE")) {
			return verb.equals("THROW") || verb.equals("ZAP") ? 0.1f : 0.2f;
		}
		if (option.equals("ENGAGE")) {
			return (verb.equals("THROW") || verb.equals("ZAP") || cls.contains(".weapon.")) ? 1f : 0.1f;
		}
		if (option.equals("RETREAT")) {
			return (verb.equals("DRINK") || verb.equals("CAST") || verb.equals("READ")) ? 0.7f : 0.05f;
		}
		if (option.equals("USE_RESOURCE")) {
			return (verb.equals("DRINK") || verb.equals("READ") || verb.equals("ZAP") || verb.equals("CAST")) ? 1f : 0.05f;
		}
		if (option.equals("CONSUME")) {
			return (verb.equals("EAT") || verb.equals("DRINK")) ? 1f : 0.05f;
		}
		if (option.equals("PICKUP")) {
			return 0.05f;
		}
		if (option.equals("UNLOCK")) {
			return cls.contains(".keys.") ? 0.7f : 0.05f;
		}
		if (option.equals("DROP")) {
			return verb.equals("DROP") ? 1f : 0.05f;
		}
		return 0f;
	}

	private static int codeAt(AgentMinState state, int cell) {
		if (state.level.visibleMap != null && cell >= 0 && cell < state.level.visibleMap.length
				&& state.level.visibleMap[cell] != AgentMinState.CELL_UNKNOWN) {
			return state.level.visibleMap[cell];
		}
		if (state.level.exploredMap != null && cell >= 0 && cell < state.level.exploredMap.length) {
			return state.level.exploredMap[cell];
		}
		return AgentMinState.CELL_UNKNOWN;
	}

	private static int[] neighbors4(int cell, int width, int height) {
		int[] out = new int[4];
		int x = cell % width;
		int y = cell / width;
		out[0] = x > 0 ? cell - 1 : -1;
		out[1] = x + 1 < width ? cell + 1 : -1;
		out[2] = y > 0 ? cell - width : -1;
		out[3] = y + 1 < height ? cell + width : -1;
		return out;
	}

	private static boolean isHealingPotion(AgentMinState.ItemState item) {
		return item != null && item.className != null && item.className.endsWith(".PotionOfHealing");
	}

	private static boolean isThrowableWeapon(AgentMinState.ItemState item) {
		return item != null && item.className != null && item.className.contains(".items.weapon.missiles.");
	}

	private static boolean isFood(AgentMinState.ItemState item) {
		return item != null && item.className != null && item.className.contains(".items.food.");
	}

	private static boolean isEatLike(AgentMinState.ItemState item) {
		if (item == null || item.className == null) {
			return false;
		}
		String c = item.className.toLowerCase();
		return c.contains("berry") || c.contains("ration") || c.contains("pasty") || c.contains("stew");
	}

	private static boolean isWandLike(AgentMinState.ItemState item) {
		if (item == null || item.className == null) {
			return false;
		}
		return item.className.contains(".items.wands.") || item.className.endsWith(".MagesStaff");
	}

	private static boolean isMeleeWeapon(AgentMinState.ItemState item) {
		return hasPath(item, ".items.weapon.melee.") || hasPath(item, ".items.weapon.SpiritBow");
	}

	private static boolean isAlchemyMaterial(AgentMinState.ItemState item) {
		return hasPath(item, ".items.potions.")
				|| hasPath(item, ".items.scrolls.")
				|| hasPath(item, ".items.stones.")
				|| hasPath(item, ".items.plants.")
				|| hasPath(item, ".items.bombs.")
				|| hasPath(item, ".items.EnergyCrystal")
				|| hasPath(item, ".items.ArcaneResin")
				|| hasPath(item, ".items.LiquidMetal");
	}

	private static boolean isTalentResource(AgentMinState.ItemState item) {
		return hasPath(item, ".items.scrolls.exotic.ScrollOfMetamorphosis")
				|| hasPath(item, ".items.spells.TransformSpell")
				|| hasPath(item, ".items.ScrollOfSublimation");
	}

	private static boolean hasPath(AgentMinState.ItemState item, String part) {
		return item != null && item.className != null && item.className.contains(part);
	}

	private static float defaultActionIndex(AgentMinState.ItemState item) {
		if (item == null || item.actions == null || item.defaultAction == null) {
			return 0f;
		}
		for (int i = 0; i < item.actions.size(); i++) {
			if (item.defaultAction.equals(item.actions.get(i))) {
				return norm(i + 1, 16f);
			}
		}
		return 0f;
	}

	private static float equipmentKind(String kind) {
		if (kind == null) return 0f;
		if (kind.equals("weapon")) return 1f / 6f;
		if (kind.equals("armor")) return 2f / 6f;
		if (kind.equals("ring")) return 3f / 6f;
		if (kind.equals("artifact")) return 4f / 6f;
		if (kind.equals("misc")) return 5f / 6f;
		return 0f;
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
		if (action.equals("DROP")) return 1f / 24f;
		if (action.equals("THROW")) return 2f / 24f;
		if (action.equals("EQUIP")) return 3f / 24f;
		if (action.equals("UNEQUIP")) return 4f / 24f;
		if (action.equals("DRINK")) return 5f / 24f;
		if (action.equals("READ")) return 6f / 24f;
		if (action.equals("EAT") || action.equals("eat")) return 7f / 24f;
		if (action.equals("ZAP")) return 8f / 24f;
		if (action.equals("CAST")) return 9f / 24f;
		if (action.equals("OPEN")) return 10f / 24f;
		if (action.equals("INSPECT")) return 11f / 24f;
		if (action.equals("PLANT")) return 12f / 24f;
		if (action.equals("PLANT_IN_BODY")) return 13f / 24f;
		if (action.equals("LIGHTTHROW")) return 14f / 24f;
		if (action.equals("LIGHT")) return 15f / 24f;
		if (action.equals("USE")) return 16f / 24f;
		if (action.equals("BLESS")) return 17f / 24f;
		if (action.equals("SNACK")) return 18f / 24f;
		if (action.equals("STEALTH")) return 19f / 24f;
		if (action.equals("ROOT")) return 20f / 24f;
		if (action.equals("ACTIVATE")) return 21f / 24f;
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
