package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.ArrayList;

public final class TowerPotionRules {

	public static final int HEALING_LIMIT = 13;

	private TowerPotionRules() {
	}

	public static int countHealing(Bag bag) {
		int count = 0;
		for (Item item : bag) {
			if (item.getClass() == PotionOfHealing.class) {
				count += item.quantity();
			}
		}
		return count;
	}

	public static int remainingCapacity(Bag bag) {
		return Math.max(0, HEALING_LIMIT - countHealing(bag));
	}

	public static boolean canCollect(Item item, Bag container) {
		if (item == null || container == null || item.getClass() != PotionOfHealing.class
				|| Dungeon.branch != TowerLevel.BRANCH || Dungeon.depth < 1
				|| Dungeon.hero == null || Dungeon.hero.belongings == null
				|| container.isLoading()) {
			return true;
		}
		Hero hero = Dungeon.hero;
		Bag backpack = hero.belongings.backpack;
		if (backpack == null || (container != backpack && !backpack.contains(container))
				|| backpack.contains(item)) {
			return true;
		}
		return item.quantity() <= remainingCapacity(backpack);
	}

	public static int pickupAmount(Item item, Bag container) {
		if (item == null) return 0;
		if (canCollect(item, container)) return item.quantity();
		if (item.getClass() != PotionOfHealing.class || Dungeon.hero == null
				|| Dungeon.hero.belongings == null) return 0;
		return Math.min(item.quantity(), remainingCapacity(Dungeon.hero.belongings.backpack));
	}

	public static int moveExcessToGround(Hero hero) {
		if (hero == null || hero.belongings == null || hero.belongings.backpack == null
				|| Dungeon.level == null || Dungeon.branch != TowerLevel.BRANCH || Dungeon.depth < 1) {
			return 0;
		}
		int extra = Math.max(0, countHealing(hero.belongings.backpack) - HEALING_LIMIT);
		ArrayList<Item> excess = new ArrayList<>();
		int moved = takeExcess(hero.belongings.backpack, extra, excess);
		for (Item item : excess) Dungeon.level.drop(item, hero.pos);
		if (moved > 0) GLog.w(Messages.get(TowerPotionRules.class, "excess", moved));
		return moved;
	}

	static int takeExcess(Bag bag, int extra, ArrayList<Item> dropped) {
		int moved = 0;
		for (Item item : new ArrayList<>(bag.items)) {
			if (moved >= extra) break;
			if (item instanceof Bag) {
				moved += takeExcess((Bag) item, extra - moved, dropped);
			} else if (item.getClass() == PotionOfHealing.class) {
				int quantity = Math.min(item.quantity(), extra - moved);
				Item excess;
				if (quantity == item.quantity()) {
					bag.items.remove(item);
					if (Dungeon.quickslot != null) Dungeon.quickslot.clearItem(item);
					excess = item;
				} else {
					excess = item.split(quantity);
					if (excess == null) continue;
				}
				dropped.add(excess);
				moved += quantity;
			}
		}
		if (moved > 0) Item.updateQuickslot();
		return moved;
	}
}
