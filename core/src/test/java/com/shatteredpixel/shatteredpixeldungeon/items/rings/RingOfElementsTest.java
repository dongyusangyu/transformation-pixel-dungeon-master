package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RingOfElementsTest {

	@Test
	public void originalMonsterDoesNotShortenUnrelatedBuffs() {
		assertEquals(1f,
				ElementalResistance.resistanceMultiplier(Haste.class, 0.67f, 1f, 0),
				0f);
	}

	@Test
	public void originalMonsterReducesElementalEffects() {
		assertEquals(0.67f,
				ElementalResistance.resistanceMultiplier(Burning.class, 0.67f, 1f, 0),
				0f);
	}

	@Test
	public void resistanceSourcesMultiplyForMagicEffects() {
		float expected = 0.67f * 1.3f * (float)Math.pow(0.825f, 2);

		assertEquals(expected,
				ElementalResistance.resistanceMultiplier(Weakness.class, 0.67f, 1.3f, 2),
				0.0001f);
	}

	@Test
	public void magicFeatherUsesFullElementalResistanceRange() {
		assertEquals(1.15f, ElementalResistance.magicFeatherMultiplier(Burning.class, 0), 0f);
		assertEquals(1.45f, ElementalResistance.magicFeatherMultiplier(Corrosion.class, 2), 0f);
		assertEquals(1f, ElementalResistance.magicFeatherMultiplier(Haste.class, 3), 0f);
	}
}
