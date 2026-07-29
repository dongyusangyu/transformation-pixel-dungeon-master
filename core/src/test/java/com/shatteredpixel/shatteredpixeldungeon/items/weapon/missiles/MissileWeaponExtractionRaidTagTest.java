package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MissileWeaponExtractionRaidTagTest {

	@Test
	public void similarityRequiresMatchingExtractionRaidOrigin() {
		TestMissile raid = missile(5L);
		TestMissile sameRaid = missile(5L);
		TestMissile otherRaid = missile(6L);
		TestMissile unmarked = new TestMissile();

		assertTrue(raid.isSimilar(sameRaid));
		assertFalse(raid.isSimilar(otherRaid));
		assertFalse(raid.isSimilar(unmarked));
	}

	@Test
	public void heapKeepsMissilesFromDifferentOriginsAsSeparateStacks() {
		Heap heap = new Heap();
		TestMissile raid = missile(5L);
		TestMissile unmarked = new TestMissile();

		heap.drop(raid);
		heap.drop(unmarked);

		assertEquals(2, heap.size());
		assertTrue(heap.items.contains(raid));
		assertTrue(heap.items.contains(unmarked));
	}

	@Test
	public void pinCushionKeepsMissilesFromDifferentOriginsAsSeparateStacks() {
		PinCushion pinCushion = new PinCushion();
		TestMissile raid = missile(5L);
		TestMissile unmarked = new TestMissile();

		pinCushion.stick(raid);
		pinCushion.stick(unmarked);

		assertEquals(2, pinCushion.getStuckItems().size());
		assertTrue(pinCushion.getStuckItems().contains(raid));
		assertTrue(pinCushion.getStuckItems().contains(unmarked));
	}

	private static TestMissile missile(long raidId) {
		TestMissile missile = new TestMissile();
		missile.markForExtractionRaid(raidId);
		return missile;
	}

	private static class TestMissile extends MissileWeapon {
	}
}
