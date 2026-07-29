package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.HallsPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;

import java.util.ArrayList;

/**
 * Keeps the halls decoration while replacing every generated void seam.
 */
public class ExtractionRaidPainter extends HallsPainter {

	@Override
	protected void decorate(Level level, ArrayList<Room> rooms) {
		super.decorate(level, rooms);
		for (int i = 0; i < level.length(); i++) {
			if (level.map[i] == Terrain.CHASM) {
				level.map[i] = Terrain.REGION_DECO;
			}
		}
	}
}

