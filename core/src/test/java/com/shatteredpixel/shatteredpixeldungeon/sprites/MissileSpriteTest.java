package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberd;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MissileSpriteTest {

	@Test
	public void radiantGoldHalberdProjectionDoesNotSpin() {
		assertEquals(0, MissileSprite.angularSpeedFor(new RadiantGoldHalberd()));
	}

	@Test
	public void unregisteredItemsKeepDefaultSpin() {
		assertEquals(720, MissileSprite.angularSpeedFor(new Item() {}));
	}
}
