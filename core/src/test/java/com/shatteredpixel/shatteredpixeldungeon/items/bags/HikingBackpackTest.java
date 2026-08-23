package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HikingBackpackTest {

	private Hero previousHero;
	private QuickSlot previousQuickslot;
	private Application previousApplication;

	@Before
	public void setUp() {
		previousHero = Dungeon.hero;
		previousQuickslot = Dungeon.quickslot;
		previousApplication = Gdx.app;
		Gdx.app = mock(Application.class);
		Dungeon.quickslot = new QuickSlot();
	}

	@After
	public void tearDown() {
		Dungeon.quickslot = previousQuickslot;
		Dungeon.hero = previousHero;
		Gdx.app = previousApplication;
	}

	@Test
	public void hasExpectedCapacityValueSpriteAndFallbackRole() throws Exception {
		HikingBackpack bag = hiking();
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/bags/HikingBackpack.java");

		assertEquals(19, bag.capacity());
		assertEquals(40, bag.value());
		assertTrue(bag.isFallbackStorage());
		assertTrue(source.contains("image = ItemSpriteSheet.BACKPACK;"));
		assertTrue(source.contains("autoGrabOnCollect()"));
	}

	@Test
	public void acceptsAnyOrdinaryItemButRejectsBags() {
		HikingBackpack bag = hiking();

		assertTrue(bag.canHold(new GeneralItem()));
		assertTrue(bag.canHold(new PreferredItem()));
		assertFalse(bag.canHold(new PreferredBag()));
		assertFalse(bag.canHold(hiking()));
	}

	@Test
	public void preferredBagWinsBeforeMainBackpackAndFallback() {
		Hero hero = heroWith(new PreferredBag(), hiking());
		PreferredItem item = new PreferredItem();

		assertTrue(item.collect(hero.belongings.backpack));

		assertTrue(hero.belongings.getItem(PreferredBag.class).contains(item));
		assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(item));
	}

	@Test
	public void mainBackpackWinsWhenPreferredBagIsFull() {
		Hero hero = heroWith(fullPreferredBag(), hiking());
		PreferredItem item = new PreferredItem();

		assertTrue(item.collect(hero.belongings.backpack));

		assertTrue(hero.belongings.backpack.items.contains(item));
		assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(item));
	}

	@Test
	public void fallbackReceivesItemOnlyAfterOtherStorageIsFull() {
		Hero hero = heroWith(fullPreferredBag(), hiking());
		fillMainBackpack(hero.belongings.backpack);
		PreferredItem item = new PreferredItem();

		assertTrue(item.collect(hero.belongings.backpack));

		assertTrue(hero.belongings.getItem(HikingBackpack.class).contains(item));
	}

	@Test
	public void collectionFailsWhenFallbackIsAlsoFull() {
		Hero hero = heroWith(fullPreferredBag(), hiking());
		fillMainBackpack(hero.belongings.backpack);
		fillBag(hero.belongings.getItem(HikingBackpack.class));

		assertFalse(new PreferredItem().collect(hero.belongings.backpack));
	}

	@Test
	public void freeingPreferredSlotPullsMatchingFallbackItemBack() {
		Hero hero = heroWith(fullPreferredBag(), hiking());
		fillMainBackpack(hero.belongings.backpack);
		PreferredItem overflow = putPreferredItemInFallback(hero);
		PreferredBag preferred = hero.belongings.getItem(PreferredBag.class);

		preferred.items.get(0).detachAll(preferred);

		assertTrue(preferred.contains(overflow));
		assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(overflow));
	}

	@Test
	public void freeingMainSlotPullsGeneralFallbackItemBack() {
		Hero hero = heroWith(hiking());
		fillMainBackpack(hero.belongings.backpack);
		GeneralItem overflow = new GeneralItem();
		hero.belongings.getItem(HikingBackpack.class).items.add(overflow);

		firstDirectOrdinaryItem(hero.belongings.backpack).detachAll(hero.belongings.backpack);

		assertTrue(hero.belongings.backpack.items.contains(overflow));
		assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(overflow));
	}

	@Test
	public void existingPreferredStackMergesBeforeFallbackStack() {
		Hero hero = heroWith(new PreferredBag(), hiking());
		PreferredItem preferredStack = new PreferredItem();
		preferredStack.quantity(2);
		hero.belongings.getItem(PreferredBag.class).items.add(preferredStack);
		PreferredItem fallbackStack = new PreferredItem();
		fallbackStack.quantity(3);
		hero.belongings.getItem(HikingBackpack.class).items.add(fallbackStack);

		hero.belongings.backpack.rebalanceFallbackStorage();

		assertEquals(5, preferredStack.quantity());
		assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(fallbackStack));
	}

	@Test
	public void rebalancePreservesQuickslotBinding() {
		Hero hero = heroWith(new PreferredBag(), hiking());
		PreferredItem overflow = putPreferredItemInFallback(hero);
		Dungeon.quickslot.setSlot(0, overflow);

		hero.belongings.backpack.rebalanceFallbackStorage();

		assertTrue(hero.belongings.getItem(PreferredBag.class).contains(overflow));
		assertSame(overflow, Dungeon.quickslot.getItem(0));
	}

	@Test
	public void repeatedRebalanceDoesNotDuplicateItems() {
		Hero hero = heroWith(new PreferredBag(), hiking());
		PreferredItem overflow = putPreferredItemInFallback(hero);

		hero.belongings.backpack.rebalanceFallbackStorage();
		hero.belongings.backpack.rebalanceFallbackStorage();

		assertEquals(1, countIdentity(hero.belongings.backpack, overflow));
	}

	@Test
	public void secondaryWeaponReservationSpillsMainBackpackOverflowToHikingBackpack() {
		Hero hero = heroWith(hiking());
		fillMainBackpack(hero.belongings.backpack);
		Item visibleItem = firstDirectOrdinaryItem(hero.belongings.backpack);
		Item overflow = lastDirectOrdinaryItem(hero.belongings.backpack);
		hero.belongings.secondWep = new TestKindOfWeapon();

		hero.belongings.backpack.rebalanceFallbackStorage();

		assertTrue(hero.belongings.backpack.items.size() <= hero.belongings.backpack.capacity());
		assertTrue(hero.belongings.backpack.contains(visibleItem));
		assertTrue(hero.belongings.getItem(HikingBackpack.class).contains(overflow));
	}

	@Test
	public void clearingSecondaryWeaponReservationPullsHikingItemBackIntoMainBackpack() {
		Hero hero = heroWith(hiking());
		hero.belongings.secondWep = new TestKindOfWeapon();
		fillMainBackpack(hero.belongings.backpack);
		GeneralItem fallbackItem = new GeneralItem();
		hero.belongings.getItem(HikingBackpack.class).items.add(fallbackItem);

		hero.belongings.secondWep = null;
		hero.belongings.backpack.rebalanceFallbackStorage();

		assertTrue(hero.belongings.backpack.items.contains(fallbackItem));
		assertFalse(hero.belongings.getItem(HikingBackpack.class).contains(fallbackItem));
	}

	@Test
	public void secondaryWeaponChangesInvokeBackpackCapacityReconciliation() throws Exception {
		String weaponSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/KindOfWeapon.java");
		String belongingsSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java");

		assertTrue(weaponSource.contains("hero.belongings.onSecondaryWeaponChanged();"));
		assertTrue(belongingsSource.contains("secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);"));
		assertTrue(belongingsSource.contains("onSecondaryWeaponChanged();"));
	}

	@Test
	public void secondaryWeaponReservationRequiresFallbackCapacityWhenMainIsFull() {
		Hero hero = heroWith(hiking());
		fillMainBackpack(hero.belongings.backpack);
		fillBag(hero.belongings.getItem(HikingBackpack.class));

		assertFalse(hero.belongings.backpack.canReserveSecondaryWeaponSlot(new TestKindOfWeapon()));
	}

	@Test
	public void fullMiscSwapDetachesIncomingItemRecursivelyInsteadOfRawMainBackpackRemoval() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/KindofMisc.java");

		assertTrue(source.contains("detachAll(hero.belongings.backpack)"));
		assertFalse(source.contains("backpack.items.remove(KindofMisc.this)"));
		assertFalse(source.contains("backpack.items.add(KindofMisc.this)"));
	}

	@Test
	public void collectingAContainerReconcilesStaleMissileMembers() {
		Hero hero = heroWith();
		MissileWeapon.UpgradedSetTracker tracker =
				Buff.affect(hero, MissileWeapon.UpgradedSetTracker.class);
		TestMissile stale = new TestMissile();
		stale.setID = 59L;
		stale.level(5);
		stale.upgradeScrollUses = 5;
		tracker.levelThresholds.put(59L, 0);
		tracker.upgradeScrollCredits.put(59L, 0);
		assertSame(tracker, hero.buff(MissileWeapon.UpgradedSetTracker.class));

		HikingBackpack dropped = hiking();
		dropped.quantity(1);
		dropped.items.add(stale);
		assertTrue(dropped.collect(hero.belongings.backpack));

		assertEquals(0, stale.trueLevel());
		assertEquals(0, stale.upgradeScrollUses);
	}

	@Test
	public void collectingAContainerDustsLowerLevelMissileMembersAfterAnotherFragmentWasUpgraded() {
		Hero hero = heroWith();
		MissileWeapon.UpgradedSetTracker tracker =
				Buff.affect(hero, MissileWeapon.UpgradedSetTracker.class);
		TestMissile stale = new TestMissile();
		stale.setID = 60L;
		stale.level(0);
		stale.upgradeScrollUses = 0;
		tracker.levelThresholds.put(60L, 1);
		tracker.upgradeScrollCredits.put(60L, 1);

		HikingBackpack dropped = hiking();
		dropped.quantity(1);
		dropped.items.add(stale);
		assertTrue(dropped.collect(hero.belongings.backpack));

		assertFalse(dropped.items.contains(stale));
		assertEquals(0, stale.quantity());
		assertEquals(0, stale.upgradeScrollUses);
	}

	@Test
	public void belongingsBundleRoundTripRebalancesAndPreservesItemState() {
		Hero original = heroWith(new PreferredBag(), new TestFallbackBag());
		PreferredItem overflow = new PreferredItem();
		overflow.quantity(3);
		overflow.upgradeScrollUses = 2;
		original.belongings.getItem(TestFallbackBag.class).items.add(overflow);
		Bundle bundle = new Bundle();
		original.belongings.storeInBundle(bundle);

		Hero restored = newHero();
		restored.belongings.restoreFromBundle(bundle);
		PreferredItem restoredItem = restored.belongings.getItem(PreferredItem.class);

		assertNotNull(restoredItem);
		assertEquals(3, restoredItem.quantity());
		assertEquals(2, restoredItem.upgradeScrollUses);
		assertTrue(restored.belongings.getItem(PreferredBag.class).contains(restoredItem));
	}

	@Test
	public void bagTabsDeclareHikingImmediatelyAfterMainBackpack() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java");
		int hiking = source.indexOf("HikingBackpack.class");
		int velvet = source.indexOf("VelvetPouch.class", hiking);
		int scroll = source.indexOf("ScrollHolder.class", velvet);
		int potion = source.indexOf("PotionBandolier.class", scroll);
		int magical = source.indexOf("MagicalHolster.class", potion);

		assertTrue(hiking >= 0);
		assertTrue(hiking < velvet && velvet < scroll && scroll < potion && potion < magical);
	}

	private Hero newHero() {
		try {
			Hero hero = allocateWithoutConstructor(Hero.class);
			hero.talents = new ArrayList<>();
			hero.metamorphedTalents = new LinkedHashMap<>();
			hero.sublimationTalents = new LinkedHashMap<>();
			hero.negativeTalents = new LinkedHashMap<>();
			setField(Char.class, hero, "buffs", new LinkedHashSet<>());
			setField(Char.class, hero, "immunities", new HashSet<>());
			setField(Char.class, hero, "properties", new HashSet<>());

			Belongings belongings = allocateWithoutConstructor(Belongings.class);
			Belongings.Backpack backpack = bag(Belongings.Backpack.class);
			backpack.owner = hero;
			belongings.backpack = backpack;
			setField(Belongings.class, belongings, "owner", hero);
			hero.belongings = belongings;
			Dungeon.hero = hero;
			return hero;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private Hero heroWith(Bag... bags) {
		Hero hero = newHero();
		for (Bag bag : bags) {
			bag.owner = hero;
			hero.belongings.backpack.items.add(bag);
		}
		return hero;
	}

	private PreferredBag fullPreferredBag() {
		PreferredBag bag = new PreferredBag();
		fillBag(bag);
		return bag;
	}

	private void fillBag(Bag bag) {
		while (bag.items.size() < bag.capacity()) {
			bag.items.add(new GeneralItem());
		}
	}

	private void fillMainBackpack(Belongings.Backpack backpack) {
		while (backpack.items.size() < backpack.capacity()) {
			backpack.items.add(new GeneralItem());
		}
	}

	private PreferredItem putPreferredItemInFallback(Hero hero) {
		PreferredItem item = new PreferredItem();
		hero.belongings.getItem(HikingBackpack.class).items.add(item);
		return item;
	}

	private Item firstDirectOrdinaryItem(Belongings.Backpack backpack) {
		for (Item item : backpack.items) {
			if (!(item instanceof Bag)) return item;
		}
		throw new AssertionError("main backpack contains no ordinary item");
	}

	private Item lastDirectOrdinaryItem(Belongings.Backpack backpack) {
		for (int i = backpack.items.size() - 1; i >= 0; i--) {
			Item item = backpack.items.get(i);
			if (!(item instanceof Bag)) return item;
		}
		throw new AssertionError("main backpack contains no ordinary item");
	}

	private int countIdentity(Bag bag, Item target) {
		int count = 0;
		for (Item item : bag.items) {
			if (item == target) count++;
			if (item instanceof Bag) count += countIdentity((Bag) item, target);
		}
		return count;
	}

	private HikingBackpack hiking() {
		return bag(HikingBackpack.class);
	}

	private <T extends Bag> T bag(Class<T> type) {
		T bag = allocateWithoutConstructor(type);
		bag.items = new ArrayList<>();
		return bag;
	}

	private static void setField(Class<?> declaringClass, Object target,
			String name, Object value) throws Exception {
		Field field = declaringClass.getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	@SuppressWarnings("unchecked")
	private static <T> T mock(Class<T> type) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
				(proxy, method, args) -> {
					if (method.getReturnType() == Preferences.class) return mock(Preferences.class);
					if (!method.getReturnType().isPrimitive()) return null;
					if (method.getReturnType() == boolean.class) return false;
					if (method.getReturnType() == char.class) return '\0';
					if (method.getReturnType() == byte.class) return (byte) 0;
					if (method.getReturnType() == short.class) return (short) 0;
					if (method.getReturnType() == int.class) return 0;
					if (method.getReturnType() == long.class) return 0L;
					if (method.getReturnType() == float.class) return 0f;
					if (method.getReturnType() == double.class) return 0d;
					return null;
				});
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			return (T) ((Unsafe) unsafeField.get(null)).allocateInstance(type);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static String readCoreSource(String relativePath) throws Exception {
		return new String(Files.readAllBytes(coreDirectory().resolve("src/main/java")
				.resolve(relativePath)), StandardCharsets.UTF_8);
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}

	public static class TestFallbackBag extends Bag {
		@Override
		public boolean isFallbackStorage() {
			return true;
		}
	}

	public static class PreferredBag extends Bag {
		@Override
		public boolean canHold(Item item) {
			return item instanceof PreferredItem && super.canHold(item);
		}
	}

	public static class GeneralItem extends Item {
	}

	public static class PreferredItem extends Item {
		{
			stackable = true;
		}
	}

	public static class TestKindOfWeapon extends com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon {
		@Override
		public int min(int level) {
			return 0;
		}

		@Override
		public int max(int level) {
			return 0;
		}
	}

	public static class TestMissile extends MissileWeapon {
		{
			tier = 1;
		}

		@Override
		public int min(int level) {
			return 1;
		}

		@Override
		public int max(int level) {
			return 1;
		}
	}
}
