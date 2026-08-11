package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CorrosiveSwarmTest {

	@Test
	public void randomPotionChanceHalvesForEverySplitGenerationAndSurvivesSave() {
		TestCorrosiveSwarm original = new TestCorrosiveSwarm();
		assertEquals(1f / 8f, original.lootChance(), 0f);
		assertSame(Generator.Category.POTION, original.lootType());
		original.createLoot();
		assertTrue(original.potionFactoryCalled);

		CorrosiveSwarm firstGeneration = (CorrosiveSwarm) original.makeSplit();
		Bundle bundle = new Bundle();
		firstGeneration.storeInBundle(bundle);
		TestCorrosiveSwarm restored = new TestCorrosiveSwarm();
		restored.restoreFromBundle(bundle);
		assertEquals(1f / 16f, restored.lootChance(), 0f);

		CorrosiveSwarm secondGeneration = (CorrosiveSwarm) restored.makeSplit();
		Bundle secondBundle = new Bundle();
		secondGeneration.storeInBundle(secondBundle);
		TestCorrosiveSwarm restoredSecond = new TestCorrosiveSwarm();
		restoredSecond.restoreFromBundle(secondBundle);
		assertEquals(1f / 32f, restoredSecond.lootChance(), 0f);
	}

	@Test
	public void usesApprovedTowerBaselineWithAdjustedHealthAndDamage() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();
		Gnoll attacker = new Gnoll();

		assertEquals(200, swarm.HT);
		assertEquals(200, swarm.HP);
		assertEquals(40, swarm.attackSkill(null));
		assertEquals(20, swarm.defenseSkill(attacker));
		assertEquals(1f, swarm.speed(), 0f);
		assertEquals(1f, swarm.attackDelay(), 0f);
		assertEquals(13, swarm.EXP);
		assertEquals(30, swarm.maxLvl);

		for (int i = 0; i < 500; i++) {
			int damage = swarm.damageRoll();
			int armor = swarm.rollArmor();
			assertTrue(damage >= 15 && damage <= 25);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void splitFactoryKeepsCorrosiveSubtype() {
		assertTrue(new TestCorrosiveSwarm().makeSplit() instanceof CorrosiveSwarm);
	}

	@Test
	public void onlySplitsAboveTenHp() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();

		swarm.HP = 11;
		assertTrue(swarm.maySplit(1));

		swarm.HP = 10;
		assertFalse(swarm.maySplit(1));

		swarm.HP = 9;
		assertFalse(swarm.maySplit(1));
	}

	@Test
	public void oozeDamageSplitsButDealsZeroActualDamage() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = new TestLevel();
			level.setSize(5, 5);
			for (int cell = 0; cell < level.length(); cell++) {
				level.passable[cell] = true;
			}
			level.mobs = new HashSet<>();
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			CorrosiveSwarm swarm = new CorrosiveSwarm();
			swarm.pos = 12;

			swarm.damage(3, new Object(),
					DamageTag.PHYSICAL, DamageTag.OOZE, DamageTag.UNAVOIDABLE);

			assertEquals(1, level.mobs.size());
			assertTrue(level.mobs.iterator().next() instanceof CorrosiveSwarm);
			assertEquals(102, swarm.HP);
			assertEquals(98, level.mobs.iterator().next().HP);
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void repeatedOozeDamageSplitsUntilHpThreshold() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = new TestLevel();
			level.setSize(5, 5);
			for (int cell = 0; cell < level.length(); cell++) {
				level.passable[cell] = true;
			}
			level.mobs = new HashSet<>();
			level.blobs = new HashMap<>();
			Dungeon.level = level;

			CorrosiveSwarm swarm = new CorrosiveSwarm();
			swarm.pos = 12;
			int[] expectedHp = {102, 53, 28, 16, 10};

			for (int hp : expectedHp) {
				swarm.damage(3, new Object(),
						DamageTag.PHYSICAL, DamageTag.OOZE, DamageTag.UNAVOIDABLE);
				assertEquals(hp, swarm.HP);
				assertEquals(1, level.mobs.size());
				level.mobs.clear();
				Actor.clear();
			}

			swarm.damage(3, new Object(),
					DamageTag.PHYSICAL, DamageTag.OOZE, DamageTag.UNAVOIDABLE);
			assertEquals(10, swarm.HP);
			assertTrue(level.mobs.isEmpty());
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void everyOozeTickAttemptsToSplitWithoutDealingDamage() {
		SplitTrackingCorrosiveSwarm swarm = new SplitTrackingCorrosiveSwarm();

		for (int i = 0; i < 5; i++) {
			swarm.damage(3, new Object(),
					DamageTag.PHYSICAL, DamageTag.OOZE, DamageTag.UNAVOIDABLE);
		}

		assertEquals(5, swarm.splitAttempts);
		assertEquals(200, swarm.HP);
	}

	@Test
	public void corrosionDamageIsNotImmune() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();
		swarm.HP = 10;

		swarm.damage(3, new Object(),
				DamageTag.PHYSICAL, DamageTag.CORROSION, DamageTag.UNAVOIDABLE);

		assertEquals(7, swarm.HP);
	}

	@Test
	public void oozeStartsAtTwoTurnsAndStacksDuration() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();
		Gnoll target = new Gnoll();

		swarm.oozed(target);
		Ooze ooze = target.buff(Ooze.class);
		assertNotNull(ooze);
		assertEquals(2f, stored(ooze, "left"), 0f);

		swarm.oozed(target);
		assertSame(ooze, target.buff(Ooze.class));
		assertEquals(4f, stored(ooze, "left"), 0f);
	}

	@Test
	public void positiveDamageStartsChainAndOnlyChainDamageAcceptsZero() {
		TestCorrosiveSwarm swarm = new TestCorrosiveSwarm();

		CorrosiveSwarm.BurstChain initial = swarm.chainFor(1, new Object());
		assertNotNull(initial);
		assertSame(initial, swarm.chainFor(0, initial));
		assertNull(swarm.chainFor(0, new Object()));
	}

	@Test
	public void eachSwarmEntersOneBurstChainOnlyOnce() {
		CorrosiveSwarm.BurstChain chain = new CorrosiveSwarm.BurstChain();
		CorrosiveSwarm first = new CorrosiveSwarm();
		CorrosiveSwarm second = new CorrosiveSwarm();

		assertTrue(chain.enter(first));
		assertFalse(chain.enter(first));
		assertTrue(chain.enter(second));
	}

	@Test
	public void zeroDamageChainsBurstOnceWithoutChangingHp() {
		CountingCorrosiveSwarm swarm = new CountingCorrosiveSwarm();
		CorrosiveSwarm.BurstChain chain = new CorrosiveSwarm.BurstChain();

		swarm.damage(0, chain);
		swarm.damage(0, chain);

		assertEquals(1, swarm.bursts);
		assertEquals(200, swarm.HP);
	}

	@Test
	public void realNeighbourScanAppliesOozeAndChainsAcrossAdjacentSwarm() {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = new TestLevel();
			level.setSize(5, 5);
			Dungeon.level = level;

			ScanningCorrosiveSwarm source = new ScanningCorrosiveSwarm();
			source.pos = 12;
			ScanningCorrosiveSwarm adjacentSwarm = new ScanningCorrosiveSwarm();
			adjacentSwarm.pos = 13;
			Gnoll sharedNeighbour = new Gnoll();
			sharedNeighbour.pos = 8;
			Gnoll distant = new Gnoll();
			distant.pos = 0;

			Actor.add(source);
			Actor.add(adjacentSwarm);
			Actor.add(sharedNeighbour);
			Actor.add(distant);

			source.damage(0, new CorrosiveSwarm.BurstChain());

			assertEquals(1, source.bursts);
			assertEquals(1, adjacentSwarm.bursts);
			assertEquals(200, source.HP);
			assertEquals(200, adjacentSwarm.HP);
			assertEquals(2f, stored(source.buff(Ooze.class), "left"), 0f);
			assertEquals(2f, stored(adjacentSwarm.buff(Ooze.class), "left"), 0f);
			assertEquals(4f, stored(sharedNeighbour.buff(Ooze.class), "left"), 0f);
			assertNull(distant.buff(Ooze.class));
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	private static float stored(Ooze ooze, String key) {
		Bundle bundle = new Bundle();
		ooze.storeInBundle(bundle);
		return bundle.getFloat(key);
	}

	private static class TestCorrosiveSwarm extends CorrosiveSwarm {

		private boolean potionFactoryCalled;

		private int rollArmor() {
			return super.armorRoll();
		}

		private Swarm makeSplit() {
			return createSplit();
		}

		private boolean maySplit(int damage) {
			return canSplit(damage);
		}

		private void oozed(Gnoll target) {
			applyOoze(target);
		}

		private BurstChain chainFor(int damage, Object source) {
			return burstChainFor(damage, source);
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			return baseChance;
		}

		@Override
		protected Item createPotionLoot() {
			potionFactoryCalled = true;
			return null;
		}

		private Object lootType() {
			return loot;
		}
	}

	private static class SplitTrackingCorrosiveSwarm extends CorrosiveSwarm {

		private int splitAttempts;

		@Override
		protected void tryToSplit(int damage) {
			splitAttempts++;
		}
	}

	private static class CountingCorrosiveSwarm extends CorrosiveSwarm {

		private int bursts;

		@Override
		protected void emitCorrosiveBurst(BurstChain chain) {
			bursts++;
		}
	}

	private static class ScanningCorrosiveSwarm extends CorrosiveSwarm {

		private int bursts;

		@Override
		protected void emitCorrosiveBurst(BurstChain chain) {
			bursts++;
			super.emitCorrosiveBurst(chain);
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
