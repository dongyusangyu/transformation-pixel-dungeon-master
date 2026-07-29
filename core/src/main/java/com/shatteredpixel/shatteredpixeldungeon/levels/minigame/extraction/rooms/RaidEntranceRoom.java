package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.rooms;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel;

public class RaidEntranceRoom extends RaidRoom {

	@Override
	public boolean isEntrance() {
		return true;
	}

	@Override
	public void paint(Level level) {
		super.paint(level);
		((ExtractionRaidLevel) level).recordEntranceCell(level.pointToCell(random(1)));
	}
}

