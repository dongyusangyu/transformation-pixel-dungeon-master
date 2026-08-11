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
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;

final class TowerGenerationRules {
	static final Class<? extends Item> GUARANTEED_FLOOR_ITEM = ScrollOfMetamorphosis.class;
	static final Class<? extends Item> GUARANTEED_SHOP_ITEM = ScrollOfExtraction.class;

	private TowerGenerationRules() {
	}

	static boolean isShopFloor(int floor) {
		return floor >= 1 && (floor - 1) % 5 == 0;
	}

	static int shopPriceDepth(int floor) {
		return 35 + 5 * ((Math.max(1, floor) - 1) / 5);
	}

	static boolean isForbiddenNaturalItem(Item item) {
		return item != null && isForbiddenNaturalItemClass(item.getClass());
	}

	static boolean isForbiddenNaturalItemClass(Class<? extends Item> itemClass) {
		return PotionOfStrength.class.isAssignableFrom(itemClass)
				|| ScrollOfUpgrade.class.isAssignableFrom(itemClass);
	}

	static Item guaranteedFloorItem() {
		return new ScrollOfMetamorphosis();
	}

	static Item guaranteedShopItem() {
		return new ScrollOfExtraction();
	}

}
