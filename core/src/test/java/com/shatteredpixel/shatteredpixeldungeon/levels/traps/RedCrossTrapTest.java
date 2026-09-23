package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RedCrossTrapTest {

	@Test
	public void usesRedCrossSpriteAndNormalTrapRules() {
		RedCrossTrap trap = new RedCrossTrap();

		assertEquals(Trap.RED, trap.color);
		assertEquals(Trap.CROSSHAIR, trap.shape);
		assertFalse(trap.preservesTerrain());
		assertFalse(trap.triggersOnEntry());
		assertTrue(trap.canBeHidden);
		assertTrue(trap.canBeSearched);
	}

	@Test
	public void scalesHealingWithTowerFloorAndCapsItAtTwoHundred() {
		assertEquals(2, RedCrossTrap.healingAmountForTowerFloor(1));
		assertEquals(40, RedCrossTrap.healingAmountForTowerFloor(20));
		assertEquals(100, RedCrossTrap.healingAmountForTowerFloor(50));
		assertEquals(200, RedCrossTrap.healingAmountForTowerFloor(100));
		assertEquals(200, RedCrossTrap.healingAmountForTowerFloor(130));
	}

	@Test
	public void documentsHostileMonsterOnlyHealing() throws IOException {
		String source = readSource();

		assertTrue(source.contains("mob.alignment != Char.Alignment.ENEMY"));
		assertTrue(source.contains("mob.isAlive()"));
		assertTrue(source.contains("mob.heal(healing"));
	}

	private static String readSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java")
				.resolve("com/shatteredpixel/shatteredpixeldungeon/levels/traps/RedCrossTrap.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
