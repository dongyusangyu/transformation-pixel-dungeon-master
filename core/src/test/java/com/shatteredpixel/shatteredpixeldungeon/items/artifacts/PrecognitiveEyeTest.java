package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
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
	public void minimumEvasionIsBoundedByTheFinalMaximum() {
		assertEquals(5, PrecognitiveEye.minimumEvasion(100, 0));
		assertEquals(20, PrecognitiveEye.minimumEvasion(100, 5));
		assertEquals(8, PrecognitiveEye.minimumEvasion(10, 10));
	}

	@Test
	public void progressionAndMomentaryForesightUsesFixedActiveCount() {
		assertEquals(100, PrecognitiveEye.expToNextLevel(0));
		assertEquals(550, PrecognitiveEye.expToNextLevel(9));
		assertEquals(1, PrecognitiveEye.activeMomentaryForesightUses());
	}

	@Test
	public void activeChargeCostScalesFromFiftyToThirty() {
		assertEquals(50, PrecognitiveEye.momentaryForesightChargeCost(0));
		assertEquals(48, PrecognitiveEye.momentaryForesightChargeCost(1));
		assertEquals(40, PrecognitiveEye.momentaryForesightChargeCost(5));
		assertEquals(30, PrecognitiveEye.momentaryForesightChargeCost(10));
	}

	@Test
	public void activeChargeCostIsBoundedOutsideTheNormalLevelRange() {
		assertEquals(50, PrecognitiveEye.momentaryForesightChargeCost(-1));
		assertEquals(30, PrecognitiveEye.momentaryForesightChargeCost(11));
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
	public void heatKeepsAnIndependentDecayClockWhenItGainsALayer() {
		PrecognitiveEye.HeatState heat = new PrecognitiveEye.HeatState(3, 2);
		heat.addLayer();

		assertEquals(4, heat.layers());
		assertEquals(2, heat.turnsToDecay());
		assertFalse(heat.tick());
		assertTrue(heat.tick());
		assertEquals(3, heat.layers());
		assertEquals(10, heat.turnsToDecay());
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
		assertEquals(0.5f, PrecognitiveEye.naturalChargePerTurn(), 0f);
		assertEquals(3f, PrecognitiveEye.artifactRechargePerTurn(), 0f);
		assertTrue(Arrays.asList(Generator.Category.ARTIFACT.classes).contains(PrecognitiveEye.class));
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
