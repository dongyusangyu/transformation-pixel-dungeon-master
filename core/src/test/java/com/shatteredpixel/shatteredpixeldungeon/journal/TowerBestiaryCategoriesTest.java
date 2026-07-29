package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TowerBestiaryCategoriesTest {

	@Test
	public void towerCategoriesAreTheFinalBestiaryGroups() {
		Bestiary[] categories = Bestiary.values();

		assertSame(Bestiary.TOWER_MOBS, categories[categories.length - 2]);
		assertSame(Bestiary.TOWER_BOSSES, categories[categories.length - 1]);
	}

	@Test
	public void towerCreaturesAreIsolatedFromNormalRegionalEnemies() {
		assertEquals(2, Bestiary.TOWER_MOBS.totalEntities());
		assertTrue(Bestiary.TOWER_MOBS.entities().contains(CamouflageGnoll.class));
		assertTrue(Bestiary.TOWER_MOBS.entities().contains(CorrosiveSwarm.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(CamouflageGnoll.class));
		assertTrue(!Bestiary.REGIONAL.entities().contains(CorrosiveSwarm.class));
		assertTrue(Bestiary.TOWER_BOSSES.entities().isEmpty());
	}

	@Test
	public void towerCategoryTitlesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadJournalMessages("journal.properties");
		Properties chinese = loadJournalMessages("journal_zh.properties");

		assertEquals("tower creatures",
				defaults.getProperty("journal.bestiary.tower_mobs.title"));
		assertEquals("tower bosses",
				defaults.getProperty("journal.bestiary.tower_bosses.title"));
		assertEquals("高塔生物",
				chinese.getProperty("journal.bestiary.tower_mobs.title"));
		assertEquals("高塔 Boss",
				chinese.getProperty("journal.bestiary.tower_bosses.title"));
	}

	private static Properties loadJournalMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/journal")
				.resolve(fileName);
		Properties messages = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		return messages;
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
