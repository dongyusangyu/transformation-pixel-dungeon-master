package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRecords;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.watabou.utils.Bundle;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SurfaceShopkeeperTest {

	@Before
	public void resetTreasureHuntRecords() {
		TreasureHuntRecords.reset();
	}

	@Test
	public void explicitDepthPricingUsesRequestedShopChapter() {
		Item item = new Item() {
			@Override
			public int value() {
				return 40;
			}
		};

		assertEquals(item.value() * 25, Shopkeeper.sellPrice(item, 20));
		assertEquals(item.value() * 30, Shopkeeper.sellPrice(item, 25));
	}

	@Test
	public void queryParserAcceptsDebugScrollStyleQuantity() {
		SurfaceShopkeeper.Query query = SurfaceShopkeeper.parseQuery("治疗药剂 x3");

		assertEquals("治疗药剂", query.itemName);
		assertEquals(3, query.quantity);
		assertEquals(1, SurfaceShopkeeper.parseQuery("治疗药剂").quantity);
	}

	@Test
	public void queryCatalogRejectsUpgradeAndStrengthFamilies() {
		assertTrue(SurfaceShopkeeper.canQueryBuyClass(PotionOfHealing.class));
		assertTrue(SurfaceShopkeeper.canQueryBuyClass(ScrollOfExtraction.class));
		assertTrue(SurfaceShopkeeper.canQueryBuyClass(SmallRation.class));
		assertFalse(SurfaceShopkeeper.canQueryBuyClass(Pasty.class));

		assertFalse(SurfaceShopkeeper.canQueryBuyClass(ScrollOfUpgrade.class));
		assertFalse(SurfaceShopkeeper.canQueryBuyClass(ScrollOfEnchantment.class));
		assertFalse(SurfaceShopkeeper.canQueryBuyClass(StoneOfEnchantment.class));
		assertFalse(SurfaceShopkeeper.canQueryBuyClass(PotionOfStrength.class));
		assertFalse(SurfaceShopkeeper.canQueryBuyClass(PotionOfMastery.class));
		assertFalse(SurfaceShopkeeper.canQueryBuyClass(ElixirOfMight.class));
		assertFalse(SurfaceShopkeeper.canQueryBuyClass(Rotberry.Seed.class));
		assertFalse(SurfaceShopkeeper.canQueryBuy(new Item()));
	}

	@Test
	public void queryPurchaseLimitIsPerItemAndSurvivesSave() {
		SurfaceShopkeeper original = new SurfaceShopkeeper();
		original.recordQueryPurchase(PotionOfHealing.class, 3);

		assertTrue(original.canPurchase(PotionOfHealing.class, 2));
		assertFalse(original.canPurchase(PotionOfHealing.class, 3));

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		SurfaceShopkeeper restored = new SurfaceShopkeeper();
		restored.restoreFromBundle(bundle);

		assertEquals(3, restored.purchasedCount(PotionOfHealing.class));
		assertTrue(restored.canPurchase(PotionOfHealing.class, 2));
		assertFalse(restored.canPurchase(PotionOfHealing.class, 3));
	}

	@Test
	public void extractionRaidSettlementsNeverRefreshQueryPurchaseLimits() {
		SurfaceShopkeeper merchant = new SurfaceShopkeeper();
		merchant.recordQueryPurchase(PotionOfHealing.class, 5);
		merchant.recordQueryPurchase(SmallRation.class, 3);

		for (int settlement = 0; settlement < 12; settlement++) {
			TreasureHuntRecords.recordExtractionRaidSettlement();
		}

		assertEquals(5, merchant.purchasedCount(PotionOfHealing.class));
		assertEquals(3, merchant.purchasedCount(SmallRation.class));
		assertFalse(merchant.canPurchase(PotionOfHealing.class, 1));
		assertTrue(merchant.canPurchase(SmallRation.class, 2));
		assertFalse(merchant.canPurchase(SmallRation.class, 3));
	}

	@Test
	public void legacyPurchaseCycleFieldCannotRefreshRestoredLimits() {
		Bundle legacyBundle = new Bundle();
		legacyBundle.put("query_classes", new String[]{ScrollOfExtraction.class.getName()});
		legacyBundle.put("query_counts", new int[]{2});
		legacyBundle.put("query_cycle", 0);
		for (int settlement = 0; settlement < 10; settlement++) {
			TreasureHuntRecords.recordExtractionRaidSettlement();
		}

		SurfaceShopkeeper restored = new SurfaceShopkeeper();
		restored.restoreFromBundle(legacyBundle);

		assertEquals(2, restored.purchasedCount(ScrollOfExtraction.class));
		assertTrue(restored.canPurchase(ScrollOfExtraction.class, 3));
		assertFalse(restored.canPurchase(ScrollOfExtraction.class, 4));
	}
}
