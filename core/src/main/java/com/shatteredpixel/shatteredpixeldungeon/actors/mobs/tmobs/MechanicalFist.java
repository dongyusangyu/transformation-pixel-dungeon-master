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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.MechanicalFistSprite;
import com.watabou.utils.Random;

public class MechanicalFist extends Mob {

	public static final int KNOCKBACK_DISTANCE = 3;

	{
		spriteClass = MechanicalFistSprite.class;

		HP = HT = 250;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = null;
		lootChance = 0f;

		properties.add(Property.INORGANIC);
		properties.add(Property.LARGE);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(10, 35);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(30, 70);
	}

	@Override
	public float lootChance() {
		return 0f;
	}

	@Override
	public int attackProc(Char target, int damage, DamageTag... damageTags) {
		damage = processBaseAttackProc(target, damage, damageTags);
		knockBack(target);
		return damage;
	}

	protected int processBaseAttackProc(Char target, int damage, DamageTag... damageTags) {
		return super.attackProc(target, damage, damageTags);
	}

	protected int knockbackAim(Char target) {
		return target.pos + KNOCKBACK_DISTANCE * (target.pos - pos);
	}

	protected void knockBack(Char target) {
		if (target == null || Dungeon.level == null) {
			return;
		}
		Ballistica trajectory = new Ballistica(
				target.pos,
				knockbackAim(target),
				Ballistica.MAGIC_BOLT);
		throwTarget(
				target,
				trajectory,
				KNOCKBACK_DISTANCE,
				false,
				false);
		if (target == Dungeon.hero) {
			Dungeon.hero.interrupt();
		}
	}

	protected void throwTarget(
			Char target,
			Ballistica trajectory,
			int distance,
			boolean closeDoors,
			boolean collideDamage) {
		WandOfBlastWave.throwChar(
				target,
				trajectory,
				distance,
				closeDoors,
				collideDamage,
				this);
	}
}
