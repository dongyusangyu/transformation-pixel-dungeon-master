package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.watabou.utils.RectF;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HeroSelectSceneTest {

	@Test
	public void bottomSafeOffsetOnlyAppliesWhenNavigationInsetExists() {
		assertEquals(0f, HeroSelectScene.bottomSafeOffset(new RectF(0, 0, 0, 0)), 0f);
		assertEquals(26f, HeroSelectScene.bottomSafeOffset(new RectF(0, 0, 0, 24)), 0f);
	}
}
