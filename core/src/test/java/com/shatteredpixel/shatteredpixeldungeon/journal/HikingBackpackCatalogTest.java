package com.shatteredpixel.shatteredpixeldungeon.journal;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class HikingBackpackCatalogTest {

	@Test
	public void catalogPlacesHikingBackpackAfterMagicalHolster() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java");
		int holster = source.indexOf("MagicalHolster.class");
		int hiking = source.indexOf("HikingBackpack.class");
		int amulet = source.indexOf("Amulet.class", holster);

		assertTrue(holster >= 0);
		assertTrue(hiking > holster);
		assertTrue(amulet > hiking);
	}

	@Test
	public void englishAndChineseTextsContainOverflowBehavior() throws IOException {
		String english = readAsset("messages/items/items.properties");
		String chinese = readAsset("messages/items/items_zh.properties");

		assertTrue(english.contains("items.bags.hikingbackpack.name=hiking backpack"));
		assertTrue(english.contains("more suitable container has space"));
		assertTrue(chinese.contains("items.bags.hikingbackpack.name=登山包"));
		assertTrue(chinese.contains("一旦更合适的容器腾出空间"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		return new String(Files.readAllBytes(coreDirectory().resolve("src/main/java")
				.resolve(relativePath)), StandardCharsets.UTF_8);
	}

	private static String readAsset(String relativePath) throws IOException {
		return new String(Files.readAllBytes(coreDirectory().resolve("src/main/assets")
				.resolve(relativePath)), StandardCharsets.UTF_8);
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
