package com.shatteredpixel.shatteredpixeldungeon.custom.ch.boss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class HardTenguLevelTest {

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
	public void replacingHardTenguTrapOverlaysDoesNotMutateTheActiveIterator() throws Exception {
		HardTenguLevel level = new HardTenguLevel();
		level.customTiles = new ArrayList<>();
		Dungeon.level = level;

		HardTenguLevel.FadingTraps firstOverlay = new HardTenguLevel.FadingTraps();
		CustomTilemap persistentTile = new PersistentTile();
		HardTenguLevel.FadingTraps secondOverlay = new HardTenguLevel.FadingTraps();
		level.customTiles.add(firstOverlay);
		level.customTiles.add(persistentTile);
		level.customTiles.add(secondOverlay);

		invokeFadingTrapCleanup(level);

		assertEquals(1, level.customTiles.size());
		assertSame(persistentTile, level.customTiles.get(0));
		assertFalse(level.customTiles.contains(firstOverlay));
		assertFalse(level.customTiles.contains(secondOverlay));
	}

	private static void invokeFadingTrapCleanup(HardTenguLevel level) throws Exception {
		Method method;
		try {
			method = HardTenguLevel.class.getDeclaredMethod("removeFadingTraps");
		} catch (NoSuchMethodException exception) {
			fail("Hard Tengu needs a dedicated safe fading-trap cleanup method");
			return;
		}
		method.setAccessible(true);
		method.invoke(level);
	}

	private static class PersistentTile extends CustomTilemap {
	}
}
