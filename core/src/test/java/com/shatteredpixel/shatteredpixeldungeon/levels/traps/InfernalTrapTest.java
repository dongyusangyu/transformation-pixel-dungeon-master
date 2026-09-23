package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import org.junit.Test;

import java.util.Arrays;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InfernalTrapTest {

	@Test
	public void usesTheUnusedOrangeGrillTrapSpriteAndNormalTrapRules() {
		InfernalTrap trap = new InfernalTrap();

		assertEquals(Trap.ORANGE, trap.color);
		assertEquals(Trap.GRILL, trap.shape);
		assertFalse(trap.preservesTerrain());
		assertFalse(trap.triggersOnEntry());
		assertTrue(trap.canBeHidden);
		assertTrue(trap.canBeSearched);
	}

	@Test
	public void distributesInfernoAcrossTheThreeByThreeAreaWithA1080Total() {
		int[] volumes = InfernalTrap.infernoVolumesForNeighbours(
				new boolean[]{true, true, true, true, true, true, true, true});

		assertEquals(1080, Arrays.stream(volumes).sum());
		for (int volume : volumes) assertEquals(120, volume);
	}

	@Test
	public void returnsBlockedNeighbourVolumeToTheCenter() {
		int[] volumes = InfernalTrap.infernoVolumesForNeighbours(
				new boolean[]{true, false, true, true, false, true, true, true});

		assertEquals(1080, Arrays.stream(volumes).sum());
		assertEquals(360, volumes[8]);
		assertEquals(0, volumes[1]);
		assertEquals(0, volumes[4]);
	}

	@Test
	public void releaseUsesWallAwareDistanceMap() throws IOException {
		String source = readSource();
		assertTrue(source.contains("PathFinder.buildDistanceMap"));
		assertTrue(source.contains("BArray.not(Dungeon.level.solid, null)"));
	}

	private static String readSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java")
				.resolve("com/shatteredpixel/shatteredpixeldungeon/levels/traps/InfernalTrap.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

}
