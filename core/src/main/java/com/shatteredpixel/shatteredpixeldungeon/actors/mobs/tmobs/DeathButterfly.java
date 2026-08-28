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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DeathCurse;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.DeathButterflySprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class DeathButterfly extends Mob {

	private static final float CURSE_CHANCE = 0.5f;
	private static final String CURSED_TARGET_ID = "cursed_target_id";
	private static final String CORNERED_COUNTERATTACK = "cornered_counterattack";
	private static final String CORNERED_COUNTERATTACK_USED = "cornered_counterattack_used";
	private static final String CORNERED_AT_POS = "cornered_at_pos";

	private int cursedTargetId = -1;
	private transient Char cursedTarget;
	private boolean corneredCounterattack;
	private boolean corneredCounterattackUsed;
	private int corneredAtPos = -1;

	{
		spriteClass = DeathButterflySprite.class;

		HP = HT = 120;
		defenseSkill = 30;
		flying = true;

		EXP = 13;
		maxLvl = 30;

		loot = PotionOfHealing.class;
		lootChance = 1f / 8f;

		properties.add(Property.UNDEAD);

		FLEEING = new DeathFleeing();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(10, 20);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 15);
	}

	@Override
	public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
		damage = super.attackProc(enemy, damage, damageTags);
		processCurseOnHit(enemy);
		return damage;
	}

	@Override
	protected boolean act() {
		boolean counterattacking = reconcileCurseBeforeAction();
		boolean result = super.act();
		finishCurseBoundAction(counterattacking);
		return result;
	}

	protected void processCurseOnHit(Char enemy) {
		if (!DeathCurse.canAffect(enemy)) {
			return;
		}
		if (isFleeingFrom(resolveCursedTarget())) {
			return;
		}
		if (enemy.buff(DeathCurse.class) == null && rollCurse()) {
			DeathCurse curse = DeathCurse.apply(enemy, this);
			if (curse != null) {
				resetCorneredCounterattack();
				cursedTarget = enemy;
				cursedTargetId = enemy.id();
				this.enemy = enemy;
				target = enemy.pos;
				state = FLEEING;
			}
		}
	}

	protected boolean rollCurse() {
		return curseRollSucceeds(Random.Float());
	}

	static boolean curseRollSucceeds(float roll) {
		return roll < CURSE_CHANCE;
	}

	protected boolean isFleeingFrom(Char target) {
		DeathCurse curse = target == null ? null : target.buff(DeathCurse.class);
		return curse != null && curse.belongsTo(id()) && target.isAlive();
	}

	protected void refreshFleeingState() {
		Char target = resolveCursedTarget();
		if (!isFleeingFrom(target)) {
			resetCorneredCounterattack();
			cursedTarget = null;
			cursedTargetId = -1;
			if (state == FLEEING) {
				state = HUNTING;
			}
		}
	}

	protected boolean reconcileCurseBeforeAction() {
		if (corneredCounterattackUsed && corneredAtPos != pos) {
			resetCorneredCounterattack();
		}
		Char target = resolveCursedTarget();
		if (corneredCounterattack) {
			if (isFleeingFrom(target)) {
				return true;
			}
			resetCorneredCounterattack();
		}
		if (isFleeingFrom(target)) {
			if (state != FLEEING) {
				enemy = target;
				this.target = target.pos;
				state = FLEEING;
			}
		} else {
			refreshFleeingState();
		}
		return false;
	}

	protected boolean markCorneredCounterattack() {
		if (!isFleeingFrom(resolveCursedTarget())
				|| corneredCounterattackUsed && corneredAtPos == pos) {
			state = FLEEING;
			return false;
		}
		corneredCounterattackUsed = true;
		corneredAtPos = pos;
		corneredCounterattack = true;
		state = HUNTING;
		return true;
	}

	protected void finishCurseBoundAction(boolean counterattacking) {
		if (!counterattacking) {
			return;
		}
		corneredCounterattack = false;
		Char target = resolveCursedTarget();
		if (isFleeingFrom(target)) {
			enemy = target;
			this.target = target.pos;
			state = FLEEING;
		} else {
			refreshFleeingState();
		}
	}

	private void resetCorneredCounterattack() {
		corneredCounterattack = false;
		corneredCounterattackUsed = false;
		corneredAtPos = -1;
	}

	protected int cursedTargetId() {
		return cursedTargetId;
	}

	private Char resolveCursedTarget() {
		if (cursedTarget != null && cursedTarget.id() == cursedTargetId) {
			return cursedTarget;
		}
		return Actor.findCharById(cursedTargetId);
	}

	protected void clearOwnedCurses() {
		if (cursedTarget != null) {
			DeathCurse curse = cursedTarget.buff(DeathCurse.class);
			if (curse != null && curse.belongsTo(id())) {
				curse.detach();
			}
		}
		DeathCurse.clearForSource(id());
		cursedTarget = null;
		cursedTargetId = -1;
		resetCorneredCounterattack();
	}

	@Override
	public void destroy() {
		clearOwnedCurses();
		super.destroy();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CURSED_TARGET_ID, cursedTargetId);
		bundle.put(CORNERED_COUNTERATTACK, corneredCounterattack);
		bundle.put(CORNERED_COUNTERATTACK_USED, corneredCounterattackUsed);
		bundle.put(CORNERED_AT_POS, corneredAtPos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		cursedTargetId = bundle.getInt(CURSED_TARGET_ID);
		cursedTarget = null;
		corneredCounterattack = bundle.getBoolean(CORNERED_COUNTERATTACK);
		corneredCounterattackUsed = bundle.getBoolean(CORNERED_COUNTERATTACK_USED);
		corneredAtPos = bundle.contains(CORNERED_AT_POS) ? bundle.getInt(CORNERED_AT_POS) : -1;
	}

	private class DeathFleeing extends Mob.Fleeing {
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			refreshFleeingState();
			if (state != FLEEING) {
				return true;
			}
			return super.act(enemyInFOV, justAlerted);
		}

		@Override
		protected void nowhereToRun() {
			if (corneredCounterattackUsed && corneredAtPos == pos) {
				return;
			}
			super.nowhereToRun();
			if (state == HUNTING && isFleeingFrom(resolveCursedTarget())) {
				markCorneredCounterattack();
			}
		}
	}
}
