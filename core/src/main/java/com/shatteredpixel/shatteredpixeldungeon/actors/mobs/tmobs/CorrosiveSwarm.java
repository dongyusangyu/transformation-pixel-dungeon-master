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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.CorrosiveSwarmSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class CorrosiveSwarm extends Swarm {

	private static final float CORROSION_DURATION = 2f;
	private static final int CORROSION_DAMAGE = 1;
	private static final int SPLIT_HP_THRESHOLD = 10;

	{
		spriteClass = CorrosiveSwarmSprite.class;

		HP = HT = 200;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 26;

		loot = null;
		lootChance = 0f;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 25);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + armorRoll();
	}

	protected int armorRoll() {
		return Random.NormalIntRange(0, 10);
	}

	@Override
	protected boolean canSplit(int damage) {
		return HP > SPLIT_HP_THRESHOLD && super.canSplit(damage);
	}

	@Override
	protected Swarm createSplit() {
		return new CorrosiveSwarm();
	}

	@Override
	public float lootChance() {
		return 0f;
	}

	@Override
	public void damage(int damage, Object source) {
		if (!isAlive()) {
			return;
		}

		BurstChain chain = burstChainFor(damage, source);
		if (chain != null && chain.enter(this)) {
			emitCorrosiveBurst(chain);
		}

		if (damage == 0 && source instanceof BurstChain) {
			return;
		}
		super.damage(damage, source);
	}

	protected BurstChain burstChainFor(int damage, Object source) {
		if (source instanceof BurstChain) {
			return (BurstChain) source;
		}
		return damage > 0 ? new BurstChain() : null;
	}

	protected void emitCorrosiveBurst(BurstChain chain) {
		if (sprite instanceof CorrosiveSwarmSprite) {
			((CorrosiveSwarmSprite) sprite).burst();
		}
		if (sprite != null) {
			Splash.at(pos, 0x56255D, 8);
		}
		if (Dungeon.level == null || Dungeon.level.map == null) {
			return;
		}

		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = pos + offset;
			if (cell < 0 || cell >= Dungeon.level.map.length
					|| !Dungeon.level.adjacent(pos, cell)) {
				continue;
			}

			Char target = Actor.findChar(cell);
			if (target == null || target == this || !target.isAlive()) {
				continue;
			}

			applyCorrosion(target);
			if (target instanceof CorrosiveSwarm) {
				target.damage(0, chain);
			}
		}
	}

	protected void applyCorrosion(Char target) {
		if (target == null || target.isImmune(Corrosion.class)) {
			return;
		}

		Corrosion corrosion = target.buff(Corrosion.class);
		if (corrosion == null) {
			Corrosion applied = Buff.affect(target, Corrosion.class);
			if (applied != null) {
				applied.set(CORROSION_DURATION, CORROSION_DAMAGE, CorrosiveSwarm.class);
			}
		} else {
			corrosion.extend(CORROSION_DURATION);
		}
	}

	static final class BurstChain {

		private final Set<CorrosiveSwarm> entered =
				Collections.newSetFromMap(new IdentityHashMap<>());

		boolean enter(CorrosiveSwarm swarm) {
			return entered.add(swarm);
		}
	}
}
