package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThief;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DeathButterfly;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TowerChainShadowThiefGenerationTest {

	@Test
	public void chainShadowThiefIsAnEquallyWeightedTowerSelection() {
		assertEquals(15, TowerMobRules.Selection.values().length);
		assertSame(TowerMobRules.Selection.CHAIN_SHADOW_THIEF,
				TowerMobRules.select(13f / 15f));
		assertSame(TowerMobRules.Selection.DEATH_BUTTERFLY,
				TowerMobRules.select(14f / 15f));
	}

	@Test
	public void naturalTowerSpawnStartsWandering() {
		Mob thief = new ChainShadowThief();
		assertSame(thief.WANDERING, TowerMobRules.prepareNaturalSpawn(thief).state);
	}

	@Test
	public void deathButterflyNaturalSpawnStartsWandering() {
		Mob butterfly = new DeathButterfly();
		assertSame(butterfly.WANDERING, TowerMobRules.prepareNaturalSpawn(butterfly).state);
	}
}
