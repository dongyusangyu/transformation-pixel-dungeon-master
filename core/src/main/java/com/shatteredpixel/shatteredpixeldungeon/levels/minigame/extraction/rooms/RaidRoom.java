package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.rooms;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;

/**
 * A plain halls-themed room which cannot paint pits or chasms.
 */
public class RaidRoom extends StandardRoom {

	@Override
	public float[] sizeCatProbs() {
		return new float[]{4, 1, 0};
	}

	@Override
	public void paint(Level level) {
		Painter.fill(level, this, Terrain.WALL);
		Painter.fill(level, this, 1, Terrain.EMPTY);

		for (Door door : connected.values()) {
			door.set(Door.Type.UNLOCKED);
		}
	}
}

