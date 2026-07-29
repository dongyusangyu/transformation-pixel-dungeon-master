/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.custom.treasurehunt.TreasureHuntRecords;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.BlizzardBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.CausticBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.InfernalBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfIcyTouch;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidRun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CurrencyIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SurfaceShopkeeper extends Shopkeeper {

	public static final int QUERY_LIMIT = 5;
	public static final int QUERY_PRICE_DEPTH = 25;

	private static final Pattern QUANTITY_SUFFIX =
			Pattern.compile("^(.*?)\\s+[xX](\\d+)\\s*$");

	private static LinkedHashSet<Class<? extends Item>> queryItems() {
		LinkedHashSet<Class<? extends Item>> result = new LinkedHashSet<>();
		addGeneratorCategory(result, Generator.Category.POTION);
		addGeneratorCategory(result, Generator.Category.SCROLL);
		addGeneratorCategory(result, Generator.Category.SEED);
		addGeneratorCategory(result, Generator.Category.STONE);
		result.addAll(ExoticPotion.exoToReg.keySet());
		result.addAll(ExoticScroll.exoToReg.keySet());

		addClasses(result,
				UnstableBrew.class, InfernalBrew.class, BlizzardBrew.class,
				ShockingBrew.class, CausticBrew.class, AquaBrew.class,
				ElixirOfHoneyedHealing.class, ElixirOfAquaticRejuvenation.class,
				ElixirOfArcaneArmor.class, ElixirOfDragonsBlood.class,
				ElixirOfIcyTouch.class, ElixirOfToxicEssence.class,
				ElixirOfMight.class, ElixirOfFeatherFall.class,
				ScrollOfExtraction.class, SmallRation.class
		);
		return result;
	}

	private final HashMap<String, Integer> queryPurchases = new HashMap<>();
	private int purchaseLimitCycle = TreasureHuntRecords.purchaseLimitCycle();

	@SuppressWarnings("unchecked")
	private static void addGeneratorCategory(LinkedHashSet<Class<? extends Item>> result,
											 Generator.Category category) {
		for (Class<?> itemClass : category.classes) {
			result.add((Class<? extends Item>) itemClass);
		}
	}

	@SafeVarargs
	private static void addClasses(LinkedHashSet<Class<? extends Item>> result,
								   Class<? extends Item>... classes) {
		for (Class<? extends Item> itemClass : classes) {
			result.add(itemClass);
		}
	}

	public static class Query {
		public final String itemName;
		public final int quantity;

		Query(String itemName, int quantity) {
			this.itemName = itemName;
			this.quantity = quantity;
		}
	}

	public static Query parseQuery(String text) {
		String trimmed = text == null ? "" : text.trim();
		Matcher matcher = QUANTITY_SUFFIX.matcher(trimmed);
		if (!matcher.matches()) {
			return new Query(trimmed, 1);
		}
		try {
			return new Query(matcher.group(1).trim(), Integer.parseInt(matcher.group(2)));
		} catch (NumberFormatException ignored) {
			return new Query(matcher.group(1).trim(), -1);
		}
	}

	public static boolean canQueryBuy(Item item) {
		if (item == null || item.value() <= 0) {
			return false;
		}
		if (!(item instanceof Potion
				|| item instanceof Scroll
				|| item instanceof Runestone
				|| item instanceof Plant.Seed
				|| item instanceof SmallRation)) {
			return false;
		}
		return !(item instanceof PotionOfStrength
				|| item instanceof PotionOfMastery
				|| item instanceof ElixirOfMight
				|| item instanceof Rotberry.Seed
				|| item instanceof ScrollOfUpgrade
				|| item instanceof ScrollOfEnchantment
				|| item instanceof StoneOfEnchantment);
	}

	public static boolean canQueryBuyClass(Class<? extends Item> itemClass) {
		boolean allowedType = Potion.class.isAssignableFrom(itemClass)
				|| Scroll.class.isAssignableFrom(itemClass)
				|| Runestone.class.isAssignableFrom(itemClass)
				|| Plant.Seed.class.isAssignableFrom(itemClass)
				|| itemClass == SmallRation.class;
		return allowedType && !(itemClass == PotionOfStrength.class
				|| itemClass == PotionOfMastery.class
				|| itemClass == ElixirOfMight.class
				|| itemClass == Rotberry.Seed.class
				|| itemClass == ScrollOfUpgrade.class
				|| itemClass == ScrollOfEnchantment.class
				|| itemClass == StoneOfEnchantment.class);
	}

	public static Item findQueryItem(String requestedName) {
		String normalized = normalize(requestedName);
		if (normalized.isEmpty()) {
			return null;
		}
		for (Class<? extends Item> itemClass : queryItems()) {
			Item item = Reflection.newInstance(itemClass);
			if (item != null && canQueryBuyClass(itemClass) && canQueryBuy(item)
					&& normalize(Messages.get(item, "name")).equals(normalized)) {
				return item;
			}
		}
		return null;
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	public int purchasedCount(Class<? extends Item> itemClass) {
		syncPurchaseLimitCycle();
		Integer count = queryPurchases.get(itemClass.getName());
		return count == null ? 0 : count;
	}

	public boolean canPurchase(Class<? extends Item> itemClass, int quantity) {
		return quantity > 0 && purchasedCount(itemClass) + quantity <= QUERY_LIMIT;
	}

	public void recordQueryPurchase(Class<? extends Item> itemClass, int quantity) {
		syncPurchaseLimitCycle();
		queryPurchases.put(itemClass.getName(),
				Math.min(QUERY_LIMIT, purchasedCount(itemClass) + quantity));
	}

	public void syncPurchaseLimitCycle() {
		int currentCycle = TreasureHuntRecords.purchaseLimitCycle();
		if (currentCycle != purchaseLimitCycle) {
			queryPurchases.clear();
			purchaseLimitCycle = currentCycle;
		}
	}

	public static void syncCurrentMerchantPurchaseLimits() {
		if (Dungeon.level == null || Dungeon.level.mobs == null) {
			return;
		}
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs) {
			if (mob instanceof SurfaceShopkeeper) {
				((SurfaceShopkeeper) mob).syncPurchaseLimitCycle();
			}
		}
	}

	@Override
	public boolean interact(Char c) {
		if (c != Dungeon.hero) {
			return true;
		}
		syncPurchaseLimitCycle();
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				showMerchantMenu();
			}
		});
		return true;
	}

	private void showMerchantMenu() {
		String[] options = new String[4 + buybackItems.size()];
		int i = 0;
		options[i++] = Messages.get(this, "sell");
		options[i++] = Messages.get(this, "query");
		options[i++] = Messages.get(this, "raid");
		options[i++] = Messages.get(this, "talk");
		for (Item item : buybackItems) {
			options[i++] = Messages.get(Heap.class, "for_sale",
					item.value(), Messages.titleCase(item.title()));
		}

		GameScene.show(new WndOptions(sprite(), Messages.titleCase(name()), description(), options) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index == 0) {
					sell();
				} else if (index == 1) {
					showQueryInput();
				} else if (index == 2) {
					showRaidOffer();
				} else if (index == 3) {
					GameScene.show(new WndTitledMessage(sprite(),
							Messages.titleCase(name()), Messages.get(SurfaceShopkeeper.this, "talk_text")));
				} else if (index > 3) {
					buyBack(index - 4);
				}
			}

			@Override
			protected boolean enabled(int index) {
				return index <= 3 || Dungeon.gold >= buybackItems.get(index - 4).value();
			}

			@Override
			protected boolean hasIcon(int index) {
				return index > 3;
			}

			@Override
			protected Image getIcon(int index) {
				return index > 3 ? new ItemSprite(buybackItems.get(index - 4)) : null;
			}

			@Override
			public void hide() {
				super.hide();
				CurrencyIndicator.showGold = false;
			}
		});
	}

	private void showRaidOffer() {
		GameScene.show(new WndOptions(
				sprite(),
				Messages.get(this, "raid_title"),
				Messages.get(this, "raid_body",
						ExtractionRaidRun.ENTRY_FEE,
						ExtractionRaidRun.EXTRA_CRYSTAL_KEY_BONUS),
				Messages.get(this, "raid_enter"),
				Messages.get(this, "raid_cancel")) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index != 0) return;
				if (!ExtractionRaidRun.canPayEntry(Dungeon.gold)) {
					GLog.n(Messages.get(SurfaceShopkeeper.this, "raid_no_gold",
							ExtractionRaidRun.ENTRY_FEE));
				} else if (!ExtractionRaidRun.beginFromSurfaceShop()) {
					GLog.n(Messages.get(SurfaceShopkeeper.this, "raid_unavailable"));
				}
			}

			@Override
			protected boolean enabled(int index) {
				return index != 0 || ExtractionRaidRun.canPayEntry(Dungeon.gold);
			}
		});
	}

	private void buyBack(int buybackIndex) {
		GLog.i(Messages.get(this, "buyback"));
		Item returned = buybackItems.remove(buybackIndex);
		Dungeon.gold -= returned.value();
		Statistics.goldCollected -= returned.value();
		if (returned instanceof MissileWeapon && returned.isUpgradable()) {
			Buff.affect(Dungeon.hero, MissileWeapon.UpgradedSetTracker.class)
					.levelThresholds.put(((MissileWeapon) returned).setID, returned.level());
		}
		if (!returned.doPickUp(Dungeon.hero)) {
			Dungeon.level.drop(returned, Dungeon.hero.pos);
		}
	}

	private void showQueryInput() {
		GameScene.show(new WndTextInput(
				Messages.get(this, "query_title"),
				Messages.get(this, "query_body"),
				"", 80, false,
				Messages.get(this, "query_confirm"),
				Messages.get(this, "query_cancel")) {
			@Override
			public void onSelect(boolean positive, String text) {
				if (positive) {
					handleQuery(text);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void handleQuery(String text) {
		Query query = parseQuery(text);
		if (query.quantity <= 0) {
			GLog.n(Messages.get(this, "invalid_quantity"));
			return;
		}
		Item item = findQueryItem(query.itemName);
		if (item == null) {
			GLog.n(Messages.get(this, "not_found"));
			return;
		}
		Class<? extends Item> itemClass = (Class<? extends Item>) item.getClass();
		if (!canPurchase(itemClass, query.quantity)) {
			GLog.n(Messages.get(this, "limit_reached", QUERY_LIMIT,
					purchasedCount(itemClass)));
			return;
		}

		int unitPrice = sellPrice(item, QUERY_PRICE_DEPTH);
		int totalPrice = unitPrice * query.quantity;
		int remaining = QUERY_LIMIT - purchasedCount(itemClass);
		GameScene.show(new WndOptions(new ItemSprite(item),
				Messages.titleCase(Messages.get(item, "name")),
				Messages.get(this, "query_offer", query.quantity, unitPrice, totalPrice, remaining),
				Messages.get(this, "buy"),
				Messages.get(this, "query_cancel")) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index == 0) {
					completeQueryPurchase(itemClass, query.quantity, totalPrice);
				}
			}

			@Override
			protected boolean enabled(int index) {
				return index != 0 || Dungeon.gold >= totalPrice;
			}
		});
	}

	private void completeQueryPurchase(Class<? extends Item> itemClass, int quantity, int totalPrice) {
		if (!canPurchase(itemClass, quantity)) {
			GLog.n(Messages.get(this, "limit_reached", QUERY_LIMIT, purchasedCount(itemClass)));
			return;
		}
		if (Dungeon.gold < totalPrice) {
			GLog.n(Messages.get(this, "not_enough_gold"));
			return;
		}

		Item bought = Reflection.newInstance(itemClass);
		if (bought == null || !canQueryBuy(bought)) {
			GLog.n(Messages.get(this, "not_found"));
			return;
		}
		bought.identify(false).quantity(quantity);
		Dungeon.gold -= totalPrice;
		Catalog.countUses(Gold.class, totalPrice);
		recordQueryPurchase(itemClass, quantity);
		if (!bought.doPickUp(Dungeon.hero)) {
			Dungeon.level.drop(bought, Dungeon.hero.pos).sprite.drop();
		}
	}

	private static final String QUERY_CLASSES = "query_classes";
	private static final String QUERY_COUNTS = "query_counts";
	private static final String QUERY_CYCLE = "query_cycle";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		syncPurchaseLimitCycle();
		String[] classes = new String[queryPurchases.size()];
		int[] counts = new int[queryPurchases.size()];
		int i = 0;
		for (Map.Entry<String, Integer> entry : queryPurchases.entrySet()) {
			classes[i] = entry.getKey();
			counts[i] = entry.getValue();
			i++;
		}
		bundle.put(QUERY_CLASSES, classes);
		bundle.put(QUERY_COUNTS, counts);
		bundle.put(QUERY_CYCLE, purchaseLimitCycle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		queryPurchases.clear();
		purchaseLimitCycle = bundle.contains(QUERY_CYCLE)
				? Math.max(0, bundle.getInt(QUERY_CYCLE))
				: TreasureHuntRecords.purchaseLimitCycle();
		String[] classes = bundle.getStringArray(QUERY_CLASSES);
		int[] counts = bundle.getIntArray(QUERY_COUNTS);
		for (int i = 0; i < Math.min(classes.length, counts.length); i++) {
			if (counts[i] > 0) {
				queryPurchases.put(classes[i], Math.min(QUERY_LIMIT, counts[i]));
			}
		}
		syncPurchaseLimitCycle();
	}
}
