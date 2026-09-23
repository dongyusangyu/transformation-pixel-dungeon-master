package com.shatteredpixel.shatteredpixeldungeon.items.keys;

import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

public class RaidAccessCard extends Key {

	{
		image = EXItemSpriteSheet.RAID_ACCESS_CARD;
	}

	public RaidAccessCard() {
		this(ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH);
	}

	public RaidAccessCard(int depth, int branch) {
		super();
		this.depth = depth;
		this.branch = branch;
	}
}
