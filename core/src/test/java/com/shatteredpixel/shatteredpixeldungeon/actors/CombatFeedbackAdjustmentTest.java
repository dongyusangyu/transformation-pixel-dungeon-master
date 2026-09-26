package com.shatteredpixel.shatteredpixeldungeon.actors;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class CombatFeedbackAdjustmentTest {

	@Test
	public void brokenSealAndAuxiliaryCoreUseTheirDedicatedBuffFrames() throws Exception {
		String indicators = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/ui/BuffIndicator.java");
		String seal = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/BrokenSeal.java");
		String core = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/AuxiliaryCore.java");

		assertTrue(indicators.contains("SEAL_COMBO = 131"));
		assertTrue(indicators.contains("AUXILIARY_CORE_BOOST = 159"));
		assertTrue(seal.contains("return sealEquipped() ? BuffIndicator.SEAL_COMBO : BuffIndicator.NONE"));
		assertTrue(core.contains("return BuffIndicator.AUXILIARY_CORE_BOOST"));
	}

	@Test
	public void cursedEyeFeedbackUsesPhysicalAndArmorPiercingAtlasFrames() throws Exception {
		String floatingText = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/effects/FloatingText.java");
		String charSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/Char.java");

		assertTrue(floatingText.contains("HIT_CURSED_EYE = 50"));
		assertTrue(floatingText.contains("HIT_CURSED_EYE_NO_ARMOR = 68"));
		assertTrue(charSource.contains("PrecognitiveEye.forcesEnemyHit(attacker, defender)"));
		assertTrue(charSource.contains("HIT_CURSED_EYE"));
		assertTrue(charSource.contains("FloatingText.HIT_CURSED_EYE_NO_ARMOR"));
	}

	@Test
	public void infectionBurstKeepsParticlesAndLogWithoutHeadText() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/buffs/tboss/Infection.java");
		String feedback = source.substring(source.indexOf("private void showRuptureFeedback()"),
				source.indexOf("@Override", source.indexOf("private void showRuptureFeedback()")));

		assertTrue(feedback.contains("target.sprite.burst(INFECTION_BURST_COLOR, 8)"));
		assertTrue(feedback.contains("GLog.w(Messages.get(this, \"rupture_log\"))"));
		assertTrue(!feedback.contains("showStatus"));
	}

	private static String readCoreSource(String relativePath) throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core"))
				? working.resolve("core") : working;
		return Files.readString(core.resolve("src/main/java").resolve(relativePath),
				StandardCharsets.UTF_8);
	}
}
