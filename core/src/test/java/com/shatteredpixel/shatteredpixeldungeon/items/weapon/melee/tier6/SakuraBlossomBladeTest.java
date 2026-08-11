package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SakuraBlossomBladeTest {

	@Test
	public void usesTierSixAssassinsBladeStatsAtReachOne() throws IOException {
		assertTrue(AssassinsBlade.class.isAssignableFrom(SakuraBlossomBlade.class));
		assertEquals(Dagger.class, MeleeWeapon.abilityType(SakuraBlossomBlade.class));
		assertTrue(source().contains("extends AssassinsBlade"));
		assertTrue(source().contains("tier = 6;"));
		assertTrue(source().contains("RCH = 1;"));

		TierSixDaggerFormulaProbe weapon = new TierSixDaggerFormulaProbe();
		assertEquals(6, weapon.min(0));
		assertEquals(28, weapon.max(0));
		assertEquals(9, weapon.min(3));
		assertEquals(49, weapon.max(3));
		assertEquals(20, weapon.STRReq(0));
		assertEquals(1, weapon.reachFactor(null));
	}

	@Test
	public void sneakAlwaysGrantsTwoTurnsInsteadOfScalingWithLevel() throws IOException {
		assertTrue(source().contains("Dagger.sneakAbility(hero, target, 3, 2, this);"));
		assertTrue(source().contains("public String upgradeAbilityStat(int level)"));
		assertTrue(source().contains("return \"2\";"));
	}

	@Test
	public void everyFifthSuccessfulHitTriggersBeforeEvolution() {
		SakuraBlossomBlade.State state = new SakuraBlossomBlade.State();

		for (int i = 0; i < 4; i++) {
			assertFalse(state.recordSuccessfulHit());
		}
		assertTrue(state.recordSuccessfulHit());
		assertFalse(state.recordSuccessfulHit());
		assertEquals(4, state.hitsUntilSpecial());
	}

	@Test
	public void evolutionUnlocksAtFiftyEightKillsAndIsIrreversible() {
		SakuraBlossomBlade.State state = new SakuraBlossomBlade.State();

		for (int i = 0; i < 57; i++) {
			assertFalse(state.recordKill());
		}
		assertFalse(state.canEvolve());
		assertEquals(1, state.killsUntilEvolution());
		assertTrue(state.recordKill());
		assertTrue(state.canEvolve());
		assertFalse(state.isEvolved());

		assertTrue(state.evolve());
		assertTrue(state.isEvolved());
		assertFalse(state.canEvolve());
		assertFalse(state.evolve());
		assertEquals(0, state.killsUntilEvolution());
		assertEquals(EXItemSpriteSheet.BLOOD_SAKURA, state.image());
		assertFalse(state.recordSuccessfulHit());
	}

	@Test
	public void evolvedHealingRoundsTenPercentOfDamage() {
		assertEquals(0, SakuraBlossomBlade.healingForDamage(4));
		assertEquals(1, SakuraBlossomBlade.healingForDamage(5));
		assertEquals(1, SakuraBlossomBlade.healingForDamage(14));
		assertEquals(2, SakuraBlossomBlade.healingForDamage(15));
		assertEquals(10, SakuraBlossomBlade.healingForDamage(100));
	}

	@Test
	public void hitKillAndEvolutionStateSurviveBundleRoundTrip() {
		SakuraBlossomBlade.State original = new SakuraBlossomBlade.State();
		for (int i = 0; i < 3; i++) original.recordSuccessfulHit();
		for (int i = 0; i < 58; i++) original.recordKill();

		Bundle readyBundle = new Bundle();
		original.storeInBundle(readyBundle);
		SakuraBlossomBlade.State ready = new SakuraBlossomBlade.State();
		ready.restoreFromBundle(readyBundle);

		assertFalse(ready.isEvolved());
		assertTrue(ready.canEvolve());
		assertEquals(2, ready.hitsUntilSpecial());
		assertEquals(EXItemSpriteSheet.SAKURA_BLOSSOM, ready.image());

		assertTrue(ready.evolve());
		Bundle evolvedBundle = new Bundle();
		ready.storeInBundle(evolvedBundle);
		SakuraBlossomBlade.State evolved = new SakuraBlossomBlade.State();
		evolved.restoreFromBundle(evolvedBundle);

		assertTrue(evolved.isEvolved());
		assertEquals(0, evolved.killsUntilEvolution());
		assertEquals(EXItemSpriteSheet.BLOOD_SAKURA, evolved.image());
		assertFalse(evolved.recordSuccessfulHit());
	}

	public static class TierSixDaggerFormulaProbe extends MeleeWeapon {
		{
			tier = 6;
			RCH = 1;
		}

		@Override
		public int max(int lvl) {
			return 4 * (tier + 1) + lvl * (tier + 1);
		}
	}

	private static String source() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/"
						+ "tier6/SakuraBlossomBlade.java");
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}
}
