package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HuntressBossBestiaryTest {

	private static final Class<?>[] HUNTRESS_ENTRIES = {
			HuntressBoss.class,
			HuntressBoss.DistractingHawk.class,
			HuntressBoss.HuntressTentacle.class
	};
	private static final String[] HUNTRESS_MESSAGE_PREFIXES = {
			"actors.mobs.huntressboss",
			"actors.mobs.huntressboss$distractinghawk",
			"actors.mobs.huntressboss$huntresstentacle"
	};

	@Test
	public void huntressAndDerivativesHaveIndependentBossEntries() {
		for (Class<?> entry : HUNTRESS_ENTRIES) {
			assertTrue(entry.getSimpleName() + " missing from BOSSES",
					Bestiary.BOSSES.entities().contains(entry));
		}
	}

	@Test
	public void huntressEntriesDoNotLeakIntoOtherCategories() {
		for (Bestiary category : Bestiary.values()) {
			if (category == Bestiary.BOSSES) {
				continue;
			}
			for (Class<?> entry : HUNTRESS_ENTRIES) {
				assertFalse(entry.getSimpleName() + " unexpectedly listed in " + category,
						category.entities().contains(entry));
			}
		}
	}

	@Test
	public void huntressEntriesHaveCompleteDefaultAndChineseMessages() throws IOException {
		for (String fileName : new String[]{"actors.properties", "actors_zh.properties"}) {
			Properties messages = loadActorMessages(fileName);
			for (String prefix : HUNTRESS_MESSAGE_PREFIXES) {
				for (String suffix : new String[]{"name", "desc", "discover_hint"}) {
					String key = prefix + "." + suffix;
					String value = messages.getProperty(key);
					assertNotNull(fileName + " missing " + key, value);
					assertFalse(fileName + " has an empty " + key, value.trim().isEmpty());
				}
			}
		}
	}

	private static Properties loadActorMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/actors")
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
