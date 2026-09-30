package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberd;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MissileSpriteTest {
	@Test public void huntressGaleArrowUsesSpiritArrowArtWithoutSpin() {
		try (com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites ignored =
				new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites()) {
			Item arrow = new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss.GaleArrowVFX();
			assertEquals(ItemSpriteSheet.SPIRIT_ARROW, arrow.image);
			assertEquals(0, MissileSprite.angularSpeedFor(arrow));
			org.junit.Assert.assertNotNull(arrow.emitter());
		}
	}

	@Test
	public void radiantGoldHalberdProjectionDoesNotSpin() {
		assertEquals(0, MissileSprite.angularSpeedFor(new RadiantGoldHalberd()));
	}

	@Test
	public void unregisteredItemsKeepDefaultSpin() {
		assertEquals(720, MissileSprite.angularSpeedFor(new Item() {}));
	}
}
