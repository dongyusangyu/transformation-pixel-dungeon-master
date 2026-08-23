package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MobSleepingDetectionTest {

	private Level previousLevel;

	@Before
	public void setUp() {
		previousLevel = Dungeon.level;
		Actor.clear();
		Actor.resetNextID();
		Dungeon.level = openLevel(9, 9);
	}

	@After
	public void tearDown() {
		Actor.clear();
		Actor.resetNextID();
		Dungeon.level = previousLevel;
	}

	@Test
	public void adjacentVisibleTargetUsesCalculatedDetectionChance() {
		assertEquals(0.5f, Mob.sleepingDetectionChanceAtDistance(1, 0.5f, false, false), 0f);
	}

	@Test
	public void distantTargetKeepsItsCalculatedDetectionChance() {
		assertEquals(0.2f, Mob.sleepingDetectionChanceAtDistance(5, 0.2f, false, false), 0f);
	}

	@Test
	public void sleepingMobChoosesTheMostDetectableThreat() {
		assertTrue(Mob.shouldReplaceSleepingThreat(1f, 0.25f));
		assertFalse(Mob.shouldReplaceSleepingThreat(0.25f, 1f));
	}

	@Test
	public void detectionChanceIsClampedAndDistanceBasedExemptionsStillApply() {
		assertEquals(1f, Mob.sleepingDetectionChanceAtDistance(1, 5f, false, false), 0f);
		assertEquals(0f, Mob.sleepingDetectionChanceAtDistance(1, -1f, false, false), 0f);
		assertEquals(0f, Mob.sleepingDetectionChanceAtDistance(2, 1f, true, false), 0f);
		assertEquals(0f, Mob.sleepingDetectionChanceAtDistance(2, 1f, false, true), 0f);
	}

	@Test
	public void staleDroneTargetDoesNotBlockAdjacentHeroDetection() {
		TestMob mob = new TestMob();
		mob.pos = 40;
		mob.HP = mob.HT = 10;
		mob.fieldOfView = new boolean[Dungeon.level.length()];
		mob.fieldOfView[41] = true;
		Actor.add(mob);
		Dungeon.level.mobs.add(mob);

		TestAlly hero = new TestAlly();
		hero.pos = 41;
		hero.HP = hero.HT = 10;
		Actor.add(hero);
		Dungeon.level.mobs.add(hero);

		TestAlly staleDrone = new TestAlly();
		staleDrone.pos = 10;
		staleDrone.HP = staleDrone.HT = 10;
		staleDrone.flying = true;
		Actor.add(staleDrone);
		Dungeon.level.mobs.add(staleDrone);
		mob.enemy = staleDrone;

		mob.actSleeping(false);

		assertSame(mob.HUNTING, mob.state);
		assertSame(hero, mob.enemy);
		assertEquals(hero.pos, mob.target);
	}

	@Test
	public void neutralSleepingMobIgnoresNearbyUnitsWithoutInteraction() {
		TestMob mob = new TestMob();
		mob.alignment = Char.Alignment.NEUTRAL;
		mob.pos = 40;
		mob.HP = mob.HT = 10;
		mob.fieldOfView = new boolean[Dungeon.level.length()];
		mob.fieldOfView[41] = true;
		Actor.add(mob);
		Dungeon.level.mobs.add(mob);

		TestAlly nearbyAlly = new TestAlly();
		nearbyAlly.pos = 41;
		nearbyAlly.HP = nearbyAlly.HT = 10;
		Actor.add(nearbyAlly);
		Dungeon.level.mobs.add(nearbyAlly);

		mob.actSleeping(false);

		assertSame(mob.SLEEPING, mob.state);
		assertFalse(mob.enemySeen);
	}

	private static Level openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		level.mobs = new HashSet<>();
		return level;
	}

	private static class TestMob extends Mob {

		private boolean actSleeping(boolean enemyInFOV) {
			return SLEEPING.act(enemyInFOV, false);
		}

		@Override
		public void notice() {
		}
	}

	private static class TestAlly extends Mob {

		private TestAlly() {
			alignment = Char.Alignment.ALLY;
		}
	}

	private static class TestLevel extends Level {

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
