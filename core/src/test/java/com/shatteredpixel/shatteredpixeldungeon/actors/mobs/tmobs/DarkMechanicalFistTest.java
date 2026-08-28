package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.util.Arrays;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DarkMechanicalFistTest {

	@Test
	public void inheritsMechanicalFistStatsAndAddsDemonicProperty() {
		DarkMechanicalFist dark = new DarkMechanicalFist();
		MechanicalFist base = new MechanicalFist();

		assertTrue(dark instanceof MechanicalFist);
		assertEquals(base.HT, dark.HT);
		assertEquals(base.defenseSkill, dark.defenseSkill);
		assertEquals(base.attackSkill(null), dark.attackSkill(null));
		assertEquals(base.EXP, dark.EXP);
		assertEquals(base.maxLvl, dark.maxLvl);
		assertTrue(dark.properties().contains(Char.Property.INORGANIC));
		assertTrue(dark.properties().contains(Char.Property.LARGE));
		assertTrue(dark.properties().contains(Char.Property.DEMONIC));
	}

	@Test
	public void laserDamageIsHalfOfTheNormalRollWithRoundedHalves() {
		DarkMechanicalFist dark = new DarkMechanicalFist();

		assertEquals(5, dark.laserDamageFrom(10));
		assertEquals(18, dark.laserDamageFrom(35));
	}

	@Test
	public void successfulMeleeProcAddsThreeTurnsOfBlindness() {
		TestDarkMechanicalFist dark = new TestDarkMechanicalFist();
		Gnoll target = new TestTarget();

		dark.attackProc(target, 12, DamageTag.PHYSICAL, DamageTag.MELEE);

		assertTrue(target.buff(Blindness.class) != null);
		assertEquals(3f, target.buff(Blindness.class).cooldown(), 0.001f);
	}

	@Test
	public void actionableTurnFiresOnceBeforePerformingNormalAction() {
		Level previous = Dungeon.level;
		try {
			Dungeon.level = openLevel(11, 7);
			TestDarkMechanicalFist dark = new TestDarkMechanicalFist();
			dark.state = dark.WANDERING;
			dark.forceMiniLaserTargetForTest();

			assertTrue(dark.actForTest());
			assertEquals(1, dark.laserCalls);
			assertEquals(1, dark.baseActCalls);
		} finally {
			Dungeon.level = previous;
		}
	}

	@Test
	public void sleepingTurnDoesNotFireTheFreeLaser() {
		Level previous = Dungeon.level;
		try {
			Dungeon.level = openLevel(11, 7);
			TestDarkMechanicalFist dark = new TestDarkMechanicalFist();
			dark.state = dark.SLEEPING;

			assertTrue(dark.actForTest());
			assertEquals(0, dark.laserCalls);
			assertEquals(1, dark.baseActCalls);
		} finally {
			Dungeon.level = previous;
		}
	}

	@Test
	public void invalidMapPositionDoesNotFireTheFreeLaser() {
		Level previous = Dungeon.level;
		try {
			Dungeon.level = openLevel(11, 7);
			TestDarkMechanicalFist dark = new TestDarkMechanicalFist();
			dark.pos = -1;
			dark.state = dark.WANDERING;

			assertTrue(dark.actForTest());
			assertEquals(0, dark.laserCalls);
			assertEquals(1, dark.baseActCalls);
		} finally {
			Dungeon.level = previous;
		}
	}

	@Test
	public void sameFactionEnemyIsNotChosenAsLaserTarget() {
		TestDarkMechanicalFist dark = new TestDarkMechanicalFist();
		Gnoll sameFactionEnemy = new TestTarget();

		assertFalse(dark.isMiniLaserHostileForTest(sameFactionEnemy));
	}

	@Test
	public void allyRemainsAValidLaserTargetForAnEnemyFist() {
		TestDarkMechanicalFist dark = new TestDarkMechanicalFist();
		Gnoll ally = new TestTarget();
		ally.alignment = Char.Alignment.ALLY;

		assertTrue(dark.isMiniLaserHostileForTest(ally));
	}

	@Test
	public void dropsOneMetalShardEveryTimeWithoutUsingWeaponDropDecay() throws Exception {
		int previousCount = Dungeon.LimitedDrops.DARK_MECHANICAL_FIST_WEAPON.count;
		try {
			DarkMechanicalFist dark = new DarkMechanicalFist();

			assertEquals(1f, dark.lootChance(), 0.000001f);
			assertEquals(MetalShard.class, lootDefinition(dark));
			assertEquals(previousCount, Dungeon.LimitedDrops.DARK_MECHANICAL_FIST_WEAPON.count);
		} finally {
			Dungeon.LimitedDrops.DARK_MECHANICAL_FIST_WEAPON.count = previousCount;
		}
	}

	private static Object lootDefinition(DarkMechanicalFist dark) throws Exception {
		Field loot = com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob.class
				.getDeclaredField("loot");
		loot.setAccessible(true);
		return loot.get(dark);
	}

	private static final class TestDarkMechanicalFist extends DarkMechanicalFist {
		private int laserCalls;
		private int baseActCalls;
		private boolean forceMiniLaserTarget;
		@Override
		protected int processBaseAttackProc(Char target, int damage, DamageTag... damageTags) {
			return damage;
		}

		@Override
		protected void knockBack(Char target) {
		}

		@Override
		protected Char chooseMiniLaserTarget() {
			return forceMiniLaserTarget ? new TestTarget() : super.chooseMiniLaserTarget();
		}

		@Override
		protected void fireMiniLaser(Char target) {
			laserCalls++;
		}

		@Override
		protected boolean performBaseAct() {
			baseActCalls++;
			return true;
		}

		private boolean actForTest() {
			return act();
		}

		private void forceMiniLaserTargetForTest() {
			forceMiniLaserTarget = true;
		}

		private boolean isMiniLaserHostileForTest(Char candidate) {
			return isMiniLaserHostile(candidate);
		}
	}

	private static Level openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		return level;
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

	private static final class TestTarget extends Gnoll {
		@Override
		public float resist(Class effect) {
			return 1f;
		}
	}
}
