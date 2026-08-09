package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

	@Test
	public void eachWarlockCanFlockTheSameTargetOnce() {
		TestWarlock first = new TestWarlock();
		TestWarlock second = new TestWarlock();
		Gnoll target = new Gnoll();

		assertTrue(first.needsFlockForTest(target));
		first.markFlockedForTest(target);
		assertFalse(first.needsFlockForTest(target));
		assertTrue(second.needsFlockForTest(target));
	}

	@Test
	public void flockRecordSurvivesSaveAndLoad() {
		TestWarlock original = new TestWarlock();
		Gnoll target = new Gnoll();
		original.markFlockedForTest(target);

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TestWarlock restored = new TestWarlock();
		restored.restoreFromBundle(bundle);

		assertFalse(restored.needsFlockForTest(target));
	}

	@Test
	public void flockUsesOnlyEmptyAdjacentNonPitCellsAndSixTurnSheep() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = openLevel(7, 7);
			Dungeon.level = level;

			TestWarlock warlock = new TestWarlock();
			Gnoll target = new Gnoll();
			target.pos = 24;
			level.pit[17] = true;
			level.solid[18] = true;

			assertEquals(6, warlock.flockForTest(target));
			assertEquals(6, warlock.spawnedCells.size());
			assertTrue(warlock.spawnedCells.stream().noneMatch(cell -> cell == 17 || cell == 18));
			assertTrue(warlock.spawnedLifespans.stream().allMatch(lifespan -> lifespan == 6f));
			assertFalse(warlock.needsFlockForTest(target));
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void blockedFlockStillRecordsTarget() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = openLevel(7, 7);
			Dungeon.level = level;
			TestWarlock warlock = new TestWarlock();
			Gnoll target = new Gnoll();
			target.pos = 24;
			for (int offset : com.watabou.utils.PathFinder.NEIGHBOURS8) {
				level.solid[target.pos + offset] = true;
			}

			assertEquals(0, warlock.flockForTest(target));
			assertFalse(warlock.needsFlockForTest(target));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void firstAttackOnlyFlocksAndDoesNotUsePhysicalAttack() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = openLevel(7, 7);
			TestWarlock warlock = new TestWarlock();
			warlock.pos = 23;
			Gnoll target = new Gnoll();
			target.pos = 24;

			assertTrue(warlock.attackTargetForTest(target));
			assertFalse(warlock.physicalAttackCalled);
			assertFalse(warlock.needsFlockForTest(target));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void fireblastMatchesFixedTwoChargeShapeAndDamage() {
		TestWarlock warlock = new TestWarlock();

		assertEquals(7, warlock.fireblastDistanceForTest());
		assertEquals(70, warlock.fireblastAngleForTest());
		assertEquals(3, warlock.fireVolumeForTest());
		for (int i = 0; i < 500; i++) {
			int damage = warlock.magicDamageForTest();
			assertTrue(damage >= 15 && damage <= 35);
		}
	}

	@Test
	public void fireblastAppliesMagicalDamageBurningAndFourTurnCripple() {
		TestWarlock warlock = new TestWarlock();
		TestTarget target = new TestTarget();

		warlock.applyFireblastToForTest(target);

		assertTrue(target.damageTaken >= 15 && target.damageTaken <= 35);
		assertTrue(Arrays.asList(target.damageTags).contains(DamageTag.MAGICAL));
		assertNotNull(target.buff(Burning.class));
		assertNotNull(target.buff(Cripple.class));
		assertEquals(4f, target.buff(Cripple.class).cooldown(), 0f);
	}

	@Test
	public void laterRangedAttackCastsFireblastAtMarkedTarget() {
		TestWarlock warlock = new TestWarlock();
		Gnoll target = new Gnoll();
		target.pos = 37;
		warlock.markFlockedForTest(target);

		assertTrue(warlock.rangedAttackForTest(target));
		assertEquals(37, warlock.fireblastTargetCell);
	}

	private static TestLevel openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		return level;
	}

	private static final class TestWarlock extends RoastLambWarlock {

		private final List<Integer> spawnedCells = new ArrayList<>();
		private final List<Float> spawnedLifespans = new ArrayList<>();
		private boolean physicalAttackCalled;
		private int fireblastTargetCell = -1;

		private boolean needsFlockForTest(Char target) {
			return needsFlock(target);
		}

		private void markFlockedForTest(Char target) {
			markFlocked(target);
		}

		private int flockForTest(Char target) {
			return performFlock(target);
		}

		private boolean attackTargetForTest(Char target) {
			return doAttack(target);
		}

		private int fireblastDistanceForTest() {
			return fireblastDistance();
		}

		private int fireblastAngleForTest() {
			return fireblastAngle();
		}

		private int fireVolumeForTest() {
			return fireVolume();
		}

		private int magicDamageForTest() {
			return magicDamageRoll();
		}

		private void applyFireblastToForTest(Char target) {
			applyFireblastTo(target);
		}

		private boolean rangedAttackForTest(Char target) {
			return doRangedAttack(target);
		}

		@Override
		public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
				DamageTag... damageTags) {
			physicalAttackCalled = true;
			return true;
		}

		@Override
		protected void spawnSheep(int cell, float lifespan) {
			spawnedCells.add(cell);
			spawnedLifespans.add(lifespan);
		}

		@Override
		protected void playFlockSounds() {
		}

		@Override
		protected void castFireblast(int targetCell) {
			fireblastTargetCell = targetCell;
		}
	}

	private static final class TestTarget extends Gnoll {

		private int damageTaken;
		private DamageTag[] damageTags = new DamageTag[0];

		@Override
		public void damage(int damage, Object source, DamageTag... tags) {
			damageTaken = damage;
			damageTags = tags;
		}

		@Override
		public float resist(Class effect) {
			return 1f;
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
