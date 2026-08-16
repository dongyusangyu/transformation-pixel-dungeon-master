package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;

import org.junit.Test;

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
	public void specialRoomKeysAreReboundToTheActualTowerFloor() {
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

	private static class TestKey extends Key {
	}

	private static class TestItem extends Item {
	}
}
