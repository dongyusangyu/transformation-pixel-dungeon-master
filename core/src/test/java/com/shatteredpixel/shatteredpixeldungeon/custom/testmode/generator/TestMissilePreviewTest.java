package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestMissilePreviewTest {

	@Test
	public void missilePreviewUsesExAwareItemSprite() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestMissile.java");
		int methodStart = source.indexOf("private void createMissileImage(");
		int methodEnd = source.indexOf("private float iconGridBottom()", methodStart);
		String method = source.substring(methodStart, methodEnd);

		assertTrue(method.contains("new ItemSprite("));
		assertFalse(method.contains("ItemSpriteSheet.film.get("));
	}

	private static String readCoreSource(String relativePath) throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
