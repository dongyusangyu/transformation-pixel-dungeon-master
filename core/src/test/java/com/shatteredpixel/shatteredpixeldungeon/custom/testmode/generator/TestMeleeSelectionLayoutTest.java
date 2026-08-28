package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMeleeSelectionLayoutTest {

	@Test
	public void fifteenWeaponsOccupyThreeRowsAndTheLastWeaponCanBeSelected() {
		assertEquals(48f, TestMelee.gridContentHeight(15, 7, 16), 0f);
		assertEquals(14, TestMelee.gridIndexAt(1, 33, 15, 7, 16, 16));
		assertEquals(-1, TestMelee.gridIndexAt(17, 33, 15, 7, 16, 16));
	}

	@Test
	public void enchantmentPoolsExposeOnlyValidEntriesAndClampOldSelections() {
		assertEquals(1, TestMelee.enchantmentList(0).length);
		assertEquals(4, TestMelee.enchantmentList(1).length);
		assertEquals(7, TestMelee.enchantmentList(2).length);
		assertEquals(4, TestMelee.enchantmentList(3).length);
		assertEquals(9, TestMelee.enchantmentList(4).length);

		assertEquals(3, TestMelee.clampSelection(8, 4));
		assertEquals(0, TestMelee.clampSelection(8, 0));
	}

	@Test
	public void scrollPanesAndFollowingControlsUseOneContinuousVerticalLayout() {
		TestMelee.SelectionLayout layout = TestMelee.selectionLayout(8, 8);

		assertEquals(28f, layout.weaponPaneTop, 0f);
		assertEquals(62f, layout.selectedWeaponTop, 0f);
		assertEquals(72f, layout.levelTop, 0f);
		assertEquals(98f, layout.enchantRarityTop, 0f);
		assertEquals(124f, layout.enchantPaneTop, 0f);
		assertEquals(174f, layout.enchantInfoTop, 0f);
		assertEquals(184f, layout.actionsTop, 0f);
		assertEquals(202, layout.windowHeight);
	}
}
