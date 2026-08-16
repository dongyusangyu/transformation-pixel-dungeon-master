package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.entrance.LibraryHallEntranceRoom;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TowerEntranceRoomTest {

	@Test
	public void entranceRoomDoesNotLeaveRegionDecorationAsSolidAirWall() {
		Level level = new Level() {
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
		};
		level.setSize(9, 9);
		level.transitions = new ArrayList<>();

		LibraryHallEntranceRoom room = new LibraryHallEntranceRoom();
		room.forceSize(7, 7);
		room.left = room.top = 1;
		room.right = room.bottom = 7;
		room.paint(level);

		int entrance = level.entrance();
		TowerRoomSanitizer.clearEntranceRoomDecor(level, room, entrance);

		int regionDecorations = 0;
		for (int y = room.top; y <= room.bottom; y++) {
			for (int x = room.left; x <= room.right; x++) {
				if (level.map[x + y * level.width()] == Terrain.REGION_DECO) {
					regionDecorations++;
				}
			}
		}

		assertEquals(0, regionDecorations);
	}
}
