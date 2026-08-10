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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DeceptiveHeroTargetTest {

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
	public void normalAllySelectionSkipsDisguisedTargets() {
		TestAlly ally = allyAt(40);
		DisguisedEnemy disguised = enemyAt(new DisguisedEnemy(), 41);
		Mob ordinary = enemyAt(new TestEnemy(), 49);

		assertSame(ordinary, ally.chooseEnemyForTest());
		assertSame(disguised, Dungeon.level.findMob(disguised.pos));
	}

	@Test
	public void normalAllySelectionReturnsNullWhenOnlyTargetIsDisguised() {
		TestAlly ally = allyAt(40);
		enemyAt(new DisguisedEnemy(), 41);

		assertNull(ally.chooseEnemyForTest());
	}

	@Test
	public void directAggroCanKeepDisguisedTarget() {
		TestAlly ally = allyAt(40);
		DisguisedEnemy disguised = enemyAt(new DisguisedEnemy(), 41);
		ally.aggro(disguised);

		assertSame(disguised, ally.chooseEnemyForTest());
	}

	private static TestAlly allyAt(int pos) {
		TestAlly ally = new TestAlly();
		ally.HP = ally.HT = 1;
		ally.pos = pos;
		ally.state = ally.HUNTING;
		ally.fieldOfView = new boolean[Dungeon.level.length()];
		Arrays.fill(ally.fieldOfView, true);
		Actor.add(ally);
		Dungeon.level.mobs.add(ally);
		return ally;
	}

	private static <T extends Mob> T enemyAt(T enemy, int pos) {
		enemy.HP = enemy.HT = 1;
		enemy.pos = pos;
		enemy.alignment = Char.Alignment.ENEMY;
		enemy.state = enemy.HUNTING;
		Actor.add(enemy);
		Dungeon.level.mobs.add(enemy);
		return enemy;
	}

	private static TestLevel openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		level.mobs = new HashSet<>();
		return level;
	}

	private static class TestAlly extends Mob {

		private TestAlly() {
			alignment = Char.Alignment.ALLY;
		}

		private Char chooseEnemyForTest() {
			return chooseEnemy();
		}
	}

	private static class TestEnemy extends Mob {
	}

	private static class DisguisedEnemy extends Mob implements DeceptiveHeroTarget {
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
