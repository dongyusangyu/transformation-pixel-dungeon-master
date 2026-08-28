package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPotionSelectionLayoutTest {

    @Test
    public void itemGridGrowsBeyondViewportWithoutChangingViewportHeight() {
        assertEquals(64f, TestPotion.itemGridContentHeight(28), 0f);
        assertEquals(80f, TestPotion.itemGridContentHeight(29), 0f);
        assertEquals(64f, TestPotion.itemPaneHeight(), 0f);
    }

    @Test
    public void lastItemRemainsClickableAfterScrollingToItsRow() {
        assertEquals(28, TestPotion.itemGridIndexAt(1, 64, 29));
        assertEquals(-1, TestPotion.itemGridIndexAt(112, 64, 29));
        assertEquals(-1, TestPotion.itemGridIndexAt(1, 80, 29));
    }

    @Test
    public void selectionIsClampedWhenCategoryHasFewerItems() {
        assertEquals(2, TestPotion.clampSelection(28, 3));
        assertEquals(0, TestPotion.clampSelection(28, 0));
    }
}
