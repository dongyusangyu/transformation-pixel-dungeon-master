package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HuntressBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HuntressBossTest {

	@Test
	public void sublimationTypeUsesIndependentHuntressMarker() {
		assertEquals("HUNTRESS", HuntressBoss.SUBLIMATION_TYPE);
	}

	@Test
	public void incomingDamageIsCappedAtThirty() {
		assertEquals(0, HuntressBoss.cappedIncomingDamage(0));
		assertEquals(30, HuntressBoss.cappedIncomingDamage(30));
		assertEquals(30, HuntressBoss.cappedIncomingDamage(31));
		assertEquals(30, HuntressBoss.cappedIncomingDamage(100));
	}

	@Test
	public void galeCountdownAdvancesOnlyOnActionableSniperTurns() {
		assertEquals(5, HuntressBoss.GALE_INTERVAL);
		assertEquals(4, HuntressBoss.advanceGaleCountdown(5, true));
		assertEquals(1, HuntressBoss.advanceGaleCountdown(2, true));
		assertEquals(0, HuntressBoss.advanceGaleCountdown(1, true));
		assertEquals(0, HuntressBoss.advanceGaleCountdown(0, true));
		assertEquals(3, HuntressBoss.advanceGaleCountdown(3, false));

		assertTrue(HuntressBoss.shouldAimGale(HuntressBoss.Phase.SNIPER, 0, true));
		assertFalse(HuntressBoss.shouldAimGale(HuntressBoss.Phase.SNIPER, 1, true));
		assertFalse(HuntressBoss.shouldAimGale(HuntressBoss.Phase.SNIPER, 0, false));
		assertFalse(HuntressBoss.shouldAimGale(HuntressBoss.Phase.WARDEN, 0, true));
	}

	@Test
	public void galeTargetEligibilityRequiresEveryCombatCondition() {
		assertTrue(HuntressBoss.isGaleTargetEligible(true, true, true, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(false, true, true, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(true, false, true, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(true, true, false, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(true, true, true, true));
	}

	@Test
	public void galeTargetPickerUsesRequestedIndexAndHandlesNoTargets() {
		TestTarget first = new TestTarget();
		TestTarget second = new TestTarget();
		ArrayList<Char> targets = new ArrayList<>(Arrays.asList(first, second));

		assertSame(first, HuntressBoss.pickGaleTarget(targets, 0));
		assertSame(second, HuntressBoss.pickGaleTarget(targets, 1));
		assertNull(HuntressBoss.pickGaleTarget(Collections.<Char>emptyList(), 0));
	}

	@Test
	public void galeCountdownPersistenceRoundTripsAndLegacyBundlesDefaultToInterval() {
		TestHuntress boss = new TestHuntress();
		boss.setGaleTurnsRemainingForTest(2);
		Bundle saved = new Bundle();
		boss.storeInBundle(saved);

		assertEquals(2, HuntressBoss.restoredGaleTurns(saved));
		assertEquals(5, HuntressBoss.restoredGaleTurns(new Bundle()));
		Bundle legacy = new Bundle();
		legacy.put("normal_shots", 3);
		assertEquals(5, HuntressBoss.restoredGaleTurns(legacy));
		assertEquals(2, boss.galeTurnsRemaining());
	}

	@Test
	public void normalShotRangeAndHawkRelayBoundariesMatchDesign() {
		assertEquals(6, HuntressBoss.BASE_NORMAL_SHOT_RANGE);
		assertEquals(4, HuntressBoss.HAWK_RELAY_RADIUS);
		assertEquals(HuntressBoss.BASE_NORMAL_SHOT_RANGE,
				new HuntressBoss().normalShotRange());

		assertTrue(HuntressBoss.normalShotEligible(true, false, true, 6,
				HuntressBoss.BASE_NORMAL_SHOT_RANGE));
		assertFalse(HuntressBoss.normalShotEligible(true, false, true, 7,
				HuntressBoss.BASE_NORMAL_SHOT_RANGE));
		assertTrue(HuntressBoss.withinHawkRelayRadius(4,
				HuntressBoss.HAWK_RELAY_RADIUS));
		assertFalse(HuntressBoss.withinHawkRelayRadius(5,
				HuntressBoss.HAWK_RELAY_RADIUS));
		assertFalse(HuntressBoss.normalShotEligible(false, true, false, 9,
				HuntressBoss.BASE_NORMAL_SHOT_RANGE));
	}

	@Test
	public void tacticalDecisionCoversVisibilityRelayLineAndDistanceBoundaries() {
		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(false, false, true, 1, 6, 0));
		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(true, false, false, 1, 6, 0));

		assertEquals(HuntressBoss.TacticalAction.MELEE,
				HuntressBoss.tacticalAction(true, false, true, 1, 6, 0));
		assertEquals(HuntressBoss.TacticalAction.MELEE,
				HuntressBoss.tacticalAction(true, false, true, 1, 6, 1));

		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(true, false, true, 2, 6, 0));
		assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
				HuntressBoss.tacticalAction(true, false, true, 2, 6, 1));
		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(true, false, true, 4, 6, 0));
		assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
				HuntressBoss.tacticalAction(true, false, true, 4, 6, 1));

		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(true, false, true, 6, 6, 0));
		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(true, false, true, 7, 6, 0));
		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(false, true, true, 12, 6, 1));
	}

	@Test
	public void hawkRelayMakesTargetSeenForHuntingWithoutChangingBossFov() {
		assertTrue(HuntressBoss.enemySeenForHunting(false, true));
		assertFalse(HuntressBoss.enemySeenForHunting(false, false));
	}

	@Test
	public void hawkDiscoversEnemyBehindWallButBossCannotShootThroughIt() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		TestLevel level = null;
		NoResistanceTarget target = null;
		try {
			level = testLevel(9, 7);
			int bossCell = 28;
			int wallCell = 29;
			int hawkCell = 31;
			int targetCell = 32;
			level.map[wallCell] = Terrain.WALL;
			level.passable[wallCell] = false;
			level.solid[wallCell] = true;
			Dungeon.level = level;

			TestHuntress boss = new TestHuntress();
			boss.pos = bossCell;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.state = boss.WANDERING;
			boss.resetFieldOfView();
			HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
			hawk.pos = hawkCell;
			target = new NoResistanceTarget();
			target.pos = targetCell;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			level.mobs.add(target);

			assertTrue(boss.isHawkRelayedForTest(target));
			assertFalse(new Ballistica(boss.pos, target.pos,
					Ballistica.PROJECTILE).collisionPos == target.pos);
			assertSame(target, boss.chooseEnemyForTest());
			assertSame(boss.HUNTING, boss.state);
			assertFalse(boss.canRangedAttack(target));
		} finally {
			if (level != null && target != null) {
				level.mobs.remove(target);
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void relayOnlyTargetDoesNotTriggerGaleWhenCountdownIsReady() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(25, 7);
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(0);
			HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
			hawk.pos = 70;
			TestTarget target = new TestTarget();
			target.pos = 96;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);

			assertTrue(boss.isHawkRelayedForTest(target));
			assertFalse(boss.bossCanSeeForTest(target));
			assertTrue(new Ballistica(boss.pos, target.pos,
					Ballistica.PROJECTILE).collisionPos == target.pos);

			assertTrue(boss.actForTest());
			assertEquals(-1, boss.galeTargetForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(0, boss.galeAims);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void fifthActionableTurnAimsAndFollowingTurnFiresThenResets() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		TestTarget target = null;
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(HuntressBoss.GALE_INTERVAL);
			target = new NoResistanceTarget();
			target.pos = 44;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			Dungeon.level.mobs.add(target);

			for (int remaining = 4; remaining >= 1; remaining--) {
				assertTrue(boss.actForTest());
				assertEquals(remaining, boss.galeTurnsRemaining());
				assertEquals(-1, boss.galeTargetForTest());
				assertEquals(0, boss.galeAims);
			}

			assertTrue(boss.actForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(target.pos, boss.galeTargetForTest());
			assertEquals(1, boss.galeAims);

			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GALE_INTERVAL, boss.galeTurnsRemaining());
			assertEquals(-1, boss.galeTargetForTest());
			assertEquals(1, boss.galeAims);
		} finally {
			if (Dungeon.level != null && target != null) {
				Dungeon.level.mobs.remove(target);
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void deferredControlTurnsDoNotAdvanceGaleOrClearPendingTarget() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 31;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(3);
			registerActor(boss, registeredActors);

			boss.paralysed = 1;
			assertTrue(boss.actForTest());
			assertEquals(3, boss.galeTurnsRemaining());

			boss.paralysed = 0;
			boss.state = boss.SLEEPING;
			assertTrue(boss.actForTest());
			assertEquals(3, boss.galeTurnsRemaining());

			boss.state = boss.FLEEING;
			boss.setGaleTargetForTest(32);
			assertTrue(boss.actForTest());
			assertEquals(3, boss.galeTurnsRemaining());
			assertEquals(32, boss.galeTargetForTest());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void readyCountdownWaitsForRealBossFovThenAims() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(25, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(0);
			TestTarget target = new TestTarget();
			target.pos = 96;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);

			assertTrue(boss.actForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(-1, boss.galeTargetForTest());

			target.pos = 80;
			assertTrue(boss.actForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(target.pos, boss.galeTargetForTest());
			assertEquals(1, boss.galeAims);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void pendingGaleDoesNotFireAfterTargetLeavesRealBossFov() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(25, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(0);
			TestTarget target = new TestTarget();
			target.pos = 80;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);

			assertTrue(boss.actForTest());
			assertEquals(target.pos, boss.galeTargetForTest());
			target.pos = 96;

			assertTrue(boss.actForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(-1, boss.galeTargetForTest());
			assertEquals(0, boss.galeResets);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void tacticalDecisionCacheOnlyMatchesItsBoundTarget() {
		Char firstTarget = new HuntressBoss.DistractingHawk();
		Char secondTarget = new HuntressBoss.DistractingHawk();
		HuntressBoss.TacticalDecision decision = new HuntressBoss.TacticalDecision();

		decision.set(firstTarget, HuntressBoss.TacticalAction.SEEK_COVER);

		assertTrue(decision.matches(firstTarget));
		assertFalse(decision.matches(secondTarget));
		assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
				decision.actionFor(firstTarget));
		assertNull(decision.actionFor(secondTarget));
	}

	@Test
	public void clearingTacticalDecisionRemovesTargetAndActionTogether() {
		Char target = new HuntressBoss.DistractingHawk();
		HuntressBoss.TacticalDecision decision = new HuntressBoss.TacticalDecision();
		decision.set(target, HuntressBoss.TacticalAction.SHOOT);

		decision.clear();

		assertFalse(decision.matches(target));
		assertNull(decision.actionFor(target));
	}

	@Test
	public void damageRangesDistinguishBothPhaseShotsMeleeAndGale() {
		assertArrayEquals(new int[]{6, 14}, HuntressBoss.damageRange(
				HuntressBoss.Phase.SNIPER, false, false));
		assertArrayEquals(new int[]{10, 16}, HuntressBoss.damageRange(
				HuntressBoss.Phase.SNIPER, true, false));
		assertArrayEquals(new int[]{20, 28}, HuntressBoss.damageRange(
				HuntressBoss.Phase.SNIPER, false, true));
		assertArrayEquals(new int[]{10, 16}, HuntressBoss.damageRange(
				HuntressBoss.Phase.WARDEN, false, false));
		assertArrayEquals(new int[]{12, 20}, HuntressBoss.damageRange(
				HuntressBoss.Phase.WARDEN, true, false));
	}

	@Test
	public void armorModesDistinguishSniperShotsMeleeWardenAndGale() {
		assertTrue(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.SNIPER, false, false));
		assertFalse(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.SNIPER, true, false));
		assertTrue(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.SNIPER, true, true));
		assertFalse(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.WARDEN, false, false));
		assertFalse(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.WARDEN, true, false));
		assertTrue(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.WARDEN, false, true));

		assertEquals(0, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, false, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, true, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.WARDEN, false, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.WARDEN, true, false, 20));
		assertEquals(0, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, true, true, 20));
	}

	@Test
	public void harmfulPlantEffectOnlyAppliesToOneInThreeWardenNormalShots() {
		assertTrue(HuntressBoss.shouldApplyHarmfulPlantEffect(
				HuntressBoss.Phase.WARDEN, false, false, 0));
		assertFalse(HuntressBoss.shouldApplyHarmfulPlantEffect(
				HuntressBoss.Phase.SNIPER, false, false, 0));
		assertFalse(HuntressBoss.shouldApplyHarmfulPlantEffect(
				HuntressBoss.Phase.WARDEN, true, false, 0));
		assertFalse(HuntressBoss.shouldApplyHarmfulPlantEffect(
				HuntressBoss.Phase.WARDEN, false, true, 0));
		assertFalse(HuntressBoss.shouldApplyHarmfulPlantEffect(
				HuntressBoss.Phase.WARDEN, false, false, 1));
		assertFalse(HuntressBoss.shouldApplyHarmfulPlantEffect(
				HuntressBoss.Phase.WARDEN, false, false, 2));
	}

	@Test
	public void phaseTransitionUpdatesBossPresentation() {
		TestHuntress boss = new TestHuntress();

		assertEquals(HuntressBoss.Phase.SNIPER, boss.phase());
		assertEquals(15, boss.viewDistance);
		assertEquals(HuntressBossSprite.class, boss.spriteClass);

		boss.setGaleTurnsRemainingForTest(2);
		boss.setGaleTargetForTest(17);
		assertTrue(boss.enterWardenPhase());

		assertEquals(HuntressBoss.Phase.WARDEN, boss.phase());
		assertEquals(HuntressBoss.GALE_INTERVAL, boss.galeTurnsRemaining());
		assertEquals(-1, boss.galeTargetForTest());
		assertFalse(boss.enterWardenPhase());
	}

	@Test
	public void normalAndGaleProjectilesUseNonSpinningItemTypes() {
		assertEquals(SpiritBow.SpiritArrow.class, HuntressBoss.projectileClassFor(false));
		assertEquals(Dart.class, HuntressBoss.projectileClassFor(true));
	}

	@Test
	public void distractingHawkOnlyDealsOneDamageAndIsBossMinion() {
		HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();

		assertEquals(1, hawk.damageRoll());
		assertEquals(1, hawk.damageAfterArmor(0));
		assertEquals(1, hawk.damageAfterArmor(7));
		assertEquals(0, hawk.EXP);
		assertEquals(Char.Alignment.ENEMY, hawk.alignment);
		assertTrue(hawk.properties().contains(Char.Property.BOSS_MINION));
	}

	@Test
	public void distractingHawkBlindsAndCripplesOnHit() {
		assertEquals(2f, HuntressBoss.DistractingHawk.DISTRACTION_DURATION, 0f);
		assertEquals(Blindness.class, HuntressBoss.DistractingHawk.distractionEffects()[0]);
		assertEquals(Cripple.class, HuntressBoss.DistractingHawk.distractionEffects()[1]);
	}

	@Test
	public void distractingHawkAttackProcAppliesItsFullHitContract() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		NoResistanceTarget target = null;
		Blindness blindness = null;
		Cripple cripple = null;
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			target = new NoResistanceTarget();
			target.pos = 32;
			blindness = new Blindness();
			cripple = new Cripple();
			assertTrue(blindness.attachTo(target));
			assertTrue(cripple.attachTo(target));
			blindness.clearTime();
			cripple.clearTime();
			assertEquals(0f, blindness.cooldown(), 0f);
			assertEquals(0f, cripple.cooldown(), 0f);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);

			int damage = hawk.attackProcForTest(target, 17);

			assertEquals(1, damage);
			assertSame(blindness, target.buff(Blindness.class));
			assertSame(cripple, target.buff(Cripple.class));
			assertEquals(HuntressBoss.DistractingHawk.DISTRACTION_DURATION,
					blindness.cooldown(), 0f);
			assertEquals(HuntressBoss.DistractingHawk.DISTRACTION_DURATION,
					cripple.cooldown(), 0f);
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
					hawk.hawkMode());
			assertEquals(target.id(), hawk.retreatTargetId());
		} finally {
			Dungeon.hero = null;
			try {
				try {
					if (blindness != null && target != null
							&& target.buff(Blindness.class) == blindness) {
						blindness.detach();
					}
				} finally {
					try {
						if (cripple != null && target != null
								&& target.buff(Cripple.class) == cripple) {
							cripple.detach();
						}
					} finally {
						removeRegisteredActors(registeredActors);
					}
				}
			} finally {
				Dungeon.level = previousLevel;
				Dungeon.hero = previousHero;
			}
		}
	}

	@Test
	public void distractingHawkTargetEligibilityRejectsEveryInvalidCondition() {
		assertTrue(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, true, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				false, true, true, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, false, true, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, false, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, true, true, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, true, false, true));
	}

	@Test
	public void distractingHawkTargetPreferenceIsNearestThenLowestActorId() {
		assertTrue(HuntressBoss.DistractingHawk.preferTarget(2, 20, 3, 10));
		assertFalse(HuntressBoss.DistractingHawk.preferTarget(4, 1, 3, 10));
		assertTrue(HuntressBoss.DistractingHawk.preferTarget(3, 9, 3, 10));
		assertFalse(HuntressBoss.DistractingHawk.preferTarget(3, 11, 3, 10));
	}

	@Test
	public void distractingHawkModeTransitionsAndRetreatBoundariesMatchDesign() {
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
				HuntressBoss.DistractingHawk.modeAfterHit());
		assertTrue(HuntressBoss.DistractingHawk.shouldRetreat(true, 1));
		assertFalse(HuntressBoss.DistractingHawk.shouldRetreat(false, 1));
		assertFalse(HuntressBoss.DistractingHawk.shouldRetreat(true, 2));
		assertFalse(HuntressBoss.DistractingHawk.inRetreatBand(1));
		assertTrue(HuntressBoss.DistractingHawk.inRetreatBand(2));
		assertTrue(HuntressBoss.DistractingHawk.inRetreatBand(4));
		assertFalse(HuntressBoss.DistractingHawk.inRetreatBand(5));
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.SEEKING,
				HuntressBoss.DistractingHawk.modeAfterRetreat(true));
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
				HuntressBoss.DistractingHawk.modeAfterRetreat(false));
	}

	@Test
	public void distractingHawkRetreatStateRoundTripsAndOldBundlesUseDefaults() {
		HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
		TestTarget target = new TestTarget();
		hawk.beginRetreatFrom(target);
		Bundle saved = new Bundle();
		hawk.storeInBundle(saved);

		HuntressBoss.DistractingHawk restored = new HuntressBoss.DistractingHawk();
		restored.restoreFromBundle(saved);
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
				restored.hawkMode());
		assertEquals(target.id(), restored.retreatTargetId());

		HuntressBoss.DistractingHawk legacy = new HuntressBoss.DistractingHawk();
		legacy.restoreFromBundle(new Bundle());
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.SEEKING,
				legacy.hawkMode());
		assertEquals(-1, legacy.retreatTargetId());
	}

	@Test
	public void distractingHawkDropsAnUnregisteredRestoredRetreatTarget() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			HuntressBoss.DistractingHawk saved = new HuntressBoss.DistractingHawk();
			saved.beginRetreatFrom(new TestTarget());
			Bundle bundle = new Bundle();
			saved.storeInBundle(bundle);
			bundle.put("retreat_target_id", Integer.MAX_VALUE);
			assertNull(Actor.findCharById(Integer.MAX_VALUE));

			TestHawk restored = new TestHawk();
			restored.restoreFromBundle(bundle);
			restored.pos = 31;
			restored.setAllVisibleForTest();
			Arrays.fill(Dungeon.level.passable, false);
			Dungeon.level.passable[32] = true;
			registerActor(restored, registeredActors);
			restored.state = restored.HUNTING;

			restored.HUNTING.act(false, false);

			assertEquals(-1, restored.retreatTargetId());
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					restored.hawkMode());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkSelectsLowestIdWhenEligibleTargetsAreEquidistant() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget lowerId = new TestTarget();
			lowerId.pos = 21;
			lowerId.alignment = Char.Alignment.ENEMY2;
			TestTarget higherId = new TestTarget();
			higherId.pos = 23;
			higherId.alignment = Char.Alignment.ENEMY2;
			registerActor(hawk, registeredActors);
			registerActor(lowerId, registeredActors);
			registerActor(higherId, registeredActors);
			hawk.setVisibleForTest(lowerId.pos, higherId.pos);
			Arrays.fill(Dungeon.level.passable, false);
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(false, false);

			assertSame(lowerId, hawk.enemyForTest());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkWaitsInsteadOfAttackingItsCrippledRetreatTarget() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget target = new TestTarget();
			target.pos = 33;
			target.alignment = Char.Alignment.ENEMY2;
			assertTrue(new Cripple().attachTo(target));
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			hawk.setVisibleForTest(target.pos);
			Arrays.fill(Dungeon.level.passable, false);
			hawk.beginRetreatFrom(target);
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(true, false);

			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					hawk.hawkMode());
			assertEquals(0, hawk.attacks);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkRetreatsOneStepAtDistanceOneThenStopsAtTwo() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget target = new TestTarget();
			target.pos = 32;
			target.alignment = Char.Alignment.ENEMY2;
			assertTrue(new Cripple().attachTo(target));
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			hawk.setAllVisibleForTest();
			hawk.beginRetreatFrom(target);
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(true, false);

			assertEquals(2, Dungeon.level.distance(hawk.pos, target.pos));
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
					hawk.hawkMode());

			hawk.HUNTING.act(true, false);

			assertTrue(HuntressBoss.DistractingHawk.inRetreatBand(
					Dungeon.level.distance(hawk.pos, target.pos)));
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					hawk.hawkMode());
			assertEquals(0, hawk.attacks);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkCustomAiLivesOnlyInsideBaseHuntingState() {
		HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
		assertEquals(Mob.Hunting.class, hawk.HUNTING.getClass().getSuperclass());
		try {
			HuntressBoss.DistractingHawk.class.getDeclaredMethod("act");
			throw new AssertionError("DistractingHawk must leave Mob.act control priority intact");
		} catch (NoSuchMethodException expected) {
			// Expected: paralysis, fear, confusion and sleep remain owned by Mob.act/state.
		}
	}

	@Test
	public void distractingHawkAmokUsesBaseTargetingWithoutAdvancingHawkMode() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			TestTarget sameAlignment = new TestTarget();
			sameAlignment.pos = 32;
			sameAlignment.alignment = Char.Alignment.ENEMY;
			TestTarget normallyHostile = new TestTarget();
			normallyHostile.pos = 33;
			normallyHostile.alignment = Char.Alignment.ALLY;
			hawk.beginRetreatFrom(normallyHostile);
			int retreatId = hawk.retreatTargetId();
			assertTrue(new Amok().attachTo(hawk));
			registerActor(hawk, registeredActors);
			registerActor(sameAlignment, registeredActors);
			registerActor(normallyHostile, registeredActors);
			Dungeon.level.mobs.add(sameAlignment);
			Dungeon.level.mobs.add(normallyHostile);
			hawk.setAllVisibleForTest();
			Arrays.fill(Dungeon.level.passable, false);
			hawk.state = hawk.HUNTING;

			assertSame(sameAlignment, hawk.chooseEnemyForTest());
			hawk.chooseEnemyAndHuntForTest();

			assertSame(sameAlignment, hawk.attackedTarget);
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
					hawk.hawkMode());
			assertEquals(retreatId, hawk.retreatTargetId());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void fadeleafMapsToAdjustedTeleportBoon() {
		assertEquals(HuntressBoss.WardenBoon.FADELEAF,
				HuntressBoss.boonForPlant(new Fadeleaf()));
	}

	@Test
	public void harmfulPlantPoolMatchesNaturesWrathWithoutPositiveSeeds() {
		assertEquals(5, HuntressBoss.harmfulPlantPool().length);
		assertEquals(Blindweed.class, HuntressBoss.harmfulPlantPool()[0]);
		assertEquals(Firebloom.class, HuntressBoss.harmfulPlantPool()[1]);
		assertEquals(Icecap.class, HuntressBoss.harmfulPlantPool()[2]);
		assertEquals(Sorrowmoss.class, HuntressBoss.harmfulPlantPool()[3]);
		assertEquals(Stormvine.class, HuntressBoss.harmfulPlantPool()[4]);
	}

	@Test
	public void wardenBoonCadenceAndOneTimeHawkSpawnsMatchDesign() {
		assertEquals(10, HuntressBoss.WARDEN_BOON_INTERVAL);
		assertEquals(1, HuntressBoss.hawksSpawnedAtFightStart());
		assertEquals(1, HuntressBoss.hawksSpawnedAtWardenTransition());
	}

	@Test
	public void closeQuartersKnockbackHasOneInThreeChanceForHeroOrAlly() {
		assertEquals(3, HuntressBoss.CLOSE_QUARTERS_KNOCKBACK_DISTANCE);
		assertTrue(HuntressBoss.shouldKnockBackCloseTarget(true, true, 0));
		assertFalse(HuntressBoss.shouldKnockBackCloseTarget(true, true, 1));
		assertFalse(HuntressBoss.shouldKnockBackCloseTarget(true, true, 2));
		assertFalse(HuntressBoss.shouldKnockBackCloseTarget(false, true, 0));
		assertFalse(HuntressBoss.shouldKnockBackCloseTarget(true, false, 0));
	}

	@Test
	public void baseMobControlStatesPreemptHuntressCustomActions() {
		assertTrue(HuntressBoss.shouldDeferCustomActions(1, false, false, false));
		assertTrue(HuntressBoss.shouldDeferCustomActions(0, true, false, false));
		assertTrue(HuntressBoss.shouldDeferCustomActions(0, false, true, false));
		assertTrue(HuntressBoss.shouldDeferCustomActions(0, false, false, true));
		assertFalse(HuntressBoss.shouldDeferCustomActions(0, false, false, false));
	}

	@Test
	public void successfulFireAbsorptionStartsCooldownAndKeepsFiveTurnImbue() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertNotNull(boss.buff(FireImbue.class));
		Bundle fireImbueState = new Bundle();
		boss.buff(FireImbue.class).storeInBundle(fireImbueState);
		assertEquals(5f, fireImbueState.getFloat("left"), 0f);
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		assertNotNull(cooldown);
		assertEquals(HuntressBoss.FIRE_ABSORPTION_COOLDOWN, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void failedFireAbsorptionStillRemovesBurningAndStartsCooldown() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(1);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertNull(boss.buff(FireImbue.class));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		assertNotNull(cooldown);
		assertEquals(20f, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void burningDuringAbsorptionCooldownAttachesNormallyWithoutRefreshingIt() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(1);
		assertFalse(new Burning().attachTo(boss));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		float remaining = cooldown.cooldown();

		Burning secondBurning = new Burning();
		assertTrue(secondBurning.attachTo(boss));

		assertSame(secondBurning, boss.buff(Burning.class));
		assertSame(cooldown, boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
		assertEquals(remaining, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void cooldownAllowsBurningDespiteTemporaryFireImbueImmunity() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);
		assertFalse(new Burning().attachTo(boss));
		assertNotNull(boss.buff(FireImbue.class));
		assertNotNull(boss.buff(HuntressBoss.FireAbsorptionCooldown.class));

		Burning secondBurning = new Burning();
		assertTrue(secondBurning.attachTo(boss));

		assertSame(secondBurning, boss.buff(Burning.class));
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void cooldownPreservesOtherBurningImmunitySources() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);
		assertFalse(new Burning().attachTo(boss));
		assertNotNull(boss.buff(FireImbue.class));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		float remaining = cooldown.cooldown();
		boss.addProperties(Char.Property.FIERY);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertTrue(boss.isImmune(Burning.class));
		assertSame(cooldown, boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
		assertEquals(remaining, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void fireAbsorptionCooldownUsesIndependentFlavourBuffWorldTime() {
		assertEquals(20f, HuntressBoss.FIRE_ABSORPTION_COOLDOWN, 0f);
		assertEquals(FlavourBuff.class,
				HuntressBoss.FireAbsorptionCooldown.class.getSuperclass());
		HuntressBoss.FireAbsorptionCooldown marker =
				new HuntressBoss.FireAbsorptionCooldown();
		assertEquals(Buff.buffType.NEUTRAL, marker.type);
		assertFalse(marker.announced);

		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(1);
		assertFalse(new Burning().attachTo(boss));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		boss.setControlledForTest();

		assertTrue(cooldown.act());

		assertNull(boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
	}

	@Test
	public void nonBurningFireBuffDoesNotStartAbsorptionCooldown() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);

		assertTrue(new FireImbue().attachTo(boss));

		assertNotNull(boss.buff(FireImbue.class));
		assertNull(boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
		assertEquals(0, boss.absorptionRolls());
	}

	private static class DeterministicFireAbsorptionHuntress extends HuntressBoss {

		private final int roll;
		private int absorptionRolls;

		private DeterministicFireAbsorptionHuntress(int roll) {
			this.roll = roll;
		}

		@Override
		protected int fireAbsorptionRoll() {
			absorptionRolls++;
			return roll;
		}

		private int absorptionRolls() {
			return absorptionRolls;
		}

		private void setControlledForTest() {
			paralysed = 1;
			state = SLEEPING;
		}
	}

	private static TestLevel testLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.map, Terrain.EMPTY);
		level.buildFlagMaps();
		return level;
	}

	private static <T extends Actor> T registerActor(T actor,
			ArrayList<Actor> registeredActors) {
		Actor.add(actor);
		registeredActors.add(actor);
		return actor;
	}

	private static void removeRegisteredActors(ArrayList<Actor> registeredActors) {
		for (int i = registeredActors.size() - 1; i >= 0; i--) {
			Actor.remove(registeredActors.get(i));
		}
	}

	private static class TestHuntress extends HuntressBoss {

		private int galeAims;
		private int galeResets;

		private void prepareForGaleAct(int remaining) {
			state = HUNTING;
			sprite = new HeadlessCharSprite();
			setPrivateBoolean("encounterDelay", false);
			setGaleTurnsRemainingForTest(remaining);
			setGaleTargetForTest(-1);
		}

		private boolean actForTest() {
			int before = galeTurnsRemaining();
			boolean result = act();
			if (before == 0 && galeTurnsRemaining() == HuntressBoss.GALE_INTERVAL) {
				galeResets++;
			}
			return result;
		}

		private void resetFieldOfView() {
			fieldOfView = new boolean[Dungeon.level.length()];
		}

		private void setVisibleForTest(int cell) {
			fieldOfView[cell] = true;
		}

		private boolean bossCanSeeForTest(Char target) {
			return bossCanSee(target);
		}

		private boolean isHawkRelayedForTest(Char target) {
			return isHawkRelayed(target);
		}

		private Char chooseEnemyForTest() {
			return chooseEnemy();
		}

		private void setGaleTurnsRemainingForTest(int remaining) {
			setPrivateInt("galeTurnsRemaining", remaining);
		}

		private void setGaleTargetForTest(int cell) {
			setPrivateInt("galeTarget", cell);
		}

		private int galeTargetForTest() {
			return getPrivateInt("galeTarget");
		}

		private void setPrivateInt(String name, int value) {
			try {
				Field field = HuntressBoss.class.getDeclaredField(name);
				field.setAccessible(true);
				field.setInt(this, value);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		private int getPrivateInt(String name) {
			try {
				Field field = HuntressBoss.class.getDeclaredField(name);
				field.setAccessible(true);
				return field.getInt(this);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		private void setPrivateBoolean(String name, boolean value) {
			try {
				Field field = HuntressBoss.class.getDeclaredField(name);
				field.setAccessible(true);
				field.setBoolean(this, value);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
				DamageTag... damageTags) {
			return false;
		}

		@Override
		protected boolean getCloser(int target) {
			return false;
		}

		@Override
		protected void announceGaleAim() {
			galeAims++;
		}
	}

	private static class HeadlessCharSprite extends CharSprite {

		private HeadlessCharSprite() {
			visible = true;
		}

		@Override
		public void showLost() {
			// No scene exists in headless actor-cycle tests.
		}
	}

	private static class TestTarget extends Mob {

		{
			HP = HT = 20;
		}

		@Override
		public int damageRoll() {
			return 0;
		}

		@Override
		public int attackSkill(Char target) {
			return 0;
		}
	}

	private static class NoResistanceTarget extends TestTarget {

		@Override
		public float resist(Class effect) {
			return 1f;
		}
	}

	private static class TestHawk extends HuntressBoss.DistractingHawk {

		private int attacks;
		private Char attackedTarget;

		private void setVisibleForTest(int... cells) {
			fieldOfView = new boolean[Dungeon.level.length()];
			for (int cell : cells) {
				fieldOfView[cell] = true;
			}
		}

		private void setAllVisibleForTest() {
			fieldOfView = new boolean[Dungeon.level.length()];
			Arrays.fill(fieldOfView, true);
		}

		private Char enemyForTest() {
			return enemy;
		}

		private Char chooseEnemyForTest() {
			return chooseEnemy();
		}

		private void chooseEnemyAndHuntForTest() {
			enemy = chooseEnemy();
			boolean enemyInFOV = enemy != null && fieldOfView[enemy.pos]
					&& enemy.invisible <= 0;
			HUNTING.act(enemyInFOV, false);
		}

		private int attackProcForTest(Char target, int incomingDamage) {
			return super.attackProc(target, incomingDamage);
		}

		@Override
		protected int baseAttackProc(Char enemy, int damage, DamageTag... damageTags) {
			return damage;
		}

		@Override
		public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
				DamageTag... damageTags) {
			attacks++;
			return true;
		}

		@Override
		protected boolean doAttack(Char enemy) {
			attacks++;
			attackedTarget = enemy;
			beginRetreatFrom(enemy);
			return true;
		}

		@Override
		protected boolean moveSprite(int from, int to) {
			return true;
		}

		@Override
		public void move(int step, boolean travelling) {
			pos = step;
		}
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			blobs = new HashMap<>();
			mobs = new HashSet<>();
		}

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
