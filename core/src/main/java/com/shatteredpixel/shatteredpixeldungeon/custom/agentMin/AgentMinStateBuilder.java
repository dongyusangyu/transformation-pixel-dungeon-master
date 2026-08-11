package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;

import java.util.ArrayList;
import java.util.Map;

public class AgentMinStateBuilder {

	private static long observationCounter;
	private static final int ITEM_EMBEDDING_TABLE_SIZE = 512;

	public static AgentMinState capture() {
		return capture(Dungeon.hero, Dungeon.level);
	}

	public static AgentMinEncodedState captureEncoded() {
		return AgentMinStateEncoder.encode(capture());
	}

	public static AgentMinState capture(Hero hero, Level level) {
		AgentMinState state = new AgentMinState();
		state.observationId = ++observationCounter;
		state.depth = Dungeon.depth;
		state.branch = Dungeon.branch;

		if (hero == null || level == null) {
			state.combat.recommendation = "NO_GAME";
			return state;
		}

		captureLevel(state, hero, level);
		captureHero(state, hero, level);
		captureInventory(state, hero);
		captureCombat(state, hero, level);
		return state;
	}

	private static void captureLevel(AgentMinState state, Hero hero, Level level) {
		state.level.width = level.width();
		state.level.height = level.height();
		state.level.length = level.length();
		state.level.heroPos = hero.pos;
		state.level.entrance = level.entrance();
		state.level.exit = level.exit();
		state.level.exploredMap = new int[level.length()];
		state.level.visibleMap = new int[level.length()];

		for (int cell = 0; cell < level.length(); cell++) {
			if (known(level, cell)) {
				state.level.exploredMap[cell] = encodeCell(level, cell, false);
			}
			if (visible(level, cell)) {
				state.level.visibleMap[cell] = encodeCell(level, cell, true);
			}
		}

		for (Heap heap : level.heaps.valueList()) {
			if (heap.seen && visible(level, heap.pos)) {
				state.level.visibleItems.add(heapState(heap));
			}
		}

		for (Plant plant : level.plants.valueList()) {
			if (visible(level, plant.pos)) {
				state.level.visiblePlants.add(cellEntity(plant.pos, plant.getClass().getName(), Messages.get(plant, "name"), plant.image, 1, true));
			}
		}

		for (Trap trap : level.traps.valueList()) {
			if (trap.visible && visible(level, trap.pos)) {
				state.level.visibleTraps.add(cellEntity(trap.pos, trap.getClass().getName(), Messages.get(trap, "name"), trap.color, 1, true));
			}
		}

		for (Mob mob : level.mobs.toArray(new Mob[0])) {
			if (visible(level, mob.pos)) {
				state.level.visibleMobs.add(mobState(mob, hero, level));
			}
		}
	}

