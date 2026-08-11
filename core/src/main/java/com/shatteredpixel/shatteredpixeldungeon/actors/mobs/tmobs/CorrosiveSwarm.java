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

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.CorrosiveSwarmSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;

public class CorrosiveSwarm extends Swarm {

	private static final float OOZE_DURATION = 2f;
	private static final int SPLIT_HP_THRESHOLD = 10;
	private static final float BASE_LOOT_CHANCE = 1f / 8f;
	private static final String SPLIT_DEPTH = "split_depth";

	private int splitDepth;

	{
		spriteClass = CorrosiveSwarmSprite.class;

		HP = HT = 200;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = Generator.Category.POTION;
		lootChance = BASE_LOOT_CHANCE;
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
		CorrosiveSwarm split = new CorrosiveSwarm();
		split.splitDepth = splitDepth + 1;
		return split;
	}

	@Override
	public float lootChance() {
		return adjustedLootChance(
				BASE_LOOT_CHANCE * (float) Math.pow(0.5f, splitDepth));
	}

	@Override
	public Item createLoot() {
		return createPotionLoot();
	}

	protected Item createPotionLoot() {
		return Generator.randomUsingDefaults(Generator.Category.POTION);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPLIT_DEPTH, splitDepth);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		splitDepth = Math.max(0, bundle.getInt(SPLIT_DEPTH));
	}

	@Override
	public void damage(int damage, Object source, DamageTag... damageTags) {
		if (!isAlive()) {
			return;
		}

		int receivedDamage = damage;
		if (DamageTag.of(damageTags).contains(DamageTag.OOZE)) {
			if (receivedDamage > 0) {
				tryToSplit(receivedDamage);
			}
			damage = 0;
		}

		BurstChain chain = burstChainFor(receivedDamage, source);
		if (chain != null) {
			chain.trigger(this);
		}

		if (damage == 0 && source instanceof BurstChain) {
			return;
		}
		super.damage(damage, source, damageTags);
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

			applyOoze(target);
			if (target instanceof CorrosiveSwarm) {
				target.damage(0, chain, DamageTag.PHYSICAL);
			}
		}
	}

	protected void applyOoze(Char target) {
		if (target == null || target.isImmune(Ooze.class)) {
			return;
		}

		Ooze ooze = target.buff(Ooze.class);
		if (ooze == null) {
			Ooze applied = Buff.affect(target, Ooze.class);
			if (applied != null) {
				applied.set(OOZE_DURATION);
			}
		} else {
			ooze.extend(OOZE_DURATION);
		}
	}

	static final class BurstChain {

		private final Set<CorrosiveSwarm> entered =
				Collections.newSetFromMap(new IdentityHashMap<>());
		private final Deque<CorrosiveSwarm> pending = new ArrayDeque<>();
		private boolean processing;

		boolean enter(CorrosiveSwarm swarm) {
			return entered.add(swarm);
		}

		void trigger(CorrosiveSwarm swarm) {
			if (!enter(swarm)) {
				return;
			}
			pending.addLast(swarm);
			if (processing) {
				return;
			}

			processing = true;
			try {
				while (!pending.isEmpty()) {
					pending.removeFirst().emitCorrosiveBurst(this);
				}
			} finally {
				processing = false;
			}
		}
	}
}
