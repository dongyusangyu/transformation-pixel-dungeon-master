package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RingBonusProviderTest {

	@Test
	public void ringHelpersIncludeCopiedProviderBonus() {
		ProviderChar target = new ProviderChar();

		assertEquals(8, RingBonusProvider.addCopiedBonus(target, null, false, 5));
		assertEquals(9, RingBonusProvider.addCopiedBonus(target, null, true, 5));
	}

	@Test
	public void charsWithoutProviderKeepOriginalBonus() {
		Char target = new Char() {
		};

		assertEquals(5, RingBonusProvider.addCopiedBonus(target, null, false, 5));
		assertEquals(5, RingBonusProvider.addCopiedBonus(target, null, true, 5));
	}

	private static final class ProviderChar extends Char implements RingBonusProvider {

		@Override
		public int copiedRingBonus(Class<? extends Ring.RingBuff> type, boolean buffed) {
			return buffed ? 4 : 3;
		}
	}
}
