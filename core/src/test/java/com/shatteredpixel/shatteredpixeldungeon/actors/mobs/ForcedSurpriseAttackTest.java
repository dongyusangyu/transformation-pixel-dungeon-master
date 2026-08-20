package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/** Regression coverage for the shared forced-surprise route used by cancel-style melee abilities. */
public class ForcedSurpriseAttackTest {

	@Test
	public void forcedSurprisePrecedesInfiniteEvasionAndUsesTheNativeMobRoute() throws Exception {
		String meleeWeapon = source("items/weapon/melee/MeleeWeapon.java");
		String charSource = source("actors/Char.java");
		String mobSource = source("actors/mobs/Mob.java");

		assertTrue(meleeWeapon.contains("isForcedSurpriseAttack(Char attacker, Char defender)"));
		int forcedHit = charSource.indexOf("MeleeWeapon.isForcedSurpriseAttack(attacker, defender)");
		int infiniteEvasion = charSource.indexOf("defStat >= INFINITE_EVASION");
		assertTrue("forced surprise must bypass even infinite evasion", forcedHit >= 0 && forcedHit < infiniteEvasion);
		assertTrue(charSource.substring(forcedHit, infiniteEvasion).contains("INFINITE_ACCURACY"));

		int surprisedBy = mobSource.indexOf("public boolean surprisedBy");
		int existingSurpriseRule = mobSource.indexOf("return enemy == hero", surprisedBy);
		int forcedMobRule = mobSource.indexOf("MeleeWeapon.isForcedSurpriseAttack(enemy, this)", surprisedBy);
		assertTrue("forced surprise must enter Mob.surprisedBy before the ordinary visibility rule",
				forcedMobRule >= surprisedBy && forcedMobRule < existingSurpriseRule);
	}

	private static String source(String relativePath) throws Exception {
		return new String(Files.readAllBytes(Paths.get("src/main/java/com/shatteredpixel/shatteredpixeldungeon/" + relativePath)),
				StandardCharsets.UTF_8);
	}
}
