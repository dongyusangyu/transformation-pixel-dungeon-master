package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

	@Test
	public void directMeleeDamageGrowsStatsAndMaximumHealthUpToBaseDouble() {
		TestSerpent serpent = new TestSerpent();

		serpent.recordMeleeDamageForTest(50);

		assertEquals(340, serpent.HT);
		assertEquals(240, serpent.HP);
		assertEquals(1.5f, serpent.growthMultiplierForTest(), 0.0001f);
		assertEquals(1.5f, serpent.speed(), 0.0001f);
		assertEquals(1f / 3f, serpent.attackDelay(), 0.0001f);
		assertEquals(36, serpent.scaleDamageForTest(24));

		serpent.recordMeleeDamageForTest(1000);

		assertEquals(480, serpent.HT);
		assertEquals(240, serpent.HP);
		assertEquals(2f, serpent.growthMultiplierForTest(), 0.0001f);
		assertEquals(2f, serpent.speed(), 0.0001f);
		assertEquals(0.25f, serpent.attackDelay(), 0.0001f);
		assertEquals(48, serpent.scaleDamageForTest(24));
	}

	@Test
	public void regenerationUsesStandardTimeAndCurrentMaximumHealth() {
		TestSerpent serpent = new TestSerpent();
		serpent.HP = 100;

		serpent.advanceRegenerationForTest(0.99f);
		assertEquals(100, serpent.HP);

		serpent.advanceRegenerationForTest(0.01f);
		assertEquals(124, serpent.HP);

		serpent.recordMeleeDamageForTest(120);
		serpent.HP = 100;
		serpent.advanceRegenerationForTest(1f);
		assertEquals(148, serpent.HP);
	}

	@Test
	public void meleeGrowthAndRegenerationProgressSurviveSaveAndLoad() {
		TestSerpent original = new TestSerpent();
		original.recordMeleeDamageForTest(60);
		original.HP = 111;
		original.advanceRegenerationForTest(0.5f);

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TestSerpent restored = new TestSerpent();
		restored.restoreFromBundle(bundle);

		assertEquals(360, restored.HT);
		assertEquals(111, restored.HP);
		assertEquals(1.6f, restored.growthMultiplierForTest(), 0.0001f);

		restored.advanceRegenerationForTest(0.5f);
		assertEquals(147, restored.HP);
	}

	@Test
	public void qualifyingMeleeDamagePoisonsRetaliationTargetForActualHealthLost() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = openLevel(7, 7);
			Dungeon.level = level;

			TestSerpent serpent = new TestSerpent();
			serpent.pos = 24;
			Gnoll target = new Gnoll();
			target.pos = 10;
			serpent.retaliationTarget = target;
			Gnoll adjacentAttacker = new Gnoll();
			adjacentAttacker.pos = 25;

			serpent.damage(17, adjacentAttacker, DamageTag.UNAVOIDABLE);

			assertEquals(223, serpent.HP);
			assertEquals("17", target.buff(Poison.class).iconTextDisplay());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void sourceClassificationAcceptsMeleeWeaponsAndAdjacentMonstersOnly() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = openLevel(7, 7);
			Dungeon.level = level;

			TestSerpent serpent = new TestSerpent();
			serpent.pos = 24;
			Gnoll target = new Gnoll();
			target.pos = 10;
			serpent.retaliationTarget = target;
			Gnoll adjacentMob = new Gnoll();
			adjacentMob.pos = 25;
			Gnoll remoteMob = new Gnoll();
			remoteMob.pos = 1;

			assertTrue(serpent.isMeleeWeaponTypeForTest(Shortsword.class));
			assertFalse(serpent.isMeleeWeaponTypeForTest(ThrowingStone.class));
			assertTrue(serpent.qualifiesForTest(adjacentMob, target));
			assertFalse(serpent.qualifiesForTest(remoteMob, target));

			serpent.damage(0, adjacentMob, DamageTag.UNAVOIDABLE);
			assertTrue(target.buff(Poison.class) == null);
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void auraAndRangedSpitSeedCorrosiveGasAtApprovedStrengths() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = openLevel(11, 11);
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			TestSerpent serpent = new TestSerpent();
			serpent.pos = 60;
			level.passable[49] = false;
			level.solid[49] = true;

			serpent.emitAuraForTest();

			CorrosiveGas gas = (CorrosiveGas) level.blobs.get(CorrosiveGas.class);
			assertNotNull(gas);
			assertTrue(Blob.volumeAt(60, CorrosiveGas.class) > 0);
			assertTrue(Blob.volumeAt(59, CorrosiveGas.class) > 0);
			assertEquals(0, Blob.volumeAt(49, CorrosiveGas.class));
			assertEquals(1, storedStrength(gas));

			serpent.spitAtForTest(82);

			assertTrue(Blob.volumeAt(82, CorrosiveGas.class) > 0);
			assertTrue(Blob.volumeAt(81, CorrosiveGas.class) > 0);
			assertEquals(8, storedStrength(gas));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void pullOnlyWorksAtThreeOrFourCellsWithClearPathAndOpenLanding() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = openLevel(11, 11);
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			TestSerpent serpent = new TestSerpent();
			serpent.pos = 60;
			Gnoll target = new Gnoll();

			target.pos = 62;
			assertFalse(serpent.canPullForTest(target));

			target.pos = 63;
			assertTrue(serpent.canPullForTest(target));

			target.pos = 64;
			assertTrue(serpent.canPullForTest(target));

			target.pos = 65;
			assertFalse(serpent.canPullForTest(target));

			target.pos = 63;
			level.solid[62] = true;
			level.losBlocking[62] = true;
			assertFalse(serpent.canPullForTest(target));

			level.solid[62] = false;
			level.losBlocking[62] = false;
			for (int offset : com.watabou.utils.PathFinder.NEIGHBOURS8) {
				level.passable[serpent.pos + offset] = false;
			}
			assertFalse(serpent.canPullForTest(target));
			assertFalse(serpent.pullWasUsedForTest());
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void successfulPullMarksFixedLandingAndExplodesThereOnNextResolution() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = openLevel(11, 11);
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			TestSerpent serpent = new TestSerpent();
			serpent.pos = 60;
			Gnoll target = new Gnoll();
			target.pos = 63;

			assertTrue(serpent.pullForTest(target));
			int landing = target.pos;
			assertTrue(level.adjacent(serpent.pos, landing));
			assertTrue(serpent.pullWasUsedForTest());
			assertEquals(landing, serpent.pendingExplosionForTest());

			target.pos = 60;
			assertTrue(serpent.resolveExplosionForTest());
			assertEquals(landing, serpent.explodedCell);
			assertEquals(-1, serpent.pendingExplosionForTest());
			assertFalse(serpent.pullForTest(target));
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void pullAndPendingExplosionStateSurviveSaveAndLoad() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = openLevel(11, 11);
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			TestSerpent original = new TestSerpent();
			original.pos = 60;
			Gnoll target = new Gnoll();
			target.pos = 63;
			assertTrue(original.pullForTest(target));
			int landing = original.pendingExplosionForTest();

			Bundle bundle = new Bundle();
			original.storeInBundle(bundle);

			TestSerpent restored = new TestSerpent();
			restored.restoreFromBundle(bundle);
			assertTrue(restored.pullWasUsedForTest());
			assertEquals(landing, restored.pendingExplosionForTest());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void ownerBombDamageIsZeroOnlyForMatchingSerpentId() {
		TestSerpent owner = new TestSerpent();
		TestSerpent other = new TestSerpent();

		assertEquals(0, owner.normalizeBombDamageForTest(10, owner.id()));
		assertEquals(10, other.normalizeBombDamageForTest(10, owner.id()));
	}

	private static int storedStrength(CorrosiveGas gas) {
		Bundle bundle = new Bundle();
		gas.storeInBundle(bundle);
		return bundle.getInt("strength");
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

		private Char retaliationTarget;
		private int explodedCell = -1;

		private boolean canStrike(Char target) {
			return canMeleeAttack(target);
		}

		private void recordMeleeDamageForTest(int damage) {
			recordMeleeDamage(damage);
		}

		private float growthMultiplierForTest() {
			return growthMultiplier();
		}

		private int scaleDamageForTest(int damage) {
			return scaleMeleeDamage(damage);
		}

		private void advanceRegenerationForTest(float elapsed) {
			advanceRegeneration(elapsed);
		}

		private boolean qualifiesForTest(Object source, Char target) {
			return isQualifyingMeleeSource(source, target);
		}

		private boolean isMeleeWeaponTypeForTest(Class<?> weaponType) {
			return isMeleeWeaponType(weaponType);
		}

		private int normalizeBombDamageForTest(int damage, int ownerId) {
			return normalizeBombDamage(damage, ownerId);
		}

		private void emitAuraForTest() {
			emitAura();
		}

		private void spitAtForTest(int cell) {
			spitAt(cell);
		}

		private boolean canPullForTest(Char target) {
			return canPull(target);
		}

		private boolean pullForTest(Char target) {
			return performPull(target);
		}

		private boolean pullWasUsedForTest() {
			return pullUsed();
		}

		private int pendingExplosionForTest() {
			return pendingExplosionCell();
		}

		private boolean resolveExplosionForTest() {
			return resolvePendingExplosion();
		}

		@Override
		protected void explodeAt(int cell) {
			explodedCell = cell;
		}

		@Override
		protected Char retaliationTarget() {
			return retaliationTarget;
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
