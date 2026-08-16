package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.levels.TestArenaLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReturnTeleportPolicyTest {

	@Test
	public void allowsTowerMinusOneAndLowerFloors() {
		assertTrue(Dungeon.returnTeleportLocationAllowed(1, TowerLevel.BRANCH));
		assertTrue(Dungeon.returnTeleportLocationAllowed(2, TowerLevel.BRANCH));
		assertTrue(Dungeon.returnTeleportLocationAllowed(100, TowerLevel.BRANCH));
	}

	@Test
	public void allowsOnlyMainDisplayFloorsOneThroughTwentyFive() {
		assertFalse(Dungeon.returnTeleportLocationAllowed(0, 0));
		assertTrue(Dungeon.returnTeleportLocationAllowed(1, 0));
		assertTrue(Dungeon.returnTeleportLocationAllowed(25, 0));
		assertFalse(Dungeon.returnTeleportLocationAllowed(26, 0));
	}

	@Test
	public void locationComparisonIncludesBranch() {
		assertTrue(Dungeon.sameLocation(10, 0, 10, 0));
		assertFalse(Dungeon.sameLocation(10, 0, 10, 1));
		assertFalse(Dungeon.sameLocation(10, 0, 11, 0));
	}

	@Test
	public void markerMustHaveBeenSetOnAnAllowedFloor() {
		assertTrue(Dungeon.returnTeleportMarkerAllowed(1, TowerLevel.BRANCH, true));
		assertTrue(Dungeon.returnTeleportMarkerAllowed(
				ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH, true));
		assertTrue(Dungeon.returnTeleportMarkerAllowed(
				TestArenaLevel.DEPTH, TestArenaLevel.BRANCH, true));
		assertFalse(Dungeon.returnTeleportMarkerAllowed(1, TowerLevel.BRANCH, false));
		assertFalse(Dungeon.returnTeleportMarkerAllowed(26, 0, true));
	}

	@Test
	public void sideBranchesOnlyAllowSameFloorReturn() {
		assertTrue(Dungeon.returnTeleportRouteAllowed(
				ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH, false, true,
				ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH, true));
		assertFalse(Dungeon.returnTeleportRouteAllowed(
				ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH, false, true,
				1, 0, true));
		assertFalse(Dungeon.returnTeleportRouteAllowed(
				11, 1, false, true,
				11, 0, true));
	}

	@Test
	public void lockedFloorOnlyAllowsSameFloorReturn() {
		assertTrue(Dungeon.returnTeleportRouteAllowed(
				5, 0, true, true,
				5, 0, true));
		assertFalse(Dungeon.returnTeleportRouteAllowed(
				5, 0, true, true,
				4, 0, true));
		assertTrue(Dungeon.returnTeleportRouteAllowed(
				5, 0, false, true,
				4, 0, true));
	}

	@Test
	public void returnRejectsEitherUnreachableEndpoint() {
		assertFalse(Dungeon.returnTeleportRouteAllowed(
				5, 0, false, false,
				5, 0, true));
		assertFalse(Dungeon.returnTeleportRouteAllowed(
				5, 0, false, true,
				5, 0, false));
	}

	@Test
	public void depthZeroNeverUsesLockedFloorException() {
		assertFalse(Dungeon.returnTeleportRouteAllowed(
				0, 0, true, true,
				0, 0, true));
	}

	@Test
	public void currentFloorMustBeInRangeAndUnlocked() {
		Level oldLevel = Dungeon.level;
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			TestLevel level = new TestLevel();
			Dungeon.level = level;

			Dungeon.depth = 1;
			Dungeon.branch = 0;
			level.locked = false;
			assertTrue(Dungeon.returnTeleportAllowed());

			Dungeon.depth = 5;
			assertTrue(Dungeon.returnTeleportAllowed());

			level.locked = true;
			assertFalse(Dungeon.returnTeleportAllowed());
			assertTrue(Dungeon.returnTeleportMarkerPlacementAllowed());

			level.locked = false;
			Dungeon.depth = 26;
			assertFalse(Dungeon.returnTeleportAllowed());
			assertFalse(Dungeon.returnTeleportMarkerPlacementAllowed());

			Dungeon.depth = 0;
			level.locked = true;
			assertFalse(Dungeon.returnTeleportMarkerPlacementAllowed());

			Dungeon.depth = 1;
			Dungeon.branch = TowerLevel.BRANCH;
			level.locked = false;
			assertTrue(Dungeon.returnTeleportAllowed());
		} finally {
			Dungeon.level = oldLevel;
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
		}
	}

	@Test
	public void warpBeaconTrackerPersistsValidityAndRejectsOutOfRangeTarget() throws Exception {
		WarpBeacon.WarpBeaconTracker source = new WarpBeacon.WarpBeaconTracker();
		setField(source, "pos", 12);
		setField(source, "depth", 1);
		setField(source, "branch", TowerLevel.BRANCH);
		setField(source, "markerValid", true);
		setField(source, "markerReachable", true);

		Bundle saved = new Bundle();
		source.storeInBundle(saved);
		WarpBeacon.WarpBeaconTracker restored = new WarpBeacon.WarpBeaconTracker();
		restored.restoreFromBundle(saved);
		assertEquals(12, getIntField(restored, "pos"));
		assertEquals(1, getIntField(restored, "depth"));
		assertEquals(TowerLevel.BRANCH, getIntField(restored, "branch"));
		assertTrue(getBooleanField(restored, "markerValid"));
		assertTrue(getBooleanField(restored, "markerReachable"));

		Bundle sideBranch = new Bundle();
		sideBranch.put(WarpBeacon.WarpBeaconTracker.POS, 12);
		sideBranch.put(WarpBeacon.WarpBeaconTracker.DEPTH, ExtractionRaidLevel.DEPTH);
		sideBranch.put(WarpBeacon.WarpBeaconTracker.BRANCH, ExtractionRaidLevel.BRANCH);
		sideBranch.put(WarpBeacon.WarpBeaconTracker.MARKER_VALID, true);
		sideBranch.put(WarpBeacon.WarpBeaconTracker.MARKER_REACHABLE, true);
		WarpBeacon.WarpBeaconTracker sideBranchRestored = new WarpBeacon.WarpBeaconTracker();
		sideBranchRestored.restoreFromBundle(sideBranch);
		assertTrue(getBooleanField(sideBranchRestored, "markerValid"));
		assertTrue(getBooleanField(sideBranchRestored, "markerReachable"));

		Bundle invalid = new Bundle();
		invalid.put(WarpBeacon.WarpBeaconTracker.POS, 12);
		invalid.put(WarpBeacon.WarpBeaconTracker.DEPTH, 26);
		invalid.put(WarpBeacon.WarpBeaconTracker.BRANCH, 0);
		invalid.put(WarpBeacon.WarpBeaconTracker.MARKER_VALID, true);
		WarpBeacon.WarpBeaconTracker invalidRestored = new WarpBeacon.WarpBeaconTracker();
		invalidRestored.restoreFromBundle(invalid);
		assertFalse(getBooleanField(invalidRestored, "markerValid"));
	}

	@Test
	public void nonBossPositionMustConnectToAScrollTeleportRegion() {
		Level oldLevel = Dungeon.level;
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			TestLevel level = new TestLevel();
			level.setSize(5, 5);
			level.transitions = new java.util.ArrayList<>();
			level.passable[7] = true;
			level.passable[8] = true;
			level.passable[18] = true;
			level.secret[18] = true;
			Dungeon.level = level;
			Dungeon.depth = 1;
			Dungeon.branch = 0;

			assertTrue(Dungeon.returnTeleportPositionAllowed(7));
			assertFalse(Dungeon.returnTeleportPositionAllowed(18));
		} finally {
			Dungeon.level = oldLevel;
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
			if (oldLevel != null) {
				com.watabou.utils.PathFinder.setMapSize(oldLevel.width(), oldLevel.height());
			}
		}
	}

	@Test
	public void lockedBossTreatsItsPassableCombatAreaAsTeleportable() {
		Level oldLevel = Dungeon.level;
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			TestLevel level = new TestLevel();
			level.setSize(5, 5);
			level.transitions = new java.util.ArrayList<>();
			level.passable[7] = true;
			level.locked = true;
			Dungeon.level = level;
			Dungeon.depth = 5;
			Dungeon.branch = 0;

			assertTrue(Dungeon.returnTeleportPositionAllowed(7));
		} finally {
			Dungeon.level = oldLevel;
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
			if (oldLevel != null) {
				com.watabou.utils.PathFinder.setMapSize(oldLevel.width(), oldLevel.height());
			}
		}
	}

	@Test
	public void lockedBossUsesTheLevelsCombatAreaPolicy() {
		Level oldLevel = Dungeon.level;
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			RestrictedBossLevel level = new RestrictedBossLevel();
			level.setSize(5, 5);
			level.transitions = new java.util.ArrayList<>();
			level.passable[7] = true;
			level.passable[8] = true;
			level.allowedPosition = 8;
			level.locked = true;
			Dungeon.level = level;
			Dungeon.depth = 5;
			Dungeon.branch = 0;

			assertFalse(Dungeon.returnTeleportPositionAllowed(7));
			assertTrue(Dungeon.returnTeleportPositionAllowed(8));
		} finally {
			Dungeon.level = oldLevel;
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
			if (oldLevel != null) {
				com.watabou.utils.PathFinder.setMapSize(oldLevel.width(), oldLevel.height());
			}
		}
	}

	private static void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static int getIntField(Object target, String name) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.getInt(target);
	}

	private static boolean getBooleanField(Object target, String name) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.getBoolean(target);
	}

	private static class TestLevel extends Level {
		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}

	private static class RestrictedBossLevel extends TestLevel {
		private int allowedPosition;

		@Override
		public boolean isBossTeleportPositionAllowed(int pos) {
			return pos == allowedPosition;
		}
	}

}
