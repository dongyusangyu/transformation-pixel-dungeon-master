package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SurfaceTownMusicTest {

	@Test
	public void surfaceTownMusicIsRegisteredAsALoopingLevelTrack() throws IOException {
		Path core = coreDirectory();
		Path assets = core.resolve("src/main/assets");
		String levelSource = Files.readString(
				core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java"),
				StandardCharsets.UTF_8);

		assertEquals("music/surface_town.ogg", Assets.Music.SURFACE_TOWN);
		assertTrue(levelSource.contains("Music.INSTANCE.play(Assets.Music.SURFACE_TOWN, true);"));
		assertTrue(Files.isRegularFile(assets.resolve("music/surface_town.ogg")));
	}

	@Test
	public void surfaceTownOggHasAValidContainerHeader() throws IOException {
		Path ogg = coreDirectory().resolve("src/main/assets/music/surface_town.ogg");
		byte[] header = Files.readAllBytes(ogg);

		assertTrue("surface town OGG is unexpectedly small", header.length > 100_000);
		assertEquals('O', header[0]);
		assertEquals('g', header[1]);
		assertEquals('g', header[2]);
		assertEquals('S', header[3]);
	}

	@Test
	public void iosOggFallbackIsPackagedWithTheSameTrackName() throws IOException {
		Path mp3 = coreDirectory().resolve("src/main/assets/music/surface_town.mp3");
		assertTrue("surface town MP3 fallback is missing", Files.isRegularFile(mp3));
		assertTrue("surface town MP3 fallback is unexpectedly small", Files.size(mp3) > 100_000L);
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
	}
}
