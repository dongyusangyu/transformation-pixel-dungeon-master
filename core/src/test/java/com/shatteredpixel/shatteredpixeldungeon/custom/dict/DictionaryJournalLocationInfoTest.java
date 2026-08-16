package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DictionaryJournalLocationInfoTest {

	@Test
	public void towerEntryFollowsRandomModeWithDedicatedIcon() throws IOException {
		String journal = readCoreFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java");
		String sprites = readCoreFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictSpriteSheet.java");
		int randomMode = journal.indexOf("DOCUMENTS.d.put(\"misc_random_mode\"");
		int tower = journal.indexOf("DOCUMENTS.d.put(\"info_tower\"", randomMode);

		assertTrue(randomMode >= 0 && randomMode < tower);
		assertTrue(journal.contains("\"info_tower\",       DictSpriteSheet.AREA_TOWER"));
		assertTrue(sprites.contains("case AREA_TOWER:"));
		assertTrue(sprites.contains("Assets.Environment.TILES_CHINESE_HALL"));
	}

	@Test
	public void locationEntriesHaveEnglishAndChineseText() throws IOException {
		Properties english = loadMessages("custom.properties");
		Properties chinese = loadMessages("custom_zh.properties");
		String prefix = "custom.dict.dict.";

		assertEquals("Floor 0", english.getProperty(prefix + "info_surface"));
		assertTrue(english.getProperty(prefix + "info_surface_d").contains("surface town"));
		assertEquals("Tower", english.getProperty(prefix + "info_tower"));
		assertTrue(english.getProperty(prefix + "info_tower_d").contains("unfinished"));

		assertEquals("第0层", chinese.getProperty(prefix + "info_surface"));
		assertTrue(chinese.getProperty(prefix + "info_surface_d").contains("地表城镇"));
		assertEquals("高塔", chinese.getProperty(prefix + "info_tower"));
		assertTrue(chinese.getProperty(prefix + "info_tower_d").contains("尚未完工"));
	}

	private static Properties loadMessages(String fileName) throws IOException {
		Path path = coreDirectory().resolve("src/main/assets/messages/custom").resolve(fileName);
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static String readCoreFile(String relativePath) throws IOException {
		return new String(Files.readAllBytes(coreDirectory().resolve(relativePath)),
				StandardCharsets.UTF_8);
	}

	private static Path coreDirectory() {
		Path coreDirectory = Paths.get(System.getProperty("user.dir"));
		if (!coreDirectory.endsWith("core")) {
			coreDirectory = coreDirectory.resolve("core");
		}
		return coreDirectory;
	}
}
