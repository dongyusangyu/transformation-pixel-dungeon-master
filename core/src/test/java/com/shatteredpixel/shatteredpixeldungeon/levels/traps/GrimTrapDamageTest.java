package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GrimTrapDamageTest {

	@Test
	public void mixesCurrentAndMaximumHealthForOrdinaryTargets() {
		Gnoll target = new Gnoll();
		target.HT = 120;
		target.HP = 40;

		assertEquals(80, GrimTrap.grimDamage(target));
	}

	@Test
	public void capsHeroDamageAtNinetyPercentMaximumHealth() {
		assertEquals(90, GrimTrap.grimDamage(100, 100, true));
	}

	@Test
	public void heroCapDoesNotChangeLowerMixedDamage() {
		assertEquals(60, GrimTrap.grimDamage(100, 20, true));
	}
}
