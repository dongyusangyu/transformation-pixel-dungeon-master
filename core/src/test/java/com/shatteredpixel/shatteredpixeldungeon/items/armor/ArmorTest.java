package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ArmorTest {

	@Test
	public void kingsRingGlyphRejectsNonHeroDefender() {
		Char nonHero = new Char() {
		};

		assertFalse(Armor.canUseKingsRingGlyph(nonHero));
	}
}
