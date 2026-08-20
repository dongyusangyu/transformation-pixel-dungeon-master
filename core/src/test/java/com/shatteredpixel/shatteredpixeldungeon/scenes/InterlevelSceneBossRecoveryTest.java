package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InterlevelSceneBossRecoveryTest {

	private int originalDepth;
	private int originalBranch;
	private boolean[] originalSublimation;

	@Before
	public void saveDungeonLocation() {
		originalDepth = Dungeon.depth;
		originalBranch = Dungeon.branch;
		originalSublimation = Statistics.subLimation;
	}

	@After
	public void restoreDungeonLocation() {
		Dungeon.depth = originalDepth;
		Dungeon.branch = originalBranch;
		Statistics.subLimation = originalSublimation;
	}

	@Test
	public void lockedTowerBossResetsForUnblessedAnkhWithoutUsingMainBossState() {
		Dungeon.depth = 100;
		Dungeon.branch = TowerLevel.BRANCH;
		Statistics.subLimation = new boolean[5];
		SafeArrivalResetLevel level = new SafeArrivalResetLevel(true);
		level.locked = true;

		assertTrue(level.shouldResetForSafeArrival());
		assertTrue(InterlevelScene.shouldResetSealedBossOnUnblessedAnkh(level));
	}

	@Test
	public void unlockedTowerBossIsNotResetDuringSafeArrival() {
		Dungeon.depth = 100;
		Dungeon.branch = TowerLevel.BRANCH;
		SafeArrivalResetLevel level = new SafeArrivalResetLevel(true);
		level.locked = false;

		assertFalse(level.shouldResetForSafeArrival());
		assertFalse(InterlevelScene.shouldResetSealedBossOnUnblessedAnkh(level));
	}

	private static class SafeArrivalResetLevel extends Level {
		private final boolean resetForSafeArrival;

		SafeArrivalResetLevel(boolean resetForSafeArrival) {
			this.resetForSafeArrival = resetForSafeArrival;
		}

		@Override
		protected boolean build() {
			return false;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}

		@Override
		public boolean shouldResetForSafeArrival() {
			return locked && resetForSafeArrival;
		}
	}
}
