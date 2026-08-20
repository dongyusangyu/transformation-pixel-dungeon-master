package com.shatteredpixel.shatteredpixeldungeon.levels;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class RegularLevelTest {

	@Test
	public void randomDestinationFallsBackWhenRoomsHaveNoPlaceableCells() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/levels/RegularLevel.java");
		int methodStart = source.indexOf("public int randomDestination( Char ch )");
		int methodEnd = source.indexOf("\n\t}", methodStart);
		String method = source.substring(methodStart, methodEnd);

		assertTrue(method.contains("return super.randomDestination(ch);"));
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
