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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DigestionTrap extends Trap {

	{
		color = GREEN;
		shape = DIAMOND;
	}

	@Override
	public void activate() {
		if (Dungeon.level == null
				|| Dungeon.level.heaps == null
				|| Dungeon.branch != TowerLevel.BRANCH
				|| Dungeon.depth < 1) {
			GLog.w(Messages.get(this, "unsupported"));
			return;
		}

		int moved = 0;
		for (Heap heap : new ArrayList<>(Dungeon.level.heaps.valueList())) {
			if (heap == null || !shouldDigest(heap.type) || heap.items == null || heap.items.isEmpty()) {
				continue;
			}

			ArrayList<Item> items = new ArrayList<>(heap.items);
			heap.items.clear();
			heap.destroy(Dungeon.level);
			for (Item item : items) {
				if (item == null) continue;
				int destination = destinationDepth(Dungeon.depth, Random.Int(2) == 0);
				Dungeon.queuePortedItem(item, destination, TowerLevel.BRANCH);
				moved++;
			}
		}

		GLog.n(Messages.get(this, moved > 0 ? "digested" : "empty"));
	}

	static boolean shouldDigest(Heap.Type type) {
		return type == Heap.Type.HEAP;
	}

	static int destinationDepth(int currentDepth, boolean next) {
		return next || currentDepth <= 1 ? currentDepth + 1 : currentDepth - 1;
	}
}
