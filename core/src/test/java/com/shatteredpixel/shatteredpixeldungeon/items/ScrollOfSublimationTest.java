package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScrollOfSublimationTest {

	@Test
	public void huntressTypeDisplaysHuntressBossName() {
		assertEquals("女猎手？", ScrollOfSublimation.bossNameFor("HUNTRESS"));
	}

	@Test
	public void huntressUsesTheDm300TierIndexAndTalentPool() {
		assertArrayEquals(
				ScrollOfSublimation.tierAndIndex("DM300"),
				ScrollOfSublimation.tierAndIndex("HUNTRESS"));
		assertArrayEquals(
				new int[]{2, 2},
				ScrollOfSublimation.tierAndIndex("HUNTRESS"));
		assertEquals(3, ScrollOfSublimation.sublimationTalentPool("DM300").size());
		assertEquals(
				Arrays.asList(Talent.HUNTING_TECHNIQUE, Talent.NATURAL_CHILD, Talent.FALCON_EYE),
				ScrollOfSublimation.sublimationTalentPool("HUNTRESS"));
		assertFalse(ScrollOfSublimation.sublimationTalentPool("HUNTRESS").contains(Talent.FASTING));
		assertFalse(ScrollOfSublimation.sublimationTalentPool("HUNTRESS").contains(Talent.THUNDER_STRIKE));
		assertFalse(ScrollOfSublimation.sublimationTalentPool("HUNTRESS").contains(Talent.DIRECTIONAL_COLLAPSE));
	}

	@Test
	public void huntressUsesTheDm300BossTalentSlot() {
		assertEquals(
				Talent.bossTalentSlot("DM300"),
				Talent.bossTalentSlot("HUNTRESS"));
	}

	@Test
	public void huntingTechniqueIsASecondTierHuntressBossTalent() {
		assertTrue(Talent.isBossTalent(Talent.HUNTING_TECHNIQUE));
		assertEquals(2, Talent.HUNTING_TECHNIQUE.tier());
		assertEquals(Talent.bossTalentSlot("HUNTRESS"), Talent.bossTalentSlot(Talent.HUNTING_TECHNIQUE));
	}

	@Test
	public void naturalChildIsASecondTierHuntressBossTalent() {
		assertTrue(Talent.isBossTalent(Talent.NATURAL_CHILD));
		assertEquals(2, Talent.NATURAL_CHILD.tier());
		assertEquals(Talent.bossTalentSlot("HUNTRESS"), Talent.bossTalentSlot(Talent.NATURAL_CHILD));
	}

	@Test
	public void falconEyeIsASecondTierHuntressBossTalent() {
		assertTrue(Talent.isBossTalent(Talent.FALCON_EYE));
		assertEquals(2, Talent.FALCON_EYE.tier());
		assertEquals(Talent.bossTalentSlot("HUNTRESS"), Talent.bossTalentSlot(Talent.FALCON_EYE));
	}
}
