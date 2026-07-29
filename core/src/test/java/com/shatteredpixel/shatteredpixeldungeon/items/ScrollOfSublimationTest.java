package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
		assertEquals(
				ScrollOfSublimation.sublimationTalentPool("DM300"),
				ScrollOfSublimation.sublimationTalentPool("HUNTRESS"));
		assertEquals(
				Arrays.asList(Talent.FASTING, Talent.THUNDER_STRIKE, Talent.DIRECTIONAL_COLLAPSE),
				ScrollOfSublimation.sublimationTalentPool("HUNTRESS"));
	}

	@Test
	public void huntressUsesTheDm300BossTalentSlot() {
		assertEquals(
				Talent.bossTalentSlot("DM300"),
				Talent.bossTalentSlot("HUNTRESS"));
	}
}
