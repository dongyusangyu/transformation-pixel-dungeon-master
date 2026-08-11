package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class WndRankingItemsTest {

	@Test
	public void putsAtMostTwoBackpackTrinketsBeforeQuickslotItems() {
		Bag backpack = new Bag();
		Item firstTrinket = new TestTrinket();
		Item secondTrinket = new TestTrinket();
		Item thirdTrinket = new TestTrinket();
		backpack.items.add(firstTrinket);
		backpack.items.add(secondTrinket);
		backpack.items.add(thirdTrinket);

		Item firstQuickslotItem = new Item();
		Item secondQuickslotItem = new Item();
		Item[] quickslotItems = new Item[]{
				firstQuickslotItem, firstTrinket, secondQuickslotItem, null, null, null
		};

		ArrayList<Item> shown = WndRanking.rankingDisplayItems(backpack, quickslotItems);

		assertEquals(4, shown.size());
		assertSame(firstTrinket, shown.get(0));
		assertSame(secondTrinket, shown.get(1));
		assertSame(firstQuickslotItem, shown.get(2));
		assertSame(secondQuickslotItem, shown.get(3));
	}

	@Test
	public void keepsEveryNonEmptyQuickslotItemInSlotOrder() {
		Bag backpack = new Bag();
		Item[] quickslotItems = new Item[6];
		for (int i = 0; i < quickslotItems.length; i++) {
			quickslotItems[i] = new Item();
		}

		ArrayList<Item> shown = WndRanking.rankingDisplayItems(backpack, quickslotItems);

		assertEquals(quickslotItems.length, shown.size());
		for (int i = 0; i < quickslotItems.length; i++) {
			assertSame(quickslotItems[i], shown.get(i));
		}
	}

	@Test
	public void omitsDepletedQuickslotPlaceholders() {
		Bag backpack = new Bag();
		Item depleted = new Item().quantity(0);
		Item available = new Item();

		ArrayList<Item> shown = WndRanking.rankingDisplayItems(
				backpack, new Item[]{depleted, available, null, null, null, null});

		assertEquals(1, shown.size());
		assertSame(available, shown.get(0));
	}

	private static class TestTrinket extends Trinket {
		@Override
		protected int upgradeEnergyCost() {
			return 0;
		}

		@Override
		public String statsDesc() {
			return "";
		}
	}
}
