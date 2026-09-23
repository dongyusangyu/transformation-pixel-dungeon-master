package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;

public class SpearShieldCollectTest {

	@Test
	public void fullBackpackReportsCollectionFailure() {
		Belongings.Backpack backpack = TestHeroFactory.allocateItem(Belongings.Backpack.class);
		backpack.items = new ArrayList<>();
		for (int i = 0; i < backpack.capacity(); i++) {
			backpack.items.add(TestHeroFactory.allocateItem(Item.class));
		}

		SpearShield spearShield = TestHeroFactory.allocateItem(SpearShield.class);
		spearShield.quantity(1);

		assertFalse(spearShield.collect(backpack));
	}
}
