package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AttackDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AuxiliaryDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Regression coverage for the global stealth contribution of drone actors. */
public class CharStealthDroneTest {

	@After
	public void clearActors() {
		Actor.clear();
	}

	@Test
	public void noDroneHasNoStealthContribution() {
		assertEquals(0f, stealthWith(), 0f);
	}

	@Test
	public void plainAndAttackDronesDoNotContributeStealth() {
		assertEquals(0f, stealthWith(new InstructionTool.Drone()), 0f);
		assertEquals(0f, stealthWith(new AttackDrone()), 0f);
		assertEquals(0f, stealthWith(new AuxiliaryDrone.ChaosDrone()), 0f);
	}

	@Test
	public void everyAuxiliaryDroneTypeUsesTheAuxiliaryStealthRule() {
		assertEquals(0f, stealthWith(new AuxiliaryDrone()), 0f);
		assertEquals(0f, stealthWith(new AuxiliaryDrone.ScoutDrone()), 0f);
		assertEquals(0f, stealthWith(new AuxiliaryDrone.MirrorDrone()), 0f);
		assertEquals(0f, stealthWith(new AuxiliaryDrone.ProtectDrone()), 0f);
		assertEquals(0f, stealthWith(new AuxiliaryDrone.EscortDrone()), 0f);
		assertEquals(0f, stealthWith(new AuxiliaryDrone.BombDrone()), 0f);
	}

	@Test
	public void escortDronesAccumulateBeforeIntegerTruncation() {
		assertEquals(1f, stealthWith(
				new AuxiliaryDrone.EscortDrone(),
				new AuxiliaryDrone.EscortDrone()), 0f);
		assertEquals(1f, stealthWith(
				new AuxiliaryDrone(),
				new AuxiliaryDrone(),
				new AuxiliaryDrone(),
				new AuxiliaryDrone()), 0f);
		assertEquals(1f, stealthWith(
				new AuxiliaryDrone.EscortDrone(),
				new AuxiliaryDrone()), 0f);
	}

	@Test
	public void anAuxiliaryDroneAlreadyOnTheFieldStillCountsAfterPoolChanges() {
		AuxiliaryDrone oldEscort = new AuxiliaryDrone.EscortDrone();
		assertEquals(0f, stealthWith(oldEscort), 0f);

		// The stealth rule is type-based, not tied to the current hero subclass.
		assertEquals(1f, stealthWith(oldEscort, new AuxiliaryDrone.EscortDrone()), 0f);
	}

	@Test
	public void droneTypeBoundariesMatchTheStealthContract() {
		assertTrue(AuxiliaryDrone.class.isAssignableFrom(AuxiliaryDrone.EscortDrone.class));
		assertTrue(AuxiliaryDrone.class.isAssignableFrom(AuxiliaryDrone.ScoutDrone.class));
		assertFalse(AuxiliaryDrone.class.isAssignableFrom(InstructionTool.Drone.class));
		assertFalse(AuxiliaryDrone.class.isAssignableFrom(AttackDrone.class));
		assertFalse(AuxiliaryDrone.class.isAssignableFrom(AuxiliaryDrone.ChaosDrone.class));
	}

	@Test
	public void randomDronePoolsKeepStealthRelevantTypesInExpectedBranches() throws IOException {
		String source = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/ally/AuxiliaryDrone.java");

		assertTrue(source.contains("hero.subClass.is(HeroSubClass.AU400)"));
		assertTrue(source.contains("d = new AttackDrone.FlashDrone();"));
		assertTrue(source.contains("hero.subClass.is(HeroSubClass.AT400)"));
		assertTrue(source.contains("d = new AuxiliaryDrone.EscortDrone();"));
		assertTrue(source.contains("d = new InstructionTool.Drone();"));
	}

	@Test
	public void stealthImplementationAggregatesAuxiliaryAndEscortValues() throws IOException {
		String source = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java");

		assertTrue(source.contains("if(ch instanceof AuxiliaryDrone.EscortDrone)"));
		assertTrue(source.contains("bonusDis+=0.8f"));
		assertTrue(source.contains("}else if(ch instanceof AuxiliaryDrone)"));
		assertTrue(source.contains("bonusDis+=0.3f"));
		assertTrue(source.contains("stealth+=(int)bonusDis"));
	}

	private static float stealthWith(Char... drones) {
		Actor.clear();
		Rat observer = new Rat();
		Actor.add(observer);
		for (Char drone : drones) {
			Actor.add(drone);
		}
		return observer.stealth();
	}

	private static String sourceFile(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)),
				StandardCharsets.UTF_8);
	}
}
