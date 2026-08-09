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
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MagicalRangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.RoastLambWarlockSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class RoastLambWarlock extends Mob implements MagicalRangedAttack {

	private static final String FLOCKED_TARGET_IDS = "flocked_target_ids";
	private static final float SHEEP_LIFESPAN = 6f;
	public static final int FIREBLAST_DISTANCE = 7;
	public static final int FIREBLAST_ANGLE = 70;
	private static final int FIRE_VOLUME = 3;
	private static final int CAST_NONE = 0;
	private static final int CAST_FLOCK = 1;
	private static final int CAST_FIREBLAST = 2;

	private final HashSet<Integer> flockedTargetIds = new HashSet<>();
	private int pendingCast = CAST_NONE;
	private Char pendingTarget;
	private int pendingTargetCell = -1;

	{
		spriteClass = RoastLambWarlockSprite.class;

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
		Invisibility.dispel(this);
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
			if (canAnimateCast(target)) {
				beginAnimatedCast(CAST_FLOCK, target);
				return false;
			} else {
				performFlock(target);
				spend(attackDelay());
				return true;
			}
		}
		return super.doAttack(target);
	}

	@Override
	public boolean doRangedAttack(Char target) {
		int cast = needsFlock(target) ? CAST_FLOCK : CAST_FIREBLAST;
		if (target != null && canAnimateCast(target)) {
			beginAnimatedCast(cast, target);
			return false;
		}
		if (cast == CAST_FLOCK) {
			performFlock(target);
		} else if (target != null) {
			castFireblast(target.pos);
		}
		spend(attackDelay());
		return true;
	}

	protected boolean canAnimateCast(Char target) {
		return target != null
				&& sprite instanceof RoastLambWarlockSprite
				&& (sprite.visible || target.sprite != null && target.sprite.visible);
	}

	protected void beginAnimatedCast(int cast, Char target) {
		pendingCast = cast;
		pendingTarget = target;
		pendingTargetCell = target.pos;
		playAnimatedCast(cast, target);
	}

	protected void playAnimatedCast(int cast, Char target) {
		RoastLambWarlockSprite warlockSprite = (RoastLambWarlockSprite) sprite;
		if (cast == CAST_FLOCK) {
			warlockSprite.flock(target.pos);
		} else {
			warlockSprite.fireblast(target.pos);
		}
	}

	public void onCastComplete() {
		int cast = pendingCast;
		Char target = pendingTarget;
		int targetCell = pendingTargetCell;
		pendingCast = CAST_NONE;
		pendingTarget = null;
		pendingTargetCell = -1;

		if (cast == CAST_FLOCK) {
			performFlock(target);
		} else if (cast == CAST_FIREBLAST) {
			castFireblast(targetCell);
		}
		if (cast != CAST_NONE) {
			spend(attackDelay());
			next();
		}
	}

	protected int fireblastDistance() {
		return FIREBLAST_DISTANCE;
	}

	protected int fireblastAngle() {
		return FIREBLAST_ANGLE;
	}

	protected int fireVolume() {
		return FIRE_VOLUME;
	}

	protected int magicDamageRoll() {
		return Random.NormalIntRange(15, 35);
	}

	protected void castFireblast(int targetCell) {
		Invisibility.dispel(this);
		ConeAOE cone = fireCone(targetCell);
		Ballistica aim = cone.coreRay;
		LinkedHashSet<Char> affectedChars = new LinkedHashSet<>();
		LinkedHashSet<Integer> adjacentCells = new LinkedHashSet<>();

		for (int cell : cone.cells) {
			if (cell == pos) {
				continue;
			}
			if (Dungeon.level.map[cell] == Terrain.DOOR) {
				Level.set(cell, Terrain.OPEN_DOOR);
				GameScene.updateMap(cell);
			}
			if (Dungeon.level.adjacent(pos, cell)
					&& !(Dungeon.level.flamable[cell] || Dungeon.level.solid[cell])) {
				adjacentCells.add(cell);
				if (Dungeon.level.heaps.get(cell) != null) {
					Dungeon.level.heaps.get(cell).burn();
				}
			} else {
				seedFire(cell);
			}

			Char affected = Actor.findChar(cell);
			if (affected != null && affected != this) {
				affectedChars.add(affected);
			}
		}

		if (cone.cells.isEmpty()) {
			adjacentCells.add(pos);
		}

		for (int cell : adjacentCells) {
			for (int offset : PathFinder.NEIGHBOURS8) {
				int candidate = cell + offset;
				if (Dungeon.level.insideMap(candidate)
						&& Dungeon.level.trueDistance(candidate, aim.collisionPos)
						< Dungeon.level.trueDistance(cell, aim.collisionPos)
						&& Dungeon.level.flamable[candidate]
						&& Fire.volumeAt(candidate, Fire.class) == 0) {
					seedFire(candidate);
				}
			}
		}

		for (Char affected : affectedChars) {
			applyFireblastTo(affected);
		}
		playFireblastSounds();
	}

	public ConeAOE fireCone(int targetCell) {
		Ballistica aim = new Ballistica(pos, targetCell, Ballistica.WONT_STOP);
		return new ConeAOE(aim, fireblastDistance(), fireblastAngle(),
				Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
	}

	protected void seedFire(int cell) {
		GameScene.add(Blob.seed(cell, fireVolume(), Fire.class));
	}

	protected void applyFireblastTo(Char target) {
		if (target == null || target == this) {
			return;
		}
		target.damage(magicDamageRoll(), this, DamageTag.MAGICAL);
		if (target.isAlive()) {
			Buff.affect(target, Burning.class).reignite(target);
			Buff.affect(target, Cripple.class, 4f);
		}
	}

	protected void playFireblastSounds() {
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
		Sample.INSTANCE.play(Assets.Sounds.BURNING);
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
