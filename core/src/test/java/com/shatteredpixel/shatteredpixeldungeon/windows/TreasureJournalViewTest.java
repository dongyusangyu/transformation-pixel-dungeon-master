package com.shatteredpixel.shatteredpixeldungeon.windows;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreasureJournalViewTest {

	@Test
	public void catalogTabAddsTreasuresAfterConsumables() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");

		assertTrue(source.contains("NUM_BUTTONS = 6"));
		assertTrue(source.contains("CONSUM_IDX = 1"));
		assertTrue(source.contains("TREASURE_IDX = 2"));
		assertTrue(source.contains("BESTIARY_IDX = 3"));
		assertTrue(source.contains("LORE_IDX = 4"));
		assertTrue(source.contains("TALENT_IDX = 5"));
		assertTrue(source.contains(
				"new ItemSprite(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT)"));
	}

	@Test
	public void treasurePageUsesItsOwnCatalogAndHeader() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");

		assertTrue(source.contains("currentItemIdx == TREASURE_IDX"));
		assertTrue(source.contains("title_treasures"));
		assertTrue(source.contains("Catalog.TREASURES.totalSeen()"));
		assertTrue(source.contains("Catalog.TREASURES.totalItems()"));
		assertTrue(source.contains("Catalog.TREASURES.items()"));
	}

	@Test
	public void treasureJournalDetailOmitsQualityAndValue() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");
		int start = source.indexOf("if (item instanceof Treasures)");
		int end = source.indexOf("} else", start);

		assertTrue("missing Treasures journal branch", start >= 0 && end > start);
		String branch = source.substring(start, end);
		assertTrue(branch.contains("journalDesc()"));
		assertTrue(branch.contains("treasure_count"));
		assertTrue(branch.contains("Catalog.useCount(itemClass)"));
		assertFalse(branch.contains("item.info()"));
		assertFalse(branch.contains("rarityName()"));
		assertFalse(branch.contains("value()"));
	}

	@Test
	public void treasureJournalDoesNotAppendGenericUseCount() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");

		assertTrue(source.contains(
				"if (!(item instanceof Treasures) && Catalog.useCount(itemClass) > 1)"));
	}

	@Test
	public void treasureJournalPreviewUsesAnIsolatedRandomGenerator() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");
		int start = source.indexOf("if (Treasures.class.isAssignableFrom(itemClass))");
		int end = source.indexOf("} else", start);

		assertTrue("missing isolated treasure preview branch", start >= 0 && end > start);
		String branch = source.substring(start, end);
		assertTrue(branch.contains("Random.pushGenerator("));
		assertTrue(branch.contains("Reflection.newInstance(itemClass)"));
		assertTrue(branch.contains("finally"));
		assertTrue(branch.contains("Random.popGenerator()"));
	}

	@Test
	public void treasureJournalMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadWindowMessages("windows.properties");
		Properties chinese = loadWindowMessages("windows_zh.properties");
		String prefix = "windows.wndjournal$catalogtab.";

		assertEquals("Treasures", defaults.getProperty(prefix + "title_treasures"));
		assertEquals("藏品", chinese.getProperty(prefix + "title_treasures"));
		assertTrue(defaults.getProperty(prefix + "treasure_count").contains("%,d"));
		assertEquals("获取次数:_%,d_", chinese.getProperty(prefix + "treasure_count"));
	}

	private static Properties loadWindowMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/windows")
				.resolve(fileName);
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path source = coreDirectory().resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
