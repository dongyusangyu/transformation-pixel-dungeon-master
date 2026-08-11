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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class AlternatingWeapons extends FlavourBuff {

	public static final float DURATION = 5f;

	private static final String BONUS = "bonus";

	private float bonus;

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public static float initialBonus() {
		return 0.1f;
	}

	public static float maxBonus(int points) {
		return points <= 0 ? 0f : 0.1f + 0.2f * points;
	}

	public static float nextBonus(float current, int points) {
		if (points <= 0) return current;
		return Math.min(maxBonus(points), current + 0.1f * points);
	}

	public static float damageMultiplier(float bonus) {
		return 1f + Math.max(0f, bonus);
	}

	public float damageMultiplier() {
		return damageMultiplier(bonus);
	}

	public static void recordPrimaryAttack(Hero hero, KindOfWeapon weapon) {
		if (hero == null
				|| !hero.hasTalent(Talent.ALTERNATING_WEAPONS)
				|| weapon == null
				|| weapon != hero.belongings.weapon()) {
			return;
		}
		AttackTracker tracker = Buff.affect(hero, AttackTracker.class);
		tracker.expiresAt = Actor.now() + Math.max(0f, hero.attackDelay());
	}

	public static boolean consumePrimaryAttack(Hero hero) {
		AttackTracker tracker = hero == null ? null : hero.buff(AttackTracker.class);
		if (tracker == null) return false;
		boolean ready = Actor.now() <= tracker.expiresAt + 0.0001f;
		tracker.detach();
		return ready;
	}

	public static void trigger(Hero hero) {
		int points = hero.pointsInTalent(Talent.ALTERNATING_WEAPONS);
		if (points <= 0) return;

		AlternatingWeapons current = hero.buff(AlternatingWeapons.class);
		float next = current == null ? initialBonus() : nextBonus(current.bonus, points);
		if (current != null) current.detach();

		AlternatingWeapons refreshed = Buff.affect(hero, AlternatingWeapons.class, DURATION);
		refreshed.bonus = next;
		BuffIndicator.refreshHero();
	}

	@Override
	public int icon() {
		return BuffIndicator.ALTERNATING_WEAPONS;
	}

	@Override
	public String iconTextDisplay() {
		return "+" + Math.round(bonus * 100) + "%";
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(), Math.round(bonus * 100));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BONUS, bonus);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		bonus = bundle.getFloat(BONUS);
	}

	public static class AttackTracker extends Buff {

		private static final String EXPIRES_AT = "expires_at";

		private float expiresAt;

		@Override
		public boolean act() {
			if (Actor.now() > expiresAt + 0.0001f) {
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(EXPIRES_AT, expiresAt);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			expiresAt = bundle.getFloat(EXPIRES_AT);
		}
	}
}
