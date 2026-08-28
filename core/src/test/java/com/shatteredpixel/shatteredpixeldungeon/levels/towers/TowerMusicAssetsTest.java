package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerMusicAssetsTest {

	@Test
	public void adaptiveTowerTracksUseCanonicalLowercasePaths() {
		assertEquals("music/tower3.ogg", Assets.Music.TOWER_3);
		assertEquals("music/tower_busy.ogg", Assets.Music.TOWER_BUSY);
		assertTrue(Files.isRegularFile(musicPath("tower3.ogg")));
		assertTrue(Files.isRegularFile(musicPath("tower_busy.ogg")));
		assertFalse(Files.exists(musicPath("TowerBuzy.ogg")));
	}

	private static Path musicPath(String name) {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return coreDirectory.resolve("src/main/assets/music").resolve(name);
	}
}
