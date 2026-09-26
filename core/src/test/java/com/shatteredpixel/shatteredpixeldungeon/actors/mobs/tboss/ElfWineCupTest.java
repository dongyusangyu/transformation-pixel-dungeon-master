package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElfWineCupTest {

	@Test
	public void newlyCreatedCupIsActiveWithoutBecomingHostileOrMobile() {
		ElfWineCup cup = new ElfWineCup();

		assertEquals(cup.HUNTING, cup.state);
		assertEquals(Char.Alignment.NEUTRAL, cup.alignment);
		assertTrue(cup.properties().contains(Char.Property.IMMOVABLE));
	}
}
