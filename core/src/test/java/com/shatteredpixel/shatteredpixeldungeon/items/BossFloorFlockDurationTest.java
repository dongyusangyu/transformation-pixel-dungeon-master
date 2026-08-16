package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.WoollyBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFlock;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BossFloorFlockDurationTest {

	@Test
	public void woollyBombUsesShortLifespanOnMainAndTowerBossFloors() {
		assertEquals(20, WoollyBomb.sheepLifespan(5, 0));
		assertEquals(20, WoollyBomb.sheepLifespan(5, TowerLevel.BRANCH));
		assertEquals(200, WoollyBomb.sheepLifespan(6, TowerLevel.BRANCH));
	}

	@Test
	public void flockStoneUsesBossLifespanOnlyOnBossFloors() {
		assertEquals(20, StoneOfFlock.sheepLifespan(10, 0));
		assertEquals(20, StoneOfFlock.sheepLifespan(10, TowerLevel.BRANCH));
		assertEquals(8, StoneOfFlock.sheepLifespan(11, TowerLevel.BRANCH));
	}
}