	private static void captureHero(AgentMinState state, Hero hero, Level level) {
		state.hero.heroClass = hero.heroClass.name();
		state.hero.subClass = hero.subClass.name();
		state.hero.pos = hero.pos;
		state.hero.level = hero.lvl;
		state.hero.exp = hero.exp;
		state.hero.str = hero.STR();
		state.hero.ht = hero.HT;
		state.hero.hp = hero.HP;
		state.hero.hpRatio = ratio(hero.HP, hero.HT);
		state.hero.gold = Dungeon.gold;

		Hunger hunger = hero.buff(Hunger.class);
		state.hero.starving = hunger != null && hunger.isStarving();
		state.hero.hungerLevel = hunger == null ? 0 : hunger.level;
		state.hero.speed = hero.speed();
		state.hero.attackDelay = hero.attackDelay();
		Mob referenceTarget = firstVisibleEnemy(level);
		state.hero.attackSkill = referenceTarget == null ? estimatedBaseAttackSkill(hero) : hero.attackSkill(referenceTarget);
		state.hero.defenseSkill = referenceTarget == null ? estimatedBaseDefenseSkill(hero) : hero.defenseSkill(referenceTarget);

		DamageRange damage = heroDamageRange(hero);
		state.hero.damageMin = damage.min;
		state.hero.damageMax = damage.max;
		state.hero.expectedDamage = average(damage.min, damage.max);

		DamageRange armor = heroArmorRange(hero);
		state.hero.armorMin = armor.min;
		state.hero.armorMax = armor.max;
		state.hero.expectedArmor = average(armor.min, armor.max);
		state.hero.hasIronKey = Notes.keyCount(new IronKey(Dungeon.depth)) > 0;
		state.hero.hasGoldenKey = Notes.keyCount(new GoldenKey(Dungeon.depth)) > 0;
		state.hero.hasCrystalKey = Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0;
		state.hero.hasWornKey = Notes.keyCount(new WornKey(Dungeon.depth)) > 0;

		for (Talent talent : Talent.values()) {
			int points = hero.pointsInTalent(talent);
			if (points != 0) {
				state.hero.talents.put(talent.name(), points);
			}
			int negative = hero.pointsNegative(talent);
			if (negative != 0) {
				state.hero.negativeTalents.put(talent.name(), negative);
			}
		}
		captureTalentSlots(state, hero);

		for (Buff buff : hero.buffs()) {
			state.hero.buffs.add(buffState(buff));
		}
	}

	private static void captureTalentSlots(AgentMinState state, Hero hero) {
		int[] tierLimits = new int[]{6, 6, 6, 6};
		if (hero.talents == null) {
			return;
		}
		for (int tierIndex = 0; tierIndex < tierLimits.length; tierIndex++) {
			if (tierIndex >= hero.talents.size()) {
				continue;
			}
			int slot = 0;
			for (Map.Entry<Talent, Integer> entry : hero.talents.get(tierIndex).entrySet()) {
				if (slot >= tierLimits[tierIndex]) {
					break;
				}
				Talent talent = entry.getKey();
				if (talent == null) {
					slot++;
					continue;
				}
				AgentMinState.TalentSlotState talentSlot = new AgentMinState.TalentSlotState();
				talentSlot.talentName = talent.name();
				talentSlot.talentIdentityId = Math.max(0, talent.icon());
				talentSlot.tier = tierIndex + 1;
				talentSlot.slot = slot;
				talentSlot.icon = talent.icon();
				talentSlot.points = entry.getValue() == null ? 0 : entry.getValue();
				talentSlot.maxPoints = talent.maxPoints();
				talentSlot.unlocked = hero.lvl >= Talent.tierLevelThresholds[Math.min(talentSlot.tier, Talent.tierLevelThresholds.length - 1)];
				talentSlot.placeholder = talentSlot.icon == 530 || talentSlot.maxPoints <= 0 || talentSlot.talentName.startsWith("BOSS_TALENT_SLOT");
				state.hero.talentSlots.add(talentSlot);
				slot++;
			}
		}
	}

	private static void captureInventory(AgentMinState state, Hero hero) {
		Belongings belongings = hero.belongings;
		addEquipped(state, hero, "weapon", belongings.weapon());
		addEquipped(state, hero, "armor", belongings.armor());
		addEquipped(state, hero, "artifact", belongings.artifact());
		addEquipped(state, hero, "misc", belongings.misc());
		addEquipped(state, hero, "ring", belongings.ring());
		addEquipped(state, hero, "second_weapon", belongings.secondWep());

		for (int i = 0; i < belongings.backpack.items.size(); i++) {
			addItemTree(state, hero, belongings.backpack.items.get(i), "backpack", null, 0, i);
		}

		if (Dungeon.quickslot != null) {
			for (int i = 0; i < QuickSlot.SIZE; i++) {
				Item item = Dungeon.quickslot.getItem(i);
				AgentMinState.QuickSlotState quickSlot = new AgentMinState.QuickSlotState();
				quickSlot.slot = i;
				quickSlot.placeholder = Dungeon.quickslot.isPlaceholder(i);
				if (item != null) {
					quickSlot.item = itemState(hero, item, "quickslot_" + i, null, 0, i, -1);
				}
				state.inventory.quickSlots.add(quickSlot);
			}
		}
	}

