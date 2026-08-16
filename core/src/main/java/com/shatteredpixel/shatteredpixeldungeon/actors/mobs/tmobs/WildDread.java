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
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.WildDreadSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class WildDread extends Mob implements com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff.NoConversionRewards {

	private int ownerId = -1;
	private boolean downed;
	private int deathCell = -1;
	private static final int MAX_ATTACK_GROWTH = 4;
	private int completedAttackGrowth;

	private static final String OWNER_ID = "owner_id";
	private static final String DOWNED = "downed";
	private static final String DEATH_CELL = "death_cell";
	private static final String COMPLETED_ATTACK_GROWTH = "completed_attack_growth";

	{
		spriteClass = WildDreadSprite.class;
		HP = HT = 21;
		defenseSkill = 20;

		EXP = 0;
		maxLvl = 30;

		loot = null;
		lootChance = 0f;

		properties.add(Property.DEMONIC);
		properties.add(Property.INORGANIC);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 45);
	}

	@Override
	public int attackSkill(Char target) {
		return 50;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 10);
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * (5f - completedAttackGrowth);
	}

	protected int completedAttackGrowth() {
		return completedAttackGrowth;
	}

	private void completeAttackGrowth() {
		completedAttackGrowth = Math.min(MAX_ATTACK_GROWTH, completedAttackGrowth + 1);
	}

	@Override
	protected boolean doAttack(Char enemy) {
		boolean completed = super.doAttack(enemy);
		if (completed) {
			completeAttackGrowth();
		}
		return completed;
	}

	@Override
	public void onAttackComplete() {
		super.onAttackComplete();
		completeAttackGrowth();
	}

	@Override
	public float lootChance() {
		return 0f;
	}

	void bindToOwner(int ownerId) {
		this.ownerId = ownerId;
	}

	protected int ownerId() {
		return ownerId;
	}

	protected Obscura owner() {
		Actor actor = Actor.findById(ownerId);
		if (actor instanceof Obscura) {
			return (Obscura) actor;
		}
		Char held = stasisAlly();
		return held instanceof Obscura && held.id() == ownerId ? (Obscura) held : null;
	}

	protected Char stasisAlly() {
		return Stasis.getStasisAlly();
	}

	protected boolean downed() {
		return downed;
	}

	int deathCell() {
		return deathCell;
	}

	@Override
	protected boolean act() {
		Obscura owner = owner();
		if (owner == null || !owner.isAlive()) {
			abandonWithOwner();
			return true;
		}
		return performBaseAct();
	}

	protected boolean performBaseAct() {
		return super.act();
	}

	@Override
	public void die(Object cause) {
		if (downed) {
			return;
		}
		Obscura owner = owner();
		if (cause == Chasm.class) {
			diePermanently(cause);
			return;
		}
		if (owner == null || !owner.isAlive()) {
			abandonWithOwner();
			return;
		}

		HP = 0;
		downed = true;
		deathCell = pos;
		owner.scheduleRevival(this);
		removeForRevival();
	}

	protected void removeForRevival() {
		Actor.remove(this);
		if (Dungeon.level != null) {
			Dungeon.level.mobs.remove(this);
		}
		if (sprite instanceof WildDreadSprite) {
			((WildDreadSprite) sprite).down();
		}
	}

	void reviveAt(int cell, Alignment ownerAlignment) {
		pos = cell;
		HP = HT;
		downed = false;
		deathCell = -1;
		alignment = ownerAlignment;
		state = WANDERING;
	}

	void diePermanently(Object cause) {
		downed = false;
		HP = 0;
		finishPermanentDeath(cause);
	}

	void abandonWithOwner() {
		downed = false;
		HP = 0;
		// Mob.destroy() grants kill credit to ENEMY mobs. This is an owner-linked
		// cleanup, not another player kill, so neutralize it before removal.
		alignment = Alignment.NEUTRAL;
		finishAbandonment();
	}

	protected void finishPermanentDeath(Object cause) {
		super.die(cause);
	}

	protected void finishAbandonment() {
		destroy();
		if (sprite != null) {
			sprite.die();
		}
	}

	@Override
	public boolean isAlive() {
		return downed || super.isAlive();
	}

	@Override
	public boolean isActive() {
		return !downed && super.isActive();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(OWNER_ID, ownerId);
		bundle.put(DOWNED, downed);
		bundle.put(DEATH_CELL, deathCell);
		bundle.put(COMPLETED_ATTACK_GROWTH, completedAttackGrowth);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		ownerId = bundle.getInt(OWNER_ID);
		downed = bundle.getBoolean(DOWNED);
		deathCell = bundle.getInt(DEATH_CELL);
		completedAttackGrowth = Math.max(0, Math.min(MAX_ATTACK_GROWTH,
				bundle.getInt(COMPLETED_ATTACK_GROWTH)));
	}
}
