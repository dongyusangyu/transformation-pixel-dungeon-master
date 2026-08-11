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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class ArcaneConfluence extends FlavourBuff {

	public static final float DURATION = 10f;

	private static final String POINTS = "points";

	private int points;

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public static void start(Hero hero) {
		if (hero == null || hero.buff(ArcaneConfluence.class) != null) return;
		int points = hero.pointsInTalent(Talent.ARCANE_CONFLUENCE);
		if (points > 0) {
			ArcaneConfluence buff = Buff.affect(hero, ArcaneConfluence.class, DURATION);
			buff.points = points;
		}
	}

	public static void cancel(Hero hero) {
		if (hero != null) {
			ArcaneConfluence buff = hero.buff(ArcaneConfluence.class);
			if (buff != null) buff.detach();
		}
	}

	public static float chargeAmount(int points) {
		switch (points) {
			case 1: return 1f;
			case 2: return 1.5f;
			case 3: return 2f;
			default: return 0f;
		}
	}

	public static float overflowEfficiency(int points) {
		switch (points) {
			case 1: return 1f;
			case 2: return 1.25f;
			case 3: return 1.5f;
			default: return 0f;
		}
	}

	@Override
	public boolean act() {
		if (target instanceof Hero) restoreCharges((Hero) target);
		return super.act();
	}

	private void restoreCharges(Hero hero) {
		MagesStaff staff = hero.belongings.getItem(MagesStaff.class);
		Wand staffWand = staff == null ? null : staff.wand();
		if (staffWand == null) return;

		float amount = chargeAmount(points);
		float current = Math.min(staffWand.maxCharges,
				staffWand.curCharges + staffWand.partialCharge);
		float accepted = Math.min(amount, Math.max(0f, staffWand.maxCharges - current));
		staffWand.gainCharge(accepted);

		float overflow = amount - accepted;
		if (overflow > 0f) {
			float sharedCharge = overflow * overflowEfficiency(points);
			for (Wand wand : hero.belongings.getAllItems(Wand.class)) {
				wand.gainCharge(sharedCharge);
			}
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.ARCANE_CONFLUENCE;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(POINTS, points);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		points = bundle.getInt(POINTS);
	}
}
