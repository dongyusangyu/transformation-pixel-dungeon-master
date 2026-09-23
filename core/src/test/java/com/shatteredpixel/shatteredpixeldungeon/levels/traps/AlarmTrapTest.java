package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.assertTrue;

public class AlarmTrapTest {

	@Test
	public void alarmIteratesOverSnapshotBecauseBeckonCallbacksCanChangeMobSet() throws IOException {
		String source = readAlarmTrapSource();

		assertTrue(source.contains("Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);"));
		assertTrue(source.contains("for (Mob mob : mobs)"));
	}

	private static String readAlarmTrapSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
				.resolve("levels/traps/AlarmTrap.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
