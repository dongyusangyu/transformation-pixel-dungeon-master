package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DisintegrationTrapDamageTest {

	@Test
	public void disintegrationDamageIsMagical() {
		assertEquals(DamageTag.MAGICAL, DisintegrationTrap.damageTag());
		assertEquals(DamageTag.MAGICAL,
				DamageTag.primaryIconTag(DamageTag.of(DisintegrationTrap.damageTag())));
	}
}
