package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HeroGlyphLevelTest {

	@Test
	public void magicImmuneSuppressesGlyphBenefits() {
		Char target = new Char() {
		};

		assertFalse(Hero.suppressGlyphBenefits(target));

		new MagicImmune().attachTo(target);

		assertTrue(Hero.suppressGlyphBenefits(target));
	}
}
