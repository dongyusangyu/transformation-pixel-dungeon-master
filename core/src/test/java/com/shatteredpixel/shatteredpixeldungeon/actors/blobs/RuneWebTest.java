package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RuneSpinner;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuneWebTest {

	@Test
	public void weakWebIsNonBlockingAndShortLived() {
		assertFalse(Web.class.isAssignableFrom(RuneWeb.class));
		assertEquals(3, RuneWeb.STRENGTH);
		assertEquals(2f, RuneWeb.SLOW_DURATION, 0f);
	}

	@Test
	public void weakWebAppliesOnlyTwoTurnsOfSlow() throws Exception {
		String source = new String(Files.readAllBytes(Path.of(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/blobs/RuneWeb.java")),
				StandardCharsets.UTF_8).replaceAll("\\s+", "");

		assertTrue(source.contains("Buff.prolong(ch,Slow.class,SLOW_DURATION);"));
	}

	@Test
	public void runeSpinnerOverridesOrdinaryWebPlacement() throws Exception {
		assertEquals(RuneSpinner.class,
				RuneSpinner.class.getDeclaredMethod("applyWebToCell", int.class).getDeclaringClass());
		assertTrue(new RuneSpinner().isImmune(RuneWeb.class));
	}

	@Test
	public void levelConsumesWeakWebOnlyWhenHeroStepsOnIt() throws Exception {
		String source = new String(Files.readAllBytes(Path.of(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/Level.java")),
				StandardCharsets.UTF_8).replaceAll("\\s+", "");

		assertTrue(source.contains("ch==Dungeon.hero&&Blob.volumeAt(ch.pos,RuneWeb.class)>0"));
		assertTrue(source.contains("blobs.get(RuneWeb.class).clear(ch.pos);RuneWeb.affectChar(ch);"));
	}
}
