package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFlock;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RoastLambWarlockTest {

	@Test
	public void mutuallyExclusiveLootKeepsStoneChanceAndHalvesWandChanceAfterDrops() {
		int previousCount = Dungeon.LimitedDrops.ROAST_LAMB_WAND.count;
		try {
			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = 0;
			LootWarlock wandWarlock = new LootWarlock(0f);
			assertEquals(1f / 20f + 1f / 6f, wandWarlock.lootChance(), 0.000001f);
			wandWarlock.createLoot();
			assertSame(WandOfFireblast.class, wandWarlock.selectedLoot);
			assertEquals(1, Dungeon.LimitedDrops.ROAST_LAMB_WAND.count);
			assertEquals(1f / 40f + 1f / 6f, wandWarlock.lootChance(), 0.000001f);

			LootWarlock stoneWarlock = new LootWarlock(1f);
			stoneWarlock.createLoot();
			assertSame(StoneOfFlock.class, stoneWarlock.selectedLoot);
			assertEquals(1, Dungeon.LimitedDrops.ROAST_LAMB_WAND.count);
		} finally {
			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = previousCount;
		}
	}

	@Test
	public void wandDropCountSurvivesLimitedDropsSaveAndLoad() {
		Bundle previousDrops = new Bundle();
		Dungeon.LimitedDrops.store(previousDrops);
		try {
			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = 3;
			Bundle savedDrops = new Bundle();
			Dungeon.LimitedDrops.store(savedDrops);

			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = 0;
			Dungeon.LimitedDrops.restore(savedDrops);

			assertEquals(3, Dungeon.LimitedDrops.ROAST_LAMB_WAND.count);
			LootWarlock warlock = new LootWarlock(1f);
			assertEquals((1f / 20f) / 8f + 1f / 6f,
					warlock.lootChance(), 0.000001f);
		} finally {
			Dungeon.LimitedDrops.restore(previousDrops);
		}
	}

	@Test
	public void failedWandCreationDoesNotReduceFutureDropChance() {
		int previousCount = Dungeon.LimitedDrops.ROAST_LAMB_WAND.count;
		try {
			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = 0;
			LootWarlock warlock = new LootWarlock(0f, true);

			assertNull(warlock.createLoot());
			assertEquals(0, Dungeon.LimitedDrops.ROAST_LAMB_WAND.count);
			assertEquals(1f / 20f + 1f / 6f, warlock.lootChance(), 0.000001f);
		} finally {
			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = previousCount;
		}
	}

	@Test
	public void wandSelectionChangesAtExactConditionalProbability() {
		int previousCount = Dungeon.LimitedDrops.ROAST_LAMB_WAND.count;
		try {
			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = 0;
			float threshold = (1f / 20f) / (1f / 20f + 1f / 6f);
			LootWarlock below = new LootWarlock(threshold - 0.000001f);
			below.createLoot();
			assertSame(WandOfFireblast.class, below.selectedLoot);

			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = 0;
			LootWarlock above = new LootWarlock(threshold + 0.000001f);
			above.createLoot();
			assertSame(StoneOfFlock.class, above.selectedLoot);
		} finally {
			Dungeon.LimitedDrops.ROAST_LAMB_WAND.count = previousCount;
		}
	}

	@Test
	public void baseStatsMatchSpecification() {
		TestWarlock warlock = new TestWarlock();

		assertEquals(150, warlock.HT);
		assertEquals(150, warlock.HP);
		assertEquals(20, warlock.defenseSkill);
		assertEquals(40, warlock.attackSkill(null));
		assertEquals(13, warlock.EXP);
		assertEquals(30, warlock.maxLvl);
		assertEquals(RangedAttack.Type.RANGED_MAGIC, warlock.rangedAttackType());
		assertEquals(1f / 20f + 1f / 6f, warlock.lootChance(), 0.000001f);
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
	public void visibleTargetIsFlockedOnceBeforeRepeatedFireblasts() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = openLevel(15, 9);
			TestWarlock warlock = new TestWarlock();
			warlock.pos = 64;
			Gnoll target = new Gnoll();
			target.pos = 68;
			warlock.setVisibleForTest(target.pos);

			assertTrue(warlock.attackTargetForTest(target));
			assertEquals(-1, warlock.fireblastTargetCell);
			assertEquals(0, warlock.fireblastCastCount);
			assertFalse(warlock.needsFlockForTest(target));
			assertFalse(warlock.physicalAttackCalled);

			assertTrue(warlock.attackTargetForTest(target));
			assertEquals(target.pos, warlock.fireblastTargetCell);
			assertEquals(1, warlock.fireblastCastCount);
			assertFalse(warlock.needsFlockForTest(target));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void flockedVisibleTargetInRangeIgnoresBlockedBallistica() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			Dungeon.level = openLevel(15, 9);
			TestWarlock warlock = new TestWarlock();
			warlock.pos = 64;
			Gnoll target = new Gnoll();
			target.pos = 68;
			Sheep blocker = new Sheep();
			blocker.pos = 65;
			Actor.add(warlock);
			Actor.add(target);
			Actor.add(blocker);
			warlock.markFlockedForTest(target);
			warlock.setVisibleForTest(target.pos);

			assertFalse(new Ballistica(warlock.pos, target.pos,
					Ballistica.MAGIC_BOLT).collisionPos == target.pos);
			assertTrue(warlock.canRangedAttackForTest(target));
			assertTrue(warlock.rangedAttackForTest(target));
			assertEquals(target.pos, warlock.fireblastTargetCell);
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void warlockIsImmuneToFireAndBurning() {
		RoastLambWarlock warlock = new RoastLambWarlock();

		assertTrue(warlock.isImmune(Fire.class));
		assertTrue(warlock.isImmune(Burning.class));
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
	public void completeFireConeHitsTargetsSeedsFireAndExcludesCaster() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = openLevel(15, 15);
			level.heaps = new com.watabou.utils.SparseArray<>();
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			FireConeWarlock warlock = new FireConeWarlock();
			warlock.pos = 112;
			TestTarget target = new TestTarget();
			target.pos = 116;
			Actor.add(warlock);
			Actor.add(target);

			warlock.castForTest(target.pos);

			assertTrue(target.damageTaken >= 15 && target.damageTaken <= 35);
			assertNotNull(target.buff(Burning.class));
			assertNotNull(target.buff(Cripple.class));
			assertTrue(warlock.seededCells.contains(target.pos));
			assertEquals(150, warlock.HP);
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
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

	@Test
	public void fireblastTargetingStopsAtSevenTiles() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			Dungeon.level = openLevel(15, 5);
			TestWarlock warlock = new TestWarlock();
			warlock.pos = 31;
			Gnoll target = new Gnoll();
			Actor.add(target);

			target.pos = 38;
			assertTrue(warlock.canRangedAttackForTest(target));

			target.pos = 39;
			assertFalse(warlock.canRangedAttackForTest(target));
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void visibleCastsWaitForAnimationCallbackBeforeApplyingEffects() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = openLevel(9, 9);
			TestWarlock warlock = new TestWarlock();
			warlock.forceAnimatedCast = true;
			warlock.pos = 39;
			Gnoll target = new Gnoll();
			target.pos = 40;

			assertFalse(warlock.attackTargetForTest(target));
			assertEquals(40, warlock.animatedTargetCell);
			assertTrue(warlock.needsFlockForTest(target));

			warlock.completeCastForTest();
			assertFalse(warlock.needsFlockForTest(target));

			warlock.fireblastTargetCell = -1;
			target.pos = 43;
			assertFalse(warlock.rangedAttackForTest(target));
			assertEquals(-1, warlock.fireblastTargetCell);

			warlock.completeCastForTest();
			assertEquals(43, warlock.fireblastTargetCell);
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

	private static final class TestWarlock extends RoastLambWarlock {

		private final List<Integer> spawnedCells = new ArrayList<>();
		private final List<Float> spawnedLifespans = new ArrayList<>();
		private boolean physicalAttackCalled;
		private int fireblastTargetCell = -1;
		private int fireblastCastCount;
		private boolean forceAnimatedCast;
		private int animatedTargetCell = -1;

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

		private boolean canRangedAttackForTest(Char target) {
			return canRangedAttack(target);
		}

		private void setVisibleForTest(int cell) {
			fieldOfView = new boolean[Dungeon.level.length()];
			fieldOfView[cell] = true;
		}

		private void completeCastForTest() {
			onCastComplete();
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
		protected boolean canAnimateCast(Char target) {
			return forceAnimatedCast || super.canAnimateCast(target);
		}

		@Override
		protected void playAnimatedCast(int cast, Char target) {
			animatedTargetCell = target.pos;
		}

		@Override
		protected void castFireblast(int targetCell) {
			fireblastTargetCell = targetCell;
			fireblastCastCount++;
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			return baseChance;
		}
	}

	private static final class LootWarlock extends RoastLambWarlock {

		private final float roll;
		private final boolean failWandCreation;
		private Class<? extends Item> selectedLoot;

		private LootWarlock(float roll) {
			this(roll, false);
		}

		private LootWarlock(float roll, boolean failWandCreation) {
			this.roll = roll;
			this.failWandCreation = failWandCreation;
		}

		@Override
		protected float lootSelectionRoll() {
			return roll;
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			return baseChance;
		}

		@Override
		protected Item createWandLoot() {
			selectedLoot = WandOfFireblast.class;
			return failWandCreation ? null : new Item();
		}

		@Override
		protected Item createFlockStoneLoot() {
			selectedLoot = StoneOfFlock.class;
			return new Item();
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

	private static final class FireConeWarlock extends RoastLambWarlock {

		private final List<Integer> seededCells = new ArrayList<>();

		private void castForTest(int targetCell) {
			castFireblast(targetCell);
		}

		@Override
		protected void seedFire(int cell) {
			seededCells.add(cell);
		}

		@Override
		protected void playFireblastSounds() {
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
