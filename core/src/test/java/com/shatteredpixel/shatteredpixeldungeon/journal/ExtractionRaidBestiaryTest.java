package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ChronoSuccubus;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.DeferredScorpio;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VaultArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VeilbreakerEye;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ExtractionRaidBestiaryTest {

	private static final Class<?>[] RAID_ENEMIES = {
			VaultArmoredStatue.class,
			VeilbreakerEye.class,
			ChronoSuccubus.class,
			DeferredScorpio.class
	};

	@Test
	public void raidEnemiesAreLastInQuestEnemiesAndBossesOnly() {
		ArrayList<Class<?>> questEntries = new ArrayList<>(Bestiary.QUEST.entities());
		assertEquals(Arrays.asList(RAID_ENEMIES),
				questEntries.subList(questEntries.size() - RAID_ENEMIES.length, questEntries.size()));
		for (Bestiary category : Bestiary.values()) {
			for (Class<?> raidEnemy : RAID_ENEMIES) {
				assertEquals(category == Bestiary.QUEST, category.entities().contains(raidEnemy));
			}
		}
	}

	@Test
	public void raidEnemyDiscoveryHintsMatchOtherQuestEnemies() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String[] prefixes = {
				"levels.minigame.extraction.mobs.vaultarmoredstatue.",
				"levels.minigame.extraction.mobs.veilbreakereye.",
				"levels.minigame.extraction.mobs.chronosuccubus.",
				"levels.minigame.extraction.mobs.deferredscorpio."
		};
		for (String prefix : prefixes) {
			assertEquals("You can find this enemy during a certain quest.",
					defaults.getProperty(prefix + "discover_hint"));
			assertEquals("你可在某个任务中发现该敌人。",
					chinese.getProperty(prefix + "discover_hint"));
		}
	}

	private static Properties loadActorMessages(String fileName) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/assets/messages/actors").resolve(fileName);
		Properties messages = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		return messages;
	}
}
