/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

public class SoulMark extends FlavourBuff {

	public static final float DURATION	= 10f;
	private static final float[] CHANCE_BONUS = {0f, 0.08f, 0.17f, 0.25f};
	private static final float[] DURATION_BONUS = {0f, 5f, 8f, 10f};
	private static final float[] DAMAGE_MULTIPLIER = {1f, 0.9f, 0.85f, 0.8f};

	public static float markChance(int wandLevel, int chargesUsed, int points) {
		float baseChance = 1.07f - (float) Math.pow(0.92f, wandLevel * chargesUsed + 1);
		return Math.min(1f, baseChance + CHANCE_BONUS[Math.max(0, Math.min(3, points))]);
	}

	public static float markDuration(int wandLevel, int points) {
		return DURATION + wandLevel + DURATION_BONUS[Math.max(0, Math.min(3, points))];
	}

	public static int healingAmount(int restoration, boolean heroAttack, int points) {
		int healing = (int) Math.ceil(restoration * 0.4f);
		return healing + (heroAttack ? Math.max(0, Math.min(3, points)) : 0);
	}

	public static boolean blocksRangedAttack(boolean marked, boolean boss,
			boolean targetsHero, int points) {
		return marked && !boss && targetsHero && points > 0;
	}

	public static int reducePhysicalDamage(int damage, boolean marked, boolean boss, int points) {
		if (!marked || boss || points <= 0) return damage;
		return Math.round(damage * DAMAGE_MULTIPLIER[Math.min(3, points)]);
	}

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.INVERT_MARK;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.5f, 0.2f, 1f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.MARKED);
		else target.sprite.remove(CharSprite.State.MARKED);
	}
}
