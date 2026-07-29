package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EarthlySerpentTest {

	@Test
	public void baseStatsMatchSpecification() {
		TestSerpent serpent = new TestSerpent();

		assertEquals(240, serpent.HT);
		assertEquals(240, serpent.HP);
		assertEquals(20, serpent.defenseSkill);
		assertEquals(40, serpent.attackSkill(null));
		assertEquals(1f, serpent.speed(), 0f);
		assertEquals(0.5f, serpent.attackDelay(), 0f);
		assertEquals(13, serpent.EXP);
		assertEquals(30, serpent.maxLvl);
	}

	@Test
	public void damageAndArmorStayInsideBaseRanges() {
		TestSerpent serpent = new TestSerpent();

		for (int i = 0; i < 500; i++) {
			int damage = serpent.damageRoll();
			int armor = serpent.drRoll();
			assertTrue(damage >= 24 && damage <= 36);
			assertTrue(armor >= 8 && armor <= 16);
		}
	}

	@Test
	public void meleeReachIsTwoCellsAndCannotPassThroughWalls() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = openLevel(7, 7);
			Dungeon.level = level;

			TestSerpent serpent = new TestSerpent();
			serpent.pos = 24;
			Gnoll target = new Gnoll();

			target.pos = 25;
			assertTrue(serpent.canStrike(target));

			target.pos = 26;
			assertTrue(serpent.canStrike(target));

			target.pos = 27;
			assertFalse(serpent.canStrike(target));

			target.pos = 26;
			level.solid[25] = true;
			level.losBlocking[25] = true;
			assertFalse(serpent.canStrike(target));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	private static TestLevel openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		Arrays.fill(level.losBlocking, false);
		return level;
	}

	private static final class TestSerpent extends EarthlySerpent {

		private boolean canStrike(Char target) {
			return canMeleeAttack(target);
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
