package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TowerInfernalTrapTest {

	@Test
	public void isRegisteredInTheTowerTrapPoolWithItsOwnWeight() throws IOException {
		String source = readCoreSource("levels/towers/TowerLevel.java");

		assertTrue(source.contains("InfernalTrap.class"));
		assertTrue(source.contains("new float[]{4, 4, 3, 2, 2, 1, 1, 1, 1, 1}"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java")
				.resolve("com/shatteredpixel/shatteredpixeldungeon")
				.resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
