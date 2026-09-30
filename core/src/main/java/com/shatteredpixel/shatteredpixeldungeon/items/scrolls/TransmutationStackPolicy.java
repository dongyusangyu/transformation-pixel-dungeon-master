package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;

final class TransmutationStackPolicy {

	private TransmutationStackPolicy() {
	}

	static boolean removesEntireStack(Item item) {
		return item instanceof MissileWeapon && !(item instanceof TippedDart);
	}
}
