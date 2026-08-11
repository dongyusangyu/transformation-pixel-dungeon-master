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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.SoulCollectorSprite;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

public class SoulCollector extends Mob implements Char.DeathListener {

	private static final int SOUL_CHARGE_REQUIRED = 3;
	private static final String SOULS = "souls";

	private final ArrayList<SoulRecord> souls = new ArrayList<>();

	{
		spriteClass = SoulCollectorSprite.class;

		HP = HT = 200;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = PotionOfMindVision.class;
		lootChance = 1f / 8f;
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill(Char target) {
		return 0;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 20);
	}

	@Override
	protected boolean canAttack(Char target) {
		return false;
	}

	@Override
	protected Char chooseEnemy() {
		Char chosen = super.chooseEnemy();
		prepareToFlee(chosen);
		return chosen;
	}

	protected void prepareToFlee(Char target) {
		if (target != null) {
			enemy = target;
			this.target = target.pos;
			state = FLEEING;
		}
	}

	@Override
	public void onCharDied(Char deceased) {
		if (deceased == null || deceased == this || !isAlive()) {
			return;
		}
		if (canCollectSoulAt(deceased.pos)) {
			enqueueSoul(deceased.pos);
		}
	}

	protected boolean canCollectSoulAt(int cell) {
		if (Dungeon.level == null
				|| Actor.findById(id()) != this
				|| cell < 0
				|| cell >= Dungeon.level.length()) {
			return false;
		}
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView(this, fieldOfView);
		return fieldOfView[cell];
	}

	protected void enqueueSoul(int cell) {
		souls.add(new SoulRecord(cell, 0));
	}

	@Override
	protected boolean act() {
		if (state == SLEEPING || paralysed > 0) {
			return performBaseAct();
		}

		if (!souls.isEmpty()) {
			SoulRecord soul = souls.get(0);
			if (soul.charge < SOUL_CHARGE_REQUIRED) {
				soul.charge++;
				if (sprite instanceof SoulCollectorSprite) {
					((SoulCollectorSprite) sprite).channel(soul.cell);
				}
				spend(TICK);
				if (soul.charge == SOUL_CHARGE_REQUIRED) {
					tryRaiseSoul(soul);
				}
				return true;
			}
			if (tryRaiseSoul(soul)) {
				spend(TICK);
				return true;
			}
		}

		return performBaseAct();
	}

	protected boolean performBaseAct() {
		return super.act();
	}

	private boolean tryRaiseSoul(SoulRecord soul) {
		int cell = findReviveCell(soul.cell);
		if (cell == -1) {
			return false;
		}

		PowerfulWraith wraith = createPowerfulWraith();
		wraith.adjustStats(Dungeon.scalingDepth());
		wraith.pos = cell;
		wraith.alignment = alignment;
		wraith.state = wraith.HUNTING;
		addPowerfulWraithToLevel(wraith);
		int soulCell = soul.cell;
		souls.remove(0);
		if (sprite instanceof SoulCollectorSprite && !hasMarkedSoulAt(soulCell)) {
			((SoulCollectorSprite) sprite).finishSoul(soulCell);
		}
		return true;
	}

	private boolean hasMarkedSoulAt(int cell) {
		for (SoulRecord soul : souls) {
			if (soul.cell == cell) {
				return true;
			}
		}
		return false;
	}

	protected PowerfulWraith createPowerfulWraith() {
		return new PowerfulWraith();
	}

	protected int findReviveCell(int deathCell) {
		if (Dungeon.level == null) {
			return -1;
		}
		if (canRaiseAt(deathCell)) {
			return deathCell;
		}
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = deathCell + offset;
			if (canRaiseAt(cell)) {
				candidates.add(cell);
			}
		}
		return candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	private boolean canRaiseAt(int cell) {
		return cell >= 0
				&& cell < Dungeon.level.length()
				&& Dungeon.level.passable[cell]
				&& !Dungeon.level.pit[cell]
				&& Actor.findChar(cell) == null;
	}

	protected void addPowerfulWraithToLevel(PowerfulWraith wraith) {
		GameScene.add(wraith);
		Dungeon.level.occupyCell(wraith);
	}

	protected int soulCount() {
		return souls.size();
	}

	protected int soulCell(int index) {
		return souls.get(index).cell;
	}

	protected int soulCharge(int index) {
		return souls.get(index).charge;
	}

	public int[] markedSoulCells() {
		int[] result = new int[souls.size()];
		for (int i = 0; i < souls.size(); i++) {
			result[i] = souls.get(i).cell;
		}
		return result;
	}

	@Override
	public void destroy() {
		souls.clear();
		super.destroy();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SOULS, souls);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		souls.clear();
		Collection<Bundlable> restored = bundle.getCollection(SOULS);
		for (Bundlable value : restored) {
			if (!(value instanceof SoulRecord)) {
				continue;
			}
			SoulRecord soul = (SoulRecord) value;
			soul.charge = Math.max(0, Math.min(SOUL_CHARGE_REQUIRED, soul.charge));
			if (soul.cell >= 0 && (Dungeon.level == null || soul.cell < Dungeon.level.length())) {
				souls.add(soul);
			}
		}
	}

	public static class SoulRecord implements Bundlable {
		private static final String CELL = "cell";
		private static final String CHARGE = "charge";

		private int cell;
		private int charge;

		public SoulRecord() {
		}

		SoulRecord(int cell, int charge) {
			this.cell = cell;
			this.charge = charge;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put(CELL, cell);
			bundle.put(CHARGE, charge);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			cell = bundle.getInt(CELL);
			charge = bundle.getInt(CHARGE);
		}
	}
}
