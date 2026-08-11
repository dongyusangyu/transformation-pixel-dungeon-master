package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GungnirTest {

	@Test
	public void tierSixTridentModelAndBloodCostAreFixed() {
		Gungnir weapon = new Gungnir();

		assertEquals(6, weapon.tier);
		assertEquals(EXItemSpriteSheet.GUNGNIR, weapon.image);
		assertEquals(1, weapon.defaultQuantity());
		assertEquals(12, weapon.min(0));
		assertEquals(30, weapon.max(0));
		assertEquals(16, weapon.min(2));
		assertEquals(42, weapon.max(2));
		assertFalse(Gungnir.canThrowWithCurrentHP(1));
		assertTrue(Gungnir.canThrowWithCurrentHP(2));
		assertEquals(1, Gungnir.lifeCostForCurrentHP(2));
		assertEquals(1, Gungnir.lifeCostForCurrentHP(19));
		assertEquals(2, Gungnir.lifeCostForCurrentHP(20));
	}

	@Test
	public void bleedingAndKillHealingRequirePreexistingBleeding() {
		assertEquals(1, Gungnir.bleedForDamage(1));
		assertEquals(5, Gungnir.bleedForDamage(10));
		assertEquals(1, Gungnir.healingForMaxHP(1));
		assertEquals(10, Gungnir.healingForMaxHP(100));
		assertFalse(Gungnir.qualifiesForBleedingKillHealing(false, true));
		assertFalse(Gungnir.qualifiesForBleedingKillHealing(true, false));
		assertTrue(Gungnir.qualifiesForBleedingKillHealing(true, true));
	}

	@Test
	public void generatorAndSourceKeepTheSpearReturningAndAlwaysAccurate() throws Exception {
		assertTrue(Arrays.asList(Generator.Category.MIS_T6.classes).contains(Gungnir.class));
		assertEquals(Generator.Category.MIS_T6.classes.length,
				Generator.Category.MIS_T6.defaultProbs.length);

		String source = readMainSource("items/weapon/missiles/Gungnir.java");
		assertTrue(source.contains("return Char.INFINITE_ACCURACY;"));
		assertTrue(source.contains("collect(hero.belongings.backpack)"));
		assertTrue(source.contains("Buff.affect(defender, Bleeding.class)"));
		assertTrue(source.contains("wasBleedingBeforeHit"));
	}

	private static String readMainSource(String relativePath) throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon").resolve(relativePath);
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}
}
