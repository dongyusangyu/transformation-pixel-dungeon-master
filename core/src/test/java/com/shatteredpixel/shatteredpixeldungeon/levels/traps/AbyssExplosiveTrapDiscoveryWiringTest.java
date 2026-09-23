package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class AbyssExplosiveTrapDiscoveryWiringTest {

	@Test
	public void hiddenAbyssTrapsParticipateInAllTrapDiscoveryPaths() throws IOException {
		String hero = source("actors/hero/Hero.java");
		String mapping = source("items/scrolls/ScrollOfMagicMapping.java");
		String talisman = source("items/artifacts/TalismanOfForesight.java");
		String bestiary = source("journal/Bestiary.java");

		assertTrue(hero.contains("Dungeon.level.hiddenTrapAt(curr)"));
		assertTrue(mapping.contains("Dungeon.level.hiddenTrapAt(i)"));
		assertTrue(talisman.contains("Dungeon.level.hiddenTrapAt(cell)"));
		assertTrue(talisman.contains("Dungeon.level.hiddenTrapAt(p)"));
		assertTrue(bestiary.contains("AbyssExplosiveTrap.class"));
	}

	private static String source(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/" + relativePath);
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}
}
