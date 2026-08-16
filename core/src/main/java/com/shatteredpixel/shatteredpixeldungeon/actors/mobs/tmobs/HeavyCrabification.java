/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.HeavyCrabificationSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class HeavyCrabification extends Mob {

	public static final int MOLT_INTERVAL = 50;
	public static final int MOLT_HEALING = 30;

	private static final String MOLT_TURNS = "molt_turns";

	private float moltProgress;
	private float lastMoltTime = Float.NaN;

	{
		spriteClass = HeavyCrabificationSprite.class;

		HP = HT = 150;
		defenseSkill = 20;
		baseSpeed = 0.5f;

		EXP = 13;
		maxLvl = 30;

		loot = MysteryMeat.class;
		lootChance = 0.5f;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 45);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 10);
	}

	@Override
	public int defenseSkill(Char attacker) {
		if (isVulnerableAttack(attacker)) {
			return 0;
		}
		if (canGuardFront(attacker)) {
			showShellBlock();
			return INFINITE_EVASION;
		}
		return super.defenseSkill(attacker);
	}

	protected boolean canGuardFront(Char attacker) {
		return attacker != null
				&& enemySeen
				&& state != SLEEPING
				&& paralysed == 0
				&& attacker == enemy
				&& attacker.invisible == 0;
	}

	protected boolean isVulnerableAttack(Char attacker) {
		return attacker != null
				&& (isAmbushAttack(attacker) || isFlankingAttack(attacker));
	}

	protected boolean isAmbushAttack(Char attacker) {
		return surprisedBy(attacker);
	}

	protected boolean isFlankingAttack(Char attacker) {
		return state == HUNTING && enemy != null && attacker != enemy;
	}

	@Override
	public void damage(int damage, Object source, DamageTag... damageTags) {
		if (DamageTag.of(damageTags).contains(DamageTag.CORRUPTION)
				|| !canBlockDamage(source, damageTags)) {
			applyAllowedDamage(damage, source, damageTags);
		} else if (source instanceof Char && isVulnerableAttack((Char) source)) {
			int reducedDamage = damage <= 0 ? 0 : damage / 2 + damage % 2;
			applyAllowedDamage(reducedDamage, source, damageTags);
		} else {
			showShellBlock();
		}
	}

	protected boolean canBlockDamage(Object source, DamageTag... damageTags) {
		if (source instanceof Char) {
			return true;
		}
		return DamageTag.of(damageTags).contains(DamageTag.MAGICAL)
				&& !isSourceType(source, Buff.class)
				&& !isSourceType(source, Blob.class)
				&& !isSourceType(source, Trap.class);
	}

	private boolean isSourceType(Object source, Class<?> type) {
		Class<?> sourceClass = source instanceof Class ? (Class<?>) source
				: source == null ? null : source.getClass();
		return sourceClass != null && type.isAssignableFrom(sourceClass);
	}

	protected void applyAllowedDamage(int damage, Object source, DamageTag... damageTags) {
		super.damage(damage, source, damageTags);
	}

	protected void showShellBlock() {
		if (sprite != null && sprite.visible) {
			sprite.showStatus(CharSprite.NEUTRAL, Messages.get(this, "def_verb"));
			Sample.INSTANCE.play(
					Assets.Sounds.HIT_PARRY,
					1f,
					Random.Float(0.96f, 1.05f));
		}
	}

	@Override
	protected boolean act() {
		float elapsed = elapsedSinceLastAct();
		if (canAdvanceMolt()) {
			moltProgress += elapsed;
			if (moltProgress >= MOLT_INTERVAL) {
				moltProgress = Math.max(0f, moltProgress - MOLT_INTERVAL);
				performMolt();
				spend(TICK);
				return true;
			}
		} else if (shouldResetMolt()) {
			moltProgress = 0f;
		}
		return performBaseAct();
	}

	protected float elapsedSinceLastAct() {
		float now = Actor.now();
		float elapsed = Float.isNaN(lastMoltTime)
				? 0f
				: Math.max(0f, now - lastMoltTime);
		lastMoltTime = now;
		return elapsed;
	}

	protected boolean canAdvanceMolt() {
		return state == WANDERING && paralysed == 0 && !enemySeen;
	}

	protected boolean shouldResetMolt() {
		return state == HUNTING || enemySeen;
	}

	protected void performMolt() {
		HP = Math.min(HT, HP + MOLT_HEALING);
		if (sprite != null && sprite.visible) {
			sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "molt"));
		}
	}

	protected boolean performBaseAct() {
		return super.act();
	}

	protected int moltTurns() {
		return (int) Math.floor(moltProgress);
	}

	protected void setMoltTurns(int turns) {
		setMoltProgress(turns);
	}

	private void setMoltProgress(float progress) {
		moltProgress = Math.max(0f, Math.min(progress, MOLT_INTERVAL - 1f));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(MOLT_TURNS, moltProgress);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		setMoltProgress(bundle.getFloat(MOLT_TURNS));
		lastMoltTime = Float.NaN;
	}
}