	private static void captureCombat(AgentMinState state, Hero hero, Level level) {
		float heroDamage = Math.max(1, state.hero.expectedDamage);
		float heroArmor = Math.max(0, state.hero.expectedArmor);
		float hpFactor = Math.max(0.1f, state.hero.hpRatio);
		float levelFactor = 1f + Dungeon.scalingDepth() * 0.045f;
		state.combat.heroPowerScore = (heroDamage * 2.25f + state.hero.attackSkill * 0.35f + heroArmor * 1.35f + state.hero.defenseSkill * 0.2f) * hpFactor;
		state.combat.floorPressureScore = levelFactor;

		for (Mob mob : level.mobs.toArray(new Mob[0])) {
			if (!visible(level, mob.pos) || mob.alignment != Char.Alignment.ENEMY) {
				continue;
			}
			AgentMinState.MobCombatState enemy = mobCombatState(mob, hero, level);
			state.combat.visibleEnemies.add(enemy);
			state.combat.visibleEnemyThreatScore += enemy.threatScore;
		}

		state.combat.visibleEnemyThreatScore *= levelFactor;
		state.combat.fightScore = state.combat.heroPowerScore - state.combat.visibleEnemyThreatScore;
		if (state.combat.visibleEnemies.isEmpty()) {
			state.combat.recommendation = "EXPLORE";
		} else if (state.combat.fightScore >= state.combat.heroPowerScore * 0.25f) {
			state.combat.recommendation = "ENGAGE";
		} else if (state.combat.fightScore >= 0) {
			state.combat.recommendation = "CAUTION";
		} else {
			state.combat.recommendation = "AVOID";
		}
	}

	private static int encodeCell(Level level, int cell, boolean currentFov) {
		int terrain = level.map[cell];
		int code = terrain & 0xFFFF;
		boolean passableDoor = isPassableDoorTerrain(terrain);
		if (level.mapped[cell]) code |= AgentMinState.FLAG_MAPPED;
		if (level.visited[cell]) code |= AgentMinState.FLAG_VISITED;
		if (currentFov && visible(level, cell)) code |= AgentMinState.FLAG_VISIBLE;
		if (level.passable[cell] || passableDoor) code |= AgentMinState.FLAG_PASSABLE;
		if (level.avoid[cell]) code |= AgentMinState.FLAG_AVOID;
		if (level.solid[cell] && !passableDoor) code |= AgentMinState.FLAG_SOLID;
		if (level.water[cell]) code |= AgentMinState.FLAG_LIQUID;
		if (level.pit[cell]) code |= AgentMinState.FLAG_PIT;
		Heap heap = level.heaps.get(cell);
		if (heap != null && heap.seen) {
			code |= AgentMinState.FLAG_ITEM;
			if (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) {
				code |= AgentMinState.FLAG_LOCKED_CHEST;
			}
		}
		Trap trap = level.traps.get(cell);
		if (trap != null && trap.visible) code |= AgentMinState.FLAG_TRAP;
		if (level.plants.get(cell) != null && visible(level, cell)) code |= AgentMinState.FLAG_PLANT;
		if (level.findMob(cell) != null && visible(level, cell)) code |= AgentMinState.FLAG_MOB;
		if (terrain == Terrain.ENTRANCE || terrain == Terrain.EXIT || terrain == Terrain.UNLOCKED_EXIT) code |= AgentMinState.FLAG_STAIRS;
		if (doorChannelValue(terrain) != 0) code |= AgentMinState.FLAG_DOOR;
		return code;
	}

	static boolean isPassableDoorTerrain(int terrain) {
		return doorChannelValue(terrain) > 0;
	}

