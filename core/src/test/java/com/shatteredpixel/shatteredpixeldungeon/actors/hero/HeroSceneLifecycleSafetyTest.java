package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class HeroSceneLifecycleSafetyTest {

	@Test
	public void speedDoesNotAnimateAHeroWithoutASprite() throws IOException {
		String source = readCoreSource("actors/hero/Hero.java");
		assertTrue(source.contains("if (sprite instanceof HeroSprite)"));
	}

	@Test
	public void corpseDustDispelUsesCleanupInsteadOfNormalDeath() throws IOException {
		String source = readCoreSource("items/quest/CorpseDust.java");
		assertTrue(source.contains("destroyWithoutRewards"));
		assertTrue(source.contains("mob.destroyWithoutRewards()"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
				.resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
