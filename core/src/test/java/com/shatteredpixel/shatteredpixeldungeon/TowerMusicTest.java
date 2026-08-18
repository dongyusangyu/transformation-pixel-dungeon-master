package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerMusicTest {

	@Test
	public void towerNormalMusicUsesBothTowerTracks() throws Exception {
		assertEquals("music/tower2.ogg", Assets.Music.TOWER_2);

		String towerLevel = readCoreSource(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java");
		assertTrue(towerLevel.contains(
				"new String[]{Assets.Music.TOWER, Assets.Music.TOWER_2}"));
		assertTrue(towerLevel.contains(
				"new float[]{1f, 1f}"));
		assertTrue(towerLevel.contains(
				"Music.INSTANCE.playTracks(TOWER_TRACK_LIST, TOWER_TRACK_CHANCES, false)"));
	}

	private static String readCoreSource(String relativePath) throws Exception {
		Path root = Path.of(System.getProperty("user.dir"));
		Path source = Files.exists(root.resolve(relativePath))
				? root.resolve(relativePath)
				: root.resolve("core").resolve(relativePath);
		return Files.readString(source, StandardCharsets.UTF_8);
	}
}
