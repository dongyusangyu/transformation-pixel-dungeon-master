package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RankingsQuickslotTest {

	@After
	public void resetDungeonState() {
		Dungeon.quickslot.reset();
		Dungeon.hero = null;
	}

	@Test
	public void rankingBackpackSnapshotKeepsNestedItemsInTheirContainer() {
		Bag backpack = new Bag();
		Bag holder = new Bag();
		holder.items.add(new UpgradeLikeItem().quantity(3));
		backpack.items.add(holder);

		ArrayList<Item> snapshot = Rankings.rankingBackpackItems(backpack);

		assertEquals(1, snapshot.size());
		assertSame(holder, snapshot.get(0));
		assertEquals(1, holder.items.size());
	}

	@Test
	public void rankingSnapshotRestoresSpecialQuickslotItemsInSlotOrder() {
		Item[] expected = {
				new UpgradeLikeItem().quantity(3),
				new StrengthLikeItem().quantity(2),
				new MetamorphosisLikeItem().quantity(4),
				new CrownLikeItem(),
				new MaskLikeItem()
		};
		for (int i = 0; i < expected.length; i++) {
			Dungeon.quickslot.setSlot(i, expected[i]);
		}

		Bundle rankingData = new Bundle();
		Dungeon.quickslot.storeRankingSnapshot(rankingData);
		Dungeon.quickslot.reset();
		assertTrue(Dungeon.quickslot.restoreRankingSnapshot(rankingData));
		for (int i = 0; i < expected.length; i++) {
			Item restored = Dungeon.quickslot.getItem(i);
			assertEquals(expected[i].getClass(), restored.getClass());
			assertEquals(expected[i].quantity(), restored.quantity());
		}
	}

	@Test
	public void rankingSnapshotRebindsSlotsToRestoredBackpackItems() {
		Item saved = new UpgradeLikeItem().quantity(3);
		Dungeon.quickslot.setSlot(2, saved);
		Bundle rankingData = new Bundle();
		Dungeon.quickslot.storeRankingSnapshot(rankingData);

		Item restoredBackpackItem = new UpgradeLikeItem().quantity(3);
		Bag backpack = new Bag();
		backpack.items.add(restoredBackpackItem);
		Dungeon.quickslot.reset();

		assertTrue(Dungeon.quickslot.restoreRankingSnapshot(rankingData, backpack));
		assertSame(restoredBackpackItem, Dungeon.quickslot.getItem(2));
	}

	@Test
	public void rankingSnapshotPrefersItemWithMatchingPersistentState() {
		PersistentStateItem saved = new PersistentStateItem(2);
		Dungeon.quickslot.setSlot(2, saved);
		Bundle rankingData = new Bundle();
		Dungeon.quickslot.storeRankingSnapshot(rankingData);

		PersistentStateItem other = new PersistentStateItem(0);
		PersistentStateItem intended = (PersistentStateItem) saved.duplicate();
		Bag backpack = new Bag();
		backpack.items.add(other);
		backpack.items.add(intended);
		Dungeon.quickslot.reset();

		assertTrue(Dungeon.quickslot.restoreRankingSnapshot(rankingData, backpack));
		assertSame(intended, Dungeon.quickslot.getItem(2));
	}

	@Test
	public void rankingSnapshotDoesNotKeepDetachedItemWhenInventoryIsProvided() {
		Dungeon.quickslot.setSlot(2, new PersistentStateItem(2));
		Bundle rankingData = new Bundle();
		Dungeon.quickslot.storeRankingSnapshot(rankingData);
		Dungeon.quickslot.reset();

		assertTrue(Dungeon.quickslot.restoreRankingSnapshot(rankingData, new Bag()));
		assertNull(Dungeon.quickslot.getItem(2));
	}

	public static class UpgradeLikeItem extends Item {
	}

	public static class StrengthLikeItem extends Item {
	}

	public static class MetamorphosisLikeItem extends Item {
	}

	public static class CrownLikeItem extends Item {
	}

	public static class MaskLikeItem extends Item {
	}

	public static class PersistentStateItem extends Item {
		private static final String STATE = "state";
		int state;

		public PersistentStateItem() {
		}

		PersistentStateItem(int state) {
			this.state = state;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STATE, state);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			state = bundle.getInt(STATE);
		}
	}
}
