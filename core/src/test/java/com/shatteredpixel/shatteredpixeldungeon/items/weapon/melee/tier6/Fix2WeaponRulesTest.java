package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Fix2WeaponRulesTest {

	@Test
	public void greatGreatGreatswordKeepsTheGreatswordCleaveBonus() {
		GreatGreatGreatsword sword = TestHeroFactory.allocateItem(GreatGreatGreatsword.class);
		sword.augment = Weapon.Augment.NONE;
		sword.tier = 6;
		assertEquals("13-42", sword.upgradeAbilityStat(0));
		assertEquals("19-66", sword.upgradeAbilityStat(3));
	}

	@Test
	public void sakuraAbilityScalesInvisibilityAndMirrorHealth() {
		assertEquals(2, SakuraBlossomBlade.sneakRange());
		assertEquals(2, SakuraBlossomBlade.invisibilityTurnsForLevel(0));
		assertEquals(5, SakuraBlossomBlade.invisibilityTurnsForLevel(3));
		assertEquals(6, SakuraBlossomBlade.mirrorHealthForLevel(0));
		assertEquals(24, SakuraBlossomBlade.mirrorHealthForLevel(3));
		SakuraBlossomBlade blade = TestHeroFactory.allocateItem(SakuraBlossomBlade.class);
		assertEquals(MeleeWeapon.UpgradeAbilityStatType.DURATION,
				blade.upgradeAbilityStats(0).get(0).type);
		assertEquals(MeleeWeapon.UpgradeAbilityStatType.MIRROR_HEALTH,
				blade.upgradeAbilityStats(0).get(1).type);
		assertEquals("6", blade.upgradeAbilityStats(0).get(1).value);
		assertEquals("24", blade.upgradeAbilityStats(3).get(1).value);
	}

	@Test
	public void chainMaceUsesOneRollForDoubleImpactAndUpdatedCurves() {
		ChainMace mace = new ChainMace();
		assertEquals(6, mace.min(0));
		assertEquals(23, mace.max(0));
		assertEquals(9, mace.min(3));
		assertEquals(47, mace.max(3));
		assertEquals(0.8f, mace.ACC, 0.0001f);
		assertEquals(34, mace.throwDamageForRoll(17));
		assertEquals(16, ChainMace.sweepDamageBoost(0));
		assertEquals(28, ChainMace.sweepDamageBoost(3));
		assertEquals("12-46", mace.upgradeThrowDamageStat(0));
		assertEquals("18-94", mace.upgradeThrowDamageStat(3));
	}

	@Test
	public void palermoAccuracyTracksAttemptsAndExpiresAfterThreeTurns() {
		PalermoSword.PrecisionState state = new PalermoSword.PrecisionState();
		assertEquals(1f, state.factorFor(10, 0f), 0.0001f);
		state.recordAttack(10, 0f);
		assertEquals(1.2f, state.factorFor(10, 0.5f), 0.0001f);
		state.recordAttack(10, 0.5f);
		assertEquals(1.4f, state.factorFor(10, 3.5f), 0.0001f);
		assertEquals(1f, state.factorFor(10, 3.5001f), 0.0001f);
		assertEquals(1f, state.factorFor(11, 1f), 0.0001f);
		state.recordAttack(11, 1f);
		assertEquals(1.2f, state.factorFor(11, 1.1f), 0.0001f);
		assertEquals(1f, state.factorFor(11, 0f), 0.0001f);
	}

	@Test
	public void palermoDamageAndAbilityChargeFollowNewRules() {
		assertEquals(1f, PalermoSword.ACCURACY, 0.0001f);
		assertEquals(6, PalermoSword.minForLevel(0));
		assertEquals(18, PalermoSword.maxForLevel(0));
		assertEquals(9, PalermoSword.minForLevel(3));
		assertEquals(30, PalermoSword.maxForLevel(3));
		assertEquals(6, PalermoSword.xiexiangDamageBoost(0));
		assertEquals(9, PalermoSword.xiexiangDamageBoost(3));
		assertEquals(2, PalermoSword.chargeCostForFuture(false));
		assertEquals(1, PalermoSword.chargeCostForFuture(true));
	}
}
