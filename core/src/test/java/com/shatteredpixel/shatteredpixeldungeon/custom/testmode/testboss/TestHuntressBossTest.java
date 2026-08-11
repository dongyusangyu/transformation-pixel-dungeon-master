package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestHuntressBossTest {

	private static final Path PROJECT_ROOT = Paths.get("..").toAbsolutePath().normalize();

	@Test
	public void testVariantReusesHuntressCombatWithoutCampaignDeathEffects() {
		ExposedTestHuntressBoss boss = new ExposedTestHuntressBoss();

		assertTrue(boss instanceof HuntressBoss);
		assertFalse(boss.campaignDeathEffectsEnabled());
	}

	@Test
	public void testVariantUsesCurrentFloorHelpersInsteadOfBossLevel() throws IOException {
		String source = readMainSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/testmode/testboss/TestHuntressBoss.java");

		assertTrue(source.contains("TestBossUtil.summonNear"));
		assertTrue(source.contains("TestBossUtil.randomSpawnCellNear"));
		assertTrue(source.contains("onWardenPhaseStarted"));
		assertTrue(source.contains("triggerFadeleafBoon"));
		assertFalse(source.contains("HuntressBossLevel"));
	}

	@Test
	public void mobPlacerRegistersTestHuntressBoss() throws IOException {
		String source = readMainSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/testmode/MobPlacer.java");

		assertTrue(source.contains("import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestHuntressBoss;"));
		assertTrue(source.contains("TEST_HUNTRESS_BOSS(TestHuntressBoss.class"));
	}

	@Test
	public void testVariantHasEnglishAndChineseJournalText() throws IOException {
		Properties english = loadProperties("custom/custom.properties");
		Properties chinese = loadProperties("custom/custom_zh.properties");
		String key = "custom.testmode.testboss.testhuntressboss.";

		assertEquals("Test Huntress?", english.getProperty(key + "name"));
		assertTrue(english.getProperty(key + "desc").contains("arbitrary floor"));
		assertEquals("Test：女猎手？", chinese.getProperty(key + "name"));
		assertTrue(chinese.getProperty(key + "desc").contains("任意楼层"));
	}

	private static String readMainSource(String relativePath) throws IOException {
		Path path = PROJECT_ROOT.resolve("core/src/main/java").resolve(relativePath);
		assertTrue("Missing source file: " + path, Files.exists(path));
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	private static Properties loadProperties(String relativePath) throws IOException {
		Properties properties = new Properties();
		Path path = PROJECT_ROOT.resolve("core/src/main/assets/messages").resolve(relativePath);
		try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static class ExposedTestHuntressBoss extends TestHuntressBoss {
		boolean campaignDeathEffectsEnabled() {
			return usesCampaignDeathEffects();
		}
	}
}
