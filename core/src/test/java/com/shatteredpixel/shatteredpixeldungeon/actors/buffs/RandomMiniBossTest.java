package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalWisp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RandomMiniBossTest {

	@Test
	public void miningQuestMobsCannotBecomeRandomModeMiniBosses() {
		assertFalse(ChampionEnemy.RandomMiniBoss.validTarget(new GnollGuard(), true));
		assertFalse(ChampionEnemy.RandomMiniBoss.validTarget(new CrystalWisp(), true));
	}

	@Test
	public void miningLevelStillAllowsOtherValidEnemies() {
		assertTrue(ChampionEnemy.RandomMiniBoss.validTarget(new Rat(), true));
	}

	@Test
	public void questMobTypesRemainEligibleOutsideMiningLevel() {
		assertTrue(ChampionEnemy.RandomMiniBoss.validTarget(new GnollGuard(), false));
		assertTrue(ChampionEnemy.RandomMiniBoss.validTarget(new CrystalWisp(), false));
	}
}
