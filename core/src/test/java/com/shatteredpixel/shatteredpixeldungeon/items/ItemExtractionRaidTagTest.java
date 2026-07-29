package com.shatteredpixel.shatteredpixeldungeon.items;

import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ItemExtractionRaidTagTest {

	@Test
	public void extractionRaidMarkSurvivesBundleRoundTripAndCanBeCleared() {
		Item original = new Item().markForExtractionRaid(42L);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		Item restored = new Item();
		restored.restoreFromBundle(bundle);

		assertEquals(42L, restored.extractionRaidId());
		assertTrue(restored.isExtractionRaidLoot());
		assertTrue(restored.isExtractionRaidLoot(42L));
		restored.clearExtractionRaidMark();
		assertEquals(0L, restored.extractionRaidId());
		assertFalse(restored.isExtractionRaidLoot());
	}

	@Test
	public void mergeRequiresMatchingExtractionRaidOriginEvenForOverriddenSimilarity() {
		AlwaysSimilarStack existing = new AlwaysSimilarStack();
		AlwaysSimilarStack unmarked = new AlwaysSimilarStack();
		AlwaysSimilarStack sameRaid = new AlwaysSimilarStack().mark(7L);
		AlwaysSimilarStack otherRaid = new AlwaysSimilarStack().mark(8L);

		existing.markForExtractionRaid(7L);
		existing.merge(unmarked);
		existing.merge(otherRaid);
		assertEquals(1, existing.quantity());
		assertEquals(1, unmarked.quantity());
		assertEquals(1, otherRaid.quantity());

		existing.merge(sameRaid);
		assertEquals(2, existing.quantity());
		assertEquals(0, sameRaid.quantity());
	}

	@Test
	public void virtualItemKeepsExtractionRaidOrigin() {
		Item original = new Item().markForExtractionRaid(91L);

		Item virtual = original.virtual();

		assertEquals(0, virtual.quantity());
		assertEquals(91L, virtual.extractionRaidId());
		assertTrue(original.hasSameExtractionRaidOrigin(virtual));
	}

	private static class AlwaysSimilarStack extends Item {
		{
			stackable = true;
		}

		AlwaysSimilarStack mark(long raidId) {
			markForExtractionRaid(raidId);
			return this;
		}

		@Override
		public boolean isSimilar(Item item) {
			return item instanceof AlwaysSimilarStack;
		}
	}
}
