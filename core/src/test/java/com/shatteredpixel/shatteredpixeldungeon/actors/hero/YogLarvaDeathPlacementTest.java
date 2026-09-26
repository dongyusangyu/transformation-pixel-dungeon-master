package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class YogLarvaDeathPlacementTest {

	@Test
	public void deathTriggeredLarvaeUseCollisionCheckedNearbyCell() throws IOException {
		String source = readTalentSource();
		assertTrue(source.contains("int spawnCell = findLarvaSpawnCell(mob.pos)"));
		assertTrue(source.contains("if (spawnCell != -1)"));
	}

	private static String readTalentSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java")
				.resolve("com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
