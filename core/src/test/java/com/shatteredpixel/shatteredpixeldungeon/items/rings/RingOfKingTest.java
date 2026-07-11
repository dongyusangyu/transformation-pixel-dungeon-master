package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RingOfKingTest {

	@After
	public void clearDiscoveredTypes() {
		Statistics.itemTypesDiscovered.clear();
	}

	@Test
	public void applyingEnchantmentRecordsItsType() {
		Statistics.discoverItemType(Blazing.class);
		assertTrue(Statistics.itemTypesDiscovered.contains(Blazing.class));
	}

	@Test
	public void applyingGlyphRecordsItsType() {
		Statistics.discoverItemType(Stone.class);
		assertTrue(Statistics.itemTypesDiscovered.contains(Stone.class));
	}

	@Test
	public void runemarkTextIsNotShownForCursedEnchantment() {
		assertTrue(RingOfKing.Text.shouldShowRunemarkText(HeroSubClass.RUNEMAGE, new Blazing()));
		assertFalse(RingOfKing.Text.shouldShowRunemarkText(HeroSubClass.RUNEMAGE, new Sacrificial()));
	}

	@Test
	public void combatMasterTextIsNotShownForCursedGlyph() {
		assertTrue(RingOfKing.Text.shouldShowCombatMasterText(HeroSubClass.COMBATMASTER, new Stone()));
		assertFalse(RingOfKing.Text.shouldShowCombatMasterText(HeroSubClass.COMBATMASTER, new Corrosion()));
	}
}
