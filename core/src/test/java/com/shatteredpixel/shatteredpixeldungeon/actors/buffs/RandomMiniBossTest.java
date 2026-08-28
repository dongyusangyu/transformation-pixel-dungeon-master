package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalWisp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

	@Test
	public void freshMiniBossBoostIsRevertedWhenBuffDetaches() {
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			Dungeon.depth = 5;
			Dungeon.branch = 0;
			Rat rat = ratWithHealth(20);

			ChampionEnemy.RandomMiniBoss buff =
					Buff.affect(rat, ChampionEnemy.RandomMiniBoss.class);
			assertEquals(35, rat.HT);
			assertEquals(35, rat.HP);

			buff.detach();
			assertEquals(20, rat.HT);
			assertEquals(20, rat.HP);
		} finally {
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
		}
	}

	@Test
	public void restoredMiniBossDoesNotApplyHealthBoostAgain() {
		int oldDepth = Dungeon.depth;
		int oldBranch = Dungeon.branch;
		try {
			Dungeon.depth = 5;
			Dungeon.branch = 0;
			Rat original = ratWithHealth(20);
			ChampionEnemy.RandomMiniBoss originalBuff =
					Buff.affect(original, ChampionEnemy.RandomMiniBoss.class);
			Bundle bundle = new Bundle();
			originalBuff.storeInBundle(bundle);

			Rat restored = ratWithHealth(35);
			ChampionEnemy.RandomMiniBoss restoredBuff =
					new ChampionEnemy.RandomMiniBoss();
			restoredBuff.restoreFromBundle(bundle);

			assertTrue(restoredBuff.attachTo(restored));
			assertEquals(35, restored.HT);
			assertEquals(35, restored.HP);

			restoredBuff.detach();
			assertEquals(20, restored.HT);
			assertEquals(20, restored.HP);
		} finally {
			Dungeon.depth = oldDepth;
			Dungeon.branch = oldBranch;
		}
	}

	private static Rat ratWithHealth(int health) {
		Rat rat = new Rat();
		rat.HT = health;
		rat.HP = health;
		return rat;
	}
}
