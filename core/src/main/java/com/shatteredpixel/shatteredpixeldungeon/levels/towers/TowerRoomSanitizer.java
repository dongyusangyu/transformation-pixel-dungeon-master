package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;

final class TowerRoomSanitizer {

	private TowerRoomSanitizer() {
	}

	/**
	 * Tower tiles do not render the library hall's solid region decorations as
	 * usable room furniture. Keep the stair itself, but make the other cells
	 * ordinary floor so the entrance room has no invisible collision cells.
	 */
	static void clearEntranceRoomDecor(Level level, Room room, int entranceCell) {
		for (int y = room.top; y <= room.bottom; y++) {
			for (int x = room.left; x <= room.right; x++) {
				int cell = x + y * level.width();
				if (cell != entranceCell && level.map[cell] == Terrain.REGION_DECO) {
					level.map[cell] = Terrain.EMPTY;
				}
			}
		}
	}
}
