package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SoulCollectorTest {

	@Test
	public void hasConfiguredPanelAndMindVisionDrop() {
		TestCollector collector = new TestCollector();

		assertEquals(200, collector.HT);
		assertEquals(200, collector.HP);
		assertEquals(0, collector.damageRoll());
		assertEquals(0, collector.attackSkill(new Char() { }));
		assertFalse(collector.canAttackForTest(new Char() { }));
		for (int i = 0; i < 100; i++) {
			assertTrue(collector.drRoll() >= 0 && collector.drRoll() <= 20);
		}
		assertEquals(13, collector.EXP);
		assertEquals(30, collector.maxLvl);
		assertSame(PotionOfMindVision.class, collector.lootClassForTest());
		assertEquals(1f / 8f, collector.lootChance(), 0f);
		assertEquals(1f / 8f, collector.adjustedBaseChance, 0f);
	}

	@Test
	public void recordsAnyVisibleDeathIncludingWraithButNeverItself() {
		TestCollector collector = new TestCollector();
		collector.visibleDeath = true;
		Char ordinary = new Char() { };
		ordinary.pos = 10;
		Wraith wraith = new Wraith();
		wraith.pos = 11;

		collector.onCharDied(ordinary);
		collector.onCharDied(wraith);
		collector.onCharDied(collector);

		assertEquals(2, collector.soulCountForTest());
		assertEquals(10, collector.soulCellForTest(0));
		assertEquals(11, collector.soulCellForTest(1));
	}

	@Test
	public void ignoresDeathsOutsideItsVision() {
		TestCollector collector = new TestCollector();
		collector.visibleDeath = false;
		Char victim = new Char() { };
		victim.pos = 10;

		collector.onCharDied(victim);

		assertEquals(0, collector.soulCountForTest());
	}

	@Test
	public void raisesSoulsFifoAfterThreeExclusiveActionTurns() {
		TestCollector collector = new TestCollector();
		collector.state = collector.WANDERING;
		collector.enqueueForTest(10);
		collector.enqueueForTest(11);
		collector.reviveCell = 12;

		collector.actForTest();
		collector.actForTest();
		assertEquals(0, collector.spawned.size());
		assertEquals(2, collector.soulChargeForTest(0));
		assertEquals(0, collector.baseActCalls);

		collector.actForTest();
		assertEquals(1, collector.spawned.size());
		assertEquals(1, collector.soulCountForTest());
		assertEquals(11, collector.soulCellForTest(0));
		assertEquals(0, collector.soulChargeForTest(0));
		assertEquals(12, collector.spawned.get(0).pos);
	}

	@Test
	public void blockedReadySoulKeepsChargeAndCanFleeUntilRetrySucceeds() {
		TestCollector collector = new TestCollector();
		collector.state = collector.WANDERING;
		collector.enqueueForTest(10);
		collector.reviveCell = -1;

		collector.actForTest();
		collector.actForTest();
		collector.actForTest();
		assertEquals(3, collector.soulChargeForTest(0));
		assertEquals(0, collector.baseActCalls);

		collector.actForTest();
		assertEquals(3, collector.soulChargeForTest(0));
		assertEquals(1, collector.baseActCalls);
		assertEquals(0, collector.spawned.size());

		collector.reviveCell = 14;
		collector.actForTest();
		assertEquals(1, collector.spawned.size());
		assertEquals(0, collector.soulCountForTest());
		assertEquals(1, collector.baseActCalls);
	}

	@Test
	public void sleepAndParalysisPauseSoulCharge() {
		TestCollector collector = new TestCollector();
		collector.enqueueForTest(10);
		collector.state = collector.SLEEPING;

		collector.actForTest();
		assertEquals(0, collector.soulChargeForTest(0));

		collector.state = collector.WANDERING;
		collector.paralysed = 1;
		collector.actForTest();
		assertEquals(0, collector.soulChargeForTest(0));
		assertEquals(2, collector.baseActCalls);
	}

	@Test
	public void spawnedWraithInheritsCurrentAlignment() {
		TestCollector collector = new TestCollector();
		collector.state = collector.WANDERING;
		collector.alignment = Char.Alignment.ALLY;
		collector.enqueueForTest(10);
		collector.reviveCell = 12;

		collector.actForTest();
		collector.actForTest();
		collector.actForTest();

		assertSame(Char.Alignment.ALLY, collector.spawned.get(0).alignment);
		assertSame(collector.spawned.get(0).HUNTING, collector.spawned.get(0).state);
	}

	@Test
	public void queueAndPartialChargeSurviveBundleRoundTrip() {
		TestCollector original = new TestCollector();
		original.state = original.WANDERING;
		original.enqueueForTest(10);
		original.enqueueForTest(11);
		original.actForTest();
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TestCollector restored = new TestCollector();
		restored.restoreFromBundle(bundle);

		assertEquals(2, restored.soulCountForTest());
		assertEquals(10, restored.soulCellForTest(0));
		assertEquals(1, restored.soulChargeForTest(0));
		assertEquals(11, restored.soulCellForTest(1));
		assertEquals(0, restored.soulChargeForTest(1));
	}

	@Test
	public void restoreRejectsNegativeSoulCellsWithoutAnActiveLevelAndClampsCharge() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = null;
			TestCollector original = new TestCollector();
			Bundle bundle = new Bundle();
			original.storeInBundle(bundle);
			ArrayList<SoulCollector.SoulRecord> corruptSouls = new ArrayList<>();
			corruptSouls.add(new SoulCollector.SoulRecord(-4, 99));
			corruptSouls.add(new SoulCollector.SoulRecord(10, 99));
			bundle.put("souls", corruptSouls);

			TestCollector restored = new TestCollector();
			restored.restoreFromBundle(bundle);

			assertEquals(1, restored.soulCountForTest());
			assertEquals(10, restored.soulCellForTest(0));
			assertEquals(3, restored.soulChargeForTest(0));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void choosingAnEnemyChangesCollectorToFleeing() {
		TestCollector collector = new TestCollector();
		Char target = new Char() { };

		collector.prepareToFleeForTest(target);

		assertSame(collector.FLEEING, collector.state);
		assertSame(target, collector.enemyForTest());
	}

	private static class TestCollector extends SoulCollector {
		boolean visibleDeath;
		int reviveCell = -1;
		int baseActCalls;
		float adjustedBaseChance;
		final ArrayList<PowerfulWraith> spawned = new ArrayList<>();

		@Override
		protected boolean canCollectSoulAt(int cell) {
			return visibleDeath;
		}

		@Override
		protected int findReviveCell(int deathCell) {
			return reviveCell;
		}

		@Override
		protected void addPowerfulWraithToLevel(PowerfulWraith wraith) {
			spawned.add(wraith);
		}

		@Override
		protected boolean performBaseAct() {
			baseActCalls++;
			return true;
		}

		boolean actForTest() {
			return act();
		}

		void enqueueForTest(int cell) {
			enqueueSoul(cell);
		}

		int soulCountForTest() {
			return soulCount();
		}

		int soulCellForTest(int index) {
			return soulCell(index);
		}

		int soulChargeForTest(int index) {
			return soulCharge(index);
		}

		boolean canAttackForTest(Char target) {
			return canAttack(target);
		}

		Class<?> lootClassForTest() {
			return (Class<?>) loot;
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			adjustedBaseChance = baseChance;
			return baseChance;
		}

		void prepareToFleeForTest(Char target) {
			prepareToFlee(target);
		}

		Char enemyForTest() {
			return enemy;
		}
	}
}
