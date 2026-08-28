package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class GhostTeleportTest {

	@Test
	public void ghostUsesAnExitRoomTeleportPolicy() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Ghost.java");

		assertTrue(source.contains("teleportWithinQuestArea"));
		assertTrue(source.contains("room(Dungeon.level.exit())"));
		assertTrue(source.contains("Dungeon.level.openSpace[cell]"));
		assertTrue(source.contains("Dungeon.level.heaps.get(cell) == null"));
	}

	@Test
	public void trapTeleportsDoNotMoveGhostToGlobalDestination() throws IOException {
		String teleportTrap = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/levels/traps/TeleportationTrap.java");
		String gatewayTrap = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/levels/traps/GatewayTrap.java");

		assertTrue(teleportTrap.contains("teleportWithinQuestArea"));
		assertTrue(gatewayTrap.contains("teleportWithinQuestArea"));
	}

	@Test
	public void questBossSpawnUsesTheQuestBossProperties() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Ghost.java");

		assertTrue(source.contains(
				"Dungeon.level.randomRespawnCell( questBoss )"));
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