	static int doorChannelValue(int terrain) {
		if (terrain == Terrain.DOOR || terrain == Terrain.OPEN_DOOR) {
			return 1;
		}
		if (terrain == Terrain.LOCKED_DOOR || terrain == Terrain.HERO_LKD_DR || terrain == Terrain.CRYSTAL_DOOR) {
			return -1;
		}
		return 0;
	}

	private static boolean known(Level level, int cell) {
		return level.mapped[cell] || level.visited[cell] || visible(level, cell);
	}

	private static boolean visible(Level level, int cell) {
		return cell >= 0 && cell < level.length() && level.heroFOV != null && level.heroFOV[cell];
	}

	private static AgentMinState.MobState mobState(Mob mob, Hero hero, Level level) {
		AgentMinState.MobState state = new AgentMinState.MobState();
		fillMobState(state, mob, hero, level);
		return state;
	}

	private static AgentMinState.MobCombatState mobCombatState(Mob mob, Hero hero, Level level) {
		AgentMinState.MobCombatState state = new AgentMinState.MobCombatState();
		fillMobState(state, mob, hero, level);
		state.estimatedDamage = estimatedMobDamage(mob);
		state.estimatedArmor = 0;
		DamageRange heroDamage = heroDamageRange(hero);
		DamageRange heroArmor = heroArmorRange(hero);
		state.expectedDamageTaken = Math.max(0f, state.estimatedDamage - heroArmor.min);
		state.expectedTurnsToKill = Math.max(1f, mob.HP / Math.max(1f, heroDamage.max));
		state.killChance = Math.max(0f, Math.min(1f, heroDamage.max / (float)Math.max(1, mob.HP)));
		state.meleeDanger = state.distanceToHero <= 1 ? 1f : state.distanceToHero <= 2 ? 0.55f : 0.15f;
		float hpWeight = Math.max(1, mob.HP) / 5f;
		float depthWeight = 1f + Dungeon.scalingDepth() * 0.06f;
		float distanceWeight = state.distanceToHero <= 1 ? 1.3f : state.distanceToHero <= 3 ? 1.1f : 0.8f;
		state.threatScore = (state.estimatedDamage * 1.8f + state.ht * 0.18f + hpWeight) * depthWeight * distanceWeight;
		state.rangedValue = state.distanceToHero > 1 ? Math.max(0f, Math.min(1f, state.threatScore / 80f + (1f - state.hpRatio) * 0.25f)) : 0.05f;
		state.targetPriority = (1f - state.hpRatio) * 1.4f
				+ state.killChance * 1.1f
				+ Math.min(1.2f, state.threatScore / 80f)
				+ distanceWeight * 0.25f;
		return state;
	}

	private static void fillMobState(AgentMinState.MobState state, Mob mob, Hero hero, Level level) {
		state.className = mob.getClass().getName();
		state.name = mob.name();
		state.alignment = mob.alignment.name();
		state.pos = mob.pos;
		state.ht = mob.HT;
		state.hp = mob.HP;
		state.hpRatio = ratio(mob.HP, mob.HT);
		state.distanceToHero = level.distance(hero.pos, mob.pos);
		state.attackDelay = mob.attackDelay();
		state.speed = mob.speed();
		state.visible = visible(level, mob.pos);
		state.alive = mob.isAlive();
		for (Buff buff : mob.buffs()) {
			state.buffs.add(buffState(buff));
		}
	}

	private static AgentMinState.BuffState buffState(Buff buff) {
		AgentMinState.BuffState state = new AgentMinState.BuffState();
		state.className = buff.getClass().getName();
		state.name = buff.name();
		state.icon = buff.icon();
		state.iconText = buff.iconTextDisplay();
		state.fadePercent = buff.iconFadePercent();
		return state;
	}

	private static AgentMinState.CellEntityState heapState(Heap heap) {
		Item top = heap.peek();
		String className = top == null ? Heap.class.getName() : top.getClass().getName();
		String name = top == null ? heap.type.name() : top.name();
		int image = top == null ? 0 : top.image();
		return cellEntity(heap.pos, className, name, image, heap.size(), true);
	}

