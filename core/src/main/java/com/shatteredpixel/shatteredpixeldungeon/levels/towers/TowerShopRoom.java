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
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ShopRoom;

import java.util.ArrayList;

public class TowerShopRoom extends ShopRoom {

	private final int towerFloor;

	public TowerShopRoom() {
		this(1);
	}

	public TowerShopRoom(int towerFloor) {
		this.towerFloor = Math.max(1, towerFloor);
	}

	@Override
	protected int generationDepth() {
		// Tower shops use the same stock tier as the final regular shop.
		return 21;
	}

	@Override
	protected int saleDepth() {
		return TowerLevel.shopPriceDepth(towerFloor);
	}

	@Override
	protected ArrayList<Item> generateItems() {
		ArrayList<Item> items = super.generateItems();
		items.add(TowerGenerationRules.guaranteedShopItem());
		items.add(TowerGenerationRules.guaranteedShopMetamorphosis());
		return items;
	}
}
