package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RogueBossTest {

	@Test
	public void specialCountersOnlyAdvanceWhenMobileAndAwake() {
		assertEquals(6, RogueBoss.advanceSpecialCounter(5, 0, false));
		assertEquals(10, RogueBoss.advanceSpecialCounter(10, 0, false));
		assertEquals(10, RogueBoss.advanceSpecialCounter(99, 1, false));
		assertEquals(5, RogueBoss.advanceSpecialCounter(5, 1, false));
		assertEquals(5, RogueBoss.advanceSpecialCounter(5, 0, true));
		assertEquals(6f, RogueBoss.advanceSpecialCounter(5f, 0, false), 0f);
		assertEquals(5f, RogueBoss.advanceSpecialCounter(5f, 1, false), 0f);
		assertEquals(5f, RogueBoss.advanceSpecialCounter(5f, 0, true), 0f);
		assertEquals(10f, RogueBoss.advanceSpecialCounter(10f, 0, false), 0f);
		assertEquals(10f, RogueBoss.advanceSpecialCounter(99f, 1, false), 0f);
	}

	@Test
	public void executionFailureMustSpendTimeBeforeRetrying() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RogueBoss.java");
		int execution = source.indexOf("InvisibilityAttack >= 10");
		int afterExecution = source.indexOf("if (state != SLEEPING)", execution);
		String block = source.substring(execution, afterExecution);

		assertTrue("a failed execution attempt must consume a turn", block.contains("spend(TICK)"));
	}

	@Test
	public void executionLandingCanClearAnAdjacentSheep() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RogueBoss.java");

		assertTrue("execution fallback must recognize sheep blockers",
				source.contains("instanceof Sheep"));
		assertTrue("execution fallback must remove a sheep before using its cell",
				source.contains(".die(null)"));
	}

	@Test
	public void sleepingFreezesCooldownWhileOtherNonHuntingStatesResetIt() {
		assertFalse(RogueBoss.shouldResetInvisibilityCooldown(true, false, 0));
		assertFalse(RogueBoss.shouldResetInvisibilityCooldown(false, true, 0));
		assertFalse(RogueBoss.shouldResetInvisibilityCooldown(false, false, 1));
		assertTrue(RogueBoss.shouldResetInvisibilityCooldown(false, false, 0));
	}

	@Test
	public void bothInvisibilityCountersUseControlledCounterRule() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RogueBoss.java");

		assertTrue(source.contains(
				"InvisibilityCoolDown = advanceSpecialCounter("));
		assertTrue(source.contains(
				"InvisibilityAttack = advanceSpecialCounter("));
	}

	@Test
	public void relocationKeepsSpriteAndLogicalPositionInSync() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RogueBoss.java");

		assertTrue(source.contains("relocateWithSpriteSync(p);"));
		assertTrue(source.contains("relocateWithSpriteSync(newPos);"));
		assertTrue(source.contains("sprite.interruptMotion();"));
		assertTrue(source.contains("sprite.place(destination);"));
		assertTrue(source.contains("move(destination, false);"));
	}

	@Test
	public void relocationCandidateMustBeInsidePassableUnoccupiedCell() {
		assertTrue(RogueBoss.isValidRelocationCell(10, 100, true, false));
		assertFalse(RogueBoss.isValidRelocationCell(-1, 100, true, false));
		assertFalse(RogueBoss.isValidRelocationCell(100, 100, true, false));
		assertFalse(RogueBoss.isValidRelocationCell(10, 100, false, false));
		assertFalse(RogueBoss.isValidRelocationCell(10, 100, true, true));
	}

	@Test
	public void specialRelocationUsesInstantPlacementInsteadOfMovementTween() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RogueBoss.java");

		assertFalse(source.contains("sprite.move(from, destination);"));
		assertTrue(source.contains("sprite.place(destination);"));
	}

	@Test
	public void testModeRelocationUsesTheSameInstantPlacementRule() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/testmode/testboss/TestRogueBoss.java");

		assertFalse(source.contains("sprite.move(pos, newPos);"));
		assertTrue(source.contains("move(destination, false);"));
		assertTrue(source.contains("sprite.place(destination);"));
	}

	@Test
	public void phaseSummonDoesNotDereferenceMissingEnemy() throws IOException {
		assertEquals(-1, RogueBoss.summonTargetCell(-1, false, -1, false, 100));
		assertEquals(42, RogueBoss.summonTargetCell(42, true, 77, true, 100));
		assertEquals(77, RogueBoss.summonTargetCell(-1, false, 77, true, 100));
		assertEquals(77, RogueBoss.summonTargetCell(100, true, 77, true, 100));

		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/RogueBoss.java");
		int phaseChange = source.indexOf("if (phase == 0 && HP<100)");
		int nextPhase = source.indexOf("if ((HP <= HT/2)", phaseChange);
		String block = source.substring(phaseChange, nextPhase);

		assertTrue("phase summon must resolve a safe fallback target",
				block.contains("summonTargetCell"));
		assertTrue("phase summon must skip beckon when no target exists",
				block.contains("targetCell >= 0"));
	}

	@Test
	public void shadowSummonChoosesOnlyAnOpenValidCell() {
		int[] candidates = {0, 10, 101, 42};
		boolean[] passable = new boolean[100];
		boolean[] occupied = new boolean[100];
		passable[0] = true;
		passable[10] = true;
		passable[42] = true;
		occupied[0] = true;
		occupied[10] = true;

		assertEquals(42, RogueBoss.firstValidSpawnCell(candidates, 100, passable, occupied));
		occupied[42] = true;
		assertEquals(-1, RogueBoss.firstValidSpawnCell(candidates, 100, passable, occupied));
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
