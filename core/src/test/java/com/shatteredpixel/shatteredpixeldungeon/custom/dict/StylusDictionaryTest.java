package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StylusDictionaryTest {

	@Test
	public void stylusAppearsBetweenHeroArmorAndCommonGlyphs() {
		ArrayList<String> keys = new ArrayList<>(DictionaryJournal.ARMORS.keyList());
		ArrayList<Integer> images = new ArrayList<>(DictionaryJournal.ARMORS.imageList());
		int stylus = keys.indexOf("armor_stylus");

		assertTrue(stylus > 0);
		assertEquals("armor_epic", keys.get(stylus - 1));
		assertEquals("armor_glyph_1", keys.get(stylus + 1));
		assertEquals(ItemSpriteSheet.STYLUS, (int) images.get(stylus));
	}

	@Test
	public void entryExplainsArmorUseCurseLimitAndTalentExpansion() throws IOException {
		Properties english = loadMessages("custom.properties");
		Properties chinese = loadMessages("custom_zh.properties");
		String prefix = "custom.dict.dict.armor_stylus";

		assertEquals("Arcane Stylus", english.getProperty(prefix));
		assertTrue(english.getProperty(prefix + "_d", "").contains("armor"));
		assertTrue(english.getProperty(prefix + "_d", "").contains("cursed glyph"));
		assertTrue(english.getProperty(prefix + "_d", "").contains("Kebi"));
		assertEquals("奥术刻笔", chinese.getProperty(prefix));
		assertTrue(chinese.getProperty(prefix + "_d", "").contains("随机刻印"));
		assertTrue(chinese.getProperty(prefix + "_d", "").contains("诅咒刻印"));
		assertTrue(chinese.getProperty(prefix + "_d", "").contains("刻笔·布莱恩特"));
		assertTrue(chinese.getProperty(prefix + "_d", "").contains("武器"));
	}

	private static Properties loadMessages(String fileName) throws IOException {
		Path path = coreDirectory().resolve("src/main/assets/messages/custom").resolve(fileName);
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static Path coreDirectory() {
		Path coreDirectory = Paths.get(System.getProperty("user.dir"));
		return coreDirectory.endsWith("core") ? coreDirectory : coreDirectory.resolve("core");
	}
}
