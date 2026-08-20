/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

final class TowerGenerationRules {
	static final Class<? extends Item> GUARANTEED_SHOP_ITEM = ScrollOfExtraction.class;
	static final Class<? extends Item> METAMORPHOSIS_ITEM = ScrollOfMetamorphosis.class;

	private TowerGenerationRules() {
	}

	static boolean isShopFloor(int floor) {
		return floor >= 1 && (floor - 1) % 5 == 0;
	}

	static boolean shouldGenerateNaturalFood(boolean bossFloor) {
		return !bossFloor;
	}

	static boolean shouldGenerateLevelFeeling(int towerFloor, boolean bossFloor) {
		return towerFloor > 1 && !bossFloor;
	}

	static int secretRoomCount(Level.Feeling feeling) {
		return feeling == Level.Feeling.SECRETS ? 1 : 0;
	}

	static int shopPriceDepth(int floor) {
		return 35 + 5 * ((Math.max(1, floor) - 1) / 5);
	}

	static int driedRosePetalProgressDepth(int towerFloor) {
		return 25 + Math.max(1, towerFloor);
	}

	static boolean driedRosePetalGenerationAllowed(int droppedPetals, boolean roseMaxLevel) {
		return droppedPetals < 11 || !roseMaxLevel;
	}

	static boolean driedRosePetalGenerationEnabled(int branch) {
		return branch == 0 || branch == TowerLevel.BRANCH;
	}

	static boolean isForbiddenNaturalItem(Item item) {
		return item != null && isForbiddenNaturalItemClass(item.getClass());
	}

	static boolean isForbiddenNaturalItemClass(Class<? extends Item> itemClass) {
		return PotionOfStrength.class.isAssignableFrom(itemClass)
				|| ScrollOfUpgrade.class.isAssignableFrom(itemClass)
				|| ScrollOfMetamorphosis.class.isAssignableFrom(itemClass);
	}

	static Item guaranteedShopItem() {
		return new ScrollOfExtraction();
	}

	static Item guaranteedShopMetamorphosis() {
		return new ScrollOfMetamorphosis();
	}

	static Item prepareFloorSpawn(Item item, int towerFloor) {
		if (isForbiddenNaturalItem(item)) {
			return null;
		}
		if (item instanceof Key) {
			((Key) item).depth = towerFloor;
		}
		return item;
	}

}
