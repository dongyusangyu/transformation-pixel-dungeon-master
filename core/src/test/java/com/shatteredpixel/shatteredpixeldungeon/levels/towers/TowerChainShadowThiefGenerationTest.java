package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThief;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TowerChainShadowThiefGenerationTest {

	@Test
	public void chainShadowThiefIsAnEquallyWeightedTowerSelection() {
		assertEquals(14, TowerMobRules.Selection.values().length);
		assertSame(TowerMobRules.Selection.CHAIN_SHADOW_THIEF,
				TowerMobRules.select(13f / 14f));
	}

	@Test
	public void naturalTowerSpawnStartsWandering() {
		Mob thief = new ChainShadowThief();
		assertSame(thief.WANDERING, TowerMobRules.prepareNaturalSpawn(thief).state);
	}
}
