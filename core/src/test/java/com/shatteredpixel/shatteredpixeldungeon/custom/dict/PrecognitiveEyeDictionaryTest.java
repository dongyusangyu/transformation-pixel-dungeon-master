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

public class PrecognitiveEyeDictionaryTest {

	@Test
	public void artifactIsRegisteredWithItsExtensionSprite() throws IOException {
		String journal = new String(Files.readAllBytes(coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java")),
				StandardCharsets.UTF_8);
		assertTrue(journal.contains(
				"ARTIFACTS.d.put(\"artifact_eye\",              EXItemSpriteSheet.PRECOGNITIVE_EYE);"));
	}

	@Test
	public void bilingualDetailsDescribeTheFullArtifactContract() throws IOException {
		Properties defaults = loadMessages("custom.properties");
		Properties chinese = loadMessages("custom_zh.properties");
		String defaultDetails = defaults.getProperty("custom.dict.dict.artifact_eye_d", "");
		String chineseDetails = chinese.getProperty("custom.dict.dict.artifact_eye_d", "");

		assertFalse(defaults.getProperty("custom.dict.dict.artifact_eye", "").isEmpty());
		assertFalse(chinese.getProperty("custom.dict.dict.artifact_eye", "").isEmpty());
		assertTrue(defaultDetails.contains("20% charge"));
		assertTrue(defaultDetails.contains("artifact level + 1"));
		assertTrue(defaultDetails.contains("maximum level is _5_"));
		assertFalse(defaultDetails.contains("overheat"));
		assertTrue(defaultDetails.contains("Momentary Foresight"));
		assertTrue(defaultDetails.contains("Trinity"));
		assertTrue(chineseDetails.contains("20%充能"));
		assertTrue(chineseDetails.contains("神器等级+1"));
		assertTrue(chineseDetails.contains("最高_5_级"));
		assertFalse(chineseDetails.contains("过热"));
		assertTrue(chineseDetails.contains("神器充能"));
		assertTrue(chineseDetails.contains("诅咒"));
		assertTrue(chineseDetails.contains("护甲技能"));
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
		if (!coreDirectory.endsWith("core")) {
			coreDirectory = coreDirectory.resolve("core");
		}
		return coreDirectory;
	}
}
