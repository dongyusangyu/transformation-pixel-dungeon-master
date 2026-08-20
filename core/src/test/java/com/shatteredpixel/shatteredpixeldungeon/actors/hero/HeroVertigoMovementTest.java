package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class HeroVertigoMovementTest {

	@Test
	public void vertigoMovementDoesNotPreMoveTheSpriteToTheRequestedCell() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java");

		assertTrue(source.contains(
				"if (buff(Vertigo.class) == null || !Dungeon.level.adjacent(pos, step))"));
		assertTrue(source.contains("sprite.move(pos, step);"));
	}

	@Test
	public void rejectedVertigoMovementResynchronizesTheSprite() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/Char.java");

		assertTrue(source.contains("sprite.place(pos);"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
