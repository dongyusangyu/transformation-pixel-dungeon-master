package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PotionThrownUseTest {
	private Level oldLevel;
	private Hero oldHero;
	private Files oldFiles;
	private Hero oldUser;
	private int oldChallenges;

	@Before public void setUp() throws Exception {
		oldLevel = Dungeon.level;
		oldHero = Dungeon.hero;
		oldChallenges = Dungeon.challenges;
		Dungeon.challenges = 0;
		oldFiles = Gdx.files;
		GdxNativesLoader.load();
		Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
				new Class<?>[]{Files.class}, (proxy, method, args) -> {
					if (method.getReturnType() == FileHandle.class) {
						File assets = new File("core/src/main/assets");
						if (!assets.isDirectory()) assets = new File("src/main/assets");
						return new FileHandle(new File(assets, (String) args[0]));
					}
					return null;
				});
		TestLevel level = new TestLevel();
		level.setSize(7, 7);
		level.blobs = new HashMap<>();
		for (int i = 0; i < level.length(); i++) level.map[i] = Terrain.EMPTY;
		Dungeon.level = level;
		Dungeon.hero = TestHeroFactory.create();
		Dungeon.hero.talents.add(new LinkedHashMap<>());
		Dungeon.hero.talents.get(0).put(Talent.LIQUID_BARRIER, 1);
		Field user = Item.class.getDeclaredField("curUser");
		user.setAccessible(true);
		oldUser = (Hero) user.get(null);
		user.set(null, Dungeon.hero);
	}

	@After public void tearDown() throws Exception {
		Dungeon.level = oldLevel;
		Dungeon.hero = oldHero;
		Dungeon.challenges = oldChallenges;
		Gdx.files = oldFiles;
		Field user = Item.class.getDeclaredField("curUser");
		user.setAccessible(true);
		user.set(null, oldUser);
	}

	@Test public void harmlessHealingPotionSplashDoesNotCountAsUse() {
		int before = Catalog.useCount(PotionOfHealing.class);
		new PotionOfHealing().onThrow(24);
		assertEquals(before, Catalog.useCount(PotionOfHealing.class));
		assertNull(Dungeon.hero.buff(BlobImmunity.class));
	}

	@Test public void usefulThrownFlamePotionStillCountsAsUse() {
		int before = Catalog.useCount(PotionOfLiquidFlame.class);
		new PotionOfLiquidFlame().onThrow(24);
		assertEquals(before + 1, Catalog.useCount(PotionOfLiquidFlame.class));
		assertNotNull(Dungeon.hero.buff(BlobImmunity.class));
	}

	private static class TestLevel extends Level {
		@Override protected boolean build() { return true; }
		@Override protected void createMobs() {}
		@Override protected void createItems() {}
		@Override public void pressCell(int cell) {}
	}
}
