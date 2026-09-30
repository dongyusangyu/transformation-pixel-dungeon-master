package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HeapSalePriceTest {

	@Test
	public void explicitSaleDepthControlsPriceAndSurvivesSave() {
		Heap original = new Heap();
		original.items.add(new FixedValueItem());
		original.saleDepth(20);

		assertEquals(Shopkeeper.sellPrice(original.peek(), 20), original.salePrice());

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		Heap restored = new Heap();
		restored.restoreFromBundle(bundle);

		assertEquals(20, restored.saleDepth());
		assertEquals(Shopkeeper.sellPrice(restored.peek(), 20), restored.salePrice());
	}

	@Test
	public void newSaleItemsRemainImmediatelyBelowLostBackpack() {
		Heap heap = new Heap();
		heap.type = Heap.Type.FOR_SALE;
		Item first = new FixedValueItem();
		Item second = new FixedValueItem();
		Item third = new FixedValueItem();
		LostBackpack backpack = TestHeroFactory.allocateItem(LostBackpack.class);
		heap.drop(first);
		heap.drop(backpack);
		heap.drop(second);
		heap.drop(third);

		assertEquals(backpack, heap.pickUp());
		assertEquals(third, heap.peek());
		assertEquals(third, heap.pickUp());
		assertEquals(second, heap.pickUp());
		assertEquals(first, heap.peek());
	}

	public static class FixedValueItem extends Item {
		@Override
		public int value() {
			return 40;
		}
	}
}
