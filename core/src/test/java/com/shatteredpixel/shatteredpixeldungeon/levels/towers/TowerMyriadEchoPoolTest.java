package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MyriadEcho;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MyriadBlackShadow;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Obscura;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.SoulCollector;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TwistedMirror;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerMyriadEchoPoolTest {

	@Test
	public void everyPoolEntryIsAFragileMarkedEnemyWithoutRecursiveSummoning() {
		for (int i = 0; i < 10; i++) {
			Mob echo = TowerMobRules.createMyriadEcho((i + 0.01f) / 10f);
			assertEquals(1, echo.HP);
			assertEquals(1, echo.HT);
			assertEquals(0, echo.EXP);
			assertTrue(MyriadEcho.isMarked(echo));
			assertFalse(echo instanceof AlienatedPrismaticGuard);
			assertFalse(echo instanceof MyriadBlackShadow);
			assertFalse(echo instanceof TwistedMirror);
			assertFalse(echo instanceof CorrosiveSwarm);
			assertFalse(echo instanceof Obscura);
			assertFalse(echo instanceof RoastLambWarlock);
			assertFalse(echo instanceof SoulCollector);
		}
	}
}
