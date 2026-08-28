package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DarkMechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerDarkMechanicalFistGenerationTest {

	@Test
	public void usesTheMechanicalFistSlotForTheDarkVariantChance() {
		assertEquals(0.025f, TowerMobRules.darkMechanicalFistChance(1f), 0f);
		assertEquals(0.050f, TowerMobRules.darkMechanicalFistChance(2f), 0f);
		assertEquals(0.075f, TowerMobRules.darkMechanicalFistChance(3f), 0f);
		assertEquals(0.100f, TowerMobRules.darkMechanicalFistChance(4f), 0f);
		assertEquals(0.125f, TowerMobRules.darkMechanicalFistChance(5f), 0f);
		assertEquals(1f, TowerMobRules.darkMechanicalFistChance(100f), 0f);
	}

	@Test
	public void usesTheExistingRatSkullMultiplier() {
		assertEquals(1f, RatSkull.exoticChanceMultiplier(-1), 0f);
		assertEquals(2f, RatSkull.exoticChanceMultiplier(0), 0f);
		assertEquals(3f, RatSkull.exoticChanceMultiplier(1), 0f);
		assertEquals(4f, RatSkull.exoticChanceMultiplier(2), 0f);
		assertEquals(5f, RatSkull.exoticChanceMultiplier(3), 0f);
	}

	@Test
	public void keepsTheExpectedOverallPoolProbability() {
		assertEquals(1f / 600f, (1f / 15f) * TowerMobRules.darkMechanicalFistChance(1f), 1e-7f);
		assertEquals(1f / 300f, (1f / 15f) * TowerMobRules.darkMechanicalFistChance(2f), 1e-7f);
		assertEquals(1f / 200f, (1f / 15f) * TowerMobRules.darkMechanicalFistChance(3f), 1e-7f);
		assertEquals(1f / 150f, (1f / 15f) * TowerMobRules.darkMechanicalFistChance(4f), 1e-7f);
		assertEquals(1f / 120f, (1f / 15f) * TowerMobRules.darkMechanicalFistChance(5f), 1e-7f);
	}

	@Test
	public void usesStrictLessThanAtTheReplacementBoundary() {
		assertTrue(TowerMobRules.shouldSpawnDarkMechanicalFist(0f, 1f));
		assertTrue(TowerMobRules.shouldSpawnDarkMechanicalFist(0.024999f, 1f));
		assertFalse(TowerMobRules.shouldSpawnDarkMechanicalFist(0.025f, 1f));
		assertTrue(TowerMobRules.shouldSpawnDarkMechanicalFist(0.124999f, 5f));
		assertFalse(TowerMobRules.shouldSpawnDarkMechanicalFist(0.125f, 5f));
	}

	@Test
	public void replacesOnlyTheMechanicalFistResult() {
		Mob dark = TowerMobRules.createMechanicalFist(0.024999f, 1f);
		Mob ordinary = TowerMobRules.createMechanicalFist(0.025f, 1f);

		assertTrue(dark instanceof DarkMechanicalFist);
		assertTrue(ordinary instanceof MechanicalFist);
		assertFalse(ordinary instanceof DarkMechanicalFist);
		assertTrue(TowerMobRules.prepareNaturalSpawn(dark).state == dark.WANDERING);
		assertTrue(TowerMobRules.prepareNaturalSpawn(ordinary).state == ordinary.WANDERING);
		assertEquals(15, TowerMobRules.Selection.values().length);
		assertTrue(Arrays.stream(TowerMobRules.Selection.values())
				.noneMatch(value -> value.name().equals("DARK_MECHANICAL_FIST")));
	}
}
