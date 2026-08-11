package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WarriorBossTest {

	@Test
	public void specialCounterOnlyAdvancesWhenMobileAndAwake() {
		assertEquals(6f, WarriorBoss.advanceSpecialCounter(5f, 0, false), 0f);
		assertEquals(5f, WarriorBoss.advanceSpecialCounter(5f, 1, false), 0f);
		assertEquals(5f, WarriorBoss.advanceSpecialCounter(5f, 0, true), 0f);
	}

	@Test
	public void leapTargetTracksEnemyCurrentPosition() {
		Char target = new Char() {
			@Override
			protected boolean act() {
				return false;
			}
		};
		target.pos = 31;

		assertEquals(31, WarriorBoss.currentLeapTarget(target));

		target.pos = 52;

		assertEquals(52, WarriorBoss.currentLeapTarget(target));
	}

	@Test
	public void playerLeapUsesCurrentPositionAsDirectBallisticaTarget() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/WarriorBoss.java");

		assertTrue(source.contains("leapPos = currentTargetPos;"));
		assertFalse(source.contains("chooseLeapLandingNearTarget"));
		assertTrue(source.contains("sprite.jump(pos, impactPos"));
		assertTrue(source.contains("findLeapLanding(impactPos)"));
	}

	@Test
	public void leapWaitsForItsCallbackAndDamagesEveryAdjacentCharacterBeforeKnockback() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/WarriorBoss.java");

		assertTrue(source.contains("boolean leapStarted = leap(leapPos);"));
		assertTrue(source.contains("return !leapStarted;"));
		assertFalse(source.contains("mob.alignment != Alignment.ENEMY"));
		assertTrue(source.contains("resolveLeapImpact(dest)"));
		assertTrue(source.indexOf("mob.damage(damage, this)")
				< source.indexOf("leapThrowChar(mob"));
	}

	@Test
	public void leapCooldownUsesControlledCounterRule() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/WarriorBoss.java");

		assertTrue(source.contains(
				"leapCooldown = advanceSpecialCounter(leapCooldown, paralysed, state == SLEEPING);"));
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
