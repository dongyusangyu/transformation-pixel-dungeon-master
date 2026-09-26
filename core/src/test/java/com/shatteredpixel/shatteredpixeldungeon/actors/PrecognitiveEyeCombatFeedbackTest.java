package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PrecognitiveEye;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class PrecognitiveEyeCombatFeedbackTest {

	@Test
	public void momentaryForesightUsesItsDedicatedAtlasFrame() throws Exception {
		String floatingText = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/effects/FloatingText.java");
		assertTrue(floatingText.contains("MISS_PRECOGNITIVE_EYE = 86"));
	}

	@Test
	public void guaranteedEvasionIsResolvedBeforeStoredEyeDodge() throws Exception {
		String source = readCoreSource("com/shatteredpixel/shatteredpixeldungeon/actors/Char.java");
		int eyeForce = source.indexOf("boolean cursedEyeForcesHit = PrecognitiveEye.forcesEnemyHit");
		int guaranteedEvasion = source.indexOf("if (defStat >= INFINITE_EVASION)", eyeForce);
		int momentaryForesight = source.indexOf(
				"if (!cursedEyeForcesHit && PrecognitiveEye.consumeMomentaryForesight(attacker, defender))",
				eyeForce);

		assertTrue(eyeForce >= 0);
		assertTrue(guaranteedEvasion > eyeForce);
		assertTrue(momentaryForesight > guaranteedEvasion);
		assertTrue(source.substring(guaranteedEvasion, momentaryForesight)
				.contains("cursedEyeHitIcon(attacker, defender, true, damageTags)"));
	}

	@Test
	public void cursedEyeIconRequiresAnEnemyAttackThatWouldMissPhysically() {
		Hero hero = TestHeroFactory.create();
		PrecognitiveEye eye = new PrecognitiveEye();
		eye.cursed = true;
		hero.belongings.artifact = eye;
		Mob enemy = new Mob() {};
		enemy.alignment = Char.Alignment.ENEMY;

		assertEquals(FloatingText.HIT_CURSED_EYE,
				Char.cursedEyeHitIcon(enemy, hero, true, DamageTag.PHYSICAL));
		assertEquals(-1,
				Char.cursedEyeHitIcon(enemy, hero, false, DamageTag.PHYSICAL));
		assertEquals(-1,
				Char.cursedEyeHitIcon(enemy, hero, true, DamageTag.MAGICAL));
		enemy.alignment = Char.Alignment.ALLY;
		assertEquals(-1,
				Char.cursedEyeHitIcon(enemy, hero, true, DamageTag.PHYSICAL));
	}

	private static String readCoreSource(String relativePath) throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core"))
				? working.resolve("core") : working;
		Path source = core.resolve("src/main/java").resolve(relativePath);
		return Files.readString(source, StandardCharsets.UTF_8);
	}
}
