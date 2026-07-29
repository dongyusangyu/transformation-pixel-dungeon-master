package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WarriorSubclassEnhancementTest {

	@Test
	public void brokenSealCapIncludesFiveBaseShield() {
		assertEquals(12, BrokenSeal.shieldCap(3, 2, 2));
	}

	@Test
	public void berserkerCalculationsMatchDesign() {
		assertEquals(30, Berserk.BASE_TURN_RECOVERY);
		assertEquals(0.3f, Berserk.rageRetentionThreshold(1), 0.0001f);
		assertEquals(0.4f, Berserk.rageRetentionThreshold(2), 0.0001f);
		assertEquals(0.5f, Berserk.rageRetentionThreshold(3), 0.0001f);
		assertEquals(1.75f, Berserk.actionSpeedMultiplier(0.75f, 1), 0.0001f);
		assertEquals(1f, Berserk.actionSpeedMultiplier(0.75f, 0), 0.0001f);
	}

	@Test
	public void gladiatorCalculationsMatchDesign() {
		assertEquals(5, Combo.sealComboCap(0));
		assertEquals(8, Combo.sealComboCap(1));
		assertEquals(20, Combo.sealComboCooldown(2));
		assertEquals(10, Combo.sealComboCooldown(3));

		assertEquals(2, Combo.moveRequirement(2, 1));
		assertEquals(1, Combo.moveRequirement(2, 2));
		assertEquals(9, Combo.moveRequirement(10, 2));

		assertEquals(3, Combo.focusDamageBonus(12, 1));
		assertEquals(20, Combo.focusDamageBonus(100, 3));
		assertEquals(1.15f, Combo.focusAccuracyMultiplier(12, 1), 0.0001f);
		assertEquals(2.5f, Combo.relentlessAttackSpeedMultiplier(8, 3), 0.0001f);
		assertEquals(1f, Combo.relentlessAttackSpeedMultiplier(7, 3), 0.0001f);
	}

	@Test
	public void warriorSubclassPoolsContainNewCandidatesButDefaultsStayUnchanged() {
		List<Talent> berserkerPool = Talent.subclassTalentPool(HeroSubClass.BERSERKER);
		assertEquals(6, berserkerPool.size());
		assertTrue(berserkerPool.contains(Talent.CEASELESS_RAGE));
		assertTrue(berserkerPool.contains(Talent.MIRRORED_REVENGE));
		assertTrue(berserkerPool.contains(Talent.BLOODTHIRSTY_BERSERK));

		List<Talent> gladiatorPool = Talent.subclassTalentPool(HeroSubClass.GLADIATOR);
		assertEquals(6, gladiatorPool.size());
		assertTrue(gladiatorPool.contains(Talent.COMBO_FOCUS));
		assertTrue(gladiatorPool.contains(Talent.RELENTLESS_COMBAT));
		assertTrue(gladiatorPool.contains(Talent.COMBO_MASTERY));

		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		Talent.initSubclassTalents(HeroSubClass.BERSERKER, talents);
		assertEquals(3, talents.get(2).size());
		assertFalse(talents.get(2).containsKey(Talent.CEASELESS_RAGE));
	}
}
