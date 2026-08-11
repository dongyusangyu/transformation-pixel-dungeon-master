package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.WildDread;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StasisLifecycleTest {

	@Test
	public void notifiesReleaseListenersExactlyOnce() {
		CountingMob mob = new CountingMob();

		Stasis.notifyReleased(mob);

		assertEquals(1, mob.releaseCalls);
	}

	@Test
	public void ignoresMobsWithoutReleaseListeners() {
		Stasis.notifyReleased(new PlainMob());
	}

	@Test
	public void wildDreadIsNotAReleaseListener() {
		WildDread dread = new WildDread();

		assertFalse(dread instanceof Stasis.ReleaseListener);
		Stasis.notifyReleased(dread);
	}

	@Test
	public void releasesMobsOnlyAfterAddingThemToTheScene() {
		ListeningMob mob = new ListeningMob();

		Stasis.release(mob, () -> mob.added = true);

		assertTrue(mob.added);
		assertEquals(1, mob.releaseCalls);
	}

	@Test
	public void productionReleasePathsUseTheUnifiedStasisRelease() throws IOException {
		String stasisSource = source("Stasis.java");
		String beamingRaySource = source("BeamingRay.java");

		assertTrue(stasisSource.contains("release(stasisAlly);"));
		assertFalse(stasisSource.contains("GameScene.add(stasisAlly);"));
		assertTrue(beamingRaySource.contains("Stasis.release((Mob) ally);"));
		assertFalse(beamingRaySource.contains("GameScene.add((Mob) ally);"));
	}

	@Test
	public void discardsHeldAllyOnlyWhenItMatchesExpectedMob() {
		Mob held = new PlainMob();
		int[] detachCalls = {0};

		Stasis.discardHeldAlly(held, held, () -> detachCalls[0]++);

		assertEquals(1, detachCalls[0]);
	}

	@Test
	public void keepsHeldAllyWhenExpectedMobDoesNotMatch() {
		Mob held = new PlainMob();
		Mob expected = new PlainMob();
		int[] detachCalls = {0};

		Stasis.discardHeldAlly(held, expected, () -> detachCalls[0]++);

		assertEquals(0, detachCalls[0]);
	}

	private static String source(String fileName) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path sourceRoot = workingDirectory;
		if (!Files.isDirectory(sourceRoot.resolve("src/main/java"))) {
			sourceRoot = workingDirectory.resolve("core");
		}
		if (!Files.isDirectory(sourceRoot.resolve("src/main/java"))) {
			throw new AssertionError("Could not locate core source root from working directory: " + workingDirectory);
		}
		Path source = sourceRoot.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells").resolve(fileName);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static class CountingMob extends Mob implements Stasis.ReleaseListener {
		int releaseCalls;

		@Override
		public void onStasisReleased() {
			releaseCalls++;
		}
	}

	private static class ListeningMob extends Mob implements Stasis.ReleaseListener {
		boolean added;
		int releaseCalls;

		@Override
		public void onStasisReleased() {
			assertTrue(added);
			releaseCalls++;
		}
	}

	private static class PlainMob extends Mob {
	}
}
