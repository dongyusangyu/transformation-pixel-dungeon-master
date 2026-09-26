package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import org.junit.After;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import sun.misc.Unsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RingOfWealthTreasureDropTest {
	private final int previousRulesVersion = Dungeon.rulesVersion;
	private final boolean previousNewCycle = Dungeon.newCycle;
	private final int previousBranch = Dungeon.branch;
	private final int previousDepth = Dungeon.depth;
	private Files previousFiles;

	@Before
	public void configureAssets() {
		previousFiles = Gdx.files;
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
	}

	@After
	public void restoreRulesVersion() {
		Gdx.files = previousFiles;
		Dungeon.rulesVersion = previousRulesVersion;
		Dungeon.newCycle = previousNewCycle;
		Dungeon.branch = previousBranch;
		Dungeon.depth = previousDepth;
	}

	@Test
	public void treasureRingPoolNeverReturnsAnotherRingOfWealth() {
		for (int i = 0; i < 500; i++) {
			Class<?> result = Generator.randomClassUsingDefaultsExcluding(
					Generator.Category.RING, RingOfWealth.class);
			assertTrue(Ring.class.isAssignableFrom(result));
			assertFalse(RingOfWealth.class.isAssignableFrom(result));
		}
	}

	@Test
	public void legacyEquipmentDropLevelStillCapsAtTen() {
		Dungeon.rulesVersion = Dungeon.LEGACY_RULES_VERSION;
		assertEquals(10, RingOfWealth.EquipmentDropLevelPolicy.level(0, 100));
		assertEquals(10, RingOfWealth.EquipmentDropLevelPolicy.level(12, 1));
		assertEquals(4, RingOfWealth.EquipmentDropLevelPolicy.level(0, 7));
	}

	@Test
	public void newRunEquipmentDropLevelCapsAtThree() {
		Dungeon.rulesVersion = Dungeon.CURRENT_RULES_VERSION;
		Dungeon.branch = TowerLevel.BRANCH;
		Dungeon.depth = 1;
		assertEquals(3, RingOfWealth.EquipmentDropLevelPolicy.level(0, 100));
		assertEquals(3, RingOfWealth.EquipmentDropLevelPolicy.level(8, 1));
		assertEquals(2, RingOfWealth.EquipmentDropLevelPolicy.level(0, 3));
	}

	@Test
	public void newRunKeepsNormalDungeonWealthCap() {
		Dungeon.rulesVersion = Dungeon.CURRENT_RULES_VERSION;
		Dungeon.branch = 0;
		Dungeon.depth = 10;
		assertEquals(10, RingOfWealth.EquipmentDropLevelPolicy.level(0, 100));
	}

	@Test
	public void newCycleCapsOnlyNewTreasureAndKeepsInheritedEquipment() {
		Dungeon.newCycle = true;
		Dungeon.rulesVersion = Dungeon.CURRENT_RULES_VERSION;
		Dungeon.branch = TowerLevel.BRANCH;
		Dungeon.depth = 1;
		RingOfWealth inherited = allocate(RingOfWealth.class);
		inherited.level(8);
		RingOfWealth fresh = allocate(RingOfWealth.class);

		RingOfWealth.EquipmentDropLevelPolicy.apply(fresh, 100);

		assertEquals(3, fresh.level());
		assertEquals(8, inherited.level());
	}

	@Test
	public void artifactDropsKeepTheirNativeLevelRegardlessOfCap() {
		Dungeon.rulesVersion = Dungeon.CURRENT_RULES_VERSION;
		EtherealChains chains = allocate(EtherealChains.class);
		SandalsOfNature sandals = allocate(SandalsOfNature.class);
		RingOfWealth.EquipmentDropLevelPolicy.apply(chains, 100);
		RingOfWealth.EquipmentDropLevelPolicy.apply(sandals, 100);
		assertEquals(0, chains.level());
		assertEquals(0, sandals.level());
		assertTrue(chains.level() <= chains.levelCap());
		assertTrue(sandals.level() <= sandals.levelCap());
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocate(Class<T> type) {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (T) ((Unsafe) field.get(null)).allocateInstance(type);
		} catch (ReflectiveOperationException error) {
			throw new AssertionError(error);
		}
	}
}
