package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import sun.misc.Unsafe;

import static org.junit.Assert.*;

public class TowerPotionRulesTest {

	private Hero previousHero;
	private QuickSlot previousQuickslot;
	private int previousBranch;
	private int previousDepth;
	private Files previousFiles;

	@Before public void setUp() throws Exception {
		previousFiles = Gdx.files;
		GdxNativesLoader.load();
		Gdx.files = assetFiles();
		previousHero = Dungeon.hero;
		previousQuickslot = Dungeon.quickslot;
		previousBranch = Dungeon.branch;
		previousDepth = Dungeon.depth;
		Dungeon.hero = allocate(Hero.class);
		Field buffs = Char.class.getDeclaredField("buffs");
		buffs.setAccessible(true);
		buffs.set(Dungeon.hero, new LinkedHashSet<>());
		Dungeon.hero.belongings = allocate(Belongings.class);
		Dungeon.hero.belongings.backpack = allocate(Belongings.Backpack.class);
		Dungeon.hero.belongings.backpack.items = new ArrayList<>();
		Dungeon.hero.belongings.backpack.owner = Dungeon.hero;
		Dungeon.hero.HP = 0;
		Dungeon.quickslot = new QuickSlot();
		Dungeon.branch = TowerLevel.BRANCH;
		Dungeon.depth = 1;
	}

	@After public void tearDown() {
		Gdx.files = previousFiles;
		Dungeon.hero = previousHero;
		Dungeon.quickslot = previousQuickslot;
		Dungeon.branch = previousBranch;
		Dungeon.depth = previousDepth;
	}

	@Test public void rejectsExternalStackBeyondThirteenWithoutMutatingIt() throws Exception {
		Bag backpack = Dungeon.hero.belongings.backpack;
		backpack.items.add(potion(12));
		PotionOfHealing incoming = potion(2);
		assertFalse(incoming.collect(backpack));
		assertEquals(2, incoming.quantity());
		assertEquals(12, TowerPotionRules.countHealing(backpack));
		assertFalse(backpack.contains(incoming));
	}

	@Test public void aTwoBottleStackAllowsOnlyTheLastFreeBottle() throws Exception {
		Bag backpack = Dungeon.hero.belongings.backpack;
		backpack.items.add(potion(12));
		assertEquals(1, TowerPotionRules.pickupAmount(potion(2), backpack));
		backpack.items.add(potion(1));
		assertEquals(0, TowerPotionRules.pickupAmount(potion(2), backpack));
	}

	@Test public void countsNestedStacksAndAllowsTheThirteenthBottle() throws Exception {
		Bag backpack = Dungeon.hero.belongings.backpack;
		PotionBandolier bandolier = bandolier();
		bandolier.owner = Dungeon.hero;
		bandolier.items.add(potion(12));
		backpack.items.add(bandolier);
		assertEquals(1, TowerPotionRules.remainingCapacity(backpack));
		PotionOfHealing incoming = potion(1);
		assertTrue(incoming.collect(backpack));
		assertEquals(13, TowerPotionRules.countHealing(backpack));
		assertEquals(0, TowerPotionRules.remainingCapacity(backpack));
	}

	@Test public void existingBottleCanMoveBetweenNestedBagsAtLimit() throws Exception {
		Bag backpack = Dungeon.hero.belongings.backpack;
		PotionBandolier bandolier = bandolier();
		bandolier.owner = Dungeon.hero;
		PotionOfHealing existing = potion(1);
		backpack.items.add(existing);
		backpack.items.add(potion(12));
		backpack.items.add(bandolier);
		backpack.items.remove(existing);
		assertTrue(existing.collect(bandolier));
		assertEquals(13, TowerPotionRules.countHealing(backpack));
	}

	@Test public void nonTowerFloorDoesNotApplyCap() throws Exception {
		Dungeon.branch = 0;
		Bag backpack = Dungeon.hero.belongings.backpack;
		backpack.items.add(potion(13));
		PotionOfHealing incoming = potion(1);
		assertTrue(incoming.collect(backpack));
		assertEquals(14, TowerPotionRules.countHealing(backpack));
	}

	@Test public void bundleRoundTripPreservesLegacyOverlimitForMigration() throws Exception {
		Bag backpack = Dungeon.hero.belongings.backpack;
		backpack.items.add(potion(14));
		Bundle bundle = new Bundle();
		backpack.storeInBundle(bundle);
		Belongings.Backpack restored = allocate(Belongings.Backpack.class);
		restored.items = new ArrayList<>();
		restored.owner = Dungeon.hero;
		restored.restoreFromBundle(bundle);
		assertEquals(14, TowerPotionRules.countHealing(restored));
		assertEquals(0, TowerPotionRules.remainingCapacity(restored));
	}

	@Test public void migrationSplitsOnlyExcessFromNestedStacks() throws Exception {
		Bag backpack = Dungeon.hero.belongings.backpack;
		PotionBandolier bandolier = bandolier();
		bandolier.owner = Dungeon.hero;
		bandolier.items.add(potion(8));
		backpack.items.add(bandolier);
		backpack.items.add(potion(7));
		ArrayList<Item> excess = new ArrayList<>();
		assertEquals(2, TowerPotionRules.takeExcess(backpack, 2, excess));
		assertEquals(13, TowerPotionRules.countHealing(backpack));
		assertEquals(1, excess.size());
		assertEquals(2, excess.get(0).quantity());
	}

	@Test public void drunkenPotencyHalvesAmountsAndDurationsButNotSoberValues() {
		assertEquals(500, PotionPotency.amount(true, 1000));
		assertEquals(15, PotionPotency.amount(true, 30));
		assertEquals(2.5f, PotionPotency.duration(true, 5f), 0.001f);
		assertEquals(1000, PotionPotency.amount(false, 1000));
		assertEquals(5f, PotionPotency.duration(false, 5f), 0.001f);
	}

	private static PotionOfHealing potion(int quantity) throws Exception {
		PotionOfHealing potion = allocate(PotionOfHealing.class);
		potion.stackable = true;
		potion.quantity(quantity);
		return potion;
	}

	private static PotionBandolier bandolier() throws Exception {
		PotionBandolier bag = allocate(PotionBandolier.class);
		bag.items = new ArrayList<>();
		return bag;
	}

	private static Files assetFiles() {
		return (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
				new Class<?>[]{Files.class}, (proxy, method, args) -> {
					if (method.getReturnType() == FileHandle.class) {
						File assets = new File("core/src/main/assets");
						if (!assets.isDirectory()) assets = new File("src/main/assets");
						return new FileHandle(new File(assets, (String) args[0]));
					}
					return null;
				});
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocate(Class<T> type) throws Exception {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (T) ((Unsafe) field.get(null)).allocateInstance(type);
	}
}
