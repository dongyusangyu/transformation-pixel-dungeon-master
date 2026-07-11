package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

public class TalentOozeAttackTest {

	@Test
	public void oozeAttackVisualToleratesACharacterWithoutAnActiveSprite() {
		Talent.showOozeAttackEffect(new SpriteLessChar());
	}

	private static class SpriteLessChar extends Char {
		@Override
		protected boolean act() {
			return true;
		}
	}
}
