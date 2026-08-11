package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MimicCrocodileTest {

	@Test
	public void mutuallyExclusiveMeatLootKeepsOneQuarterAndOnePercentChances() {
		LootCrocodile phantom = new LootCrocodile(0f);
		LootCrocodile mystery = new LootCrocodile(1f);

		assertEquals(13f / 50f, phantom.lootChance(), 0.000001f);
		phantom.createLoot();
		mystery.createLoot();
		assertSame(PhantomMeat.class, phantom.selectedLoot);
		assertSame(MysteryMeat.class, mystery.selectedLoot);
	}

	@Test
	public void meatSelectionChangesAtOneInTwentySixConditionalThreshold() {
		float threshold = 1f / 26f;
		LootCrocodile below = new LootCrocodile(threshold - 0.000001f);
		LootCrocodile above = new LootCrocodile(threshold + 0.000001f);

		below.createLoot();
		above.createLoot();

		assertSame(PhantomMeat.class, below.selectedLoot);
		assertSame(MysteryMeat.class, above.selectedLoot);
	}

	@Test
	public void usesApprovedTowerBaselineAndStartsWandering() {
		TestCrocodile crocodile = new TestCrocodile();
		Gnoll attacker = new Gnoll();

		assertEquals(100, crocodile.HT);
		assertEquals(100, crocodile.HP);
		assertEquals(20, crocodile.defenseSkill(attacker));
		assertEquals(1f, crocodile.speed(), 0f);
		assertEquals(1f, crocodile.attackDelay(), 0f);
		assertEquals(13, crocodile.EXP);
		assertEquals(30, crocodile.maxLvl);
		assertEquals(13f / 50f, crocodile.lootChance(), 0.000001f);
		assertSame(crocodile.WANDERING, crocodile.state);

		for (int i = 0; i < 500; i++) {
			int armor = crocodile.armorRollForTest();
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void takingDamageDoesNotRevealLurkingCrocodile() {
		TestCrocodile crocodile = new TestCrocodile();

		crocodile.damage(1, new Gnoll(), DamageTag.PHYSICAL);

		assertEquals(99, crocodile.HP);
		assertTrue(crocodile.isLurking());
	}

	@Test
	public void lurkingCrocodileIsHiddenFromEnemyIndicatorsUntilItAmbushes() {
		TestCrocodile crocodile = new TestCrocodile();

		assertFalse(crocodile.isVisibleEnemyForHero());
		crocodile.beginAttackForTest();
		assertTrue(crocodile.isVisibleEnemyForHero());
		assertTrue(new Gnoll().isVisibleEnemyForHero());
	}

	@Test
	public void firstAttackRevealsAndUsesTripleMaximumDamageAmbush() {
		TestCrocodile crocodile = new TestCrocodile();

		assertTrue(crocodile.isLurking());
		crocodile.beginAttackForTest();

		assertFalse(crocodile.isLurking());
		assertEquals(Char.INFINITE_ACCURACY, crocodile.attackSkill(null));
		assertEquals(90, crocodile.damageRoll());

		crocodile.finishAttackForTest();
		assertEquals(40, crocodile.attackSkill(null));
		for (int i = 0; i < 500; i++) {
			int damage = crocodile.damageRoll();
			assertTrue(damage >= 20 && damage <= 30);
		}
	}

	@Test
	public void ambushDamageTracksThreeTimesNormalMaximumDamage() {
		ScalingDamageCrocodile crocodile = new ScalingDamageCrocodile();

		crocodile.beginAttackForTest();

		assertEquals(120, crocodile.damageRoll());
	}

	@Test
	public void landedAttackUsesFinalResolvedDamageForCrippleAndBleeding() {
		TestCrocodile crocodile = new TestCrocodile();
		Gnoll target = new TestTarget();

		crocodile.resolveAttackForTest(target, true, 9);

		Cripple cripple = target.buff(Cripple.class);
		Bleeding bleeding = target.buff(Bleeding.class);
		assertNotNull(cripple);
		assertNotNull(bleeding);
		assertEquals(3f, cripple.cooldown(), 0f);
		assertEquals(5f, bleeding.level(), 0f);
	}

	@Test
	public void zeroDamageStillCreatesMinimumOnePointBleeding() {
		TestCrocodile crocodile = new TestCrocodile();
		Gnoll target = new TestTarget();

		crocodile.resolveAttackForTest(target, true, 0);

		assertEquals(1f, target.buff(Bleeding.class).level(), 0f);
	}

	@Test
	public void missedAttackDoesNotApplyCrippleOrBleeding() {
		TestCrocodile crocodile = new TestCrocodile();
		Gnoll target = new TestTarget();

		crocodile.resolveAttackForTest(target, false, 0);

		assertTrue(target.buff(Cripple.class) == null);
		assertTrue(target.buff(Bleeding.class) == null);
	}

	@Test
	public void lurkingAndRevealedStateSurviveSaveAndLoad() {
		TestCrocodile lurking = new TestCrocodile();
		Bundle lurkingBundle = new Bundle();
		lurking.storeInBundle(lurkingBundle);
		TestCrocodile restoredLurking = new TestCrocodile();
		restoredLurking.restoreFromBundle(lurkingBundle);
		assertTrue(restoredLurking.isLurking());

		TestCrocodile revealed = new TestCrocodile();
		revealed.beginAttackForTest();
		revealed.finishAttackForTest();
		revealed.state = revealed.SLEEPING;
		Bundle revealedBundle = new Bundle();
		revealed.storeInBundle(revealedBundle);
		TestCrocodile restoredRevealed = new TestCrocodile();
		restoredRevealed.restoreFromBundle(revealedBundle);
		assertFalse(restoredRevealed.isLurking());
		assertSame(restoredRevealed.SLEEPING, restoredRevealed.state);
	}

	@Test
	public void pendingAmbushSurvivesSaveDuringAttackAnimation() {
		TestCrocodile original = new TestCrocodile();
		original.beginAttackForTest();

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		TestCrocodile restored = new TestCrocodile();
		restored.restoreFromBundle(bundle);

		assertFalse(restored.isLurking());
		assertEquals(Char.INFINITE_ACCURACY, restored.attackSkill(null));
		assertEquals(90, restored.damageRoll());
	}

	@Test
	public void synchronousAttackCompletionClearsAmbushWithoutResolvedHook() {
		TestCrocodile crocodile = new TestCrocodile();
		crocodile.attackCompletesSynchronously = true;

		assertTrue(crocodile.doAttackForTest(new TestTarget()));

		assertFalse(crocodile.isLurking());
		assertEquals(40, crocodile.attackSkill(null));
	}

	private static class TestCrocodile extends MimicCrocodile {

		private boolean attackCompletesSynchronously;

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		private int armorRollForTest() {
			return armorRoll();
		}

		private void beginAttackForTest() {
			beginAmbushAttack();
		}

		private boolean doAttackForTest(Char target) {
			return doAttack(target);
		}

		@Override
		protected boolean performAttack(Char target) {
			return attackCompletesSynchronously;
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			return baseChance;
		}

		private void finishAttackForTest() {
			finishAmbushAttack();
		}

		private void resolveAttackForTest(Char target, boolean hit, int damageDealt) {
			onAttackResolved(
					target,
					hit,
					damageDealt,
					DamageTag.PHYSICAL,
					DamageTag.MELEE);
		}
	}

	private static class TestTarget extends Gnoll {

		@Override
		public float resist(Class effect) {
			return 1f;
		}
	}

	private static final class ScalingDamageCrocodile extends MimicCrocodile {

		@Override
		protected int normalDamageMax() {
			return 40;
		}

		private void beginAttackForTest() {
			beginAmbushAttack();
		}
	}

	private static final class LootCrocodile extends MimicCrocodile {

		private final float roll;
		private Class<? extends Item> selectedLoot;

		private LootCrocodile(float roll) {
			this.roll = roll;
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
		protected Item createPhantomMeatLoot() {
			selectedLoot = PhantomMeat.class;
			return new Item();
		}

		@Override
		protected Item createMysteryMeatLoot() {
			selectedLoot = MysteryMeat.class;
			return new Item();
		}
	}
}
