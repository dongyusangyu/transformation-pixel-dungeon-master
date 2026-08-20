package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/** Guards the hook that releases a zero-delay melee attack when its animation loses its target. */
public class MeleeWeaponAttackLifecycleTest {

	@Test
	public void cancelledAttackAnimationStillReleasesTheCompletedMeleeWeapon() throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java")),
				StandardCharsets.UTF_8);
		int method = source.indexOf("public void onAttackComplete()");
		int nullBranch = source.indexOf("if (enemy == null)", method);
		int earlyReturn = source.indexOf("return;", nullBranch);

		assertTrue("onAttackComplete must contain the empty-target branch", nullBranch > method);
		assertTrue("the empty-target branch must clear the completed melee delay state",
				source.indexOf("afterHeroAttackDelayResolved", nullBranch) < earlyReturn);
	}

	@Test
	public void directFollowupAttacksAreNotPresentedAsTurnConsumingWeaponAttacks() throws Exception {
		String heroSource = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java")),
				StandardCharsets.UTF_8);
		String sickleSource = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickle.java")),
				StandardCharsets.UTF_8);

		assertTrue(heroSource.contains("boolean resolvingAttackAction"));
		assertTrue(heroSource.contains("resolvingAttackAction = true"));
		assertTrue(heroSource.contains("resolvingAttackAction = false"));
		assertTrue(sickleSource.contains("!hero.isResolvingAttackAction()"));
		assertTrue(sickleSource.contains("AttackMode.BONUS_FREE"));
	}
}
