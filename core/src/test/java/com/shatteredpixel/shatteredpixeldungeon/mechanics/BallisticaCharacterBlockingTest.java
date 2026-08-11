package com.shatteredpixel.shatteredpixeldungeon.mechanics;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.ChainMace;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BallisticaCharacterBlockingTest {

	@Test
	public void onlyCharactersThatBlockBallisticsCauseCharacterCollisions() {
		assertTrue(Ballistica.stopsOnCharacter(new Rat()));
		assertFalse(Ballistica.stopsOnCharacter(new ChainMace.BallFollower()));
		assertFalse(Ballistica.stopsOnCharacter(null));
	}
}
