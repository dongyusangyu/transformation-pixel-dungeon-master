package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharFriarReasonTest {

	@Test
	public void friarReasonLossOnlyAcceptsAvoidableMobAttacks() {
		assertTrue(Char.isFriarReasonAttack(HeroClass.FRIAR, new Rat(), false));
		assertFalse(Char.isFriarReasonAttack(HeroClass.WARRIOR, new Rat(), false));
		assertFalse(Char.isFriarReasonAttack(HeroClass.FRIAR, new Rat(), true));
		assertFalse(Char.isFriarReasonAttack(
				HeroClass.FRIAR, new Viscosity.DeferedDamage(), false));
	}
}
