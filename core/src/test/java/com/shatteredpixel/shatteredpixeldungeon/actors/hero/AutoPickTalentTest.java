package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import sun.misc.Unsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AutoPickTalentTest {

	private Application previousApplication;
	private Files previousFiles;
	private Hero previousHero;
	private Level previousLevel;

	@Before
	public void setUp() {
		previousApplication = Gdx.app;
		previousFiles = Gdx.files;
		previousHero = Dungeon.hero;
		previousLevel = Dungeon.level;
		Gdx.app = mock(Application.class);
		Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
				new Class<?>[]{Files.class}, (proxy, method, args) -> {
					if (method.getReturnType() == FileHandle.class) {
						File assetRoot = new File("core/src/main/assets");
						if (!assetRoot.isDirectory()) assetRoot = new File("src/main/assets");
						return new FileHandle(new File(assetRoot, (String) args[0]));
					}
					return defaultValue(method.getReturnType());
				});
	}

	@After
	public void tearDown() {
		Gdx.app = previousApplication;
		Gdx.files = previousFiles;
		Dungeon.hero = previousHero;
		Dungeon.level = previousLevel;
		Dungeon.quickslot.reset();
	}

	@Test
	public void plusOneReducesPickupDelayButDoesNotAutoPick() throws Exception {
		TestContext context = contextWithTalentPoints(1);
		TestItem item = context.dropTestItem(13);

		assertEquals(0.5f, item.pickupDelay(), 0f);
		assertTrue(stepToward(context.hero, 13));

		assertSame(item, Dungeon.level.heaps.get(13).peek());
		assertFalse(context.hero.belongings.backpack.contains(item));
		assertEquals(0, item.pickupAttempts);
	}

	@Test
	public void plusTwoAutoPicksEveryCollectableItemOnEnteredHeap() throws Exception {
		TestContext context = contextWithTalentPoints(2);
		TestItem first = context.dropTestItem(13);
		TestItem second = context.appendTestItem(13);

		assertTrue(stepToward(context.hero, 13));

		assertNull(Dungeon.level.heaps.get(13));
		assertTrue(context.hero.belongings.backpack.contains(first));
		assertTrue(context.hero.belongings.backpack.contains(second));
		assertEquals(1, first.pickupAttempts);
		assertEquals(1, second.pickupAttempts);
	}

	@Test
	public void plusTwoDoesNotAutoPickFromShopHeap() throws Exception {
		TestContext context = contextWithTalentPoints(2);
		TestItem item = context.dropTestItem(13);
		Dungeon.level.heaps.get(13).type = Heap.Type.FOR_SALE;

		assertTrue(stepToward(context.hero, 13));

		assertSame(item, Dungeon.level.heaps.get(13).peek());
		assertFalse(context.hero.belongings.backpack.contains(item));
		assertEquals(0, item.pickupAttempts);
	}

	@Test
	public void plusTwoLeavesTopItemWhenBackpackCannotAcceptIt() throws Exception {
		TestContext context = contextWithTalentPoints(2);
		for (int i = 0; i < context.hero.belongings.backpack.capacity(); i++) {
			context.hero.belongings.backpack.items.add(new TestItem());
		}
		TestItem item = context.dropTestItem(13);

		assertTrue(stepToward(context.hero, 13));

		assertSame(item, Dungeon.level.heaps.get(13).peek());
		assertFalse(context.hero.belongings.backpack.contains(item));
		assertEquals(1, item.pickupAttempts);
	}

	@Test
	public void plusTwoCanMergeIntoAFullBackpack() throws Exception {
		TestContext context = contextWithTalentPoints(2);
		StackableTestItem stored = new StackableTestItem();
		context.hero.belongings.backpack.items.add(stored);
		while (context.hero.belongings.backpack.items.size()
				< context.hero.belongings.backpack.capacity()) {
			context.hero.belongings.backpack.items.add(new TestItem());
		}
		StackableTestItem dropped = new StackableTestItem();
		context.drop(13, dropped);

		assertTrue(stepToward(context.hero, 13));

		assertNull(Dungeon.level.heaps.get(13));
		assertEquals(2, stored.quantity());
		assertEquals(1, dropped.pickupAttempts);
	}

	private static TestContext contextWithTalentPoints(int points) throws Exception {
		TestLevel level = new TestLevel();
		level.setSize(5, 5);
		Arrays.fill(level.map, Terrain.EMPTY);
		level.blobs = new HashMap<>();
		level.heaps = new SparseArray<>();
		level.buildFlagMaps();
		Dungeon.level = level;

		TestHero hero = allocate(TestHero.class);
		setField(Char.class, hero, "buffs", new LinkedHashSet<Buff>());
		setField(Char.class, hero, "resistances", new HashSet<Class>());
		setField(Char.class, hero, "immunities", new HashSet<Class>());
		setField(Char.class, hero, "properties", new HashSet<Char.Property>());
		hero.heroClass = HeroClass.ROGUE;
		hero.subClass = HeroSubClass.NONE;
		hero.HP = hero.HT = 10;
		hero.pos = 12;
		hero.sprite = new HeadlessSprite();
		hero.talents = new ArrayList<>();
		LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
		tier.put(Talent.AUTO_PICK, points);
		hero.talents.add(tier);

		Belongings belongings = allocate(Belongings.class);
		Belongings.Backpack backpack = allocate(Belongings.Backpack.class);
		backpack.items = new ArrayList<>();
		backpack.owner = hero;
		belongings.backpack = backpack;
		setField(Belongings.class, belongings, "owner", hero);
		hero.belongings = belongings;
		Dungeon.hero = hero;
		return new TestContext(hero);
	}

	private static boolean stepToward(Hero hero, int target) throws Exception {
		Method method = Hero.class.getDeclaredMethod("getCloser", int.class);
		method.setAccessible(true);
		return (boolean) method.invoke(hero, target);
	}

	private static void setField(Class<?> owner, Object target, String name, Object value)
			throws Exception {
		Field field = owner.getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocate(Class<T> type) throws Exception {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (T) ((Unsafe) field.get(null)).allocateInstance(type);
	}

	@SuppressWarnings("unchecked")
	private static <T> T mock(Class<T> type) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
				(proxy, method, args) -> {
					if (method.getReturnType() == Preferences.class) return mock(Preferences.class);
					return defaultValue(method.getReturnType());
				});
	}

	private static Object defaultValue(Class<?> type) {
		if (!type.isPrimitive()) return null;
		if (type == boolean.class) return false;
		if (type == char.class) return '\0';
		if (type == byte.class) return (byte) 0;
		if (type == short.class) return (short) 0;
		if (type == int.class) return 0;
		if (type == long.class) return 0L;
		if (type == float.class) return 0f;
		if (type == double.class) return 0d;
		return null;
	}

	private static class TestContext {
		final TestHero hero;

		TestContext(TestHero hero) {
			this.hero = hero;
		}

		TestItem dropTestItem(int cell) {
			TestItem item = new TestItem();
			drop(cell, item);
			return item;
		}

		TestItem appendTestItem(int cell) {
			TestItem item = new TestItem();
			Dungeon.level.heaps.get(cell).items.addLast(item);
			return item;
		}

		void drop(int cell, Item item) {
			Heap heap = new Heap();
			heap.pos = cell;
			heap.items.add(item);
			Dungeon.level.heaps.put(cell, heap);
		}
	}

	private static class TestHero extends Hero {
		@Override
		public void updateHT(boolean boostHP) {
		}

		@Override
		public boolean isImmune(Class effect) {
			return false;
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		@Override
		public float speed() {
			return 1f;
		}

		@Override
		public boolean search(boolean intentional) {
			return false;
		}

		@Override
		public void move(int step, boolean travelling) {
			pos = step;
		}
	}

	private static class TestItem extends Item {
		int pickupAttempts;

		@Override
		public boolean doPickUp(Hero hero, int pos) {
			pickupAttempts++;
			if (collect(hero.belongings.backpack)) {
				hero.spendAndNext(pickupDelay());
				return true;
			}
			return false;
		}
	}

	private static class StackableTestItem extends TestItem {
		{
			stackable = true;
		}
	}

	private static class HeadlessSprite extends CharSprite {
		@Override
		public void move(int from, int to) {
		}
	}

	private static class TestLevel extends Level {
		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}
}
