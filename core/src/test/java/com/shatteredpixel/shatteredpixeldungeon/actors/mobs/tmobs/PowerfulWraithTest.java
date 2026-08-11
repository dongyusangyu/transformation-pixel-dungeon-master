package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PowerfulWraithTest {

	@Test
	public void keepsWraithOffenseWithFixedHealthAndNoRewards() {
		TestWraith wraith = new TestWraith();
		wraith.adjustStats(10);

		assertEquals(100, wraith.HT);
		assertEquals(100, wraith.HP);
		assertEquals(20, wraith.attackSkill(new Char() { }));
		for (int i = 0; i < 100; i++) {
			int damage = wraith.damageRoll();
			assertEquals(true, damage >= 6 && damage <= 12);
		}
		assertEquals(0, wraith.EXP);
		assertEquals(30, wraith.maxLvl);
		assertEquals(0f, wraith.lootChance(), 0f);
	}

	@Test
	public void healsTenEachOwnTurnAndCapsAtMaximum() {
		TestWraith wraith = new TestWraith();
		wraith.HP = 85;

		wraith.actForTest();
		wraith.actForTest();

		assertEquals(100, wraith.HP);
		assertEquals(2, wraith.baseActCalls);
	}

	@Test
	public void movementAndAttackSpeedAreBothTwo() {
		TestWraith wraith = new TestWraith();

		assertEquals(2f, wraith.speed(), 0.0001f);
		assertEquals(0.5f, wraith.attackDelay(), 0.0001f);
	}

	@Test
	public void successfulRollKnocksHeroOneCellWithoutCollisionDamage() {
		TestWraith wraith = new TestWraith();
		Char target = new Char() { };
		wraith.heroTarget = true;
		wraith.proc = true;

		assertEquals(7, wraith.attackProc(target, 7, DamageTag.MELEE));

		assertEquals(1, wraith.knockbacks);
		assertEquals(1, wraith.lastDistance);
	}

	@Test
	public void failedRollOrNonHeroNeverKnocksBack() {
		TestWraith wraith = new TestWraith();
		Char target = new Char() { };
		wraith.heroTarget = true;
		wraith.proc = false;
		wraith.attackProc(target, 7, DamageTag.MELEE);
		wraith.heroTarget = false;
		wraith.proc = true;
		wraith.attackProc(target, 7, DamageTag.MELEE);

		assertEquals(0, wraith.knockbacks);
	}

	private static class TestWraith extends PowerfulWraith {
		boolean heroTarget;
		boolean proc;
		int knockbacks;
		int lastDistance;
		int baseActCalls;

		@Override
		protected int processBaseAttackProc(Char target, int damage, DamageTag... tags) {
			return damage;
		}

		@Override
		protected boolean isHeroTarget(Char target) {
			return heroTarget;
		}

		@Override
		protected boolean rollKnockback() {
			return proc;
		}

		@Override
		protected void knockBack(Char target, int distance) {
			knockbacks++;
			lastDistance = distance;
		}

		@Override
		protected boolean performBaseAct() {
			baseActCalls++;
			return true;
		}

		boolean actForTest() {
			return act();
		}
	}
}
