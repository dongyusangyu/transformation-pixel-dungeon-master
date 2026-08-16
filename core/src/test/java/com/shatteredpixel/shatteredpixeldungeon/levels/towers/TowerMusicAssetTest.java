package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerMusicAssetTest {

	@Test
	public void towerMusicIsPackagedAsANonEmptyOggAsset() throws IOException {
		assertEquals("music/tower.ogg", Assets.Music.TOWER);
		Path music = coreDirectory().resolve("src/main/assets").resolve(Assets.Music.TOWER);

		assertTrue("tower music asset is missing", Files.isRegularFile(music));
		assertTrue("tower music asset is unexpectedly small", Files.size(music) > 100_000L);
		byte[] header = Files.readAllBytes(music);
		assertEquals('O', header[0]);
		assertEquals('g', header[1]);
		assertEquals('g', header[2]);
		assertEquals('S', header[3]);
	}

	@Test
	public void pestilenceBossUsesItsDedicatedPackagedMusic() throws IOException {
		assertEquals("music/pestilence_boss.ogg", Assets.Music.PESTILENCE_BOSS);
		assertEquals(Assets.Music.PESTILENCE_BOSS,
				TowerBossMusic.musicFor(TowerBossGenerator.PESTILENCE_KNIGHT_ID));
		assertEquals(Assets.Music.HALLS_BOSS, TowerBossMusic.musicFor("future_boss"));

		Path music = coreDirectory().resolve("src/main/assets")
				.resolve(Assets.Music.PESTILENCE_BOSS);
		assertTrue("pestilence boss music asset is missing", Files.isRegularFile(music));
		assertTrue("pestilence boss music asset is unexpectedly small", Files.size(music) > 100_000L);
		byte[] header = Files.readAllBytes(music);
		assertEquals('O', header[0]);
		assertEquals('g', header[1]);
		assertEquals('g', header[2]);
		assertEquals('S', header[3]);
	}

	@Test
	public void deathKnightUsesItsDedicatedPackagedMusic() throws IOException {
		assertEquals("music/death_knight_boss.ogg", Assets.Music.DEATH_KNIGHT_BOSS);
		assertEquals(Assets.Music.DEATH_KNIGHT_BOSS,
				TowerBossMusic.musicFor(TowerBossGenerator.DEATH_KNIGHT_ID));
		assertEquals(Assets.Music.PESTILENCE_BOSS,
				TowerBossMusic.musicFor(TowerBossGenerator.PESTILENCE_KNIGHT_ID));
		assertEquals(Assets.Music.HALLS_BOSS, TowerBossMusic.musicFor("future_boss"));

		Path music = coreDirectory().resolve("src/main/assets")
				.resolve(Assets.Music.DEATH_KNIGHT_BOSS);
		assertTrue("death knight boss music asset is missing", Files.isRegularFile(music));
		assertTrue("death knight boss music asset is unexpectedly small", Files.size(music) > 100_000L);
		byte[] header = Files.readAllBytes(music);
		assertEquals('O', header[0]);
		assertEquals('g', header[1]);
		assertEquals('g', header[2]);
		assertEquals('S', header[3]);
	}

	@Test
	public void normalTowerFloorsAlwaysUseDedicatedMusic() throws IOException {
		Path source = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java");
		String towerLevel = new String(Files.readAllBytes(source), StandardCharsets.UTF_8)
				.replace("\r\n", "\n");
		Path bossSource = coreDirectory().resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java");
		String towerBossLevel = new String(Files.readAllBytes(bossSource), StandardCharsets.UTF_8)
				.replace("\r\n", "\n");

		assertTrue(towerLevel.contains(
				"Music.INSTANCE.play(Assets.Music.TOWER, true);"));
		assertTrue(!towerLevel.contains("Statistics.amuletObtained"));
		assertTrue(towerBossLevel.contains("if (locked) {\n"
				+ "\t\t\tMusic.INSTANCE.play(TowerBossMusic.musicFor(encounter.selectedBossId()), true);\n"
				+ "\t\t} else {\n"
				+ "\t\t\tsuper.playLevelMusic();\n"
				+ "\t\t}"));
	}

	private static Path coreDirectory() {
		Path working = Paths.get(System.getProperty("user.dir"));
		return Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
	}
}
