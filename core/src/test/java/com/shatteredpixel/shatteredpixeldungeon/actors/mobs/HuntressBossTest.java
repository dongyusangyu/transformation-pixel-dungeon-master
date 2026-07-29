package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HuntressBossSprite;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
	public void tacticalDecisionCoversVisibilityLineAndDistanceBoundaries() {
		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(false, true, 1, 0));
		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(true, false, 1, 0));

		assertEquals(HuntressBoss.TacticalAction.MELEE,
				HuntressBoss.tacticalAction(true, true, 1, 0));
		assertEquals(HuntressBoss.TacticalAction.MELEE,
				HuntressBoss.tacticalAction(true, true, 1, 1));

		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(true, true, 2, 0));
		assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
				HuntressBoss.tacticalAction(true, true, 2, 1));
		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(true, true, 4, 0));
		assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
				HuntressBoss.tacticalAction(true, true, 4, 1));

		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(true, true, 5, 0));
		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(true, true, 8, 1));
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
	public void armorModesDistinguishSniperShotsMeleeWardenAndGale() {
		assertEquals(0, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, false, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, true, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.WARDEN, false, false, 20));
		assertEquals(0, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.WARDEN, false, true, 20));
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
		HuntressBoss boss = new HuntressBoss();

		assertEquals(HuntressBoss.Phase.SNIPER, boss.phase());
		assertEquals(15, boss.viewDistance);
		assertEquals(HuntressBossSprite.class, boss.spriteClass);

		assertTrue(boss.enterWardenPhase());

		assertEquals(HuntressBoss.Phase.WARDEN, boss.phase());
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
	public void burningIsRemovedAndSuccessfulAbsorptionGrantsFireImbue() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertNotNull(boss.buff(FireImbue.class));
		assertEquals(FireImbue.DURATION * 0.3f,
				HuntressBoss.FIRE_IMBUE_DURATION, 0f);
	}

	@Test
	public void failedFireAbsorptionStillRemovesBurning() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(1);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertNull(boss.buff(FireImbue.class));
	}

	private static class DeterministicFireAbsorptionHuntress extends HuntressBoss {

		private final int roll;

		private DeterministicFireAbsorptionHuntress(int roll) {
			this.roll = roll;
		}

		@Override
		protected int fireAbsorptionRoll() {
			return roll;
		}
	}
}
