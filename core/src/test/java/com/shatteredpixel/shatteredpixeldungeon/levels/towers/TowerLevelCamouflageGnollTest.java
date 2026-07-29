package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class TowerLevelCamouflageGnollTest {

	@Test
	public void everyTowerFloorUsesCamouflageGnollForFirstTwentyPercent() {
		assertSame(TowerMobRules.Selection.CAMOUFLAGE_GNOLL,
				TowerMobRules.select(1, 0f));
		assertSame(TowerMobRules.Selection.CAMOUFLAGE_GNOLL,
				TowerMobRules.select(1, 0.1999f));
		assertSame(TowerMobRules.Selection.CAMOUFLAGE_GNOLL,
				TowerMobRules.select(100, 0.1f));
	}

	@Test
	public void everyTowerFloorUsesCorrosiveSwarmForSecondTwentyPercent() {
		assertSame(TowerMobRules.Selection.CORROSIVE_SWARM,
				TowerMobRules.select(1, 0.2f));
		assertSame(TowerMobRules.Selection.CORROSIVE_SWARM,
				TowerMobRules.select(8, 0.3999f));
	}

	@Test
	public void remainingSixtyPercentUsesOriginalPool() {
		assertSame(TowerMobRules.Selection.DEFAULT_POOL,
				TowerMobRules.select(1, 0.4f));
		assertSame(TowerMobRules.Selection.DEFAULT_POOL,
				TowerMobRules.select(100, 0.9999f));
	}
}
