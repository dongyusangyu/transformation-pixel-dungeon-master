package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GreatGreatGreatswordTest {

	@Test
	public void tierSixMeleeFormulaProvidesRequiredStatsAndReach() {
		TierSixFormulaProbe weapon = new TierSixFormulaProbe();

		assertEquals(6, weapon.tier);
		assertEquals(6, weapon.min(0));
		assertEquals(35, weapon.max(0));
		assertEquals(9, weapon.min(3));
		assertEquals(56, weapon.max(3));
		assertEquals(20, weapon.STRReq(0));
		assertEquals(3, weapon.RCH);
		assertEquals(3, weapon.reachFactor(null));
	}

	@Test
	public void classInheritsGreatswordCleaveWithoutOverridingMethods() {
		assertTrue(Greatsword.class.isAssignableFrom(GreatGreatGreatsword.class));
		assertEquals(Sword.class, MeleeWeapon.abilityType(GreatGreatGreatsword.class));
		assertEquals(0, GreatGreatGreatsword.class.getDeclaredMethods().length);
	}

	@Test
	public void initializerSetsTierReachAndExtendedSprite() throws IOException {
		String source = new String(Files.readAllBytes(sourcePath()), StandardCharsets.UTF_8);

		assertTrue(source.contains("extends Greatsword"));
		assertTrue(source.contains("image = EXItemSpriteSheet.GREAT_GREAT_GREATSWORD;"));
		assertTrue(source.contains("tier = 6;"));
		assertTrue(source.contains("RCH = 3;"));
	}

	private static Path sourcePath() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		return coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/"
						+ "tier6/GreatGreatGreatsword.java");
	}

	public static class TierSixFormulaProbe extends MeleeWeapon {
		{
			tier = 6;
			RCH = 3;
		}
	}
}
