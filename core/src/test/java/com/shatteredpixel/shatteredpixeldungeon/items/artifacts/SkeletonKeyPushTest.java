package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SkeletonKeyPushTest {
	private Level oldLevel;
	private TestLevel level;

	@Before public void setUp() {
		oldLevel = Dungeon.level;
		Actor.clear();
		level = new TestLevel();
		level.setSize(7, 7);
		for (int cell = 0; cell < level.length(); cell++) level.openSpace[cell] = true;
		Dungeon.level = level;
	}

	@After public void tearDown() {
		Dungeon.level = oldLevel;
		Actor.clear();
	}

	@Test public void lockingDoorPrefersCellDirectlyBehindEnemy() throws Exception {
		assertEquals(31, pushCell(17, 24));
	}

	@Test public void blockedOppositeCellFallsBackToFurthestOpenCell() throws Exception {
		level.solid[31] = true;
		int selected = pushCell(17, 24);
		// 30 and 32 are equally distant from the hero, unlike lateral cells 23 and 25.
		assertTrue(selected == 30 || selected == 32);
	}

	@Test public void blockedDoorHasNoPushDestination() throws Exception {
		for (int cell = 0; cell < level.length(); cell++) level.solid[cell] = true;
		assertEquals(-1, pushCell(17, 24));
	}

	private int pushCell(int heroCell, int doorCell) throws Exception {
		Method method = SkeletonKey.class.getDeclaredMethod("findDoorPushCell", int.class, int.class, Char.class);
		method.setAccessible(true);
		return (int) method.invoke(null, heroCell, doorCell, TestHeroFactory.create());
	}

	private static class TestLevel extends Level {
		@Override protected boolean build() { return true; }
		@Override protected void createMobs() {}
		@Override protected void createItems() {}
	}
}
