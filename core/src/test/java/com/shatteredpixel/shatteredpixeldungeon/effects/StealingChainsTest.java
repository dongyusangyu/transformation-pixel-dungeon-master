package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.utils.PointF;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StealingChainsTest {

	private static final PointF FROM = new PointF(10f, 20f);
	private static final PointF TO = new PointF(30f, 40f);

	@Test
	public void tipMovesFromThiefToHeroAndBack() {
		assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, 0f));
		assertPoint(20f, 30f, StealingChains.tipPosition(FROM, TO, 0.25f));
		assertPoint(30f, 40f, StealingChains.tipPosition(FROM, TO, 0.5f));
		assertPoint(20f, 30f, StealingChains.tipPosition(FROM, TO, 0.75f));
		assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, 1f));
	}

	@Test
	public void progressIsClampedAtBothEnds() {
		assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, -1f));
		assertPoint(10f, 20f, StealingChains.tipPosition(FROM, TO, 2f));
	}

	@Test
	public void lifecycleGuardsBothCallbacksAgainstDuplicateExecution() throws Exception {
		String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);
		assertTrue(source.contains("if (!contactCalled && progress >= 0.5f)"));
		assertTrue(source.contains("if (completionCalled) return;"));
	}

	private static void assertPoint(float x, float y, PointF actual) {
		assertEquals(x, actual.x, 0.0001f);
		assertEquals(y, actual.y, 0.0001f);
	}

	private static Path sourcePath() {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core"))
				? working.resolve("core") : working;
		return core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/StealingChains.java");
	}
}
