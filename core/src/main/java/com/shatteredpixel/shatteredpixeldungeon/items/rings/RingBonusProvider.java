package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

public interface RingBonusProvider {

	int copiedRingBonus(Class<? extends Ring.RingBuff> type, boolean buffed);

	static int addCopiedBonus(Char target, Class<? extends Ring.RingBuff> type,
			boolean buffed, int bonus) {
		if (target instanceof RingBonusProvider) {
			bonus += ((RingBonusProvider) target).copiedRingBonus(type, buffed);
		}
		return bonus;
	}
}
