package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sun.misc.Unsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SurfaceTownLevelTest {

	@After
	public void resetLimitedDrops() {
		Dungeon.LimitedDrops.reset();
	}

	@Test
	public void surfaceShopContainsOneSublimationScrollPerAllowedBoss() throws Exception {
		Path sourcePath = Paths.get("src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java");
		String source = Files.readString(sourcePath);

		assertTrue(source.contains("import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"GOO\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"TENGU\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"DM300\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"DWARFKING\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"YOG\")"));
		assertEquals(1, occurrences(source, "items.add(new Torch());"));
	}

	@Test
	public void startupShopOffersHikingBackpackOnlyWhenHeroDoesNotOwnOne() {
		Dungeon.LimitedDrops.HIKING_BACKPACK.count = 0;
		Belongings belongings = emptyBelongings();

		assertTrue(SurfaceTownLevel.shouldOfferHikingBackpack(belongings));

		Dungeon.LimitedDrops.HIKING_BACKPACK.drop();
		assertFalse(SurfaceTownLevel.shouldOfferHikingBackpack(belongings));
	}

	@Test
	public void startupShopDoesNotAddOwnedHikingBackpackAfterLimitedDropsReset() {
		Dungeon.LimitedDrops.HIKING_BACKPACK.count = 0;
		Belongings belongings = emptyBelongings();
		belongings.backpack.items.add(emptyBag(HikingBackpack.class));
		assertFalse(SurfaceTownLevel.shouldOfferHikingBackpack(belongings));
		assertEquals(0, Dungeon.LimitedDrops.HIKING_BACKPACK.count);
	}

	private static int occurrences(String source, String token) {
		int count = 0;
		int offset = 0;
		while ((offset = source.indexOf(token, offset)) >= 0) {
			count++;
			offset += token.length();
		}
		return count;
	}

	private static Belongings emptyBelongings() {
		try {
			Belongings belongings = (Belongings) unsafe().allocateInstance(Belongings.class);
			Belongings.Backpack backpack = (Belongings.Backpack)
					unsafe().allocateInstance(Belongings.Backpack.class);
			backpack.items = new ArrayList<>();
			belongings.backpack = backpack;
			return belongings;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag> T emptyBag(
			Class<T> type) {
		try {
			T bag = (T) unsafe().allocateInstance(type);
			bag.items = new ArrayList<>();
			return bag;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static Unsafe unsafe() throws Exception {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (Unsafe) field.get(null);
	}
}