	private static AgentMinState.CellEntityState cellEntity(int pos, String className, String name, int image, int count, boolean visible) {
		AgentMinState.CellEntityState state = new AgentMinState.CellEntityState();
		state.pos = pos;
		state.className = className;
		state.name = name;
		state.image = image;
		state.count = count;
		state.visible = visible;
		return state;
	}

	private static void addEquipped(AgentMinState state, Hero hero, String slot, Item item) {
		if (item == null) {
			return;
		}
		int row = state.inventory.equipped.size();
		AgentMinState.ItemState itemState = itemState(hero, item, slot, null, 0, row, row);
		state.inventory.equipped.add(itemState);
		addActions(state, hero, item, slot, row);
	}

	private static void addItemTree(AgentMinState state, Hero hero, Item item, String slot, Bag container, int depth, int containerIndex) {
		int row = state.inventory.equipped.size() + state.inventory.backpack.size();
		AgentMinState.ItemState itemState = itemState(hero, item, slot, container, depth, containerIndex, row);
		state.inventory.backpack.add(itemState);
		addActions(state, hero, item, slot, row);
		if (item instanceof Bag) {
			Bag bag = (Bag)item;
			for (int i = 0; i < bag.items.size(); i++) {
				addItemTree(state, hero, bag.items.get(i), slot + "/" + item.getClass().getSimpleName(), bag, depth + 1, i);
			}
		}
	}

	private static AgentMinState.ItemState itemState(Hero hero, Item item, String slot, Bag container, int depth, int containerIndex, int row) {
		AgentMinState.ItemState state = new AgentMinState.ItemState();
		state.itemRow = row;
		state.slot = slot;
		state.className = item.getClass().getName();
		state.name = item.name();
		state.itemIdentityId = itemIdentityId(item);
		state.modifierIdentityId = modifierIdentityId(item);
		state.modifierKind = modifierKind(item);
		state.modifierClassName = modifierClassName(item);
		state.image = item.image();
		state.quantity = item.quantity();
		state.level = item.level();
		state.buffedLevel = item.buffedLvl();
		state.levelKnown = item.levelKnown;
		state.cursed = item.cursed;
		state.cursedKnown = item.cursedKnown;
		state.equipped = item.isEquipped(hero);
		state.usesTargeting = item.usesTargeting;
		state.stackable = item.stackable;
		state.unique = item.unique;
		state.bones = item.bones;
		state.icon = item.icon;
		state.equipmentKind = equipmentKind(item);
		state.strengthRequirement = strengthRequirement(item);
		state.strengthMargin = state.strengthRequirement <= 0 ? 0 : hero.STR() - state.strengthRequirement;
		state.equipmentScore = equipmentScore(item);
		float currentScore = currentEquipmentScore(hero, item, state.equipmentKind);
		state.equipmentScoreDiff = state.equipped ? 0f : state.equipmentScore - currentScore;
		state.equipmentCooldown = AgentMinRewardTracker.equipmentActionCooldownRemaining(state);
		state.equipmentUpgradeCandidate = !state.equipped
				&& state.equipmentScore > 0f
				&& state.equipmentScoreDiff >= AgentMinRewardConfig.EQUIP_MIN_SCORE_IMPROVEMENT
				&& state.strengthMargin >= -1
				&& !(state.cursedKnown && state.cursed)
				&& state.equipmentCooldown <= 0f;
		state.equipmentSidegrade = !state.equipped
				&& state.equipmentScore > 0f
				&& Math.abs(state.equipmentScoreDiff) < AgentMinRewardConfig.EQUIP_MIN_SCORE_IMPROVEMENT;
		state.inContainer = container != null;
		state.containerClassName = container == null ? null : container.getClass().getName();
		state.containerName = container == null ? null : container.name();
		state.containerDepth = depth;
		state.containerItemIndex = containerIndex;
		state.containerSize = container == null ? 0 : container.items.size();
		state.containerCapacity = container == null ? 0 : container.capacity();
		if (item instanceof Bag) {
			Bag bag = (Bag)item;
			state.bag = true;
			state.bagSize = bag.items.size();
			state.bagCapacity = bag.capacity();
		}
		state.defaultAction = item.defaultAction();
		state.actions.addAll(item.actions(hero));
		return state;
	}

