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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.CamouflageGnollSprite;
import com.watabou.utils.Random;

public class CamouflageGnoll extends Mob {

	private static final int NORMAL_POISON_MIN_DURATION = 2;
	private static final int NORMAL_POISON_MAX_DURATION = 4;
	private static final int GRASS_POISON_MIN_DURATION = 10;
	private static final int GRASS_POISON_MAX_DURATION = 20;

	{
		spriteClass = CamouflageGnollSprite.class;

		HP = HT = 100;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = Gold.class;
		lootChance = 0.5f;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(20, 30);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int defenseSkill(Char enemy) {
		int evasion = super.defenseSkill(enemy);
		return isOnGrass() ? Math.round(evasion * 1.5f) : evasion;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + armorRoll();
	}

	@Override
	public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
		damage = super.attackProc(enemy, damage, damageTags);
		applyPoison(enemy, damage);
		return damage;
	}

	protected int armorRoll() {
		return Random.NormalIntRange(0, 10);
	}

	protected void applyPoison(Char enemy, int damage) {
		Poison poison = enemy.buff(Poison.class);
		int duration = poisonDuration();
		if (poison == null) {
			poison = Buff.affect(enemy, Poison.class);
			if (poison != null) {
				poison.set(duration);
			}
		} else {
			poison.extend(duration);
		}
	}

	public boolean isCamouflaged() {
		return isOnGrass();
	}

	protected int poisonDuration() {
		return isOnGrass()
				? Random.IntRange(GRASS_POISON_MIN_DURATION, GRASS_POISON_MAX_DURATION)
				: Random.IntRange(NORMAL_POISON_MIN_DURATION, NORMAL_POISON_MAX_DURATION);
	}

	protected boolean isOnGrass() {
		if (Dungeon.level == null || Dungeon.level.map == null
				|| pos < 0 || pos >= Dungeon.level.map.length) {
			return false;
		}
		int terrain = Dungeon.level.map[pos];
		return terrain == Terrain.GRASS
				|| terrain == Terrain.HIGH_GRASS
				|| terrain == Terrain.FURROWED_GRASS;
	}
}
