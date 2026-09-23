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

public class RimeTrapTest {

	@Test
	public void usesTheUnusedGreyWavesTrapSpriteAndNormalTrapRules() {
		RimeTrap trap = new RimeTrap();

		assertEquals(Trap.GREY, trap.color);
		assertEquals(Trap.WAVES, trap.shape);
		assertFalse(trap.preservesTerrain());
		assertFalse(trap.triggersOnEntry());
		assertTrue(trap.canBeHidden);
		assertTrue(trap.canBeSearched);
	}

	@Test
	public void distributesConfusionAndBlizzardAcrossTheFiveByFiveArea() {
		boolean[] open = new boolean[25];
		Arrays.fill(open, true);
		int[] confusion = RimeTrap.gasVolumesForArea(open, 0);
		int[] blizzard = RimeTrap.gasVolumesForArea(open, 80);

		assertEquals(1000, Arrays.stream(confusion).sum());
		assertEquals(1080, Arrays.stream(blizzard).sum());
		assertEquals(120, blizzard[12]);
	}

	@Test
	public void returnsBlockedCellVolumeToTheCenter() {
		boolean[] open = new boolean[25];
		Arrays.fill(open, true);
		open[0] = false;
		open[6] = false;

		int[] volumes = RimeTrap.gasVolumesForArea(open, 80);

		assertEquals(1080, Arrays.stream(volumes).sum());
		assertEquals(0, volumes[0]);
		assertEquals(0, volumes[6]);
		assertEquals(200, volumes[12]);
	}

	@Test
	public void schedulesARecoverableNextTurnRelease() throws IOException {
		String source = readSource();
		assertTrue(source.contains("Actor.addDelayed(delayed, Actor.TICK)"));
		assertTrue(source.contains("static class PendingRime extends Actor"));
		assertTrue(source.contains("ConfusionGas.class"));
		assertTrue(source.contains("Blizzard.class"));
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
				.resolve("com/shatteredpixel/shatteredpixeldungeon/levels/traps/RimeTrap.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
