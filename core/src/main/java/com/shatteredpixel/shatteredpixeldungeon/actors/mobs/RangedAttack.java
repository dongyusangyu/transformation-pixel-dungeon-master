/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

/**
 * Shared protocol for attacks delivered at range. Implementations retain their
 * own damage and effects while targeting and shared hit reactions stay common.
 */
public interface RangedAttack {

	enum Type {
		MELEE,
		RANGED_PHYSICAL,
		RANGED_MAGIC
	}

	Type rangedAttackType();

	int rangedAttackBallisticaMode();

	default boolean canRangedAttack(Char target) {
		Mob attacker = rangedAttackMob();
		return target != null
				&& !Dungeon.level.adjacent(attacker.pos, target.pos)
				&& new Ballistica(attacker.pos, target.pos,
						rangedAttackBallisticaMode()).collisionPos == target.pos;
	}

	default boolean canMeleeAttack(Char target) {
		return true;
	}

	boolean doRangedAttack(Char target);

	default boolean rangedHit(Char target) {
		Mob attacker = rangedAttackMob();
		if (Char.hit(attacker, target, true)) {
			onRangedAttackHit(target);
			return true;
		}
		return false;
	}

	default void showRangedMiss(Char target) {
		if (target != null && target.sprite != null) {
			target.sprite.showStatus(CharSprite.NEUTRAL, target.defenseVerb());
		}
	}

	default void onRangedAttackHit(Char target) {
		Talent.onRangedAttackHit(rangedAttackMob(), target);
	}

	default Mob rangedAttackMob() {
		if (!(this instanceof Mob)) {
			throw new IllegalStateException("RangedAttack must be implemented by a Mob");
		}
		return (Mob) this;
	}
}
