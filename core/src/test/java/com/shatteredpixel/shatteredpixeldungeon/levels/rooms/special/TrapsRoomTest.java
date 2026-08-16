package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Test;

public class TrapsRoomTest {

	private final int originalDepth = Dungeon.depth;

	@After
	public void restoreGlobals() {
		Dungeon.depth = originalDepth;
	}

	@Test(expected = TrapPlaced.class)
	public void deepestContentDepthUsesLastTrapPool() {
		Dungeon.depth = 25;
		long seed = seedForTrapRoom();
		Random.pushGenerator(seed);
		try {
			TestLevel level = new TestLevel();
			level.setSize(10, 10);

			TrapsRoom room = new TrapsRoom();
			room.set(1, 1, 7, 7);
			room.connected.put(new EmptyRoom(), new Room.Door(1, 4));

			room.paint(level);
		} finally {
			Random.popGenerator();
		}
	}

	private static long seedForTrapRoom() {
		for (long seed = 0; ; seed++) {
			Random.pushGenerator(seed);
			boolean suitable = Random.Int(4) != 0;
			Random.popGenerator();
			if (suitable) {
				return seed;
			}
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

		@Override
		public Trap setTrap(Trap trap, int pos) {
			throw new TrapPlaced();
		}
	}

	private static class TrapPlaced extends RuntimeException {
	}

	private static class EmptyRoom extends Room {
		@Override
		public void paint(Level level) {
		}
	}
}