	private static int itemIdentityId(Item item) {
		if (item == null) {
			return 0;
		}
		int image = Math.max(0, item.image());
		int classBucket = positiveHash(item.getClass().getName()) % 257;
		return 1 + positiveHash(image + ":" + classBucket) % (ITEM_EMBEDDING_TABLE_SIZE - 1);
	}

	private static int modifierIdentityId(Item item) {
		String modifierClassName = modifierClassName(item);
		if (modifierClassName != null && modifierClassName.length() > 0) {
			return 1 + positiveHash(modifierClassName) % 63;
		}
		return 0;
	}

	private static String modifierClassName(Item item) {
		if (item instanceof Weapon) {
			Weapon weapon = (Weapon)item;
			if (weapon.enchantment != null && (weapon.cursedKnown || !weapon.enchantment.curse())) {
				return weapon.enchantment.getClass().getName();
			}
		}
		if (item instanceof Armor) {
			Armor armor = (Armor)item;
			if (armor.glyph != null && (armor.cursedKnown || !armor.glyph.curse())) {
				return armor.glyph.getClass().getName();
			}
		}
		if (item != null && item.cursedKnown && item.cursed) {
			return item.getClass().getName() + "#cursed";
		}
		return "";
	}

	private static int modifierKind(Item item) {
		if (item instanceof Weapon) {
			Weapon weapon = (Weapon)item;
			if (weapon.enchantment != null && (weapon.cursedKnown || !weapon.enchantment.curse())) {
				return weapon.enchantment.curse() ? 2 : 1;
			}
			return item.cursedKnown && item.cursed ? 5 : 0;
		}
		if (item instanceof Armor) {
			Armor armor = (Armor)item;
			if (armor.glyph != null && (armor.cursedKnown || !armor.glyph.curse())) {
				return armor.glyph.curse() ? 4 : 3;
			}
			return item.cursedKnown && item.cursed ? 5 : 0;
		}
		return item != null && item.cursedKnown && item.cursed ? 5 : 0;
	}

	private static int positiveHash(String value) {
		return value == null ? 0 : value.hashCode() & 0x7fffffff;
	}

	private static String equipmentKind(Item item) {
		if (item instanceof Armor) {
			return "armor";
		}
		if (item instanceof KindOfWeapon) {
			return "weapon";
		}
		if (item instanceof Ring) {
			return "ring";
		}
		if (item instanceof Artifact) {
			return "artifact";
		}
		return item != null && item.isEquipped(Dungeon.hero) ? "misc" : "";
	}

	private static int strengthRequirement(Item item) {
		if (item instanceof Weapon) {
			return ((Weapon)item).STRReq();
		}
		if (item instanceof Armor) {
			return ((Armor)item).STRReq();
		}
		return 0;
	}

	private static float equipmentScore(Item item) {
		if (item instanceof KindOfWeapon) {
			KindOfWeapon weapon = (KindOfWeapon)item;
			return (weapon.min() + weapon.max()) * 0.5f + Math.max(0, weapon.buffedLvl()) * 1.5f;
		}
		if (item instanceof Armor) {
			Armor armor = (Armor)item;
			return (armor.DRMin() + armor.DRMax()) * 0.65f + armor.tier + Math.max(0, armor.buffedLvl()) * 1.5f;
		}
		if (item instanceof Ring || item instanceof Artifact) {
			return 4f + Math.max(0, item.buffedLvl()) * 2f;
		}
		return item != null && item.isEquipped(Dungeon.hero) ? 2f + Math.max(0, item.buffedLvl()) : 0f;
	}

