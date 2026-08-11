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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.PowerfulWraithSprite;
import com.watabou.utils.Random;

public class PowerfulWraith extends Wraith {

	private static final int KNOCKBACK_DISTANCE = 1;

	{
		spriteClass = PowerfulWraithSprite.class;
		HP = HT = 100;
		EXP = 0;
		maxLvl = 30;
		loot = null;
		lootChance = 0f;
	}

	@Override
	protected boolean act() {
		if (isAlive() && HP < HT) {
			HP = Math.min(HT, HP + 10);
		}
		return performBaseAct();
	}

	protected boolean performBaseAct() {
		return super.act();
	}

	@Override
	public float speed() {
		return super.speed() * 2f;
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * 0.5f;
	}

	@Override
	public int attackProc(Char target, int damage, DamageTag... damageTags) {
		damage = processBaseAttackProc(target, damage, damageTags);
		if (isHeroTarget(target) && rollKnockback()) {
			knockBack(target, KNOCKBACK_DISTANCE);
		}
		return damage;
	}

	protected int processBaseAttackProc(Char target, int damage, DamageTag... damageTags) {
		return super.attackProc(target, damage, damageTags);
	}

	protected boolean isHeroTarget(Char target) {
		return target != null && target == Dungeon.hero;
	}

	protected boolean rollKnockback() {
		return Random.Int(3) == 0;
	}

	protected void knockBack(Char target, int distance) {
		if (target == null || Dungeon.level == null) {
			return;
		}
		int aim = target.pos + distance * (target.pos - pos);
		Ballistica trajectory = new Ballistica(target.pos, aim, Ballistica.MAGIC_BOLT);
		WandOfBlastWave.throwChar(target, trajectory, distance, false, false, this);
		if (target == Dungeon.hero) {
			Dungeon.hero.interrupt();
		}
	}

	@Override
	public float lootChance() {
		return 0f;
	}
}
