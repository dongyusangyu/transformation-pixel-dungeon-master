package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RogueSubclassEnhancementTest {

	@Test
	public void assassinCalculationsMatchDesign() {
		assertEquals(0.15f,
				Preparation.damageBonus(Preparation.AttackLevel.LVL_1, 1), 0.0001f);
		assertEquals(0.55f,
				Preparation.damageBonus(Preparation.AttackLevel.LVL_3, 2), 0.0001f);
		assertEquals(0.80f,
				Preparation.damageBonus(Preparation.AttackLevel.LVL_4, 3), 0.0001f);
		assertEquals(0.50f,
				Preparation.damageBonus(Preparation.AttackLevel.LVL_4, 0), 0.0001f);

		assertEquals(1, Preparation.finishingHasteDuration(1, 1));
		assertEquals(5, Preparation.finishingHasteDuration(3, 2));
		assertEquals(8, Preparation.finishingHasteDuration(4, 3));
		assertEquals(0, Preparation.finishingHasteDuration(4, 0));

		assertEquals(1f, Preparation.perfectFinaleCharge(1, 1), 0.0001f);
		assertEquals(3.5f, Preparation.perfectFinaleCharge(3, 2), 0.0001f);
		assertEquals(5.5f, Preparation.perfectFinaleCharge(4, 3), 0.0001f);
		assertEquals(0f, Preparation.perfectFinaleCharge(4, 0), 0.0001f);
	}

	@Test
	public void closingStageGrantsExactHasteDurationsWithoutAnExtraBaselineTurn() {
		int[][] expected = {
				{1, 2, 4, 6},
				{2, 3, 5, 7},
				{3, 4, 6, 8}
		};

		for (int points = 1; points <= expected.length; points++) {
			int cap = expected[points - 1][3];
			for (int level = 1; level <= expected[points - 1].length; level++) {
				int duration = Preparation.finishingHasteDuration(level, points);
				Haste haste = new Haste();
				haste.extendCapped(duration, cap);
				assertEquals("points=" + points + ", level=" + level,
						expected[points - 1][level - 1], haste.getturns(), 0.0001f);
			}
		}

		Haste stacked = new Haste();
		stacked.extendCapped(3, 8);
		stacked.extendCapped(6, 8);
		assertEquals(8f, stacked.getturns(), 0.0001f);
	}

	@Test
	public void freerunnerCalculationsMatchDesign() {
		assertEquals(5, Momentum.afterimageDuration(30, 1));
		assertEquals(7, Momentum.afterimageDuration(30, 2));
		assertEquals(8, Momentum.afterimageDuration(30, 3));
		assertEquals(0, Momentum.afterimageDuration(30, 0));

		assertEquals(1.5f, Momentum.recoveryWandChargeMultiplier(1), 0.0001f);
		assertEquals(2f, Momentum.recoveryWandChargeMultiplier(2), 0.0001f);
		assertEquals(2.5f, Momentum.recoveryWandChargeMultiplier(3), 0.0001f);
		assertEquals(1f, Momentum.recoveryWandChargeMultiplier(0), 0.0001f);

		assertEquals(4, Momentum.warmupMomentum(1));
		assertEquals(4, Momentum.warmupMomentum(3));
		assertEquals(0, Momentum.warmupMomentum(0));
		assertEquals(0, Momentum.warmupCloakCharge(1));
		assertEquals(2, Momentum.warmupCloakCharge(2));
		assertEquals(8, Momentum.warmupStaminaDuration(3));
	}

	@Test
	public void rogueSubclassPoolsContainNewCandidatesButDefaultsStayUnchanged() {
		assertEquals(684, Talent.UNEXPECTED_STRIKE.icon());
		assertEquals(685, Talent.CLOSING_STAGE.icon());
		assertEquals(686, Talent.PERFECT_FINALE.icon());
		assertEquals(687, Talent.FREERUNNER_AFTERIMAGE.icon());
		assertEquals(688, Talent.MOMENTUM_RESERVE.icon());
		assertEquals(689, Talent.WARMUP_PREPARATION.icon());

		List<Talent> assassinPool = Talent.subclassTalentPool(HeroSubClass.ASSASSIN);
		assertEquals(6, assassinPool.size());
		assertTrue(assassinPool.contains(Talent.UNEXPECTED_STRIKE));
		assertTrue(assassinPool.contains(Talent.CLOSING_STAGE));
		assertTrue(assassinPool.contains(Talent.PERFECT_FINALE));
		assertEquals(assassinPool, Talent.subclassTalentPool(Talent.UNEXPECTED_STRIKE));

		List<Talent> freerunnerPool = Talent.subclassTalentPool(HeroSubClass.FREERUNNER);
		assertEquals(6, freerunnerPool.size());
		assertTrue(freerunnerPool.contains(Talent.FREERUNNER_AFTERIMAGE));
		assertTrue(freerunnerPool.contains(Talent.MOMENTUM_RESERVE));
		assertTrue(freerunnerPool.contains(Talent.WARMUP_PREPARATION));
		assertEquals(freerunnerPool, Talent.subclassTalentPool(Talent.FREERUNNER_AFTERIMAGE));

		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		Talent.initSubclassTalents(HeroSubClass.ASSASSIN, talents);
		assertEquals(3, talents.get(2).size());
		assertFalse(talents.get(2).containsKey(Talent.UNEXPECTED_STRIKE));

		talents.clear();
		Talent.initSubclassTalents(HeroSubClass.FREERUNNER, talents);
		assertEquals(3, talents.get(2).size());
		assertFalse(talents.get(2).containsKey(Talent.FREERUNNER_AFTERIMAGE));
	}
}
