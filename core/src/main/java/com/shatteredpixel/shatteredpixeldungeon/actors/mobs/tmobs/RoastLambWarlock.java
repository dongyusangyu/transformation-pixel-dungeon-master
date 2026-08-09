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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MagicalRangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;

public class RoastLambWarlock extends Mob implements MagicalRangedAttack {

	private static final String FLOCKED_TARGET_IDS = "flocked_target_ids";
	private static final float SHEEP_LIFESPAN = 6f;

	private final HashSet<Integer> flockedTargetIds = new HashSet<>();

	{
		HP = HT = 150;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = null;
		lootChance = 0f;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(0, 20);
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
	public float lootChance() {
		return 0f;
	}

	protected boolean needsFlock(Char target) {
		return target != null && !flockedTargetIds.contains(target.id());
	}

	protected void markFlocked(Char target) {
		if (target != null) {
			flockedTargetIds.add(target.id());
		}
	}

	protected int performFlock(Char target) {
		if (target == null) {
			return 0;
		}
		markFlocked(target);
		int summoned = 0;
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = target.pos + offset;
			if (validSheepCell(cell)) {
				spawnSheep(cell, SHEEP_LIFESPAN);
				summoned++;
			}
		}
		playFlockSounds();
		return summoned;
	}

	private boolean validSheepCell(int cell) {
		return Dungeon.level != null
				&& Dungeon.level.insideMap(cell)
				&& !Dungeon.level.solid[cell]
				&& !Dungeon.level.pit[cell]
				&& Actor.findChar(cell) == null;
	}

	protected void spawnSheep(int cell, float lifespan) {
		Sheep sheep = new Sheep();
		sheep.initialize(lifespan);
		sheep.pos = cell;
		GameScene.add(sheep);
		Dungeon.level.occupyCell(sheep);
		CellEmitter.get(cell).burst(Speck.factory(Speck.WOOL), 4);
	}

	protected void playFlockSounds() {
		Sample.INSTANCE.play(Assets.Sounds.PUFF);
		Sample.INSTANCE.play(Assets.Sounds.SHEEP);
	}

	@Override
	protected boolean doAttack(Char target) {
		if (needsFlock(target)) {
			performFlock(target);
			spend(attackDelay());
			return true;
		}
		return super.doAttack(target);
	}

	@Override
	public boolean doRangedAttack(Char target) {
		return false;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		int[] ids = new int[flockedTargetIds.size()];
		int index = 0;
		for (int id : flockedTargetIds) {
			ids[index++] = id;
		}
		bundle.put(FLOCKED_TARGET_IDS, ids);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		flockedTargetIds.clear();
		if (bundle.contains(FLOCKED_TARGET_IDS)) {
			for (int id : bundle.getIntArray(FLOCKED_TARGET_IDS)) {
				flockedTargetIds.add(id);
			}
		}
	}
}
