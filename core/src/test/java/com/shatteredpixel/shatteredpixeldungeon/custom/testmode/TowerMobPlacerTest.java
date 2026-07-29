package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerMobPlacerTest {

	@Test
	public void towerMobCatalogStartsWithOnlyCamouflageGnoll() {
		assertEquals(15, TowerMobPlacer.MOBS_PER_PAGE);
		assertEquals(5, TowerMobPlacer.GRID_COLUMNS);
		assertEquals(3, TowerMobPlacer.gridRowsForCount(15));
		assertEquals(1, TowerMobPlacer.mobCount());
		assertEquals(1, TowerMobPlacer.pageCount());
		assertEquals(
				Collections.singletonList(CamouflageGnoll.class),
				TowerMobPlacer.mobsOnPage(0)
		);
	}

	@Test
	public void pageOutsideTowerMobCatalogIsEmpty() {
		assertTrue(TowerMobPlacer.mobsOnPage(-1).isEmpty());
		assertTrue(TowerMobPlacer.mobsOnPage(1).isEmpty());
	}
}
