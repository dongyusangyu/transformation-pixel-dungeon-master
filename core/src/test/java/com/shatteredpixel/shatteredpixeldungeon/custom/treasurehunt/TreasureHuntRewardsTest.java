/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreasureHuntRewardsTest {

	@Test
	public void completingTenStepsOverridesScoreWithPremiumReward() {
		Set<Class<? extends Item>> rewards = rewardClasses(0, true, 256);
		Set<Class<? extends Item>> expected = new HashSet<>();
		expected.add(PotionOfExperience.class);
		expected.add(ScrollOfTransmutation.class);

		assertEquals(expected, rewards);
	}

	@Test
	public void scoreAbove500UsesOnlyOrdinaryPotionsAndScrolls() {
		Set<Class<? extends Item>> rewards = rewardClasses(501, false, 4096);
		boolean foundPotion = false;
		boolean foundScroll = false;

		for (Class<? extends Item> reward : rewards) {
			foundPotion |= Potion.class.isAssignableFrom(reward);
			foundScroll |= Scroll.class.isAssignableFrom(reward);
			assertTrue(Potion.class.isAssignableFrom(reward)
					|| Scroll.class.isAssignableFrom(reward));
		}

		assertTrue(foundPotion);
		assertTrue(foundScroll);
		assertFalse(rewards.contains(PotionOfStrength.class));
		assertFalse(rewards.contains(PotionOfExperience.class));
		assertFalse(rewards.contains(ScrollOfUpgrade.class));
		assertFalse(rewards.contains(ScrollOfTransmutation.class));
	}

	@Test
	public void scoreAt500UsesOnlyOrdinaryRunestonesAndSeeds() {
		Set<Class<? extends Item>> rewards = rewardClasses(500, false, 4096);
		boolean foundRunestone = false;
		boolean foundSeed = false;

		for (Class<? extends Item> reward : rewards) {
			foundRunestone |= Runestone.class.isAssignableFrom(reward);
			foundSeed |= Plant.Seed.class.isAssignableFrom(reward);
			assertTrue(Runestone.class.isAssignableFrom(reward)
					|| Plant.Seed.class.isAssignableFrom(reward));
		}

		assertTrue(foundRunestone);
		assertTrue(foundSeed);
		assertFalse(rewards.contains(StoneOfEnchantment.class));
		assertFalse(rewards.contains(StoneOfAugmentation.class));
		assertFalse(rewards.contains(Rotberry.Seed.class));
		assertFalse(rewards.contains(Starflower.Seed.class));
	}

	@Test
	public void negativeScoresUseTheSameLowRewardPool() {
		Set<Class<? extends Item>> rewards = rewardClasses(-1, false, 512);

		for (Class<? extends Item> reward : rewards) {
			assertTrue(Runestone.class.isAssignableFrom(reward)
					|| Plant.Seed.class.isAssignableFrom(reward));
		}
	}

	private static Set<Class<? extends Item>> rewardClasses(
			int score, boolean completedTenSteps, int samples) {
		Set<Class<? extends Item>> rewards = new HashSet<>();
		Random random = new Random(20260726L);
		for (int index = 0; index < samples; index++) {
			rewards.add(TreasureHuntRewards.tenStepRewardClass(
					score, completedTenSteps, random));
		}
		return rewards;
	}
}
