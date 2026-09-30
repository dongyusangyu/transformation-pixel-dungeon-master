package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.utils.RectF;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MenuPaneLayoutTest {

	@Test
	public void dangerBoundsAreStableAcrossRepeatedLayoutPasses() {
		RectF first = MenuPane.dangerBounds(280f, 1f, 311f, 21f);
		RectF second = MenuPane.dangerBounds(280f, 1f, 311f, 21f);

		assertEquals(first.left, second.left, 0f);
		assertEquals(first.top, second.top, 0f);
		assertEquals(first.right, second.right, 0f);
		assertEquals(first.bottom, second.bottom, 0f);
		assertEquals(24f, first.width(), 0f);
		assertEquals(311f, first.right, 0f);
	}

	@Test
	public void dangerBoundsExpandFromFixedRightAnchorWhenMenuMovesLeft() {
		RectF bounds = MenuPane.dangerBounds(240f, 3f, 311f, 21f);

		assertEquals(247f, bounds.left, 0f);
		assertEquals(311f, bounds.right, 0f);
		assertEquals(64f, bounds.width(), 0f);
	}
}
