package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import sun.misc.Unsafe;
import java.util.ArrayList;

public class ChainShadowThiefTest {

	private Files previousFiles;
	private Application previousApplication;

	@Before
	public void installHeadlessFiles() {
		previousFiles = Gdx.files;
		previousApplication = Gdx.app;
		Gdx.files = new HeadlessFiles();
		Gdx.app = mock(Application.class);
	}

	@After
	public void restoreFiles() {
		Gdx.files = previousFiles;
		Gdx.app = previousApplication;
	}

	@Test
	public void usesTowerBaselineStatsAndConfiguredLoot() {
		TestThief thief = new TestThief();

		assertEquals(100, thief.HT);
		assertEquals(100, thief.HP);
		assertEquals(40, thief.attackSkill(null));
		assertEquals(20, thief.defenseSkillValue());
		assertEquals(1f, thief.baseSpeedValue(), 0f);
		assertEquals(1f, thief.attackDelay(), 0f);
		assertEquals(13, thief.EXP);
		assertEquals(30, thief.maxLvl);
		assertTrue(thief.lootValue() != null);
		assertEquals(0.03f, thief.lootChanceValue(), 0f);

		for (int i = 0; i < 500; i++) {
			int damage = thief.damageRoll();
			assertTrue(damage >= 15 && damage <= 40);
			assertEquals(0, thief.drRoll());
		}
	}

	@Test
	public void carryingAndFleeingSpeedIsExactlyTwo() {
		TestThief thief = new TestThief();
		assertEquals(1f, thief.speed(), 0f);

		thief.item = new TestItem();
		thief.state = thief.FLEEING;
		assertEquals(2f, thief.speed(), 0f);
	}

	@Test
	public void ordinaryThiefSpeedAndAttackTimingRemainUnchanged() {
		com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief thief =
				new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief();
		thief.item = new TestItem();

		assertEquals(5f / 6f, thief.speed(), 0.0001f);
		assertEquals(0.5f, thief.attackDelay(), 0f);
	}

	@Test
	public void meleeStealRemainsAvailableAfterChainUse() {
		Hero hero = testHero();
		TestItem potions = new TestItem();
		potions.quantity(5);
		hero.belongings.backpack.items.add(potions);
		TestThief thief = new TestThief();
		thief.markChainUsedForTest();

		assertTrue(thief.stealForTest(hero));
		assertEquals(5, thief.item.quantity());
		assertTrue(hero.belongings.backpack.items.isEmpty());
		assertTrue(thief.state == thief.FLEEING);
		assertTrue(thief.chainUsedFromBundle());
	}

	@Test
	public void meleeStealDoesNotConsumeUnusedChainUse() {
		Hero hero = testHero();
		hero.belongings.backpack.items.add(new TestItem().quantity(5));
		TestThief thief = new TestThief();

		assertTrue(thief.stealForTest(hero));
		assertTrue(thief.item.quantity() == 5);
		assertTrue(!thief.chainUsedFromBundle());
	}

	@Test
	public void onlyEligibleBackpackItemsCanBeSelected() {
		Hero hero = testHero();
		TestItem upgraded = (TestItem)new TestItem().upgrade();
		upgraded.unique = true;
		hero.belongings.backpack.items.add(upgraded);
		TestThief thief = new TestThief();

		assertTrue(!thief.stealForTest(hero));
		assertNull(thief.item);
		assertTrue(!thief.chainUsedFromBundle());
	}

	@Test
	public void chainUseIsLimitedPerThiefAndIndependentBetweenThieves() {
		Hero hero = testHero();
		hero.belongings.backpack.items.add(new TestItem().quantity(2));
		TestThief first = new TestThief();

		assertTrue(first.chainStealForTest(hero));
		first.item = null;
		hero.belongings.backpack.items.add(new TestItem().quantity(2));
		assertTrue(!first.chainStealForTest(hero));

		TestThief second = new TestThief();
		assertTrue(second.chainStealForTest(hero));
	}

	@Test
	public void chainStateAndCarriedStackSurviveBundleRoundTrip() {
		Hero hero = testHero();
		TestItem potions = (TestItem)new TestItem().quantity(4);
		hero.belongings.backpack.items.add(potions);
		TestThief original = new TestThief();
		assertTrue(original.chainStealForTest(hero));

		Bundle saved = new Bundle();
		original.storeInBundle(saved);
		TestThief restored = new TestThief();
		restored.restoreFromBundle(saved);

		assertTrue(restored.chainUsedFromBundle());
		assertEquals(4, restored.item.quantity());
		assertTrue(!restored.stealForTest(hero));
	}

	private static class TestThief extends ChainShadowThief {

		private boolean stealForTest(Hero hero) {
			boolean result = steal(hero);
			if (result) state = FLEEING;
			return result;
		}

		private boolean chainStealForTest(Hero hero) {
			Item target = hero.belongings.backpack.items.isEmpty()
					? null : hero.belongings.backpack.items.get(0);
			boolean result = stealByChain(hero, target);
			if (result) state = FLEEING;
			return result;
		}

		private boolean chainUsedFromBundle() {
			Bundle saved = new Bundle();
			storeInBundle(saved);
			return saved.getBoolean("chain_used");
		}

		private void markChainUsedForTest() {
			try {
				Field field = ChainShadowThief.class.getDeclaredField("chainUsed");
				field.setAccessible(true);
				field.setBoolean(this, true);
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}

		private int defenseSkillValue() {
			return defenseSkill;
		}

		private float baseSpeedValue() {
			return baseSpeed;
		}

		private Object lootValue() {
			return loot;
		}

		private float lootChanceValue() {
			return lootChance;
		}
	}

	private static Hero testHero() {
		try {
			Hero hero = allocateWithoutConstructor(Hero.class);
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings belongings =
					allocateWithoutConstructor(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings.class);
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings.Backpack backpack =
					allocateWithoutConstructor(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings.Backpack.class);
			backpack.items = new ArrayList<>();
			backpack.owner = hero;
			belongings.backpack = backpack;
			hero.belongings = belongings;
			return hero;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		return (T) ((Unsafe) unsafeField.get(null)).allocateInstance(type);
	}

	public static class TestItem extends Item {
		{
			stackable = true;
		}

		@Override
		public String name() {
			return "test item";
		}
	}

	private static class HeadlessFiles implements Files {
		private FileHandle asset(String path) {
			java.io.File root = new java.io.File("core/src/main/assets");
			if (!root.isDirectory()) {
				root = new java.io.File("src/main/assets");
			}
			return new FileHandle(new java.io.File(root, path));
		}

		@Override public FileHandle getFileHandle(String path, FileType type) { return asset(path); }
		@Override public FileHandle classpath(String path) { return asset(path); }
		@Override public FileHandle internal(String path) { return asset(path); }
		@Override public FileHandle external(String path) { return asset(path); }
		@Override public FileHandle absolute(String path) { return asset(path); }
		@Override public FileHandle local(String path) { return asset(path); }
		@Override public String getExternalStoragePath() { return ""; }
		@Override public boolean isExternalStorageAvailable() { return false; }
		@Override public String getLocalStoragePath() { return "."; }
		@Override public boolean isLocalStorageAvailable() { return true; }
	}

	@SuppressWarnings("unchecked")
	private static <T> T mock(Class<T> type) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
				(proxy, method, args) -> {
					if (method.getReturnType() == Preferences.class) return mock(Preferences.class);
					if (!method.getReturnType().isPrimitive()) return null;
					if (method.getReturnType() == boolean.class) return false;
					if (method.getReturnType() == char.class) return '\0';
					return 0;
				});
	}
}
