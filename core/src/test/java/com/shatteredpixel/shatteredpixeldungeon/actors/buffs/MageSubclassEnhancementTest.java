package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MageSubclassEnhancementTest {

	@Test
	public void battlemageCalculationsMatchDesign() {
		assertEquals(0, MagesStaff.longArmReachBonus(7, 1));
		assertEquals(1, MagesStaff.longArmReachBonus(8, 1));
		assertEquals(2, MagesStaff.longArmReachBonus(14, 2));
		assertEquals(3, MagesStaff.longArmReachBonus(18, 3));

		assertEquals(1f, ArcaneConfluence.chargeAmount(1), 0.0001f);
		assertEquals(1.5f, ArcaneConfluence.chargeAmount(2), 0.0001f);
		assertEquals(2f, ArcaneConfluence.chargeAmount(3), 0.0001f);
		assertEquals(1f, ArcaneConfluence.overflowEfficiency(1), 0.0001f);
		assertEquals(1.25f, ArcaneConfluence.overflowEfficiency(2), 0.0001f);
		assertEquals(1.5f, ArcaneConfluence.overflowEfficiency(3), 0.0001f);

		assertEquals(2, FocusedCasting.maxLevelBonus(1));
		assertEquals(3, FocusedCasting.maxLevelBonus(2));
		assertEquals(4, FocusedCasting.maxLevelBonus(3));
		assertTrue(FocusedCasting.canSpendCharge(0, 0.5f, 1));
		assertEquals(0f, FocusedCasting.remainingCharge(0, 0.5f, 1), 0.0001f);
		assertEquals(0.5f, FocusedCasting.remainingCharge(1, 0f, 1), 0.0001f);
		assertEquals(0.5f, FocusedCasting.remainingCharge(2, 0f, 3), 0.0001f);
	}

	@Test
	public void warlockCalculationsMatchDesign() {
		float baseChance = SoulMark.markChance(4, 1, 0);
		assertEquals(baseChance + 0.08f, SoulMark.markChance(4, 1, 1), 0.0001f);
		assertEquals(baseChance + 0.17f, SoulMark.markChance(4, 1, 2), 0.0001f);
		assertEquals(baseChance + 0.25f, SoulMark.markChance(4, 1, 3), 0.0001f);
		assertTrue(SoulMark.markChance(100, 10, 3) <= 1f);

		assertEquals(14f, SoulMark.markDuration(4, 0), 0.0001f);
		assertEquals(19f, SoulMark.markDuration(4, 1), 0.0001f);
		assertEquals(22f, SoulMark.markDuration(4, 2), 0.0001f);
		assertEquals(24f, SoulMark.markDuration(4, 3), 0.0001f);

		assertEquals(4, SoulMark.healingAmount(10, true, 0));
		assertEquals(7, SoulMark.healingAmount(10, true, 3));
		assertEquals(4, SoulMark.healingAmount(10, false, 3));

		assertTrue(SoulMark.blocksRangedAttack(true, false, true, 1));
		assertFalse(SoulMark.blocksRangedAttack(true, true, true, 3));
		assertFalse(SoulMark.blocksRangedAttack(false, false, true, 3));
		assertEquals(90, SoulMark.reducePhysicalDamage(100, true, false, 1));
		assertEquals(85, SoulMark.reducePhysicalDamage(100, true, false, 2));
		assertEquals(80, SoulMark.reducePhysicalDamage(100, true, false, 3));
		assertEquals(100, SoulMark.reducePhysicalDamage(100, true, true, 3));
	}

	@Test
	public void mageSubclassPoolsContainNewCandidatesButDefaultsStayUnchanged() {
		assertEquals(678, Talent.LONG_ARM.icon());
		assertEquals(679, Talent.ARCANE_CONFLUENCE.icon());
		assertEquals(680, Talent.FOCUSED_CASTING.icon());
		assertEquals(681, Talent.FINE_TASTING.icon());
		assertEquals(682, Talent.BONE_DEEP.icon());
		assertEquals(683, Talent.MIND_IMPRISONMENT.icon());

		List<Talent> battlemagePool = Talent.subclassTalentPool(HeroSubClass.BATTLEMAGE);
		assertEquals(6, battlemagePool.size());
		assertTrue(battlemagePool.contains(Talent.LONG_ARM));
		assertTrue(battlemagePool.contains(Talent.ARCANE_CONFLUENCE));
		assertTrue(battlemagePool.contains(Talent.FOCUSED_CASTING));

		List<Talent> warlockPool = Talent.subclassTalentPool(HeroSubClass.WARLOCK);
		assertEquals(6, warlockPool.size());
		assertTrue(warlockPool.contains(Talent.FINE_TASTING));
		assertTrue(warlockPool.contains(Talent.BONE_DEEP));
		assertTrue(warlockPool.contains(Talent.MIND_IMPRISONMENT));

		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		Talent.initSubclassTalents(HeroSubClass.BATTLEMAGE, talents);
		assertEquals(3, talents.get(2).size());
		assertFalse(talents.get(2).containsKey(Talent.LONG_ARM));

		talents.clear();
		Talent.initSubclassTalents(HeroSubClass.WARLOCK, talents);
		assertEquals(3, talents.get(2).size());
		assertFalse(talents.get(2).containsKey(Talent.FINE_TASTING));
	}
}
