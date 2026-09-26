package com.shatteredpixel.shatteredpixeldungeon.effects;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class PushingSchedulerOrderTest {

	@Test
	public void schedulerStartsConcurrentPushesBeforeWaiting() throws IOException {
		String source = compactSource();
		String act = blockStartingAt(source, "protectedbooleanact()");
		String startDuePushes = blockStartingAt(source, "privatestaticvoidstartDuePushes()");

		assertTrue("a Pushing actor must remain scheduled while its visual is active",
				!act.contains("Actor.remove(Pushing.this)"));
		assertTrue("the scheduler must start all due pushes together",
				act.contains("startDuePushes()"));
		assertTrue("concurrent pushes must be enumerated from the actor snapshot",
				startDuePushes.contains("Actor.all()"));
		assertTrue("the scheduled push should start only once",
				startDuePushes.contains("beginEffect()"));
	}

	@Test
	public void movementCallbackFinishesBeforeSchedulerIsReleased() throws IOException {
		String source = compactSource();
		String finish = blockStartingAt(source, "privatevoidfinish(boolean");

		int heldRemoval = finish.indexOf("Actor.removeButKeepCurrent(this)");
		int movementCallback = finish.indexOf("completeCallback()");
		int release = finish.indexOf("next()");

		assertTrue("push resolution must remove itself without waking the actor thread",
				heldRemoval >= 0 && heldRemoval < movementCallback);
		assertTrue("the actor thread must remain blocked until position and collision callbacks finish",
				movementCallback >= 0 && movementCallback < release);
	}

	@Test
	public void saveBoundaryWaitsForPendingPushes() throws IOException {
		String source = compactSource("scenes/GameScene.java");
		String idleCheck = blockStartingAt(source, "privatestaticbooleanactorThreadIdle()");
		assertTrue("checkpoints must not capture characters before knockback callbacks resolve",
				idleCheck.contains("!Pushing.hasPendingPushes()"));
	}

	private static String compactSource() throws IOException {
		return compactSource("effects/Pushing.java");
	}

	private static String compactSource(String sourceFile) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/" + sourceFile);
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8)
				.replaceAll("\\s+", "");
	}

	private static String blockStartingAt(String source, String marker) {
		int markerStart = source.indexOf(marker);
		assertTrue("missing source marker: " + marker, markerStart >= 0);
		int blockStart = source.indexOf('{', markerStart);
		assertTrue("missing block for source marker: " + marker, blockStart >= 0);
		int depth = 0;
		for (int i = blockStart; i < source.length(); i++) {
			char current = source.charAt(i);
			if (current == '{') depth++;
			if (current == '}' && --depth == 0) {
				return source.substring(markerStart, i + 1);
			}
		}
		throw new AssertionError("unterminated block for source marker: " + marker);
	}
}
