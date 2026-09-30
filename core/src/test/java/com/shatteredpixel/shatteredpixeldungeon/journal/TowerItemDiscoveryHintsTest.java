package com.shatteredpixel.shatteredpixeldungeon.journal;

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
import static org.junit.Assert.assertEquals;

public class TowerItemDiscoveryHintsTest {

	@Test
	public void cursedFlameWandIsCataloguedWithoutEnteringRandomWandPool() throws IOException {
		String catalog = readCoreSource("journal/Catalog.java");
		String generator = readCoreSource("items/Generator.java");
		assertTrue(catalog.contains("WANDS.addItems(WandOfCursedFlame.class)"));
		assertFalse(generator.contains("WandOfCursedFlame.class"));
	}

	@Test
	public void towerItemDiscoveryHintsExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadItemMessages("items.properties");
		Properties chinese = loadItemMessages("items_zh.properties");

		assertEquals("You can craft this item through alchemy.",
				defaults.getProperty("items.wands.wandofcursedflame.discover_hint"));
		assertEquals("你可通过炼金合成该物品。",
				chinese.getProperty("items.wands.wandofcursedflame.discover_hint"));

		assertEquals("You can obtain this item as a drop from a certain tower enemy.",
				defaults.getProperty("items.food.greenglowfruit.discover_hint"));
		assertEquals("你可从某种高塔敌人的掉落物中获得该物品。",
				chinese.getProperty("items.food.greenglowfruit.discover_hint"));
		assertEquals("You can obtain this item as a drop from a certain tower enemy.",
				defaults.getProperty("items.sourwinearoma.discover_hint"));
		assertEquals("你可从某种高塔敌人的掉落物中获得该物品。",
				chinese.getProperty("items.sourwinearoma.discover_hint"));
	}

	private static Properties loadItemMessages(String fileName) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/assets/messages/items").resolve(fileName);
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static String readCoreSource(String path) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve("src/main/java/com/shatteredpixel/"
				+ "shatteredpixeldungeon/" + path)), StandardCharsets.UTF_8);
	}
}
