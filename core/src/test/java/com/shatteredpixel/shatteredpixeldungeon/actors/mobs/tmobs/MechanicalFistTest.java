package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MechanicalFistTest {

	@Test
	public void baseStatsMatchSpecification() {
		MechanicalFist fist = new MechanicalFist();

		assertEquals(250, fist.HT);
		assertEquals(250, fist.HP);
		assertEquals(20, fist.defenseSkill);
		assertEquals(40, fist.attackSkill(null));
		assertEquals(13, fist.EXP);
		assertEquals(30, fist.maxLvl);
		assertEquals(0f, fist.lootChance(), 0f);
	}

	@Test
	public void damageAndArmorStayInsideSpecifiedRanges() {
		MechanicalFist fist = new MechanicalFist();

		for (int i = 0; i < 500; i++) {
			int damage = fist.damageRoll();
			int armor = fist.drRoll();
			assertTrue(damage >= 10 && damage <= 35);
			assertTrue(armor >= 30 && armor <= 70);
		}
	}

	@Test
	public void everySuccessfulAttackProcRequestsKnockbackEvenAtZeroDamage() {
		TestMechanicalFist fist = new TestMechanicalFist();
		Gnoll target = new Gnoll();

		assertEquals(0, fist.attackProc(target, 0));
		assertEquals(1, fist.knockbackCalls);
		assertSame(target, fist.knockbackTarget);
	}

	@Test
	public void knockbackAimExtendsThreeGridVectorsAwayFromAttacker() {
		TestMechanicalFist fist = new TestMechanicalFist();
		Gnoll target = new Gnoll();
		fist.pos = 24;
		target.pos = 25;

		assertEquals(28, fist.knockbackAimForTest(target));

		fist.pos = 24;
		target.pos = 32;
		assertEquals(56, fist.knockbackAimForTest(target));
	}

	private static final class TestMechanicalFist extends MechanicalFist {

		private int knockbackCalls;
		private Char knockbackTarget;

		@Override
		protected int processBaseAttackProc(
				Char target, int damage, DamageTag... damageTags) {
			return damage;
		}

		@Override
		protected void knockBack(Char target) {
			knockbackCalls++;
			knockbackTarget = target;
		}

		private int knockbackAimForTest(Char target) {
			return knockbackAim(target);
		}
	}
}
