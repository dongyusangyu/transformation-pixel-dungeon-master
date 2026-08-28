package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MyriadEchoTest {

	@Test
	public void markerNormalizesOneHpAndEnemyAlignment() {
		CamouflageGnoll mob = new CamouflageGnoll();
		mob.HP = mob.HT = 100;
		mob.EXP = 13;
		mob.alignment = Char.Alignment.ALLY;

		MyriadEcho marker = Buff.affect(mob, MyriadEcho.class);

		assertTrue(MyriadEcho.isMarked(mob));
		assertEquals(1, mob.HT);
		assertEquals(1, mob.HP);
		assertEquals(0, mob.EXP);
		assertEquals(Char.Alignment.ENEMY, mob.alignment);
		assertFalse(AllyBuff.isConversionRewardEligible(mob));
		assertTrue(marker.act());
	}

	@Test
	public void normalizingDeadEchoDoesNotReviveIt() {
		CamouflageGnoll mob = new CamouflageGnoll();
		Buff.affect(mob, MyriadEcho.class);
		mob.HP = 0;
		MyriadEcho.normalize(mob);
		assertEquals(0, mob.HP);
		assertEquals(1, mob.HT);
	}
}
