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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MagicalRangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

public class RoastLambWarlock extends Mob implements MagicalRangedAttack {

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

	@Override
	public boolean doRangedAttack(Char target) {
		return false;
	}
}
