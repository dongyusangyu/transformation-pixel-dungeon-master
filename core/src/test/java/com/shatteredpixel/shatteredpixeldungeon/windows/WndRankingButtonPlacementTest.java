package com.shatteredpixel.shatteredpixeldungeon.windows;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WndRankingButtonPlacementTest {

	@Test
	public void newCycleButtonBelongsToStatsTab() throws Exception {
		String source = readSource();
		int statsStart = source.indexOf("private class StatsTab");
		int challengesStart = source.lastIndexOf("private class ChallengesTab");
		assertTrue(statsStart >= 0);
		assertTrue(challengesStart > statsStart);
		String statsTab = source.substring(statsStart, challengesStart);

		assertTrue(statsTab.contains("RankingRestart.canBegin(record)"));
		assertTrue(statsTab.contains("Messages.get(ChallengesTab.class, \"restart\")"));
		assertTrue(statsTab.contains("HEIGHT - 16"));
		assertFalse(statsTab.contains("copy_to_hero_hall"));
	}

	@Test
	public void heroHallButtonBelongsToChallengesTab() throws Exception {
		String source = readSource();
		int challengesStart = source.lastIndexOf("private class ChallengesTab");
		int itemButtonStart = source.indexOf("private class ItemButton", challengesStart);
		assertTrue(challengesStart >= 0);
		assertTrue(itemButtonStart > challengesStart);
		String challengesTab = source.substring(challengesStart, itemButtonStart);

		assertTrue(challengesTab.contains("copy_to_hero_hall"));
		assertTrue(challengesTab.contains("heroHall.icon"));
		assertTrue(challengesTab.contains("title.bottom() + 12"));
		assertFalse(challengesTab.contains("RankingRestart.canBegin(record)"));
	}

	private static String readSource() throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = Files.isDirectory(workingDirectory.resolve("core"))
				? workingDirectory.resolve("core") : workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndRanking.java");
		String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
		return source;
	}
}
