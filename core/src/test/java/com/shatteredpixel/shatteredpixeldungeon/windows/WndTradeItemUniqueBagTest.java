/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import sun.misc.Unsafe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WndTradeItemUniqueBagTest {

	@Test
	public void rejectsShopBagWhenSameTypeIsAlreadyOwned() throws Exception {
		Bag backpack = emptyBag(Bag.class);
		backpack.items.add(emptyBag(VelvetPouch.class));

		assertTrue(WndTradeItem.ownsSameBag(backpack, emptyBag(VelvetPouch.class)));
	}

	@Test
	public void permitsDifferentBagTypesAndOrdinaryItems() throws Exception {
		Bag backpack = emptyBag(Bag.class);
		backpack.items.add(emptyBag(VelvetPouch.class));

		assertFalse(WndTradeItem.ownsSameBag(backpack, emptyBag(ScrollHolder.class)));
		assertFalse(WndTradeItem.ownsSameBag(backpack, new Item()));
	}

	@SuppressWarnings("unchecked")
	private static <T extends Bag> T emptyBag(Class<T> type) throws Exception {
		T bag = (T) unsafe().allocateInstance(type);
		bag.items = new ArrayList<>();
		return bag;
	}

	private static Unsafe unsafe() throws Exception {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (Unsafe) field.get(null);
	}
}
