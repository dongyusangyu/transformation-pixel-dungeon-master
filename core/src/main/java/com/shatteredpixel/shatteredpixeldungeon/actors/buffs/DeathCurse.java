/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DeathButterfly;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class DeathCurse extends Buff {

	public static final float DURATION = 10f;

	private static final String SOURCE_ID = "source_id";
	private static final String REMAINING = "remaining";

	private int sourceId = -1;
	private float remaining = DURATION;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	/**
	 * Death curses cannot affect boss or miniboss targets. Keep this rule here
	 * so both new applications and curses restored from older saves agree.
	 */
	public static boolean canAffect(Char target) {
		return target != null
				&& !Char.hasProp(target, Char.Property.BOSS)
				&& !Char.hasProp(target, Char.Property.MINIBOSS);
	}

	public static DeathCurse apply(Char target, DeathButterfly source) {
		if (!canAffect(target) || source == null || target.buff(DeathCurse.class) != null) {
			return null;
		}
		DeathCurse curse = new DeathCurse();
		curse.sourceId = source.id();
		if (!curse.attachTo(target)) {
			return null;
		}
		curse.remaining = DURATION;
		curse.spend(TICK);
		return curse;
	}

	public int sourceId() {
		return sourceId;
	}

	public boolean belongsTo(int actorId) {
		return sourceId == actorId;
	}

	public float remaining() {
		return remaining;
	}

	@Override
	public boolean act() {
		if (!canAffect(target)) {
			detach();
			return true;
		}

		Actor source = Actor.findById(sourceId);
		if (!(source instanceof DeathButterfly) || !((DeathButterfly) source).isAlive()) {
			detach();
			return true;
		}

		remaining = Math.max(0f, remaining - TICK);
		if (remaining <= 0f) {
			expireNaturally();
		} else {
			spend(TICK);
		}
		return true;
	}

	void expireNaturally() {
		Char victim = target;
		if (!canAffect(victim)) {
			detach();
			return;
		}

		if (victim.sprite != null) {
			victim.sprite.emitter().burst(ShadowParticle.UP, 10);
			Sample.INSTANCE.play(Assets.Sounds.CURSED);
		}
		detach();

		if (!victim.isAlive()) {
			return;
		}
		if (victim instanceof Hero) {
			victim.damage(GrimTrap.grimDamage(victim), this, DamageTag.MAGICAL);
			if (!victim.isAlive()) {
				Badges.validateDeathFromGrimOrDisintTrap();
				Dungeon.fail(this);
				GLog.n(Messages.get(this, "ondeath"));
			}
		} else {
			victim.HP = 0;
			victim.die(this);
		}
	}

	public static void clearForSource(int actorId) {
		for (Char ch : Actor.chars().toArray(new Char[0])) {
			for (DeathCurse curse : ch.buffs(DeathCurse.class).toArray(new DeathCurse[0])) {
				if (curse.belongsTo(actorId)) {
					curse.detach();
				}
			}
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0x76518F);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0f, (DURATION - remaining) / DURATION);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(Math.max(0, (int) Math.ceil(remaining)));
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(remaining));
	}

	@Override
	public void fx(boolean on) {
		if (on && target != null && target.sprite != null) {
			target.sprite.emitter().burst(ShadowParticle.CURSE, 6);
			Sample.INSTANCE.play(Assets.Sounds.CURSED);
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SOURCE_ID, sourceId);
		bundle.put(REMAINING, remaining);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		sourceId = bundle.getInt(SOURCE_ID);
		remaining = bundle.contains(REMAINING) ? bundle.getFloat(REMAINING) : DURATION;
	}
}
