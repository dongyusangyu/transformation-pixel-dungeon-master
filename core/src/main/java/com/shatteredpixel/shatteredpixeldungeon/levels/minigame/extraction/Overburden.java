/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Overburden extends Buff {

	private static final float RETENTION_PER_SLOT = 2f / 3f;

	{
		type = buffType.NEUTRAL;
		announced = true;
		revivePersists = true;
	}

	public static Overburden ensureAttached(Hero hero) {
		if (hero == null) return null;
		Overburden existing = hero.buff(Overburden.class);
		if (existing != null) return existing;
		Overburden buff = new Overburden();
		return buff.attachTo((Char) hero) ? buff : null;
	}

	public static void detachFrom(Hero hero) {
		if (hero == null) return;
		Overburden buff = hero.buff(Overburden.class);
		if (buff != null) buff.detach();
	}

	public static int countSlots(Hero hero) {
		return hero == null || hero.belongings == null
				? 0
				: countSlots(hero.belongings.backpack);
	}

	public static int countSlots(Bag bag) {
		if (bag == null) return 0;
		int count = 0;
		for (Item item : bag.items) {
			if (item instanceof Bag) {
				count += countSlots((Bag) item);
			} else if (!(item instanceof Waterskin)) {
				count++;
			}
		}
		return count;
	}

	public static float retentionForSlots(int slots) {
		return (float) Math.pow(RETENTION_PER_SLOT, Math.max(0, slots));
	}

	public static float attenuatePositiveMultiplier(float multiplier, float retention) {
		if (multiplier <= 1f) return multiplier;
		float boundedRetention = Math.max(0f, Math.min(1f, retention));
		return 1f + (multiplier - 1f) * boundedRetention;
	}

	public static float attenuateEquipmentSpeed(Char owner, float multiplier) {
		if (multiplier <= 1f
				|| !(owner instanceof Hero)
				|| owner.buff(Overburden.class) == null) {
			return multiplier;
		}
		return attenuatePositiveMultiplier(
				multiplier,
				retentionForSlots(countSlots((Hero) owner)));
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public int icon() {
		float retention = target instanceof Hero
				? retentionForSlots(countSlots((Hero) target))
				: 1f;
		return iconForRetention(retention);
	}

	public static int iconForRetention(float retention) {
		if (retention >= 0.5f) return BuffIndicator.HASTE;
		if (retention >= 0.2f) return BuffIndicator.CRIPPLE;
		return BuffIndicator.TIME;
	}

	@Override
	public String iconTextDisplay() {
		return target instanceof Hero ? Integer.toString(countSlots((Hero) target)) : "0";
	}

	@Override
	public String desc() {
		int slots = target instanceof Hero ? countSlots((Hero) target) : 0;
		int percent = Math.round(100f * retentionForSlots(slots));
		return Messages.get(this, "desc", slots, percent);
	}
}
