package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Random;
import com.watabou.utils.Bundle;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrecognitiveEyeTest {

	@Test
	public void levelCapAndProgressionMatchFiveLevelDesign() {
		PrecognitiveEye eye = new PrecognitiveEye();

		assertEquals(5, eye.levelCap);
		assertEquals(100, PrecognitiveEye.expToNextLevel(0));
		assertEquals(300, PrecognitiveEye.expToNextLevel(2));
		assertEquals(500, PrecognitiveEye.expToNextLevel(4));
	}

	@Test
	public void activeChargeCostIsAlwaysTwentyPercent() {
		assertEquals(20, PrecognitiveEye.momentaryForesightChargeCost(-1));
		assertEquals(20, PrecognitiveEye.momentaryForesightChargeCost(0));
		assertEquals(20, PrecognitiveEye.momentaryForesightChargeCost(3));
		assertEquals(20, PrecognitiveEye.momentaryForesightChargeCost(5));
		assertEquals(20, PrecognitiveEye.momentaryForesightChargeCost(6));
	}

	@Test
	public void activationSpendsTwentyChargeAndAddsOneDodgeAtATime() {
		Hero hero = TestHeroFactory.create();
		PrecognitiveEye eye = new PrecognitiveEye();
		eye.level(2);
		eye.charge = 40;
		hero.belongings.artifact = eye;

		assertTrue(eye.storeMomentaryForesight(hero));
		assertEquals(20, eye.charge);
		assertEquals(1, hero.buff(PrecognitiveEye.MomentaryForesight.class).uses());

		assertTrue(eye.storeMomentaryForesight(hero));
		assertEquals(0, eye.charge);
		assertEquals(2, hero.buff(PrecognitiveEye.MomentaryForesight.class).uses());
	}

	@Test
	public void momentaryForesightAddsOneDodgeAndStopsAtLevelCapacity() {
		PrecognitiveEye.MomentaryForesight foresight = new PrecognitiveEye.MomentaryForesight();

		assertTrue(foresight.addDodge(3));
		assertEquals(1, foresight.uses());
		assertTrue(foresight.addDodge(3));
		assertEquals(2, foresight.uses());
		assertTrue(foresight.addDodge(3));
		assertEquals(3, foresight.uses());
		assertFalse(foresight.addDodge(3));
		assertEquals(3, foresight.uses());
	}

	@Test
	public void momentaryForesightCapacityFollowsArtifactLevel() {
		assertEquals(1, PrecognitiveEye.momentaryForesightCapacity(0));
		assertEquals(3, PrecognitiveEye.momentaryForesightCapacity(2));
		assertEquals(6, PrecognitiveEye.momentaryForesightCapacity(5));
		assertEquals(6, PrecognitiveEye.momentaryForesightCapacity(99));
	}

	@Test
	public void levelZeroIsNotBlockedFromArtifactCharging() throws IOException {
		String source = sourceFile();
		String canCharge = source.substring(source.indexOf("private boolean canCharge"),
				source.indexOf("private void gainCharge"));

		assertTrue(canCharge.contains("target.buff(MagicImmune.class)"));
		assertFalse(canCharge.contains("level()"));
	}

	@Test
	public void trinityUsesFollowSpiritFormTalentPoints() {
		assertEquals(1, PrecognitiveEye.trinityDodgeUses(0));
		assertEquals(2, PrecognitiveEye.trinityDodgeUses(1));
		assertEquals(3, PrecognitiveEye.trinityDodgeUses(2));
		assertEquals(4, PrecognitiveEye.trinityDodgeUses(3));
	}

	@Test
	public void rechargeAndGeneratorRegistrationMatchTheSpecification() {
		assertEquals(0.5f, PrecognitiveEye.naturalChargePerTurn(0), 0f);
		assertEquals(0.7f, PrecognitiveEye.naturalChargePerTurn(2), 0.0001f);
		assertEquals(1f, PrecognitiveEye.naturalChargePerTurn(5), 0f);
		assertEquals(1f, PrecognitiveEye.naturalChargePerTurn(99), 0f);
		assertEquals(3f, PrecognitiveEye.artifactRechargePerTurn(), 0f);
		assertTrue(Arrays.asList(Generator.Category.ARTIFACT.classes).contains(PrecognitiveEye.class));
	}

	@Test
	public void equippedEyeGainsExperienceFromHeroExperience() {
		Hero hero = TestHeroFactory.create();
		PrecognitiveEye eye = new PrecognitiveEye();
		hero.belongings.artifact = eye;

		eye.onHeroGainExp(0.5f, hero);
		assertEquals(50, eye.exp);

		hero.belongings.artifact = null;
		eye.onHeroGainExp(0.5f, hero);
		assertEquals(50, eye.exp);
	}

	@Test
	public void ordinaryDodgesGrantExperienceButStoredForesightDoesNot() {
		Hero hero = TestHeroFactory.create();
		PrecognitiveEye eye = new PrecognitiveEye();
		hero.belongings.artifact = eye;
		Mob attacker = new Mob() {};
		attacker.alignment = Char.Alignment.ENEMY;
		int previousDepth = Dungeon.depth;
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.depth = 1;
			Dungeon.level = null;
			PrecognitiveEye.onEnemyAttackDodged(attacker, hero);
			assertEquals(10, eye.exp);

			eye.exp = 0;
			PrecognitiveEye.MomentaryForesight foresight =
					new PrecognitiveEye.MomentaryForesight().set(1);
			assertTrue(foresight.attachTo(hero));
			assertTrue(PrecognitiveEye.consumeMomentaryForesight(attacker, hero));
			assertEquals(0, eye.exp);
		} finally {
			Dungeon.depth = previousDepth;
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void equippedEyeDoesNotRaiseTheEvasionRoll() {
		Hero hero = TestHeroFactory.create();
		PrecognitiveEye eye = new PrecognitiveEye();
		eye.level(5);
		hero.belongings.artifact = eye;
		Mob attacker = new Mob() {};
		attacker.alignment = Char.Alignment.ENEMY;

		float actual;
		Random.pushGenerator(0xC0FFEE);
		try {
			actual = PrecognitiveEye.rollEvasion(attacker, hero, 100f);
		} finally {
			Random.popGenerator();
		}
		Random.pushGenerator(0xC0FFEE);
		try {
			assertEquals(Random.Float(100f), actual, 0f);
		} finally {
			Random.popGenerator();
		}
	}

	@Test
	@SuppressWarnings("deprecation")
	public void legacyOverheatDoesNotBlockActivation() {
		Hero hero = TestHeroFactory.create();
		PrecognitiveEye eye = new PrecognitiveEye();
		eye.charge = 20;
		hero.belongings.artifact = eye;
		assertTrue(new PrecognitiveEye.PrecognitiveOverheat().attachTo(hero));

		assertTrue(eye.actions(hero).contains(PrecognitiveEye.AC_ACTIVATE));
	}

	@Test
	public void fullMomentaryForesightStorageDisablesActivation() {
		Hero hero = TestHeroFactory.create();
		PrecognitiveEye eye = new PrecognitiveEye();
		eye.level(2);
		eye.charge = 100;
		hero.belongings.artifact = eye;
		PrecognitiveEye.MomentaryForesight foresight =
				new PrecognitiveEye.MomentaryForesight().set(3);
		assertTrue(foresight.attachTo(hero));

		assertFalse(eye.actions(hero).contains(PrecognitiveEye.AC_ACTIVATE));
		assertTrue(foresight.consumeDodge());
		assertTrue(eye.actions(hero).contains(PrecognitiveEye.AC_ACTIVATE));
	}

	@Test
	public void legacyLevelsAreClampedToFiveWhenRestored() {
		PrecognitiveEye legacyEye = new PrecognitiveEye();
		legacyEye.level(10);
		Bundle bundle = new Bundle();
		legacyEye.storeInBundle(bundle);

		PrecognitiveEye restoredEye = new PrecognitiveEye();
		restoredEye.restoreFromBundle(bundle);

		assertEquals(5, restoredEye.level());
	}

	@Test
	public void naturalRechargeRespectsTheLockedFloorRecoveryWindow() throws IOException {
		String source = sourceFile();
		String recharge = source.substring(source.indexOf("private class EyeRecharge"));

		assertTrue(recharge.contains("Regeneration.regenOn()"));
	}

	@Test
	public void momentaryForesightConsumesExactlyOneDodge() {
		PrecognitiveEye.MomentaryForesight foresight = new PrecognitiveEye.MomentaryForesight().set(2);
		assertTrue(foresight.consumeDodge());
		assertEquals(1, foresight.uses());
	}

	private static String sourceFile() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEye.java")),
				StandardCharsets.UTF_8);
	}
}
