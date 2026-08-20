package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class PrisonBossLevelTest {

	private Level previousLevel;

	@Before
	public void rememberDungeonLevel() {
		previousLevel = Dungeon.level;
	}

	@After
	public void restoreDungeonLevel() {
		Dungeon.level = previousLevel;
	}

	@Test
	public void cleanTenguCellRemovesAllFadingTrapOverlaysWithoutMutatingItsIterator() {
		PrisonBossLevel level = new PrisonBossLevel();
		level.setSize(21, 35);
		Arrays.fill(level.map, Terrain.EMPTY);
		level.blobs = new HashMap<>();
		level.traps = new SparseArray<>();
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

	private static class PersistentTile extends CustomTilemap {
	}
}
