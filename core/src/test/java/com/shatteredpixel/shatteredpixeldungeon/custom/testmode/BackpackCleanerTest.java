package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;

public class BackpackCleanerTest {

	private Application previousApplication;
	private Files previousFiles;

	@Before
	public void installHeadlessApplication() {
		previousApplication = Gdx.app;
		previousFiles = Gdx.files;
		Gdx.app = mock(Application.class);
		Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
				new Class<?>[]{Files.class}, (proxy, method, args) -> {
					if (method.getReturnType() == FileHandle.class) {
						File assetRoot = new File("core/src/main/assets");
						if (!assetRoot.isDirectory()) assetRoot = new File("src/main/assets");
						return new FileHandle(new File(assetRoot, (String) args[0]));
					}
					if (!method.getReturnType().isPrimitive()) return null;
					if (method.getReturnType() == boolean.class) return false;
					if (method.getReturnType() == char.class) return '\0';
					return 0;
				});
	}

	@After
	public void resetDungeonState() {
		Gdx.app = previousApplication;
		Gdx.files = previousFiles;
		Dungeon.hero = null;
		Dungeon.quickslot.reset();
	}

	@Test
	public void clearAllStillRemovesItemsMovedOutOfHikingBackpackDuringCleanup() throws Exception {
		Hero hero = TestHeroFactory.create();
		hero.belongings.backpack.owner = hero;
		Dungeon.hero = hero;

		HikingBackpack hikingBackpack = TestHeroFactory.allocateItem(HikingBackpack.class);
		hikingBackpack.unique = true;
		hikingBackpack.items = new ArrayList<>();
		hikingBackpack.owner = hero;
		Item firstNestedItem = new TestItem();
		Item secondNestedItem = new TestItem();
		hikingBackpack.items.add(firstNestedItem);
		hikingBackpack.items.add(secondNestedItem);

		for (int i = 0; i < 21; i++) {
			hero.belongings.backpack.items.add(new TestItem());
		}
		hero.belongings.backpack.items.add(hikingBackpack);

		BackpackCleaner cleaner = TestHeroFactory.allocateItem(BackpackCleaner.class);
		cleaner.setCurrent(hero);
		invokePrivate(cleaner, "clearAllItem");

		assertFalse(hero.belongings.backpack.contains(firstNestedItem));
		assertFalse(hero.belongings.backpack.contains(secondNestedItem));
	}

	private static void invokePrivate(BackpackCleaner cleaner, String methodName)
			throws Exception {
		Method method = BackpackCleaner.class.getDeclaredMethod(methodName);
		method.setAccessible(true);
		method.invoke(cleaner);
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

	private static class TestItem extends Item {
	}
}
