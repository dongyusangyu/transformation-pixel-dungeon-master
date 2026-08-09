package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RangedAttack;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoastLambWarlockTest {

	@Test
	public void baseStatsMatchSpecification() {
		RoastLambWarlock warlock = new RoastLambWarlock();

		assertEquals(150, warlock.HT);
		assertEquals(150, warlock.HP);
		assertEquals(20, warlock.defenseSkill);
		assertEquals(40, warlock.attackSkill(null));
		assertEquals(13, warlock.EXP);
		assertEquals(30, warlock.maxLvl);
		assertEquals(RangedAttack.Type.RANGED_MAGIC, warlock.rangedAttackType());
		assertEquals(0f, warlock.lootChance(), 0f);
	}

	@Test
	public void meleeDamageAndArmorStayInsideRanges() {
		RoastLambWarlock warlock = new RoastLambWarlock();

		for (int i = 0; i < 500; i++) {
			int damage = warlock.damageRoll();
			int armor = warlock.drRoll();
			assertTrue(damage >= 0 && damage <= 20);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}
}
