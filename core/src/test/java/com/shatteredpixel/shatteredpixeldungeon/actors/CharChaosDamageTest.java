package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class CharChaosDamageTest {

	@Test
	public void chaosConvertsPhysicalDamageToMagicalAndPreservesOtherTags() {
		DamageTag[] result = Char.resolveDamageTagsForFeeling(Level.Feeling.CHAOS,
				DamageTag.PHYSICAL, DamageTag.MELEE, DamageTag.FIRE, DamageTag.NO_ARMOR);

		assertEquals(EnumSet.of(DamageTag.MAGICAL, DamageTag.MELEE,
				DamageTag.FIRE, DamageTag.NO_ARMOR), DamageTag.of(result));
	}

	@Test
	public void chaosConvertsMagicalDamageToPhysicalAndPreservesOtherTags() {
		DamageTag[] result = Char.resolveDamageTagsForFeeling(Level.Feeling.CHAOS,
				DamageTag.MAGICAL, DamageTag.RANGED, DamageTag.ELECTRIC);

		assertEquals(EnumSet.of(DamageTag.PHYSICAL, DamageTag.RANGED,
				DamageTag.ELECTRIC), DamageTag.of(result));
	}

	@Test
	public void chaosLeavesAmbiguousDamageTypesUnchanged() {
		DamageTag[] both = Char.resolveDamageTagsForFeeling(Level.Feeling.CHAOS,
				DamageTag.PHYSICAL, DamageTag.MAGICAL, DamageTag.FIRE);
		DamageTag[] neither = Char.resolveDamageTagsForFeeling(Level.Feeling.CHAOS,
				DamageTag.FIRE, DamageTag.UNAVOIDABLE);

		assertEquals(EnumSet.of(DamageTag.PHYSICAL, DamageTag.MAGICAL, DamageTag.FIRE),
				DamageTag.of(both));
		assertEquals(EnumSet.of(DamageTag.FIRE, DamageTag.UNAVOIDABLE),
				DamageTag.of(neither));
	}

	@Test
	public void ordinaryFeelingsDoNotConvertDamageTypes() {
		DamageTag[] result = Char.resolveDamageTagsForFeeling(Level.Feeling.NONE,
				DamageTag.PHYSICAL, DamageTag.MELEE);

		assertEquals(EnumSet.of(DamageTag.PHYSICAL, DamageTag.MELEE), DamageTag.of(result));
	}
}
