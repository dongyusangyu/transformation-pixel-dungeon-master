package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TowerChasmDropTest {

	@Test
	public void towerItemsFallToThePreviousTowerFloor() {
		assertEquals(2, Dungeon.chasmDropDepthForLocation(3, TowerLevel.BRANCH));
	}

	@Test
	public void towerFirstFloorItemsFallToSurface() {
		assertEquals(0, Dungeon.chasmDropDepthForLocation(1, TowerLevel.BRANCH));
		assertEquals(0, Dungeon.chasmDropKeyForLocation(1, TowerLevel.BRANCH));
	}

	@Test
	public void ordinaryFloorItemDirectionIsUnchanged() {
		assertEquals(4, Dungeon.chasmDropDepthForLocation(3, 0));
		assertEquals(4, Dungeon.chasmDropKeyForLocation(3, 0));
	}

	@Test
	public void towerItemsUseASeparateDestinationKey() {
		assertEquals(Integer.MIN_VALUE + 2,
				Dungeon.chasmDropKeyForLocation(3, TowerLevel.BRANCH));
		assertEquals(Integer.MIN_VALUE + 2,
				Dungeon.droppedItemsKeyForLocation(2, TowerLevel.BRANCH));
	}
}
