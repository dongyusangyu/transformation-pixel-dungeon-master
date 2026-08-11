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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class FocusedCasting extends FlavourBuff {

	public static final float DURATION = 5f;

	private static final String LEVEL_BONUS = "level_bonus";

	private int levelBonus;

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public static void onStaffCast(Hero hero) {
		if (hero == null) return;
		int points = hero.pointsInTalent(Talent.FOCUSED_CASTING);
		if (points <= 0) return;
		FocusedCasting buff = Buff.prolong(hero, FocusedCasting.class, DURATION);
		buff.levelBonus = Math.min(maxLevelBonus(points), buff.levelBonus + 1);
	}

	public int levelBonus() {
		return levelBonus;
	}

	public static int maxLevelBonus(int points) {
		return points <= 0 ? 0 : points + 1;
	}

	public static float chargeCost(int nominalCost) {
		return nominalCost * 0.5f;
	}

	public static boolean canSpendCharge(int current, float partial, int nominalCost) {
		return current + partial + 0.0001f >= chargeCost(nominalCost);
	}

	public static float remainingCharge(int current, float partial, int nominalCost) {
		return Math.max(0f, current + partial - chargeCost(nominalCost));
	}

	@Override
	public int icon() {
		return BuffIndicator.FOCUSED_CASTING;
	}

	@Override
	public String iconTextDisplay() {
		return "+" + levelBonus;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", levelBonus, dispTurns());
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEVEL_BONUS, levelBonus);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		levelBonus = bundle.getInt(LEVEL_BONUS);
	}
}
