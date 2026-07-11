package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WndSkinsTest {

	@Test
	public void skinNameKeyUsesHeroClassAndSkinNumber() {
		assertEquals("skin_warrior_1", WndSkins.skinNameKey(HeroClass.WARRIOR, 1));
		assertEquals("skin_dm400_2", WndSkins.skinNameKey(HeroClass.DM400, 2));
	}

	@Test
	public void fallbackSkinNameKeepsOriginalNumberedFormat() {
		assertEquals("皮肤3", WndSkins.fallbackSkinName(3));
	}
}
