package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerDictionaryEntriesTest {

	private static final List<String> TOWER_KEYS = Arrays.asList(
			"tower_camouflage_gnoll",
			"tower_corrosive_swarm",
			"tower_corpse",
			"tower_earthly_serpent",
			"tower_roast_lamb_warlock",
			"tower_mechanical_fist",
			"tower_mimic_crocodile",
			"tower_obscura",
			"tower_wild_dread",
			"tower_alienated_prismatic_guard",
			"tower_twisted_mirror",
			"tower_soul_collector",
			"tower_powerful_wraith",
			"tower_heavy_crabification",
			"tower_marsh_slime",
			"tower_rune_spinner",
			"tower_chain_shadow_thief",
			"tower_pestilence_knight",
			"tower_death_knight",
			"tower_gentleman_elf"
	);
	private static final List<String> TOWER_ACTOR_KEYS = Arrays.asList(
			"actors.mobs.tmobs.camouflagegnoll",
			"actors.mobs.tmobs.corrosiveswarm",
			"actors.mobs.tmobs.corpse",
			"actors.mobs.tmobs.earthlyserpent",
			"actors.mobs.tmobs.roastlambwarlock",
			"actors.mobs.tmobs.mechanicalfist",
			"actors.mobs.tmobs.mimiccrocodile",
			"actors.mobs.tmobs.obscura",
			"actors.mobs.tmobs.wilddread",
			"actors.mobs.tmobs.alienatedprismaticguard",
			"actors.mobs.tmobs.twistedmirror",
			"actors.mobs.tmobs.soulcollector",
			"actors.mobs.tmobs.powerfulwraith",
			"actors.mobs.tmobs.heavycrabification",
			"actors.mobs.tmobs.marshslime",
			"actors.mobs.tmobs.runespinner",
			"actors.mobs.tmobs.chainshadowthief",
			"actors.mobs.tboss.pestilenceknight",
			"actors.mobs.tboss.deathknight",
			"actors.mobs.tboss.gentlemanelf"
	);

	@Test
	public void towerCreaturesHaveIndependentDictionaryEntriesAndIcons() throws IOException {
		String journal = readCoreFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java");
		String sprites = readCoreFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictSpriteSheet.java");
		int chapter = journal.indexOf("MOBS.d.put(\"tower_chapter\"");
		assertTrue(chapter >= 0);

		int previous = chapter;
		for (String key : TOWER_KEYS) {
			int position = journal.indexOf("MOBS.d.put(\"" + key + "\"", previous);
			assertTrue(key + " must be registered in tower order", position > previous);
			previous = position;
			assertTrue(key + " must use a dedicated sprite constant",
					sprites.contains("case " + key.toUpperCase() + ":"));
		}
	}

	@Test
	public void towerEntriesHaveCompleteDefaultAndChineseDetails() throws IOException {
		Properties defaults = loadMessages("custom.properties");
		Properties chinese = loadMessages("custom_zh.properties");
		String prefix = "custom.dict.dict.";

		for (String key : TOWER_KEYS) {
			assertFalse(key + " default title", defaults.getProperty(prefix + key, "").isEmpty());
			assertFalse(key + " default details", defaults.getProperty(prefix + key + "_d", "").isEmpty());
			assertFalse(key + " Chinese title", chinese.getProperty(prefix + key, "").isEmpty());
			String details = chinese.getProperty(prefix + key + "_d", "");
			assertFalse(key + " Chinese details", details.isEmpty());
			assertTrue(key + " details must state maximum health",
					details.contains("\u6700\u5927\u751f\u547d"));
			assertTrue(key + " details must state vision",
					details.contains("\u89c6\u91ce"));
		}
	}

	@Test
	public void chineseGameplayDescriptionsStayConciseAndNonNumeric() throws IOException {
		Properties chinese = loadActorMessages("actors_zh.properties");
		for (String key : TOWER_ACTOR_KEYS) {
			String description = chinese.getProperty(key + ".desc", "");
			assertFalse(key + " gameplay description", description.isEmpty());
			assertTrue(key + " gameplay description is too long", description.length() <= 90);
			assertFalse(key + " gameplay description contains detailed numbers",
					description.matches(".*[0-9%].*"));
		}
	}

	private static Properties loadMessages(String fileName) throws IOException {
		Path path = coreDirectory().resolve("src/main/assets/messages/custom").resolve(fileName);
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static Properties loadActorMessages(String fileName) throws IOException {
		Path path = coreDirectory().resolve("src/main/assets/messages/actors").resolve(fileName);
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
