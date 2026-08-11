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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.CorpseSprite;
import com.watabou.utils.Random;

public class Corpse extends Mob {

	private enum HealingDamage {
		INSTANCE
	}

	{
		spriteClass = CorpseSprite.class;

		HP = HT = 1;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = PotionOfToxicGas.class;
		lootChance = 1f / 8f;

		properties.add(Property.UNDEAD);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(10, 40);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return 0;
	}

	@Override
	public void damage(int damage, Object source, DamageTag... damageTags) {
		// Ordinary damage cannot harm a corpse.
	}

	@Override
	public int heal(int amount, boolean visual) {
		if (amount > 0 && isAlive()) {
			super.damage(amount, HealingDamage.INSTANCE,
					DamageTag.UNAVOIDABLE, DamageTag.NO_ARMOR);
		}
		return 0;
	}

}
