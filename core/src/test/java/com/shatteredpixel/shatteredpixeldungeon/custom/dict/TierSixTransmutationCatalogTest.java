package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TierSixTransmutationCatalogTest {

	@Test
	public void tierSixDowngradeRuleAppearsInChineseAndEnglishCatalogs() throws IOException {
		assertRule(loadMessages("custom.properties"),
				"tier 6", "scroll of transmutation", "scroll of uptier",
				"tier 5", "tier 6 item");
		assertRule(loadMessages("custom_zh.properties"),
				"6阶", "嬗变卷轴", "升变秘卷", "5阶", "6阶装备");
	}

	private static void assertRule(Properties messages, String tierSix, String transmutation,
			String uptier, String tierFive, String tierSixItem) {
		String description = messages.getProperty("custom.dict.dict.info_tier_d");
		assertNotNull(description);
		assertTrue(description.contains(tierSix));
		assertTrue(description.contains(transmutation));
		assertTrue(description.contains(uptier));
		assertTrue(description.contains(tierFive));
		assertTrue(description.contains(tierSixItem));
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
