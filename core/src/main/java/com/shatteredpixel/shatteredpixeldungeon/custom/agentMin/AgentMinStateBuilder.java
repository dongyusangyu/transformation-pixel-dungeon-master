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
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;

import java.util.ArrayList;

public class AgentMinStateBuilder {

	private static long observationCounter;

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

		for (Buff buff : hero.buffs()) {
			state.hero.buffs.add(buffState(buff));
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

		for (Item item : belongings.backpack.items) {
			addItemTree(state, hero, item, "backpack");
		}

		if (Dungeon.quickslot != null) {
			for (int i = 0; i < QuickSlot.SIZE; i++) {
				Item item = Dungeon.quickslot.getItem(i);
				AgentMinState.QuickSlotState quickSlot = new AgentMinState.QuickSlotState();
				quickSlot.slot = i;
				quickSlot.placeholder = Dungeon.quickslot.isPlaceholder(i);
				if (item != null) {
					quickSlot.item = itemState(hero, item, "quickslot_" + i);
				}
				state.inventory.quickSlots.add(quickSlot);
			}
		}
	}

	private static void captureCombat(AgentMinState state, Hero hero, Level level) {
		float heroDamage = Math.max(1, state.hero.expectedDamage);
		float heroArmor = Math.max(0, state.hero.expectedArmor);
		float hpFactor = Math.max(0.1f, state.hero.hpRatio);
		float levelFactor = 1f + Dungeon.depth * 0.045f;
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
		int code = level.map[cell] & 0xFFFF;
		if (level.mapped[cell]) code |= AgentMinState.FLAG_MAPPED;
		if (level.visited[cell]) code |= AgentMinState.FLAG_VISITED;
		if (currentFov && visible(level, cell)) code |= AgentMinState.FLAG_VISIBLE;
		if (level.passable[cell]) code |= AgentMinState.FLAG_PASSABLE;
		if (level.avoid[cell]) code |= AgentMinState.FLAG_AVOID;
		if (level.solid[cell]) code |= AgentMinState.FLAG_SOLID;
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
		if (level.map[cell] == Terrain.ENTRANCE || level.map[cell] == Terrain.EXIT || level.map[cell] == Terrain.UNLOCKED_EXIT) code |= AgentMinState.FLAG_STAIRS;
		if (doorChannelValue(level.map[cell]) != 0) code |= AgentMinState.FLAG_DOOR;
		return code;
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
		float depthWeight = 1f + Dungeon.depth * 0.06f;
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
		AgentMinState.ItemState itemState = itemState(hero, item, slot);
		state.inventory.equipped.add(itemState);
		addActions(state, hero, item, slot);
	}

	private static void addItemTree(AgentMinState state, Hero hero, Item item, String slot) {
		AgentMinState.ItemState itemState = itemState(hero, item, slot);
		state.inventory.backpack.add(itemState);
		addActions(state, hero, item, slot);
		if (item instanceof Bag) {
			for (Item child : ((Bag)item).items) {
				addItemTree(state, hero, child, slot + "/" + item.getClass().getSimpleName());
			}
		}
	}

	private static AgentMinState.ItemState itemState(Hero hero, Item item, String slot) {
		AgentMinState.ItemState state = new AgentMinState.ItemState();
		state.slot = slot;
		state.className = item.getClass().getName();
		state.name = item.name();
		state.image = item.image();
		state.quantity = item.quantity();
		state.level = item.level();
		state.buffedLevel = item.buffedLvl();
		state.levelKnown = item.levelKnown;
		state.cursed = item.cursed;
		state.cursedKnown = item.cursedKnown;
		state.equipped = item.isEquipped(hero);
		state.usesTargeting = item.usesTargeting;
		state.defaultAction = item.defaultAction();
		state.actions.addAll(item.actions(hero));
		return state;
	}

	private static void addActions(AgentMinState state, Hero hero, Item item, String slot) {
		ArrayList<String> actions = item.actions(hero);
		for (String action : actions) {
			AgentMinState.ActionState actionState = new AgentMinState.ActionState();
			actionState.itemClassName = item.getClass().getName();
			actionState.itemName = item.name();
			actionState.slot = slot;
			actionState.action = action;
			actionState.actionName = item.actionName(action, hero);
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
		int depthDamage = Math.max(1, Dungeon.depth / 2);
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
