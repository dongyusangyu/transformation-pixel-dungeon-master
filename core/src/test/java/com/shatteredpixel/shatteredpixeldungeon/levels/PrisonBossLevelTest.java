package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.utils.SparseArray;
import com.watabou.utils.Rect;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PrisonBossLevelTest {

	private Level previousLevel;

	@Before
	public void rememberDungeonLevel() {
		previousLevel = Dungeon.level;
	}

	@After
	public void restoreDungeonLevel() {
		Actor.clear();
		Dungeon.level = previousLevel;
	}

	@Test
	public void cleanTenguCellRemovesAllFadingTrapOverlaysWithoutMutatingItsIterator() {
		PrisonBossLevel level = new PrisonBossLevel();
		level.setSize(21, 35);
		Arrays.fill(level.map, Terrain.EMPTY);
		level.blobs = new HashMap<>();
		level.traps = new SparseArray<>();
		level.mobs = new HashSet<>();
		level.customTiles = new ArrayList<>();
		Dungeon.level = level;

		PrisonBossLevel.FadingTraps firstOverlay = new PrisonBossLevel.FadingTraps();
		CustomTilemap persistentTile = new PersistentTile();
		PrisonBossLevel.FadingTraps secondOverlay = new PrisonBossLevel.FadingTraps();
		level.customTiles.add(firstOverlay);
		level.customTiles.add(persistentTile);
		level.customTiles.add(secondOverlay);

		level.cleanTenguCell();

		assertEquals(1, level.customTiles.size());
		assertSame(persistentTile, level.customTiles.get(0));
		assertFalse(level.customTiles.contains(firstOverlay));
		assertFalse(level.customTiles.contains(secondOverlay));
	}

	@Test
	public void strongerTenguGuardPrefersOpenSideCellsAndNeverOverlapsTengu() {
		PrisonBossLevel level = openPrisonLevel();
		int center = 40;
		Tengu tengu = new Tengu();
		tengu.pos = center;
		Actor.add(tengu);

		int guardCell = level.findTenguGuardCell(center, new Rect(0, 0, 9, 9));

		assertTrue(guardCell == center - 1 || guardCell == center + 1);
		assertTrue(guardCell != center);
	}

	@Test
	public void strongerTenguGuardFallsBackToOtherAdjacentCellAndSkipsWhenBlocked() {
		PrisonBossLevel level = openPrisonLevel();
		int center = 40;
		Tengu tengu = new Tengu();
		tengu.pos = center;
		Actor.add(tengu);
		level.solid[center - 1] = true;
		level.solid[center + 1] = true;

		int fallback = level.findTenguGuardCell(center, new Rect(0, 0, 9, 9));
		assertTrue(fallback >= 0 && fallback < level.length());
		assertTrue(fallback != center && fallback != center - 1 && fallback != center + 1);

		for (int offset : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			level.solid[center + offset] = true;
		}
		assertEquals(-1, level.findTenguGuardCell(center, new Rect(0, 0, 9, 9)));
	}

	private static PrisonBossLevel openPrisonLevel() {
		Actor.clear();
		PrisonBossLevel level = new PrisonBossLevel();
		level.setSize(9, 9);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		Arrays.fill(level.pit, false);
		level.blobs = new HashMap<>();
		level.traps = new SparseArray<>();
		level.mobs = new HashSet<>();
		Dungeon.level = level;
		return level;
	}

	private static class PersistentTile extends CustomTilemap {
	}
}
