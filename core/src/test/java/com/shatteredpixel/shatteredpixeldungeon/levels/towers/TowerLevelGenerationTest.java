package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
		assertFalse(TowerGenerationRules.isForbiddenNaturalItemClass(PotionOfHealing.class));
		assertEquals(ScrollOfMetamorphosis.class,
				TowerGenerationRules.GUARANTEED_FLOOR_ITEM);
		assertEquals(ScrollOfExtraction.class,
				TowerGenerationRules.GUARANTEED_SHOP_ITEM);
	}
}
