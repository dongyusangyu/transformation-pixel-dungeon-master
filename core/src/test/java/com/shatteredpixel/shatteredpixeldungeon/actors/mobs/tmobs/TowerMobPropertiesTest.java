package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class TowerMobPropertiesTest {

	@Test
	public void constructsUseMechanicalAndProjectionProperties() {
		assertProperties("MechanicalFist", "INORGANIC", "LARGE");
		assertProperties("DarkMechanicalFist", "DEMONIC");
		assertProperties("AlienatedPrismaticGuard", "INORGANIC");
		assertProperties("TwistedMirror", "INORGANIC");
	}

	@Test
	public void monstersUseElementalAndBodyProperties() {
		assertProperties("CorrosiveSwarm", "ACIDIC");
		assertProperties("EarthlySerpent", "ACIDIC", "LARGE");
		assertProperties("HeavyCrabification");
		assertProperties("MarshSlime", "DARKSLIME", "LARGE");
		assertProperties("MimicCrocodile");
	}

	@Test
	public void supernaturalMonstersUseTheirOriginProperties() {
		assertProperties("Corpse", "UNDEAD");
		assertProperties("RoastLambWarlock", "UNDEAD", "FIERY");
		assertProperties("SoulCollector", "UNDEAD");
		assertProperties("DeathButterfly", "UNDEAD");
		assertProperties("Obscura", "DEMONIC");
		assertProperties("WildDread", "DEMONIC", "INORGANIC");
		assertProperties("PowerfulWraith");
		assertBaseWraithProperties("UNDEAD", "INORGANIC");
	}

	@Test
	public void ordinaryTowerCreaturesDoNotGainUnrelatedProperties() {
		assertProperties("CamouflageGnoll");
		assertProperties("RuneSpinner");
	}

	private static void assertProperties(String className, String... expected) {
		assertEquals(new HashSet<>(Arrays.asList(expected)), declaredProperties(
				towerMobDirectory().resolve(className + ".java")));
	}

	private static void assertBaseWraithProperties(String... expected) {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Wraith.java");
		assertEquals(new HashSet<>(Arrays.asList(expected)), declaredProperties(source));
	}

	private static Set<String> declaredProperties(Path source) {
		try {
			String text = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
			Matcher matcher = Pattern.compile(
					"properties\\.add\\(Property\\.([A-Z_]+)\\)").matcher(text);
			Set<String> result = new HashSet<>();
			while (matcher.find()) {
				result.add(matcher.group(1));
			}
			return result;
		} catch (IOException e) {
			throw new AssertionError("Unable to read " + source, e);
		}
	}

	private static Path towerMobDirectory() {
		return coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs");
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
