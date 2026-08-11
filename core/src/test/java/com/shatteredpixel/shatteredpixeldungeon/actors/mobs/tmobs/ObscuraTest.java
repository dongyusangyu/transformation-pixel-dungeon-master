package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ObscuraTest {

	@Test
	public void obscuraBaseStatsMatchSpecification() {
		TestObscura obscura = new TestObscura();

		assertEquals(150, obscura.HT);
		assertEquals(150, obscura.HP);
		assertEquals(30, obscura.defenseSkill);
		assertEquals(50, obscura.attackSkill(null));
		assertEquals(1f, obscura.speed(), 0f);
		assertEquals(1f, obscura.attackDelayForTest(), 0f);
		assertEquals(13, obscura.EXP);
		assertEquals(30, obscura.maxLvl);
		assertEquals(0f, obscura.lootChance(), 0f);

		for (int i = 0; i < 500; i++) {
			int damage = obscura.damageRoll();
			int armor = obscura.drRoll();
			assertTrue(damage >= 15 && damage <= 30);
			assertTrue(armor >= 0 && armor <= 20);
		}
	}

	@Test
	public void wildDreadBaseStatsMatchSpecificationAndCannotRewardFarming() {
		TestWildDread dread = new TestWildDread();

		assertEquals(21, dread.HT);
		assertEquals(21, dread.HP);
		assertEquals(20, dread.defenseSkill);
		assertEquals(50, dread.attackSkill(null));
		assertEquals(1f, dread.speed(), 0f);
		assertEquals(5f, dread.attackDelayForTest(), 0f);
		assertEquals(0, dread.EXP);
		assertEquals(30, dread.maxLvl);
		assertEquals(0f, dread.lootChance(), 0f);

		for (int i = 0; i < 500; i++) {
			int damage = dread.damageRoll();
			int armor = dread.drRoll();
			assertTrue(damage >= 15 && damage <= 45);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void completedSynchronousAttacksUseCurrentDelayThenSpeedUpAndCapEvenOnMiss() {
		TestWildDread dread = new TestWildDread();
		TestEnemyMob target = new TestEnemyMob();
		float[] expectedDelays = {5f, 4f, 3f, 2f, 1f, 1f};

		for (int attack = 0; attack < expectedDelays.length; attack++) {
			dread.attackResult = attack != 2;
			float cooldownBefore = dread.cooldown();

			assertEquals(expectedDelays[attack], dread.attackDelayForTest(), 0f);
			assertEquals(Math.min(attack, 4), dread.completedAttackGrowthForTest());
			assertTrue(dread.doAttackForTest(target));

			assertEquals(expectedDelays[attack], dread.cooldown() - cooldownBefore, 0f);
			assertEquals(Math.min(attack + 1, 4), dread.completedAttackGrowthForTest());
		}

		assertEquals(6, dread.attackCalls);
	}

	@Test
	public void animatedAttackCompletionUsesCurrentDelayAndGrowsExactlyOnceOnMiss() {
		TestWildDread dread = new TestWildDread();
		TestEnemyMob target = new TestEnemyMob();
		TestAttackSprite attackSprite = new TestAttackSprite();
		dread.sprite = attackSprite;
		dread.attackResult = false;
		float cooldownBefore = dread.cooldown();

		assertEquals(5f, dread.attackDelayForTest(), 0f);
		assertEquals(0, dread.completedAttackGrowthForTest());
		assertFalse(dread.doAttackForTest(target));
		assertEquals(0f, dread.cooldown() - cooldownBefore, 0f);
		assertEquals(0, dread.completedAttackGrowthForTest());
		assertEquals(1, attackSprite.attackCalls);
		assertEquals(0, dread.attackCalls);

		dread.finishAnimatedAttackForTest(target);

		assertEquals(5f, dread.cooldown() - cooldownBefore, 0f);
		assertEquals(1, dread.completedAttackGrowthForTest());
		assertEquals(4f, dread.attackDelayForTest(), 0f);
		assertEquals(1, dread.attackCalls);
	}

	@Test
	public void completedAttackGrowthSurvivesBundleRoundTrip() {
		TestWildDread original = new TestWildDread();
		TestEnemyMob target = new TestEnemyMob();
		assertTrue(original.doAttackForTest(target));
		assertTrue(original.doAttackForTest(target));
		assertEquals(2, original.completedAttackGrowthForTest());
		assertEquals(3f, original.attackDelayForTest(), 0f);

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		TestWildDread restored = new TestWildDread();
		restored.restoreFromBundle(bundle);

		assertEquals(2, restored.completedAttackGrowthForTest());
		assertEquals(3f, restored.attackDelayForTest(), 0f);
	}

	@Test
	public void normalDownedRevivalPreservesCompletedAttackGrowthButNewDreadStartsSlow() {
		TestWildDread dread = new TestWildDread();
		TestEnemyMob target = new TestEnemyMob();
		assertTrue(dread.doAttackForTest(target));
		assertTrue(dread.doAttackForTest(target));
		assertEquals(2, dread.completedAttackGrowthForTest());

		TestObscura owner = new TestObscura();
		dread.owner = owner;
		dread.bindToOwner(owner.id());
		dread.pos = 24;
		dread.dieForTest(new Object());
		assertTrue(dread.downedForTest());
		assertEquals(2, dread.completedAttackGrowthForTest());

		owner.reviveCell = 24;
		assertTrue(owner.advanceRevivalForTest());
		assertSame(dread, owner.revived);
		assertEquals(2, dread.completedAttackGrowthForTest());
		assertEquals(3f, dread.attackDelayForTest(), 0f);

		TestWildDread fresh = new TestWildDread();
		assertEquals(0, fresh.completedAttackGrowthForTest());
		assertEquals(5f, fresh.attackDelayForTest(), 0f);
	}

	@Test
	public void wildDreadConversionNeverQualifiesForKillRewards() {
		TestWildDread dread = new TestWildDread();
		TestEnemyMob ordinaryEnemy = new TestEnemyMob();
		ordinaryEnemy.alignment = Char.Alignment.ENEMY;

		assertFalse(AllyBuff.isConversionRewardEligible(dread));
		assertTrue(AllyBuff.isConversionRewardEligible(ordinaryEnemy));
	}

	@Test
	public void firstSightArmsSummonThenChargesThreeActionsAndOnlySummonsOnce() {
		TestObscura obscura = new TestObscura();
		obscura.summonCell = 37;
		obscura.revealEnemyDuringBaseAct = true;

		assertTrue(obscura.actForTest());
		assertTrue(obscura.summonPendingForTest());
		assertFalse(obscura.summonConsumedForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(0, obscura.summonChargeTurnsForTest());
		assertEquals(1f, obscura.cooldown(), 0f);

		obscura.revealEnemyDuringBaseAct = false;
		assertTrue(obscura.actForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(1, obscura.summonChargeTurnsForTest());
		assertEquals(2f, obscura.cooldown(), 0f);

		assertTrue(obscura.actForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(2, obscura.summonChargeTurnsForTest());
		assertEquals(3f, obscura.cooldown(), 0f);

		assertTrue(obscura.actForTest());
		assertFalse(obscura.summonPendingForTest());
		assertTrue(obscura.summonConsumedForTest());
		assertEquals(1, obscura.spawnCount);
		assertEquals(0, obscura.summonChargeTurnsForTest());
		assertEquals(37, obscura.spawned.pos);
		assertEquals(obscura.id(), obscura.spawned.ownerIdForTest());
		assertEquals(obscura.spawned.id(), obscura.wildDreadIdForTest());
		assertSame(obscura.alignment, obscura.spawned.alignment);
		assertEquals(1, obscura.activeOwned.size());
		assertEquals(4f, obscura.cooldown(), 0f);

		assertTrue(obscura.actForTest());
		assertEquals(1, obscura.spawnCount);
		assertEquals(1, obscura.activeOwned.size());
		assertEquals(2, obscura.baseActCalls);
		assertEquals(5f, obscura.cooldown(), 0f);
	}

	@Test
	public void wakingFromSleepArmsButDoesNotSummonOnFirstSight() {
		TestObscura obscura = new TestObscura();
		obscura.state = obscura.SLEEPING;
		obscura.wakeDuringBaseAct = true;

		assertTrue(obscura.actForTest());

		assertTrue(obscura.summonPendingForTest());
		assertFalse(obscura.summonConsumedForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(0, obscura.summonChargeTurnsForTest());
	}

	@Test
	public void blockedSummonKeepsFullChargeAndRetriesWithoutDuplicating() {
		TestObscura obscura = new TestObscura();
		obscura.armSummonForTest();
		obscura.summonCell = -1;

		assertTrue(obscura.actForTest());
		assertEquals(1f, obscura.cooldown(), 0f);
		assertEquals(1, obscura.summonChargeTurnsForTest());
		assertEquals(0, obscura.spawnCount);

		assertTrue(obscura.actForTest());
		assertEquals(2f, obscura.cooldown(), 0f);
		assertEquals(2, obscura.summonChargeTurnsForTest());
		assertEquals(0, obscura.spawnCount);

		assertTrue(obscura.actForTest());
		assertEquals(3f, obscura.cooldown(), 0f);
		assertEquals(3, obscura.summonChargeTurnsForTest());
		assertTrue(obscura.summonPendingForTest());
		assertFalse(obscura.summonConsumedForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(0, obscura.baseActCalls);

		assertTrue(obscura.actForTest());
		assertEquals(4f, obscura.cooldown(), 0f);
		assertEquals(3, obscura.summonChargeTurnsForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(0, obscura.baseActCalls);

		obscura.summonCell = 42;
		assertTrue(obscura.actForTest());
		assertEquals(5f, obscura.cooldown(), 0f);
		assertEquals(1, obscura.spawnCount);
		assertEquals(1, obscura.activeOwned.size());
		assertEquals(0, obscura.summonChargeTurnsForTest());
		assertTrue(obscura.summonConsumedForTest());

		assertTrue(obscura.actForTest());
		assertEquals(6f, obscura.cooldown(), 0f);
		assertEquals(1, obscura.spawnCount);
		assertEquals(1, obscura.activeOwned.size());
	}

	@Test
	public void paralysisAndSleepDelayRatherThanBypassThePendingSummon() {
		TestObscura obscura = new TestObscura();
		obscura.armSummonForTest();
		obscura.summonCell = 12;

		assertTrue(obscura.actForTest());
		assertEquals(1, obscura.summonChargeTurnsForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(1f, obscura.cooldown(), 0f);

		obscura.paralysed = 1;

		assertTrue(obscura.actForTest());
		assertEquals(0, obscura.spawnCount);
		assertTrue(obscura.summonPendingForTest());
		assertEquals(1, obscura.summonChargeTurnsForTest());
		assertEquals(1, obscura.baseActCalls);
		assertEquals(2f, obscura.cooldown(), 0f);

		obscura.paralysed = 0;
		obscura.state = obscura.SLEEPING;
		assertTrue(obscura.actForTest());
		assertEquals(0, obscura.spawnCount);
		assertTrue(obscura.summonPendingForTest());
		assertEquals(1, obscura.summonChargeTurnsForTest());
		assertEquals(2, obscura.baseActCalls);
		assertEquals(3f, obscura.cooldown(), 0f);

		obscura.state = obscura.WANDERING;
		assertTrue(obscura.actForTest());
		assertEquals(0, obscura.spawnCount);
		assertEquals(2, obscura.summonChargeTurnsForTest());
		assertEquals(4f, obscura.cooldown(), 0f);

		assertTrue(obscura.actForTest());
		assertEquals(1, obscura.spawnCount);
		assertEquals(0, obscura.summonChargeTurnsForTest());
		assertTrue(obscura.summonConsumedForTest());
		assertEquals(5f, obscura.cooldown(), 0f);
	}

	@Test
	public void defeatedWildDreadLeavesCombatAndRevivesAtFullHealthNextOwnerTurn() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		dread.owner = owner;
		dread.bindToOwner(owner.id());
		dread.pos = 24;

		dread.dieForTest(new Object());

		assertTrue(dread.downedForTest());
		assertEquals(0, dread.HP);
		assertTrue(dread.removedForRevival);
		assertTrue(owner.revivalPendingForTest());
		assertSame(dread, owner.downedDreadForTest());

		owner.reviveCell = 24;
		assertTrue(owner.advanceRevivalForTest());

		assertFalse(dread.downedForTest());
		assertEquals(21, dread.HP);
		assertEquals(24, dread.pos);
		assertSame(dread, owner.revived);
		assertFalse(owner.revivalPendingForTest());

		dread.pos = 25;
		dread.dieForTest(new Object());
		owner.reviveCell = 25;
		assertTrue(owner.advanceRevivalForTest());
		assertEquals(21, dread.HP);
		assertEquals(25, dread.pos);
	}

	@Test
	public void normalRevivalDoesNotConsumeOwnerAction() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		dread.owner = owner;
		dread.bindToOwner(owner.id());
		dread.pos = 24;
		dread.dieForTest(new Object());
		owner.reviveCell = 25;

		assertTrue(owner.actForTest());

		assertSame(dread, owner.revived);
		assertEquals(21, dread.HP);
		assertEquals(1, owner.baseActCalls);
		assertEquals(1f, owner.cooldown(), 0f);
	}

	@Test
	public void blockedRevivalWaitsUntilAPlacementCellBecomesAvailable() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		dread.owner = owner;
		dread.bindToOwner(owner.id());
		dread.pos = 31;
		dread.dieForTest(new Object());

		owner.reviveCell = -1;
		assertFalse(owner.advanceRevivalForTest());
		assertTrue(dread.downedForTest());
		assertTrue(owner.revivalPendingForTest());

		owner.reviveCell = 32;
		assertTrue(owner.advanceRevivalForTest());
		assertEquals(32, dread.pos);
		assertEquals(21, dread.HP);
	}

	@Test
	public void chasmDeathIsPermanentAndNeverSchedulesRevival() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		dread.owner = owner;
		dread.bindToOwner(owner.id());

		dread.dieForTest(Chasm.class);

		assertTrue(dread.permanentDeath);
		assertFalse(dread.downedForTest());
		assertFalse(owner.revivalPendingForTest());
	}

	@Test
	public void ownerDeathRemovesBothLivingAndDownedMinionsWithoutKillRewards() {
		TestObscura livingOwner = new TestObscura();
		TestWildDread living = new TestWildDread();
		living.owner = livingOwner;
		living.bindToOwner(livingOwner.id());
		livingOwner.owned = living;

		livingOwner.dieForTest(new Object());

		assertTrue(living.abandoned);
		assertFalse(living.permanentDeath);
		assertSame(Char.Alignment.NEUTRAL, living.alignment);
		assertTrue(livingOwner.ownerDeathFinished);

		TestObscura downedOwner = new TestObscura();
		TestWildDread downed = new TestWildDread();
		downed.owner = downedOwner;
		downed.bindToOwner(downedOwner.id());
		downed.dieForTest(new Object());

		downedOwner.dieForTest(new Object());

		assertTrue(downed.abandoned);
		assertFalse(downed.permanentDeath);
		assertSame(Char.Alignment.NEUTRAL, downed.alignment);
		assertFalse(downedOwner.revivalPendingForTest());
	}

	@Test
	public void ownerDeathRemovesAllActiveDuplicatesAndHeldDread() {
		TestObscura owner = new TestObscura();
		TestWildDread tracked = new TestWildDread();
		TestWildDread duplicate = new TestWildDread();
		TestWildDread held = new TestWildDread();
		tracked.bindToOwner(owner.id());
		duplicate.bindToOwner(owner.id());
		held.bindToOwner(owner.id());
		owner.heldStasisAlly = held;
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		owner.scheduleRevivalForTest(held);
		owner.owned = tracked;
		owner.activeOwned.add(tracked);
		owner.activeOwned.add(duplicate);

		owner.dieForTest(new Object());

		assertTrue(tracked.abandoned);
		assertTrue(duplicate.abandoned);
		assertTrue(held.abandoned);
		assertSame(Char.Alignment.NEUTRAL, tracked.alignment);
		assertSame(Char.Alignment.NEUTRAL, duplicate.alignment);
		assertSame(Char.Alignment.NEUTRAL, held.alignment);
		assertNull(owner.heldStasisAlly);
		assertEquals(1, owner.heldDiscardCount);
		assertFalse(owner.revivalPendingForTest());
		assertNull(owner.downedDreadForTest());
		assertFalse(owner.stasisReplacementPendingForTest());
		assertEquals(-1, owner.wildDreadIdForTest());
	}

	@Test
	public void wildDreadConversionNeverAwardsKillCreditEvenRepeatedly() {
		int enemiesSlain = Statistics.enemiesSlain;
		try {
			TestObscura owner = new TestObscura();
			TestWildDread dread = new TestWildDread();
			owner.owned = dread;
			dread.alignment = Char.Alignment.ENEMY;

			AllyBuff.affectAndLoot(dread, null, TestAllyBuff.class);
			assertEquals(enemiesSlain, Statistics.enemiesSlain);
			assertNotNull(dread.buff(TestAllyBuff.class));

			assertTrue(owner.actForTest());
			assertSame(Char.Alignment.ENEMY, dread.alignment);

			AllyBuff.affectAndLoot(dread, null, TestAllyBuff.class);
			assertEquals(enemiesSlain, Statistics.enemiesSlain);
			assertNotNull(dread.buff(TestAllyBuff.class));
		} finally {
			Statistics.enemiesSlain = enemiesSlain;
		}
	}

	@Test
	public void ownerDeathPreservesDifferentHeldOwner() {
		TestObscura currentOwner = new TestObscura();
		TestObscura otherOwner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		currentOwner.heldStasisAlly = dread;
		dread.bindToOwner(otherOwner.id());

		currentOwner.dieForTest(new Object());

		assertFalse(dread.abandoned);
		assertSame(dread, currentOwner.heldStasisAlly);
		assertEquals(0, currentOwner.heldDiscardCount);
	}

	@Test
	public void ownerDeathCleansSameHeldAndActiveDreadOnce() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		owner.activeOwned.add(dread);
		owner.heldStasisAlly = dread;
		dread.bindToOwner(owner.id());

		owner.dieForTest(new Object());

		assertTrue(dread.abandoned);
		assertEquals(1, owner.heldDiscardCount);
		assertEquals(1, dread.abandonCalls);
	}

	@Test
	public void revivedDreadSpriteIsPlacedBeforeReviveAnimation() throws IOException {
		String obscuraSource = source("Obscura.java");
		int place = obscuraSource.indexOf("dread.sprite.place(dread.pos);");
		int revive = obscuraSource.indexOf(").revive();", place);

		assertTrue(place >= 0);
		assertTrue(revive > place);
	}

	@Test
	public void orphanedWildDreadRemovesItselfWithoutKillRewards() {
		TestWildDread dread = new TestWildDread();

		assertTrue(dread.actForTest());

		assertTrue(dread.abandoned);
		assertFalse(dread.permanentDeath);
		assertSame(Char.Alignment.NEUTRAL, dread.alignment);
	}

	@Test
	public void wildDreadCanResolveOwnerHeldInStasis() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		dread.bindToOwner(owner.id());
		dread.stasisOwner = owner;

		assertSame(owner, dread.owner());
	}

	@Test
	public void wildDreadDoesNotSilentlyUndoAlignmentConversion() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		dread.owner = owner;
		dread.bindToOwner(owner.id());
		dread.alignment = Char.Alignment.ALLY;

		assertTrue(dread.actForTest());
		assertSame(Char.Alignment.ALLY, dread.alignment);
	}

	@Test
	public void ownerSpendsWholeTurnRemovingOnlyAlignmentConversion() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		owner.owned = dread;
		dread.alignment = Char.Alignment.ENEMY;
		TestAllyBuff allyBuff = new TestAllyBuff();
		TestOrdinaryBuff ordinaryBuff = new TestOrdinaryBuff();
		assertTrue(allyBuff.attachTo(dread));
		assertTrue(ordinaryBuff.attachTo(dread));
		assertSame(Char.Alignment.ALLY, dread.alignment);

		assertTrue(owner.actForTest());

		assertEquals(1f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertTrue(dread.buffs(AllyBuff.class).isEmpty());
		assertSame(owner.alignment, dread.alignment);
		assertSame(ordinaryBuff, dread.buff(TestOrdinaryBuff.class));
	}

	@Test
	public void directAlignmentChangeAlsoCostsOneTurn() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		owner.owned = dread;
		dread.alignment = Char.Alignment.NEUTRAL;

		assertTrue(owner.actForTest());

		assertEquals(1f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertSame(owner.alignment, dread.alignment);
	}

	@Test
	public void paralysisAndSleepDelayAlignmentCleanup() {
		TestObscura owner = new TestObscura();
		TestWildDread dread = new TestWildDread();
		owner.owned = dread;
		dread.alignment = Char.Alignment.ALLY;
		owner.paralysed = 1;

		assertTrue(owner.actForTest());
		assertEquals(1, owner.baseActCalls);
		assertSame(Char.Alignment.ALLY, dread.alignment);

		owner.paralysed = 0;
		owner.state = owner.SLEEPING;
		assertTrue(owner.actForTest());
		assertEquals(2, owner.baseActCalls);
		assertSame(Char.Alignment.ALLY, dread.alignment);
	}

	@Test
	public void releaseWithLivingDreadRepairsLinkWithoutSpawning() {
		TestObscura owner = new TestObscura();
		TestWildDread living = new TestWildDread();
		living.bindToOwner(owner.id());
		owner.activeOwned.add(living);

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();

		assertEquals(living.id(), owner.wildDreadIdForTest());
		assertTrue(owner.summonConsumedForTest());
		assertFalse(owner.summonPendingForTest());
		assertEquals(0, owner.spawnCount);
		assertEquals(1, owner.activeOwned.size());
	}

	@Test
	public void releaseWithoutDreadChargesThreeAvailableTurnsAndSpawnsOnce() {
		TestObscura owner = new TestObscura();
		owner.summonCell = 41;

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertEquals(0, owner.spawnCount);
		assertEquals(0, owner.summonChargeTurnsForTest());

		assertTrue(owner.actForTest());
		assertEquals(1f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertEquals(0, owner.spawnCount);
		assertEquals(1, owner.summonChargeTurnsForTest());
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertEquals(1f, owner.cooldown(), 0f);
		assertEquals(0, owner.spawnCount);
		assertEquals(1, owner.summonChargeTurnsForTest());

		assertTrue(owner.actForTest());
		assertEquals(2f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertEquals(0, owner.spawnCount);
		assertEquals(2, owner.summonChargeTurnsForTest());
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertEquals(2f, owner.cooldown(), 0f);
		assertEquals(0, owner.spawnCount);
		assertEquals(2, owner.summonChargeTurnsForTest());

		assertTrue(owner.actForTest());
		assertEquals(3f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertEquals(1, owner.spawnCount);
		assertEquals(0, owner.summonChargeTurnsForTest());
		assertSame(owner.alignment, owner.spawned.alignment);
		assertEquals(owner.id(), owner.spawned.ownerIdForTest());
		assertEquals(owner.spawned.id(), owner.wildDreadIdForTest());
		assertEquals(1, owner.activeOwned.size());

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertTrue(owner.actForTest());
		assertEquals(1, owner.spawnCount);
		assertEquals(1, owner.activeOwned.size());
		assertEquals(1, owner.baseActCalls);
		assertEquals(4f, owner.cooldown(), 0f);
	}

	@Test
	public void stasisReplacementPendingSurvivesBundleRoundTrip() {
		TestObscura owner = new TestObscura();
		owner.summonCell = 41;
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertTrue(owner.actForTest());
		assertTrue(owner.actForTest());
		assertEquals(2, owner.summonChargeTurnsForTest());
		assertEquals(0, owner.spawnCount);
		Bundle bundle = new Bundle();
		owner.storeInBundle(bundle);

		TestObscura restored = new TestObscura();
		restored.summonCell = 42;
		restored.restoreFromBundle(bundle);

		assertTrue(restored.stasisReplacementPendingForTest());
		assertEquals(2, restored.summonChargeTurnsForTest());
		assertTrue(restored.actForTest());
		assertEquals(0, restored.baseActCalls);
		assertEquals(1, restored.spawnCount);
		assertEquals(0, restored.summonChargeTurnsForTest());
		assertSame(restored.alignment, restored.spawned.alignment);
	}

	@Test
	public void disabledOwnerDelaysStasisReplacement() {
		TestObscura owner = new TestObscura();
		owner.summonCell = 41;
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();

		assertTrue(owner.actForTest());
		assertEquals(1, owner.summonChargeTurnsForTest());
		assertEquals(0, owner.spawnCount);

		owner.paralysed = 1;

		assertTrue(owner.actForTest());
		assertEquals(0, owner.spawnCount);
		assertTrue(owner.stasisReplacementPendingForTest());
		assertEquals(1, owner.summonChargeTurnsForTest());
		assertEquals(1, owner.baseActCalls);

		owner.paralysed = 0;
		owner.state = owner.SLEEPING;
		assertTrue(owner.actForTest());
		assertEquals(0, owner.spawnCount);
		assertTrue(owner.stasisReplacementPendingForTest());
		assertEquals(1, owner.summonChargeTurnsForTest());
		assertEquals(2, owner.baseActCalls);

		owner.state = owner.WANDERING;
		assertTrue(owner.actForTest());
		assertEquals(0, owner.spawnCount);
		assertEquals(2, owner.summonChargeTurnsForTest());

		assertTrue(owner.actForTest());
		assertEquals(1, owner.spawnCount);
		assertEquals(0, owner.summonChargeTurnsForTest());
	}

	@Test
	public void releaseWithConvertedLivingDreadCleansInsteadOfReplacing() {
		TestObscura owner = new TestObscura();
		TestWildDread living = new TestWildDread();
		living.bindToOwner(owner.id());
		owner.activeOwned.add(living);
		owner.owned = living;
		TestAllyBuff allyBuff = new TestAllyBuff();
		TestOrdinaryBuff ordinaryBuff = new TestOrdinaryBuff();
		assertTrue(allyBuff.attachTo(living));
		assertTrue(ordinaryBuff.attachTo(living));

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();

		assertFalse(owner.stasisReplacementPendingForTest());
		assertTrue(owner.actForTest());
		assertEquals(1f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertEquals(0, owner.spawnCount);
		assertTrue(living.buffs(AllyBuff.class).isEmpty());
		assertSame(owner.alignment, living.alignment);
		assertSame(ordinaryBuff, living.buff(TestOrdinaryBuff.class));
	}

	@Test
	public void ownerDeathCancelsPendingReplacement() {
		TestObscura owner = new TestObscura();
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertTrue(owner.stasisReplacementPendingForTest());

		owner.dieForTest(new Object());

		assertFalse(owner.stasisReplacementPendingForTest());
	}

	@Test
	public void ownerDeathCancelsPendingSummonAndResetsCharge() {
		TestObscura owner = new TestObscura();
		owner.armSummonForTest();

		assertTrue(owner.actForTest());
		assertTrue(owner.summonPendingForTest());
		assertEquals(1, owner.summonChargeTurnsForTest());

		owner.dieForTest(new Object());

		assertFalse(owner.summonPendingForTest());
		assertFalse(owner.stasisReplacementPendingForTest());
		assertEquals(0, owner.summonChargeTurnsForTest());
	}

	@Test
	public void releaseWithDownedDreadConsumesTurnAndReusesSameObject() {
		TestObscura owner = new TestObscura();
		TestWildDread downed = new TestWildDread();
		downed.owner = owner;
		downed.bindToOwner(owner.id());
		downed.pos = 52;
		downed.dieForTest(new Object());
		owner.reviveCell = 53;

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertEquals(0, owner.spawnCount);

		assertTrue(owner.actForTest());
		assertEquals(1f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertSame(downed, owner.revived);
		assertFalse(downed.downedForTest());
		assertEquals(53, downed.pos);
		assertEquals(downed.id(), owner.wildDreadIdForTest());
		assertEquals(1, owner.activeOwned.size());
	}

	@Test
	public void blockedReplacementKeepsFullChargeThenRetriesAndNeverDuplicates() {
		TestObscura owner = new TestObscura();
		owner.summonCell = -1;

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();

		assertTrue(owner.actForTest());
		assertEquals(1, owner.summonChargeTurnsForTest());
		assertTrue(owner.actForTest());
		assertEquals(2, owner.summonChargeTurnsForTest());
		assertTrue(owner.actForTest());
		assertEquals(3, owner.summonChargeTurnsForTest());
		assertEquals(3f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertEquals(0, owner.spawnCount);

		assertTrue(owner.actForTest());
		assertEquals(3, owner.summonChargeTurnsForTest());
		assertEquals(4f, owner.cooldown(), 0f);
		assertEquals(0, owner.baseActCalls);
		assertEquals(0, owner.spawnCount);

		owner.summonCell = 61;
		assertTrue(owner.actForTest());
		assertEquals(5f, owner.cooldown(), 0f);
		assertEquals(1, owner.spawnCount);
		assertEquals(1, owner.activeOwned.size());
		assertEquals(0, owner.summonChargeTurnsForTest());

		assertTrue(owner.actForTest());
		assertEquals(1, owner.baseActCalls);
		assertEquals(1, owner.spawnCount);
		assertEquals(1, owner.activeOwned.size());
		assertEquals(6f, owner.cooldown(), 0f);
	}

	@Test
	public void canonicalDreadAppearingDuringBlockedReplacementCancelsChargeWithoutSpawning() {
		TestObscura owner = new TestObscura();
		owner.summonCell = -1;
		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();

		assertTrue(owner.actForTest());
		assertTrue(owner.actForTest());
		assertTrue(owner.actForTest());
		assertEquals(3, owner.summonChargeTurnsForTest());
		assertEquals(0, owner.spawnCount);

		TestWildDread canonical = new TestWildDread();
		canonical.bindToOwner(owner.id());
		owner.activeOwned.add(canonical);

		assertTrue(owner.actForTest());
		assertEquals(4f, owner.cooldown(), 0f);
		assertEquals(1, owner.baseActCalls);
		assertEquals(0, owner.spawnCount);
		assertEquals(0, owner.summonChargeTurnsForTest());
		assertFalse(owner.stasisReplacementPendingForTest());
		assertTrue(owner.summonConsumedForTest());
		assertEquals(canonical.id(), owner.wildDreadIdForTest());
		assertEquals(1, owner.activeOwned.size());
	}

	@Test
	public void releaseKeepsCanonicalDreadAndAbandonsDuplicatesWithoutReward() {
		TestObscura owner = new TestObscura();
		TestWildDread lowerIdDuplicate = new TestWildDread();
		lowerIdDuplicate.bindToOwner(owner.id());
		owner.armSummonForTest();
		owner.summonCell = 70;
		assertTrue(owner.actForTest());
		assertTrue(owner.actForTest());
		assertTrue(owner.actForTest());
		TestWildDread recorded = owner.spawned;
		owner.activeOwned.add(lowerIdDuplicate);

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();

		assertEquals(recorded.id(), owner.wildDreadIdForTest());
		assertFalse(recorded.abandoned);
		assertTrue(lowerIdDuplicate.abandoned);
		assertFalse(lowerIdDuplicate.permanentDeath);
		assertSame(Char.Alignment.NEUTRAL, lowerIdDuplicate.alignment);
		assertEquals(1, owner.spawnCount);

		((Stasis.ReleaseListener) (Object) owner).onStasisReleased();
		assertTrue(owner.actForTest());
		assertEquals(1, owner.spawnCount);
	}

	@Test
	public void summonAndOwnerLinkStateSurviveBundleRoundTrip() {
		TestObscura pending = new TestObscura();
		pending.armSummonForTest();
		Bundle pendingBundle = new Bundle();
		pending.storeInBundle(pendingBundle);

		TestObscura restoredPending = new TestObscura();
		restoredPending.restoreFromBundle(pendingBundle);
		assertTrue(restoredPending.summonPendingForTest());
		assertFalse(restoredPending.summonConsumedForTest());
		assertEquals(0, restoredPending.summonChargeTurnsForTest());

		TestObscura summoned = new TestObscura();
		summoned.armSummonForTest();
		summoned.summonCell = 40;
		summoned.actForTest();
		summoned.actForTest();
		summoned.actForTest();
		Bundle summonedBundle = new Bundle();
		summoned.storeInBundle(summonedBundle);

		TestObscura restoredSummoned = new TestObscura();
		restoredSummoned.restoreFromBundle(summonedBundle);
		assertTrue(restoredSummoned.summonConsumedForTest());
		assertFalse(restoredSummoned.summonPendingForTest());
		assertEquals(0, restoredSummoned.summonChargeTurnsForTest());
		assertEquals(summoned.wildDreadIdForTest(), restoredSummoned.wildDreadIdForTest());

		TestWildDread linked = new TestWildDread();
		linked.bindToOwner(77);
		Bundle linkedBundle = new Bundle();
		linked.storeInBundle(linkedBundle);
		TestWildDread restoredLinked = new TestWildDread();
		restoredLinked.restoreFromBundle(linkedBundle);
		assertEquals(77, restoredLinked.ownerIdForTest());
	}

	@Test
	public void downedMinionAndRevivalCountdownSurviveBundleRoundTrip() {
		TestObscura owner = new TestObscura();
		TestWildDread downed = new TestWildDread();
		downed.owner = owner;
		downed.bindToOwner(owner.id());
		downed.pos = 19;
		downed.dieForTest(new Object());

		Bundle bundle = new Bundle();
		owner.storeInBundle(bundle);
		TestObscura restored = new TestObscura();
		restored.restoreFromBundle(bundle);

		assertTrue(restored.revivalPendingForTest());
		assertNotNull(restored.downedDreadForTest());
		assertTrue(restored.downedDreadForTest().downed());
		assertEquals(19, restored.downedDreadForTest().deathCell());
		assertEquals(owner.id(), restored.downedDreadForTest().ownerId());
		restored.reviveCell = 20;
		assertTrue(restored.advanceRevivalForTest());
		assertNull(restored.downedDreadForTest());
		assertNotNull(restored.revived);
		assertEquals(20, restored.revived.pos);
	}

	private static final class TestObscura extends Obscura {
		private boolean revealEnemyDuringBaseAct;
		private boolean wakeDuringBaseAct;
		private int summonCell = -1;
		private int baseActCalls;
		private int spawnCount;
		private TestWildDread spawned;
		private int reviveCell = -1;
		private WildDread revived;
		private WildDread owned;
		private boolean ownerDeathFinished;
		private final ArrayList<WildDread> activeOwned = new ArrayList<>();
		private Char heldStasisAlly;
		private int heldDiscardCount;

		private TestObscura() {
			state = WANDERING;
		}

		private float attackDelayForTest() {
			return attackDelay();
		}

		private boolean actForTest() {
			return act();
		}

		private void armSummonForTest() {
			armSummon();
		}

		private boolean summonPendingForTest() {
			return summonPending();
		}

		private boolean summonConsumedForTest() {
			return summonConsumed();
		}

		private int summonChargeTurnsForTest() {
			return summonChargeTurns();
		}

		private int wildDreadIdForTest() {
			return wildDreadId();
		}

		private boolean stasisReplacementPendingForTest() {
			return stasisReplacementPending();
		}

		private boolean revivalPendingForTest() {
			return revivalPending();
		}

		private WildDread downedDreadForTest() {
			return downedDread();
		}

		private boolean advanceRevivalForTest() {
			return advanceRevival();
		}

		private void scheduleRevivalForTest(WildDread dread) {
			scheduleRevival(dread);
		}

		private void dieForTest(Object cause) {
			die(cause);
		}

		@Override
		protected boolean performBaseAct() {
			baseActCalls++;
			spend(TICK);
			if (revealEnemyDuringBaseAct) {
				enemySeen = true;
			}
			if (wakeDuringBaseAct) {
				state = HUNTING;
				enemySeen = true;
			}
			return true;
		}

		@Override
		protected int findSummonCell() {
			return summonCell;
		}

		@Override
		protected WildDread createWildDread() {
			return new TestWildDread();
		}

		@Override
		protected void addWildDreadToLevel(WildDread dread) {
			spawnCount++;
			spawned = (TestWildDread) dread;
			activeOwned.add(dread);
		}

		@Override
		protected int findReviveCell(WildDread dread) {
			return reviveCell;
		}

		@Override
		protected void addRevivedDreadToLevel(WildDread dread) {
			revived = dread;
			activeOwned.add(dread);
		}

		protected ArrayList<WildDread> activeOwnedDreads() {
			ArrayList<WildDread> active = new ArrayList<>();
			for (WildDread dread : activeOwned) {
				if (!dread.downed() && dread.HP > 0) {
					active.add(dread);
				}
			}
			return active;
		}

		@Override
		protected WildDread ownedWildDread() {
			return owned;
		}

		protected Char stasisAlly() {
			return heldStasisAlly;
		}

		protected void discardHeldStasisAlly(WildDread expected) {
			if (heldStasisAlly == expected) {
				heldStasisAlly = null;
				heldDiscardCount++;
			}
		}

		@Override
		protected void finishOwnerDeath(Object cause) {
			ownerDeathFinished = true;
		}
	}

	public static class TestWildDread extends WildDread {
		private Obscura owner;
		private Char stasisOwner;
		private boolean removedForRevival;
		private boolean permanentDeath;
		private boolean abandoned;
		private int abandonCalls;
		private boolean attackResult = true;
		private int attackCalls;

		private float attackDelayForTest() {
			return attackDelay();
		}

		private int completedAttackGrowthForTest() {
			return completedAttackGrowth();
		}

		private boolean doAttackForTest(Char target) {
			return doAttack(target);
		}

		private void finishAnimatedAttackForTest(Char target) {
			enemy = target;
			onAttackComplete();
		}

		private int ownerIdForTest() {
			return ownerId();
		}

		private boolean downedForTest() {
			return downed();
		}

		private void dieForTest(Object cause) {
			die(cause);
		}

		private boolean actForTest() {
			return act();
		}

		@Override
		public boolean attack(Char target, float dmgMulti, float dmgBonus, float accMulti,
				DamageTag... damageTags) {
			attackCalls++;
			return attackResult;
		}

		@Override
		protected Obscura owner() {
			return owner != null ? owner : super.owner();
		}

		@Override
		protected Char stasisAlly() {
			return stasisOwner;
		}

		@Override
		protected boolean performBaseAct() {
			return true;
		}

		@Override
		protected void removeForRevival() {
			removedForRevival = true;
		}

		@Override
		protected void finishPermanentDeath(Object cause) {
			permanentDeath = true;
		}

		@Override
		protected void finishAbandonment() {
			abandonCalls++;
			abandoned = true;
		}
	}

	public static class TestAllyBuff extends AllyBuff {
	}

	private static final class TestOrdinaryBuff extends Buff {
	}

	private static final class TestEnemyMob extends Mob {
	}

	private static final class TestAttackSprite extends CharSprite {
		private int attackCalls;

		private TestAttackSprite() {
			visible = true;
		}

		@Override
		public void attack(int cell) {
			attackCalls++;
		}
	}

	private static String source(String fileName) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path sourceRoot = workingDirectory;
		if (!Files.isDirectory(sourceRoot.resolve("src/main/java"))) {
			sourceRoot = workingDirectory.resolve("core");
		}
		if (!Files.isDirectory(sourceRoot.resolve("src/main/java"))) {
			throw new AssertionError("Could not locate core source root from working directory: " + workingDirectory);
		}
		Path source = sourceRoot.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs").resolve(fileName);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
