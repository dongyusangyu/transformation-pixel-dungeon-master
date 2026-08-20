package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class VenomousSickleTest {

	@Test
	public void tierSixWarScytheProfileMatchesSpecification() {
		VenomousSickle weapon = new VenomousSickle();

		assertEquals(6, weapon.tierForTest());
		assertEquals(20, weapon.STRReq(0));
		assertEquals(6, weapon.min(0));
		assertEquals(47, weapon.max(0));
		assertEquals(13, weapon.min(7));
		assertEquals(96, weapon.max(7));
		assertEquals(0.8f, weapon.accuracyForTest(), 0f);
		assertEquals(1f, weapon.delayForTest(), 0f);
		assertEquals(1, weapon.rangeForTest());
	}

	@Test
	public void toxicEffectFormulaUsesNonNegativeWeaponLevels() {
		assertEquals(0.20f, VenomousSickle.procChanceForLevel(0), 0f);
		assertEquals(0.25f, VenomousSickle.procChanceForLevel(1), 0f);
		assertEquals(0.70f, VenomousSickle.procChanceForLevel(10), 0f);
		assertEquals(1.00f, VenomousSickle.procChanceForLevel(99), 0f);
		assertEquals(0.20f, VenomousSickle.procChanceForLevel(-3), 0f);
		assertEquals(10, VenomousSickle.poisonDuration(0));
		assertEquals(15, VenomousSickle.corrosionDamage(10));
		assertEquals(13, VenomousSickle.corrosionDuration(10));
		assertEquals(20, VenomousSickle.oozeDuration(10));
		assertEquals(36, VenomousSickle.corruptionDebuffDuration(10));
	}

	@Test
	public void corruptionFallbackUsesOnlySpecifiedWeightedDebuffs() {
		LinkedHashMap<Class<?>, Float> expected = new LinkedHashMap<>();
		expected.put(Weakness.class, 2f);
		expected.put(Vulnerable.class, 2f);
		expected.put(Cripple.class, 1f);
		expected.put(Blindness.class, 1f);
		expected.put(Terror.class, 1f);
		expected.put(Amok.class, 3f);
		expected.put(Slow.class, 2f);
		expected.put(Hex.class, 2f);
		expected.put(Paralysis.class, 1f);

		assertEquals(expected, VenomousSickle.corruptionDebuffWeights());
	}
}
