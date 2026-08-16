package com.shatteredpixel.shatteredpixeldungeon.items.keys;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerKeyTest {

	private int previousDepth;
	private int previousBranch;

	@Before
	public void setUp() {
		previousDepth = Dungeon.depth;
		previousBranch = Dungeon.branch;
		Notes.reset();
	}

	@After
	public void tearDown() {
		Notes.reset();
		Dungeon.depth = previousDepth;
		Dungeon.branch = previousBranch;
	}

	@Test
	public void pickupRebindsVirtualGenerationDepthToTheCurrentTowerFloor() {
		Dungeon.branch = TowerLevel.BRANCH;
		Dungeon.depth = 18;
		TestIronKey key = new TestIronKey(Dungeon.depth);

		Dungeon.depth = 3;
		key.bindToCurrentLocation();
		Notes.add(key);

		assertEquals(3, key.depth);
		assertEquals(TowerLevel.BRANCH, key.branch);
		assertEquals(1, Notes.keyCount(new TestIronKey(3)));
	}

	@Test
	public void keysFromTheMainDungeonAndTowerDoNotMix() {
		Dungeon.depth = 3;
		Dungeon.branch = TowerLevel.BRANCH;
		Notes.add(new TestIronKey(3));

		assertEquals(1, Notes.keyCount(new TestIronKey(3)));

		Dungeon.branch = 0;
		assertEquals(0, Notes.keyCount(new TestIronKey(3)));
		assertFalse(Notes.remove(new TestIronKey(3)));
	}

	@Test
	public void towerDoorAndChestQueriesFindTheirMatchingKeys() {
		Dungeon.depth = 7;
		Dungeon.branch = TowerLevel.BRANCH;
		Notes.add(new TestIronKey(7));
		Notes.add(new TestGoldenKey(7));
		Notes.add(new TestCrystalKey(7));

		assertEquals(1, Notes.keyCount(new TestIronKey(Dungeon.depth)));
		assertEquals(1, Notes.keyCount(new TestGoldenKey(Dungeon.depth)));
		assertEquals(1, Notes.keyCount(new TestCrystalKey(Dungeon.depth)));
	}

	@Test
	public void keyBranchSurvivesSaveAndLoad() {
		Dungeon.depth = 5;
		Dungeon.branch = TowerLevel.BRANCH;
		TestIronKey original = new TestIronKey(5);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		Dungeon.branch = 0;
		TestIronKey restored = new TestIronKey();
		restored.restoreFromBundle(bundle);

		assertEquals(5, restored.depth);
		assertEquals(TowerLevel.BRANCH, restored.branch);
	}

	@Test
	public void legacyTowerKeyRecordMigratesFromContentDepth() {
		Dungeon.depth = 3;
		Dungeon.branch = TowerLevel.BRANCH;
		Bundle legacyKeyBundle = new Bundle();
		legacyKeyBundle.put("depth", 18);
		legacyKeyBundle.put("quantity", 1);
		TestIronKey legacyKey = new TestIronKey();
		legacyKey.restoreFromBundle(legacyKeyBundle);
		Notes.add(legacyKey);

		Notes.migrateLegacyKeys(Dungeon.depth, Dungeon.branch);

		assertEquals(1, Notes.keyCount(new TestIronKey(3)));
		assertEquals(0, Notes.keyCount(new TestIronKey(18)));
	}

	public static class TestIronKey extends Key {
		public TestIronKey() {
			this(0);
		}

		public TestIronKey(int depth) {
			this.depth = depth;
		}
	}

	public static class TestGoldenKey extends Key {
		public TestGoldenKey() {
			this(0);
		}

		public TestGoldenKey(int depth) {
			this.depth = depth;
		}
	}

	public static class TestCrystalKey extends Key {
		public TestCrystalKey() {
			this(0);
		}

		public TestCrystalKey(int depth) {
			this.depth = depth;
		}
	}
}
