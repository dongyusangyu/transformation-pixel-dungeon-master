package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ArmorTest {

	@Test
	public void kingsRingGlyphRejectsNonHeroDefender() {
		Char nonHero = new Char() {
		};

		assertFalse(Armor.canUseKingsRingGlyph(nonHero));
	}

	@Test
	public void armorTransferReplacesOldClassArmorUpgradeScrollUses() {
		ClassArmor destination = new ClassArmor() {
		};
		destination.upgradeScrollUses = 4;
		Armor source = new Armor(1);
		source.upgradeScrollUses = 2;

		ClassArmor.transferUpgradeScrollUses(destination, source);

		assertEquals(2, destination.upgradeScrollUses);
	}

	@Test
	public void armorWithoutUpgradeCreditClearsDestinationCredit() {
		ClassArmor destination = new ClassArmor() {
		};
		destination.upgradeScrollUses = 4;
		Armor source = new Armor(1);

		ClassArmor.transferUpgradeScrollUses(destination, source);

		assertEquals(0, destination.upgradeScrollUses);
	}
}
