package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TowerLevelGenerationTest {

	@Test
	public void shopsAppearEveryFiveFloorsStartingAtTowerOne() {
		assertTrue(TowerGenerationRules.isShopFloor(1));
		assertTrue(TowerGenerationRules.isShopFloor(6));
		assertTrue(TowerGenerationRules.isShopFloor(11));
		assertFalse(TowerGenerationRules.isShopFloor(2));
		assertFalse(TowerGenerationRules.isShopFloor(5));
		assertFalse(TowerGenerationRules.isShopFloor(10));
	}

	@Test
	public void towerShopPricesContinueAfterDepthThirty() {
		assertEquals(35, TowerGenerationRules.shopPriceDepth(1));
		assertEquals(40, TowerGenerationRules.shopPriceDepth(6));
		assertEquals(45, TowerGenerationRules.shopPriceDepth(11));
		assertEquals(50, TowerGenerationRules.shopPriceDepth(16));
	}

	@Test
	public void towerGuaranteedItemsExcludePermanentProgressionItems() {
		assertTrue(TowerGenerationRules.isForbiddenNaturalItemClass(PotionOfStrength.class));
		assertTrue(TowerGenerationRules.isForbiddenNaturalItemClass(ScrollOfUpgrade.class));
		assertTrue(TowerGenerationRules.isForbiddenNaturalItemClass(ScrollOfMetamorphosis.class));
		assertFalse(TowerGenerationRules.isForbiddenNaturalItemClass(PotionOfHealing.class));
		assertEquals(ScrollOfExtraction.class,
				TowerGenerationRules.GUARANTEED_SHOP_ITEM);
		assertEquals(ScrollOfMetamorphosis.class,
				TowerGenerationRules.METAMORPHOSIS_ITEM);
	}

	@Test
	public void nonBossTowerFloorsGenerateNaturalFood() {
		assertTrue(TowerGenerationRules.shouldGenerateNaturalFood(false));
		assertFalse(TowerGenerationRules.shouldGenerateNaturalFood(true));
	}

	@Test
	public void towerAtmospheresMatchNormalNonBossFloorBoundaries() {
		assertFalse(TowerGenerationRules.shouldGenerateLevelFeeling(1, false));
		assertTrue(TowerGenerationRules.shouldGenerateLevelFeeling(2, false));
		assertFalse(TowerGenerationRules.shouldGenerateLevelFeeling(5, true));
		assertTrue(TowerGenerationRules.shouldGenerateLevelFeeling(101, false));
	}

	@Test
	public void secretsAtmosphereAddsOneTowerSecretRoom() {
		assertEquals(1, TowerGenerationRules.secretRoomCount(Level.Feeling.SECRETS));
		assertEquals(0, TowerGenerationRules.secretRoomCount(Level.Feeling.WATER));
	}

	@Test
	public void towerUsesTheSharedFeelingGenerationHook() throws IOException {
		String levelSource = readCoreSource("com/shatteredpixel/shatteredpixeldungeon/levels/Level.java");
		String towerSource = readCoreSource("com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerLevel.java");

		assertTrue(levelSource.contains("if (shouldGenerateLevelFeeling())"));
		assertTrue(towerSource.contains("shouldGenerateLevelFeeling()"));
	}

	@Test
	public void specialRoomKeysAreAssignedTheActualTowerFloorBeforeSpawning() {
		TestKey key = new TestKey();
		key.depth = 21;

		assertSame(key, TowerGenerationRules.prepareFloorSpawn(key, 7));
		assertEquals(7, key.depth);
	}

	@Test
	public void preparedTowerSpawnsPassThroughAllowedItemsAndNull() {
		TestItem item = new TestItem();
		assertSame(item, TowerGenerationRules.prepareFloorSpawn(item, 7));
		assertNull(TowerGenerationRules.prepareFloorSpawn(null, 7));
	}

	@Test
	public void driedRoseProgressContinuesPastTowerContentDepthCap() {
		assertEquals(26, TowerGenerationRules.driedRosePetalProgressDepth(1));
		assertEquals(35, TowerGenerationRules.driedRosePetalProgressDepth(10));
		assertEquals(46, TowerGenerationRules.driedRosePetalProgressDepth(21));
	}

	@Test
	public void unfinishedRoseCanGenerateMissedPetalsInTowerOnly() {
		assertTrue(TowerGenerationRules.driedRosePetalGenerationAllowed(11, false));
		assertFalse(TowerGenerationRules.driedRosePetalGenerationAllowed(11, true));
	}

	@Test
	public void rosePetalsOnlyGenerateOnMainAndTowerBranches() {
		assertTrue(TowerGenerationRules.driedRosePetalGenerationEnabled(0));
		assertFalse(TowerGenerationRules.driedRosePetalGenerationEnabled(1));
		assertTrue(TowerGenerationRules.driedRosePetalGenerationEnabled(TowerLevel.BRANCH));
	}

	private static class TestKey extends Key {
	}

	private static class TestItem extends Item {
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
