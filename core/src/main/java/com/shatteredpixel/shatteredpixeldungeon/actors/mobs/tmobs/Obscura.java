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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.ObscuraSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Obscura extends Mob implements Stasis.ReleaseListener {

	private boolean summonPending;
	private boolean summonConsumed;
	private int summonChargeTurns;
	private int wildDreadId = -1;
	private boolean revivalPending;
	private int revivalTurns;
	private WildDread downedDread;
	private boolean stasisReplacementPending;
	private static final int SUMMON_CHARGE_REQUIRED = 3;

	private static final String SUMMON_PENDING = "summon_pending";
	private static final String SUMMON_CONSUMED = "summon_consumed";
	private static final String SUMMON_CHARGE_TURNS = "summon_charge_turns";
	private static final String WILD_DREAD_ID = "wild_dread_id";
	private static final String REVIVAL_PENDING = "revival_pending";
	private static final String REVIVAL_TURNS = "revival_turns";
	private static final String DOWNED_DREAD = "downed_dread";
	private static final String STASIS_REPLACEMENT_PENDING = "stasis_replacement_pending";

	{
		spriteClass = ObscuraSprite.class;
		HP = HT = 150;
		defenseSkill = 30;

		EXP = 13;
		maxLvl = 30;

		loot = null;
		lootChance = 0f;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 30);
	}

	@Override
	public int attackSkill(Char target) {
		return 50;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 20);
	}

	@Override
	public float lootChance() {
		return 0f;
	}

	@Override
	protected boolean act() {
		if (paralysed > 0 || state == SLEEPING) {
			return performBaseActAndTrackFirstSight();
		}

		if (stasisReplacementPending
				&& reconcileActiveOwnedDreads() == null) {
			return performStasisReplacementTurn();
		}

		WildDread dread = ownedWildDread();
		if (needsAlignmentCleanup(dread)) {
			return performAlignmentCleanup(dread);
		}

		advanceRevival();

		if (summonPending && !summonConsumed) {
			return performSummonTurn();
		}

		return performBaseActAndTrackFirstSight();
	}

	protected boolean performBaseActAndTrackFirstSight() {
		boolean enemyWasSeen = enemySeen;
		boolean result = performBaseAct();
		if (!enemyWasSeen && enemySeen && !summonConsumed) {
			armSummon();
		}
		return result;
	}

	protected boolean performBaseAct() {
		return super.act();
	}

	protected void armSummon() {
		if (!summonConsumed && !summonPending) {
			resetSummonCharge();
			summonPending = true;
		}
	}

	protected boolean summonPending() {
		return summonPending;
	}

	protected boolean summonConsumed() {
		return summonConsumed;
	}

	protected int summonChargeTurns() {
		return summonChargeTurns;
	}

	protected int wildDreadId() {
		return wildDreadId;
	}

	protected boolean stasisReplacementPending() {
		return stasisReplacementPending;
	}

	protected boolean performSummonTurn() {
		spend(TICK);
		summonChargeTurns = Math.min(SUMMON_CHARGE_REQUIRED, summonChargeTurns + 1);
		if (summonChargeTurns < SUMMON_CHARGE_REQUIRED) {
			return true;
		}
		tryCreateWildDread();
		return true;
	}

	private boolean tryCreateWildDread() {
		int cell = findSummonCell();
		if (cell == -1) {
			return false;
		}

		WildDread dread = createWildDread();
		dread.pos = cell;
		dread.alignment = alignment;
		dread.state = dread.WANDERING;
		dread.bindToOwner(id());
		addWildDreadToLevel(dread);
		recordOwnedDread(dread);
		if (sprite instanceof ObscuraSprite) {
			((ObscuraSprite) sprite).summon();
		}
		return true;
	}

	private void resetSummonCharge() {
		summonChargeTurns = 0;
	}

	protected int findSummonCell() {
		if (Dungeon.level == null) {
			return -1;
		}
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = pos + offset;
			if (cell >= 0
					&& cell < Dungeon.level.length()
					&& Dungeon.level.passable[cell]
					&& !Dungeon.level.pit[cell]
					&& Actor.findChar(cell) == null
					&& (!Char.hasProp(this, Property.LARGE) || Dungeon.level.openSpace[cell])) {
				candidates.add(cell);
			}
		}
		return candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	protected WildDread createWildDread() {
		return new WildDread();
	}

	protected void addWildDreadToLevel(WildDread dread) {
		GameScene.add(dread);
		Dungeon.level.occupyCell(dread);
	}

	@Override
	public void onStasisReleased() {
		if (reconcileActiveOwnedDreads() != null) {
			return;
		}
		boolean replacementAlreadyPending = stasisReplacementPending;
		if (!hasValidDownedDread()) {
			clearRevival();
			wildDreadId = -1;
		}
		summonPending = false;
		if (!replacementAlreadyPending) {
			resetSummonCharge();
		}
		stasisReplacementPending = true;
	}

	protected ArrayList<WildDread> activeOwnedDreads() {
		ArrayList<WildDread> owned = new ArrayList<>();
		if (Dungeon.level == null) {
			return owned;
		}
		for (Mob mob : Dungeon.level.mobs) {
			if (mob instanceof WildDread) {
				WildDread dread = (WildDread) mob;
				if (!dread.downed() && dread.ownerId() == id()) {
					owned.add(dread);
				}
			}
		}
		return owned;
	}

	private WildDread reconcileActiveOwnedDreads() {
		ArrayList<WildDread> active = activeOwnedDreads();
		if (active.isEmpty()) {
			return null;
		}

		WildDread canonical = null;
		for (WildDread dread : active) {
			if (dread.id() == wildDreadId) {
				canonical = dread;
				break;
			}
			if (canonical == null || dread.id() < canonical.id()) {
				canonical = dread;
			}
		}
		for (WildDread dread : active) {
			if (dread != canonical) {
				dread.abandonWithOwner();
			}
		}

		if (downedDread != null && downedDread != canonical) {
			downedDread.abandonWithOwner();
			clearRevival();
		}
		recordOwnedDread(canonical);
		return canonical;
	}

	private boolean hasValidDownedDread() {
		return downedDread != null
				&& downedDread.downed()
				&& downedDread.ownerId() == id();
	}

	private void clearRevival() {
		revivalPending = false;
		revivalTurns = 0;
		downedDread = null;
	}

	protected void recordOwnedDread(WildDread dread) {
		dread.bindToOwner(id());
		wildDreadId = dread.id();
		summonConsumed = true;
		summonPending = false;
		stasisReplacementPending = false;
		resetSummonCharge();
	}

	protected boolean performStasisReplacementTurn() {
		spend(TICK);
		if (hasValidDownedDread()) {
			int cell = findReviveCell(downedDread);
			if (cell == -1) {
				return true;
			}
			WildDread dread = downedDread;
			dread.reviveAt(cell, alignment);
			addRevivedDreadToLevel(dread);
			clearRevival();
			recordOwnedDread(dread);
			return true;
		}

		clearRevival();
		summonChargeTurns = Math.min(SUMMON_CHARGE_REQUIRED, summonChargeTurns + 1);
		if (summonChargeTurns >= SUMMON_CHARGE_REQUIRED) {
			tryCreateWildDread();
		}
		return true;
	}

	void scheduleRevival(WildDread dread) {
		downedDread = dread;
		revivalPending = true;
		revivalTurns = 1;
		wildDreadId = dread.id();
	}

	protected boolean revivalPending() {
		return revivalPending;
	}

	protected WildDread downedDread() {
		return downedDread;
	}

	protected boolean advanceRevival() {
		if (!revivalPending || downedDread == null) {
			return false;
		}
		if (revivalTurns > 0) {
			revivalTurns--;
		}
		if (revivalTurns > 0) {
			return false;
		}
		int cell = findReviveCell(downedDread);
		if (cell == -1) {
			return false;
		}

		WildDread dread = downedDread;
		dread.reviveAt(cell, alignment);
		addRevivedDreadToLevel(dread);
		clearRevival();
		recordOwnedDread(dread);
		return true;
	}

	protected int findReviveCell(WildDread dread) {
		if (Dungeon.level == null) {
			return -1;
		}
		int deathCell = dread.deathCell();
		if (canReviveAt(deathCell, dread)) {
			return deathCell;
		}
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = deathCell + offset;
			if (canReviveAt(cell, dread)) {
				candidates.add(cell);
			}
		}
		return candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	private boolean canReviveAt(int cell, WildDread dread) {
		return cell >= 0
				&& cell < Dungeon.level.length()
				&& Dungeon.level.passable[cell]
				&& !Dungeon.level.pit[cell]
				&& Actor.findChar(cell) == null
				&& (!Char.hasProp(dread, Property.LARGE) || Dungeon.level.openSpace[cell]);
	}

	protected void addRevivedDreadToLevel(WildDread dread) {
		if (dread.sprite == null) {
			GameScene.addSprite(dread);
		}
		Actor.add(dread);
		dread.timeToNow();
		Dungeon.level.mobs.add(dread);
		Dungeon.level.occupyCell(dread);
		if (dread.sprite != null) {
			dread.sprite.place(dread.pos);
		}
		if (dread.sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.WildDreadSprite) {
			((com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.WildDreadSprite) dread.sprite).revive();
		} else if (dread.sprite != null) {
			dread.sprite.idle();
		}
	}

	protected WildDread ownedWildDread() {
		Actor actor = Actor.findById(wildDreadId);
		return actor instanceof WildDread ? (WildDread) actor : null;
	}

	protected boolean needsAlignmentCleanup(WildDread dread) {
		return dread != null && (dread.alignment != alignment || !dread.buffs(AllyBuff.class).isEmpty());
	}

	protected boolean performAlignmentCleanup(WildDread dread) {
		spend(TICK);
		for (AllyBuff allyBuff : dread.buffs(AllyBuff.class)) {
			allyBuff.detach();
		}
		dread.alignment = alignment;
		dread.updateSpriteState();
		return true;
	}

	@Override
	public void die(Object cause) {
		LinkedHashSet<WildDread> cleanup = new LinkedHashSet<>(activeOwnedDreads());
		WildDread living = ownedWildDread();
		if (living != null) {
			cleanup.add(living);
		}
		if (downedDread != null) {
			cleanup.add(downedDread);
		}
		WildDread heldDread = null;
		Char held = stasisAlly();
		if (held instanceof WildDread && ((WildDread) held).ownerId() == id()) {
			heldDread = (WildDread) held;
			cleanup.add(heldDread);
		}
		for (WildDread dread : cleanup) {
			dread.abandonWithOwner();
		}
		if (heldDread != null) {
			discardHeldStasisAlly(heldDread);
		}
		revivalPending = false;
		revivalTurns = 0;
		downedDread = null;
		wildDreadId = -1;
		summonPending = false;
		stasisReplacementPending = false;
		resetSummonCharge();
		finishOwnerDeath(cause);
	}

	protected Char stasisAlly() {
		return Stasis.getStasisAlly();
	}

	protected void discardHeldStasisAlly(WildDread expected) {
		Stasis.discardHeldAlly(expected);
	}

	protected void finishOwnerDeath(Object cause) {
		super.die(cause);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SUMMON_PENDING, summonPending);
		bundle.put(SUMMON_CONSUMED, summonConsumed);
		bundle.put(SUMMON_CHARGE_TURNS, summonChargeTurns);
		bundle.put(WILD_DREAD_ID, wildDreadId);
		bundle.put(REVIVAL_PENDING, revivalPending);
		bundle.put(REVIVAL_TURNS, revivalTurns);
		bundle.put(STASIS_REPLACEMENT_PENDING, stasisReplacementPending);
		if (downedDread != null) {
			bundle.put(DOWNED_DREAD, downedDread);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		summonPending = bundle.getBoolean(SUMMON_PENDING);
		summonConsumed = bundle.getBoolean(SUMMON_CONSUMED);
		summonChargeTurns = Math.max(0, Math.min(SUMMON_CHARGE_REQUIRED,
				bundle.getInt(SUMMON_CHARGE_TURNS)));
		wildDreadId = bundle.getInt(WILD_DREAD_ID);
		revivalPending = bundle.getBoolean(REVIVAL_PENDING);
		revivalTurns = bundle.getInt(REVIVAL_TURNS);
		stasisReplacementPending = bundle.getBoolean(STASIS_REPLACEMENT_PENDING);
		if (bundle.contains(DOWNED_DREAD)) {
			downedDread = (WildDread) bundle.get(DOWNED_DREAD);
			wildDreadId = downedDread.id();
		}
		if (!summonPending && !stasisReplacementPending) {
			resetSummonCharge();
		}
	}
}
