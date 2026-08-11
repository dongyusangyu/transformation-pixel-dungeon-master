/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class SkilledParryCooldown extends FlavourBuff {

	public static final float DURATION = 30f;

	private static final String ABILITY = "ability";

	public Class<?> ability;

	{
		type = buffType.NEUTRAL;
	}

	public static boolean isDifferentAbility(Class<?> previous, Class<?> current) {
		return current != null
				&& MeleeWeapon.abilityType(previous) != MeleeWeapon.abilityType(current);
	}

	@Override
	public int icon() {
		return BuffIndicator.SKILLED_PARRY_COOLDOWN;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ABILITY, ability);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		ability = bundle.getClass(ABILITY);
	}
}
