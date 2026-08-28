package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RadiantGoldHalberdTest {

	@Test
	public void heavyTierSixProfileMatchesAllBreakpoints() {
		TestableRadiantGoldHalberd weapon = new TestableRadiantGoldHalberd();

		assertEquals(6, RadiantGoldHalberd.TIER);
		assertEquals(1f, RadiantGoldHalberd.ACCURACY, 0f);
		assertEquals(2f, RadiantGoldHalberd.DELAY, 0f);
		assertEquals(3, RadiantGoldHalberd.RANGE);
		assertEquals(6, weapon.min(0));
		assertEquals(48, weapon.max(0));
		assertEquals(10, weapon.min(3));
		assertEquals(77, weapon.max(3));
		assertEquals(18, weapon.min(9));
		assertEquals(135, weapon.max(9));
		assertEquals(23, weapon.min(12));
		assertEquals(168, weapon.max(12));
		assertEquals(30, weapon.min(15));
		assertEquals(209, weapon.max(15));
		assertEquals(22, weapon.STRReq(0));
		assertEquals(22, weapon.STRReq(8));
		assertEquals(21, weapon.STRReq(9));
		assertEquals(21, weapon.STRReq(14));
		assertEquals(20, weapon.STRReq(15));
		assertEquals(EXItemSpriteSheet.RADIANT_GOLD_HALBERD, weapon.image);
		assertEquals(1f, weapon.actualAccuracy(), 0f);
		assertEquals(2f, weapon.actualDelay(), 0f);
		assertEquals(3, weapon.actualRange());
	}

	@Test
	public void negativeLevelsClampToZeroAndMasteryStillReducesTwoStrength() {
		TestableRadiantGoldHalberd weapon = new TestableRadiantGoldHalberd();
		assertEquals(6, weapon.min(-5));
		assertEquals(48, weapon.max(-5));
		assertEquals(22, weapon.STRReq(-5));
		weapon.masteryPotionBonus = true;
		assertEquals(20, weapon.STRReq(0));
		assertEquals(19, weapon.STRReq(9));
		assertEquals(18, weapon.STRReq(15));
	}

	@Test
	public void abilityFormulaUsesTheApprovedRange() {
		assertEquals(7, RadiantGoldHalberd.abilityMin(-3));
		assertEquals(7, RadiantGoldHalberd.abilityMax(-3));
		assertEquals(7, RadiantGoldHalberd.abilityMin(0));
		assertEquals(7, RadiantGoldHalberd.abilityMax(0));
		assertEquals(10, RadiantGoldHalberd.abilityMin(3));
		assertEquals(40, RadiantGoldHalberd.abilityMax(3));
		assertEquals(19, RadiantGoldHalberd.abilityMin(12));
		assertEquals(139, RadiantGoldHalberd.abilityMax(12));
	}

	@Test
	public void lineEligibilityIgnoresVisibilityButRejectsInvalidTargets() {
		assertTrue(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ENEMY, false, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				false, Char.Alignment.ENEMY, false, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ALLY, false, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ENEMY, true, true));
		assertFalse(RadiantGoldHalberd.abilityTargetAllowed(
				true, Char.Alignment.ENEMY, false, false));
	}

	@Test
	public void onlyTerrainOrBoundaryAddsWallDamage() {
		assertEquals(1.2f, RadiantGoldHalberd.wallDamageMultiplier(true), 0f);
		assertEquals(1f, RadiantGoldHalberd.wallDamageMultiplier(false), 0f);
		assertFalse(RadiantGoldHalberd.wallCollision(false, false));
		assertTrue(RadiantGoldHalberd.wallCollision(true, false));
		assertTrue(RadiantGoldHalberd.wallCollision(false, true));
	}

	@Test
	public void targetCountControlsTheWholeAttackDamageBonus() {
		assertEquals(1.25f, RadiantGoldHalberd.splashDamageMultiplier(1), 0f);
		assertEquals(1.12f, RadiantGoldHalberd.splashDamageMultiplier(2), 0f);
		assertEquals(1f, RadiantGoldHalberd.splashDamageMultiplier(3), 0f);
		assertEquals(1f, RadiantGoldHalberd.splashDamageMultiplier(4), 0f);
	}

	@Test
	public void onlyNormalTopLevelHeroHitsCanStartSplash() {
		assertTrue(RadiantGoldHalberd.canStartSplash(false, false, true));
		assertFalse(RadiantGoldHalberd.canStartSplash(true, false, true));
		assertFalse(RadiantGoldHalberd.canStartSplash(false, true, true));
		assertFalse(RadiantGoldHalberd.canStartSplash(false, false, false));
	}

	@Test
	public void splashEligibilityRejectsFriendlyCharmedAndInvalidTargets() {
		assertTrue(RadiantGoldHalberd.splashTargetAllowed(
				true, Char.Alignment.ENEMY, false, false, false));
		assertFalse(RadiantGoldHalberd.splashTargetAllowed(
				false, Char.Alignment.ENEMY, false, false, false));
		assertFalse(RadiantGoldHalberd.splashTargetAllowed(
				true, Char.Alignment.ALLY, false, false, false));
		assertFalse(RadiantGoldHalberd.splashTargetAllowed(
				true, Char.Alignment.ENEMY, true, false, false));
		assertFalse(RadiantGoldHalberd.splashTargetAllowed(
				true, Char.Alignment.ENEMY, false, true, false));
		assertFalse(RadiantGoldHalberd.splashTargetAllowed(
				true, Char.Alignment.ENEMY, false, false, true));
	}

	@Test
	public void splashSelectionUsesOnlyCardinalEnemiesAndCapsAtTwo() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		Actor.clear();
		Actor.resetNextID();
		try {
			Dungeon.level = openLevel(9, 9);
			Mob attacker = mobAt(4, Char.Alignment.ALLY);

			Mob primary = mobAt(40, Char.Alignment.ENEMY);
			Mob up = mobAt(31, Char.Alignment.ENEMY);
			Mob left = mobAt(39, Char.Alignment.ENEMY);
			Mob right = mobAt(41, Char.Alignment.ENEMY);
			Mob friendlyBelow = mobAt(49, Char.Alignment.ALLY);
			Mob diagonal = mobAt(30, Char.Alignment.ENEMY);

			ArrayList<Char> targets = new RadiantGoldHalberd()
					.collectSplashTargets(attacker, primary);

			assertEquals(2, targets.size());
			for (Char target : targets) {
				assertTrue(target == up || target == left || target == right);
			}
			assertFalse(targets.contains(friendlyBelow));
			assertFalse(targets.contains(diagonal));
		} finally {
			Actor.clear();
			Actor.resetNextID();
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	private static Mob mobAt(int pos, Char.Alignment alignment) {
		Mob mob = new TestMob();
		mob.pos = pos;
		mob.HP = mob.HT = 100;
		mob.alignment = alignment;
		Actor.add(mob);
		Dungeon.level.mobs.add(mob);
		return mob;
	}

	private static Level openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		Arrays.fill(level.heroFOV, true);
		level.mobs = new HashSet<>();
		return level;
	}

	private static class TestableRadiantGoldHalberd extends RadiantGoldHalberd {
		float actualAccuracy() {
			return ACC;
		}

		float actualDelay() {
			return DLY;
		}

		int actualRange() {
			return RCH;
		}
	}

	private static class TestMob extends Mob {
	}

	private static class TestLevel extends Level {

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
