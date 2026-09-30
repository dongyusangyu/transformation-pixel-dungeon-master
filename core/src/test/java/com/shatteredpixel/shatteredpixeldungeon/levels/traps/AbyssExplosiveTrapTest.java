package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.SparseArray;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AbyssExplosiveTrapTest {

	@Test
	public void onlyAbyssExplosiveTrapPreservesTerrainAndTriggersOnEntry() {
		Trap abyssTrap = new AbyssExplosiveTrap();
		Trap regularTrap = new ExplosiveTrap();

		assertTrue(abyssTrap.preservesTerrain());
		assertTrue(abyssTrap.triggersOnEntry());
		assertFalse(regularTrap.preservesTerrain());
		assertFalse(regularTrap.triggersOnEntry());
	}

	@Test
	public void abyssTrapOnlyAllowsAbyssPlacement() {
		Trap abyssTrap = new AbyssExplosiveTrap();

		assertTrue(abyssTrap.canPlaceOnTerrain(Terrain.CHASM));
		assertFalse(abyssTrap.canPlaceOnTerrain(Terrain.EMPTY));
	}

	@Test
	public void revealingAbyssTrapKeepsItOnTheChasm() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = levelWithChasm(12);
			Dungeon.level = level;
			AbyssExplosiveTrap trap = new AbyssExplosiveTrap();
			level.setTrap(trap.hide(), 12);

			level.discover(12);

			assertEquals(Terrain.CHASM, level.map[12]);
			assertSame(trap, level.traps.get(12));
			assertTrue(trap.visible);
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void disarmingAbyssTrapRemovesOnlyTheTrap() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = levelWithChasm(12);
			Dungeon.level = level;
			AbyssExplosiveTrap trap = new AbyssExplosiveTrap();
			level.setTrap(trap.reveal(), 12);

			trap.disarm();

			assertEquals(Terrain.CHASM, level.map[12]);
			assertNull(level.traps.get(12));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void hiddenAbyssTrapIsDiscoverableWithoutSecretTerrain() {
		TestLevel level = levelWithChasm(12);
		level.buildFlagMaps();
		AbyssExplosiveTrap trap = new AbyssExplosiveTrap();
		level.setTrap(trap.hide(), 12);

		assertFalse(level.secret[12]);
		assertTrue(level.hiddenTrapAt(12));
		trap.reveal();
		assertFalse(level.hiddenTrapAt(12));
	}

	@Test
	public void floatingCharactersTriggerAbyssTrapOnEntry() {
		assertEntryTriggers(true);
	}

	@Test
	public void groundedCharactersTriggerAbyssTrapBeforePitHandling() {
		assertEntryTriggers(false);
	}

	@Test
	public void hiddenAbyssTrapDoesNotTriggerOnMonsterEntry() {
		assertMonsterEntryTriggers(false, 0);
	}

	@Test
	public void revealedAbyssTrapTriggersOnForcedMonsterEntry() {
		assertMonsterEntryTriggers(true, 1);
	}

	@Test
	public void hiddenAbyssTrapDoesNotBlockFlyingMonsterPath() {
		assertFlyingMonsterPathing(false, true);
	}

	@Test
	public void revealedAbyssTrapBlocksFlyingMonsterPath() {
		assertFlyingMonsterPathing(true, false);
	}

	@Test
	public void oneThirdLevitationRemovalOnlyAppliesToAbyssTrapAffectedCharacters() {
		assertTrue(AbyssExplosiveTrap.shouldRemoveLevitation(0));
		assertFalse(AbyssExplosiveTrap.shouldRemoveLevitation(1));
		assertFalse(AbyssExplosiveTrap.shouldRemoveLevitation(2));
	}

	@Test
	public void revealingAbyssTrapInvalidatesCachedMonsterStep() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = corridorLevel();
			level.map[14] = Terrain.CHASM;
			level.buildFlagMaps();
			Dungeon.level = level;
			AbyssExplosiveTrap trap = new AbyssExplosiveTrap();
			level.setTrap(trap.hide(), 13);

			PathingMob mob = new PathingMob();
			mob.pos = 11;
			mob.flying = true;
			mob.HT = mob.HP = 10;

			assertTrue(mob.stepToward(14));
			assertEquals(12, mob.pos);
			trap.reveal();
			assertFalse(mob.stepToward(14));
			assertEquals(12, mob.pos);
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	private static void assertFlyingMonsterPathing(boolean visible, boolean expectedMove) {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = corridorLevel();
			Dungeon.level = level;
			AbyssExplosiveTrap trap = new AbyssExplosiveTrap();
			level.setTrap(visible ? trap.reveal() : trap.hide(), 12);

			PathingMob mob = new PathingMob();
			mob.pos = 11;
			mob.flying = true;
			mob.HT = mob.HP = 10;

			assertEquals(expectedMove, mob.stepToward(13));
			assertEquals(expectedMove ? 12 : 11, mob.pos);
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	private static void assertEntryTriggers(boolean flying) {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		try {
			TestLevel level = levelWithChasm(12);
			level.buildFlagMaps();
			Dungeon.level = level;
			Dungeon.hero = TestHeroFactory.create();
			RecordingAbyssTrap trap = new RecordingAbyssTrap();
			level.setTrap(trap.reveal(), 12);

			TestChar character = new TestChar();
			character.pos = 12;
			character.HT = character.HP = 10;
			character.flying = flying;
			level.occupyCell(character);

			assertEquals(1, trap.triggerCount);
		} finally {
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	private static void assertMonsterEntryTriggers(boolean visible, int expectedTriggers) {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		try {
			TestLevel level = levelWithChasm(12);
			level.buildFlagMaps();
			Dungeon.level = level;
			Dungeon.hero = TestHeroFactory.create();
			RecordingAbyssTrap trap = new RecordingAbyssTrap();
			level.setTrap(visible ? trap.reveal() : trap.hide(), 12);

			PathingMob mob = new PathingMob();
			mob.pos = 12;
			mob.flying = true;
			mob.HT = mob.HP = 10;
			level.occupyCell(mob);

			assertEquals(expectedTriggers, trap.triggerCount);
		} finally {
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	private static TestLevel levelWithChasm(int cell) {
		TestLevel level = new TestLevel();
		level.setSize(5, 5);
		level.traps = new SparseArray<>();
		level.plants = new SparseArray<>();
		level.blobs = new HashMap<>();
		level.map[cell] = Terrain.CHASM;
		return level;
	}

	private static TestLevel corridorLevel() {
		TestLevel level = new TestLevel();
		level.setSize(5, 5);
		level.traps = new SparseArray<>();
		level.plants = new SparseArray<>();
		level.blobs = new HashMap<>();
		for (int i = 0; i < level.length(); i++) {
			level.map[i] = Terrain.WALL;
		}
		level.map[11] = Terrain.CHASM;
		level.map[12] = Terrain.CHASM;
		level.map[13] = Terrain.CHASM;
		level.buildFlagMaps();
		return level;
	}

	private static class RecordingAbyssTrap extends AbyssExplosiveTrap {
		int triggerCount;

		@Override
		public void trigger() {
			triggerCount++;
		}
	}

	private static class TestChar extends Char {
		@Override protected boolean act() { return true; }
	}

	private static class PathingMob extends Mob {
		boolean stepToward(int target) {
			return getCloser(target);
		}

		@Override
		public void move(int step, boolean travelling) {
			pos = step;
		}
	}

	private static class TestLevel extends Level {
		@Override protected boolean build() { return true; }
		@Override protected void createMobs() {}
		@Override protected void createItems() {}
	}
}
