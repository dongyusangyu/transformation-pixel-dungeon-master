package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImpShopRoomHikingBackpackTest {

	private int[] previousLimitedDropCounts;
	private Hero previousHero;
	private int previousVersion;
	private int previousChallenges;

	@Before
	public void setUp() {
		Dungeon.LimitedDrops[] drops = Dungeon.LimitedDrops.values();
		previousLimitedDropCounts = new int[drops.length];
		for (int i = 0; i < drops.length; i++) {
			previousLimitedDropCounts[i] = drops[i].count;
		}
		previousHero = Dungeon.hero;
		previousVersion = Dungeon.version;
		previousChallenges = Dungeon.challenges;
		Dungeon.version = Integer.MAX_VALUE;
		Dungeon.challenges = 0;
	}

	@After
	public void tearDown() {
		Dungeon.LimitedDrops[] drops = Dungeon.LimitedDrops.values();
		for (int i = 0; i < drops.length; i++) {
			drops[i].count = previousLimitedDropCounts[i];
		}
		Dungeon.hero = previousHero;
		Dungeon.version = previousVersion;
		Dungeon.challenges = previousChallenges;
	}

	@Test
	public void finalImpShopOffersHikingBackpackOnlyBeforeLimitedDrop() {
		Dungeon.LimitedDrops.HIKING_BACKPACK.count = 0;

		assertTrue(ImpShopRoom.shouldOfferHikingBackpack(null));

		Dungeon.LimitedDrops.HIKING_BACKPACK.drop();
		assertFalse(ImpShopRoom.shouldOfferHikingBackpack(null));
	}

	@Test
	public void finalImpShopChecksLimitedDropAndOwnedBag() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/levels/rooms/standard/ImpShopRoom.java");

		assertTrue(source.contains("!Dungeon.LimitedDrops.HIKING_BACKPACK.dropped()"));
		assertTrue(source.contains("belongings.getItem(HikingBackpack.class) == null"));
		assertTrue(source.contains("stock.add(new HikingBackpack());"));
		assertTrue(source.contains("Dungeon.LimitedDrops.HIKING_BACKPACK.drop();"));
	}

	@Test
	public void floorTwentyPriceIsOneThousandGold() {
		assertEquals(1000, Shopkeeper.sellPrice(allocateWithoutConstructor(HikingBackpack.class), 20));
	}

	@Test
	public void oldLimitedDropBundleDefaultsHikingBackpackToAvailable() {
		Dungeon.LimitedDrops.HIKING_BACKPACK.count = 1;
		Dungeon.LimitedDrops.restore(new Bundle());

		assertEquals(0, Dungeon.LimitedDrops.HIKING_BACKPACK.count);
	}

	@Test
	public void ordinaryShopBagPoolDoesNotContainHikingBackpack() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/levels/rooms/special/ShopRoom.java");
		int start = source.indexOf("protected static Bag ChooseBag");
		int end = source.indexOf("return bestBag;", start);

		assertTrue(start >= 0 && end > start);
		assertFalse(source.substring(start, end).contains("HikingBackpack"));
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
}
