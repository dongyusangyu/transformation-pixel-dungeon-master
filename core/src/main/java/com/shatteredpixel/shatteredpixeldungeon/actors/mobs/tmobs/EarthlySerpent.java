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
 * any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

public class EarthlySerpent extends Mob {

	private static final int BASE_HT = 240;
	private static final float BASE_ATTACK_DELAY = 0.5f;

	{
		HP = HT = BASE_HT;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = null;
		lootChance = 0f;

		immunities.add(CorrosiveGas.class);
		immunities.add(Corrosion.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(24, 36);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(8, 16);
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * BASE_ATTACK_DELAY;
	}

	@Override
	public float lootChance() {
		return 0f;
	}

	protected boolean canMeleeAttack(Char target) {
		if (Dungeon.level == null || target == null
				|| Dungeon.level.distance(pos, target.pos) > 2) {
			return false;
		}
		Ballistica path = new Ballistica(pos, target.pos, Ballistica.PROJECTILE);
		return path.collisionPos == target.pos;
	}
}
