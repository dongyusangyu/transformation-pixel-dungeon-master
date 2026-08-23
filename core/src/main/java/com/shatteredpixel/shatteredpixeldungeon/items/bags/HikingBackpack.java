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

package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class HikingBackpack extends Bag {

	{
		image = ItemSpriteSheet.BACKPACK;
	}

	@Override
	public boolean canHold(Item item) {
		return !(item instanceof Bag) && super.canHold(item);
	}

	@Override
	public boolean isFallbackStorage() {
		return true;
	}

	@Override
	protected boolean autoGrabOnCollect() {
		return false;
	}

	@Override
	public int capacity() {
		return 19;
	}

	@Override
	public int value() {
		return 40;
	}
}
