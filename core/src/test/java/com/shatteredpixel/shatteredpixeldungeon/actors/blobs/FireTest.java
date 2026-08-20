package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.util.HashMap;

public class FireTest {

	@Test
	public void evolveSafelyIgnoresAreaExtendingPastMapEdge() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = new TestLevel();
			level.setSize(5, 5);
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			TestFire fire = new TestFire();
			fire.prepare(level, 22);

			fire.evolveNow();
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	private static class TestFire extends Fire {
		void prepare(Level level, int edgeCell) {
			cur = new int[level.length()];
			off = new int[level.length()];
			area.union(edgeCell % level.width(), edgeCell / level.width());
		}

		void evolveNow() {
			evolve();
		}
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
}
