package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TrinityTest {

	@Test
	public void bodyFormCannotDuplicateKingsRingEnchantment() {
		assertTrue(Trinity.duplicatesBodyFormEffect(null, null, new Blazing(), null, new Blazing()));
	}

	@Test
	public void bodyFormCannotDuplicateKingsRingGlyph() {
		assertTrue(Trinity.duplicatesBodyFormEffect(null, null, null, new Stone(), new Stone()));
	}
}
