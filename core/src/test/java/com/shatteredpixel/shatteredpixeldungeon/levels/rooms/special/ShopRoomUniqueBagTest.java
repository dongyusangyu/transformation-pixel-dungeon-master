package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;

import org.junit.After;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShopRoomUniqueBagTest {

	@After
	public void resetLimitedDrops() {
		Dungeon.LimitedDrops.reset();
	}

	@Test
	public void ownedBagIsNotOfferedWhenLimitedDropFlagWasReset() throws Exception {
		Bag backpack = new Bag();
		backpack.items.add(emptyBag(VelvetPouch.class));
		Dungeon.LimitedDrops.VELVET_POUCH.count = 0;

		assertFalse(ShopRoom.shouldOfferBag(backpack, VelvetPouch.class,
				Dungeon.LimitedDrops.VELVET_POUCH));
	}

	@Test
	public void unownedUndroppedBagRemainsAvailable() {
		assertTrue(ShopRoom.shouldOfferBag(new Bag(), ScrollHolder.class,
				Dungeon.LimitedDrops.SCROLL_HOLDER));
	}

	@Test
	public void everyOrdinaryShopBagUsesTheOwnershipGuard() throws Exception {
		Bag backpack = new Bag();
		backpack.items.add(emptyBag(ScrollHolder.class));
		backpack.items.add(emptyBag(PotionBandolier.class));
		backpack.items.add(emptyBag(MagicalHolster.class));

		assertFalse(ShopRoom.shouldOfferBag(backpack, ScrollHolder.class,
				Dungeon.LimitedDrops.SCROLL_HOLDER));
		assertFalse(ShopRoom.shouldOfferBag(backpack, PotionBandolier.class,
				Dungeon.LimitedDrops.POTION_BANDOLIER));
		assertFalse(ShopRoom.shouldOfferBag(backpack, MagicalHolster.class,
				Dungeon.LimitedDrops.MAGICAL_HOLSTER));
	}

	private static <T extends Bag> T emptyBag(Class<T> type) throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		T bag = (T) ((Unsafe) unsafeField.get(null)).allocateInstance(type);
		bag.items = new ArrayList<>();
		return bag;
	}
}
