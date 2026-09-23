package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DigestionTrapTest {

	@Test
	public void usesGreenDiamondSpriteAndNormalTrapRules() {
		DigestionTrap trap = new DigestionTrap();

		assertEquals(Trap.GREEN, trap.color);
		assertEquals(Trap.DIAMOND, trap.shape);
		assertFalse(trap.preservesTerrain());
		assertFalse(trap.triggersOnEntry());
		assertTrue(trap.canBeHidden);
		assertTrue(trap.canBeSearched);
	}

	@Test
	public void onlyLooseHeapItemsAreDigestible() {
		assertTrue(DigestionTrap.shouldDigest(Heap.Type.HEAP));
		assertFalse(DigestionTrap.shouldDigest(Heap.Type.FOR_SALE));
		assertFalse(DigestionTrap.shouldDigest(Heap.Type.CHEST));
		assertFalse(DigestionTrap.shouldDigest(Heap.Type.LOCKED_CHEST));
		assertFalse(DigestionTrap.shouldDigest(Heap.Type.CRYSTAL_CHEST));
		assertFalse(DigestionTrap.shouldDigest(Heap.Type.TOMB));
		assertFalse(DigestionTrap.shouldDigest(Heap.Type.SKELETON));
		assertFalse(DigestionTrap.shouldDigest(Heap.Type.REMAINS));
	}

	@Test
	public void neverRoutesTowerFloorOneToTheInaccessibleSurface() {
		assertEquals(2, DigestionTrap.destinationDepth(1, true));
		assertEquals(2, DigestionTrap.destinationDepth(1, false));
		assertEquals(3, DigestionTrap.destinationDepth(2, true));
		assertEquals(1, DigestionTrap.destinationDepth(2, false));
	}

	@Test
	public void usesSafePortingAndKeepsPotionAndSeedOutOfDestructiveDropFlow() throws IOException {
		String source = readSource();

		assertTrue(source.contains("Dungeon.queuePortedItem"));
		assertTrue(source.contains("heap.items.clear()"));
		assertFalse(source.contains("item.onThrow"));
		assertFalse(source.contains("Potion.shatter"));
		assertFalse(source.contains("level.plant"));
	}

	@Test
	public void targetFloorDeliveryUsesTheSafePortedItemChannel() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java")
				.resolve("com/shatteredpixel/shatteredpixeldungeon/scenes/GameScene.java");
		String gameScene = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);

		assertTrue(gameScene.contains("Dungeon.takePortedItems"));
		assertFalse(gameScene.contains("portedItem).shatter"));
	}

	private static String readSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java")
				.resolve("com/shatteredpixel/shatteredpixeldungeon/levels/traps/DigestionTrap.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
