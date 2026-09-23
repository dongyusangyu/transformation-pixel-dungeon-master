package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TowerRedCrossTrapTest {

	@Test
	public void highTowerTrapPoolIncludesRedCrossTrapWithMatchingChance() throws IOException {
		String source = readSource("src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java");

		assertTrue(source.contains("RedCrossTrap.class"));
		assertTrue(source.contains("new float[]{4, 4, 3, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1}"));
	}

	@Test
	public void redCrossTrapIsRegisteredForTheTrapJournal() throws IOException {
		String source = readSource("src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Bestiary.java");

		assertTrue(source.contains("RedCrossTrap.class"));
	}

	@Test
	public void hasEnglishAndChineseTrapText() throws IOException {
		String english = readSource("src/main/assets/messages/levels/levels.properties");
		String chinese = readSource("src/main/assets/messages/levels/levels_zh.properties");

		assertTrue(english.contains("levels.traps.redcrosstrap.name="));
		assertTrue(english.contains("levels.traps.redcrosstrap.desc="));
		assertTrue(chinese.contains("levels.traps.redcrosstrap.name=红十字陷阱"));
		assertTrue(chinese.contains("levels.traps.redcrosstrap.desc="));
	}

	private static String readSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)), StandardCharsets.UTF_8);
	}
}
