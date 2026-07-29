package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.rooms;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel;

public class RaidExtractionRoom extends RaidRoom {

	@Override
	public boolean isExit() {
		return true;
	}

	@Override
	public void paint(Level level) {
		super.paint(level);
		int cell = level.pointToCell(random(1));
		level.map[cell] = Terrain.EMPTY_DECO;
		((ExtractionRaidLevel) level).recordExtractionCell(cell);
	}
}

