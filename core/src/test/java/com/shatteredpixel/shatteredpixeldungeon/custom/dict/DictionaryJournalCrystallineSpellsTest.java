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

public class DictionaryJournalCrystallineSpellsTest {

	@Test
	public void metamorphosisPrismAppearsBetweenMetamorphosisCrystalAndWildEnergy()
			throws IOException {
		assertOrdered(
				loadMessages("custom.properties").getProperty("custom.dict.dict.spell_spells_d"),
				"_Metamorphosis Crystal:_", "_Metamorphosis Prism:_", "_Wild Energy:_");
		assertOrdered(
				loadMessages("custom_zh.properties").getProperty("custom.dict.dict.spell_spells_d"),
				"_蜕变结晶：_", "_蜕变棱晶：_", "_强能结晶：_");
	}

	private static void assertOrdered(String description, String first, String second, String third) {
		assertNotNull(description);
		int firstIndex = description.indexOf(first);
		int secondIndex = description.indexOf(second);
		int thirdIndex = description.indexOf(third);
		assertTrue(firstIndex >= 0 && firstIndex < secondIndex && secondIndex < thirdIndex);
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
