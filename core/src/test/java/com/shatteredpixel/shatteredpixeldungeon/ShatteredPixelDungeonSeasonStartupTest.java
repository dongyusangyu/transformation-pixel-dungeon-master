package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class ShatteredPixelDungeonSeasonStartupTest {

	@Test
	public void initializesSurfaceSeasonBeforeFirstSceneCreation() throws IOException {
		String source = new String(
				Files.readAllBytes(shatteredPixelDungeonSource()), StandardCharsets.UTF_8);
		int seasonInitialization = source.indexOf("SurfaceSeason.initializeFromSystemDate();");
		int firstSceneCreation = source.indexOf("super.create();");

		assertTrue("startup must initialize SurfaceSeason", seasonInitialization >= 0);
		assertTrue("season must be fixed before the first scene is created",
				firstSceneCreation >= 0 && seasonInitialization < firstSceneCreation);
	}

	private static Path shatteredPixelDungeonSource() {
		Path workingDirectory = Paths.get("").toAbsolutePath();
		Path fromRoot = workingDirectory.resolve(
				"core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java");
		if (Files.isRegularFile(fromRoot)) {
			return fromRoot;
		}
		return workingDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java");
	}
}
