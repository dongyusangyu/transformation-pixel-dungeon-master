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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class SkilledParry extends FlavourBuff {

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public static float duration(int points) {
		return Math.max(0, points) + 1f;
	}

	public static void trigger(Hero hero, MeleeWeapon weapon) {
		int points = hero.pointsInTalent(Talent.SKILLED_PARRY);
		if (points <= 0 || weapon == null) return;

		Class<?> abilityType = weapon.abilityType();
		SkilledParryCooldown cooldown = hero.buff(SkilledParryCooldown.class);
		if (cooldown != null
				&& !SkilledParryCooldown.isDifferentAbility(cooldown.ability, abilityType)) {
			return;
		}

		SkilledParry active = hero.buff(SkilledParry.class);
		if (active != null) active.detach();
		Buff.affect(hero, SkilledParry.class, duration(points));

		if (cooldown != null) cooldown.detach();
		cooldown = Buff.affect(hero, SkilledParryCooldown.class, SkilledParryCooldown.DURATION);
		cooldown.ability = abilityType;
		BuffIndicator.refreshHero();
	}

	public static boolean blocks(Hero hero, Char attacker) {
		return hero != null
				&& attacker != null
				&& attacker.alignment == Char.Alignment.ENEMY
				&& hero.buff(SkilledParry.class) != null;
	}

	@Override
	public int icon() {
		return BuffIndicator.SKILLED_PARRY;
	}
}
