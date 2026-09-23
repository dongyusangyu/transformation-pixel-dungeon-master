package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FeelingDictionaryTest {

	@Test
	public void feelingArticleExplainsNewTowerFeelingsWithoutImplementationTerms() throws IOException {
		Path messages = coreDirectory().resolve("src/main/assets/messages/custom/custom_zh.properties");
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(messages, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		String article = properties.getProperty("custom.dict.dict.info_feeling_d");

		assertTrue(article.contains("空岛层"));
		assertTrue(article.contains("荒芜层"));
		assertTrue(article.contains("混沌层"));
		assertTrue(article.contains("10种氛围层"));
		assertTrue(article.contains("1/20"));
		assertFalse(article.contains("SKY_ISLAND"));
		assertFalse(article.contains("BARREN"));
		assertFalse(article.contains("CHAOS"));
		assertFalse(article.contains("Terrain"));
	}

	@Test
	public void chaosFeelingHasChineseTitleAndDescription() throws IOException {
		Path messages = coreDirectory().resolve("src/main/assets/messages/levels/levels_zh.properties");
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(messages, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}

		assertTrue(properties.containsKey("levels.level$feeling.chaos_title"));
		assertTrue(properties.containsKey("levels.level$feeling.chaos_desc"));
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
	}
}