	private static float currentEquipmentScore(Hero hero, Item item, String kind) {
		if (hero == null || item == null || kind == null || kind.length() == 0 || item.isEquipped(hero)) {
			return item == null ? 0f : equipmentScore(item);
		}
		Item equipped = null;
		if ("weapon".equals(kind)) {
			equipped = hero.belongings.weapon();
		} else if ("armor".equals(kind)) {
			equipped = hero.belongings.armor();
		} else if ("ring".equals(kind)) {
			equipped = hero.belongings.ring() != null ? hero.belongings.ring() : hero.belongings.misc();
		} else if ("artifact".equals(kind)) {
			equipped = hero.belongings.artifact() != null ? hero.belongings.artifact() : hero.belongings.misc();
		} else if ("misc".equals(kind)) {
			equipped = hero.belongings.misc();
		}
		return equipped == null ? 0f : equipmentScore(equipped);
	}

	private static void addActions(AgentMinState state, Hero hero, Item item, String slot, int row) {
		ArrayList<String> actions = item.actions(hero);
		for (int i = 0; i < actions.size(); i++) {
			String action = actions.get(i);
			AgentMinState.ActionState actionState = new AgentMinState.ActionState();
			actionState.itemRow = row;
			actionState.itemClassName = item.getClass().getName();
			actionState.itemName = item.name();
			actionState.slot = slot;
			actionState.action = action;
			actionState.actionName = item.actionName(action, hero);
			actionState.actionIndex = i;
			actionState.actionCount = actions.size();
			actionState.defaultAction = action.equals(item.defaultAction());
			actionState.usesTargeting = item.usesTargeting;
			actionState.quickSlot = Dungeon.quickslot == null ? -1 : Dungeon.quickslot.getSlot(item);
			state.inventory.availableActions.add(actionState);
		}
	}

	private static DamageRange heroDamageRange(Hero hero) {
		KindOfWeapon weapon = hero.belongings.attackingWeapon();
		if (weapon != null) {
			int min = weapon.min();
			int max = weapon.max();
			return new DamageRange(Math.max(1, min), Math.max(1, max));
		}
		return new DamageRange(Math.max(1, hero.lvl / 2), Math.max(2, hero.lvl + Math.max(0, hero.STR() - 10)));
	}

	private static DamageRange heroArmorRange(Hero hero) {
		int min = 0;
		int max = 0;
		Armor armor = hero.belongings.armor();
		if (armor != null) {
			min += Math.max(0, armor.DRMin());
			max += Math.max(0, armor.DRMax());
		}
		KindOfWeapon weapon = hero.belongings.weapon();
		if (weapon != null) {
			max += Math.max(0, weapon.defenseFactor(hero));
		}
		return new DamageRange(min, max);
	}

	private static Mob firstVisibleEnemy(Level level) {
		for (Mob mob : level.mobs.toArray(new Mob[0])) {
			if (visible(level, mob.pos) && mob.alignment == Char.Alignment.ENEMY) {
				return mob;
			}
		}
		return null;
	}

	private static int estimatedBaseAttackSkill(Hero hero) {
		return Math.max(1, 8 + hero.lvl * 2 + Math.max(0, hero.STR() - 10));
	}

	private static int estimatedBaseDefenseSkill(Hero hero) {
		return Math.max(1, 4 + hero.lvl + Math.max(0, hero.STR() - 10) / 2);
	}

	private static int estimatedMobDamage(Mob mob) {
		int depthDamage = Math.max(1, Dungeon.scalingDepth() / 2);
		int hpDamage = Math.max(1, mob.HT / 12);
		return Math.max(1, depthDamage + hpDamage);
	}

	private static float average(int min, int max) {
		return (min + max) / 2f;
	}

	private static float ratio(int value, int max) {
		return max <= 0 ? 0 : Math.max(0, Math.min(1f, value / (float)max));
	}

	private static class DamageRange {
		final int min;
		final int max;

		DamageRange(int min, int max) {
			this.min = min;
			this.max = Math.max(min, max);
		}
	}
}
