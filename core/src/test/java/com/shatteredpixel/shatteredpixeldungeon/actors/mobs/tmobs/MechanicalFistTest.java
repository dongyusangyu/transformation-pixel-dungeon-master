package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

	@Test
	public void knockbackBuildsThreeTileTrajectoryWithoutCollisionEffects() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = openLevel(11, 7);
			ThrowCapturingMechanicalFist fist = new ThrowCapturingMechanicalFist();
			Gnoll target = new Gnoll();
			fist.pos = 37;
			target.pos = 38;

			fist.knockBackForTest(target);

			assertSame(target, fist.thrownTarget);
			assertEquals(3, fist.distance);
			assertFalse(fist.closeDoors);
			assertFalse(fist.collideDamage);
			assertTrue(fist.trajectory.dist >= fist.distance);
			assertEquals(41, fist.trajectory.path.get(fist.distance).intValue());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	private static TestLevel openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		return level;
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

	private static final class ThrowCapturingMechanicalFist extends MechanicalFist {

		private Char thrownTarget;
		private Ballistica trajectory;
		private int distance;
		private boolean closeDoors;
		private boolean collideDamage;

		private void knockBackForTest(Char target) {
			knockBack(target);
		}

		@Override
		protected void throwTarget(
				Char target,
				Ballistica trajectory,
				int distance,
				boolean closeDoors,
				boolean collideDamage) {
			this.thrownTarget = target;
			this.trajectory = trajectory;
			this.distance = distance;
			this.closeDoors = closeDoors;
			this.collideDamage = collideDamage;
		}
	}

	private static final class TestLevel extends Level {

		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}
}
