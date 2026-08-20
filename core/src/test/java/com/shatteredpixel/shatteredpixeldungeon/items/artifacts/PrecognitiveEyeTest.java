package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import org.junit.Test;

import java.util.Arrays;

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
	public void progressionAndMomentaryForesightUseSpecifiedThresholds() {
		assertEquals(100, PrecognitiveEye.expToNextLevel(0));
		assertEquals(550, PrecognitiveEye.expToNextLevel(9));
		assertEquals(1, PrecognitiveEye.momentaryForesightUses(1));
		assertEquals(3, PrecognitiveEye.momentaryForesightUses(5));
		assertEquals(5, PrecognitiveEye.momentaryForesightUses(10));
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
}
