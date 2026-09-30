package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.PoisonDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScrollOfTransmutationStackTest {

	@Test
	public void transmutingTippedDartsRemovesOneWhileOtherMissilesRemoveTheStack() {
		PoisonDart darts = TestHeroFactory.allocateItem(PoisonDart.class);
		Shuriken shuriken = TestHeroFactory.allocateItem(Shuriken.class);

		assertFalse(TransmutationStackPolicy.removesEntireStack(darts));
		assertTrue(TransmutationStackPolicy.removesEntireStack(shuriken));
	}
}
