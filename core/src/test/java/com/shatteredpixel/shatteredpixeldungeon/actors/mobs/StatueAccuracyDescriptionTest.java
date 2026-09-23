package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatueAccuracyDescriptionTest {

	@Test
	public void statueAccuracyFormulaAndBothBestiaryEntriesAgree() throws IOException {
		String statueSource = readCoreSource("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Statue.java");
		String armoredStatueSource = readCoreSource("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/ArmoredStatue.java");
		Properties chinese = loadChineseCustomMessages();

		assertTrue(statueSource.contains("(9 + Dungeon.scalingDepth()) * weapon.accuracyFactor( this, target )"));
		assertFalse(armoredStatueSource.contains("attackSkill("));
		assertTrue(chinese.getProperty("custom.dict.dict.mob_statue_d")
				.contains("精准为_手中武器精准修正*(9+楼层数)_"));
		assertTrue(chinese.getProperty("custom.dict.dict.mob_armored_statue_d")
				.contains("精准为_手中武器精准修正*(9+楼层数)_"));
		assertTrue(chinese.getProperty("custom.dict.dict.mob_statue_d").contains("闪避为_4+楼层数_"));
		assertTrue(chinese.getProperty("custom.dict.dict.mob_armored_statue_d").contains("闪避为_4+楼层数_"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path core = coreDirectory();
		return new String(Files.readAllBytes(core.resolve(relativePath)), StandardCharsets.UTF_8);
	}

	private static Properties loadChineseCustomMessages() throws IOException {
		Path core = coreDirectory();
		Properties messages = new Properties();
		Path path = core.resolve("src/main/assets/messages/custom/custom_zh.properties");
		try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		return messages;
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path core = workingDirectory.resolve("core");
		return Files.isDirectory(core) ? core : workingDirectory;
	}
}
